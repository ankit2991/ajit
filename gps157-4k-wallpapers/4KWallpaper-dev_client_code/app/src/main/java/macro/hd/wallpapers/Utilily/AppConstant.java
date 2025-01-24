package macro.hd.wallpapers.Utilily;


import android.graphics.Bitmap;

import macro.hd.wallpapers.BuildConfig;
import macro.hd.wallpapers.R;

/**
 * Created by hungama1 on 27/4/16.
 */
public class AppConstant {

    public static String EMAIL="roombapps@gmail.com";
    public static int ADS_PER_PAGE = -1;
    public static int ADS_PER_PAGE_DETAIL = 1;

    public static int color[]=new int[]{R.color.color_1,R.color.color_2,R.color.color_3,R.color.color_4,R.color.color_5,R.color.color_6,R.color.color_7,R.color.color_8,R.color.color_9,R.color.color_10};
    public static int colorWhite[]=new int[]{R.color.color_11,R.color.color_12,R.color.color_13,R.color.color_14,R.color.color_15,R.color.color_16,R.color.color_17,R.color.color_18,R.color.color_19,R.color.color_20};

    public static String AUTO_WALLPAPER_FILE_NAME = "Auto.jpg";
    public static String AUTO_WALLPAPER_TEMP_FILE_NAME = "AutoTemp.jpg";
    public static final int CAROUSEL_ROTATE_TIME = 2000;
    public static final boolean CAROUSEL_AUTO_ROTATE = true;

    public static final String DEFAULT = "Exclusive1.jpg";
    public static final String DEFAULT_EDGE = "edge_img2.jpg";

    public static boolean isWallSetDirectly=true;

//    public static final int TYPE_UNSPLASH=2;
    public static final Bitmap.Config bitmapConfig8888 = Bitmap.Config.ARGB_8888;

    public static final int AD_DISPLAY_FREQUENCY = 12;
    public static final int TILE_SIZE = 3;

    public static final int time_frame_name[] = new int[]{R.string.min_30,R.string.hr_1
             ,R.string.hr_2,R.string.hr_6 ,R.string.hr_12,R.string.dy_1 ,R.string.dy_3 };

    public static final int time_frame_duration[] = new int[] {1800000, 3600000,
            7200000, 21600000, 43200000, 86400000, 259200000 };

    public static final int REFERESH_TIME_OUT = 2000;

    // if delete option to remove wallpaper
    public static final boolean IS_DELETE=false;
//    public static final boolean IS_DELETE=BuildConfig.DEBUG;

    public static final boolean IS_ASC=BuildConfig.DEBUG;

    public static final String STORE_LINK = "https://bit.ly/3sqfPRP";

    public static final String DOWNLOAD_EXTENTION=".jpg";
    public static final String DOWNLOAD_EXTENTION_VIDEO=".mp4";

    public static final String TERMS_URL="privacy_policy_new.html";
    public static final String HELP_URL = "live_help.html";

// 9825404542Dd#
    public static final String START_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'+'SS:SS";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    private final static String VERSION="v1/";
    public final static String URL_UPDATE_GCM_TOKAN = VERSION+"update_device_token.php"; //done
    public final static String URL_CATEGORY_LIST = VERSION+"category_list_update.php";  //done
    public final static String URL_POST_LIST = VERSION+"post_list_final.php"; //done
    public final static String URL_POST_LIST_ASC = VERSION+"post_list_final_asc.php"; //done
//    public final static String URL_POST_LIST = "post_list_events.php";

    public final static String URL_DELETE = VERSION+"delete_post_new.php";// not able to delete file
    public final static String URL_DELETE_LIVE = VERSION+"delete_post_live_new.php";// not able to delete file
    public final static String URL_DELETE_STOCK = VERSION+"delete_post_stock_new.php";// not able to delete file

    public final static String URL_GET_TRENDING = VERSION+"trending_final.php"; //done
    public final static String URL_ALL_DATA = VERSION+"add_all_new.php"; // done

    public static final String URL_IN_VIDEO_WALLPAPER = VERSION+"live_wallpaper_list.php"; //done
    public static final String URL_IN_VIDEO_WALLPAPER_ASC = VERSION+"live_wallpaper_list.php"; //done

    public final static String URL_DOUBLE_WALLPAPER = VERSION+"post_list_double_page.php"; //done post_list_double
    public final static String URL_EXCLUSIVE_WALLPAPER = VERSION+"post_list_exclusive.php"; //done
    public final static String URL_EXCLUSIVE_WALLPAPER_ASC = VERSION+"post_list_exclusive.php"; //done

    public final static String URL_SEARCH = VERSION+"search_list.php"; //done

    public final static String URL_GET_LIKE = VERSION+"get_like.php"; //done
    public final static String URL_USER_UPDATE = VERSION+"update_user.php"; //done
    public final static String URL_USER_FEEDBACK = VERSION+"user_feedback.php";//done

    public static final String URL_AUTO_WALLPAPER= VERSION+"get_auto_wallpaper_img_new.php"; //done
    public static final String URL_SEARCH_WALLPAPER= VERSION+"get_search_wallpaper.php"; //done

}
