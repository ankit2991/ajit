package com.messaging.textrasms.manager.common.util;

import android.content.Context;
import android.util.Log;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Collection;
import java.util.List;

public class PermissionHandler {
    public static final int PERMISSION_REQ_CODE = 20151;
    private static final String TAG = PermissionHandler.class.getSimpleName();
    private static PermissionHandler sInstance;

    public static PermissionHandler getInstance() {
        if (sInstance == null) {
            sInstance = new PermissionHandler();
        }
        return sInstance;
    }

    public void requestPermissions(final Context context, final Collection<String> permissionList, final OnListener listener) {
        Log.i(TAG, "requestPermissions: ");

        Dexter.withContext(context)
                .withPermissions(permissionList)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        Log.i(TAG, "onPermissionsChecked: ");
                        if (report.areAllPermissionsGranted()) {
                            if (listener != null) {
                                listener.onPermissionGranted();
                            }
                        } else {
                            if (listener != null) {
                                listener.onPermissionDenied();
                            }
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            if (listener != null) {
                                listener.onOpenSettings();
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<com.karumi.dexter.listener.PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(dexterError -> Log.i(TAG, "onError: ")).check();
    }

    public interface OnListener {
        void onPermissionGranted();

        void onPermissionDenied();

        void onOpenSettings();
    }
}
