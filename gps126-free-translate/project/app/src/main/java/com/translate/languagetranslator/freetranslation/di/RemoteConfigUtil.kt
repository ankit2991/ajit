package com.translate.languagetranslator.freetranslation.di

import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder
import com.google.gson.Gson
import com.translate.languagetranslator.freetranslation.models.RemoteAdDetails


object RemoteConfigUtil {

    const val AD_SETTING = "ads_setting_new_vc21111"

    private const val FETCH_INTERVAL_TIME = 3600L

    fun initializeConfigs(): FirebaseRemoteConfig {
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = Builder()
                .setMinimumFetchIntervalInSeconds(FETCH_INTERVAL_TIME)
                .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        firebaseRemoteConfig.setDefaultsAsync(mapOf(AD_SETTING to Gson().toJson(RemoteAdValues())))
        return firebaseRemoteConfig
    }

}

@Keep
data class RemoteAdValues(
    val app_open_ad: Boolean = true,
    val splash_close_interstitial: RemoteAdDetails = RemoteAdDetails(true, "am"),
    val home_native: RemoteAdDetails = RemoteAdDetails(true),
    val translate_back_interstitial: RemoteAdDetails = RemoteAdDetails(true, "am"),
    val camera_back_interstitial: RemoteAdDetails = RemoteAdDetails(true, "am"),
    val conversation_back_interstitial: RemoteAdDetails = RemoteAdDetails(true, "am"),
    val dictionary_back_interstitial: RemoteAdDetails = RemoteAdDetails(true, "am"),
    val translate_top_native: RemoteAdDetails = RemoteAdDetails(true, "am"),
    val translate_history_list_native: RemoteAdDetails = RemoteAdDetails(true, "am"),
    val translate_button_interstitial: RemoteAdDetails = RemoteAdDetails(true, "am"),
    val language_list_native: RemoteAdDetails = RemoteAdDetails(true, "am"),
    val conversation_top_native: RemoteAdDetails = RemoteAdDetails(true, "am"),
    val ocr_translate_btn_interstitial: RemoteAdDetails = RemoteAdDetails(true, "am")

)
