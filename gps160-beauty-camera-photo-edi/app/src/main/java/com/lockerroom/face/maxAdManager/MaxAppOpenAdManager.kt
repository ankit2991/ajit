package com.lockerroom.face.maxAdManager

import android.annotation.SuppressLint
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
import com.lockerroom.face.maxAdManager.MaxAdConstants.isInterAdShow
import com.lockerroom.face.utils.SharePreferenceUtil

class MaxAppOpenAdManager(applicationContext: Context?)  {
    companion object {
        private const val LOG_TAG = "AppOpenAdManager"
        @SuppressLint("StaticFieldLeak")
        private var instance: MaxAppOpenAdManager? = null
        fun getInstance(applicationContext: Context?): MaxAppOpenAdManager {
            if (instance == null) {
                instance = MaxAppOpenAdManager(applicationContext)
            }
            return instance!!
        }
    }

    private var appOpenAd: MaxAppOpenAd? = null
    private var appOpenSplashAd: MaxAppOpenAd? = null
    private lateinit var context: Context
    var isAppOpenShowing = false



    init {
        context = applicationContext!!
        appOpenAd = MaxAppOpenAd(MaxAdConstants.APP_OPEN_AD_ID, applicationContext)
        appOpenSplashAd = MaxAppOpenAd(MaxAdConstants.APP_OPEN_AD_ID, applicationContext)
    }
    fun  initialize(context:Context) {

        this.context = context
        if (SharePreferenceUtil.isPurchased(context)) {
            return
        }
            appOpenAd = MaxAppOpenAd(MaxAdConstants.APP_OPEN_AD_ID, context)
    }

    fun loadAd(context: Context, callback: AppOpenCallback) {
        if (SharePreferenceUtil.isPurchased(context)) {
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

        if (appOpenAd==null){
            appOpenAd = MaxAppOpenAd(MaxAdConstants.APP_OPEN_AD_ID, context)
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
                Log.e(LOG_TAG, "Open Ad load failed " +p1.message)
                Log.e(LOG_TAG, "Open Ad load failed " +p1.mediatedNetworkErrorMessage)
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

    fun loadSplashAd(context: Context, callback: AppOpenCallback) {
        if (SharePreferenceUtil.isPurchased(context)) {
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

        if (appOpenSplashAd==null){
            appOpenSplashAd = MaxAppOpenAd(MaxAdConstants.APP_OPEN_AD_ID, context)
        }
        appOpenSplashAd!!.setListener(object : MaxAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                callback.isAdLoad(true)
                Log.e(LOG_TAG, "Open Ad loaded.")

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
                Log.e(LOG_TAG, "Open Ad load failed " +p1.message)
                Log.e(LOG_TAG, "Open Ad load failed " +p1.mediatedNetworkErrorMessage)
                callback.isAdDismiss(false)

            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                Log.e(LOG_TAG, "Open Ad shown Failed.")
                isAppOpenShowing = false
                callback.isAdDismiss(false)
            }

        })
        appOpenSplashAd!!.loadAd()

        Handler(Looper.getMainLooper()).postDelayed({
            if (appOpenSplashAd!!.isReady) {
                appOpenSplashAd!!.showAd()
            } else {
                callback.isAdDismiss(false)
            }
        },5000)
    }

}