package com.translate.languagetranslator.freetranslation.activities.introScreen;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.translate.languagetranslator.freetranslation.Fragments.Intro1Fragment;
import com.translate.languagetranslator.freetranslation.Fragments.Intro2Fragment;
import com.translate.languagetranslator.freetranslation.Fragments.Intro3Fragment;
import com.translate.languagetranslator.freetranslation.R;
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity;
import com.translate.languagetranslator.freetranslation.appUtils.Constants;
import com.translate.languagetranslator.freetranslation.appUtils.UtilsMethodsKt;


public class IntroActivity extends AppIntro /*implements BillingProcessor.IBillingHandler*/ {

    private FirebaseRemoteConfig mFireBaseRemoteConfig;
    private FirebaseRemoteConfigSettings configSettings;
    private boolean isSubscriptionEnabled = false;
//    private BillingProcessor billingProcessor;
    private boolean readyToPurchase = false;
    private boolean isSubscriptionAvailed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initRemoteConfig();
//        initinApp();
        addSlide(Intro1Fragment.Companion.getInst());
        addSlide(Intro2Fragment.Companion.getInst());
        addSlide(Intro3Fragment.Companion.getInst());


    }

//    private void initinApp() {
//        billingProcessor = new BillingProcessor(this, Constants.LICENSE_KEY, Constants.MERCHANT_ID, this);
//        billingProcessor.initialize();
//    }

    private void initRemoteConfig() {
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(100)
                .build();
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config);
        isSubscriptionEnabled = mFireBaseRemoteConfig.getBoolean(Constants.SUBSCRIPTION_SCREEN_SHOW);
//        mFireBaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
//            @Override
//            public void onComplete(@NonNull Task<Boolean> task) {
//                if (task.isSuccessful()){
//                    isSubscriptionEnabled= mFireBaseRemoteConfig.getBoolean(Constants.SUBSCRIPTION_SCREEN_SHOW);
//                }
//                else {
//                    task.addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Log.e("remote config failure",""+e.getMessage());
//                        }
//                    });
//                }
//            }
//        });


    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        checkSubscription();

    }

    public void checkSubscription() {
        if (isSubscriptionAvailed) {
            startMainActivity();
        } else {
            if (isSubscriptionEnabled) {
                startSubscriptionScreen();
            } else {
                startMainActivity();
            }
        }
//        TinyDB.getInstance(this).putBoolean(Constants.SPLASH_FIRST, true);
        finish();

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        checkSubscription();

    }

    @Override
    public void onSlideChanged(Fragment oldFragment, Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    public void startMainActivity() {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
    }

    public void startSubscriptionScreen() {
//        UtilsMethodsKt.openSubscriptionActivity(this, "intro");
//        startActivity(new Intent(getApplicationContext(), SubscriptionActivity.class));
    }

//    @Override
//    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
//
//    }
//
//    @Override
//    public void onPurchaseHistoryRestored() {
//        for (String sku : billingProcessor.listOwnedProducts()) {
//            if (sku.equals(getString(R.string.in_app_key))) {
////                TinyDB.getInstance(IntroActivity.this).putBoolean(Constants.IS_PREMIUM, true);
//                isSubscriptionAvailed = true;
//            } else {
////                TinyDB.getInstance(IntroActivity.this).putBoolean(Constants.IS_PREMIUM, false);
//                isSubscriptionAvailed = false;
//            }
//        }
//
//        for (String sku : billingProcessor.listOwnedSubscriptions()) {
//            if ( sku.equals(Constants.SUBSCRIPTION_ID_YEAR)) {
////                TinyDB.getInstance(IntroActivity.this).putBoolean(Constants.IS_PREMIUM, true);
//                isSubscriptionAvailed = true;
//            } else {
////                TinyDB.getInstance(IntroActivity.this).putBoolean(Constants.IS_PREMIUM, false);
//                isSubscriptionAvailed = false;
//            }
//        }
//
//
//    }
//
//    @Override
//    public void onBillingError(int errorCode, @Nullable Throwable error) {
//        Log.i("InApp", "error" + errorCode);
//    }
//
//    @Override
//    public void onBillingInitialized() {
//        if (billingProcessor.isOneTimePurchaseSupported()) {
//            readyToPurchase = true;
//            Log.i("InApp", "initialized");
//        }
//
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (billingProcessor != null) {
//            billingProcessor.release();
//        }
//    }
}
