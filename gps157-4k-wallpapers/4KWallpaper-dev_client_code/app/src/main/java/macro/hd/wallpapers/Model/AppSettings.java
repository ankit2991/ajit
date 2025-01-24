package macro.hd.wallpapers.Model;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppSettings implements IModel, Serializable {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("package")
    @Expose
    private String _package;
    @SerializedName("app_version")
    @Expose
    private String appVersion;
    @SerializedName("ad_flag_new")
    @Expose
    private String adFlag;
    @SerializedName("apply_playstore")
    @Expose
    private String applyPlaystore;
    @SerializedName("is_random")
    @Expose
    private String isRandom;

    public String getSecond_ad() {
        return second_ad;
    }

    @SerializedName("second_ad")
    @Expose
    private String second_ad;

    @SerializedName("total_ad_count")
    @Expose
    private String totalAdCount;
    @SerializedName("ad_disable")
    @Expose
    private String adDisable;

    @SerializedName("call_service")
    @Expose
    private String callService;

    public String getDouble_sample() {
        return double_sample_new;
    }

    public String getIs_double_wall() {
        return is_double_wall;
    }

    @SerializedName("is_double_wall")
    @Expose
    private String is_double_wall;

    public String getIs_edge_wall() {
        return is_edge_wall;
    }

    public String getIs_status_saver() {
        return is_status_saver;
//        return "1";
    }
    @SerializedName("splash_time")
    @Expose
    private String splash_time;

    public int getSplashTime() {
        try {
            return Integer.parseInt(splash_time);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return 8000;
    }

    @SerializedName("admob_banner_id")
    @Expose
    private String admob_banner_id;

    public String getAdmob_main_banner_id() {
        return admob_main_banner_id;
    }

    @SerializedName("admob_main_banner_id")
    @Expose
    private String admob_main_banner_id;

    @SerializedName("admob_banner_detail_id")
    @Expose
    private String admob_banner_detail_id;

    @SerializedName("admob_interstitial_id")
    @Expose
    private String admob_interstitial_id;

    public String getAdmob_open_ap_id() {
        return admob_open_ap_id;
    }

    @SerializedName("admob_open_ap_id")
    @Expose
    private String admob_open_ap_id;

    @SerializedName("admob_native_id")
    @Expose
    private String admob_native_id;

    public String getAdmob_interstitial_download_id() {
        return admob_interstitial_download_id;
    }

    @SerializedName("admob_interstitial_download_id")
    @Expose
    private String admob_interstitial_download_id;

    public String getAdmob_native_detail_id() {
        return admob_native_detail_id;
    }

    @SerializedName("admob_native_detail_id")
    @Expose
    private String admob_native_detail_id;

    public String getAdmob_inters_splash() {
        return admob_inters_splash;
    }

    @SerializedName("admob_inters_splash")
    @Expose
    private String admob_inters_splash;


    @SerializedName("fb_interstitial_id")
    @Expose
    private String fb_interstitial_id;

    @SerializedName("fb_native_id")
    @Expose
    private String fb_native_id;

    @SerializedName("fb_banner_id")
    @Expose
    private String fb_banner_id;

    public String getFb_banner_other_id() {
        return fb_banner_other_id;
    }

    @SerializedName("fb_banner_other_id")
    @Expose
    private String fb_banner_other_id;

    @SerializedName("vpn")
    @Expose
    private String vpn;

    public String getApp_update_text() {
        return app_update_text;
    }

    @SerializedName("app_update_text")
    @Expose
    private String app_update_text;


    public String getImg_domain() {
        return img_domain;
    }

    @SerializedName("img_domain")
    @Expose
    private String img_domain;

    @SerializedName("is_status_saver")
    @Expose
    private String is_status_saver;

    @SerializedName("is_edge_wall")
    @Expose
    private String is_edge_wall;

    public String getIs_gradient_wall() {
        return is_gradient_wall;
    }

    @SerializedName("is_gradient_wall")
    @Expose
    private String is_gradient_wall;

    @SerializedName("double_sample_new")
    @Expose
    private String double_sample_new;

    public String getEdge_sample() {
        return edge_sample;
    }

    @SerializedName("edge_sample")
    @Expose
    private String edge_sample;

    public String getGradient_img() {
        return gradient_img;
    }

    @SerializedName("gradient_img")
    @Expose
    private String gradient_img;

    public String getStatus_sample() {
        return status_sample;
    }

    @SerializedName("status_sample")
    @Expose
    private String status_sample;

    @SerializedName("ADS_PER_PAGE")
    @Expose
    private String ADS_PER_PAGE;

    public int getAdsPerPage() {
        int temp= 4;
        try {
            temp = Integer.parseInt(ADS_PER_PAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    @SerializedName("ADS_PER_PAGE_DETAIL")
    @Expose
    private String ADS_PER_PAGE_DETAIL;

    @SerializedName("is_inapp_rating")
    @Expose
    private String is_inapp_rating;

    public int getAdsPerPageDetail() {
        int temp= 1;
        try {
            temp = Integer.parseInt(ADS_PER_PAGE_DETAIL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    public int getWallDirect() {
        try {
            return Integer.parseInt(is_set_wall_direct);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public String getIs_search_enable() {
        return is_search_enable;
    }


    public boolean isFilterEnable() {
//        return false;
        try {
            return Integer.parseInt(is_filter_enable) == 1;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isInAppRating() {
//        return false;
        try {
            return Integer.parseInt(is_inapp_rating) == 1;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isCatTitleEnable() {
//        return false;
        try {
            return Integer.parseInt(is_cat_title_enable) == 1;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return true;
    }



    public String getForce_update() {
        return force_update;
    }

    @SerializedName("force_update")
    @Expose
    private String force_update;

    public String getIn_app_update() {
        return in_app_update;
    }

    @SerializedName("in_app_update")
    @Expose
    private String in_app_update;

    public String getReview() {
//        return  "0";
        return review;
    }

    @SerializedName("review")
    @Expose
    private String review;

    public String getTransfer() {
        return transfer;
//        return "This application is no longer supported so please install new application using below button. \n\nAlso, you have to re-set your current live or exclusive wallpaper if already set. \nSorry for the inconvenience";
    }

    @SerializedName("transfer_new")
    @Expose
    private String transfer;

    public String getIsback() {
        return isback;
//        return "1";
    }


    @SerializedName("isback_transer_new")
    @Expose
    private String isback;

    public int getPost_count() {
        if(post_count==0)
            post_count=30;
        return post_count;
    }
    @SerializedName("page_count")
    @Expose
    private int post_count;

    public String getAd_freq_count() {
        return ad_freq_count;
    }

    public String getAd_freq_time() {
        return ad_freq_time;
    }

    @SerializedName("ad_freq_count")
    @Expose
    private String ad_freq_count;

    @SerializedName("ad_freq_time")
    @Expose
    private String ad_freq_time;

    @SerializedName("banner_ironsource")
    @Expose
    private String banner_ironsource;


    public boolean isBannerIronSource() {
        try {
            return Integer.parseInt(banner_ironsource) == 1;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return true;
    }

    public String getIs_god_wall() {
        return is_god_wall;
    }

    @SerializedName("is_god_wall")
    @Expose
    private String is_god_wall;

    public String getFb_ad_download() {
        return fb_ad_download;
    }

    public String getFb_native() {
        return fb_native;
    }

    @SerializedName("fb_ad_download")
    @Expose
    private String fb_ad_download;

    @SerializedName("fb_native")
    @Expose
    private String fb_native;

    public String getIs_mediation() {
        return is_mediation;
    }

    @SerializedName("is_mediation")
    @Expose
    private String is_mediation;

    public String getIs_mediation_native_detail() {
        return is_mediation_native_detail;
    }

    @SerializedName("is_mediation_native_detail")
    @Expose
    private String is_mediation_native_detail;

    @SerializedName("fourk_resize_bitmap")
    @Expose
    private String fourk_resize;

    @SerializedName("native_ad_count")
    @Expose
    private String native_ad_count;

    @SerializedName("admob_native_ad_count")
    @Expose
    private String admob_native_ad_count;

    public String getIs_splash_ad() {
        return is_splash_ad;
//        return "1";
    }

    @SerializedName("is_splash_ad")
    @Expose
    private String is_splash_ad;

    @SerializedName("is_new_detail")
    @Expose
    private String is_new_detail;

    public String getIs_splash_remote_config() {
        return is_splash_remote_config;
    }

    @SerializedName("is_splash_remote_config")
    @Expose
    private String is_splash_remote_config;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPackage() {
        return _package;
//        return "macro.hd.wallpapers&referrer=4K_123456789";
    }

    public void setPackage(String _package) {
        this._package = _package;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAdFlag() {
        return adFlag;
//        return "GG";
    }

    public void setAdFlag(String adFlag) {
        this.adFlag = adFlag;
    }

    public String getApplyPlaystore() {
        return applyPlaystore;
    }

    public void setApplyPlaystore(String applyPlaystore) {
        this.applyPlaystore = applyPlaystore;
    }

    public String getIsRandom() {
        return isRandom;
    }

    public void setIsRandom(String isRandom) {
        this.isRandom = isRandom;
    }

    public int getTotalAdCount() {
        return Integer.parseInt(totalAdCount);
    }

    public void setTotalAdCount(String totalAdCount) {
        this.totalAdCount = totalAdCount;
    }

    public String getAdDisable() {
        return adDisable;
    }

    public void setAdDisable(String adDisable) {
        this.adDisable = adDisable;
    }

    public String getCallService() {
        return callService;
    }

    public void setCallService(String callService) {
        this.callService = callService;
    }

    public int getFourk_resize() {
        try {
            return Integer.parseInt(fourk_resize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @SerializedName("is_filter_enable")
    @Expose
    private String is_filter_enable;

    @SerializedName("is_cat_title_enable")
    @Expose
    private String is_cat_title_enable;

    public boolean isSplashDelay() {
//        return false;
        try {
            return Integer.parseInt(is_splash_delay) == 1;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return true;
    }
    @SerializedName("is_splash_delay")
    @Expose
    private String is_splash_delay;

    @SerializedName("is_set_wall_direct")
    @Expose
    private String is_set_wall_direct;


    @SerializedName("is_search_enable")
    @Expose
    private String is_search_enable;

    public String getSearch_type() {
//        return "1";
        return search_type;
    }

    @SerializedName("search_type")
    @Expose
    private String search_type;

    public String getSearch_keywords() {
//        return "sex,kissing,sexy,kiss,fuck,porn,Threesome,Foursome,Reality porn,xxx,xx,ass,pussy,Oral,Anal,tits,cunt,couple,romance,lying,condom,naked,panty,topless,breast,bikini,adult,hugging,bed";
        return search_keywords;
    }

    @SerializedName("search_keywords")
    @Expose
    private String search_keywords;


    public int getNativeAdCount() {
        try {
            return Integer.parseInt(native_ad_count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 4;
    }

    public boolean getNewDetailScreen() {
//        return false;
        try {
            return Integer.parseInt(is_new_detail) == 1;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return true;
    }


    public int getAdmobNativeAdCount() {
        try {
            return Integer.parseInt(admob_native_ad_count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 2;
    }

    public String getAdmob_banner_id() {
        return admob_banner_id;
    }

    public String getAdmobDetail_banner_id() {
        return admob_banner_detail_id;
    }

    public String getAdmob_interstitial_id() {
        return admob_interstitial_id;
    }

    public String getAdmob_native_id() {
        return admob_native_id;
    }


    public String getFb_interstitial_id() {
        return fb_interstitial_id;
    }

    public String getFb_native_id() {
        return fb_native_id;
    }

    public String getFb_banner_id() {
        return fb_banner_id;
    }


    public boolean isVPN_Check() {
        try {
            return Integer.parseInt(vpn) == 1;
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return false;
    }

    public int Is_search_category() {
        try {
            return Integer.parseInt(is_search_category);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public int Is_search_external_api() {
        try {
            return Integer.parseInt(is_search_external_api);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    @SerializedName("is_search_category")
    @Expose
    private String is_search_category;

    @SerializedName("is_search_external_api")
    @Expose
    private String is_search_external_api;

    public String getUnderMaintenance() {
//        return "under maintentance";
        return underMaintenance;
    }

    @SerializedName("under_maintenance")
    @Expose
    private String underMaintenance;

    public String getFilter_home() {
        if(TextUtils.isEmpty(filter_home))
            return "0";
        return filter_home;
    }

    public String getCategoryFolder() {
        if(TextUtils.isEmpty(category_folder))
            return "category/newcat1/";
        return category_folder;
    }

    public String getFilter_live() {
        if(TextUtils.isEmpty(filter_live))
            return "0";
        return filter_live;
    }

    public String getFilter_exclusive() {
        if(TextUtils.isEmpty(filter_exclusive))
            return "0";
        return filter_exclusive;
    }

    @SerializedName("category_folder")
    @Expose
    private String category_folder;

    @SerializedName("filter_home")
    @Expose
    private String filter_home;

    @SerializedName("filter_live")
    @Expose
    private String filter_live;

    @SerializedName("filter_exclusive")
    @Expose
    private String filter_exclusive;

    @SerializedName("is_GG_show_failed")
    @Expose
    private String is_GG_show_failed;
    public boolean isGGshowOnFailed() {
        try{
            return is_GG_show_failed.equalsIgnoreCase("1");
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean getIs_AdManger_show_failed() {
        try{
            return is_AdManger_show_failed.equalsIgnoreCase("1");
        }catch (Exception e){
//            e.printStackTrace();
        }
        return false;
    }
    @SerializedName("is_AdManger_show_failed")
    @Expose
    private String is_AdManger_show_failed;

    public String getGg_banner_id() {
        return gg_banner_id;
    }

    @SerializedName("gg_banner_id")
    @Expose
    private String gg_banner_id;

    public String getGg_banner_id_detail() {
        return gg_banner_id_detail;
    }

    @SerializedName("gg_banner_id_detail")
    @Expose
    private String gg_banner_id_detail;

    public String getGg_interstitial_id() {
        return gg_interstitial_id;
    }

    public String getGg_interstitial_splash_id() {
        return gg_interstitial_splash_id;
    }

    public String getGg_native_id() {
        return gg_native_id;
    }

    @SerializedName("gg_interstitial_id")
    @Expose
    private String gg_interstitial_id;

    @SerializedName("gg_interstitial_splash_id")
    @Expose
    private String gg_interstitial_splash_id;

    public String getGg_interstitial_download_id() {
        return gg_interstitial_download_id;
    }

    @SerializedName("gg_interstitial_download_id")
    @Expose
    private String gg_interstitial_download_id;

    @SerializedName("gg_native_id")
    @Expose
    private String gg_native_id;

    public String getGg_open_id() {
        return gg_open_id;
    }

    @SerializedName("gg_open_id")
    @Expose
    private String gg_open_id;

    @SerializedName("is_pro_show")
    @Expose
    private String is_pro_show;
    public boolean is_pro_show() {
        try{
            return is_pro_show.equalsIgnoreCase("1");
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    // Admanager id
    public String getAm_interstitial_id() {
        return am_interstitial_id;
    }

    public String getAm_splash_interstitial() {
        return am_splash_interstitial;
    }

    @SerializedName("am_interstitial_id")
    @Expose
    private String am_interstitial_id;

    @SerializedName("am_splash_interstitial")
    @Expose
    private String am_splash_interstitial;

    public String getAm_open_id() {
        return am_open_id;
    }

    @SerializedName("am_open_id")
    @Expose
    private String am_open_id;

    public String getAm_banner_id() {
        return am_banner_id;
    }

    @SerializedName("am_banner_id")
    @Expose
    private String am_banner_id;

    public String getAm_detail_banner_id() {
        return am_detail_banner_id;
    }

    @SerializedName("am_detail_banner_id")
    @Expose
    private String am_detail_banner_id;

    public String getAm_mainscreen_banner_id() {
        return am_mainscreen_banner_id;
    }

    @SerializedName("am_mainscreen_banner_id")
    @Expose
    private String am_mainscreen_banner_id;

    public String getAm_native_id() {
        return am_native_id;
    }

    @SerializedName("am_native_id")
    @Expose
    private String am_native_id;

    @SerializedName("is_gg_facebook")
    @Expose
    private String is_gg_facebook;

    public boolean getGGWithFB() {
        try{
            return is_gg_facebook.equalsIgnoreCase("1");
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    /// greedy game id
}
