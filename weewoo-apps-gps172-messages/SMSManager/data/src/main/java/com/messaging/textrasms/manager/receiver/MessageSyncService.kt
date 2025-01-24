package com.messaging.textrasms.manager.receiver

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.messaging.textrasms.manager.data.R
import com.messaging.textrasms.manager.interactor.SyncMessages
import com.messaging.textrasms.manager.manager.MyNotificationManager
import com.messaging.textrasms.manager.repository.SyncRepository
import dagger.android.AndroidInjection
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MessageSyncService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 100

        private const val ACTION_START = "com.messaging.textrasms.manager.ACTION_START_SYNC"
        private const val ACTION_STOP = "com.messaging.textrasms.manager.ACTION_STOP_STOP"
        private const val EXTRA_FILE_PATH = "com.messaging.textrasms.manager.EXTRA_FILE_PATH"

        fun start(context: Context) {
            val intent = Intent(context, MessageSyncService::class.java)
                .setAction(ACTION_START)
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: java.lang.Exception) {

            }


        }

        fun stop(context: Context) {
            val intent = Intent(context, MessageSyncService::class.java)
                .setAction(ACTION_STOP)
            ContextCompat.startForegroundService(context, intent)
        }
    }

    @Inject
    lateinit var syncMessages: SyncMessages

    @Inject
    lateinit var syncRepository: SyncRepository

    @Inject
    lateinit var myNotificationManager: MyNotificationManager

    private val notification by lazy { myNotificationManager.getNotificationForSunc() }

    override fun onCreate() = AndroidInjection.inject(this)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        try {


            if (intent!!.action != null) {
                when (intent.action) {
                    ACTION_START -> start()
                    ACTION_STOP -> stop()
                }
            }
        } catch (e: Exception) {

        }
        return Service.START_STICKY
    }

    @SuppressLint("CheckResult")
    private fun start() {
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        startForeground(NOTIFICATION_ID, notification.build())
        try {


            syncRepository.syncProgress
                .sample(300, TimeUnit.MILLISECONDS, true)
                .subscribeOn(Schedulers.io())
                .subscribe { progress ->
                    when (progress) {
                        is SyncRepository.SyncProgress.Idle -> stop()
                        is SyncRepository.SyncProgress.Syncing -> notification
                            .setProgress(0, 0, true)
                            .setContentText(resources.getString(R.string.backup_progress_syncing))
                            .let {
                                notificationManager.notify(
                                    NOTIFICATION_ID,
                                    notification.build()
                                )
                            }
                        is SyncRepository.SyncProgress.Running -> notification
                            .setProgress(progress.max, progress.progress, progress.indeterminate)
                            .setContentText(
                                resources.getString(
                                    R.string.backup_progress_running,
                                    progress.progress,
                                    progress.max
                                )
                            )
                            .let {
                                notificationManager.notify(
                                    NOTIFICATION_ID,
                                    notification.build()
                                )
                            }

                        else -> notification
                            .setProgress(0, 0, true)
                            .setContentText((resources.getString(R.string.backup_progress_syncing)))
                            .let {
                                notificationManager.notify(
                                    NOTIFICATION_ID,
                                    notification.build()
                                )
                            }
                    }
                }
        } catch (E: Exception) {

        }
    }

    fun stop() {
        stopForeground(true)
        stopSelf()
    }


}