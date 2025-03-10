package com.messaging.textrasms.manager.repository

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.provider.Telephony
import androidx.core.content.contentValuesOf
import com.messaging.textrasms.manager.model.BackupFile
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.model.logDebug
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.QkFileObserver
import com.messaging.textrasms.manager.util.tryOrNull
import com.squareup.moshi.Moshi
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.Realm
import okio.buffer
import okio.source
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.schedule


@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val context: Context,
    private val moshi: Moshi,
    private val prefs: Preferences,
    private val syncRepo: SyncRepository
) : BackupRepository {

    companion object {
        private var BACKUP_DIRECTORY =
            Environment.getExternalStorageDirectory().toString() + "/Messages/SmsBackups"


    }

    data class Backup(
        val messages: List<BackupMessage> = listOf(),
        val messageCount: Int = 0
    )

    data class BackupMetadata(
        val messageCount: Int = 0
    )

    data class BackupMessage(
        val type: Int,
        val address: String,
        val date: Long,
        val dateSent: Long,
        val read: Boolean,
        val status: Int,
        val body: String,
        val protocol: Int,
        val serviceCenter: String?,
        val locked: Boolean,
        val subId: Int
    )

    private val backupProgress: Subject<BackupRepository.Progress> =
        BehaviorSubject.createDefault(BackupRepository.Progress.Idle())
    private val restoreProgress: Subject<BackupRepository.Progress> =
        BehaviorSubject.createDefault(BackupRepository.Progress.Idle())

    @Volatile
    private var stopFlag: Boolean = false

    override fun performBackup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BACKUP_DIRECTORY = context.getExternalFilesDir("").toString() + "/Messages/SmsBackups"
        } else {
            BACKUP_DIRECTORY =
                Environment.getExternalStorageDirectory().toString() + "/Messages/SmsBackups"
        }
        if (isBackupOrRestoreRunning()) return

        var messageCount = 0

        val backupMessages = Realm.getDefaultInstance().use { realm ->
            val messages = realm.where(Message::class.java).sort("date").findAll().createSnapshot()
            messageCount = messages.size

            messages.mapIndexed { index, message ->
                backupProgress.onNext(BackupRepository.Progress.Running(messageCount, index))
                messageToBackupMessage(message)
            }
        }

        backupProgress.onNext(BackupRepository.Progress.Saving())

        val adapter = moshi.adapter(Backup::class.java).indent("\t")
        val json = adapter.toJson(
            Backup(
                backupMessages.filter { it.body.isNotEmpty() },
                backupMessages.filter { it.body.isNotEmpty() }.size
            )
        ).toByteArray()

        try {
            val dir = File(BACKUP_DIRECTORY).apply { mkdirs() }
            val timestamp = SimpleDateFormat(
                "yyyyMMddHHmmss",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            val file = File(dir, "backup-$timestamp.json")

            FileOutputStream(file, true).use { fileOutputStream -> fileOutputStream.write(json) }
        } catch (e: Exception) {
            Timber.w(e)
        }

        backupProgress.onNext(BackupRepository.Progress.Finished())
        Timer().schedule(1000) {
            backupProgress.onNext(BackupRepository.Progress.Idle())
        }
    }

    private fun messageToBackupMessage(message: Message): BackupMessage = BackupMessage(
        type = message.boxId,
        address = message.address,
        date = message.date,
        dateSent = message.dateSent,
        read = message.read,
        status = message.deliveryStatus,
        body = message.body,
        protocol = 0,
        serviceCenter = null,
        locked = message.locked,
        subId = message.subId
    )

    override fun getBackupProgress(): Observable<BackupRepository.Progress> = backupProgress

    override fun getBackups(): Observable<List<BackupFile>> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BACKUP_DIRECTORY = context.getExternalFilesDir("").toString() + "/Messages/SmsBackups"
        } else {
            BACKUP_DIRECTORY =
                Environment.getExternalStorageDirectory().toString() + "/Messages/SmsBackups"
        }
        return QkFileObserver(BACKUP_DIRECTORY).observable
            .map { File(BACKUP_DIRECTORY).listFiles() ?: arrayOf() }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .map { files ->
                files.mapNotNull { file ->
                    val adapter = moshi.adapter(BackupMetadata::class.java)
                    val backup = tryOrNull(false) {
                        file.source().buffer().use(adapter::fromJson)
                    } ?: return@mapNotNull null

                    val path = file.path
                    val date = file.lastModified()
                    val messages = backup.messageCount
                    val size = file.length()
                    BackupFile(path, date, messages, size)
                }
            }
            .map { files -> files.sortedByDescending { file -> file.date } }
    }

    override fun performRestore(filePath: String) {
        if (isBackupOrRestoreRunning()) return

        restoreProgress.onNext(BackupRepository.Progress.Parsing())

        val file = File(filePath)
        val backup = file.source().buffer().use { source ->
            moshi.adapter(Backup::class.java).fromJson(source)
        }

        val messageCount = backup?.messages?.size ?: 0
        var errorCount = 0

        backup?.messages?.forEachIndexed { index, message ->
            if (stopFlag) {
                stopFlag = false
                logDebug("backup>>" + message.address)
                restoreProgress.onNext(BackupRepository.Progress.Idle())
                return
            }

            restoreProgress.onNext(BackupRepository.Progress.Running(messageCount, index))
            try {
                val values = contentValuesOf(
                    Telephony.Sms.TYPE to message.type,
                    Telephony.Sms.ADDRESS to message.address,
                    Telephony.Sms.DATE to message.date,
                    Telephony.Sms.DATE_SENT to message.dateSent,
                    Telephony.Sms.READ to message.read,
                    Telephony.Sms.SEEN to 1,
                    Telephony.Sms.STATUS to message.status,
                    Telephony.Sms.BODY to message.body,
                    Telephony.Sms.PROTOCOL to message.protocol,
                    Telephony.Sms.SERVICE_CENTER to message.serviceCenter,
                    Telephony.Sms.LOCKED to message.locked
                )

                if (prefs.canUseSubId.get()) {
                    values.put(Telephony.Sms.SUBSCRIPTION_ID, message.subId)
                }
                val selection =
                    Telephony.Sms.TYPE + "=? AND " + Telephony.Sms.ADDRESS + "=? AND " + Telephony.Sms.DATE + "=? AND " +
                            Telephony.Sms.DATE_SENT + "=? AND " + Telephony.Sms.BODY + "=? AND " + Telephony.Sms.SUBSCRIPTION_ID + "=?"
                val selectionArgs = arrayOf(
                    message.type.toString(),
                    message.address,
                    message.date.toString(),
                    message.dateSent.toString(),
                    message.body,
                    message.subId.toString()
                )
                val c = context.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null
                ) as Cursor
                if (c.count == 0) {
                    context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
                }
            } catch (e: Exception) {
                Timber.w(e)
                errorCount++
            }
        }

        if (errorCount > 0) {
            Timber.w(Exception("Failed to backup $errorCount/$messageCount messages"))
        }

        restoreProgress.onNext(BackupRepository.Progress.Syncing())
        syncRepo.syncMessages(context, true)

        restoreProgress.onNext(BackupRepository.Progress.Finished())
        Timer().schedule(1000) { restoreProgress.onNext(BackupRepository.Progress.Idle()) }
    }

    override fun stopRestore() {
        stopFlag = true
    }

    override fun getRestoreProgress(): Observable<BackupRepository.Progress> = restoreProgress

    private fun isBackupOrRestoreRunning(): Boolean {
        return backupProgress.blockingFirst().running || restoreProgress.blockingFirst().running
    }

}