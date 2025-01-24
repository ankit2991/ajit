package callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location;

import static callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.SQLiteAdapter.MYDATABASE_NAME;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adjust.sdk.Adjust;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.BaseActivity;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;

public class NumberLocationActivity extends BaseActivity implements View.OnClickListener {

    private static final String DATABASE_NAME = "myDB.db";
    private String DB_PATH;
    private String dbpath;
    private SFun sfun;

    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_location);

        sfun = new SFun(NumberLocationActivity.this);
        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.btnMobNumLoc).setOnClickListener(this);
        findViewById(R.id.btnStdCode).setOnClickListener(this);
        findViewById(R.id.btnIsdCode).setOnClickListener(this);
        copydataMobilebase();
        copyDataBase();
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

    @Override
    public void onClick(View v) {
        int id = v.getId();

        int Get_Current_Time_in_Second = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        int aa = sfun.getTimeVar("mytime");
        int finalaa = Get_Current_Time_in_Second - aa;
        switch (id) {
            case R.id.btnMobNumLoc:
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loadInterstitial(dialog, SearchMobileNumActivity.class, SearchMobileNumActivity.class);
                } else {
                    startActivity(new Intent(this, SearchMobileNumActivity.class));
                }
                break;
            case R.id.btnStdCode:
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loadInterstitial(dialog, StdCodeActivity.class, SearchMobileNumActivity.class);
                } else {
                    startActivity(new Intent(this, StdCodeActivity.class));
                }
                break;
            case R.id.btnIsdCode:
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loadInterstitial(dialog, IsdCodeActivity.class, SearchMobileNumActivity.class);
                } else {
                    startActivity(new Intent(this, IsdCodeActivity.class));
                }
                break;
        }
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
            InputStream myInput1 = NumberLocationActivity.this.getAssets().open(DATABASE_NAME);
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
}
