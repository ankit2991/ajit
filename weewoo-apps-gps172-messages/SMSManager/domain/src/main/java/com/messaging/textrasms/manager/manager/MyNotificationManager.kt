package com.messaging.textrasms.manager.manager

import androidx.core.app.NotificationCompat

interface MyNotificationManager {

    fun update(threadId: Long)

    fun notifyFailed(threadId: Long)

    fun createNotificationChannel(threadId: Long = 0L)

    fun buildNotificationChannelId(threadId: Long): String

    fun getNotificationForBackup(): NotificationCompat.Builder

    fun getNotificationForSunc(): NotificationCompat.Builder

}
