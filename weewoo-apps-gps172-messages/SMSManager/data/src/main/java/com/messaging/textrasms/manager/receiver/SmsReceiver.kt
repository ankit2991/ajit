package com.messaging.textrasms.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms
import android.util.Log
import com.messaging.textrasms.manager.interactor.ReceiveSms
import dagger.android.AndroidInjection
import javax.inject.Inject

class SmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var receiveMessage: ReceiveSms

    override fun onReceive(context: Context, intent: Intent) {
        Thread {
            logDebug("getreceive" + ">>>>>>>>>>>>>>>>>>>>>>receive")
            AndroidInjection.inject(this, context)
            Sms.Intents.getMessagesFromIntent(intent)?.let { messages ->
                val subId = intent.extras?.getInt("subscription", -1) ?: -1
                receiveMessage.execute(ReceiveSms.Params(subId, messages, context))
            }
        }.start()
    }

    var mDebugLog = true
    var mDebugTag = "RECEIVER"
    fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

}