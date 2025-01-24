package com.zaeem.mytvcast.Utils;

import android.Manifest;

public class Constant {

    public static final String STREAM_NEW_CONTENT = "STREAM_NEW_CONTENT";
    public static final String ACCESS_TOKEN = "access-token";
    public static final String SELECTED_ACCOUNT = "selectedAccount";


    public static final String[] PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
}
