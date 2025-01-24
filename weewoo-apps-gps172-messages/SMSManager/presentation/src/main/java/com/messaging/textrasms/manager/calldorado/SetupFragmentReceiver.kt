package com.messaging.textrasms.manager.calldorado

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.calldorado.Calldorado
import com.messaging.textrasms.manager.common.base.QKApplication
import com.messaging.textrasms.manager.common.util.Navigator
import javax.inject.Inject



class SetupFragmentReceiver : BroadcastReceiver() {

    @Inject
    lateinit var navigator: Navigator

    override fun onReceive(context: Context, intent: Intent) {

        (context.applicationContext as QKApplication).activityInjector()
        if (intent.action == "com.calldorado.android.intent.SEARCH" ||
            intent.action == "android.intent.action.PHONE_STATE"
        ) {
            Log.e("calldorado>>","receiver after call")
            Calldorado.setAftercallCustomView(context, AftercallCustomView(context))
        }
    }
}