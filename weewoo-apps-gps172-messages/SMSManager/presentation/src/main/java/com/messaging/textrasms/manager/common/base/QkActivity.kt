package com.messaging.textrasms.manager.common.base

import android.app.Activity
import android.app.ActivityManager
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.lifecycle.Lifecycle
import com.messaging.textrasms.manager.InterstitialConditionDisplay
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.interfaces.adShowCallBack
import com.messaging.textrasms.manager.common.interfaces.onAdfailedToLoadListner
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager.checkIsInterIsReady
import com.messaging.textrasms.manager.common.util.*
import com.messaging.textrasms.manager.extensions.Optional
import com.messaging.textrasms.manager.extensions.asObservable
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.feature.Activities.MainActivity
import com.messaging.textrasms.manager.feature.ads.AdvertiseHandler
import com.messaging.textrasms.manager.onShowInterstitialListener
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import com.messaging.textrasms.manager.util.PhoneNumberUtils
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.toolbar.toolbar
import kotlinx.android.synthetic.main.toolbar.toolbarTitle
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


abstract class QkActivity : AppCompatActivity(), onShowInterstitialListener {

    protected val menu: Subject<Menu> = BehaviorSubject.create()

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var conversationRepo: ConversationRepository

    @Inject
    lateinit var messageRepo: MessageRepository

    @Inject
    lateinit var phoneNumberUtils: PhoneNumberUtils

    @Inject
    lateinit var prefs: Preferences

    @Inject
    lateinit var utli: Utils
    

    private var idsNotTracker = mutableListOf<Int>(
        -1,
        R.id.drawerLayout,
        R.id.recyclerView1,
        R.id.back,
        R.id.tv_continue,
        R.id.cancel,
        R.id.toolbar,
        R.id.drawer,
        R.id.conversations,
        R.id.backcompose,
        R.id.messageList,
        R.id.body,
//        R.id.small_ad_headline,
//        R.id.small_ad_image,
        R.id.message,
        R.id.attach,
        R.id.send,
        R.id.next,
        R.id.next2,
        R.id.next5,
        R.id.setdefault,
        R.id.activity_welcome_view_next,
    )

    var advertiseHandler: AdvertiseHandler? = null

    /**
     * In case the activity should be themed for a specific conversation, the selected conversation
     * can be changed by pushing the threadId to this subject
     */
    val threadId: Subject<Long> = BehaviorSubject.createDefault(0)

    /**
     * Switch the theme if the threadId changes
     * Set it based on the latest message in the conversation
     */
    val theme: Observable<Colors.Theme> = threadId
        .distinctUntilChanged()
        .switchMap { threadId ->
            val conversation = conversationRepo.getConversation(threadId)
            when {
                conversation == null -> Observable.just(Optional(null))

                conversation.recipients.size == 1 -> Observable.just(Optional(conversation.recipients.first()))

                else -> messageRepo.getLastIncomingMessage(conversation.id)
                    .asObservable()
                    .mapNotNull { messages -> messages.firstOrNull() }
                    .distinctUntilChanged { message -> message.address }
                    .mapNotNull { message ->
                        conversation.recipients.find { recipient ->
                            phoneNumberUtils.compare(recipient.address, message.address)
                        }
                    }
                    .map { recipient -> Optional(recipient) }
                    .startWith(Optional(conversation.recipients.firstOrNull()))
                    .distinctUntilChanged()
            }
        }
        .switchMap { colors.themeObservable(it.value) }

    fun isDarkTheme(activity: Activity): Boolean {
        return activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppBaseTheme)
        if( Preferences.getBoolean(this,Preferences.IS_LANG_SELECTED)) {
            changelanguge(prefs.Language.get())
        }

        InterstitialConditionDisplay.getInstance().setOnShowInterstitialListener(this)
        advertiseHandler = AdvertiseHandler.getInstance(this)
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val typedValue = TypedValue()
            val theme: Resources.Theme = getTheme()
            theme.resolveAttribute(android.R.attr.statusBarColor, typedValue, true)
            window.navigationBarColor = typedValue.data
        }

        onNewIntent(intent)

        val triggers = listOf(
            prefs.nightMode,
            prefs.night,
            prefs.black,
            prefs.textSize,
            prefs.systemFont,
            prefs.Language
        )
        Observable.merge(triggers.map { it.asObservable().skip(1) })
            .debounce(400, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { recreate() }

        // We can only set light nav bar on API 27 in attrs, but we can do it in API 26 here
        /*if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            val night = !resolveThemeBoolean(R.attr.isLightTheme)
            window.decorView.systemUiVisibility = if (night) 0 else
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }*/

        // Set the color for the recent apps title
        val toolbarColor = resolveThemeColor(com.google.android.material.R.attr.colorPrimaryFixed)
        val icon = BitmapFactory.decodeResource(resources, R.drawable.messages_app_icon)
        val taskDesc =
            ActivityManager.TaskDescription(getString(R.string.in_app_name), icon, toolbarColor)
        setTaskDescription(taskDesc)
        //   val colorFilter1 = PorterDuffColorFilter(resolveThemeColor(android.R.attr.textColorPrimary), PorterDuff.Mode.MULTIPLY)
        // TypefaceUtil.setOverflowButtonColor(this, colorFilter1)
        if (advertiseHandler?.shouldShowDialogLoading == true) {
            advertiseHandler?.showProgress(QKApplication().currentActivity ?: this@QkActivity)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Set the color for the overflow and navigation icon
        val textSecondary = resolveThemeColor(android.R.attr.textColorSecondary)
        toolbar?.overflowIcon = toolbar?.overflowIcon?.apply { setTint(textSecondary) }

        // Update the colours of the menu items
        Observables.combineLatest(menu, theme) { menu, theme ->
            menu.iterator().forEach { menuItem ->
                val tint = when (menuItem.itemId) {
                    in getColoredMenuItems() -> theme.theme
                    else -> textSecondary
                }

                menuItem.icon = menuItem.icon?.apply { setTint(tint) }
            }
        }.autoDispose(scope(Lifecycle.Event.ON_DESTROY)).subscribe()
    }

    open fun getColoredMenuItems(): List<Int> {
        return listOf()
    }

    /**
     * This can be overridden in case an activity does not want to use the default themes
     */
    open fun getActivityThemeRes(black: Boolean) = when {
        black -> R.style.AppTheme_Black
        else -> R.style.AppTheme
    }

    override fun setContentView(layoutResID: Int) {

        super.setContentView(layoutResID)
        setSupportActionBar(toolbar)
        title = title // The titl
        val decorView = window.decorView
        decorView.viewTreeObserver.addOnGlobalLayoutListener { printClickedViews(window.decorView) }// e may have been set before layout inflation
    }

    override fun setTitle(titleId: Int) {
        title = getString(titleId)
    }

    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        toolbarTitle?.text = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        if (menu != null) {
            this.menu.onNext(menu)
        }
        return result
    }

    protected open fun showBackButton(show: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(show)
    }

    protected open fun showBackOrCancelButton(resID: Int) {
        supportActionBar?.setHomeAsUpIndicator(resID)
    }

    fun changelanguge(languageCode: Int) {
        val config = resources.configuration
        var Language = "es"

        Language = when (languageCode) {
            0 -> Resources.getSystem().configuration.locale.language
            1 -> "af"
            2 -> "sq"
            3 -> "ar"
            4 -> "az"
            5 -> "eu"
            6 -> "be"
            7 -> "bn"
            8 -> "bg"
            9 -> "ca"
            10 -> "CN"
            11 -> "TW"
            12 -> "hr"
            13 -> "cs"
            14 -> "da"
            15 -> "nl"
            16 -> "en"
            17 -> "eo"
            18 -> "et"
            19 -> "fil"
            20 -> "fi"
            21 -> "fr"
            22 -> "gl"
            23 -> "ka"
            24 -> "de"
            25 -> "el"
            26 -> "gu"
            27 -> "ht"
            28 -> "iw"
            29 -> "hi"
            30 -> "hu"
            31 -> "is"
            32 -> "id"
            33 -> "ga"
            34 -> "it"
            35 -> "ja"
            36 -> "ko"
            37 -> "kn"
            38 -> "la"
            39 -> "lv"
            40 -> "lt"
            41 -> "mk"
            42 -> "ms"
            43 -> "mt"
            44 -> "no"
            45 -> "fa"
            46 -> "pl"
            47 -> "pt"
            48 -> "ro"
            49 -> "ru"
            50 -> "sr"
            51 -> "sk"
            52 -> "sl"
            53 -> "es"
            54 -> "sw"
            55 -> "sv"
            56 -> "ta"
            57 -> "te"
            58 -> "th"
            59 -> "tr"
            60 -> "uk"
            61 -> "ur"
            62 -> "vi"
            63 -> "cy"
            64 -> "ji"
            else -> Resources.getSystem().configuration.locale.language
        }
        if (Language == "CN" || Language == "TW"){
            val locale = Locale("zh",Language)
            config.setLocale(locale)
        } else {
            val locale = Locale(Language)
            config.setLocale(locale)
        }
        AppUtils.updateConfig(this, config)
        //  recreate()

        this.baseContext.resources.updateConfiguration(
            config,
            this.baseContext.resources.displayMetrics
        )
    }

    fun getLAnguge(languageCode: Int): String {
        val config = resources.configuration
        var Language = "es"

        Language = when (languageCode) {
            0 -> Resources.getSystem().configuration.locale.language
            1 -> "ar"
            2 -> "bg"
            3 -> "bn"
            4 -> "cs"
            5 -> "de"
            6 -> "el"
            7 -> "en"
            8 -> "en-CA"
            9 -> "en-GB"
            10 -> "es"
            11 -> "fi"
            12 -> "fr"
            13 -> "gu"
            14 -> "hi"
            15 -> "hr"
            16 -> "hu"
            17 -> "in"
            18 -> "it"
            19 -> "ja"
            20 -> "ko"
            21 -> "nl"
            22 -> "nn"
            23 -> "pl"
            24 -> "pt-BR"
            25 -> "pt-PT"
            26 -> "ro"
            27 -> "ru"
            28 -> "sv"
            29 -> "tr"
            30 -> "uk"
            31 -> "vi"
            32 -> "zh"
            else -> Resources.getSystem().configuration.locale.language
        }
        return Language
    }

    private fun getSystemLocale(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return LocaleList.getDefault().get(0).language
        } else {
            return Locale.getDefault().language
        }
    }

    private fun printClickedViews(currentView: View?) {
        if (currentView == null) {
            return
        }
        if (currentView is ViewGroup) {
            val viewGroup: ViewGroup = currentView
            for (i in 0 until viewGroup.childCount) {
                printClickedViews(viewGroup.getChildAt(i))
            }
        }
        currentView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                Log.d("Click view id:","${v.id}")
                if (!idsNotTracker.contains(v.id) || ((this@QkActivity is MainActivity) && v.id == -1))  {
                    // dc tang len
//                    Log.d("Click name:","${v.id} -- ${getResources().getResourceEntryName(v.id)}")
//                    if ((this@QkActivity is BlockingActivity || this@QkActivity is SettingsActivity) && v.id == -1) return@setOnTouchListener false
                    InterstitialConditionDisplay.getInstance().increaseClicked()
                }
            }
            false
        }
    }

    private fun showInterstitialAd(adShown: (shown: Boolean) -> Unit) {


//        Toast.makeText(this,"Inter ads show Called",Toast.LENGTH_SHORT).show()
        if (!InterstitialConditionDisplay.getInstance().shouldShowInterstitialAd() || Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
            adShown(true)
            return
        }
//        if (MaxAdInter.isInterstitialReady()) {
        if (checkIsInterIsReady()) {
            MaxMainAdsManger.showInterstitial(this@QkActivity, object : adShowCallBack {
                override fun adShown(bol: Boolean?) {
                    InterstitialConditionDisplay.getInstance().resetClickedAmount()
                    adShown(true)
                }
            })
        } else {
            advertiseHandler?.shouldShowDialogLoading = true
            advertiseHandler?.showProgress(this@QkActivity)
            MaxMainAdsManger.initiateAd(this@QkActivity, object : onAdfailedToLoadListner {
                override fun onAdFailedToLoad() {
                    adShown(true)
                    advertiseHandler?.hideProgress(this@QkActivity)
                    advertiseHandler?.shouldShowDialogLoading = false
                }

                override fun onSuccess() {
                    advertiseHandler?.hideProgress(this@QkActivity)
                    advertiseHandler?.shouldShowDialogLoading = false
                    MaxMainAdsManger.showInterstitial(
                        this@QkActivity,
                        object : adShowCallBack {
                            override fun adShown(bol: Boolean?) {
                                InterstitialConditionDisplay.getInstance().resetClickedAmount()
                                adShown(true)

                            }
                        })
                }

                override fun adClose() {
                    advertiseHandler?.shouldShowDialogLoading = false
                    advertiseHandler?.hideProgress(this@QkActivity)
                }
            })

        }
    }

    override fun onPause() {
        super.onPause()
        advertiseHandler?.hideProgress(this@QkActivity)
    }

    override fun onShowAd() {
        showInterstitialAd {  }
    }
}