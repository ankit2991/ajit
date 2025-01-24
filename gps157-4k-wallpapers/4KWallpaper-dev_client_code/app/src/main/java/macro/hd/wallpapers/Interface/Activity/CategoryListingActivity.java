package macro.hd.wallpapers.Interface.Activity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentManager;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Interface.Fragments.ParentHomeFragment;

public class CategoryListingActivity extends BaseBannerActivity {

    ParentHomeFragment photoDetailActivity;
    public static boolean isShowSimilar=true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setLocale();
        setContentView(R.layout.activity_category);
        CategoryListingActivity.isShowSimilar=false;
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        String category_name = null;
        if(getIntent().getExtras()!=null){
            category_name=getIntent().getExtras().getString("category_name");
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(TextUtils.isEmpty(category_name))
            getSupportActionBar().setTitle(getString(R.string.app_name));
        else
            getSupportActionBar().setTitle(category_name);

        FragmentManager mFragmentManager = getSupportFragmentManager();

        photoDetailActivity = new ParentHomeFragment();
        photoDetailActivity.setArguments(getIntent().getExtras());

        mFragmentManager.beginTransaction().
                add(R.id.frame_layout, photoDetailActivity).disallowAddToBackStack().
                commit();

        WallpapersApplication.getApplication().ratingCounter++;
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
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onDestroy();
        }
        photoDetailActivity=null;
        CategoryListingActivity.isShowSimilar=true;
    }
}
