package com.lockerroom.face.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FontUtils {


    public static void setFontByName(Context context, TextView textView, String str) {
        AssetManager assets = context.getAssets();
        textView.setTypeface(Typeface.createFromAsset(assets, "fonts/" + str));
    }


    public static List<String> getListFonts() {
        List<String> arrayList = new ArrayList<>();
        arrayList.add("MPLUSRounded1c-Black.ttf");
        arrayList.add("MPLUSRounded1c-Bold.ttf");
        arrayList.add("MPLUSRounded1c-ExtraBold.ttf");
        arrayList.add("MPLUSRounded1c-Light.ttf");
        arrayList.add("MPLUSRounded1c-Medium.ttf");
        arrayList.add("MPLUSRounded1c-Regular.ttf");
        arrayList.add("36.ttf");
        return arrayList;
    }
}
