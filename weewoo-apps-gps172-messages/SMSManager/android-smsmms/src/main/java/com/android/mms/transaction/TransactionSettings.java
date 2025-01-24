package com.android.mms.transaction;

import android.content.Context;
import android.net.NetworkUtilsHelper;
import android.provider.Telephony;
import android.text.TextUtils;

import com.android.mms.MmsConfig;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import timber.log.Timber;

public class TransactionSettings {
    private static final String[] APN_PROJECTION = {
            Telephony.Carriers.TYPE,
            Telephony.Carriers.MMSC,
            Telephony.Carriers.MMSPROXY,
            Telephony.Carriers.MMSPORT
    };
    private static final int COLUMN_TYPE = 0;
    private static final int COLUMN_MMSC = 1;
    private static final int COLUMN_MMSPROXY = 2;
    private static final int COLUMN_MMSPORT = 3;
    private final String mServiceCenter;
    private final String mProxyAddress;
    private int mProxyPort = -1;

    public TransactionSettings(Context context, String apnName) {
        Timber.v("TransactionSettings: apnName: " + apnName);

        if (Transaction.Companion.getSettings() == null) {
            Transaction.Companion.setSettings(Utils.getDefaultSendSettings(context));
        }

        mServiceCenter = NetworkUtilsHelper.trimV4AddrZeros(Transaction.Companion.getSettings().getMmsc());
        mProxyAddress = NetworkUtilsHelper.trimV4AddrZeros(Transaction.Companion.getSettings().getProxy());

        String agent = Transaction.Companion.getSettings().getAgent();
        if (agent != null && !agent.trim().equals("")) {
            MmsConfig.setUserAgent(agent);
            Timber.v("set user agent");
        }

        String uaProfUrl = Transaction.Companion.getSettings().getUserProfileUrl();
        if (uaProfUrl != null && !uaProfUrl.trim().equals("")) {
            MmsConfig.setUaProfUrl(uaProfUrl);
            Timber.v("set user agent profile url");
        }

        String uaProfTagName = Transaction.Companion.getSettings().getUaProfTagName();
        if (uaProfTagName != null && !uaProfTagName.trim().equals("")) {
            MmsConfig.setUaProfTagName(uaProfTagName);
            Timber.v("set user agent profile tag name");
        }

        if (isProxySet()) {
            try {
                mProxyPort = Integer.parseInt(Transaction.Companion.getSettings().getPort());
            } catch (NumberFormatException e) {
                Timber.e(e, "could not get proxy: " + Transaction.Companion.getSettings().getPort());
            }
        }
    }

    public TransactionSettings(String mmscUrl, String proxyAddr, int proxyPort) {
        mServiceCenter = mmscUrl != null ? mmscUrl.trim() : null;
        mProxyAddress = proxyAddr;
        mProxyPort = proxyPort;

        Timber.v("TransactionSettings: " + mServiceCenter
                + " proxyAddress: " + mProxyAddress
                + " proxyPort: " + mProxyPort);
    }

    static private boolean isValidApnType(String types, String requestType) {
        if (TextUtils.isEmpty(types)) {
            return true;
        }

        for (String t : types.split(",")) {
            if (t.equals(requestType) || t.equals("*")) {
                return true;
            }
        }
        return false;
    }

    public String getMmscUrl() {
        return mServiceCenter;
    }

    public String getProxyAddress() {
        return mProxyAddress;
    }

    public int getProxyPort() {
        return mProxyPort;
    }

    public boolean isProxySet() {
        return (mProxyAddress != null) && (mProxyAddress.trim().length() != 0);
    }
}
