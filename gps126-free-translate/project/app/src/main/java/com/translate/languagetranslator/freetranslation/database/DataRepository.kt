package com.translate.languagetranslator.freetranslation.database

import androidx.lifecycle.MutableLiveData
import com.code4rox.adsmanager.Constants
import com.code4rox.adsmanager.Constants.IS_FIRST_TIME
import com.code4rox.adsmanager.MaxAppOpenAdManager
import com.code4rox.adsmanager.TinyDB
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.di.RemoteAdValues
import com.translate.languagetranslator.freetranslation.di.RemoteConfigUtil
import com.translate.languagetranslator.freetranslation.extension.ConstantsKT.SHOW_SUBSCRIPTION_ONE_TIME
import com.translate.languagetranslator.freetranslation.models.RemoteAdDetails

class DataRepository(
    var applicationClass: AppBase,
    var firebaseRemoteConfig: FirebaseRemoteConfig
) {


    init {
        getAllRemoteValues()
    }

    //Remote Config settings

    val splash_close_interstitial = MutableLiveData<RemoteAdDetails>()
    val home_native = MutableLiveData<RemoteAdDetails>()
    val translate_back_interstitial = MutableLiveData<RemoteAdDetails>()
    val camera_back_interstitial = MutableLiveData<RemoteAdDetails>()
    val conversation_back_interstitial = MutableLiveData<RemoteAdDetails>()
    val dictionary_back_interstitial = MutableLiveData<RemoteAdDetails>()
    val translate_top_native = MutableLiveData<RemoteAdDetails>()
    val translate_history_list_native = MutableLiveData<RemoteAdDetails>()
    val translate_button_interstitial = MutableLiveData<RemoteAdDetails>()
    val language_list_native = MutableLiveData<RemoteAdDetails>()
    val conversation_top_native = MutableLiveData<RemoteAdDetails>()
    val ocr_translate_btn_interstitial = MutableLiveData<RemoteAdDetails>()


    private fun getAllRemoteValues() {
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
            try {
                val adData = firebaseRemoteConfig.getValue(RemoteConfigUtil.AD_SETTING)
                val allAdsSetting = Gson().fromJson(adData.asString(), RemoteAdValues::class.java)
                splash_close_interstitial.postValue(allAdsSetting.splash_close_interstitial)
                home_native.postValue(allAdsSetting.home_native)
                translate_back_interstitial.postValue(allAdsSetting.translate_back_interstitial)
                camera_back_interstitial.postValue(allAdsSetting.camera_back_interstitial)
                conversation_back_interstitial.postValue(allAdsSetting.conversation_back_interstitial)
                dictionary_back_interstitial.postValue(allAdsSetting.dictionary_back_interstitial)
                translate_top_native.postValue(allAdsSetting.translate_top_native)
                translate_history_list_native.postValue(allAdsSetting.translate_history_list_native)
                translate_button_interstitial.postValue(allAdsSetting.translate_button_interstitial)
                language_list_native.postValue(allAdsSetting.language_list_native)
                conversation_top_native.postValue(allAdsSetting.conversation_top_native)
                ocr_translate_btn_interstitial.postValue(allAdsSetting.ocr_translate_btn_interstitial)
                if (allAdsSetting.app_open_ad) {
                    MaxAppOpenAdManager.Companion.canShowAppOpen = true
                } else {
                    MaxAppOpenAdManager.Companion.canShowAppOpen = false

                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }
    }

    fun isAdsRemoved(): Boolean {
        return TinyDB.getInstance(applicationClass).getBoolean(Constants.IS_PREMIUM)
    }

    fun isSubscriptionScreenShown(): Boolean {
        return TinyDB.getInstance(applicationClass).getBoolean(SHOW_SUBSCRIPTION_ONE_TIME)
    }

    fun setSubscriptionScreenShown(yes: Boolean) {
        return TinyDB.getInstance(applicationClass).putBoolean(SHOW_SUBSCRIPTION_ONE_TIME, yes)
    }
    fun isFirstTime() : Boolean{
        return TinyDB.getInstance(applicationClass).getBoolean(IS_FIRST_TIME)
    }
    fun setIsFirstTime(yes: Boolean) {
        return TinyDB.getInstance(applicationClass).putBoolean(IS_FIRST_TIME, yes)
    }

    ////.......................................


}