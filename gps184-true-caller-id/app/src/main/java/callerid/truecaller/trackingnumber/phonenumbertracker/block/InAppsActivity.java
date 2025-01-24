package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import static callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils.ACTION_FROM_ON_BOARDING;
import static callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils.EXTRA_FROM_ON_BOARDING;

import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.iap.IapConnector;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.iap.SubscriptionServiceListener;

public class InAppsActivity extends AppCompatActivity {
    public static final String WEEKLY_SUBS = "weekly_subscription3";
    public static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlSJBy0w6dpXi0LQWqq9Tj9/tIsU5Qh3WCdVMyYRJX8d+O6vI4j97A4fJ8FZyhndw4eZGxmNei+XRTYjwh4j3d8e+cvFo5hMK4UqLUS2RxTQubQ38E7kv16qW/VaDllpono6/ACm8mBy05FtcYW0wlqbL+GlH8keeYOa7nqrR9R3SnmnKI7eQIA9ew2IPng0sk2n1HYkVYA3CJ5YcY7ks3ph95C0/8lsOmgoWRNriZeOa64+bVJ0CJQFYsm0/ZmNupwyXEq5D4GGepJRuYsKuu0TG6axYlTfw0nT216FVJi2+zcJD6+CsjBc4ifSzeor5vHsV2HXRovHcC5s2sUmguQIDAQAB";
    private IapConnector iapConnector;
    private ProductDetails productFromRemotePlan;
    private ProductDetails productOtherConfig;
    private TextView txtPricePeriod;
    private TextView txtSubscribe;
    private boolean shouldShowSubscriptionSuccess = true;

    public static List<String> getIds() {
        List<String> ids = new ArrayList<>();
        ids.add(WEEKLY_SUBS);
        return ids;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TinyDB.getInstance(this).setInAppsShowed(true);
        setContentView(R.layout.inapps_activity);
        makeStatusbarTransparent();
        setupUI();
        setupInApps();
        TinyDB.getInstance(this).setNotFirstLaunchApp();
//        App.eventHelper.sendPaymentCardEvent();
    }

    private void setupUI() {
        txtPricePeriod = findViewById(R.id.price_period);
        findViewById(R.id.continue_with_ads).setOnClickListener(v -> actionTryNowForFree());

        txtSubscribe = findViewById(R.id.txt_purchase);
        findViewById(R.id.subscribe_btn).setOnClickListener(v -> actionSubscribeFor());
        findViewById(R.id.btn_close).setOnClickListener(v -> {

                TinyDB.getInstance(getApplicationContext()).setWeeklyPurchased(false);
                Intent intent = new Intent(InAppsActivity.this, StartPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

        });

        findViewById(R.id.privacy).setOnClickListener(v -> {
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(Utils.Privacy)));
            } catch (Exception ignore) {

            }
        });

        findViewById(R.id.terms).setOnClickListener(v -> {
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(Utils.Terms)));
            } catch (Exception ignore) {

            }
        });
    }

    private void setupInApps() {
        shouldShowSubscriptionSuccess = !(getIntent().getAction() != null
                && getIntent().getAction().equals(ACTION_FROM_ON_BOARDING)
                && getIntent().getBooleanExtra(EXTRA_FROM_ON_BOARDING, false));
        //in app billing v5
        iapConnector = IapConnector.Companion.getInstance(this);
        iapConnector.addSubscriptionListener(new SubscriptionServiceListener() {
            @Override
            public void onSubscriptionRestored(@NonNull Purchase purchaseInfo) {
                TinyDB.getInstance(getApplicationContext()).setWeeklyPurchased(true);
                subscribeDialog();
            }

            @Override
            public void onSubscriptionPurchased(@NonNull Purchase purchaseInfo) {
                Log.d("Volts", "onProductsPurchased()");
                TinyDB.getInstance(getApplicationContext()).setWeeklyPurchased(true);
                subscribeDialog();
            }

            @Override
            public void onSubscriptionsExpired() {
                Log.d("Volts", "onProductsExpired()");
                TinyDB.getInstance(getApplicationContext()).setWeeklyPurchased(false);
            }

            @Override
            public void onPricesUpdated(@NonNull Map<String, ProductDetails> iapKeyPrices) {
                productFromRemotePlan = iapKeyPrices.get(Utils.KEY_SUBSCRIPTION_REMOTE_PLAN);
                if (productFromRemotePlan != null) {
                    ProductDetails.PricingPhase pricingPhase = iapConnector.getOriginPricingPhase(productFromRemotePlan);
                    if (pricingPhase != null) {
                        String periodicRemotePlanConfig = Objects.equals(Utils.KEY_SUBSCRIPTION_REMOTE_PLAN, IapConnector.PURCHASE_YEARLY_SUBSCRIPTION) ? "yearly" : "weekly";
                        String priceRemotePlanString = String.format(
                                getResources().getString(R.string.price_payment_card),
                                pricingPhase.getFormattedPrice(),
                                periodicRemotePlanConfig
                        );
                        txtPricePeriod.setText(priceRemotePlanString);
                    }
                }

                String keySubOther = Objects.equals(Utils.KEY_SUBSCRIPTION_REMOTE_PLAN, IapConnector.PURCHASE_YEARLY_SUBSCRIPTION) ? IapConnector.PURCHASE_WEEKLY_SUBSCRIPTION : IapConnector.PURCHASE_YEARLY_SUBSCRIPTION;
                productOtherConfig = iapKeyPrices.get(keySubOther);
                if (productOtherConfig != null) {
                    ProductDetails.PricingPhase pricingPhase = iapConnector.getOriginPricingPhase(productOtherConfig);
                    if (pricingPhase != null) {
                        String periodicOtherConfig = Objects.equals(Utils.KEY_SUBSCRIPTION_REMOTE_PLAN, IapConnector.PURCHASE_YEARLY_SUBSCRIPTION) ? "weekly" : "yearly";
                        String priceSubscribeText = String.format(
                                getResources().getString(R.string.subscribe_text),
                                pricingPhase.getFormattedPrice(),
                                periodicOtherConfig
                        );
                        txtSubscribe.setText(priceSubscribeText);
                    }
                }
            }
        });
        iapConnector.restore();
    }

    private void actionTryNowForFree() {
        if (TinyDB.getInstance(this).weeklyPurchased()) {
            showSubscribedDialog();
            return;
        }
        if (iapConnector != null && productFromRemotePlan != null) {
            shouldShowSubscriptionSuccess = true;
            iapConnector.subscribe(this, productFromRemotePlan);
        }
    }

    private void actionSubscribeFor() {
        if (TinyDB.getInstance(this).weeklyPurchased()) {
            showSubscribedDialog();
            return;
        }
        if (iapConnector != null && productOtherConfig != null) {
            shouldShowSubscriptionSuccess = true;
            iapConnector.subscribe(this, productOtherConfig);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void makeStatusbarTransparent() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void subscribeDialog() {
        if (isDestroyed() || !shouldShowSubscriptionSuccess) return;
//        App.eventHelper.sendCTASubscribeEvent();
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    TaskStackBuilder.create(this).addNextIntent(new Intent(this, SplashActivitynew.class)).startActivities();
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Subscribed Successfully!").setPositiveButton("OK", dialogClickListener).show();

    }

    private void showSubscribedDialog() {
        if (isDestroyed()) return;
//        App.eventHelper.sendCTASubscribeEvent();
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    TaskStackBuilder.create(this).addNextIntent(new Intent(this, SplashActivitynew.class)).startActivities();
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("You are subscribed!").setPositiveButton("OK", dialogClickListener).show();

    }

    public Spanned getSpannedText(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    @Override
    public void onBackPressed() {
     finish();
    }


}

