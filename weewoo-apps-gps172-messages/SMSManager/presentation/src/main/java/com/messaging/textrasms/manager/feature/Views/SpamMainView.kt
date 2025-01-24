package com.messaging.textrasms.manager.feature.Views

import android.content.Intent
import com.messaging.textrasms.manager.common.base.QkView
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.model.Conversation
import io.reactivex.Observable
import io.realm.RealmResults

interface SpamMainView : QkView<MainState> {

    val onNewIntentIntent: Observable<Intent>
    val activityResumedIntent: Observable<Boolean>
    val homeIntent: Observable<*>
    val syncunknown: Observable<*>
    val optionsItemIntent: Observable<Int>
    val conversationsSelectedIntent: Observable<List<Long>>
    val confirmDeleteIntent: Observable<List<Long>>
    val sorted: Observable<Boolean>
    val undoArchiveIntent: Observable<Unit>
    val snackbarButtonIntent: Observable<Unit>
    fun requestStoragePermissions()
    fun requestDefaultSms()
    fun requestPermissions()
    fun clearSearch()
    fun clearSelection()
    fun themeChanged()
    fun showBlockingDialog(conversations: List<Long>, block: Boolean)
    fun showDeleteDialog(conversations: List<Long>)
    fun selectionAll(conversations: RealmResults<Conversation>)
    fun showArchivedSnackbar()
    fun Showsorting()
    fun previoussorting()
}
