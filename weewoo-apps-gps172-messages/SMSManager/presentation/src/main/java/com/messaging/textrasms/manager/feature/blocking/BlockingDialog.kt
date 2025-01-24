package com.messaging.textrasms.manager.feature.blocking

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.interactor.MarkBlocked
import com.messaging.textrasms.manager.interactor.MarkUnblocked
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.util.Preferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BlockingDialog @Inject constructor(
    private val blockingManager: BlockingClient,
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val prefs: Preferences,
    private val markBlocked: MarkBlocked,
    private val markUnblocked: MarkUnblocked
) {

    fun show(activity: Activity, conversationIds: List<Long>, block: Boolean) = GlobalScope.launch {
        val addresses = conversationIds.toLongArray()
            .let { conversationRepo.getConversations(*it) }
            .flatMap { conversation -> conversation.recipients }
            .map { it.address }
            .distinct()

        if (addresses.isEmpty()) {
            return@launch
        }

        if (blockingManager.getClientCapability() == BlockingClient.Capability.BLOCK_WITHOUT_PERMISSION) {
            if (block) {
                markBlocked.execute(
                    MarkBlocked.Params(
                        conversationIds,
                        prefs.blockingManager.get(),
                        null
                    )
                )
                blockingManager.block(addresses).subscribe()
            } else {
                markUnblocked.execute(conversationIds)
                blockingManager.unblock(addresses).subscribe()
            }
        } else if (block == allBlocked(addresses)) {
            when (block) {
                true -> markBlocked.execute(
                    MarkBlocked.Params(
                        conversationIds,
                        prefs.blockingManager.get(),
                        null
                    )
                )
                false -> markUnblocked.execute(conversationIds)
            }
        } else {
            showDialog(activity, conversationIds, addresses, block)
        }
    }

    private fun allBlocked(addresses: List<String>): Boolean = addresses.all { address ->
        blockingManager.getAction(address).blockingGet() is BlockingClient.Action.Block
    }

    private suspend fun showDialog(
        activity: Activity,
        conversationIds: List<Long>,
        addresses: List<String>,
        block: Boolean
    ) = withContext(MainScope().coroutineContext) {
        val res = when (block) {
            true -> R.plurals.blocking_block_external
            false -> R.plurals.blocking_unblock_external
        }

        val manager = context.getString(
            when (prefs.blockingManager.get()) {
                Preferences.BLOCKING_MANAGER_SIA -> R.string.blocking_manager_sia_title
                Preferences.BLOCKING_MANAGER_CC -> R.string.blocking_manager_call_control_title
                else -> R.string.blocking_manager_sms_title
            }
        )

        val message = context.resources.getQuantityString(res, addresses.size, manager)

        AlertDialog.Builder(activity)
            .setTitle(
                when (block) {
                    true -> R.string.blocking_block_title
                    false -> R.string.blocking_unblock_title
                }
            )
            .setMessage(message)
            .setPositiveButton(R.string.button_continue) { _, _ ->
                if (block) {
                    markBlocked.execute(
                        MarkBlocked.Params(
                            conversationIds,
                            prefs.blockingManager.get(),
                            null
                        )
                    )
                    blockingManager.block(addresses).subscribe()
                } else {
                    markUnblocked.execute(conversationIds)
                    blockingManager.unblock(addresses).subscribe()
                }
            }
            .setNegativeButton(R.string.button_cancel) { _, _ -> }
            .create()
            .show()
    }

}
