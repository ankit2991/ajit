package com.messaging.textrasms.manager.common.util;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;


import androidx.annotation.NonNull;


import com.messaging.textrasms.manager.common.interfaces.adShowCallBack;
import com.messaging.textrasms.manager.common.interfaces.onAdfailedToLoadListner;
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdListener;
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager;
import com.messaging.textrasms.manager.common.maxAdManager.OnAdShowCallback;
import com.messaging.textrasms.manager.util.Preferences;
import com.messaging.textrasms.manager.utils.Constants;


public class MaxMainAdsManger {
    static Boolean isSdkInit = false;
    static Boolean showingAdInter = false;
    public static Boolean canShowInter=false;
    static int banner_try = 0;
    static String LOG_TAG = "AD_MANAGER_Class";
//    static MaxAdBanner banner = null;
    static String showingFrom = "main";

    public static void initSdk(Activity activity) {
        if (!isSdkInit) {

            //MaxAdInit
            isSdkInit = true;
        }
    }

    public static void initiateAd(Activity context, onAdfailedToLoadListner listener) {
//        if(!showingAdInter) {
            Log.d(LOG_TAG, "Initiating Ad1");

            loadAd1(context, listener);
//        }
    }

    private static void loadAd1(Activity context, onAdfailedToLoadListner listener) {


        MaxAdManager.INSTANCE.loadInterAd(context, new MaxAdListener() {
            @Override
            public void onAdLoaded(boolean adLoad) {
                listener.onSuccess();
            }

            @Override
            public void onAdShowed(boolean adShow) {

            }

            @Override
            public void onAdHidden(boolean adHidden) {
                listener.adClose();
            }

            @Override
            public void onAdLoadFailed(boolean adLoadFailed) {
                listener.onAdFailedToLoad();
            }

            @Override
            public void onAdDisplayFailed(boolean adDisplayFailed) {
                listener.onAdFailedToLoad();
            }
        });

        //MaxAdInterAdLoad>done
    }

    public static void showInterstitial(Activity context, adShowCallBack listner) {

        if (isPurchased(context)) {
            listner.adShown(false);
            return;
        }


        //MaxAdInterAdShow>done
        MaxAdManager.INSTANCE.showInterAd(context, new OnAdShowCallback() {
            @Override
            public void onAdHidden(boolean ishow) {
                showingAdInter=false;
                listner.adShown(true);
            }

            @Override
            public void onAdfailed() {
                showingAdInter=false;
                listner.adShown(false);
            }

            @Override
            public void onAdDisplay() {

            }
        });

        showingAdInter=true;

    }


    public static void showBanner(Activity context, FrameLayout bannerContainer,String from
    ) {

        if (isPurchased(context)) {
            return;
        }


        // Toast.makeText(context,"Banner Ccalled",Toast.LENGTH_SHORT).show()
        // IronSource.init(context, context.getString(R.string.iron_sorce_app_id), IronSource.AD_UNIT.BANNER);


        //MaxAdBannerAd
        showingFrom = from;


    }



    /**
     * Check if premium user
     * @param context
     * @return
     */
    public static boolean isPurchased(@NonNull final Context context) {

        boolean purchase = Preferences.Companion.getBoolean(context, Preferences.ADSREMOVED);
        Log.i("Ads MANAGER", "isPurchased: purchase :- " + purchase);
        return purchase;

    }

public static void destroyBanner(){

//    if (banner != null) {
       //MaxAdBannerAdDestroy
//    }

}

}

