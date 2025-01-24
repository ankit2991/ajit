package com.messaging.textrasms.manager.feature.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.role.RoleManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.provider.Telephony
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.*
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.emoji.widget.EmojiAppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.calldorado.Calldorado
import com.calldorado.Calldorado.acceptConditions

import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics

import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.messaging.textrasms.manager.InterstitialConditionDisplay
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.calldorado.CalldoradoPermissions
import com.messaging.textrasms.manager.calldorado.OverlayPermissionManager
import com.messaging.textrasms.manager.common.androidxcompat.drawerOpen
import com.messaging.textrasms.manager.common.base.QkDialog
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.iap.IapConnector
import com.messaging.textrasms.manager.common.iap.SubscriptionServiceListener
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdListener
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.common.maxAdManager.OnAdShowCallback
import com.messaging.textrasms.manager.common.messgeData.ViewPagerAdapter
import com.messaging.textrasms.manager.common.util.*
import com.messaging.textrasms.manager.common.util.extensions.dismissKeyboard
import com.messaging.textrasms.manager.common.util.extensions.scrapViews
import com.messaging.textrasms.manager.common.util.extensions.setPadding
import com.messaging.textrasms.manager.common.util.extensions.setTint
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.Views.MainView
import com.messaging.textrasms.manager.feature.Views.NavItem
import com.messaging.textrasms.manager.feature.adapters.CustomAdListAdapter
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsAdapter
import com.messaging.textrasms.manager.feature.ads.AdvertiseHandler
import com.messaging.textrasms.manager.feature.blocking.BlockingDialog
import com.messaging.textrasms.manager.feature.compose.ComposeActivity.Companion.stickerlist
import com.messaging.textrasms.manager.feature.fragments.other_fragment
import com.messaging.textrasms.manager.feature.fragments.personal_fragment
import com.messaging.textrasms.manager.feature.fragments.spam_fragment
import com.messaging.textrasms.manager.feature.models.AdModel
import com.messaging.textrasms.manager.feature.settings.SettingsActivity
import com.messaging.textrasms.manager.feature.states.Archived
import com.messaging.textrasms.manager.feature.states.Inbox
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.feature.states.Searching
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.model.Sticker
import com.messaging.textrasms.manager.model.logDebug
import com.messaging.textrasms.manager.receiver.SmsReceiver
import com.messaging.textrasms.manager.repository.SyncRepository
import com.messaging.textrasms.manager.util.*
import com.messaging.textrasms.manager.util.Preferences.Companion.IS_RATE
import com.messaging.textrasms.manager.util.Preferences.Companion.LAST_OPEN
import com.messaging.textrasms.manager.util.Preferences.Companion.RATE_DIALOG_COUNT
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import games.moisoni.google_iab.BillingConnector
import games.moisoni.google_iab.BillingEventListener
import games.moisoni.google_iab.enums.ErrorType
import games.moisoni.google_iab.enums.ProductType
import games.moisoni.google_iab.models.BillingResponse
import games.moisoni.google_iab.models.ProductInfo
import games.moisoni.google_iab.models.PurchaseInfo
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.app_redirect_layout.*
import kotlinx.android.synthetic.main.drawer_view.*
import kotlinx.android.synthetic.main.layout_big_native_ad.maxAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.nativeAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.textViewRectangleNative
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.main_permission_hint.*
import kotlinx.android.synthetic.main.main_syncing.*
import kotlinx.android.synthetic.main.rating_dialog.*
import kotlinx.android.synthetic.main.views.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.solovyev.android.checkout.ActivityCheckout
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.Sku
import xyz.teamgravity.checkinternet.CheckInternet
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.ceil


class MainActivity : QkThemedActivity(), MainView, personal_fragment.Menuoptioninterface,
    other_fragment.Menuoptioninterfaceother, spam_fragment.Menuoptioninterfacespam
     {

    private val permissionRequestCodeCalldorado = 356
    private lateinit var iapConnector: IapConnector
    var  numberOfPaymentCardAfterOnBoard = 6L;
    private val permissionRequestCode = 358

    //  private val SKUS_IN_APP = Arrays.asList("com.messaging.textrasms.manager_49.99")
    var mFirebaseAnalytics: FirebaseAnalytics? = null
    val TAG: String = MainActivity::class.java.simpleName

    var overlayPermissionManager = OverlayPermissionManager(this)


//    private lateinit var purchases: Purchases

    override fun hassortingpersonal() {
        try {
            spamFragment.sorting()
            otherFragment.sorting()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun hassortingother() {
        try {
            personalFragment.sorting()
        } catch (e: Exception) {

        }
    }

    override fun hassortingspam() {
        try {
            personalFragment.sorting()
            otherFragment.sorting()
        } catch (e: Exception) {

        }

    }


    lateinit var mHandler: Handler
    lateinit var mHandler2: Handler

    @Inject
    lateinit var conversationsAdapter: ConversationsAdapter

    @Inject
    lateinit var blockingDialog: BlockingDialog

    @Inject
    lateinit var disposables: CompositeDisposable

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var drawerBadgesExperiment: DrawerBadgesExperiment

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var languageDialog: QkDialog

    @Inject
    lateinit var nightModeDialog: QkDialog

    @Inject
    lateinit var textSizeDialog: QkDialog

    @Inject
    lateinit var mmsSizeDialog: QkDialog

    val SKUS = Arrays.asList(
        "com.messaging.textrasms.manager_9.99",
        "com.messaging.textrasms.manager_29.99"
    )

    val SKUS_IN_APP = Arrays.asList("com.messaging.textrasms.manager_49.99")

    var mCheckout: ActivityCheckout? = null
    var mSkuDetailsList: Inventory.Products? = null
    var skuItemArrayList = java.util.ArrayList<Sku>()

    override val onNewIntentIntent: Subject<Intent> = PublishSubject.create()
    override val activityResumedIntent: Subject<Boolean> = PublishSubject.create()
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val composeIntent by lazy {
        compose.clicks()

    }

    override fun showInterAdsFromViewmodel() {

        if (getConnectionType(this) == 1 || getConnectionType(this) == 2 || getConnectionType(this) == 3) {
            if (!MaxMainAdsManger.isPurchased(this)) {
//                showInterstitialAd() {

                viewModel.moveCompose()
                compose.isClickable = true
//                }
            } else {
                viewModel.moveCompose()
                compose.isClickable = true
            }
        } else {
            viewModel.moveCompose()
            compose.isClickable = true
        }
    }

    override val drawerOpenIntent: Observable<Boolean> by lazy {
        drawerLayout.drawerOpen(Gravity.START)
            .doOnNext {
                dismissKeyboard()
                try {
                    clearSelection()

                } catch (e: java.lang.Exception) {

                }
            }
    }
    override val loadddata: Subject<Boolean> = PublishSubject.create()
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val navigationIntent: Observable<NavItem> by lazy {
        Observable.merge(listOf(
            backPressedSubject,
            inbox.clicks().map { NavItem.INBOX },
            archived.clicks().map { NavItem.ARCHIVED },
            transaction.clicks().map { NavItem.TRANSACTIONAL },
            promotion.clicks().map { NavItem.PROMOTIONS },
            spam.clicks().map { NavItem.SPAM },
            filter.clicks().map { NavItem.Filter },
            backup1.clicks().map { NavItem.BACKUP },
            scheduled.clicks().map { NavItem.SCHEDULED },
            blocking.clicks().map { NavItem.BLOCKING },
            calldorado.clicks().map { NavItem.CALLDORADO },
            settings.clicks().map { NavItem.SETTINGS },
            group.clicks().map { NavItem.GROUP },
            removeAds.clicks().map { NavItem.REMOVEADS },
            help.clicks().map { NavItem.HELP },
            benefits.clicks().map { NavItem.BENEFIT },
            price_layout.clicks().map { NavItem.PRICE },
            price_layout_en.clicks().map { NavItem.PRICE },
            share.clicks().map { NavItem.SHARE },
            rate_us.clicks().map { NavItem.RATEUS }
        ))
    }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()
    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()
    override fun nightModeSelected(): Observable<Int> = nightModeDialog.adapter.menuItemClicks
    override val modeclick by lazy { invite1.clicks() }
    override val languageclick by lazy { language.clicks() }


    private var frompersonalragment: Boolean = false
    private var fromspamragment: Boolean = false
    val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")

    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[MainViewModel::class.java]
    }
    private val toggle by lazy {
        ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.main_drawer_open_cd,
            0
        )
    }
    private val snackbar by lazy { findViewById<View>(R.id.snackbar) }
    private val syncing by lazy { findViewById<View>(R.id.syncing) }
    private val notification by lazy { findViewById<View>(R.id.notification) }
    private val set_default by lazy { findViewById<View>(R.id.set_default) }
    private val backPressedSubject: Subject<NavItem> = PublishSubject.create()
    private lateinit var animShow: Animation
    private lateinit var animHide: Animation
    private lateinit var pro1: ImageView
    private lateinit var pro2: ImageView

    @Inject
    lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>
    lateinit var motionLayout: MotionLayout
    lateinit var animation1: Animation
    lateinit var animation2: Animation
    lateinit var animation3: Animation
    lateinit var animation4: Animation
    lateinit var animation5: Animation
    lateinit var animation6: Animation
    lateinit var animationactivity: Animation
    lateinit var animationactivityout: Animation
    lateinit var custom_recyclerview: RecyclerView
    lateinit var customadapter: CustomAdListAdapter

    var next5BtnClick = false
    companion object {
        var calldoradoInitialized = false
        var datachsnged: Boolean = false
        var comesresume: Boolean = false
        var install1: Boolean = false
        var install2: Boolean = false
        var onfirst: Boolean = true
        var isvisible: Boolean = false

        var backuprun = false
        var ifdissmiss: Boolean = true

        var screenInches = 0.0
        var issync: Boolean = false
        var ifsettingsync = false
        lateinit var personalFragment: personal_fragment
        lateinit var otherFragment: other_fragment
        lateinit var spamFragment: spam_fragment
        var permissonallow: Boolean = false

        var frompersonal: Boolean = false
        var fromother: Boolean = false
        var fromspam: Boolean = false
        var sortingorder: Int = 1
        var asendesend: Int = 1
        private const val VOICE_TRANSCRIPTION_CAPABILITY_NAME = "voice_transcription"
        val itemList = ArrayList<AdModel>()
        const val VOICE_TRANSCRIPTION_MESSAGE_PATH = "/voice_transcription"
    }

    private val br_ScreenOffReceiver: SmsReceiver? = null

    private fun registerScreenOffReceiver() {
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(br_ScreenOffReceiver, filter)
    }

    override fun drawerevent(value: Boolean) {
        if (value) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else if (!drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }


    fun isDefaultSms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_SMS) == true
        } else {
            Telephony.Sms.getDefaultSmsPackage(this) == packageName
        }
    }

    private fun customTitleOnBoarding(string: String): String {
        val titleArr = string.split("\\s+".toRegex())
        val firstTitle = ceil(titleArr.size / 2.0)
        val firstString =
            titleArr.subList(0, firstTitle.toInt()).toString().replace(",", " ").substring(
                1,
                titleArr.subList(0, firstTitle.toInt()).toString().length - 1
            )
        val lastString =
            titleArr.subList(firstTitle.toInt(), titleArr.size).toString().replace(",", " ")
                .substring(
                    1,
                    titleArr.subList(firstTitle.toInt(), titleArr.size).toString().length - 1
                )
        return "<font color=#FF8A00>${
            firstString
        }<br> </font> <font color=#151D2A>${lastString}</font>"
    }

    private fun openPurchaseScreenInBoarding(onActivityOpened: () -> Unit) {
        Preferences.setBoolean(this, Preferences.HAS_DISPLAYED_FIRST_PAYMENT_CARD, false)
        startActivity(Intent(this, PurchaseActivity::class.java))
        Handler(Looper.getMainLooper()).postDelayed({
            onActivityOpened()
        }, 700)
    }

    private fun introduction() {

        val numberOfPaymentCardAfterOnBoard: Long = RemoteConfigHelper.get(RemoteConfigHelper.GPS172_PAYMENT_CARD_POSITION, 6L)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        val next = findViewById<View>(R.id.next)
        val title1: TextView = findViewById<TextView>(R.id.txt_title1)
        val tvTerms: TextView = findViewById(R.id.tvTerms)
        settingTermsPrivacy(tvTerms)
        title1.text = getSpannedText(customTitleOnBoarding(resources.getString(R.string.onboarding_step_1_title)))

//        tv_privacy_description.formatSpanColor(
//            getString(R.string.privacy_terms_description),
//            R.color.grayColor
//        )

        //next.isEnabled = false

        animation1 = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.right_to_left
        )

//        Handler().postDelayed(Runnable {
//            t1.setVisible(true)
//            t1.startAnimation(animation1)
//        }, 600)
        animation2 = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.right_to_left
        )
//        Handler().postDelayed(Runnable {
//            t2.setVisible(true)
//            t2.startAnimation(animation2)
//        }, 1200)
        animation3 = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.right_to_left
        )
//        Handler().postDelayed(Runnable {
//            t3.setVisible(true)
//            t3.startAnimation(animation3)
//        }, 1800)
        animation4 = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.right_to_left
        )
//        Handler().postDelayed(Runnable {
//            t4?.setVisible(true)
//            t4?.startAnimation(animation4)
//            next.isEnabled = true
//        }, 2400)

        animation5 = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.pull_out
        )
        animation6 = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.slide_in_bottom
        )

        val setdefault = findViewById<View>(R.id.setdefault)
        val cancel = findViewById<View>(R.id.canceldefault)


        val next2 = findViewById<View>(R.id.next2)
        val title2: TextView = findViewById<TextView>(R.id.txt_title2)
        title2.text = getSpannedText(customTitleOnBoarding(resources.getString(R.string.onboarding_step_2_title)))


        next.setOnClickListener(View.OnClickListener {
            firstScreen = true
            if(askedCdoPermissionFromOnbordingSlideOne)
            {
            if (numberOfPaymentCardAfterOnBoard == 1L) {
                openPurchaseScreenInBoarding(onActivityOpened = {
//                    skipping page 2 as client asked
                    motionLayout.setTransition(R.id.s1, R.id.s3)
                    motionLayout.transitionToEnd()
                    eulaAccepted()
                })
            }
            else
            {
//                    skipping page 2 as client asked
                motionLayout.setTransition(R.id.s1, R.id.s3)
                motionLayout.transitionToEnd()
                eulaAccepted()
            }}
            else
            {
                onBoardingSuccess()
                askedCdoPermissionFromOnbordingSlideOne= true
            }

        })


        next2.setOnClickListener(View.OnClickListener {
            if (numberOfPaymentCardAfterOnBoard == 2L) {
                openPurchaseScreenInBoarding(onActivityOpened = {
                    Preferences.setBoolean(this, Preferences.INTRODUCTION, true)
                    if (isDefaultSms()) {
                        if (prefs.version.get() >= 50) {
                            loadddata.onNext(true)
                            Preferences.setBoolean(this, Preferences.FirstLaunch, true)
                            logDebug("checkstate" + "loadtwotimes")

                            motionLayout.setTransition(R.id.s2, R.id.s3) //orange to blue transition
                            motionLayout.transitionToEnd()
                        }

                        //checkAndAskForBatteryOptimization()

                    } else {
                        motionLayout.setTransition(R.id.s2, R.id.s3) //orange to blue transition
                        motionLayout.transitionToEnd()
                    }
                })
            } else {
                Preferences.setBoolean(this, Preferences.INTRODUCTION, true)
                if (isDefaultSms()) {
                    if (prefs.version.get() >= 50) {

                        loadddata.onNext(true)
                        Preferences.setBoolean(this, Preferences.FirstLaunch, true)
                        logDebug("checkstate" + "loadtwotimes")

                        motionLayout.setTransition(R.id.s2, R.id.s3) //orange to blue transition
                        motionLayout.transitionToEnd()
                    }

                    //checkAndAskForBatteryOptimization()

                } else {
                    motionLayout.setTransition(R.id.s2, R.id.s3) //orange to blue transition
                    motionLayout.transitionToEnd()
                }
            }
        })

        val activity_welcome_view_next = findViewById<View>(R.id.activity_welcome_view_next)

        setdefault.setOnClickListener {
            Preferences.setBoolean(this, Preferences.INTRODUCTION, true)
            Preferences.setBoolean(this, Preferences.FirstLaunch, true)
            Preferences.setBoolean(this, Preferences.newversion, true)
            showDefaultSmsDialog()
        }

        cancel.setOnClickListener {
            Preferences.setBoolean(this, Preferences.INTRODUCTION, true)
            dissmissfragment()
            if (motionLayout != null) {
                if (numberOfPaymentCardAfterOnBoard == 3L) {
                    openPurchaseScreenInBoarding(onActivityOpened = {
                        motionLayout.setTransition(R.id.s3, R.id.s4)
                        motionLayout.transitionToEnd()
                    })
                } else {
                    motionLayout.setTransition(R.id.s3, R.id.s4)
                    motionLayout.transitionToEnd()
                }
            }
            //startActivity(Intent(this, PurchaseActivity::class.java))
        }

        activity_welcome_view_next.setOnClickListener {
            checkAndAskForBatteryOptimization()
        }

        val title3: TextView = findViewById<TextView>(R.id.txt_title3)
        title3.text =
            getSpannedText(customTitleOnBoarding(resources.getString(R.string.onboarding_step_3_title)))

        val title4: TextView = findViewById<TextView>(R.id.txt_title4)
        title4.text =
            getSpannedText(customTitleOnBoarding(resources.getString(R.string.onboarding_step_4_title)))


        val next5 = findViewById<View>(R.id.next5)

        val title5: TextView = findViewById<TextView>(R.id.txt_title5)

        title5.text =
            getSpannedText(customTitleOnBoarding(resources.getString(R.string.onboarding_step_5_title)))

        next5.setOnClickListener {
            val numberOfPaymentCardAfterOnBoard: Long = RemoteConfigHelper.get(
                RemoteConfigHelper.GPS172_PAYMENT_CARD_POSITION,
                6L
            )
            if (numberOfPaymentCardAfterOnBoard >= 5L) {
                openPurchaseScreenInBoarding(onActivityOpened = {})
            }
            if (!overlayPermissionManager.isGranted) {
                overlayPermissionManager.requestOverlay()
                updateForAfterCall()
            } else {
                startActivity(Intent(this,SelectLanguageActivity::class.java))
                checkCdoPermission()
                updateForAfterCall()

            }

            next5BtnClick = true
        }

    }

    private fun eulaAccepted() {
        val conditionsMap: HashMap<Calldorado.Condition, Boolean> = HashMap()
        conditionsMap[Calldorado.Condition.EULA] = true
        conditionsMap[Calldorado.Condition.PRIVACY_POLICY] = true
        acceptConditions(this, conditionsMap)
    }

    lateinit var loader1: ProgressBar
    private var isFirstLaunch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        initSubFromRemoteConfig()
        setContentView(R.layout.main_activity)
//        setCustomDrawerTextSizes()
        changeCustomDrawerTxt()
        Calldorado.start(this)



        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
//      val optionSource =  Calldorado.OptinSource.APP_OPEN
//        Log.e("calldorado_appopen",">>"+optionSource.name)

//        purchases = Purchases(this, { list ->
//            Preferences.setBoolean(this, Preferences.ADSREMOVED, list.isNotEmpty())
//        }, { purchase ->
//            Preferences.setBoolean(this, Preferences.ADSREMOVED, purchase != null)
//        }, { _, _ ->
//        })


        Log.w("MainActivity", "======================>Oncreate()")
        advertiseHandler = AdvertiseHandler.getInstance(this@MainActivity)
        pro1 = findViewById(R.id.pro1)
        pro2 = findViewById(R.id.pro2)
        custom_recyclerview = findViewById(R.id.recycler_view)
        motionLayout = findViewById(R.id.motion_container)
        viewModel.bindView(this)
        loader1 = findViewById(R.id.loader1)


        // MediationTestSuite.launch(this,getString(R.string.app_unit_id))
        animationactivity = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.enter
        )
        animationactivityout = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.exit
        )

        setupviewpager()
        setSupportActionBar(toolbar)
        mainactivitysetup()
        if (!Preferences.getBoolean(this, Preferences.INTRODUCTION)) {

            isFirstLaunch = true
            main_contain.setVisible(false)
            ifdissmiss = false
            motion_container.setVisible(true)
            introduction()

        } else {

//                onBoardingSuccess()

            checkAndAskForBatteryOptimization()
//            if (!overlayPermissionManager.isGranted) {
//                overlayPermissionManager.requestOverlay()
//            }

            checkCdoPermission()
        }
        val layoutManager = LinearLayoutManager(this)
        customadapter = CustomAdListAdapter(this, itemList)
        custom_recyclerview.layoutManager = layoutManager
        custom_recyclerview.adapter = customadapter

        findViewById<ImageView>(R.id.iv_pro).setOnClickListener {
            navigator.showRemoveAds()
        }
        updateDrawerItems()
    }


         private fun changeCustomDrawerTxt(){
             val config = resources.configuration
             val Language = config.locale.language
             if (Language != "en"){
                 tv_go_premium_en.visibility = View.GONE
                 tv_unlock_everything_en.visibility = View.GONE
                 price_layout_en.visibility = View.GONE

                 tv_go_premium.visibility = View.VISIBLE
                 tv_unlock_everything.visibility = View.VISIBLE
                 price_layout.visibility = View.VISIBLE

             }else{
                 tv_go_premium_en.visibility = View.VISIBLE
                 tv_unlock_everything_en.visibility = View.VISIBLE
                 price_layout_en.visibility = View.VISIBLE

                 tv_go_premium.visibility = View.GONE
                 tv_unlock_everything.visibility = View.GONE
                 price_layout.visibility = View.GONE
             }
         }
         private fun setCustomDrawerTextSizes() {
             val config = resources.configuration
             val Language = config.locale.language
             if (Language == "es" ||
                 Language == "ja" ||
                 Language == "nl" ||
                 Language == "sq" ||
                 Language == "ur" ||
                 Language == "tr" ||
                 Language == "pt" ||
                 Language == "fr" ||
                 Language == "ro" ||
                 Language == "sv" ||
                 Language == "ga" ||
                 Language == "it" ||
                 Language == "hi" ||
                 Language == "ar" ||
                 Language == "az" ||
                 Language == "af" ||
                 Language == "da" ||
                 Language == "no" ||
                 Language == "eo" ||
                 Language == "et" ||
                 Language == "eu" ||
                 Language == "fi" ||
                 Language == "sw" ||
                 Language == "sk" ||
                 Language == "sl" ||
                 Language == "bn" ||
                 Language == "ca" ||
                 Language == "gu" ||
                 Language == "is" ||
                 Language == "in" ||
                 Language == "ht" ||
                 Language == "ko" ||
                 Language == "la" ||
                 Language == "ms" ||
                 Language == "mt" ||
                 Language == "vi" ||
                 Language == "cy" ||
                 Language == "ji" ||
                 Language == "th"
                 )
             {
                 tv_go_premium.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._6sdp).toFloat()
                 tv_unlock_everything.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp).toFloat()
                 price_txt.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp).toFloat()
                 price_layout.setPadding(0,0,0,0)

             }
             if (
                 Language == "be" ||
                 Language == "bg" ||
                 Language == "hr" ||
                 Language == "fil" ||
                 Language == "de" ||
                 Language == "el" ||
                 Language == "lv" ||
                 Language == "lt" ||
                 Language == "mk" ||
                 Language == "pl" ||
                 Language == "sr" ||
                 Language == "te"
             ){
                 //reduce button text and heading text
                 tv_go_premium.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp).toFloat()
                 tv_unlock_everything.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp).toFloat()
                 price_txt.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp).toFloat()
                 price_layout.setPadding(0,0,0,0)
             }
             if (Language == "cs" ||
                 Language == "gl"
                 ){
                 //reduce heading text
                 tv_go_premium.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._4sdp).toFloat()
                 tv_unlock_everything.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp).toFloat()
                 price_txt.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp).toFloat()
                 price_layout.setPadding(0,0,0,0)
             }
             if (Language == "iw" ||
                 Language == "ka" ||
                 Language == "hu" ||
                 Language == "kn"){
                 //reduce sub heading text
                 tv_go_premium.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp).toFloat()
                 tv_unlock_everything.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp).toFloat()
                 price_txt.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp).toFloat()
                 price_layout.setPadding(0,0,0,0)
             }
             if (Language == "ru"){
                 //reduce sub heading text and button text
                 tv_go_premium.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._6sdp).toFloat()
                 tv_unlock_everything.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp).toFloat()
                 price_txt.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp).toFloat()
                 price_layout.setPadding(0,0,0,0)
             }
             if (Language == "sk"){
                 //reduce heading text
                 tv_go_premium.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp).toFloat()
                 tv_unlock_everything.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp).toFloat()
                 price_txt.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp).toFloat()
                 price_layout.setPadding(0,0,0,0)
             }
             if (Language == "ta"){
                 //reduce every thing
                 tv_go_premium.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp).toFloat()
                 tv_unlock_everything.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp).toFloat()
                 price_txt.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp).toFloat()
                 price_layout.setPadding(0,0,0,0)
             }
             if (Language == "uk"){
                 //reduce every thing
                 tv_go_premium.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._4sdp).toFloat()
                 tv_unlock_everything.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp).toFloat()
                 price_txt.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp).toFloat()
                 price_layout.setPadding(0,0,0,0)
             }
         }

         private fun callStep1() {

        if (overlayPermissionManager.isGranted){
            updateForAfterCall()
        } else {
            val numberOfPaymentCardAfterOnBoard: Long = RemoteConfigHelper.get(
                RemoteConfigHelper.GPS172_PAYMENT_CARD_POSITION,
                6L
            )
            if (numberOfPaymentCardAfterOnBoard == 3L) {
                openPurchaseScreenInBoarding(onActivityOpened = {
                    motionLayout.setTransition(R.id.s3, R.id.s5)
                })
            } else {
                motionLayout.setTransition(R.id.s3, R.id.s5)
            }

        }
        //orange to blue transition

    }


    override fun onRestart() {
        if(com.messaging.textrasms.manager.utils.Constants.IS_FROM_ACTIVITY) {
            com.messaging.textrasms.manager.utils.Constants.IS_FROM_ACTIVITY = false
            Log.w("MainActivity", "======================>onRestart()")
            if (MaxMainAdsManger.canShowInter) {
                compose.isClickable = true
            } else {
                compose.isClickable = true
            }
            if (!Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
                MaxAdManager.showInterAd(this, object : OnAdShowCallback {
                    override fun onAdHidden(ishow: Boolean) {
                        Log.w("MainActivity", "onAdHidden")
                    }

                    override fun onAdfailed() {
                        Log.w("MainActivity", "onAdfailed")
                    }

                    override fun onAdDisplay() {
                        Log.w("MainActivity", "onAdDisplay")
                    }

                })
            }
        }
//        showInterstitialAd()
        super.onRestart()
    }



    private fun updateForAfterCall() {
//        Toast.makeText(this,"Line 539",Toast.LENGTH_SHORT).show()
//        Log.e("Main Activity","LineNumber 540")

//        Old Banner Ads Integration
//        advertiseHandler!!.loadBannerAds(this, UtilsData.banner_ad_unit_id, relativeBanner, null)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        main_contain.startAnimation(animationactivityout)
        motion_container.setVisible(false)
        main_contain.setVisible(true)
        main_contain.startAnimation(animationactivity)
    }

    private fun mainactivitysetup() {

        dialog = Dialog(this, R.style.Theme_Dialog)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.inapp_dialog)

        if (isNetworkAvailable()) {
            LoardUrl(this)
        }

        when (Build.VERSION.SDK_INT >= 29) {
            true -> nightModeDialog.adapter.setData(R.array.night_modes)
            false -> nightModeDialog.adapter.data =
                this.resources.getStringArray(R.array.night_modes)
                    .mapIndexed { index, title ->
                        com.messaging.textrasms.manager.common.util.MenuItem(
                            title,
                            index
                        )
                    }
                    .drop(1)
        }
        languageDialog.adapter.setData(R.array.Language_name)

        toolbarSearch.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, Searchactivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in, R.anim.slideout)
        })


        val lastOpen = Preferences.getStringVal(this, LAST_OPEN)
        if (lastOpen != simpleDateFormat.format(Date())) {
            Preferences.setStringVal(this, LAST_OPEN, simpleDateFormat.format(Date()))
            Preferences.setIntVal(this, RATE_DIALOG_COUNT, 0)
        }



        onNewIntentIntent.onNext(intent)

        (snackbar as? ViewStub)?.setOnInflateListener { _, _ ->
            snackbarButton.clicks()
                .autoDispose(scope(Lifecycle.Event.ON_DESTROY))
                .subscribe(snackbarButtonIntent)

            notnow.setOnClickListener {
                snackbar.setVisible(false)
            }
        }


        (syncing as? ViewStub)?.setOnInflateListener { _, _ ->
        }
        (notification as? ViewStub)?.setOnInflateListener { _, _ ->
            val next = findViewById<TextView>(R.id.activity_welcome_view_next)
            next.setOnClickListener(View.OnClickListener {
                notification.setVisible(false)
                toolbar.setVisible(true)
                checkAndAskForBatteryOptimization()
            })
        }
        (set_default as? ViewStub)?.setOnInflateListener { _, _ ->
            val setdefault = findViewById<TextView>(R.id.setdefault)
            val canceldefault = findViewById<TextView>(R.id.canceldefault)
            val label = findViewById<TextView>(R.id.label)
            val sublabel = findViewById<TextView>(R.id.sublabel)

            label.setText(R.string.default_title)
            sublabel.text = this.getString(R.string.default_name)
            setdefault.setText(R.string.set_as_defaault)

            setdefault.setOnClickListener(View.OnClickListener {
                showDefaultSmsDialog()
            })

            canceldefault.setOnClickListener(View.OnClickListener {
                toolbar.setVisible(true)
                view_pager.setVisible(true)
                set_default.setVisible(false)
                //syncing.setVisible(false)
                snackbar.setVisible(true)
                snackbarTitle?.setText(R.string.main_default_sms_title)
                snackbarMessage?.setText(R.string.love_app_title)
                snackbarButton?.setText(R.string.main_default_sms_change)
                notnow?.setText(R.string.not_now_txt)
            })
        }

        (syncing as? ViewStub)?.setOnInflateListener { _, _ ->


        }
        toggle.syncState()
        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }


        // Don't allow clicks to pass through the drawer layout
        drawer?.clicks()!!.autoDispose(scope()).subscribe()

        back.setOnClickListener({ drawerclose() })
    }

    var isOptimizationSettingOpen = false
    var isOnCurrent = true

    private fun setupviewpager() {

        var firstFragmet: personal_fragment = personal_fragment.newInstance()
        var secondFragmet: other_fragment = other_fragment.newInstance()
        var thirdFragmet: spam_fragment = spam_fragment.newInstance()
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        val viewPager = findViewById<View>(R.id.view_pager) as ViewPager
        val adapter = ViewPagerAdapter(supportFragmentManager)
        //replaceFragment(0)
        adapter.addFragment(firstFragmet, "")
        adapter.addFragment(secondFragmet, "")
        adapter.addFragment(thirdFragmet, "")
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        val headerView = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.custom_tab, null, false)

        val linearLayoutOne = headerView.findViewById(R.id.ll) as LinearLayout
        val linearLayout2 = headerView.findViewById(R.id.ll2) as LinearLayout
        val linearLayout3 = headerView.findViewById(R.id.ll3) as LinearLayout
        val person_img = headerView.findViewById(R.id.person_img) as ImageView
        val other_img = headerView.findViewById(R.id.other_img) as ImageView
        val spam_img = headerView.findViewById(R.id.spam_img) as ImageView
        val tvtab1 = headerView.findViewById(R.id.tvtab1) as TextView
        val tvtab2 = headerView.findViewById(R.id.tvtab2) as TextView
        val tvtab3 = headerView.findViewById(R.id.tvtab3) as TextView
        tvtab1.setTextColor(Color.parseColor("#0074FE"))
        tabLayout!!.getTabAt(0)!!.customView = linearLayoutOne
        tabLayout.getTabAt(1)!!.customView = linearLayout2
        tabLayout.getTabAt(2)!!.customView = linearLayout3

        tvtab2.setTextColor(resolveThemeColor(R.attr.tabcolor))
        tvtab3.setTextColor(resolveThemeColor(R.attr.tabcolor))
        tabLayout.setSelectedTabIndicatorColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.tools_theme
            )
        )

        viewPager.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(tabLayout)
        )
        tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        person_img.colorFilter = null
                        person_img.setImageResource(R.drawable.allmessage_sel)
                        tvtab1.setTextColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.tools_theme
                            )
                        )
                        tvtab1.setVisible(true)
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.tools_theme
                            )
                        )

                    }
                    1 -> {
                        other_img.colorFilter = null
                        other_img.setImageResource(R.drawable.known_sel)
                        tvtab2.setTextColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.tools_theme
                            )
                        )
                        tvtab2.setVisible(true)
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.tools_theme
                            )
                        )
                    }
                    2 -> {
                        spam_img.colorFilter = null
                        spam_img.setImageResource(R.drawable.unknwon_sel)
                        tvtab3.setTextColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.tools_theme
                            )
                        )
                        tvtab3.setVisible(true)
                        tabLayout.setSelectedTabIndicatorColor(
                            ContextCompat.getColor(
                                applicationContext,
                                R.color.tools_theme
                            )
                        )
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        person_img.setImageResource(R.drawable.allmessage_unsel)
                        tvtab1.setTextColor(resolveThemeColor(R.attr.tabcolor))
                        if (toolbarTitle.isVisible) {
                            try {
                                clearSelection()
                            } catch (e: Exception) {

                            }
                        }
                    }
                    1 -> {
                        other_img.setImageResource(R.drawable.known_unsel)
                        tvtab2.setTextColor(resolveThemeColor(R.attr.tabcolor))
                        if (toolbarTitle.isVisible) {
                            try {
                                clearSelection()
                            } catch (e: Exception) {

                            }

                        }
                    }
                    2 -> {
                        spam_img.setImageResource(R.drawable.unknwon_unsel)
                        tvtab3.setTextColor(resolveThemeColor(R.attr.tabcolor))
                        if (toolbarTitle.isVisible) {
                            try {
                                clearSelection()
                            } catch (e: Exception) {

                            }
                        }
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })
    }

    fun showDefaultSmsDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java) as RoleManager
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            startActivityForResult(intent, 42389)
        } else {
            try {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                startActivityForResult(intent, 42389)
            } catch (e: Exception) {

            }
        }
    }


    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is personal_fragment) {
            personalFragment = fragment
            fragment.setOnTextClickedListener(this)
        }
        if (fragment is other_fragment) {
            fragment.setOnTextClickedListener(this)
            otherFragment = fragment
        }

        if (fragment is spam_fragment) {
            fragment.setOnTextClickedListener(this)
            spamFragment = fragment
        }
    }

    override fun drawerclose() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun showLanguage() {
        ifdissmiss = false
        languageDialog.show(activity!!)
    }

    override fun LanguageClicked(): Observable<Int> = languageDialog.adapter.menuItemClicks


    override
    val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }


    override fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.run(onNewIntentIntent::onNext)
        if (intent.action?.equals(Constants.ACTION_SHOULD_DISPLAY_PAYMENT) == true) {
            val numberOfPaymentCardAfterOnBoard: Long = RemoteConfigHelper.get(
                RemoteConfigHelper.GPS172_PAYMENT_CARD_POSITION,
                6L
            )
            if (numberOfPaymentCardAfterOnBoard >= 5L) {
                openPurchaseScreenInBoarding(onActivityOpened = {})
            }
        }
    }


    var c = 0
    override fun render(state: MainState) {

        if (state.hasError) {
            finish()
            return
        }

        if (state.nightModeId.equals(1)) {
            invitetext.setText(R.string.DarkMode)
            inviteicon.setImageResource(R.drawable.night_icon)
            invite.setColorFilter(ContextCompat.getColor(applicationContext, R.color.white))
            if (isPurchased()) {
                invite.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.tools_theme
                    )
                )
                back.setTint(resolveThemeColor(android.R.attr.textColorPrimary))
            } else {
                invite.setColorFilter(ContextCompat.getColor(applicationContext, R.color.white))
                back.setTint(ContextCompat.getColor(applicationContext, R.color.white))

            }
        } else if (state.nightModeId.equals(0)) {
            if (isPurchased()) {
                invite?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.tools_theme
                    )
                )
                back?.setTint(resolveThemeColor(android.R.attr.textColorPrimary))
            } else {
                invite?.setTint(ContextCompat.getColor(applicationContext, R.color.white))
                back?.setTint(resolveThemeColor(android.R.attr.textColorPrimary))

            }
        } else {
            inviteicon?.setImageResource(R.drawable.light_icon)
            inviteicon?.setTint(Color.parseColor("#BFF4F4F4"))
            if (isPurchased()) {
                invite?.setColorFilter(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.tools_theme
                    )
                )
                back?.setTint(resolveThemeColor(android.R.attr.textColorPrimary))

            } else {
                invite?.setTint(ContextCompat.getColor(applicationContext, R.color.white))
                back?.setTint(resolveThemeColor(android.R.attr.textColorPrimary))
            }
        }

        when (Build.VERSION.SDK_INT >= 29) {
            true -> nightModeDialog.adapter.setData(R.array.night_modes)
            false -> nightModeDialog.adapter.data =
                this.resources.getStringArray(R.array.night_modes)
                    .mapIndexed { index, title ->
                        com.messaging.textrasms.manager.common.util.MenuItem(
                            title,
                            index
                        )
                    }
                    .drop(1)
        }
        nightModeDialog.adapter.selectedItem = state.nightModeId

        languageDialog.adapter.selectedItem = state.LanguageId

        toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0 || state.page is Searching)
        toolbarTitle.setTextColor(resolveThemeColor(android.R.attr.textColorPrimary))
        toolbarTitle?.setVisible(toolbarSearch?.visibility != View.VISIBLE)

        when (state.page) {

            is Inbox -> {

            }

            is Searching -> {
                showBackButton(true)
            }

            is Archived -> {
                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> ""
                }
            }
        }

        inbox.isActivated = state.page is Inbox
        archived.isActivated = state.page is Archived

        when {
            !state.defaultSms -> {
                snackbarTitle?.setText(R.string.main_default_sms_title)
                snackbarMessage?.setText(R.string.love_app_title)
                snackbarButton?.setText(R.string.main_default_sms_change)
                notnow?.setText(R.string.not_now_txt)
            }

            !state.smsPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_sms)
                snackbarButton?.setText(R.string.main_permission_allow)
                notnow?.setText(R.string.not_now_txt)
            }

            !state.contactPermission -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_contacts)
                snackbarButton?.setText(R.string.main_permission_allow)
                notnow?.setText(R.string.not_now_txt)

            }
            !state.writeexternal -> {
                snackbarTitle?.setText(R.string.main_permission_required)
                snackbarMessage?.setText(R.string.main_permission_allow_read)
                snackbarButton?.setText(R.string.main_permission_allow)
                notnow?.setText(R.string.not_now_txt)
            }

        }
        //  }
        when (state.syncing) {
            is SyncRepository.SyncProgress.Idle -> {

                if (Preferences.getBoolean(this, "syncing")) {
                    set_default.setVisible(false)
                    syncing.setVisible(false)

                    Preferences.setBoolean(this, Preferences.FirstLaunch, true)
                    Preferences.setBoolean(this, Preferences.newversion, true)
                    Preferences.setBoolean(this, Preferences.NewVERSION, true)
                    logDebug("checkstate" + "stable")

                    ifdissmiss = true

                    Preferences.setBoolean(this, Preferences.SYNCING, false)

                    ifsettingsync = false
                    backuprun = false

                }
                if (!Preferences.getBoolean(this, "FirstLaunch1")) {
                    ifdissmiss = false
                    Preferences.setIntVal(applicationContext, "which", 3)

                    if (!state.defaultSms) {
                        Preferences.setBoolean(this, Preferences.NewVERSION, true)
                        if (!ifsettingsync) {
                            logDebug("checkstate" + "first")


                            set_default.setVisible(false)
                            ifdissmiss = false

                        }

                    } else {
                        set_default.setVisible(false)
                        ifdissmiss = true

                        if (!Preferences.getBoolean(this, "newversion")) {
                            logDebug("checkstate" + "load")
                            Preferences.setBoolean(this, Preferences.FirstLaunch, true)
                        }
                    }


                } else {
                    if (ifdissmiss) {
                        if (!Preferences.getBoolean(this, "NewVERSION")) {
                            loadddata.onNext(true)
                            Preferences.setBoolean(this, Preferences.NewVERSION, true)
                            logDebug("checkstate" + "newversion")
                        }
                        logDebug("checkstate" + "dismiss")
                        set_default?.setVisible(false)
                        ifsettingsync = false
                        snackbar.isVisible =
                            !state.defaultSms || !state.smsPermission || !state.contactPermission
                    }

                    set_default.setVisible(false)
                    snackbar.isVisible =
                        !state.defaultSms || !state.smsPermission || !state.contactPermission
                    ifdissmiss = true

                    logDebug("checkstate" + "else")

                    if (!permissonallow && ifdissmiss) {
                        permissonallow = true
                    }
                }

                snackbar.isVisible =
                    !state.defaultSms || !state.smsPermission || !state.contactPermission
                snackbarTitle?.setText(R.string.main_default_sms_title)
                snackbarMessage?.setText(R.string.love_app_title)
                snackbarButton?.setText(R.string.main_default_sms_change)
                notnow?.setText(R.string.not_now_txt)
                try {
                    loader1.setVisible(false)
                } catch (e: java.lang.Exception) {

                }

            }

            is SyncRepository.SyncProgress.Running -> {
                try {
                    syncing.setVisible(!state.syncing.indeterminate)
                    loader1.setVisible(state.syncing.indeterminate)
                } catch (E: Exception) {

                }

                tryOrNull {
                    try {
                        syncingProgressmain?.progress = state.syncing.progress
                    } catch (e: Exception) {

                    }

                }
                snackbar?.isVisible =
                    !state.defaultSms || !state.smsPermission || !state.contactPermission


                ifdissmiss = false
                logDebug("checkstate" + "countrunning" + state.syncing.progress)
                ifsettingsync = true
                set_default.setVisible(false)
                logDebug("progress" + state.syncing.progress)

                Preferences.setBoolean(this, Preferences.SYNCING, true)
                set_default.setVisible(false)
                logDebug("message" + state.smsPermission)

                ifdissmiss = false
                logDebug("checkstate" + "countrunning" + state.syncing.progress)

            }

            else -> {}
        }


    }

    override fun sendstatespam(state: MainState) {
        fromspamragment = true
        frompersonalragment = false


        val addContact = when (state.page) {
            is Inbox -> state.page.addContact
            is Archived -> state.page.addContact
            else -> false
        }

        val markPinned = when (state.page) {
            is Inbox -> state.page.markPinned
            is Archived -> state.page.markPinned
            else -> true
        }

        val markRead = when (state.page) {
            is Inbox -> state.page.markRead
            is Archived -> state.page.markRead
            else -> true
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }

        toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0 || state.page is Searching)
        toolbarTitle.setVisible(toolbarSearch.visibility != View.VISIBLE)
        if (state.page is Inbox)
            title = when (state.page.selected != 0) {
                true -> getString(R.string.main_title_selected, state.page.selected)
                false -> ""
            }

        toolbar.menu.findItem(R.id.archive)?.isVisible =
            state.page is Inbox && selectedConversations != 0
        toolbar.menu.findItem(R.id.unarchive)?.isVisible =
            state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.add)?.isVisible =
            addContact && selectedConversations != 0 && state.page is Inbox && state.page.selected != state.page.data!!.size

        toolbar.menu.findItem(R.id.deselectAll)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.pin)?.isVisible =
            markPinned && selectedConversations != 0 && state.page is Inbox && state.page.selected != state.page.data!!.size

        toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        //toolbar.menu.findItem(R.id.selectAll)?.isVisible = selectedConversations != 0 && selectedConversations != conversationsAdapter.itemCount
        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0
        // toolbar.menu.findItem(R.id.info)?.isVisible = state.page is Inbox && state.page.selected == 0
        toolbar.menu.findItem(R.id.Rate)?.isVisible = selectedConversations == 0
        toolbar.menu.findItem(R.id.selectAllmenu)?.isVisible = selectedConversations == 0
        // toolbar.menu.findItem(R.id.setting)?.isVisible = selectedConversations == 0
        var order = Preferences.getIntVal(applicationContext, "which")

        if (state.page is Inbox && state.page.data != null)
            toolbar.menu.findItem(R.id.selectAll)?.isVisible =
                order.equals(3) && state.page is Inbox && state.page.selected != state.page.data.size

        toolbar.menu.findItem(R.id.sort)?.isVisible = selectedConversations == 0
        //  toolbar.menu.findItem(R.id.remove_ad)?.isVisible = Preferences.getIntVal(this, Preferences.Coinsvalue, 0) < 100

    }

    override fun sendstateother(state: MainState) {
        frompersonalragment = false
        fromspamragment = false

        val markPinned = when (state.page) {
            is Inbox -> state.page.markPinned
            is Archived -> state.page.markPinned
            else -> true
        }

        val markRead = when (state.page) {
            is Inbox -> state.page.markRead
            is Archived -> state.page.markRead
            else -> true
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }
        toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0 || state.page is Searching)
        toolbarTitle.setVisible(toolbarSearch.visibility != View.VISIBLE)
        if (state.page is Inbox)
            title = when (state.page.selected != 0) {
                true -> getString(R.string.main_title_selected, state.page.selected)
                false -> ""
            }

        toolbar.menu.findItem(R.id.archive)?.isVisible =
            state.page is Inbox && selectedConversations != 0
        toolbar.menu.findItem(R.id.unarchive)?.isVisible =
            state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.add)?.isVisible = false
        toolbar.menu.findItem(R.id.pin)?.isVisible =
            markPinned && selectedConversations != 0 && state.page is Inbox && state.page.selected != state.page.data!!.size

        toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible =
            markRead && selectedConversations != 0 || state.page is Inbox && state.page.selected == state.page.data!!.size
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0

        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.Rate)?.isVisible = selectedConversations == 0
        toolbar.menu.findItem(R.id.selectAllmenu)?.isVisible = selectedConversations == 0

        var order = Preferences.getIntVal(applicationContext, "which")
        if (state.page is Inbox && state.page.data != null) {
            toolbar.menu.findItem(R.id.selectAll)?.isVisible =
                order.equals(3) && state.page is Inbox && state.page.selected != state.page.data.size
        }
        toolbar.menu.findItem(R.id.deselectAll)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.sort)?.isVisible = selectedConversations == 0

    }

    override fun sendstate(state: MainState) {
        frompersonalragment = true
        fromspamragment = false

        val addContact = when (state.page) {
            is Inbox -> state.page.addContact
            is Archived -> state.page.addContact
            else -> false
        }

        val markPinned = when (state.page) {
            is Inbox -> state.page.markPinned
            is Archived -> state.page.markPinned
            else -> true
        }

        val markRead = when (state.page) {
            is Inbox -> state.page.markRead
            is Archived -> state.page.markRead
            else -> true
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }
        frompersonalragment = true

        toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0 || state.page is Searching)
        toolbarTitle.setVisible(toolbarSearch.visibility != View.VISIBLE)
        if (state.page is Inbox)
            title = when (state.page.selected != 0) {
                true -> getString(R.string.main_title_selected, state.page.selected)
                false -> ""
            }
        toolbar.menu.findItem(R.id.archive)?.isVisible =
            state.page is Inbox && selectedConversations != 0
        toolbar.menu.findItem(R.id.unarchive)?.isVisible =
            state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.add)?.isVisible =
            addContact && selectedConversations != 0 && state.page is Inbox && state.page.selected != state.page.data!!.size
        toolbar.menu.findItem(R.id.pin)?.isVisible =
            markPinned && selectedConversations != 0 && state.page is Inbox && state.page.selected != state.page.data!!.size

        toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible =
            markRead && selectedConversations != 0 || state.page is Inbox && state.page.selected == state.page.data!!.size
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.info)?.isVisible =
            state.page is Inbox && state.page.selected == 0
        toolbar.menu.findItem(R.id.Rate)?.isVisible = selectedConversations == 0
        toolbar.menu.findItem(R.id.selectAllmenu)?.isVisible = selectedConversations == 0
        var order = Preferences.getIntVal(applicationContext, "which")
        if (state.page is Inbox && state.page.data != null) {
            toolbar.menu.findItem(R.id.selectAll)?.isVisible =
                order.equals(3) && state.page is Inbox && state.page.selected != state.page.data.size
        }
        toolbar.menu.findItem(R.id.sort)?.isVisible = selectedConversations == 0
        toolbar.menu.findItem(R.id.deselectAll)?.isVisible = selectedConversations != 0

    }

    private fun delayedPurchaseMimic() {

        Handler(Looper.getMainLooper()).postDelayed({
            updatePrefBoolean(true)
        }, 6000)
    }


    override fun onResume() {
        super.onResume()
        showNativeAd()
//        delayedPurchaseMimic()
        Log.e("tag", "isPurchase>>" + MaxMainAdsManger.isPurchased(this))

        Log.w("MainActivity", "======================>onResume()")




        if (CustomAdLayoutActivity.fromcustom) {
            CustomAdLayoutActivity.fromcustom = false
            recreate()
        }

        activityResumedIntent.onNext(true)

        if (isPurchased()) {
            if (itemList.size > 0) {
                itemList.clear()
            }

            if (customadapter != null && customadapter.itemCount > 0) {
                customadapter.updateList(itemList)
            }
        }

        if (!calldoradoInitialized) {
            calldoradoInitialized = true
            val conditions = Calldorado.getAcceptedConditions(this)
            if (conditions.containsKey(Calldorado.Condition.EULA)) {
                if (conditions[Calldorado.Condition.EULA] == null) {
                    val conMap = HashMap<Calldorado.Condition, Boolean>()
                    conMap[Calldorado.Condition.EULA] = true
                    conMap[Calldorado.Condition.PRIVACY_POLICY] = true
                    Calldorado.acceptConditions(this, conMap)
                } else {

//                    if(!Preferences.getBoolean(this, Preferences.FirstLaunch)){
//                    onBoardingSuccess()
//                    }
                }
            } else {
//                if(!Preferences.getBoolean(this, Preferences.FirstLaunch)){
//                onBoardingSuccess()
//                }
            }

        }
        setupInApps()
        initBilling()
        if (!com.messaging.textrasms.manager.utils.Constants.IS_FROM_SETTING || isPurchased()){
            updateDrawerItems()
        }

        if(next5BtnClick){
            next5BtnClick = false
            startActivity(Intent(this@MainActivity,SelectLanguageActivity::class.java))
            checkCdoPermission()
        }

        if(Preferences.getBoolean(this, Preferences.INTRODUCTION)){
            if (CalldoradoPermissions.isReadPhoneStatePermissionGranted(this@MainActivity) && overlayPermissionManager.isGranted) {
                Preferences.setBoolean(this,Preferences.ISCONSENT,true)

                checkCdoPermission()
            }
        }


    }

    fun checkCdoPermission() {
       var permission_layout = findViewById<RelativeLayout>(R.id.permission_layout_new)
        var  home_layout = findViewById<RelativeLayout>(R.id.home_layout)
        var btn_continue = findViewById<Button>(R.id.permission_btn)
        var terms_policy = findViewById<EmojiAppCompatTextView>(R.id.privacy_policy)
        CalldoradoPermissions.checkPermissions(
            this@MainActivity,
            permission_layout,
            home_layout,
            btn_continue,
            overlayPermissionManager,
            terms_policy
        )
    }


    private var askedCdoPermissionFromOnbordingSlideOne = false
    private var firstScreen = false
    fun onBoardingSuccess() {

        requestCdoPermissions()
    }

    fun requestCdoPermissions() {
        val permissionList = mutableListOf<String>()
        permissionList.add(Manifest.permission.READ_PHONE_STATE)
        permissionList.add(Manifest.permission.CALL_PHONE)
        permissionList.add(Manifest.permission.ANSWER_PHONE_CALLS)

        ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), permissionRequestCodeCalldorado)
    }

    override fun onPause() {
        Log.w("MainActivity", "======================>onPause()")
        try {
            isOnCurrent = false
            super.onPause()
            activityResumedIntent.onNext(false)
        } catch (e: java.lang.Exception) {

        }

    }

    private lateinit var serviceConnection: ServiceConnection


    private fun updateDrawerItems() {
        remove_ad_layout.setVisible(true)
        //relativeBanner.setVisible(true)
        val config = resources.configuration
        val Language = config.locale.language
        if (Language != "en"){
            price_layout.setVisible(true)
        }else{
            price_layout_en.setVisible(true)
        }


        if (!isPurchased()) {
            upgrade_txt.text = " Upgrade to Premium"

            pro1.visibility = View.VISIBLE
            pro2.visibility = View.VISIBLE
            remove_ad_layout.visibility = View.VISIBLE
            iv_pro.visibility = View.VISIBLE
            val config = resources.configuration
            val Language = config.locale.language
            if (Language == "ar" || Language == "ur" || Language == "iw" || Language == "fa") {
                toolbar.setPadding(resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._36sdp), 0, 0, 0)
            } else {
                toolbar.setPadding(0, 0, resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._36sdp), 0)
            }

            price_txt_en.setText(getText(R.string.start_three_day))
            price_txt.setText(getText(R.string.start_three_day))
        } else {
            upgrade_txt.text = "Premium user"

            pro1.visibility = View.GONE
            pro2.visibility = View.GONE
            remove_ad_layout.visibility = View.GONE
            iv_pro.visibility = View.GONE
            toolbar.setPadding(0,0,0,0)
            price_txt_en.setText(R.string.no_ads)
            price_txt.setText(R.string.no_ads)
        }
    }


    var activity: MainActivity? = null

    init {
        activity = this
    }

    private fun showInterstitialAd(adShown: (shown: Boolean) -> Unit) {

        compose.isClickable = false
        adShown(true)

//        Toast.makeText(this,"Inter ads show Called",Toast.LENGTH_SHORT).show()
        /*InterstitialConditionDisplay.getInstance().increaseClicked()
        if (!InterstitialConditionDisplay.getInstance().shouldShowInterstitialAd()) {
            adShown(true)
            return
        }

        if (IronSource.isInterstitialReady()) {
            IronSourceAdsManger.showInterstitial(this, object : adShowCallBack {
                override fun adShown(bol: Boolean?) {
                    finishAppStartUpAds()
                    InterstitialConditionDisplay.getInstance().resetClickedAmount()
                    adShown(true)
                }
            })
        }
         else
          {
            advertiseHandler!!.showProgress(this@MainActivity)
            IronSourceAdsManger.initiateAd(this, object : onAdfailedToLoadListner {
                override fun onAdFailedToLoad() {
                    finishAppStartUpAds()
                    adShown(true)
                    advertiseHandler!!.hideProgress(this@MainActivity)
                }

                override fun onSuccess() {
                    advertiseHandler!!.hideProgress(this@MainActivity)
                    IronSourceAdsManger.showInterstitial(
                        this@MainActivity,
                        object : adShowCallBack {
                            override fun adShown(bol: Boolean?) {
                                InterstitialConditionDisplay.getInstance().resetClickedAmount()
                                finishAppStartUpAds()
                                adShown(true)

                            }
                        })
                }
                override fun adClose() {
                    finishAppStartUpAds()
                }
            })
        }
*/

        /*
        Old Inter Ads
        */


        /*
        if (advertiseHandler!!.isAppStartUpAdsEnabled && !(advertiseHandler!!.isAppStartUpAdsPause)) {
            advertiseHandler!!.loadInterstitialAds(
                this@MainActivity,
                UtilsData.interstitial_ad_unit_id, 0,

                object : AdvertiseHandler.AdsLoadsListener {
                    override fun onAdLoaded() {
                        Log.i(TAG, "onAdLoaded: ")
                        if (advertiseHandler!!.isAppStartUpAdsEnabled && !(advertiseHandler!!.isAppStartUpAdsPause)) {
                            advertiseHandler!!.showInterstitialAds(
                                this@MainActivity,
                                1,
                                object :
                                    AdvertiseHandler.AdsListener {
                                    override fun onAdsOpened() {
                                        Log.i(TAG, "onAdsOpened: ")
                                        finishAppStartUpAds()
                                        advertiseHandler!!.isAppStartUpAdsEnabled = false
                                    }

                                    override fun onAdsClose() {
                                        Log.i(TAG, "onAdsClose: ")
                                        finishAppStartUpAds()
                                        advertiseHandler!!.isAppStartUpAdsEnabled = false
                                    }

                                    override fun onAdsLoadFailed() {
                                        Log.i(TAG, "onAdsLoadFailed: ")
                                        finishAppStartUpAds()
                                    }
                                }, true, false
                            )
                        } else {
                            finishAppStartUpAds()
                        }
                    }
                    override fun onAdsLoadFailed() {
                        Log.i(TAG, "onAdsLoadFailed: 1")
                        finishAppStartUpAds()
                    }
                })
        }*/

    }

    private fun finishAppStartUpAds() {
        advertiseHandler!!.hideProgress(this@MainActivity)
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun showNativeAd() {
        if (!isPurchased()) {

            MaxAdManager.createNativeAd(this,maxAdContainer,nativeAdContainer,textViewRectangleNative,{
                relativeBanner.visibility = View.GONE
            },{
                relativeBanner.visibility = View.VISIBLE
            })

        } else {
            relativeBanner.visibility = View.GONE
        }
    }


    override fun onDestroy() {

        Log.w("MainActivity", "======================>onDestroy()")
        super.onDestroy()
        Preferences.setIntVal(applicationContext, "which", 3)
        disposables.dispose()
        if (billingConnector != null) {
            billingConnector?.release();
        }
        try {
            if (serviceConnection != null) {
                unbindService(serviceConnection)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun showBackButton(show: Boolean) {
        toggle.onDrawerSlide(drawer, if (show) 1f else 0f)
        toggle.drawerArrowDrawable.color = resolveThemeColor(android.R.attr.textColorPrimary)
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog(this)
    }

    override fun requestPermissions() {

    }

    override fun clearSearch() {
        dismissKeyboard()
        toolbarSearch.text = null
    }

    override fun selectionAll(conversations: RealmResults<Conversation>) {

    }

    override fun clearSelection() {
        if (frompersonal) {
            personalFragment.conversationsAdapter.clearSelection()
        } else if (fromspam) {
            spamFragment.conversationsAdapter.clearSelection()
        } else if (fromother) {

            otherFragment.conversationsAdapter.clearSelection()

        }
    }
//take reference of fragment from attach fragment don;t ibitialise globally to fragment


    override fun themeChanged() {
        recyclerView.scrapViews()
    }

    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
        blockingDialog.show(this, conversations, block)
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(
                resources.getQuantityString(
                    R.plurals.dialog_delete_conversion,
                    count,
                    count
                )
            )
            .setPositiveButton(R.string.button_delete) { _, _ ->
                confirmDeleteIntent.onNext(
                    conversations
                )
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    override fun showArchivedSnackbar() {
        Snackbar.make(drawerLayout, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
            setActionTextColor(resources.getColor(R.color.tools_theme))
            show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        if (menu != null) {
            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                menuItem.icon?.colorFilter = PorterDuffColorFilter(
                    resolveThemeColor(android.R.attr.textColorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //  optionsItemIntent.onNext(item.itemId)
        if (frompersonal) {
            // firstFragmet.optionsItemIntent.onNext(item.itemId)
            personalFragment.onOptionsItemSelected(item)

        } else if (fromspam) {

            spamFragment.optionitem(item)

        } else {
            try {

                otherFragment.optionitem(item)

            } catch (e: Exception) {

            }

        }
        when (item.itemId) {

            R.id.Rate
            -> {
                InterstitialConditionDisplay.getInstance().increaseClicked()
                openRateUs()
            }
            R.id.selectAllmenu -> {
                InterstitialConditionDisplay.getInstance().increaseClicked()
                openShare()
            }
            R.id.setting
            -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.remove_ad -> {
                val intent = Intent(this, CustomAdLayoutActivity::class.java)
                startActivity(intent)
            }
        }


        return true
    }

    private fun openShare() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND

        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=" + packageName
        )
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Share with"))
    }

    private fun openRateUs() {
        val uri = Uri.parse("market://details?id=" + applicationContext.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        } else {
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        }
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + applicationContext.packageName)
                )
            )
        }

    }


    override fun onBackPressed() {
        if (!Preferences.getBoolean(this, Preferences.INTRODUCTION)) {
            super.onBackPressed()
        } else {
            Log.e("ratePrompt",">"+com.messaging.textrasms.manager.utils.Constants.RATE_PROMPT)
            if (com.messaging.textrasms.manager.utils.Constants.RATE_PROMPT) {
                clearSelection()

                if (drawer.isVisible) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return
                }

                if (rateDialogLayout.isVisible) {
                    return
                }

                if (view_pager.currentItem.equals(0)) {
                    if (toolbarTitle.isVisible) {
                        clearSelection()

                    } else {
                        try {
                            val rateCount = Preferences.getIntVal(this, RATE_DIALOG_COUNT)
                            if (!Preferences.getBoolean(this, IS_RATE) && inbox.isActivated) {
                                if (!Preferences.getBoolean(
                                        this@MainActivity,
                                        Preferences.ADSREMOVED
                                    )
                                ) {
                                    showDialog()

                                } else {
                                    showDialog()
                                }
                            } else {
                                finish()
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }

                } else {
                    view_pager.currentItem = 0
                }
            }else{
//                System.exit(0)
                super.onBackPressed()
            }
        }



    }

    override fun showNightModeDialog() = nightModeDialog.show(activity!!)

    fun dissmissfragment() {

        compose.setVisible(false)
        set_default.setVisible(false)
        toolbar.setVisible(false)
        Preferences.setBoolean(this, Preferences.FirstLaunch, true)
        Preferences.setBoolean(this, Preferences.newversion, true)

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        logDebug("requesty>" + "permission" + requestCode)
        advertiseHandler!!.isNeedOpenAdRequest = false
        if (requestCode == 42389) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //   MessageSyncService.start(this)
                ifdissmiss = false
                dissmissfragment()
                //   checkAndAskForBatteryOptimization()
            }

            if (isFirstLaunch) {

                isFirstLaunch = false
//                startActivity(Intent(this, PurchaseActivity::class.java))
            }
        } else if (requestCode == permissionRequestCodeCalldorado) {
//            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPhoneCallpermissionGranted()
//            }
            if (isFirstLaunch) {
                isFirstLaunch = false
//                startActivity(Intent(this, PurchaseActivity::class.java))
            }
        }


        if (requestCode == permissionRequestCode) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (overlayPermissionManager != null && !overlayPermissionManager.isGranted) {
                    overlayPermissionManager.requestOverlay()
                }
                CalldoradoPermissions.phonePermissionGranted = "granted"
                //            permission_layout.setVisibility(View.GONE);
//            home_layout.setVisibility(View.VISIBLE);


            } else {
                CalldoradoPermissions.phonePermissionGranted = "notGranted"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i(TAG, "onActivityResult: requestCode :- $requestCode resultCode :- $resultCode")

        when (requestCode) {
            42389 -> {
                advertiseHandler!!.isNeedOpenAdRequest = false
                //  checkAndAskForBatteryOptimization()
                if (motionLayout != null) {
                    val numberOfPaymentCardAfterOnBoard: Long = RemoteConfigHelper.get(
                        RemoteConfigHelper.GPS172_PAYMENT_CARD_POSITION,
                        6L
                    )
                    if (numberOfPaymentCardAfterOnBoard == 3L) {
                        openPurchaseScreenInBoarding(onActivityOpened = {
                            motionLayout.setTransition(R.id.s3, R.id.s4)
                            motionLayout.transitionToEnd()
                        })
                    } else {
                        motionLayout.setTransition(R.id.s3, R.id.s4)
                        motionLayout.transitionToEnd()
                    }

                }
                if (isFirstLaunch) {
                    isFirstLaunch = false
//                    startActivity(Intent(this, PurchaseActivity::class.java))
                }
            }
            permissionRequestCodeCalldorado -> {

                if (resultCode == Activity.RESULT_CANCELED){

                    CalldoradoPermissions.phonePermissionGranted = "granted"
                }else{
                    CalldoradoPermissions.phonePermissionGranted = "notGranted"
                }
//                 onPhoneCallpermissionGranted()
                isOptimizationSettingOpen = false
                Preferences.setBoolean(this, Preferences.INTRODUCTION, true)
                advertiseHandler!!.isNeedOpenAdRequest = false
                //   checkAndAskForBatteryOptimization()
            }

            OverlayPermissionManager.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (overlayPermissionManager.isGranted)
                    updateForAfterCall()
            }
            else -> {
                if (requestCode == 50) {
                    isOptimizationSettingOpen = false
                    Preferences.setBoolean(this, Preferences.INTRODUCTION, true)
                    advertiseHandler!!.isNeedOpenAdRequest = false
                    val powerManager =
                        this.applicationContext.getSystemService(POWER_SERVICE) as PowerManager


                    if (overlayPermissionManager.isGranted) {
                        updateForAfterCall()
                    } else {
                        if (motionLayout != null) {
                             numberOfPaymentCardAfterOnBoard = RemoteConfigHelper.get(
                                RemoteConfigHelper.GPS172_PAYMENT_CARD_POSITION,
                                6L
                            )
                            if (numberOfPaymentCardAfterOnBoard == 4L) {
                                openPurchaseScreenInBoarding(onActivityOpened = {
                                    motionLayout.setTransition(R.id.s4, R.id.s5)
                                    motionLayout.transitionToEnd()
                                })
                            } else {
                                motionLayout.setTransition(R.id.s4, R.id.s5)
                                motionLayout.transitionToEnd()
                            }
                        }

                    }


                }
            }
        }
    }

    @Throws(IOException::class)
    fun getJsonFile(str: String?): String? {
        val inputStreamReader: InputStreamReader?
        inputStreamReader = try {
            InputStreamReader(this.assets.open(str!!), "UTF-8")
        } catch (unused: IOException) {
            null
        }
        val bufferedReader = BufferedReader(inputStreamReader)
        val sb = StringBuilder()
        while (true) {
            val readLine = bufferedReader.readLine()
            if (readLine != null) {
                sb.append(readLine)
            } else {
                bufferedReader.close()
                inputStreamReader!!.close()
                return sb.toString()
            }
        }
    }

    fun LoardUrl(activity: MainActivity) {
    }

    class GetBaseUrl(activity1: MainActivity) : AsyncHttpResponseHandler() {

        var activity = activity1

        override fun onSuccess(
            statusCode: Int,
            headers: Array<out Header>?,
            responseBody: ByteArray?
        ) {


            try {
                if (responseBody != null) {
                    val response = String(responseBody)
                    stickerlist = arrayListOf<Sticker>()
                    var jsonObject: JSONObject? = null
                    jsonObject = JSONObject(response)
                    var jsonarray: JSONArray? = null
                    jsonarray = jsonObject.getJSONArray("info")
                    for (i in 0 until jsonarray.length()) {
                        var jsonObjectmain = jsonarray.getJSONObject(i)
                        var stickerdata: Sticker = Sticker()
                        var stickerurlArrayList = ArrayList<Sticker.stickerurl>()
                        val jsonArraysticker = jsonObjectmain.getJSONArray("image")
                        for (j in 0 until jsonArraysticker.length()) {
                            var stickerurl: Sticker.stickerurl = Sticker().stickerurl()
                            stickerurl.name = jsonObjectmain.getString("name")
                            stickerurl.setUrl(Uri.parse(jsonArraysticker.getString(j)))
                            stickerurlArrayList.add(stickerurl)

                        }
                        stickerdata.setStickerurlArrayList(stickerurlArrayList)
                        stickerdata.setName(jsonObjectmain.getString("name"))
                        stickerlist.add(stickerdata)

                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        override fun onFailure(
            statusCode: Int,
            headers: Array<out Header>?,
            responseBody: ByteArray?,
            error: Throwable?
        ) {
            try {
            } catch (Th: Exception) {

            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null
//        var isConnected = false
//        CheckInternet().check(){
//                connected ->
//            if (connected){
//                isConnected =  true
//            }else{
//                isConnected = false
//            }
//        }
//        return isConnected
    }

    private fun isPurchased(): Boolean {
        val purchase = Preferences.getBoolean(this, Preferences.ADSREMOVED)
        Log.i(TAG, "isPurchased: purchase :- $purchase")
        return purchase
    }

    fun passDataToCompose(readFile: String) {
        val goToMarket = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=" + readFile)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        } else {
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        }
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
        }

    }


    private fun showDialog() {
        var floatrating: Float = 0f
        val inAppRatingDialog = Dialog(this, R.style.Theme_Dialog)
        inAppRatingDialog.setCancelable(false)
        inAppRatingDialog.setContentView(R.layout.rating_dialog)
        inAppRatingDialog.window!!.attributes.windowAnimations = R.style.popup_window_animation
        inAppRatingDialog.window!!.setBackgroundDrawableResource(R.color.dialog_bg)
        inAppRatingDialog.notNow1.setVisible(true)
        inAppRatingDialog.feedback.setTextColor(resolveThemeColor(android.R.attr.textColorPrimary))
        inAppRatingDialog.feedback.setHintTextColor(resolveThemeColor(android.R.attr.textColorTertiary))
        inAppRatingDialog.title_txt.text = "Like Messages ?"
        inAppRatingDialog.title_txt.setTextColor(resolveThemeColor(android.R.attr.textColorPrimary))
        inAppRatingDialog.notNow1.setTextColor(resolveThemeColor(android.R.attr.textColorTertiary))

        inAppRatingDialog.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->

            logDebug("rating$rating")
            floatrating = rating
            if (rating == 1f) {
                inAppRatingDialog.emoji.setImageResource(R.drawable.emoji_1)
            } else if (rating == 2f) {
                inAppRatingDialog.emoji.setImageResource(R.drawable.emoji_2)
            } else if (rating == 3f) {
                inAppRatingDialog.emoji.setImageResource(R.drawable.emoji_3)

            } else if (rating.equals(4f)) {
                inAppRatingDialog.emoji.setImageResource(R.drawable.emoji_4)

            } else if (rating.equals(5f)) {
                inAppRatingDialog.emoji.setImageResource(R.drawable.emoji_5)

            }


            if (rating <= 3) {

            } else {
                inAppRatingDialog.feedback_layout.setVisible(false)
                inAppRatingDialog.rate_submit_layout.setVisible(true)
                inAppRatingDialog.feeback_layout_submit.setVisible(false)
            }
        }
        inAppRatingDialog.button.setOnClickListener(View.OnClickListener {
            if (isNetworkAvailable()) {
                if (floatrating <= 3f) {
                    inAppRatingDialog.msg1.text = getString(R.string.feedback_txt)
                    inAppRatingDialog.title_txt.text = "Your opinion matters to us!"
                    inAppRatingDialog.msg1.gravity = Gravity.CENTER
                    inAppRatingDialog.title_txt.setTextColor(ContextCompat.getColor(this, R.color.tools_theme))
                    inAppRatingDialog.submit.setVisible(true)
                    inAppRatingDialog.rate_submit_layout.setVisible(false)
                    inAppRatingDialog.ratingBar.setVisible(true)
                    inAppRatingDialog.ratingBar.isClickable = false
                    inAppRatingDialog.ratingBar.isEnabled = false
                    inAppRatingDialog.feedback_layout.setVisible(true)
                    inAppRatingDialog.feeback_layout_submit.setVisible(true)
                    inAppRatingDialog.text_quote.setTextColor(resolveThemeColor(android.R.attr.textColorPrimary))
                } else {
                    try {
                        navigator.showRateUsPlayStore()
                        Preferences.setBoolean(this, IS_RATE, true)
                        try{
                            inAppRatingDialog.dismiss()
                        }catch (e:Exception){e.printStackTrace()}
                        finish()
                    }catch (e:Exception){e.printStackTrace()}
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please turn on Internet Connection.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        inAppRatingDialog.notNow1.setOnClickListener(View.OnClickListener {
            try{
                inAppRatingDialog.dismiss()
            }catch (e:Exception){e.printStackTrace()}
            try{
            finish()
        }catch (e:Exception){e.printStackTrace()}

        })
        inAppRatingDialog.submit.setOnClickListener(View.OnClickListener {
            var text = inAppRatingDialog.feedback.text.toString()
            if (text.length != 0) {

                feedbacksubmit(floatrating, text)
                Preferences.setBoolean(this, IS_RATE, true)


                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    if (!isFinishing && inAppRatingDialog.isShowing) {
                        inAppRatingDialog.dismiss()
                    }
                    val snack = Snackbar.make(
                        drawerLayout,
                        "Thank you for valuable Feedback",
                        Snackbar.LENGTH_LONG
                    ).apply {
                    }
                    snack.view.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow))
                    snack.show()

                }, 200)
            }
        })
        if (!isFinishing) {
            try {
                inAppRatingDialog.show()
            } catch (e: Exception) {
                e.printStackTrace()

            }
        }

    }

    @SuppressLint("RemoteViewLayout")
    private fun headsUpNotification() {
        lateinit var notificationManager: NotificationManager
        lateinit var notificationChannel: NotificationChannel
        lateinit var builder: NotificationCompat.Builder
        val channelId = "i.apps.notifications"
        val notificationId = 1
        val description = "Test notification"

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val contentView = RemoteViews(
            packageName,
            R.layout.floting_laout
        )
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.messages_app_icon)
            .setContentTitle(resources.getString(R.string.in_app_name))
            .setContentText(resources.getString(R.string.reason_to_change_battery))
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority((NotificationCompat.PRIORITY_MAX))
            .setAutoCancel(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                channelId, description, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            // notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)


        } else {

            builder = NotificationCompat.Builder(this)
                .setContent(contentView)
                .setSmallIcon(R.drawable.messages_app_icon)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.drawable.messages_app_icon
                    )
                )
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(notificationId, builder.build())
    }

    lateinit
    var dialog: Dialog

    fun feedbacksubmit(value: Float, feedback: String) {
        val client: AsyncHttpClient = AsyncHttpClient()
        client.addHeader("id", "jk.apps.review")
        val params = RequestParams()
        val jsonObject1 = JSONObject()
        // val jsonObject2 = JSONObject()
        try {
            jsonObject1.put("package", packageName)
            jsonObject1.put("star", value)
            jsonObject1.put("text", feedback)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val cryptLib = CryptLib()
        val rating = cryptLib.encryptPlainTextWithRandomIV(
            jsonObject1.toString(),
            resources.getString(R.string.encryption_key)
        )
        val entity = StringEntity(rating)
        logDebug("rating" + rating)

        val decryptedString = cryptLib.decryptCipherTextWithRandomIV(
            rating,
            resources.getString(R.string.encryption_key)
        )
        logDebug("rating" + decryptedString)
        client.post(
            this, resources.getString(R.string.feedback_url),
            entity, "application/json", GetFeedbackUrl(this)
        )


    }

    class GetFeedbackUrl(activity1: MainActivity) : AsyncHttpResponseHandler() {
        var pd = ProgressDialog(activity1)

        var activity = activity1
        override fun onStart() {
            super.onStart()
            pd.setMessage("submiting")
            pd.show()
        }

        override fun onSuccess(
            statusCode: Int,
            headers: Array<out Header>?,
            responseBody: ByteArray?
        ) {


            try {
                val response = String(responseBody!!)
                val cryptLib = CryptLib()
                val decryptedString =
                    cryptLib.decryptCipherTextWithRandomIV(response, "VoEDexAF8PRzntXT")
                //  val json = JSONObject(responseBody?.let { String(it) })
                logDebug("responsedata" + "code" + statusCode + ">>>>>>>>>" + decryptedString + ">>>>>>" + response)


//                activity.finish()

                if (pd != null && pd.isShowing && !activity.isFinishing) {
                    pd.dismiss()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        override fun onFailure(
            statusCode: Int,
            headers: Array<out Header>?,
            responseBody: ByteArray?,
            error: Throwable?
        ) {
            try {
                // Toast.makeText(activity, "fail", Toast.LENGTH_SHORT).show()
                logDebug("responsedata" + error!!.message)
                activity.finish()

            } catch (Th: Exception) {

            }

        }

        override fun onFinish() {
            if (pd != null && pd.isShowing && !activity.isFinishing) {
                pd.dismiss()
            }
        }


    }

    fun showdownloadlink(plauuri: String) {

        val dialog = Dialog(this, R.style.Theme_Dialog)

        dialog.setCancelable(false)
        dialog.setContentView(R.layout.app_redirect_layout)
        dialog.window!!.attributes.windowAnimations = R.style.popup_window_animation

        dialog.redirect.setOnClickListener(View.OnClickListener {
            if (dialog.isShowing && !isFinishing) {
                dialog.dismiss()
            }
            val uri = plauuri
            val goToMarket = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                goToMarket.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
            } else {
                goToMarket.addFlags(
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                )
            }
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {

            }
        })
        if (dialog != null && !isFinishing)
            dialog.show()
    }

    private fun checkAndAskForBatteryOptimization() {
        try {

            if (Build.VERSION.SDK_INT >= 28) {
                isOptimizationSettingOpen = true

                val powerManager =
                    this.applicationContext.getSystemService(POWER_SERVICE) as PowerManager

                if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
//                    callStep1()
                    val numberOfPaymentCardAfterOnBoard: Long = RemoteConfigHelper.get(
                        RemoteConfigHelper.GPS172_PAYMENT_CARD_POSITION,
                        6L
                    )
                    if (numberOfPaymentCardAfterOnBoard == 4L) {
                        openPurchaseScreenInBoarding(onActivityOpened = {
                            motionLayout.setTransition(R.id.s4, R.id.s5) //orange to blue transition
                            motionLayout.transitionToEnd()
                        })
                    } else {
                        motionLayout.setTransition(R.id.s4, R.id.s5) //orange to blue transition
                        motionLayout.transitionToEnd()
                    }
                } else {
//                    motionLayout.setTransition(R.id.s3, R.id.s4) //orange to blue transition
//                    motionLayout.transitionToEnd()

                    val intent = Intent()
                    intent.action = "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"
                    if (Build.MANUFACTURER.uppercase(Locale.getDefault()) == "VIVO") {
                        startActivityForResult(Intent("android.settings.SETTINGS"), 50)
                        headsUpNotification()
                    } else if (Build.MANUFACTURER.uppercase(Locale.getDefault()) == "OPPO" || Build.MANUFACTURER.uppercase(
                            Locale.getDefault()
                        ) == "REALME"
                    ) {
                        val intent3 = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                        intent3.data = Uri.fromParts("package", packageName, null as String?)
                        startActivityForResult(intent3, 50)
                        headsUpNotification()
                    } else if (Build.MANUFACTURER.uppercase(Locale.getDefault()) == "XIAOMI") {
                        intent.action = "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"
                        intent.data = Uri.parse("package:$packageName")
                        startActivityForResult(intent, 50)
                        headsUpNotification()
                    } else if (Build.MANUFACTURER.uppercase(Locale.getDefault())
                            .contains("ONEPLUS")
                    ) {
                        val intent4 = Intent("android.settings.APPLICATION_DETAILS_SETTINGS")
                        intent4.data = Uri.fromParts("package", packageName, null as String?)
                        startActivityForResult(intent4, 50)
                        headsUpNotification()
                    } else {
                        intent.data = Uri.parse("package:$packageName")
                        startActivityForResult(intent, 50)
                    }
                }
            } else {
                callStep1()

            }
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
            callStep1()
        }
    }


    fun getConnectionType(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
//        val cm: ConnectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (cm != null) {
//                val capabilities: NetworkCapabilities? =
//                    cm.getNetworkCapabilities(cm.getActiveNetwork())
//                if (capabilities != null) {
//                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                        result = 2
//                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                        result = 1
//                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
//                        result = 3
//                    }
//                }
//            }
//        } else {
//            if (cm != null) {
//                val activeNetwork: NetworkInfo? = cm.getActiveNetworkInfo()
//                if (activeNetwork != null) {
//                    // connected to the internet
//                    if (activeNetwork.getType() === ConnectivityManager.TYPE_WIFI) {
//                        result = 2
//                    } else if (activeNetwork.getType() === ConnectivityManager.TYPE_MOBILE) {
//                        result = 1
//                    } else if (activeNetwork.getType() === ConnectivityManager.TYPE_VPN) {
//                        result = 3
//                    }
//                }
//            }
//        }
                CheckInternet().check(){
                connected ->
            if (connected){
                result = 1
            }else{
                result = 0
            }
        }
        return result
    }


    fun TextView.formatSpanColor(string: String, color: Int) {

        // this is the text we'll be operating on
        val text = SpannableString(string)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                //  Toast.makeText(activity, "dolor", Toast.LENGTH_LONG).show();
            }
        }
        val clickableSpan2: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                //  Toast.makeText(activity, "dolor", Toast.LENGTH_LONG).show();
            }
        }

        val startText1 = string.indexOf("terms")
        val endText1 = string.indexOf("and the")
        val startText2 = string.indexOf("privacy")
        val endText2 = string.length - 1


        text.setSpan(URLSpan(getString(R.string.terms_link)), startText1, endText1, 0)
        text.setSpan(clickableSpan, startText1, endText1, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            text.setSpan(
                ForegroundColorSpan(context.getColor(color)),
                startText1,
                endText1,
                0
            )
        }
        text.setSpan(URLSpan(getString(R.string.privacy_policy_link)), startText2, endText2, 0)
        text.setSpan(clickableSpan2, startText2, endText2, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            text.setSpan(
                ForegroundColorSpan(context.getColor(color)),
                startText2,
                endText2,
                0
            )
        }
        movementMethod = LinkMovementMethod.getInstance()

        setText(text, TextView.BufferType.SPANNABLE)

    }

    private fun initSubFromRemoteConfig() {
        val subPlan: String = RemoteConfigHelper.get(
            RemoteConfigHelper.GPS172_PRICE_PLANS,
            SubscriptionType.yearly_sub.name
        )
        val subConfig: String = SubscriptionType.fromNameToValue(subPlan)
        Log.d("RemoteConfig", "${RemoteConfigHelper.initialized} -- $subPlan --- $subConfig")

        val interstitialAdTapMax = RemoteConfigHelper.get(
            RemoteConfigHelper.GPS172_INTERSTITIAL_TAP,
            3L
        )
        InterstitialConditionDisplay.getInstance().setMaxClicked(interstitialAdTapMax)
        Log.d(
            "RemoteConfig",
            "${RemoteConfigHelper.initialized} -- GPS172_interstital_taps: $interstitialAdTapMax"
        )
        val numberOfPaymentCardAfterOnBoard = RemoteConfigHelper.get(
            RemoteConfigHelper.GPS172_PAYMENT_CARD_POSITION,
            6L
        )
        Log.d(
            "RemoteConfig",
            "${RemoteConfigHelper.initialized} -- GPS172_paymentcard_onboardingposition: --- $numberOfPaymentCardAfterOnBoard"
        )
    }

    private fun setupInApps() {
        //in app billing v5
        iapConnector = IapConnector.getInstance(this@MainActivity)
        iapConnector?.addSubscriptionListener(object : SubscriptionServiceListener {
            override fun onSubscriptionRestored(purchaseInfo: Purchase) {
                Log.d("MainAct_IAP: ", "purchased")
//                updatePrefBoolean(true)
            }

            override fun onSubscriptionPurchased(purchaseInfo: Purchase) {
                Log.d("MainAct_IAP: ", "purchased")
//                updatePrefBoolean(true)
            }

            override fun onSubscriptionsExpired() {
                Log.d("MainAct_IAP: ", "onProductsExpired")
//                updatePrefBoolean(false)
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, ProductDetails>) {
            }
        }
        )

            iapConnector.restore()
    }

    private fun updatePrefBoolean(isPurchased: Boolean) {
        Preferences.setBoolean(activity!!, Preferences.ADSREMOVED, isPurchased)
    }

    private fun getSpannedText(text: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(text)
        }
    }



    private val SKUs = Arrays.asList(
        "com.messaging.textrasms.manager_29.99",
        "com.messaging.textrasms.manager_9.99",
        "com.messaging.textrasms.manager_2.99"
    )

    private var billingConnector: BillingConnector? = null
    private fun initBilling() {


        billingConnector = BillingConnector(this, getString(R.string.base64key))
//            .setConsumableIds(nonConsumableIds)
//            .setNonConsumableIds(SKUS_IN_APP)
            .setSubscriptionIds(SKUs)
            .autoAcknowledge()
            .autoConsume()
            .enableLogging()
            .connect()



        billingConnector?.setBillingEventListener(object : BillingEventListener {
            override fun onProductsFetched(skuDetails: List<ProductInfo>) {
            }

            override fun onPurchasedProductsFetched(
                skuType: ProductType ,
                purchases: MutableList<PurchaseInfo>
            ) {
                /*Provides a list with fetched purchased products*/
                if(skuType== ProductType.SUBS) {
                    if (purchases.isEmpty()) {
                        updatePrefBoolean(false)
                        Log.e("Tag", "inappPurchase>>>notpurchase")
                    } else {
                        updatePrefBoolean(true)
                        Log.e("Tag", "inappPurchase>>>purchase")
                    }
                }

            }

            override fun onProductsPurchased(purchases: List<PurchaseInfo>) {


            }

            override fun onPurchaseAcknowledged(purchase: PurchaseInfo) {

                /*Callback after a purchase is acknowledged*/

                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */
            }

            override fun onPurchaseConsumed(purchase: PurchaseInfo) {
                /*Callback after a purchase is consumed*/

                /*
                 * CONSUMABLE products entitlement can be granted either here or in onProductsPurchased
                 * */
            }

            override fun onBillingError(
                billingConnector: BillingConnector,
                response: BillingResponse
            ) {

                showLogs(response.debugMessage)
//                Toast.makeText(this@SubscriptionActivity,"${response.debugMessage}",Toast.LENGTH_LONG).show()
                /*Callback after an error occurs*/
                when (response.errorType) {
                    ErrorType.CLIENT_NOT_READY -> {
                    }
                    ErrorType.CLIENT_DISCONNECTED -> {
                    }

                    ErrorType.CONSUME_ERROR -> {
                    }
                    ErrorType.ACKNOWLEDGE_ERROR -> {
                    }
                    ErrorType.ACKNOWLEDGE_WARNING -> {
                    }
                    ErrorType.FETCH_PURCHASED_PRODUCTS_ERROR -> {
                    }
                    ErrorType.BILLING_ERROR -> {
                    }
                    ErrorType.USER_CANCELED -> {
                    }
                    ErrorType.SERVICE_UNAVAILABLE -> {
                    }
                    ErrorType.BILLING_UNAVAILABLE -> {
                    }
                    ErrorType.ITEM_UNAVAILABLE -> {
                    }
                    ErrorType.DEVELOPER_ERROR -> {
                    }
                    ErrorType.ERROR -> {
                    }
                    ErrorType.ITEM_ALREADY_OWNED -> {
                    }
                    ErrorType.ITEM_NOT_OWNED -> {
                    }

                    else -> {}
                }
            }
        })

    }


    private fun showLogs(msg: String) {

        Log.e("billing ", "=============================\n")
        Log.e("billing ", msg)
        Log.e("billing ", "\n=============================")

    }


    fun onPhoneCallpermissionGranted(){
        if (firstScreen) {
            firstScreen =false
            if (numberOfPaymentCardAfterOnBoard == 1L) {
                openPurchaseScreenInBoarding(onActivityOpened = {
//                    skipping page 2 as client asked
                    motionLayout.setTransition(R.id.s1, R.id.s3)
                    motionLayout.transitionToEnd()
                    eulaAccepted()
                })
            } else {
//                    skipping page 2 as client asked
                motionLayout.setTransition(R.id.s1, R.id.s3)
                motionLayout.transitionToEnd()
                eulaAccepted()
            }
        }

    }

    fun settingTermsPrivacy(tvTerms: TextView){
        val termsOfUse = "terms of use"
        val privacyPolicy = "privacy policy"

        val message = "By continuing, you accept and approve the $termsOfUse and the $privacyPolicy."

        val spannableString = SpannableString(message)

        val termsOfUseClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openUrl(getString(R.string.terms_link))
            }
        }
        val termsOfUseColorSpan = ForegroundColorSpan(Color.parseColor("#BBBBBB"))
        val termsOfUseUnderlineSpan = UnderlineSpan()

        val privacyPolicyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {

                openUrl(getString(R.string.privacy_policy_link))
            }
        }
        val privacyPolicyColorSpan = ForegroundColorSpan(Color.parseColor("#BBBBBB"))
        val privacyPolicyUnderlineSpan = UnderlineSpan()

        spannableString.setSpan(
            termsOfUseClickableSpan,
            message.indexOf(termsOfUse),
            message.indexOf(termsOfUse) + termsOfUse.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            termsOfUseColorSpan,
            message.indexOf(termsOfUse),
            message.indexOf(termsOfUse) + termsOfUse.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            termsOfUseUnderlineSpan,
            message.indexOf(termsOfUse),
            message.indexOf(termsOfUse) + termsOfUse.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            privacyPolicyClickableSpan,
            message.indexOf(privacyPolicy),
            message.indexOf(privacyPolicy) + privacyPolicy.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            privacyPolicyColorSpan,
            message.indexOf(privacyPolicy),
            message.indexOf(privacyPolicy) + privacyPolicy.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            privacyPolicyUnderlineSpan,
            message.indexOf(privacyPolicy),
            message.indexOf(privacyPolicy) + privacyPolicy.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvTerms.text = spannableString
        tvTerms.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun openUrl(url: String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e2: Exception) {
                Toast.makeText(this, "Unable to find market app", Toast.LENGTH_LONG).show()
            }
        }
    }

}