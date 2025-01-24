package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.manager.ShortcutManager
import com.messaging.textrasms.manager.manager.WidgetManager
import io.reactivex.Flowable
import javax.inject.Inject

class UpdateBadge @Inject constructor(
    private val shortcutManager: ShortcutManager,
    private val widgetManager: WidgetManager
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> {
        return Flowable.just(params)
            .doOnNext { shortcutManager.updateBadge() }
            .doOnNext { widgetManager.updateUnreadCount() }
    }

}