package com.code4rox.adsmanager

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
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
import java.util.concurrent.TimeUnit


object MaxAdManager {
    private var interstitialAd: MaxInterstitialAd? = null
    private var retryAttempt = 0.0


    var tapCounter = 0
    val defaultTaps = 4

    fun checkTap():Boolean{
        var tapCounterReadched = false
        tapCounter++
        Log.e("MaxAdCount",">"+ tapCounter)
        if (tapCounter == defaultTaps){
            tapCounter = 0
            tapCounterReadched = true
        }

        return tapCounterReadched
    }

    fun maxInit(context: Context) {
        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(context).setMediationProvider("max")
        AppLovinSdk.getInstance(context).initializeSdk({ configuration: AppLovinSdkConfiguration ->
            // AppLovin SDK is initialized, start loading ads
            Log.e("MaxAdInit", "initialized")
        })
        AppLovinSdk.getInstance( context ).settings.setCreativeDebuggerEnabled( false )

    }



    //    <<<<<<<<Interstitial app>>>>>>>>>>
    fun loadInterAd(context: Activity, adListener: MaxAdCallbacks) {
        if (TinyDB.getInstance(context).isPremium) {
            adListener.onAdShowed(false)
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            adListener.onAdShowed(false)
            return
        }
        interstitialAd = MaxInterstitialAd(MaxAdConstants.INTER_AD_ID, context)
        interstitialAd!!.setListener(object : com.applovin.mediation.MaxAdListener {
            override fun onAdLoaded(p0: MaxAd) {

                Log.e("MaxAdLoad", "loaded")
                retryAttempt = 0.0
                adListener.onAdLoaded(true)
            }

            override fun onAdDisplayed(p0: MaxAd) {
                adListener.onAdShowed(true)
                MaxAdConstants.isInterAdShowing = true
            }

            override fun onAdHidden(p0: MaxAd) {
                // Interstitial ad is hidden. Pre-load the next ad
                Log.e("MaxAdLoad", "hidden")
                adListener.onAdHidden(true)
                interstitialAd!!.loadAd()
                MaxAdConstants.isInterAdShowing = false
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
                MaxAdConstants.isInterAdShowing = false
            }

        })
        // Load the first ad
        interstitialAd!!.loadAd()
    }


    fun showInterAd(context: Activity,onAdShowCallback: OnAdShowCallback) {
        if (TinyDB.getInstance(context).isPremium) {
            onAdShowCallback.onAdShow(true)
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            onAdShowCallback.onAdShow(true)
            return
        }
        if (interstitialAd != null) {
            if (interstitialAd!!.isReady) {
                interstitialAd!!.showAd()

                interstitialAd!!.setListener(object : com.applovin.mediation.MaxAdListener {
                    override fun onAdLoaded(p0: MaxAd) {
                    }
                    override fun onAdDisplayed(p0: MaxAd) {
                        MaxAdConstants.isInterAdShowing = true
                    }
                    override fun onAdHidden(p0: MaxAd) {
                        interstitialAd!!.loadAd()
                        onAdShowCallback.onAdShow(true)
                        MaxAdConstants.isInterAdShowing = false
                    }
                    override fun onAdClicked(p0: MaxAd) {
                    }
                    override fun onAdLoadFailed(p0: String, p1: MaxError) {
                    }
                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                        onAdShowCallback.onAdShow(false)
                        MaxAdConstants.isInterAdShowing = false
                    }

                })
                }else{
                    interstitialAd!!.loadAd()
                onAdShowCallback.onAdShow(true)
            }
        }
        else{
           loadInterAd(context,object :MaxAdCallbacks{
               override fun onAdLoaded(adLoad: Boolean) {
               }

               override fun onAdShowed(adShow: Boolean) {
               }

               override fun onAdHidden(adHidden: Boolean) {
               }

               override fun onAdLoadFailed(adLoadFailed: Boolean) {
               }

               override fun onAdDisplayFailed(adDisplayFailed: Boolean) {
               }
           })
            onAdShowCallback.onAdShow(true)
        }
    }




    fun checkIsInterIsReady(): Boolean {
        if(interstitialAd==null){return false}
        return interstitialAd!!.isReady
    }



    //    <<<<<<<<<<<banner ad>>>>>>>>>>>>
    private var adView: MaxAdView? = null

    fun createBannerAd(
        context: Context,
        rootView: FrameLayout
    ) {
        if (TinyDB.getInstance(context).isPremium) {

            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }

        if (adView == null) {

            adView = MaxAdView(MaxAdConstants.BANNER_AD_ID, context)
            adView?.setListener(object : MaxAdViewAdListener {
                override fun onAdLoaded(p0: MaxAd) {
                    // Stretch to the width of the screen for banners to be fully functional
                    Log.e("MaxAdLoad", "bannerAdLoaded")
                    val width = ViewGroup.LayoutParams.MATCH_PARENT
                    val parent = adView?.parent as? ViewGroup
                    parent?.removeView(adView)
                    // Banner height on phones and tablets is 50 and 90, respectively
                    val heightPx = context.resources.getDimensionPixelSize(R.dimen.banner_height)

                    adView?.layoutParams = FrameLayout.LayoutParams(width, heightPx)

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
                    Log.e("MaxAdLoad", "bannerAdFailedToLoad " + p1.message);


                }

                override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                    Log.e("MaxAdLoad", "adFailedToDisplay " + p1)

                }

                override fun onAdExpanded(p0: MaxAd) {
                }

                override fun onAdCollapsed(p0: MaxAd) {
                }

            })

            // Load the ad
            adView?.loadAd()

        }
        else{
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



    //    <<<<<<<<<<<native ad>>>>>>>>>>>>
    private var nativeAdLoader: MaxNativeAdLoader? = null
    private var nativeAd: MaxAd? = null
    private var maxNativeAdView: MaxNativeAdView? = null
    fun createNativeAd(
        context: Context?,
        nativeAdContainer: FrameLayout,
        placeholder: ImageView
    ) {
        if (TinyDB.getInstance(context!!).isPremium) {
            return
        }
        if (!MaxUtils.isNetworkConnected(context)) {
            return
        }
        if ((nativeAd == null && maxNativeAdView == null) ) {
            nativeAdLoader = MaxNativeAdLoader(MaxAdConstants.NATIVE_AD_ID, context)
            nativeAdLoader!!.setNativeAdListener(object : MaxNativeAdListener() {
                override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                    // Clean up any pre-existing native ad to prevent memory leaks.
                    if (nativeAd != null) {
                        nativeAdLoader!!.destroy(nativeAd)
                    }
                    Log.e("maxNativeAd", "adloaded")
                    // Save ad for cleanup.
                    nativeAd = ad
                    maxNativeAdView = nativeAdView
                    // Add ad view to view.
                    nativeAdContainer.removeAllViews()
                    nativeAdContainer.addView(nativeAdView)
                    placeholder.visibility=View.GONE

                }
                override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                    // We recommend retrying with exponentially higher delays up to a maximum delay
                    Log.e("maxNativeAd", "adFailedToLoad$error")
                    placeholder.visibility=View.VISIBLE

                }
                override fun onNativeAdClicked(ad: MaxAd) {
                }
            })
            nativeAdLoader!!.loadAd()
        }else{
            val parent = maxNativeAdView!!.parent
            if (parent is ViewGroup) {
                parent.removeView(maxNativeAdView)
            }
            // Add ad view to view.
//            nativeAdContainer.removeAllViews()
            nativeAdContainer.addView(maxNativeAdView)
            placeholder.visibility=View.GONE
        }
    }

    private fun getDefaultAdView(context: Context): View {
        return LinearLayout(context)
    }

}