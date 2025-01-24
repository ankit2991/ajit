package com.android.mms.transaction;

import android.content.Context;
import android.net.Uri;
import android.provider.Telephony.Mms.Sent;

import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.ReadRecInd;
import com.klinker.android.send_message.Utils;

import java.io.IOException;

import timber.log.Timber;

public class ReadRecTransaction extends Transaction implements Runnable {
    private static final boolean LOCAL_LOGV = false;
    private final Uri mReadReportURI;
    private Thread mThread;

    public ReadRecTransaction(Context context,
                              int transId,
                              TransactionSettings connectionSettings,
                              String uri) {
        super(context, transId, connectionSettings);
        mReadReportURI = Uri.parse(uri);
        mId = uri;

        attach(RetryScheduler.getInstance(context));
    }

    @Override
    public void process() {
        mThread = new Thread(this, "ReadRecTransaction");
        mThread.start();
    }

    public void run() {
        PduPersister persister = PduPersister.getPduPersister(mContext);

        try {
            ReadRecInd readRecInd = (ReadRecInd) persister.load(mReadReportURI);

            String lineNumber = Utils.getMyPhoneNumber(mContext);
            readRecInd.setFrom(new EncodedStringValue(lineNumber));

            byte[] postingData = new PduComposer(mContext, readRecInd).make();
            sendPdu(postingData);

            Uri uri = persister.move(mReadReportURI, Sent.CONTENT_URI);
            mTransactionState.setState(TransactionState.SUCCESS);
            mTransactionState.setContentUri(uri);
        } catch (IOException e) {
            if (LOCAL_LOGV) {
                Timber.v(e, "Failed to send M-Read-Rec.Ind.");
            }
        } catch (MmsException e) {
            if (LOCAL_LOGV) {
                Timber.v(e, "Failed to load message from Outbox.");
            }
        } catch (RuntimeException e) {
            if (LOCAL_LOGV) {
                Timber.e(e, "Unexpected RuntimeException.");
            }
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mReadReportURI);
            }
            notifyObservers();
        }
    }

    @Override
    public int getType() {
        return READREC_TRANSACTION;
    }
}
