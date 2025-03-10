package com.android.mms.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms;
import android.widget.Toast;

import com.android.internal.telephony.TelephonyProperties;
import com.android.mms.service_alt.SystemPropertiesProxy;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.NotificationInd;
import com.google.android.mms.pdu_alt.PduPersister;
import com.klinker.android.send_message.R;

import timber.log.Timber;

public class DownloadManager {
    public static final int DEFERRED_MASK = 0x04;
    public static final int STATE_UNKNOWN = 0x00;
    public static final int STATE_UNSTARTED = 0x80;
    public static final int STATE_DOWNLOADING = 0x81;
    public static final int STATE_TRANSIENT_FAILURE = 0x82;
    public static final int STATE_PERMANENT_FAILURE = 0x87;
    public static final int STATE_PRE_DOWNLOADING = 0x88;
    public static final int STATE_SKIP_RETRYING = 0x89;
    private static final boolean LOCAL_LOGV = false;
    private static DownloadManager sInstance;
    private final Context mContext;
    private final Handler mHandler;
    private final SharedPreferences mPreferences;
    private final boolean mAutoDownload;

    private DownloadManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mAutoDownload = getAutoDownloadState(context, mPreferences);
        if (LOCAL_LOGV) {
            Timber.v("mAutoDownload ------> " + mAutoDownload);
        }
    }

    public static void init(Context context) {
        if (LOCAL_LOGV) {
            Timber.v("DownloadManager.init()");
        }

        if (sInstance != null) {
            Timber.w("Already initialized.");
        }
        sInstance = new DownloadManager(context);
    }

    public static DownloadManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Uninitialized.");
        }
        return sInstance;
    }

    static boolean getAutoDownloadState(Context context, SharedPreferences prefs) {
        return getAutoDownloadState(prefs, isRoaming(context));
    }

    static boolean getAutoDownloadState(SharedPreferences prefs, boolean roaming) {
        boolean autoDownload = prefs.getBoolean("auto_download_mms", true);

        if (LOCAL_LOGV) {
            Timber.v("auto download without roaming -> " + autoDownload);
        }

        if (autoDownload) {
            boolean alwaysAuto = true;

            if (LOCAL_LOGV) {
                Timber.v("auto download during roaming -> " + alwaysAuto);
            }

            return !roaming || alwaysAuto;
        }
        return false;
    }

    static boolean isRoaming(Context context) {
        // TODO: fix and put in Telephony layer
        String roaming = SystemPropertiesProxy.get(context,
                TelephonyProperties.PROPERTY_OPERATOR_ISROAMING, null);
        if (LOCAL_LOGV) {
            Timber.v("roaming ------> " + roaming);
        }
        return "true".equals(roaming);
    }

    public boolean isAuto() {
        return mAutoDownload;
    }

    public void markState(final Uri uri, int state) {
        try {
            NotificationInd nInd = (NotificationInd) PduPersister.getPduPersister(mContext)
                    .load(uri);
            if ((nInd.getExpiry() < System.currentTimeMillis() / 1000L)
                    && (state == STATE_DOWNLOADING || state == STATE_PRE_DOWNLOADING)) {
                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(mContext, R.string.service_message_not_found,
                                Toast.LENGTH_LONG).show();
                    }
                });
                SqliteWrapper.delete(mContext, mContext.getContentResolver(), uri, null, null);
                return;
            }
        } catch (MmsException e) {
            Timber.e(e, e.getMessage());
            return;
        }

        if (state == STATE_PERMANENT_FAILURE) {
            mHandler.post(new Runnable() {
                public void run() {
                    try {
                        Toast.makeText(mContext, getMessage(uri),
                                Toast.LENGTH_LONG).show();
                    } catch (MmsException e) {
                        Timber.e(e, e.getMessage());
                    }
                }
            });
        } else if (!mAutoDownload) {
            state |= DEFERRED_MASK;
        }

        ContentValues values = new ContentValues(1);
        values.put(Mms.STATUS, state);
        SqliteWrapper.update(mContext, mContext.getContentResolver(),
                uri, values, null, null);
    }

    public void showErrorCodeToast(int errorStr) {
        final int errStr = errorStr;
        mHandler.post(new Runnable() {
            public void run() {
                try {
                    Toast.makeText(mContext, errStr, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Timber.e("Caught an exception in showErrorCodeToast");
                }
            }
        });
    }

    private String getMessage(Uri uri) throws MmsException {
        NotificationInd ind = (NotificationInd) PduPersister
                .getPduPersister(mContext).load(uri);

        EncodedStringValue v = ind.getSubject();
        String subject = (v != null) ? v.getString()
                : mContext.getString(R.string.no_subject);

        String from = mContext.getString(R.string.unknown_sender);

        return mContext.getString(R.string.dl_failure_notification, subject, from);
    }

    public int getState(Uri uri) {
        Cursor cursor = SqliteWrapper.query(mContext, mContext.getContentResolver(),
                uri, new String[]{Mms.STATUS}, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int state = cursor.getInt(0) & ~DEFERRED_MASK;
                    cursor.close();
                    return state;
                }
            } finally {
                cursor.close();
            }
        }
        return STATE_UNSTARTED;
    }
}
