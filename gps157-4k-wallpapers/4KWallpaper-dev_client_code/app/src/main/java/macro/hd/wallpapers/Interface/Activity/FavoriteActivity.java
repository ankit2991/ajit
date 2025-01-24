package macro.hd.wallpapers.Interface.Activity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Interface.Fragments.MyFavFragment;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventState;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.IEventListener;
import macro.hd.wallpapers.notifier.ListenerPriority;
import macro.hd.wallpapers.notifier.NotifierFactory;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;


public class FavoriteActivity extends BaseBannerActivity implements IEventListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setLocale();
        setContentView(R.layout.activity_favorite);


        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.label_my_fav));

        if(MainNavigationActivity.isFavListCalled){
            findViewById(R.id.rl_progress).setVisibility(View.GONE);
            setupTabIcons();
        }else{
            findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
        }
        registerUserInfoListener();

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


    @Override
    public int eventNotify(int eventType, Object eventObject) {
        int eventState = EventState.EVENT_IGNORED;
        switch (eventType) {
            case EventTypes.EVENT_USER_FAV_LOADED:
                eventState = EventState.EVENT_PROCESSED;
                findViewById(R.id.rl_progress).setVisibility(View.GONE);
                setupTabIcons();
                break;

        }
        return eventState;
    }

    private void setupTabIcons() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText(getResources().getString(R.string.label_wal));
        tabLayout.getTabAt(1).setText(getResources().getString(R.string.title_material_bold));
        tabLayout.getTabAt(2).setText(getResources().getString(R.string.menu_exclusive));
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }


        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            return mFragmentTitleList.get(position);
            // return null to display only the icon
            return null;
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        viewPager.setOffscreenPageLimit(3);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        MyFavFragment wallPaperFragment = null;
        MyFavFragment liveWallFragment = null;
        MyFavFragment exclusiveWallFragment = null;

        Bundle b = new Bundle();
        if(wallPaperFragment==null) {
            wallPaperFragment = new MyFavFragment();
            wallPaperFragment.setArguments(b);
        }
        adapter.addFragment(wallPaperFragment, "Wallpaper");


        if(liveWallFragment==null) {
            liveWallFragment = new MyFavFragment();
            b = new Bundle();
            b.putBoolean("isVideoWall", true);
            liveWallFragment.setArguments(b);
        }

        if(exclusiveWallFragment==null) {
            exclusiveWallFragment = new MyFavFragment();
            b = new Bundle();
            b.putBoolean("isVideoWall", false);
            b.putBoolean("isExclusiveWall", true);
            exclusiveWallFragment.setArguments(b);
        }

        adapter.addFragment(liveWallFragment, "Live Wallpaper");
        adapter.addFragment(exclusiveWallFragment, "Exclusive Wallpaper");
        viewPager.setAdapter(adapter);
    }

    private boolean isAlreadyDisplay;
    public void displayBannerAd(){
        if(!isAlreadyDisplay){
            isAlreadyDisplay=true;
            FrameLayout AdContainer1= (FrameLayout) findViewById(R.id.AdContainer1);
            requestBanner(this,AdContainer1,false,false);
        }
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
        try {
            if (WallpapersApplication.getApplication() != null) {
                WallpapersApplication.getApplication().onResume(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerUserInfoListener() {
        EventNotifier notifier =
                NotifierFactory.getInstance().getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_USER_INFO);
        notifier.registerListener(this, ListenerPriority.PRIORITY_HIGH);


    }

    private void unregisterUserInfoListener() {
        EventNotifier notifier =
                NotifierFactory.getInstance().getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_USER_INFO);
        notifier.unRegisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onDestroy();
        }
        unregisterUserInfoListener();
        isAlreadyDisplay=false;
        viewPager=null;
        tabLayout=null;
    }
}
