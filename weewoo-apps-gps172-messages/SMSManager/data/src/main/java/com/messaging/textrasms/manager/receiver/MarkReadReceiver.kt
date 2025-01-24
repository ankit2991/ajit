package com.messaging.textrasms.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.messaging.textrasms.manager.interactor.MarkRead
import dagger.android.AndroidInjection
import javax.inject.Inject

class MarkReadReceiver : BroadcastReceiver() {

    @Inject
    lateinit var markRead: MarkRead

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)
        markRead.execute(listOf(threadId)) { pendingResult.finish() }
    }

}