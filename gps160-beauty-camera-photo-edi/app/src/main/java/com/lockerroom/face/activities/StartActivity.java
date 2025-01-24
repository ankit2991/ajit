package com.lockerroom.face.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lockerroom.face.Constants;
import com.lockerroom.face.PhotoApp;
import com.lockerroom.face.R;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.MaxAdListener;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.utils.SharePreferenceUtil;

public class StartActivity extends AppCompatActivity {
    RelativeLayout abc;
    FrameLayout app_native_ad;
    RelativeLayout mainNativeAdContainer;
    TextView tvNativeAdLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);
//        PhotoApp.appOpenManager.showAdIfAvailable();


        app_native_ad = (FrameLayout) findViewById(R.id.nativeContainer);
        mainNativeAdContainer = (RelativeLayout) findViewById(R.id.maxNativeAdContainer);
        tvNativeAdLoading = (TextView) findViewById(R.id.tvNativeAdLoading);
        IronSourceAdsManager.INSTANCE.loadInter(this, new IronSourceCallbacks() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail() {

            }
        });

        abc = findViewById(R.id.abc);
//        Boom(abc);
        abc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartActivity.this.startToMainActivity();//

            }
        });


    }
    public void startToMainActivity() {
        startActivity(new Intent(StartActivity.this, MainActivity.class));
        finish();
    }

    public void Boom(View iv) {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(iv, "scaleX", 0.8f);
        scaleXAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleXAnimator.setDuration(700);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(iv, "scaleY", 0.8f);
        scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleYAnimator.setDuration(700);
        scaleXAnimator.start();
        scaleYAnimator.start();
    }


    @Override
    protected void onResume() {
        super.onResume();

       loadNativeAd();


    }


    public void loadNativeAd(){

        //MaxAdNativeAd>>done
        if (!SharePreferenceUtil.isPurchased(StartActivity.this)){
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