package com.lockerroom.face.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.amazon.device.ads.AdRegistration;
import com.amazon.device.ads.DTBAdNetwork;
import com.amazon.device.ads.DTBAdNetworkInfo;
import com.amazon.device.ads.MRAIDPolicy;
import com.android.billingclient.api.ProductDetails;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;

import com.lockerroom.face.BuildConfig;
import com.lockerroom.face.PhotoApp;
import com.lockerroom.face.R;
import com.lockerroom.face.activities.onboardingScreen.MainOnBoardingScreen;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.ads.consentManager.AdsConsentManager;
import com.lockerroom.face.firebaseAdsConfig.RemoteConfigData;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.AppOpenCallback;
import com.lockerroom.face.maxAdManager.MaxAdConstants;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.maxAdManager.MaxAppOpenAdManager;
import com.lockerroom.face.utils.Global;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.QuimeraInit;
import com.lockerroom.face.utils.RemoteConfig;
import com.lockerroom.face.utils.SharePreferenceUtil;

import java.util.ArrayList;
import java.util.List;

import games.moisoni.google_iab.BillingConnector;
import games.moisoni.google_iab.BillingEventListener;
import games.moisoni.google_iab.enums.ProductType;
import games.moisoni.google_iab.models.BillingResponse;
import games.moisoni.google_iab.models.ProductInfo;
import games.moisoni.google_iab.models.PurchaseInfo;
import timber.log.Timber;


public class EditorSplashActivity extends AppCompatActivity {
    ImageView img, abc, imageBg;
    RelativeLayout title_animation;
    CardView agree_btn;
    TextView privacy_text;
    private FirebaseAnalytics mFirebaseAnalytics;
    private BillingConnector billingConnector;
    private String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn1/9UuE3xHKiJsnsC1QpB76P+pd+ppGOR3pp0eDW8vqjhKNfqMzYrq9LKQ415Mc5K9Ks1AjSb0Qcajh8FMtuv8Q+4pVPWuRam57xYSY0u5tzrchEkEeg4nO3xy4A5lfGKCq0TRVJOUg9G0ANJ9rhPMfQwHwA02RlWfN3oTX6vYKdkRamD9fcIA82p5jcIc0t9FntPiHyLwuZQ+kT/SeTgxwJp+W0hCUetnfObiMRulMgaE9vUMohLVhHRxNdtNizerU+cCIKBWq7afRdGjfABQOIzOMZKcRdz3yiIy5BWoReStTksrax4R6CL/iaRqaviZINZXZRbwDIxYDCJb/x/wIDAQAB";

    private ArrayList<String> SKUs = new ArrayList<>();
    AdsConsentManager adsConsentManager;

    Boolean weeklyPurchase = false;
    Boolean yearlyPurchase = false;
    ConstraintLayout cl_privacy_policy;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.red(PhotoApp.resources.getColor(R.color.__picker_common_primary)));
        setContentView(R.layout.activity_splash);
//        initConfig();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        agree_btn = findViewById(R.id.agree_btn);
        privacy_text = findViewById(R.id.privacy_text);
        title_animation = findViewById(R.id.title_animation);
        cl_privacy_policy = findViewById(R.id.cl_privacy_policy);
        imageBg = findViewById(R.id.bg_image_splash);
        abc = findViewById(R.id.abc);
        setupSplashBG();
        adsConsentManager = new AdsConsentManager(this);


        if (bundle == null) {
            QuimeraInit.INSTANCE.initQuimeraSdk(getApplicationContext());
        }

        SKUs.add("weekly_3");
        SKUs.add("subs_yearly");


        // Initialize adQuality



        RemoteConfig.INSTANCE.getRemoteconfig();


        initData();

//        Boom(abc);

        abc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditorSplashActivity.this.startToMainActivity();

            }

        });

    }

    private void initData() {
        Boolean canRequestAds = adsConsentManager != null ? adsConsentManager.getCanRequestAds() : null;

        if (canRequestAds != null && !canRequestAds && !SharePreferenceUtil.isPurchased(this)) {
            adsConsentManager.showGDPRConsent(this, BuildConfig.DEBUG, formError -> {
                if (formError != null) {
                    Log.w("consentManager", formError.getErrorCode() + ": " + formError.getMessage());
                }
                loadData();
                return null;
            });
        } else {
            loadData();
        }
    }

    private void loadData(){

        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance(this).setMediationProvider( "max" );
        AppLovinSdk.initializeSdk( this, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration)
            {
                AppLovinSdk.getInstance( EditorSplashActivity.this ).getSettings().setCreativeDebuggerEnabled( false );

                // AppLovin SDK is initialized, start loading ads
                Log.e("MaxADInit","maxadinitialized");
            }
        } );

        // Amazon requires an 'Activity' instance
        AdRegistration.getInstance(MaxAdConstants.AMAZON_ID, this );
        AdRegistration.setAdNetworkInfo( new DTBAdNetworkInfo( DTBAdNetwork.MAX ) );
        AdRegistration.setMRAIDSupportedVersions( new String[] { "1.0", "2.0", "3.0" } );
        AdRegistration.setMRAIDPolicy( MRAIDPolicy.CUSTOM );

        //load inter ad
        IronSourceAdsManager.INSTANCE.loadInter(this, new IronSourceCallbacks() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail() {

            }
        });

        //load rewarded ad
        MaxAdManager.INSTANCE.createRewardedAd(this,()->{return null;},()->{return null;});

        initBilling();
    }

    private void next() {

        startToMainActivity();

    }


    public void initConfig() {
        ((PhotoApp) getApplication()).initConfig(this);
    }

    public void startToMainActivity() {

        if (SharePreferenceUtil.isFirstTime((EditorSplashActivity.this))) {
            if (MyConstant.INSTANCE.getOnboarding_flag()) {
                SharePreferenceUtil.setFirstTime(EditorSplashActivity.this, false);
                startActivity(new Intent(this, MainOnBoardingScreen.class));
                endThis();
            } else {
                if (MyConstant.INSTANCE.getPaymentcard_flag()) {
                    SharePreferenceUtil.setFirstTime(EditorSplashActivity.this, false);
                    startActivity(new Intent(this, SubscriptionActivity.class));
                    endThis();
                } else {
                    SharePreferenceUtil.setFirstTime(EditorSplashActivity.this, false);
                    startActivity(new Intent(this, MainActivity.class));
                    endThis();
                }
            }

        } else {
            SharePreferenceUtil.setFirstTime(EditorSplashActivity.this, false);
            startActivity(new Intent(this, MainActivity.class));
            endThis();
        }

    }

//    public void Boom(View iv) {
//        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(iv, "scaleX", 0.8f);
//        scaleXAnimator.setRepeatMode(ValueAnimator.REVERSE);
//        scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        scaleXAnimator.setDuration(700);
//        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(iv, "scaleY", 0.8f);
//        scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE);
//        scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        scaleYAnimator.setDuration(700);
//        scaleXAnimator.start();
//        scaleYAnimator.start();
//    }

//    private void showPrivacyDialog() {
//
//        cl_privacy_policy.animate().alphaBy(1f);
////        img.setVisibility(View.GONE);
//        adParent.setVisibility(View.VISIBLE);
//
//        agree_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPrefs.write(SplashActivity.this, SharedPrefs.IS_AGREE, true);
//                SplashActivity.this.startToMainActivity();
//            }
//        });
//        privacy_text.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String url = " https://zedlatino.info/privacy-policy-apps.html";
//                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//
//                startActivity(i);
//            }
//        });
//    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    private void setupSplashBG() {

        Glide.with(this).load(R.drawable.splash_screen_new).into(imageBg);


    }

    private void endThis() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                EditorSplashActivity.this.finish();
            }
        }, 1000);
    }


    //check subscription

    private void initBilling() {


        billingConnector = new BillingConnector(this, LICENSE_KEY)
//            .setConsumableIds(consumableIds)
//            .setNonConsumableIds(nonConsumableIds)
                .setSubscriptionIds(SKUs)
                .autoAcknowledge()
                .autoConsume()
                .enableLogging()
                .connect();

        billingConnector.setBillingEventListener(new BillingEventListener() {

            @Override
            public void onProductsFetched(@NonNull List<ProductInfo> skuDetails, List<ProductDetails> productDetailsList) {


            }

            @Override
            public void onPurchasedProductsFetched(@NonNull ProductType skuType, @NonNull List<PurchaseInfo> purchases) {
                if (purchases.isEmpty()) {
                    Log.e("billing", "not purchase");
                    SharePreferenceUtil.setPurchased(EditorSplashActivity.this.getApplicationContext(), false);
                } else {

                    Log.e("billing", "purchase");

                    for (int i = 0; i < purchases.size(); i++) {
//                        Log.e("billing",">"+ purchases.get(i).getProduct());
                        if (purchases.get(i).getProduct().contains("weekly_3")) {
                            weeklyPurchase = true;
                        } else {
                            yearlyPurchase = true;
                        }
                    }
                    SharePreferenceUtil.setPurchased(EditorSplashActivity.this.getApplicationContext(), true);
//                    startToMainActivity();


                }

                checkSubscription();

            }

            @Override
            public void onProductsPurchased(@NonNull List<PurchaseInfo> purchases) {
                if (purchases.isEmpty()) {
                    SharePreferenceUtil.setPurchased(EditorSplashActivity.this.getApplicationContext(), false);
                } else {
                    SharePreferenceUtil.setPurchased(EditorSplashActivity.this.getApplicationContext(), true);

                }

            }

            @Override
            public void onPurchaseAcknowledged(@NonNull PurchaseInfo purchase) {
                /*Callback after a purchase is acknowledged*/

                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */
            }

            @Override
            public void onPurchaseConsumed(@NonNull PurchaseInfo purchase) {
                /*Callback after a purchase is consumed*/

                /*
                 * CONSUMABLE products entitlement can be granted either here or in onProductsPurchased
                 * */
            }

            @Override
            public void onBillingError(@NonNull BillingConnector billingConnector, @NonNull BillingResponse response) {
                switch (response.getErrorType()) {
                    case CLIENT_NOT_READY:
                        Timber.e("=-================>Client Not Ready");
                        break;

                    case CLIENT_DISCONNECTED:
                        Timber.e("=-================>Client Not Disconnected");


                        break;
//                    case SKU_NOT_EXIST:
//                        Timber.e("=-================>Sku Not Exists");
//
//                        break;
                    case CONSUME_ERROR:
                        Timber.e("=-================>Consume Eerr");

                        break;
                    case ACKNOWLEDGE_ERROR:
                        Timber.e("=-================>Ack Err");
                        break;
                    case ACKNOWLEDGE_WARNING:
                        Timber.e("=-================>Ack Warning");

                        break;
                    case FETCH_PURCHASED_PRODUCTS_ERROR:
                        Timber.e("=-================>Fetch_Purchase Product Err");
                        break;
                    case BILLING_ERROR:
                        Timber.e("=-================>Billing Err");

                        break;
                    case USER_CANCELED:
                        Timber.e("=-================>User Cancelled");

                        break;
                    case SERVICE_UNAVAILABLE:
                        Timber.e("=-================>SService Unavailable");

                        break;
                    case BILLING_UNAVAILABLE:
                        Timber.e("=-================>Billing Not Available");

                        break;
                    case ITEM_UNAVAILABLE:
                        Timber.e("=-================>Item Not Available");

                        break;
                    case DEVELOPER_ERROR:
                        Timber.e("=-================>Developer Err");

                        break;
                    case ERROR:
                        Timber.e("=-================> Error!");

                        break;
                    case ITEM_ALREADY_OWNED:
                        Timber.e("=-================>Item Already Owned");

                        break;
                    case ITEM_NOT_OWNED:
                        Timber.e("=-================>Item Not Ownd");

                        break;
                }
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                moveNext();
            }
        }, 1000);
    }


    public void moveNext() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                // Your Code
//                if (!SharedPrefs.read(SplashActivity.this, SharedPrefs.IS_AGREE, false)) {
//                    showPrivacyDialog();
//                    PhotoApp.appOpenManager.showAdIfAvailable();
//                    title_animation.setVisibility(View.GONE);
//                } else {

//                title_animation.setVisibility(View.GONE);
                if (!SharePreferenceUtil.isPurchased(EditorSplashActivity.this)) {
                    if (RemoteConfigData.INSTANCE.getAllAdsEnabled_()) {


                        //MaxAdOpenApp>>done
                        MaxAppOpenAdManager.Companion.getInstance(getApplicationContext()).loadSplashAd(EditorSplashActivity.this, new AppOpenCallback() {

                            @Override
                            public void isAdDismiss(boolean isShow) {
                                next();
                            }

                            @Override
                            public void isAdShown(boolean isShow) {

                            }

                            @Override
                            public void isAdLoad(boolean isLoad) {

                            }
                        });

                    } else {

                        next();

                    }
                } else {

                    SharePreferenceUtil.setFirstTime(EditorSplashActivity.this, false);
                    startActivity(new Intent(EditorSplashActivity.this, MainActivity.class));
                    endThis();
                }

//                }
            }
        }, 200);
    }

    public void checkSubscription() {
        if (weeklyPurchase || yearlyPurchase) {
            SharePreferenceUtil.setPurchased(EditorSplashActivity.this.getApplicationContext(), true);

        } else {
            SharePreferenceUtil.setPurchased(EditorSplashActivity.this.getApplicationContext(), false);

        }
    }

}
