package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.manager.MyNotificationManager
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class DeleteMessages @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val myNotificationManager: MyNotificationManager,
    private val updateBadge: UpdateBadge
) : Interactor<DeleteMessages.Params>() {

    data class Params(val messageIds: List<Long>, val threadId: Long? = null)

    override fun buildObservable(params: Params): Flowable<*> {
        return Flowable.just(params.messageIds.toLongArray())
            .doOnNext { messageIds -> messageRepo.deleteMessages(*messageIds) }
            .doOnNext {
                params.threadId?.let { conversationRepo.updateConversations(it) }
            }
            .doOnNext { params.threadId?.let { myNotificationManager.update(it) } }
            .flatMap { updateBadge.buildObservable(Unit) }
    }

}