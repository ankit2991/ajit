package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import static callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils.ACTION_FROM_ON_BOARDING;
import static callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils.EXTRA_FROM_ON_BOARDING;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import com.adjust.sdk.Adjust;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.concurrent.atomic.AtomicBoolean;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdAppOpen;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdAppOpenCallback;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdConstants;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.onboarding.OnBoardingActivity;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.util.RemoteConfig;


public class SplashActivitynew extends BaseActivity {
    private LottieAnimationView animation_view;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    private static final String TAG_CMP = "AdMob CMP";
    private ConsentInformation consentInformation;
    // Use an atomic boolean to initialize the Google Mobile Ads SDK and load ads once.
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);

    protected void onResume() {
        super.onResume();
        Adjust.onResume();
    }

    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        checkUserConsent();
    }

    private void checkUserConsent() {
        // Set tag for under age of consent. false means users are not under age
        // of consent.
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            this,
                            (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {
                                if (loadAndShowError != null) {
                                    // Consent gathering failed.
                                    Log.w(TAG_CMP, String.format("%s: %s",
                                            loadAndShowError.getErrorCode(),
                                            loadAndShowError.getMessage()));
                                }
                                // Consent has been gathered.
                                if (consentInformation.canRequestAds()) {
                                    initializeMobileAdsSdk();
                                    initApp();
                                } else {
                                    initApp();
                                }
                            }
                    );
                },
                (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                    // Consent gathering failed.
                    Log.w(TAG_CMP, String.format("%s: %s",
                            requestConsentError.getErrorCode(),
                            requestConsentError.getMessage()));
                    initApp();
                });
    }

    private void initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return;
        }

        // Initialize the Google Mobile Ads SDK.
        MobileAds.initialize(this);
    }

    private void initApp() {
        // Applovin Ads mediation
        MaxAdManager.INSTANCE.initAppLovinSdkMax(this);
        //Init Amazon SDK
        MaxAdManager.INSTANCE.initAmazonSdk(this);

        RemoteConfig.INSTANCE.getRemoteconfig();

        Utils.NATIVE_SESSION = TinyDB.getInstance(this).getInt("IsFirstSession") + 1;
//        Log.e("remotevalues",">>"+Constants.INSTANCE.getINTERSTITIAL_TRESH()+">>>"+Constants.INSTANCE.getOPEN_APP_FLAG());

        activeRemoteConfig();

//        new Handler().postDelayed(this::moveToNextScreen, 5000);


        if (TinyDB.getInstance(this).isFirstLaunchApp()) {
            Intent intent = new Intent(this, OnBoardingActivity.class);
            startActivity(intent);
            finish();
        } else {
            createTimer(5L);
        }
    }

    private boolean shouldShowOnboarding() {
//        List<String> locales = new ArrayList<>();
//        locales.add("th_TH");
//        locales.add("ms_MY");
//        locales.add("in_ID");
//        locales.add("th_TH_TH");
//        locales.add("in");
//        locales.add("ms");
//        locales.add("th");
//        locales.add("vni_VN");
//        String currentLocale = Locale.getDefault().toString();
        boolean showOnboarding = false;
//        if (locales.contains(currentLocale)) {
        if (!TinyDB.getInstance(this).isFirstLaunchApp()) {
            showOnboarding = true;
//            }
        }

        return showOnboarding;
    }

    public FirebaseRemoteConfig activeRemoteConfig() {

        FirebaseRemoteConfig mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings =
                new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1).build();
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFireBaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                Utils.KEY_SUBSCRIPTION_REMOTE_PLAN = Utils.getRemoteConfig().getString(Utils.REMOTE_KEY_PRICE_PLAN);
            }
        });

        return mFireBaseRemoteConfig;

    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private MaxAdAppOpen appOpenAdSplash = null;

    private final Runnable callback = () -> {
        if (appOpenAdSplash != null) {
            appOpenAdSplash.destroy();
            appOpenAdSplash = null;
        }
        moveToNextScreen();
    };

    private void startAdLoaderTimer(long seconds) {
        handler.postDelayed(callback, seconds * 1000);
    }

    private void stopAdLoaderTimer() {
        handler.removeCallbacksAndMessages(null);
    }

    private void createTimer(long seconds) {

        CountDownTimer countDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {

                // Show the app open ad.
                if (!TinyDB.getInstance(SplashActivitynew.this).weeklyPurchased()) {

                    appOpenAdSplash = new MaxAdAppOpen(
                            SplashActivitynew.this,
                            MaxAdConstants.MAX_AD_APP_OPEN_ID_START,
                            new MaxAdAppOpenCallback() {

                                @Override
                                public void onAdDisplayFailed() {
                                    moveToNextScreen();
                                }

                                @Override
                                public void onAdLoadFailed() {
                                    stopAdLoaderTimer();
                                    moveToNextScreen();
                                }

                                @Override
                                public void onAdClicked() {

                                }

                                @Override
                                public void onAdHidden() {
                                    moveToNextScreen();
                                }

                                @Override
                                public void onAdDisplayed() {

                                }

                                @Override
                                public void onAdLoaded() {
                                    stopAdLoaderTimer();

                                    if (appOpenAdSplash != null && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                                        appOpenAdSplash.showAdIfReady(MaxAdConstants.MAX_AD_APP_OPEN_ID_START);
                                    } else {
                                        moveToNextScreen();
                                    }
                                }
                            }
                    );
                    startAdLoaderTimer(MaxAdConstants.MAX_AD_LOAD_TIMEOUT);
                } else {
                    moveToNextScreen();
                }
            }


        };
        countDownTimer.start();

    }

    private void moveToNextScreen() {
//        if (AppOpenManager.getInstance().isAdAvailable()) {
//            AppOpenManager.getInstance().showAdIfAvailable(SplashActivitynew.this);
//        } else {
        Log.d("splash_Remote_config", "onboarding_flag:" + Utils.getRemoteConfig().getBoolean(Utils.REMOTE_KEY_ON_BOARDING_FLAG) +
                " --- payment_card_flag:" + Utils.getRemoteConfig().getBoolean(Utils.REMOTE_KEY_PAYMENT_CARD_FLAG));
        Log.e("Condition Check:", "is first launch:" + TinyDB.getInstance(this).isFirstLaunchApp());
        if (TinyDB.getInstance(this).isFirstLaunchApp() && Utils.getRemoteConfig().getBoolean(Utils.REMOTE_KEY_ON_BOARDING_FLAG)) {
            Intent intent = new Intent(this, OnBoardingActivity.class);
            startActivity(intent);
        } else if (TinyDB.getInstance(this).isFirstLaunchApp() && Utils.getRemoteConfig().getBoolean(Utils.REMOTE_KEY_PAYMENT_CARD_FLAG)) {
            Intent intent = new Intent(this, InAppsActivity.class)
                    .setAction(ACTION_FROM_ON_BOARDING)
                    .putExtra(EXTRA_FROM_ON_BOARDING, true);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, StartPage.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
        finish();
//        }
    }
}
