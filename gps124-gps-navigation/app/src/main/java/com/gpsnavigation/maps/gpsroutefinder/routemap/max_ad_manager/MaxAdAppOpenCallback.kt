package com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager

interface MaxAdAppOpenCallback {
    fun onAdLoaded() {}
    fun onAdDisplayed() {}
    fun onAdHidden() {}
    fun onAdClicked() {}
    fun onAdLoadFailed() {}
    fun onAdDisplayFailed() {}
}