package com.lockerroom.face.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity

import com.lockerroom.face.interfaces.IronSourceCallbacks
import com.lockerroom.face.interfaces.RewardedAdCallbck
import com.lockerroom.face.maxAdManager.MaxAdListener
import com.lockerroom.face.maxAdManager.MaxAdManager
import com.lockerroom.face.utils.SharePreferenceUtil
import timber.log.Timber


object IronSourceAdsManager {

    var activityStarted: Boolean = false;
    var lastAd: String = "unknown"
    var lastNetwork: String = "unknown"



    fun loadInter(context: Activity, callback: IronSourceCallbacks) {

        Log.e("AdsManager", "================>  OnSuccess()/OnFailed  called   from")
        if (!SharePreferenceUtil.isPurchased(context.applicationContext)) {

            //MaxAdInterLoad>>temp
            MaxAdManager.loadInterAd(context,object:MaxAdListener{
                override fun onAdLoaded(adLoad: Boolean) {}
                override fun onAdShowed(adShow: Boolean) {}
                override fun onAdHidden(adHidden: Boolean) {}
                override fun onAdLoadFailed(adLoadFailed: Boolean) {}
                override fun onAdDisplayFailed(adDisplayFailed: Boolean) {}
            })

        } else {
            callback.onSuccess()
        }
    }

    fun showInter(context: FragmentActivity, callback: IronSourceCallbacks) {
        if (!SharePreferenceUtil.isPurchased(context.applicationContext)) {


        }
    }


    fun showBanner(context: Activity, bannerContainer: FrameLayout) {

        //MaxAdBannerAd


    }




    fun showRewardedVideo(context: Context, rewardedAdCallbck: RewardedAdCallbck) {
//    if (!sharedPreference.getPremium(context)) {


    }


}