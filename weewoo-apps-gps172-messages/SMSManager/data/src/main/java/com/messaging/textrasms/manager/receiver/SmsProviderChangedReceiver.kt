package com.messaging.textrasms.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.messaging.textrasms.manager.interactor.SyncMessage
import dagger.android.AndroidInjection
import javax.inject.Inject

class SmsProviderChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var syncMessage: SyncMessage

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val uri = intent.data ?: return

        val pendingResult = goAsync()
        syncMessage.execute(uri) { pendingResult.finish() }
    }

}