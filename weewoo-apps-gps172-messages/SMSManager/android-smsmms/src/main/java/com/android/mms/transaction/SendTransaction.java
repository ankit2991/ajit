package com.android.mms.transaction;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Sent;
import android.text.TextUtils;

import com.android.mms.util.RateController;
import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.SendConf;
import com.google.android.mms.pdu_alt.SendReq;
import com.klinker.android.send_message.BroadcastUtils;
import com.klinker.android.send_message.Utils;

import java.util.Arrays;

import timber.log.Timber;

public class SendTransaction extends Transaction implements Runnable {
    public final Uri mSendReqURI;
    private Thread mThread;

    public SendTransaction(Context context,
                           int transId, TransactionSettings connectionSettings, String uri) {
        super(context, transId, connectionSettings);
        mSendReqURI = Uri.parse(uri);
        mId = uri;

        attach(RetryScheduler.getInstance(context));
    }

    @Override
    public void process() {
        mThread = new Thread(this, "SendTransaction");
        mThread.start();
    }

    public void run() {
        StringBuilder builder = new StringBuilder();
        try {
            RateController.init(mContext);
            RateController rateCtlr = RateController.getInstance();
            if (rateCtlr.isLimitSurpassed() && !rateCtlr.isAllowedByUser()) {
                Timber.e("Sending rate limit surpassed.");
                return;
            }

            PduPersister persister = PduPersister.getPduPersister(mContext);
            SendReq sendReq = (SendReq) persister.load(mSendReqURI);

            long date = System.currentTimeMillis() / 1000L;
            sendReq.setDate(date);

            ContentValues values = new ContentValues(1);
            values.put(Mms.DATE, date);
            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                    mSendReqURI, values, null, null);

            String lineNumber = Utils.getMyPhoneNumber(mContext);
            if (!TextUtils.isEmpty(lineNumber)) {
                sendReq.setFrom(new EncodedStringValue(lineNumber));
            }

            long tokenKey = ContentUris.parseId(mSendReqURI);
            byte[] response = sendPdu(SendingProgressTokenManager.get(tokenKey),
                    new PduComposer(mContext, sendReq).make());
            SendingProgressTokenManager.remove(tokenKey);

            String respStr = new String(response);
            builder.append("[SendTransaction] run: send mms msg (" + mId + "), resp=" + respStr);
            Timber.d("[SendTransaction] run: send mms msg (" + mId + "), resp=" + respStr);

            SendConf conf = (SendConf) new PduParser(response).parse();
            if (conf == null) {
                Timber.e("No M-Send.conf received.");
                builder.append("No M-Send.conf received.\n");
            }

            byte[] reqId = sendReq.getTransactionId();
            byte[] confId = conf.getTransactionId();
            if (!Arrays.equals(reqId, confId)) {
                Timber.e("Inconsistent Transaction-ID: req="
                        + new String(reqId) + ", conf=" + new String(confId));
                builder.append("Inconsistent Transaction-ID: req="
                        + new String(reqId) + ", conf=" + new String(confId) + "\n");
                return;
            }

            values = new ContentValues(2);
            int respStatus = conf.getResponseStatus();
            values.put(Mms.RESPONSE_STATUS, respStatus);

            if (respStatus != PduHeaders.RESPONSE_STATUS_OK) {
                SqliteWrapper.update(mContext, mContext.getContentResolver(),
                        mSendReqURI, values, null, null);
                Timber.e("Server returned an error code: " + respStatus);
                builder.append("Server returned an error code: " + respStatus + "\n");
                return;
            }

            String messageId = PduPersister.toIsoString(conf.getMessageId());
            values.put(Mms.MESSAGE_ID, messageId);
            SqliteWrapper.update(mContext, mContext.getContentResolver(),
                    mSendReqURI, values, null, null);

            Uri uri = persister.move(mSendReqURI, Sent.CONTENT_URI);

            mTransactionState.setState(TransactionState.SUCCESS);
            mTransactionState.setContentUri(uri);
        } catch (Throwable t) {
            Timber.e(t, "error");
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mSendReqURI);
                Timber.e("Delivery failed.");
                builder.append("Delivery failed\n");

                Intent intent = new Intent(com.klinker.android.send_message.Transaction.MMS_ERROR);
                intent.putExtra("stack", builder.toString());
                BroadcastUtils.sendExplicitBroadcast(
                        mContext, intent, com.klinker.android.send_message.Transaction.MMS_ERROR);
            }
            notifyObservers();
        }
    }

    @Override
    public int getType() {
        return SEND_TRANSACTION;
    }
}
