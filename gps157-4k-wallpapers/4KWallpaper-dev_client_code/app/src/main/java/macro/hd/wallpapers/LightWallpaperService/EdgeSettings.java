package macro.hd.wallpapers.LightWallpaperService;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Interface.Activity.BaseBannerActivity;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.WallpapersApplication;

public class EdgeSettings extends BaseBannerActivity {
    
    public TextView txt_review;
    public SharedPreferences sharedPreferences;
    public TextView showImageSettings;
    public TextView showNotchSettings;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme();
        setLocale();
        setContentView(R.layout.edge_settings);
        setUpDefault();

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.txtedgesetting));

        showNotchSettings = (TextView) findViewById(R.id.showNotchSettings);
        showImageSettings = (TextView) findViewById(R.id.showImageSettings);
        txt_review = (TextView) findViewById(R.id.txt_review);
        showImageSettings.setVisibility(View.GONE);
        showNotchSettings.setVisibility(View.GONE);

        txt_review.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                WallpapersApplication.getApplication().ratingCounter++;
                if (isAlreadySet) {
                    WallpaperManager instance = WallpaperManager.getInstance(EdgeSettings.this);
                    if (instance.getWallpaperInfo() != null && instance.getWallpaperInfo().getPackageName().equals(getPackageName())) {
                        try {
                            instance.clear();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        updateButton();
                    }
                } else {
                    EventManager.sendEvent(EventManager.LBL_EDGE_WALLPAPER, EventManager.ATR_EDGE_Preview,"Click Preview");
                    try {
                        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(EdgeSettings.this, EdgeWallpaperService.class));
                        startActivityForResult(intent,200);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            startActivityForResult(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new
                                    ComponentName(EdgeSettings.this, EdgeWallpaperService.class))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 200);
                        } catch (Exception e2) {
                            Toast.makeText(EdgeSettings.this, R.string
                                    .toast_failed_launch_wallpaper_chooser, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

        sharedPreferences = getSharedPreferences("borderlightwall", 0);
        setSeekValue(R.id.seekBarSpeed, "cyclespeed");
        setSeekValue(R.id.seekBarBorderSize, "bordersize");
        setSeekValue(R.id.seekBarRadiusBottom, "radiusbottom");
        setSeekValue(R.id.seekBarRadiusTop, "radiustop");

        SwitchCompat switchR = (SwitchCompat) findViewById(R.id.switchNotch);
        switchR.setChecked(sharedPreferences.getBoolean("enablenotch", false));
        switchR.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    showNotchSettings.setVisibility(View.VISIBLE);
                } else {
                    showNotchSettings.setVisibility(View.GONE);
                }
                sharedPreferences.edit().putBoolean("enablenotch", isChecked).apply();
            }
        });
        if (switchR.isChecked()) {
            showNotchSettings.setVisibility(View.VISIBLE);
        } else {
            showNotchSettings.setVisibility(View.GONE);
        }


        SwitchCompat switchR2 = (SwitchCompat) findViewById(R.id.switchImage);

        switchR2.setChecked(sharedPreferences.getBoolean("enableimage", false));
        switchR2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    showImageSettings.setVisibility(View.VISIBLE);
                } else {
                    showImageSettings.setVisibility(View.GONE);
                }
                if (!isChecked || new ImageUtility().m2888b(EdgeSettings.this)) {
                    sharedPreferences.edit().putBoolean("enableimage", isChecked).apply();
                }
            }
        });
        if (switchR2.isChecked()) {
            showImageSettings.setVisibility(View.VISIBLE);
        } else {
            showImageSettings.setVisibility(View.GONE);
        }
        showNotchSettings.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(EdgeSettings.this, Edge_NotchSetings.class));
            }
        });
        showImageSettings.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                startActivity(new Intent(EdgeSettings.this, Edge_ImgSettings.class));
            }
        });
        ((SwitchCompat) findViewById(R.id.switchImage)).setChecked(sharedPreferences.getBoolean("enableimage", false));

        LinearLayout linearLayout=findViewById(R.id.ll_border);
        TextView showNotchSettings=findViewById(R.id.showNotchSettings);
        TextView showImageSettings=findViewById(R.id.showImageSettings);
        SettingStore settingStore=SettingStore.getInstance(this);
        if(settingStore.getTheme()==0) {
            linearLayout.setBackgroundResource(R.drawable.settings_border);
            showNotchSettings.setBackgroundResource(R.drawable.settings_border1);
            showImageSettings.setBackgroundResource(R.drawable.settings_border1);
        }else {
            linearLayout.setBackgroundResource(R.drawable.settings_border_dark);
            showNotchSettings.setBackgroundResource(R.drawable.settings_border1_dark);
            showImageSettings.setBackgroundResource(R.drawable.settings_border1_dark);
        }

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioBorder);
        radioGroup.clearCheck();

        boolean is_dash = sharedPreferences.getBoolean("is_dash", false);
        if(is_dash)
            ((RadioButton)findViewById(R.id.radioDash)).setChecked(true);
        else
            ((RadioButton)findViewById(R.id.radioline)).setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (R.id.radioline==checkedId) {
                    sharedPreferences.edit().putBoolean("is_dash", false).apply();
                }else if (R.id.radioDash==checkedId) {
                    sharedPreferences.edit().putBoolean("is_dash", true).apply();
                }

            }
        });

        FrameLayout AdContainer1= (FrameLayout) findViewById(R.id.AdContainer1);
        requestBanner(this,AdContainer1,false,false);
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onPause(this);
        }
        super.onPause();
    }


    private void setUpDefault(){
        SettingStore settingStore=SettingStore.getInstance(this);
        if(!settingStore.getEdgeSetDafualt()){
            settingStore.setEdgeSetDafualt(true);
            SharedPreferences.Editor edit = getSharedPreferences("borderlightwall", 0).edit();
            edit.putInt("cyclespeed", 10);
            edit.putInt("bordersize", 20);
            edit.putInt("bordersizelockscreen", 20);
            edit.putInt("radiusbottom", 76);
            edit.putInt("radiustop", 96);
            edit.putBoolean("enablenotch", false);
            edit.putBoolean("enableimage", false);
            edit.putInt("notchwidth", 158);
            edit.putInt("notchheight", 80);
            edit.putInt("notchradiustop", 28);
            edit.putInt("notchradiusbottom", 50);
            edit.putInt("notchfullnessbottom", 82);
            edit.putInt("imagevisibilitylocked", 100);
            edit.putInt("imagevisibilityunlocked", 40);
            edit.putInt("imagedesaturationlocked", 0);
            edit.putInt("imagedesaturationunlocked", 50);
            edit.putBoolean("is_dash", false).apply();
            edit.apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onResume(this);
        }
        updateButton();
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

    private void setSeekValue(int id, final String str) {
        SeekBar seekBar = (SeekBar) findViewById(id);
        seekBar.setProgress(sharedPreferences.getInt(str, 0));
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean z) {
                sharedPreferences.edit().putInt(str, progress).apply();
            }
        });
    }

    boolean isAlreadySet = false;
    WallpaperInfo old_info;
    private void updateButton(){

        try {
            WallpaperManager wpm = WallpaperManager.getInstance(this);
            old_info = wpm.getWallpaperInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Error e) {
            e.printStackTrace();
        }

        if (old_info != null && old_info.getComponent().getClassName().equals(EdgeWallpaperService.class.getCanonicalName())) {
            isAlreadySet=true;
            txt_review.setText(getResources().getString(R.string.rem_wallpaper));
        }else {
            txt_review.setText(getResources().getString(R.string.pre_set));
            isAlreadySet=false;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.e("onActivityResult","resultCode:"+resultCode+" requestCode:"+requestCode);
        if(resultCode==RESULT_OK){
            EventManager.sendEvent(EventManager.LBL_EDGE_WALLPAPER, EventManager.ATR_EDGE_SET,"Set Edge");
        } else if(resultCode==RESULT_CANCELED) {
                WallpaperManager wpm = WallpaperManager.getInstance(this);
                WallpaperInfo info = wpm.getWallpaperInfo();
                if(info == null)
                    return;
                if (info != null && (old_info == null || (!old_info.getComponent().getClassName().equals(info.getComponent().getClassName())
                        && info.getComponent().getClassName().equals(EdgeWallpaperService.class.getCanonicalName())))) {
                    EventManager.sendEvent(EventManager.LBL_EDGE_WALLPAPER, EventManager.ATR_EDGE_SET,"Set Edge");
                }

        }
        // -1 success 0 failed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onDestroy();
        }
        txt_review=null;
        sharedPreferences=null;
        showImageSettings=null;
        showNotchSettings=null;
        old_info=null;

    }
}
