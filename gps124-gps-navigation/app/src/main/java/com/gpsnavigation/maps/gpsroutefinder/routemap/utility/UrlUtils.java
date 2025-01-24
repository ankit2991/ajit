package com.gpsnavigation.maps.gpsroutefinder.routemap.utility;

import android.content.Context;
import android.net.Uri;
import androidx.core.content.ContextCompat;
import androidx.browser.customtabs.CustomTabsIntent.Builder;
import com.gpsnavigation.maps.gpsroutefinder.routemap.R;
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.MyConstant;

public class UrlUtils {
    private UrlUtils() {
    }

    public static void openUrl(Context context, String url) {
        MyConstant.INSTANCE.setIS_APPOPEN_BG_IMPLICIT(true);

        Builder builder = new Builder();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        builder.build().launchUrl(context, Uri.parse(url));
    }

}
