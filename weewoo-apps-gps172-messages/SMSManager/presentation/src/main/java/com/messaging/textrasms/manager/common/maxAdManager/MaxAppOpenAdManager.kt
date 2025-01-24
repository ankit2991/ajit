package com.messaging.textrasms.manager.common.maxAdManager

import android.app.Activity
import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log

import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdConstants.isInterAdShow
import com.messaging.textrasms.manager.util.Preferences

class MaxAppOpenAdManager(applicationContext: Context?)  {
    companion object {
        private const val LOG_TAG = "AppOpenAdManager"
    }

    private var appOpenAd: MaxAppOpenAd? = null
    private lateinit var context: Context
    var isAppOpenShowing = false

    init {
        context = applicationContext!!
        appOpenAd = MaxAppOpenAd(MaxAdConstants.APP_OPEN_AD_ID, applicationContext)
    }


    fun loadAd(context: Context, callback: AppOpenCallback) {
        if (Preferences.getBoolean(context, Preferences.ADSREMOVED)) {
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            callback.isAdDismiss(false)
            return
        }
        if (isAppOpenShowing){
            return
        }
        if (isInterAdShow) {
            return
        }

        val handler = Handler(Looper.getMainLooper())
        val adTimeoutRunnable = Runnable {
            if (!appOpenAd!!.isReady) {
                appOpenAd!!.destroy()
                callback.isAdDismiss(false)
                isAppOpenShowing = false
                Log.e(LOG_TAG, "Open Ad load timed out.")

            }
        }

        handler.postDelayed(adTimeoutRunnable, 7000) // 7 seconds timeout


        appOpenAd!!.setListener(object : MaxAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                handler.removeCallbacks(adTimeoutRunnable)
                callback.isAdLoad(true)
                Log.e(LOG_TAG, "Open Ad loaded.")
                if (appOpenAd!!.isReady) {
                    appOpenAd!!.showAd()
                }
            }

            override fun onAdDisplayed(p0: MaxAd) {
//                handler.removeCallbacks(adTimeoutRunnable)
                Log.e(LOG_TAG, "Open Ad Shown.")
                isAppOpenShowing = true
                callback.isAdShown(true)
            }

            override fun onAdHidden(p0: MaxAd) {
//                handler.removeCallbacks(adTimeoutRunnable)
                isAppOpenShowing = false
                callback.isAdDismiss(true)

            }

            override fun onAdClicked(p0: MaxAd) {

            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                handler.removeCallbacks(adTimeoutRunnable)
                Log.e(LOG_TAG, "Open Ad load Failed.")
                Log.e(LOG_TAG, "Open Ad load failed " +p1.message)
                Log.e(LOG_TAG, "Open Ad load failed " +p1.mediatedNetworkErrorMessage)
                callback.isAdDismiss(false)

            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                handler.removeCallbacks(adTimeoutRunnable)
                Log.e(LOG_TAG, "Open Ad shown Failed.")
                isAppOpenShowing = false
                callback.isAdDismiss(false)
            }

        })
        appOpenAd!!.loadAd()

    }

}