package com.translate.languagetranslator.freetranslation.utils

import android.os.Build
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object RemoteConfig {


    fun getRemoteconfig(): FirebaseRemoteConfig? {


        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
//        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        mFirebaseRemoteConfig.let {
            it.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    Log.e("RemoteConfig", " - updated=$result")

                    Constants.NATIVE_REFRESH_TIME = it.getLong("GPS126_native_refreshRate").toInt()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        it.all.forEach { (t, _) ->
                            Log.e(
                                "RemoteConfig",
                                "All remoteConfig values = $t - ${it.getLong(t)}"
                            )
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


