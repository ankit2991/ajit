package macro.hd.wallpapers.Interface.Activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Interface.Fragments.UserDownloadFragment;

public class MyDownloadActivity extends BaseBannerActivity {

    UserDownloadFragment photoDetailActivity;
//    FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setLocale();
        setContentView(R.layout.activity_category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(getString(R.string.txtmydownload));


        FragmentManager mFragmentManager = getSupportFragmentManager();

        photoDetailActivity = new UserDownloadFragment();
        photoDetailActivity.setArguments(getIntent().getExtras());

        mFragmentManager.beginTransaction().
                add(R.id.frame_layout, photoDetailActivity).disallowAddToBackStack().
                commit();

//        FrameLayout AdContainer1= (FrameLayout) findViewById(R.id.AdContainer1);
//        JesusApplication.getApplication().requestBanner(this,AdContainer1);
    }

    private boolean isAlreadyDisplay;
    public void displayBannerAd(){
        if(!isAlreadyDisplay){
            isAlreadyDisplay=true;
            FrameLayout AdContainer1= (FrameLayout) findViewById(R.id.AdContainer1);
            requestBanner(this,AdContainer1,false,false);
        }
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

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onPause(this);
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onResume(this);
        }
    }



    @Override
    protected void onDestroy() {

        super.onDestroy();
//        try {
//            mFragmentManager.beginTransaction().
//                    remove(photoDetailActivity).
//                    commit();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onDestroy();
        }
//        mFragmentManager=null;
        photoDetailActivity=null;
    }
}
