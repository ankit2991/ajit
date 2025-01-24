package com.lockerroom.face;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {
    static SharedPreferences mSharedPref;
    public static final String IS_AGREE = "agreed";
    public static final String GOT_IT = "got_it";

    static void init(Context context){
        if (mSharedPref == null)
            mSharedPref = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
    }

    public static boolean read(Context context, String key, boolean defValue) {
        if (mSharedPref == null)
            init(context);
        return mSharedPref.getBoolean(key, defValue);
    }

    public static void write(Context context, String key, boolean value) {
        if (mSharedPref == null)
            init(context);
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }
}
