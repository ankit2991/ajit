package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.util.Calendar


fun convertSecondsToTime(seconds: Long): String {
    var timeStr: String? = null
    var hour = 0L
    var minute = 0L
    var second = 0L
    if (seconds <= 0)
        return "0h:0m"
    else {
        minute = seconds / 60
        if (minute < 60) {
            second = seconds % 60
            timeStr = unitFormat(minute) + "m " + unitFormat(second.toDouble()) + "s"
        } else {
            hour = minute / 60
            if (hour > 99)
                return "99h:59m:59s"
            minute = minute % 60
            //second = (seconds - (hour * 3600).toLong() - (minute * 60).toLong()).toInt()
            timeStr = unitFormat(hour) + "h " + unitFormat(minute) + "m"
        }
    }
    return timeStr
}





fun getSecondsFromHourAndMinutes(hours: Int, minute: Int): Int {
    return 60 * minute + 3600 * hours
}

fun getSecondsFromMinutes( minute: Int): Int {
    return 60 * minute + 3600
}

fun getTimeInAMPMFormat(hours: Int, minute: Int): String {
    if (hours == 0)
        return "12:" + minute + "AM"
    else if (hours == 12)
        return hours.toString() + ":" + minute + "PM"
    else if (hours > 12) {
        val hour = hours - 12
        return hour.toString() + ":" + minute + "PM"
    } else return hours.toString() + ":" + minute + "AM"
}



private fun unitFormat(i: Long): String {
    var retStr: String? = null
    if (i >= 0 && i < 10)
        retStr = "0" + i
    else
        retStr = "" + i
    return retStr
}



