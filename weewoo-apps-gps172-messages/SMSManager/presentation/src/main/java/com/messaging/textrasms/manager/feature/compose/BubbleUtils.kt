package com.messaging.textrasms.manager.feature.compose

import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.model.Message
import java.util.concurrent.TimeUnit

object BubbleUtils {

    const val TIMESTAMP_THRESHOLD = 10

    fun canGroup(message: Message, other: Message?): Boolean {
        if (other == null) return false
        val diff = TimeUnit.MILLISECONDS.toMinutes(Math.abs(message.date - other.date))
        return message.compareSender(other) && diff < TIMESTAMP_THRESHOLD
    }


    fun getBubble(
        emojiOnly: Boolean,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean,
        isMe: Boolean
    ): Int {
        return when {
            emojiOnly -> R.drawable.message_emoji
            !canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_only_blue else R.drawable.message_in_first
            canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_only_blue else R.drawable.message_in_middle
            canGroupWithPrevious && !canGroupWithNext -> if (isMe) R.drawable.message_only_blue else R.drawable.message_in_last
            else -> R.drawable.message_only
        }
    }

    fun getBubbleme(
        emojiOnly: Boolean,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean,
        isMe: Boolean
    ): Int {
        return when {
            emojiOnly -> R.drawable.message_emoji
            !canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_only_blue else R.drawable.message_in_first
            canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_only_blue else R.drawable.message_in_middle
            canGroupWithPrevious && !canGroupWithNext -> if (isMe) R.drawable.message_only_blue else R.drawable.message_in_last
            else -> R.drawable.message_only_blue
        }
    }

}