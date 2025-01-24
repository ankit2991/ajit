package callerid.truecaller.trackingnumber.phonenumbertracker.block.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
/**
 * Created by Hamza Chaudhary
 * Sr. Software Engineer Android
 * Created on 11 Apr,2023 12:11
 * Copyright (c) All rights reserved.
 * @see "<a href="https://www.linkedin.com/in/iamhco/">Linkedin Profile</a>"
 */

public class OverlayPermissionManager {
    private static final String TAG = "OverlayPermissionManage";
    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2803;
    public static final int LISTEN_TIMEOUT = 5 * 20;
    private static final int THREAD_SLEEP_TIME = 200;
    private Activity activity;
    private Thread thread;
    private boolean shouldContinueThread = true;
    public OverlayPermissionManager(Activity activity) {
        this.activity = activity;
    }
    public void requestOverlay() {
        shouldContinueThread = true;
        sendToSettings();
        startGrantedCheckThread();
    }
    private void sendToSettings() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        activity.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
    }
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            shouldContinueThread = false;
        }
    }
    private void startGrantedCheckThread() {
        thread = new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                int counter = 0;
                while(!Settings.canDrawOverlays(activity) && shouldContinueThread
                        && counter < LISTEN_TIMEOUT) {
                    try {
                        counter++;
                        Log.d(TAG, "run: still no permission");
                        sleep(THREAD_SLEEP_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(shouldContinueThread && counter < LISTEN_TIMEOUT) {
                    Intent intent = new Intent(activity, activity.getClass());
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        activity.startActivity(intent);
                    } else {
                        activity.startActivityIfNeeded(intent, 0);
                    }
                }
            }
        };
        thread.start();
    }
    public boolean isGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(activity);
        }
        return true;
    }
}
