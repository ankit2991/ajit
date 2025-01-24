package com.messaging.textrasms.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.annotation.RequiresApi
import com.messaging.textrasms.manager.interactor.SyncMessages
import com.messaging.textrasms.manager.util.Preferences
import dagger.android.AndroidInjection
import javax.inject.Inject

class DefaultSmsChangedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var prefs: Preferences

    @Inject
    lateinit var syncMessages: SyncMessages

    @Inject
    lateinit var context: Context

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        if (intent.getBooleanExtra(Telephony.Sms.Intents.EXTRA_IS_DEFAULT_SMS_APP, false)) {
            val pendingResult = goAsync()

            syncMessages.execute(Unit) {

                pendingResult.finish()
            }
        }
    }

}