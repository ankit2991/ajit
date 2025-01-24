package macro.hd.wallpapers.Interface.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.viewpager.widget.ViewPager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.bumptech.glide.Glide;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.review.model.ReviewErrorCode;
import com.google.android.play.core.review.testing.FakeReviewManager;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.ironsource.mediationsdk.IronSource;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.RetryPolicy;
import com.thin.downloadmanager.ThinDownloadManager;
import com.weewoo.sdkproject.events.EventHelper;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import macro.hd.wallpapers.AppController.RequestManager;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.AppOpenManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Interface.Fragments.CategoryHomeFragment;
import macro.hd.wallpapers.Interface.Fragments.ParentHomeFragment;
import macro.hd.wallpapers.LightWallpaperService.EdgeSettings;
import macro.hd.wallpapers.Model.Category;
import macro.hd.wallpapers.Model.GetLikeModel;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.IModelBase;
import macro.hd.wallpapers.Model.Wallpapers;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.MarshMallowPermission;
import macro.hd.wallpapers.Utilily.UniversalAdListener;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.WallpapersService;
import macro.hd.wallpapers.billing.BillingUtilities;
import macro.hd.wallpapers.billing.PurchaseView;
import macro.hd.wallpapers.billing.Security;
import macro.hd.wallpapers.fcm.FirebaseInstanceIDService;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventState;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.IEventListener;
import macro.hd.wallpapers.notifier.ListenerPriority;
import macro.hd.wallpapers.notifier.NotifierFactory;

public class MainNavigationActivity extends BaseBannerActivity implements IEventListener, UniversalAdListener, ViewModelStoreOwner {

    private static final String TAG = "MainBottomActivity";
    private static final String TAG_PURCHASE = "PURCHASE";
    public String[] tabString;
    private Toolbar toolbar;

    private EventHelper eventHelper = new EventHelper();

    FragmentManager mFragmentManager;
    MarshMallowPermission marshMallowPermission;
    Bundle savedInstanceState;
    SettingStore settingStore;

    private DrawerLayout mDrawerLayout;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    NavigationView navigationView;

    private PurchaseView purchaseView = null;

//	private static final String TAG_L14DC = "Last14DayCount";
//	public void startWorker(){
//		WorkManager.getInstance(this).cancelAllWorkByTag(TAG_L14DC);
//		OneTimeWorkRequest myWorkRequest =
//				new OneTimeWorkRequest.Builder(Last14DayCountWorkManager.class)
//						.setInitialDelay(5, TimeUnit.MINUTES).addTag(TAG_L14DC)
//						.build();
////		PeriodicWorkRequest workRequest2 = new PeriodicWorkRequest.Builder(Last14DayCountWorkManager.class, 10,
////				TimeUnit.MINUTES).addTag(TAG_L14DC).build();
//		WorkManager.getInstance(this).enqueueUniqueWork(TAG_L14DC,
//				ExistingWorkPolicy.KEEP, myWorkRequest);
//	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setLocale();
        isShowResumeAd = false;
        tabString = new String[]{
                getString(R.string.menu_home),
                getString(R.string.label_live),
                getString(R.string.menu_exclusive),
                getString(R.string.label_trending),
                getString(R.string.label_category)
        };

        setContentView(R.layout.activity_main_home);

        AppOpenManager appOpenManager = new AppOpenManager(WallpapersApplication.getApplication());
        WallpapersApplication.setAppOpenManager(appOpenManager);

        WallpapersApplication.getApplication().mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(this);

        WallpapersApplication.isAppRunning = true;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        purchaseView = findViewById(R.id.purchase_view);
        purchaseView.setDidClickPurchase(() -> {

            launchBillingFlow(MainNavigationActivity.this);
            return Unit.INSTANCE;
        });

        WallpapersApplication.getApplication().ratingCounter = 0;
        WallpapersApplication.getApplication().displayRated = false;
//        WallpapersApplication.getApplication().isGGLoaded=false;
//		JesusApplication.getApplication().userClickCount = 0;

        if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_splash_ad()) && WallpapersApplication.getApplication().getSettings().getIs_splash_ad().equalsIgnoreCase("1")) {
        } else
            WallpapersApplication.getApplication().lastAdTime = 0;

        try {
            boolean result = CommonFunctions.isStoreVersion(this);
            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.LBL_STORE, "" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        settingStore = SettingStore.getInstance(this);

        this.savedInstanceState = savedInstanceState;

        mFragmentManager = getSupportFragmentManager();

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        int filter = 0;
        try {
            filter = Integer.parseInt(WallpapersApplication.getApplication().getSettings().getFilter_home());
        } catch (Exception e) {
            e.printStackTrace();
        }
        videoPowerMenu(filter);
//        getSupportActionBar().setDisplayShowTitleEnabled(true);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        // Make the hamburger button work
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

//        Drawable drawable= ContextCompat.getDrawable(this,R.mipmap.ic_more);
//        toolbar.setOverflowIcon(drawable);

        marshMallowPermission = new MarshMallowPermission(this);

        navigationView = findViewById(R.id.nav_view);
//		navigationView.setItemIconTintList(null);
        View headerLayout =
                navigationView.inflateHeaderView(R.layout.nav_header_main);
        TextView nav_header_version = headerLayout.findViewById(R.id.nav_header_version);
//		nav_header_version.setText("v" + Common.appVerstionName(this));


        MenuItem nav_item_double_wall = navigationView.getMenu().findItem(R.id.nav_item_double_wall);
        if (CommonFunctions.isDoubleWallpaperSupported())
            nav_item_double_wall.setVisible(true);
        else
            nav_item_double_wall.setVisible(false);

        MenuItem nav_item_pro = navigationView.getMenu().findItem(R.id.nav_item_pro);
        if (CommonFunctions.isProIconShow())
            nav_item_pro.setVisible(true);
        else
            nav_item_pro.setVisible(false);

        MenuItem nav_item_color = navigationView.getMenu().findItem(R.id.gradien);
        if (CommonFunctions.isGradientSupported())
            nav_item_color.setVisible(true);
        else
            nav_item_color.setVisible(false);

//		if(!TextUtils.isEmpty(screenType) && screenType.equalsIgnoreCase(ParentHomeFragment.TYPE_EXCLUSIVE))
        findViewById(R.id.img_select).setVisibility(View.GONE);
//		else
//			findViewById(R.id.img_select).setVisibility(View.GONE);


        findViewById(R.id.img_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!CommonFunctions.isSensorAvailable(MainNavigationActivity.this)) {
                    return;
                }
                EventManager.sendEvent(EventManager.LBL_EXCLUSIVE_WALLPAPER, EventManager.ATR_KEY_CREATE_EXCLUSIVE_WALLPAPER, "Click");
                Intent intent = new Intent(MainNavigationActivity.this, CreateExclusiveActivity.class);
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        if (menuItem.getItemId() == R.id.nav_item_rate) {
                            final String appPackageName = getPackageName(); // package name of the app
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        } else if (menuItem.getItemId() == R.id.nav_item_share) {
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_SHARE, EventManager.ATR_VALUE_SHARE);
                            shareApp();
                        } else if (menuItem.getItemId() == R.id.nav_item_edge_light) {
                            EventManager.sendEvent(EventManager.LBL_EDGE_WALLPAPER, EventManager.ATR_EDGE_WALLPAPER, "Left Menu");
                            Intent intent = new Intent(MainNavigationActivity.this, EdgeSettings.class);
                            if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
                                WallpapersApplication.getApplication().showInterstitial(MainNavigationActivity.this, intent, false);
                            } else {
                                WallpapersApplication.getApplication().incrementUserClickCounter();
                                startActivity(intent);
                            }
                        } else if (menuItem.getItemId() == R.id.nav_item_pro) {
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_PRO, EventManager.ATR_VALUE_PRO);
                            displayAlert();
                        } else if (menuItem.getItemId() == R.id.gradien) {
                            EventManager.sendEvent(EventManager.LBL_GRADIENT_WALLPAPER, EventManager.ATR_GRADIENT_WALLPAPER, "Left Menu");

                            Intent intent = new Intent(MainNavigationActivity.this, GradientActivity.class);
                            if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
                                WallpapersApplication.getApplication().showInterstitial(MainNavigationActivity.this, intent, false);
                            } else {
                                WallpapersApplication.getApplication().incrementUserClickCounter();
                                startActivity(intent);
                            }

                        } else if (menuItem.getItemId() == R.id.nav_item_exclusive_wall) {
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_SET_EXCLUSIVE_WALLPAPER, EventManager.ATR_KEY_SET_EXCLUSIVE_WALLPAPER);
                            if (viewPager != null)
                                viewPager.setCurrentItem(2, true);
                        } else if (menuItem.getItemId() == R.id.nav_item_my_download) {
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.LBL_MY_DOWNLOAD, "Left Menu");
                            Intent intent = new Intent(MainNavigationActivity.this, MyDownloadActivity.class);
                            if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
                                WallpapersApplication.getApplication().showInterstitial(MainNavigationActivity.this, intent, false);
                            } else {
                                WallpapersApplication.getApplication().incrementUserClickCounter();
                                startActivity(intent);
                            }

                        } else if (menuItem.getItemId() == R.id.nav_item_double_wall) {
                            if (!CommonFunctions.isNetworkAvailable(MainNavigationActivity.this)) {
                                CommonFunctions.showInternetDialog(MainNavigationActivity.this);
                                return true;
                            }
                            EventManager.sendEvent(EventManager.LBL_DOUBLE_WALLPAPER, EventManager.ATR_KEY_DOUBLE, "Left Menu");
                            Intent intent = new Intent(MainNavigationActivity.this, DoubleWallpaperActivity.class);
                            if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
                                WallpapersApplication.getApplication().showInterstitial(MainNavigationActivity.this, intent, false);
                            } else {
                                WallpapersApplication.getApplication().incrementUserClickCounter();
                                startActivity(intent);
                            }

                        } else if (menuItem.getItemId() == R.id.nav_privacy) {
                            if (!CommonFunctions.isNetworkAvailable(MainNavigationActivity.this)) {
                                CommonFunctions.showInternetDialog(MainNavigationActivity.this);
                                return true;
                            }
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_TERMS, EventManager.ATR_VALUE_TERMS);
                            Intent intent = new Intent(MainNavigationActivity.this, Termsactivity.class);
                            intent.putExtra("privacy", true);
                            startActivity(intent);

                        } else if (menuItem.getItemId() == R.id.nav_item_feedback) {
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_FEEDBACK, EventManager.ATR_VALUE_FEEDBACK);
//							JesusApplication.getApplication().incrementUserClickCounter();
                            PackageInfo pInfo = null;
                            try {
                                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }

                            final String version = pInfo.versionName;
                            final Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("plain/text");

                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConstant.EMAIL});
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback Version - v" + version);
                            String temp = settingStore.getUserID();
                            if (TextUtils.isEmpty(temp)) {
                                temp = CommonFunctions.getHardwareId(MainNavigationActivity.this);
                            }
                            temp += "\nDevice: " + CommonFunctions.getDeviceName();
                            String user_id = "Support ID: " + temp + "\n\nDescribe your problem:";

                            intent.putExtra(Intent.EXTRA_TEXT, "" + user_id);
                            startActivity(intent);
                        } else if (menuItem.getItemId() == R.id.nav_item_settings) {
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_SETTING, EventManager.ATR_VALUE_SETTING);
                            Intent intent = new Intent(MainNavigationActivity.this, SettingActivity.class);
                            startActivity(intent);
                        } else if (menuItem.getItemId() == R.id.nav_item_about) {
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_ABOUT, EventManager.ATR_VALUE_ABOUT);
//                            Intent intent1=new Intent(MainBottomNavigationActivity.this,DownloadFilesActivity.class);
//                            startActivity(intent1);

//							JesusApplication.getApplication().incrementUserClickCounter();
                            Intent intent = new Intent(MainNavigationActivity.this, AboutUsActivity.class);
                            startActivity(intent);

                        } else if (menuItem.getItemId() == R.id.nav_item_my_fav) {
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_FAV, EventManager.ATR_VALUE_FAV);
                            Intent intent = new Intent(MainNavigationActivity.this, FavoriteActivity.class);
                            if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
                                WallpapersApplication.getApplication().showInterstitial(MainNavigationActivity.this, intent, false);
                            } else {
//								JesusApplication.getApplication().incrementUserClickCounter();
                                startActivity(intent);
                            }
                        } else if (menuItem.getItemId() == R.id.nav_item_theme) {
                            showThemeAlertDialog();
//							updateTheme();
                        } else if (menuItem.getItemId() == R.id.nav_item_language) {
                            showLanguageAlertDialog();
                        } else if (menuItem.getItemId() == R.id.nav_item_tour) {
                            Intent intent = new Intent(MainNavigationActivity.this, ApplicationIntroActivity.class);

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
                            startActivity(intent);
                        } else if (menuItem.getItemId() == R.id.nav_item_wallpaper) {

                            if (marshMallowPermission != null && !marshMallowPermission.checkPermissionForExternalStorage()) {
                                isAutoChangerCalled = true;
                                checkPermission();
                                return true;
                            }
                            openAutoWallChanger();

                        } else if (menuItem.getItemId() == R.id.nav_item_wallpaper_setting) {
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_LIVE_WALL_SETTING, EventManager.ATR_VALUE_LIVE_WALL_SETTING);
                            Intent intent = new Intent(MainNavigationActivity.this, AutoWallChangerActivity.class);
                            if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
                                WallpapersApplication.getApplication().showInterstitial(MainNavigationActivity.this, intent, false);
                            } else {
                                WallpapersApplication.getApplication().incrementUserClickCounter();
                                startActivity(intent);
                            }
                        } else if (menuItem.getItemId() == R.id.nav_item_wallpaper_not) {
                            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_LIVE_WALL_WORKING, EventManager.ATR_VALUE_LIVE_WALL_WORKING);
                            showAlertDialogButtonClicked();
                        } else {
                            try {
                                if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_god_wall()) && WallpapersApplication.getApplication().getSettings().getIs_god_wall().equalsIgnoreCase("1")) {
                                    startActivity(new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/details?id=com.app.livewallpaper")));
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // set item as selected to persist highlight
//                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        registerLoginInfoListener();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isFinishing())
                        checkPermission();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 2000);


        WallpapersApplication.getApplication().addUniversalAdListener(this);

//		startService(new Intent(getBaseContext(), OnClearFromRecentService.class));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                WallpapersApplication.getApplication().sendAllDataToServer(true);
            }
        }, 3000);


        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                if (bundle.containsKey("post") || bundle.containsKey("category")) {
                    Logger.e("deeplink ", "saveinstance test yes");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            manageDeeplink(getIntent());
                        }
                    }, 2000);
                } else {
                    Logger.e("deeplink ", "saveinstance test no");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getCategory() == null || UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getCategory().size() == 0) {
            UserInfoManager favGroupManager = UserInfoManager.getInstance(getApplicationContext());
            favGroupManager.getGroupList(CommonFunctions.getHardwareId(MainNavigationActivity.this));
        } else {
            EventNotifier notifier =
                    NotifierFactory.getInstance().getNotifier(
                            NotifierFactory.EVENT_NOTIFIER_USER_INFO);
            notifier.eventNotify(EventTypes.EVENT_USER_INFO_UPDATED, null);
        }

        removeAdIcon();

        FirebaseInstanceIDService.sendRegistrationToServer(this);

//		try {
//			boolean isAuto = AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(this);
//			MenuItem nav_item_auto = navigationView.getMenu().findItem(R.id.nav_item_wallpaper_not);
//			if (isAuto)
//				nav_item_auto.setVisible(true);
//			else
//				nav_item_auto.setVisible(false);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

        CommonFunctions.renameSavedFolderIfAvailable();

        FrameLayout AdContainer1 = (FrameLayout) findViewById(R.id.AdContainer1);
        requestBanner(this, AdContainer1, false, true);
    }

    public void showAlertDialogButtonClicked() {
        // create an alert builder

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


        builder.setTitle(getResources().getString(R.string.label_not_work_title));
        builder.setMessage(getResources().getString(R.string.not_working));
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_dlg_layout, null);
        builder.setView(customLayout);
        // add a button
        builder.setPositiveButton(getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(getResources().getString(R.string.label_more_detail), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                final Intent i = new Intent(MainNavigationActivity.this, Termsactivity.class);
                i.putExtra("help", true);
                startActivity(i);
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();

        if (!isFinishing()) {
            dialog.show();
        }
    }

    private void loadInterstital() {
        CommonFunctions.getRandomAd(this, true, false);
        WallpapersApplication.getApplication().setUpAd(false);
        WallpapersApplication.getApplication().requestNewInterstitial(this);
    }

    @Override
    public void onBackPressed() {
        try {

            if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return;
            }
//            int position = FragmentPagerItem.getPosition(getArguments());
            if (tabLayout != null && tabLayout.getSelectedTabPosition() != 0) {
//				tsabLayout.setScrollPosition(0,0,false);
                viewPager.setCurrentItem(0, true);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setText(tabString[0]);
        tabLayout.getTabAt(1).setText(tabString[1]);
        tabLayout.getTabAt(2).setText(tabString[2]);
        tabLayout.getTabAt(3).setText(tabString[3]);
        tabLayout.getTabAt(4).setText(tabString[4]);


        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                FloatingActionButton button = findViewById(R.id.img_select);

                if (tab.getPosition() == 2) {
                    button.show();
//					findViewById(R.id.img_select).setVisibility(View.VISIBLE);
                } else {
                    button.hide();
//					findViewById(R.id.img_select).setVisibility(View.GONE);
                }
//                if (tab.getPosition() == 4) {
//                    updateAdDataCategory(500);
//                } else
//                    updateAdData(500);


                if (tab.getPosition() == 0) {
                    hideShowFilter(true);
                    if (adapter != null && adapter.mFragmentList != null) {
                        ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(0);
                        setMenuSelction(parentHomeFragment.getfilter());
                    }
                } else if (tab.getPosition() == 1) {
                    hideShowFilter(true);
                    if (adapter != null && adapter.mFragmentList != null) {
                        ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(1);
                        setMenuSelction(parentHomeFragment.getfilter());
                    }
                } else if (tab.getPosition() == 2) {
                    hideShowFilter(true);
                    if (adapter != null && adapter.mFragmentList != null) {
                        ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(2);
                        setMenuSelction(parentHomeFragment.getfilter());
                    }
                } else
                    hideShowFilter(false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
//                if(tab.getPosition()==0)
//                    tab.setIcon(R.mipmap.ic_pager_jesus);
//                else if(tab.getPosition()==1)
//                    tab.setIcon(R.mipmap.ic_pager_home);
//                else  if(tab.getPosition()==2)
//                    tab.setIcon(R.mipmap.ic_pager_trending);
//                else  if(tab.getPosition()==3)
//                    tab.setIcon(R.mipmap.ic_pager_category);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void updateTheme() {
        EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_THEME, EventManager.ATR_VALUE_THEME);

        startActivity(new Intent(MainNavigationActivity.this, ThemeChangingActivity.class));
        this.overridePendingTransition(0, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 200);

//		recreate();
    }

    View actionView;
    Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                actionView = findViewById(R.id.action_filter);
            }
        });


        Drawable yourdrawable = menu.getItem(0).getIcon(); // change 0 with 1,2 ...
        yourdrawable.mutate();

        Drawable yourdrawable1 = menu.getItem(1).getIcon(); // change 0 with 1,2 ...
        yourdrawable1.mutate();

        Drawable yourdrawable2 = menu.getItem(2).getIcon(); // change 0 with 1,2 ...
        yourdrawable2.mutate();

        Resources.Theme themes = getTheme();
        TypedValue storedValueInTheme = new TypedValue();
        if (themes.resolveAttribute(R.attr.my_textColor, storedValueInTheme, true)) {
            yourdrawable.setColorFilter(storedValueInTheme.data, PorterDuff.Mode.SRC_IN);
            yourdrawable1.setColorFilter(storedValueInTheme.data, PorterDuff.Mode.SRC_IN);
            yourdrawable2.setColorFilter(storedValueInTheme.data, PorterDuff.Mode.SRC_IN);
        }


//		try {
//			MenuItem menuItemShopCart = menu.findItem(R.id.nav_item_status_saver);
//			MenuItemBadge.update(this, menuItemShopCart, new MenuItemBadge.Builder());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		MenuItemBadge.getBadgeTextView(menuItemShopCart).setText("New");
//		MenuItemBadge.getBadgeTextView(menuItemShopCart).setHighLightMode();

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            MenuItem item = menu.findItem(R.id.action_search);
            try {
                if (viewPager != null && WallpapersApplication.getApplication().getSettings().getIs_search_enable().equalsIgnoreCase("1")) {
                    item.setVisible(true);
                } else {
                    // disabled
                    item.setVisible(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                item.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            MenuItem menuItem = menu.findItem(R.id.action_filter);
            if (WallpapersApplication.getApplication().getSettings().isFilterEnable()) {
                menuItem.setVisible(true);
            } else {
                // disabled
                menuItem.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MenuItem menuItem = menu.findItem(R.id.action_ad);
        if (CommonFunctions.isProIconShow()) {
            try {
                if (settingStore.getIsPro()) {
                    menuItem.setVisible(false);
                } else
                    menuItem.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
            menuItem.setVisible(false);


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.action_search) {
            if (!CommonFunctions.isNetworkAvailable(this)) {
                CommonFunctions.showInternetDialog(this);
                return true;
            }
            WallpapersApplication.getApplication().incrementUserClickCounter();
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_ad) {
            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_PRO, EventManager.ATR_VALUE_PRO_TOOLBAR);
            displayAlert();
        } else if (id == R.id.action_filter) {
            if (powerMenu != null)
                powerMenu.showAsDropDown(actionView); // view is an anchor
        }
        return super.onOptionsItemSelected(item);
    }

    PowerMenu powerMenu;

    private void videoPowerMenu(int filter_pos) {
        Logger.e(TAG, "filter_pos:" + filter_pos);


        if (settingStore.getTheme() == 0) {
            powerMenu = new PowerMenu.Builder(MainNavigationActivity.this)
                    .addItem(new PowerMenuItem(getResources().getString(R.string.label_popular), filter_pos == 0 ? true : false)) // add an item.
                    .addItem(new PowerMenuItem(getResources().getString(R.string.label_new_Added), filter_pos == 1 ? true : false)) // add an item.
                    .addItem(new PowerMenuItem(getResources().getString(R.string.label_random), filter_pos == 2 ? true : false)) // add an item.
                    .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | LEFT).
                    .setMenuRadius(10f) // sets the corner radius.
                    .setMenuShadow(8f) // sets the shadow.0
                    .setTextColor(ContextCompat.getColor(MainNavigationActivity.this, R.color.colorPrimary1))
                    .setTextGravity(Gravity.LEFT)
                    .setSelectedTextColor(Color.WHITE)
                    .setMenuColor(Color.WHITE)
                    .setSelectedMenuColor(ContextCompat.getColor(MainNavigationActivity.this, R.color.colorPrimary1))
                    .setOnMenuItemClickListener(onMenuItemClickListener)
                    .build();
        } else if (settingStore.getTheme() == 1) {
            powerMenu = new PowerMenu.Builder(MainNavigationActivity.this)
                    .addItem(new PowerMenuItem(getResources().getString(R.string.label_popular), filter_pos == 0 ? true : false)) // add an item.
                    .addItem(new PowerMenuItem(getResources().getString(R.string.label_new_Added), filter_pos == 1 ? true : false)) // add an item.
                    .addItem(new PowerMenuItem(getResources().getString(R.string.label_random), filter_pos == 2 ? true : false)) // add an item.
                    .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | LEFT).
                    .setMenuRadius(10f) // sets the corner radius.
                    .setMenuShadow(8f) // sets the shadow.0
                    .setTextColor(ContextCompat.getColor(MainNavigationActivity.this, R.color.white))
                    .setTextGravity(Gravity.LEFT)
                    .setSelectedTextColor(ContextCompat.getColor(MainNavigationActivity.this, R.color.colorPrimary1))
                    .setMenuColor(ContextCompat.getColor(MainNavigationActivity.this, R.color.colorPrimary1))
                    .setSelectedMenuColor(ContextCompat.getColor(MainNavigationActivity.this, R.color.white))
                    .setOnMenuItemClickListener(onMenuItemClickListener)
                    .build();
        }

        setMenuSelction(filter_pos);
    }

    private void showCaseView() {
        uninstalledOld();

//        try {
//            if (actionView != null && !settingStore.getIsFilterShow() && WallpapersApplication.getApplication().getSettings().isFilterEnable()) {
//                new GuideView.Builder(this)
//                        .setTitle(getResources().getString(R.string.filter_text))
//                        .setContentText(getResources().getString(R.string.label_apply_filter_wall))
//                        .setGravity(smartdevelop.ir.eram.showcaseviewlib.config.Gravity.auto) //optional
//                        .setDismissType(DismissType.anywhere) //optional - default DismissType.targetView
//                        .setTargetView(actionView)
//                        .setContentTextSize(12)//optional
//                        .setTitleTextSize(16)//optional
//                        .build()
//                        .show();
//                settingStore.setIsFilterShow(true);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private OnMenuItemClickListener< PowerMenuItem > onMenuItemClickListener = new OnMenuItemClickListener< PowerMenuItem >() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
//            Toast.makeText(getBaseContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
            powerMenu.setSelectedPosition(position); // change selected item
            powerMenu.dismiss();
            String name = "";
            if (position == 0) {
                name = getResources().getString(R.string.label_popular);
            } else if (position == 1) {
                name = getResources().getString(R.string.label_new_Added);
            } else if (position == 2) {
                name = getResources().getString(R.string.label_random);
            }
            if (adapter != null && adapter.mFragmentList != null && tabLayout.getSelectedTabPosition() == 0) {
                ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(0);
                if (parentHomeFragment.getfilter() == position)
                    return;
                parentHomeFragment.setfilter(position);
                parentHomeFragment.refreshCall(true);
                EventManager.sendEvent(EventManager.LBL_FILTER, "Home", name);
            } else if (adapter != null && adapter.mFragmentList != null && tabLayout.getSelectedTabPosition() == 1) {
                ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(1);
                if (parentHomeFragment.getfilter() == position)
                    return;
                parentHomeFragment.setfilter(position);
                parentHomeFragment.refreshCall(true);
                EventManager.sendEvent(EventManager.LBL_FILTER, "Live Wallpaper", name);
            } else if (adapter != null && adapter.mFragmentList != null && tabLayout.getSelectedTabPosition() == 2) {
                ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(2);
                if (parentHomeFragment.getfilter() == position)
                    return;
                parentHomeFragment.setfilter(position);
                parentHomeFragment.refreshCall(true);
                EventManager.sendEvent(EventManager.LBL_FILTER, "Exclusive Wallpaper", name);
            }
        }
    };

    public void setMenuSelction(int position) {
        Logger.e(TAG, "setMenuSelction:" + position);
        if (powerMenu != null)
            powerMenu.setSelectedPosition(position);
    }

    private void registerLoginInfoListener() {
        EventNotifier notifier =
                NotifierFactory.getInstance().getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_USER_INFO);
        notifier.registerListener(this, ListenerPriority.PRIORITY_HIGH);


        notifier =
                NotifierFactory.getInstance().getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_AD_STATUS);
        notifier.registerListener(this, ListenerPriority.PRIORITY_HIGH);
    }

    private void unregisterLoginInfoListener() {
        EventNotifier notifier =
                NotifierFactory.getInstance().getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_USER_INFO);
        notifier.unRegisterListener(this);

        notifier =
                NotifierFactory.getInstance().getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_AD_STATUS);
        notifier.unRegisterListener(this);
    }

    @Override
    public int eventNotify(int eventType, Object eventObject) {
        int eventState = EventState.EVENT_IGNORED;
        switch (eventType) {
            case EventTypes.EVENT_USER_INFO_UPDATED:

                eventState = EventState.EVENT_PROCESSED;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            findViewById(R.id.rl_progress).setVisibility(View.GONE);
                            loadInterstital();
                            initializeWithUpgrate();
//                            uninstalledOld();

                            viewPager = (ViewPager) findViewById(R.id.viewpager);
                            setupViewPager(viewPager);

                            tabLayout = (TabLayout) findViewById(R.id.tabs);
                            tabLayout.setupWithViewPager(viewPager);
                            setupTabIcons();

                            invalidateOptionsMenu();
//						refreshMenu();
//						MediationTestSuite.launch(MainBottomNavigationActivity.this);

//                            WallpapersApplication.getApplication().refreshAd();
                            startSplashDownload();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;

            case EventTypes.EVENT_CALL_FAV:
                eventState = EventState.EVENT_PROCESSED;
                getFavListResponse();
                break;
            case EventTypes.EVENT_AD_LOADED:
                eventState = EventState.EVENT_PROCESSED;
//                updateAdData(2000);
                break;
            case EventTypes.EVENT_AD_NATIVE_DETAIL_LOADED:
                eventState = EventState.EVENT_PROCESSED;
//                updateAdDataCategory(2000);
                break;
            case EventTypes.EVENT_USER_INTERNET_NOT:
                eventState = EventState.EVENT_PROCESSED;
                CommonFunctions.showInternetDialog(MainNavigationActivity.this);
                break;
            case EventTypes.EVENT_AD_REMOVED:
                eventState = EventState.EVENT_PROCESSED;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    removeAd();
                                    WallpapersApplication.getApplication().isNativeAdEnable = false;
                                    WallpapersApplication.getApplication().destroyGoogleAd();
                                    WallpapersApplication.getApplication().destroyGoogleNativeDetail();
                                    WallpapersApplication.getApplication().destroyGGDetail();
                                    removeAdIcon();
                                    invalidateOptionsMenu();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 2000);

                    }
                });
                break;
        }
        return eventState;
    }


    public static boolean isShowResumeAd;

    @Override
    public void onResume() {
        try {
            super.onResume();
            CommonFunctions.hideKeyboard(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.e("onResume", "onResume");
        inAppReview();
        try {
            if (!TextUtils.isEmpty(UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getApp_settings().get(0).getIn_app_update()) && UserInfoManager.getInstance(this).getUserInfo().getApp_settings().get(0).getIn_app_update().equals("1")) {
                checkNewAppVersionState();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkPermission() {

        if (!marshMallowPermission.checkPermissionForExternalStorage()) {
            marshMallowPermission.requestPermissionForExternalStorage();
        } else {
            showCaseView();
//				Common.renameFolder(this);
//                getCategoryListResponse(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.e("onRequestPermissionsResult", "Activity");
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//			Common.renameFolder(this);
            if (isAutoChangerCalled)
                openAutoWallChanger();
//			checkPermission();
        } else if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            if (isAutoChangerCalled)
                Toast.makeText(this, getResources().getString(R.string.per_set_Auto), Toast.LENGTH_SHORT).show();
        }
        showCaseView();
    }

    @Override
    public void onAdLoaded() {
        Logger.e(TAG, "onAdLoaded");
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        Logger.e(TAG, "onAdFailedToLoad");
    }

    @Override
    public void onAdClosed() {
        loadInterstital();
    }


    public void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
        intent.putExtra(
                Intent.EXTRA_TEXT,
                getResources().getString(R.string.label_down_text) + AppConstant.STORE_LINK);

        Intent chooser = Intent.createChooser(intent, getResources().getString(R.string.label_invite_frnd)
                + getString(R.string.app_name));
        try {
            startActivity(chooser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initializeWithUpgrate() {
        try {
//			showUpdateDialog(true);

            try {
                if (isFinishing()) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            int currentVersion = Integer.parseInt(CommonFunctions.appVerstion(MainNavigationActivity.this));
            int serverVersion = Integer.parseInt(UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getApp_settings().get(0).getAppVersion());
            if (currentVersion < serverVersion) {
                if (!TextUtils.isEmpty(UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getApp_settings().get(0).getIn_app_update()) && UserInfoManager.getInstance(this).getUserInfo().getApp_settings().get(0).getIn_app_update().equals("1")) {
                    checkForAppUpdate();
                } else {
                    if (!TextUtils.isEmpty(UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getApp_settings().get(0).getForce_update()) && UserInfoManager.getInstance(this).getUserInfo().getApp_settings().get(0).getForce_update().equals("1"))
                        showUpdateDialog(true);
                    else
                        showUpdateDialog(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showUpdateDialog(final boolean p_MandatoryUpdate) {

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

        builder.setCancelable(false);

        if (!p_MandatoryUpdate) {
            String server_msg = UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getApp_settings().get(0).getApp_update_text();
            String msg = getResources().getString(R.string.label_update_avail) + "\n\n" + getResources().getString(R.string.label_changelog) + "\n\n" + server_msg + "\n\n" + getResources().getString(R.string.label_update_use_it);
            builder.setMessage(msg);
        } else {
            String server_msg = UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getApp_settings().get(0).getApp_update_text();
            String msg = getResources().getString(R.string.label_update_avail) + "\n\n" + getResources().getString(R.string.label_changelog) + " \n\n" + server_msg + "\n\n" + getResources().getString(R.string.label_new_update_continue);
            builder.setMessage(msg);
        }

        builder.setTitle("\uD83D\uDE4F" + getResources().getString(R.string.label_new_up) + "\uD83D\uDE4F")

                .setPositiveButton(getResources().getString(R.string.label_update), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String appPackageName = getPackageName(); // getPackageName()

                        if (WallpapersApplication.getApplication().getSettings() != null && !TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getPackage())) {
                            appPackageName = WallpapersApplication.getApplication().getSettings().getPackage();
                        }

                        try {
                            Intent appStoreIntent = new Intent(Intent.ACTION_VIEW, Uri
                                    .parse("market://details?id="
                                            + appPackageName));
                            appStoreIntent.setPackage("com.android.vending");
                            startActivity(appStoreIntent);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://play.google.com/store/apps/details?id="
                                            + appPackageName)));
                        }
                        if (p_MandatoryUpdate)
                            MainNavigationActivity.this.finish();

                    }
                });

        if (!p_MandatoryUpdate) {
            builder.setNegativeButton(getResources().getString(R.string.label_later), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        if (!isFinishing()) {
            builder.show();
        }


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        manageDeeplink(intent);
    }

    private void manageDeeplink(Intent intent) {
        try {
            if (intent != null) {
                Wallpapers post = (Wallpapers) intent.getSerializableExtra("post");
                Category category = (Category) intent.getSerializableExtra("category");
                if (post != null) {
//                    if(post.getType().equalsIgnoreCase(AppConstant.TYPE_IMAGE_STRING)){
                    List< Wallpapers > mList = new ArrayList<>();
                    mList.add(post);

                    boolean isNewDetail = true;

                    Intent intent1;
                    intent1 = new Intent(this, WallpaperDetailActivity.class);

                    intent1.putExtra("post", post);
                    intent1.putExtra("post_list", (Serializable) mList);
                    intent1.putExtra("isTrending", false);
                    intent1.putExtra("pos", "" + post.getPostId());
                    startActivity(intent1);
//                    }
                    intent.removeExtra("post");
                    getIntent().removeExtra("post");
                } else if (category != null) {
                    final Intent intentCat = new Intent(MainNavigationActivity.this, CategoryListingActivity.class);
                    intentCat.putExtra("category", category.getName());
                    intentCat.putExtra("category_name", category.getDisplay_name());
                    startActivity(intentCat);

                    intent.removeExtra("category");
                    getIntent().removeExtra("category");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean isAutoChangerCalled;

    private void openAutoWallChanger() {
        try {
            isAutoChangerCalled = false;
            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_LIVE_WALL, EventManager.ATR_VALUE_LIVE_WALL);

            if (TextUtils.isEmpty(settingStore.getImages()))
                CommonFunctions.saveToSettingData(settingStore);

            File path = new File(CommonFunctions.getSavedFilePath());
            path.mkdirs();

            File files[] = path.listFiles();
            if (files == null || files.length == 0) {
                Toast.makeText(MainNavigationActivity.this, getResources().getString(R.string.label_2_more), Toast.LENGTH_SHORT).show();
                return;
            }
            settingStore.setLastAutoChangedTimeTemp();

            try {
                Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(MainNavigationActivity.this,
                                WallpapersService.class));
                WallpapersApplication.getApplication().incrementUserClickCounter();
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    startActivityForResult(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new
                            ComponentName(MainNavigationActivity.this, WallpapersService.class))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 200);
                } catch (Exception e2) {
                    Toast.makeText(MainNavigationActivity.this, R.string
                            .toast_failed_launch_wallpaper_chooser, Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void hideShowFilter(boolean isShow) {
        try {
            MenuItem menuItem = menu.findItem(R.id.action_filter);
            if (!isShow) {
                menuItem.setVisible(false);
            } else
                menuItem.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ViewPagerAdapter adapter;

    private void setupViewPager(ViewPager viewPager) {
        viewPager.setOffscreenPageLimit(5);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        CategoryHomeFragment categoryHomeFragment = null;
        ParentHomeFragment homeFragment = null;
        ParentHomeFragment liveFragment = null;
        ParentHomeFragment parentTrending = null;
        ParentHomeFragment exclusiveFragment = null;
//		if(savedInstanceState!=null){
//			List<Fragment> fragments = mFragmentManager.getFragments();
//			for (int i = 0; i < fragments.size(); i++) {
//				if (fragments.get(i) instanceof ParentHomeFragment) {
//					ParentHomeFragment temp= (ParentHomeFragment) fragments.get(i);
//					if(temp.getScreenType().equalsIgnoreCase(ParentHomeFragment.TYPE_HOME)){
//						homeFragment=temp;
//					}else if(temp.getScreenType().equalsIgnoreCase(ParentHomeFragment.TYPE_LIVE_WALLPAPER)){
//						liveFragment=temp;
//					}else if(temp.getScreenType().equalsIgnoreCase(ParentHomeFragment.TYPE_TRENDING)){
//						parentTrending=temp;
//					}
//				}else if (fragments.get(i) instanceof CategoryHomeFragment) {
//					categoryHomeFragment= (CategoryHomeFragment) fragments.get(i);
//				}/*else if (fragments.get(i) instanceof UploadFragment) {
//					addPostFragment = (UploadFragment) fragments.get(i);
//				}else if (fragments.get(i) instanceof ProfileFragment  ) {
//					profileFragment = (ProfileFragment) fragments.get(i);
//				}*/
//			}
//		}

        if (categoryHomeFragment == null) {
            categoryHomeFragment = new CategoryHomeFragment();
        }
        Bundle b = new Bundle();
        if (homeFragment == null) {
            homeFragment = new ParentHomeFragment();
            b.putString("screenType", ParentHomeFragment.TYPE_HOME);
            b.putString("category", "");
            homeFragment.setArguments(b);
        }
        adapter.addFragment(homeFragment, "Home");


        if (liveFragment == null) {
            liveFragment = new ParentHomeFragment();
            b = new Bundle();
            b.putString("category", "");
            b.putString("screenType", ParentHomeFragment.TYPE_LIVE_WALLPAPER);
            b.putBoolean("isVideoWall", true);
            liveFragment.setArguments(b);
        }
        adapter.addFragment(liveFragment, "Jesus");

        if (exclusiveFragment == null) {
            exclusiveFragment = new ParentHomeFragment();
            Bundle b1 = new Bundle();
            b1.putString("category", "");
            b1.putString("screenType", ParentHomeFragment.TYPE_EXCLUSIVE);
            exclusiveFragment.setArguments(b1);
        }

        adapter.addFragment(exclusiveFragment, "Exclusive");

        if (parentTrending == null) {
            parentTrending = new ParentHomeFragment();
            Bundle b1 = new Bundle();
            b1.putString("category", "-1");
            b1.putString("screenType", ParentHomeFragment.TYPE_TRENDING);
            parentTrending.setArguments(b1);
        }
        adapter.addFragment(parentTrending, "Trending");

        adapter.addFragment(categoryHomeFragment, "Category");


        viewPager.setAdapter(adapter);
    }

    public static boolean isFavListCalled;

    private void getFavListResponse() {
        try {
            if (isFinishing())
                return;
            RequestManager manager = new RequestManager(WallpapersApplication.sContext);
            manager.getLikeWallService(CommonFunctions.getHardwareId(this), new NetworkCommunicationManager.CommunicationListnerNew() {
                @Override
                public void onSuccess(final IModel response, int operationCode) {

                    if (isFinishing())
                        return;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                isFavListCalled = true;
                                GetLikeModel model = (GetLikeModel) response;
                                if (model != null && model.getStatus().equalsIgnoreCase("1")) {
                                    UserInfoManager userInfoManager = UserInfoManager.getInstance(getApplicationContext());
                                    if (model.getLike_live() != null) {
                                        userInfoManager.getUserInfo().setLike_live(model.getLike_live());
                                    } else
                                        userInfoManager.getUserInfo().setLike_live(new ArrayList< Wallpapers >());

                                    if (model.getLike() != null) {
                                        userInfoManager.getUserInfo().setLike(model.getLike());
                                    } else
                                        userInfoManager.getUserInfo().setLike(new ArrayList< Wallpapers >());

                                    if (model.getLike_exclusive() != null) {
                                        userInfoManager.getUserInfo().setLike_exclusive(model.getLike_exclusive());
                                    } else
                                        userInfoManager.getUserInfo().setLike_exclusive(new ArrayList< Wallpapers >());


                                } else if (model != null && model.getStatus().equalsIgnoreCase("0")) {

                                }

                                EventNotifier notifier =
                                        NotifierFactory.getInstance().getNotifier(
                                                NotifierFactory.EVENT_NOTIFIER_USER_INFO);
                                notifier.eventNotify(EventTypes.EVENT_USER_FAV_LOADED, null);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                @Override
                public void onStartLoading() {
                }

                @Override
                public void onFail(WebServiceError errorMsg) {
                    isFavListCalled = true;
                    EventNotifier notifier =
                            NotifierFactory.getInstance().getNotifier(
                                    NotifierFactory.EVENT_NOTIFIER_USER_INFO);
                    notifier.eventNotify(EventTypes.EVENT_USER_FAV_LOADED, null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private List< Fragment > mFragmentList = new ArrayList<>();
        private List< String > mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        public void destroy() {
            if (mFragmentList != null) {
                mFragmentList.clear();
                mFragmentList = null;
            }

            if (mFragmentTitleList != null) {
                mFragmentTitleList.clear();
                mFragmentTitleList = null;
            }
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            if (mFragmentList != null)
                return mFragmentList.size();
            else
                return 0;
        }

        public void addFragment(Fragment fragment, String title) {
            if (mFragmentList == null) {
                mFragmentList = new ArrayList<>();
                mFragmentTitleList = new ArrayList<>();
            }
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            return mFragmentTitleList.get(position);
            // return null to display only the icon
            return null;
        }
    }

    private void startSplashDownload() {
        List< Wallpapers > list = UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getSplash();
        for (int i = 0; i < list.size(); i++) {
            Wallpapers post = list.get(i);
            String path = CommonFunctions.getDomainImages() + "splash/" + post.getImg();
            if (post.getImg().startsWith("http")) {
                path = post.getImg();
            }
//                                String dst = Common.createSpashDirCache()+"/"+post.getImgId()+AppConstant.DOWNLOAD_EXTENTION;
            String dst;
//			if (post.getIs_event().equalsIgnoreCase("1"))
//				dst = Common.createEventDirCache() + "/" + post.getEnd_date() + AppConstant.DOWNLOAD_EXTENTION;
//			else {
//				try {
//					deleteRecursive(new File(Common.createEventDirCache()));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
            dst = CommonFunctions.createSpashDirCache() + "/" + post.getImg();
//			}
            File file = new File(dst);
            if (file != null && file.exists())
                continue;
            initializeDownload(dst, path);
            break;
        }
    }

    private ThinDownloadManager downloadManagerThin;
    private static final int DOWNLOAD_THREAD_POOL_SIZE = 1;

    private void initializeDownload(String pathToDownload, String downloadURL) {

        downloadManagerThin = new ThinDownloadManager(DOWNLOAD_THREAD_POOL_SIZE);

        RetryPolicy retryPolicy = new DefaultRetryPolicy();

        Uri downloadUri = Uri.parse(downloadURL);
        Uri destinationUri = Uri.parse(pathToDownload);
        final DownloadRequest downloadRequest1 = new DownloadRequest(downloadUri)
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setRetryPolicy(retryPolicy)
                .setDownloadContext("Download1")
                .setStatusListener(new DownloadStatusListenerV1() {
                    @Override
                    public void onDownloadComplete(DownloadRequest downloadRequest) {

                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {

                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {

                    }
                });

        int downloadId1 = 0;
        if (downloadManagerThin.query(downloadId1) == com.thin.downloadmanager.DownloadManager.STATUS_NOT_FOUND) {
            downloadId1 = downloadManagerThin.add(downloadRequest1);
        }
    }

    private String oldPackageName = "com.app.jesuslivewallpaper";

    private void uninstalledOld() {

        if (TextUtils.isEmpty(oldPackageName)) {
            return;
        }
        if (CommonFunctions.isPackageInstalled(this, oldPackageName)) {
            CommonFunctions.showDialogCustom(this, getString(R.string.uninstall_title), getString(R.string.uninstall_msg), getResources().getString(R.string.uninstall_btn), "", false, new CommonFunctions.DialogOnButtonClickListener() {
                @Override
                public void onOKButtonCLick() {
                    uninstallPackage(MainNavigationActivity.this, oldPackageName);
                }

                @Override
                public void onCancelButtonCLick() {

                }
            });


//			HungamaAlertDialog timedOutDialog = new HungamaAlertDialog(this);
//			timedOutDialog.setTitle("UnInstall Old App");
//			timedOutDialog
//					.setMsg("Please UnInstall old 4K Wallpaper to continue with New App.\n\nWe have migrate old data to this application.");
//
//			final DialogInterface.OnClickListener dialogButtonClickListener =
//					new DialogInterface.OnClickListener() {
//
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							dialog.dismiss();
//							uninstallPackage(MainBottomNavigationActivity.this,oldPackageName);
//						}
//					};
//			timedOutDialog.setPositiveButton(
//					"UnInstall",
//					dialogButtonClickListener);
//
//			timedOutDialog.setCanceledOnTouchOutside(false);
//			timedOutDialog.setCancelable(false);
//			View view;
//
//			timedOutDialog.setView(null);
//			timedOutDialog.show();
        }
    }

    public void uninstallPackage(Context context, String packageName) {

        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        System.err.println("old sdk");
    }

    private static final int REQ_CODE_VERSION_UPDATE = 530;
    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;

    @Override
    public void onActivityResult(int requestCode, final int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {

            case REQ_CODE_VERSION_UPDATE:
                if (resultCode != RESULT_OK) { //RESULT_OK / RESULT_CANCELED / RESULT_IN_APP_UPDATE_FAILED
                    Logger.i("Update flow", "Update flow failed! Result code: " + resultCode);
                    // If the update is cancelled or fails,
                    // you can request to start the update again.
                    unregisterInstallStateUpdListener();
                }

                break;
//            default:
//                if (bp != null && !bp.handleActivityResult(requestCode, resultCode, intent))
//                    super.onActivityResult(requestCode, resultCode, intent);
//                break;
        }
    }


    private void checkForAppUpdate() {

        // Returns an intent object that you use to check for an update.
        Task< AppUpdateInfo > appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Create a listener to track request state updates.
        installStateUpdatedListener = new InstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(InstallState installState) {
                // Show module progress, log state, or install the update.
                if (installState.installStatus() == InstallStatus.DOWNLOADED)
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    popupSnackbarForCompleteUpdateAndUnregister();
            }
        };

        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener< AppUpdateInfo >() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    // Request the update.
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                        // Before starting an update, register a listener for updates.
                        appUpdateManager.registerListener(installStateUpdatedListener);
                        // Start an update.
                        startAppUpdateFlexible(appUpdateInfo);
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        // Start an update.
                        startAppUpdateImmediate(appUpdateInfo);
                    }
                }
            }
        });

    }

    private void startAppUpdateImmediate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void startAppUpdateFlexible(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            unregisterInstallStateUpdListener();
        }
    }

    /**
     * Displays the snackbar notification and call to action.
     * Needed only for Flexible app update
     */
    private void popupSnackbarForCompleteUpdateAndUnregister() {
        appUpdateManager.completeUpdate();

//		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        Snackbar snackbar =
//                Snackbar.make(drawerLayout, "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE);
//        snackbar.setAction("RESTART", new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                appUpdateManager.completeUpdate();
//            }
//        });
//        snackbar.setActionTextColor(getResources().getColor(R.color.white));
//        snackbar.show();
        unregisterInstallStateUpdListener();
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private void checkNewAppVersionState() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener< AppUpdateInfo >() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                //FLEXIBLE:
                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    popupSnackbarForCompleteUpdateAndUnregister();
                }

                //IMMEDIATE:
                if (appUpdateInfo.updateAvailability()
                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // If an in-app update is already running, resume the update.
                    startAppUpdateImmediate(appUpdateInfo);
                }
            }
        });
    }

    /**
     * Needed only for FLEXIBLE update
     */
    private void unregisterInstallStateUpdListener() {
        if (appUpdateManager != null && installStateUpdatedListener != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener);
    }


    // PRODUCT & SUBSCRIPTION IDS

    private static final List< String > LIST_OF_SKUS = Collections.unmodifiableList(
            new ArrayList< String >() {{
                add(PRODUCT_ID);
            }});

    private static final String PRODUCT_ID = "weekly_subscription3";

    private BillingClient billingClient;
    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List< Purchase > purchases) {
            // To be implemented in a later section.
            if (billingResult == null) {
                Log.wtf(TAG_PURCHASE, "onPurchasesUpdated: null BillingResult");
                return;
            }
            int responseCode = billingResult.getResponseCode();
            String debugMessage = billingResult.getDebugMessage();
            Log.d(TAG_PURCHASE, String.format("onPurchasesUpdated: %s %s", responseCode, debugMessage));
            switch (responseCode) {
                case BillingClient.BillingResponseCode.OK:
                    EventManager.sendEvent(EventManager.ATR_KEY_PURCHASE, "IN-APP", "onPurchasesUpdated");
                    if (purchases == null) {
                        Log.d(TAG_PURCHASE, "onPurchasesUpdated: null purchase list");
                        processPurchases(null);
                    } else {
                        processPurchases(purchases);
                    }
                    break;
                case BillingClient.BillingResponseCode.USER_CANCELED:
                    EventManager.sendEvent(EventManager.ATR_KEY_PURCHASE, "IN-APP", "User canceled");
                    Log.i(TAG_PURCHASE, "onPurchasesUpdated: User canceled the purchase");
                    break;
                case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                    EventManager.sendEvent(EventManager.ATR_KEY_PURCHASE, "IN-APP", "already owns");
                    Log.i(TAG_PURCHASE, "onPurchasesUpdated: The user already owns this item");
                    if (purchases == null) {
                        Log.d(TAG_PURCHASE, "onPurchasesUpdated: null purchase list");
                        processPurchases(null);
                    } else {
                        processPurchases(purchases);
                    }
                    break;
                case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                    EventManager.sendEvent(EventManager.ATR_KEY_PURCHASE, "IN-APP", "DEVELOPER_ERROR");
                    Log.e(TAG_PURCHASE, "onPurchasesUpdated: Developer error means that Google Play " +
                            "does not recognize the configuration. If you are just getting started, " +
                            "make sure you have configured the application correctly in the " +
                            "Google Play Console. The SKU product ID must match and the APK you " +
                            "are using must be signed with release keys."
                    );
                    break;
            }
        }
    };

    private void initializeBillingNew() {
        EventManager.sendEvent(EventManager.ATR_KEY_PURCHASE, "IN-APP", "initializeBilling");
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG_PURCHASE, "onBillingSetupFinished:");
                    // The BillingClient is ready. You can query purchases here.
                    querySkuDetails();

                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.i(TAG_PURCHASE, "onBillingServiceDisconnected:");
            }
        });
    }

    /**
     * Query Google Play Billing for existing purchases.
     * <p>
     * New purchases will be provided to the PurchasesUpdatedListener.
     * You still need to check the Google Play Billing API to know when purchase tokens are removed.
     */
    public void queryPurchases() {
        if (billingClient != null && !billingClient.isReady()) {
            Log.e(TAG_PURCHASE, "queryPurchases: BillingClient is not ready");
            showToast("Please try again after some time.");
            return;
        }
        Log.d(TAG_PURCHASE, "queryPurchases: SUBS");
        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List< Purchase > list) {
                processPurchases(list);
            }
        });
    }

    /**
     * Purchases are observable. This list will be updated when the Billing Library
     * detects new or existing purchases. All observers will be notified.
     */
    public MutableLiveData< List< Purchase > > purchases = new MutableLiveData<>();

    /**
     * Send purchase SingleLiveEvent and update purchases LiveData.
     * <p>
     * The SingleLiveEvent will trigger network call to verify the subscriptions on the sever.
     * The LiveData will allow Google Play settings UI to update based on the latest purchase data.
     */
    private void processPurchases(List< Purchase > purchasesList) {
        if (purchasesList != null) {
            Log.d(TAG_PURCHASE, "processPurchases: " + purchasesList.size() + " purchase(s)");
        } else {
            Log.d(TAG_PURCHASE, "processPurchases: with no purchases");

        }
        if (isUnchangedPurchaseList(purchasesList)) {
            Log.d(TAG_PURCHASE, "processPurchases: Purchase list has not changed");
            return;
        }
        purchases.postValue(purchasesList);
        if (purchasesList != null && purchasesList.size() > 0) {

            for (final Purchase purchase : purchasesList) {

                // Global check to make sure all purchases are signed correctly.
                // This check is best performed on your server.
                int purchaseState = purchase.getPurchaseState();
                Logger.e(TAG_PURCHASE, "purchaseState:" + purchaseState);
                if (purchaseState == Purchase.PurchaseState.PURCHASED) {
                    EventManager.sendEvent(EventManager.ATR_KEY_PURCHASE, "IN-APP", "PURCHASED");
                    if (!isSignatureValid(purchase)) {
                        Log.e(TAG, "Invalid signature on purchase. Check to make " +
                                "sure your public key is correct.");
                        continue;
                    }

                    purchaseView.setVisibility(View.GONE);
                    if (!purchase.isAcknowledged()) {
                        acknowledgePurchase(purchase);
                    } else
                        purchaseAPICalled(purchase);
                } else if (purchaseState == Purchase.PurchaseState.PENDING) {
                    EventManager.sendEvent(EventManager.ATR_KEY_PURCHASE, "IN-APP", "PENDING");
                    // make sure the state is set
//                    setSkuStateFromPurchase(purchase);
                    showToast("Purchase is pending. it will done automatically after some time");
                    purchaseView.setVisibility(View.GONE);
                } else if (purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                    EventManager.sendEvent(EventManager.ATR_KEY_PURCHASE, "IN-APP", "UNSPECIFIED_STATE");
                    // make sure the state is set
//                    setSkuStateFromPurchase(purchase);
                    showToast("Not able to purchase. try again later");
                    purchaseView.setVisibility(View.GONE);
                }
            }

            logAcknowledgementStatus(purchasesList);
        } else
            onClickToPurchase();
    }

    /**
     * Ideally your implementation will comprise a secure server, rendering this check unnecessary.
     *
     * @see [Security]
     */
    private boolean isSignatureValid(@NonNull Purchase purchase) {
        return Security.verifyPurchase(purchase.getOriginalJson(), purchase.getSignature());
    }

    private void acknowledgePurchase(Purchase purchase) {

        Log.d(TAG_PURCHASE, "acknowledgePurchase");
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                Logger.e(TAG_PURCHASE, "acknowledgePurchase:" + billingClient);

                if (billingClient != null)
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                        @Override
                        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                            Logger.e(TAG_PURCHASE, "onAcknowledgePurchaseResponse:" + billingResult);
                            int responseCode = billingResult.getResponseCode();
                            String debugMessage = billingResult.getDebugMessage();
                            Log.d(TAG_PURCHASE, "acknowledgePurchase: " + responseCode + " " + debugMessage);
                            if (billingResult.getResponseCode()
                                    == BillingClient.BillingResponseCode.OK) {
                                purchaseAPICalled(purchase);
                            }
                        }
                    });
            }
        }
    }

    /**
     * Check whether the purchases have changed before posting changes.
     */
    private boolean isUnchangedPurchaseList(List< Purchase > purchasesList) {
        // TODO: Optimize to avoid updates with identical data.
        return false;
    }

    /**
     * Log the number of purchases that are acknowledge and not acknowledged.
     * <p>
     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     * <p>
     * When the purchase is first received, it will not be acknowledge.
     * This application sends the purchase token to the server for registration. After the
     * purchase token is registered to an account, the Android app acknowledges the purchase token.
     * The next time the purchase list is updated, it will contain acknowledged purchases.
     */
    private void logAcknowledgementStatus(List< Purchase > purchasesList) {
        int ack_yes = 0;
        int ack_no = 0;
        try {
            for (Purchase purchase : purchasesList) {

                if (skusWithSkuDetails.getValue() != null) {
                    SkuDetails skuDetails = skusWithSkuDetails.getValue().get(purchase.getSkus().get(0));
                    Logger.e(TAG_PURCHASE, "type:" + skuDetails.getType());
                }
                if (purchase.isAcknowledged()) {
                    ack_yes++;
                } else {
                    ack_no++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG_PURCHASE, "logAcknowledgementStatus: acknowledged=" + ack_yes +
                " unacknowledged=" + ack_no);
    }

    /**
     * SkuDetails for all known SKUs.
     */
    public MutableLiveData< Map< String, SkuDetails > > skusWithSkuDetails = new MutableLiveData<>();

    /**
     * In order to make purchases, you need the {@link SkuDetails} for the item or subscription.
     * This is an asynchronous call that will receive a result in .
     */
    public void querySkuDetails() {
        Log.d(TAG_PURCHASE, "querySkuDetails");
        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setType(BillingClient.SkuType.SUBS)
                .setSkusList(LIST_OF_SKUS)
                .build();
        Log.i(TAG_PURCHASE, "querySkuDetailsAsync");
        billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List< SkuDetails > skuDetailsList) {
                if (billingResult == null) {
                    Log.wtf(TAG_PURCHASE, "onSkuDetailsResponse: null BillingResult");
                    return;
                }

                int responseCode = billingResult.getResponseCode();
                String debugMessage = billingResult.getDebugMessage();
                switch (responseCode) {
                    case BillingClient.BillingResponseCode.OK:
                        Log.i(TAG_PURCHASE, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        final int expectedSkuDetailsCount = LIST_OF_SKUS.size();
                        if (skuDetailsList == null) {
                            skusWithSkuDetails.postValue(Collections.< String, SkuDetails >emptyMap());
                            Log.e(TAG_PURCHASE, "onSkuDetailsResponse: " +
                                    "Expected " + expectedSkuDetailsCount + ", " +
                                    "Found null SkuDetails. " +
                                    "Check to see if the SKUs you requested are correctly published " +
                                    "in the Google Play Console.");
                        } else {
                            purchaseView.refreshView(skuDetailsList);
                            Map< String, SkuDetails > newSkusDetailList = new HashMap< String, SkuDetails >();
                            for (SkuDetails skuDetails : skuDetailsList) {
                                newSkusDetailList.put(skuDetails.getSku(), skuDetails);
                            }
                            skusWithSkuDetails.postValue(newSkusDetailList);
                            int skuDetailsCount = newSkusDetailList.size();
                            if (skuDetailsCount == expectedSkuDetailsCount) {
                                Log.i(TAG_PURCHASE, "onSkuDetailsResponse: Found " + skuDetailsCount + " SkuDetails");
                                queryPurchases();
                            } else {
                                Log.e(TAG_PURCHASE, "onSkuDetailsResponse: " +
                                        "Expected " + expectedSkuDetailsCount + ", " +
                                        "Found " + skuDetailsCount + " SkuDetails. " +
                                        "Check to see if the SKUs you requested are correctly published " +
                                        "in the Google Play Console.");
                            }
                        }
                        break;
                    case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                    case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                    case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                    case BillingClient.BillingResponseCode.ERROR:
                        Log.e(TAG_PURCHASE, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    case BillingClient.BillingResponseCode.USER_CANCELED:
                        Log.i(TAG_PURCHASE, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                        break;
                    // These response codes are not expected.
                    case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                    case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                    case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
                    default:
                        Log.wtf(TAG_PURCHASE, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                }
            }
        });
    }

    /**
     * Launching the billing flow.
     * <p>
     * Launching the UI to make a purchase requires a reference to the Activity.
     */
    public void launchBillingFlow(Activity activity) {
        if (billingClient != null && !billingClient.isReady()) {
            Log.e(TAG_PURCHASE, "launchBillingFlow: BillingClient is not ready");
            return;
        }

//        boolean isSkuOnServer = BillingUtilities
//                .serverHasSubscription(subscriptions.getValue(), PRODUCT_ID);
        boolean isSkuOnDevice = BillingUtilities
                .deviceHasGooglePlaySubscription(purchases.getValue(), PRODUCT_ID);
        Log.d(TAG_PURCHASE, PRODUCT_ID +
                ", isSkuOnDevice: " + isSkuOnDevice);
        if (isSkuOnDevice /*&& isSkuOnServer*/) {
            Log.e(TAG_PURCHASE, "You cannot buy a SKU that is already owned: " + PRODUCT_ID +
                    "This is an error in the application trying to use Google Play Billing.");
        } /*else if (isSkuOnDevice && !isSkuOnServer) {
            Log.e("Billing", "The Google Play Billing Library APIs indicate that" +
                    "this SKU is already owned, but the purchase token is not registered " +
                    "with the server. There might be an issue registering the purchase token.");
        } else if (!isSkuOnDevice && isSkuOnServer) {
            Log.w("Billing", "WHOA! The server says that the user already owns " +
                    "this item: $sku. This could be from another Google account. " +
                    "You should warn the user that they are trying to buy something " +
                    "from Google Play that they might already have access to from " +
                    "another purchase, possibly from a different Google account " +
                    "on another device.\n" +
                    "You can choose to block this purchase.\n" +
                    "If you are able to cancel the existing subscription on the server, " +
                    "you should allow the user to subscribe with Google Play, and then " +
                    "cancel the subscription after this new subscription is complete. " +
                    "This will allow the user to seamlessly transition their payment " +
                    "method from an existing payment method to this Google Play account.");
        }*/ else {
            SkuDetails skuDetails = null;
            // Create the parameters for the purchase.
            if (skusWithSkuDetails.getValue() != null) {
                skuDetails = skusWithSkuDetails.getValue().get(PRODUCT_ID);
            }

            if (skuDetails == null) {
                Log.e(TAG_PURCHASE, "Could not find SkuDetails to make purchase.");
                return;
            }

            // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();

            BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);
            int responseCode = billingResult.getResponseCode();
            String debugMessage = billingResult.getDebugMessage();
            Log.d(TAG_PURCHASE, "launchBillingFlow: BillingResponse " + responseCode + " " + debugMessage);
        }
    }


//    private BillingProcessor bp;
//    private boolean readyToPurchase = false;

//    private void initializeBilling() {
//        if (!BillingProcessor.isIabServiceAvailable(this)) {
//            showToast(getResources().getString(R.string.initialize_billing));
//        }
//
//        bp = new BillingProcessor(this, LICENSE_KEY, MERCHANT_ID, new BillingProcessor.IBillingHandler() {
//            @Override
//            public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
////				showToast("onProductPurchased: " + productId);
//                Logger.e("onProductPurchased", "" + productId);
//                updateTextViews();
//                purchaseAPICalled(details);
//            }
//
//            @Override
//            public void onBillingError(int errorCode, @Nullable Throwable error) {
//                Logger.e("onBillingError", "" + errorCode);
//                if (errorCode == Constants.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
//                    try {
//                        if (bp != null && bp.isPurchased(PRODUCT_ID)) {
//                            Logger.e("onBillingError", "isPurchased true");
//                            TransactionDetails transactionDetails = bp.getPurchaseTransactionDetails(PRODUCT_ID);
//                            Logger.e("onBillingError", "transactionDetails order id" + transactionDetails.purchaseInfo.purchaseData.orderId + " token:" + transactionDetails.purchaseInfo.purchaseData.purchaseToken);
//                            purchaseAPICalled(transactionDetails);
//                        }
////						if(bp.isRequestBillingHistorySupported(Constants.PRODUCT_TYPE_MANAGED))
////							bp.getPurchaseHistory(Constants.PRODUCT_TYPE_MANAGED,null);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onBillingInitialized() {
////				showToast("onBillingInitialized");
//                Logger.e("onBillingInitialized", "onBillingInitialized");
//                readyToPurchase = true;
//                updateTextViews();
//                onClickToPurchase();
//            }
//
//            @Override
//            public void onPurchaseHistoryRestored() {
////				showToast("onPurchaseHistoryRestored");
//                if (bp != null) {
//                    for (String sku : bp.listOwnedProducts())
//                        Log.d(TAG, "Owned Managed Product: " + sku);
//                    for (String sku : bp.listOwnedSubscriptions())
//                        Log.d(TAG, "Owned Subscription: " + sku);
//                    Logger.e("onPurchaseHistoryRestored", "onPurchaseHistoryRestored");
//                    updateTextViews();
//                }
//            }
//        });
//    }

    private void purchaseAPICalled(Purchase details) {
        if (billingClient != null) {
            EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_PURCHASE, "IN-APP");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RequestManager apiManager = new RequestManager(MainNavigationActivity.this);
                    apiManager.getPurchaseService(CommonFunctions.getHardwareId(MainNavigationActivity.this), settingStore.getUserID(), details.getOrderId(), details.getPurchaseToken(), new NetworkCommunicationManager.CommunicationListnerNew() {
                        @Override
                        public void onSuccess(final IModel response, int operationCode) {

                            if (!isFinishing())
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.rl_progress).setVisibility(View.GONE);
                                        IModelBase styleListRider = (IModelBase) response;
                                        hideLoadingDialog();
                                        if (styleListRider.getStatus().equalsIgnoreCase("1")) {
                                            Logger.e("message", styleListRider.getMsg());
//								Toast.makeText(MainBottomNavigationActivity.this,"Purchase or Restore Successfully",Toast.LENGTH_SHORT).show();
                                            final SettingStore settingStore = SettingStore.getInstance(MainNavigationActivity.this);
                                            settingStore.setIsPro(true);
                                            EventNotifier notifier =
                                                    NotifierFactory.getInstance().getNotifier(
                                                            NotifierFactory.EVENT_NOTIFIER_AD_STATUS);
                                            notifier.eventNotify(EventTypes.EVENT_AD_REMOVED, null);

                                            showPurchaseDlg(getResources().getString(R.string.purchase_title), getResources().getString(R.string.purchase_msg));

                                        } else if (styleListRider.getStatus().equalsIgnoreCase("0")) {
                                            Logger.e("message", styleListRider.getMsg());
                                        }
                                    }
                                });
                        }

                        @Override
                        public void onStartLoading() {
                            findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFail(WebServiceError errorMsg) {
                            if (!isFinishing())
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        findViewById(R.id.rl_progress).setVisibility(View.GONE);
                                        showPurchaseDlg(getResources().getString(R.string.purchase_fail_title), getResources().getString(R.string.purchase_fail_msg));
                                    }
                                });
                        }
                    });
                }
            });

        }
    }

    private void showPurchaseDlg(String title, String msg) {

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
                    .setPositiveButton(getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert = builder.create();
            if (!isFinishing()) {
                alert.show();
            }
        }
    }

    private void updateTextViews() {
//		Logger.e("",""+String.format("%s is%s purchased", PRODUCT_ID, bp.isPurchased(PRODUCT_ID) ? "" : " not"));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void removeAd() {
        try {
            for (int i = 0; i < adapter.mFragmentList.size(); i++) {
                if (adapter.mFragmentList.get(i) instanceof ParentHomeFragment) {
                    ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(i);
                    parentHomeFragment.removeAllAd();
                } else if (adapter.mFragmentList.get(i) instanceof CategoryHomeFragment) {
                    CategoryHomeFragment parentHomeFragment = (CategoryHomeFragment) adapter.mFragmentList.get(i);
                    parentHomeFragment.removeAllAd();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            findViewById(R.id.AdContainer1).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickToPurchase() {
        EventManager.sendEvent(EventManager.ATR_KEY_PURCHASE, "IN-APP", "onClickToPurchase");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                purchaseView.setVisibility(View.VISIBLE);
                eventHelper.sendPaymentCardEvent();
            }
        });
        runOnUiThread(() -> purchaseView.setVisibility(View.VISIBLE));
//        if (!readyToPurchase) {
//            showToast(getResources().getString(R.string.billing_not_initialize));
//            return;
//        }
//
//        if (bp != null)
//            bp.purchase(this, PRODUCT_ID);

    }

    private void removeAdIcon() {
        try {
            Menu menuNav = navigationView.getMenu();
            if (settingStore.getIsPro()) {
                MenuItem nav_pro = menuNav.findItem(R.id.nav_item_pro);
                nav_pro.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayAlert() {
//        initializeBilling();
        initializeBillingNew();
    }


    int selection = 0;

    private void showThemeAlertDialog() {

        int checkedItem = 1;
        boolean isDarkTheme = false;

        if (settingStore.getTheme() == 0) {
            checkedItem = 0;
            isDarkTheme = false;
        } else if (settingStore.getTheme() == 1) {
            checkedItem = 1;
            isDarkTheme = true;
        }

        if (settingStore.getSystemTheme()) {
            checkedItem = 2;
        }

        AlertDialog.Builder alertDialog = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isDarkTheme)
                alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
            else
                alertDialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            alertDialog = new AlertDialog.Builder(this);
        }

//		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainBottomNavigationActivity.this);
        alertDialog.setTitle(getResources().getString(R.string.label_theme_Select));
        String[] items = new String[]{getResources().getString(R.string.theme_light), getResources().getString(R.string.theme_dark)};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            items = new String[]{getResources().getString(R.string.theme_light), getResources().getString(R.string.theme_dark), getResources().getString(R.string.theme_system_default)};
        }

        selection = checkedItem;
        alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        selection = 0;
                        break;
                    case 1:
                        selection = 1;
                        break;
                    case 2:
                        selection = 2;
                        break;
                }
            }
        });

        final int finalCheckedItem = checkedItem;
        alertDialog.setPositiveButton(getResources().getString(R.string.label_apply), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (finalCheckedItem == selection)
                    return;

                if (selection == 0) {
                    settingStore.setSystemTheme(false);
                    settingStore.setTheme(0);
                    updateTheme();
                } else if (selection == 1) {
                    settingStore.setSystemTheme(false);
                    settingStore.setTheme(1);
                    updateTheme();
                } else if (selection == 2) {
                    settingStore.setSystemTheme(true);
                    boolean isNight = CommonFunctions.isNightMode(MainNavigationActivity.this);
                    settingStore.setTheme(isNight ? 1 : 0);
                    updateTheme();
                }
            }
        });
        alertDialog.setNegativeButton(getResources().getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        if (!isFinishing()) {
            alert.show();
        }
    }

    int language_selection = 0;

    private void showLanguageAlertDialog() {

        boolean isDarkTheme = false;

        if (settingStore.getTheme() == 0) {
            isDarkTheme = false;
        } else if (settingStore.getTheme() == 1) {
            isDarkTheme = true;
        }

        AlertDialog.Builder alertDialog = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isDarkTheme)
                alertDialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
            else
                alertDialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            alertDialog = new AlertDialog.Builder(this);
        }

//		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainBottomNavigationActivity.this);
        alertDialog.setTitle(getResources().getString(R.string.sele_lan));
        String[] items = new String[]{getResources().getString(R.string.lang_en), getResources().getString(R.string.lang_hi)};

        alertDialog.setSingleChoiceItems(items, settingStore.getLanguage(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:
                        language_selection = 0;
                        break;
                    case 1:
                        language_selection = 1;
                        break;
                }
            }
        });

        alertDialog.setPositiveButton(getResources().getString(R.string.label_apply), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                if (settingStore.getLanguage() == language_selection) {
                    Toast.makeText(MainNavigationActivity.this, getResources().getString(R.string.label_already_change), Toast.LENGTH_SHORT).show();
                } else {
                    settingStore.setLanguage(language_selection);
                    Log.e("language_selection", "English");

                    Intent i = new Intent(MainNavigationActivity.this, LanguageChangeActivity.class);
                    startActivity(i);
                    MainNavigationActivity.this.overridePendingTransition(0, 0);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 200);
                }
            }
        });
        alertDialog.setNegativeButton(getResources().getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.setCanceledOnTouchOutside(false);
        if (!isFinishing()) {
            alert.show();
        }
    }

    private ReviewManager reviewManager;

    private void inAppReview() {
        SettingStore settingStore = SettingStore.getInstance(this);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yy", Locale.getDefault());
        String formattedDate = df.format(c);
        Logger.e("onResume", "app" + settingStore.getRateDate() + " : " + formattedDate);
        if (TextUtils.isEmpty(settingStore.getRateDate()) || !settingStore.getRateDate().equals(formattedDate)) {
//            if (RatingDialog.needToShowAuto(MainNavigationActivity.this)) {
            if (WallpapersApplication.getApplication().isRatingShow()) {
                reviewManager = ReviewManagerFactory.create(this);
//                    reviewManager = new FakeReviewManager(this);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isFinishing())
                                return;

                            if (WallpapersApplication.getApplication().getSettings().isInAppRating()) {
                                Task< ReviewInfo > request = reviewManager.requestReviewFlow();
                                request.addOnCompleteListener(task -> {
                                    if (isFinishing())
                                        return;
                                    if (task.isSuccessful()) {
                                        try {
                                            // We can get the ReviewInfo object
                                            ReviewInfo reviewInfo = task.getResult();
                                            Task< Void > flow = reviewManager.launchReviewFlow(MainNavigationActivity.this, reviewInfo);
                                            flow.addOnCompleteListener(task1 -> {
                                                // The flow has finished. The API does not indicate whether the user
                                                // reviewed or not, or even whether the review dialog was shown. Thus, no
                                                // matter the result, we continue our app flow.
                                            });
                                        } catch (Exception e) {
                                            WallpapersApplication.getApplication().showRatingDialog(MainNavigationActivity.this, true);
                                        }

                                    } else {
                                        // There was some problem, log or handle the error code.
                                        WallpapersApplication.getApplication().showRatingDialog(MainNavigationActivity.this, true);
                                    }
                                });
                            } else {
                                WallpapersApplication.getApplication().showRatingDialog(MainNavigationActivity.this, true);
                            }
                            settingStore.setRateDate(formattedDate);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
//                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.e(TAG_PURCHASE, "onDestroy:");
        WallpapersApplication.counter = -1;
        if (WallpapersApplication.newList != null) {
            WallpapersApplication.newList.clear();
            WallpapersApplication.newList = null;
        }
        unregisterLoginInfoListener();
        WallpapersApplication.getApplication().getNetworkChecker().destroy();
        WallpapersApplication.getApplication().userClickCount = 0;
        WallpapersApplication.getApplication().currentAdDisplayCount = 0;
        WallpapersApplication.isAppRunning = false;
        WallpapersApplication.getApplication().displayRated = false;
        WallpapersApplication.getApplication().fb_display_detail = false;
        WallpapersApplication.getApplication().isAdmobLoaded = false;
        WallpapersApplication.getApplication().isAdmobLoading = false;
        WallpapersApplication.getApplication().isGGLoaded = false;
        isFavListCalled = false;
        try {
            WallpapersApplication.getApplication().getAppOpenManager().destroyApplication();
        } catch (Exception e) {
            e.printStackTrace();
        }
        WallpapersApplication.getApplication().sendAllDataToServer(false);
//        JesusApplication.getApplication().setViewCountToServer();
//        JesusApplication.getApplication().setLikeCountToServer();
//        JesusApplication.getApplication().setDownloadCountToServer();

        WallpapersApplication.getApplication().destroyAd();
        WallpapersApplication.getApplication().destroyGoogleAd();
        WallpapersApplication.getApplication().destroyGoogleNativeDetail();

        try {
            WallpapersApplication.getApplication().removeUniversalAdListener(this);
//			stopService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            IronSource.removeInterstitialListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
//		try {
//			UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getStock_category().clear();
//			UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getLive_category().clear();
//			UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getCategory().clear();
//			UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getSplash().clear();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

        if (adapter != null) {
            adapter.destroy();
            adapter = null;
        }

        viewPager = null;
        settingStore = null;
        appUpdateManager = null;
        installStateUpdatedListener = null;
        mFragmentManager = null;
        if (marshMallowPermission != null)
            marshMallowPermission.releaseResource();
        marshMallowPermission = null;
        savedInstanceState = null;
        toolbar = null;
        navigationView = null;
        mDrawerLayout = null;
        tabLayout = null;
        powerMenu = null;
        actionView = null;
        menu = null;
        downloadManagerThin = null;
        onMenuItemClickListener = null;
        reviewManager = null;

        if (billingClient != null && billingClient.isReady())
            billingClient.endConnection();
        ;

        billingClient = null;
      /*  Glide.get(WallpapersApplication.sContext).clearMemory();
        new ClearGlideCacheAsyncTask().execute();*/
        CommonFunctions.garbageCollector(true);

    }



    /*Not Usage Method*/


    private void updateAdData(final int duration) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (adapter != null && adapter.mFragmentList != null && tabLayout.getSelectedTabPosition() == 0) {
                                ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(0);
                                parentHomeFragment.adLoaded();
                            }
                            if (adapter != null && adapter.mFragmentList != null && tabLayout.getSelectedTabPosition() == 1) {
                                ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(1);
                                parentHomeFragment.adLoaded();
                            }
                            if (adapter != null && adapter.mFragmentList != null && tabLayout.getSelectedTabPosition() == 2) {
                                ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(2);
                                parentHomeFragment.adLoaded();
                            }
                            if (adapter != null && adapter.mFragmentList != null && tabLayout.getSelectedTabPosition() == 3) {
                                ParentHomeFragment parentHomeFragment = (ParentHomeFragment) adapter.mFragmentList.get(3);
                                parentHomeFragment.adLoaded();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, duration);
            }
        });
    }

    private void updateAdDataCategory(final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (adapter != null && adapter.mFragmentList != null && tabLayout.getSelectedTabPosition() == 4) {
                                CategoryHomeFragment parentHomeFragment = (CategoryHomeFragment) adapter.mFragmentList.get(4);
                                parentHomeFragment.adLoaded();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, duration);
            }
        });
    }


    private class ClearGlideCacheAsyncTask extends AsyncTask< Void, Void, Boolean > {

        private boolean result;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Glide.get(WallpapersApplication.sContext).clearDiskCache();
                result = true;
            } catch (Exception e) {
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }
}
