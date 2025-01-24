package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import com.adjust.sdk.Adjust;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.squareup.picasso.Picasso;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.BaseActivity;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.util.Constants;


public class RechargeDetailActivity extends BaseActivity {


    public String f2231A;


    public String[] f2234t = {"Airtel", "Vodafone", "BSNL", "Idea", "Jio", "MTNL", "Tata DOCOMO"};

    public String[] f2235u = {"Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chattisgarh", "Delhi", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka", "Kerala", "Madhya Pradesh", "Manipur", "Meghalaya", "Maharashtra", "Mizoram", "Nagaland", "Orissa", "Punjab", "Rajastan", "Sikkim", "Tamilnadu", "Tripura", "Uttar Pradesh", "Uttaranchal", "West Bengal"};

    public RelativeLayout f2236v;


    public Spinner f2238x;

    public Spinner f2239y;

    public String f2240z;
    private SFun sfun;


    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    public class C0597a implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
            Utils.ad_count++;
            if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(RechargeDetailActivity.this).weeklyPurchased()) {
                Utils.ad_count = 0;
                Utils.showInter(RechargeDetailActivity.this, new adShowCallBack() {
                    @Override
                    public void adShown(Boolean bol) {
                        RechargeDetailActivity rechargeDetailActivity = RechargeDetailActivity.this;
                        rechargeDetailActivity.f2240z = rechargeDetailActivity.f2234t[i];
                    }

                    @Override
                    public void adFailed(Boolean fal) {
                        RechargeDetailActivity rechargeDetailActivity = RechargeDetailActivity.this;
                        rechargeDetailActivity.f2240z = rechargeDetailActivity.f2234t[i];
                    }
                });

            }else{
                RechargeDetailActivity rechargeDetailActivity = RechargeDetailActivity.this;
                rechargeDetailActivity.f2240z = rechargeDetailActivity.f2234t[i];
            }

        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    public class C0598b implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {

            Utils.ad_count++;
            if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(RechargeDetailActivity.this).weeklyPurchased()) {
                Utils.ad_count = 0;
                Utils.showInter(RechargeDetailActivity.this, new adShowCallBack() {
                    @Override
                    public void adShown(Boolean bol) {
                        RechargeDetailActivity rechargeDetailActivity = RechargeDetailActivity.this;
                        rechargeDetailActivity.f2231A = rechargeDetailActivity.f2235u[i];
                    }

                    @Override
                    public void adFailed(Boolean fal) {
                        RechargeDetailActivity rechargeDetailActivity = RechargeDetailActivity.this;
                        rechargeDetailActivity.f2231A = rechargeDetailActivity.f2235u[i];
                    }
                });

            }else{
                RechargeDetailActivity rechargeDetailActivity = RechargeDetailActivity.this;
                rechargeDetailActivity.f2231A = rechargeDetailActivity.f2235u[i];
            }

        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    public class C0599c implements OnClickListener {

        public void onClick(View view) {
            Utils.ad_count++;
            Intent intent = new Intent(RechargeDetailActivity.this,RechargePlanActivity.class);
            if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(RechargeDetailActivity.this).weeklyPurchased()) {
                Utils.ad_count = 0;
                Utils.showInter(RechargeDetailActivity.this, new adShowCallBack() {
                    @Override
                    public void adShown(Boolean bol) {
                        String str = " ";
                        StringBuilder a = new StringBuilder(str);
                        a.append(RechargeDetailActivity.this.f2240z);
                        StringBuilder sb = new StringBuilder();
                        sb.append(str);
                        sb.append(RechargeDetailActivity.this.f2231A);
                        intent.putExtra("Card", RechargeDetailActivity.this.f2240z);
                        intent.putExtra("State", RechargeDetailActivity.this.f2231A);
                        RechargeDetailActivity.this.startActivity(intent);
                    }

                    @Override
                    public void adFailed(Boolean fal) {
                        String str = " ";
                        StringBuilder a = new StringBuilder(str);
                        a.append(RechargeDetailActivity.this.f2240z);
                        StringBuilder sb = new StringBuilder();
                        sb.append(str);
                        sb.append(RechargeDetailActivity.this.f2231A);
                        intent.putExtra("Card", RechargeDetailActivity.this.f2240z);
                        intent.putExtra("State", RechargeDetailActivity.this.f2231A);
                        RechargeDetailActivity.this.startActivity(intent);
                    }
                });


//                RechargeDetailActivity.this.startActivity(intent);
            }else{
                String str = " ";
                StringBuilder a = new StringBuilder(str);
                a.append(RechargeDetailActivity.this.f2240z);
                StringBuilder sb = new StringBuilder();
                sb.append(str);
                sb.append(RechargeDetailActivity.this.f2231A);
//                Intent intent = new Intent(RechargeDetailActivity.this.getApplicationContext(), RechargePlanActivity.class);
                intent.putExtra("Card", RechargeDetailActivity.this.f2240z);
                intent.putExtra("State", RechargeDetailActivity.this.f2231A);
                RechargeDetailActivity.this.startActivity(intent);
            }

        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_recharge_detail);
        sfun = new SFun(this);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        this.f2238x = (Spinner) findViewById(R.id.spinnerLocation);
        this.f2239y = (Spinner) findViewById(R.id.spinnerCompany);
        this.f2236v = (RelativeLayout) findViewById(R.id.l_btn_search);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, 17367048, this.f2234t);
        arrayAdapter.setDropDownViewResource(17367049);
        this.f2239y.setAdapter(arrayAdapter);
        ArrayAdapter arrayAdapter2 = new ArrayAdapter(this, 17367048, this.f2235u);
        arrayAdapter2.setDropDownViewResource(17367049);
        this.f2238x.setAdapter(arrayAdapter2);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                f2239y.setOnItemSelectedListener(new C0597a());
                f2238x.setOnItemSelectedListener(new C0598b());
            }
        }, 700);

        this.f2236v.setOnClickListener(new C0599c());
    }

    public void onResume() {
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
