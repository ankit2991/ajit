package macro.hd.wallpapers.LightWallpaperService;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import androidx.appcompat.widget.Toolbar;

import macro.hd.wallpapers.Interface.Activity.BaseActivity;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;

public class Edge_NotchSetings extends BaseActivity {

    public SharedPreferences sharedPreferences;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme();
        setLocale();
        setContentView(R.layout.edge_notch_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.notch_setting));

        sharedPreferences = getSharedPreferences("borderlightwall", 0);
        setSeekValue(R.id.seekBarNotchWidth, "notchwidth");
        setSeekValue(R.id.seekBarNotchHeight, "notchheight");
        setSeekValue(R.id.seekBarNotchRadiusTop, "notchradiustop");
        setSeekValue(R.id.seekBarNotchRadiusBottom, "notchradiusbottom");
        setSeekValue(R.id.seekBarNotchFullness, "notchfullnessbottom");

        LinearLayout linearLayout=findViewById(R.id.ll_border);
        SettingStore settingStore=SettingStore.getInstance(this);
        if(settingStore.getTheme()==0)
            linearLayout.setBackgroundResource(R.drawable.settings_border);
        else
            linearLayout.setBackgroundResource(R.drawable.settings_border_dark);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences=null;
    }
}
