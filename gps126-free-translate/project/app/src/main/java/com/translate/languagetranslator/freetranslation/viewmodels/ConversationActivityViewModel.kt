package com.translate.languagetranslator.freetranslation.viewmodels

import androidx.lifecycle.ViewModel
import com.translate.languagetranslator.freetranslation.database.DataRepository

class ConversationActivityViewModel(var dataRepository: DataRepository) :ViewModel() {

    fun isAutoAdsRemoved() = dataRepository.isAdsRemoved()

    fun conversation_back_interstitial() = dataRepository.conversation_back_interstitial
}