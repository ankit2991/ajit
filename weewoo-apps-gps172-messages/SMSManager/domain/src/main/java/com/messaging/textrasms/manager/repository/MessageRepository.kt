package com.messaging.textrasms.manager.repository

import com.messaging.textrasms.manager.model.Attachment
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.model.MmsPart
import io.realm.RealmResults
import java.io.File

interface MessageRepository {

    fun getMessages(threadId: Long, query: String = ""): RealmResults<Message>

    fun getMessage(id: Long): Message?

    fun getMessageForPart(id: Long): Message?

    fun getLastIncomingMessage(threadId: Long): RealmResults<Message>

    fun getUnreadCount(): Long

    fun getPart(id: Long): MmsPart?

    fun getPartsForConversation(threadId: Long): RealmResults<MmsPart>

    fun savePart(id: Long): File?
    fun getmessagecount(threadId: Long): Int

    fun getUnreadUnseenMessages(threadId: Long): RealmResults<Message>

    fun getUnreadMessages(threadId: Long): RealmResults<Message>

    fun markAllSeen()

    fun markSeen(threadId: Long)

    fun markRead(vararg threadIds: Long)

    fun markUnread(vararg threadIds: Long)

    fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>,
        delay: Int = 0

    )

    fun sendSms(message: Message, addresses: List<String>, x: Int = 0)

    fun resendMms(message: Message)

    fun cancelDelayedSms(id: Long)

    fun insertSentSms(
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long
    ): Message

    fun insertReceivedSms(subId: Int, address: String, body: String, sentTime: Long): Message
    fun markSending(id: Long)

    fun markSent(id: Long)

    fun markFailed(address: String = "", id: Long, resultCode: Int)

    fun markDelivered(id: Long)

    fun markDeliveryFailed(id: Long, resultCode: Int)

    fun deleteMessages(vararg messageIds: Long)

}
