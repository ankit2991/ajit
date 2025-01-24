package macro.hd.wallpapers.Interface.Activity;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.AppController.RequestManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Interface.Fragments.DoubleFragment;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.DoubleWallpaper;
import macro.hd.wallpapers.Model.DoubleWallInfoModel;

import com.bumptech.glide.Glide;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.RetryPolicy;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DoubleWallpaperActivity extends BaseBannerActivity implements NetworkCommunicationManager.CommunicationListnerNew {

    SettingStore settingStore;
    private List<DoubleWallpaper> movieList = new ArrayList<DoubleWallpaper>();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTheme();
        setLocale();
        setContentView(R.layout.activity_double_wallpaper_pager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.txtdouble));


        settingStore = SettingStore.getInstance(this);

        WallpapersApplication.getApplication().ratingCounter++;


        findViewById(R.id.layout_loading).setVisibility(View.VISIBLE);

        getPostListResponse();

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

    MyPagerAdapter adapterViewPager;
    Handler handler = new Handler();

    ViewPager mRecyclerView;

    protected void initViewPager() {

        if (mRecyclerView==null) {
            Logger.e("initview","null");
            mRecyclerView = (ViewPager) findViewById(R.id.viewpager);
            mRecyclerView.setClipToPadding(false);
            mRecyclerView.setPageMargin(12);
            adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
            mRecyclerView.setAdapter(adapterViewPager);

            // Attach the page change listener inside the activity
            mRecyclerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                // This method will be invoked when a new page becomes selected.
                @Override
                public void onPageSelected(final int position) {
                    Logger.e("DoubleFragment11", "position:" + position);

                    if (position == (movieList.size() - 3) && moreData) {
                        showLoadingDialogMsg(getResources().getString(R.string.dialog_loading_double_Wallpaper));
                        getPostListResponse();
                    }
                    try {
                        DoubleFragment.selected_pos = position;

                        for (int i = 0; i < adapterViewPager.getAllFragments().size(); i++) {
                            ((DoubleFragment) adapterViewPager.getAllFragments().get(i)).stopAnimation();
                        }

                        if (handler != null && runnable != null) {
                            handler.removeCallbacks(runnable);
                            handler.postDelayed(runnable, 1000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // This method will be invoked when the current page is scrolled
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    // Code goes here
//                Logger.e("DoubleFragment11","onPageScrolled:"+position);
                }

                // Called when the scroll state changes:
                // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
                @Override
                public void onPageScrollStateChanged(int state) {
                    // Code goes here

//                Logger.e("DoubleFragment11","onPageScrollStateChanged:"+state);
                }
            });
            FrameLayout AdContainer1 = (FrameLayout) findViewById(R.id.AdContainer1);
            requestBanner(DoubleWallpaperActivity.this, AdContainer1, false,false);
        }else {

            Logger.e("initview","null");
            adapterViewPager.notifyDataSetChanged();
//            mRecyclerView.setCurrentItem(mRecyclerView.getAdapter().getCount());
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (adapterViewPager != null) {
                    DoubleFragment fragment = (DoubleFragment) adapterViewPager.getRegisteredFragment(selected_pos);
                    fragment.startAnimation();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void updateBG(int pos) {
    }

    private void getPostListResponse() {

        RequestManager manager = new RequestManager(this);
        Logger.e("used_id", getUsedIds());
        String api = CommonFunctions.getDomain() + AppConstant.URL_DOUBLE_WALLPAPER;
//        String used_ids="";
        manager.getDoubleWallService(api, "1", CommonFunctions.getHardwareId(this), getUsedIds(), this);
    }

    private int paginationCount = 15;
    boolean isLastPage, moreData;

    @Override
    public void onSuccess(final IModel response, int operationCode) {
        try {
            if (isFinishing())
                return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing())
                    return;
                try {
                    dismissLoadingProgress();

                    findViewById(R.id.layout_loading).setVisibility(View.GONE);
                    DoubleWallInfoModel model = (DoubleWallInfoModel) response;
                    if (model != null && model.getStatus().equalsIgnoreCase("1")) {
//                            movieList.clear();
//                            movieList.addAll(model.getPost());
                        movieList.addAll(model.getPost());

                        boolean pagination_end = false;
                        if (model.getPost() == null || paginationCount != model.getPost().size()) {
                            pagination_end = true;
                        }

                        if (!pagination_end) {
                            isLastPage = false;
                            moreData = true;
                        } else {
                            isLastPage = true;
                            moreData = false;
                        }

                        Logger.e("model size", movieList.size() + " : length");
                        initViewPager();


                    } else if (model != null && model.getStatus().equalsIgnoreCase("0")) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getUsedIds() {
        String finalString = null;
//		int counter = 0;
        StringBuffer ids = new StringBuffer();
        if (movieList != null)
            for (int i = 0; i < movieList.size(); i++) {
                if (!movieList.get(i).getPostId().equalsIgnoreCase("-99")) {
                    ids.append("'" + movieList.get(i).getPostId() + "'");
                    ids.append(",");
//					counter++;
                }
            }
//		Logger.e("counter:", "" + counter);
        finalString = ids.toString();
        if (!TextUtils.isEmpty(finalString)) {
            finalString = finalString.substring(0, finalString.length() - 1);
        }
        Logger.e("used id", finalString);
        return finalString;
    }

    @Override
    public void onStartLoading() {

    }

    @Override
    public void onFail(final WebServiceError errorMsg) {
        if (!isFinishing())
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.layout_loading).setVisibility(View.GONE);
                }
            });

    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onPause(this);
        }
        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
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

    private void setWallpapersFinal() {
        dismissLoadingProgress();
        showLoadingDialogMsg(getResources().getString(R.string.dialog_Settind_double_Wallpaper));
        counter = 0;
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    DoubleWallpaper post = movieList.get(selected_pos);
                    EventManager.sendEvent(EventManager.LBL_DOUBLE_WALLPAPER, EventManager.ATR_VALUE_SET_DOUBLE, "Id:" + post.getPostId());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Logger.e("setWallpapersFinal", "wall1:" + wall1 + " wall2:" + wall2);
                Bitmap bitmap1;
                try {
                    bitmap1 = Glide
                            .with(DoubleWallpaperActivity.this)
                            .asBitmap()
                            .load(wall1)
                            .submit()
                            .get();
                } catch (Exception e) {
                    e.printStackTrace();
                    bitmap1 = BitmapFactory.decodeFile(wall1);
                }

                Bitmap bitmap2;
                try {
                    bitmap2 = Glide
                            .with(DoubleWallpaperActivity.this)
                            .asBitmap()
                            .load(wall2)
                            .submit()
                            .get();
                } catch (Exception e) {
                    e.printStackTrace();
                    bitmap2 = BitmapFactory.decodeFile(wall2);
                }

                final Bitmap finalBitmap = bitmap1;
                final Bitmap finalBitmap1 = bitmap2;
                WallpaperManager wallpaperManager =
                        WallpaperManager.getInstance(getApplicationContext());
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (finalBitmap != null) {
                            if (Build.VERSION.SDK_INT < 24) {
                                wallpaperManager.setBitmap(finalBitmap);
                            } else {
                                wallpaperManager.setBitmap(finalBitmap, null, true, WallpaperManager.FLAG_LOCK);
                            }
                        }
                        if (finalBitmap1 != null)
                            wallpaperManager.setBitmap(finalBitmap1, null, true, WallpaperManager.FLAG_SYSTEM);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingProgress();
                            Toast.makeText(DoubleWallpaperActivity.this, getResources().getString(R.string.wallpaper_updated), Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        if (!isFinishing())
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        dismissLoadingProgress();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            if (finalBitmap != null) {
                                                if (Build.VERSION.SDK_INT < 24) {
                                                    wallpaperManager.setBitmap(finalBitmap);
                                                } else {
                                                    wallpaperManager.setBitmap(finalBitmap, null, true, WallpaperManager.FLAG_LOCK);
                                                }
                                            }
                                            if (finalBitmap1 != null)
                                                wallpaperManager.setBitmap(finalBitmap1, null, true, WallpaperManager.FLAG_SYSTEM);
                                        }
                                        Toast.makeText(DoubleWallpaperActivity.this, getResources().getString(R.string.wallpaper_updated), Toast.LENGTH_SHORT).show();
                                    } catch (Exception ioException) {
                                        ioException.printStackTrace();
                                    }
                                }
                            });
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                }

            }
        }.start();
    }

    String wall1, wall2;
    private int selected_pos;
    int counter;

    public void setWallpaper(int position) {
        selected_pos = position;
        if (counter == 0) {
            showLoadingDialogDownload(getResources().getString(R.string.download_text));
            getTv().setText("0%");
        }

        try {
            DoubleWallpaper post = movieList.get(position);
            String path;
            String filename = "";
            if (counter == 0) {
                path = CommonFunctions.getDomainImages() + "double_hd/" + post.getImg1();
                filename = post.getImg1();
                if (post.getImg1().startsWith("http")) {
                    path = post.getImg1();
                    filename = path.substring(path.lastIndexOf("/") + 1);
                }
                Logger.e("path", "" + path);
                wall1 = CommonFunctions.createBaseDirectoryCacheDoubleWall() + "/" + filename;
                Logger.e("dst", "" + wall1);
                File file = new File(wall1);
                if (file != null && file.exists()) {
                    counter++;
                    setWallpaper(selected_pos);
                } else
                    initializeDownload(wall1, path);

            } else {
                path = CommonFunctions.getDomainImages() + "double_hd/" + post.getImg2();
                filename = post.getImg2();
                if (post.getImg2().startsWith("http")) {
                    path = post.getImg2();
                    filename = path.substring(path.lastIndexOf("/") + 1);
                }

//                Logger.e("path",""+path);
                wall2 = CommonFunctions.createBaseDirectoryCacheDoubleWall() + "/" + filename;
                Logger.e("dst", "" + wall2);

                File file = new File(wall2);
                if (file != null && file.exists()) {
                    setWallpapersFinal();
                } else
                    initializeDownload(wall2, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeDownload(String pathToDownload, String downloadURL) {

//        Logger.e("initializeDownload",""+downloadURL);

        downloadManagerThin = new ThinDownloadManager(DOWNLOAD_THREAD_POOL_SIZE);

        RetryPolicy retryPolicy = new DefaultRetryPolicy();

        Uri downloadUri = Uri.parse(downloadURL);
        Uri destinationUri = Uri.parse(pathToDownload);
        final DownloadRequest downloadRequest1 = new DownloadRequest(downloadUri)
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setRetryPolicy(retryPolicy)
                .setDownloadContext("Download1")
                .setStatusListener(myDownloadStatusListener);

        if (downloadManagerThin.query(downloadId1) == com.thin.downloadmanager.DownloadManager.STATUS_NOT_FOUND) {
            downloadId1 = downloadManagerThin.add(downloadRequest1);
        }
    }

    private int progress_final;
    private ThinDownloadManager downloadManagerThin;
    private static final int DOWNLOAD_THREAD_POOL_SIZE = 1;
    MyDownloadDownloadStatusListenerV1
            myDownloadStatusListener = new MyDownloadDownloadStatusListenerV1();
    int downloadId1;

    class MyDownloadDownloadStatusListenerV1 implements DownloadStatusListenerV1 {

        @Override
        public void onDownloadComplete(DownloadRequest request) {
            final int id = request.getDownloadId();
            if (id == downloadId1) {
//                Logger.e("onDownloadComplete","onDownloadComplete");
                if (counter == 0) {
                    counter++;
                    setWallpaper(selected_pos);
                } else
                    setWallpapersFinal();
            }
        }

        @Override
        public void onDownloadFailed(DownloadRequest request, int errorCode, String errorMessage) {
            try {
                final int id = request.getDownloadId();
                if (id == downloadId1) {
                    dismissLoadingProgress();
                    Toast.makeText(DoubleWallpaperActivity.this, getResources().getString(R.string.download_unable), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProgress(DownloadRequest request, long totalBytes, long downloadedBytes, int progress) {
            int id = request.getDownloadId();

            System.out.println("######## onProgress ###### " + id + " : " + totalBytes + " : " + downloadedBytes + " : " + progress);
            if (id == downloadId1) {
                try {
                    progress_final = (int) (progress / 2);
                    if (counter == 1)
                        progress_final += 50;
//                    Logger.e("progress_final",""+progress_final);
                    if (getProgress() != null) {
                        getProgress().setCurrentProgress(progress_final);
                        getTv().setText("" + progress_final + "%");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {
//        private int NUM_ITEMS = 3;
        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        @Override
        public float getPageWidth(int position) {
//            return JesusApplication.isNewLayout?0.96f:0.90f;
            return 0.92f;
        }

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
//            NUM_ITEMS = movieList.size();
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return movieList.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            if (registeredFragments.get(position) != null) {
                return registeredFragments.get(position);
            }
            DoubleFragment doubleFragment = DoubleFragment.newInstance(position, movieList.get(position));
            registeredFragments.put(position, doubleFragment);
            return doubleFragment;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_UNCHANGED;
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        public SparseArray<Fragment> getAllFragments() {
            return registeredFragments;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onDestroy();
        }
        if (adapterViewPager != null) {
            adapterViewPager.getAllFragments().clear();
            adapterViewPager = null;
        }
        if (movieList != null)
            movieList.clear();

        try {
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecyclerView = null;
        handler = null;
        runnable = null;

    }
}
