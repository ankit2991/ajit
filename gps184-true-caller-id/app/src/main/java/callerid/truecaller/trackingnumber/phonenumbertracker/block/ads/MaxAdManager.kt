package callerid.truecaller.trackingnumber.phonenumbertracker.block.ads

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import callerid.truecaller.trackingnumber.phonenumbertracker.block.BuildConfig
import callerid.truecaller.trackingnumber.phonenumbertracker.block.R
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils
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
import java.util.UUID


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

    fun initAmazonSdk(activity: Activity) {
        AdRegistration.getInstance(AmazonConstants.AMAZON_APP_ID, activity)
        AdRegistration.setAdNetworkInfo(DTBAdNetworkInfo(DTBAdNetwork.MAX))
        AdRegistration.setMRAIDSupportedVersions(arrayOf("1.0", "2.0", "3.0"))
        AdRegistration.setMRAIDPolicy(MRAIDPolicy.CUSTOM)
        if (BuildConfig.DEBUG) {
            AdRegistration.enableTesting(true)
            AdRegistration.enableLogging(true)
        }
        Log.d("MaxAdInitAmazon", "Amazon SDK initialized")
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

        if (!TinyDB.getInstance(activity).weeklyPurchased()
            && Utils.getRemoteConfig().getBoolean("GPS119_Interstitial_flag")
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

    private var adView: MaxAdView? = null

    fun destroyBannerAd() {
        Log.d("MaxAdBanner", "bannerAdDestroyed")
        adView?.destroy()
        adView = null
    }

    fun createBannerAd(
        context: Context,
        rootView: FrameLayout
    ) {

        if (!TinyDB.getInstance(context).weeklyPurchased()
            && Utils.getRemoteConfig().getBoolean("GPS119_Banner_Bottom_flag")
        ) {
            Log.d("MaxAdBanner", "create bannerAd")

            adView?.destroy()

            adView = MaxAdView(MaxAdConstants.MAX_AD_BANNER_ID, context).apply {
                setListener(object : MaxAdViewAdListener {
                    override fun onAdLoaded(p0: MaxAd) {
                        // Stretch to the width of the screen for banners to be fully functional
                        Log.d("MaxAdBanner", "bannerAdLoaded")
                        val width = ViewGroup.LayoutParams.MATCH_PARENT

                        // Banner height 60dp
                        val heightPx =
                            context.resources.getDimensionPixelSize(R.dimen.banner_height)

                        adView?.layoutParams = FrameLayout.LayoutParams(width, heightPx)

                        // Set background or background color for banners to be fully functional
                        adView?.setBackgroundColor(Color.WHITE)

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
                        Log.e("MaxAdBanner", "banner adFailedToLoad$p1")

                    }

                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                        Log.e("MaxAdBanner", "banner adFailedToDisplay$p1")
                    }

                    override fun onAdExpanded(p0: MaxAd) {
                    }

                    override fun onAdCollapsed(p0: MaxAd) {
                    }

                })
            }
            // Load the ad
            loadAmazonBannerAd()
        }
    }

    private fun loadAmazonBannerAd() {

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
                adView?.setLocalExtraParameter("amazon_ad_response", dtbAdResponse)
                adView?.loadAd()
            }

            override fun onFailure(adError: AdError) {
                // 'adView' is your instance of MaxAdView
                adView?.setLocalExtraParameter("amazon_ad_error", adError)
                adView?.loadAd()
            }
        })
    }

    //    <<<<<<<<<<<native ad>>>>>>>>>>>>
    private var nativeAdLoader: MaxNativeAdLoader? = null
    private var nativeAd: MaxAd? = null
    private var nativeAdTag = ""
    fun createNativeAd(context: Context, nativeAdContainer: FrameLayout, flagKey: String?) {
        if (!TinyDB.getInstance(context).weeklyPurchased()
            && flagKey?.let { Utils.getRemoteConfig().getBoolean(it) } != false
        ) {

            if (nativeAdContainer.childCount != 0) {
                if (nativeAdContainer.getChildAt(0).tag == nativeAdTag) {
                    return
                } else {
                    nativeAdContainer.removeAllViews()
                }
            }

            Log.d("MaxAdNative", "create nativeAd")
            nativeAdLoader = MaxNativeAdLoader(MaxAdConstants.MAX_AD_NATIVE_ID, context).apply {
                setNativeAdListener(object : MaxNativeAdListener() {
                    override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                        Log.d("MaxAdNative", "nativeAdLoaded")
                        // Clean up any pre-existing native ad to prevent memory leaks.
                        nativeAd?.apply {
                            nativeAdLoader?.destroy(this)
                        }

                        // Save ad for cleanup.
                        nativeAd = ad

                        nativeAdTag = UUID.randomUUID().toString()
                        nativeAdView?.apply {
                            tag = nativeAdTag
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                context.resources.getDimension(R.dimen.native_ad_height).toInt()
                            )
                        }

                        // Add ad view to view.
                        nativeAdContainer.removeAllViews()
                        nativeAdContainer.addView(nativeAdView)
                    }

                    override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                        // We recommend retrying with exponentially higher delays up to a maximum delay
                        Log.e("MaxAdNative", "adFailedToLoad$error")
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