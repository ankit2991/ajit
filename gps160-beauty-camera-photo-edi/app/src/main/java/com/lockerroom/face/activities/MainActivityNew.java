package com.lockerroom.face.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.lockerroom.face.R;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.base.BaseActivity;
import com.lockerroom.face.dialog.RateDialog;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.MaxAdListener;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.utils.SharePreferenceUtil;
//import com.weewoo.sdkproject.SDKProject;


public class MainActivityNew extends BaseActivity  {

    FrameLayout app_native_ad;
    RelativeLayout mainNativeAdContainer;
    TextView tvNativeAdLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        getWindow().setStatusBarColor(Color.red(PhotoApp.resources.getColor(R.color.__picker_common_primary)));
        setContentView(R.layout.activity_main_new);

        app_native_ad = (FrameLayout) findViewById(R.id.nativeContainer);
        mainNativeAdContainer = (RelativeLayout) findViewById(R.id.maxNativeAdContainer);
        tvNativeAdLoading = (TextView) findViewById(R.id.tvNativeAdLoading);

        loadAds();
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivityNew.this.onClick(view);
            }
        });

        findViewById(R.id.rate).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MainActivityNew.this.onClick(view);
            }
        });
    }

    public void onClick(View view) {
        if (view != null) {
            int id = view.getId();
            switch (id) {

                case R.id.start:
                    startToMainActivity();
                    return;
                case R.id.rate:
                    new RateDialog(this, false).show();
                default:

            }

        }
    }

    public void startToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
    private void loadAds(){


//        PhotoApp.appOpenManager.showAdIfAvailable();
        IronSourceAdsManager.INSTANCE.loadInter(this, new IronSourceCallbacks() {
            @Override
            public void onSuccess() {

            }
            @Override
            public void onFail() {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();


        loadNativeAd();
    }

    public void loadNativeAd(){

        //MaxAdNativeAd>>done
        if (!SharePreferenceUtil.isPurchased(MainActivityNew.this)){
        MaxAdManager.INSTANCE.createNativeAd(this, mainNativeAdContainer, app_native_ad, tvNativeAdLoading, new MaxAdListener() {
            @Override
            public void onAdLoaded(boolean adLoad) {}
            @Override
            public void onAdShowed(boolean adShow) {}
            @Override
            public void onAdHidden(boolean adHidden) {}
            @Override
            public void onAdLoadFailed(boolean adLoadFailed) {}
            @Override
            public void onAdDisplayFailed(boolean adDisplayFailed) {}
        });
    }else{
        mainNativeAdContainer.setVisibility(View.GONE);
    }
    }
}