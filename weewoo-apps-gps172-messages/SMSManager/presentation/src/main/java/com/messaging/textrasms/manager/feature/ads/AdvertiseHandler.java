package com.messaging.textrasms.manager.feature.ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;


import com.messaging.textrasms.manager.R;
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdListener;
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager;
import com.messaging.textrasms.manager.common.maxAdManager.OnAdShowCallback;
import com.messaging.textrasms.manager.common.util.UtilsData;
import com.messaging.textrasms.manager.util.Preferences;

import java.util.List;
import java.util.Random;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import xyz.teamgravity.checkinternet.CheckInternet;


/**
 * Author: BHAVEN SHAH
 * Date: 01-09-2021
 * Organization: Erasoft Technology
 * Email: erasoft.bhaven@gmail.com
 */

public class AdvertiseHandler implements LifecycleObserver, Application.ActivityLifecycleCallbacks {

    @SuppressLint("StaticFieldLeak")
    private static AdvertiseHandler sInstance;
    private final String TAG = AdvertiseHandler.class.getSimpleName();
    private final long ADS_MAX_LOAD_TIME = 5000L;
    private final Random randomGenerator;
    public boolean isNeedOpenAdRequest = false;
    public boolean isAppStartUpAdsEnabled = true;
    public boolean isAppStartUpAdsPause = false;
    private Application myApplication;
    private Activity activity;
    private boolean isAppOpen = false;
    private boolean isShowingAd = false;
    private boolean isAppOpenAdEnable = false;
    private boolean interstitialAdsLoading = false;
    private boolean isAppOpenAdLoading = false;
    private long appOpenLoadTime = 0L;
    private AdsLoadsListener appAdsListener;
    public boolean shouldShowDialogLoading = false;

    private AdvertiseHandler(@NonNull Application application) {
        this();

        myApplication = application;
        myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    private AdvertiseHandler() {
        randomGenerator = new Random();
    }

    @NonNull
    public static AdvertiseHandler getInstance(@NonNull Application application) {
        if (sInstance == null) {
            synchronized (AdvertiseHandler.class) {
                if (sInstance == null) {
                    sInstance = new AdvertiseHandler(application);
                }
            }
        }

        return sInstance;
    }

    @NonNull
    public static AdvertiseHandler getInstance(@Nullable Context context) {
        if (sInstance == null) {
            synchronized (AdvertiseHandler.class) {
                if (sInstance == null) {
                    sInstance = new AdvertiseHandler();
                }
            }
        }

        return sInstance;
    }


    public void showProgress(@NonNull final Activity activity) {
        if (UtilsData.isContextActive(activity)) {
            activity.runOnUiThread(() -> UtilsData.showProgressDialog(activity, R.string.ads_loading_msg));
        }
    }

    public void hideProgress(@NonNull final Activity activity) {
        if (UtilsData.isContextActive(activity)) {
            activity.runOnUiThread(UtilsData::hideProgressDialog);
        }
    }

    private boolean isNetworkNotAvailable(@NonNull final Context context) {

        boolean isAvailable = false;
        try {
            ConnectivityManager manager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                isAvailable = true;
            }
        } catch (Exception ignored) {

        }

        return !isAvailable;

    }

    private boolean isPurchased(@NonNull final Context context) {
        boolean purchase = Preferences.Companion.getBoolean(context, Preferences.ADSREMOVED);
        Log.i(TAG, "isPurchased: purchase :- " + purchase);
        return purchase;
    }





    public void loadInterstitialAds(@NonNull final Context context, @NonNull final String interstitialID, final int index, @Nullable final AdsLoadsListener adsListener) {
        this.appAdsListener = adsListener;

        if (!interstitialAdsLoading) {

            if (!UtilsData.isContextActive(context) || isShowingAd) {
                Log.i(TAG, "onAdFailedToLoad: loadInterstitialAds isShowingAd :- " + isShowingAd);
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (isPurchased(context)) {
                Log.i(TAG, "onAdFailedToLoad: loadInterstitialAds purchase");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            if (isNetworkNotAvailable(context)) {
                Log.i(TAG, "onAdFailedToLoad: loadInterstitialAds no network");
                interstitialAdsLoading = false;
                if (appAdsListener != null) {
                    appAdsListener.onAdsLoadFailed();
                    appAdsListener = null;
                }
                return;
            }

            //MaxAdInter->Uncomment if needed
//            if (mInterstitialAd != null) {
//                Log.i(TAG, "onAdFailedToLoad: loadInterstitialAds mInterstitialAd NULL");
//                interstitialAdsLoading = false;
//                if (appAdsListener != null) {
//                    appAdsListener.onAdLoaded();
//                    appAdsListener = null;
//                }
//                return;
//            }

            interstitialAdsLoading = true;
            Log.i(TAG, "loadInterstitialAds: interstitialAdsLoading ");


            //MaxAdInterAdLoad



        } else {
            Log.i(TAG, "loadInterstitialAds: loading ads");
        }
    }

//    public boolean isInterstitialAdsAvailableToShow(@NonNull final Activity activity) {
//        return !isShowingAd && mInterstitialAd != null && !isPurchased(activity);
//    }

    public void showInterstitialAds(@NonNull final Activity activity, int _id, @Nullable final AdsListener adsListener, final boolean isNeedToShowProgress, final boolean isNeedNewRequest) {
        Log.i(TAG, "showInterstitialAds: _id :- " + _id + " isShowingAd :- " + isShowingAd);


            if (isPurchased(activity)) {
                Log.i(TAG, "onAdFailedToLoad: showInterstitialAds purchase");
                if (adsListener != null) {
                    adsListener.onAdsClose();
                }
                return;
            }

            //MaxAdInter->Uncommnet if needed
            if (MaxAdManager.INSTANCE.checkIsInterIsReady()) {

                if (_id != 1) {
                    final int min = 1;
                    final int max = _id;
                    _id = randomGenerator.nextInt((max - min) + 1) + min;
                    Log.i(TAG, "showInterstitialAds: _id :- " + _id);
                    if (_id != 1) {
                        if (adsListener != null) {
                            adsListener.onAdsClose();
                        }
                        return;
                    }
                }

                isAppStartUpAdsEnabled = false;
                isShowingAd = true;
                Log.i(TAG, "showInterstitialAds: isShowingAd");

                if (isNeedToShowProgress) {
                    showProgress(activity);
                    new Handler(Looper.myLooper()).postDelayed(() -> {
                        hideProgress(activity);
                        finalShowInterstitialAds(activity, adsListener, isNeedNewRequest);
                    }, 1000);
                } else {
                    hideProgress(activity);
                    finalShowInterstitialAds(activity, adsListener, isNeedNewRequest);
                }

            } else {

                isAppStartUpAdsEnabled = false;
//                if (isNeedNewRequest) {
//                    loadInterstitialAds(activity, UtilsData.interstitial_ad_unit_id, 0, null);
//                }

                MaxAdManager.INSTANCE.loadInterAd(activity, new MaxAdListener() {
                    @Override
                    public void onAdLoaded(boolean adLoad) {}
                    @Override
                    public void onAdShowed(boolean adShow) {}

                    @Override
                    public void onAdHidden(boolean adHidden) {}

                    @Override
                    public void onAdLoadFailed(boolean adLoadFailed) {}

                    @Override
                    public void onAdDisplayFailed(boolean adDisplayFailed) {}
                });


                if (adsListener != null) {
                    adsListener.onAdsLoadFailed();
                }
            }

    }

    private void finalShowInterstitialAds(Activity activity, AdsListener adsListener, boolean isNeedNewRequest) {

        //MaxAdInterAdShow>done

        MaxAdManager.INSTANCE.showInterAd(activity, new OnAdShowCallback() {
            @Override
            public void onAdHidden(boolean ishow) {
                Log.i(TAG, "onAdDismissedFullScreenContent: ");
                if (adsListener != null) {
                    adsListener.onAdsClose();
                }

                isShowingAd = false;
            }

            @Override
            public void onAdfailed() {
                if (adsListener != null) {
                    adsListener.onAdsLoadFailed();
                }

                isShowingAd = false;
            }

            @Override
            public void onAdDisplay() {
                if (adsListener != null) {
                    adsListener.onAdsOpened();
                }
                isShowingAd = true;
            }
        });
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        Log.i(TAG, "onActivityCreated: ");
        this.activity = activity;
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        Log.i(TAG, "onActivityStarted: ");
        this.activity = activity;
        isAppOpen = true;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        this.activity = activity;

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            isAppOpen = true;
            Log.i(TAG, "onActivityResumed: isAppOpenAdEnable :- " + isAppOpenAdEnable + " isNeedOpenAdRequest :- " + isNeedOpenAdRequest);

            if (isAppOpenAdEnable && isNeedOpenAdRequest && !isShowingAd) {
               // loadAdOpenAds(activity, UtilsData.app_open_id, 0);
            }

            isAppOpenAdEnable = false;
            isNeedOpenAdRequest = true;
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        Log.i(TAG, "onActivityPaused: ");
        this.activity = activity;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            isAppOpenAdEnable = isApplicationBroughtToBackground();
            isAppOpen = false;
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        Log.i(TAG, "onActivityStopped: ");
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
        Log.i(TAG, "onActivitySaveInstanceState: ");
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        Log.i(TAG, "onActivityDestroyed: ");
        isAppOpenAdEnable = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            isAppOpen = true;

            Log.i(TAG, "onActivityResumed: isAppOpenAdEnable :- " + isAppOpenAdEnable + " isNeedOpenAdRequest :- " + isNeedOpenAdRequest + " isShowingAd :- " + isShowingAd);
            if (isAppOpenAdEnable && isNeedOpenAdRequest && !isShowingAd) {
                //loadAdOpenAds(activity, UtilsData.app_open_id, 0);
            }

            isAppOpenAdEnable = false;
            isNeedOpenAdRequest = true;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            isAppOpenAdEnable = true;
            isAppOpen = false;
        }
    }

    private boolean isApplicationBroughtToBackground() {
        if (myApplication != null) {
            ActivityManager am = (ActivityManager) myApplication.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                if (tasks != null && !tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    return topActivity != null && !topActivity.getPackageName().equals(myApplication.getPackageName());
                }
            }
        }
        return false;
    }

    public interface AdsLoadsListener {
        void onAdLoaded();

        void onAdsLoadFailed();
    }

    public interface AdsListener {
        void onAdsOpened();

        void onAdsClose();

        void onAdsLoadFailed();
    }

}
