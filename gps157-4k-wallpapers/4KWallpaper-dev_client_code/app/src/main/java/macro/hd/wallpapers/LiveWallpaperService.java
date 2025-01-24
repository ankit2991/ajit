package macro.hd.wallpapers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.view.SurfaceHolder;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.Logger;

public class LiveWallpaperService extends WallpaperService {

    String path;
    public static LiveWallpaperService videoWallpaperService;
    public LoveEngine engine;

    @Override
    public Engine onCreateEngine() {
//        if(engine!=null) {
////            engine.onSurfaceCreated(engine.getSurfaceHolder());
//            engine.onSurfaceRedrawNeeded(engine.getSurfaceHolder());
//            return engine;
//        }
        engine=new LoveEngine();
        return engine;
    }

    SettingStore settingStore;

    @Override
    public void onCreate() {
        super.onCreate();
        videoWallpaperService=this;
        settingStore = SettingStore.getInstance(this);
        path = settingStore.getVideoLiveWallpaperPath();
        Logger.e("VideoWallpaperService", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.e("VideoWallpaperService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private class LoveEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        // A reference to our shared prefs;
        private SharedPreferences mPreferences;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            Logger.e("VideoWallpaperService", "onCreate:" );

            mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            // Register an OnSharedPreferenceChangeListener to our shared prefs
            mPreferences.registerOnSharedPreferenceChangeListener(this);

            // Your existing code ...
        }

        public LoveEngine() {
            super();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            isPreview=isPreview();
            if (visible) {
//                if(!isPreview()){
//                    stopSelf();
//                }
//                resetPlayer(getSurfaceHolder());
                if (mp != null) {
//                    resetPlayer(null);

                    if(!isPreview /*&& !TextUtils.isEmpty(VideoLiveDetailFragment.dst)*/) {
                        try {
                            if (mp != null) {
                                try {
                                    mp.stop();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                mp.reset();
                                mp.release();
                                mp = null;
                                engine=null;

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        resetPlayer(null);
                    }else {
                        if(mp!=null && !mp.isPlaying())
                            mp.start();
                    }
                }else
                    resetPlayer(null);
            } else {
                if (mp != null && mp.isPlaying())
                    mp.pause();
//                else
//                    resetPlayer(null);
            }
            Logger.e("VideoWallpaperService", "onVisibilityChanged:" + visible+" isPreview():"+isPreview() +" mp:"+mp);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            // Whenever the user changes a setting, this method will be called.
            // So do what needs to be done here, like redrawing the wallpaper
            Logger.e("VideoWallpaperService", "onSharedPreferenceChanged:" + key);

            if(key.equalsIgnoreCase("video_wallpaper"))
            {

            }
        }

        private MediaPlayer mp;
        VideoSurfaceHolder videoSurfaceHolder;
        private boolean isPreview=false;
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            try {
                Logger.e("VideoWallpaperService", "onSurfaceCreated:" + mp + " path:" + path);
                Logger.e("VideoWallpaperService", "onSurfaceCreated: new path" + settingStore.getVideoLiveWallpaperPath());

                Logger.e("VideoWallpaperService", "isPreview:" + isPreview());
                isPreview=isPreview();

//                if(!isPreview){
//                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
//                    try {
//                        wallpaperManager.clear();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

                holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

                if (settingStore == null)
                    settingStore = SettingStore.getInstance(LiveWallpaperService.this);

                if(isPreview && !TextUtils.isEmpty(settingStore.getVideoLiveWallpaperPathTemp())){
                    path=settingStore.getVideoLiveWallpaperPathTemp();
                }else{
                    if (TextUtils.isEmpty(path) || !settingStore.getVideoLiveWallpaperPath().equalsIgnoreCase(path)) {
                        path = settingStore.getVideoLiveWallpaperPath();
                    }
                }

                Logger.e("VideoWallpaperService", "final" + path);

                videoSurfaceHolder = new VideoSurfaceHolder(holder);

                mp = MediaPlayer.create(getApplicationContext(), Uri.parse(path));
                mp.setDisplay(videoSurfaceHolder);
                mp.setLooping(true);
                mp.setVolume(0, 0);
                mp.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void resetPlayer(SurfaceHolder holder) {

            if (settingStore == null)
                settingStore = SettingStore.getInstance(LiveWallpaperService.this);
            try {
                if(isPreview && !TextUtils.isEmpty(settingStore.getVideoLiveWallpaperPathTemp())){
                    path=settingStore.getVideoLiveWallpaperPathTemp();
                }else
                    path = settingStore.getVideoLiveWallpaperPath();
                    if(TextUtils.isEmpty(path))
                        return;

                    mp = MediaPlayer.create(getApplicationContext(), Uri.parse(path));
                    mp.setDisplay(videoSurfaceHolder);
                    mp.setLooping(true);
                    mp.setVolume(0, 0);
                    mp.start();
                Logger.e("VideoWallpaperService", "resetPlayer");
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                                     int width, int height) {
            Logger.e("VideoWallpaperService", "onSurfaceChanged:"+" isPreview():"+isPreview());

//            if (!settingStore.getVideoLiveWallpaperPath().equalsIgnoreCase(path)) {

        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            if (mp != null) {
                try {
                    mp.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    mp.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    mp.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mp = null;
                engine=null;

            }
            Logger.e("VideoWallpaperService", "onSurfaceDestroyed");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mPreferences.unregisterOnSharedPreferenceChangeListener(this);
            Logger.e("VideoWallpaperService", "onDestroy");
        }
    }
}
