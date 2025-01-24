package macro.hd.wallpapers.Interface.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.flurry.android.FlurryAgent;
import com.greedygame.core.AppConfig;
import com.greedygame.core.GreedyGameAds;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.weewoo.sdkproject.events.EventHelper;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Model.Wallpapers;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.UniversalAdListener;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventState;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.IEventListener;
import macro.hd.wallpapers.notifier.ListenerPriority;
import macro.hd.wallpapers.notifier.NotifierFactory;

//import com.greedygame.core.AppConfig;
//import com.greedygame.core.GreedyGameAds;

public class SplashActivity extends BaseActivity implements IEventListener, UniversalAdListener {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static int SPLASH_TIME_OUT = 3000;
    private boolean isSplash = true;
    //    private boolean isApiCalled;
//    private boolean isHandlerCalled;
    SettingStore settingStore;
    Wallpapers post = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CommonFunctions.setOverlayAction(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);

        if (CommonFunctions.isStoreVersion(this)) {
            IronSource.init(this, "12a67631d", IronSource.AD_UNIT.INTERSTITIAL, IronSource.AD_UNIT.BANNER);
        }
//        IntegrationHelper.validateIntegration(this);

        setLocale();
        WallpapersApplication.isAppRunning = true;
        settingStore = SettingStore.getInstance(getApplicationContext());
        WallpapersApplication.getApplication().isInterstitialShow = false;
        WallpapersApplication.getApplication().isGGLoaded = false;
//        settingStore.setFirstTimeLaunch(true);
        if (settingStore.getSystemTheme()) {
            boolean isNight = CommonFunctions.isNightMode(SplashActivity.this);
            settingStore.setTheme(isNight ? 1 : 0);
        }

        if (getIntent() != null) {
            post = (Wallpapers) getIntent().getSerializableExtra("post");
        }
//        getRemoteConfigParam();


        SettingStore store = SettingStore.getInstance(this);
        EventHelper eh = new EventHelper();
        if (store.FirstLaunch()) {

            eh.sendFirstEvent();
        } else {

            eh.sendOpenAppEvent();
        }

        eh.sendSplashViewEvent();

        Log.i("HashKey", "HashKey" + CommonFunctions.getHashKey(this));

        if (isSplash) {
            setContentView(R.layout.activity_splash);
            findViewById(R.id.btn_skip).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isFirstTime = false;
                    nextActivity();
                }
            });

            ImageView img_logo = findViewById(R.id.img_logo);

            Glide.with(this)
                    .load(R.mipmap.ic_splash_round_new)
                    .apply(RequestOptions.circleCropTransform())
                    .into(img_logo);

            TextView tv = (TextView) findViewById(R.id.txt_title);
            TextView txt_subtitle = (TextView) findViewById(R.id.txt_subtitle);
            Typeface face = null;
            try {
                face = Typeface.createFromAsset(getAssets(),
                        "LCALLIG.TTF");
                tv.setTypeface(face);
                txt_subtitle.setTypeface(face);
            } catch (Exception e) {
                e.printStackTrace();
            }

            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                final String version = pInfo.versionName;
                ((TextView) findViewById(R.id.txt_v)).setText("v" + version);
                ((TextView) findViewById(R.id.txt_v)).setTypeface(face);
            } catch (Exception e) {
                e.printStackTrace();
            }

            CommonFunctions.updateWindow(SplashActivity.this);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    isHandlerCalled=true;
//                    Logger.e(TAG,"Handler finish: "+isApiCalled);
//                    if(isApiCalled) {
//                        if(post!=null){
//                            nextActivity();
//                            return;
//                        }
//                        if(!TextUtils.isEmpty(JesusApplication.getApplication().getSettings().getIs_splash_ad()) && JesusApplication.getApplication().getSettings().getIs_splash_ad().equalsIgnoreCase("1")){
//                            if((!TextUtils.isEmpty(JesusApplication.getApplication().getSettings().getIs_splash_remote_config()) && JesusApplication.getApplication().getSettings().getIs_splash_remote_config().equalsIgnoreCase("1"))){
//                                if(isAdShowFromRemoteConfig){
//
//                                }else
//                                    nextActivity();
//                            }
//                        }else
//                        if(!settingStore.FirstLaunch())
//                            nextActivity();
//                    }
//                }
//            },SPLASH_TIME_OUT);
        } else {
            nextActivity();
        }

        ImageView img = findViewById(R.id.img);
        boolean isEvent = false;

        try {
            String splashDire = CommonFunctions.createEventDirCache();
            File dir = new File(splashDire);
            if (dir != null && dir.isDirectory() && dir.list().length > 0) {
                File eventFile = dir.listFiles()[0];
                String date = eventFile.getName().substring(0, eventFile.getName().indexOf("."));
                Logger.e("date", "" + date);
                String currentDate = getCurrentDate();
                Logger.e("currentDate", "" + currentDate);
                boolean isOutDated = isDateOutdated(date);
                Logger.e("isOutDated", "" + isOutDated);
                try {
                    if (date.equalsIgnoreCase(currentDate))
                        isOutDated = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!isOutDated) {
                    isEvent = true;
                    Uri imageUri = Uri.fromFile(eventFile);
                    Glide.with(this)
                            .load(imageUri)
                            .apply(new RequestOptions())
                            .into(img);
                } else {
                    isEvent = false;
                    try {
                        if (eventFile != null && eventFile.exists()) {
                            eventFile.delete();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (!isEvent) {
                String set_img = settingStore.getSplashImg();
                int total_download = set_img.split("#").length;
                String file_name = "";
                File dir_splash = new File(CommonFunctions.createSpashDirCache());
                String list[] = dir_splash.list();
                if (total_download == list.length) {
                    set_img = "";
                    settingStore.setSplashImg(set_img);
                }
                if (list != null && list.length > 0) {
                    for (int i = 0; i < list.length; i++) {
                        if (set_img.contains(list[i])) {
                            continue;
                        } else {
                            file_name = list[i];
                            break;
                        }
                    }
                }

                Logger.e("file_name", "" + file_name);
                Logger.e("set_img", "" + set_img);
                if (TextUtils.isEmpty(set_img)) {
                    settingStore.setSplashImg(file_name);
                } else {
                    settingStore.setSplashImg(set_img + "#" + file_name);
                }

                String dst = CommonFunctions.createSpashDirCache() + "/" + file_name;
                File file = new File(dst);
                if (file != null && file.exists() && !TextUtils.isEmpty(file_name)) {
                    Uri imageUri = Uri.fromFile(file);
                    Glide.with(this)
                            .load(imageUri)
                            .apply(new RequestOptions()
                            )
                            .into(img);
                } else {
                    img.setImageResource(R.mipmap.splash);
                    //Glide.with(this).load(getImage("splash")).into(img);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (img != null)
                img.setImageResource(R.mipmap.splash);
        }

        registerLoginInfoListener();

        WallpapersApplication.getApplication().getReferrerClient();

    }

    private void loadGG() {

        if (!CommonFunctions.isGreedyGameShow())
            return;

        boolean isTemp = false;
        try {
            isTemp = WallpapersApplication.getApplication().getSettings().getGGWithFB();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AppConfig appConfig = new AppConfig.Builder(this)
                    .withAppId("14252407")  //Replace the app ID with your app's ID
                    .enableFacebookAds(isTemp)
                    .enableInstallTracking(false)
                    .build();
            GreedyGameAds.initWith(appConfig, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isFirstTime;
    private boolean isContinue;

    private void nextActivity() {
        Logger.e(TAG, "nextActivity:" + activityVisible + "isFinishing:" + isFinishing());
        if (!activityVisible) {
            isContinue = true;
            return;
        }

        if (isFinishing())
            return;

        try {
            unregisterLoginInfoListener();
            if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getTransfer())) {
                Intent intent = new Intent(SplashActivity.this, Transferactivity.class);
                startActivity(intent);

                if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIsback()) && WallpapersApplication.getApplication().getSettings().getIsback().equalsIgnoreCase("1")) {
                    finish();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent;
        if (settingStore.FirstLaunch()) {
            isFirstTime = true;

            if (CommonFunctions.isStoreVersion(this)) {

                if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getAdDisable()) && WallpapersApplication.getApplication().getSettings().getAdDisable().equalsIgnoreCase("0")) {
                    if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_splash_ad()) && WallpapersApplication.getApplication().getSettings().getIs_splash_ad().equalsIgnoreCase("1")) {
                        startLoadingAd();
                    }
                }
//                    String adUnitId = settingStore.getFromSettingPreference(SettingStore.ADMOB_INTERSTITIAL_SPLASH_ID);
//                    if (!CommonFunctions.isEmpty(adUnitId)){
//                        if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getAdDisable()) && WallpapersApplication.getApplication().getSettings().getAdDisable().equalsIgnoreCase("0")) {
//                            if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_splash_ad()) && WallpapersApplication.getApplication().getSettings().getIs_splash_ad().equalsIgnoreCase("1")) {
//                                startLoadingAd();
//                            }
//                        }
//                    }else{
//                        String adUnitIdGG = settingStore.getFromSettingPreference(SettingStore.GG_INTERSTITIAL_ID_SPLASH);
//                        if (!CommonFunctions.isEmpty(adUnitIdGG) && CommonFunctions.isGreedyGameShow()){
//                            if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getAdDisable()) && WallpapersApplication.getApplication().getSettings().getAdDisable().equalsIgnoreCase("0")) {
//                                if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_splash_ad()) && WallpapersApplication.getApplication().getSettings().getIs_splash_ad().equalsIgnoreCase("1")) {
//                                    startLoadingAd();
//                                }
//                            }
//                        }
//                    }
            }
//                Logger.e(TAG,"MainIntroActivity: "+isAdShowFromRemoteConfig);
            intent = new Intent(SplashActivity.this, ApplicationIntroActivity.class);

            intent.putExtra(ApplicationIntroActivity.EXTRA_FULLSCREEN, false);
            intent.putExtra(ApplicationIntroActivity.EXTRA_SCROLLABLE, false);
            intent.putExtra(ApplicationIntroActivity.EXTRA_CUSTOM_FRAGMENTS,
                    true);
            intent.putExtra(ApplicationIntroActivity.EXTRA_PERMISSIONS, false);
            intent.putExtra(ApplicationIntroActivity.EXTRA_SKIP_ENABLED, true);
            intent.putExtra(ApplicationIntroActivity.EXTRA_SHOW_BACK, true);
            intent.putExtra(ApplicationIntroActivity.EXTRA_SHOW_NEXT, true);
            intent.putExtra(ApplicationIntroActivity.EXTRA_FINISH_ENABLED,
                    true);
            intent.putExtra(ApplicationIntroActivity.EXTRA_GET_STARTED_ENABLED,
                    false);
            startActivityForResult(intent, 100);
        } else {
            if (!isFinishing()) {
                intent = new Intent(SplashActivity.this, MainNavigationActivity.class);
                if (getIntent() != null && getIntent().getExtras() != null)
                    intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                finish();
            }
        }
    }

    private void resultOnAct() {
        settingStore.setFirstTimeLaunch(false);

        if (WallpapersApplication.getApplication().getSettings().getAdDisable().equalsIgnoreCase("1")) {
            isFirstTime = false;
            nextActivity();
            return;
        }

        if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_splash_ad()) && WallpapersApplication.getApplication().getSettings().getIs_splash_ad().equalsIgnoreCase("1")) {
            isFirstTime = false;
//            if((!TextUtils.isEmpty(JesusApplication.getApplication().getSettings().getIs_splash_remote_config()) && JesusApplication.getApplication().getSettings().getIs_splash_remote_config().equalsIgnoreCase("1"))){
//                if(isAdShowFromRemoteConfig){
//                    if (JesusApplication.getApplication().isAdLoaded() && !JesusApplication.getApplication().isDisable()) {
//                        onAdLoaded();
//                    }else
//                        nextActivity();
//                }else{
//                    nextActivity();
//                }
//            }else{
            if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable()) {
                onAdLoaded();
            } else
                nextActivity();
//            }
        } else
            nextActivity();
    }

    private void registerLoginInfoListener() {
        EventNotifier notifier =
                NotifierFactory.getInstance().getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_USER_INFO);
        notifier.registerListener(this, ListenerPriority.PRIORITY_HIGH);
    }

    private void unregisterLoginInfoListener() {
        EventNotifier notifier =
                NotifierFactory.getInstance().getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_USER_INFO);
        notifier.unRegisterListener(this);
    }

    @Override
    public int eventNotify(int eventType, final Object eventObject) {
        int eventState = EventState.EVENT_IGNORED;
        switch (eventType) {
            case EventTypes.EVENT_USER_INFO_UPDATED:
                eventState = EventState.EVENT_PROCESSED;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            String admob_banner_id = WallpapersApplication.getApplication().getSettings().getAdmob_banner_id();
                            settingStore.setInSettingPreference(SettingStore.ADMOB_BANNER_ID, admob_banner_id);

                            String admob_main_banner_id = WallpapersApplication.getApplication().getSettings().getAdmob_main_banner_id();
                            settingStore.setInSettingPreference(SettingStore.ADMOB_MAIN_BANNER_ID, admob_main_banner_id);

                            String admob_banner_detail_id = WallpapersApplication.getApplication().getSettings().getAdmobDetail_banner_id();
                            settingStore.setInSettingPreference(SettingStore.ADMOB_BANNER_DETAIL_ID, admob_banner_detail_id);

                            String admob_interstitial_id = WallpapersApplication.getApplication().getSettings().getAdmob_interstitial_id();
                            settingStore.setInSettingPreference(SettingStore.ADMOB_INTERSTITIAL_ID, admob_interstitial_id);
                            String admob_native_id = WallpapersApplication.getApplication().getSettings().getAdmob_native_id();
                            settingStore.setInSettingPreference(SettingStore.ADMOB_NATIVE_ID, admob_native_id);

                            String admob_interstitial_download_id = WallpapersApplication.getApplication().getSettings().getAdmob_interstitial_download_id();
                            settingStore.setInSettingPreference(SettingStore.ADMOB_INTERSTITIAL_DOWNLOAD_ID, admob_interstitial_download_id);


                            String admob_open_ap_id = WallpapersApplication.getApplication().getSettings().getAdmob_open_ap_id();
                            settingStore.setInSettingPreference(SettingStore.ADMOB_OPEN_AD_ID, admob_open_ap_id);

                            String admob_native_id_detail = WallpapersApplication.getApplication().getSettings().getAdmob_native_detail_id();
                            settingStore.setInSettingPreference(SettingStore.ADMOB_DETAIL_NATIVE_ID, admob_native_id_detail);

                            String admob_rewarded_id = WallpapersApplication.getApplication().getSettings().getAdmob_inters_splash();
                            settingStore.setInSettingPreference(SettingStore.ADMOB_INTERSTITIAL_SPLASH_ID, admob_rewarded_id);

                            String fb_native_id = WallpapersApplication.getApplication().getSettings().getFb_native_id();
                            settingStore.setInSettingPreference(SettingStore.FB_NATIVE_ID, fb_native_id);
                            String fb_interstitial_id = WallpapersApplication.getApplication().getSettings().getFb_interstitial_id();
                            settingStore.setInSettingPreference(SettingStore.FB_INTERSTITIAL_ID, fb_interstitial_id);
                            String fb_banner_id = WallpapersApplication.getApplication().getSettings().getFb_banner_id();
                            settingStore.setInSettingPreference(SettingStore.FB_BANNER_ID, fb_banner_id);
                            String fb_banner_id_other = WallpapersApplication.getApplication().getSettings().getFb_banner_other_id();
                            settingStore.setInSettingPreference(SettingStore.FB_BANNER_OTHER_ID, fb_banner_id_other);

                            // greddy
                            String greedy_banner = WallpapersApplication.getApplication().getSettings().getGg_banner_id();
                            settingStore.setInSettingPreference(SettingStore.GG_BANNER_ID, greedy_banner);

                            String greedy_banner_detail = WallpapersApplication.getApplication().getSettings().getGg_banner_id_detail();
                            settingStore.setInSettingPreference(SettingStore.GG_BANNER_ID_DETAIL, greedy_banner_detail);

                            String gg_interstitial_id = WallpapersApplication.getApplication().getSettings().getGg_interstitial_id();
                            settingStore.setInSettingPreference(SettingStore.GG_INTERSTITIAL_ID, gg_interstitial_id);

                            String gg_interstitial_splash_id = WallpapersApplication.getApplication().getSettings().getGg_interstitial_splash_id();
                            settingStore.setInSettingPreference(SettingStore.GG_INTERSTITIAL_ID_SPLASH, gg_interstitial_splash_id);

                            String gg_interstitial_download = WallpapersApplication.getApplication().getSettings().getGg_interstitial_download_id();
                            settingStore.setInSettingPreference(SettingStore.GG_INTERSTITIAL_ID_DOWNLOAD, gg_interstitial_download);

                            String gg_native_id = WallpapersApplication.getApplication().getSettings().getGg_native_id();
                            settingStore.setInSettingPreference(SettingStore.GG_NATIVE_ID, gg_native_id);
                            //

                            Logger.e("Splash", "greedy_banner:" + greedy_banner + " greedy_banner_detail:" + greedy_banner_detail);

                            String gg_open_id = WallpapersApplication.getApplication().getSettings().getGg_open_id();
                            settingStore.setInSettingPreference(SettingStore.GG_OPEN_ID, gg_open_id);

                            String img_domain = WallpapersApplication.getApplication().getSettings().getImg_domain();
                            settingStore.setImageDomain(img_domain);

                            String am_spash_interstial = WallpapersApplication.getApplication().getSettings().getAm_splash_interstitial();
                            settingStore.setInSettingPreference(SettingStore.AM_SPLASH_INTERSTITIAL_ID, am_spash_interstial);
                            String am_interstitial_id = WallpapersApplication.getApplication().getSettings().getAm_interstitial_id();
                            settingStore.setInSettingPreference(SettingStore.AM_INTERSTITIAL_ID, am_interstitial_id);
                            String am_open_id = WallpapersApplication.getApplication().getSettings().getAm_open_id();
                            settingStore.setInSettingPreference(SettingStore.AM_OPEN_ID, am_open_id);
                            String am_banner_id = WallpapersApplication.getApplication().getSettings().getAm_banner_id();
                            settingStore.setInSettingPreference(SettingStore.AM_BANNER_ID, am_banner_id);
                            String am_detail_banner_id = WallpapersApplication.getApplication().getSettings().getAm_detail_banner_id();
                            settingStore.setInSettingPreference(SettingStore.AM_DETAIL_BANNER_ID, am_detail_banner_id);

                            String am_mainscreen_banner_id = WallpapersApplication.getApplication().getSettings().getAm_mainscreen_banner_id();
                            settingStore.setInSettingPreference(SettingStore.AM_MAINSCREEN_BANNER_ID, am_mainscreen_banner_id);

                            String am_native_id = WallpapersApplication.getApplication().getSettings().getAm_native_id();
                            settingStore.setInSettingPreference(SettingStore.AM_NATIVE_ID, am_native_id);

                            if (WallpapersApplication.getApplication().getSettings().getWallDirect() == 1)
                                AppConstant.isWallSetDirectly = true;
                            else
                                AppConstant.isWallSetDirectly = false;

                            WallpapersApplication.getApplication().refreshAd();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!isFinishing()) {
                            //loadGG();
                            //WallpapersApplication.getApplication().loadFBNativeAd();
                            WallpapersApplication.getApplication().loadNativeForDetail();
                        }

                        try {
                            FlurryAgent.setUserId(settingStore.getUserID());
                        } catch (Exception e) {
                            //e.printStackTrace();
                        } catch (Error e) {
                            //e.printStackTrace();
                        }

                        if (CommonFunctions.checkIfVPNIsActive()) {
                            showErrDlg(getString(R.string.dialog_title_info), getString(R.string.disablevpn));
                            return;
                        }

                        try {
                            if (!TextUtils.isEmpty(UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getApp_settings().get(0).getUnderMaintenance()) && !UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getApp_settings().get(0).getUnderMaintenance().equalsIgnoreCase("0")) {
                                CommonFunctions.showDialogOk(SplashActivity.this, getString(R.string.error_title), UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getApp_settings().get(0).getUnderMaintenance(), new CommonFunctions.DialogOnButtonClickListener() {
                                    @Override
                                    public void onOKButtonCLick() {
                                        finish();
                                    }

                                    @Override
                                    public void onCancelButtonCLick() {

                                    }
                                });
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        settingStore.setInSettingPreferenceInt(SettingStore.EXCLUSIVE_RESIZE_BITMAP, WallpapersApplication.getApplication().getSettings().getFourk_resize());

                        Logger.e(TAG, "eventNotify finish: ");
                        if (post != null) {
                            nextActivity();
                            return;
                        }

                        if (settingStore.FirstLaunch()) {
                            nextActivity();
                        } else {

                            if (isFinishing())
                                return;

                            if (WallpapersApplication.getApplication().getSettings().getAdDisable().equalsIgnoreCase("1")) {
                                nextActivity();
                                return;
                            }

                            if (!CommonFunctions.isStoreVersion(SplashActivity.this)) {
                                nextActivity();
                                return;
                            }

                            // comment advertise
//                            String adUnitId = settingStore.getFromSettingPreference(SettingStore.ADMOB_INTERSTITIAL_SPLASH_ID);
//                            if (CommonFunctions.isEmpty(adUnitId)){
//                                // new for GG
//                                String adUnitIdGG = settingStore.getFromSettingPreference(SettingStore.GG_INTERSTITIAL_ID_SPLASH);
//                                if (CommonFunctions.isEmpty(adUnitIdGG) || !CommonFunctions.isGreedyGameShow()){
//                                    nextActivity();
//                                    return;
//                                }
//                            }

                            if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_splash_ad()) && WallpapersApplication.getApplication().getSettings().getIs_splash_ad().equalsIgnoreCase("1")) {
//                                if((!TextUtils.isEmpty(JesusApplication.getApplication().getSettings().getIs_splash_remote_config()) && JesusApplication.getApplication().getSettings().getIs_splash_remote_config().equalsIgnoreCase("1"))){
//                                    if(isAdShowFromRemoteConfig){
//                                        if(settingStore.getIsPro()){
//                                            nextActivity();
//                                        }else
//                                            startLoadingAd();
//                                    }else{
//                                        nextActivity();
//                                    }
//                                }else{
                                if (settingStore.getIsPro()) {
                                    nextActivity();
                                } else
                                    startLoadingAd();
//                                }
                            } else {
                                nextActivity();
                            }
                        }
                    }
                });
                break;
            case EventTypes.EVENT_USER_NOT_VALID:
                eventState = EventState.EVENT_PROCESSED;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WebServiceError error = (WebServiceError) eventObject;

                        showErrDlg(getString(R.string.dialog_title_info), error.getDescription());
                    }
                });
                break;
            case EventTypes.EVENT_REFERER_GOT:
                eventState = EventState.EVENT_PROCESSED;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UserInfoManager favGroupManager = UserInfoManager.getInstance(getApplicationContext());
                        favGroupManager.getGroupList(CommonFunctions.getHardwareId(SplashActivity.this));
                    }
                });
                break;
        }
        return eventState;
    }

    Handler handle_delay = new Handler();
    Runnable runnable_delay = new Runnable() {
        @Override
        public void run() {
            Logger.e(TAG, "startLoadingAd over:");
            if (!settingStore.FirstLaunch())
                nextActivity();
        }
    };

    private void startLoadingAd() {
        Logger.e(TAG, "startLoadingAd: ");
        loadInterstital();

        try {
            int time = 8000;
            try {
                time = WallpapersApplication.getApplication().getSettings().getSplashTime();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (WallpapersApplication.getApplication().getSettings().isSplashDelay()) {
                if (handle_delay != null && runnable_delay != null)
                    handle_delay.postDelayed(runnable_delay, time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void loadInterstital() {
        WallpapersApplication.getApplication().addUniversalAdListener(this);
        CommonFunctions.getRandomAd(this, true, true);
        WallpapersApplication.getApplication().setUpAd(true);
        WallpapersApplication.getApplication().requestNewInterstitial(this);
    }

    private boolean activityVisible = true;

    @Override
    public void onResume() {
        super.onResume();
        Logger.e(TAG, "onResume" + " isBypassed:" + isBypassed + " isContinue:" + isContinue);
        activityVisible = true;
        if (isBypassed) {
            isBypassed = false;
            Intent intent = new Intent(SplashActivity.this, MainNavigationActivity.class);
            if (getIntent() != null && getIntent().getExtras() != null)
                intent.putExtras(getIntent().getExtras());

            WallpapersApplication.getApplication().lastAdTime = 0;
            startActivity(intent);
            finish();
        } else {
            if (isContinue) {
                isContinue = false;
                nextActivity();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.e(TAG, "resultCode:" + resultCode + " requestCode:" + requestCode);
        if (requestCode == 100) {
            activityVisible = true;
            if (resultCode == RESULT_OK) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        resultOnAct();
                    }
                });
            } else if (resultCode == RESULT_CANCELED) {
//                resultOnAct();
                finish();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        activityVisible = false;
    }

    private boolean isBypassed;

    @Override
    public void onAdLoaded() {
        try {
            if (handle_delay != null)
                handle_delay.removeCallbacks(runnable_delay);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Logger.e(TAG, "onAdLoaded:" + isFirstTime);
        EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_SPLASH_AD, "onAdLoaded");
//        isNextScreen = false;
        if (!isFirstTime) {
            unregisterLoginInfoListener();
            Intent intent = new Intent(SplashActivity.this, MainNavigationActivity.class);
            if (getIntent() != null && getIntent().getExtras() != null)
                intent.putExtras(getIntent().getExtras());

            WallpapersApplication.getApplication().incrementUserClickCounter();

            if (activityVisible) {
                if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable()) {
                    WallpapersApplication.getApplication().lastAdTime = System.currentTimeMillis();
                    WallpapersApplication.getApplication().showInterstitial(SplashActivity.this, intent, true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                findViewById(R.id.btn_skip).setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1000);

                } else {
                    WallpapersApplication.getApplication().lastAdTime = 0;
                    startActivity(intent);
                    finish();
                }
            } else
                isBypassed = true;
        }
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        Logger.e(TAG, "onAdFailedToLoad");
        try {
            if (handle_delay != null)
                handle_delay.removeCallbacks(runnable_delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventManager.sendEvent(EventManager.LBL_Advertise, EventManager.ATR_KEY_SPLASH_AD, "onAdFailedToLoad:" + errorCode);
        if (!settingStore.FirstLaunch())
            nextActivity();
    }

    @Override
    public void onAdClosed() {
        Logger.e("SplashActivity", "onAdClosed");
    }

//    boolean isAdShowFromRemoteConfig=false;
//    FirebaseRemoteConfig mFirebaseRemoteConfig;
//    private void getRemoteConfigParam(){
//
//        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setMinimumFetchIntervalInSeconds(3600)
//                .build();
//        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
//		Map<String,Object> map =new HashMap<>();
//		map.put("IS_SPLASH_AD_SHOW","0");
//		mFirebaseRemoteConfig.setDefaultsAsync(map);
//
//        mFirebaseRemoteConfig.fetchAndActivate()
//                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Boolean> task) {
//                        if (task!=null && task.isSuccessful()) {
//                            Log.e(TAG, "Config size: " + mFirebaseRemoteConfig.getAll().size());
//                            String IS_SPLASH_AD_SHOW = mFirebaseRemoteConfig.getString("IS_SPLASH_AD_SHOW");
//                            Log.e(TAG, "Config params updated: " + IS_SPLASH_AD_SHOW);
//                            EventManager.sendEvent(EventManager.LBL_REMOTE_CONFIG,EventManager.ATR_KEY_REMOTE,""+IS_SPLASH_AD_SHOW);
//                            if(!TextUtils.isEmpty(IS_SPLASH_AD_SHOW) && IS_SPLASH_AD_SHOW.equalsIgnoreCase("1")){
//                                isAdShowFromRemoteConfig=true;
//                            }else
//                                isAdShowFromRemoteConfig=false;
//
//                            Log.e(TAG, "Config isAdShowFromRemoteConfig: " + isAdShowFromRemoteConfig);
//                        } else {
//
//                        }
//                    }
//                });
//    }

    private void showErrDlg(String title, String msg) {
        if (!isFinishing()) {

            AlertDialog.Builder builder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                boolean isDarkTheme = false;
                if (settingStore.getTheme() == 0) {
                    isDarkTheme = false;
                } else if (settingStore.getTheme() == 1) {
                    isDarkTheme = true;
                }
                if (isDarkTheme)
                    builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
                else
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);

            } else {
                builder = new AlertDialog.Builder(this);
            }

            builder.setMessage(msg).setTitle(title)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });

            AlertDialog alert = builder.create();
            if (!isFinishing()) {
                alert.show();
            }
        }
    }


    private boolean isDateOutdated(String enddate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date strDate = null;
        try {
            strDate = sdf.parse(enddate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (new Date().after(strDate)) {
            return true;
        }
        return false;
    }

    private String getCurrentDate() {
        Date date = Calendar.getInstance().getTime();

        // Display a date in day, month, year format
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today = formatter.format(date);
        System.out.println("Today : " + today);
        return today;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        post = null;
        try {
            handle_delay.removeCallbacks(runnable_delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
        handle_delay = null;
        runnable_delay = null;
        unregisterLoginInfoListener();
        WallpapersApplication.getApplication().removeUniversalAdListener(this);
    }
}
