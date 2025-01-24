package com.messaging.textrasms.manager.feature

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.messaging.textrasms.Oraganizer.MyBrodcastRecieverService
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.util.Preferences


class Closingservice : Service() {
    companion object {
        private const val NOTIFICATION_ID = -5
        const val BACKUP_RESTORE_CHANNEL_ID = "closingid"
        lateinit var notification: Notification
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground()
        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_NOT_STICKY
    }

    internal var title = "App is running in background"

    internal var manager: NotificationManager? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "com.messaging.textrasms.manager"
        val channelName = "SMS Messages.."
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assert(manager != null)
        manager!!.createNotificationChannel(chan)

        title = "App is running in background"
        val intent = Intent(this, Closingservice::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this.applicationContext, 0, intent, 0)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.messages_app_icon)
            .setContentTitle(title)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setAutoCancel(true)
            .setDeleteIntent(pendingIntent)
            .build()
        notification.flags = Notification.FLAG_INSISTENT or Notification.FLAG_AUTO_CANCEL
        startForeground(2, notification)
        startForeground(this)


    }

    private fun startForeground(service: Service) {
        val service1 = Intent(this, MyBrodcastRecieverService::class.java)
        startService(service1)
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Preferences.setIntVal(applicationContext, "which", 3)
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }
}