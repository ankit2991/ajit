package com.messaging.textrasms.manager.feature.Activities

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.util.Log
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkViewModel
import com.messaging.textrasms.manager.common.util.AppUtils
import com.messaging.textrasms.manager.common.util.BillingManager
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.feature.Activities.MainActivity.Companion.ifdissmiss
import com.messaging.textrasms.manager.feature.Views.MainView
import com.messaging.textrasms.manager.feature.Views.NavItem
import com.messaging.textrasms.manager.feature.ads.AdvertiseHandler
import com.messaging.textrasms.manager.feature.states.Archived
import com.messaging.textrasms.manager.feature.states.Inbox
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.feature.states.Searching
import com.messaging.textrasms.manager.interactor.*
import com.messaging.textrasms.manager.listener.ContactAddedListener
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.model.SyncLog
import com.messaging.textrasms.manager.repository.SyncRepository
import com.messaging.textrasms.manager.util.NightModeManager
import com.messaging.textrasms.manager.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MainViewModel @Inject constructor(

    contactAddedListener: ContactAddedListener,
    markAllSeen: MarkAllSeen,
    migratePreferences: MigratePreferences,
    private val syncContacts: SyncContacts,
    private val context: Context,
    private val navigator: Navigator,
    private val permissionManager: PermissionManager,
    private val prefs: Preferences,
    syncRepository: SyncRepository,
    private val synccount: Synccount,
    private val nightModeManager: NightModeManager,
    private val billingManager: BillingManager,
    private val syncMessages: SyncMessages,
    private val dateFormatter: DateFormatter

) : QkViewModel<MainView, MainState>(
    MainState(
        page = Inbox(data = null),
        nightModeId = prefs.nightMode.get(),
        defaultSms = permissionManager.isDefaultSms()
    )
) {

    init {
        disposables += migratePreferences
        disposables += syncContacts
        disposables += syncMessages
        val nightModeLabels = context.resources.getStringArray(R.array.night_modes)
        disposables += prefs.nightMode.asObservable()
            .subscribe { nightMode ->
                newState {
                    copy(
                        nightModeSummary = nightModeLabels[nightMode],
                        nightModeId = nightMode
                    )
                }
            }

        disposables += prefs.nightStart.asObservable()
            .map { time -> nightModeManager.parseTime(time) }
            .map { calendar -> calendar.timeInMillis }
            .map { millis -> dateFormatter.getTimestamp(millis) }
            .subscribe { nightStart -> newState { copy(nightStart = nightStart) } }

        disposables += prefs.nightEnd.asObservable()
            .map { time -> nightModeManager.parseTime(time) }
            .map { calendar -> calendar.timeInMillis }
            .map { millis -> dateFormatter.getTimestamp(millis) }
            .subscribe { nightEnd -> newState { copy(nightEnd = nightEnd) } }

        disposables += prefs.black.asObservable()
            .subscribe { black -> newState { copy(black = black) } }

        disposables += prefs.notifications().asObservable()
            .subscribe { enabled -> newState { copy(notificationsEnabled = enabled) } }

        // Show the syncing UI
        disposables += syncRepository.syncProgress
            .sample(16, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribe { syncing -> newState { copy(syncing = syncing) } }
        disposables += syncRepository.synccount
            .sample(16, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribe { syncingcount -> newState { copy(syncingcount = syncingcount) } }


        migratePreferences.execute(Unit)

        val lastSync = Realm.getDefaultInstance().use { realm ->
            realm.where(SyncLog::class.java)?.max("date") ?: 0
        }

        if (lastSync == 0 && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
            syncMessages.execute(Unit)
        }

        if (permissionManager.hasContacts()) {
            disposables += contactAddedListener.listen()
                .debounce(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe { syncContacts.execute(Unit) }
        }

        markAllSeen.execute(Unit)

    }

    override fun bindView(view: MainView) {
        super.bindView(view)


        val permissions = view.activityResumedIntent
            .filter { resumed -> resumed }
            .observeOn(Schedulers.io())
            .map { permissionManager.hasStorage() }
            .map {
                Triple(
                    permissionManager.isDefaultSms(),
                    permissionManager.hasReadSms(),
                    permissionManager.hasContacts()
                )
            }
            .distinctUntilChanged()
            .share()

        permissions
            .doOnNext { (defaultSms, smsPermission, contactPermission) ->
                newState {
                    copy(
                        defaultSms = defaultSms,
                        smsPermission = smsPermission,
                        contactPermission = contactPermission,
                        writeexternal = writeexternal
                    )
                }
            }
            .autoDispose(view.scope())
            .subscribe()

        permissions
            .skip(1)
            .filter { it.first && it.second && it.third }
            .take(1)
            .autoDispose(view.scope())
            .subscribe {
                syncMessages.execute(Unit)
            }

        view.modeclick.autoDispose(view.scope())
            .subscribe {
                if (!Preferences.getBoolean(context, Preferences.ADSREMOVED)) {
                    navigator.showRemoveAds()
                } else {
                    ifdissmiss = false
                    if( Preferences.getBoolean(context,Preferences.IS_LANG_SELECTED)) {
                        changelanguge(prefs.Language.get())
                    }
                    view.showNightModeDialog()
                }
            }

        view.languageclick
            .autoDispose(view.scope())
            .subscribe { view.showLanguage() }


        view.LanguageClicked()
            .autoDispose(view.scope())
            .subscribe(prefs.Language::set)
        // Launch screen from intent
        view.onNewIntentIntent
            .autoDispose(view.scope())
            .subscribe { intent ->
                when (intent.getStringExtra("screen")) {
                    "blocking" -> navigator.showBlockedConversations()
                }
            }

        view.activityResumedIntent
            .filter { resumed -> !resumed }
            .switchMap {
                prefs.keyChanges
                    .filter { key -> key.contains("theme") }
                    .map { true }
                    .mergeWith(prefs.autoColor.asObservable().skip(1))
                    .doOnNext { view.themeChanged() }
                    .takeUntil(view.activityResumedIntent.filter { resumed -> resumed })

            }
            .autoDispose(view.scope())
            .subscribe()

        view.loadddata
            .autoDispose(view.scope())
            .subscribe {
                syncMessages.execute(Unit)
            }



        view.composeIntent
            .autoDispose(view.scope())
            .subscribe {
                try {

                    view.clearSelection()
                } catch (e: Exception) {

                }

                view.showInterAdsFromViewmodel()

            }
        view.nightModeSelected()
            .withLatestFrom(billingManager.upgradeStatus) { mode, upgraded ->
                nightModeManager.updateNightMode(mode)


            }
            .autoDispose(view.scope())
            .subscribe()
        view.homeIntent
            .withLatestFrom(state) { _, state ->
                when {
                    state.page is Searching -> view.clearSearch()
                    state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                    state.page is Archived && state.page.selected > 0 -> view.clearSelection()

                    else -> {
                        view.clearSelection()
                        view.drawerevent(true)
                        //                            newState {
                        //                                copy(drawerOpen = true)
                        //                            }
                    }
                }
            }
            .autoDispose(view.scope())
            .subscribe()

        view.drawerOpenIntent
            .autoDispose(view.scope())
            .subscribe { open ->
            }

        view.navigationIntent
            .withLatestFrom(state) { drawerItem, state ->
                view.drawerevent(true)
                AdvertiseHandler.getInstance(context).isAppStartUpAdsPause = true
                when (drawerItem) {
                    NavItem.BACK -> when {
                        state.drawerOpen -> {
                            view.clearSelection()
                        }
                        state.page is Searching -> view.clearSearch()
                        state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                        state.page !is Inbox -> {
                            newState { copy(page = Inbox(data = null)) }
                        }
                        else -> newState { copy(hasError = true) }
                    }
                    NavItem.BACKUP -> {
                        view.drawerclose()
                        navigator.showBackup()
                    }
                    NavItem.SCHEDULED -> {
                        view.drawerclose()
                        if (Preferences.getBoolean(context, Preferences.ADSREMOVED)) {
                            navigator.showScheduled()
                        } else {
                            navigator.showRemoveAds()
                        }
                    }
                    NavItem.BLOCKING -> {
                        view.drawerclose()
                        navigator.showBlockedConversations()
                    }

                    NavItem.CALLDORADO -> {

                        view.drawerclose()
                        navigator.showCalldorado()
                    }
                    NavItem.SETTINGS -> {
                        view.drawerclose()
                        navigator.showSettings()
                    }
                    NavItem.REMOVEADS -> {
                        view.drawerclose()
                        navigator.showRemoveAds()
                    }
                    NavItem.HELP -> {
                        view.drawerclose()
                        navigator.showSupport()
                    }
                    NavItem.INVITE -> {
                        navigator.showInvite()
                    }
                    NavItem.BENEFIT -> {
                        view.drawerclose()
                    }
                    NavItem.PRICE -> {
                        view.drawerclose()
                        val hasPurchase = Preferences.getBoolean(context, Preferences.ADSREMOVED)
                        if (!hasPurchase) {
                            navigator.showRemoveAds()
                        }
                    }
                    NavItem.GROUP -> {
                        view.drawerclose()
                        navigator.showgroupContacts()
                    }
                    NavItem.ARCHIVED -> {
                        view.drawerclose()
                        Handler().postDelayed(Runnable { navigator.showArchiveActivity() }, 200)

                    }
                    NavItem.TRANSACTIONAL -> {
                        view.drawerclose()
                        Handler().postDelayed(
                            Runnable { navigator.showTransactionalActivity() },
                            200
                        )

                    }
                    NavItem.PROMOTIONS -> {
                        view.drawerclose()
                        Handler().postDelayed(Runnable { navigator.showPromotionalActivity() }, 200)

                    }
                    NavItem.Filter -> {
                        view.drawerclose()
                        Handler().postDelayed(Runnable { navigator.showFilterActivity() }, 200)

                    }
                    NavItem.SPAM -> {
                        view.drawerclose()
                        Handler().postDelayed(Runnable { navigator.showSpamActivity() }, 200)

                    }
                    NavItem.RATEUS -> {
                        view.drawerclose()
                        navigator.showRateUsPlayStore()
                    }
                    NavItem.SHARE -> {
                        view.drawerclose()
                        navigator.shareApp()
                    }
                    else -> Unit
                }
                drawerItem
            }
            .distinctUntilChanged()
            .doOnNext { drawerItem ->
                when (drawerItem) {

                    NavItem.INBOX -> newState { copy(page = Inbox(data = null)) }
                    else -> Unit
                }
            }
            .autoDispose(view.scope())
            .subscribe()

        view.snackbarButtonIntent
            .withLatestFrom(state) { _, state ->
                when {
                    !state.defaultSms -> view.requestDefaultSms()
                    !state.smsPermission -> view.requestPermissions()
                    !state.contactPermission -> view.requestPermissions()
                    !state.writeexternal -> view.requestStoragePermissions()
                }
            }
            .autoDispose(view.scope())
            .subscribe()
    }


    override fun moveCompose() {
        navigator.showCompose()


    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

    fun changelanguge(languageCode: Int) {
        val config = context.resources.configuration
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
        val locale = Locale(Language)
        config.setLocale(locale)
        AppUtils.updateConfig(context, config)
        //  recreate()
    }

}