package com.translate.languagetranslator.freetranslation.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

//Use this receiver to report Firebase Events from cdo using local Firebase setup
public class FirebaseEventBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "FirebaseEventB";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals("custom_firebase_event")) {
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            String eventName = intent.getStringExtra("eventName");
            String imageName = intent.getStringExtra("imageName");
            String fullText = intent.getStringExtra("fullText");
            String eventType = intent.getStringExtra("eventType");
            if (mFirebaseAnalytics != null && eventName != null && !eventName.isEmpty() && TextUtils.equals("firebase", eventType)) {
                Log.d(TAG, "logging firebase event.. eventName = " + eventName + ", imageName = " + imageName + ", fullText = " + fullText + ", eventType = " + eventType);
                Bundle params = intent.getBundleExtra("eventParams");
                if (params == null) {
                    params = new Bundle();
                }
                if (imageName != null) {
                    params.putString("image_name", imageName);
                }
                if (fullText != null) {
                    params.putString("full_text", fullText);
                }
                mFirebaseAnalytics.logEvent(eventName, params);
            }
        }
    }
}