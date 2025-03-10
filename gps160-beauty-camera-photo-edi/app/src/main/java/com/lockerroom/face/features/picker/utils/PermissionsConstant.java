package com.lockerroom.face.features.picker.utils;

import androidx.annotation.RequiresApi;

public class PermissionsConstant {
    public static final String[] PERMISSIONS_CAMERA = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    @RequiresApi(api = 16)
    public static final String[] PERMISSIONS_EXTERNAL_READ = {"android.permission.READ_EXTERNAL_STORAGE"};
    public static final String[] PERMISSIONS_REAN_MEDIA_IMAGES = {"android.permission.READ_MEDIA_IMAGES"};
    public static final String[] PERMISSIONS_EXTERNAL_WRITE = {"android.permission.WRITE_EXTERNAL_STORAGE"};

}
