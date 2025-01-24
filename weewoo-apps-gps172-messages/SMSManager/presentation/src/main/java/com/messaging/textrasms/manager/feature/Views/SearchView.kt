package com.messaging.textrasms.manager.feature.Views

import android.content.Intent
import com.messaging.textrasms.manager.common.base.QkView
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.model.Conversation
import io.reactivex.Observable
import io.realm.RealmResults

interface SearchView : QkView<MainState> {

    val onNewIntentIntent: Observable<Intent>
    val activityResumedIntent: Observable<Boolean>
    val queryChangedIntent: Observable<CharSequence>
    val backPressedSubject: Observable<String>
    val homeIntent: Observable<*>
    val optionsItemIntent: Observable<Int>
    val queryClearedIntent: Observable<*>
    val conversationsSelectedIntent: Observable<List<Long>>
    val confirmDeleteIntent: Observable<List<Long>>
    val undoArchiveIntent: Observable<Unit>
    val snackbarButtonIntent: Observable<Unit>
    fun clearSearch()
    fun clearSelection()
    fun themeChanged()
    fun clearQuery()

    fun requestDefaultSms()
    fun requestPermissions()
    fun requestStoragePermissions()
    fun showBlockingDialog(conversations: List<Long>, block: Boolean)
    fun showDeleteDialog(conversations: List<Long>)
    fun selectionAll(conversations: RealmResults<Conversation>)
    fun showArchivedSnackbar()
}
