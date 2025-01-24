package callerid.truecaller.trackingnumber.phonenumbertracker.block.ussd;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adjust.sdk.Adjust;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.squareup.picasso.Picasso;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;

public class SelectUSSDCodeActivity extends AppCompatActivity {

    public static View.OnClickListener myOnClickListener;
    private LinearLayoutManager layoutManager;
    private CustomAdapter adapter;
    private ArrayList<DataModel> data;
    private RecyclerView recyclerView;
    static String[] nameArray = {"Airtel", "BSNL", "Aircel", "Idea", "Reliance", "Telenor", "Vodafone", "Tata Docomo", "Ninemobile", "MTN", "GLO"};
    static Integer[] drawableArray = {
            R.drawable.ic_airtel,
            R.drawable.ic_bsnlgsm,
            R.drawable.ic_aircel,
            R.drawable.ic_idea,
            R.drawable.ic_relagsm,
            R.drawable.ic_uni,
            R.drawable.ic_vodaf,
            R.drawable.ic_taomo,
            R.drawable.mobile9,
            R.drawable.mtn,
            R.drawable.glo};


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
                null
        );

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_ussdcode);
        sfun = new SFun(this);
        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        myOnClickListener = new MyOnClickListener(this);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        data = new ArrayList<DataModel>();
        for (int i = 0; i < nameArray.length; i++) {
            data.add(new DataModel(
                    nameArray[i],
                    drawableArray[i]
            ));
        }
        adapter = new CustomAdapter(data);
        recyclerView.setAdapter(adapter);
    }


    private class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {

            Utils.ad_count++;
            if (Utils.defaultValue<=Utils.ad_count) {
                Utils.ad_count = 0;
                Utils.showInter(SelectUSSDCodeActivity.this, new adShowCallBack() {
                    @Override
                    public void adShown(Boolean bol) {
                        int selectedItemPosition = recyclerView.getChildPosition(v);
                        RecyclerView.ViewHolder viewHolder
                                = recyclerView.findViewHolderForPosition(selectedItemPosition);
                        Intent intent = new Intent(context, USSDCodeActivity.class);
                        intent.putExtra("name", nameArray[selectedItemPosition]);
                        startActivity(intent);
                    }

                    @Override
                    public void adFailed(Boolean fal) {
                        int selectedItemPosition = recyclerView.getChildPosition(v);
                        RecyclerView.ViewHolder viewHolder
                                = recyclerView.findViewHolderForPosition(selectedItemPosition);
                        Intent intent = new Intent(context, USSDCodeActivity.class);
                        intent.putExtra("name", nameArray[selectedItemPosition]);
                        startActivity(intent);
                    }
                });

            }else{
                int selectedItemPosition = recyclerView.getChildPosition(v);
                RecyclerView.ViewHolder viewHolder
                        = recyclerView.findViewHolderForPosition(selectedItemPosition);
                Intent intent = new Intent(context, USSDCodeActivity.class);
                intent.putExtra("name", nameArray[selectedItemPosition]);
                startActivity(intent);
            }

        }
    }

    private SFun sfun;


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
