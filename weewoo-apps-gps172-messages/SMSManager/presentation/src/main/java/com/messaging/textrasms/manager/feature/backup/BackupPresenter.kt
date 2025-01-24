package com.messaging.textrasms.manager.feature.backup

import android.content.Context
import android.util.Log
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkPresenter
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.interactor.PerformBackup
import com.messaging.textrasms.manager.interactor.SyncConversation
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.repository.BackupRepository
import com.messaging.textrasms.manager.util.makeToast
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BackupPresenter @Inject constructor(
    private val backupRepo: BackupRepository,
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val performBackup: PerformBackup,
    private val syncConversation: SyncConversation,

    private val permissionManager: PermissionManager
) : QkPresenter<BackupView, BackupState>(BackupState()) {

    private val storagePermissionSubject: Subject<Boolean> =
        BehaviorSubject.createDefault(permissionManager.hasStorage())

    init {
        disposables += syncConversation
        disposables += backupRepo.getBackupProgress()
            .sample(16, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribe { progress -> newState { copy(backupProgress = progress) } }

        disposables += backupRepo.getRestoreProgress()
            .sample(16, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribe { progress -> newState { copy(restoreProgress = progress) } }

        disposables += storagePermissionSubject
            .distinctUntilChanged()
            .switchMap { backupRepo.getBackups() }
            .doOnNext { backups -> newState { copy(backups = backups) } }
            .map { backups -> backups.map { it.date }.maxOrNull() ?: 0L }
            .map { lastBackup ->
                when (lastBackup) {
                    0L -> context.getString(R.string.backup_never)
                    else -> dateFormatter.getDetailedTimestamp(lastBackup)
                }
            }
            .startWith(context.getString(R.string.backup_loading))
            .subscribe { lastBackup -> newState { copy(lastBackup = lastBackup) } }
    }

    override fun bindIntents(view: BackupView) {
        super.bindIntents(view)

        view.activityVisible()
            .map { permissionManager.hasStorage() }
            .autoDispose(view.scope())
            .subscribe(storagePermissionSubject)

        view.restoreClicks()
            .withLatestFrom(
                backupRepo.getBackupProgress(),
                backupRepo.getRestoreProgress()
            )
            { _, backupProgress, restoreProgress ->
                when {
                    backupProgress.running -> context.makeToast(R.string.backup_restore_error_backup)
                    restoreProgress.running -> context.makeToast(R.string.backup_restore_error_restore)
                    !permissionManager.hasStorage() -> view.requestStoragePermission()
                    else -> view.selectFile()
                }
            }
            .autoDispose(view.scope())
            .subscribe()

        view.restoreFileSelected()
            .autoDispose(view.scope())
            .subscribe { view.confirmRestore() }

        view.restoreConfirmed()
            .withLatestFrom(view.restoreFileSelected()) { _, backup -> backup }
            .autoDispose(view.scope())
            .subscribe { backup -> RestoreBackupService.start(context, backup.path) }





        view.stopRestoreClicks()
            .autoDispose(view.scope())
            .subscribe { view.stopRestore() }

        view.stopRestoreConfirmed()
            .autoDispose(view.scope())
            .subscribe { backupRepo.stopRestore() }

        view.fabClicks()
            .autoDispose(view.scope())
            .subscribe {
                when {
                    !permissionManager.hasStorage() -> view.requestStoragePermission()
                    else -> performBackup.execute(Unit)
                }
            }
    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

}