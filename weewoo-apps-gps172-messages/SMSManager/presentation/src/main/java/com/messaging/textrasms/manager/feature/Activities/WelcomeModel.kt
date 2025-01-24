package com.messaging.textrasms.manager.feature.Activities

import android.content.Context
import android.util.Log
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.common.base.QkViewModel
import com.messaging.textrasms.manager.common.util.BillingManager
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.feature.Views.WelcomeView
import com.messaging.textrasms.manager.feature.states.Inbox
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.interactor.*
import com.messaging.textrasms.manager.listener.ContactAddedListener
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.model.SyncLog
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.SyncRepository
import com.messaging.textrasms.manager.util.NightModeManager
import com.messaging.textrasms.manager.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class WelcomeModel @Inject constructor(

    contactAddedListener: ContactAddedListener,
    markAllSeen: MarkAllSeen,
    migratePreferences: MigratePreferences,
    syncRepository: SyncRepository,
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val deleteConversations: DeleteConversations,
    private val markArchived: MarkArchived,
    private val markPinned: MarkPinned,
    private val markRead: MarkRead,
    private val markUnarchived: MarkUnarchived,
    private val markUnpinned: MarkUnpinned,
    private val markUnread: MarkUnread,
    private val navigator: Navigator,
    private val markBlocked: MarkBlocked,
    private val blockingManager: BlockingClient,
    private val permissionManager: PermissionManager,
    private val prefs: Preferences,
    private val syncContacts: SyncContacts,
    private val syncMessages: SyncMessages,
    private val synccount: Synccount,
    private val syncConversation: SyncConversation,
    private val nightModeManager: NightModeManager,
    private val billingManager: BillingManager,
    private val dateFormatter: DateFormatter


) : QkViewModel<WelcomeView, MainState>(
    MainState(
        page = Inbox(data = null),
        nightModeId = prefs.nightMode.get(),
        defaultSms = permissionManager.isDefaultSms()
    )
) {

    init {
        disposables += deleteConversations
        disposables += markAllSeen
        disposables += markArchived
        disposables += markUnarchived
        disposables += migratePreferences
        disposables += syncContacts
        disposables += syncMessages
        disposables += syncConversation
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

        val LanguageLabels = context.resources.getStringArray(R.array.Language_name)
        disposables += prefs.Language.asObservable()
            .subscribe { language ->
                newState { copy(language = LanguageLabels[language], LanguageId = language) }
            }


        migratePreferences.execute(Unit)


        val lastSync = Realm.getDefaultInstance().use { realm ->
            realm.where(SyncLog::class.java)?.max("date") ?: 0
        }

        if (lastSync == 0 && permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
            syncMessages.execute(Unit)
            logDebug("checkmsg" + "dgfdhfdhgfhgfhgf")
        }

        if (permissionManager.hasContacts()) {
            disposables += contactAddedListener.listen()
                .debounce(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe { syncContacts.execute(Unit) }
        }

        markAllSeen.execute(Unit)

    }

    override fun bindView(view: WelcomeView) {
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

        // If the default SMS state or permission states change, update the ViewState
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


        // If we go from not having all permissions to having them, sync messages
        permissions
            .skip(1)
            .filter { it.first && it.second && it.third }
            .take(1)
            .autoDispose(view.scope())
            .subscribe {
                syncMessages.execute(Unit)

            }



        view.loadddata
            .autoDispose(view.scope())
            .subscribe {
                if (permissionManager.isDefaultSms() && permissionManager.hasReadSms() && permissionManager.hasContacts()) {
                    syncMessages.execute(Unit)
                    logDebug("checkmsg" + "dgfdhfdhgfhgfhgf")
                }
            }


    }


    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

}