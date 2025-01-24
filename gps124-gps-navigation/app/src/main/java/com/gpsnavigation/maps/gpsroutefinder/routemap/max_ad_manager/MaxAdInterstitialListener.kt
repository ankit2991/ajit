package com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager

interface MaxAdInterstitialListener {
    fun onAdLoaded(adLoad: Boolean)
    fun onAdShowed(adShow: Boolean)
}