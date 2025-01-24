package com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.applovin.sdk.AppLovinSdk

class MaxAdAppOpen(
    applicationContext: Context?,
    adId: String,
    openCallback: MaxAdAppOpenCallback
) : LifecycleObserver {

    private var appOpenAd: MaxAppOpenAd? = null
    private lateinit var context: Context

    init {

        if (applicationContext != null) {

            context = applicationContext
            appOpenAd = MaxAppOpenAd(adId, applicationContext).apply {
                setListener(object : MaxAdListener {
                    override fun onAdLoaded(p0: MaxAd) {
                        Log.d("MaxAdLoad", "appOpenLoaded")
                        openCallback.onAdLoaded()
                    }

                    override fun onAdDisplayed(p0: MaxAd) {
                        openCallback.onAdDisplayed()
                    }

                    override fun onAdHidden(p0: MaxAd) {
                        openCallback.onAdHidden()
                    }

                    override fun onAdClicked(p0: MaxAd) {
                        openCallback.onAdClicked()
                    }

                    override fun onAdLoadFailed(p0: String, p1: MaxError) {
                        Log.e("MaxAdLoad", "appOpenLoadfailed" + p1)
                        openCallback.onAdLoadFailed()
                    }

                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                        Log.e("MaxAdLoad", "appOpenDisplayfailed")
                        openCallback.onAdDisplayFailed()
                    }

                })

                loadAd()
            }
        } else {
            // Handle the error when applicationContext is null
            // For example, you can log an error or show a fallback ad
            Log.e("appapplication", "applicationcontext null");
        }
    }

    fun showAdIfReady(adId: String): Boolean {
        when {
            !AppLovinSdk.getInstance(context).isInitialized -> {}
            appOpenAd?.isReady == true -> {
                appOpenAd?.showAd(adId)
                return true
            }
        }
        Log.e("MaxadLoad", "openApp Not ready")
        return false
    }

    fun loadAd() {
        appOpenAd?.loadAd()
    }

    fun destroy() {
        appOpenAd?.destroy()
        appOpenAd = null
    }
}