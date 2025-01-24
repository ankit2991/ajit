package callerid.truecaller.trackingnumber.phonenumbertracker.block.sim_info;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;

import java.util.ArrayList;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;




public class MainActivity1 extends AppCompatActivity {

    public static String[] f2023D = {"How To Recharge", "Main Balance Enquiry", "Message Balance Enquiry", "Net Balance Enquiry", "How to Know your number", "Customer care number"};

    public static ArrayList<C2557h> f2024E = new ArrayList<>();


    public static int[] f2025F = {R.drawable.ic_airtel, R.drawable.ic_aircel, R.drawable.ic_idea, R.drawable.ic_vodaf, R.drawable.ic_uni, R.drawable.ic_taomo, R.drawable.ic_jeeio, R.drawable.ic_bsnlgsm, R.drawable.ic_relagsm};

    public static String[] f2026G = {"Airtel", "Aircel", "Idea", "Vodafone", "Telenor", "Tata Docomo", "Jio", "Bsnl", "Reliance"};

    public String[] f2027A;

    public String[] f2028B;

    public String[] f2030t;

    public String[] f2031u;

    public String[] f2032v;

    public String[] f2033w = {"*140*(16 digits code)#", "*145# or *146#", "*142#", "*111*6# or *111*6*2#", "*777*0#", "121 or 9885098850"};

    public String[] f2034x;

    public RecyclerView f2035y;

    public String[] f2036z;
    private SFun sfun;


    protected void onPause() {
        super.onPause();
        Adjust.onPause();
    }

    public MainActivity1() {
        String str = "*123#";
        String str2 = "*555#";
        String str3 = "121";
        this.f2030t = new String[]{"*120*(16 digits code)#", str, str2, "*123*10#/*123*11#", "*121*9#", str3};
        String str4 = "*124*(16 digits code)#";
        String str5 = "*125#";
        String str6 = "*123*1#";
        String str7 = "*1#";
        this.f2031u = new String[]{str4, str5, "*111*5#and*111*12#", str6, str7, "121 or 198"};
        String str8 = "*111#";
        this.f2032v = new String[]{str4, str8, "*167*3#", str5, str7, "12345"};
        this.f2034x = new String[]{"*222*3*(16 digits code)#", "*222*2#", "*222*2#", str, str7, "121 or 9059090590"};
        this.f2036z = new String[]{"*135*2*(16 digits code)#", str8, "*111*1#", str6, "*580#", str3};
        this.f2027A = new String[]{"*123*(16 digits code)#", str, "*112# then press 3", "*112# then press 2", str7, "1503"};
        this.f2028B = new String[]{"*368*(16 digits code)#", "*367#", str2, "*367*3#", str7, "*333"};
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_main1);
        sfun = new SFun(this);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        f2024E = new ArrayList<>();
        for (int i = 0; i < f2026G.length; i++) {
            C2557h hVar = new C2557h();
            hVar.f13006c = f2026G[i];
            hVar.f13005b = f2025F[i];
            C2557h hVar2 = new C2557h();
            if (i == 0) {
                hVar2.f13007d = this.f2030t;
            } else if (i == 1) {
                hVar2.f13007d = this.f2031u;
            } else if (i == 2) {
                hVar2.f13007d = this.f2031u;
            } else if (i == 3) {
                hVar2.f13007d = this.f2032v;
            } else if (i == 4) {
                hVar2.f13007d = this.f2033w;
            } else if (i == 5) {
                hVar2.f13007d = this.f2034x;
            } else if (i == 6) {
                hVar2.f13007d = this.f2036z;
            } else if (i == 7) {
                hVar2.f13007d = this.f2027A;
            } else if (i == 8) {
                hVar2.f13007d = this.f2028B;
            }
            hVar.f13004a = hVar2;
            f2024E.add(hVar);
        }
        this.f2035y = (RecyclerView) findViewById(R.id.rcList);
        this.f2035y.setLayoutManager(new LinearLayoutManager(this));
        this.f2035y.setAdapter(new CustomAdapterSimInfo(this, f2024E));
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

}
