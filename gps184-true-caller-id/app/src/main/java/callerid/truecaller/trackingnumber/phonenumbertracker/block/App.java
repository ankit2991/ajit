package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdAppOpen;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdAppOpenCallback;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdConstants;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.iap.IapConnector;


public class App extends MultiDexApplication implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    public static App appLicationLoad;
    private Activity currentActivity;
    private String currentActivityName = "";
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        appLicationLoad = this;
        instance = this;

        MultiDex.install(this);
        AudienceNetworkAds.initialize(this);

        List<String> testDevices = new ArrayList<>();
        testDevices.add(AdRequest.DEVICE_ID_EMULATOR);
        testDevices.add("CB2996FEF389BC3A39D0C584FAAC5900");
        testDevices.add("4486F8359D68B01582819D2E8E9358E2");
        testDevices.add("72A7DA0D72336E6FA646013F081830E4");

        RequestConfiguration requestConfiguration
                = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                Log.d("MyApp size ads Adapter", statusMap.size() + "");
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus status = statusMap.get(adapterClass);
                    Log.d("MyApp", String.format(
                            "Adapter name: %s, Description: %s, Latency: %d",
                            adapterClass,
                            status != null ? status.getDescription() : "",
                            status != null ? status.getLatency() : -1));
                }

            }
        });

//        AppOpenManager.getInstance().initialize(this, true);
//        AppOpenManager.getInstance().fetchAd();

        String appToken = "pxpc4a1jdkhs";
        String environment = "";
        if (BuildConfig.DEBUG) {
            environment = AdjustConfig.ENVIRONMENT_SANDBOX;
        } else {
            environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
        }
        AdjustConfig config = new AdjustConfig(this, appToken, environment);
        Adjust.onCreate(config);

        registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        ArrayList<String> ids = new ArrayList<>();
        ids.add("b0049bee-734f-480e-bcd4-df9408825398");
        //init in app purchase
        IapConnector.Companion.getInstance(this);

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        currentActivity = activity;
        currentActivityName = activity.getLocalClassName();
    }

    private MaxAdAppOpen appOpenAdMax = null;
    private boolean isAdBusy = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable callback = () -> {
        if (appOpenAdMax != null) {
            appOpenAdMax.destroy();
            appOpenAdMax = null;
        }
    };

    private void startAdLoaderTimer(long seconds) {
        handler.postDelayed(callback, seconds * 1000);
    }

    private void stopAdLoaderTimer() {
        handler.removeCallbacksAndMessages(null);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart() {
        if (currentActivity == null || TinyDB.getInstance(this).weeklyPurchased()
                || currentActivity instanceof SplashActivitynew) {
            if (appOpenAdMax != null) {
                appOpenAdMax.destroy();
                appOpenAdMax = null;
            }
            return;
        }

        if (appOpenAdMax == null) {
            isAdBusy = true;
            appOpenAdMax = new MaxAdAppOpen(
                    this,
                    MaxAdConstants.MAX_AD_APP_OPEN_ID_BACKGROUND,
                    new MaxAdAppOpenCallback() {

                        @Override
                        public void onAdDisplayFailed() {
                            isAdBusy = false;
                        }

                        @Override
                        public void onAdLoadFailed() {
                            stopAdLoaderTimer();
                            isAdBusy = false;
                        }

                        @Override
                        public void onAdClicked() {

                        }

                        @Override
                        public void onAdHidden() {
                            isAdBusy = false;
                        }

                        @Override
                        public void onAdDisplayed() {

                        }

                        @Override
                        public void onAdLoaded() {
                            stopAdLoaderTimer();
                            if (currentActivity != null
                                    && currentActivity instanceof AppCompatActivity
                                    && ((AppCompatActivity) currentActivity).getLifecycle()
                                    .getCurrentState()
                                    .isAtLeast(Lifecycle.State.RESUMED)
                            ) {
                                isAdBusy = appOpenAdMax != null && appOpenAdMax.showAdIfReady(MaxAdConstants.MAX_AD_APP_OPEN_ID_BACKGROUND);
                            }
                        }
                    }
            );
            startAdLoaderTimer(MaxAdConstants.MAX_AD_LOAD_TIMEOUT);
        } else if (!isAdBusy) {
            isAdBusy = true;
            appOpenAdMax.loadAd();
            startAdLoaderTimer(MaxAdConstants.MAX_AD_LOAD_TIMEOUT);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {


    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
        currentActivityName = activity.getLocalClassName();

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

    }

    public static App getApp() {
        return instance;
    }
}