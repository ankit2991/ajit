package com.lockerroom.face.maxAdManager

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
import androidx.lifecycle.viewmodel.viewModelFactory
import com.amazon.device.ads.AdError
import com.amazon.device.ads.DTBAdCallback
import com.amazon.device.ads.DTBAdRequest
import com.amazon.device.ads.DTBAdResponse
import com.amazon.device.ads.DTBAdSize
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.lockerroom.face.R
import com.lockerroom.face.utils.SharePreferenceUtil

import java.util.concurrent.TimeUnit


object MaxAdManager {
    private var interstitialAd: MaxInterstitialAd? = null
    private var isFirstLoad = true
    private var retryAttempt = 0.0


    var tapCounter = 0
    val defaultTaps = 8
    var tries = 0
    var IS_INTER_SHOWING = false
//     var onAdShowCallback:OnAdShowCallback? =null
    fun checkTap(context: Activity,onAdClose:()-> Unit ){

    if(!SharePreferenceUtil.isPurchased(context)) {
        tapCounter++
        Log.e("MaxAdCount", ">" + tapCounter)
        if (tapCounter == defaultTaps) {
            tapCounter = 0

            showInterAd(context, object : OnAdShowCallback {
                override fun onAdHidden(ishow: Boolean) {
                    onAdClose.invoke()
                }

                override fun onAdfailed() {
                    onAdClose.invoke()
                }

                override fun onAdDisplay() {
                }

            })
        } else {
            onAdClose.invoke()
        }
    }else{
        onAdClose.invoke()
    }
//        return tapCounterReadched
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
        if (SharePreferenceUtil.isPurchased(context)) {
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }
//        loadAmazonInterAd(context)
        if (interstitialAd == null) {
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
                        TimeUnit.SECONDS.toMillis(
                            Math.pow(2.0, Math.min(6.0, retryAttempt)).toLong()
                        )

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
        }else{
            if (!interstitialAd!!.isReady){
                interstitialAd!!.loadAd()
            }
        }
    }


    fun showInterAd(context: Activity,onAdShowCallback: OnAdShowCallback) {
        if (SharePreferenceUtil.isPurchased(context)) {
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
                onAdShowCallback.onAdfailed()
            }
        }else{

            loadInterAd(context,object:MaxAdListener{
                override fun onAdLoaded(adLoad: Boolean) {}
                override fun onAdShowed(adShow: Boolean) {}
                override fun onAdHidden(adHidden: Boolean) {}
                override fun onAdLoadFailed(adLoadFailed: Boolean) {}
                override fun onAdDisplayFailed(adDisplayFailed: Boolean) {}
            })
            onAdShowCallback.onAdfailed()
        }
    }

    fun loadInterAd(context: Activity) {
        if (SharePreferenceUtil.isPurchased(context)) {
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
        return interstitialAd!!.isReady
    }

    private fun loadAmazonInterAd(context: Activity) {
        if (isFirstLoad) {
            isFirstLoad = false
            if (interstitialAd == null) {
                interstitialAd = MaxInterstitialAd(MaxAdConstants.AMAZON_INTER_ID, context)
            }
            val adLoader = DTBAdRequest()

            // Switch video player width and height values(320, 480) depending on device orientation
            adLoader.setSizes(DTBAdSize.DTBVideo(320, 480, MaxAdConstants.AMAZON_INTER_ID))
            adLoader.loadAd(object : DTBAdCallback {
                override fun onSuccess(dtbAdResponse: DTBAdResponse) {
                    // 'interstitialAd' is your instance of MaxInterstitialAd
                    Log.e("amazonAd","Inter Loaded")
                    interstitialAd!!.setLocalExtraParameter("amazon_ad_response", dtbAdResponse)
                    interstitialAd!!.loadAd()
                }

                override fun onFailure(adError: AdError) {
                    // 'interstitialAd' is your instance of MaxInterstitialAd
                    Log.e("amazonAd","Inter Failed>${adError.message} code${adError.code}")
                    interstitialAd!!.setLocalExtraParameter("amazon_ad_error", adError)
                    interstitialAd!!.loadAd()
                }
            })
        } else {
            interstitialAd!!.loadAd()
        }
    }

    //    <<<<<<<<<<<banner ad>>>>>>>>>>>>
    private var adView: MaxAdView? = null

    fun createBannerAd(
        context: Context,
        mainContainer: RelativeLayout,
        rootView: FrameLayout,
        tvLoading:TextView,
        isLoadBannerAd:Boolean,
        bannerAdListener: BannerAdListener

    ) {
        if (SharePreferenceUtil.isPurchased(context)) {
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
            mainContainer.visibility = View.VISIBLE
            rootView.visibility = View.VISIBLE
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

    private fun loadAmazonBannerAd() {
        val amazonAdSlotId: String
        val adFormat: MaxAdFormat


        amazonAdSlotId = MaxAdConstants.AMAZON_BANNER_ID
        adFormat = MaxAdFormat.BANNER
        val rawSize = adFormat.size
        val size = DTBAdSize(rawSize.width, rawSize.height, amazonAdSlotId)

        val adLoader = DTBAdRequest()
        adLoader.setSizes(size)
        adLoader.loadAd(object : DTBAdCallback {
            override fun onSuccess(dtbAdResponse: DTBAdResponse) {
                // 'adView' is your instance of MaxAdView
                Log.e("amazonAd","bannerLoaded")
                adView?.setLocalExtraParameter("amazon_ad_response", dtbAdResponse)
                adView?.loadAd()
            }

            override fun onFailure(adError: AdError) {
                // 'adView' is your instance of MaxAdView
                Log.e("amazonAd","bannerFailedToLoad>${adError.message} code${adError.code}")
                adView?.setLocalExtraParameter("amazon_ad_error", adError)
                adView?.loadAd()
            }
        })
    }

    //    <<<<<<<<<<<native ad>>>>>>>>>>>>
    private var nativeAdLoader: MaxNativeAdLoader? = null
    private var nativeAd: MaxAd? = null
    private var maxNativeAdView: MaxNativeAdView? = null
    fun createNativeAd(
        context: Context?,
        mainContainer:RelativeLayout?,
        nativeAdContainer: FrameLayout,
        tvAdLoading:TextView,
        call: MaxAdListener,
    ) {
        if (SharePreferenceUtil.isPurchased(context)) {
                mainContainer?.visibility = View.GONE

            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }
        tvAdLoading.visibility = View.VISIBLE
        if ((nativeAd == null && maxNativeAdView == null) /*|| isLoadNativeAd*/) {
//        FrameLayout nativeAdContainer = findViewById( R.id.native_ad_layout );
            nativeAdLoader = MaxNativeAdLoader(MaxAdConstants.NATIVE_AD_ID, context)
            nativeAdLoader!!.setNativeAdListener(object : MaxNativeAdListener() {
                override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                    // Clean up any pre-existing native ad to prevent memory leaks.

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
                    call.onAdLoaded(true)
                }

                override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                    // We recommend retrying with exponentially higher delays up to a maximum delay
                    tvAdLoading.visibility = View.GONE
                    Log.e("maxNativeAd", "adFailedToLoad$error")
                    call.onAdLoaded(false)
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
            call.onAdLoaded(true)
        }
    }

    private fun getDefaultAdView(context: Context): View {
        return LinearLayout(context)
    }


    ///////Max Ad Rewarded Ad/////////
    private var rewardedAd: MaxRewardedAd? = null
    private var retryAttemptRewarded = 0.0

    fun createRewardedAd(context: Activity, onAdLoad:() -> Unit, onLoadFailed:() -> Unit)
    {

        if (SharePreferenceUtil.isPurchased(context)) {
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }

        rewardedAd = MaxRewardedAd.getInstance( MaxAdConstants.REWARDED_AD_ID, context)
        rewardedAd!!.setListener(object: MaxRewardedAdListener{
            override fun onAdLoaded(p0: MaxAd) {
                Log.e("MaxRewardedAd","onAdLoaded")
                retryAttemptRewarded = 0.0;
                onAdLoad.invoke()
            }

            override fun onAdDisplayed(p0: MaxAd) {
            }

            override fun onAdHidden(p0: MaxAd) {
            }

            override fun onAdClicked(p0: MaxAd) {
            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                Log.e("MaxRewardedAd","OnAdLoadFailed"+p1.message+"error code"+p1.code)

                retryAttemptRewarded++
                val delayMillis =
                    TimeUnit.SECONDS.toMillis(Math.pow(2.0, Math.min(6.0, retryAttemptRewarded)).toLong())

                Handler().postDelayed({ rewardedAd!!.loadAd() }, delayMillis)
                onLoadFailed.invoke()
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
            }

            override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
            }

        })

        rewardedAd!!.loadAd()
    }


    fun showRewardedAd(context: Activity, onSuccess: () -> Unit, onFailed: () -> Unit,onReward:() -> Unit){

        if (SharePreferenceUtil.isPurchased(context)) {
            onReward.invoke()
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }
        if(rewardedAd != null) {
            if (rewardedAd!!.isReady()) {
                rewardedAd!!.showAd()
                rewardedAd!!.setListener(object : MaxRewardedAdListener {
                    override fun onAdLoaded(p0: MaxAd) {

                    }

                    override fun onAdDisplayed(p0: MaxAd) {
                    }

                    override fun onAdHidden(p0: MaxAd) {
                        rewardedAd!!.loadAd()
                    }

                    override fun onAdClicked(p0: MaxAd) {
                    }

                    override fun onAdLoadFailed(p0: String, p1: MaxError) {
                        onFailed.invoke()
                    }

                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                        Log.e("MaxRewardedAd", "onAdDisplayFailed")
                        rewardedAd!!.loadAd();
                        onFailed.invoke()
                    }

                    override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
                        Log.e("MaxRewardedAd", "OnUserRewarded")
                        onReward.invoke()
                    }

                })
            } else {
                rewardedAd!!.loadAd()
                onFailed.invoke()
            }
        }else{
            createRewardedAd(context,{},{})
            onFailed.invoke()
        }
    }

}