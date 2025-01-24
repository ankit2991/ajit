package com.messaging.textrasms.manager.utils

import android.os.Build
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.RemoteConfigHelper

object RemoteConfig {


    fun getRemoteconfig(): FirebaseRemoteConfig? {


        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        mFirebaseRemoteConfig.let {
            it.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    Log.e("RemoteConfig", " - updated=$result")
                    Constants.RATE_PROMPT = it.getBoolean(RemoteConfigHelper.GPS172_RATE_APP_PROMPT)
                    Constants.PRICE_PLAN = it.getString(RemoteConfigHelper.GPS172_PRICE_PLANS)

//                    Log.e("remoteConfig","GPS172_PRICE_PLANS>>"+it.getString(RemoteConfigHelper.GPS172_PRICE_PLANS))
//                    Log.e("remoteConfig","GPS172_RATE_APP_PROMPT>>"+it.getBoolean(RemoteConfigHelper.GPS172_RATE_APP_PROMPT))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        it.all.forEach { (t, _) ->

                        }
                    }
                } else {
                    Log.e("RemoteConfig", "RemoteConfig - ERROR fetching ..")
                }
            }
        }
        return mFirebaseRemoteConfig
    }
}


