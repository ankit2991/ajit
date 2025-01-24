package com.android.mms.transaction;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.MmsException;
import com.klinker.android.send_message.Utils;

import java.io.IOException;

public abstract class Transaction extends Observable {
    public static final int NOTIFICATION_TRANSACTION = 0;
    public static final int RETRIEVE_TRANSACTION = 1;
    public static final int SEND_TRANSACTION = 2;
    public static final int READREC_TRANSACTION = 3;
    private final int mServiceId;
    protected Context mContext;
    protected String mId;
    protected TransactionState mTransactionState;
    protected TransactionSettings mTransactionSettings;

    public Transaction(Context context, int serviceId,
                       TransactionSettings settings) {
        mContext = context;
        mTransactionState = new TransactionState();
        mServiceId = serviceId;
        mTransactionSettings = settings;
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

    @Override
    public TransactionState getState() {
        return mTransactionState;
    }

    public abstract void process();

    public boolean isEquivalent(Transaction transaction) {
        return mId.equals(transaction.mId);
    }

    public int getServiceId() {
        return mServiceId;
    }

    public TransactionSettings getConnectionSettings() {
        return mTransactionSettings;
    }

    public void setConnectionSettings(TransactionSettings settings) {
        mTransactionSettings = settings;
    }

    protected byte[] sendPdu(byte[] pdu) throws IOException, MmsException {
        return sendPdu(SendingProgressTokenManager.NO_TOKEN, pdu,
                mTransactionSettings.getMmscUrl());
    }

    protected byte[] sendPdu(byte[] pdu, String mmscUrl) throws IOException, MmsException {
        return sendPdu(SendingProgressTokenManager.NO_TOKEN, pdu, mmscUrl);
    }

    protected byte[] sendPdu(long token, byte[] pdu) throws IOException, MmsException {
        return sendPdu(token, pdu, mTransactionSettings.getMmscUrl());
    }

    protected byte[] sendPdu(final long token, final byte[] pdu,
                             final String mmscUrl) throws IOException, MmsException {
        if (pdu == null) {
            throw new MmsException();
        }

        if (mmscUrl == null) {
            throw new IOException("Cannot establish route: mmscUrl is null");
        }

        if (useWifi(mContext)) {
            return HttpUtils.httpConnection(
                    mContext, token,
                    mmscUrl,
                    pdu, HttpUtils.HTTP_POST_METHOD,
                    false, null, 0);
        }

        return Utils.ensureRouteToMmsNetwork(mContext, mmscUrl, mTransactionSettings.getProxyAddress(), new Utils.Task<byte[]>() {
            @Override
            public byte[] run() throws IOException {
                return HttpUtils.httpConnection(
                        mContext, token,
                        mmscUrl,
                        pdu, HttpUtils.HTTP_POST_METHOD,
                        mTransactionSettings.isProxySet(),
                        mTransactionSettings.getProxyAddress(),
                        mTransactionSettings.getProxyPort());
            }
        });
    }

    protected byte[] getPdu(final String url) throws IOException {
        if (url == null) {
            throw new IOException("Cannot establish route: url is null");
        }

        if (useWifi(mContext)) {
            return HttpUtils.httpConnection(
                    mContext,
                    SendingProgressTokenManager.NO_TOKEN,
                    url,
                    null,
                    HttpUtils.HTTP_GET_METHOD,
                    false,
                    null,
                    0);
        }

        return Utils.ensureRouteToMmsNetwork(mContext, url, mTransactionSettings.getProxyAddress(), new Utils.Task<byte[]>() {
            @Override
            public byte[] run() throws IOException {
                return HttpUtils.httpConnection(
                        mContext,
                        SendingProgressTokenManager.NO_TOKEN,
                        url,
                        null,
                        HttpUtils.HTTP_GET_METHOD,
                        mTransactionSettings.isProxySet(),
                        mTransactionSettings.getProxyAddress(),
                        mTransactionSettings.getProxyPort());
            }
        });
    }

    @Override
    public String toString() {
        return getClass().getName() + ": serviceId=" + mServiceId;
    }

    abstract public int getType();
}
