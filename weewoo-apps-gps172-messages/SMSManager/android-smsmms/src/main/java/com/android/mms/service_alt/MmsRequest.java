package com.android.mms.service_alt;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.carrier.CarrierMessagingService;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import com.android.mms.service_alt.exception.ApnException;
import com.android.mms.service_alt.exception.MmsHttpException;
import com.klinker.android.send_message.Utils;

import timber.log.Timber;

public abstract class MmsRequest {

    private static final int RETRY_TIMES = 3;
    protected RequestManager mRequestManager;
    protected int mSubId;
    protected String mCreator;
    protected MmsConfig.Overridden mMmsConfig;
    protected Bundle mMmsConfigOverrides;
    private boolean mobileDataEnabled;

    public MmsRequest(RequestManager requestManager, int subId, String creator,
                      Bundle configOverrides) {
        mRequestManager = requestManager;
        mSubId = subId;
        mCreator = creator;
        mMmsConfigOverrides = configOverrides;
        mMmsConfig = null;
    }

    private static boolean inAirplaneMode(final Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private static boolean isMobileDataEnabled(final Context context, final int subId) {
        final TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return Utils.isDataEnabled(telephonyManager, subId);
    }

    private static boolean isDataNetworkAvailable(final Context context, final int subId) {
        return !inAirplaneMode(context) && isMobileDataEnabled(context, subId);
    }

    public static boolean useWifi(Context context) {
        if (Utils.isMmsOverWifiEnabled(context)) {
            ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnMgr != null) {
                NetworkInfo niWF = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                return (niWF != null) && (niWF.isConnected());
            }
        }
        return false;
    }

    protected static int toSmsManagerResult(int carrierMessagingAppResult) {
        switch (carrierMessagingAppResult) {
            case CarrierMessagingService.SEND_STATUS_OK:
                return Activity.RESULT_OK;
            case CarrierMessagingService.SEND_STATUS_RETRY_ON_CARRIER_NETWORK:
                return SmsManager.MMS_ERROR_RETRY;
            default:
                return SmsManager.MMS_ERROR_UNSPECIFIED;
        }
    }

    public int getSubId() {
        return mSubId;
    }

    private boolean ensureMmsConfigLoaded() {
        if (mMmsConfig == null) {
            final MmsConfig config;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                config = MmsConfigManager.getInstance().getMmsConfigBySubId(mSubId);
            } else {
                config = MmsConfigManager.getInstance().getMmsConfig();
            }

            if (config != null) {
                mMmsConfig = new MmsConfig.Overridden(config, mMmsConfigOverrides);
            }
        }
        return mMmsConfig != null;
    }

    public void execute(Context context, MmsNetworkManager networkManager) {
        int result = SmsManager.MMS_ERROR_UNSPECIFIED;
        int httpStatusCode = 0;
        byte[] response = null;

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean isWifiEnabled = wifi.isWifiEnabled();

        if (!useWifi(context)) {
            wifi.setWifiEnabled(false);
        }

        mobileDataEnabled = Utils.isMobileDataEnabled(context);
        Timber.v("mobile data enabled: " + mobileDataEnabled);

        if (!mobileDataEnabled && !useWifi(context)) {
            Timber.v("mobile data not enabled, so forcing it to enable");
            Utils.setMobileDataEnabled(context, true);
        }

        if (!ensureMmsConfigLoaded()) { // Check mms config
            Timber.e("MmsRequest: mms config is not loaded yet");
            result = SmsManager.MMS_ERROR_CONFIGURATION_ERROR;
        } else if (!prepareForHttpRequest()) { // Prepare request, like reading pdu data from user
            Timber.e("MmsRequest: failed to prepare for request");
            result = SmsManager.MMS_ERROR_IO_ERROR;
        } else if (!isDataNetworkAvailable(context, mSubId)) {
            Timber.e("MmsRequest: in airplane mode or mobile data disabled");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                result = SmsManager.MMS_ERROR_NO_DATA_NETWORK;
            } else {
                result = 8;
            }
        } else { // Execute
            long retryDelaySecs = 2;
            String apnName = networkManager.getApnName();

            try {
                networkManager.acquireNetwork();
            } catch (Exception e) {
                Timber.e(e, "error acquiring network");
            }

            for (int i = 0; i < RETRY_TIMES; i++) {
                try {

                    if (apnName == null) {
                        apnName = networkManager.getApnName();

                        try {
                            networkManager.acquireNetwork();
                        } catch (Exception e) {
                            Timber.e(e, "error acquiring network");
                        }
                    }

                    try {
                        ApnSettings apn = null;
                        try {
                            apn = ApnSettings.load(context, apnName, mSubId);
                        } catch (ApnException e) {
                            if (apnName == null) {
                                throw (e);
                            }
                            Timber.i("MmsRequest: No match with APN name:"
                                    + apnName + ", try with no name");
                            apn = ApnSettings.load(context, null, mSubId);
                        }
                        Timber.i("MmsRequest: using " + apn);
                        response = doHttp(context, networkManager, apn);
                        result = Activity.RESULT_OK;
                        break;
                    } finally {
                        networkManager.releaseNetwork();
                    }
                } catch (ApnException e) {
                    Timber.e(e, "MmsRequest: APN failure");
                    result = SmsManager.MMS_ERROR_INVALID_APN;
                    break;
                } catch (MmsHttpException e) {
                    Timber.e(e, "MmsRequest: HTTP or network I/O failure");
                    result = SmsManager.MMS_ERROR_HTTP_FAILURE;
                    httpStatusCode = e.getStatusCode();
                } catch (Exception e) {
                    Timber.e(e, "MmsRequest: unexpected failure");
                    result = SmsManager.MMS_ERROR_UNSPECIFIED;
                    break;
                }
                try {
                    Thread.sleep(retryDelaySecs * 1000, 0/*nano*/);
                } catch (InterruptedException e) {
                }
                retryDelaySecs <<= 1;
            }
        }

        if (!mobileDataEnabled) {
            Timber.v("setting mobile data back to disabled");
            Utils.setMobileDataEnabled(context, false);
        }

        if (!useWifi(context)) {
            wifi.setWifiEnabled(isWifiEnabled);
        }

        processResult(context, result, response, httpStatusCode);
    }

    public void processResult(Context context, int result, byte[] response, int httpStatusCode) {
        final Uri messageUri = persistIfRequired(context, result, response);

        final PendingIntent pendingIntent = getPendingIntent();
        if (pendingIntent != null) {
            boolean succeeded = true;
            Intent fillIn = new Intent();
            if (response != null) {
                succeeded = transferResponse(fillIn, response);
            }
            if (messageUri != null) {
                fillIn.putExtra("uri", messageUri.toString());
            }
            if (result == SmsManager.MMS_ERROR_HTTP_FAILURE && httpStatusCode != 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    fillIn.putExtra(SmsManager.EXTRA_MMS_HTTP_STATUS, httpStatusCode);
                } else {
                    fillIn.putExtra("android.telephony.extra.MMS_HTTP_STATUS", httpStatusCode);
                }
            }
            try {
                if (!succeeded) {
                    result = SmsManager.MMS_ERROR_IO_ERROR;
                }
                pendingIntent.send(context, result, fillIn);
            } catch (PendingIntent.CanceledException e) {
                Timber.e(e, "MmsRequest: sending pending intent canceled");
            }
        }

        revokeUriPermission(context);
    }

    protected boolean maybeFallbackToRegularDelivery(int carrierMessagingAppResult) {
        if (carrierMessagingAppResult
                == CarrierMessagingService.SEND_STATUS_RETRY_ON_CARRIER_NETWORK
                || carrierMessagingAppResult
                == CarrierMessagingService.DOWNLOAD_STATUS_RETRY_ON_CARRIER_NETWORK) {
            Timber.d("Sending/downloading MMS by IP failed.");
            mRequestManager.addSimRequest(MmsRequest.this);
            return true;
        } else {
            return false;
        }
    }

    protected abstract byte[] doHttp(Context context, MmsNetworkManager netMgr, ApnSettings apn)
            throws MmsHttpException;

    protected abstract PendingIntent getPendingIntent();

    protected abstract int getQueueType();

    protected abstract Uri persistIfRequired(Context context, int result, byte[] response);

    protected abstract boolean prepareForHttpRequest();

    protected abstract boolean transferResponse(Intent fillIn, byte[] response);

    protected abstract void revokeUriPermission(Context context);

    public interface RequestManager {
        void addSimRequest(MmsRequest request);

        boolean getAutoPersistingPref();

        byte[] readPduFromContentUri(final Uri contentUri, final int maxSize);

        boolean writePduToContentUri(final Uri contentUri, final byte[] pdu);
    }

}
