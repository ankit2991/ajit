package macro.hd.wallpapers.Interface.Fragments.IntroFagment;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import macro.hd.wallpapers.ExclusiveService.ExclusiveLiveWallpaperRenderer;
import macro.hd.wallpapers.ExclusiveService.sensor.RotationSensor;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.Logger;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import static macro.hd.wallpapers.ExclusiveService.GLWallpaperService.GLEngine.RENDERMODE_WHEN_DIRTY;
import static macro.hd.wallpapers.Interface.Fragments.ExclusiveDetailFragment.RangeValue;
import static macro.hd.wallpapers.Interface.Fragments.ExclusiveDetailFragment.SENSOR_RATE;
import static macro.hd.wallpapers.Interface.Fragments.ExclusiveDetailFragment.SpeedMaxValue;

public class FragmentIntro4 extends SlideFragment implements RotationSensor.Callback,ExclusiveLiveWallpaperRenderer.Callbacks{

    protected static final String TAG = FragmentIntro4.class.getSimpleName();
    public FragmentIntro4() {
    }

    public static FragmentIntro4 newInstance() {
        return new FragmentIntro4();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View rootView = inflater.inflate(R.layout.fragment_intro4, container,
                false);
        return rootView;
    }

    View rootView;

    ImageView thumbNail;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView=view;

        CardView card_view=view.findViewById(R.id.card_view);

        try {
            Point windowDimensions = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(windowDimensions);
            Logger.e("DoubleFragment","y:"+windowDimensions.y + " X:"+windowDimensions.x);
            int itemHeight = Math.round(windowDimensions.y * 0.54f);
            int itemWidht = Math.round(windowDimensions.x * 0.56f);

            card_view.getLayoutParams().height = itemHeight;
            card_view.getLayoutParams().width = itemWidht;
        } catch (Exception e) {
            e.printStackTrace();
            card_view.getLayoutParams().width = (int) getResources().getDimension(R.dimen.intro_witdh);
            card_view.getLayoutParams().height = (int) getResources().getDimension(R.dimen.intro_hieght);
        }

        thumbNail = (ImageView) rootView
                .findViewById(R.id.img_banner);

        playVideo();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "onDestroy");

        rootView = null;

        thumbNail = null;

        try {

            rotationSensor.unregister();
            if (Build.VERSION.SDK_INT >= 21) {
                if (powerSaverChangeReceiver != null && pauseInSavePowerMode) {
                    try {
                        getActivity().unregisterReceiver(powerSaverChangeReceiver);
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

            if(glSurfaceView!=null)
                glSurfaceView.setRenderer(null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        glSurfaceView=null;
        rotationSensor=null;
        preference=null;
        renderer=null;
        powerSaverChangeReceiver=null;
    }


    private ExclusiveLiveWallpaperRenderer renderer;
    private RotationSensor rotationSensor;
    private SharedPreferences preference;
    GLSurfaceView glSurfaceView ;
    private void playVideo() {
        try {

            glSurfaceView = new GLSurfaceView(getActivity());

            thumbNail.setVisibility(View.GONE);

             String dst = "file:///android_asset/Exclusive1.jpg";

            glSurfaceView.setEGLContextClientVersion(2);
            glSurfaceView.setEGLConfigChooser(8, 8, 8, 0, 0, 0);
            renderer = new ExclusiveLiveWallpaperRenderer(getActivity().getApplicationContext
                    (), this);
            renderer.setTempPath(dst);
            glSurfaceView.setRenderer(renderer);
            glSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
            rotationSensor = new RotationSensor(getActivity().getApplicationContext()
                    , this, SENSOR_RATE);


            preference = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
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

            RelativeLayout relativeLayout=rootView.findViewById(R.id.rl_photos);
            relativeLayout.addView(glSurfaceView);

            setPowerSaverEnabled(preference.getBoolean("power_saver", true));

            onVisibilityChanged(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private BroadcastReceiver powerSaverChangeReceiver;
    private boolean pauseInSavePowerMode = false;
    private boolean savePowerMode = false;
    void setPowerSaverEnabled(boolean enabled) {
        pauseInSavePowerMode = enabled;
        if (Build.VERSION.SDK_INT >= 21) {
            final PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
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
                getActivity().registerReceiver(powerSaverChangeReceiver, filter);
                savePowerMode = pm.isPowerSaveMode();
                if (savePowerMode && isVisible()) {
                    rotationSensor.unregister();
                    renderer.setOrientationAngle(0, 0);
                }
            } else {
                if(powerSaverChangeReceiver!=null)
                    getActivity().unregisterReceiver(powerSaverChangeReceiver);
                savePowerMode = pm.isPowerSaveMode();
//                if (savePowerMode && isVisible()) {
                rotationSensor.register();
//                }

            }
        }
    }


    @Override
    public void onSensorChanged(float[] angle) {
//        Logger.e(TAG, "onSensorChanged:");
        if (getResources().getConfiguration().orientation == Configuration
                .ORIENTATION_LANDSCAPE)
            renderer.setOrientationAngle(angle[1], angle[2]);
        else renderer.setOrientationAngle(-angle[2], angle[1]);
    }

    @Override
    public void requestRender() {
//        super.requestRender();
        if(glSurfaceView!=null)
            glSurfaceView.requestRender();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(glSurfaceView!=null)
            glSurfaceView.onResume();
        onVisibilityChanged(true);
    }

    public void onVisibilityChanged(boolean visible) {
        if(rotationSensor==null)
            return;
        if (!pauseInSavePowerMode || !savePowerMode) {
            if (visible) {
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

    @Override
    public void onPause() {
        super.onPause();
        if(glSurfaceView!=null)
            glSurfaceView.onPause();
        onVisibilityChanged(false);
    }

}
