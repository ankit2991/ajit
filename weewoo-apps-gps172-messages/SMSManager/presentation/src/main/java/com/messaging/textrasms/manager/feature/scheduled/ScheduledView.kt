package com.messaging.textrasms.manager.feature.scheduled

import com.messaging.textrasms.manager.common.base.QkView
import io.reactivex.Observable

interface ScheduledView : QkView<ScheduledState> {

    val messageClickIntent: Observable<Long>
    val messageMenuIntent: Observable<Int>
    val composeIntent: Observable<*>

    fun showMessageOptions()

}
