package com.messaging.textrasms.manager.feature.settings

import com.messaging.textrasms.manager.repository.SyncRepository
import com.messaging.textrasms.manager.util.Preferences

data class SettingsState(
    val theme: Int = 0,
    val nightModeSummary: String = "",
    val nightModeId: Int = Preferences.NIGHT_MODE_OFF,
    val nightStart: String = "",
    val nightEnd: String = "",
    val black: Boolean = false,
    val autoColor: Boolean = true,
    val autoEmojiEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val sendDelaySummary: String = "",
    val sendDelayId: Int = 0,
    val deliveryEnabled: Boolean = true,
    val signature: String = "",
    val textSizeSummary: String = "",
    val textSizeId: Int = Preferences.TEXT_SIZE_NORMAL,
    val LanguageId: Int = Preferences.en,
    val systemFontEnabled: Boolean = false,
    val splitSmsEnabled: Boolean = false,
    val stripUnicodeEnabled: Boolean = false,
    val mobileOnly: Boolean = false,
    val longAsMms: Boolean = false,
    val maxMmsSizeSummary: String = "100KB",
    val maxMmsSizeId: Int = 100,
    val loadddata: Boolean = false,
    val language: String = "English",
    val isset: Boolean = false,
    val syncProgress: SyncRepository.SyncProgress = SyncRepository.SyncProgress.Idle


)