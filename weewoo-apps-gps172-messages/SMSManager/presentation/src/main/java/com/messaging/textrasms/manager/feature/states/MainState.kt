package com.messaging.textrasms.manager.feature.states

import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.model.SearchResult
import com.messaging.textrasms.manager.repository.SyncRepository
import com.messaging.textrasms.manager.util.Preferences
import io.realm.RealmResults

data class MainState(
    val hasError: Boolean = false,
    val page: MainPage = Inbox(),
    val drawerOpen: Boolean = false,
    val syncing: SyncRepository.SyncProgress = SyncRepository.SyncProgress.Idle,
    val syncingcount: SyncRepository.Synccount = SyncRepository.Synccount.Idle,
    val defaultSms: Boolean = false,
    val newversion: Boolean = true,
    val smsPermission: Boolean = true,
    val contactPermission: Boolean = true,
    val writeexternal: Boolean = true,
    val textSizeId: Int = Preferences.TEXT_SIZE_NORMAL,
    val maxMmsSizeId: Int = 100,
    val theme: Int = 0,
    val black: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val nightModeSummary: String = "",
    val nightModeId: Int = Preferences.NIGHT_MODE_OFF,
    val LanguageId: Int = Preferences.en,
    val language: String = "English",
    val nightStart: String = "",
    val nightEnd: String = ""
)

sealed class MainPage

data class Inbox(
    val addContact: Boolean = false,
    val markPinned: Boolean = true,
    val markRead: Boolean = false,
    val data: RealmResults<Conversation>? = null,
    val datarecent: RealmResults<Conversation>? = null,
    val selected: Int = 0
) : MainPage()

data class Searching(
    val loading: Boolean = false,
    val data: List<SearchResult>? = null
) : MainPage()

data class Archived(
    val addContact: Boolean = false,
    val markPinned: Boolean = true,
    val markRead: Boolean = false,
    val data: RealmResults<Conversation>? = null,
    val selected: Int = 0
) : MainPage()
