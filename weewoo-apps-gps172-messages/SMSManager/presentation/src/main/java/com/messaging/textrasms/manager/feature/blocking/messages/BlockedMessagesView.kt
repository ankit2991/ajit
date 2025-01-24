package com.messaging.textrasms.manager.feature.blocking.messages

import com.messaging.textrasms.manager.common.base.QkViewContract
import io.reactivex.Observable

interface BlockedMessagesView : QkViewContract<BlockedMessagesState> {

    val menuReadyIntent: Observable<Unit>
    val optionsItemIntent: Observable<Int>
    val conversationClicks: Observable<Long>
    val selectionChanges: Observable<List<Long>>
    val confirmDeleteIntent: Observable<List<Long>>

    fun clearSelection()
    fun showBlockingDialog(conversations: List<Long>, block: Boolean)
    fun showDeleteDialog(conversations: List<Long>)

}
