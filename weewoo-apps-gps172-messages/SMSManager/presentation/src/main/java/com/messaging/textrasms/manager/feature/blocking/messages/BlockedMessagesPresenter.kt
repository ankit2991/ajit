package com.messaging.textrasms.manager.feature.blocking.messages

import android.util.Log
import com.messaging.textrasms.manager.InterstitialConditionDisplay
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.common.base.QkPresenter
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.interactor.DeleteConversations
import com.messaging.textrasms.manager.interactor.MarkUnblocked
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.rxkotlin.withLatestFrom
import javax.inject.Inject

class BlockedMessagesPresenter @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val deleteConversations: DeleteConversations,
    private val blockingManager: BlockingClient,
    private val markUnblocked: MarkUnblocked,
    private val navigator: Navigator
) : QkPresenter<BlockedMessagesView, BlockedMessagesState>(
    BlockedMessagesState(
        data = conversationRepo.getBlockedConversationsAsync()
    )
) {

    override fun bindIntents(view: BlockedMessagesView) {
        super.bindIntents(view)

        view.menuReadyIntent
            .autoDispose(view.scope())
            .subscribe { newState { copy() } }

        view.optionsItemIntent
            .withLatestFrom(view.selectionChanges) { itemId, conversations ->
                when (itemId) {
                    R.id.block -> {
                        val addresses = conversations.toLongArray()
                            .let { conversationRepo.getConversations(*it) }
                            .flatMap { conversation -> conversation.recipients }
                            .map { it.address }
                            .distinct()
                        markUnblocked.execute(conversations)
                        blockingManager.unblock(addresses).subscribe()
//                            view.showBlockingDialog(conversations, false)
                        view.clearSelection()
                    }
                    R.id.delete -> {
                        view.showDeleteDialog(conversations)
                    }
                }

            }
            .autoDispose(view.scope())
            .subscribe()

        view.confirmDeleteIntent
            .autoDispose(view.scope())
            .subscribe { conversations ->
                deleteConversations.execute(conversations)
                view.clearSelection()
            }

        view.conversationClicks
            .autoDispose(view.scope())
            .subscribe { threadId ->
                navigator.showConversation(threadId)
                InterstitialConditionDisplay.getInstance().increaseClicked()
            }

        view.selectionChanges
            .autoDispose(view.scope())
            .subscribe { selection -> newState { copy(selected = selection.size) } }
    }

}
