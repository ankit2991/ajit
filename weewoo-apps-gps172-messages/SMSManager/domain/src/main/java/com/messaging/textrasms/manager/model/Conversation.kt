package com.messaging.textrasms.manager.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*


open class Conversation(
    @PrimaryKey var id: Long = 0,
    @Index var archived: Boolean = false,
    @Index var blocked: Boolean = false,
    @Index var pinned: Boolean = false,
    var recipients: RealmList<Recipient> = RealmList(),
    var lastMessage: Message? = null,
    var draft: String = "",
    var ispossible: Boolean = false,
    var isunknown: Boolean = true,
    var isspam: Boolean = false,
    var transactional: Boolean = false,
    var Promotion: Boolean = false,
    var blockingClient: Int? = null,
    var blockfromfilter: Boolean = false,
    var blockReason: String? = null,

    var name: String = ""
) : RealmObject() {

    val date: Long get() = lastMessage?.date ?: 0
    val snippet: String? get() = lastMessage?.getSummary()
    val unread: Boolean get() = lastMessage?.read == false
    val me: Boolean get() = lastMessage?.isMe() == true
    val iserror: Boolean get() = lastMessage?.isFailedMessage() == true
    val issending: Boolean get() = lastMessage?.isSending() == true
    fun getTitle(): String {
        return name.takeIf { it.isNotBlank() }
            ?: recipients.joinToString { recipient ->
                recipient.getDisplayName()
            }
    }

    fun getaddress(): String {
        return lastMessage!!.address
    }


    @Index
    private var month: String? = "January"
    private var datemanual: Date? = null

    @Deprecated("")
    fun setMonth(month: String) {
        this.month = month
    }

    fun setDate(date: Date?) {
        this.datemanual = date
        if (date == null) {
            this.month = null
        } else {
            this.month = SimpleDateFormat("MMMMM", Locale.US).format(date)
        }
    }
}
