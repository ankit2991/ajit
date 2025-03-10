package com.android.mms.transaction;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.text.TextUtils;

import com.android.mms.MmsConfig;
import com.android.mms.util.DownloadManager;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.AcknowledgeInd;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.RetrieveConf;
import com.klinker.android.send_message.Utils;

import java.io.IOException;

import timber.log.Timber;

public class RetrieveTransaction extends Transaction implements Runnable {
    static final String[] PROJECTION = new String[]{
            Mms.CONTENT_LOCATION,
            Mms.LOCKED
    };
    static final int COLUMN_CONTENT_LOCATION = 0;
    static final int COLUMN_LOCKED = 1;
    private static final boolean LOCAL_LOGV = false;
    private final Uri mUri;
    private final String mContentLocation;
    private boolean mLocked;

    public RetrieveTransaction(Context context, int serviceId,
                               TransactionSettings connectionSettings, String uri)
            throws MmsException {
        super(context, serviceId, connectionSettings);

        if (uri.startsWith("content://")) {
            mUri = Uri.parse(uri);
            mId = mContentLocation = getContentLocation(context, mUri);
            if (LOCAL_LOGV) {
                Timber.v("X-Mms-Content-Location: " + mContentLocation);
            }
        } else {
            throw new IllegalArgumentException(
                    "Initializing from X-Mms-Content-Location is abandoned!");
        }

        attach(RetryScheduler.getInstance(context));
    }

    private static boolean isDuplicateMessage(Context context, RetrieveConf rc) {
        byte[] rawMessageId = rc.getMessageId();
        if (rawMessageId != null) {
            String messageId = new String(rawMessageId);
            String selection = "(" + Mms.MESSAGE_ID + " = ? AND "
                    + Mms.MESSAGE_TYPE + " = ?)";
            String[] selectionArgs = new String[]{messageId,
                    String.valueOf(PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF)};

            Cursor cursor = SqliteWrapper.query(
                    context, context.getContentResolver(),
                    Mms.CONTENT_URI, new String[]{Mms._ID, Mms.SUBJECT, Mms.SUBJECT_CHARSET},
                    selection, selectionArgs, null);

            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        boolean dup = isDuplicateMessageExtra(cursor, rc);
                        if (!cursor.isClosed()) {
                            cursor.close();
                        }

                        return dup;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }

    private static boolean isDuplicateMessageExtra(Cursor cursor, RetrieveConf rc) {
        EncodedStringValue encodedSubjectReceived = null;
        EncodedStringValue encodedSubjectStored = null;
        String subjectReceived = null;
        String subjectStored = null;
        String subject = null;

        encodedSubjectReceived = rc.getSubject();
        if (encodedSubjectReceived != null) {
            subjectReceived = encodedSubjectReceived.getString();
        }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int subjectIdx = cursor.getColumnIndex(Mms.SUBJECT);
            int charsetIdx = cursor.getColumnIndex(Mms.SUBJECT_CHARSET);
            subject = cursor.getString(subjectIdx);
            int charset = cursor.getInt(charsetIdx);
            if (subject != null) {
                encodedSubjectStored = new EncodedStringValue(charset, PduPersister
                        .getBytes(subject));
            }
            if (encodedSubjectStored == null && encodedSubjectReceived == null) {
                return true;
            } else if (encodedSubjectStored != null && encodedSubjectReceived != null) {
                subjectStored = encodedSubjectStored.getString();
                if (!TextUtils.isEmpty(subjectStored) && !TextUtils.isEmpty(subjectReceived)) {
                    return subjectStored.equals(subjectReceived);
                } else if (TextUtils.isEmpty(subjectStored) && TextUtils.isEmpty(subjectReceived)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void updateContentLocation(Context context, Uri uri,
                                              String contentLocation,
                                              boolean locked) {
        ContentValues values = new ContentValues(2);
        values.put(Mms.CONTENT_LOCATION, contentLocation);
        values.put(Mms.LOCKED, locked);
        SqliteWrapper.update(context, context.getContentResolver(),
                uri, values, null, null);
    }

    public String getContentLocation(Context context, Uri uri)
            throws MmsException {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                uri, PROJECTION, null, null, null);
        mLocked = false;

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    mLocked = cursor.getInt(COLUMN_LOCKED) == 1;
                    String location = cursor.getString(COLUMN_CONTENT_LOCATION);
                    cursor.close();
                    return location;
                }
            } finally {
                cursor.close();
            }
        }

        throw new MmsException("Cannot get X-Mms-Content-Location from: " + uri);
    }

    @Override
    public void process() {
        new Thread(this, "RetrieveTransaction").start();
    }

    public void run() {

        try {
            DownloadManager.init(mContext.getApplicationContext());
            DownloadManager.getInstance().markState(
                    mUri, DownloadManager.STATE_DOWNLOADING);

            byte[] resp = getPdu(mContentLocation);

            RetrieveConf retrieveConf = (RetrieveConf) new PduParser(resp).parse();
            if (null == retrieveConf) {
                throw new MmsException("Invalid M-Retrieve.conf PDU.");
            }

            Uri msgUri = null;
            if (isDuplicateMessage(mContext, retrieveConf)) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mUri);
            } else {
                PduPersister persister = PduPersister.getPduPersister(mContext);
                msgUri = persister.persist(retrieveConf, Inbox.CONTENT_URI,
                        PduPersister.DUMMY_THREAD_ID, true, true, null);

                ContentValues values = new ContentValues(3);
                values.put(Mms.DATE, System.currentTimeMillis() / 1000L);
                try {
                    values.put(Mms.DATE_SENT, retrieveConf.getDate());
                } catch (Exception ignored) {
                }
                values.put(Mms.MESSAGE_SIZE, resp.length);
                SqliteWrapper.update(mContext, mContext.getContentResolver(),
                        msgUri, values, null, null);

                mTransactionState.setState(TransactionState.SUCCESS);
                mTransactionState.setContentUri(msgUri);
                updateContentLocation(mContext, msgUri, mContentLocation, mLocked);
            }

            SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                    mUri, null, null);

            sendAcknowledgeInd(retrieveConf);
        } catch (Throwable t) {
            Timber.e(t, "error");
            if ("HTTP error: Not Found".equals(t.getMessage())) {
                SqliteWrapper.delete(mContext, mContext.getContentResolver(),
                        mUri, null, null);
            }
        } finally {
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                mTransactionState.setContentUri(mUri);
                Timber.e("Retrieval failed.");
            }
            notifyObservers();
        }
    }

    private void sendAcknowledgeInd(RetrieveConf rc) throws MmsException, IOException {
        byte[] tranId = rc.getTransactionId();
        if (tranId != null) {
            AcknowledgeInd acknowledgeInd = new AcknowledgeInd(
                    PduHeaders.CURRENT_MMS_VERSION, tranId);

            String lineNumber = Utils.getMyPhoneNumber(mContext);
            acknowledgeInd.setFrom(new EncodedStringValue(lineNumber));

            if (MmsConfig.getNotifyWapMMSC()) {
                sendPdu(new PduComposer(mContext, acknowledgeInd).make(), mContentLocation);
            } else {
                sendPdu(new PduComposer(mContext, acknowledgeInd).make());
            }
        }
    }

    @Override
    public int getType() {
        return RETRIEVE_TRANSACTION;
    }
}
