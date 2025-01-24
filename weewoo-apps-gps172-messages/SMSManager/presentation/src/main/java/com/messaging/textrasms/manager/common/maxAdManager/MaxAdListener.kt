package com.messaging.textrasms.manager.common.maxAdManager

interface MaxAdListener {

    fun onAdLoaded(adLoad:Boolean)
    fun onAdShowed(adShow:Boolean)
    fun onAdHidden(adHidden:Boolean)
    fun onAdLoadFailed(adLoadFailed:Boolean)
    fun onAdDisplayFailed(adDisplayFailed:Boolean)

}