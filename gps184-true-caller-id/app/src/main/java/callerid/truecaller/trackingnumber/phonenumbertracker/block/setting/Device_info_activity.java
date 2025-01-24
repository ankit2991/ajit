package callerid.truecaller.trackingnumber.phonenumbertracker.block.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.hardware.Camera;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Iterator;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;



public class Device_info_activity extends AppCompatActivity {

    public TextView f13016Y;
    public TextView f13017Z;
    public TextView f13018a0;
    public TextView f13019b0;
    public double f13020c0;
    public TextView f13021d0;
    public TextView f13022e0;
    public Display f13023f0;
    public DisplayMetrics f13024g0;
    public TextView f13025h0;
    public TextView f13026i0;
    public TextView f13027j0;
    public TextView f13028k0;
    public TextView f13029l0;
    public TextView f13030m0;
    public TelephonyManager f13032o0;
    public DecimalFormat f13033p0;
    public TextView f13034q0;
    public TextView f13039Y;
    public TextView f13040Z;
    public TextView f13041a0;
    public TextView f13042b0;
    public TextView f13043c0;
    public TextView f13044d0;
    public TextView f13045e0;
    public TextView f13047g0;
    public TextView f11480i0;
    public TelephonyManager f13050j0;
    public TextView f13051k0;
    private String f11477f0;
    private TextView newtextView, newtextView2, newtextView3, newtextView4, newtextView5, newtextView6, newtextView7, newtextView8, newtextView9;
    private int textcpu;
    private String newstr4;
    private String newstr;
    private String newstr3;
    private String newstr2;
    private String f13037a;
    private String f13035a;
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

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_device_info_activity);
        sfun = new SFun(this);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        f13021d0 = (TextView) findViewById(R.id.device_name);
        f13022e0 = (TextView) findViewById(R.id.device_user_name);
        f13018a0 = (TextView) findViewById(R.id.brand_name);
        f13028k0 = (TextView) findViewById(R.id.product_name);
        f13026i0 = (TextView) findViewById(R.id.imei_name);
        f13017Z = (TextView) findViewById(R.id.back_camera);
        f13025h0 = (TextView) findViewById(R.id.front_camera);
        f13030m0 = (TextView) findViewById(R.id.resolution);
        f13019b0 = (TextView) findViewById(R.id.density);
        f13029l0 = (TextView) findViewById(R.id.refresh);
        f13027j0 = (TextView) findViewById(R.id.physical_size);
        f13034q0 = (TextView) findViewById(R.id.version);
        f13016Y = (TextView) findViewById(R.id.api_level);
        f13039Y = findViewById(R.id.core);
        f13043c0 = findViewById(R.id.max_frequency);
        f13041a0 = findViewById(R.id.instruction_set);
        f13044d0 = findViewById(R.id.network_type);
        f13042b0 = findViewById(R.id.ip_add);
        f13051k0 = findViewById(R.id.wifi_add);
        f13045e0 = findViewById(R.id.operator);
        f13040Z = findViewById(R.id.country);
        f13047g0 = findViewById(R.id.roaming);
        f11480i0 = findViewById(R.id.service_state);
        f13050j0 = (TelephonyManager) getSystemService("phone");

        try {
            f13032o0 = (TelephonyManager) getSystemService("phone");
            f13023f0 = ((WindowManager) getSystemService("window")).getDefaultDisplay();
            f13024g0 = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(f13024g0);
            DisplayMetrics displayMetrics = f13024g0;
            int i = displayMetrics.widthPixels;
            float f = (float) i;
            float f2 = displayMetrics.xdpi;
            float f3 = (((float) i) / f2) * (f / f2);
            int i2 = displayMetrics.heightPixels;
            float f4 = (float) i2;
            float f5 = displayMetrics.ydpi;
            f13020c0 = Math.sqrt((double) (((((float) i2) / f5) * (f4 / f5)) + f3));
            f13033p0 = new DecimalFormat("#.##");
            mo1158j0();
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong! Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint({"MissingPermission"})
    public void mo1158j0() {
        newtextView = f13039Y;
        newtextView2 = f13041a0;
        newtextView3 = f13045e0;
        newtextView4 = f13040Z;
        newtextView5 = f13043c0;
        newtextView6 = f13044d0;
        newtextView7 = f13047g0;
        newtextView8 = f13042b0;
        newtextView9 = f13051k0;
        TextView textView = this.f13021d0;
        if (textView != null) {
            textView.setText(Build.MODEL);
        }
        TextView textView2 = this.f13022e0;
        if (textView2 != null) {
            BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
            textView2.setText(defaultAdapter != null ? defaultAdapter.getName() : "");

        }
        TextView textView3 = this.f13018a0;
        if (textView3 != null) {
            textView3.setText(Build.BRAND);
        }
        TextView textView4 = this.f13028k0;
        if (textView4 != null) {
            textView4.setText(Build.PRODUCT);
        }
        TextView textView5 = this.f13026i0;
        if (textView5 != null) {
            String str1;
            try {
                str1 = f13032o0.getDeviceId();
            } catch (SecurityException e) {
                str1 = "Not Available";
            }
            textView5.setText(str1 != null ? str1 : "Not Available");
        }
        TextView textView6 = this.f13030m0;
        if (textView6 != null) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(this.f13024g0.widthPixels);
                sb.append("w X ");
                sb.append(this.f13024g0.heightPixels);
                sb.append("h");
                textView6.setText(sb.toString());
            } catch (Exception e) {
                textView6.setText("Not Available");
            }

        }
        TextView textView7 = this.f13019b0;
        if (textView7 != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int i = displayMetrics.densityDpi;
            String str = i == 120 ? "120 dpi (Low)" : i == 160 ? "160 dpi (Medium)" : i == 240 ? "240 dpi (High)" : i == 320 ? "320 dpi (X High)" : i == 480 ? "480 dpi (XX High)" : i == 640 ? "640 dpi (XXX High)" : i == 213 ? "TV" : i == 400 ? "400 dpi" : "Unknown";
            textView7.setText(str);
        }
        TextView textView8 = this.f13029l0;
        if (textView8 != null) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(this.f13023f0.getRefreshRate());
            sb2.append("Hz");
            textView8.setText(sb2.toString());
        }
        TextView textView9 = this.f13027j0;
        if (textView9 != null) {
            StringBuilder sb3 = new StringBuilder();
            sb3.append(this.f13033p0.format(this.f13020c0));
            sb3.append("\"(");
            sb3.append(this.f13033p0.format(this.f13020c0 * 2.54d));
            sb3.append(" cm)");
            textView9.setText(sb3.toString());
        }
        TextView textView10 = this.f13034q0;
        if (textView10 != null) {
            StringBuilder sb4 = new StringBuilder();
            sb4.append(Build.VERSION.RELEASE);
            sb4.append("( ");
            int i2 = Build.VERSION.SDK_INT;
            String str2 = (i2 == 16 || i2 == 17 || i2 == 18) ? "Jelly Bean" : i2 == 19 ? "KitKat" : (i2 == 21 || i2 == 22) ? "Lollipop" : i2 == 23 ? "Marshmallow" : (i2 == 24 || i2 == 25) ? "Naugat" : null;
            sb4.append(str2);
            sb4.append(")");
            textView10.setText(sb4.toString());
        }
        TextView textView11 = this.f13016Y;
        if (textView11 != null) {
            textView11.setText(String.valueOf(Build.VERSION.SDK_INT));
        }
        if (newtextView3 != null) {
            String stropr1 = f13050j0.getNetworkOperatorName();
            if (stropr1 != "") {
                newtextView3.setText(stropr1);
            } else {
                newtextView3.setText("Not Available");
            }
        }

        if (newtextView4 != null) {
            String stropr = f13050j0.getNetworkCountryIso();
            if (stropr != "") {
                String st1 = stropr.toUpperCase();
                newtextView4.setText(st1);
            } else {
                newtextView4.setText("Not Available");
            }
        }
        if (newtextView != null) {
            try {
                textcpu = new File("/sys/devices/system/cpu/").listFiles(new C2565l()).length;
            } catch (Exception unused) {
                textcpu = 1;
            }
            newtextView.setText(String.valueOf(textcpu));
        }

        if (newtextView2 != null) {
            newstr4 = Build.CPU_ABI;
            int i2 = Build.VERSION.SDK_INT;
            String str5 = Build.CPU_ABI2;
            if (str5 != null && !str5.equals("unknown")) {
                StringBuilder b = new StringBuilder().append(newstr4);
                b.append(", ");
                b.append(Build.CPU_ABI2);
                newstr4 = b.toString();
            }
            newtextView2.setText(newstr4);
        }

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
        } catch (FileNotFoundException e) {
        }
        try {
            newstr = randomAccessFile.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            randomAccessFile.close();
        } catch (Exception unused2) {
        }
        if (newstr != "") {
            StringBuilder sb = new StringBuilder();
            sb.append(newstr);
            sb.append("Hz");
            newtextView5.setText(sb.toString());
        }
        if (newtextView7 != null) {
            newtextView7.setText(f13050j0.isNetworkRoaming() ? "In Roaming" : "Not Roaming");
        }

        if (newtextView9 != null) {
            String str7 = "wlan0";
            Iterator it3 = null;
            try {
                it3 = Collections.list(NetworkInterface.getNetworkInterfaces()).iterator();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            if (it3.hasNext()) {
                NetworkInterface networkInterface = (NetworkInterface) it3.next();
                networkInterface.getName().equalsIgnoreCase(str7);
                byte[] hardwareAddress = new byte[0];
                try {
                    hardwareAddress = networkInterface.getHardwareAddress();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                if (hardwareAddress == null) {
                    newstr3 = "No H/W";
                } else {
                    StringBuilder sb2 = new StringBuilder();
                    for (byte valueOf : hardwareAddress) {
                        sb2.append(String.format("%02X:", Byte.valueOf(valueOf)));
                    }
                    if (sb2.length() > 0) {
                        sb2.deleteCharAt(sb2.length() - 1);
                    }
                    newstr3 = sb2.toString();
                }
            }
            newtextView9.setText(newstr3);
        }
        if (newtextView6 != null) {
            int phoneType = f13050j0.getPhoneType();
            String str6 = phoneType != 0 ? phoneType != 1 ? phoneType != 2 ? newstr3 : "CDMA" : "GSM" : "NONE";
            newtextView6.setText(str6);
        }
        f13037a = mo11254d(1);

        TextView textView1111 = f13025h0;
        if (textView1111 != null) {
            textView1111.setText(this.f13037a);
        }
        f13035a = mo11253c(0);
        TextView textView222 = f13017Z;
        if (textView222 != null) {
            textView222.setText(this.f13035a);
        }
        if (newtextView8 != null) {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            if (ip != "") {
                newtextView8.setText(ip);
            } else {
                newtextView8.setText("Not Available");
            }
        }
        this.f13050j0.listen(new C2567a(), 1);
    }

    public class C2567a extends PhoneStateListener {
        public void onServiceStateChanged(android.telephony.ServiceState serviceState) {
            TextView textView;
            super.onServiceStateChanged(serviceState);
            serviceState.getRoaming();
            int state = serviceState.getState();
            if (state == 0) {
                f11477f0 = "IN SERVICE";
                textView = f11480i0;
                if (textView == null) {
                    return;
                }
            } else if (state == 1) {

                f11477f0 = "OUT OF SERVICE";
                textView = f11480i0;
                if (textView == null) {
                    return;
                }
            } else if (state == 2) {
                f11477f0 = "EMERGENCY ONLY";
                textView = f11480i0;
                if (textView == null) {
                    return;
                }
            } else if (state != 3) {

                f11477f0 = "Unknown";
                textView = f11480i0;
                if (textView == null) {
                    return;
                }
            } else {
                f11477f0 = "POWER OFF";
                textView = f11480i0;
                if (textView == null) {
                    return;
                }
            }
            textView.setText(f11477f0);
        }
    }

    public String mo11253c(int i) {
        int numberOfCameras = Camera.getNumberOfCameras();
        long j = -1;
        float f = -1.0f;
        for (int i2 = 0; i2 < numberOfCameras; i2++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i2, cameraInfo);
            if (cameraInfo.facing == 0) {
                Camera open = Camera.open(i2);
                Camera.Parameters parameters = open.getParameters();
                long j2 = j;
                float f2 = f;
                for (int i3 = 0; i3 < parameters.getSupportedPictureSizes().size(); i3++) {
                    long j3 = (long) (((Camera.Size) parameters.getSupportedPictureSizes().get(i3)).width * ((Camera.Size) parameters.getSupportedPictureSizes().get(i3)).height);
                    if (j3 > j2) {
                        f2 = ((float) j3) / 1024000.0f;
                        j2 = j3;
                    }
                }
                open.release();
                f = f2;
                j = j2;
            }
        }
        return String.valueOf(f);
    }


    public final String mo11254d(int i) {
        int numberOfCameras = Camera.getNumberOfCameras();
        long j = -1;
        float f = -1.0f;
        for (int i2 = 0; i2 < numberOfCameras; i2++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i2, cameraInfo);
            if (cameraInfo.facing == 1) {
                Camera open = Camera.open(i2);
                Camera.Parameters parameters = open.getParameters();
                long j2 = j;
                float f2 = f;
                for (int i3 = 0; i3 < parameters.getSupportedPictureSizes().size(); i3++) {
                    long j3 = (long) (((Camera.Size) parameters.getSupportedPictureSizes().get(i3)).width * ((Camera.Size) parameters.getSupportedPictureSizes().get(i3)).height);
                    if (j3 > j2) {
                        f2 = ((float) j3) / 1024000.0f;
                        j2 = j3;
                    }
                }
                open.release();
                f = f2;
                j = j2;
            }
        }
        return String.valueOf(f);
    }
}
