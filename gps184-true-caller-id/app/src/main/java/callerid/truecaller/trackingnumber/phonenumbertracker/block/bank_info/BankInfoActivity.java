package callerid.truecaller.trackingnumber.phonenumbertracker.block.bank_info;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
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

import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;



//import callerid.truecaller.trackingnumber.phonenumbertracker.block.BuildConfig;

public class BankInfoActivity extends AppCompatActivity {

    private GetSearchDetailTask get_search_detail_task;
    private SQLiteAdapterBank sqlite_adapter;
    private String bank_name;
    private ArrayList<DataModel3> bank_array = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private CustomAdapterBankList adapter;
    public static View.OnClickListener myOnClickListener;
    private String imagename;
    private SFun sfun;


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
        setContentView(R.layout.activity_bank_info);
        sfun = new SFun(this);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        myOnClickListener = new MyOnClickListener(this);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        sqlite_adapter = new SQLiteAdapterBank(BankInfoActivity.this);
        try {
            sqlite_adapter.openToRead();
        } catch (Exception e) {

        }
        get_search_detail_task = new GetSearchDetailTask();
        get_search_detail_task.execute(new String[0]);


    }

    public class GetSearchDetailTask extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
        }

        public String doInBackground(String... args) {
            try {

                Cursor cursor = sqlite_adapter.GetSearchResultByMobileno();
                if (cursor != null) {
                    int i = 0;
                    startManagingCursor(cursor);
                    if (cursor.moveToFirst()) {
                        do {
                            bank_name = cursor.getString(cursor.getColumnIndex("bank_name"));
                            imagename = cursor.getString(cursor.getColumnIndex("bank_fav"));
                            bank_array.add(new DataModel3(
                                    bank_name, imagename));
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
            adapter = new CustomAdapterBankList(bank_array, BankInfoActivity.this);
            recyclerView.setAdapter(adapter);
        }
    }

    private InputStream getdrawablefromRes(String imagename) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(imagename, "drawable", packageName);
        InputStream s = null;
        try {
            s = getAssets().open(imagename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;


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
                Utils.showInter(BankInfoActivity.this, new adShowCallBack() {
                    @Override
                    public void adShown(Boolean bol) {
                        int selectedItemPosition = recyclerView.getChildPosition(v);
                        RecyclerView.ViewHolder viewHolder
                                = recyclerView.findViewHolderForPosition(selectedItemPosition);
                        Intent intent = new Intent(context, BankDetailActivity.class);
                        intent.putExtra("bankname", bank_array.get(selectedItemPosition).getName());
                        startActivity(intent);
                    }

                    @Override
                    public void adFailed(Boolean fal) {
                        int selectedItemPosition = recyclerView.getChildPosition(v);
                        RecyclerView.ViewHolder viewHolder
                                = recyclerView.findViewHolderForPosition(selectedItemPosition);
                        Intent intent = new Intent(context, BankDetailActivity.class);
                        intent.putExtra("bankname", bank_array.get(selectedItemPosition).getName());
                        startActivity(intent);
                    }
                });

            }else{
                int selectedItemPosition = recyclerView.getChildPosition(v);
                RecyclerView.ViewHolder viewHolder
                        = recyclerView.findViewHolderForPosition(selectedItemPosition);
                Intent intent = new Intent(context, BankDetailActivity.class);
                intent.putExtra("bankname", bank_array.get(selectedItemPosition).getName());
                startActivity(intent);
            }

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
