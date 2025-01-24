package com.messaging.textrasms.manager.feature.scheduled

import android.content.Context
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkViewModel
import com.messaging.textrasms.manager.common.util.ClipboardUtils
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.interactor.SendScheduledMessage
import com.messaging.textrasms.manager.repository.ScheduledMessageRepository
import com.messaging.textrasms.manager.util.makeToast
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.rxkotlin.withLatestFrom
import javax.inject.Inject

class ScheduledViewModel @Inject constructor(
    private val context: Context,
    private val navigator: Navigator,
    private val scheduledMessageRepo: ScheduledMessageRepository,
    private val sendScheduledMessage: SendScheduledMessage
) : QkViewModel<ScheduledView, ScheduledState>(
    ScheduledState(
        scheduledMessages = scheduledMessageRepo.getScheduledMessages()
    )
) {

    override fun bindView(view: ScheduledView) {
        super.bindView(view)

        view.messageClickIntent
            .autoDispose(view.scope())
            .subscribe { view.showMessageOptions() }

        view.messageMenuIntent
            .withLatestFrom(view.messageClickIntent) { itemId, messageId ->
                when (itemId) {
                    0 -> sendScheduledMessage.execute(messageId)
                    1 -> scheduledMessageRepo.getScheduledMessage(messageId)?.let { message ->
                        ClipboardUtils.copy(context, message.body)
                        context.makeToast(R.string.toast_copied)
                    }
                    2 -> scheduledMessageRepo.deleteScheduledMessage(messageId)
                }
                Unit
            }
            .autoDispose(view.scope())
            .subscribe()

        view.composeIntent
            .autoDispose(view.scope())
            .subscribe { navigator.showCompose() }
    }

}