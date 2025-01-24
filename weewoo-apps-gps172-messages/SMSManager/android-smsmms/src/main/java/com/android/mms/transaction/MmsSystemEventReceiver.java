package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Telephony.Mms;

import com.klinker.android.send_message.Utils;

import timber.log.Timber;

public class MmsSystemEventReceiver extends BroadcastReceiver {
    private static ConnectivityManager mConnMgr = null;

    public static void wakeUpService(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.v("Intent received: " + intent);

        if (!Utils.isDefaultSmsApp(context)) {
            Timber.v("not default sms app, cancelling");
            return;
        }

        String action = intent.getAction();
        if (action.equals(Mms.Intents.CONTENT_CHANGED_ACTION)) {
            Uri changed = intent.getParcelableExtra(Mms.Intents.DELETED_CONTENTS);
        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (mConnMgr == null) {
                mConnMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
            }

            if (Utils.isMmsOverWifiEnabled(context)) {
                NetworkInfo niWF = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if ((niWF != null) && (niWF.isConnected())) {
                    Timber.v("TYPE_WIFI connected");
                    wakeUpService(context);
                }
            } else {
                boolean mobileDataEnabled = Utils.isMobileDataEnabled(context);
                if (!mobileDataEnabled) {
                    Timber.v("mobile data turned off, bailing");
                    return;
                }
                NetworkInfo mmsNetworkInfo = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
                if (mmsNetworkInfo == null) {
                    return;
                }
                boolean available = mmsNetworkInfo.isAvailable();
                boolean isConnected = mmsNetworkInfo.isConnected();

                Timber.v("TYPE_MOBILE_MMS available = " + available + ", isConnected = " + isConnected);

                if (available && !isConnected) {
                    wakeUpService(context);
                }
            }
        } else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            wakeUpService(context);
        }
    }
}
