package com.messaging.textrasms.manager.feature.qkreply

import com.messaging.textrasms.manager.compat.SubscriptionInfoCompat
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.model.Message
import io.realm.RealmResults

data class QkReplyState(
    val hasError: Boolean = false,
    val threadId: Long = 0,
    val title: String = "",
    val expanded: Boolean = false,
    val data: Pair<Conversation, RealmResults<Message>>? = null,
    val remaining: String = "",
    val subscription: SubscriptionInfoCompat? = null,
    val canSend: Boolean = false
)