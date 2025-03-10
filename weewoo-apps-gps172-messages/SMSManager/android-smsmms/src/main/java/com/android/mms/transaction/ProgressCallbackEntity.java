package com.android.mms.transaction;

import android.content.Context;
import android.content.Intent;

import com.klinker.android.send_message.BroadcastUtils;

import org.apache.http.entity.ByteArrayEntity;

import java.io.IOException;
import java.io.OutputStream;

public class ProgressCallbackEntity extends ByteArrayEntity {
    public static final String PROGRESS_STATUS_ACTION = "com.android.mms.PROGRESS_STATUS";
    public static final int PROGRESS_START = -1;
    public static final int PROGRESS_ABORT = -2;
    public static final int PROGRESS_COMPLETE = 100;
    private static final int DEFAULT_PIECE_SIZE = 4096;
    private final Context mContext;
    private final byte[] mContent;
    private final long mToken;

    public ProgressCallbackEntity(Context context, long token, byte[] b) {
        super(b);

        mContext = context;
        mContent = b;
        mToken = token;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }

        boolean completed = false;
        try {
            broadcastProgressIfNeeded(PROGRESS_START);

            int pos = 0, totalLen = mContent.length;
            while (pos < totalLen) {
                int len = totalLen - pos;
                if (len > DEFAULT_PIECE_SIZE) {
                    len = DEFAULT_PIECE_SIZE;
                }
                outstream.write(mContent, pos, len);
                outstream.flush();

                pos += len;

                broadcastProgressIfNeeded(100 * pos / totalLen);
            }

            broadcastProgressIfNeeded(PROGRESS_COMPLETE);
            completed = true;
        } finally {
            if (!completed) {
                broadcastProgressIfNeeded(PROGRESS_ABORT);
            }
        }
    }

    private void broadcastProgressIfNeeded(int progress) {
        if (mToken > 0) {
            Intent intent = new Intent(PROGRESS_STATUS_ACTION);
            intent.putExtra("progress", progress);
            intent.putExtra("token", mToken);
            BroadcastUtils.sendExplicitBroadcast(mContext, intent, PROGRESS_STATUS_ACTION);
        }
    }
}
