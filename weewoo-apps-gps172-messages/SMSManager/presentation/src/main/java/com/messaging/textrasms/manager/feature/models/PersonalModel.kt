package com.messaging.textrasms.manager.feature.models

import android.content.Context
import android.util.Log
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.common.base.QkViewModel
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.feature.Views.PersonalMainView
import com.messaging.textrasms.manager.feature.states.Archived
import com.messaging.textrasms.manager.feature.states.Inbox
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.interactor.*
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.SyncRepository
import com.messaging.textrasms.manager.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.realm.Sort
import javax.inject.Inject

class PersonalModel @Inject constructor(
    markAllSeen: MarkAllSeen,
    migratePreferences: MigratePreferences,
    private val conversationRepo: ConversationRepository,
    private val deleteConversations: DeleteConversations,
    private val multiselectConversations: MultiselectConversations,
    private val markArchived: MarkArchived,
    private val markPinned: MarkPinned,
    private val markRead: MarkRead,
    private val markUnarchived: MarkUnarchived,
    private val markUnpinned: MarkUnpinned,
    private val markUnread: MarkUnread,
    private val navigator: Navigator,
    private val markBlocked: MarkBlocked,
    syncRepository: SyncRepository,
    private val blockingManager: BlockingClient,
    private val permissionManager: PermissionManager,
    private val prefs: Preferences,
    private val context: Context
) : QkViewModel<PersonalMainView, MainState>(
    MainState(
        page = Inbox(
            data = conversationRepo.getConversationspersonal(
                false,
                true
            )
        )
    )
) {

    init {
        disposables += deleteConversations
        disposables += markAllSeen
        disposables += markArchived
        disposables += markUnarchived
        disposables += migratePreferences
    }

    fun previoussorting(category: Int) {
        if (category != 3) {
            val month = Preferences.getIntVal(context, "selectedMonth")
            val year = Preferences.getIntVal(context, "selectedYear")
            val order = Preferences.getIntVal(context, "order")
            val toselectdate = Preferences.getStringVal(context, "dayStart") as String
            val fromselectdate = Preferences.getStringVal(context, "dayEnd") as String


            val sort: Sort
            if (order.equals(1)) {
                sort = Sort.ASCENDING
            } else {
                sort = Sort.DESCENDING
            }

            sorting("", month.toLong(), year.toLong(), sort, category, toselectdate, fromselectdate)
        }
    }

    override fun bindView(view: PersonalMainView) {
        super.bindView(view)

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.archive }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                markArchived.execute(conversations)
                view.clearSelection()
            }
            .autoDispose(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.unarchive }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                markUnarchived.execute(conversations)
                view.clearSelection()
            }
            .autoDispose(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.delete }
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                view.showDeleteDialog(conversations)
            }
            .autoDispose(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.add }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations -> conversations }
            .doOnNext { view.clearSelection() }
            .filter { conversations -> conversations.size == 1 }
            .map { conversations -> conversations.first() }
            .mapNotNull(conversationRepo::getConversation)
            .map { conversation -> conversation.recipients }
            .mapNotNull { recipients -> recipients[0]?.address?.takeIf { recipients.size == 1 } }
            .doOnNext(navigator::addContact)
            .autoDispose(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.pin }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                markPinned.execute(conversations)
                view.clearSelection()
            }
            .autoDispose(view.scope())
            .subscribe()
        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.unpin }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                markUnpinned.execute(conversations)
                view.clearSelection()
            }
            .autoDispose(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.read }
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                markRead.execute(conversations)
                view.clearSelection()
            }
            .autoDispose(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.unread }
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                markUnread.execute(conversations)
                view.clearSelection()
            }
            .autoDispose(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.block }
            .withLatestFrom(view.conversationsSelectedIntent) { _, conversations ->
                val addresses = conversations.toLongArray()
                    .let { conversationRepo.getConversations(*it) }
                    .flatMap { conversation -> conversation.recipients }
                    .map { it.address }
                    .distinct()
                markBlocked.execute(
                    MarkBlocked.Params(
                        conversations,
                        prefs.blockingManager.get(),
                        null
                    )
                )
                blockingManager.block(addresses).subscribe()
                view.clearSelection()
            }
            .autoDispose(view.scope())
            .subscribe()



        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.sort }
            .withLatestFrom(state) { _, state ->
                view.Showsorting()
            }

            .autoDispose(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.selectAll }
            .withLatestFrom(state) { _, state ->


                android.os.Handler().postDelayed(Runnable {
                    val conversations = conversationRepo.getConversationspersonal(false, false)
                    view.selectionAll(conversations)
                }, 150)

            }
            .autoDispose(view.scope())
            .subscribe()

        view.optionsItemIntent
            .filter { itemId -> itemId == R.id.deselectAll }
            .doOnNext { view.clearSelection() }
            .autoDispose(view.scope())
            .subscribe()

        view.conversationsSelectedIntent
            .withLatestFrom(state) { selection, state ->
                val conversations = selection.mapNotNull(conversationRepo::getConversation)
                val add = conversations.firstOrNull()
                    ?.takeIf { conversations.size == 1 }
                    ?.takeIf { conversation -> conversation.recipients.size == 1 }
                    ?.recipients?.first()
                    ?.takeIf { recipient -> recipient.contact == null } != null
                val pin = conversations.sumBy({ if (it.pinned) -1 else 1 }) >= 0
                val read = conversations.sumBy({ if (!it.unread) -1 else 1 }) >= 0
                val selected = selection.size

                when (state.page) {
                    is Inbox -> {
                        val page = state.page.copy(
                            addContact = add,
                            markPinned = pin,
                            markRead = read,
                            selected = selected
                        )
                        newState { copy(page = page) }
                    }

                    is Archived -> {
                        val page = state.page.copy(
                            addContact = add,
                            markPinned = pin,
                            markRead = read,
                            selected = selected
                        )
                        newState { copy(page = page) }
                    }

                    else -> {}
                }
            }
            .autoDispose(view.scope())
            .subscribe()

        view.confirmDeleteIntent
            .autoDispose(view.scope())
            .subscribe { conversations ->
                deleteConversations.execute(conversations)
                view.clearSelection()
            }

        view.snackbarButtonIntent
            .withLatestFrom(state) { _, state ->
            }
            .autoDispose(view.scope())
            .subscribe()
    }


    fun sorting(
        field: String,
        month: Long,
        year: Long,
        orderad: Sort,
        category: Int,
        toselectdate: String,
        fromselect: String
    ) {
        when {

            category.equals(0) -> newState {
                copy(
                    page = Inbox(
                        data = conversationRepo.sortingOfDatasortingOfDataDate(
                            false,
                            true,
                            false,
                            field,
                            month,
                            year,
                            "lastMessage.date",
                            orderad,
                            toselectdate,
                            fromselect
                        )
                    )
                )
            }
            category.equals(1) -> newState {
                copy(
                    page = Inbox(
                        data = conversationRepo.sortingOfDatasortingOfDataMonth(
                            false,
                            true,
                            false,
                            field,
                            month,
                            year,
                            "lastMessage.date",
                            orderad
                        )
                    )
                )
            }
            category.equals(2) -> newState {
                copy(
                    page = Inbox(
                        data = conversationRepo.sortingOfDatasortingOfDataYear(
                            false,
                            true,
                            false,
                            field,
                            month,
                            year,
                            "lastMessage.date",
                            orderad
                        )
                    )
                )
            }
            category.equals(3) -> newState {
                copy(
                    page = Inbox(
                        data = conversationRepo.getConversationspersonal(
                            false,
                            true
                        )
                    )
                )
            }
            category.equals(4) -> newState {
                copy(
                    page = Inbox(
                        data = conversationRepo.sortingOfDatasortingOfDataDate(
                            false,
                            true,
                            false,
                            field,
                            month,
                            year,
                            "lastMessage.date",
                            orderad,
                            toselectdate,
                            fromselect
                        )
                    )
                )
            }

        }


    }


    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }
}