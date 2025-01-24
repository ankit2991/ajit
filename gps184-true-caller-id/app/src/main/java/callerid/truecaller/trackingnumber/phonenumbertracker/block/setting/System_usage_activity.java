package callerid.truecaller.trackingnumber.phonenumbertracker.block.setting;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;


import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class System_usage_activity extends AppCompatActivity {
    private Fragment fragment;
    private Intent f12974e0;
    private TextView f12968Y;
    private TextView f12969Z;
    private TextView f12970a0;
    private TextView f12971b0;
    private TextView f12972c0;
    private TextView f12973d0;
    private ProgressBar f12975f0;
    private TextView f12977h0;

    public static final String f12981i0 = null;
    public long f12982Y = 0;
    public TextView f12983Z;
    public TextView f12984a0;
    public TextView f12985b0;
    public TextView f12987d0;
    public TextView f12988e0;
    public TextView f12989f0;
    public TextView f12990g0;
    public long f12991h0 = 0;
    private SFun sfun;

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

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_system_usage_activity);
        sfun = new SFun(this);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        f12974e0 = registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        f12969Z = (TextView) findViewById(R.id.batt_level_value);
        f12970a0 = (TextView) findViewById(R.id.batt_status_val);
        f12971b0 = (TextView) findViewById(R.id.batt_tech_val);
        f12973d0 = (TextView) findViewById(R.id.batt_voltage_val);
        f12972c0 = (TextView) findViewById(R.id.batt_temp_val);
        f12968Y = (TextView) findViewById(R.id.batt_health_val);
        f12975f0 = (ProgressBar) findViewById(R.id.progressBar);
        f12977h0 = (TextView) findViewById(R.id.system_up_time);
        f12984a0 = (TextView) findViewById(R.id.ram_size);
        f12985b0 = (TextView) findViewById(R.id.ram_used);
        f12983Z = (TextView) findViewById(R.id.ram_free);
        f12988e0 = (TextView) findViewById(R.id.sd_total);
        f12987d0 = (TextView) findViewById(R.id.sd_free);
        f12989f0 = (TextView) findViewById(R.id.sd_used);
        f12990g0 = (TextView) findViewById(R.id.sdcard_total);
        try {
            new BufferedReader(new InputStreamReader(new FileInputStream(new File("/proc/meminfo"))));
        } catch (Exception unused) {
            new ArrayList();
            mo11243D0();
            Boolean.valueOf(Environment.getExternalStorageState().equals("mounted")).booleanValue();

        }
        mo11242D0();
        mo1158j0();

    }

    public void mo11242D0() {
        float f;
        TextView textView = this.f12969Z;
        if (textView != null) {
            int intExtra = this.f12974e0.getIntExtra("level", -1);
            int intExtra2 = this.f12974e0.getIntExtra("scale", -1);
            if (intExtra == -1 || intExtra2 == -1) {
                f = 50.0f;
            } else {
                this.f12975f0.setProgress(intExtra);
                f = (((float) intExtra) / ((float) intExtra2)) * 100.0f;
            }
            textView.setText(String.valueOf(f));
        }
        TextView textView2 = this.f12970a0;
        if (textView2 != null) {
            int intExtra3 = this.f12974e0.getIntExtra("plugged", -1);
            String str = intExtra3 == 4 ? "WIRELESS Charging" : intExtra3 != 1 ? intExtra3 != 2 ? "NOT Charging" : "USB Charging" : "AC Charging";
            textView2.setText(str);
        }
        TextView textView3 = this.f12971b0;
        if (textView3 != null) {
            textView3.setText(this.f12974e0.getExtras().getString("technology"));
        }
        TextView textView4 = this.f12973d0;
        if (textView4 != null) {
            textView4.setText(String.valueOf(this.f12974e0.getIntExtra("voltage", -1)));
        }
        TextView textView5 = this.f12972c0;
        if (textView5 != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf(((float) this.f12974e0.getIntExtra("temperature", -1)) / 10.0f));
            sb.append(" *C");
            textView5.setText(sb.toString());
        }
        TextView textView6 = this.f12968Y;
        if (textView6 != null) {
            int intExtra4 = this.f12974e0.getIntExtra("health", 1);
            String str2 = intExtra4 == 2 ? "Good" : intExtra4 == 3 ? "Over Heat" : intExtra4 == 4 ? "Dead" : intExtra4 == 5 ? "Over Voltage" : intExtra4 == 6 ? "Unspecified Failure" : "Unknown";
            textView6.setText(str2);
        }
        TextView textView7 = this.f12977h0;
        if (textView7 != null) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            StringBuffer stringBuffer = new StringBuffer("");
            if (elapsedRealtime > 86400000) {
                stringBuffer.append(elapsedRealtime / 86400000);
                stringBuffer.append(" days ");
                elapsedRealtime %= 86400000;
            }
            if (elapsedRealtime > 3600000) {
                stringBuffer.append(elapsedRealtime / 3600000);
                stringBuffer.append(" hours ");
                elapsedRealtime %= 3600000;
            }
            if (elapsedRealtime > 60000) {
                stringBuffer.append(elapsedRealtime / 60000);
                stringBuffer.append(" min. ");
                elapsedRealtime %= 60000;
            }
            if (elapsedRealtime > 1000) {
                stringBuffer.append(elapsedRealtime / 1000);
                stringBuffer.append(" sec.");
                long j = elapsedRealtime % 1000;
            }
            textView7.setText(stringBuffer.toString());
        }
    }

    public final void mo11243D0() {
        Pattern compile = Pattern.compile("([a-zA-Z]+):\\s*(\\d+)");
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile("/proc/meminfo", "r");
            while (true) {
                String readLine = randomAccessFile.readLine();
                if (readLine != null) {
                    Matcher matcher = compile.matcher(readLine);
                    if (matcher.find()) {
                        String group = matcher.group(1);
                        String group2 = matcher.group(2);
                        if (group.equalsIgnoreCase("MemTotal")) {
                            f12991h0 = Long.parseLong(group2);
                        } else if (group.equalsIgnoreCase("MemFree") || group.equalsIgnoreCase("SwapFree")) {
                            f12982Y = Long.parseLong(group2);
                        }
                    }
                } else {
                    randomAccessFile.close();
                    f12991h0 *= 1024;
                    f12982Y *= 1024;
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mo1158j0() {
        String str;
        String str2;
        mo11243D0();
        Runtime.getRuntime().totalMemory();
        Runtime.getRuntime().freeMemory();
        TextView textView = this.f12984a0;
        if (textView != null) {
            textView.setText(mo11244a((double) this.f12991h0));
        }
        if (this.f12984a0 != null) {
            this.f12983Z.setText(mo11244a((double) this.f12982Y));
        }
        TextView textView2 = this.f12985b0;
        if (textView2 != null) {
            textView2.setText(mo11244a((double) (this.f12991h0 - this.f12982Y)));
        }
        TextView textView3 = this.f12988e0;
        if (textView3 != null) {
            StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
            textView3.setText(m11004b((double) (((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize()))));
        }
        TextView textView4 = this.f12987d0;
        if (textView4 != null) {
            StatFs statFs2 = new StatFs(Environment.getDataDirectory().getPath());
            textView4.setText(m11004b((double) (((long) statFs2.getAvailableBlocks()) * ((long) statFs2.getBlockSize()))));
        }
        TextView textView5 = this.f12989f0;
        if (textView5 != null) {
            if (!m11003E0()) {
                str2 = f12981i0;
            } else {
                StatFs statFs3 = new StatFs(Environment.getExternalStorageDirectory().getPath());
                str2 = m11004b((double) (((long) statFs3.getAvailableBlocks()) * ((long) statFs3.getBlockSize())));
            }
            textView5.setText(str2);
        }
        TextView textView6 = this.f12990g0;
        if (textView6 != null) {
            if (!m11003E0()) {
                str = f12981i0;
            } else {
                StatFs statFs4 = new StatFs(Environment.getExternalStorageDirectory().getPath());
                str = m11004b((double) (((long) statFs4.getBlockCount()) * ((long) statFs4.getBlockSize())));
            }
            textView6.setText(str);
        }
    }

    public String mo11244a(double d) {
        String format;
        String str;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        double d2 = d / 1048576.0d;
        double d3 = d / 1.073741824E9d;
        double d4 = d / 1.099511627776E12d;
        if (d4 > 1.0d) {
            format = decimalFormat.format(d4);
            str = " TB";
        } else if (d3 > 1.0d) {
            format = decimalFormat.format(d3);
            str = " GB";
        } else if (d2 > 1.0d) {
            format = decimalFormat.format(d2);
            str = " MB";
        } else {
            format = decimalFormat.format(d);
            str = " KB";
        }
        return format.concat(str);
    }

    public static String m11004b(double d) {
        String format;
        String str;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        double d2 = d / 1048576.0d;
        double d3 = d / 1.073741824E9d;
        double d4 = d / 1.099511627776E12d;
        if (d4 > 1.0d) {
            format = decimalFormat.format(d4);
            str = " TB";
        } else if (d3 > 1.0d) {
            format = decimalFormat.format(d3);
            str = " GB";
        } else if (d2 > 1.0d) {
            format = decimalFormat.format(d2);
            str = " MB";
        } else {
            format = decimalFormat.format(d);
            str = " KB";
        }
        return format.concat(str);
    }

    public static boolean m11003E0() {
        return Environment.getExternalStorageState().equals("mounted");
    }
}
