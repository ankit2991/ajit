package com.messaging.textrasms.manager.feature.conversationinfo

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.common.base.QkPresenter
import com.messaging.textrasms.manager.common.util.ClipboardUtils
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.extensions.asObservable
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.feature.conversationinfo.ConversationInfoItem.ConversationInfoMedia
import com.messaging.textrasms.manager.feature.conversationinfo.ConversationInfoItem.ConversationInfoRecipient
import com.messaging.textrasms.manager.interactor.*
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.makeToast
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject
import javax.inject.Named

class ConversationInfoPresenter @Inject constructor(
    @Named("threadId") threadId: Long,
    messageRepo: MessageRepository,
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val deleteConversations: DeleteConversations,
    private val markArchived: MarkArchived,
    private val markUnarchived: MarkUnarchived,
    private val navigator: Navigator,
    private val permissionManager: PermissionManager,
    private val prefs: Preferences,
    private val blockingManager: BlockingClient,
    private val markBlocked: MarkBlocked,
    private val markUnblocked: MarkUnblocked
) : QkPresenter<ConversationInfoView, ConversationInfoState>(
    ConversationInfoState(threadId = threadId)
) {

    private val conversation: Subject<Conversation> = BehaviorSubject.create()

    init {
        disposables += conversationRepo.getConversationAsync(threadId)
            .asObservable()
            .filter { conversation -> conversation.isLoaded }
            .doOnNext { conversation ->
                if (!conversation.isValid) {
                    newState { copy(hasError = true) }
                }
            }
            .filter { conversation -> conversation.isValid }
            .filter { conversation -> conversation.id != 0L }
            .subscribe(conversation::onNext)

        disposables += markArchived
        disposables += markUnarchived
        disposables += deleteConversations

        disposables += Observables
            .combineLatest(
                conversation,
                messageRepo.getPartsForConversation(threadId).asObservable()
            ) { conversation, parts ->
                val data = mutableListOf<ConversationInfoItem>()

                if (!conversation.isLoaded || !conversation.isValid || !parts.isLoaded || !parts.isValid) {
                    return@combineLatest
                }

                data += conversation.recipients.map(::ConversationInfoRecipient)
                data += ConversationInfoItem.ConversationInfoSettings(
                    name = conversation.name,
                    recipients = conversation.recipients,
                    archived = conversation.archived,
                    blocked = conversation.blocked
                )
                data += parts.map(::ConversationInfoMedia)

                newState { copy(data = data) }
            }
            .subscribe()
    }

    override fun bindIntents(view: ConversationInfoView) {
        super.bindIntents(view)

        view.recipientClicks()
            .mapNotNull(conversationRepo::getRecipient)
            .doOnNext { recipient ->
                recipient.contact?.lookupKey?.let(navigator::showContact)
                    ?: navigator.addContact(recipient.address)
            }
            .autoDispose(view.scope(Lifecycle.Event.ON_DESTROY)) // ... this should be the default
            .subscribe()

        view.recipientLongClicks()
            .mapNotNull(conversationRepo::getRecipient)
            .map { recipient -> recipient.address }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(view.scope())
            .subscribe { address ->
                ClipboardUtils.copy(context, address)
                context.makeToast(R.string.info_copied_address)
            }

        view.themeClicks()
            .autoDispose(view.scope())
            .subscribe(view::showThemePicker)

        view.nameClicks()
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .map { conversation -> conversation.name }
            .autoDispose(view.scope())
            .subscribe(view::showNameDialog)

//        view.nameChanges()
//            .withLatestFrom(conversation) { name, conversation ->
//                conversationRepo.setConversationName(conversation.id, name)
//            }
//            .autoDispose(view.scope())
//            .subscribe()

        view.nameChanges()
            .withLatestFrom(conversation) { name, conversation ->
                Pair(name, conversation)
            }
            .observeOn(Schedulers.io())  // Switch to a background thread
            .map { (name, conversation) ->
                // Background thread: perform operations that should run in the background
                conversationRepo.setConversationName(conversation.id, name)
            }
            .observeOn(AndroidSchedulers.mainThread()) // Switch back to the main thread for UI updates
            .autoDispose(view.scope())
            .subscribe()

        view.notificationClicks()

            .withLatestFrom(conversation) { _, conversation -> conversation }
            .autoDispose(view.scope())

            .subscribe { conversation ->
                if (conversation.isValid) {
                    navigator.showNotificationSettings(conversation.id)
                }
            }

        view.archiveClicks()
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .autoDispose(view.scope())
            .subscribe { conversation ->
                when (conversation.archived) {
                    true -> markUnarchived.execute(listOf(conversation.id))
                    false -> markArchived.execute(listOf(conversation.id))
                }
            }

        view.blockClicks()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .autoDispose(view.scope())
            //above code is for bg task
//            .withLatestFrom(conversation) { _, conversation -> conversation }
//            .autoDispose(view.scope())
            .subscribe { conversation ->
                if (conversation.isValid) {
                    val addresses = listOf(conversation.id).toLongArray()
                        .let { conversationRepo.getConversations(*it) }
                        .flatMap { conversation1 -> conversation1.recipients }
                        .map { it.address }
                        .distinct()

                    if (conversation.blocked) {
                        markUnblocked.execute(listOf(conversation.id))
                        blockingManager.unblock(addresses).subscribe()
                    } else {
                        markBlocked.execute(
                            MarkBlocked.Params(
                                listOf(conversation.id),
                                prefs.blockingManager.get(),
                                null
                            )
                        )
                        blockingManager.block(addresses).subscribe()
                    }
                }

            }

        view.deleteClicks()
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .autoDispose(view.scope())
            .subscribe {
                try {
                    view.showDeleteDialog()
                } catch (e: Exception) {

                }
            }

        view.confirmDelete()
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .autoDispose(view.scope())
            .subscribe(
                { conversation -> deleteConversations.execute(listOf(conversation.id)) },
                { throwable -> })

        view.mediaClicks()
            .autoDispose(view.scope())
            .subscribe(navigator::showMedia)
    }

}