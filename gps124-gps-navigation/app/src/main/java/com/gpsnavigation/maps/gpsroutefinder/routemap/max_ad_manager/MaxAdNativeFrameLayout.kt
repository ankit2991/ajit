package com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout

class MaxAdNativeFrameLayout(context: Context, attributeSet: AttributeSet) :
    FrameLayout(context, attributeSet) {

    override fun removeAllViews() {
        MaxAdManager.destroyNativeAd(this)
        Log.d("MaxAdNative", "Removed All Views")
        super.removeAllViews()
    }

    override fun onDetachedFromWindow() {
        MaxAdManager.destroyNativeAd(this)
        Log.d("MaxAdNative", "Detached From Window")
        super.onDetachedFromWindow()
    }
}
