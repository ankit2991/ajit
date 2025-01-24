package com.code4rox.adsmanager

interface MaxAdCallbacks {

    fun onAdLoaded(adLoad:Boolean)
    fun onAdShowed(adShow:Boolean)
    fun onAdHidden(adHidden:Boolean)
    fun onAdLoadFailed(adLoadFailed:Boolean)
    fun onAdDisplayFailed(adDisplayFailed:Boolean)

}