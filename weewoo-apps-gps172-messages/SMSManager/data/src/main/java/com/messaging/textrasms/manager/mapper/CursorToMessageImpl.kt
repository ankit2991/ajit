package com.messaging.textrasms.manager.mapper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony.*
import android.text.format.DateFormat
import android.util.Log
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.PduHeaders
import com.google.android.mms.pdu_alt.PduPersister
import com.messaging.textrasms.manager.extensions.map
import com.messaging.textrasms.manager.manager.KeyManager
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.SqliteWrapper
import com.messaging.textrasms.manager.util.tryOrNull
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class CursorToMessageImpl @Inject constructor(
    private val context: Context,
    private val cursorToPart: CursorToPart,
    private val keys: KeyManager,
    private val permissionManager: PermissionManager,
    private val preferences: Preferences

) : CursorToMessage {

    val cl = Calendar.getInstance()
    override fun map(from: Pair<Cursor, CursorToMessage.MessageColumns>): Message {
        val cursor = from.first
        val columnsMap = from.second
        return Message().apply {
            type = when {
                cursor.getColumnIndex(MmsSms.TYPE_DISCRIMINATOR_COLUMN) != -1 -> cursor.getString(
                    columnsMap.msgType
                )
                cursor.getColumnIndex(Mms.SUBJECT) != -1 -> "mms"
                cursor.getColumnIndex(Sms.ADDRESS) != -1 -> "sms"
                else -> "unknown"
            }

            id = keys.newId()
            threadId = cursor.getLong(columnsMap.threadId)
            contentId = cursor.getLong(columnsMap.msgId)
            date = cursor.getLong(columnsMap.date)
            datecompare = toDate(
                DateFormat.format("yyyy-MM-dd", Date(cursor.getLong(columnsMap.date))).toString()
            )
            cl.timeInMillis = cursor.getLong(columnsMap.date)
            month = cl.get(Calendar.MONTH).toLong()
            year = cl.get(Calendar.YEAR).toLong()
            dateSent = cursor.getLong(columnsMap.dateSent)
            read = cursor.getInt(columnsMap.read) != 0
            locked = cursor.getInt(columnsMap.locked) != 0
            subId = if (columnsMap.subId != -1) cursor.getInt(columnsMap.subId) else -1

            when (type) {

                "sms" -> {


                    address = cursor.getString(columnsMap.smsAddress) ?: ""
                    boxId = cursor.getInt(columnsMap.smsType)
                    seen = cursor.getInt(columnsMap.smsSeen) != 0

                    body = columnsMap.smsBody
                        .takeIf { column -> column != -1 }
                        ?.let { column -> cursor.getString(column) }
                        ?: ""

                    logDebug("bodyget" + address)

                    errorCode = cursor.getInt(columnsMap.smsErrorCode)
                    deliveryStatus = cursor.getInt(columnsMap.smsStatus)

                }

                "mms" -> {


                    address = getMmsAddress(contentId)
                    boxId = cursor.getInt(columnsMap.mmsMessageBox)
                    date *= 1000L
                    dateSent *= 1000L

                    datecompare = toDate(DateFormat.format("yyyy-MM-dd", Date(date)).toString())
                    cl.timeInMillis = date
                    month = cl.get(Calendar.MONTH).toLong()
                    year = cl.get(Calendar.YEAR).toLong()
                    seen = cursor.getInt(columnsMap.mmsSeen) != 0
                    mmsDeliveryStatusString = cursor.getString(columnsMap.mmsDeliveryReport)
                        ?: ""
                    errorType =
                        if (columnsMap.mmsErrorType != -1) cursor.getInt(columnsMap.mmsErrorType) else 0
                    messageSize = 0
                    readReportString = cursor.getString(columnsMap.mmsReadReport) ?: ""
                    messageType = cursor.getInt(columnsMap.mmsMessageType)
                    mmsStatus = cursor.getInt(columnsMap.mmsStatus)
                    val subjectCharset = cursor.getInt(columnsMap.mmsSubjectCharset)
                    subject = cursor.getString(columnsMap.mmsSubject)
                        ?.takeIf { it.isNotBlank() }
                        ?.let(PduPersister::getBytes)
                        ?.let { EncodedStringValue(subjectCharset, it).string } ?: ""
                    textContentType = ""
                    attachmentType = Message.AttachmentType.NOT_LOADED

                    try {


                        parts.addAll(
                            cursorToPart.getPartsCursor(contentId)?.map { cursorToPart.map(it) }
                                ?: listOf())
                    } catch (e: Exception) {

                    }


                }
            }
        }
    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

    fun toDate(dateString: String): Date? {
        var date: Date? = null
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        try {
            date = formatter.parse(dateString)

        } catch (e1: ParseException) {
            e1.printStackTrace()

        }

        return date
    }

    private val uri = Uri.parse("content://mms-sms/complete-conversations")
    private val projection = arrayOf(
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        MmsSms._ID,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.THREAD_ID,
        Mms.LOCKED,

        Sms.ADDRESS,
        Sms.BODY,
        Sms.SEEN,
        Sms.TYPE,
        Sms.STATUS,
        Sms.ERROR_CODE,

        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.SEEN,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        MmsSms.PendingMessages.ERROR_TYPE,
        Mms.STATUS
    )


    override fun getMessagesCursor(): Cursor? {

        if (!preferences.canUseSubId.isSet) {
            val canUseSubId = tryOrNull {
                SqliteWrapper.query(context, uri, arrayOf(Mms.SUBSCRIPTION_ID), logError = false)
                    ?.use { true }
            }

            preferences.canUseSubId.set(canUseSubId ?: false)
        }

        val projection = when (preferences.canUseSubId.get()) {
            true -> this.projection + Mms.SUBSCRIPTION_ID
            false -> this.projection
        }
        val date = Date(System.currentTimeMillis() - 14L * 24 * 3600 * 1000).time
        return when (permissionManager.hasReadSms()) {
            true -> SqliteWrapper.query(
                context,
                uri,
                projection,
                sortOrder = "normalized_date desc"
            )
            false -> null
        }
    }


    override fun getMessageCursor(id: Long): Cursor? {
        TODO("not implemented")
    }

    private fun getMmsAddress(messageId: Long): String {
        try {


            val uri = Mms.CONTENT_URI.buildUpon()
                .appendPath(messageId.toString())
                .appendPath("addr").build()

            //TODO: Use Charset to ensure address is decoded correctly
            val projection = arrayOf(Mms.Addr.ADDRESS, Mms.Addr.CHARSET)
            val selection = "${Mms.Addr.TYPE} = ${PduHeaders.FROM}"

            val cursor = context.contentResolver.query(uri, projection, selection, null, null)
            cursor?.use {
                if (cursor.moveToFirst()) {

                    return cursor.getString(0) ?: ""
                }
            }
            cursor!!.close()
        } catch (e: Exception) {

        }

        return ""
    }

}
