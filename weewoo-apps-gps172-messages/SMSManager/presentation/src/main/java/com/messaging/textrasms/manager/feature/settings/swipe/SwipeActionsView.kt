package com.messaging.textrasms.manager.feature.settings.swipe

import com.messaging.textrasms.manager.common.base.QkViewContract
import io.reactivex.Observable

interface SwipeActionsView : QkViewContract<SwipeActionsState> {

    enum class Action { LEFT, RIGHT }

    fun actionClicks(): Observable<Action>
    fun actionSelected(): Observable<Int>

    fun showSwipeActions(selected: Int)

}