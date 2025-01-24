package com.messaging.textrasms.manager.common.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.messaging.textrasms.manager.R;


public class CustomFontTextView extends TextView {
    private static final String TAG = "CustomFontTextView";

    public CustomFontTextView(Context context) {
        super(context);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomFontTextView);
        String customFont = a.getString(R.styleable.CustomFontTextView_customFont);
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String asset) {
        try {
            if (ctx == null) {
                Log.e(TAG, "Context is null");
                return false;
            }
            if (asset == null){
                asset = "fonts/avenir_bold.ttf";
                Log.e(TAG, "Font asset is null, using default font");
            }
            Log.e("customFont",">"+asset);
            Typeface tf = Typeface.createFromAsset(ctx.getAssets(), asset);

            // Log the loaded font file path
            Log.d(TAG, "Loaded font from asset: " + asset);

            if (tf != null) {
                setTypeface(tf);
                return true;
            }else {
                Log.e(TAG, "Typeface is null");
                return false;
            }
        } catch (Exception e) {
            // Log the exception
            Log.e(TAG, "Error loading custom font: " + asset, e);
            return false;
        }
    }
}
