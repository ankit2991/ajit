package com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout

class MaxAdBannerFrameLayout(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {
    override fun onDetachedFromWindow() {
        MaxAdManager.destroyBannerAd(this)
        Log.d("MaxAdBanner", "Detached From Window")
        super.onDetachedFromWindow()
    }
}
