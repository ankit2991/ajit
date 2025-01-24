package com.messaging.textrasms.manager.feature.blocking.messages

import com.messaging.textrasms.manager.model.Conversation
import io.realm.RealmResults

data class BlockedMessagesState(
    val data: RealmResults<Conversation>? = null,
    val selected: Int = 0
)
