package com.lockerroom.face.activities.onboardingScreen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.android.billingclient.api.ProductDetails;
import com.lockerroom.face.PhotoApp;
import com.lockerroom.face.R;
import com.lockerroom.face.activities.MainActivity;
import com.lockerroom.face.activities.SubscriptionActivity;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.SharePreferenceUtil;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

import games.moisoni.google_iab.BillingConnector;
import games.moisoni.google_iab.BillingEventListener;
import games.moisoni.google_iab.enums.ProductType;
import games.moisoni.google_iab.models.BillingResponse;
import games.moisoni.google_iab.models.ProductInfo;
import games.moisoni.google_iab.models.PurchaseInfo;
import timber.log.Timber;

public class MainOnBoardingScreen extends AppCompatActivity {
    ViewPager2 viewPager;
    DotsIndicator dotsIndicator;
    ConstraintLayout mNextButton;
    OnBoardingAdapter mAdapter;
    private BillingConnector billingConnector;
    private String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn1/9UuE3xHKiJsnsC1QpB76P+pd+ppGOR3pp0eDW8vqjhKNfqMzYrq9LKQ415Mc5K9Ks1AjSb0Qcajh8FMtuv8Q+4pVPWuRam57xYSY0u5tzrchEkEeg4nO3xy4A5lfGKCq0TRVJOUg9G0ANJ9rhPMfQwHwA02RlWfN3oTX6vYKdkRamD9fcIA82p5jcIc0t9FntPiHyLwuZQ+kT/SeTgxwJp+W0hCUetnfObiMRulMgaE9vUMohLVhHRxNdtNizerU+cCIKBWq7afRdGjfABQOIzOMZKcRdz3yiIy5BWoReStTksrax4R6CL/iaRqaviZINZXZRbwDIxYDCJb/x/wIDAQAB";
    private ArrayList<String> SKUs = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.red(PhotoApp.resources.getColor(R.color.__picker_common_primary)));
        setContentView(R.layout.activity_main_on_boarding);
        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dots_indicator);
        mNextButton = findViewById(R.id.btn_Next);
        mAdapter = new OnBoardingAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(mAdapter);
        dotsIndicator.attachTo(viewPager);

        SKUs.add("weekly_3");
//        SKUs.add("yearly");
        SKUs.add("subs_yearly");

        initBilling();

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (viewPager.getCurrentItem() == 0) {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                    } else if (viewPager.getCurrentItem() == 1) {

                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                    } else {
                        if (!SharePreferenceUtil.isPurchased(MainOnBoardingScreen.this)) {
                            if (MyConstant.INSTANCE.getPaymentcard_flag()) {
                                startActivity(new Intent(MainOnBoardingScreen.this, SubscriptionActivity.class));
                                slideIn();
                            } else {
                                startActivity(new Intent(MainOnBoardingScreen.this, MainActivity.class));
                                slideIn();
                            }
                        } else {
                            startActivity(new Intent(MainOnBoardingScreen.this, MainActivity.class));
                            slideIn();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        });
    }

    void slideIn() {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void initBilling() {


        billingConnector = new BillingConnector(this, LICENSE_KEY)
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
                    SharePreferenceUtil.setPurchased(MainOnBoardingScreen.this.getApplicationContext(), false);
                } else {
                    SharePreferenceUtil.setPurchased(MainOnBoardingScreen.this.getApplicationContext(), true);
//                    startToMainOnBoardingScreen();
                }

            }

            @Override
            public void onProductsPurchased(@NonNull List<PurchaseInfo> purchases) {
                if (purchases.isEmpty()) {
                    SharePreferenceUtil.setPurchased(MainOnBoardingScreen.this.getApplicationContext(), false);
                } else {
                    SharePreferenceUtil.setPurchased(MainOnBoardingScreen.this.getApplicationContext(), true);

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

    }

}

