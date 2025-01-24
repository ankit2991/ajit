package com.lockerroom.face.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.lockerroom.face.PhotoApp;
import com.lockerroom.face.R;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.MaxAdListener;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.utils.Global;
import com.lockerroom.face.utils.SharePreferenceUtil;


public class StartEditActivity extends AppCompatActivity {
    ImageView img, abc   ,secondImageBg;
    RelativeLayout title_animation;
    CardView agree_btn;
    TextView privacy_text;
    ConstraintLayout adParent;
    private FirebaseAnalytics mFirebaseAnalytics;
    ConstraintLayout cl_privacy_policy;

    FrameLayout app_native_ad;
    RelativeLayout mainNativeAdContainer;
    TextView tvNativeAdLoading;
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.red(PhotoApp.resources.getColor(R.color.__picker_common_primary)));
        setContentView(R.layout.activity_second_splash);

        adParent = findViewById(R.id.ad_layout_parent);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        agree_btn = findViewById(R.id.agree_btn);
        privacy_text = findViewById(R.id.privacy_text);
        title_animation = findViewById(R.id.title_animation);
        cl_privacy_policy = findViewById(R.id.cl_privacy_policy);
        secondImageBg = findViewById(R.id.second_splash_image);
        abc = findViewById(R.id.abc);

        app_native_ad = (FrameLayout) findViewById(R.id.nativeContainer);
        mainNativeAdContainer = (RelativeLayout) findViewById(R.id.maxNativeAdContainer);
        tvNativeAdLoading = (TextView) findViewById(R.id.tvNativeAdLoading);
        setupSplashBG();
        abc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startToMainActivity();
            }
        });



    }







    public void startToMainActivity() {
        if(!SharePreferenceUtil.isFirstTime((StartEditActivity.this))){
            SharePreferenceUtil.setFirstTime(StartEditActivity.this,false);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else{
            SharePreferenceUtil.setFirstTime(StartEditActivity.this,false);
            startActivity(new Intent(this, SubscriptionActivity.class));
            finish();
        }

    }

//    public void Boom(View iv) {
//        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(iv, "scaleX", 0.8f);
//        scaleXAnimator.setRepeatMode(ValueAnimator.REVERSE);
//        scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        scaleXAnimator.setDuration(700);
//        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(iv, "scaleY", 0.8f);
//        scaleYAnimator.setRepeatMode(ValueAnimator.REVERSE);
//        scaleYAnimator.setRepeatCount(ValueAnimator.INFINITE);
//        scaleYAnimator.setDuration(700);
//        scaleXAnimator.start();
//        scaleYAnimator.start();
//    }

//    private void showPrivacyDialog() {
//
//        cl_privacy_policy.animate().alphaBy(1f);
////        img.setVisibility(View.GONE);
//        adParent.setVisibility(View.VISIBLE);
//
//        agree_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SharedPrefs.write(SplashActivity.this, SharedPrefs.IS_AGREE, true);
//                SplashActivity.this.startToMainActivity();
//            }
//        });
//        privacy_text.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String url = " https://zedlatino.info/privacy-policy-apps.html";
//                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//
//                startActivity(i);
//            }
//        });
//    }


    @Override
    public void onResume() {
        super.onResume();

        loadAds();
    }

    private void loadAds() {
        //MaxAdNativeAd>>done
        if (!SharePreferenceUtil.isPurchased(StartEditActivity.this)){
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



    @Override
    protected void onPause() {
        super.onPause();
    }


    private void setupSplashBG(){


//        Glide.with(this).load(R.drawable.splash_new_scnd).into(secondImageBg);
        Glide.with(this).load(R.drawable.start_button).into(abc);



    }



}
