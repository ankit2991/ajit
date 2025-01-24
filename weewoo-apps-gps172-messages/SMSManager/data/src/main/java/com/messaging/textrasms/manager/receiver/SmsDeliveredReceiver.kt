package com.messaging.textrasms.manager.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.messaging.textrasms.manager.interactor.MarkDelivered
import com.messaging.textrasms.manager.interactor.MarkDeliveryFailed
import dagger.android.AndroidInjection
import javax.inject.Inject

class SmsDeliveredReceiver : BroadcastReceiver() {

    @Inject
    lateinit var markDelivered: MarkDelivered

    @Inject
    lateinit var markDeliveryFailed: MarkDeliveryFailed

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val id = intent.getLongExtra("id", 0L)
        val address = intent.getStringExtra("add")
        when (resultCode) {
            Activity.RESULT_OK -> {
                val pendingResult = goAsync()
                markDelivered.execute(id) { pendingResult.finish() }
            }

            Activity.RESULT_CANCELED -> {
                val pendingResult = goAsync()
                markDeliveryFailed.execute(
                    MarkDeliveryFailed.Params(
                        id,
                        resultCode
                    )
                ) { pendingResult.finish() }
            }
        }
    }

    var mDebugLog = true
    var mDebugTag = "aaaaaaaaaaa"
    fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }
}
