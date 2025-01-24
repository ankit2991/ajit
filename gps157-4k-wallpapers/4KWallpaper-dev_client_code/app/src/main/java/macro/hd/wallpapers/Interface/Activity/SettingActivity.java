package macro.hd.wallpapers.Interface.Activity;

import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.judemanutd.autostarter.AutoStartPermissionHelper;

public class SettingActivity extends BaseActivity {

    private SettingStore settingStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setLocale();
        setContentView(R.layout.activity_setting);

        settingStore = SettingStore.getInstance(this);

        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.settings_label));

        try {
            boolean isAuto = AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(this);
            if (isAuto)
                findViewById(R.id.ll_notwork).setVisibility(View.VISIBLE);
            else
                findViewById(R.id.ll_notwork).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        findViewById(R.id.ll_feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_FEEDBACK, EventManager.ATR_VALUE_FEEDBACK);
//							JesusApplication.getApplication().incrementUserClickCounter();
                PackageInfo pInfo = null;
                try {
                    pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                final String version = pInfo.versionName;
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConstant.EMAIL});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback Version - v" + version);
                String temp = settingStore.getUserID();
                if (TextUtils.isEmpty(temp)) {
                    temp = CommonFunctions.getHardwareId(SettingActivity.this);
                }
                temp += "\nDevice: " + CommonFunctions.getDeviceName();
                String user_id = "Support ID: " + temp + "\n\nDescribe your problem:";
                intent.putExtra(Intent.EXTRA_TEXT, "" + user_id);
                startActivity(intent);
            }
        });
        findViewById(R.id.ll_about_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_ABOUT, EventManager.ATR_VALUE_ABOUT);
//                            Intent intent1=new Intent(MainBottomNavigationActivity.this,DownloadFilesActivity.class);
//                            startActivity(intent1);

//							JesusApplication.getApplication().incrementUserClickCounter();
                Intent intent = new Intent(SettingActivity.this, AboutUsActivity.class);
                startActivity(intent);

            }
        });

        findViewById(R.id.ll_apptour).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, ApplicationIntroActivity.class);

                intent.putExtra(ApplicationIntroActivity.EXTRA_FULLSCREEN, false);
                intent.putExtra(ApplicationIntroActivity.EXTRA_SCROLLABLE, false);
                intent.putExtra(ApplicationIntroActivity.EXTRA_CUSTOM_FRAGMENTS,
                        true);
                intent.putExtra(ApplicationIntroActivity.EXTRA_PERMISSIONS, false);
                intent.putExtra(ApplicationIntroActivity.EXTRA_SKIP_ENABLED, true);
                intent.putExtra(ApplicationIntroActivity.EXTRA_SHOW_BACK, true);
                intent.putExtra(ApplicationIntroActivity.EXTRA_SHOW_NEXT, true);
                intent.putExtra(ApplicationIntroActivity.EXTRA_FINISH_ENABLED,
                        true);
                intent.putExtra(ApplicationIntroActivity.EXTRA_GET_STARTED_ENABLED,
                        false);
                startActivity(intent);
            }
        });
        findViewById(R.id.ll_privacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonFunctions.isNetworkAvailable(SettingActivity.this)) {
                    CommonFunctions.showInternetDialog(SettingActivity.this);
                    return;
                }
                EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_TERMS, EventManager.ATR_VALUE_TERMS);
                Intent intent = new Intent(SettingActivity.this, Termsactivity.class);
                intent.putExtra("privacy", true);
                startActivity(intent);

            }
        });
        findViewById(R.id.ll_notwork).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_LIVE_WALL_WORKING, EventManager.ATR_VALUE_LIVE_WALL_WORKING);
                showAlertDialogButtonClicked();
            }
        });

        findViewById(R.id.ll_cc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearCacheDialog();
            }
        });

        Switch push_switch = findViewById(R.id.check_push_notification);
        if (settingStore.isPushEnable())
            push_switch.setChecked(true);
        else
            push_switch.setChecked(false);
        push_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingStore.setIsPushEnable(isChecked);
                if (isChecked)
                    EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_PUSH_NOTIFICATION, "Yes");
                else
                    EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_PUSH_NOTIFICATION, "No");
            }
        });
    }

    private void showClearCacheDialog() {
        AlertDialog.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean isDarkTheme = false;
            if (settingStore.getTheme() == 0) {
                isDarkTheme = false;
            } else if (settingStore.getTheme() == 1) {
                isDarkTheme = true;
            }
            if (isDarkTheme)
                builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
            else
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);

        } else {
            builder = new AlertDialog.Builder(this);
        }


        builder.setTitle(getResources().getString(R.string.label_info_new));
        builder.setMessage(getResources().getString(R.string.msg_cc));
        // set the custom layout
//        final View customLayout = getLayoutInflater().inflate(R.layout.layout_clear_cache, null);
//        builder.setView(customLayout);
        // add a button
        builder.setPositiveButton(getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                dialog.dismiss();
                clearWallpaperCache();
            }
        });

        builder.setNeutralButton(getResources().getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();

        if (!isFinishing()) {
            dialog.show();
        }
    }

    private void clearWallpaperCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(SettingActivity.this).clearDiskCache();
            }
        }).start();

        Toast.makeText(SettingActivity.this, "Cache Cleared.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAlertDialogButtonClicked() {
        // create an alert builder

        AlertDialog.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean isDarkTheme = false;
            if (settingStore.getTheme() == 0) {
                isDarkTheme = false;
            } else if (settingStore.getTheme() == 1) {
                isDarkTheme = true;
            }
            if (isDarkTheme)
                builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
            else
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);

        } else {
            builder = new AlertDialog.Builder(this);
        }


        builder.setTitle(getResources().getString(R.string.label_not_work_title));
        builder.setMessage(getResources().getString(R.string.not_working));
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_dlg_layout, null);
        builder.setView(customLayout);
        // add a button
        builder.setPositiveButton(getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                dialog.dismiss();
            }
        });

        builder.setNeutralButton(getResources().getString(R.string.label_more_detail), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // send data from the AlertDialog to the Activity
                final Intent i = new Intent(SettingActivity.this, Termsactivity.class);
                i.putExtra("help", true);
                startActivity(i);
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();

        if (!isFinishing()) {
            dialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        tv_clear_cache=null;
        settingStore = null;
    }

}
