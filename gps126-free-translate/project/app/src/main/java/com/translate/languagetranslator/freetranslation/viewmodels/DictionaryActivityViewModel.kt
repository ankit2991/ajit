package com.translate.languagetranslator.freetranslation.viewmodels

import androidx.lifecycle.ViewModel
import com.translate.languagetranslator.freetranslation.database.DataRepository

class DictionaryActivityViewModel(var dataRepository: DataRepository) :ViewModel() {

    fun isAutoAdsRemoved() = dataRepository.isAdsRemoved()

    fun dictionary_back_interstitial() = dataRepository.dictionary_back_interstitial
}