package com.lockerroom.face.maxAdManager;

import android.content.Context;
import android.net.ConnectivityManager;

public class MaxUtils {
    public static boolean isNetworkConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            assert cm != null;
            return cm.getActiveNetworkInfo() != null;
        } catch (Exception e) {
            return true;
        }
    }
}
