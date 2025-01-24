package callerid.truecaller.trackingnumber.phonenumbertracker.block.setting;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;

import com.adjust.sdk.Adjust;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;


public class Caller_info_activity extends AppCompatActivity {

    public ImageView f1941t;

    public SharedPreferences.Editor f12992Y;
    public CheckBox f12993Z;
    public CheckBox f12994a0;
    public SharedPreferences f12996c0;
    private SFun sfun;

    public void onBackPressed() {
        Utils.ad_count++;
        if (Utils.defaultValue<=Utils.ad_count) {
            Utils.ad_count = 0;
            Utils.showInter(this, new adShowCallBack() {
                @Override
                public void adShown(Boolean bol) {
                    finish();
                }

                @Override
                public void adFailed(Boolean fal) {
                    finish();
                }
            });

        }else{
            finish();
        }
    }


    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_caller_info_activity);
        sfun = new SFun(this);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        f12996c0 = getSharedPreferences("call_setings", 0);
        f12992Y = f12996c0.edit();
        f12993Z = (CheckBox) findViewById(R.id.in_check);
        f12994a0 = (CheckBox) findViewById(R.id.out_check);
        f12993Z.setChecked(f12996c0.getBoolean("in_call_value", true));
        f12994a0.setChecked(f12996c0.getBoolean("out_call_value", true));
        f12993Z.setOnCheckedChangeListener(new C2548a());
        f12994a0.setOnCheckedChangeListener(new C2549b());

    }

    @Override
    protected void onResume() {
        super.onResume();
        Adjust.onResume();
        final FrameLayout app_ad = (FrameLayout) findViewById(R.id.app_ad);
        MaxAdManager.INSTANCE.createNativeAd(
                this,
                app_ad,
                null
        );

    }

    public class C2548a implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            f12992Y.putBoolean("in_call_value", z);
            f12992Y.commit();
        }
    }

    public class C2549b implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            f12992Y.putBoolean("out_call_value", z);
            f12992Y.commit();
        }
    }

}
