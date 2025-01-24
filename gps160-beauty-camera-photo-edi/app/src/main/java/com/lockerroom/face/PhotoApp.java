package com.lockerroom.face;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;
//
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.lockerroom.face.activities.EditorSplashActivity;
import com.lockerroom.face.firebaseAdsConfig.RemoteConfigData;
import com.lockerroom.face.maxAdManager.AppOpenCallback;
import com.lockerroom.face.maxAdManager.MaxAppOpenAdManager;
import com.lockerroom.face.utils.SharePreferenceUtil;
//import com.weewoo.sdkproject.SDKProject;

import java.util.ArrayList;
import java.util.Objects;

import timber.log.Timber;

public class PhotoApp extends Application implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private static PhotoApp sPhotoApp;
    private Activity currentActivity;
    public static Resources resources;
    private boolean isFirstFetch = true;

//    public MaxAppOpenAdManager appOpenAdManager;

    // Remote Config keys
    private static final String ALL_ADS_ENABLED = "all_ads_enabled";


    private FirebaseRemoteConfig mFirebaseRemoteConfig;


    public void onCreate() {
        super.onCreate();
        sPhotoApp = this;
        registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        AdjustConfig config = new AdjustConfig(this, getString(R.string.adjust_id), getAdjustConfig());
        Adjust.initSdk(config);
//        SDKProject.INSTANCE.config(this, "NzE0", "Tmt4RlVURndhSEk9OlpHSjRmRFY1SUZaU2JYNHBVVHRVWUE9PQ==");
        resources = getResources();
//        D7EF04558C5B8D0CF1FB26F41EC46227
        ArrayList<String> idsTest = new ArrayList<>();
        idsTest.add("D7EF04558C5B8D0CF1FB26F41EC46227");
        idsTest.add("2283077FF675EE509F974F7187C98760");
        idsTest.add("C9DC580F682B26813B8281A9A5D9797A");
        idsTest.add("FB02786A1A0F294D55ECAB38B7C0744A");
        idsTest.add("4486F8359D68B01582819D2E8E9358E2");
        idsTest.add("02D68A0876F8691421D0C6775CFDC7F2");


        if (Build.VERSION.SDK_INT >= 24) {
            try {
                StrictMode.class.getMethod("disableDeathOnFileUriExposure", new Class[0]).invoke(null, new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        appOpenAdManager = new MaxAppOpenAdManager(getApplicationContext());

    }

    public void initConfig(Activity activity) {

        int interval = 28800 /*8*3600     Eight Hours in secs.*/;
        // Get Remote Config instance.
        // [START get_remote_config_instance]
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        getLatestConfiguration(activity);
        // [END get_remote_config_instance]
        // Create a Remote Config Setting to enable developer mode, which you can use to increase
        // the number of fetches available per hour during development. Also use Remote Config
        // Setting to set the minimum fetch interval.
        // [START enable_dev_mode]
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        // [END enable_dev_mode]
        // Set default Remote Config parameter values. An app uses the in-app default values, and
        // when you need to adjust those defaults, you set an updated value for only the values you
        // want to change in the Firebase console. See Best Practices in the README for more
        // information.
        // [START set_default_values]
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        // [END set_default_values]


    }


    /**
     * Fetch a welcome message from the Remote Config service, and then activate it.
     */

    private void getLatestConfiguration(Activity activity) {
        // [START fetch_config_with_callback]
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(activity, (OnCompleteListener<Boolean>) task -> {
                    if (task.isSuccessful()) {
                        mFirebaseRemoteConfig.activate();
                        Boolean welcomeMessage = mFirebaseRemoteConfig.getBoolean(ALL_ADS_ENABLED);
                        RemoteConfigData.INSTANCE.setAllAdsEnabled_(welcomeMessage);
//                        Toast.makeText(sPhotoApp, "Fetch and activate succeeded", Toast.LENGTH_SHORT).show();
//                        Toast.makeText(sPhotoApp, "Fetched value" + welcomeMessage, Toast.LENGTH_SHORT).show();
//                        Log.e("Fetching Config", "====================>Fetch Success");
                    } else {
//                        Log.e("Fetching Config", "====================>Fetch Failed");
//                        Toast.makeText(sPhotoApp, "Fetch failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private String getAdjustConfig() {
        if (BuildConfig.DEBUG) {
            return AdjustConfig.ENVIRONMENT_SANDBOX;
        } else {
            return AdjustConfig.ENVIRONMENT_PRODUCTION;
        }
    }

    public Context getContext() {
        return sPhotoApp.getContext();
    }




    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        if (!SharePreferenceUtil.isPurchased(this)) {
//            appOpenManager.setCurrentActivity(activity);
            currentActivity = activity;
            if (activity instanceof EditorSplashActivity) {
                initConfig(activity);
            }
//            if (!appOpenManager.isAdAvailable()) {
////              Toast.makeText(activity, "Loading Ad", Toast.LENGTH_SHORT).show();
//                appOpenManager.loadAd(activity);
//            }
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
//        if (!SharePreferenceUtil.isPurchased(this)) {
//            if (appOpenManager != null && !appOpenManager.isShowingAd()) {
//                this.activity = activity;
//            }
//        }
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
//        if (!SharePreferenceUtil.isPurchased(this)) {
//            appOpenManager.setCurrentActivity(activity);
//
//
//        }
        Adjust.onResume();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

        Adjust.onPause();
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
//        appOpenManager.setCurrentActivity(null);

    }


    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        if (currentActivity instanceof EditorSplashActivity) return;
        if (!SharePreferenceUtil.isPurchased(this)) {
            //MaxAdOpenApp>>done

            MaxAppOpenAdManager.Companion.getInstance(getApplicationContext()).loadAd(currentActivity, new AppOpenCallback() {
                @Override
                public void isAdLoad(boolean isLoad) {}
                @Override
                public void isAdShown(boolean isShow) {}
                @Override
                public void isAdDismiss(boolean isShow) {}
            });
        }
    }

}
