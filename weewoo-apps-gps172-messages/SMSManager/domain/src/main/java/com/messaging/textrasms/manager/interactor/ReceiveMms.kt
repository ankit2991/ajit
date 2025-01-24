package com.messaging.textrasms.manager.interactor

import android.net.Uri
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.manager.ActiveConversationManager
import com.messaging.textrasms.manager.manager.MyNotificationManager
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import com.messaging.textrasms.manager.repository.SyncRepository
import com.messaging.textrasms.manager.util.Preferences
import io.reactivex.Flowable
import timber.log.Timber
import javax.inject.Inject

class ReceiveMms @Inject constructor(
    private val activeConversationManager: ActiveConversationManager,
    private val conversationRepo: ConversationRepository,
    private val blockingClient: BlockingClient,
    private val prefs: Preferences,
    private val syncManager: SyncRepository,
    private val messageRepo: MessageRepository,
    private val myNotificationManager: MyNotificationManager,
    private val updateBadge: UpdateBadge
) : Interactor<Uri>() {

    override fun buildObservable(params: Uri): Flowable<*> {
        return Flowable.just(params)
            .mapNotNull(syncManager::syncMessage)
            .doOnNext { message ->
                if (activeConversationManager.getActiveConversation() == message.threadId) {
                    messageRepo.markRead(message.threadId)
                }
            }
            .mapNotNull { message ->
                val action = blockingClient.getAction(message.address).blockingGet()
                val shouldDrop = prefs.drop.get()
                Timber.v("block=$action, drop=$shouldDrop")

                if (action is BlockingClient.Action.Block && shouldDrop) {
                    messageRepo.deleteMessages(message.id)
                    return@mapNotNull null
                }

                when (action) {
                    is BlockingClient.Action.Block -> {
                        messageRepo.markRead(message.threadId)
                        conversationRepo.markBlocked(
                            listOf(message.threadId),
                            prefs.blockingManager.get(),
                            action.reason
                        )
                    }
                    is BlockingClient.Action.Unblock -> conversationRepo.markUnblocked(message.threadId)
                    else -> Unit
                }

                message
            }
            .doOnNext { message ->
                conversationRepo.updateConversations(message.threadId)
            }
            .mapNotNull { message ->
                conversationRepo.getOrCreateConversation(message.threadId)
            }
            .filter { conversation -> !conversation.blocked }
            .doOnNext { conversation ->
                if (conversation.archived) conversationRepo.markUnarchived(conversation.id)
            }
            .map { conversation -> conversation.id }
            .doOnNext(myNotificationManager::update)
            .flatMap { updateBadge.buildObservable(Unit) }
    }

}