package com.android.mms.service_alt.exception;

public class MmsNetworkException extends Exception {

    public MmsNetworkException() {
        super();
    }

    public MmsNetworkException(String message) {
        super(message);
    }

    public MmsNetworkException(Throwable cause) {
        super(cause);
    }

    public MmsNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
