package com.messaging.textrasms.manager.migration

import android.content.Context
import com.messaging.textrasms.manager.blocking.QksmsBlockingClient
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.versionCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class QkMigration @Inject constructor(
    private val context: Context,
    private val prefs: Preferences,
    private val conversationRepo: ConversationRepository,
    private val qksmsBlockingClient: QksmsBlockingClient
) {
    fun performMigration() {
        GlobalScope.launch {
            try {
                val oldVersion = prefs.version.get()
                if (oldVersion < 39) {

                    upgradeTo370()
                }
                prefs.version.set(context.versionCode)
            } catch (e: Throwable) {
            }
        }
    }

    private fun upgradeTo370() {
        prefs.changelogVersion.set(prefs.version.get())

        if (prefs.sia.get()) {
            prefs.blockingManager.set(Preferences.BLOCKING_MANAGER_SIA)
            prefs.sia.delete()
        }

        val addresses = conversationRepo.getBlockedConversations()
            .flatMap { conversation -> conversation.recipients }
            .map { recipient -> recipient.address }
            .distinct()

        qksmsBlockingClient.block(addresses).blockingAwait()
    }

}