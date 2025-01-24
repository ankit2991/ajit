package com.messaging.textrasms.manager.feature.compose

import com.messaging.textrasms.manager.compat.SubscriptionInfoCompat
import com.messaging.textrasms.manager.model.Attachment
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.model.Recipient
import io.realm.RealmResults

data class ComposeState(
    val hasError: Boolean = false,
    val editingMode: Boolean = false,
    val threadId: Long = 0,
    val selectedChips: List<Recipient> = ArrayList(),
    val sendAsGroup: Boolean = true,
    val conversationtitle: String = "",
    val loading: Boolean = false,
    val query: String = "",
    val searchSelectionId: Long = -1,
    val searchSelectionPosition: Int = 0,
    val searchResults: Int = 0,
    val messages: Pair<Conversation, RealmResults<Message>>? = null,
    val selectedMessages: Int = 0,
    val scheduled: Long = 0,
    val attachments: List<Attachment> = ArrayList(),
    val attaching: Boolean = false,
    val fromrecent: Boolean = true,
    val remaining: String = "",
    val subscription: SubscriptionInfoCompat? = null,
    val canSend: Boolean = false,
    val hasstorage: Boolean = false,
    val setupgif: Boolean = false,
    val setupsticker: Boolean = false,
    val isgroup: Boolean = false
)