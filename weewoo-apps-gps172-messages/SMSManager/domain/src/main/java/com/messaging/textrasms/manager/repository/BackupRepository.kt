package com.messaging.textrasms.manager.repository

import com.messaging.textrasms.manager.model.BackupFile
import io.reactivex.Observable

interface BackupRepository {

    sealed class Progress(
        val running: Boolean = false,
        val indeterminate: Boolean = true,
        val finished: Boolean = false
    ) {
        class Idle : Progress()
        class Parsing : Progress(true)
        class Running(val max: Int, val count: Int) : Progress(true, false)
        class Saving : Progress(true)
        class Syncing : Progress(true)
        class Finished : Progress(true, false, true)
    }

    fun performBackup()

    fun getBackupProgress(): Observable<Progress>

    fun getBackups(): Observable<List<BackupFile>>

    fun performRestore(filePath: String)

    fun stopRestore()

    fun getRestoreProgress(): Observable<Progress>

}