package com.messaging.textrasms.manager.model

import android.content.ContentUris
import android.net.Uri
import android.provider.Telephony.*
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import java.util.*

open class Message : RealmObject() {

    enum class AttachmentType { TEXT, IMAGE, VIDEO, AUDIO, SLIDESHOW, NOT_LOADED }

    @PrimaryKey
    var id: Long = 0

    @Index
    var threadId: Long = 0

    var contentId: Long = 0
    var address: String = ""
    var boxId: Int = 0
    var type: String = ""
    var date: Long = 0
    var datecompare: Date? = null
    var month: Long = 0
    var year: Long = 0
    var dateSent: Long = 0
    var seen: Boolean = false
    var read: Boolean = false
    var locked: Boolean = false
    var subId: Int = -1

    var body: String = ""
    var errorCode: Int = 0
    var deliveryStatus: Int = Sms.STATUS_NONE

    var attachmentTypeString: String = AttachmentType.NOT_LOADED.toString()
    var attachmentType: AttachmentType
        get() = AttachmentType.valueOf(attachmentTypeString)
        set(value) {
            attachmentTypeString = value.toString()
        }

    var mmsDeliveryStatusString: String = ""
    var readReportString: String = ""
    var errorType: Int = 0
    var messageSize: Int = 0
    var messageType: Int = 0
    var mmsStatus: Int = 0
    var subject: String = ""
    var textContentType: String = ""
    var parts: RealmList<MmsPart> = RealmList()
    var group_msg: RealmList<String> = RealmList()
    fun getUri(): Uri {
        val baseUri = if (isMms()) Mms.CONTENT_URI else Sms.CONTENT_URI
        return ContentUris.withAppendedId(baseUri, contentId)
    }

    fun isMms(): Boolean = type == "mms"

    fun isSms(): Boolean = type == "sms"

    fun isMe(): Boolean {
        val isIncomingMms =
            isMms() && (boxId == Mms.MESSAGE_BOX_INBOX || boxId == Mms.MESSAGE_BOX_ALL)
        val isIncomingSms =
            isSms() && (boxId == Sms.MESSAGE_TYPE_INBOX || boxId == Sms.MESSAGE_TYPE_ALL)

        return !(isIncomingMms || isIncomingSms)
    }

    fun isOutgoingMessage(): Boolean {
        val isOutgoingMms = isMms() && boxId == Mms.MESSAGE_BOX_OUTBOX
        val isOutgoingSms = isSms() && (boxId == Sms.MESSAGE_TYPE_FAILED
                || boxId == Sms.MESSAGE_TYPE_OUTBOX
                || boxId == Sms.MESSAGE_TYPE_QUEUED)

        return isOutgoingMms || isOutgoingSms
    }

    fun getText(): String {
        return when {
            isSms() -> body

            else -> parts
                .filter { it.type == "text/plain" }
                .mapNotNull { it.text }
                .joinToString("\n") { text -> text }
        }
    }

    fun getSummary(): String = when {
        isSms() -> body

        else -> {
            val sb = StringBuilder()

            getCleansedSubject().takeIf { it.isNotEmpty() }?.run(sb::appendln)

            parts.mapNotNull { it.getSummary() }.forEach { summary -> sb.appendLine(summary) }

            sb.toString().trim()
        }
    }

    fun getCleansedSubject(): String {
        val uselessSubjects = listOf("no subject", "NoSubject", "<not present>")

        return if (uselessSubjects.contains(subject)) "" else subject
    }

    fun isSending(): Boolean {
        return !isFailedMessage() && isOutgoingMessage()
    }

    fun isDelivered(): Boolean {
        val isDeliveredMms = false // TODO
        val isDeliveredSms = deliveryStatus == Sms.STATUS_COMPLETE
        return isDeliveredMms || isDeliveredSms
    }

    fun isFailedMessage(): Boolean {
        val isFailedMms =
            isMms() && (errorType >= MmsSms.ERR_TYPE_GENERIC_PERMANENT || boxId == Mms.MESSAGE_BOX_FAILED)
        val isFailedSms = isSms() && boxId == Sms.MESSAGE_TYPE_FAILED
        return isFailedMms || isFailedSms
    }

    fun compareSender(other: Message): Boolean = when {
        isMe() && other.isMe() -> subId == other.subId
        !isMe() && !other.isMe() -> subId == other.subId && address == other.address
        else -> false
    }
}