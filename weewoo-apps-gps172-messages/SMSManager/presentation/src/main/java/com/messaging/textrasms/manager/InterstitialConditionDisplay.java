package com.messaging.textrasms.manager;

import android.util.Log;

public class InterstitialConditionDisplay {

    private static InterstitialConditionDisplay INSTANCE = null;
    private static long MAX_CLICKED = 0;
    private long clickedAmount;

    private onShowInterstitialListener adListener;

    public void setOnShowInterstitialListener(onShowInterstitialListener listener) {
        adListener = listener;
    }

    private InterstitialConditionDisplay() {
        // using for init, first time ad should show right away
        clickedAmount = 0;
    }

    public static synchronized InterstitialConditionDisplay getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InterstitialConditionDisplay();
        }
        return (INSTANCE);
    }

    public void increaseClicked() {
        clickedAmount++;
        if (adListener != null) {
//            adListener.onShowAd();
        }
//        Toast.makeText(App.appLicationLoad, "Clicked Amount: " + clickedAmount, Toast.LENGTH_SHORT).show();
//        Log.d("AdCondition_clickAmount", clickedAmount +"");
    }

    public void resetClickedAmount() {
        clickedAmount = 0;
    }

    public boolean shouldShowInterstitialAd() {

        Log.d("AdCondition", "" + clickedAmount + "-----" + MAX_CLICKED);
        return clickedAmount > MAX_CLICKED;

    }

    public void setMaxClicked(long max) {
        MAX_CLICKED = max;
    }

}
