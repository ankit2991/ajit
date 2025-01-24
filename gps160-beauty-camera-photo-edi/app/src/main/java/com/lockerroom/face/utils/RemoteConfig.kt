package com.lockerroom.face.utils

import android.os.Build
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.lockerroom.face.R
import com.lockerroom.face.utils.MyConstant.EXIT_INTERSTITIAL
import com.lockerroom.face.utils.MyConstant.INTERSTITIAL_TAP
import com.lockerroom.face.utils.MyConstant.MAIN_MENU_INTERSTITIAL
import com.lockerroom.face.utils.MyConstant.SAVING_INTERSTITIAL
import com.lockerroom.face.utils.MyConstant.onboarding_flag
import com.lockerroom.face.utils.MyConstant.paymentcard_flag
import com.lockerroom.face.utils.MyConstant.price_plan

object RemoteConfig {
    fun getRemoteconfig(): FirebaseRemoteConfig {

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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        onboarding_flag = it.getBoolean("GPS160_onboarding_flag")
                        paymentcard_flag = it.getBoolean("GPS160_paymentcard_flag")
                        price_plan = it.getString("GPS160_price_plan")
                        MAIN_MENU_INTERSTITIAL = it.getBoolean("GPS160_init_interstitial")
                        EXIT_INTERSTITIAL = it.getBoolean("GPS160_exit_interstitial")
                        SAVING_INTERSTITIAL = it.getBoolean("GPS160_saving_interstitial")
//                        INTERSTITIAL_TAP = it.getLong("GPS160_interstitial_taps").toInt()

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            Log.e( "RemoteConfig","All remoteConfig values = $ ${it.getString("GPS079_onboarding_flag")}")
//                            Log.e( "RemoteConfig","All remoteConfig values = $ ${it.getString("GPS079_paymentcard_flag")}")
                            Log.e( "RemoteConfig","All remoteConfig values = $ $onboarding_flag")
                            Log.e( "RemoteConfig","All remoteConfig values = $ $paymentcard_flag")
                            Log.e( "RemoteConfig","All remoteConfig values = $ $INTERSTITIAL_TAP")
                            Log.e( "RemoteConfig","All remoteConfig values = $ $price_plan")
                            Log.e( "RemoteConfig","All remoteConfig values = $ $MAIN_MENU_INTERSTITIAL")
                            Log.e( "RemoteConfig","All remoteConfig values = $ $EXIT_INTERSTITIAL")
                            Log.e( "RemoteConfig","All remoteConfig values = $ $SAVING_INTERSTITIAL")
//                        it.all.forEach { (t, _) ->
//                            Log.e( "RemoteConfig","All remoteConfig values = $t - ${it.getBoolean(t)}")
//                        }
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