package com.android.mms.layout;

import android.content.Context;
import android.content.res.Configuration;

import timber.log.Timber;

public class LayoutManager {
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;
    private static LayoutManager sInstance;
    private final Context mContext;
    private LayoutParameters mLayoutParams;

    private LayoutManager(Context context) {
        mContext = context;
        initLayoutParameters(context.getResources().getConfiguration());
    }

    public static void init(Context context) {
        if (LOCAL_LOGV) {
            Timber.v("DefaultLayoutManager.init()");
        }

        if (sInstance != null) {
            Timber.w("Already initialized.");
        }
        sInstance = new LayoutManager(context);
    }

    public static LayoutManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Uninitialized.");
        }
        return sInstance;
    }

    private void initLayoutParameters(Configuration configuration) {
        mLayoutParams = getLayoutParameters(
                configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                        ? LayoutParameters.HVGA_PORTRAIT
                        : LayoutParameters.HVGA_LANDSCAPE);

        if (LOCAL_LOGV) {
            Timber.v("LayoutParameters: " + mLayoutParams.getTypeDescription()
                    + ": " + mLayoutParams.getWidth() + "x" + mLayoutParams.getHeight());
        }
    }

    private LayoutParameters getLayoutParameters(int displayType) {
        switch (displayType) {
            case LayoutParameters.HVGA_LANDSCAPE:
                return new HVGALayoutParameters(mContext, LayoutParameters.HVGA_LANDSCAPE);
            case LayoutParameters.HVGA_PORTRAIT:
                return new HVGALayoutParameters(mContext, LayoutParameters.HVGA_PORTRAIT);
        }

        throw new IllegalArgumentException(
                "Unsupported display type: " + displayType);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (LOCAL_LOGV) {
            Timber.v("-> LayoutManager.onConfigurationChanged().");
        }
        initLayoutParameters(newConfig);
    }

    public int getLayoutType() {
        return mLayoutParams.getType();
    }

    public int getLayoutWidth() {
        return mLayoutParams.getWidth();
    }

    public int getLayoutHeight() {
        return mLayoutParams.getHeight();
    }

    public LayoutParameters getLayoutParameters() {
        return mLayoutParams;
    }
}
