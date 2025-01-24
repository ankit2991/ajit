package com.code4rox.adsmanager

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.code4rox.adsmanager.Constants.IS_PREMIUM

class BGMaxAppOpenAdManager(applicationContext: Context?) {
    companion object {
        private const val LOG_TAG = "AppOpenAdManager"
        public var canShowAppOpen = true
        var IS_OVERLAY_PERMISSION = false
            set(value) {
            field = value
            // Check the new value and toast a message
                if (value) {
                    Log.d(LOG_TAG, "Overlay permission is now enabled")
                } else {
                    Log.d(LOG_TAG, "Overlay permission is now disabled")
                }
        }
    }

    private var appOpenAd: MaxAppOpenAd? = null
    private lateinit var context: Context
    var isAppOpenShowing = false

    init {
        context = applicationContext!!
        appOpenAd = MaxAppOpenAd(MaxAdConstants.APP_OPEN_AD_ID, applicationContext)
    }


    fun loadAd(context: Context, callback: AppOpenCallback) {

        if (IS_OVERLAY_PERMISSION) {
            callback.isAdDismiss(false)
            return
        }
        if (TinyDB.getInstance(context!!).isPremium) {
            callback.isAdDismiss(false)
            return
        }
        if (canShowAppOpen == false) {
            callback.isAdDismiss(false)
            return
        }

        if (!MaxUtils.isNetworkConnected(context)) {
            callback.isAdDismiss(false)
            return
        }

        if (isAppOpenShowing) {
            return
        }
        appOpenAd!!.setListener(object : MaxAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                callback.isAdLoad(true)
                Log.e(LOG_TAG, "Open Ad loaded.")
                if (appOpenAd!!.isReady) {
                    appOpenAd!!.showAd()
                }
            }

            override fun onAdDisplayed(p0: MaxAd) {
                Log.e(LOG_TAG, "Open Ad Shown.")
                isAppOpenShowing = true
                callback.isAdShown(true)
            }

            override fun onAdHidden(p0: MaxAd) {
                isAppOpenShowing = false
                callback.isAdDismiss(true)

            }

            override fun onAdClicked(p0: MaxAd) {

            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                Log.e(LOG_TAG, "Open Ad load Failed.")
                Log.e(LOG_TAG, "" )
                Log.e(LOG_TAG, "Open Ad load failed " + p1.message)
                Log.e(LOG_TAG, "" )
                Log.e(LOG_TAG, "Open Ad load failed " + p1.mediatedNetworkErrorMessage)
                Log.e(LOG_TAG, "" )
                Log.e(LOG_TAG, "Open Ad load failed " + p0)
                callback.isAdDismiss(false)

            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                Log.e(LOG_TAG, "Open Ad shown Failed.")
                isAppOpenShowing = false
                callback.isAdDismiss(false)
            }

        })
        appOpenAd!!.loadAd()
    }

}