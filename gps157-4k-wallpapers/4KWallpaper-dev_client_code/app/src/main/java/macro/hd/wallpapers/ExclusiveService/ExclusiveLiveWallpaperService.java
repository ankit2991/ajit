package macro.hd.wallpapers.ExclusiveService;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.ExclusiveService.sensor.RotationSensor;
import macro.hd.wallpapers.Utilily.Logger;

public class ExclusiveLiveWallpaperService extends GLWallpaperService {
    public static final int SENSOR_RATE = 60;
    String TAG = "ExclusiveLiveWallpaperService";
    public static int SpeedMaxValue = 31;
    public static int RangeValue = 20;
    @Override
    public Engine onCreateEngine() {
        return new MyEngine();
    }

    private class MyEngine extends GLEngine implements ExclusiveLiveWallpaperRenderer.Callbacks,
            SharedPreferences.OnSharedPreferenceChangeListener, RotationSensor.Callback {
        private SharedPreferences preference;
        private ExclusiveLiveWallpaperRenderer renderer;
        private RotationSensor rotationSensor;
        private BroadcastReceiver powerSaverChangeReceiver;
        private boolean pauseInSavePowerMode = false;
        private boolean savePowerMode = false;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setEGLContextClientVersion(2);
            setEGLConfigChooser(8, 8, 8, 0, 0, 0);
            renderer = new ExclusiveLiveWallpaperRenderer(ExclusiveLiveWallpaperService.this.getApplicationContext
                    (), this);

            if(isPreview()){
                SettingStore preferenceStore=SettingStore.getInstance(ExclusiveLiveWallpaperService.this);
                renderer.setTempPath(preferenceStore.getExclusiveWallpaperPathTemp());
            }

            setRenderer(renderer);
            setRenderMode(RENDERMODE_WHEN_DIRTY);
            rotationSensor = new RotationSensor(ExclusiveLiveWallpaperService.this.getApplicationContext()
                    , this, SENSOR_RATE);
            preference = PreferenceManager.getDefaultSharedPreferences(ExclusiveLiveWallpaperService.this);
            //preference.registerOnSharedPreferenceChangeListener(this);
            SharedPreferences.Editor editor = preference.edit();
            editor.putBoolean("power_saver", false);
            editor.putInt("default_picture", 1);
            editor.putInt("range", RangeValue);
            editor.putInt("delay", SpeedMaxValue-1);
            editor.putBoolean("scroll", false);
            editor.apply();
            renderer.setBiasRange(preference.getInt("range", 10));
            renderer.setDelay(SpeedMaxValue - preference.getInt("delay", 10));
            renderer.setScrollMode(preference.getBoolean("scroll", true));
            renderer.setIsDefaultWallpaper(preference.getInt("default_picture", 0) == 0);
            setPowerSaverEnabled(preference.getBoolean("power_saver", true));


        }

        @Override
        public void requestRender() {
            super.requestRender();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case "range":
                    renderer.setBiasRange(sharedPreferences.getInt(key, 10));
                    break;
                case "delay":
                    renderer.setDelay(SpeedMaxValue - sharedPreferences.getInt(key, 10));
                    break;
                case "scroll":
                    renderer.setScrollMode(sharedPreferences.getBoolean(key, true));
                    break;
                case "power_saver":
                    setPowerSaverEnabled(sharedPreferences.getBoolean(key, true));
                    break;
                case "default_picture":
                    renderer.setIsDefaultWallpaper(sharedPreferences.getInt(key, 0) == 0);
            }
        }

        @Override
        public void onSensorChanged(float[] angle) {
            if (getResources().getConfiguration().orientation == Configuration
                    .ORIENTATION_LANDSCAPE)
                renderer.setOrientationAngle(angle[1], angle[2]);
            else renderer.setOrientationAngle(-angle[2], angle[1]);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {

            Logger.e(TAG,"ispreview:"+isPreview());
            if (!pauseInSavePowerMode || !savePowerMode) {
                if (visible) {
                    renderer.setIsDefaultWallpaper(false);
                    rotationSensor.register();
                    renderer.startTransition();
                } else {
                    rotationSensor.unregister();
                    renderer.stopTransition();
                }
            } else {
                if (visible) {
                    renderer.startTransition();
                } else {
                    renderer.stopTransition();
                }
            }
        }

        void setPowerSaverEnabled(boolean enabled) {
            if (pauseInSavePowerMode == enabled) return;
            pauseInSavePowerMode = enabled;
            if (Build.VERSION.SDK_INT >= 21) {
                final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pauseInSavePowerMode) {
                    powerSaverChangeReceiver = new BroadcastReceiver() {
                        @TargetApi(21)
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            savePowerMode = pm.isPowerSaveMode();
                            if (savePowerMode && isVisible()) {
                                rotationSensor.unregister();
                                renderer.setOrientationAngle(0, 0);
                            } else if (!savePowerMode && isVisible()) {
                                rotationSensor.register();
                            }
                        }
                    };

                    IntentFilter filter = new IntentFilter();
                    filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
                    registerReceiver(powerSaverChangeReceiver, filter);
                    savePowerMode = pm.isPowerSaveMode();
                    if (savePowerMode && isVisible()) {
                        rotationSensor.unregister();
                        renderer.setOrientationAngle(0, 0);
                    }
                } else {
                    unregisterReceiver(powerSaverChangeReceiver);
                    savePowerMode = pm.isPowerSaveMode();
                    if (savePowerMode && isVisible()) {
                        rotationSensor.register();
                    }

                }
            }
        }


        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                                     float xOffsetStep, float yOffsetStep, int xPixelOffset,
                                     int yPixelOffset) {
            if (!isPreview()) {
                renderer.setOffset(xOffset, yOffset);
                renderer.setOffsetStep(xOffsetStep, yOffsetStep);
                Log.i(TAG, xOffset + ", " + yOffset + ", " + xOffsetStep + ", " + yOffsetStep);
            }
        }



        @Override
        public void onDestroy() {
            // Unregister this as listener
            rotationSensor.unregister();
            if (Build.VERSION.SDK_INT >= 21) {
                if (powerSaverChangeReceiver != null && pauseInSavePowerMode) {
                    try {
                        unregisterReceiver(powerSaverChangeReceiver);
                    } catch (Exception e) {
                    }
                }
            }
            //preference.unregisterOnSharedPreferenceChangeListener(this);
            // Kill renderer
            if (renderer != null) {
                renderer.release(); // assuming yours has this method - it
                // should!
            }
            super.onDestroy();
        }
    }

}
