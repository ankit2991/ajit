package com.android.mms.transaction;

import static android.provider.Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION;
import static android.provider.Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_DELIVERY_IND;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
import static com.google.android.mms.pdu_alt.PduHeaders.MESSAGE_TYPE_READ_ORIG_IND;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;

import com.android.mms.MmsConfig;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.DeliveryInd;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.ReadOrigInd;
import com.klinker.android.send_message.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class PushReceiver extends BroadcastReceiver {

    static final String[] PROJECTION = new String[]{Mms.CONTENT_LOCATION, Mms.LOCKED};

    static final int COLUMN_CONTENT_LOCATION = 0;
    private static final ExecutorService PUSH_RECEIVER_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Set<String> downloadedUrls = new HashSet<String>();

    public static String getContentLocation(Context context, Uri uri) throws MmsException {
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                uri, PROJECTION, null, null, null);

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
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

    private static long findThreadId(Context context, GenericPdu pdu, int type) {
        String messageId;

        if (type == MESSAGE_TYPE_DELIVERY_IND) {
            messageId = new String(((DeliveryInd) pdu).getMessageId());
        } else {
            messageId = new String(((ReadOrigInd) pdu).getMessageId());
        }

        StringBuilder sb = new StringBuilder('(');
        sb.append(Mms.MESSAGE_ID);
        sb.append('=');
        sb.append(DatabaseUtils.sqlEscapeString(messageId));
        sb.append(" AND ");
        sb.append(Mms.MESSAGE_TYPE);
        sb.append('=');
        sb.append(PduHeaders.MESSAGE_TYPE_SEND_REQ);
        // TODO ContentResolver.query() appends closing ')' to the selection argument
        // sb.append(')');

        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                Mms.CONTENT_URI, new String[]{Mms.THREAD_ID},
                sb.toString(), null, null);
        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    long id = cursor.getLong(0);
                    cursor.close();
                    return id;
                }
            } finally {
                cursor.close();
            }
        }

        return -1;
    }

    private static boolean isDuplicateNotification(Context context, NotificationInd nInd) {
        byte[] rawLocation = nInd.getContentLocation();
        if (rawLocation != null) {
            String location = new String(rawLocation);
            String selection = Mms.CONTENT_LOCATION + " = ?";
            String[] selectionArgs = new String[]{location};
            Cursor cursor = SqliteWrapper.query(
                    context, context.getContentResolver(),
                    Mms.CONTENT_URI, new String[]{Mms._ID},
                    selection, selectionArgs, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        cursor.close();
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.v(intent.getAction() + " " + intent.getType());
        if ((intent.getAction().equals(WAP_PUSH_DELIVER_ACTION) || intent.getAction().equals(WAP_PUSH_RECEIVED_ACTION))
                && ContentType.MMS_MESSAGE.equals(intent.getType())) {
            Timber.v("Received PUSH Intent: " + intent);

            MmsConfig.init(context);
            new ReceivePushTask(context, goAsync()).executeOnExecutor(PUSH_RECEIVER_EXECUTOR, intent);

            Timber.v(context.getPackageName() + " received and aborted");
        }
    }

    private class ReceivePushTask extends AsyncTask<Intent, Void, Void> {
        private final Context mContext;
        private final PendingResult pendingResult;

        private ReceivePushTask(Context context, PendingResult pendingResult) {
            mContext = context;
            this.pendingResult = pendingResult;
        }

        @Override
        protected Void doInBackground(Intent... intents) {
            Timber.v("receiving a new mms message");
            Intent intent = intents[0];

            byte[] pushData = intent.getByteArrayExtra("data");
            PduParser parser = new PduParser(pushData);
            GenericPdu pdu = parser.parse();

            if (pdu == null) {
                Timber.e("Invalid PUSH data");
                return null;
            }

            PduPersister p = PduPersister.getPduPersister(mContext);
            ContentResolver cr = mContext.getContentResolver();
            int type = pdu.getMessageType();
            long threadId;

            try {
                switch (type) {
                    case MESSAGE_TYPE_DELIVERY_IND:
                    case MESSAGE_TYPE_READ_ORIG_IND: {
                        threadId = findThreadId(mContext, pdu, type);
                        if (threadId == -1) {
                            break;
                        }

                        Uri uri = p.persist(pdu, Uri.parse("content://mms/inbox"),
                                PduPersister.DUMMY_THREAD_ID, true, true, null);
                        ContentValues values = new ContentValues(1);
                        values.put(Mms.THREAD_ID, threadId);
                        SqliteWrapper.update(mContext, cr, uri, values, null, null);
                        break;
                    }
                    case MESSAGE_TYPE_NOTIFICATION_IND: {
                        NotificationInd nInd = (NotificationInd) pdu;

                        if (MmsConfig.getTransIdEnabled()) {
                            byte[] contentLocation = nInd.getContentLocation();
                            if ('=' == contentLocation[contentLocation.length - 1]) {
                                byte[] transactionId = nInd.getTransactionId();
                                byte[] contentLocationWithId = new byte[contentLocation.length
                                        + transactionId.length];
                                System.arraycopy(contentLocation, 0, contentLocationWithId,
                                        0, contentLocation.length);
                                System.arraycopy(transactionId, 0, contentLocationWithId,
                                        contentLocation.length, transactionId.length);
                                nInd.setContentLocation(contentLocationWithId);
                            }
                        }

                        if (!isDuplicateNotification(mContext, nInd)) {
                            Uri uri = p.persist(pdu, Inbox.CONTENT_URI,
                                    PduPersister.DUMMY_THREAD_ID, true, true, null);

                            String location = getContentLocation(mContext, uri);
                            if (downloadedUrls.contains(location)) {
                                Timber.v("already added this download, don't download again");
                                return null;
                            } else {
                                downloadedUrls.add(location);
                            }

                            int subId = intent.getIntExtra("subscription", Utils.getDefaultSubscriptionId());
                            DownloadManager.getInstance().downloadMultimediaMessage(mContext, location, uri, true, subId);
                        } else {
                            Timber.v("Skip downloading duplicate message: " + new String(nInd.getContentLocation()));
                        }
                        break;
                    }
                    default:
                        Timber.e("Received unrecognized PDU.");
                }
            } catch (MmsException e) {
                Timber.e(e, "Failed to save the data from PUSH: type=" + type);
            } catch (RuntimeException e) {
                Timber.e(e, "Unexpected RuntimeException.");
            }

            Timber.v("PUSH Intent processed.");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pendingResult.finish();
        }
    }
}
