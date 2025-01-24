package com.android.mms.layout;

public interface LayoutParameters {
    int UNKNOWN = -1;
    int HVGA_LANDSCAPE = 10;
    int HVGA_PORTRAIT = 11;

    int HVGA_LANDSCAPE_WIDTH = 480;
    int HVGA_LANDSCAPE_HEIGHT = 320;
    int HVGA_PORTRAIT_WIDTH = 320;
    int HVGA_PORTRAIT_HEIGHT = 480;

    int getWidth();

    int getHeight();

    int getImageHeight();

    int getTextHeight();

    int getType();

    String getTypeDescription();
}
