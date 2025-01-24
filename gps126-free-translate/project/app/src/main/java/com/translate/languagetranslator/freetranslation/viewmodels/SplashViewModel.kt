package com.translate.languagetranslator.freetranslation.viewmodels

import androidx.lifecycle.ViewModel
import com.translate.languagetranslator.freetranslation.database.DataRepository

class SplashViewModel(private var dataRepository: DataRepository) : ViewModel() {


    fun isAutoAdsRemoved() = dataRepository.isAdsRemoved()

    fun splash_close_interstitial() = dataRepository.splash_close_interstitial

    fun isSubscriptionScreenShown() = dataRepository.isSubscriptionScreenShown()
    fun setSubscriptionScreenShown(yes: Boolean) = dataRepository.setSubscriptionScreenShown(yes)
    fun setIsFirstTime(yes: Boolean) = dataRepository.setIsFirstTime(yes)
    fun isFirstTime() = dataRepository.isFirstTime()

}