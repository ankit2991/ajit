package com.google.android.mms.util_alt;

import android.content.Context;
import android.drm.DrmManagerClient;

import timber.log.Timber;

public class DownloadDrmHelper {


    public static final String MIMETYPE_DRM_MESSAGE = "application/vnd.oma.drm.message";

    public static final String EXTENSION_DRM_MESSAGE = ".dm";

    public static final String EXTENSION_INTERNAL_FWDL = ".fl";

    public static boolean isDrmMimeType(Context context, String mimetype) {
        boolean result = false;
        if (context != null) {
            try {
                DrmManagerClient drmClient = new DrmManagerClient(context);
                if (drmClient != null && mimetype != null && mimetype.length() > 0) {
                    result = drmClient.canHandle("", mimetype);
                }
            } catch (IllegalArgumentException e) {
                Timber.w("DrmManagerClient instance could not be created, context is Illegal.");
            } catch (IllegalStateException e) {
                Timber.w("DrmManagerClient didn't initialize properly.");
            }
        }
        return result;
    }

    public static boolean isDrmConvertNeeded(String mimetype) {
        return MIMETYPE_DRM_MESSAGE.equals(mimetype);
    }

    public static String modifyDrmFwLockFileExtension(String filename) {
        if (filename != null) {
            int extensionIndex;
            extensionIndex = filename.lastIndexOf(".");
            if (extensionIndex != -1) {
                filename = filename.substring(0, extensionIndex);
            }
            filename = filename.concat(EXTENSION_INTERNAL_FWDL);
        }
        return filename;
    }

    public static String getOriginalMimeType(Context context, String path, String containingMime) {
        String result = containingMime;
        DrmManagerClient drmClient = new DrmManagerClient(context);
        try {
            if (drmClient.canHandle(path, null)) {
                result = drmClient.getOriginalMimeType(path);
            }
        } catch (IllegalArgumentException ex) {
            Timber.w("Can't get original mime type since path is null or empty string.");
        } catch (IllegalStateException ex) {
            Timber.w("DrmManagerClient didn't initialize properly.");
        }
        return result;
    }
}
