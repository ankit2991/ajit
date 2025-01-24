package com.android.mms.service_alt.exception;

public class MmsHttpException extends Exception {
    private final int mStatusCode;

    public MmsHttpException(int statusCode) {
        super();
        mStatusCode = statusCode;
    }

    public MmsHttpException(int statusCode, String message) {
        super(message);
        mStatusCode = statusCode;
    }

    public MmsHttpException(int statusCode, Throwable cause) {
        super(cause);
        mStatusCode = statusCode;
    }

    public MmsHttpException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        mStatusCode = statusCode;
    }

    public int getStatusCode() {
        return mStatusCode;
    }
}
