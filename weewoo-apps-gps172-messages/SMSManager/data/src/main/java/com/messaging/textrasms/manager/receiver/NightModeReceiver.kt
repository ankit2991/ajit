package com.messaging.textrasms.manager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.messaging.textrasms.manager.util.NightModeManager
import dagger.android.AndroidInjection
import javax.inject.Inject

class NightModeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var nightModeManager: NightModeManager

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        nightModeManager.updateCurrentTheme()
    }
}