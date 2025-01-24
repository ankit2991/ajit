package com.lockerroom.face.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.billingclient.api.ProductDetails;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.lockerroom.face.Constants;
import com.lockerroom.face.PhotoApp;
import com.lockerroom.face.R;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.iab.ProductPrice;
import com.lockerroom.face.iab.SubscriptionItem;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.QuimeraInit;
import com.lockerroom.face.utils.SharePreferenceUtil;
import com.lockerroom.face.utils.StaticData;

import java.util.ArrayList;
import java.util.List;

import games.moisoni.google_iab.BillingConnector;
import games.moisoni.google_iab.BillingEventListener;
import games.moisoni.google_iab.enums.ProductType;
import games.moisoni.google_iab.models.BillingResponse;
import games.moisoni.google_iab.models.ProductInfo;
import games.moisoni.google_iab.models.PurchaseInfo;
import timber.log.Timber;

//import games.moisoni.google_iab.enums.SkuType;
//import games.moisoni.google_iab.models.SkuInfo;


public class SubscriptionActivity extends AppCompatActivity {

    private TextView skuDetailsText, yearlySkuDetail, continueWithAds, privacyPolicy, termsConditions;

    Boolean weeklyPurchase = false;
    Boolean yearlyPurchase = false;
    private ConstraintLayout continueSubBtn, yearlySubButton;
    private ImageView imagePremium, backButton;
    private boolean isButtonClickable = true;
    private BillingConnector billingConnector;
    private String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn1/9UuE3xHKiJsnsC1QpB76P+pd+ppGOR3pp0eDW8vqjhKNfqMzYrq9LKQ415Mc5K9Ks1AjSb0Qcajh8FMtuv8Q+4pVPWuRam57xYSY0u5tzrchEkEeg4nO3xy4A5lfGKCq0TRVJOUg9G0ANJ9rhPMfQwHwA02RlWfN3oTX6vYKdkRamD9fcIA82p5jcIc0t9FntPiHyLwuZQ+kT/SeTgxwJp+W0hCUetnfObiMRulMgaE9vUMohLVhHRxNdtNizerU+cCIKBWq7afRdGjfABQOIzOMZKcRdz3yiIy5BWoReStTksrax4R6CL/iaRqaviZINZXZRbwDIxYDCJb/x/wIDAQAB";
    private ArrayList<String> SKUs = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.red(PhotoApp.resources.getColor(R.color.__picker_common_primary)));
        setContentView(R.layout.subscription_activity);
//        SKUs.add("weekly_3");
        SKUs.add("weekly_3");
//        SKUs.add("yearly");
        SKUs.add("subs_yearly");
//        SDKProject.INSTANCE.getEventDispatcher() .sendPaymentCardEvent();
        initViews();
        initListeners();
        initBilling();
        if (Constants.SHOW_ADS) {
            IronSourceAdsManager.INSTANCE.loadInter(this, new IronSourceCallbacks() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFail() {

                }
            });

        }
    }


    private void initListeners() {
        backButton.setOnClickListener((v) ->{
            MaxAdManager.INSTANCE.checkTap(SubscriptionActivity.this,()->{
                startToMainActivity();

                return null;
            });
        });






        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    String url = getResources().getString(R.string.privacy_policy_url);
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    startActivity(i);
                } catch (Exception ignored) {
                }
            }
        });


        termsConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = "https://zedlatino.info/TermsOfUse.html";
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                    startActivity(i);

                } catch (Exception ignored) {
                }
            }

        });


    }

    private void initViews() {
        skuDetailsText = findViewById(R.id.sku_text);
        yearlySkuDetail = findViewById(R.id.continue_yearly_purchase_label);
//        continueWithAds = findViewById(R.id.go_with_ads);
        imagePremium = findViewById(R.id.mainImagePremium);
        continueSubBtn = findViewById(R.id.monthly_purchase_button);
        yearlySubButton = findViewById(R.id.yearly_purchase_button);
        privacyPolicy = findViewById(R.id.privacy_policy);
        termsConditions = findViewById(R.id.terms_conditions);
        backButton = findViewById(R.id.back);
        try {
            Glide.with(this)
                    .load(R.drawable.premium_img)
                    .into(imagePremium);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "fail to load image", Toast.LENGTH_SHORT).show();
        }
//
    }

    private void endThis() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                SubscriptionActivity.this.finish();
            }
        }, 1000);
    }

    public void startToMainActivity() {
        if (StaticData.fromMain) {
            endThis();
        } else {
            startActivity(new Intent(SubscriptionActivity.this, MainActivity.class));
            endThis();
        }

    }

    @Override
    public void onBackPressed() {

        MaxAdManager.INSTANCE.checkTap(SubscriptionActivity.this,()->{
            startToMainActivity();

            return null;
        });

    }

    @Override
    protected void onDestroy() {
        StaticData.fromMain = false;
        super.onDestroy();
    }

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

                if (skuDetails != null) {


                    continueSubBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //method for weekly subscription
//                subscribeMonthly();
                            //method for yearly subscription
                            subscribeYearly(productDetailsList);
                        }
                    });

                    yearlySubButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                subscribeYearly();
                            //method for weekly subscription
                            subscribeMonthly(productDetailsList);
                        }
                    });



                    if (skuDetails.size() == 1) {
//                    String temp = "Enjoy 3 days free trial - after that " +
//                            " " + skuDetails.get(1).getSubscriptionOfferPrice(0, 0) + "/" + " Week";
//                    skuDetailsText.setText(temp);
                    }

                    if (skuDetails.size() == 2) {

                        int size = skuDetails.get(1).getSubscriptionOfferDetails().size();
//                    Enjoy 3 days free trial - after that [price] / [period].

                        if (MyConstant.INSTANCE.getPrice_plan().equals("subs_yearly")) {

                            String price_yearly = ProductPrice.INSTANCE.getProductPrice(skuDetails.get(0));
                            String price_weekly = ProductPrice.INSTANCE.getProductPrice(skuDetails.get(1));
                            String temp = price_yearly + "/" + "Year" + " & 3 day free trial";
                            String temp1 = "Subscribe " + price_weekly + "/Weekly";

                            skuDetailsText.setText(temp);
                            yearlySkuDetail.setText(temp1);
                        } else {
                            String price_yearly = ProductPrice.INSTANCE.getProductPrice(skuDetails.get(0));
                            String price_weekly = ProductPrice.INSTANCE.getProductPrice(skuDetails.get(1));
                            String temp = price_weekly + "/" + "Weekly" + " & 3 day free trial";
                            String temp1 = "Subscribe " + price_yearly + "/Yearly";
                            skuDetailsText.setText(temp);
                            yearlySkuDetail.setText(temp1);
                        }
                    }
                }

            }

            @Override
            public void onPurchasedProductsFetched(@NonNull ProductType skuType, @NonNull List<PurchaseInfo> purchases) {
                if (purchases.isEmpty()) {
                    SharePreferenceUtil.setPurchased(SubscriptionActivity.this.getApplicationContext(), false);
                } else {

                    for (int i=0; i<purchases.size(); i++) {
//                        Log.e("billing",">"+ purchases.get(i).getProduct());
                        if (purchases.get(i).getProduct().contains("weekly_3")) {
                            weeklyPurchase = true;
                        } else {
                            yearlyPurchase = true;
                        }
                    }
                    SharePreferenceUtil.setPurchased(SubscriptionActivity.this.getApplicationContext(), true);


                }
                checkSubscription();
            }

            @Override
            public void onProductsPurchased(@NonNull List<PurchaseInfo> purchases) {
                if (purchases.isEmpty()) {
                    SharePreferenceUtil.setPurchased(SubscriptionActivity.this.getApplicationContext(), false);
                } else {
                    for (int i=0; i<purchases.size(); i++) {
//                        Log.e("billing",">"+ purchases.get(i).getProduct());
                        if (purchases.get(i).getProduct().contains("weekly_3")) {
                            weeklyPurchase = true;
                        } else {
                            yearlyPurchase = true;
                        }
                    }

                    SharePreferenceUtil.setPurchased(SubscriptionActivity.this.getApplicationContext(), true);
//                    startActivity(new Intent(SubscriptionActivity.this, EditorSplashActivity.class));
//                    finishAffinity();

                    if (!SubscriptionActivity.this.isFinishing() && !SubscriptionActivity.this.isDestroyed()){
                        QuimeraInit.INSTANCE.userPurchased(getApplicationContext(),purchases);
                    }

                    restartApp();
                }
                checkSubscription();
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
                        if (!SubscriptionActivity.this.isFinishing() && !SubscriptionActivity.this.isDestroyed()){
                            QuimeraInit.INSTANCE.billingError(getApplicationContext());
                        }
                        break;
                    case USER_CANCELED:
                        Timber.e("=-================>User Cancelled");
                        if (!SubscriptionActivity.this.isFinishing() && !SubscriptionActivity.this.isDestroyed()){
                            QuimeraInit.INSTANCE.userCancelBilling(getApplicationContext());
                        }
                        break;
                    case SERVICE_UNAVAILABLE:
                        Timber.e("=-================>SService Unavailable");
                        if (!SubscriptionActivity.this.isFinishing() && !SubscriptionActivity.this.isDestroyed()){
                            QuimeraInit.INSTANCE.serviceUnavailable(getApplicationContext());
                        }
                        break;
                    case BILLING_UNAVAILABLE:
                        Timber.e("=-================>Billing Not Available");
                        if (!SubscriptionActivity.this.isFinishing() && !SubscriptionActivity.this.isDestroyed()){
                            QuimeraInit.INSTANCE.billingUnavailable(getApplicationContext());
                        }
                        break;
                    case ITEM_UNAVAILABLE:
                        Timber.e("=-================>Item Not Available");
                        if (!SubscriptionActivity.this.isFinishing() && !SubscriptionActivity.this.isDestroyed()){
                            QuimeraInit.INSTANCE.itemUnavailable(getApplicationContext());
                        }
                        break;
                    case DEVELOPER_ERROR:
                        Timber.e("=-================>Developer Err");
                        if (!SubscriptionActivity.this.isFinishing() && !SubscriptionActivity.this.isDestroyed()){
                            QuimeraInit.INSTANCE.developerError(getApplicationContext());
                        }
                        break;
                    case ERROR:
                        Timber.e("=-================> Error!");
                        if (!SubscriptionActivity.this.isFinishing() && !SubscriptionActivity.this.isDestroyed()){
                            QuimeraInit.INSTANCE.billingError(getApplicationContext());
                        }
                        break;
                    case ITEM_ALREADY_OWNED:
                        Timber.e("=-================>Item Already Owned");
                        if (!SubscriptionActivity.this.isFinishing() && !SubscriptionActivity.this.isDestroyed()){
                            QuimeraInit.INSTANCE.itemAlreadyOwned(getApplicationContext());
                        }
                        break;
                    case ITEM_NOT_OWNED:
                        Timber.e("=-================>Item Not Ownd");
                        if (!SubscriptionActivity.this.isFinishing() && !SubscriptionActivity.this.isDestroyed()){
                            QuimeraInit.INSTANCE.itemNotOwned(getApplicationContext());
                        }
                        break;
                }
            }
        });

    }

    public void subscribeMonthly(List<ProductDetails> skuDetails) {
        try {
            checkButtonState();
            if (MyConstant.INSTANCE.getPrice_plan().equals("subs_yearly")) {


                if (skuDetails != null) {
                    SubscriptionItem subscriptionItem = new SubscriptionItem(skuDetails.get(0));
                    QuimeraInit.INSTANCE.setSingleCall(true);
                    QuimeraInit.INSTANCE.sendSkuDetails(getApplicationContext(), subscriptionItem);
                }

                billingConnector.subscribe(this, SKUs.get(1));
            } else {


                if (skuDetails != null) {
                    SubscriptionItem subscriptionItem = new SubscriptionItem(skuDetails.get(1));
                    QuimeraInit.INSTANCE.setSingleCall(true);
                    QuimeraInit.INSTANCE.sendSkuDetails(getApplicationContext(), subscriptionItem);
                }

                billingConnector.subscribe(this, SKUs.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribeYearly(List<ProductDetails> skuDetails) {
        try {
            checkButtonState();
            if (MyConstant.INSTANCE.getPrice_plan().equals("subs_yearly")) {



                if (skuDetails != null) {
                    SubscriptionItem subscriptionItem = new SubscriptionItem(skuDetails.get(1));
                    QuimeraInit.INSTANCE.setSingleCall(true);
                    QuimeraInit.INSTANCE.sendSkuDetails(getApplicationContext(), subscriptionItem);
                }
                billingConnector.subscribe(this, SKUs.get(0));
            } else {


                if (skuDetails != null) {
                    SubscriptionItem subscriptionItem = new SubscriptionItem(skuDetails.get(0));
                    QuimeraInit.INSTANCE.setSingleCall(true);
                    QuimeraInit.INSTANCE.sendSkuDetails(getApplicationContext(), subscriptionItem);
                }
                billingConnector.subscribe(this, SKUs.get(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void restartApp(){

        startActivity(new Intent(SubscriptionActivity.this, MainActivity.class));
//        overridePendingTransition(R.anim.animation,R.anim.animation2)
        finishAffinity();
    }

    public void checkSubscription(){
        if (weeklyPurchase || yearlyPurchase){
            SharePreferenceUtil.setPurchased(SubscriptionActivity.this.getApplicationContext(), true);

        }else{
            SharePreferenceUtil.setPurchased(SubscriptionActivity.this.getApplicationContext(), false);

        }
    }


    private void checkButtonState() {
        yearlySubButton.setEnabled(false);
        continueSubBtn.setEnabled(false);
        if (isButtonClickable) {
            isButtonClickable = false;
            yearlySubButton.setEnabled(false);
            continueSubBtn.setEnabled(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isButtonClickable = true;
                    yearlySubButton.setEnabled(true);
                    continueSubBtn.setEnabled(true);
                }
            },3000);

        }
    }
}
