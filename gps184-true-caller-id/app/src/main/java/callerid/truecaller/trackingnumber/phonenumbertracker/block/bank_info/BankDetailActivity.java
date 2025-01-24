package callerid.truecaller.trackingnumber.phonenumbertracker.block.bank_info;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.adjust.sdk.Adjust;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.squareup.picasso.Picasso;

import java.util.Random;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.StartPage;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;



public class BankDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private SQLiteAdapterBank sqlite_adapter;
    private GetSearchDetailTask get_search_detail_task;
    private String bankname;
    private String bank_name;
    private String imagename;
    private String bank_inquiry;
    private String bank_care;
    private ImageView bank_image;
    private TextView txt_name;
    private TextView txt_check_balance;
    private TextView txt_cus_care;
    private int imageid;
    private View callcare;
    private View callbalance;


    protected void onPause() {
        super.onPause();
        Adjust.onPause();
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
        setContentView(R.layout.activity_bank_detail);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        bankname = getIntent().getStringExtra("bankname");
        bank_image = findViewById(R.id.bank_image);
        txt_name = findViewById(R.id.txt_name);
        txt_check_balance = findViewById(R.id.txt_check_balance);
        txt_cus_care = findViewById(R.id.txt_cus_care);
        callcare = findViewById(R.id.callcare);
        callbalance = findViewById(R.id.callbalance);
        callbalance.setOnClickListener(this);
        callcare.setOnClickListener(this);


        sqlite_adapter = new SQLiteAdapterBank(BankDetailActivity.this);
        try {
            sqlite_adapter.openToRead();
        } catch (Exception e) {

        }
        get_search_detail_task = new GetSearchDetailTask();
        get_search_detail_task.execute(new String[0]);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.callcare) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.DIAL");
            StringBuilder a = new StringBuilder("tel:");
            a.append(bank_care);
            intent.setData(Uri.parse(a.toString()));
            startActivity(intent);
        } else {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.DIAL");
            StringBuilder a = new StringBuilder("tel:");
            a.append(bank_inquiry);
            intent.setData(Uri.parse(a.toString()));
            startActivity(intent);
        }
    }

    public class GetSearchDetailTask extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
        }

        public String doInBackground(String... args) {
            try {
                Cursor cursor = sqlite_adapter.Getbankdetals(bankname);
                if (cursor != null) {
                    int i = 0;
                    startManagingCursor(cursor);
                    if (cursor.moveToFirst()) {
                        do {
                            bank_name = cursor.getString(cursor.getColumnIndex("bank_name"));
                            imagename = cursor.getString(cursor.getColumnIndex("bank_fav"));
                            bank_inquiry = cursor.getString(cursor.getColumnIndex("bank_inquiry"));
                            bank_care = cursor.getString(cursor.getColumnIndex("bank_care"));
                            imageid = getdrawablefromRes(imagename);

                            i++;
                        } while (cursor.moveToNext());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            Picasso.get().load(Uri.parse("file:///android_asset/" + imagename + ".png")).placeholder(R.drawable.icc_bank).into(bank_image);

            txt_name.setText(bank_name);
            txt_check_balance.setText(bank_inquiry);
            txt_cus_care.setText(bank_care);

        }
    }

    private int getdrawablefromRes(String imagename) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(imagename, "drawable", packageName);
        return resId;
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
