package callerid.truecaller.trackingnumber.phonenumbertracker.block.setting;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.TinyDB;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ads.MaxAdManager;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.SFun;

import com.adjust.sdk.Adjust;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.interfaces.adShowCallBack;


public class Audio_manager_activity extends AppCompatActivity {

    private static final int CODE_WRITE_SETTINGS_PERMISSION = 1001;
    public LinearLayout f12953Y;
    public SharedPreferences.Editor f12954Z;
    public int f12955a0;
    public LinearLayout f12956b0;
    public LinearLayout f12957c0;
    public SharedPreferences f12959e0;
    public CheckBox f12960f0;
    private LinearLayout btnyes, btnno;
    private boolean settingsCanWrite;
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

    @RequiresApi(api = Build.VERSION_CODES.M)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settingsCanWrite = Settings.System.canWrite(Audio_manager_activity.this);
        } else {
            settingsCanWrite = ContextCompat.checkSelfPermission(Audio_manager_activity.this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (!settingsCanWrite) {
            final Dialog updateDialog = new Dialog(this);
            updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            updateDialog.setCancelable(false);
            updateDialog.setContentView(R.layout.settingdialog);
            btnyes = updateDialog.findViewById(R.id.btnyes);
            btnno = updateDialog.findViewById(R.id.btnno);
            btnno.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateDialog.dismiss();
                    onBackPressed();
                }
            });
            btnyes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateDialog.dismiss();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + Audio_manager_activity.this.getPackageName()));
                        startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
                    } else {
                        ActivityCompat.requestPermissions(Audio_manager_activity.this, new String[]{Manifest.permission.WRITE_SETTINGS}, CODE_WRITE_SETTINGS_PERMISSION);
                    }
                }
            });
            updateDialog.show();
        }
    }


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_audio_manager_activity);
        sfun = new SFun(this);

        findViewById(R.id.btnback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        f12957c0 = (LinearLayout) findViewById(R.id.ringtone_select);
        f12956b0 = (LinearLayout) findViewById(R.id.notification_select);
        f12953Y = (LinearLayout) findViewById(R.id.alarm_select);
        String str = "vibrate_when_ringing";
        f12959e0 = getSharedPreferences(str, 0);
        f12954Z = f12959e0.edit();
        f12960f0 = (CheckBox) findViewById(R.id.virb_ringing);
        f12955a0 = Settings.System.getInt(getApplicationContext().getContentResolver(), str, 0);
        StringBuilder a = new StringBuilder().append("sys vibrate value ");
        a.append(f12955a0);
        int i = this.f12955a0;
        if (i == 0) {
            f12960f0.setChecked(false);
        } else if (i == 1) {
            f12960f0.setChecked(true);
        }
        f12960f0.setOnCheckedChangeListener(new C2534a());
        f12957c0.setOnClickListener(new C2535b());
        f12956b0.setOnClickListener(new C2536c());
        f12953Y.setOnClickListener(new C2538e());
    }

    public class C2534a implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            String str = "vibrate_when_ringing";
            f12954Z.putBoolean(str, z);
            f12954Z.commit();

            int i = f12955a0;
            if (i == 0) {
                Settings.System.putInt(getContentResolver(), str, 1);
            } else if (i == 1) {
                Settings.System.putInt(getContentResolver(), str, 0);
            }
        }
    }

    public class C2535b implements View.OnClickListener {
        public void onClick(View view) {
            Uri actualDefaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(Audio_manager_activity.this, 1);
            Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
            intent.putExtra("android.intent.extra.ringtone.TYPE", 1);
            intent.putExtra("android.intent.extra.ringtone.TITLE", "Select Ringtone");
            intent.putExtra("android.intent.extra.ringtone.EXISTING_URI", actualDefaultRingtoneUri);
            intent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", true);
            intent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", false);
            startActivityForResult(intent, (int) 1001, (Bundle) null);
        }
    }

    public class C2536c implements View.OnClickListener {
        public void onClick(View view) {
            Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
            intent.putExtra("android.intent.extra.ringtone.TYPE", 2);
            intent.putExtra("android.intent.extra.ringtone.TITLE", "Select Notification Tone");
            intent.putExtra("android.intent.extra.ringtone.EXISTING_URI", RingtoneManager.getActualDefaultRingtoneUri(Audio_manager_activity.this, 2));
            startActivityForResult(intent, 1002, (Bundle) null);
        }
    }


    public class C2538e implements View.OnClickListener {
        public void onClick(View view) {
            Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
            intent.putExtra("android.intent.extra.ringtone.TYPE", 4);
            intent.putExtra("android.intent.extra.ringtone.TITLE", "Select Alarm Tone");
            intent.putExtra("android.intent.extra.ringtone.EXISTING_URI", RingtoneManager.getActualDefaultRingtoneUri(Audio_manager_activity.this, 4));
            startActivityForResult(intent, 1003, (Bundle) null);
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        int i3;
        if (i2 == -1) {
            Uri uri = (Uri) intent.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
            switch (i) {
                case 1001 /*1001*/:
                    i3 = 1;
                    break;
                case 1002 /*1002*/:
                    i3 = 2;
                    break;
                case 1003:
                    i3 = 4;
                    break;
                default:
                    return;
            }
            RingtoneManager.setActualDefaultRingtoneUri(Audio_manager_activity.this, i3, uri);
        }
    }
}
