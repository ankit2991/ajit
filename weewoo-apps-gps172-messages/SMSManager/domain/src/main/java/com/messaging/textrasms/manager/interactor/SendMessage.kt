package com.messaging.textrasms.manager.interactor

import android.content.Context
import com.messaging.textrasms.manager.compat.TelephonyCompat
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.model.Attachment
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SendMessage @Inject constructor(
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val updateBadge: UpdateBadge
) : Interactor<SendMessage.Params>() {

    data class Params(
        val subId: Int,
        val threadId: Long,
        val addresses: List<String>,
        val body: String,
        val attachments: List<Attachment> = listOf(),
        val delay: Int = 0

    )

    override fun buildObservable(params: Params): Flowable<*> = Flowable.just(Unit)
        .filter { params.addresses.isNotEmpty() }
        .doOnNext {
            val threadId = when (params.threadId) {
                0L -> TelephonyCompat.getOrCreateThreadId(context, params.addresses.toSet())
                else -> params.threadId
            }
            messageRepo.sendMessage(
                params.subId, threadId, params.addresses, params.body, params.attachments,
                params.delay
            )
        }
        .mapNotNull {
            when (params.threadId) {
                0L -> conversationRepo.getOrCreateConversation(params.addresses)?.id
                else -> params.threadId
            }
        }
        .doOnNext { threadId -> conversationRepo.updateConversations(threadId) }
        .doOnNext { threadId -> conversationRepo.markUnarchived(threadId) }
        .flatMap { updateBadge.buildObservable(Unit) } // Update the widget

}