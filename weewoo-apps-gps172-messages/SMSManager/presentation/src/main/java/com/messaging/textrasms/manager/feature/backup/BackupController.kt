package com.messaging.textrasms.manager.feature.backup

import android.Manifest
import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.clicks
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkController
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.extensions.getLabel
import com.messaging.textrasms.manager.common.util.extensions.setBackgroundTint
import com.messaging.textrasms.manager.common.util.extensions.setPositiveButton
import com.messaging.textrasms.manager.common.util.extensions.setTint
import com.messaging.textrasms.manager.common.widget.PreferenceView
import com.messaging.textrasms.manager.feature.Activities.MainActivity.Companion.backuprun
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.model.BackupFile
import com.messaging.textrasms.manager.repository.BackupRepository
import com.messaging.textrasms.manager.util.makeToast
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.backup_controller.*
import kotlinx.android.synthetic.main.backup_list_dialog.view.*
import kotlinx.android.synthetic.main.preference_view.view.*
import javax.inject.Inject

class BackupController : QkController<BackupView, BackupState, BackupPresenter>(), BackupView {


    @Inject
    lateinit var adapter: BackupAdapter

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    override lateinit var presenter: BackupPresenter

    private val activityVisibleSubject: Subject<Unit> = PublishSubject.create()
    private val confirmRestoreSubject: Subject<Unit> = PublishSubject.create()
    private val stopRestoreSubject: Subject<Unit> = PublishSubject.create()

    private val backupFilesDialog by lazy {
        val view = View.inflate(activity, R.layout.backup_list_dialog, null)
            .apply { files.adapter = adapter.apply { emptyView = empty } }

        AlertDialog.Builder(activity!!)
            .setView(view)
            .setCancelable(true)
            .create()
    }

    private val confirmRestoreDialog by lazy {
        AlertDialog.Builder(activity!!)
            .setTitle(R.string.backup_restore_confirm_title)
            .setMessage(R.string.backup_restore_confirm_message)
            .setPositiveButton(R.string.backup_restore_title, confirmRestoreSubject)
            .setNegativeButton(R.string.button_cancel, null)
            .create()
    }

    private val stopRestoreDialog by lazy {
        AlertDialog.Builder(activity!!)
            .setTitle(R.string.backup_restore_stop_title)
            .setMessage(R.string.backup_restore_stop_message)
            .setPositiveButton(R.string.button_stop, stopRestoreSubject)
            .setNegativeButton(R.string.button_cancel, null)
            .create()
    }

    init {
        appComponent.inject(this)
        layoutRes = R.layout.backup_controller
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.backup_title)
        showBackButton(true)
    }

    override fun onViewCreated() {
        super.onViewCreated()

        themedActivity?.colors?.theme()?.let { theme ->
            progressBar.indeterminateTintList = ColorStateList.valueOf(theme.theme)
            progressBar.progressTintList = ColorStateList.valueOf(theme.theme)
            fab.setBackgroundTint(theme.theme)
            fabIcon.setTint(theme.textPrimary)
            fabLabel.setTextColor(theme.textPrimary)
        }

        // Make the list titles bold
        linearLayout.children
            .mapNotNull { it as? PreferenceView }
            .map { it.titleView }
            .forEach { it.setTypeface(it.typeface, Typeface.BOLD) }
        backup?.settint()
        restore?.settint()
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        activityVisibleSubject.onNext(Unit)
    }

    var isFinish = false
    override fun render(state: BackupState) {
        when {
            state.backupProgress.running -> {
                progressIcon.setImageResource(R.drawable.ic_file_upload_black_24dp)
                progressTitle.setText(R.string.backup_backing_up)
                progressSummary.text = state.backupProgress.getLabel(activity!!)
                progressSummary.isVisible = progressSummary.text.isNotEmpty()
                progressCancel.isVisible = false
                val running = (state.backupProgress as? BackupRepository.Progress.Running)
                progressBar.isVisible = state.backupProgress.indeterminate || running?.max ?: 0 > 0
                progressBar.isIndeterminate = state.backupProgress.indeterminate
                progressBar.max = running?.max ?: 0
                progressBar.progress = running?.count ?: 0
                progress.isVisible = true
                fab.isVisible = false
                backuprun = true
                Log.e(
                    "------>finished",
                    state.backupProgress.finished.toString() + " running" + state.backupProgress.running.toString()
                )
                if (state.backupProgress.finished && !isFinish) {
                    isFinish = true
                    activity!!.makeToast("Backup successful")
                }
            }

            state.restoreProgress.running -> {
                progressIcon.setImageResource(R.drawable.ic_file_download_black_24dp)
                progressTitle.setText(R.string.backup_restoring)
                progressSummary.text = state.restoreProgress.getLabel(activity!!)
                progressSummary.isVisible = progressSummary.text.isNotEmpty()
                progressCancel.isVisible = true
                val running = (state.restoreProgress as? BackupRepository.Progress.Running)
                progressBar.isVisible = state.restoreProgress.indeterminate || running?.max ?: 0 > 0
                progressBar.isIndeterminate = state.restoreProgress.indeterminate
                progressBar.max = running?.max ?: 0
                progressBar.progress = running?.count ?: 0
                progress.isVisible = true
                fab.isVisible = false
                backuprun = true
                if (state.restoreProgress.finished) {
                    activity!!.makeToast("Restore successful")
                }

            }


            else -> {
                progress.isVisible = false
                fab.isVisible = true
                backuprun = false
            }
        }

        backup.summary = state.lastBackup

        adapter.data = state.backups
    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

    override fun activityVisible(): Observable<*> = activityVisibleSubject

    override fun restoreClicks(): Observable<*> = restore.clicks()

    override fun restoreFileSelected(): Observable<BackupFile> = adapter.backupSelected
        .doOnNext { backupFilesDialog.dismiss() }

    override fun restoreConfirmed(): Observable<*> = confirmRestoreSubject

    override fun stopRestoreClicks(): Observable<*> = progressCancel.clicks()

    override fun stopRestoreConfirmed(): Observable<*> = stopRestoreSubject

    override fun fabClicks(): Observable<*> = fab.clicks()

    override fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    override fun selectFile() = backupFilesDialog.show()

    override fun confirmRestore() = confirmRestoreDialog.show()

    override fun stopRestore() = stopRestoreDialog.show()


}