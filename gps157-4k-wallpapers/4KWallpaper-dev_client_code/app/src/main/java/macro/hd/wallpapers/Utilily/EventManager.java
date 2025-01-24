package macro.hd.wallpapers.Utilily;

import android.os.Bundle;
import android.text.TextUtils;

import macro.hd.wallpapers.WallpapersApplication;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hungam on 13/5/19.
 */

public class EventManager {

    public static String LBL_COMMUNICATION="Communication";
    public static String LBL_TRANSFER_FILE="Transfer To Picture";
    public static String LBL_HOME="Home Screen";
    public static String LBL_FILTER="Filter";
    public static String LBL_GG_Advertise="GG Advertise";
    public static String LBL_LIVE_WALLPAPER="Live Wallpaper Screen";
    public static String LBL_EXCLUSIVE_WALLPAPER="Exclusive Wallpaper Screen";
    public static String LBL_Advertise="Advertise";
    public static String LBL_Advertise_IS="IranSource";
    public static String LBL_SEARCH="Search";
    public static String LBL_REMOTE_CONFIG="Remote Config";
    public static String LBL_VPN="VPN";
    public static String LBL_STORE="Playstore Download";
    public static String LBL_USER_NOT_VALID="User Not Valid";
    public static String LBL_REFERRER_PAGE="Referer";
    public static String ATR_KEY_REFERRER_DETAILS="Detail";
    public static String LBL_AM_Advertise="AdManger Advertise";
    public static String LBL_qureka_Advertise="qureka Advertise";

    public static String ATR_KEY_RATE="Rate Click";
    public static String LBL_DOUBLE_WALLPAPER="Double Wallpaper";
    public static String LBL_MY_DOWNLOAD="My Download";
    public static String LBL_PUSH="Push Notification";
    public static String LBL_FOURK_ERROR = "OOM Error";

    public static String LBL_EDGE_WALLPAPER="Edge Wallpaper";
    public static String LBL_GRADIENT_WALLPAPER="Gradiant Wallpaper";
    public static String LBL_STATUS_SAVER="Status Saver";

    public static String ATR_KEY_SHARE="Share App";
    public static String ATR_VALUE_SHARE="Share Click";

    public static String ATR_KEY_ABOUT="About Us";
    public static String ATR_VALUE_ABOUT="About Click";


    public static String ATR_KEY_SETTING="Settings";
    public static String ATR_VALUE_SETTING="Settings Click";

    public static String ATR_KEY_PRO="Pro App";
    public static String ATR_VALUE_PRO="Pro Click";
    public static String ATR_VALUE_PRO_TOOLBAR="Pro Click Toolbar";

    public static String ATR_KEY_FEEDBACK="Send Feedback";
    public static String ATR_VALUE_FEEDBACK="Send Feedback Click";

    public static String ATR_KEY_REPORT="Report";
    public static String ATR_VALUE_REPORT="Report Click";

    public static String ATR_KEY_TERMS="Terms";
    public static String ATR_VALUE_TERMS="Terms Click";

    public static String ATR_KEY_FAV="Fav Page";
    public static String ATR_VALUE_FAV="Fav Page Click";

    public static String ATR_KEY_THEME="Theme Page";
    public static String ATR_VALUE_THEME="Theme Click";

    public static String ATR_KEY_LIVE_WALL="Live Wallpaper Screen";
    public static String ATR_KEY_AUTO_WALL="Auto Wallpaper Screen";
    public static String ATR_KEY_LIVE_ACTION="Action";
    public static String ATR_VALUE_LIVE_WALL="Live Wallpaper Click";
    public static String ATR_VALUE_SET_WALL="Live Wallpaper Set";

    public static String ATR_KEY_LIVE_WALL_SETTING="Live Wallpaper Setting Screen";
    public static String ATR_VALUE_LIVE_WALL_SETTING="Live Wallpaper Setting Click";

    public static String ATR_KEY_LIVE_WALL_WORKING="Live Wallpaper Setting Screen";
    public static String ATR_VALUE_LIVE_WALL_WORKING="Live Wallpaper Setting Click";

    public static String ATR_KEY_CACHE_CLEAR="Cache Clear";
    public static String ATR_KEY_PUSH_NOTIFICATION="Notification Enable";

    public static String ATR_KEY_CATEGORY_CLICK="Category Cilck";
    public static String ATR_KEY_STOCK_CATEGORY_CLICK="Stock Category Cilck";
    public static String ATR_KEY_COLOR_CATEGORY_CLICK="color Category Cilck";
    public static String ATR_KEY_LIVE_WALLPAPER_CLICK="Live Wallpaper Cilck";
    public static String ATR_KEY_GOD_CLICK="God App Cilck";
    public static String ATR_KEY_SET_LIVE_WALLPAPER="Set Live Wallpaper";
    public static String ATR_KEY_NOT_DOWNLOAD_LIVE_WALLPAPER="Unable Download Wallpaper";
    public static String ATR_KEY_SET_EXCLUSIVE_WALLPAPER="Set Exclusive Wallpaper";
    public static String ATR_KEY_OPEN_EXCLUSIVE_WALLPAPER="Open Exclusive Wallpaper";
    public static String ATR_KEY_CREATE_EXCLUSIVE_WALLPAPER="Create Exclusive Wallpaper";

    public static String ATR_KEY_SPLASH_AD="splash_ad";
    public static String ATR_KEY_ADMOB="admob";
    public static String ATR_KEY_ADMOB_SPLASH="intert_splash";
    public static String ATR_KEY_FB="FB";
    public static String ATR_KEY_FB_NATIVE="FB_NATIVE";
    public static String ATR_KEY_ADMOB_NATIVE="ADMOB_DETAIL_NATIVE";
    public static String ATR_KEY_ADMOB_BANNER="Admob Banner";
    public static String ATR_KEY_ADMOB_OPEN_AD="Admob Open Ad";
    public static String ATR_KEY_FB_BANNER="FB Banner";
    public static String ATR_KEY_SEARCH_OPEN="Search Open";
    public static String ATR_KEY_SEARCH_KEYWORD="Search keyword";
    public static String ATR_KEY_AM_BANNER="AM Banner";
    public static String ATR_KEY_AM_NATIVE="ADMANAGER_NATIVE";
    public static String ATR_KEY_AM_OPEN_AD="AdManager Open Ad";

    public static String ATR_KEY_DOUBLE="Screen Open";
    public static String ATR_EDGE_WALLPAPER="Edge Open";
    public static String ATR_GRADIENT_WALLPAPER="gradient Open";
    public static String ATR_GRADIENT_SET="gradient set download";
    public static String ATR_STATYS_SAVER="Status Open";
    public static String ATR_EDGE_Preview="Edge Preview";
    public static String ATR_EDGE_SET="Edge Set";

    public static String ATR_VALUE_SET_DOUBLE="Set Wallpaper";


    public static String ATR_KEY_PUSH_CATEGORY="Category Push";
    public static String ATR_KEY_PUSH="Push Notification";
    public static String ATR_KEY_IMAGE_PUSH="Image Push Notification";
    public static String ATR_VALUE_SUCCESS="Image Loaded";
    public static String ATR_VALUE_FAIL="Image Failed";

    public static String ATR_VALUE_OPEN="Open";
    public static String ATR_VALUE_DISPLAY="Display";
    public static String ATR_VALUE_NOT_DISPLAY="Fail";

    public static String ATR_KEY_REMOTE="Remote Value";

    public static String ATR_KEY_PURCHASE="Purchase";

    public static String LBL_WEB_DEEPLINK="Web Deeplink";

    public static String ATR_KEY_GG_BANNER="GG Banner";
    public static String ATR_KEY_GG="Interstitial";
    public static String ATR_KEY_GG_SPASH="Interstitial_splash";
    public static String ATR_KEY_GG_NATIVE="GG_NATIVE";

    public static String ATR_KEY_GG_OPEN="GG Open";

    public static void sendEvent(String eventLable,String attributeKey,String attributeValue){
        try {
            if(!TextUtils.isEmpty(attributeKey))
                attributeKey=attributeKey.replace(" ","_");

            if(!TextUtils.isEmpty(eventLable))
                eventLable=eventLable.replace(" ","_");

            Logger.e("EventManager", "Lable:" + eventLable + " :: Key:" + attributeKey + " :: Value:" + attributeValue);

            if (TextUtils.isEmpty(attributeKey)) {
                Bundle bundle = new Bundle();
                WallpapersApplication.getApplication().getFirebaseAnalytics().logEvent(eventLable, bundle);

                //---- Send Flurry Event
                Map<String, String> flurryMap = new HashMap<String, String>();
                FlurryAgent.logEvent(eventLable, flurryMap);
            } else {
                Bundle bundle = new Bundle();
                bundle.putString(attributeKey, attributeValue);
                WallpapersApplication.getApplication().getFirebaseAnalytics().logEvent(eventLable, bundle);

                //---- Send Flurry Event
                Map<String, String> flurryMap = new HashMap<String, String>();
                flurryMap.put(attributeKey, attributeValue);
                FlurryAgent.logEvent(eventLable, flurryMap);

            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }
}
