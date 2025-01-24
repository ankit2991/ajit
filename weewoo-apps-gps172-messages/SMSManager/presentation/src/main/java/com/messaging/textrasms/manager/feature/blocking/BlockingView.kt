package com.messaging.textrasms.manager.feature.blocking

import com.messaging.textrasms.manager.common.base.QkViewContract
import io.reactivex.Observable

interface BlockingView : QkViewContract<BlockingState> {

    val blockedMessagesIntent: Observable<*>

    fun openBlockedMessages()
}
