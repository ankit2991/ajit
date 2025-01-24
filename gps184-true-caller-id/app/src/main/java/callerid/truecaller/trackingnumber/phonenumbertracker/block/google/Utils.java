package callerid.truecaller.trackingnumber.phonenumbertracker.block.google;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.StartPage;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdConstants;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdInterstitialListener;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;

public class Utils {

    public static String Privacy = "https://zedlatino.info/privacy-policy-apps.html";
    public static String Terms = "https://zedlatino.info/TermsOfUse.html";

    public static final String ON_BOARDING_ACTIVE_REMOTE_CONFIG_KEY = "onBoarding_active";
    public static final String ACTION_FROM_ON_BOARDING = "ACTION_FROM_ON_BOARDING";
    public static final String EXTRA_FROM_ON_BOARDING = "EXTRA_FROM_ON_BOARDING";

    public static final String REMOTE_KEY_ON_BOARDING_FLAG = "GPS184_onboarding_flag";
    public static final String REMOTE_KEY_PAYMENT_CARD_FLAG = "GPS184_paymentcard_flag";
    public static final String REMOTE_KEY_PRICE_PLAN = "GPS184_price_plan";
    public static String KEY_SUBSCRIPTION_REMOTE_PLAN = "weekly_subscription3";

    public static ArrayList<AppAddDataList> Big_native = new ArrayList<>();

    public static String App_Update = "";
    public static String App_Update_image = "";
    public static String App_Update_link = "";
    public static int Time_interval = 60;

    public static Integer defaultValue = 2;
    public static Integer ad_count = 0;

    public static Integer NATIVE_SESSION = 0;
    public static Boolean IS_SPLASH_SCREEN = false;  //this is for not to advanced load appopen ad when app launch movetoforeground call and also in splash appopen load call thats why load is called 2 times
    public static int wi;
    public static float hg;

    public static String card;
    public static String state;

    public static FirebaseRemoteConfig getRemoteConfig() {

        FirebaseRemoteConfig mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings =
                new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1).build();
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFireBaseRemoteConfig.fetchAndActivate();

        return mFireBaseRemoteConfig;

    }

    public static void showInter(AppCompatActivity activity, adShowCallBack adShowCallBack) {
        if (TinyDB.getInstance(activity).weeklyPurchased()) {
            adShowCallBack.adFailed(true);
            return;
        }
        Intent intent = new Intent(activity, StartPage.class);
        Dialog dialog = new Dialog(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_ad, null);

        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                showInterAndMoveToNext();

        MaxAdManager.INSTANCE.createInterstitialAd(
                activity,
                MaxAdConstants.MAX_AD_INTERSTITIAL_ID,
                new MaxAdInterstitialListener() {
                    @Override
                    public void onAdLoaded(boolean adLoad) {
                        dialog.dismiss();
                        if (!adLoad) adShowCallBack.adFailed(true);
                    }

                    @Override
                    public void onAdShowed(boolean adShow) {
                        if (adShow) adShowCallBack.adShown(true);
                        else adShowCallBack.adFailed(true);
                    }
                }

        );
    }
}
