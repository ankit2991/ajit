package macro.hd.wallpapers.DB;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.NotifierFactory;

import java.util.Calendar;

/**
 * @author Administrator
 *         <p>
 *         Class for methods which save settings values
 */
public class SettingStore {

    private String PRE_TIME_DURATION_INDEX= "time_duration_index";
    private static final String LOCATION = "location";
    public static final String EXCLUSIVE_RESIZE_BITMAP = "EXCLUSIVE_resize_bitmap";

    private String PRE_USER_ID = "USER_ID";
    private String PRE_OLD_DEVICE_ID = "OLD_DEVICE_ID";
    private static final String LAST_AUTO_CHANGE_TIME = "Last_Auto_Change_Time";
    private String PRE_VIEW_COUNT = "IS_VIEW_COUNT";
    private String PRE_LIKE_COUNT = "IS_LIKE_COUNT";
    private String PRE_UNLIKE_COUNT = "IS_UNLIKE_COUNT";
    private String PRE_LIVE_LIKE_COUNT = "IS_LIVE_LIKE_COUNT";
    private String PRE_UNLIKE_COUNT_LIVE = "IS_UNLIKE_COUNT_LIVE";
    private String PRE_EXCLUSIVE_LIKE_COUNT = "IS_EXCLUSIVE_LIKE_COUNT";
    private String PRE_EXCLUSIVE_UNLIKE_COUNT = "IS_EXCLUSIVE_UNLIKE_COUNT";
    private String PRE_DOWNLOAD_COUNT = "IS_DOWNLOAD_COUNT";
    private String PRE_SEARCH_KEYWORD = "SEARCH KEYWORD";
    private String PRE_RANDOM = "random";

    private String PRE_DOWMAIN = "domain";
    private String PRE_IMG_DOWMAIN = "img_domain";
    private String PRE_IS_FIRST= "is_first";
    private String PRE_IS_SHOWCASE_DISPLA= "is_showcase_display";

    private String PRE_GET_IMAGES = "images";
    private String PRE_GET_LAST_IMAGES = "lastImageCount";
    private String PRE_GET_CAT_ID = "CAT_ID";

    private static final String FCM_UPDATE = "fcm_update";
    private static final String LANGUAGE = "language";
    private static final String THEME_SET = "theme_set";
    private static final String THEME_SYSTEM_SET = "theme_system_set";

    private static final String VIDEO_LIVE_WALLPAPER = "video_wallpaper";
    private static final String VIDEO_LIVE_WALLPAPER_TEMP = "video_wallpaper_temp";

    private static final String RATING_NOT_SHOW = "RATING_NOT_SHOW";
    private static final String SPLASH_IMG = "SPLASH_IMG";
    private static final String PUSHNOTIFICATION_ENABLE = "push_enable";

    private static final String DOUBLE_TAP = "DOUBLE TAP";
    private static final String EDGE_DEFAULT_SET = "edge default set";

    private static final String Exclusive_WALLPAPER = "four_k_wallpaper_path";
    private static final String Exclusive_WALLPAPER_TEMP = "four_k_wallpaper_path_temp";


    SharedPreferences pref;
    public static SettingStore store;

    public static SettingStore getInstance(Context context) {
        if (store == null)
            store = new SettingStore(context);
        return store;
    }

    private String PRE_IS_FILTER_SHOW = "IS_FILTER_SHOW";
    public void setIsFilterShow(boolean val) {
        pref.edit().putBoolean(PRE_IS_FILTER_SHOW, val).commit();
    }
    public boolean getIsFilterShow() {
        return pref.getBoolean(PRE_IS_FILTER_SHOW, false);
    }

    public static final String ADMOB_BANNER_ID = "admob_banner_id";
    public static final String ADMOB_MAIN_BANNER_ID = "admob_main_banner_id";
    public static final String ADMOB_BANNER_DETAIL_ID = "admob_banner_detail_id";
    public static final String ADMOB_INTERSTITIAL_ID = "admob_interstitial_id";
    public static final String ADMOB_INTERSTITIAL_DOWNLOAD_ID = "admob_interstitial_download_id";
    public static final String ADMOB_NATIVE_ID = "admob_native_id";
    public static final String ADMOB_DETAIL_NATIVE_ID = "admob_detail_native_id";
    public static final String ADMOB_INTERSTITIAL_SPLASH_ID = "admob_inters_splash";
    public static final String FB_NATIVE_ID = "fb_native_id";
    public static final String FB_BANNER_ID = "fb_banner_id";
    public static final String FB_BANNER_OTHER_ID = "fb_banner_other_id";
    public static final String FB_INTERSTITIAL_ID = "fb_intersti_id";
    public static final String ADMOB_OPEN_AD_ID = "admob_open_ad_id";

    public static final String GG_BANNER_ID = "gg_banner_id";
    public static final String GG_BANNER_ID_DETAIL = "gg_banner_id_detail";
    public static final String GG_INTERSTITIAL_ID = "gg_interstitial_id";
    public static final String GG_INTERSTITIAL_ID_SPLASH = "gg_interstitial_splash_id";
    public static final String GG_INTERSTITIAL_ID_DOWNLOAD = "gg_interstitial_download_id";
    public static final String GG_NATIVE_ID = "gg_native_id";
    public static final String GG_OPEN_ID = "gg_open_id";


    public static final String AM_NATIVE_ID = "am_native_id";
    public static final String AM_BANNER_ID = "am_banner_id";
    public static final String AM_DETAIL_BANNER_ID = "am_detail_banner_id";
    public static final String AM_MAINSCREEN_BANNER_ID = "am_mainscreen_banner_id";
    public static final String AM_INTERSTITIAL_ID = "am_interstitial_id";
    public static final String AM_SPLASH_INTERSTITIAL_ID = "am_splash_interstitial";
    public static final String AM_OPEN_ID = "am_open_id";

    public String getFromSettingPreference(String key) {

//        if(key.equalsIgnoreCase(SettingStore.FB_BANNER_ID) || key.equalsIgnoreCase(SettingStore.FB_BANNER_OTHER_ID) || key.equalsIgnoreCase(SettingStore.FB_INTERSTITIAL_ID)  || key.equalsIgnoreCase(SettingStore.FB_NATIVE_ID)){
//            return "";
//        }

//        if(key.equalsIgnoreCase(SettingStore.ADMOB_BANNER_DETAIL_ID) || key.equalsIgnoreCase(SettingStore.ADMOB_DETAIL_NATIVE_ID)
//                || key.equalsIgnoreCase(SettingStore.ADMOB_INTERSTITIAL_ID) || key.equalsIgnoreCase(SettingStore.ADMOB_INTERSTITIAL_SPLASH_ID)
//                || key.equalsIgnoreCase(SettingStore.ADMOB_OPEN_AD_ID) || key.equalsIgnoreCase(SettingStore.ADMOB_BANNER_ID)
//                || key.equalsIgnoreCase(SettingStore.ADMOB_NATIVE_ID) ){
//                return "";
//        }

//        if(key.equalsIgnoreCase(SettingStore.AM_BANNER_ID) || key.equalsIgnoreCase(SettingStore.AM_INTERSTITIAL_ID)
//                || key.equalsIgnoreCase(SettingStore.AM_NATIVE_ID) || key.equalsIgnoreCase(SettingStore.AM_OPEN_ID)
//                || key.equalsIgnoreCase(SettingStore.AM_SPLASH_INTERSTITIAL_ID) ){
//
//        }
//
//        if(key.equalsIgnoreCase(SettingStore.GG_BANNER_ID) || key.equalsIgnoreCase(SettingStore.GG_BANNER_ID_DETAIL)
//                || key.equalsIgnoreCase(SettingStore.GG_INTERSTITIAL_ID) || key.equalsIgnoreCase(SettingStore.GG_INTERSTITIAL_ID_SPLASH)
//                || key.equalsIgnoreCase(SettingStore.GG_NATIVE_ID) || key.equalsIgnoreCase(SettingStore.GG_OPEN_ID)){
//
//        }

        String value = pref.getString(key, "");
        if (TextUtils.isEmpty(value)) {
            return value;
        }
        return value.trim();
    }

    public void setInSettingPreference(String key, String value) {
        pref.edit().putString(key, value).commit();
    }

    private String PRE_IS_PRO= "is_pro";
    public void setIsPro(boolean val) {
        pref.edit().putBoolean(PRE_IS_PRO, val).commit();
    }
    public boolean getIsPro() {
        return pref.getBoolean(PRE_IS_PRO, false);
    }

    public void setIsDoubleTap(boolean val) {
        pref.edit().putBoolean(DOUBLE_TAP, val).commit();
    }
    public boolean getIsDoubleTap() {
        return pref.getBoolean(DOUBLE_TAP, false);
    }


    public String getSplashImg() {
        return pref.getString(SPLASH_IMG, "");
    }
    public void setSplashImg(String value) {
        pref.edit().putString(SPLASH_IMG, value).commit();
    }

    public SettingStore(Context context) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getFromSettingPreferenceInt(String key) {
        return pref.getInt(key, -1);
    }

    public void setInSettingPreferenceInt(String key, int value) {
        pref.edit().putInt(key, value).commit();
    }

    public String getVideoLiveWallpaperPath() {
        return pref.getString(VIDEO_LIVE_WALLPAPER, "");
    }
    public void setVideoLiveWallpaperPath(String value) {
        pref.edit().putString(VIDEO_LIVE_WALLPAPER, value).commit();
    }

    public String getVideoLiveWallpaperPathTemp() {
        return pref.getString(VIDEO_LIVE_WALLPAPER_TEMP, "");
    }
    public void setVideoLiveWallpaperPathTemp(String value) {
        pref.edit().putString(VIDEO_LIVE_WALLPAPER_TEMP, value).commit();
    }
    public int getLastImageCount() {
        return pref.getInt(PRE_GET_LAST_IMAGES, 0);
    }

    public void setLastImageCount(int value) {
        pref.edit().putInt(PRE_GET_LAST_IMAGES, value).commit();
    }

    public String getImages() {
        return pref.getString(PRE_GET_IMAGES, "");
    }

    public void setImages(String value) {
        pref.edit().putString(PRE_GET_IMAGES, value).commit();
    }

    public String getCatIdAuto() {
        return pref.getString(PRE_GET_CAT_ID, "");
    }

    public void setCatIdAuto(String value) {
        pref.edit().putString(PRE_GET_CAT_ID, value).commit();
        EventNotifier notifier =
                NotifierFactory.getInstance().getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_UPDATE_FOURK_WALL);
        notifier.eventNotify(EventTypes.EVENT_UPDATE_AUTO_WALL, null);
    }

    private static final String AUTO_CATEGORY_TEMP = "AUTO_CATEGORY_TEMP";
    public void setAutoCategoryTemp(String val) {
        pref.edit().putString(AUTO_CATEGORY_TEMP, val).commit();
    }

    public String getAutoCategoryTemp() {
        return pref.getString(AUTO_CATEGORY_TEMP, "");
    }

    public void setRandomNumber(String val) {
        pref.edit().putString(PRE_RANDOM, val).commit();
    }
    public String getRandomNumber() {
        return pref.getString(PRE_RANDOM, "");
    }

    public String getLocation() {
        return pref.getString(LOCATION, "");
    }

    public void setLocation(String value) {
        pref.edit().putString(LOCATION, value).commit();
    }

    public String getSaveResponse(String OperationID) {
        return pref.getString(OperationID, "");
    }

    public void setSaveResponse(String OperationID, String value) {
        pref.edit().putString(OperationID, value).commit();
    }

    private String PRE_IS_FCM_SEND = "IS_FCM_SEND";

    public void setIsFcmSend(boolean val) {
        pref.edit().putBoolean(PRE_IS_FCM_SEND, val).commit();
    }

    public boolean getIsFcmSend() {
        return pref.getBoolean(PRE_IS_FCM_SEND, false);
    }

    public void setRitingnotshow(boolean val) {
        Logger.e("pref",val+"rating");
        pref.edit().putBoolean(RATING_NOT_SHOW, val).commit();
    }
    public boolean getRatingNotShow() {
        return pref.getBoolean(RATING_NOT_SHOW, true);
    }

    public void setIsPushEnable(boolean val) {
        pref.edit().putBoolean(PUSHNOTIFICATION_ENABLE, val).commit();
    }
    public boolean isPushEnable() {
        return pref.getBoolean(PUSHNOTIFICATION_ENABLE, true);
    }

    public void setEdgeSetDafualt(boolean val) {
        pref.edit().putBoolean(EDGE_DEFAULT_SET, val).commit();
    }
    public boolean getEdgeSetDafualt() {
        return pref.getBoolean(EDGE_DEFAULT_SET, false);
    }

    public void setTheme(int val) {
        pref.edit().putInt(THEME_SET, val).commit();
    }
    public int getTheme() {
        return pref.getInt(THEME_SET, 0);
    }

    public void setLanguage(int val) {
//        switch (val)
//        {
//            case 0:
//                Log.e("lan","English");
//                break;
//            case 1:
//                Log.e("lan","Hindi");
//                break;
//        }
        pref.edit().putInt(LANGUAGE, val).commit();
    }
    public int getLanguage() {
        return pref.getInt(LANGUAGE, 0);
    }
    //0-ENGLISH,1-HINDI

    public void setSystemTheme(boolean val) {
        pref.edit().putBoolean(THEME_SYSTEM_SET, val).commit();
    }
    public boolean getSystemTheme() {
        return pref.getBoolean(THEME_SYSTEM_SET, false);
    }

    public void setUserID(String val) {
        pref.edit().putString(PRE_USER_ID, val).commit();
    }
    public String getUserID() {
        return pref.getString(PRE_USER_ID, "");
    }

    public void setOldDeviceID(String val) {
        pref.edit().putString(PRE_OLD_DEVICE_ID, val).commit();
    }
    public String getOldDeviceID() {
        return pref.getString(PRE_OLD_DEVICE_ID, "");
    }


    public void setShowCaseDisplay(boolean val) {
        pref.edit().putBoolean(PRE_IS_SHOWCASE_DISPLA, val).commit();
    }
    public boolean getShowCaseDisplay() {
        return pref.getBoolean(PRE_IS_SHOWCASE_DISPLA, false);
    }


    public void setFCM(String val) {
        pref.edit().putString(FCM_UPDATE, val).commit();
    }
    public String getFCM() {
        return pref.getString(FCM_UPDATE, "");
    }


    public void setViewCount(String val) {
        pref.edit().putString(PRE_VIEW_COUNT, val).commit();
    }
    public String getViewCount() {
        return pref.getString(PRE_VIEW_COUNT, "");
    }

    public void setLikeCount(String val) {
        pref.edit().putString(PRE_LIKE_COUNT, val).commit();
    }
    public String getLikeCount() {
        return pref.getString(PRE_LIKE_COUNT, "");
    }

    public void setUnLikeCount(String val) {
        pref.edit().putString(PRE_UNLIKE_COUNT, val).commit();
    }
    public String getUnLikeCount() {
        return pref.getString(PRE_UNLIKE_COUNT, "");
    }

    public void setSearchKeyword(String val) {
        pref.edit().putString(PRE_SEARCH_KEYWORD, val).commit();
    }
    public String getSearchKeyword() {
        return pref.getString(PRE_SEARCH_KEYWORD, "");
    }

    public void setLikeLiveCount(String val) {
        pref.edit().putString(PRE_LIVE_LIKE_COUNT, val).commit();
    }
    public String getLikeLiveCount() {
        return pref.getString(PRE_LIVE_LIKE_COUNT, "");
    }

    public void setUnLikeCountLive(String val) {
        pref.edit().putString(PRE_UNLIKE_COUNT_LIVE, val).commit();
    }
    public String getUnLikeCountLive() {
        return pref.getString(PRE_UNLIKE_COUNT_LIVE, "");
    }


    public void setLikeExclusiveCount(String val) {
        pref.edit().putString(PRE_EXCLUSIVE_LIKE_COUNT, val).commit();
    }
    public String getLikeExclusiveCount() {
        return pref.getString(PRE_EXCLUSIVE_LIKE_COUNT, "");
    }

    public void setUnLikeCountExclusive(String val) {
        pref.edit().putString(PRE_EXCLUSIVE_UNLIKE_COUNT, val).commit();
    }
    public String getUnLikeCountExclusive() {
        return pref.getString(PRE_EXCLUSIVE_UNLIKE_COUNT, "");
    }


    public void setDomain(String val) {
        pref.edit().putString(PRE_DOWMAIN, val).commit();
    }
    public String getDomain() {
        return pref.getString(PRE_DOWMAIN, "https://4kwallpaperhub.com/jesuswallpaper/");
//        return pref.getString(PRE_DOWMAIN, "https://jesuswallpapper.el.r.appspot.com/jesuswallpaper/");
//        return pref.getString(PRE_DOWMAIN, "https://jwapisv1-dot-jesuswallpapper.el.r.appspot.com/jesuswallpaper/");
//        return "https://jwapisv1-dot-jesuswallpapper.el.r.appspot.com/jesuswallpaper/";
//        return "http://206.189.141.200/jesuswallpaper/";

    }

    public void setImageDomain(String val) {
        pref.edit().putString(PRE_IMG_DOWMAIN, val).commit();
    }
    public String getImageDomain() {
        return pref.getString(PRE_IMG_DOWMAIN, "https://4kwallpaperhub.com/jesuswallpaper/");
//        return "http://206.189.141.200/jesuswallpaper/";
    }


    public void setDownloadCount(String val) {
        pref.edit().putString(PRE_DOWNLOAD_COUNT, val).commit();
    }
    public String getDownloadCount() {
        return pref.getString(PRE_DOWNLOAD_COUNT, "");
    }

    public void setTimeDurationIndex(int val) {
        pref.edit().putInt(PRE_TIME_DURATION_INDEX, val).commit();
    }

    public int getTimeDurationIndex() {
        return pref.getInt(PRE_TIME_DURATION_INDEX, 0);
    }

    public void setIsFirst(boolean val) {
        pref.edit().putBoolean(PRE_IS_FIRST, val).commit();
    }
    public boolean getIsFirst() {
        return pref.getBoolean(PRE_IS_FIRST, true);
    }

    private static final String FIRST_LAUNCH = "firstLaunch";
    public void setFirstTimeLaunch(boolean isFirstTime) {
        pref.edit().putBoolean(FIRST_LAUNCH, isFirstTime).commit();
    }

    public boolean FirstLaunch() {
        return pref.getBoolean(FIRST_LAUNCH, true);
//        return true;
    }

    public String getExclusiveWallpaperPath() {
        return pref.getString(Exclusive_WALLPAPER, "");
    }
    public void setExclusiveWallpaperPath(String value) {
        pref.edit().putString(Exclusive_WALLPAPER, value).commit();
    }

    public String getExclusiveWallpaperPathTemp() {
        return pref.getString(Exclusive_WALLPAPER_TEMP, "");
    }
    public void setExclusiveWallpaperPathTemp(String value) {
        pref.edit().putString(Exclusive_WALLPAPER_TEMP, value).commit();
    }



    public void setLastAutoChangedTime() {
        long time = Calendar.getInstance().getTimeInMillis();
        Log.i("LastAutoChangedTime", "LastAutoChangedTime:::" + time);
        pref.edit().putLong(LAST_AUTO_CHANGE_TIME, time).commit();
    }

    public void resetLastAutoChangedTime() {
        long time = 0;//Calendar.getInstance().getTimeInMillis();
        Log.i("LastAutoChangedTime", "Reset LastAutoChangedTime:::" + time);
        pref.edit().putLong(LAST_AUTO_CHANGE_TIME, time).commit();
    }

    public long getLastAutoChangedTime() {
        return pref.getLong(LAST_AUTO_CHANGE_TIME, 0);
    }

    private static final String LAST_AUTO_CHANGE_TIME_TEMP = "Last_Auto_Change_Time_Temp";
    public void setLastAutoChangedTimeTemp() {
        long time = Calendar.getInstance().getTimeInMillis();
        Log.i("LastAutoChangedTime", "LastAutoChangedTime:::" + time);
        pref.edit().putLong(LAST_AUTO_CHANGE_TIME_TEMP, time).commit();
    }

    public long getLastAutoChangedTimeTemp() {
        return pref.getLong(LAST_AUTO_CHANGE_TIME_TEMP, 0);
    }


    private String PRE_AUTO_IMAGES = "AUTO_images";
    private String PRE_GET_IMAGES_TEMP = "images_temp";

    public String getAutoWallImage() {
        return pref.getString(PRE_AUTO_IMAGES, "");
    }

    public void setAutoWallImage(String value) {
        pref.edit().putString(PRE_AUTO_IMAGES, value).commit();
    }

    public String getAutoWallImageTemp() {
        return pref.getString(PRE_GET_IMAGES_TEMP, "");
    }

    public void setAutoWallImageTemp(String value) {
        pref.edit().putString(PRE_GET_IMAGES_TEMP, value).commit();
    }

    private String PRE_TIME_DURATION_INDEX_TEMP = "time_duration_index_temp";
    public void setTimeDurationIndexTemp(int val) {
        pref.edit().putInt(PRE_TIME_DURATION_INDEX_TEMP, val).commit();
    }

    public int getTimeDurationIndexTemp() {
        return pref.getInt(PRE_TIME_DURATION_INDEX_TEMP, 0);
    }

    private static final String IS_REFFERER_EVENT_SEND = "REFERER_SEND";
    public void setIsReferSend(boolean val) {
        pref.edit().putBoolean(IS_REFFERER_EVENT_SEND, val).commit();
    }

    public boolean getIsReferSend() {
        return pref.getBoolean(IS_REFFERER_EVENT_SEND, false);
    }

    private static final String REFFERAL_ID = "REFFERAL_ID";
    public void setReferId(String val) {
        pref.edit().putString(REFFERAL_ID, val).commit();
    }

    public String getReferId() {
        return pref.getString(REFFERAL_ID, "");
    }

    private String rateAppDate = "RATE_APP_DATE";
    public void setRateDate(String val) {
        pref.edit().putString(rateAppDate, val).commit();
    }

    public String getRateDate() {
        return pref.getString(rateAppDate, "");
    }
}
