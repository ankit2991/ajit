package com.messaging.textrasms.manager.manager

import android.app.PendingIntent

interface AlarmManager {

    fun getScheduledMessageIntent(id: Long): PendingIntent

    fun setAlarm(date: Long, intent: PendingIntent)

}