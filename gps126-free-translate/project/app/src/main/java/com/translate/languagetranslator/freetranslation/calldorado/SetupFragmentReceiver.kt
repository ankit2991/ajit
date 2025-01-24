package com.translate.languagetranslator.freetranslation.calldorado

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.calldorado.Calldorado
import com.code4rox.adsmanager.MaxAppOpenAdManager
import com.translate.languagetranslator.freetranslation.utils.Constants


class SetupFragmentReceiver : BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {


        if (intent.action == "com.calldorado.android.intent.SEARCH" ||
            intent.action == "android.intent.action.PHONE_STATE"
        ) {
            MaxAppOpenAdManager.IS_CALLDORADO_SCREEN = true
            Log.e("calldorado>>","receiver after call")
            Calldorado.setAftercallCustomView(context, AftercallCustomView(context))
        }
    }
}
