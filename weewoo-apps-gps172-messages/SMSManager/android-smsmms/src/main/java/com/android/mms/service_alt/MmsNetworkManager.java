package com.android.mms.service_alt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.SSLCertificateSocketFactory;
import android.os.Build;
import android.os.SystemClock;

import com.android.mms.service_alt.exception.MmsNetworkException;
import com.squareup.okhttp.ConnectionPool;

import java.net.InetAddress;
import java.net.UnknownHostException;

import timber.log.Timber;

public class MmsNetworkManager implements com.squareup.okhttp.internal.Network {

    private static final int NETWORK_REQUEST_TIMEOUT_MILLIS = 60 * 1000;
    private static final int NETWORK_ACQUIRE_TIMEOUT_MILLIS =
            NETWORK_REQUEST_TIMEOUT_MILLIS + (5 * 1000);

    private static final boolean httpKeepAlive =
            Boolean.parseBoolean(System.getProperty("http.keepAlive", "true"));
    private static final int httpMaxConnections =
            httpKeepAlive ? Integer.parseInt(System.getProperty("http.maxConnections", "5")) : 0;
    private static final long httpKeepAliveDurationMs =
            Long.parseLong(System.getProperty("http.keepAliveDuration", "300000"));  // 5 minutes.
    private static final InetAddress[] EMPTY_ADDRESS_ARRAY = new InetAddress[0];
    private final Context mContext;
    private final int mSubId;
    private Network mNetwork;
    private int mMmsRequestCount;
    private NetworkRequest mNetworkRequest;
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    private volatile ConnectivityManager mConnectivityManager;
    private ConnectionPool mConnectionPool;
    private MmsHttpClient mMmsHttpClient;
    private boolean permissionError = false;

    public MmsNetworkManager(Context context, int subId) {
        mContext = context;
        mNetworkCallback = null;
        mNetwork = null;
        mMmsRequestCount = 0;
        mConnectivityManager = null;
        mConnectionPool = null;
        mMmsHttpClient = null;
        mSubId = subId;

        if (!MmsRequest.useWifi(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                mNetworkRequest = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_MMS)
                        .setNetworkSpecifier(Integer.toString(mSubId))
                        .build();
            } else {
                mNetworkRequest = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_MMS)
                        .build();
            }
        } else {
            mNetworkRequest = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
        }

        MmsConfigManager.getInstance().init(context);
    }

    public Network acquireNetwork() throws MmsNetworkException {
        synchronized (this) {
            mMmsRequestCount += 1;
            if (mNetwork != null) {
                Timber.d("MmsNetworkManager: already available");
                return mNetwork;
            }
            Timber.d("MmsNetworkManager: start new network request");
            newRequest();
            final long shouldEnd = SystemClock.elapsedRealtime() + NETWORK_ACQUIRE_TIMEOUT_MILLIS;
            long waitTime = NETWORK_ACQUIRE_TIMEOUT_MILLIS;
            while (waitTime > 0) {
                try {
                    this.wait(waitTime);
                } catch (InterruptedException e) {
                    Timber.w("MmsNetworkManager: acquire network wait interrupted");
                }
                if (mNetwork != null || permissionError) {
                    return mNetwork;
                }
                waitTime = shouldEnd - SystemClock.elapsedRealtime();
            }
            Timber.d("MmsNetworkManager: timed out");
            releaseRequestLocked(mNetworkCallback);
            throw new MmsNetworkException("Acquiring network timed out");
        }
    }

    public void releaseNetwork() {
        synchronized (this) {
            if (mMmsRequestCount > 0) {
                mMmsRequestCount -= 1;
                Timber.d("MmsNetworkManager: release, count=" + mMmsRequestCount);
                if (mMmsRequestCount < 1) {
                    releaseRequestLocked(mNetworkCallback);
                }
            }
        }
    }

    private void newRequest() {
        final ConnectivityManager connectivityManager = getConnectivityManager();
        mNetworkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Timber.d("NetworkCallbackListener.onAvailable: network=" + network);
                synchronized (MmsNetworkManager.this) {
                    mNetwork = network;
                    MmsNetworkManager.this.notifyAll();
                }
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Timber.d("NetworkCallbackListener.onLost: network=" + network);
                synchronized (MmsNetworkManager.this) {
                    releaseRequestLocked(this);
                    MmsNetworkManager.this.notifyAll();
                }
            }
        };

        try {
            connectivityManager.requestNetwork(
                    mNetworkRequest, mNetworkCallback);
        } catch (SecurityException e) {
            Timber.e(e, "permission exception... skipping it for testing purposes");
            permissionError = true;
        }
    }

    private void releaseRequestLocked(ConnectivityManager.NetworkCallback callback) {
        if (callback != null) {
            final ConnectivityManager connectivityManager = getConnectivityManager();

            try {
                connectivityManager.unregisterNetworkCallback(callback);
            } catch (Exception e) {
                Timber.e(e, "couldn't unregister");
            }
        }
        resetLocked();
    }

    private void resetLocked() {
        mNetworkCallback = null;
        mNetwork = null;
        mMmsRequestCount = 0;
        mConnectionPool = null;
        mMmsHttpClient = null;
    }

    @Override
    public InetAddress[] resolveInetAddresses(String host) throws UnknownHostException {
        Network network = null;
        synchronized (this) {
            if (mNetwork == null) {
                return EMPTY_ADDRESS_ARRAY;
            }
            network = mNetwork;
        }
        return network.getAllByName(host);
    }

    private ConnectivityManager getConnectivityManager() {
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager) mContext.getSystemService(
                    Context.CONNECTIVITY_SERVICE);
        }
        return mConnectivityManager;
    }

    private ConnectionPool getOrCreateConnectionPoolLocked() {
        if (mConnectionPool == null) {
            mConnectionPool = new ConnectionPool(httpMaxConnections, httpKeepAliveDurationMs);
        }
        return mConnectionPool;
    }

    public MmsHttpClient getOrCreateHttpClient() {
        synchronized (this) {
            if (mMmsHttpClient == null) {
                if (mNetwork != null) {
                    mMmsHttpClient = new MmsHttpClient(
                            mContext,
                            mNetwork.getSocketFactory(),
                            MmsNetworkManager.this,
                            getOrCreateConnectionPoolLocked());
                } else if (permissionError) {
                    mMmsHttpClient = new MmsHttpClient(
                            mContext,
                            new SSLCertificateSocketFactory(NETWORK_REQUEST_TIMEOUT_MILLIS),
                            MmsNetworkManager.this,
                            getOrCreateConnectionPoolLocked());
                }
            }
            return mMmsHttpClient;
        }
    }

    public String getApnName() {
        Network network = null;
        synchronized (this) {
            if (mNetwork == null) {
                Timber.d("MmsNetworkManager: getApnName: network not available");
                mNetworkRequest = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build();
                return null;
            }
            network = mNetwork;
        }
        String apnName = null;
        final ConnectivityManager connectivityManager = getConnectivityManager();
        NetworkInfo mmsNetworkInfo = connectivityManager.getNetworkInfo(network);
        if (mmsNetworkInfo != null) {
            apnName = mmsNetworkInfo.getExtraInfo();
        }
        Timber.d("MmsNetworkManager: getApnName: " + apnName);
        return apnName;
    }

}
