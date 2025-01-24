package com.android.mms.transaction;

import static com.android.mms.transaction.TransactionState.FAILED;
import static com.android.mms.transaction.TransactionState.INITIALIZED;
import static com.android.mms.transaction.TransactionState.SUCCESS;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF;
import static com.google.android.mms.pdu_alt.PduHeaders.STATUS_DEFERRED;
import static com.google.android.mms.pdu_alt.PduHeaders.STATUS_RETRIEVED;
import static com.google.android.mms.pdu_alt.PduHeaders.STATUS_UNRECOGNIZED;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.provider.Telephony.Threads;
import android.telephony.TelephonyManager;

import com.android.mms.MmsConfig;
import com.android.mms.util.DownloadManager;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.NotifyRespInd;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.RetrieveConf;
import com.klinker.android.send_message.BroadcastUtils;

import java.io.IOException;

import timber.log.Timber;

public class NotificationTransaction extends Transaction implements Runnable {
    private static final boolean LOCAL_LOGV = false;
    private final NotificationInd mNotificationInd;
    private Uri mUri;
    private String mContentLocation;

    public NotificationTransaction(
            Context context, int serviceId,
            TransactionSettings connectionSettings, String uriString) {
        super(context, serviceId, connectionSettings);

        mUri = Uri.parse(uriString);

        try {
            mNotificationInd = (NotificationInd)
                    PduPersister.getPduPersister(context).load(mUri);
        } catch (MmsException e) {
            Timber.e(e, "Failed to load NotificationInd from: " + uriString);
            throw new IllegalArgumentException();
        }

        mContentLocation = new String(mNotificationInd.getContentLocation());
        mId = mContentLocation;

        attach(RetryScheduler.getInstance(context));
    }

    public NotificationTransaction(
            Context context, int serviceId,
            TransactionSettings connectionSettings, NotificationInd ind) {
        super(context, serviceId, connectionSettings);

        try {
            mUri = PduPersister.getPduPersister(context).persist(ind, Inbox.CONTENT_URI,
                    PduPersister.DUMMY_THREAD_ID, !allowAutoDownload(mContext), true, null);
        } catch (MmsException e) {
            Timber.e(e, "Failed to save NotificationInd in constructor.");
            throw new IllegalArgumentException();
        }

        mNotificationInd = ind;
        mId = new String(mNotificationInd.getContentLocation());
    }

    public static boolean allowAutoDownload(Context context) {
        try {
            Looper.prepare();
        } catch (Exception e) {
        }
        boolean autoDownload = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("auto_download_mms", true);
        boolean dataSuspended = (((TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE)).getDataState() ==
                TelephonyManager.DATA_SUSPENDED);
        return autoDownload && !dataSuspended;
    }

    @Override
    public void process() {
        new Thread(this, "NotificationTransaction").start();
    }

    public void run() {
        try {
            Looper.prepare();
        } catch (Exception e) {
        }
        DownloadManager.init(mContext);
        DownloadManager downloadManager = DownloadManager.getInstance();
        boolean autoDownload = allowAutoDownload(mContext);
        try {
            if (LOCAL_LOGV) {
                Timber.v("Notification transaction launched: " + this);
            }

            int status = STATUS_DEFERRED;
            if (!autoDownload) {
                downloadManager.markState(mUri, DownloadManager.STATE_UNSTARTED);
                sendNotifyRespInd(status);
                return;
            }

            downloadManager.markState(mUri, DownloadManager.STATE_DOWNLOADING);

            if (LOCAL_LOGV) {
                Timber.v("Content-Location: " + mContentLocation);
            }

            byte[] retrieveConfData = null;
            try {
                retrieveConfData = getPdu(mContentLocation);
            } catch (IOException e) {
                mTransactionState.setState(FAILED);
            }

            if (retrieveConfData != null) {
                GenericPdu pdu = new PduParser(retrieveConfData).parse();
                if ((pdu == null) || (pdu.getMessageType() != MESSAGE_TYPE_RETRIEVE_CONF)) {
                    Timber.e("Invalid M-RETRIEVE.CONF PDU. " +
                            (pdu != null ? "message type: " + pdu.getMessageType() : "null pdu"));
                    mTransactionState.setState(FAILED);
                    status = STATUS_UNRECOGNIZED;
                } else {
                    PduPersister p = PduPersister.getPduPersister(mContext);
                    Uri uri = p.persist(pdu, Inbox.CONTENT_URI, PduPersister.DUMMY_THREAD_ID,
                            true, true, null);

                    RetrieveConf retrieveConf = (RetrieveConf) pdu;

                    ContentValues values = new ContentValues(2);
                    values.put(Mms.DATE, System.currentTimeMillis() / 1000L);
                    try {
                        values.put(Mms.DATE_SENT, retrieveConf.getDate());
                    } catch (Exception ignored) {
                    }
                    SqliteWrapper.update(mContext, mContext.getContentResolver(),
                            uri, values, null, null);

                    SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                            mUri, null, null);
                    Timber.v("NotificationTransaction received new mms message: " + uri);
                    SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                            Threads.OBSOLETE_THREADS_URI, null, null);

                    mUri = uri;
                    status = STATUS_RETRIEVED;
                    BroadcastUtils.sendExplicitBroadcast(
                            mContext,
                            new Intent(),
                            com.klinker.android.send_message.Transaction.NOTIFY_OF_MMS);
                }
            }

            if (LOCAL_LOGV) {
                Timber.v("status=0x" + Integer.toHexString(status));
            }

            switch (status) {
                case STATUS_RETRIEVED:
                    mTransactionState.setState(SUCCESS);
                    break;
                case STATUS_DEFERRED:
                    if (mTransactionState.getState() == INITIALIZED) {
                        mTransactionState.setState(SUCCESS);
                    }
                    break;
            }

            sendNotifyRespInd(status);
        } catch (Throwable t) {
            Timber.e(t, "error");
        } finally {
            mTransactionState.setContentUri(mUri);
            if (!autoDownload) {
                mTransactionState.setState(SUCCESS);
            }
            if (mTransactionState.getState() != SUCCESS) {
                mTransactionState.setState(FAILED);
                Timber.e("NotificationTransaction failed.");
            }
            notifyObservers();
        }
    }

    private void sendNotifyRespInd(int status) throws MmsException, IOException {
        NotifyRespInd notifyRespInd = new NotifyRespInd(
                PduHeaders.CURRENT_MMS_VERSION,
                mNotificationInd.getTransactionId(),
                status);

        if (MmsConfig.getNotifyWapMMSC()) {
            sendPdu(new PduComposer(mContext, notifyRespInd).make(), mContentLocation);
        } else {
            sendPdu(new PduComposer(mContext, notifyRespInd).make());
        }
    }

    @Override
    public int getType() {
        return NOTIFICATION_TRANSACTION;
    }
}
