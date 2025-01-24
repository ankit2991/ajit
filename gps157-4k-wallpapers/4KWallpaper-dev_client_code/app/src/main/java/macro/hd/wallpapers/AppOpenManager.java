package macro.hd.wallpapers;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.adjust.sdk.Adjust;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.greedygame.core.models.general.AdErrors;
import com.greedygame.core.app_open_ads.general.AdOrientation;
import com.greedygame.core.app_open_ads.general.AppOpenAdsEventsListener;
import com.greedygame.core.app_open_ads.general.GGAppOpenAds;
import com.weewoo.sdkproject.events.EventHelper;

import org.jetbrains.annotations.NotNull;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
//import com.google.android.gms.ads.AdError;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.FullScreenContentCallback;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.Date;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

/** Prefetches App Open Ads. */
public class AppOpenManager implements LifecycleObserver,Application.ActivityLifecycleCallbacks{
    private static final String LOG_TAG = "AppOpenManager";
//    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/3419835294";
    private AppOpenAd appOpenAd = null;
    private Activity currentActivity;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;

    private WallpapersApplication myApplication;
    private long loadTime = 0;
    private final int MAX_ADS_SHOW = 3;
    private int userAdCount;
    private boolean isGGLoaded,isAdmanager;

    public boolean isPause() {
        return isPause;
    }

    private boolean isPause;

    private boolean haveJustShown = false;



    private EventHelper eventHelper = new EventHelper();
    boolean isForeground = true;
    private boolean isOpened = false;
    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;


    /** Constructor */
    public AppOpenManager(WallpapersApplication myApplication) {
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        userAdCount=0;

        if(CommonFunctions.isGreedyGameShow()){
            try {
                SettingStore settingStore=SettingStore.getInstance(myApplication.sContext);
                String adUnitId = settingStore.getFromSettingPreference(SettingStore.GG_OPEN_ID);
                if (!CommonFunctions.isEmpty(adUnitId)) {
//                    if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O && Build.VERSION.SDK_INT != Build.VERSION_CODES.O_MR1) {
                        GGAppOpenAds.setOrientation(AdOrientation.PORTRAIT);
//                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** LifecycleObserver methods */
    @OnLifecycleEvent(ON_START)
    public void onStart() {
        showAdIfAvailable();
        Log.d(LOG_TAG, "onStart");
    }

    /** Request an ad */
    public void fetchAd() {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable()) {
            return;
        }

        if (!CommonFunctions.isStoreVersion(myApplication.sContext)) {
            return;
        }

        if(WallpapersApplication.getApplication().isProUser()){
            return;
        }

        if(userAdCount>=MAX_ADS_SHOW) {
            return;
        }


//        if(CommonFunctions.isGreedyGameShow()){
//            SettingStore settingStore=SettingStore.getInstance(myApplication.sContext);
//            String adUnitId = settingStore.getFromSettingPreference(SettingStore.GG_OPEN_ID);
//            if (CommonFunctions.isEmpty(adUnitId)){
//                return;
//            }
//            loadGreedyGameOpen();
//            GGAppOpenAds.loadAd(adUnitId);
//        }

        String adUnitId;
        if (CommonFunctions.isTestAdEnable())
            adUnitId = myApplication.sContext.getResources().getString(R.string.ad_unit_id_open_ad_test);
        else{
            SettingStore settingStore=SettingStore.getInstance(myApplication.sContext);
            adUnitId = settingStore.getFromSettingPreference(SettingStore.ADMOB_OPEN_AD_ID);
        }

        if (CommonFunctions.isEmpty(adUnitId)){
            loadOnFailed();
            return;
        }
        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {

                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */
                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        AppOpenManager.this.appOpenAd = ad;
                        AppOpenManager.this.loadTime = (new Date()).getTime();
                        EventManager.sendEvent(EventManager.LBL_Advertise,EventManager.ATR_KEY_ADMOB_OPEN_AD,"onAppOpenAdLoaded");

                        if (!haveJustShown) {

                            showAdIfAvailable();
                        }
                    }


                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error.
                        EventManager.sendEvent(EventManager.LBL_Advertise,EventManager.ATR_KEY_ADMOB_OPEN_AD,"onAppOpenAdFailedToLoad");
                        loadOnFailed();
                    }
                };
        if(myApplication!=null) {
            AdRequest request = getAdRequest();
            AppOpenAd.load(
                    myApplication, adUnitId, request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
        }
    }

    private void loadOnFailed(){
//        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O && Build.VERSION.SDK_INT != Build.VERSION_CODES.O_MR1) {
            if(CommonFunctions.isAdManagerShow()) {
                fetchAdAdManger();
            }else if(CommonFunctions.isGreedyGameShow()) {
                    SettingStore settingStore = SettingStore.getInstance(myApplication.sContext);
                    String adUnitId = settingStore.getFromSettingPreference(SettingStore.GG_OPEN_ID);
                    if (CommonFunctions.isEmpty(adUnitId)) {
                        fetchAdAdManger();
                        return;
                    }
                    loadGreedyGameOpen();
                    GGAppOpenAds.loadAd(adUnitId);
            }
//        }
    }

    /** Request an ad */
    public void fetchAdAdManger() {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable()) {
            return;
        }

        if (!CommonFunctions.isStoreVersion(myApplication.sContext)) {
            return;
        }

        if(userAdCount>=MAX_ADS_SHOW) {
            return;
        }

        String adUnitId;

        SettingStore settingStore = SettingStore.getInstance(myApplication.sContext);
        if(CommonFunctions.isTestAdEnable()){
            adUnitId="/6499/example/app_open_new";
        }else
            adUnitId = settingStore.getFromSettingPreference(SettingStore.AM_OPEN_ID);

        if (CommonFunctions.isEmpty(adUnitId)) {
            return;
        }


        Logger.e(LOG_TAG,"fetchAdAdManger:"+adUnitId);

        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        super.onAdLoaded(appOpenAd);
                        isAdmanager=true;
                        AppOpenManager.this.appOpenAd = appOpenAd;
                        AppOpenManager.this.loadTime = (new Date()).getTime();
                        EventManager.sendEvent(EventManager.LBL_AM_Advertise,EventManager.ATR_KEY_AM_OPEN_AD,"onAppOpenAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        isAdmanager=false;
                        EventManager.sendEvent(EventManager.LBL_AM_Advertise,EventManager.ATR_KEY_AM_OPEN_AD,"onAppOpenAdFailedToLoad");
                    }
                };
        AdManagerAdRequest request = new AdManagerAdRequest.Builder().build();

        AppOpenAd.load(
                myApplication, adUnitId, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
    }

    private void loadGreedyGameOpen() {

        GGAppOpenAds.setListener(new AppOpenAdsEventsListener() {

            @Override
            public void onAdLoaded() {
                EventManager.sendEvent(EventManager.LBL_GG_Advertise,EventManager.ATR_KEY_GG_OPEN,"onAdLoaded");
                Log.d("GGADS","AppOpenAd loaded");
                isGGLoaded=true;
                AppOpenManager.this.loadTime = (new Date()).getTime();
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors adErrors) {
                Log.d("GGADS","AppOpenAd load failed ");
                EventManager.sendEvent(EventManager.LBL_GG_Advertise,EventManager.ATR_KEY_GG_OPEN,"onAdLoadFailed");
                isGGLoaded=false;
            }

            @Override
            public void onAdShowFailed() {
                Log.d("GGADS","AppOpenAd show failed");
            }

            @Override
            public void onAdOpened() {
                Log.d("GGADS","AppOpenAd Opened");
                isShowingAd = true;
            }
            @Override
            public void onAdClosed() {
                Log.d("GGADS","AppOpenAd closed");
                isShowingAd = false;
                haveJustShown = true;
                new Handler(Looper.getMainLooper()).postDelayed(() -> haveJustShown = false, 7000);
                GGAppOpenAds.setListener(null);
                fetchAd();
            }
        });

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(LOG_TAG, "onActivityStopped");

        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {

            isForeground = false;
            eventHelper.sendBackgroundEvent();
        }
    }


    /** Utility method to check if ad was loaded more than n hours ago. */
    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }


    private static boolean isShowingAd = false;

    /** Shows the ad if one isn't already showing. */
    public void showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.

//        if(currentActivity!=null && currentActivity instanceof JesusDetailActivity){
//            if(JesusDetailFragmentFinal.isDownloading){
//                return;
//            }
//        }

        if(WallpapersApplication.getApplication().isFBDownloadLoaded){
            return;
        }

        if(WallpapersApplication.getApplication().isInterstitialShow){
            return;
        }

        if(WallpapersApplication.getApplication().isProUser()){
            return;
        }

        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            isAdmanager=false;
                            haveJustShown = true;
                            new Handler(Looper.getMainLooper()).postDelayed(() -> haveJustShown = false, 7000);
                            fetchAd();
                        }
                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            isAdmanager=false;
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                            if(isAdmanager)
                                EventManager.sendEvent(EventManager.LBL_AM_Advertise,EventManager.ATR_KEY_AM_OPEN_AD,"onAdShowedFullScreenContent");
                            else
                                EventManager.sendEvent(EventManager.LBL_Advertise,EventManager.ATR_KEY_ADMOB_OPEN_AD,"onAdShowedFullScreenContent");
                        }
                    };

            if(appOpenAd!=null) {
                appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                appOpenAd.show(currentActivity);
            }
            userAdCount++;
        }else if(!isShowingAd && isGGLoaded){
            GGAppOpenAds.show();
            isGGLoaded=false;
            userAdCount++;
        }  else {
            Log.d(LOG_TAG, "Can not show ad.");
            fetchAd();
        }
    }

    /** Creates and returns ad request. */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /** Utility method that checks if ad exists and can be shown. */

    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }



    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
        Log.d(LOG_TAG, "onActivityDestroyed");
    }


    public void destroyApplication(){
        if(myApplication!=null)
            myApplication.unregisterActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);

        appOpenAd=null;
        currentActivity=null;
        loadCallback=null;
        myApplication=null;
        GGAppOpenAds.setListener(null);
    }

    /** ActivityLifecycleCallback methods */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onActivityCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
        Log.d(LOG_TAG, "onActivityStarted");
        isPause=false;

        if (++activityReferences == 1 && !isActivityChangingConfigurations) {

            if (!isForeground) {

                eventHelper.sendForegroundEvent();
            }
            isForeground = true;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
        Log.d(LOG_TAG, "onActivityResumed");
        isPause=false;

        Adjust.onResume();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(LOG_TAG, "onActivityPaused");
        isPause=true;

        Adjust.onPause();
    }

}