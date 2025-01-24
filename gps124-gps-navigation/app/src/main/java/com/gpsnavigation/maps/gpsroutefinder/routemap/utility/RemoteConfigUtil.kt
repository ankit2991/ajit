package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import androidx.annotation.Keep
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder
import com.google.gson.Gson

@Keep
object RemoteConfigUtil {

    const val AD_SETTING = "gps_navigation"
    private const val FETCH_INTERVAL_TIME = 0L

    fun initializeConfigs(): FirebaseRemoteConfig {
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = Builder()
                //.setDeveloperModeEnabled(BuildConfig.DEBUG) //deprecated and useless with setMinimumFetchIntervalInSeconds
                .setMinimumFetchIntervalInSeconds(FETCH_INTERVAL_TIME)
                .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.setDefaultsAsync(mapOf(
            AD_SETTING to Gson().toJson(
                RemoteAdValues()
            )))
        return firebaseRemoteConfig
    }

}

@Keep
data class RemoteAdValues(
        val isShowSubscriptionDialogOnStartBtn: Boolean = false,
        val isShowSubscriptionDialogOnStartBtnAfterTen: Boolean = true,
        val isShowSubscriptionDialogOnOptimizeBtnAfterTen: Boolean = false,
        val showSubscriptionBtnInDrawer: Boolean = true,

        val splash_close_interstitial: Boolean = true,
        val route_finish_interstitial: Boolean = true,
        val subscription_screen_interval_on_rf_button: Int = 0,
        val showRemoveAdsPopupInterval: Int = 1
)


fun getRemoteconfig(): FirebaseRemoteConfig? {
    val mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    val configSettings = Builder()
        .setMinimumFetchIntervalInSeconds(0)
        .build()
    mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
    mFirebaseRemoteConfig.fetchAndActivate()
    return mFirebaseRemoteConfig
}


