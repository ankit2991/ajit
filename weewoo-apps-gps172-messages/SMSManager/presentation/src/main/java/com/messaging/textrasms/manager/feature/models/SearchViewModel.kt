package com.messaging.textrasms.manager.feature.models

import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.common.base.QkViewModel
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.feature.Views.SearchView
import com.messaging.textrasms.manager.feature.states.Archived
import com.messaging.textrasms.manager.feature.states.Inbox
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.feature.states.Searching
import com.messaging.textrasms.manager.interactor.*
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.SyncRepository
import com.messaging.textrasms.manager.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    markAllSeen: MarkAllSeen,
    migratePreferences: MigratePreferences,
    syncRepository: SyncRepository,
    private val conversationRepo: ConversationRepository,
    private val prefs: Preferences,
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
    private val permissionManager: PermissionManager
) : QkViewModel<SearchView, MainState>(
    MainState(
        page = Inbox(
            datarecent = conversationRepo.getRecentConversations(),
            data = conversationRepo.getConversations()
        )
    )
) {


    init {
        disposables += deleteConversations
        disposables += markAllSeen
        disposables += markArchived
        disposables += markUnarchived
        disposables += migratePreferences

        disposables += syncRepository.syncProgress
            .sample(16, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribe { syncing -> newState { copy(syncing = syncing) } }

        migratePreferences.execute(Unit)
        markAllSeen.execute(Unit)
    }

    override fun bindView(view: SearchView) {
        super.bindView(view)

        view.backPressedSubject
            .withLatestFrom(state) { drawerItem, state ->
                newState { copy(drawerOpen = false) }
                when (drawerItem) {
                    "back" -> when {
                        state.page is Searching -> view.clearSearch()
                        state.page is Inbox && state.page.selected > 0 -> view.clearSelection()
                        state.page is Archived && state.page.selected > 0 -> view.clearSelection()
                        state.page !is Inbox -> {
                            newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                        }
                        else -> newState { copy(hasError = true) }
                    }

                    else -> Unit
                }
                drawerItem
            }
            .distinctUntilChanged()
            .autoDispose(view.scope())
            .subscribe()

        view.queryClearedIntent
            .autoDispose(view.scope())
            .subscribe { view.clearQuery() }

        view.queryChangedIntent
            .debounce(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .withLatestFrom(state) { query, state ->
                if (query.isEmpty() && state.page is Searching) {
                    newState { copy(page = Inbox(data = conversationRepo.getConversations())) }
                }
                query
            }
            .filter { query -> query.length >= 2 }
            .doOnNext {
                newState {
                    val page = (page as? Searching) ?: Searching()
                    copy(page = page.copy(loading = true))
                }
            }
            .observeOn(Schedulers.io())
            .map(conversationRepo::searchConversations)
            .autoDispose(view.scope())
            .subscribe { data -> newState { copy(page = Searching(loading = false, data = data)) } }

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
            .filter { itemId -> itemId == R.id.selectAll }
            .doOnNext { view.selectionAll(conversationRepo.getConversations()) }
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


    }


}