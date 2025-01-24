package com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager

interface MaxAdBannerListener {
    fun onAdLoaded(adLoad: Boolean)
    fun onAdDisplayed(adDisplay: Boolean)
}