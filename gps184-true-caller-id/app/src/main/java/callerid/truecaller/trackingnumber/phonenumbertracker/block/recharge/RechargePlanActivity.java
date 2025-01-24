package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.adjust.sdk.Adjust;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.tabs.TabLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1.C0468f;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1.C3540f;
import cz.msebera.android.httpclient.Header;


public class RechargePlanActivity extends AppCompatActivity {


    public static List< C3540f > f2248C = new ArrayList();


    public static List< C3540f > f2249D = new ArrayList();


    public static List< C3540f > f2250E = new ArrayList();


    public static List< C3540f > f2251F = new ArrayList();


    public static List< C3540f > f2252G = new ArrayList();


    public ViewPager f2253A;


    public TextView f2254B;


    public C0468f f2255t;


    public int f2257v = 1;
    public TabLayout f2258w;
    public TextView f2259x;
    public String f2260y;
    public String f2261z;
    private ProgressDialog pd;


    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    public class C0609f extends AsyncHttpResponseHandler {

        public void onFailure(int i, Header[] headerArr, byte[] bArr, Throwable th) {
            pd.dismiss();
        }

        public void onSuccess(int i, Header[] headerArr, byte[] bArr) {
            String str = "category_name";
            String str2 = " ";
            try {
                JSONObject jSONObject = new JSONObject(new String(bArr));
                boolean z = jSONObject.getBoolean("has_more");
                JSONArray jSONArray = jSONObject.getJSONArray("grid_layout");
                int i2 = 0;
                if (jSONArray.toString().length() == 2) {
                    RechargePlanActivity.this.f2254B.setVisibility(0);
                }
                JSONObject jSONObject2 = jSONArray.getJSONObject(0);
                int i3 = 0;
                while (i2 < jSONArray.length()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(str2);
                    sb.append(jSONArray.getJSONObject(i2).getString("name"));
                    JSONArray jSONArray2 = jSONArray.getJSONObject(i2).getJSONArray("long_rich_desc");
                    while (i3 < jSONArray2.length()) {
                        JSONObject jSONObject3 = jSONArray2.getJSONObject(i3).getJSONObject("attributes");
                        String str3 = "description";
                        String str4 = "Talktime";
                        String str5 = "Validity";
                        String str6 = "offer_price";
                        if (jSONObject3.getString(str).matches("Top up")) {
                            C3540f fVar = new C3540f();
                            fVar.f15203e = jSONArray.getJSONObject(i2).getString(str6);
                            fVar.f15202d = jSONObject3.getString(str5);
                            fVar.f15201c = jSONObject3.getString(str4);
                            fVar.f15200b = jSONArray2.getJSONObject(i3).getString(str3);
                            RechargePlanActivity.f2252G.add(fVar);
                        }
                        if (jSONObject3.getString(str).matches("Special Recharge")) {
                            C3540f fVar2 = new C3540f();
                            fVar2.f15203e = jSONArray.getJSONObject(i2).getString(str6);
                            fVar2.f15202d = jSONObject3.getString(str5);
                            fVar2.f15201c = jSONObject3.getString(str4);
                            fVar2.f15200b = jSONArray2.getJSONObject(i3).getString(str3);
                            RechargePlanActivity.f2250E.add(fVar2);
                        }
                        if (jSONObject3.getString(str).matches("Roaming")) {
                            C3540f fVar3 = new C3540f();
                            fVar3.f15203e = jSONArray.getJSONObject(i2).getString(str6);
                            fVar3.f15202d = jSONObject3.getString(str5);
                            fVar3.f15201c = jSONObject3.getString(str4);
                            fVar3.f15200b = jSONArray2.getJSONObject(i3).getString(str3);
                            RechargePlanActivity.f2249D.add(fVar3);
                        }
                        if (jSONObject3.getString(str).matches("2G/3G/4G Data")) {
                            C3540f fVar4 = new C3540f();
                            fVar4.f15203e = jSONArray.getJSONObject(i2).getString(str6);
                            fVar4.f15202d = jSONObject3.getString(str5);
                            fVar4.f15201c = jSONObject3.getString(str4);
                            fVar4.f15200b = jSONArray2.getJSONObject(i3).getString(str3);
                            RechargePlanActivity.f2248C.add(fVar4);
                        }
                        if (jSONObject3.getString(str).matches("Full Talktime")) {
                            C3540f fVar5 = new C3540f();
                            fVar5.f15203e = jSONArray.getJSONObject(i2).getString(str6);
                            fVar5.f15202d = jSONObject3.getString(str5);
                            fVar5.f15201c = jSONObject3.getString(str4);
                            fVar5.f15200b = jSONArray2.getJSONObject(i3).getString(str3);
                            RechargePlanActivity.f2251F.add(fVar5);
                        }
                        i3++;
                    }
                    i2++;
                    i3 = 0;
                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append(str2);
                sb2.append(RechargePlanActivity.f2252G.size());
                StringBuilder sb3 = new StringBuilder();
                sb3.append(str2);
                sb3.append(RechargePlanActivity.f2250E.size());
                StringBuilder sb4 = new StringBuilder();
                sb4.append(str2);
                sb4.append(RechargePlanActivity.f2249D.size());
                StringBuilder sb5 = new StringBuilder();
                sb5.append(str2);
                sb5.append(RechargePlanActivity.f2248C.size());
                StringBuilder sb6 = new StringBuilder();
                sb6.append(str2);
                sb6.append(z);
                if (z) {
                    RechargePlanActivity.this.f2257v++;
                    RechargePlanActivity.this.mo2569d(RechargePlanActivity.this.f2257v);
                } else {
                    SimpleFragmentPagerAdapter simpleFragmentPagerAdapter = new SimpleFragmentPagerAdapter(RechargePlanActivity.this, getSupportFragmentManager());
                    RechargePlanActivity.this.f2253A.setOffscreenPageLimit(5);
                    RechargePlanActivity.this.f2258w.setupWithViewPager(RechargePlanActivity.this.f2253A);
                    RechargePlanActivity.this.f2253A.setAdapter(simpleFragmentPagerAdapter);
                }
                StringBuilder sb7 = new StringBuilder();
                sb7.append(str2);
                sb7.append(jSONObject2.length());
                pd.dismiss();
            } catch (JSONException e) {
                Log.d("C0609f", "Exception: " + e);
                pd.dismiss();
            }
        }
    }


    public void mo2569d(int i) {
        new AsyncHttpClient();
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        StringBuilder a = new StringBuilder().append("https://catalog.paytm.com/v1/g/recharge-plans/mobile?channel=web&child_site_id=1&circle=");
        a.append(this.f2261z);
        a.append("&description=1&items_per_page=30&locale=en-in&operator=");
        a.append(this.f2260y);
        a.append("&page_count=");
        a.append(i);
        a.append("&site_id=1&type=mobile&version=2");
        StringBuilder sb = new StringBuilder();
        sb.append("API ");
        sb.append(a.toString());
        asyncHttpClient.get(a.toString(), (RequestParams) null, (ResponseHandlerInterface) new C0609f());
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

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_recharge_plan);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        pd = new ProgressDialog(RechargePlanActivity.this);
        pd.setCancelable(false);
        pd.setMessage("Please Wait...");
        pd.show();

        this.f2258w = (TabLayout) findViewById(R.id.tabs_main);
        this.f2258w.setTabRippleColor(null);
        this.f2254B = (TextView) findViewById(R.id.notfound);

        this.f2253A = (ViewPager) findViewById(R.id.viewpager_main);
        this.f2259x = (TextView) findViewById(R.id.txtData);

        f2253A.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                Utils.ad_count++;
                if (Utils.defaultValue <= Utils.ad_count) {
                    Utils.ad_count = 0;
                    Utils.showInter(RechargePlanActivity.this, new adShowCallBack() {
                        @Override
                        public void adShown(Boolean bol) {

                        }

                        @Override
                        public void adFailed(Boolean fal) {

                        }
                    });
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        Intent intent = getIntent();
        this.f2260y = intent.getStringExtra("Card");
        this.f2261z = intent.getStringExtra("State");

        NetworkInfo activeNetworkInfo = ((ConnectivityManager) getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
        } else {
            this.f2259x.setVisibility(0);
        }
        f2252G.clear();
        f2250E.clear();
        f2249D.clear();
        f2251F.clear();
        f2248C.clear();
        mo2569d(1);

    }

    @Override
    public void onBackPressed() {
        Utils.ad_count++;
        if (Utils.defaultValue <= Utils.ad_count) {
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

        } else {
            finish();
        }
    }
}
