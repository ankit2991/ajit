package com.messaging.textrasms.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.messaging.textrasms.manager.interactor.RetrySending
import com.messaging.textrasms.manager.repository.MessageRepository
import dagger.android.AndroidInjection
import javax.inject.Inject

class SendSmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var messageRepo: MessageRepository

    @Inject
    lateinit var retrySending: RetrySending


    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val messageId = intent.getLongExtra("id", -1L).takeIf { it >= 0 } ?: return

        val result = goAsync()
        retrySending.execute(messageId) { result.finish() }


    }

}
