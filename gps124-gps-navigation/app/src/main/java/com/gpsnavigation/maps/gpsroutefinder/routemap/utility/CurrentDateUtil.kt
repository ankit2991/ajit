package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar

object CurrentDateUtil {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(): String{
        val currentDate = LocalDate.now()
        return currentDate.toString()
    }

    fun getCurrentDateMin():String{
        val currentDate = Calendar.getInstance().time
        return currentDate.toString()
    }
    fun getCurrentDate2(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy_MM_dd")
        return dateFormat.format(calendar.time)
    }
}