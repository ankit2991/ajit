package com.messaging.textrasms.manager.feature.backup

import com.messaging.textrasms.manager.model.BackupFile
import com.messaging.textrasms.manager.repository.BackupRepository

data class BackupState(
    val backupProgress: BackupRepository.Progress = BackupRepository.Progress.Idle(),
    val restoreProgress: BackupRepository.Progress = BackupRepository.Progress.Idle(),
    val lastBackup: String = "",
    val backups: List<BackupFile> = listOf(),
    val upgraded: Boolean = true
)