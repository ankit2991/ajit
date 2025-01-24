package com.translate.languagetranslator.freetranslation.viewmodels

import androidx.lifecycle.ViewModel
import com.translate.languagetranslator.freetranslation.database.DataRepository

class MainActivityViewModel(private var dataRepository: DataRepository) :ViewModel() {


    fun isAutoAdsRemoved() = dataRepository.isAdsRemoved()

    fun home_native() = dataRepository.home_native

    fun translate_back_interstitial() = dataRepository.translate_back_interstitial

}