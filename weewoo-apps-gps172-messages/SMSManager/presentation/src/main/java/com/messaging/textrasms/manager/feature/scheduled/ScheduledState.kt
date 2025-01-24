package com.messaging.textrasms.manager.feature.scheduled

import com.messaging.textrasms.manager.model.ScheduledMessage
import io.realm.RealmResults

data class ScheduledState(
    val scheduledMessages: RealmResults<ScheduledMessage>? = null
)
