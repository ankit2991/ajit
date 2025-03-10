package com.messaging.textrasms.manager.common.util

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.feature.compose.ComposeActivity
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import com.messaging.textrasms.manager.util.GlideApp
import com.messaging.textrasms.manager.util.tryOrNull
import me.leolin.shortcutbadger.ShortcutBadger
import javax.inject.Inject

class ShortcutManagerImpl @Inject constructor(
    private val context: Context,
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository
) : com.messaging.textrasms.manager.manager.ShortcutManager {

    override fun updateBadge() {
        val count = messageRepo.getUnreadCount().toInt()
        ShortcutBadger.applyCount(context, count)
    }

    override fun updateShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager =
                context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            if (shortcutManager.isRateLimitingActive) return

            shortcutManager.dynamicShortcuts = conversationRepo.getTopConversations()
                .take(shortcutManager.maxShortcutCountPerActivity - shortcutManager.manifestShortcuts.size)
                .map { conversation ->
                    createShortcutForConversation(
                        conversation,
                        shortcutManager
                    )
                }
        }
    }

    @TargetApi(25)
    private fun createShortcutForConversation(
        conversation: Conversation,
        shortcutManager: ShortcutManager
    ): ShortcutInfo {
        val icon = when {
            conversation.recipients.size == 1 -> {
                val address = conversation.recipients.first()!!.address
                val request = GlideApp.with(context)
                    .asBitmap()
                    .circleCrop()
                    .load("tel:$address")
                    .submit(shortcutManager.iconMaxWidth, shortcutManager.iconMaxHeight)
                val bitmap = tryOrNull(false) { request.get() }

                if (bitmap != null) Icon.createWithBitmap(bitmap)
                else Icon.createWithResource(context, R.mipmap.ic_shortcut_person)
            }

            else -> Icon.createWithResource(context, R.mipmap.ic_shortcut_people)
        }

        val intent = Intent(context, ComposeActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .putExtra("threadId", conversation.id)

        return ShortcutInfo.Builder(context, "${conversation.id}")
            .setShortLabel(conversation.getTitle())
            .setLongLabel(conversation.getTitle())
            .setIcon(icon)
            .setIntent(intent)
            .build()
    }

}