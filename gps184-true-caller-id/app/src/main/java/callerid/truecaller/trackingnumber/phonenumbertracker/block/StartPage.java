package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import static callerid.truecaller.trackingnumber.phonenumbertracker.block.Glob.isOnline;
import static callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.SQLiteAdapter.MYDATABASE_NAME;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.adjust.sdk.Adjust;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.calldorado.Calldorado;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdConstants;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdInterstitialListener;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.App_Update;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.iap.IapConnector;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.iap.SubscriptionServiceListener;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.util.OverlayPermissionManager;


public class StartPage extends BaseActivity {
    private static final String TAG = "StartPage";
    public static final int RequestPermissionCode = 1;
    private static final String PREFS_NAME = "prefname";
    private static final String DATABASE_NAME = "myDB.db";
    private static CountDownTimer timerForAds;
    private final int i = 0;
    public NetworkChangeReceiver brd;
    Dialog dialogf;
    FrameLayout app_ad;
    FrameLayout adview1;
    boolean iffirst = false;
    private LinearLayoutManager layoutManager;
    private Adapter_moreStart adapter_moreStart;
    private String dbpath;
    private String DB_PATH;
    private Dialog video_dilog1;
    private View btnok;
    private SharedPreferences settings;
    private IapConnector iapConnector;
    FirebaseRemoteConfig mFirebaseRemoteConfig;

    private OverlayPermissionManager overlayPermissionManage;
    private final PermissionHandler permisiionhanderl = new PermissionHandler() {
        @Override
        public void onGranted() {
            xiaomipermmision();
            Log.d(TAG, "onGranted: Called ");

            if (!overlayPermissionManage.isGranted())
                overlayPermissionManage.requestOverlay();
        }

        @Override
        public boolean onBlocked(Context context, ArrayList<String> blockedList) {
            return super.onBlocked(context, blockedList);
        }

        @Override
        public void onDenied(Context context, ArrayList<String> deniedPermissions) {
            super.onDenied(context, deniedPermissions);
            prmission();
        }
    };
    private SFun sfun;
    private boolean check = false;
    private RelativeLayout blockAds;

    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    public static com.google.android.gms.ads.AdSize getAdSize(Activity ctx) {
        Display display = ctx.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        Utils.wi = outMetrics.widthPixels;
        Utils.hg = outMetrics.density;

        float widthPixels = Utils.wi;
        float density = Utils.hg;

        int adWidth = (int) (widthPixels / density);

        return com.google.android.gms.ads.AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth);
    }

    private void setupInApps() {

        //upgrade to billing v5
        iapConnector = IapConnector.Companion.getInstance(this);
        iapConnector.addSubscriptionListener(new SubscriptionServiceListener() {
            @Override
            public void onSubscriptionRestored(@NonNull Purchase purchaseInfo) {
                Log.d(TAG, "restore");
                TinyDB.getInstance(getApplicationContext()).setWeeklyPurchased(true);
                if (TinyDB.getInstance(StartPage.this).weeklyPurchased()) {
                    blockAds.setVisibility(View.GONE);
                } else {
                    blockAds.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSubscriptionPurchased(@NonNull Purchase purchaseInfo) {
                Log.d(TAG, "purchase");
                TinyDB.getInstance(getApplicationContext()).setWeeklyPurchased(true);
                if (TinyDB.getInstance(StartPage.this).weeklyPurchased()) {
                    blockAds.setVisibility(View.GONE);
                } else {
                    blockAds.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSubscriptionsExpired() {
                Log.d(TAG, "expired");
                TinyDB.getInstance(getApplicationContext()).setWeeklyPurchased(false);
                if (TinyDB.getInstance(StartPage.this).weeklyPurchased()) {
                    blockAds.setVisibility(View.GONE);
                } else {
                    blockAds.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPricesUpdated(@NonNull Map<String, ProductDetails> iapKeyPrices) {

            }
        });
    }

    public void onlinechecking(final Activity act) {


        dialogf = new Dialog(StartPage.this);
        View dialogView = LayoutInflater.from(StartPage.this).inflate(R.layout.dialog_internet, null);
        dialogf.setContentView(dialogView);
        dialogf.setCancelable(false);

        if (!isOnline(act)) {

            brodCarst(act);

        }
        ImageView img_views = dialogf.findViewById(R.id.img_views);
        img_views.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnline(act)) {
                    Toast.makeText(act, "Please Connect Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    dialogf.dismiss();

                }
            }
        });
        if (!isOnline(act)) {
            dialogf.show();
        }
        dialogf.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public void brodCarst(final Context ctx) {
        try {
            brd = new NetworkChangeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            ctx.registerReceiver(brd, filter);
        } catch (Exception e) {

        }
    }

    public void LoadDataFromUri(final Activity ctx) {
        if (Utils.Big_native != null) {
            if (Utils.Big_native.size() == 0) {
//                LoadDataForAd(ctx);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page);
        Calldorado.start(this);
        // IntegrationHelper.validateIntegration(this);
//        App.eventHelper.sendOnHomeEvent();
        overlayPermissionManage = new OverlayPermissionManager(this);
        Utils.IS_SPLASH_SCREEN = true;
        TinyDB.getInstance(this).putInt("IsFirstSession", Utils.NATIVE_SESSION);

        FirebaseAnalytics.getInstance(this);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebasemessaging", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
//                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.e("Firebase_cloud_token", token);
//                        Toast.makeText(StartPage.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
        //firebase get FirebaseRemoteConfig
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        fetchActiveRemote();

        prmission();
        sfun = new SFun(this);
        app_ad = (FrameLayout) findViewById(R.id.app_ad);
        adview1 = findViewById(R.id.ads_banner);
        iffirst = true;
        if (isOnline(StartPage.this)) {

            MaxAdManager.INSTANCE.createBannerAd(StartPage.this, adview1);

            LoadDataFromUri(StartPage.this);
        } else {
            onlinechecking(StartPage.this);
        }
        App_Update.UpdateApp(StartPage.this, R.drawable.logo_128);
        copyDataBase1();
        copyDataBase();
        copydataMobilebase();


        Glob.i++;


        blockAds = findViewById(R.id.blockAds);

        if (TinyDB.getInstance(this).weeklyPurchased()) {
            blockAds.setVisibility(View.GONE);
        } else {
            blockAds.setVisibility(View.VISIBLE);
        }

        blockAds.setOnClickListener(v -> startActivity(new Intent(this, InAppsActivity.class)));

        findViewById(R.id.moreapps).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:Multi Tech Apps")));
            } catch (ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/developer?id=6159115823114783469")));
            }
//                startActivity(new Intent(StartPage.this, PlayStoreActivity.class));
        });


        findViewById(R.id.relative_layout).requestFocus();

        // Init Interstitial ad
        MaxAdManager.INSTANCE.createInterstitialAd(
                this,
                MaxAdConstants.MAX_AD_INTERSTITIAL_ID,
                null
        );
        findViewById(R.id.start).setOnClickListener(v -> {
            //startActivity(new Intent(StartPage.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            Intent intent = new Intent(StartPage.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (TinyDB.getInstance(this).weeklyPurchased()) {
                startActivity(intent);
                return;
            }

            Utils.ad_count++;
            if (Utils.defaultValue <= Utils.ad_count) {
                Utils.ad_count = 0;
                Dialog dialog = new Dialog(StartPage.this);
                View dialogView = LayoutInflater.from(StartPage.this).inflate(R.layout.dialog_ad, null);

                dialog.setContentView(dialogView);
                dialog.setCancelable(false);
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                showInterAndMoveToNext();

                MaxAdManager.INSTANCE.createInterstitialAd(
                        this,
                        MaxAdConstants.MAX_AD_INTERSTITIAL_ID,
                        new MaxAdInterstitialListener() {
                            @Override
                            public void onAdLoaded(boolean adLoad) {
                                dialog.dismiss();
                                if (!adLoad) startActivity(intent);
                            }

                            @Override
                            public void onAdShowed(boolean adShow) {
                                int Get_Current_Time_in_Second = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                                sfun.setTimeVar("mytime", Get_Current_Time_in_Second);
                                startActivity(intent);
                            }
                        }

                );
            } else {
                startActivity(intent);
            }
        });

        findViewById(R.id.rate).setOnClickListener(view -> rate());

        setupInApps();
    }

    private void xiaomipermmision() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            if (settings.getBoolean("my_first_time", true)) {
                video_dilog1 = new Dialog(StartPage.this, R.style.AppThemeexit);
                video_dilog1.setContentView(R.layout.dialog1);
                btnok = video_dilog1.findViewById(R.id.btnok);
                btnok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", getPackageName(), null)));
                        settings.edit().putBoolean("my_first_time", false).commit();
                        video_dilog1.dismiss();
                    }
                });
                video_dilog1.show();
            }
        }

    }

    private void rate() {
        if (isOnline(this)) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Conection Available", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean ReadContactsPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (ReadContactsPermission) {
                    } else {
                        Toast.makeText(StartPage.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {


        try {
            unregisterReceiver(brd);
        } catch (Exception e) {

        }

        startActivity(new Intent(StartPage.this, ExitActivty.class));


    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.KEY_SUBSCRIPTION_REMOTE_PLAN = Utils.getRemoteConfig().getString(Utils.REMOTE_KEY_PRICE_PLAN);
        iapConnector.restore();
        if (iffirst) {
            if (Utils.NATIVE_SESSION >= 3) {
                TinyDB.getInstance(this).putInt("IsFirstSession", 3);
                MaxAdManager.INSTANCE.createNativeAd(
                        StartPage.this,
                        app_ad,
                        "GPS119_Native_Main_Menu_flag"
                );
            }
        }
        Adjust.onResume();

    }

    private void copydataMobilebase() {
        try {
            InputStream myinput = this.getAssets().open("monofinder");
            dbpath = "/data/data/" + getPackageName() + "/databases/" + MYDATABASE_NAME;
            OutputStream myoutput = new FileOutputStream(dbpath);
            byte[] buffer = new byte[1024];
            int i = 0;
            while (true) {
                int length = myinput.read(buffer);
                if (length <= 0) {
                    myoutput.flush();
                    myoutput.close();
                    myinput.close();
                    return;
                }
                myoutput.write(buffer, 0, length);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyDataBase() {
        try {
            DB_PATH = "/data/data/" + getApplicationContext().getPackageName() + "/databases/";
            InputStream myInput1 = StartPage.this.getAssets().open(DATABASE_NAME);
            OutputStream myOutput1 = new FileOutputStream(DB_PATH + DATABASE_NAME);
            byte[] buffer = new byte[1024];
            while (true) {
                int length = myInput1.read(buffer);
                if (length <= 0) {
                    myOutput1.flush();
                    myOutput1.close();
                    myInput1.close();
                    return;
                }
                myOutput1.write(buffer, 0, length);
            }
        } catch (IOException e) {
        }
    }

    private void copyDataBase1() {
        byte[] buffer = new byte[1024];
        OutputStream myOutput = null;
        int length;
        InputStream myInput = null;
        try {
            myInput = getAssets().open("bank_checker.db");
            myOutput = new FileOutputStream(DB_PATH + "bank_checker");
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.close();
            myOutput.flush();
            myInput.close();
        } catch (IOException e) {
        }
    }


    private void prmission() {
        String[] permissions;

        if (Build.VERSION.SDK_INT <= 28) {
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        } else if (Build.VERSION.SDK_INT <= 32) {
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        } else if (Build.VERSION.SDK_INT == 33) {
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            };
        }
        Permissions.check(this/*context*/, permissions, null/*rationale*/, null/*options*/, permisiionhanderl);
    }

    @Override
    protected void onDestroy() {
        MaxAdManager.INSTANCE.destroyInterstitialAd();
        MaxAdManager.INSTANCE.destroyBannerAd();
        super.onDestroy();
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
        boolean c = true;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (c) {
                c = false;
                check = true;
            }

            final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            final NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifi.isAvailable() || mobile.isAvailable()) {
                if (isOnline(context)) {

                    check = false;
                    dialogf.dismiss();

                    MaxAdManager.INSTANCE.createBannerAd(StartPage.this, adview1);

                    LoadDataFromUri(StartPage.this);

                    if (Utils.NATIVE_SESSION >= 3) {
                        TinyDB.getInstance(StartPage.this).putInt("IsFirstSession", 3);
                        MaxAdManager.INSTANCE.createNativeAd(
                                StartPage.this,
                                app_ad,
                                "GPS119_Native_Main_Menu_flag"
                        );
                    }
                }
            } else {
            }
        }
    }

    public void fetchActiveRemote() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d("TAG", "Config params updated: " + updated);
                            //   Toast.makeText(SplashActivity.this, "Fetch and activate succeeded",
                            //      Toast.LENGTH_SHORT).show();

                        } else {
                            Log.d("TAG", "firebase failed Config failed to load ");
                            //   Toast.makeText(SplashActivity.this, "Fetch failed",
                            //     Toast.LENGTH_SHORT).show();
                        }
                        //    displayWelcomeMessage();
                    }
                });
    }


}
