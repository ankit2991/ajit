package com.messaging.textrasms.manager.common.util

import android.content.Context
import com.google.android.mms.pdu_alt.EncodedStringValue
import com.google.android.mms.pdu_alt.MultimediaMessagePdu
import com.google.android.mms.pdu_alt.PduPersister
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.util.tryOrNull
import java.util.*
import javax.inject.Inject

class MessageDetailsFormatter @Inject constructor(
    private val context: Context,
    private val dateFormatter: DateFormatter
) {

    fun format(message: Message, conversation: Conversation?): String {
        val builder = StringBuilder()

        message.type
            .takeIf { it.isNotBlank() }
            ?.uppercase(Locale.getDefault())
            ?.let { context.getString(R.string.compose_details_type, it) }
            ?.let(builder::appendln)

        if (message.isSms()) {
            message.address
                .takeIf { it.isNotBlank() && !message.isMe() }
                ?.let { context.getString(R.string.compose_details_from, it) }
                ?.let(builder::appendln)

            message.address
                .takeIf { it.isNotBlank() && message.isMe() }
                ?.let { context.getString(R.string.compose_details_to, it) }
                ?.let(builder::appendln)

            if (conversation != null && conversation.recipients.size > 1) {
                for (index in 0 until conversation.recipients.size) {
                    val value = conversation.recipients[index]?.address.toString()
                    if (builder.contains(value, true)) {
                        continue
                    }
                    builder.append(", ")
                    builder.append(value)
                }

                builder.append("\n")
            }
        } else {
            val pdu = tryOrNull {
                PduPersister.getPduPersister(context)
                    .load(message.getUri())
                        as MultimediaMessagePdu
            }

            pdu?.from?.string
                ?.takeIf { it.isNotBlank() }
                ?.let { context.getString(R.string.compose_details_from, it) }
                ?.let(builder::appendln)

            pdu?.to
                ?.let(EncodedStringValue::concat)
                ?.takeIf { it.isNotBlank() }
                ?.let { context.getString(R.string.compose_details_to, it) }
                ?.let(builder::appendln)
        }

        message.date
            .takeIf { it > 0 && message.isMe() }
            ?.let(dateFormatter::getDetailedTimestamp)
            ?.let { context.getString(R.string.compose_details_sent, it) }
            ?.let(builder::appendln)

        message.dateSent
            .takeIf { it > 0 && !message.isMe() }
            ?.let(dateFormatter::getDetailedTimestamp)
            ?.let { context.getString(R.string.compose_details_sent, it) }
            ?.let(builder::appendln)

        message.date
            .takeIf { it > 0 && !message.isMe() }
            ?.let(dateFormatter::getDetailedTimestamp)
            ?.let { context.getString(R.string.compose_details_received, it) }
            ?.let(builder::appendln)

        message.dateSent
            .takeIf { it > 0 && message.isMe() }
            ?.let(dateFormatter::getDetailedTimestamp)
            ?.let { context.getString(R.string.compose_details_delivered, it) }
            ?.let(builder::appendln)

        message.errorCode
            .takeIf { it != 0 && message.isSms() }
            ?.let { context.getString(R.string.compose_details_error_code, it) }
            ?.let(builder::appendln)

        return builder.toString().trim()
    }

}