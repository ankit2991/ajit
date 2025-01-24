package macro.hd.wallpapers.Interface.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentManager;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Interface.Fragments.WallpaperDetailFragment;

public class WallpaperDetailActivity extends BaseBannerActivity {
    WallpaperDetailFragment photoDetailActivity;
//    FragmentManager mFragmentManager;
//    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
       // CommonFunctions.setOverlayAction(this);
//        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_detail);
        CommonFunctions.updateWindow(WallpaperDetailActivity.this);
//        toolbar = (Toolbar) findViewById(R.id.app_bar);
//        setSupportActionBar(toolbar);
//
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle(getString(R.string.app_name));

        FragmentManager mFragmentManager = getSupportFragmentManager();

//        if(savedInstanceState==null){
            photoDetailActivity = new WallpaperDetailFragment();
            photoDetailActivity.setArguments(getIntent().getExtras());

            mFragmentManager.beginTransaction().
                    add(R.id.frame_layout, photoDetailActivity).disallowAddToBackStack().
                    commit();
//        }

        WallpapersApplication.getApplication().ratingCounter++;

        FrameLayout AdContainer1= (FrameLayout) findViewById(R.id.AdContainer1);
        requestBanner(this,AdContainer1,true,false);
    }


    @Override
    public void onBackPressed() {
        if(!isFinishing() && WallpapersApplication.isAppRunning){
            if(!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getFb_ad_download()) && WallpapersApplication.getApplication().getSettings().getFb_ad_download().equalsIgnoreCase("1")) {
                if (WallpapersApplication.getApplication().isGGAdLoadedDownload(this)) {
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.e("onDestroy","onDestroy");
//        try {
//            mFragmentManager.beginTransaction().
//                    remove(photoDetailActivity).
//                    commit();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mFragmentManager=null;
        photoDetailActivity=null;
        WallpapersApplication.getApplication().activity_download=null;
        try {
            WallpapersApplication.getApplication().isAdDisplayBefore=false;

//            if(WallpapersApplication.isAppRunning){
//                if(WallpapersApplication.getApplication().getSettings().isDownloadAdnormal()){
//                    if(!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getFb_ad_download()) && WallpapersApplication.getApplication().getSettings().getFb_ad_download().equalsIgnoreCase("1")) {
//                        if (WallpapersApplication.getApplication().isGGAdLoadedDownload()) {
//                        }
//                    }
//                }else{
//                    if(!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getFb_ad_download()) && WallpapersApplication.getApplication().getSettings().getFb_ad_download().equalsIgnoreCase("1")) {
//                        if (WallpapersApplication.getApplication().isFBAdLoaded()) {
//                        }
//                    }
//                }
//            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onDestroy();
        }
//        WallpapersApplication.getApplication().isFBDownloadLoaded=false;
    }
}
