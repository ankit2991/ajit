package com.klinker.android.send_message;

import static com.google.android.mms.pdu_alt.PduHeaders.STATUS_RETRIEVED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.telephony.SmsManager;

import com.android.mms.service_alt.DownloadRequest;
import com.android.mms.service_alt.MmsConfig;
import com.android.mms.transaction.DownloadManager;
import com.android.mms.transaction.HttpUtils;
import com.android.mms.transaction.TransactionSettings;
import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.NotifyRespInd;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.RetrieveConf;
import com.google.android.mms.util_alt.SqliteWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class MmsReceivedReceiver extends BroadcastReceiver {
    public static final String MMS_RECEIVED = "com.klinker.android.messaging.MMS_RECEIVED";
    public static final String EXTRA_FILE_PATH = "file_path";
    public static final String EXTRA_LOCATION_URL = "location_url";
    public static final String EXTRA_TRIGGER_PUSH = "trigger_push";
    public static final String EXTRA_URI = "notification_ind_uri";

    private static final String LOCATION_SELECTION =
            Telephony.Mms.MESSAGE_TYPE + "=? AND " + Telephony.Mms.CONTENT_LOCATION + " =?";

    private static final ExecutorService RECEIVE_NOTIFICATION_EXECUTOR = Executors.newSingleThreadExecutor();

    private static NotificationInd getNotificationInd(Context context, Intent intent) throws MmsException {
        return (NotificationInd) PduPersister.getPduPersister(context).load(intent.getParcelableExtra(EXTRA_URI));
    }

    public MmscInformation getMmscInfoForReceptionAck() {
        return null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.v("MMS has finished downloading, persisting it to the database");

        String path = intent.getStringExtra(EXTRA_FILE_PATH);
        Timber.v(path);

        FileInputStream reader = null;
        Uri messageUri = null;
        String errorMessage = null;

        try {
            File mDownloadFile = new File(path);
            final int nBytes = (int) mDownloadFile.length();
            reader = new FileInputStream(mDownloadFile);
            final byte[] response = new byte[nBytes];
            reader.read(response, 0, nBytes);

            List<CommonAsyncTask> tasks = getNotificationTask(context, intent, response);

            messageUri = DownloadRequest.persist(context, response,
                    new MmsConfig.Overridden(new MmsConfig(context), null),
                    intent.getStringExtra(EXTRA_LOCATION_URL),
                    Utils.getDefaultSubscriptionId(), null);

            Timber.v("response saved successfully");
            Timber.v("response length: " + response.length);
            mDownloadFile.delete();

            if (tasks != null) {
                Timber.v("running the common async notifier for download");
                for (CommonAsyncTask task : tasks)
                    task.executeOnExecutor(RECEIVE_NOTIFICATION_EXECUTOR);
            }
        } catch (FileNotFoundException e) {
            errorMessage = "MMS received, file not found exception";
            Timber.e(e, errorMessage);
        } catch (IOException e) {
            errorMessage = "MMS received, io exception";
            Timber.e(e, errorMessage);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    errorMessage = "MMS received, io exception";
                    Timber.e(e, "MMS received, io exception");
                }
            }
        }

        handleHttpError(context, intent);
        DownloadManager.finishDownload(intent.getStringExtra(EXTRA_LOCATION_URL));

        if (messageUri != null) {
            onMessageReceived(messageUri);
        }

        if (errorMessage != null) {
            onError(errorMessage);
        }
    }

    protected void onMessageReceived(Uri messageUri) {
    }

    protected void onError(String error) {

    }

    private void handleHttpError(Context context, Intent intent) {
        final int httpError = intent.getIntExtra(SmsManager.EXTRA_MMS_HTTP_STATUS, 0);
        if (httpError == 404 ||
                httpError == 400) {
            SqliteWrapper.delete(context,
                    context.getContentResolver(),
                    Telephony.Mms.CONTENT_URI,
                    LOCATION_SELECTION,
                    new String[]{
                            Integer.toString(PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND),
                            intent.getStringExtra(EXTRA_LOCATION_URL)
                    });
        }
    }

    private List<CommonAsyncTask> getNotificationTask(Context context, Intent intent, byte[] response) {
        if (response.length == 0) {
            Timber.v("MmsReceivedReceiver.sendNotification blank response");
            return null;
        }

        if (getMmscInfoForReceptionAck() == null) {
            Timber.v("No MMSC information set, so no notification tasks will be able to complete");
            return null;
        }

        final GenericPdu pdu =
                (new PduParser(response, new MmsConfig.Overridden(new MmsConfig(context), null).
                        getSupportMmsContentDisposition())).parse();
        if (pdu == null || !(pdu instanceof RetrieveConf)) {
            Timber.e("MmsReceivedReceiver.sendNotification failed to parse pdu");
            return null;
        }

        try {
            final NotificationInd ind = getNotificationInd(context, intent);
            final MmscInformation mmsc = getMmscInfoForReceptionAck();
            final TransactionSettings transactionSettings = new TransactionSettings(mmsc.mmscUrl, mmsc.mmsProxy, mmsc.proxyPort);

            final List<CommonAsyncTask> responseTasks = new ArrayList<>();
            responseTasks.add(new AcknowledgeIndTask(context, ind, transactionSettings, (RetrieveConf) pdu));
            responseTasks.add(new NotifyRespTask(context, ind, transactionSettings));

            return responseTasks;
        } catch (MmsException e) {
            Timber.e(e, "error");
            return null;
        }
    }

    private static abstract class CommonAsyncTask extends AsyncTask<Void, Void, Void> {
        protected final Context mContext;
        protected final TransactionSettings mTransactionSettings;
        final NotificationInd mNotificationInd;
        final String mContentLocation;

        CommonAsyncTask(Context context, TransactionSettings settings, NotificationInd ind) {
            mContext = context;
            mTransactionSettings = settings;
            mNotificationInd = ind;
            mContentLocation = new String(ind.getContentLocation());
        }

        byte[] sendPdu(byte[] pdu, String mmscUrl) throws IOException, MmsException {
            return sendPdu(SendingProgressTokenManager.NO_TOKEN, pdu, mmscUrl);
        }

        byte[] sendPdu(byte[] pdu) throws IOException, MmsException {
            return sendPdu(SendingProgressTokenManager.NO_TOKEN, pdu,
                    mTransactionSettings.getMmscUrl());
        }

        private byte[] sendPdu(long token, byte[] pdu,
                               String mmscUrl) throws IOException, MmsException {
            if (pdu == null) {
                throw new MmsException();
            }

            if (mmscUrl == null) {
                throw new IOException("Cannot establish route: mmscUrl is null");
            }

            if (com.android.mms.transaction.Transaction.useWifi(mContext)) {
                return HttpUtils.httpConnection(
                        mContext, token,
                        mmscUrl,
                        pdu, HttpUtils.HTTP_POST_METHOD,
                        false, null, 0);
            }

            Utils.ensureRouteToHost(mContext, mmscUrl, mTransactionSettings.getProxyAddress());
            return HttpUtils.httpConnection(
                    mContext, token,
                    mmscUrl,
                    pdu, HttpUtils.HTTP_POST_METHOD,
                    mTransactionSettings.isProxySet(),
                    mTransactionSettings.getProxyAddress(),
                    mTransactionSettings.getProxyPort());
        }
    }

    private static class NotifyRespTask extends CommonAsyncTask {
        NotifyRespTask(Context context, NotificationInd ind, TransactionSettings settings) {
            super(context, settings, ind);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Create the M-NotifyResp.ind
            NotifyRespInd notifyRespInd;
            try {
                notifyRespInd = new NotifyRespInd(
                        PduHeaders.CURRENT_MMS_VERSION,
                        mNotificationInd.getTransactionId(),
                        STATUS_RETRIEVED);

                // Pack M-NotifyResp.ind and send it
                if (com.android.mms.MmsConfig.getNotifyWapMMSC()) {
                    sendPdu(new PduComposer(mContext, notifyRespInd).make(), mContentLocation);
                } else {
                    sendPdu(new PduComposer(mContext, notifyRespInd).make());
                }
            } catch (MmsException | IOException e) {
                Timber.e(e, "error");
            }
            return null;
        }
    }

    private static class AcknowledgeIndTask extends CommonAsyncTask {
        private final RetrieveConf mRetrieveConf;

        AcknowledgeIndTask(Context context, NotificationInd ind, TransactionSettings settings, RetrieveConf rc) {
            super(context, settings, ind);
            mRetrieveConf = rc;
        }

        @Override
        protected Void doInBackground(Void... params) {
            byte[] tranId = mRetrieveConf.getTransactionId();
            if (tranId != null) {
                Timber.v("sending ACK to MMSC: " + mTransactionSettings.getMmscUrl());
                com.google.android.mms.pdu_alt.AcknowledgeInd acknowledgeInd;

                try {
                    acknowledgeInd = new com.google.android.mms.pdu_alt.AcknowledgeInd(
                            PduHeaders.CURRENT_MMS_VERSION, tranId);

                    String lineNumber = Utils.getMyPhoneNumber(mContext);
                    acknowledgeInd.setFrom(new EncodedStringValue(lineNumber));

                    if (com.android.mms.MmsConfig.getNotifyWapMMSC()) {
                        sendPdu(new PduComposer(mContext, acknowledgeInd).make(), mContentLocation);
                    } else {
                        sendPdu(new PduComposer(mContext, acknowledgeInd).make());
                    }
                } catch (IOException | MmsException e) {
                    Timber.e(e, "error");
                }
            }
            return null;
        }
    }

    public static class MmscInformation {
        String mmscUrl;
        String mmsProxy;
        int proxyPort;

        public MmscInformation(String mmscUrl, String mmsProxy, int proxyPort) {
            this.mmscUrl = mmscUrl;
            this.mmsProxy = mmsProxy;
            this.proxyPort = proxyPort;
        }
    }
}
