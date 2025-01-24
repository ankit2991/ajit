package com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.ProgressDialogUtils
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB

class MaxAppOpenAdManager(applicationContext: Context?)  {
    companion object {
        private const val LOG_TAG = "AppOpenAdManager"
    }

    private var appOpenAd: MaxAppOpenAd? = null
    private lateinit var context: Context

    init {
        context = applicationContext!!
        appOpenAd = MaxAppOpenAd(MaxAdConstants.APP_OPEN_AD_ID, applicationContext)
    }



    fun loadAd(context: Context, callback: AppOpenCallback) {
        if (TinyDB.getInstance(context).getBoolean(TinyDB.IS_PREMIUM)) {
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
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
                callback.isAdShown(true)
            }

            override fun onAdHidden(p0: MaxAd) {
                callback.isAdDismiss(true)

            }

            override fun onAdClicked(p0: MaxAd) {

            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                Log.e(LOG_TAG, "Open Ad load Failed.")
                Log.e(LOG_TAG, "Open Ad load failed " +p1.message)
                Log.e(LOG_TAG, "Open Ad load failed " +p1.mediatedNetworkErrorMessage)
                callback.isAdDismiss(false)

            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                Log.e(LOG_TAG, "Open Ad shown Failed.")
                callback.isAdDismiss(false)
            }

        })
        appOpenAd!!.loadAd()
    }

}