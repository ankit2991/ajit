package com.messaging.textrasms.manager.repository

import android.content.Context
import android.net.Uri
import com.messaging.textrasms.manager.model.Message
import io.reactivex.Observable

interface SyncRepository {

    sealed class SyncProgress {
        object Idle : SyncProgress()
        data class Running(val max: Int, val progress: Int, val indeterminate: Boolean) :
            SyncProgress()

        data class Syncing(val intermidiate: Boolean = false) : SyncProgress()
    }

    sealed class Synccount {
        object Idle : Synccount()
        data class Runningcount(
            val personalcount: Int,
            val othercount: Int,
            val spamcount: Int,
            val indeterminate: Boolean
        ) : Synccount()
    }

    val syncProgress: Observable<SyncProgress>
    val synccount: Observable<Synccount>

    fun syncMessages(context: Context, fromsetting: Boolean)

    fun syncMessage(uri: Uri): Message?

    fun syncContacts()

    fun syncConversation()
    fun ischeckspam(string: String): Boolean
    fun ischeckTransactional(string: String): Boolean
    fun ischeckPromotional(string: String): Boolean

    var personalcount: Int
    var othercount: Int
    var spamcount: Int


}
