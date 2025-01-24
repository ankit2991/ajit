package com.gpsnavigation.maps.gpsroutefinder.routemap.ads

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.IS_PREMIUM

open class BaseActivity : AppCompatActivity() {
    var isPremium = false

    override fun onResume() {
        super.onResume()
        refreshAdsStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshAdsStatus()
    }

    protected fun refreshAdsStatus() {
        isPremium = TinyDB.getInstance(this).getBoolean(IS_PREMIUM)
    }

    override fun onPause() {
        super.onPause()
    }
}