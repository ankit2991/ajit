package com.android.mms.transaction;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.mms.util.RateController;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.klinker.android.send_message.BroadcastUtils;
import com.klinker.android.send_message.R;
import com.klinker.android.send_message.Utils;

import java.io.IOException;
import java.util.ArrayList;

import timber.log.Timber;

public class TransactionService extends Service implements Observer {

    public static final String TRANSACTION_COMPLETED_ACTION =
            "android.intent.action.TRANSACTION_COMPLETED_ACTION";

    public static final String ACTION_ONALARM = "android.intent.action.ACTION_ONALARM";

    public static final String ACTION_ENABLE_AUTO_RETRIEVE
            = "android.intent.action.ACTION_ENABLE_AUTO_RETRIEVE";

    public static final String STATE = "state";

    public static final String STATE_URI = "uri";

    private static final int EVENT_TRANSACTION_REQUEST = 1;
    private static final int EVENT_CONTINUE_MMS_CONNECTIVITY = 3;
    private static final int EVENT_HANDLE_NEXT_PENDING_TRANSACTION = 4;
    private static final int EVENT_NEW_INTENT = 5;
    private static final int EVENT_QUIT = 100;

    private static final int TOAST_MSG_QUEUED = 1;
    private static final int TOAST_DOWNLOAD_LATER = 2;
    private static final int TOAST_NO_APN = 3;
    private static final int TOAST_NONE = -1;

    private static final int APN_EXTENSION_WAIT = 30 * 1000;
    private final ArrayList<Transaction> mProcessing = new ArrayList<Transaction>();
    private final ArrayList<Transaction> mPending = new ArrayList<Transaction>();
    private final boolean lollipopReceiving = false;
    public Handler mToastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String str = null;

            if (msg.what == TOAST_MSG_QUEUED) {
                str = getString(R.string.message_queued);
            } else if (msg.what == TOAST_DOWNLOAD_LATER) {
                str = getString(R.string.download_later);
            } else if (msg.what == TOAST_NO_APN) {
                str = getString(R.string.no_apn);
            }

            if (str != null) {
                Toast.makeText(TransactionService.this, str,
                        Toast.LENGTH_LONG).show();
            }
        }
    };
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private ConnectivityManager mConnMgr;
    private ConnectivityBroadcastReceiver mReceiver;
    private boolean mobileDataEnabled;
    private PowerManager.WakeLock mWakeLock;

    private static boolean isTransientFailure(int type) {
        return type > MmsSms.NO_ERROR && type < MmsSms.ERR_TYPE_GENERIC_PERMANENT;
    }

    @Override
    public void onCreate() {
        Timber.v("Creating TransactionService");

        if (!Utils.isDefaultSmsApp(this)) {
            Timber.v("not default app, so exiting");
            stopSelf();
            return;
        }

        initServiceHandler();

        mReceiver = new ConnectivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }

    private void initServiceHandler() {
        HandlerThread thread = new HandlerThread("TransactionService");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {

            if (mServiceHandler == null) {
                initServiceHandler();
            }

            Message msg = mServiceHandler.obtainMessage(EVENT_NEW_INTENT);
            msg.arg1 = startId;
            msg.obj = intent;
            mServiceHandler.sendMessage(msg);
        }
        return Service.START_NOT_STICKY;
    }

    private boolean isNetworkAvailable() {
        if (mConnMgr == null) {
            return false;
        } else if (Utils.isMmsOverWifiEnabled(this)) {
            NetworkInfo niWF = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return (niWF != null && niWF.isConnected());
        } else {
            NetworkInfo ni = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
            return (ni != null && ni.isAvailable());
        }
    }

    public void onNewIntent(Intent intent, int serviceId) {
        try {
            mobileDataEnabled = Utils.isMobileDataEnabled(this);
        } catch (Exception e) {
            mobileDataEnabled = true;
        }
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!mobileDataEnabled) {
            Utils.setMobileDataEnabled(this, true);
        }
        if (mConnMgr == null) {
            endMmsConnectivity();
            stopSelf(serviceId);
            return;
        }

        boolean noNetwork = !isNetworkAvailable();

        Timber.v("onNewIntent: serviceId: " + serviceId + ": " + intent.getExtras() + " intent=" + intent);
        Timber.v("    networkAvailable=" + !noNetwork);

        String action = intent.getAction();
        if (ACTION_ONALARM.equals(action) || ACTION_ENABLE_AUTO_RETRIEVE.equals(action) ||
                (intent.getExtras() == null)) {
            // Scan database to find all pending operations.
            Cursor cursor = PduPersister.getPduPersister(this).getPendingMessages(
                    System.currentTimeMillis());
            if (cursor != null) {
                try {
                    int count = cursor.getCount();

                    Timber.v("onNewIntent: cursor.count=" + count + " action=" + action);

                    if (count == 0) {
                        Timber.v("onNewIntent: no pending messages. Stopping service.");
                        RetryScheduler.setRetryAlarm(this);
                        stopSelfIfIdle(serviceId);
                        return;
                    }

                    int columnIndexOfMsgId = cursor.getColumnIndexOrThrow(PendingMessages.MSG_ID);
                    int columnIndexOfMsgType = cursor.getColumnIndexOrThrow(
                            PendingMessages.MSG_TYPE);

                    while (cursor.moveToNext()) {
                        int msgType = cursor.getInt(columnIndexOfMsgType);
                        int transactionType = getTransactionType(msgType);

                        try {
                            Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI,
                                    cursor.getLong(columnIndexOfMsgId));
                            com.android.mms.transaction.DownloadManager.getInstance().
                                    downloadMultimediaMessage(this, PushReceiver.getContentLocation(this, uri), uri, false, Utils.getDefaultSubscriptionId());

                            break;
                        } catch (MmsException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    cursor.close();
                }
            } else {
                Timber.v("onNewIntent: no pending messages. Stopping service.");
                RetryScheduler.setRetryAlarm(this);
                stopSelfIfIdle(serviceId);
            }
        } else {
            Timber.v("onNewIntent: launch transaction...");
            TransactionBundle args = new TransactionBundle(intent.getExtras());
            launchTransaction(serviceId, args, noNetwork);
        }
    }

    private void stopSelfIfIdle(int startId) {
        synchronized (mProcessing) {
            if (mProcessing.isEmpty() && mPending.isEmpty()) {
                Timber.v("stopSelfIfIdle: STOP!");

                stopSelf(startId);
            }
        }
    }

    private int getTransactionType(int msgType) {
        switch (msgType) {
            case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
                return Transaction.RETRIEVE_TRANSACTION;
            case PduHeaders.MESSAGE_TYPE_READ_REC_IND:
                return Transaction.READREC_TRANSACTION;
            case PduHeaders.MESSAGE_TYPE_SEND_REQ:
                return Transaction.SEND_TRANSACTION;
            default:
                Timber.w("Unrecognized MESSAGE_TYPE: " + msgType);
                return -1;
        }
    }

    private void launchTransaction(int serviceId, TransactionBundle txnBundle, boolean noNetwork) {
        if (noNetwork) {
            Timber.w("launchTransaction: no network error!");
            onNetworkUnavailable(serviceId, txnBundle.getTransactionType());
            return;
        }
        Message msg = mServiceHandler.obtainMessage(EVENT_TRANSACTION_REQUEST);
        msg.arg1 = serviceId;
        msg.obj = txnBundle;

        Timber.v("launchTransaction: sending message " + msg);
        mServiceHandler.sendMessage(msg);
    }

    private void onNetworkUnavailable(int serviceId, int transactionType) {
        Timber.v("onNetworkUnavailable: sid=" + serviceId + ", type=" + transactionType);

        int toastType = TOAST_NONE;
        if (transactionType == Transaction.RETRIEVE_TRANSACTION) {
            toastType = TOAST_DOWNLOAD_LATER;
        } else if (transactionType == Transaction.SEND_TRANSACTION) {
            toastType = TOAST_MSG_QUEUED;
        }
        if (toastType != TOAST_NONE) {
            mToastHandler.sendEmptyMessage(toastType);
        }
        stopSelf(serviceId);
    }

    @Override
    public void onDestroy() {
        Timber.v("Destroying TransactionService");
        if (!mPending.isEmpty()) {
            Timber.w("TransactionService exiting with transaction still pending");
        }

        releaseWakeLock();

        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
        }

        mServiceHandler.sendEmptyMessage(EVENT_QUIT);

        if (!mobileDataEnabled && !lollipopReceiving) {
            Timber.v("disabling mobile data");
            Utils.setMobileDataEnabled(TransactionService.this, false);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void update(Observable observable) {
        Transaction transaction = (Transaction) observable;
        int serviceId = transaction.getServiceId();

        Timber.v("update transaction " + serviceId);

        try {
            synchronized (mProcessing) {
                mProcessing.remove(transaction);
                if (mPending.size() > 0) {
                    Timber.v("update: handle next pending transaction...");
                    Message msg = mServiceHandler.obtainMessage(
                            EVENT_HANDLE_NEXT_PENDING_TRANSACTION,
                            transaction.getConnectionSettings());
                    mServiceHandler.sendMessage(msg);
                } else if (mProcessing.isEmpty()) {
                    Timber.v("update: endMmsConnectivity");
                    endMmsConnectivity();
                } else {
                    Timber.v("update: mProcessing is not empty");
                }
            }

            Intent intent = new Intent(TRANSACTION_COMPLETED_ACTION);
            TransactionState state = transaction.getState();
            int result = state.getState();
            intent.putExtra(STATE, result);

            switch (result) {
                case TransactionState.SUCCESS:
                    Timber.v("Transaction complete: " + serviceId);

                    intent.putExtra(STATE_URI, state.getContentUri());

                    switch (transaction.getType()) {
                        case Transaction.NOTIFICATION_TRANSACTION:
                        case Transaction.RETRIEVE_TRANSACTION:
                            break;
                        case Transaction.SEND_TRANSACTION:
                            RateController.init(getApplicationContext());
                            RateController.getInstance().update();
                            break;
                    }
                    break;
                case TransactionState.FAILED:
                    Timber.v("Transaction failed: " + serviceId);
                    break;
                default:
                    Timber.v("Transaction state unknown: " + serviceId + " " + result);
                    break;
            }

            Timber.v("update: broadcast transaction result " + result);
            BroadcastUtils.sendExplicitBroadcast(this, intent, TRANSACTION_COMPLETED_ACTION);
        } finally {
            transaction.detach(this);
            stopSelfIfIdle(serviceId);
        }
    }

    private synchronized void createWakeLock() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MMS_@_:Connectivity");
            mWakeLock.setReferenceCounted(false);
        }
    }

    private void acquireWakeLock() {
        Timber.v("mms acquireWakeLock");
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            Timber.v("mms releaseWakeLock");
            mWakeLock.release();
        }
    }

    protected int beginMmsConnectivity() throws IOException {
        Timber.v("beginMmsConnectivity");
        createWakeLock();

        if (Utils.isMmsOverWifiEnabled(this)) {
            NetworkInfo niWF = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if ((niWF != null) && (niWF.isConnected())) {
                Timber.v("beginMmsConnectivity: Wifi active");
                return 0;
            }
        }
        /*
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_MMS);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);

        NetworkRequest networkRequest = builder.build();
        mConnMgr.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network){
                super.onAvailable(network);
                dialog.dismiss();
                sendNormalMms();
            }
        });*/

        int result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");

        Timber.v("beginMmsConnectivity: result=" + result);

        switch (result) {
            case 0:
            case 1:
                acquireWakeLock();
                return result;
        }

        throw new IOException("Cannot establish MMS connectivity");
    }

    protected void endMmsConnectivity() {
        try {
            Timber.v("endMmsConnectivity");

            mServiceHandler.removeMessages(EVENT_CONTINUE_MMS_CONNECTIVITY);
            if (mConnMgr != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                mConnMgr.stopUsingNetworkFeature(
                        ConnectivityManager.TYPE_MOBILE,
                        "enableMMS");
            }
        } finally {
            releaseWakeLock();
        }
    }

    private void renewMmsConnectivity() {
        mServiceHandler.sendMessageDelayed(
                mServiceHandler.obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY),
                APN_EXTENSION_WAIT);
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        private String decodeMessage(Message msg) {
            if (msg.what == EVENT_QUIT) {
                return "EVENT_QUIT";
            } else if (msg.what == EVENT_CONTINUE_MMS_CONNECTIVITY) {
                return "EVENT_CONTINUE_MMS_CONNECTIVITY";
            } else if (msg.what == EVENT_TRANSACTION_REQUEST) {
                return "EVENT_TRANSACTION_REQUEST";
            } else if (msg.what == EVENT_HANDLE_NEXT_PENDING_TRANSACTION) {
                return "EVENT_HANDLE_NEXT_PENDING_TRANSACTION";
            } else if (msg.what == EVENT_NEW_INTENT) {
                return "EVENT_NEW_INTENT";
            }
            return "unknown message.what";
        }

        private String decodeTransactionType(int transactionType) {
            if (transactionType == Transaction.NOTIFICATION_TRANSACTION) {
                return "NOTIFICATION_TRANSACTION";
            } else if (transactionType == Transaction.RETRIEVE_TRANSACTION) {
                return "RETRIEVE_TRANSACTION";
            } else if (transactionType == Transaction.SEND_TRANSACTION) {
                return "SEND_TRANSACTION";
            } else if (transactionType == Transaction.READREC_TRANSACTION) {
                return "READREC_TRANSACTION";
            }
            return "invalid transaction type";
        }

        @Override
        public void handleMessage(Message msg) {
            Timber.v("Handling incoming message: " + msg + " = " + decodeMessage(msg));

            Transaction transaction = null;

            switch (msg.what) {
                case EVENT_NEW_INTENT:
                    onNewIntent((Intent) msg.obj, msg.arg1);
                    break;

                case EVENT_QUIT:
                    getLooper().quit();
                    return;

                case EVENT_CONTINUE_MMS_CONNECTIVITY:
                    synchronized (mProcessing) {
                        if (mProcessing.isEmpty()) {
                            return;
                        }
                    }

                    Timber.v("handle EVENT_CONTINUE_MMS_CONNECTIVITY event...");

                    try {
                        int result = beginMmsConnectivity();
                        if (result != 0) {
                            Timber.v("Extending MMS connectivity returned " + result +
                                    " instead of APN_ALREADY_ACTIVE");
                            return;
                        }
                    } catch (IOException e) {
                        Timber.w("Attempt to extend use of MMS connectivity failed");
                        return;
                    }

                    renewMmsConnectivity();
                    return;

                case EVENT_TRANSACTION_REQUEST:
                    int serviceId = msg.arg1;
                    try {
                        TransactionBundle args = (TransactionBundle) msg.obj;
                        TransactionSettings transactionSettings;

                        Timber.v("EVENT_TRANSACTION_REQUEST MmscUrl=" +
                                args.getMmscUrl() + " proxy port: " + args.getProxyAddress());

                        String mmsc = args.getMmscUrl();
                        if (mmsc != null) {
                            transactionSettings = new TransactionSettings(
                                    mmsc, args.getProxyAddress(), args.getProxyPort());
                        } else {
                            transactionSettings = new TransactionSettings(
                                    TransactionService.this, null);
                        }

                        int transactionType = args.getTransactionType();

                        Timber.v("handle EVENT_TRANSACTION_REQUEST: transactionType=" +
                                transactionType + " " + decodeTransactionType(transactionType));

                        switch (transactionType) {
                            case Transaction.NOTIFICATION_TRANSACTION:
                                String uri = args.getUri();
                                if (uri != null) {
                                    transaction = new NotificationTransaction(
                                            TransactionService.this, serviceId,
                                            transactionSettings, uri);
                                } else {
                                    byte[] pushData = args.getPushData();
                                    PduParser parser = new PduParser(pushData);
                                    GenericPdu ind = parser.parse();

                                    int type = PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
                                    if ((ind != null) && (ind.getMessageType() == type)) {
                                        transaction = new NotificationTransaction(
                                                TransactionService.this, serviceId,
                                                transactionSettings, (NotificationInd) ind);
                                    } else {
                                        Timber.e("Invalid PUSH data.");
                                        transaction = null;
                                        return;
                                    }
                                }
                                break;
                            case Transaction.RETRIEVE_TRANSACTION:
                                transaction = new RetrieveTransaction(
                                        TransactionService.this, serviceId,
                                        transactionSettings, args.getUri());

                                Uri u = Uri.parse(args.getUri());
                                com.android.mms.transaction.DownloadManager.getInstance().
                                        downloadMultimediaMessage(TransactionService.this,
                                                ((RetrieveTransaction) transaction).getContentLocation(TransactionService.this, u), u, false, Utils.getDefaultSubscriptionId());
                                return;


                        }

                        if (!processTransaction(transaction)) {
                            transaction = null;
                            return;
                        }

                        Timber.v("Started processing of incoming message: " + msg);
                    } catch (Exception ex) {
                        Timber.w(ex, "Exception occurred while handling message: " + msg);

                        if (transaction != null) {
                            try {
                                transaction.detach(TransactionService.this);
                                if (mProcessing.contains(transaction)) {
                                    synchronized (mProcessing) {
                                        mProcessing.remove(transaction);
                                    }
                                }
                            } catch (Throwable t) {
                                Timber.e(t, "Unexpected Throwable.");
                            } finally {
                                transaction = null;
                            }
                        }
                    } finally {
                        if (transaction == null) {
                            Timber.v("Transaction was null. Stopping self: " + serviceId);
                            endMmsConnectivity();
                            stopSelf(serviceId);
                        }
                    }
                    return;
                case EVENT_HANDLE_NEXT_PENDING_TRANSACTION:
                    processPendingTransaction(transaction, (TransactionSettings) msg.obj);
                    return;
                default:
                    Timber.w("what=" + msg.what);
                    return;
            }
        }

        public void markAllPendingTransactionsAsFailed() {
            synchronized (mProcessing) {
                while (mPending.size() != 0) {
                    Transaction transaction = mPending.remove(0);
                    transaction.mTransactionState.setState(TransactionState.FAILED);
                    if (transaction instanceof SendTransaction) {
                        Uri uri = ((SendTransaction) transaction).mSendReqURI;
                        transaction.mTransactionState.setContentUri(uri);
                        int respStatus = PduHeaders.RESPONSE_STATUS_ERROR_NETWORK_PROBLEM;
                        ContentValues values = new ContentValues(1);
                        values.put(Mms.RESPONSE_STATUS, respStatus);

                        SqliteWrapper.update(TransactionService.this,
                                TransactionService.this.getContentResolver(),
                                uri, values, null, null);
                    }
                    transaction.notifyObservers();
                }
            }
        }

        public void processPendingTransaction(Transaction transaction,
                                              TransactionSettings settings) {

            Timber.v("processPendingTxn: transaction=" + transaction);

            int numProcessTransaction = 0;
            synchronized (mProcessing) {
                if (mPending.size() != 0) {
                    transaction = mPending.remove(0);
                }
                numProcessTransaction = mProcessing.size();
            }

            if (transaction != null) {
                if (settings != null) {
                    transaction.setConnectionSettings(settings);
                }

                try {
                    int serviceId = transaction.getServiceId();

                    Timber.v("processPendingTxn: process " + serviceId);

                    if (processTransaction(transaction)) {
                        Timber.v("Started deferred processing of transaction  " + transaction);
                    } else {
                        transaction = null;
                        stopSelf(serviceId);
                    }
                } catch (IOException e) {
                    Timber.w(e, e.getMessage());
                }
            } else {
                if (numProcessTransaction == 0) {
                    Timber.v("processPendingTxn: no more transaction, endMmsConnectivity");
                    endMmsConnectivity();
                }
            }
        }

        private boolean processTransaction(Transaction transaction) throws IOException {
            synchronized (mProcessing) {
                for (Transaction t : mPending) {
                    if (t.isEquivalent(transaction)) {
                        Timber.v("Transaction already pending: " + transaction.getServiceId());
                        return true;
                    }
                }
                for (Transaction t : mProcessing) {
                    if (t.isEquivalent(transaction)) {
                        Timber.v("Duplicated transaction: " + transaction.getServiceId());
                        return true;
                    }
                }

                Timber.v("processTransaction: call beginMmsConnectivity...");
                int connectivityResult = beginMmsConnectivity();
                if (connectivityResult == 1) {
                    mPending.add(transaction);
                    Timber.v("processTransaction: connResult=APN_REQUEST_STARTED, " +
                            "defer transaction pending MMS connectivity");
                    return true;
                }
                if (mProcessing.size() > 0) {
                    Timber.v("Adding transaction to 'mPending' list: " + transaction);
                    mPending.add(transaction);
                    return true;
                } else {
                    Timber.v("Adding transaction to 'mProcessing' list: " + transaction);
                    mProcessing.add(transaction);
                }
            }

            sendMessageDelayed(obtainMessage(EVENT_CONTINUE_MMS_CONNECTIVITY), APN_EXTENSION_WAIT);

            Timber.v("processTransaction: starting transaction " + transaction);

            transaction.attach(TransactionService.this);
            transaction.process();
            return true;
        }
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Timber.w("ConnectivityBroadcastReceiver.onReceive() action: " + action);

            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
            }

            NetworkInfo mmsNetworkInfo = null;

            if (mConnMgr != null && Utils.isMobileDataEnabled(context)) {
                mmsNetworkInfo = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
            } else {
                Timber.v("mConnMgr is null, bail");
            }

            Timber.v("Handle ConnectivityBroadcastReceiver.onReceive(): " + mmsNetworkInfo);

            if (mmsNetworkInfo == null) {
                Timber.v("mms type is null or mobile data is turned off, bail");
            } else {
                if ("2GVoiceCallEnded".equals(mmsNetworkInfo.getReason())) {
                    Timber.v("   reason is " + "2GVoiceCallEnded" + ", retrying mms connectivity");
                    renewMmsConnectivity();
                    return;
                }

                if (mmsNetworkInfo.isConnected()) {
                    TransactionSettings settings = new TransactionSettings(
                            TransactionService.this, mmsNetworkInfo.getExtraInfo());
                    if (TextUtils.isEmpty(settings.getMmscUrl())) {
                        Timber.v("   empty MMSC url, bail");
                        BroadcastUtils.sendExplicitBroadcast(
                                TransactionService.this,
                                new Intent(),
                                com.klinker.android.send_message.Transaction.MMS_ERROR);
                        mServiceHandler.markAllPendingTransactionsAsFailed();
                        endMmsConnectivity();
                        stopSelf();
                        return;
                    }
                    mServiceHandler.processPendingTransaction(null, settings);
                } else {
                    Timber.v("   TYPE_MOBILE_MMS not connected, bail");

                    if (mmsNetworkInfo.isAvailable()) {
                        Timber.v("   retrying mms connectivity for it's available");
                        renewMmsConnectivity();
                    }
                }
            }
        }
    }
}
