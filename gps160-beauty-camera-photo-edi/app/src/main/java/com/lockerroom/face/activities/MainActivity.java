package com.lockerroom.face.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.billingclient.api.ProductDetails;

import com.google.android.material.navigation.NavigationView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.lockerroom.face.BuildConfig;
import com.lockerroom.face.Constants;
import com.lockerroom.face.PhotoApp;
import com.lockerroom.face.R;
import com.lockerroom.face.SharedPrefs;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.base.BaseActivity;
import com.lockerroom.face.dialog.RateDialog;
import com.lockerroom.face.dialog.SettingDialog;
import com.lockerroom.face.features.picker.PhotoPicker;
import com.lockerroom.face.features.picker.utils.ImageCaptureManager;
import com.lockerroom.face.features.puzzle.photopicker.activity.PickImageActivity;
import com.lockerroom.face.firebaseAdsConfig.RemoteConfigData;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.interfaces.RewardedAdCallbck;
import com.lockerroom.face.maxAdManager.BannerAdListener;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.maxAdManager.OnAdShowCallback;
import com.lockerroom.face.utils.AdsUtils;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.SharePreferenceUtil;
import com.lockerroom.face.utils.StaticData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.moisoni.google_iab.BillingConnector;
import games.moisoni.google_iab.BillingEventListener;
import games.moisoni.google_iab.enums.ProductType;
import games.moisoni.google_iab.models.BillingResponse;
import games.moisoni.google_iab.models.ProductInfo;
import games.moisoni.google_iab.models.PurchaseInfo;
import timber.log.Timber;


public class MainActivity extends BaseActivity  {
    private String TAG = "MainActivity";
    private Boolean collage = false;

    private ImageCaptureManager captureManager;
    private Boolean isAdAvailable = false;

    private ImageView adIcon;
    boolean doubleBackToExitPressedOnce = false;
    private BillingConnector billingConnector;
    private String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn1/9UuE3xHKiJsnsC1QpB76P+pd+ppGOR3pp0eDW8vqjhKNfqMzYrq9LKQ415Mc5K9Ks1AjSb0Qcajh8FMtuv8Q+4pVPWuRam57xYSY0u5tzrchEkEeg4nO3xy4A5lfGKCq0TRVJOUg9G0ANJ9rhPMfQwHwA02RlWfN3oTX6vYKdkRamD9fcIA82p5jcIc0t9FntPiHyLwuZQ+kT/SeTgxwJp+W0hCUetnfObiMRulMgaE9vUMohLVhHRxNdtNizerU+cCIKBWq7afRdGjfABQOIzOMZKcRdz3yiIy5BWoReStTksrax4R6CL/iaRqaviZINZXZRbwDIxYDCJb/x/wIDAQAB";
    private ArrayList<String> SKUs = new ArrayList<>();

    private static final String LAST_CLICK_TIMESTAMP_KEY = "last_click_timestamp";
    private static final long HOURS_24 = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    private SharedPreferences sharedPreferences;
    FrameLayout app_ad;
    TextView tvLoading;
    RelativeLayout mainConaitner;
    List<String> permissionReqList;
    List<String> permissionList = Arrays.asList("android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE");
    List<String> permissionList13 = Arrays.asList("android.permission.CAMERA", "android.permission.READ_MEDIA_IMAGES");

//    ImageView iv_gif_load;

    View.OnClickListener onClickListener = view -> {
        switch (view.getId()) {
            case R.id.home_btn:
                toggleDrawer();
                return;
            case R.id.remove_adbtn_tlbr:
            case R.id.remove_ads_btn:
                removeAds();
                return;
            case R.id.takePhoto:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionReqList = permissionList13;
                } else {
                    permissionReqList = permissionList;
                }
//                Dexter.withContext(MainActivity.this).withPermissions("android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE").withListener(new MultiplePermissionsListener() {
                Dexter.withContext(MainActivity.this).withPermissions(permissionReqList).withListener(new MultiplePermissionsListener() {
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {

                            if (MyConstant.INSTANCE.getMAIN_MENU_INTERSTITIAL()) {

                                //MaxAdInterShow>>done
                                MaxAdManager.INSTANCE.checkTap(MainActivity.this,()->{
                                    openCamera();
                                    return null;
                                });
                            } else {
                                openCamera();
                            }

                        }
                        if (!multiplePermissionsReport.areAllPermissionsGranted()) {
                            SettingDialog.showSettingDialog(MainActivity.this);
                        }
                    }

                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(dexterError -> Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show()).onSameThread().check();
                return;

            case R.id.btnShare:
                share();
                return;

            case R.id.collage:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionReqList = permissionList13;
                } else {
                    permissionReqList = permissionList;
                }
//                Dexter.withContext(MainActivity.this).withPermissions("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE").withListener(new MultiplePermissionsListener() {
                Dexter.withContext(MainActivity.this).withPermissions(permissionReqList).withListener(new MultiplePermissionsListener() {
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
//                            MyConstant.INSTANCE.setTapCount(MyConstant.INSTANCE.getTapCount() + 1);
                            findViewById(R.id.btn_gallery).setEnabled(false);
                            findViewById(R.id.btnEdit).setEnabled(false);
                            findViewById(R.id.collage).setEnabled(false);

                            if (!SharePreferenceUtil.isPurchased(MainActivity.this)) {
                                if (Constants.IS_SHOW_REWARDED_AD) {

                                    //MaxAdRewardedAd>>done

                                    MaxAdManager.INSTANCE.showRewardedAd(MainActivity.this,()->{return null;},
                                            ()->{
                                                Toast.makeText(MainActivity.this, "please try again later", Toast.LENGTH_SHORT).show();
                                                collage = false;
                                                return null;
                                            },()->{
                                                long currentTimestamp = System.currentTimeMillis();
                                                sharedPreferences.edit().putLong(LAST_CLICK_TIMESTAMP_KEY, currentTimestamp).apply();
                                                checkRewardedAdTime();
                                                adIcon.setVisibility(View.GONE);
                                                findViewById(R.id.adUnlockText).setVisibility(View.GONE);
                                                Intent intent = new Intent(MainActivity.this, PickImageActivity.class);
                                                intent.putExtra(PickImageActivity.KEY_LIMIT_MAX_IMAGE, 9);
                                                intent.putExtra(PickImageActivity.KEY_LIMIT_MIN_IMAGE, 2);
                                                startActivityForResult(intent, 1001);
                                                findViewById(R.id.btn_gallery).setEnabled(true);
                                                findViewById(R.id.btnEdit).setEnabled(true);
                                                findViewById(R.id.collage).setEnabled(true);

                                                collage = true;
                                                return null;
                                            });

//                                    boolean isShow = true;
//                                    if (isShow) {
//
//                                    } else {
//                                        Toast.makeText(MainActivity.this, "please watch Full ad", Toast.LENGTH_SHORT).show();
//                                    }


                                    findViewById(R.id.btn_gallery).setEnabled(true);
                                    findViewById(R.id.btnEdit).setEnabled(true);
                                    findViewById(R.id.collage).setEnabled(true);
                                } else {
                                    Intent intent = new Intent(MainActivity.this, PickImageActivity.class);
                                    intent.putExtra(PickImageActivity.KEY_LIMIT_MAX_IMAGE, 9);
                                    intent.putExtra(PickImageActivity.KEY_LIMIT_MIN_IMAGE, 2);
                                    startActivityForResult(intent, 1001);
                                    findViewById(R.id.btn_gallery).setEnabled(true);
                                    findViewById(R.id.btnEdit).setEnabled(true);
                                    findViewById(R.id.collage).setEnabled(true);
                                }
                            } else {
                                Intent intent = new Intent(MainActivity.this, PickImageActivity.class);
                                intent.putExtra(PickImageActivity.KEY_LIMIT_MAX_IMAGE, 9);
                                intent.putExtra(PickImageActivity.KEY_LIMIT_MIN_IMAGE, 2);
                                startActivityForResult(intent, 1001);
                                findViewById(R.id.btn_gallery).setEnabled(true);
                                findViewById(R.id.btnEdit).setEnabled(true);
                                findViewById(R.id.collage).setEnabled(true);
                            }
//                            if (IronSource.isInterstitialReady() || IronSourceAdsManager.INSTANCE.getMInterstitialAd() != null) {
//                                if (MyConstant.INSTANCE.interCriteria()) {
//                                    IronSourceAdsManager.INSTANCE.showInter(MainActivity.this, new IronSourceCallbacks() {
//                                        @Override
//                                        public void onSuccess() {
//                                            Intent intent = new Intent(MainActivity.this, PickImageActivity.class);
//                                            intent.putExtra(PickImageActivity.KEY_LIMIT_MAX_IMAGE, 9);
//                                            intent.putExtra(PickImageActivity.KEY_LIMIT_MIN_IMAGE, 2);
//                                            startActivityForResult(intent, 1001);
//                                            findViewById(R.id.btn_gallery).setEnabled(true);
//                                            findViewById(R.id.btnEdit).setEnabled(true);
//                                            findViewById(R.id.collage).setEnabled(true);
//                                        }
//
//                                        @Override
//                                        public void onFail() {
//                                            Intent intent = new Intent(MainActivity.this, PickImageActivity.class);
//                                            intent.putExtra(PickImageActivity.KEY_LIMIT_MAX_IMAGE, 9);
//                                            intent.putExtra(PickImageActivity.KEY_LIMIT_MIN_IMAGE, 2);
//                                            startActivityForResult(intent, 1001);
//                                            findViewById(R.id.btn_gallery).setEnabled(true);
//                                            findViewById(R.id.btnEdit).setEnabled(true);
//                                            findViewById(R.id.collage).setEnabled(true);
//                                        }
//                                    });
//                                } else {
//                                    Intent intent = new Intent(MainActivity.this, PickImageActivity.class);
//                                    intent.putExtra(PickImageActivity.KEY_LIMIT_MAX_IMAGE, 9);
//                                    intent.putExtra(PickImageActivity.KEY_LIMIT_MIN_IMAGE, 2);
//                                    startActivityForResult(intent, 1001);
//                                    findViewById(R.id.btn_gallery).setEnabled(true);
//                                    findViewById(R.id.btnEdit).setEnabled(true);
//                                    findViewById(R.id.collage).setEnabled(true);
//                                }
//                            } else {
//                                Intent intent = new Intent(MainActivity.this, PickImageActivity.class);
//                                intent.putExtra(PickImageActivity.KEY_LIMIT_MAX_IMAGE, 9);
//                                intent.putExtra(PickImageActivity.KEY_LIMIT_MIN_IMAGE, 2);
//                                startActivityForResult(intent, 1001);
//                                findViewById(R.id.btn_gallery).setEnabled(true);
//                                findViewById(R.id.btnEdit).setEnabled(true);
//                                findViewById(R.id.collage).setEnabled(true);
//                            }


                        }
                        if (!multiplePermissionsReport.areAllPermissionsGranted()) {
                            SettingDialog.showSettingDialog(MainActivity.this);
                        }
                    }

                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(dexterError -> Toast.makeText(MainActivity.this, "Error occurred! ", Toast.LENGTH_SHORT).show()).onSameThread().check();


                return;

            case R.id.btnEdit:
            case R.id.btn_gallery:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionReqList = permissionList13;
                } else {
                    permissionReqList = permissionList;
                }
//                Dexter.withContext(MainActivity.this).withPermissions("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE").withListener(new MultiplePermissionsListener() {
                Dexter.withContext(MainActivity.this).withPermissions(permissionReqList).withListener(new MultiplePermissionsListener() {
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.areAllPermissionsGranted()) {
                            findViewById(R.id.btn_gallery).setEnabled(false);
                            findViewById(R.id.btnEdit).setEnabled(false);
                            findViewById(R.id.collage).setEnabled(false);

                            if (MyConstant.INSTANCE.getMAIN_MENU_INTERSTITIAL()) {

                                //MaxAdInterShow>>done
                                MaxAdManager.INSTANCE.checkTap(MainActivity.this,()->{
                                    openGallery();
                                    findViewById(R.id.btn_gallery).setEnabled(true);
                                    findViewById(R.id.btnEdit).setEnabled(true);
                                    findViewById(R.id.collage).setEnabled(true);

                                    return null;
                                });

                            } else {
                                openGallery();
                                findViewById(R.id.btn_gallery).setEnabled(true);
                                findViewById(R.id.btnEdit).setEnabled(true);
                                findViewById(R.id.collage).setEnabled(true);
                            }
                        }
                        if (!multiplePermissionsReport.areAllPermissionsGranted()) {
                            SettingDialog.showSettingDialog(MainActivity.this);
                        }
                    }

                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(dexterError -> Toast.makeText(MainActivity.this, "Error occurred! ", Toast.LENGTH_SHORT).show()).onSameThread().check();
                return;


            default:
        }

    };

    private void share() {
        Intent intent2 = new Intent("android.intent.action.SEND");
        intent2.setType("text/plain");
        intent2.putExtra("android.intent.extra.SUBJECT", MainActivity.this.getString(R.string.app_name));
        intent2.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        MainActivity.this.startActivity(Intent.createChooser(intent2, "Choose"));
    }

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    ActionBarDrawerToggle drawerToggle;

    //    public static MaxInterstitialAd interstitialAd;
    private int retryAttempt;
//    void createInterstitialAd()
//    {
//        interstitialAd = new MaxInterstitialAd( "762f04ccdbb983c6", this );
//        interstitialAd.setListener( this );
//
//        // Load the first ad
//        interstitialAd.loadAd();
//    }

    private void removeAds() {

        MaxAdManager.INSTANCE.checkTap(MainActivity.this,()->{
            StaticData.fromMain = true;
            startActivity(new Intent(this, SubscriptionActivity.class));
            return null;
        });


    }

    public void onCreate(Bundle bundle) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        adIcon = findViewById(R.id.adIcon);

        app_ad = (FrameLayout) findViewById(R.id.bannerContainer);
        tvLoading = (TextView) findViewById(R.id.bannerTvLoading);
        mainConaitner = (RelativeLayout) findViewById(R.id.maxBannerAdContainer);
        checkRewardedAdTime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionReqList = permissionList13;
        } else {
            permissionReqList = permissionList;
        }

        if (!SharedPrefs.read(MainActivity.this, SharedPrefs.GOT_IT, false)) {
            showPrivacyDialog();
        }

        Log.d(TAG, "onCreate: " + SharePreferenceUtil.isPurchased(MainActivity.this.getApplicationContext()));

//        createInterstitialAd();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);
        // This will display an Up icon (<-), we will replace it with hamburger later
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerToggle = setupDrawerToggle();

        // Setup toggle to display hamburger icon with nice animation
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);
//        SKUs.add("subs_weekly");
////        SKUs.add("yearly");
//        SKUs.add("subs_yearly");

        SKUs.add("weekly_3");
//        SKUs.add("yearly");
        SKUs.add("subs_yearly");

        initBilling();

        if (SharePreferenceUtil.isPurchased(this)) {
            (findViewById(R.id.removeAdsBanner)).setVisibility(View.GONE);
            (findViewById(R.id.remove_adbtn_tlbr)).setVisibility(View.GONE);
            (findViewById(R.id.admob_native_container)).setVisibility(View.GONE);
        } else {
            (findViewById(R.id.removeAdsBanner)).setVisibility(View.VISIBLE);
            (findViewById(R.id.remove_adbtn_tlbr)).setVisibility(View.VISIBLE);
//            (findViewById(R.id.admob_native_container)).setVisibility(View.VISIBLE);
            (findViewById(R.id.admob_native_container)).setVisibility(View.GONE);

        }
        (findViewById(R.id.btnShare)).setOnClickListener(this.onClickListener);
        findViewById(R.id.btnEdit).setOnClickListener(this.onClickListener);
        (findViewById(R.id.takePhoto)).setOnClickListener(this.onClickListener);
        (findViewById(R.id.collage)).setOnClickListener(this.onClickListener);
        (findViewById(R.id.btn_gallery)).setOnClickListener(this.onClickListener);
        (findViewById(R.id.remove_ads_btn)).setOnClickListener(this.onClickListener);
        (findViewById(R.id.home_btn)).setOnClickListener(this.onClickListener);
        (findViewById(R.id.remove_adbtn_tlbr)).setOnClickListener(this.onClickListener);

        this.captureManager = new ImageCaptureManager(this);
        if (Constants.SHOW_ADS) {
//            AdmobAds.loadNativeAds(this, (View) null);
            IronSourceAdsManager.INSTANCE.loadInter(this, new IronSourceCallbacks() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFail() {

                }
            });

        }
//        throw new  RuntimeException("Test Crash"); // Force a crash

    }


    public void onPostCreate(@Nullable Bundle bundle) {
        super.onPostCreate(bundle);
    }


    public void onActivityResult(int i, int i2, Intent intent) {
        if (i2 != -1) {
            super.onActivityResult(i, i2, intent);
        } else {
            if (this.captureManager == null) {
                this.captureManager = new ImageCaptureManager(this);
            }
            if (i == 1) {

                try {
                    new Handler(Looper.getMainLooper()).post(() -> MainActivity.this.captureManager.galleryAddPic());
//                    Log.e("currentphotopath",">"+this.captureManager.getCurrentPhotoPath());
                    Intent intent2 = new Intent(getApplicationContext(), EditImageActivity.class);
                    intent2.putExtra(PhotoPicker.KEY_SELECTED_PHOTOS, this.captureManager.getCurrentPhotoPath());
                    startActivity(intent2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Uri uri = intent.getData();
                    if (uri != null) {

                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(uri,
                                filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();
                        Intent intent2 = new Intent(getApplicationContext(), EditImageActivity.class);
                        intent2.putExtra(PhotoPicker.KEY_SELECTED_PHOTOS, picturePath);
                        startActivity(intent2);

                    } else {
                        Toast.makeText(MainActivity.this, "try again", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "try again", Toast.LENGTH_LONG).show();
                }


            }
        }
    }


    public void onResume() {
        super.onResume();
        if (AdsUtils.isNetworkAvailabel(getApplicationContext())) {
            if (!SharePreferenceUtil.isRated(getApplicationContext()) && SharePreferenceUtil.getCounter(getApplicationContext()) % 6 == 0) {
            }
            SharePreferenceUtil.increateCounter(getApplicationContext());
        }

        //MaxAdBanner>>done
        if (!SharePreferenceUtil.isPurchased(MainActivity.this)) {
            mainConaitner.setVisibility(View.VISIBLE);
            MaxAdManager.INSTANCE.createBannerAd(this, mainConaitner,app_ad , tvLoading, false, new BannerAdListener() {
                @Override
                public void bannerAdLoaded(boolean isLoad) {
                }
            });
        }else{
            mainConaitner.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void openCamera() {
        try {
            this.captureManager = new ImageCaptureManager(MainActivity.this);
            startActivityForResult(this.captureManager.dispatchTakePictureIntent(), 1);
        } catch (IOException | ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void onDestroy() {
        super.onDestroy();
    }


    public void popupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.feedback) {
                Intent intent = new Intent("android.intent.action.SEND");
                intent.putExtra("android.intent.extra.EMAIL", new String[]{getResources().getString(R.string.email_feedback)});
                intent.putExtra("android.intent.extra.SUBJECT", "Beauty Feedback:");
                intent.putExtra("android.intent.extra.TEXT", "");
                intent.setType("message/rfc822");
                MainActivity.this.startActivity(Intent.createChooser(intent, MainActivity.this.getString(R.string.choose_email) + " :"));
            } else if (itemId == R.id.privacy_policy) {
                try {
                    MainActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.privacy_policy_url))));
                } catch (Exception ignored) {
                }
            } else if (itemId == R.id.rate_us) {

                new RateDialog(MainActivity.this, false).show();

            } else if (itemId == R.id.share_friend) {
                Intent intent2 = new Intent("android.intent.action.SEND");
                intent2.setType("text/plain");
                intent2.putExtra("android.intent.extra.SUBJECT", MainActivity.this.getString(R.string.app_name));
                intent2.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                MainActivity.this.startActivity(Intent.createChooser(intent2, "Choose"));
            }
            return false;
        });
        popupMenu.show();
    }

    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (this.doubleBackToExitPressedOnce) {
                showGoodBye();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.press_back,
                    0).show();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    MainActivity.this.doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    public void showGoodBye() {

                MainActivity.this.startActivity(new Intent(MainActivity.this, ThankYouActivity.class));
                MainActivity.this.finish();

//        if (!AdmobAds.canShowFullAds()) {
//
//            AdmobAds.showFullAds(admobListener);
//
//        } else {
//
//            admobListener.onAdsClose();
//        }
//        if (IronSource.isInterstitialReady())
//        {
//            //show the interstitial
//            IronSourceAdsManager.INSTANCE.showInter(this, new IronSourceCallbacks() {
//                @Override
//                public void onSuccess() {
//                    admobListener.onAdsClose();
//                }
//                @Override
//                public void onFail() {
//                    admobListener.onAdsClose();
//
//                }
//            });
//        }
//        else
//        {
//        }
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });


    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        switch (menuItem.getItemId()) {
            case R.id.rate_us:
                new RateDialog(MainActivity.this, false).show();
                break;
            case R.id.share_friend:
                Intent intent2 = new Intent("android.intent.action.SEND");
                intent2.setType("text/plain");
                intent2.putExtra("android.intent.extra.SUBJECT", MainActivity.this.getString(R.string.app_name));
                intent2.putExtra("android.intent.extra.TEXT", "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                MainActivity.this.startActivity(Intent.createChooser(intent2, "Choose"));
                break;
            case R.id.feedback:
                Intent intent = new Intent("android.intent.action.SEND");
                intent.putExtra("android.intent.extra.EMAIL", new String[]{getResources().getString(R.string.email_feedback)});
                intent.putExtra("android.intent.extra.SUBJECT", "Beauty Feedback: ");
                intent.putExtra("android.intent.extra.TEXT", "");
                intent.setType("message/rfc822");
                MainActivity.this.startActivity(Intent.createChooser(intent, MainActivity.this.getString(R.string.choose_email) + " :"));
                break;
            case R.id.privacy_policy:
                try {
                    MainActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.privacy_policy_url))));
                } catch (Exception ignored) {
                }
                break;
            default:
        }
        mDrawer.closeDrawers();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }


    //MaxAdRewardEarn
//    @Override
//    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
//        if (rewardItem.getType() != null) {
//            Log.i(TAG, "onUserEarnedReward");
//            collage = true;
////            loadAd();
//        } else {
//            Log.i(TAG, "onUserEarnedReward");
//            collage = false;
////            loadAd();
//        }
//
//    }

    public void openGallery() {
        try {
            startActivityForResult(this.captureManager.dispatchGalleryIntent(), 2);
        } catch (IOException | ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showPrivacyDialog() {
        final Dialog bottomSheetDialog = new Dialog(this, R.style.Theme_AppCompat_Dialog_Alert);
        bottomSheetDialog.setContentView(R.layout.permission_dialog);

        Button btn = bottomSheetDialog.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                SharedPrefs.write(MainActivity.this, SharedPrefs.GOT_IT, true);
            }
        });
        bottomSheetDialog.show();
        bottomSheetDialog.setCancelable(false);
    }

    public static String convertMediaUriToPath(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            String string = getPath(context, uri);
            return string;
        } else
            return getFilePathForN(uri, context);
    }

    public static String getPath(Context context, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null)
            return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    private static String getFilePathForN(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            //int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    private void toggleDrawer() {

        MaxAdManager.INSTANCE.checkTap(MainActivity.this,()->{
            if (mDrawer.isDrawerOpen(GravityCompat.START)) {
                mDrawer.closeDrawer(GravityCompat.START);
            } else {
                mDrawer.openDrawer(GravityCompat.START);

            }

            return null;
        });


    }

    private void initBilling() {


        billingConnector = new BillingConnector(this, LICENSE_KEY)
                .setSubscriptionIds(SKUs)
                .autoAcknowledge()
                .autoConsume()
                .enableLogging()
                .connect();

        billingConnector.setBillingEventListener(new BillingEventListener() {

            @Override
            public void onProductsFetched(@NonNull List<ProductInfo> skuDetails, List<ProductDetails> productDetailsList) {
            }

            @Override
            public void onPurchasedProductsFetched(@NonNull ProductType skuType, @NonNull List<PurchaseInfo> purchases) {
                if (purchases.isEmpty()) {
                    SharePreferenceUtil.setPurchased(MainActivity.this.getApplicationContext(), false);
                } else {
                    SharePreferenceUtil.setPurchased(MainActivity.this.getApplicationContext(), true);
//                    startToMainActivity();
                }

            }

            @Override
            public void onProductsPurchased(@NonNull List<PurchaseInfo> purchases) {
                if (purchases.isEmpty()) {
                    SharePreferenceUtil.setPurchased(MainActivity.this.getApplicationContext(), false);
                } else {
                    SharePreferenceUtil.setPurchased(MainActivity.this.getApplicationContext(), true);

                }

            }

            @Override
            public void onPurchaseAcknowledged(@NonNull PurchaseInfo purchase) {
                /*Callback after a purchase is acknowledged*/

                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */
            }

            @Override
            public void onPurchaseConsumed(@NonNull PurchaseInfo purchase) {
                /*Callback after a purchase is consumed*/

                /*
                 * CONSUMABLE products entitlement can be granted either here or in onProductsPurchased
                 * */
            }

            @Override
            public void onBillingError(@NonNull BillingConnector billingConnector, @NonNull BillingResponse response) {
                switch (response.getErrorType()) {
                    case CLIENT_NOT_READY:
                        Timber.e("=-================>Client Not Ready");
                        break;

                    case CLIENT_DISCONNECTED:
                        Timber.e("=-================>Client Not Disconnected");


                        break;
//                    case SKU_NOT_EXIST:
//                        Timber.e("=-================>Sku Not Exists");
//
//                        break;
                    case CONSUME_ERROR:
                        Timber.e("=-================>Consume Eerr");

                        break;
                    case ACKNOWLEDGE_ERROR:
                        Timber.e("=-================>Ack Err");
                        break;
                    case ACKNOWLEDGE_WARNING:
                        Timber.e("=-================>Ack Warning");

                        break;
                    case FETCH_PURCHASED_PRODUCTS_ERROR:
                        Timber.e("=-================>Fetch_Purchase Product Err");
                        break;
                    case BILLING_ERROR:
                        Timber.e("=-================>Billing Err");

                        break;
                    case USER_CANCELED:
                        Timber.e("=-================>User Cancelled");

                        break;
                    case SERVICE_UNAVAILABLE:
                        Timber.e("=-================>SService Unavailable");

                        break;
                    case BILLING_UNAVAILABLE:
                        Timber.e("=-================>Billing Not Available");

                        break;
                    case ITEM_UNAVAILABLE:
                        Timber.e("=-================>Item Not Available");

                        break;
                    case DEVELOPER_ERROR:
                        Timber.e("=-================>Developer Err");

                        break;
                    case ERROR:
                        Timber.e("=-================> Error!");

                        break;
                    case ITEM_ALREADY_OWNED:
                        Timber.e("=-================>Item Already Owned");

                        break;
                    case ITEM_NOT_OWNED:
                        Timber.e("=-================>Item Not Ownd");

                        break;
                }
            }
        });

    }


    private void checkRewardedAdTime() {
        if (!SharePreferenceUtil.isPurchased(MainActivity.this)) {
            long lastClickTimestamp = sharedPreferences.getLong(LAST_CLICK_TIMESTAMP_KEY, 0);

            long currentTimestamp = System.currentTimeMillis();

            long elapsedMillis = currentTimestamp - lastClickTimestamp;

            if (elapsedMillis >= HOURS_24) {
                // More than 24 hours have passed, show ad
                adIcon.setVisibility(View.VISIBLE);
                findViewById(R.id.adUnlockText).setVisibility(View.VISIBLE);
                Constants.IS_SHOW_REWARDED_AD = true;
            } else {
                // Less than 24 hours have passed, now show ad
                adIcon.setVisibility(View.GONE);
                findViewById(R.id.adUnlockText).setVisibility(View.GONE);
                Constants.IS_SHOW_REWARDED_AD = false;
            }
        } else {
            adIcon.setVisibility(View.GONE);
            findViewById(R.id.adUnlockText).setVisibility(View.GONE);
            Constants.IS_SHOW_REWARDED_AD = false;
        }
    }


//    File cameraTempFile;

//    public void openCameraPicker() {
//        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//        try {
//            this.cameraTempFile = new File(getFilesDir(), "temp.jpg");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(MainActivity.this, "Unable to perform this action please contact developers to resolve this issue", Toast.LENGTH_SHORT).show();
//        }
//        Uri data = FileProvider.getUriForFile(this, getPackageName() + ".provider", cameraTempFile);
//        intent.putExtra("output", data);
//        Dexter.withContext(this)
//                .withPermission(Manifest.permission.CAMERA)
//                .withListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted(PermissionGrantedResponse response) {
//
//                        cameraLauncher.launch(intent);
//
//                    }
//
//                    @Override
//                    public void onPermissionDenied(PermissionDeniedResponse response) {
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
//                    }
//                }).check();
//    }
//
//    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            result -> {
//                if (result.getResultCode() == Activity.RESULT_OK) {
//                    Uri uri  = result.getData().getData();
//                        // Handle the selected image URI here
//                        try {
//                            if (uri != null) {
//
//                                String[] filePathColumn = {MediaStore.Images.Media.DATA};
//                                Cursor cursor = getContentResolver().query(uri,
//                                        filePathColumn, null, null, null);
//                                cursor.moveToFirst();
//                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                                String picturePath = cursor.getString(columnIndex);
//                                cursor.close();
//                                Intent intent2 = new Intent(getApplicationContext(), EditImageActivity.class);
//                                intent2.putExtra(PhotoPicker.KEY_SELECTED_PHOTOS, picturePath);
//                                startActivity(intent2);
//
//                            } else {
//                                Toast.makeText(MainActivity.this, "try again uri is null", Toast.LENGTH_LONG).show();
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            Toast.makeText(MainActivity.this, "try again exception occure", Toast.LENGTH_LONG).show();
//                        }
//                }
//            }
//    );
}
