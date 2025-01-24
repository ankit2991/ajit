package com.messaging.textrasms.manager.repository

import com.messaging.textrasms.manager.model.ScheduledMessage
import io.realm.RealmResults

interface ScheduledMessageRepository {


    fun saveScheduledMessage(
        date: Long,
        subId: Int,
        recipients: List<String>,
        sendAsGroup: Boolean,
        body: String,
        attachments: List<String>
    )

    fun getScheduledMessages(): RealmResults<ScheduledMessage>

    fun getScheduledMessage(id: Long): ScheduledMessage?

    fun deleteScheduledMessage(id: Long)

}