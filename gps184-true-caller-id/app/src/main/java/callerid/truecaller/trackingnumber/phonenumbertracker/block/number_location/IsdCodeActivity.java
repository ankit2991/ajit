package callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location;

import android.app.Activity;
import android.app.SearchManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;



public class IsdCodeActivity extends AppCompatActivity {

    private RecyclerView rv_isd_code;
    private ISD_Adapter adapter;


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
        setContentView(R.layout.activity_isd_code);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        rv_isd_code = findViewById(R.id.rv_isd_code);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv_isd_code.setLayoutManager(mLayoutManager);
        rv_isd_code.setItemAnimator(new DefaultItemAnimator());
        try {
            adapter = new ISD_Adapter(CountryToPhonePrefix.getAll());
            rv_isd_code.setAdapter(adapter);
        } catch (Exception e) {
        }

        SearchView search = findViewById(R.id.ISD_Searche);
        View backgroundView = search.findViewById(androidx.appcompat.R.id.search_plate);
        backgroundView.setBackgroundColor(Color.TRANSPARENT);
        EditText searchEdit = search.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEdit.setBackgroundColor(Color.TRANSPARENT);
        search.setFocusable(true);
        search.setSelected(true);
        search.setIconifiedByDefault(false);
        search.setSearchableInfo(((SearchManager) getSystemService("search")).getSearchableInfo(getComponentName()));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String query) {
                try {
                    adapter.filter(query);
                } catch (Exception e) {
                }
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });
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
