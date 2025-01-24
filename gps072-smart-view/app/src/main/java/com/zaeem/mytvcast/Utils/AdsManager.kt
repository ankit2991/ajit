package com.zaeem.mytvcast.Utils

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.logger.IronSourceError
import com.ironsource.mediationsdk.sdk.BannerListener
import com.ironsource.mediationsdk.sdk.InterstitialListener


object AdsManager {


    fun showInter(context: Activity, onSuccess: () -> Unit, onFailed: () -> Unit) {

        if (!TinyDB.getInstance(context).isPremium(context)) {

            IronSource.setInterstitialListener(object : InterstitialListener {
                override fun onInterstitialAdReady() {
                    Log.e("","")

                    IronSource.showInterstitial()


                }

                override fun onInterstitialAdLoadFailed(p0: IronSourceError?) {
                    Log.e("","")

                    onFailed.invoke()
                }

                override fun onInterstitialAdOpened() {
                    Log.e("","")

                }

                override fun onInterstitialAdClosed() {
                    onFailed.invoke()
                    Log.e("","")

                }

                override fun onInterstitialAdShowSucceeded() {
                    Log.e("","")

                }

                override fun onInterstitialAdShowFailed(p0: IronSourceError?) {
                    onFailed.invoke()
                    Log.e("","")

                }

                override fun onInterstitialAdClicked() {
                    Log.e("","")

                }
            })
            IronSource.loadInterstitial()
        } else {
            onSuccess.invoke()
        }


    }


    fun showBanner(context: Activity, bannerContainer: FrameLayout) {

        if (!TinyDB.getInstance(context).isPremium(context)) {
            bannerContainer.visibility = View.VISIBLE
            val banner = IronSource.createBanner(context, ISBannerSize.BANNER)
            val layoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            bannerContainer.addView(banner, 0, layoutParams)
            banner.bannerListener = object : BannerListener {
                override fun onBannerAdLoaded() {
                    Log.e("","")


                }

                override fun onBannerAdLoadFailed(error: IronSourceError?) {
                    Log.e("","")

                    context.runOnUiThread(Runnable { bannerContainer.removeAllViews() })
                }

                override fun onBannerAdClicked() {
                    Log.e("","")


                }

                override fun onBannerAdScreenPresented() {
                    Log.e("","")

                }

                override fun onBannerAdScreenDismissed() {
                    Log.e("","")

                }

                override fun onBannerAdLeftApplication() {
                    Log.e("","")

                }
            }
            IronSource.loadBanner(banner)
        } else {
            bannerContainer.visibility = View.GONE
        }


    }

}