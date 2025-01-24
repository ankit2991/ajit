package com.messaging.textrasms.manager.feature.blocking

import android.content.Context
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkPresenter
import com.messaging.textrasms.manager.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class BlockingPresenter @Inject constructor(
    context: Context,
    prefs: Preferences
) : QkPresenter<BlockingView, BlockingState>(BlockingState()) {

    init {
        disposables += prefs.blockingManager.asObservable()
            .map { client ->
                when (client) {
                    Preferences.BLOCKING_MANAGER_SIA -> R.string.blocking_manager_sia_title
                    Preferences.BLOCKING_MANAGER_CC -> R.string.blocking_manager_call_control_title
                    else -> R.string.blocking_manager_sms_title
                }
            }
            .map(context::getString)
            .subscribe { manager -> newState { copy(blockingManager = manager) } }

        disposables += prefs.drop.asObservable()
            .subscribe { enabled -> newState { copy(dropEnabled = enabled) } }
    }

    override fun bindIntents(view: BlockingView) {
        super.bindIntents(view)

        view.blockedMessagesIntent
            .autoDispose(view.scope())
            .subscribe { view.openBlockedMessages() }
    }

}
