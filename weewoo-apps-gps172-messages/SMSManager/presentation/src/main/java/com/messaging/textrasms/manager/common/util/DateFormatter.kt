package com.messaging.textrasms.manager.common.util

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import com.messaging.textrasms.manager.common.util.extensions.isSameDay
import com.messaging.textrasms.manager.common.util.extensions.isSameWeek
import com.messaging.textrasms.manager.common.util.extensions.isSameYear
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateFormatter @Inject constructor(val context: Context) {

    /**
     * Formats the [pattern] correctly for the current locale, and replaces 12 hour format with
     * 24 hour format if necessary
     */
    private fun getFormatter(pattern: String): SimpleDateFormat {
        var formattedPattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), pattern)

        if (DateFormat.is24HourFormat(context)) {
            formattedPattern = formattedPattern
                .replace("h", "HH")
                .replace("K", "HH")

        }

        return SimpleDateFormat(formattedPattern, Locale.getDefault())
    }

    fun getDetailedTimestamp(date: Long): String {
        return getFormatter("M/d/y, h:mm:ss a").format(date)
    }

    fun getTimestamp(date: Long): String {
        return getFormatter("h:mm a").format(date)
    }

    fun getMessageTimestamp(date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = date

        return when {
            now.isSameDay(then) -> getFormatter("MMM d, h:mm a")
            now.isSameWeek(then) -> getFormatter("MMM d, h:mm a")
            now.isSameYear(then) -> getFormatter("MMM d, h:mm a")
            else -> getFormatter("MMM d yyyy, h:mm a")
        }.format(date)
    }

    fun getsameday(date: Long): Boolean {
        getFormatter("h:mm a")
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = date
        logDebug("check>>>" + now.isSameDay(then))
        return when {
            now.isSameDay(then) -> true
            else -> false

        }
    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

    fun getMessageTimestampsms(date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = date
        var time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
        time = time.replace("AM", "am").replace("PM", "pm")
        // SimpleDateFormat(formattedPattern, Locale.getDefault())
        return time
    }

    fun getConversationTimestamp(date: Long): String {

        val now = Calendar.getInstance(Locale.getDefault())
        val then = Calendar.getInstance(Locale.getDefault())
        then.timeInMillis = date

        return when {
            now.isSameDay(then) -> getFormatter("h:mm a")
            now.isSameWeek(then) -> getFormatter("E")
            now.isSameYear(then) -> getFormatter("MMM d")
            else -> getFormatter("MM/d/yy")
        }.format(date)
    }

    fun getScheduledTimestamp(date: Long): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance()
        then.timeInMillis = date

        return when {
            now.isSameDay(then) -> getFormatter("h:mm a")
            now.isSameYear(then) -> getFormatter("MMM d h:mm a")
            else -> getFormatter("MMM d yyyy h:mm a")
        }.format(date)
    }

}