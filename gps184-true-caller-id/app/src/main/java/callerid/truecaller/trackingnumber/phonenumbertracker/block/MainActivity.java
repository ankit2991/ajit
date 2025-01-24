package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.calldorado.Calldorado;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;


import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.bank_info.BankInfoActivity;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.location_info.LocationInfoActivity;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.near_by_place.NearByPlaceActivity;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.NumberLocationActivity;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.SQLiteAdapter;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.RechargeDetailActivity;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.setting.SettingCalller;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.sim_info.MainActivity1;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ussd.SelectUSSDCodeActivity;

import static callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.SQLiteAdapter.MYDATABASE_NAME;

//import callerid.truecaller.trackingnumber.phonenumbertracker.block.appsqlite.SQLiteAdapterContent;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private String dbpath;
    private String DB_PATH;
    private static final String DATABASE_NAME = "myDB.db";
    private SQLiteAdapter sqlite_adapter;
    private String DB_PATH1;


    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    public static boolean isOnline(Context ctx) {
        NetworkInfo netInfo = ((ConnectivityManager) ctx.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sfun = new SFun(MainActivity.this);
        Log.d("getAcceptedConditions", "onCreate: "+ Calldorado.getAcceptedConditions(this));
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        DB_PATH1 = cw.getFilesDir().getAbsolutePath() + "/databases/"; //edited to databases
        copydataMobilebase();
        copyDataBase();
        copyDataBase1();
        findViewById(R.id.btn_num_location).setOnClickListener(this);
        findViewById(R.id.btn_recharge_plan).setOnClickListener(this);
        findViewById(R.id.btn_location_info).setOnClickListener(this);
        findViewById(R.id.btn_ussd_code).setOnClickListener(this);
        findViewById(R.id.btn_bank).setOnClickListener(this);
        findViewById(R.id.btn_near_by_place).setOnClickListener(this);
        findViewById(R.id.btn_sim_info).setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
        findViewById(R.id.btn_spam_call).setOnClickListener(this);

        sqlite_adapter = new SQLiteAdapter(MainActivity.this);
        try {
            sqlite_adapter.openToRead();
        } catch (Exception e) {

        }

    }

    private SFun sfun;

    @Override
    public void onClick(final View v) {
        startaaaaaa(v.getId());
    }

    private void startaaaaaa(int id) {
        int Get_Current_Time_in_Second = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        int aa = sfun.getTimeVar("mytime");
        int finalaa = Get_Current_Time_in_Second - aa;
        switch (id) {
            case R.id.btn_spam_call:

                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);

                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loadInterstitial(dialog, SearchSpamCallerActivity.class, SearchSpamCallerActivity.class);
//                    showInterAndMoveToNext();
//                    startActivity(new Intent(MainActivity.this, SearchSpamCallerActivity.class));
                }else{
                    startActivity(new Intent(MainActivity.this, SearchSpamCallerActivity.class));
                }
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
//                    Dialog dialog = new Dialog(this);
//                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
//
//                    dialog.setContentView(dialogView);
//                    dialog.setCancelable(false);
//                    dialog.show();
//                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    loadAdmobInterstitial(dialog, SearchSpamCallerActivity.class, SearchSpamCallerActivity.class);
//                } else {
//                    startActivity(new Intent(MainActivity.this, SearchSpamCallerActivity.class));
//                }
                break;
            case R.id.btn_num_location:

                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);

                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loadInterstitial(dialog, NumberLocationActivity.class, NumberLocationActivity.class);
//                    showInterAndMoveToNext();
//                    startActivity(new Intent(MainActivity.this, NumberLocationActivity.class));
                }else{
                    startActivity(new Intent(MainActivity.this, NumberLocationActivity.class));
                }
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
//                    Dialog dialog = new Dialog(this);
//                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
//
//                    dialog.setContentView(dialogView);
//                    dialog.setCancelable(false);
//                    dialog.show();
//                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    loadAdmobInterstitial(dialog, NumberLocationActivity.class, NumberLocationActivity.class);
//                } else {
//                    startActivity(new Intent(MainActivity.this, NumberLocationActivity.class));
//                }
                break;
            case R.id.btn_recharge_plan:

                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loadInterstitial(dialog, RechargeDetailActivity.class, RechargeDetailActivity.class);
//                    showInterAndMoveToNext();
//                    startActivity(new Intent(MainActivity.this, RechargeDetailActivity.class));
                }else{
                    startActivity(new Intent(MainActivity.this, RechargeDetailActivity.class));
                }
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
//                    Dialog dialog = new Dialog(this);
//                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
//                    dialog.setContentView(dialogView);
//                    dialog.setCancelable(false);
//                    dialog.show();
//                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    loadAdmobInterstitial(dialog, RechargeDetailActivity.class, RechargeDetailActivity.class);
//                } else {
//                    startActivity(new Intent(MainActivity.this, RechargeDetailActivity.class));
//                }
                break;
            case R.id.btn_location_info:

                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loadInterstitial(dialog, LocationInfoActivity.class, LocationInfoActivity.class);
//                    showInterAndMoveToNext();
//                    startActivity(new Intent(MainActivity.this, LocationInfoActivity.class));
                }else{
                    startActivity(new Intent(MainActivity.this, LocationInfoActivity.class));
                }

//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
//                    Dialog dialog = new Dialog(this);
//                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
//                    dialog.setContentView(dialogView);
//                    dialog.setCancelable(false);
//                    dialog.show();
//                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    loadAdmobInterstitial(dialog, LocationInfoActivity.class, LocationInfoActivity.class);
//                } else {
//                    startActivity(new Intent(MainActivity.this, LocationInfoActivity.class));
//                }
                break;
            case R.id.btn_ussd_code:

                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loadInterstitial(dialog, SelectUSSDCodeActivity.class, SelectUSSDCodeActivity.class);
//                    showInterAndMoveToNext();
//                    startActivity(new Intent(MainActivity.this, SelectUSSDCodeActivity.class));
                }else{
                    startActivity(new Intent(MainActivity.this, SelectUSSDCodeActivity.class));
                }
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
//                    Dialog dialog = new Dialog(this);
//                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
//                    dialog.setContentView(dialogView);
//                    dialog.setCancelable(false);
//                    dialog.show();
//                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    loadAdmobInterstitial(dialog, SelectUSSDCodeActivity.class, SelectUSSDCodeActivity.class);
//                } else {
//                    startActivity(new Intent(MainActivity.this, SelectUSSDCodeActivity.class));
//                }
                break;
            case R.id.btn_bank:

                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
//                    showInterAndMoveToNext();
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loadInterstitial(dialog, BankInfoActivity.class, BankInfoActivity.class);
                }else{
                    startActivity(new Intent(MainActivity.this, BankInfoActivity.class));
                }
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
//                    Dialog dialog = new Dialog(this);
//                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
//                    dialog.setContentView(dialogView);
//                    dialog.setCancelable(false);
//                    dialog.show();
//                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    loadAdmobInterstitial(dialog, BankInfoActivity.class, BankInfoActivity.class);
//                } else {
//                    startActivity(new Intent(MainActivity.this, BankInfoActivity.class));
//                }
                break;
            case R.id.btn_near_by_place:

                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
//                    showInterAndMoveToNext();
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loadInterstitial(dialog, NearByPlaceActivity.class, NearByPlaceActivity.class);
                }else{
                    startActivity(new Intent(MainActivity.this, NearByPlaceActivity.class));
                }
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
//                    Dialog dialog = new Dialog(this);
//                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
//                    dialog.setContentView(dialogView);
//                    dialog.setCancelable(false);
//                    dialog.show();
//                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    loadAdmobInterstitial(dialog, NearByPlaceActivity.class, NearByPlaceActivity.class);
//                } else {
//                    startActivity(new Intent(MainActivity.this, NearByPlaceActivity.class));
//                }
                break;
            case R.id.btn_sim_info:

                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    showInterAndMoveToNext();
                    loadInterstitial(dialog, MainActivity1.class, MainActivity1.class);
                }else{
                    startActivity(new Intent(MainActivity.this, MainActivity1.class));
                }
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
//                    Dialog dialog = new Dialog(this);
//                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
//                    dialog.setContentView(dialogView);
//                    dialog.setCancelable(false);
//                    dialog.show();
//                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    loadAdmobInterstitial(dialog, MainActivity1.class, MainActivity1.class);
//                } else {
//                    startActivity(new Intent(MainActivity.this, MainActivity1.class));
//                }
                break;
            case R.id.btn_setting:

                Utils.ad_count++;
                if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                    Utils.ad_count = 0;
                    Dialog dialog = new Dialog(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
                    dialog.setContentView(dialogView);
                    dialog.setCancelable(false);
                    dialog.show();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    showInterAndMoveToNext();
                    loadInterstitial(dialog, SettingCalller.class, SettingCalller.class);
                }else{
                    startActivity(new Intent(MainActivity.this, SettingCalller.class));
                }
//                if (finalaa >= Utils.Time_interval && !TinyDB.getInstance(this).weeklyPurchased()) {
//                    Dialog dialog = new Dialog(this);
//                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ad, null);
//                    dialog.setContentView(dialogView);
//                    dialog.setCancelable(false);
//                    dialog.show();
//                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                    loadAdmobInterstitial(dialog, SettingCalller.class, SettingCalller.class);
//                } else {
//                    startActivity(new Intent(MainActivity.this, SettingCalller.class));
//                }

                break;
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
//        super.onBackPressed();
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
            InputStream myInput1 = MainActivity.this.getAssets().open(DATABASE_NAME);
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


}
