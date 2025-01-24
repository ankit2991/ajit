package com.messaging.textrasms.manager.feature.Views

import android.content.Intent
import com.messaging.textrasms.manager.common.base.QkView
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.model.Conversation
import io.reactivex.Observable
import io.realm.RealmResults

interface MainView : QkView<MainState> {

    val onNewIntentIntent: Observable<Intent>
    val activityResumedIntent: Observable<Boolean>
    val queryChangedIntent: Observable<CharSequence>
    val composeIntent: Observable<Unit>
    val drawerOpenIntent: Observable<Boolean>
    val homeIntent: Observable<*>
    val navigationIntent: Observable<NavItem>
    val optionsItemIntent: Observable<Int>
    val conversationsSelectedIntent: Observable<List<Long>>
    val confirmDeleteIntent: Observable<List<Long>>

    val undoArchiveIntent: Observable<Unit>
    val snackbarButtonIntent: Observable<Unit>
    val loadddata: Observable<Boolean>
    val modeclick: Observable<Unit>
    val languageclick: Observable<Unit>
    fun nightModeSelected(): Observable<Int>
    fun LanguageClicked(): Observable<Int>

    fun showInterAdsFromViewmodel()
    fun requestDefaultSms()
    fun requestPermissions()
    fun requestStoragePermissions()
    fun clearSearch()
    fun clearSelection()
    fun themeChanged()
    fun showBlockingDialog(conversations: List<Long>, block: Boolean)
    fun showDeleteDialog(conversations: List<Long>)
    fun selectionAll(conversations: RealmResults<Conversation>)
    fun showArchivedSnackbar()
    fun showNightModeDialog()
    fun drawerclose()
    fun showLanguage()
    fun drawerevent(value: Boolean)
}

enum class NavItem { SHARE, RATEUS, BACK, INBOX, TRANSACTIONAL, PROMOTIONS, SPAM, Filter, ARCHIVED, BACKUP, SCHEDULED, BLOCKING, CALLDORADO, SETTINGS, REMOVEADS, HELP, INVITE, BENEFIT, PRICE, GROUP }