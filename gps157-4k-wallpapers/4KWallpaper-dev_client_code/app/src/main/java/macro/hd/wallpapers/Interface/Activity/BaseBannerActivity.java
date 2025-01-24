package macro.hd.wallpapers.Interface.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.greedygame.core.adview.general.AdLoadCallback;
import com.greedygame.core.adview.general.GGAdview;
import com.greedygame.core.models.general.AdErrors;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.BannerListener;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.WallpapersApplication;

public abstract class BaseBannerActivity extends AppCompatActivity {
    SettingStore settings;
    private Locale mCurrentLocale;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings=SettingStore.getInstance(this);
    }

    public void setTheme(){
        SettingStore settingStore = SettingStore.getInstance(this);
        if (settingStore.getTheme()==0) {
            setTheme(R.style.AppTheme);
        }else if (settingStore.getTheme()==1) {
            setTheme(R.style.AppTheme1);
        }
    }

    public void setLocale() {
        String languageCode="";
        SettingStore settingStore = SettingStore.getInstance(this);
        if (settingStore.getLanguage()==0) {
           languageCode="en";
        }else if (settingStore.getLanguage()==1) {
            languageCode="hi";
        }

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            createConfigurationContext(config);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        } else {
            config.locale = locale;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }


    private ProgressDialog progressDialog;
    private ProgressDialog progressDialogMsg;

    public CircularProgressIndicator getProgress() {
        return mProgress;
    }

//    private ProgressBar mProgress;
    private CircularProgressIndicator mProgress;
    private TextView tv;
    public void showLoadingDialog(String msg){

        try {
            if (progressDialog == null) {
                progressDialog = CommonFunctions.createProgressDialog(this);

                mProgress=progressDialog.findViewById(R.id.circularProgressbar);

                if(mProgress!=null) {
                    mProgress.setProgress(0,100);   // Main Progress
    //                mProgress.setSecondaryProgress(100); // Secondary Progress
    //                mProgress.setMax(100); // Maximum Progress
                }

                if(!isFinishing()) {
                    progressDialog.show();
                }

            } else if(!progressDialog.isShowing()) {
                if(!isFinishing()) {
                    progressDialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void hideLoadingDialog(){
        if(progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        if(progressDialogMsg!=null && progressDialogMsg.isShowing()){
            progressDialogMsg.dismiss();
        }
    }

    public TextView getTv() {
        return tv;
    }

    public void showLoadingDialogDownload(String msg){
        try {
            showLoadingDialog(msg);

            progressDialog.findViewById(R.id.progress).setVisibility(View.GONE);
            progressDialog.findViewById(R.id.rl_download).setVisibility(View.VISIBLE);
            mProgress=progressDialog.findViewById(R.id.circularProgressbar);
            tv=progressDialog.findViewById(R.id.tv);

            if(mProgress!=null) {
                mProgress.setProgress(0,100);   // Main Progress
//                mProgress.setSecondaryProgress(100); // Secondary Progress
//                mProgress.setMax(100); // Maximum Progress
                //                mProgress.setProgressDrawable(drawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoadingDialogMsg(String msg){

        try {
            if (progressDialogMsg == null) {
                progressDialogMsg = CommonFunctions.createProgressDialogMsg(this,msg);
                if(!isFinishing()) {
                    progressDialogMsg.show();
                }

            } else if(!progressDialogMsg.isShowing()) {
                if(!isFinishing()) {
                    TextView textView = progressDialogMsg.findViewById(R.id.txt_msg);
                    textView.setText(msg);
                    progressDialogMsg.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoadingProgress(){
        showLoadingDialog("");//"Please Wait"
//        findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
    }

    public void dismissLoadingProgress(){
        hideLoadingDialog();
//        findViewById(R.id.rl_progress).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        progressDialog=null;
        progressDialogMsg=null;

        mProgress=null;
        tv=null;
        adView=null;

        try {
            if(ggAdView!=null && isGGBanerLoaded){
                isGGBanerLoaded=false;
            }else if(mAdManagerAdView!=null && isAdManagerOpen){
                isAdManagerOpen=false;
                mAdManagerAdView.destroy();
            }else if(adView!=null && isAdMobBannerLoaded){
                isAdMobBannerLoaded=false;
                adView.destroy();
            }else {
                if (WallpapersApplication.getApplication().getIronSourceBanner() != null && !WallpapersApplication.getApplication().getIronSourceBanner().isDestroyed())
                    IronSource.destroyBanner(WallpapersApplication.getApplication().getIronSourceBanner());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////// banner
    private AdView adView;
    public void requestBanner(Activity activity, FrameLayout layout, boolean isFromDetail,boolean isFormainScreen){
        if (CommonFunctions.isStoreVersion(this) && !WallpapersApplication.getApplication().isProUser()) {

            //TODO lets ignore backend, always take IronSource
            loadIronSrcBanner(activity,layout,isFromDetail,isFormainScreen);
            /*
            if(WallpapersApplication.getApplication().getCurrentAdType()== WallpapersApplication.AdType.ADMOB){
                if(layout!=null) {
                    if(isFromDetail)
                        loadAdmobDetailBanner(activity,layout,isFromDetail);
                    else
                        loadBanner(activity, layout, isFromDetail,isFormainScreen);
                }
            }else if(WallpapersApplication.getApplication().getCurrentAdType()== WallpapersApplication.AdType.IS){
                if(WallpapersApplication.getApplication().getSettings().isBannerIronSource()){
                    loadIronSrcBanner(activity,layout,isFromDetail,isFormainScreen);
                }else{
                    if(layout!=null){
                        if(isFromDetail)
                            loadAdmobDetailBanner(activity,layout,isFromDetail);
                        else
                            loadBanner(activity,layout,isFromDetail,isFormainScreen);
                    }
                }
            }else if(WallpapersApplication.getApplication().getCurrentAdType()== WallpapersApplication.AdType.GG){
                if(CommonFunctions.isAdManagerShow()){
                    loadAdMangerBanner(activity,layout,isFromDetail,isFormainScreen);
                }else if(CommonFunctions.isGreedyGameShow())
                    loadGreedyGameBanner(activity,layout,isFromDetail,isFormainScreen);
            }else{
                if(layout!=null)
                    layout.setVisibility(View.GONE);
            }
            */
        }else{
            if(layout!=null) {
//                layout.getLayoutParams().width= ViewGroup.LayoutParams.MATCH_PARENT;
//                layout.getLayoutParams().height= (int) activity.getResources().getDimension(R.dimen.value_50);
//                layout.setBackgroundColor(Color.RED);
//                layout.setVisibility(View.VISIBLE);
                layout.setVisibility(View.GONE);
            }
        }
    }

    private static final String TAG = "BaseActivity";

    private void loadIronSrcBanner(final Activity activity, final FrameLayout layout, final boolean isFromDetail,boolean isFormainScreen){

        if(WallpapersApplication.getApplication().getIronSourceBanner()!=null && !WallpapersApplication.getApplication().getIronSourceBanner().isDestroyed()){
            bannerFailedOn(activity,layout,isFromDetail,isFormainScreen);
            return;
        }
        IronSourceBannerLayout banner = IronSource.createBanner(activity, ISBannerSize.SMART);
        WallpapersApplication.getApplication().setIronSourceBanner(banner);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(WallpapersApplication.getApplication().getIronSourceBanner(), 0, layoutParams);

        try {
            layout.setMinimumHeight((int) activity.getResources().getDimension(R.dimen.banner_hight_temp));
        } catch (Exception e) {
            e.printStackTrace();
        }

        WallpapersApplication.getApplication().getIronSourceBanner().setBannerListener(new BannerListener() {
            @Override
            public void onBannerAdLoaded() {
                // Called after a banner ad has been successfully loaded
                Logger.e(TAG,"irons onBannerAdLoaded");
                EventManager.sendEvent(EventManager.LBL_Advertise_IS, EventManager.ATR_KEY_ADMOB_BANNER, "onBannerAdLoaded");
            }

            @Override
            public void onBannerAdLoadFailed(IronSourceError error) {
                // Called after a banner has attempted to load an ad but failed.
                Logger.e(TAG,"irons onBannerAdLoadFailed");
                EventManager.sendEvent(EventManager.LBL_Advertise_IS, EventManager.ATR_KEY_ADMOB_BANNER, "onBannerAdLoadFailed:"+error.getErrorCode());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        layout.removeAllViews();
                        bannerFailedOn(activity,layout,isFromDetail,isFormainScreen);
                    }
                });
            }

            @Override
            public void onBannerAdClicked() {
                // Called after a banner has been clicked.
                Logger.e(TAG,"irons onBannerAdClicked");
            }

            @Override
            public void onBannerAdScreenPresented() {
                // Called when a banner is about to present a full screen content.
                Logger.e(TAG,"irons onBannerAdScreenPresented");
            }

            @Override
            public void onBannerAdScreenDismissed() {
                // Called after a full screen content has been dismissed
                Logger.e(TAG,"irons onBannerAdScreenDismissed");
            }

            @Override
            public void onBannerAdLeftApplication() {
                // Called when a user would be taken out of the application context.
                Logger.e(TAG,"irons onBannerAdLeftApplication");
            }
        });

        IronSource.loadBanner(WallpapersApplication.getApplication().getIronSourceBanner());
    }

    AdManagerAdView mAdManagerAdView;
    boolean isAdManagerOpen;
    protected void loadAdMangerBanner(final Activity activity, final FrameLayout layout,boolean isFromDetail,boolean is_mainscreen){
        if(layout!=null) {

            if (!CommonFunctions.isStoreVersion(this)) {
                try {
                    if(layout!=null)
                        layout.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            String adUnitId ;
            if(CommonFunctions.isTestAdEnable()){
                adUnitId="/6499/example/banner";
            }else{
                if(is_mainscreen){
                    adUnitId = settings.getFromSettingPreference(SettingStore.AM_MAINSCREEN_BANNER_ID);
                }else if(isFromDetail){
                    adUnitId = settings.getFromSettingPreference(SettingStore.AM_DETAIL_BANNER_ID);
                }else
                    adUnitId = settings.getFromSettingPreference(SettingStore.AM_BANNER_ID);
            }

            if (CommonFunctions.isEmpty(adUnitId)){
                if(layout!=null)
                    layout.setVisibility(View.GONE);
                return;
            }

            Logger.e(TAG,"loadAdMangerBanner:"+adUnitId);

            mAdManagerAdView = new AdManagerAdView(this);
            mAdManagerAdView.setAdUnitId(adUnitId);  //Replace with your Ad Unit ID here
            layout.removeAllViews();
            layout.addView(mAdManagerAdView);

            AdSize adSize = getAdSize(activity,null);

            try {
//                if(activity instanceof StatusViewPagerActivity || activity instanceof DoubleWallpaperActivity || activity instanceof AutoWallChangerActivity || activity instanceof WallpaperDetailActivity || activity instanceof EdgeSettings || activity instanceof CreateExclusiveActivity)
                layout.setMinimumHeight(adSize.getHeightInPixels(activity));
            } catch (Exception e) {
                e.printStackTrace();
            }

            mAdManagerAdView.setAdSizes(adSize);

            mAdManagerAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    isAdManagerOpen=true;
                    EventManager.sendEvent(EventManager.LBL_AM_Advertise,EventManager.ATR_KEY_AM_BANNER,"onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    // Code to be executed when an ad request fails.
                    Log.d("GGADS","Ad Load Failed ");
                    isAdManagerOpen=false;
                    EventManager.sendEvent(EventManager.LBL_AM_Advertise,EventManager.ATR_KEY_AM_BANNER,"onAdLoadFailed");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(layout!=null)
                                layout.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onAdOpened() {
                    Log.d("GGADS","onAdOpened");

                }

                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdClosed() {
                    Log.d("GGADS","onAdClosed");

                }
            });

            AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
            mAdManagerAdView.loadAd(adRequest);

        }
    }

    private void bannerFailedOn(final Activity activity, final FrameLayout layout,boolean isFromDetail,boolean isFormainScreen){
        if(CommonFunctions.isAdManagerShow()){
            loadAdMangerBanner(activity,layout,isFromDetail,isFormainScreen);
        }else if(CommonFunctions.isGreedyGameShow()){
            loadGreedyGameBanner(activity,layout,isFromDetail,isFormainScreen);
        }
    }

    private GGAdview ggAdView;
    private boolean isGGBanerLoaded;
    protected void loadGreedyGameBanner(final Activity activity, final FrameLayout layout,boolean isFromDetail,boolean isFormainScreen){
        if(layout!=null) {

            if(WallpapersApplication.getApplication().isProUser()){
                return;
            }

            if (!CommonFunctions.isStoreVersion(this)) {
                return;
            }

            String adUnitId="";
            if(isFromDetail){
                adUnitId = settings.getFromSettingPreference(SettingStore.GG_BANNER_ID_DETAIL);
            }else
                adUnitId = settings.getFromSettingPreference(SettingStore.GG_BANNER_ID);

            Logger.e(TAG,"loadGreedyGameBanner:"+adUnitId);

            if (CommonFunctions.isEmpty(adUnitId)){
                if(CommonFunctions.isAdManagerShow())
                    loadAdMangerBanner(activity,layout,isFromDetail,isFormainScreen);
                return;
            }

            ggAdView = new GGAdview(this);
            ggAdView.setUnitId(adUnitId);  //Replace with your Ad Unit ID here
            ggAdView.setAdsMaxHeight(250); //Value is in pixels, not in dp

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 200);
            layout.addView(ggAdView,layoutParams);
            ggAdView.loadAd(new AdLoadCallback(){
                                @Override
                                public void onReadyForRefresh() {
                                    Log.d("GGADS","Ad Ready for refresh");
                                }
                                @Override
                                public void onUiiClosed() {
                                    Log.d("GGADS","Uii closed");
                                }
                                @Override
                                public void onUiiOpened() {
                                    Log.d("GGADS","Uii Opened");
                                }

                                @Override
                                public void onAdLoaded() {
                                    Log.d("GGADS","Ad Loaded");
                                    isGGBanerLoaded=true;
                                    EventManager.sendEvent(EventManager.LBL_GG_Advertise,EventManager.ATR_KEY_GG_BANNER,"onAdLoaded");
                                }

                                @Override
                                public void onAdLoadFailed(@NotNull AdErrors adErrors) {
                                    Log.d("GGADS","Ad Load Failed ");
                                    isGGBanerLoaded=false;
                                    EventManager.sendEvent(EventManager.LBL_GG_Advertise,EventManager.ATR_KEY_GG_BANNER,"onAdLoadFailed");
                                    try {
                                        if(activity!=null)
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(layout!=null)
                                                        layout.setVisibility(View.GONE);
                                                }
                                            });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
            );
        }
    }

    public void loadAdmobDetailBanner(Activity activity, FrameLayout adContainerView,boolean isFromDetail){
        adView = new AdView(this);

        String adUnitId = settings.getFromSettingPreference(SettingStore.ADMOB_BANNER_DETAIL_ID);

        if (CommonFunctions.isTestAdEnable()) {
            adUnitId = getResources().getString(R.string.ad_unit_banner_id_test);
        }else
            adUnitId = settings.getFromSettingPreference(SettingStore.ADMOB_BANNER_DETAIL_ID);

        if (CommonFunctions.isEmpty(adUnitId)){
            bannerFailedOn(activity,adContainerView,isFromDetail,false);
            return;
        }

//        adView.setAdUnitId(getString(com.app.jesuslivewallpaper.R.string.ad_unit_banner_id));
        adView.setAdUnitId(adUnitId);

        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize(activity,adContainerView);
        adView.setAdSize(adSize);

        try {
//            if(activity instanceof StatusViewPagerActivity || activity instanceof DoubleWallpaperActivity || activity instanceof AutoWallChangerActivity || activity instanceof WallpaperDetailActivity || activity instanceof EdgeSettings  || activity instanceof CreateExclusiveActivity)
            adContainerView.setMinimumHeight(adSize.getHeightInPixels(activity));
        } catch (Exception e) {
            e.printStackTrace();
        }

        adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                isAdMobBannerLoaded=false;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bannerFailedOn(activity,adContainerView,isFromDetail,false);
                    }
                });
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }


            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Logger.e(TAG,"Banner onAdClosed");
            }
        });

        AdRequest.Builder adRequestBuilder=new AdRequest.Builder();

        AdRequest adRequest = adRequestBuilder.build();
        isAdMobBannerLoaded=true;
        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private boolean isAdMobBannerLoaded;
    private void loadBanner(final Activity activity, final FrameLayout adContainerView,boolean isFromDetail,boolean isFormainScreen) {

        adView = new AdView(this);

        String adUnitId;
        if (CommonFunctions.isTestAdEnable())
            adUnitId = getResources().getString(R.string.ad_unit_banner_id_test);
        else if(isFormainScreen){
            adUnitId = settings.getFromSettingPreference(SettingStore.ADMOB_MAIN_BANNER_ID);
        }else {
            adUnitId = settings.getFromSettingPreference(SettingStore.ADMOB_BANNER_ID);
        }

        if (CommonFunctions.isEmpty(adUnitId)){
            bannerFailedOn(activity,adContainerView,isFromDetail,isFormainScreen);
            return;
        }

        adView.setAdUnitId(adUnitId);

        adContainerView.removeAllViews();
        adContainerView.addView(adView);

        AdSize adSize = getAdSize(activity,adContainerView);
//        Logger.e("loadBanner","height:"+adSize.getHeight()+" pix:"+adSize.getHeightInPixels(activity));
        try {
//            if(activity instanceof StatusViewPagerActivity || activity instanceof DoubleWallpaperActivity || activity instanceof AutoWallChangerActivity || activity instanceof WallpaperDetailActivity || activity instanceof EdgeSettings || activity instanceof CreateExclusiveActivity)
            adContainerView.setMinimumHeight(adSize.getHeightInPixels(activity));
        } catch (Exception e) {
            e.printStackTrace();
        }

        adView.setAdSize(adSize);

        adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                isAdMobBannerLoaded=true;
                EventManager.sendEvent(EventManager.LBL_Advertise,EventManager.ATR_KEY_ADMOB_BANNER,"onAdLoaded");
                Logger.e(TAG,"Banner onAdLoaded");

            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Logger.e(TAG,"Banner onAdFailedToLoad");
                isAdMobBannerLoaded=false;
                EventManager.sendEvent(EventManager.LBL_Advertise,EventManager.ATR_KEY_ADMOB_BANNER,"onAdFailedToLoad");
                try {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(adContainerView!=null)
                                adContainerView.removeAllViews();
                            bannerFailedOn(activity,adContainerView,isFromDetail,isFormainScreen);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }


            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Logger.e(TAG,"Banner onAdClosed");
            }
        });

        AdRequest.Builder adRequestBuilder=new AdRequest.Builder();

        AdRequest adRequest = adRequestBuilder.build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);

    }

    private AdSize getAdSize(Activity activity,FrameLayout adContainerView) {

        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    public void onPause(Activity act) {
        try {
            if (adView != null) {
                adView.pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onResume(Activity act) {
        try {
            if (adView != null) {
                adView.resume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
