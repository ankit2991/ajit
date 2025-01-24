package com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.amazon.device.ads.AdError
import com.amazon.device.ads.AdRegistration
import com.amazon.device.ads.DTBAdCallback
import com.amazon.device.ads.DTBAdNetwork
import com.amazon.device.ads.DTBAdNetworkInfo
import com.amazon.device.ads.DTBAdRequest
import com.amazon.device.ads.DTBAdResponse
import com.amazon.device.ads.DTBAdSize
import com.amazon.device.ads.MRAIDPolicy
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.gpsnavigation.maps.gpsroutefinder.routemap.BuildConfig
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.dpToPx


object MaxAdManager {

    private var interstitialAd: MaxInterstitialAd? = null
    private var interstitialBusy: Boolean = false
    private var interstitialAdListener: MaxAdInterstitialListener? = null

    fun initAppLovinSdkMax(context: Context) {
        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(context).mediationProvider = "max"
        AppLovinSdk.getInstance(context).initializeSdk { configuration: AppLovinSdkConfiguration ->
            // AppLovin SDK is initialized, start loading ads
            Log.d("MaxAdInit", "initialized")
        }
    }

    fun initAmazonSdk(context: Context) {
        try {
            AdRegistration.getInstance(AmazonConstants.AMAZON_APP_ID, context)
            AdRegistration.setAdNetworkInfo(DTBAdNetworkInfo(DTBAdNetwork.MAX))
            AdRegistration.setMRAIDSupportedVersions(arrayOf("1.0", "2.0", "3.0"))
            AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM)
            if (BuildConfig.DEBUG) {
                AdRegistration.enableTesting(true)
                AdRegistration.enableLogging(true)
            }
            Log.d("MaxAdInitAmazon", "Amazon SDK initialized")
        } catch (e: Exception) {
            Log.d("MaxAdInitAmazon", "Amazon SDK not initialized: ${e.message}")
        }
    }

    fun destroyInterstitialAd() {
        Log.d("Interstitial", "interstitialAdDestroyed")
        interstitialAd?.destroy()
        interstitialAd = null
    }

    fun createInterstitialAd(
        activity: AppCompatActivity,
        adId: String,
        adListener: MaxAdInterstitialListener?
    ) {

        if (!TinyDB.getInstance(activity).isPremium
            && !interstitialBusy
        ) {
            Log.d("MaxAdInterstitial", "create interstitialAd")
            interstitialAdListener = adListener

            when {
                interstitialAd == null -> {
                    MaxInterstitialAd(
                        adId,
                        activity
                    ).also { maxInterstitialAd ->

                        interstitialAd = maxInterstitialAd

                        maxInterstitialAd.setListener(object : MaxAdListener {
                            override fun onAdLoaded(maxAd: MaxAd) {
                                Log.d("MaxAdInterstitial", "interstitialAdLoaded")
                                interstitialBusy = false
                                interstitialAdListener?.also { maxAdListener ->
                                    if (interstitialAd?.isReady == true
                                        && activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
                                        && !activity.isFinishing
                                    ) {
                                        interstitialBusy = true
                                        interstitialAd?.showAd()
                                        maxAdListener.onAdLoaded(true)
                                    } else {
                                        maxAdListener.onAdLoaded(false)
                                        interstitialAdListener = null
                                    }
                                }

                            }

                            override fun onAdDisplayed(maxAd: MaxAd) {

                            }

                            override fun onAdHidden(maxAd: MaxAd) {
                                // Interstitial ad is hidden. Pre-load the next ad
                                Log.d("MaxAdInterstitial", "interstitialAdHidden")
                                interstitialAdListener?.onAdShowed(true)
                                interstitialAdListener = null
                                interstitialBusy = true
                                loadAmazonInterstitialAd()
                            }

                            override fun onAdClicked(maxAd: MaxAd) {

                            }

                            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                                Log.e("MaxAdInterstitial", "interstitial failed to load$error")
                                interstitialAdListener?.onAdLoaded(false)
                                interstitialAdListener = null
                                interstitialBusy = false
                            }

                            override fun onAdDisplayFailed(maxAd: MaxAd, error: MaxError) {
                                Log.e("MaxAdInterstitial", "interstitial failed to display$error")
                                interstitialAdListener?.onAdShowed(false)
                                interstitialAdListener = null
                                interstitialBusy = true
                                loadAmazonInterstitialAd()
                            }
                        })
                    }

                    // Load the first ad
                    loadAmazonInterstitialAd()
                }

                interstitialAd?.isReady == true -> {
                    interstitialBusy = true
                    interstitialAd?.showAd()
                    adListener?.onAdLoaded(true)
                }

                else -> {
                    interstitialBusy = true
                    loadAmazonInterstitialAd()
                }
            }
        } else {
            adListener?.onAdLoaded(false)
        }
    }


    private fun loadAmazonInterstitialAd() {
        val adLoader = DTBAdRequest()
        adLoader.setSizes(DTBAdSize.DTBInterstitialAdSize(AmazonConstants.AMAZON_INTERSTITIAL_ID))
        adLoader.loadAd(object : DTBAdCallback {
            override fun onSuccess(dtbAdResponse: DTBAdResponse) {
                // 'interstitialAd' is your instance of MaxInterstitialAd
                interstitialAd?.apply {
                    setLocalExtraParameter("amazon_ad_response", dtbAdResponse)
                    loadAd()
                }
            }

            override fun onFailure(adError: AdError) {
                // 'interstitialAd' is your instance of MaxInterstitialAd
                interstitialAd?.apply {
                    setLocalExtraParameter("amazon_ad_error", adError)
                    loadAd()
                }
            }
        })
    }

    fun showBannerAds(
        context: Context,
        rootView: FrameLayout
    ) {
        if (!TinyDB.getInstance(context).isPremium && rootView.tag == null) {
            rootView.tag = true
            createBannerAd(
                context,
                rootView,
                object : MaxAdBannerListener {
                    override fun onAdLoaded(adLoad: Boolean) {

                    }

                    override fun onAdDisplayed(adDisplay: Boolean) {
                        rootView.isVisible = adDisplay
                    }
                }
            )
        }
    }

    fun destroyBannerAd(rootView: FrameLayout) {
        rootView.forEach {
            if (it is MaxAdView) {
                try {
                    it.destroy()
                    Log.d("MaxAdBanner", "bannerAdDestroyed")
                } catch (e: Exception) {
                    Log.d("MaxAdBanner", "$e")
                }
            }
        }
    }

    fun createBannerAd(
        context: Context,
        rootView: FrameLayout,
        adListener: MaxAdBannerListener?
    ) {

        if (!TinyDB.getInstance(context).isPremium) {
            Log.d("MaxAdBanner", "create bannerAd")

            destroyBannerAd(rootView)

            val adView = MaxAdView(MaxAdConstants.BANNER_AD_ID, context).apply {
                setListener(object : MaxAdViewAdListener {
                    override fun onAdLoaded(p0: MaxAd) {
                        // Stretch to the width of the screen for banners to be fully functional
                        Log.d("MaxAdBanner", "bannerAdLoaded")
                        val width = ViewGroup.LayoutParams.MATCH_PARENT

                        // Banner height 60dp
                        val heightPx =
                            context.resources.getDimensionPixelSize(R.dimen.banner_height)

                        this@apply.layoutParams = FrameLayout.LayoutParams(width, heightPx)

                        // Set background or background color for banners to be fully functional
                        this@apply.setBackgroundColor(Color.WHITE)

                        rootView.removeAllViews()
                        rootView.addView(this@apply)
                        adListener?.onAdLoaded(true)
                    }

                    override fun onAdDisplayed(p0: MaxAd) {
                        adListener?.onAdDisplayed(true)
                    }

                    override fun onAdHidden(p0: MaxAd) {
                    }

                    override fun onAdClicked(p0: MaxAd) {
                    }

                    override fun onAdLoadFailed(p0: String, p1: MaxError) {
                        Log.e("MaxAdBanner", "banner adFailedToLoad$p1")
                        adListener?.onAdLoaded(false)

                    }

                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                        Log.e("MaxAdBanner", "banner adFailedToDisplay$p1")
                        adListener?.onAdDisplayed(false)
                    }

                    override fun onAdExpanded(p0: MaxAd) {
                    }

                    override fun onAdCollapsed(p0: MaxAd) {
                    }

                })
            }
            // Load the ad
            loadAmazonBannerAd(adView)
        }
    }

    private fun loadAmazonBannerAd(adView: MaxAdView) {

        val amazonAdSlotId: String = AmazonConstants.AMAZON_BANNER_ID
        val adFormat: MaxAdFormat = MaxAdFormat.BANNER

        // Raw size will be 320x50 for BANNERs on phones, and 728x90 for LEADERs on tablets
        val rawSize = adFormat.size
        val size = DTBAdSize(rawSize.width, rawSize.height, amazonAdSlotId)

        val adLoader = DTBAdRequest()
        adLoader.setSizes(size)
        adLoader.loadAd(object : DTBAdCallback {
            override fun onSuccess(dtbAdResponse: DTBAdResponse) {
                // 'adView' is your instance of MaxAdView
                adView.setLocalExtraParameter("amazon_ad_response", dtbAdResponse)
                adView.loadAd()
            }

            override fun onFailure(adError: AdError) {
                // 'adView' is your instance of MaxAdView
                adView.setLocalExtraParameter("amazon_ad_error", adError)
                adView.loadAd()
            }
        })
    }

    //    <<<<<<<<<<<native ad>>>>>>>>>>>>
     fun showNativeAds(
        context: Context,
        nativeAdContainer: FrameLayout,
        adHeight: Int = 260.dpToPx
    ) {
        if (!TinyDB.getInstance(context).isPremium && nativeAdContainer.tag == null) {
            createNativeAd(
                context,
                nativeAdContainer,
                object : MaxAdNativeListener {
                    override fun onAdLoaded(adLoad: Boolean) {
                        nativeAdContainer.isVisible = adLoad
                    }
                },
                adHeight
            )
        }
    }
    fun destroyNativeAd(nativeAdContainer: FrameLayout) {
        nativeAdContainer.forEach {
            if (it is MaxNativeAdView) {
                try {
                    it.tag.apply {
                        if (this is MaxAd) {
                            nativeAdLoader?.destroy(this)
                        }
                    }
                    it.tag = null
                    Log.d("MaxAdNative", "nativeAdDestroyed")
                } catch (e: Exception) {
                    Log.d("MaxAdNative", "$e")
                }
            }
        }
    }

    private var nativeAdLoader: MaxNativeAdLoader? = null
    fun createNativeAd(
        context: Context,
        nativeAdContainer: FrameLayout,
        adListener: MaxAdNativeListener?,
        adHeight: Int = 260.dpToPx
    ) {
        if (!TinyDB.getInstance(context).isPremium) {

            destroyNativeAd(nativeAdContainer)

            Log.d("MaxAdNative", "create nativeAd")

//            nativeAdContainer.setBackgroundColor(Color.parseColor("#D3D3D3"))

            nativeAdContainer.tag = true
            nativeAdLoader = MaxNativeAdLoader(MaxAdConstants.NATIVE_AD_ID, context).apply {
                setNativeAdListener(object : MaxNativeAdListener() {
                    override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                        Log.d("MaxAdNative", "nativeAdLoaded")

                        nativeAdView?.apply {
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                adHeight
                            )

                            // Set background color or drawable here
                            setBackgroundColor(Color.parseColor("#D3D3D3")) // Light gray color
                            // Or set a drawable: setBackgroundResource(R.drawable.your_drawable)

                            // Apply padding if needed for better visual layout
                            setPadding(16.dpToPx, 16.dpToPx, 16.dpToPx, 16.dpToPx)
                        }

                        nativeAdView?.tag = ad

                        // Add ad view to view.
                        nativeAdContainer.removeAllViews()
                        nativeAdContainer.addView(nativeAdView)
//                        nativeAdContainer.setBackgroundColor(Color.parseColor("#D3D3D3"))
                        adListener?.onAdLoaded(true)
                    }

                    override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                        // We recommend retrying with exponentially higher delays up to a maximum delay
                        Log.e("MaxAdNative", "adFailedToLoad$error")
                        adListener?.onAdLoaded(false)
                    }

                    override fun onNativeAdClicked(ad: MaxAd) {
                        // Optional click callback
                    }
                })

                loadAd()
            }
        }
    }
}