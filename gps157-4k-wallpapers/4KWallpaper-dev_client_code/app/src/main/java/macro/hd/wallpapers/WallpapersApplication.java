package macro.hd.wallpapers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.multidex.MultiDex;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.bumptech.glide.Glide;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.flurry.android.FlurryAgent;
import com.google.ads.mediation.facebook.FacebookAdapter;
import com.google.ads.mediation.facebook.FacebookExtras;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.greedygame.core.GreedyGameAds;
import com.greedygame.core.adview.general.AdLoadCallback;
import com.greedygame.core.adview.general.GGAdview;
import com.greedygame.core.interfaces.PrefetchAdsListener;
import com.greedygame.core.interstitial.general.GGInterstitialAd;
import com.greedygame.core.interstitial.general.GGInterstitialEventsListener;
import com.greedygame.core.models.general.AdErrors;
import com.greedygame.core.models.general.PrefetchUnit;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;
import com.ironsource.mediationsdk.sdk.InterstitialListener;
import com.weewoo.sdkproject.SDKProject;
import com.weewoo.sdkproject.events.EventHelper;

import org.jetbrains.annotations.NotNull;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;

import macro.hd.wallpapers.AppController.RequestManager;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Interface.Activity.MainNavigationActivity;
import macro.hd.wallpapers.Model.AppSettings;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.IModelBase;
import macro.hd.wallpapers.Model.Wallpapers;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.DiscreteScrollViewOptions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.NetworkChecker;
import macro.hd.wallpapers.Utilily.UniversalAdListener;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.NotifierFactory;

//import androidx.preference.PreferenceManager;

/**
 * main application
 */

public class WallpapersApplication extends Application implements NativeAdsManager.Listener, View.OnClickListener {

    public static List<Wallpapers> newList = new ArrayList<>();
    public static int counter = -1;

    public static AppOpenManager getAppOpenManager() {
        return appOpenManager;
    }

    public static void setAppOpenManager(AppOpenManager appOpenManager) {
        WallpapersApplication.appOpenManager = appOpenManager;
    }

    private static AppOpenManager appOpenManager;

    private EventHelper eventHelper = new EventHelper();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private static WallpapersApplication jesusApplication;
    public static boolean isAppRunning = false;
    public boolean isInterstitialShow;

    public static void setApplication(WallpapersApplication application) {
        jesusApplication = application;
    }

    public static WallpapersApplication getApplication() {
        return jesusApplication;
    }

    public static Context sContext = null;

    public FirebaseAnalytics mFirebaseAnalytics;

    public FirebaseAnalytics getFirebaseAnalytics() {
        try {
            if (mFirebaseAnalytics == null)
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mFirebaseAnalytics;
    }

    private NetworkChecker _networkChecker;


    private String getProcessName(Context context) {
        if (context == null) return null;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreate() {
        super.onCreate();

        setAdjust();
        sContext = getApplicationContext();
//        setupLeakCanary();
        DiscreteScrollViewOptions.init(this);
        Logger.e("FoniconApplication", "onCreate");

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        _networkChecker = new NetworkChecker(this);
        Logger.e("FoniconApplication", "onCreate :: Network " + _networkChecker.isNetworkAvailable());

//        if (!BuildConfig.DEBUG)
//            Fabric.with(this, new Crashlytics());

//        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        sContext = new ContextThemeWrapper(this, macro.hd.wallpapers.R.style.AppTheme);
//		Thread.setDefaultUncaughtExceptionHandler(new AppUncaughtExceptionHandler());
        setApplication(this);

        settings = SettingStore.getInstance(sContext);
        updateAndroidSecurityProvider(sContext);

        try {
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            sslContext.createSSLEngine();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                String processName = getProcessName(this);
                String packageName = this.getPackageName();
                if (!packageName.equals(processName)) {
                    WebView.setDataDirectorySuffix(processName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//		if (BuildConfig.DEBUG) {
//			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//					.detectDiskReads()
//					.detectDiskWrites()
//					.detectAll()
//					.penaltyLog()
//					.build());
//
//			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//					.detectLeakedSqlLiteObjects()
//					.detectLeakedClosableObjects()
//					.penaltyLog()
//					.build());
//		}

//        scheduleJob(sContext);

        if (CommonFunctions.isStoreVersion(sContext)) {
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    EventManager.sendEvent(EventManager.LBL_HOME, EventManager.LBL_Advertise, "Admob Initialized");
                }
            });
        }

        if (CommonFunctions.isStoreVersion(sContext)) {
            AudienceNetworkInitializeHelper.initialize(this);
        }

        if (BuildConfig.DEBUG) {
            AdSettings.addTestDevice("1d76e6cb-71de-4fb4-b72b-e77b4545a3a3");
//            AdSettings.addTestDevice("3e3fc5a0-a70a-4c34-875f-a45c8b6c3ef4");

            List<String> testDeviceIds = Arrays.asList("E86A07F03CE03FC2989B191B256A2191");
            RequestConfiguration configuration =
                    new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
            MobileAds.setRequestConfiguration(configuration);
        }

        try {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .build(this, "MCH7BX25BH9V2RZJR97W");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        appOpenManager = new AppOpenManager(this);

        SDKProject.INSTANCE.config(this, "NzEz", "TjMxS1R3dEJNR1E9OlZDY2dXbnNoTDI1Qk9nMGhMVHB3VlE9PQ==");
    }

    public NetworkChecker getNetworkChecker() {
        return _networkChecker;
    }

    private void updateAndroidSecurityProvider(Context callingActivity) {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
//			GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), callingActivity, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Logger.e("SecurityException", "Google Play Services not available.");
        }
    }

    SettingStore settings;


    @Override
    public void onTerminate() {
        super.onTerminate();
        Logger.e("FoniconApplication", "onTerminate");
    }

    public enum AdType {
        ADMOB,
        GG,
        IS,
        NONE
    }

    public static String AD_AM = "AM";
    public static String AD_GG = "GG";
    public static String AD_IS = "IS";


    private ArrayList<UniversalAdListener> universalAdListener = new ArrayList<>();

    public void addUniversalAdListener(UniversalAdListener universalAdListener) {
        this.universalAdListener.add(universalAdListener);
    }

    public void removeUniversalAdListener(UniversalAdListener universalAdListener) {
        this.universalAdListener.remove(universalAdListener);
    }

    public AppSettings getSettings() {
//        Logger.e("getsettings",""+UserInfoManager.getInstance(sContext).getUserInfo().getApp_settings().get(0).getPackage());
        return UserInfoManager.getInstance(sContext).getUserInfo().getApp_settings().get(0);
    }

    private boolean isFromSplash;
    public int currentAdDisplayCount = 0;

    public void setUpAd(boolean isFromSplash) {
        this.isFromSplash = isFromSplash;
        if (getSettings().getAdDisable().equalsIgnoreCase("0")) {
            String lastStore = "";
            lastStore = settings.getRandomNumber();

//            if (CommonFunctions.isStoreVersion(sContext)) {
            if (getSettings().getApplyPlaystore().equalsIgnoreCase("1")) {

                Logger.e("lastStore:", "" + lastStore);

                //TODO lets ignore backend, always take IronSource
                currentAdType = AdType.IS;
                /*
                if (lastStore.equalsIgnoreCase(AD_AM)) {
                    currentAdType = AdType.ADMOB;
                } else if (lastStore.equalsIgnoreCase(AD_IS)) {
                    currentAdType = AdType.IS;
                } else if (lastStore.equalsIgnoreCase(AD_GG)) {
                    currentAdType = AdType.GG;
                } else {
                    currentAdType = AdType.NONE;
                }
                */
            } else {
                currentAdType = AdType.NONE;
            }
//            }else{
//                currentAdType=AdType.NONE;
//            }
            Logger.e("type ", "" + currentAdType.toString());
        } else
            currentAdType = AdType.NONE;

//        currentAdType=AdType.IS;

        if (isProUser()) {
            currentAdType = AdType.NONE;
        }
    }

    public AdType getCurrentAdType() {
        return currentAdType;
    }

    private AdType currentAdType;

    //	FlurryAdInterstitial mFlurryAdInterstitial;
//    private  com.facebook.ads.InterstitialAd interstitialAd_fb;

    //	UcUnionAdSdk ucAds;
    InterstitialAd mInterstitialAd;
//    AdRequest.Builder adRequestBuilder;


    public void onPause(Activity act) {
        IronSource.onPause(act);
    }

    public void onResume(Activity act) {
        IronSource.onResume(act);
    }

    public IronSourceBannerLayout getIronSourceBanner() {
        return banner;
    }

    public void setIronSourceBanner(IronSourceBannerLayout banner) {
        this.banner = banner;
    }

    private IronSourceBannerLayout banner;

    public void onDestroy() {
    }

    private int getCounter() {
        if (universalAdListener != null && universalAdListener.size() > 0) {
            return universalAdListener.size() - 1;
        } else
            return 0;
    }

    public List<String> getGGNativeAds() {
        return GGNativeAds;
    }


    public void loadGGNativeAd() {
        try {
            SettingStore preferenceStore = SettingStore.getInstance(this);
            String adUnitId = preferenceStore.getFromSettingPreference(SettingStore.GG_NATIVE_ID);
            Logger.e(TAG, "loadGGNativeAd " + adUnitId);
            if (CommonFunctions.isEmpty(adUnitId)) {
                if (CommonFunctions.isAdManagerShow())
                    adManagerNativeAd();
                return;
            }

            if (CommonFunctions.isStoreVersion(sContext)) {
                isNativeAdEnable = true;
            }

            try {
                NUMBER_OF_ADS = adUnitId.split(",").length;
            } catch (Exception e) {
                e.printStackTrace();
                NUMBER_OF_ADS = 3;
            }

            Logger.e(TAG, "NUMBER_OF_ADS:" + NUMBER_OF_ADS + " adUnitId:" + adUnitId);
//            isNativeAdEnable = false;
            if (isNativeAdEnable) {

                PrefetchAdsListener prefetchAdsListener = new PrefetchAdsListener() {
                    @Override
                    public void onAdPrefetched(String unitId) {
                        //Callback when a unit is prefetched successfully
                        Logger.e(TAG, "GG onAdPrefetched");
                    }

                    @Override
                    public void onAdPrefetchFailed(@NotNull String s, @NotNull AdErrors adErrors) {
                        Logger.e(TAG, "GG onAdPrefetchFailed");
                        EventManager.sendEvent(EventManager.LBL_GG_Advertise, EventManager.ATR_KEY_GG_NATIVE, "onAdPrefetchFailed");
                    }

                    @Override
                    public void onPrefetchComplete() {
                        EventManager.sendEvent(EventManager.LBL_GG_Advertise, EventManager.ATR_KEY_GG_NATIVE, "onPrefetchComplete");

                        //Callback when all units are prefetched. This does not guarantee that all units have successfully prefetched.
                        Logger.e(TAG, "GG onPrefetchComplete");
                        if (handler != null)
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String[] split = adUnitId.split(",");
                                        for (int i = 0; i < split.length; i++) {
                                            GGNativeAds.add(split[i]);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    isGGLoaded = true;
                                    EventNotifier notifier =
                                            NotifierFactory.getInstance().getNotifier(
                                                    NotifierFactory.EVENT_NOTIFIER_AD_STATUS);
                                    notifier.eventNotify(EventTypes.EVENT_AD_LOADED, null);
                                }
                            });
                    }
                };
                String[] data = adUnitId.split(","); //add all the Unit IDs to prefetch

                if (data != null && data.length > 0) {
                    PrefetchUnit[] units = new PrefetchUnit[data.length];
                    int i = 0;
                    for (String ids : data) {
                        PrefetchUnit temp = new PrefetchUnit(ids, PrefetchUnit.UnitType.NATIVE_OR_BANNER);
                        units[i] = temp;
                        i++;
                    }
                    GreedyGameAds.prefetchAds(prefetchAdsListener, units);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> GGNativeAds = new ArrayList<>();
    Handler handler = new Handler();

    //    private int counter=0;
    public void requestNewInterstitial(Activity activity) {
        if (isProUser()) {
            return;
        }

        if (!CommonFunctions.isStoreVersion(sContext)) {
            return;
        }

        if (getSettings().getAdDisable().equalsIgnoreCase("1")) {
//			if(sContext instanceof SplashActivity) {
//				Intent intent = new Intent(sContext, HomeActivity.class);
//				startActivity(intent);
//				finish();
//			}

            return;
        }
        if (getSettings().getTotalAdCount() <= getApplication().currentAdDisplayCount) {
//			if(sContext instanceof SplashActivity) {
//				Intent intent = new Intent(sContext, HomeActivity.class);
//				startActivity(intent);
//				finish();
//			}

            return;
        }

        isInterstitialShow = false;
        //TODO lets ignore backend, always take IronSource
        loadInterstitialIron(activity);
        /*
        AdRequest.Builder adRequestBuilder;
        if (currentAdType == AdType.ADMOB) {
            adRequestBuilder = new AdRequest.Builder();

            String adUnitId;
            if (CommonFunctions.isTestAdEnable()) {
                adUnitId = getResources().getString(R.string.ad_unit_id_test);
            } else {
                if (isFromSplash) {
                    adUnitId = settings.getFromSettingPreference(SettingStore.ADMOB_INTERSTITIAL_SPLASH_ID);
                } else {
                    adUnitId = settings.getFromSettingPreference(SettingStore.ADMOB_INTERSTITIAL_ID);
                }
            }

            if (CommonFunctions.isEmpty(adUnitId)) {
                interstitalFailOn(activity);
                return;
            }

            Logger.e("requestNewInterstitial", "adUnitId:" + adUnitId);

            AdRequest adRequest = adRequestBuilder.build();
            InterstitialAd.load(this, adUnitId, adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            // The mInterstitialAd reference will be null until
                            // an ad is loaded.

                            mInterstitialAd = interstitialAd;
                            Logger.e("admob onAdLoaded", "onAdLoaded");

                            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    // Called when fullscreen content is dismissed.
                                    Log.d("TAG", "The ad was dismissed.");
                                    Logger.e("admob onAdClosed", "onAdClosed");
                                    mInterstitialAd = null;
                                    isInterstitialShow = false;
                                    closedAd();
                                    if (universalAdListener != null && universalAdListener.size() > 0)
                                        universalAdListener.get(getCounter()).onAdClosed();
                                }


                                @Override
                                public void onAdImpression() {
                                    super.onAdImpression();
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                    super.onAdFailedToShowFullScreenContent(adError);
                                    mInterstitialAd = null;
                                    if (isFromSplash)
                                        EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_ADMOB_SPLASH, "onAdFailedToShowFullScreenContent");
                                    else
                                        EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_ADMOB, "onAdFailedToShowFullScreenContent");
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    // Called when fullscreen content is shown.
                                    // Make sure to set your reference to null so you don't
                                    // show it a second time.
                                    isInterstitialShow = true;
                                    Log.d("TAG", "The ad was shown.");
                                }
                            });
                            if (isFromSplash)
                                EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_ADMOB_SPLASH, "onAdLoaded");
                            else
                                EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_ADMOB, "onAdLoaded");

                            if (universalAdListener != null && universalAdListener.size() > 0)
                                universalAdListener.get(getCounter()).onAdLoaded();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            mInterstitialAd = null;
                            Logger.e("admob onAdFailedToLoad", "onAdFailedToLoad" + loadAdError.getMessage());
                            if (isFromSplash)
                                EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_ADMOB_SPLASH, "onAdFailedToLoad" + loadAdError.getCode());
                            else
                                EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_ADMOB, "onAdFailedToLoad:" + loadAdError.getCode());
                            interstitalFailOn(activity);
                        }
                    });
        } else if (currentAdType == AdType.GG) {
            if (CommonFunctions.isAdManagerShow()) {
                loadAdManagerInterst(activity);
            } else if (CommonFunctions.isGreedyGameShow()) {
                loadGreedyGameInterst(activity);
            }
        } else if (currentAdType == AdType.IS) {
            loadInterstitialIron(activity);
        } else if (currentAdType == AdType.NONE) {
//			if(sContext instanceof SplashActivity) {
//				Intent intent = new Intent(sContext, HomeActivity.class);
//				showInterstitial(sContext, intent, true);
//			}
        }
         */
    }

    private void loadInterstitialIron(Activity activity) {
        IronSource.setInterstitialListener(new InterstitialListener() {
            /**
             * Invoked when Interstitial Ad is ready to be shown after load function was called.
             */
            @Override
            public void onInterstitialAdReady() {
                Logger.e("Ironssrouce ", "onInterstitialAdReady");
                if (isFromSplash)
                    EventManager.sendEvent(EventManager.LBL_Advertise_IS, EventManager.ATR_KEY_ADMOB_SPLASH, "onInterstitialAdReady");
                else
                    EventManager.sendEvent(EventManager.LBL_Advertise_IS, EventManager.ATR_KEY_SPLASH_AD, "onInterstitialAdReady");

                if (universalAdListener != null && universalAdListener.size() > 0)
                    universalAdListener.get(getCounter()).onAdLoaded();
            }

            /**
             * invoked when there is no Interstitial Ad available after calling load function.
             */
            @Override
            public void onInterstitialAdLoadFailed(IronSourceError error) {
                Logger.e("Ironssrouce onAdFailedToLoad", "onAdFailedToLoad" + error.getErrorMessage());
                if (isFromSplash)
                    EventManager.sendEvent(EventManager.LBL_Advertise_IS, EventManager.ATR_KEY_ADMOB_SPLASH, "onAdFailedToLoad" + error.getErrorCode());
                else
                    EventManager.sendEvent(EventManager.LBL_Advertise_IS, EventManager.ATR_KEY_SPLASH_AD, "onAdFailedToLoad:" + error.getErrorCode());

                try {
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                interstitalFailOn(activity);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            /**
             * Invoked when the Interstitial Ad Unit is opened
             */
            @Override
            public void onInterstitialAdOpened() {
                Logger.e("Ironssrouce ", "onInterstitialAdClosed");
                isInterstitialShow = true;
            }

            /*
             * Invoked when the ad is closed and the user is about to return to the application.
             */
            @Override
            public void onInterstitialAdClosed() {
                Logger.e("Ironssrouce ", "onInterstitialAdClosed");
                isInterstitialShow = false;
                closedAd();
                if (universalAdListener != null && universalAdListener.size() > 0)
                    universalAdListener.get(getCounter()).onAdClosed();
            }

            /**
             * Invoked when Interstitial ad failed to show.
             * @param error - An object which represents the reason of showInterstitial failure.
             */
            @Override
            public void onInterstitialAdShowFailed(IronSourceError error) {
                Logger.e("Ironssrouce ", "onInterstitialAdShowFailed:" + error.getErrorMessage());

                if (isFromSplash)
                    EventManager.sendEvent(EventManager.LBL_Advertise_IS, EventManager.ATR_KEY_ADMOB_SPLASH, "onInterstitialAdShowFailed");
                else
                    EventManager.sendEvent(EventManager.LBL_Advertise_IS, EventManager.ATR_KEY_SPLASH_AD, "onInterstitialAdShowFailed:");

                isInterstitialShow = false;
                closedAd();
                if (universalAdListener != null && universalAdListener.size() > 0)
                    universalAdListener.get(getCounter()).onAdClosed();
            }

            /*
             * Invoked when the end user clicked on the interstitial ad, for supported networks only.
             */
            @Override
            public void onInterstitialAdClicked() {

            }

            /** Invoked right before the Interstitial screen is about to open.
             *  NOTE - This event is available only for some of the networks.
             *  You should NOT treat this event as an interstitial impression, but rather use InterstitialAdOpenedEvent
             */
            @Override
            public void onInterstitialAdShowSucceeded() {
                Logger.e("Ironssrouce ", "onInterstitialAdShowSucceeded");
            }
        });

        if (!isFromSplash) {

            IronSource.loadInterstitial();
        }
    }

    private AdManagerInterstitialAd mPublisherInterstitialAd;

    private void loadAdManagerInterst(Activity activity) {

        String adUnitId = "";

        if (CommonFunctions.isTestAdEnable()) {
            adUnitId = "/6499/example/interstitial";
        } else {
            if (isFromSplash) {
                adUnitId = settings.getFromSettingPreference(SettingStore.AM_SPLASH_INTERSTITIAL_ID);
            } else
                adUnitId = settings.getFromSettingPreference(SettingStore.AM_INTERSTITIAL_ID);
        }

        if (CommonFunctions.isEmpty(adUnitId)) {
            return;
        }

        Logger.e(TAG, "loadAdManagerInterst:" + adUnitId);

        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();

        AdManagerInterstitialAd.load(this, adUnitId, adRequest,
                new AdManagerInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
                        // The mAdManagerInterstitialAd reference will be null until
                        // an ad is loaded.
                        mPublisherInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");

                        mPublisherInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");
                                mPublisherInterstitialAd = null;
                                isInterstitialShow = false;
                                closedAd();
                                if (universalAdListener != null && universalAdListener.size() > 0)
                                    universalAdListener.get(getCounter()).onAdClosed();

                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                Log.d("TAG", "The ad failed to show.");
                                mPublisherInterstitialAd = null;
                                if (isFromSplash)
                                    EventManager.sendEvent(EventManager.LBL_AM_Advertise, EventManager.ATR_KEY_GG_SPASH, "onAdFailedToShowFullScreenContent");
                                else
                                    EventManager.sendEvent(EventManager.LBL_AM_Advertise, EventManager.ATR_KEY_GG, "onAdFailedToShowFullScreenContent");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                isInterstitialShow = true;
                                Log.d("TAG", "The ad was shown.");
                            }
                        });
                        if (isFromSplash)
                            EventManager.sendEvent(EventManager.LBL_AM_Advertise, EventManager.ATR_KEY_GG_SPASH, "onAdLoaded");
                        else
                            EventManager.sendEvent(EventManager.LBL_AM_Advertise, EventManager.ATR_KEY_GG, "onAdLoaded");
                        if (universalAdListener != null && universalAdListener.size() > 0)
                            universalAdListener.get(getCounter()).onAdLoaded();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mPublisherInterstitialAd = null;

                        Logger.d("GGADS", "Ad Load Failed " + loadAdError.getCode());
                        if (isFromSplash)
                            EventManager.sendEvent(EventManager.LBL_AM_Advertise, EventManager.ATR_KEY_GG_SPASH, "onAdLoadFailed");
                        else
                            EventManager.sendEvent(EventManager.LBL_AM_Advertise, EventManager.ATR_KEY_GG, "onAdLoadFailed:");
                        if (universalAdListener != null && universalAdListener.size() > 0)
                            universalAdListener.get(getCounter()).onAdFailedToLoad(0);
                    }
                });
    }

    private void interstitalFailOn(Activity activity) {
        if (CommonFunctions.isAdManagerShow()) {
            loadAdManagerInterst(activity);
        } else if (CommonFunctions.isGreedyGameShow()) {
            loadGreedyGameInterst(activity);
        }
    }

    private void closedAd() {
//		if(settings.getAdDisable().equalsIgnoreCase("1")){
//			return;
//		}
//		if(settings.getTotalAdCount()<=GPSFindApplication.getApplication().currentAdDisplayCount){
//			return;
//		}
        try {
            if (intent != null)
                act.startActivity(intent);
            if (isFinished) {
                if (act != null && !act.isFinishing())
                    act.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isDisable() {
        if (currentAdType == AdType.NONE) {
            return true;
        }
        return false;
    }

    public boolean isAdLoaded() {
        // Commented on 09012020
        //TODO lets ignore backend, always take IronSource
        return IronSource.isInterstitialReady();
        /*
        if (currentAdType == AdType.ADMOB && mInterstitialAd != null) {
            return true;
        } else if (currentAdType == AdType.IS && IronSource.isInterstitialReady()) {
            return true;
        } else if (mPublisherInterstitialAd != null) {
            return true;
        } else if (ggInterstitialAd != null && ggInterstitialAd.isAdLoaded()) {
            return true;
        } else
            return false;
         */
    }

    Intent intent;
    boolean isFinished;
    Activity act;

    public void showInterstitial(Activity act, Intent intent, boolean isFinished) {
        this.intent = intent;
        this.act = act;
        this.isFinished = isFinished;
        try {

            if (isProUser()) {
                if (intent != null)
                    act.startActivity(intent);
                if (isFinished) {
                    if (act != null && !act.isFinishing())
                        act.finish();
                }
                return;
            }

            if (getSettings().getAdDisable().equalsIgnoreCase("1")) {
                if (intent != null)
                    startActivity(intent);
                if (isFinished) {
                    if (act != null && !act.isFinishing())
                        act.finish();
                }
                return;
            }
            if (getSettings().getTotalAdCount() <= getApplication().currentAdDisplayCount) {
                if (intent != null)
                    startActivity(intent);
                if (isFinished) {
                    if (act != null && !act.isFinishing())
                        act.finish();
                }
                return;
            }
            getApplication().currentAdDisplayCount++;
            // Show the ad if it's ready. Otherwise toast and restart the game.

            Logger.e(TAG, "showInterstitial");
            //TODO lets ignore backend, always take IronSource
            if (IronSource.isInterstitialReady()) {
                IronSource.showInterstitial();
            }
            /*
            if (currentAdType == AdType.ADMOB && mInterstitialAd != null) {
                mInterstitialAd.show(act);
            } else if (currentAdType == AdType.IS && IronSource.isInterstitialReady()) {
                IronSource.showInterstitial();
            } else if (mPublisherInterstitialAd != null) {
                mPublisherInterstitialAd.show(act);
            } else if (ggInterstitialAd != null && ggInterstitialAd.isAdLoaded()) {
                isInterstitialShow = true;
                ggInterstitialAd.show(act);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isNativeAdEnable, isGGLoaded;

    public NativeAdsManager getNativeAdsManager() {
        return mNativeAdsManager;
    }

    private NativeAdsManager mNativeAdsManager;

    public void loadFBNativeAd() {

        try {

            if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getFb_native()) && WallpapersApplication.getApplication().getSettings().getFb_native().equalsIgnoreCase("0")) {
                return;
            }

            if (CommonFunctions.isStoreVersion(sContext)) {
                isNativeAdEnable = true;
            }

            if (isProUser()) {
                isNativeAdEnable = false;
            }

            try {
                NUMBER_OF_ADS = getSettings().getNativeAdCount();
            } catch (Exception e) {
                e.printStackTrace();
            }

//            if (currentAdType == AdType.GG) {
//                if (CommonFunctions.isGreedyGameShow())
//                    loadGGNativeAd();
//                return;
//            }

            String adUnitId = settings.getFromSettingPreference(SettingStore.FB_NATIVE_ID);
            if (CommonFunctions.isEmpty(adUnitId)) {
                if (!isAdmobLoaded && !isAdmobLoading)
                    refreshAd();
                return;
            }

            if (isNativeAdEnable) {
                mNativeAdsManager = new NativeAdsManager(sContext, adUnitId, NUMBER_OF_ADS);
                mNativeAdsManager.loadAds(NativeAd.MediaCacheFlag.ALL);
                mNativeAdsManager.setListener(WallpapersApplication.this);
            }

            Logger.e(TAG, "NUMBER_OF_ADS:" + NUMBER_OF_ADS);
//              refreshAd();
//            isNativeAdEnable = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String TAG = "JesusApplication";

    @Override
    public void onAdsLoaded() {
        Logger.e(TAG, "onAdsLoaded: FB");
        EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_FB_NATIVE, "onAdsLoaded");
        EventNotifier notifier =
                NotifierFactory.getInstance().getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_AD_STATUS);
        notifier.eventNotify(EventTypes.EVENT_AD_LOADED, null);
    }

    @Override
    public void onAdError(AdError error) {
        EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_FB_NATIVE, "onAdError:" + error.getErrorCode());
        Logger.e(TAG, "onAdError: FB " + error.getErrorMessage());

        if (!isAdmobLoaded && !isAdmobLoading)
            refreshAd();
    }


    private int MAX_RATING_COUNT = 5;
    public int ratingCounter;
    public boolean displayRated;

    public boolean isRatingShow() {
        Logger.e("isRatingShow", "" + ratingCounter);
        if (ratingCounter >= MAX_RATING_COUNT && !displayRated) {
            displayRated = true;
            ratingCounter = 0;
            return true;
        } else
            return false;
    }

    public long lastAdTime;

    public void incrementUserClickCounter() {
        this.userClickCount++;
        Logger.e("needToShowAd", "incrementUserClickCounter:" + userClickCount);
    }

    public int userClickCount;

    public boolean needToShowAd() {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastAdTime;
        long serverTime = Long.parseLong(getSettings().getAd_freq_time());
        int server_count = Integer.parseInt(getSettings().getAd_freq_count());

        Logger.e("needToShowAd", "serverTime:" + serverTime + " server_count:" + server_count);
        Logger.e("needToShowAd", "diff:" + diff);
        Logger.e("needToShowAd", "userClickCount:" + userClickCount + " lastAdTime:" + lastAdTime);

        if (diff >= serverTime) {
            lastAdTime = currentTime;
            userClickCount = 0;
            Logger.e("needToShowAd", "Time true");
            return true;
        } else if (userClickCount >= server_count) {
            userClickCount = 0;
            lastAdTime = currentTime;
            Logger.e("needToShowAd", "click true");
            return true;
        }
        Logger.e("needToShowAd", "false");
        return false;
    }

//    public void loadAdmobDownloadAd(){
//        MainNavigationActivity.isShowResumeAd = false;
//        if (!CommonFunctions.isStoreVersion(sContext))
//            return;
//
//        if (isProUser()) {
//            return;
//        }
//
//        String adUnitId = settings.getFromSettingPreference(SettingStore.ADMOB_INTERSTITIAL_DOWNLOAD_ID);
//        if (CommonFunctions.isEmpty(adUnitId)) {
//            return;
//        }
//        isFBDownloadLoaded = false;
//
//        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
//
//        AdManagerInterstitialAd.load(this, adUnitId, adRequest,
//                new AdManagerInterstitialAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
//                        // The mAdManagerInterstitialAd reference will be null until
//                        // an ad is loaded.
//                        ggInterstitialAdDownload = interstitialAd;
//                        Log.i(TAG, "onAdLoaded");
//                        isFBDownloadLoaded = true;
//                        MainNavigationActivity.isShowResumeAd = false;
//                        ggInterstitialAdDownload.setFullScreenContentCallback(new FullScreenContentCallback() {
//                            @Override
//                            public void onAdDismissedFullScreenContent() {
//                                // Called when fullscreen content is dismissed.
//                                Log.d("TAG", "The ad was dismissed.");
//                                ggInterstitialAdDownload = null;
//                                isInterstitialShow = false;
//                                isFBDownloadLoaded = false;
//
//                                try {
//                                    if (activity_download != null && !activity_download.isFinishing())
//                                        activity_download.finish();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//
//                            }
//
//                            @Override
//                            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
//                                super.onAdFailedToShowFullScreenContent(adError);
//                                Log.d("TAG", "The ad failed to show.");
//                                ggInterstitialAdDownload = null;
//                                isInterstitialShow = false;
//                                isFBDownloadLoaded = false;
//                            }
//
//                            @Override
//                            public void onAdShowedFullScreenContent() {
//                                // Called when fullscreen content is shown.
//                                // Make sure to set your reference to null so you don't
//                                // show it a second time.
//                                isInterstitialShow = true;
//                                Log.d("TAG", "The ad was shown.");
//                            }
//                        });
//
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        // Handle the error
//                        Log.i(TAG, loadAdError.getMessage());
//                        ggInterstitialAdDownload = null;
//
//                        Log.e("GGADS", "onAdLoadFailed");
//                        isFailedToLoad = true;
//                        isInterstitialShow = false;
//                    }
//                });
//    }

    private InterstitialAd ggInterstitialAdDownload;
    public void loadAdmobDownloadAd() {
        MainNavigationActivity.isShowResumeAd = false;
        if (!CommonFunctions.isStoreVersion(sContext))
            return;

        if (isProUser()) {
            return;
        }
        String adUnitId;
        if (CommonFunctions.isTestAdEnable()) {
            adUnitId = getResources().getString(R.string.ad_unit_id_test);
        }else
            adUnitId = settings.getFromSettingPreference(SettingStore.ADMOB_INTERSTITIAL_DOWNLOAD_ID);
        if (CommonFunctions.isEmpty(adUnitId)) {
            return;
        }
        isFBDownloadLoaded = false;

        AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
        AdRequest adRequest = adRequestBuilder.build();
        InterstitialAd.load(this, adUnitId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mAdManagerInterstitialAd reference will be null until
                        // an ad is loaded.
                        ggInterstitialAdDownload = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                        isFBDownloadLoaded = true;
                        MainNavigationActivity.isShowResumeAd = false;
                        ggInterstitialAdDownload.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");
                                ggInterstitialAdDownload = null;
                                isInterstitialShow = false;
                                isFBDownloadLoaded = false;

                                try {
                                    if (activity_download != null && !activity_download.isFinishing())
                                        activity_download.finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                Log.d("TAG", "The ad failed to show.");
                                ggInterstitialAdDownload = null;
                                isInterstitialShow = false;
                                isFBDownloadLoaded = false;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                isInterstitialShow = true;
                                Log.d("TAG", "The ad was shown.");
                            }
                        });

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        ggInterstitialAdDownload = null;

                        Log.e("GGADS", "onAdLoadFailed");
                        isFailedToLoad = true;
                        isInterstitialShow = false;
                    }
                });
    }

    public boolean fb_display_detail = false;
    public boolean isFBDownloadLoaded;

    public boolean isAdDisplayBefore;
    //    public boolean showAdDirectly;
    boolean isFailedToLoad;
    public Activity activity_download;

    public boolean isGGAdLoadedDownload(Activity act) {
        boolean needToCallAgain = false;
        activity_download = act;
        if (ggInterstitialAdDownload != null) {
            Log.e("GGADS", "isGGAdLoadedDownload");
            ggInterstitialAdDownload.show(act);
            isInterstitialShow = true;
            isAdDisplayBefore = false;
            needToCallAgain = true;

            userClickCount = 0;
            lastAdTime = System.currentTimeMillis();
        }
        if (isFailedToLoad) {
//            needToCallAgain=false;
            isFailedToLoad = false;
        }
        return needToCallAgain;
    }

    public void destroyAd() {
        try {

            if (universalAdListener != null) {
                universalAdListener.clear();
            }

            if (ggInterstitialAd != null)
                ggInterstitialAd.destroy();

            mNativeAdsManager = null;
            adLoader = null;
            ggInterstitialAd = null;
            ggInterstitialAdDownload = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AdLoader adLoader;

    public List<com.google.android.gms.ads.nativead.NativeAd> getGoogleNativeAd() {
        return mNativeAds;
    }

    public boolean isAdmobLoaded, isAdmobLoading;
    // The number of native ads to load and display.
    public static int NUMBER_OF_ADS = 4;
    // List of native ads that have been successfully loaded.
    private List<com.google.android.gms.ads.nativead.NativeAd> mNativeAds = new ArrayList<>();

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     */
    public void refreshAd() {
        if (CommonFunctions.isStoreVersion(sContext)) {
            isNativeAdEnable = true;
        }

        if (isProUser()) {
            isNativeAdEnable = false;
        }

        if (isNativeAdEnable) {
            isAdmobLoading = true;
            String adUnitId;

            if (CommonFunctions.isTestAdEnable())
                adUnitId = getString(R.string.ad_unit_id_native_test);
            else {
                adUnitId = settings.getFromSettingPreference(SettingStore.ADMOB_NATIVE_ID);
            }

            if (CommonFunctions.isEmpty(adUnitId)) {
                isNativeAdEnable = false;
                if (CommonFunctions.isAdManagerShow()) {
                    adManagerNativeAd();
                } else if (CommonFunctions.isGreedyGameShow()) {
                    loadGGNativeAd();
                }
                return;
            }

            VideoOptions videoOptions = new VideoOptions.Builder()
                    .setStartMuted(false)
                    .build();

            com.google.android.gms.ads.nativead.NativeAdOptions adOptions = new com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .build();

            adLoader = new AdLoader.Builder(this, adUnitId)
                    .forNativeAd(new com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener() {
                        @Override
                        public void onNativeAdLoaded(com.google.android.gms.ads.nativead.NativeAd ad) {
                            Logger.e("onUnifiedNativeAdLoaded", "mNativeAds.si:" + (mNativeAds != null ? mNativeAds.size() : 0));

                            try {
                                if (!WallpapersApplication.isAppRunning) {
                                    ad.destroy();
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            mNativeAds.add(ad);
                            if (adLoader != null && adLoader.isLoading()) {
                                Logger.e("onUnifiedNativeAdLoaded", "loading:");
                            } else {
                                Logger.e("onUnifiedNativeAdLoaded", "finish loading:");
                                EventNotifier notifier =
                                        NotifierFactory.getInstance().getNotifier(
                                                NotifierFactory.EVENT_NOTIFIER_AD_STATUS);
                                notifier.eventNotify(EventTypes.EVENT_AD_LOADED, null);
                                EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_FB_NATIVE, "Admob onAdLoaded:");
                                isAdmobLoaded = true;
                                isAdmobLoading = false;
                            }

                        }
                    }).withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError adError) {
                            // Handle the failure by logging, altering the UI, and so on.
                            EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_FB_NATIVE, "Admob onAdFailedToLoad:" + adError.getCode());
                            if (CommonFunctions.isAdManagerShow()) {
                                adManagerNativeAd();
                            } else if (CommonFunctions.isGreedyGameShow()) {
                                loadGGNativeAd();
                            }
                        }

                        @Override
                        public void onAdClicked() {
                            // Log the click event or other custom behavior.
                        }
                    }).withNativeAdOptions(adOptions).build();

            Bundle extras = new FacebookExtras()
                    .setNativeBanner(true)
                    .build();

            AdRequest request = new AdRequest.Builder()
                    .addNetworkExtrasBundle(FacebookAdapter.class, extras)
                    .build();

            adLoader.loadAds(request, NUMBER_OF_ADS);
        }
    }

    private ArrayList<String> admanager_id = new ArrayList<>();

    public void adManagerNativeAd() {
        if (CommonFunctions.isStoreVersion(sContext)) {
            isNativeAdEnable = true;
        }

        if (isNativeAdEnable) {
            isAdmobLoading = true;

            String adUnitId;
            if (CommonFunctions.isTestAdEnable()) {
                adUnitId = "/6499/example/native";
            } else
                adUnitId = settings.getFromSettingPreference(SettingStore.AM_NATIVE_ID);

            if (CommonFunctions.isEmpty(adUnitId)) {
                isNativeAdEnable = false;
                return;
            }
            admanager_id = new ArrayList<String>(Arrays.asList(adUnitId.split("#")));

            Logger.e(TAG, "adManagerNativeAd:" + adUnitId);

            if (admanager_id != null && admanager_id.size() > 0)
                setAdLoader(admanager_id.get(0));
        }
    }

    private void setAdLoader(String adUnitId) {
        Logger.e("adManagerNativeAd", "setAdLoader:" + adUnitId);
        adLoader = new AdLoader.Builder(this, adUnitId)
                .forNativeAd(new com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(com.google.android.gms.ads.nativead.NativeAd ad) {
                        try {

                            try {
                                if (!WallpapersApplication.isAppRunning) {
                                    ad.destroy();
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            mNativeAds.add(ad);
                            Logger.e("adManagerNativeAd", "mNativeAds.si:" + (mNativeAds != null ? mNativeAds.size() : 0));
                            if (adLoader != null && adLoader.isLoading()) {
                                Logger.e("adManagerNativeAd", "loading:");
                            } else {
                                Logger.e("adManagerNativeAd", "finish loading:");

                                admanager_id.remove(adUnitId);
                                if (admanager_id.size() <= 0) {
                                    EventNotifier notifier =
                                            NotifierFactory.getInstance().getNotifier(
                                                    NotifierFactory.EVENT_NOTIFIER_AD_STATUS);
                                    notifier.eventNotify(EventTypes.EVENT_AD_LOADED, null);
                                    EventManager.sendEvent(EventManager.LBL_AM_Advertise, EventManager.ATR_KEY_AM_NATIVE, "onAdLoaded:");
                                    isAdmobLoaded = true;
                                    isAdmobLoading = false;
                                } else {
                                    setAdLoader(admanager_id.get(0));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
                        EventManager.sendEvent(EventManager.LBL_AM_Advertise, EventManager.ATR_KEY_AM_NATIVE, "onAdFailedToLoad:" + adError.getCode());
                        if (mNativeAds != null && mNativeAds.size() > 0) {
                            EventNotifier notifier =
                                    NotifierFactory.getInstance().getNotifier(
                                            NotifierFactory.EVENT_NOTIFIER_AD_STATUS);
                            notifier.eventNotify(EventTypes.EVENT_AD_LOADED, null);
                            EventManager.sendEvent(EventManager.LBL_AM_Advertise, EventManager.ATR_KEY_AM_NATIVE, "onAdLoaded:");
                            isAdmobLoaded = true;
                            isAdmobLoading = false;
                        }
                    }

                    @Override
                    public void onAdClicked() {
                        // Log the click event or other custom behavior.
                    }
                }).build();

        adLoader.loadAd(new AdManagerAdRequest.Builder().build());
    }

    public void destroyGoogleAd() {
        try {
            displayAdCount = -1;
            act = null;
            if (mNativeAds != null) {
                for (int i = 0; i < mNativeAds.size(); i++) {
                    mNativeAds.get(i).destroy();
                }
                mNativeAds.clear();
            }
            mInterstitialAd = null;
            mPublisherInterstitialAd = null;
            intent = null;
            act = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroyGoogleNativeDetail() {
        try {
            displayAdCountDetail = -1;
            // comment advertise
//            if(mNativeAdsDetail!=null){
//                for (int i = 0; i < mNativeAdsDetail.size(); i++) {
//                    mNativeAdsDetail.get(i).destroy();
//                }
//                mNativeAdsDetail.clear();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroyGGDetail() {
        try {
            displayAdCountDetail = -1;
            if (GGNativeAds != null) {
                GGNativeAds.clear();
            }
            ggInterstitialAd = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int FOURK_WALLPAPER_RESIZE = -1;

    public static boolean needToResizeFourkWallpaper() {
        if (FOURK_WALLPAPER_RESIZE == -1) {
            try {
                if (getApplication() != null) {
                    SettingStore preferenceStore = SettingStore.getInstance(getApplication());
                    int fourk_resize_btmp = preferenceStore.getFromSettingPreferenceInt(SettingStore.EXCLUSIVE_RESIZE_BITMAP);
                    FOURK_WALLPAPER_RESIZE = fourk_resize_btmp;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        if (FOURK_WALLPAPER_RESIZE == -1 || FOURK_WALLPAPER_RESIZE == 1) {
            return true;
        }
        return false;
    }

    private int displayAdCount = -1;

    public int getCurrentAdDisplayCount() {
        Logger.e(TAG, "Size:" + WallpapersApplication.getApplication().getGoogleNativeAd().size() + " displayAdCount:" + displayAdCount);

        if (WallpapersApplication.getApplication().getGoogleNativeAd().size() - 1 <= displayAdCount) {
            displayAdCount = 0;
        } else {
            displayAdCount++;
        }
        Logger.e(TAG, "After:" + displayAdCount);
//        if(JesusApplication.getApplication().getGoogleNativeAd().size()<=displayAdCount){
//            displayAdCount=-1;
//        }else{
//            displayAdCount++;
//        }
        return displayAdCount;
    }

    public int getCurrentAdDisplayCountGG() {
        try {
            if (getGGNativeAds().size() - 1 <= displayAdCount) {
                displayAdCount = 0;
            } else {
                displayAdCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.e(TAG, "After:" + displayAdCount);
        return displayAdCount;
    }

    private int displayAdCountDetail = -1;

    public int getCurrentAdDisplayCountDetail() {
        // comment advertise
//        Logger.e(TAG,"Size:"+JesusApplication.getApplication().getGoogleNativeAdDetail().size()+" displayAdCount:"+displayAdCountDetail);
//
//        if(WallpapersApplication.getApplication().getGoogleNativeAdDetail().size()-1<=displayAdCountDetail){
//            displayAdCountDetail=0;
//        }else{
//            displayAdCountDetail++;
//        }
        Logger.e(TAG, "After:" + displayAdCountDetail);
        return displayAdCountDetail;
    }

    public boolean isProUser() {
        if (settings.getIsPro())
            return true;
        return false;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        try {
            Glide.get(this).onLowMemory();
            CommonFunctions.garbageCollector(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (appOpenManager != null) {

            if (level == TRIM_MEMORY_UI_HIDDEN) {

                eventHelper.sendCloseEvent();
                appOpenManager.isForeground = false;
            } else {

                appOpenManager.isForeground = true;
            }
        }

        try {
            Glide.get(this).onTrimMemory(level);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
            try {
                Glide.get(this).clearMemory();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    //    public List<UnifiedNativeAd> getGoogleNativeAdDetail() {
//        return mNativeAdsDetail;
//    }
    public static int NUMBER_OF_ADS_DETAILS = 3;

    // List of native ads that have been successfully loaded.
//    private List<UnifiedNativeAd> mNativeAdsDetail = new ArrayList<>();
    public void loadNativeForDetail() {

//        String adUnitId1 = settings.getFromSettingPreference(SettingStore.ADMOB_DETAIL_NATIVE_ID);
//        Logger.e("loadNativeForDetail","id:"+adUnitId1);
//        Logger.e("loadNativeForDetail","te:"+JesusApplication.getApplication().getSettings().getIs_mediation_native_detail());

        boolean isNativeAdEnable = false;
        if (CommonFunctions.isStoreVersion(sContext)) {
            isNativeAdEnable = true;
        }

        if (isProUser()) {
            isNativeAdEnable = false;
        }

        try {
            NUMBER_OF_ADS_DETAILS = getSettings().getAdmobNativeAdCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
// comment advertise
//        if(isNativeAdEnable){
//
//            AdLoader.Builder builder;
//
//            if (Common.isTestAdEnable())
//                builder = new AdLoader.Builder(this, getString(R.string.ad_unit_id_native_test));
//            else {
//                String adUnitId = settings.getFromSettingPreference(SettingStore.ADMOB_DETAIL_NATIVE_ID);
//                if (Common.isEmpty(adUnitId)){
//                    isNativeAdEnable=false;
//                    return;
//                }
//                builder = new AdLoader.Builder(this, adUnitId);
//            }
//            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
//                // OnUnifiedNativeAdLoadedListener implementation.
//                @Override
//                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
//                    // otherwise you will have a memory leak.
//
//                    Logger.e("onUnifiedNativeAdLoaded detail","NUMBER_OF_ADS.si:"+NUMBER_OF_ADS_DETAILS);
//                    EventManager.sendEvent(EventManager.LBL_Advertise,EventManager.ATR_KEY_ADMOB_NATIVE,"onAdsLoaded");
//                    mNativeAdsDetail.add(unifiedNativeAd);
//                    Logger.e("onUnifiedNativeAdLoaded detail","mNativeAds.si:"+mNativeAdsDetail.size());
//                    EventNotifier notifier =
//                            NotifierFactory.getInstance( ).getNotifier(
//                                    NotifierFactory.EVENT_NOTIFIER_AD_STATUS);
//                    notifier.eventNotify( EventTypes.EVENT_AD_NATIVE_DETAIL_LOADED, null);
//
//                }
//            });
//
//            VideoOptions videoOptions = new VideoOptions.Builder()
//                    .setStartMuted(true)
//                    .build();
//
//            NativeAdOptions adOptions = new NativeAdOptions.Builder()
//                    .setVideoOptions(videoOptions)
//                    .build();
//
//            builder.withNativeAdOptions(adOptions);
//
//            AdLoader adLoader = builder.withAdListener(new AdListener() {
//                @Override
//                public void onAdFailedToLoad(int errorCode) {
//                    Logger.e("onAdFailedToLoad detail","errorCode: "+errorCode);
//                    EventManager.sendEvent(EventManager.LBL_Advertise,EventManager.ATR_KEY_ADMOB_NATIVE,"onAdFailedToLoad");
//                }
//
//            }).build();
//
//            AdRequest.Builder builerRe=new AdRequest.Builder();
//
//            try {
//
//                if(!TextUtils.isEmpty(JesusApplication.getApplication().getSettings().getIs_mediation_native_detail()) && JesusApplication.getApplication().getSettings().getIs_mediation_native_detail().equalsIgnoreCase("1")) {
//                    Bundle extras = new FacebookExtras()
//                            .build();
//                    builerRe.addNetworkExtrasBundle(FacebookAdapter.class, extras);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            if (BuildConfig.DEBUG)
//                builerRe.addTestDevice("1DE6BD86227455E0040B8C0232C16B0F").addTestDevice("B3DBC5ED0DFDF8207E53E500939F90A8").addTestDevice("DE1A5649893A0F813AB422751E9E28E1");
//
//            adLoader.loadAds(builerRe.build(),NUMBER_OF_ADS_DETAILS);
//
//        }
    }

    GGInterstitialAd ggInterstitialAd;

    private void loadGreedyGameInterst(Activity activity) {

        SettingStore preferenceStore = SettingStore.getInstance(sContext);
        String adUnitId = "";
        if (isFromSplash) {
            adUnitId = preferenceStore.getFromSettingPreference(SettingStore.GG_INTERSTITIAL_ID_SPLASH);
        } else
            adUnitId = preferenceStore.getFromSettingPreference(SettingStore.GG_INTERSTITIAL_ID);

        Logger.e(TAG, "loadGreedyGameInterst:" + adUnitId);

        if (CommonFunctions.isEmpty(adUnitId)) {
            return;
        }

        ggInterstitialAd = new GGInterstitialAd(sContext, adUnitId);
        GGInterstitialEventsListener ggInterstitialEventsListener = new GGInterstitialEventsListener() {
            @Override
            public void onAdLoaded() {
                Log.d("GGADS", "Ad Loaded");
                EventManager.sendEvent(EventManager.LBL_GG_Advertise, EventManager.ATR_KEY_GG, "onAdLoaded");
                if (universalAdListener != null && universalAdListener.size() > 0)
                    universalAdListener.get(getCounter()).onAdLoaded();
            }

            @Override
            public void onAdLoadFailed(@NotNull AdErrors adErrors) {
                Log.d("GGADS", "Ad Load Failed ");
                EventManager.sendEvent(EventManager.LBL_GG_Advertise, EventManager.ATR_KEY_GG, "onAdLoadFailed");
                if (universalAdListener != null && universalAdListener.size() > 0)
                    universalAdListener.get(getCounter()).onAdFailedToLoad(0);
            }

            @Override
            public void onAdShowFailed() {
                isInterstitialShow = false;
                EventManager.sendEvent(EventManager.LBL_GG_Advertise, EventManager.ATR_KEY_GG, "onAdShowFailed");
            }

            @Override
            public void onAdClosed() {
                Log.d("GGADS", "Ad Closed");
                isInterstitialShow = false;
                closedAd();
                if (universalAdListener != null && universalAdListener.size() > 0)
                    universalAdListener.get(getCounter()).onAdClosed();
            }

            @Override
            public void onAdOpened() {
                Log.d("GGADS", "Ad Opened");
            }

        };

        ggInterstitialAd.setListener(ggInterstitialEventsListener);
        ggInterstitialAd.loadAd();

    }

    View dialogView;
    TextView feedbackTxt;
    Button cancel;
    LinearLayout awe_LL, bad_LL, okay_LL, good_LL, great_LL;
    ImageView awe, bad, okay, good, great;
    int selected = 0;
    AlertDialog alertDialog;

    private void showFeedbackDialog(Activity act) {
        if (act.isFinishing())
            return;

        SettingStore settingStore = SettingStore.getInstance(act);
        boolean isDarkTheme = false;
        if (settingStore.getTheme() == 0) {
            isDarkTheme = false;
        } else if (settingStore.getTheme() == 1) {
            isDarkTheme = true;
        }

        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(act, R.style.roundCorner);

        // set the custom layout
        final View customLayout = act.getLayoutInflater().inflate(R.layout.feedback_layout, null,false);
        builder.setView(customLayout);

        RelativeLayout main = customLayout.findViewById(R.id.mainRL);
        TextView heading = customLayout.findViewById(R.id.heading);
        EditText feedback = customLayout.findViewById(R.id.feedback);
        TextView cancel = customLayout.findViewById(R.id.cancel);
        TextView submit = customLayout.findViewById(R.id.submit);

        if (isDarkTheme) {
            heading.setTextColor(getResources().getColor(R.color.white));
            feedback.setTextColor(getResources().getColor(R.color.white));
            feedback.setHintTextColor(getResources().getColor(R.color.feedbackhint));
            cancel.setTextColor(getResources().getColor(R.color.white));
            submit.setTextColor(getResources().getColor(R.color.white));
            main.setBackgroundColor(getResources().getColor(R.color.colorPrimary1));
        } else {
            heading.setTextColor(getResources().getColor(R.color.black));
            feedback.setTextColor(getResources().getColor(R.color.black));
            cancel.setTextColor(getResources().getColor(R.color.black));
            submit.setTextColor(getResources().getColor(R.color.black));
            main.setBackgroundColor(getResources().getColor(R.color.white));
        }

        AlertDialog dlg = builder.create();
        dlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if (!act.isFinishing()) {
            dlg.show();
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(feedback.getText().toString().trim()) || feedback.getText().length()==0)
                {
                    Toast.makeText(act, "Enter details.", Toast.LENGTH_SHORT).show();
                    return;
                }

                CommonFunctions.hideKeyboard(act);
                SettingStore settingStore = SettingStore.getInstance(act);
                RequestManager manager = new RequestManager(act);
                manager.setFeedbackService(settingStore.getUserID(), feedback.getText().toString(), new NetworkCommunicationManager.CommunicationListnerNew() {
                    @Override
                    public void onSuccess(IModel response, int operationCode) {
                        dlg.dismiss();
                        Toast.makeText(act,"Feedback Submitted.",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onStartLoading() { }

                    @Override
                    public void onFail(WebServiceError errorMsg) { }
                });
            }
        });
    }

    public void showRatingDialog(final Activity act, boolean showCheckBox) {
        try {
            SettingStore settingStore = SettingStore.getInstance(act);
            if (!settingStore.getRatingNotShow()) {
                return;
            }

            boolean isDarkTheme = false;
            if (settingStore.getTheme() == 0) {
                isDarkTheme = false;
            } else if (settingStore.getTheme() == 1) {
                isDarkTheme = true;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(act, R.style.roundCorner);

            dialogView = LayoutInflater.from(act).inflate(R.layout.dialog_layout, null, false);

            CheckBox notShowCheckBox=dialogView.findViewById(R.id.checkbox);
            RelativeLayout mainLL = dialogView.findViewById(R.id.mailRL);
            awe_LL = dialogView.findViewById(R.id.btn_awful);
            bad_LL = dialogView.findViewById(R.id.btn_bad);
            okay_LL = dialogView.findViewById(R.id.btn_okay);
            good_LL = dialogView.findViewById(R.id.btn_good);
            great_LL = dialogView.findViewById(R.id.btn_great);

            TextView header = dialogView.findViewById(R.id.header);
            TextView notShow = dialogView.findViewById(R.id.txtNotShow);
            feedbackTxt = dialogView.findViewById(R.id.feedbackTxt);
            awe = dialogView.findViewById(R.id.iv_awful);
            bad = dialogView.findViewById(R.id.iv_bad);
            okay = dialogView.findViewById(R.id.iv_okay);
            good = dialogView.findViewById(R.id.iv_good);
            great = dialogView.findViewById(R.id.iv_great);


            notShowCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    settingStore.setRitingnotshow(!b);
                    Logger.e("checkbox",b+" :selected");
                }
            });
            if (isDarkTheme) {
                mainLL.setBackgroundColor(getResources().getColor(R.color.colorPrimary1));
                awe_LL.setBackgroundColor(getResources().getColor(R.color.colorPrimary1));
                bad_LL.setBackgroundColor(getResources().getColor(R.color.colorPrimary1));
                okay_LL.setBackgroundColor(getResources().getColor(R.color.colorPrimary1));
                good_LL.setBackgroundColor(getResources().getColor(R.color.colorPrimary1));
                great_LL.setBackgroundColor(getResources().getColor(R.color.colorPrimary1));
                feedbackTxt.setTextColor(getResources().getColor(R.color.white));
                notShow.setTextColor(getResources().getColor(R.color.white));
                header.setTextColor(getResources().getColor(R.color.white));
            } else {
                mainLL.setBackgroundColor(getResources().getColor(R.color.white));
                awe_LL.setBackgroundColor(getResources().getColor(R.color.white));
                bad_LL.setBackgroundColor(getResources().getColor(R.color.white));
                okay_LL.setBackgroundColor(getResources().getColor(R.color.white));
                good_LL.setBackgroundColor(getResources().getColor(R.color.white));
                great_LL.setBackgroundColor(getResources().getColor(R.color.white));
                feedbackTxt.setTextColor(getResources().getColor(R.color.black));
                header.setTextColor(getResources().getColor(R.color.black));
                notShow.setTextColor(getResources().getColor(R.color.black));
            }

            awe_LL.setOnClickListener(this);
            bad_LL.setOnClickListener(this);
            okay_LL.setOnClickListener(this);
            good_LL.setOnClickListener(this);
            great_LL.setOnClickListener(this);

            builder.setView(dialogView);
            alertDialog = builder.create();

            alertDialog.setCancelable(false);
            alertDialog.show();

            cancel = dialogView.findViewById(R.id.cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            dialogView.findViewById(R.id.okay).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selected != 0) {
                        startAction(act);
                    } else {
                        Toast.makeText(act, "Please select properly.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        final RatingDialog ratingDialog = new RatingDialog.Builder(act)
//                .session(1).showCheckBox(showCheckBox)
//                .threshold(3).icon(sContext.getResources().getDrawable(R.mipmap.ic_launcher_about))
//                .ratingBarColor(isDarkTheme?R.color.white:R.color.main_text_color).ratingBarBackgroundColor(isDarkTheme?R.color.placeholder_color:R.color.grey_200).positiveButtonTextColor(isDarkTheme?R.color.white:R.color.black).titleTextColor(isDarkTheme?R.color.white:R.color.black).isDark(isDarkTheme)
//                .playstoreUrl("https://play.google.com/store/apps/details?id=" + act.getPackageName())
//                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
//                    @Override
//                    public void onFormSubmitted(String feedback) {
//                        Log.i("MainBotttomAct", "Feedback:" + feedback);
//
//                        CommonFunctions.hideKeyboard(act);
//                        SettingStore settingStore = SettingStore.getInstance(act);
//                        RequestManager manager = new RequestManager(act);
//                        manager.setFeedbackService(settingStore.getUserID(), feedback, new NetworkCommunicationManager.CommunicationListnerNew() {
//                            @Override
//                            public void onSuccess(IModel response, int operationCode) {
//                                Toast.makeText(act,"Feedback Submitted.",Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onStartLoading() {
//
//                            }
//
//                            @Override
//                            public void onFail(WebServiceError errorMsg) {
//
//                            }
//                        });
//                    }
//                }).onRatingChanged(new RatingDialog.Builder.RatingDialogListener() {
//                    @Override
//                    public void onRatingSelected(float rating, boolean thresholdCleared) {
//                        Logger.e("onRatingSelected",""+rating);
//                        EventManager.sendEvent(EventManager.LBL_HOME,EventManager.ATR_KEY_RATE,""+rating);
//                    }
//                })
//                .build();
//
//        if(act!=null && !act.isFinishing()) {
//            ratingDialog.show();
//        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_awful:
                selected = 1;
                break;
            case R.id.btn_bad:
                selected = 2;
                break;
            case R.id.btn_okay:
                selected = 3;
                break;
            case R.id.btn_good:
                selected = 4;
                break;
            case R.id.btn_great:
                selected = 5;
                break;
        }
        startAnimation(v);
        setText();
    }

    private void setText() {
        feedbackTxt.setVisibility(View.VISIBLE);
        if (selected < 4) {
            feedbackTxt.setText("Thanks for your feedback! Share your ideas on how we can improve?");
        } else {
            feedbackTxt.setText("Thanks for your Support! Rate our app in Play Store as well?");
        }
    }

    private void startAnimation(View view) {

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY);
        animator.setRepeatCount(1);
        animator.setDuration(200);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                changeColor();
            }
        });
        animator.start();
    }

    private void changeColor() {
        if (selected == 1) {
            awe.setImageDrawable(getResources().getDrawable(R.drawable.ic_awful));
            bad.setImageDrawable(getResources().getDrawable(R.drawable.ic_bad_inactive));
            okay.setImageDrawable(getResources().getDrawable(R.drawable.ic_okay_inactive));
            good.setImageDrawable(getResources().getDrawable(R.drawable.ic_good_inactive));
            great.setImageDrawable(getResources().getDrawable(R.drawable.ic_great_inactive));
        } else if (selected == 2) {
            awe.setImageDrawable(getResources().getDrawable(R.drawable.ic_bad));
            bad.setImageDrawable(getResources().getDrawable(R.drawable.ic_bad));
            okay.setImageDrawable(getResources().getDrawable(R.drawable.ic_okay_inactive));
            good.setImageDrawable(getResources().getDrawable(R.drawable.ic_good_inactive));
            great.setImageDrawable(getResources().getDrawable(R.drawable.ic_great_inactive));
        } else if (selected == 3) {
            awe.setImageDrawable(getResources().getDrawable(R.drawable.ic_okay));
            bad.setImageDrawable(getResources().getDrawable(R.drawable.ic_okay));
            okay.setImageDrawable(getResources().getDrawable(R.drawable.ic_okay));
            good.setImageDrawable(getResources().getDrawable(R.drawable.ic_good_inactive));
            great.setImageDrawable(getResources().getDrawable(R.drawable.ic_great_inactive));
        } else if (selected == 4) {
            awe.setImageDrawable(getResources().getDrawable(R.drawable.ic_good));
            bad.setImageDrawable(getResources().getDrawable(R.drawable.ic_good));
            okay.setImageDrawable(getResources().getDrawable(R.drawable.ic_good));
            good.setImageDrawable(getResources().getDrawable(R.drawable.ic_good));
            great.setImageDrawable(getResources().getDrawable(R.drawable.ic_great_inactive));
        } else if (selected == 5) {
            awe.setImageDrawable(getResources().getDrawable(R.drawable.ic_great));
            bad.setImageDrawable(getResources().getDrawable(R.drawable.ic_great));
            okay.setImageDrawable(getResources().getDrawable(R.drawable.ic_great));
            good.setImageDrawable(getResources().getDrawable(R.drawable.ic_great));
            great.setImageDrawable(getResources().getDrawable(R.drawable.ic_great));
        }

    }

    private void startAction(Activity act) {
        alertDialog.dismiss();
        EventManager.sendEvent(EventManager.LBL_HOME,EventManager.ATR_KEY_RATE,""+selected);
        if (selected < 4) {
            showFeedbackDialog(act);
        } else {
            try {
                act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } catch (android.content.ActivityNotFoundException e) {
                Log.e("rating error", e.getMessage());
                act.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        }
        selected = 0;
    }

    public void sendAllDataToServer(boolean isFromCreate) {
        Logger.e("sendAllDataToServer", "" + settings.getViewCount());
        Logger.e("sendAllDataToServer", "" + settings.getUnLikeCount());
        if (TextUtils.isEmpty(settings.getLikeCount()) && TextUtils.isEmpty(settings.getViewCount()) && TextUtils.isEmpty(settings.getDownloadCount()) && TextUtils.isEmpty(settings.getLikeLiveCount()) && TextUtils.isEmpty(settings.getLikeExclusiveCount()) && TextUtils.isEmpty(settings.getSearchKeyword())) {
            if (isFromCreate) {
                EventNotifier notifier =
                        NotifierFactory.getInstance().getNotifier(
                                NotifierFactory.EVENT_NOTIFIER_USER_INFO);
                notifier.eventNotify(EventTypes.EVENT_CALL_FAV, null);
            }
            return;
        }
        Logger.e("sendAllDataToServer", "Called");
        RequestManager manager = new RequestManager(sContext);
        manager.sendDataToServer(settings.getLikeCount(), settings.getViewCount(), settings.getDownloadCount(), settings.getLikeLiveCount(), settings.getLikeExclusiveCount(), settings.getSearchKeyword(), CommonFunctions.getHardwareId(sContext), new NetworkCommunicationManager.CommunicationListnerNew() {
            @Override
            public void onSuccess(IModel response, int operationCode) {
                IModelBase base_model = (IModelBase) response;
                if (base_model != null && base_model.getStatus().equalsIgnoreCase("1")) {
                    settings.setViewCount("");
                    settings.setLikeCount("");
                    settings.setDownloadCount("");
                    settings.setLikeLiveCount("");
                    settings.setLikeExclusiveCount("");
                    settings.setSearchKeyword("");

                    settings.setUnLikeCount("");
                    settings.setUnLikeCountLive("");
                    settings.setUnLikeCountExclusive("");
                    if (isFromCreate) {
                        EventNotifier notifier =
                                NotifierFactory.getInstance().getNotifier(
                                        NotifierFactory.EVENT_NOTIFIER_USER_INFO);
                        notifier.eventNotify(EventTypes.EVENT_CALL_FAV, null);
                    }
                }
            }

            @Override
            public void onStartLoading() {
            }

            @Override
            public void onFail(WebServiceError errorMsg) {
                if (isFromCreate) {
                    EventNotifier notifier =
                            NotifierFactory.getInstance().getNotifier(
                                    NotifierFactory.EVENT_NOTIFIER_USER_INFO);
                    notifier.eventNotify(EventTypes.EVENT_CALL_FAV, null);
                }
            }
        });
    }

    private InstallReferrerClient referrerClient;

    public void getReferrerClient() {
        try {

            boolean isReferrerEventSend = settings.getIsReferSend();
            Logger.e("isReferrerEventSend", "" + isReferrerEventSend);
            if (isReferrerEventSend) {
                EventNotifier notifier =
                        NotifierFactory.getInstance().getNotifier(
                                NotifierFactory.EVENT_NOTIFIER_USER_INFO);
                notifier.eventNotify(EventTypes.EVENT_REFERER_GOT, null);
                return;
            }
            referrerClient = InstallReferrerClient.newBuilder(this).build();
            referrerClient.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    try {
                        ReferrerDetails response = referrerClient.getInstallReferrer();
                        String referrer = response.getInstallReferrer();

                        try { // Remove any url encoding
                            referrer = URLDecoder.decode(referrer, "UTF-8");
                        } catch (Exception e) {
                            return;
                        }

                        String referrerValue = "Other";
                        if (referrer != null && !referrer.equals("")) {
                            String storedReferrerId = settings.getReferId();
                            if (/*!isProcessingStarted &&*/ TextUtils.isEmpty(storedReferrerId)) {
                                settings.setReferId(referrer);
                            }

                            if (referrer.contains("FMR_")) {
                                referrerValue = "FMR Referrer";
                            } else if (referrer.contains("F_")) {
                                referrerValue = "Free Referrer";
                            } else if (referrer.contains("organic")) {
                                referrerValue = "Organic";
                            } else if (referrer.contains("not set")) {
                                referrerValue = "Not Set";
                            } else if (referrer.contains("adsplayload")) {
                                referrerValue = "Ads playload";
                            } else if (referrer.contains("4K")) {
                                referrerValue = "Old 4K";
                            } else if (referrer.length() > 22) {
                                referrerValue = referrer.substring(0, 21);
                            }
                        }

                        String oldDeviceId = "";
                        if (!TextUtils.isEmpty(referrer) && referrer.contains("4K")) {
                            oldDeviceId = referrer.replace("4K_", "");
                        }
                        settings.setOldDeviceID(oldDeviceId);

                        EventManager.sendEvent(EventManager.LBL_REFERRER_PAGE,
                                EventManager.ATR_KEY_REFERRER_DETAILS, referrerValue);
                        settings.setIsReferSend(true);
                        Logger.e("getReferrerClient", "" + referrerValue + " userId:" + oldDeviceId);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    switch (responseCode) {
                        case InstallReferrerClient.InstallReferrerResponse.OK:
                            // Connection established
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                            // API not available on the current Play Store app
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                            // Connection could not be established
                            break;
                    }
                    try {
                        if (referrerClient != null)
                            referrerClient.endConnection();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    EventNotifier notifier =
                            NotifierFactory.getInstance().getNotifier(
                                    NotifierFactory.EVENT_NOTIFIER_USER_INFO);
                    notifier.eventNotify(EventTypes.EVENT_REFERER_GOT, null);
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            EventNotifier notifier =
                    NotifierFactory.getInstance().getNotifier(
                            NotifierFactory.EVENT_NOTIFIER_USER_INFO);
            notifier.eventNotify(EventTypes.EVENT_REFERER_GOT, null);
            Logger.printStackTrace(e);
        }
    }

    private void setAdjust() {

        String appToken = "s0b4ufvyssn4";
        String environment;
        if (BuildConfig.DEBUG) {

            environment = AdjustConfig.ENVIRONMENT_SANDBOX;
        } else {

            environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
        }
        AdjustConfig config = new AdjustConfig(this, appToken, environment);
        Adjust.onCreate(config);
    }
}
