package callerid.truecaller.trackingnumber.phonenumbertracker.block.setting;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import com.adjust.sdk.Adjust;
import com.calldorado.Calldorado;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;




public class SettingCalller extends AppCompatActivity implements OnClickListener {
    private LinearLayout callerInfo;
    private LinearLayout audioManager;
    private LinearLayout system_usage;
    private LinearLayout deviceInfo;

    private LinearLayout after_call_settings;
    private SFun sfun;

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.audioManager:
                intent = new Intent(this, Audio_manager_activity.class);
                showInters(intent);
                break;
            case R.id.callerInfo:
                intent = new Intent(this, Caller_info_activity.class);
                showInters(intent);
                break;
            case R.id.deviceInfo:
                intent = new Intent(this, Device_info_activity.class);
                showInters(intent);
                break;
            case R.id.system_usage:
                intent = new Intent(this, System_usage_activity.class);
                showInters(intent);
                break;

            case R.id.after_call_settings:
                Calldorado.createSettingsActivity(this);
                break;
            default:
                return;
        }
//        startActivity(intent);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_setting_calller);
        sfun = new SFun(this);
        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        callerInfo = (LinearLayout) findViewById(R.id.callerInfo);
        callerInfo.setOnClickListener(this);
        audioManager = (LinearLayout) findViewById(R.id.audioManager);
        audioManager.setOnClickListener(this);
        system_usage = (LinearLayout) findViewById(R.id.system_usage);
        system_usage.setOnClickListener(this);
        deviceInfo = (LinearLayout) findViewById(R.id.deviceInfo);
        deviceInfo.setOnClickListener(this);

        after_call_settings = (LinearLayout) findViewById(R.id.after_call_settings);
        after_call_settings.setOnClickListener(this);
    }

    public void onResume() {
        super.onResume();
        Adjust.onResume();
        final FrameLayout app_ad = (FrameLayout) findViewById(R.id.app_ad);
        MaxAdManager.INSTANCE.createNativeAd(
                this,
                app_ad,
                "GPS119_Native_Small_flag"
        );

    }


    @Override
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

    public void showInters(Intent intent){
        Utils.ad_count++;
        if (Utils.defaultValue<=Utils.ad_count) {
            Utils.ad_count = 0;
            Utils.showInter(this, new adShowCallBack() {
                @Override
                public void adShown(Boolean bol) {
                    startActivity(intent);
                }

                @Override
                public void adFailed(Boolean fal) {
                    startActivity(intent);
                }
            });

        }else{
            startActivity(intent);
        }
    }

    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }
}
