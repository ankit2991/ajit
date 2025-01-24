package com.messaging.textrasms.manager.interactor

import android.net.Uri
import android.util.Log
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.SyncRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SyncMessage @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val syncManager: SyncRepository,
    private val updateBadge: UpdateBadge
) : Interactor<Uri>() {

    override fun buildObservable(params: Uri): Flowable<*> {
        return Flowable.just(params)
            .doOnNext { logDebug("checkhnfjdfn" + "msg sync") }
            .mapNotNull { uri -> syncManager.syncMessage(uri) }
            .doOnNext { message -> conversationRepo.updateConversations(message.threadId) }
            .flatMap { updateBadge.buildObservable(Unit) } // Update the badge
    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(msg, msg)
    }
}