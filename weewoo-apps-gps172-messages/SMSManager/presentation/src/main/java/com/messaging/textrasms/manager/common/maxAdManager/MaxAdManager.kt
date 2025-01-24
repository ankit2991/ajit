package com.messaging.textrasms.manager.common.maxAdManager

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView


import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.R
import java.util.concurrent.TimeUnit


object MaxAdManager {
    private var interstitialAd: MaxInterstitialAd? = null
    private var isFirstLoad = true
    private var retryAttempt = 0.0


    var tapCounter = 0
    val defaultTaps = 4
    var tries = 0
    var IS_INTER_SHOWING = false
//     var onAdShowCallback:OnAdShowCallback? =null
    fun checkTap():Boolean{
        var tapCounterReadched = false
        tapCounter++
        Log.e("MaxAdCount",">"+tapCounter)
        if (tapCounter == defaultTaps){
            tapCounter = 0
            tapCounterReadched = true
        }

        return tapCounterReadched
    }
//    fun interAdCountCriteria(currentCount: Int, defaultCount: Int): Boolean {
//        Log.e("checktaps",">"+currentCount % defaultCount)
//        return currentCount % defaultCount == 0
//    }

    fun maxInit(context: Context) {
        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(context).setMediationProvider("max")
        AppLovinSdk.getInstance(context).initializeSdk({ configuration: AppLovinSdkConfiguration ->
            // AppLovin SDK is initialized, start loading ads
            Log.e("MaxAdInit", "initialized")
        })
    }

//    fun onAdShown(onAdShowInter: OnAdShowCallback){
//        onAdShowCallback = onAdShowInter
//    }

    //    <<<<<<<<Interstitial app>>>>>>>>>>
    fun loadInterAd(context: Activity, adListener: MaxAdListener) {
        if (Preferences.getBoolean(context, Preferences.ADSREMOVED)) {
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }
//        loadAmazonInterAd(context)
        interstitialAd = MaxInterstitialAd(MaxAdConstants.INTER_AD_ID, context)
        interstitialAd!!.setListener(object : com.applovin.mediation.MaxAdListener {
            override fun onAdLoaded(p0: MaxAd) {

                Log.e("MaxAdLoad", "loaded")
                retryAttempt = 0.0
                adListener.onAdLoaded(true)
            }

            override fun onAdDisplayed(p0: MaxAd) {
                adListener.onAdShowed(true)
                MaxAdConstants.isInterAdShow = true
            }

            override fun onAdHidden(p0: MaxAd) {
                // Interstitial ad is hidden. Pre-load the next ad
                Log.e("MaxAdLoad", "hidden")
                adListener.onAdHidden(true)
                interstitialAd!!.loadAd()
                MaxAdConstants.isInterAdShow = false
            }

            override fun onAdClicked(p0: MaxAd) {

            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {

                Log.e("MaxAdLoad", "failed to load" + p1)
                adListener.onAdLoadFailed(true)
                retryAttempt++
                val delayMillis =
                    TimeUnit.SECONDS.toMillis(Math.pow(2.0, Math.min(6.0, retryAttempt)).toLong())

                Handler().postDelayed({ interstitialAd!!.loadAd() }, delayMillis)
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                adListener.onAdDisplayFailed(true)
                Log.e("MaxAdLoad", "failed to display" + p1)
                interstitialAd!!.loadAd()
                MaxAdConstants.isInterAdShow = false
            }

        })

        // Load the first ad
        interstitialAd!!.loadAd()
    }


    fun showInterAd(context: Activity,onAdShowCallback: OnAdShowCallback) {
        if (Preferences.getBoolean(context, Preferences.ADSREMOVED)) {
            onAdShowCallback.onAdHidden(true)
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            onAdShowCallback.onAdHidden(true)
        }
        if (interstitialAd != null) {
            if (interstitialAd!!.isReady) {
                interstitialAd!!.showAd()

                interstitialAd!!.setListener(object : com.applovin.mediation.MaxAdListener {
                    override fun onAdLoaded(p0: MaxAd) {
                    }

                    override fun onAdDisplayed(p0: MaxAd) {
                        MaxAdConstants.isInterAdShow = true
                        onAdShowCallback.onAdDisplay()
                    }

                    override fun onAdHidden(p0: MaxAd) {
                        interstitialAd!!.loadAd()
                        onAdShowCallback.onAdHidden(true)
                        MaxAdConstants.isInterAdShow = false
                    }

                    override fun onAdClicked(p0: MaxAd) {
                    }

                    override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    }

                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                        Log.e("MaxAdShow", "failed to show" + p1)

                        onAdShowCallback.onAdfailed()
                        MaxAdConstants.isInterAdShow = false
                    }

                })
                }else{
                    interstitialAd!!.loadAd()
                Log.e("MaxAdShow", "not ready")
                onAdShowCallback.onAdfailed()
            }
        }else{
            Log.e("MaxAdShow", "inter null")
            onAdShowCallback.onAdfailed()

        }
    }

    fun loadInterAd(context: Activity) {
        if (Preferences.getBoolean(context, Preferences.ADSREMOVED)) {
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }
        if (!interstitialAd!!.isReady) {
            interstitialAd!!.loadAd()
        }
    }

    fun checkIsInterIsReady(): Boolean {
        if (interstitialAd != null) {
            return interstitialAd!!.isReady
        }else{
           return false
        }
    }


    //    <<<<<<<<<<<banner ad>>>>>>>>>>>>
    private var adView: MaxAdView? = null

    var BANNERTYPE = 1
    fun createBannerAd(
        context: Context,
        mainContainer: RelativeLayout,
        rootView: FrameLayout,
        tvLoading:TextView,
        bannerAdListener: BannerAdListener

    ) {
        if (Preferences.getBoolean(context, Preferences.ADSREMOVED)) {
            mainContainer.visibility = View.GONE
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }

        tvLoading.visibility = View.VISIBLE
        if (adView == null) {

            if (adView != null) {
                adView!!.destroy()
            }
//        loadAmazonBannerAd()
            adView = MaxAdView(MaxAdConstants.BANNER_AD_ID, context)
            adView?.setListener(object : MaxAdViewAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    mainContainer.visibility = View.VISIBLE
                    // Stretch to the width of the screen for banners to be fully functional
                    Log.e("MaxAdLoad", "bannerAdLoaded")
                    tvLoading.visibility = View.GONE
                    bannerAdListener.bannerAdLoaded(true)
                    val width = ViewGroup.LayoutParams.MATCH_PARENT

                    // Banner height on phones and tablets is 50 and 90, respectively
                    val parent = adView?.parent as? ViewGroup
                    parent?.removeView(adView)
                    val heightPx = context.resources.getDimensionPixelSize(R.dimen.banner_height)

                    adView?.layoutParams = FrameLayout.LayoutParams(width, heightPx)


//        val rootView = findViewById<ViewGroup>(android.R.id.content)
//        rootView.addView(adView)

                    rootView.removeAllViews()
                    rootView.addView(adView)
                }

                override fun onAdDisplayed(p0: MaxAd) {

                }

                override fun onAdHidden(p0: MaxAd) {
                }

                override fun onAdClicked(p0: MaxAd) {
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    Log.e("MaxAdLoad", "adFailedToLoad " + p1.message);
                    mainContainer.visibility = View.GONE
                    bannerAdListener.bannerAdLoaded(false)
                    tvLoading.visibility = View.GONE

                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    Log.e("MaxAdLoad", "adFailedToDisplay " + p1)
                    bannerAdListener.bannerAdLoaded(false)
                    tvLoading.visibility = View.GONE

                }

                override fun onAdExpanded(p0: MaxAd) {
                }

                override fun onAdCollapsed(p0: MaxAd) {
                }

            })


            // Load the ad
            adView?.loadAd()

        }else{
            tvLoading.visibility = View.GONE
            bannerAdListener.bannerAdLoaded(true)
            val width = ViewGroup.LayoutParams.MATCH_PARENT

            // Banner height on phones and tablets is 50 and 90, respectively
            val parent = adView?.parent as? ViewGroup
            parent?.removeView(adView)

            val heightPx = context.resources.getDimensionPixelSize(R.dimen.banner_height)
            adView?.layoutParams = FrameLayout.LayoutParams(width, heightPx)

            rootView.removeAllViews()
            rootView.addView(adView)
        }
    }

    //    <<<<<<<<<<<banner ad 2>>>>>>>>>>>>
    private var adView2: MaxAdView? = null

    fun createBannerAd2(
        context: Context,
        mainContainer: RelativeLayout,
        rootView: FrameLayout,
        tvLoading:TextView,
        bannerAdListener: BannerAdListener

    ) {
        if (Preferences.getBoolean(context, Preferences.ADSREMOVED)) {
            mainContainer.visibility = View.GONE
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }

        tvLoading.visibility = View.VISIBLE
        if (adView2 == null) {

            if (adView2 != null) {
                adView2!!.destroy()
            }
//        loadAmazonBannerAd()
            adView2 = MaxAdView(MaxAdConstants.BANNER_AD_ID, context)
            adView2?.setListener(object : MaxAdViewAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    mainContainer.visibility = View.VISIBLE
                    // Stretch to the width of the screen for banners to be fully functional
                    Log.e("MaxAdLoad", "bannerAdLoaded")
                    tvLoading.visibility = View.GONE
                    bannerAdListener.bannerAdLoaded(true)
                    val width = ViewGroup.LayoutParams.MATCH_PARENT

                    // Banner height on phones and tablets is 50 and 90, respectively
                    val parent = adView2?.parent as? ViewGroup
                    parent?.removeView(adView2)
                    val heightPx = context.resources.getDimensionPixelSize(R.dimen.banner_height)

                    adView2?.layoutParams = FrameLayout.LayoutParams(width, heightPx)


//        val rootView = findViewById<ViewGroup>(android.R.id.content)
//        rootView.addView(adView2)

                    rootView.removeAllViews()
                    rootView.addView(adView2)
                }

                override fun onAdDisplayed(p0: MaxAd) {

                }

                override fun onAdHidden(p0: MaxAd) {
                }

                override fun onAdClicked(p0: MaxAd) {
                }

                override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    Log.e("MaxAdLoad", "adFailedToLoad " + p1.message);
                    mainContainer.visibility = View.GONE
                    bannerAdListener.bannerAdLoaded(false)
                    tvLoading.visibility = View.GONE

                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    Log.e("MaxAdLoad", "adFailedToDisplay " + p1)
                    bannerAdListener.bannerAdLoaded(false)
                    tvLoading.visibility = View.GONE

                }

                override fun onAdExpanded(p0: MaxAd) {
                }

                override fun onAdCollapsed(p0: MaxAd) {
                }

            })


            // Load the ad
            adView2?.loadAd()

        }else{
            tvLoading.visibility = View.GONE
            bannerAdListener.bannerAdLoaded(true)
            val width = ViewGroup.LayoutParams.MATCH_PARENT

            // Banner height on phones and tablets is 50 and 90, respectively
            val parent = adView2?.parent as? ViewGroup
            parent?.removeView(adView2)

            val heightPx = context.resources.getDimensionPixelSize(R.dimen.banner_height)
            adView2?.layoutParams = FrameLayout.LayoutParams(width, heightPx)

            rootView.removeAllViews()
            rootView.addView(adView2)
        }
    }


    //    <<<<<<<<<<<native ad>>>>>>>>>>>>
    private var nativeAdLoader: MaxNativeAdLoader? = null
    private var nativeAd: MaxAd? = null
    private var maxNativeAdView: MaxNativeAdView? = null
    fun createNativeAd(
        context: Context?,
        mainContainer:RelativeLayout,
        nativeAdContainer: FrameLayout,
        tvAdLoading:TextView,
        failed:()->Unit,
        load:()->Unit
    ) {
        if (Preferences.getBoolean(context, Preferences.ADSREMOVED)) {
            mainContainer.visibility = View.GONE
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }
        tvAdLoading.visibility = View.VISIBLE
        if ((nativeAd == null || maxNativeAdView == null) /*|| isLoadNativeAd*/) {
//        FrameLayout nativeAdContainer = findViewById( R.id.native_ad_layout );
            nativeAdLoader = MaxNativeAdLoader(MaxAdConstants.NATIVE_AD_ID, context)
            nativeAdLoader!!.setNativeAdListener(object : MaxNativeAdListener() {
                override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                    // Clean up any pre-existing native ad to prevent memory leaks.

                    if(nativeAdView != null) {
                        if (nativeAd != null) {
                            nativeAdLoader!!.destroy(nativeAd)
                        }

                        tvAdLoading.visibility = View.GONE
                        Log.e("maxNativeAd", "adloaded")

                        // Save ad for cleanup.
                        nativeAd = ad
                        maxNativeAdView = nativeAdView
                        // Add ad view to view.
                        nativeAdContainer.removeAllViews()
                        nativeAdContainer.addView(nativeAdView)

                        load.invoke()
                    }else{
                        failed.invoke()
                        Log.e("maxNativeAd", "nativeADViewNull")
                    }
                }

                override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                    // We recommend retrying with exponentially higher delays up to a maximum delay
                    tvAdLoading.visibility = View.GONE
                    Log.e("maxNativeAd", "adFailedToLoad$error")
                    failed.invoke()
                }

                override fun onNativeAdClicked(ad: MaxAd) {
                    // Optional click callback
                }
            })
            nativeAdLoader!!.loadAd()
        }else{
            tvAdLoading.visibility = View.GONE
            val parent = maxNativeAdView!!.parent
            if (parent is ViewGroup) {
                parent.removeView(maxNativeAdView)
            }
            // Add ad view to view.
//            nativeAdContainer.removeAllViews()
            nativeAdContainer.addView(maxNativeAdView)
            load.invoke()
        }
    }

    private fun getDefaultAdView(context: Context): View {
        return LinearLayout(context)
    }

}