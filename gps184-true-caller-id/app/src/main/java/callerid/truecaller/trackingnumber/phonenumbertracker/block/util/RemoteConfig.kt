package callerid.truecaller.trackingnumber.phonenumbertracker.block.util

import android.app.Activity
import android.os.Build
import android.util.Log
import callerid.truecaller.trackingnumber.phonenumbertracker.block.R
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils
import com.adjust.sdk.Util
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings


object RemoteConfig{


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
                Log.e("RemoteConfig"," - updated=$result")
                Constants.OPEN_APP_FLAG  = it.getBoolean("GPS184_openapp_flag")
                Constants.OPEN_APP_FLAG_BACKGROUND  = it.getBoolean("GPS184_openapp_background_flag")
                Utils.defaultValue  = it.getLong("GPS184_interstitial_taps").toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    it.all.forEach { (t, _) ->
                        Log.e( "RemoteConfig","All remoteConfig values = $t - ${it.getString(t)}")
                    }
                }
                it.fetchAndActivate()
            } else {
                Log.e("RemoteConfig","RemoteConfig - ERROR fetching ..")
            }
        }
    }
    return mFirebaseRemoteConfig
}


//    fun getRemoteconfigSubPlan(activity: Activity): FirebaseRemoteConfig? {
//
//
//
//        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
//        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
//            .setMinimumFetchIntervalInSeconds(0)
//            .build()
//        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
//        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
//        mFirebaseRemoteConfig.let {
//            it.fetchAndActivate().addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val result = task.result
//                    Log.e("RemoteConfig"," - updated=$result")
//                    Constants.SUBSCRIPTION_PLAN  = it.getString("GPS169_price_plan")
//
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        it.all.forEach { (t, _) ->
//                            Log.e( "RemoteConfig","All remoteConfig values = $t - ${it.getString(t)}")
//                        }
//                    }
//                    it.fetchAndActivate()
//                } else {
//                    Log.e("RemoteConfig","RemoteConfig - ERROR fetching ..")
//                }
//            }
//        }
//        return mFirebaseRemoteConfig
//    }
}


