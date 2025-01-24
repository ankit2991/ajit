package com.lockerroom.face.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.lockerroom.face.Constants;
import com.lockerroom.face.PhotoApp;
import com.lockerroom.face.R;
//import com.rebel.photoeditor.ads.FacebookAds;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.base.BaseActivity;
import com.lockerroom.face.dialog.RateDialog;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.BannerAdListener;
import com.lockerroom.face.maxAdManager.MaxAdListener;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.SharePreferenceUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;

public class SaveAndShareActivity extends BaseActivity implements View.OnClickListener {
    static final boolean $assertionsDisabled = false;
    private File imgFile;

    private void loadAd() {
    }
    FrameLayout app_ad;
    TextView tvLoading;
    RelativeLayout mainConaitner;

//    FrameLayout app_native_ad;
//    RelativeLayout mainNativeAdContainer;
//    TextView tvNativeAdLoading;

    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.red(PhotoApp.resources.getColor(R.color.__picker_common_primary)));
        setContentView((int) R.layout.save_and_share_layout);
        String string = getIntent().getExtras().getString("path");
        app_ad = (FrameLayout) findViewById(R.id.bannerContainer);
        tvLoading = (TextView) findViewById(R.id.bannerTvLoading);
        mainConaitner = (RelativeLayout) findViewById(R.id.maxBannerAdContainer);




        if (Constants.SHOW_ADS) {
            IronSourceAdsManager.INSTANCE.loadInter(this, new IronSourceCallbacks() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFail() {

                }
            });
        }
        this.imgFile = new File(string);
        if (this.imgFile == null) {
            Log.d("this.imgFile", "this.imgFile");
        } else {
            Picasso.get().load(this.imgFile).into((ImageView) findViewById(R.id.preview));

        }


//
//        Glide.with(getApplicationContext()).load(this.imgFile).into((ImageView) findViewById(R.id.preview));
        findViewById(R.id.preview).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(SaveAndShareActivity.this,()->{
                    SaveAndShareActivity.this.onClick(view);

                    return null;
                });

            }
        });
        findViewById(R.id.btnBackShare).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(SaveAndShareActivity.this,()->{
                    SaveAndShareActivity.this.onClick(view);

                    return null;
                });

            }
        });
        findViewById(R.id.btn_new).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(SaveAndShareActivity.this,()->{
                    SaveAndShareActivity.this.onClick(view);

                    return null;
                });

            }
        });
        findViewById(R.id.btnWallpaper).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SaveAndShareActivity.this.onClick(view);
            }
        });
        findViewById(R.id.btnShareMore).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SaveAndShareActivity.this.onClick(view);
            }
        });
        findViewById(R.id.btnInstagram).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SaveAndShareActivity.this.onClick(view);
            }
        });
        findViewById(R.id.btnFacebook).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SaveAndShareActivity.this.onClick(view);
            }
        });
        findViewById(R.id.btnWhatsApp).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SaveAndShareActivity.this.onClick(view);
            }
        });
        findViewById(R.id.btnTwitter).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SaveAndShareActivity.this.onClick(view);
            }
        });
        findViewById(R.id.btnGmail).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SaveAndShareActivity.this.onClick(view);
            }
        });
        findViewById(R.id.btnMessenger).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SaveAndShareActivity.this.onClick(view);
            }
        });
        ((TextView) findViewById(R.id.path)).setText(string);
        if (!SharePreferenceUtil.isPurchased(getApplicationContext())) {
            loadAd();
            findViewById(R.id.admobFrameLayout).setVisibility(View.VISIBLE);
        } else {

            findViewById(R.id.admobFrameLayout).setVisibility(View.GONE);
        }
        findViewById(R.id.shareLayout).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                SaveAndShareActivity.lambda$onCreate$0(SaveAndShareActivity.this, view);
            }
        });
        if (!SharePreferenceUtil.isRated(this)) {
            new RateDialog(this, false).show();
        }

//        findViewById(R.id.adsContainer).setVisibility(View.GONE);

    }

    public static void lambda$onCreate$0(SaveAndShareActivity saveAndShareActivity, View view) {

        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("image/*");

        intent.putExtra("android.intent.extra.STREAM", FileProvider.getUriForFile(saveAndShareActivity.getApplicationContext(), "com.lockerroom.face.changer.mustache.beard.handsome.free.provider", saveAndShareActivity.imgFile));
        intent.addFlags(3);
        saveAndShareActivity.startActivity(Intent.createChooser(intent, "Share"));
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        finish();
        return true;
    }


    public void onResume() {
        super.onResume();

        //MaxAdBanner>>done
        if (!SharePreferenceUtil.isPurchased(SaveAndShareActivity.this)) {
            mainConaitner.setVisibility(View.VISIBLE);
            MaxAdManager.INSTANCE.createBannerAd(this, mainConaitner,app_ad , tvLoading, false, new BannerAdListener() {
                @Override
                public void bannerAdLoaded(boolean isLoad) {
                }
            });
        }else{
            mainConaitner.setVisibility(View.GONE);
        }

        if (Constants.SHOW_ADS) {

            loadNativeAd();
        }


    }


    public void loadNativeAd(){

        //MaxAdNativeAd>>done
       final FrameLayout app_native_ad =  findViewById(R.id.nativeContainer);
      final RelativeLayout  mainNativeAdContainer = findViewById(R.id.maxNativeAdContainer);
      final TextView  tvNativeAdLoading =  findViewById(R.id.tvNativeAdLoading);
        if (!SharePreferenceUtil.isPurchased(SaveAndShareActivity.this)){

                MaxAdManager.INSTANCE.createNativeAd(this, mainNativeAdContainer, app_native_ad, tvNativeAdLoading, new MaxAdListener() {
                    @Override
                    public void onAdLoaded(boolean adLoad) {
                    }

                    @Override
                    public void onAdShowed(boolean adShow) {
                    }

                    @Override
                    public void onAdHidden(boolean adHidden) {
                    }

                    @Override
                    public void onAdLoadFailed(boolean adLoadFailed) {
                    }

                    @Override
                    public void onAdDisplayFailed(boolean adDisplayFailed) {
                    }
                });

        }else{
            mainNativeAdContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onClick(View view) {
        if (view != null) {
            int id = view.getId();
            if (id == R.id.btnBackShare) {
                MaxAdManager.INSTANCE.checkTap(SaveAndShareActivity.this,()->{
                    SaveAndShareActivity.this.onBackPressed();

                    return null;
                });

            } else if (id != R.id.preview) {
                switch (id) {
                    case R.id.btnFacebook:
                        sharePhoto(Constants.FACE);
                        return;
                    case R.id.btnGmail:
                        sharePhoto(Constants.GMAIL);
                        return;
                    case R.id.btnInstagram:
                        sharePhoto(Constants.INSTA);
                        return;
                    case R.id.btnMessenger:
                        sharePhoto(Constants.MESSEGER);
                        return;
                    case R.id.btnShareMore:
                        Uri createCacheFile = createCacheFile();
                        if (createCacheFile != null) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.SEND");
                            intent.addFlags(1);
                            intent.setDataAndType(createCacheFile, getContentResolver().getType(createCacheFile));
                            intent.putExtra("android.intent.extra.STREAM", createCacheFile);
                            startActivity(Intent.createChooser(intent, "Choose an app"));
                            return;
                        }
                        Toast.makeText(SaveAndShareActivity.this, "Fail to sharing", 0).show();

                        return;
                    default:
                        switch (id) {
                            case R.id.btnTwitter:
                                sharePhoto(Constants.TWITTER);
                                return;
                            case R.id.btnWallpaper:
                                Uri createCacheFile2 = createCacheFile();
                                if (createCacheFile2 != null) {
                                    Intent intent2 = new Intent("android.intent.action.ATTACH_DATA");
                                    intent2.setDataAndType(createCacheFile2, getContentResolver().getType(createCacheFile2));
                                    intent2.setFlags(1);
                                    startActivity(Intent.createChooser(intent2, "Use as"));
                                    return;
                                }
                                Toast.makeText(SaveAndShareActivity.this, "Fail", 0).show();

                                return;
                            case R.id.btnWhatsApp:
                                sharePhoto(Constants.WHATSAPP);
                                return;
                            case R.id.btn_new:
//                                if (IronSource.isInterstitialReady()) {
                                SaveAndShareActivity.this.startActivity(new Intent(SaveAndShareActivity.this, MainActivity.class));
                                SaveAndShareActivity.this.finish();
//                                } else {
//                                    SaveAndShareActivity.this.startActivity(new Intent(SaveAndShareActivity.this, MainActivity.class));
//                                    SaveAndShareActivity.this.finish();
//                                }

                                return;
                            default:
                                return;
                        }
                }
            } else {
                Intent intent4 = new Intent();
                intent4.setAction("android.intent.action.VIEW");
                intent4.setDataAndType(FileProvider.getUriForFile(getApplicationContext(), "com.lockerroom.face.changer.mustache.beard.handsome.free.provider", SaveAndShareActivity.this.imgFile), "image/*");
                intent4.addFlags(3);
                startActivity(intent4);

            }
        }
    }

    public void sharePhoto(String str) {
        if (isPackageInstalled(SaveAndShareActivity.this, str)) {
            Uri createCacheFile = createCacheFile();
            if (createCacheFile != null) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.SEND");
                intent.addFlags(1);
                intent.setDataAndType(createCacheFile, getContentResolver().getType(createCacheFile));
                intent.putExtra("android.intent.extra.STREAM", createCacheFile);
                intent.setPackage(str);
                startActivity(intent);
                return;
            }
            Toast.makeText(SaveAndShareActivity.this, "Fail to sharing", 0).show();
            return;
        }
        Toast.makeText(SaveAndShareActivity.this, "Can't find this App, please download and try it again", 0).show();
        Intent intent2 = new Intent("android.intent.action.VIEW");
        intent2.setData(Uri.parse("market://details?id=" + str));
        startActivity(intent2);

    }

    public static boolean isPackageInstalled(Context context, String str) {
        try {
            context.getPackageManager().getPackageInfo(str, 128);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    private Uri createCacheFile() {
        return FileProvider.getUriForFile(getApplicationContext(),
                "com.lockerroom.face.changer.mustache.beard.handsome.free.provider", this.imgFile);
    }
}
