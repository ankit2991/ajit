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
import androidx.fragment.app.FragmentManager;


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



import org.w3c.dom.Text;

public class StdCodeActivity extends AppCompatActivity implements View.OnClickListener {


    LinearLayout buttonCity;
    LinearLayout buttonState;
    FragmentManager fragmentmanagaer;
    public static City_Fragment frag_city;
    State_Fragment frag_state;
    public static LinearLayout llCity;
    public static LinearLayout llState;
    private TextView txtState;
    private TextView txtCity;

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
        setContentView(R.layout.activity_std_code);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        this.llState = (LinearLayout) findViewById(R.id.ll_state);
        this.llCity = (LinearLayout) findViewById(R.id.ll_city);
        this.buttonCity = (LinearLayout) findViewById(R.id.btn_city);
        this.buttonState = (LinearLayout) findViewById(R.id.btn_state);
        this.buttonCity.setOnClickListener(this);
        this.buttonState.setOnClickListener(this);
        this.frag_state = new State_Fragment();
        this.frag_city = new City_Fragment();
        txtState = (TextView) findViewById(R.id.txt_state);
        txtCity = (TextView) findViewById(R.id.txt_city);
        this.fragmentmanagaer = getSupportFragmentManager();
        this.fragmentmanagaer.beginTransaction().add(R.id.ll_city, this.frag_city).commit();
        this.fragmentmanagaer.beginTransaction().add(R.id.ll_state, this.frag_state).commit();

        try {
            SearchView search = (SearchView) findViewById(R.id.search);
            View backgroundView = search.findViewById(androidx.appcompat.R.id.search_plate);
            backgroundView.setBackgroundColor(Color.TRANSPARENT);
            EditText searchEdit = ((EditText) search.findViewById(androidx.appcompat.R.id.search_src_text));
            searchEdit.setBackgroundColor(Color.TRANSPARENT);
            search.setFocusable(true);
            search.setSelected(true);
            search.setIconifiedByDefault(false);
            search.setSearchableInfo(((SearchManager) getSystemService("search")).getSearchableInfo(getComponentName()));
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                public boolean onQueryTextChange(String query) {
                    if (llCity.getVisibility() == 0) {
                        frag_city.adapter.filter(query);
                    }
                    return true;
                }

                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
            });
        } catch (Exception e) {
        }
    }

    public static void updateCityView(int State_ID, String state) {
        try {
            frag_city.CityperState(State_ID, state);
            llCity.setVisibility(0);
            llState.setVisibility(8);
        } catch (Exception e) {
        }
    }

    public void onClick(View v) {
        if (v == this.buttonState) {
            Utils.ad_count++;
            if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                Utils.ad_count = 0;
                Utils.showInter(this, new adShowCallBack() {
                    @Override
                    public void adShown(Boolean bol) {
                        statesAction();
                    }

                    @Override
                    public void adFailed(Boolean fal) {
                        statesAction();
                    }
                });

            }else{
                statesAction();
            }
        } else if (v == this.buttonCity) {
            Utils.ad_count++;
            if (Utils.defaultValue<=Utils.ad_count && !TinyDB.getInstance(this).weeklyPurchased()) {
                Utils.ad_count = 0;
                Utils.showInter(this, new adShowCallBack() {
                    @Override
                    public void adShown(Boolean bol) {
                        cityAction();
                    }

                    @Override
                    public void adFailed(Boolean fal) {
                        cityAction();
                    }
                });

            }else{
                cityAction();
            }

        }
    }

    public void statesAction(){
        llCity.setVisibility(8);
        llState.setVisibility(0);
        buttonState.setBackgroundResource(R.drawable.bg_tab_std_active);
        txtState.setTextColor(Color.parseColor("#FFFFFF"));
        buttonCity.setBackgroundResource(R.drawable.bg_tab_std_inactive);
        txtCity.setTextColor(Color.parseColor("#00B4D8"));
    }
    public void cityAction(){
        llCity.setVisibility(0);
        llState.setVisibility(8);
        buttonCity.setBackgroundResource(R.drawable.bg_tab_std_active);
        txtCity.setTextColor(Color.parseColor("#FFFFFF"));
        buttonState.setBackgroundResource(R.drawable.bg_tab_std_inactive);
        txtState.setTextColor(Color.parseColor("#00B4D8"));
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
