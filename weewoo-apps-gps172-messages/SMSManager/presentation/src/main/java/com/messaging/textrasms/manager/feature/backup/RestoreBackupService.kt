package com.messaging.textrasms.manager.feature.backup

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.messaging.textrasms.manager.common.util.extensions.getLabel
import com.messaging.textrasms.manager.manager.MyNotificationManager
import com.messaging.textrasms.manager.repository.BackupRepository
import com.messaging.textrasms.manager.util.tryOrNull
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class RestoreBackupService : Service() {

    companion object {
        private const val NOTIFICATION_ID = -1

        private const val ACTION_START = "com.messaging.textrasms.manager.ACTION_START"
        private const val ACTION_STOP = "com.messaging.textrasms.manager.ACTION_STOP"
        private const val EXTRA_FILE_PATH = "com.messaging.textrasms.manager.EXTRA_FILE_PATH"

        fun start(context: Context, filePath: String) {
            val intent = Intent(context, RestoreBackupService::class.java)
                .setAction(ACTION_START)
                .putExtra(EXTRA_FILE_PATH, filePath)
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {

            }
        }
    }

    @Inject
    lateinit var backupRepo: BackupRepository

    @Inject
    lateinit var myNotificationManager: MyNotificationManager

    private val notification by lazy { myNotificationManager.getNotificationForBackup() }

    override fun onCreate() = AndroidInjection.inject(this)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent != null) {
            tryOrNull {
                super.onStartCommand(intent, flags, startId)
                tryOrNull {
                    if (intent.action != null) {
                        when (intent.action) {
                            ACTION_START -> start(intent)
                            ACTION_STOP -> stop()
                        }
                    }
                }
            }
        }
        return Service.START_STICKY
    }

    @SuppressLint("CheckResult")
    private fun start(intent: Intent) {
        tryOrNull {
            val notificationManager = NotificationManagerCompat.from(this)

            startForeground(NOTIFICATION_ID, notification.build())

            backupRepo.getRestoreProgress()
                .sample(200, TimeUnit.MILLISECONDS, true)
                .subscribeOn(Schedulers.io())
                .subscribe { progress ->
                    when (progress) {
                        is BackupRepository.Progress.Idle -> stop()

                        is BackupRepository.Progress.Running -> notification
                            .setProgress(progress.max, progress.count, progress.indeterminate)
                            .setContentText(progress.getLabel(this))
                            .let { notificationManager.notify(NOTIFICATION_ID, it.build()) }

                        else -> notification
                            .setProgress(0, 0, progress.indeterminate)
                            .setContentText(progress.getLabel(this))
                            .let { notificationManager.notify(NOTIFICATION_ID, it.build()) }
                    }
                }

            Observable.just(intent)
                .map { it.getStringExtra(EXTRA_FILE_PATH) }
                .map(backupRepo::performRestore)
                .subscribeOn(Schedulers.io())
                .subscribe({}, Timber::w)
        }
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }
}