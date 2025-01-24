package com.google.android.mms.util_alt;

import android.content.Context;
import android.drm.DrmConvertedStatus;
import android.drm.DrmManagerClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import timber.log.Timber;


public class DrmConvertSession {
    private static final int STATUS_UNKNOWN_ERROR = 491;
    private static final int STATUS_NOT_ACCEPTABLE = 406;
    private static final int STATUS_SUCCESS = 200;
    private static final int STATUS_FILE_ERROR = 492;
    private final DrmManagerClient mDrmClient;
    private final int mConvertSessionId;

    private DrmConvertSession(DrmManagerClient drmClient, int convertSessionId) {
        mDrmClient = drmClient;
        mConvertSessionId = convertSessionId;
    }

    public static DrmConvertSession open(Context context, String mimeType) {
        DrmManagerClient drmClient = null;
        int convertSessionId = -1;
        if (context != null && mimeType != null && !mimeType.equals("")) {
            try {
                drmClient = new DrmManagerClient(context);
                try {
                    convertSessionId = drmClient.openConvertSession(mimeType);
                } catch (IllegalArgumentException e) {
                    Timber.w("Conversion of Mimetype: " + mimeType
                            + " is not supported.", e);
                } catch (IllegalStateException e) {
                    Timber.w(e, "Could not access Open DrmFramework.");
                }
            } catch (IllegalArgumentException e) {
                Timber.w("DrmManagerClient instance could not be created, context is Illegal.");
            } catch (IllegalStateException e) {
                Timber.w("DrmManagerClient didn't initialize properly.");
            }
        }

        if (drmClient == null || convertSessionId < 0) {
            return null;
        } else {
            return new DrmConvertSession(drmClient, convertSessionId);
        }
    }

    public byte[] convert(byte[] inBuffer, int size) {
        byte[] result = null;
        if (inBuffer != null) {
            DrmConvertedStatus convertedStatus = null;
            try {
                if (size != inBuffer.length) {
                    byte[] buf = new byte[size];
                    System.arraycopy(inBuffer, 0, buf, 0, size);
                    convertedStatus = mDrmClient.convertData(mConvertSessionId, buf);
                } else {
                    convertedStatus = mDrmClient.convertData(mConvertSessionId, inBuffer);
                }

                if (convertedStatus != null &&
                        convertedStatus.statusCode == DrmConvertedStatus.STATUS_OK &&
                        convertedStatus.convertedData != null) {
                    result = convertedStatus.convertedData;
                }
            } catch (IllegalArgumentException e) {
                Timber.w("Buffer with data to convert is illegal. Convertsession: "
                        + mConvertSessionId, e);
            } catch (IllegalStateException e) {
                Timber.w("Could not convert data. Convertsession: " +
                        mConvertSessionId, e);
            }
        } else {
            throw new IllegalArgumentException("Parameter inBuffer is null");
        }
        return result;
    }

    public int close(String filename) {
        DrmConvertedStatus convertedStatus = null;
        int result = STATUS_UNKNOWN_ERROR;
        if (mDrmClient != null && mConvertSessionId >= 0) {
            try {
                convertedStatus = mDrmClient.closeConvertSession(mConvertSessionId);
                if (convertedStatus == null ||
                        convertedStatus.statusCode != DrmConvertedStatus.STATUS_OK ||
                        convertedStatus.convertedData == null) {
                    result = STATUS_NOT_ACCEPTABLE;
                } else {
                    RandomAccessFile rndAccessFile = null;
                    try {
                        rndAccessFile = new RandomAccessFile(filename, "rw");
                        rndAccessFile.seek(convertedStatus.offset);
                        rndAccessFile.write(convertedStatus.convertedData);
                        result = STATUS_SUCCESS;
                    } catch (FileNotFoundException e) {
                        result = STATUS_FILE_ERROR;
                        Timber.w(e, "File: " + filename + " could not be found.");
                    } catch (IOException e) {
                        result = STATUS_FILE_ERROR;
                        Timber.w(e, "Could not access File: " + filename + " .");
                    } catch (IllegalArgumentException e) {
                        result = STATUS_FILE_ERROR;
                        Timber.w(e, "Could not open file in mode: rw");
                    } catch (SecurityException e) {
                        Timber.w("Access to File: " + filename +
                                " was denied denied by SecurityManager.", e);
                    } finally {
                        if (rndAccessFile != null) {
                            try {
                                rndAccessFile.close();
                            } catch (IOException e) {
                                result = STATUS_FILE_ERROR;
                                Timber.w("Failed to close File:" + filename
                                        + ".", e);
                            }
                        }
                    }
                }
            } catch (IllegalStateException e) {
                Timber.w("Could not close convertsession. Convertsession: " +
                        mConvertSessionId, e);
            }
        }
        return result;
    }
}
