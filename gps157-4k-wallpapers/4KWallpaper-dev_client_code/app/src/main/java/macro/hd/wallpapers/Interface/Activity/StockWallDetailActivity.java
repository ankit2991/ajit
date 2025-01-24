package macro.hd.wallpapers.Interface.Activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import macro.hd.wallpapers.Utilily.BlurImage;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.MyCustomView.MyImageView;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Model.Wallpapers;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
//import com.crashlytics.android.answers.Answers;
//import com.crashlytics.android.answers.CustomEvent;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.RetryPolicy;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class StockWallDetailActivity extends BaseActivity implements View.OnClickListener {
    private UserInfoManager userInfoManager;
    public Wallpapers post;
    protected static final String TAG = StockWallDetailActivity.class.getSimpleName();
    SettingStore settingStore;
    DownloadManager downloadManager;
    View decorView;
    Bitmap imgOriginal;
    private static final int REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_stock);

        hideButton();
        CommonFunctions.updateWindow(StockWallDetailActivity.this);
        userInfoManager = UserInfoManager.getInstance(getApplicationContext());
        settingStore = SettingStore.getInstance(getApplicationContext());
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
//        setSupportActionBar(toolbar);
//
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("My Favorite");

        findViewById(R.id.img_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        post = (Wallpapers) getIntent().getSerializableExtra("post");
//        post.setImg("Alcatel31.jpg");
        if(post==null) {
            finish();
            return;
        }

//        Logger.e("Image",""+Common.getDomainImages() + "stock_thumb/" + post.getImg());

        final RelativeLayout rl_photos=findViewById(R.id.rl_photos);

        ImageView blur=findViewById(R.id.img_blur);
        Logger.e("path thumb",CommonFunctions.getDomainImages() + "stock_thumb/" + post.getImg());
        if (!TextUtils.isEmpty(post.getImg())) {
            Glide.with(this)
                    .asBitmap()
                    .load(CommonFunctions.getDomainImages() + "stock_thumb/" + post.getImg())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                BlurImage.with(getApplicationContext()).load(resource).intensity(25).Async(true).into(blur);
                            }else
                                {
                                new Thread(){
                                    @Override
                                    public void run() {
                                        super.run();
                                        try {
                                            if( !isFinishing()){
                                                final Bitmap temp = CommonFunctions.fastblur1(resource, 25, StockWallDetailActivity.this);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            blur.setImageBitmap(temp);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }.start();

                            }
                        }
                    });
        }

        if (!TextUtils.isEmpty(post.getImg())) {

            String path = CommonFunctions.getDomainImages()+"stock_uhd/" + post.getImg();
            if (post.getImg().startsWith("http")) {
                path = post.getImg();
            }

            		Glide.with(this)
					.asBitmap()
					.load(path).apply(new RequestOptions())
					.into(new SimpleTarget<Bitmap>() {
						@Override
						public void onResourceReady(final Bitmap resource, Transition<? super Bitmap> transition) {
							try {
                                imgOriginal=resource;

								Drawable d = new BitmapDrawable(getResources(), resource);
//								holder.thumbNail.setImageDrawable(d);

//								LinearLayout item = (LinearLayout) rootView.findViewById(R.id.llImage);
								LayoutInflater inflater = (LayoutInflater)   getSystemService(Context.LAYOUT_INFLATER_SERVICE);
								View child = inflater.inflate(R.layout.item_photo_detail, rl_photos, false);
								MyImageView thumbNail = (MyImageView) child
										.findViewById(R.id.img_banner);
								thumbNail.setImageDrawable(d);
//								thumbNail.setOnClickListener(this);
								rl_photos.addView(child);

								setAlphaAnimation(blur);

							} catch (Exception e) {
								e.printStackTrace();
							}catch (Error e) {
								e.printStackTrace();
							}
						}
					});
        }

        findViewById(R.id.fab_set_wallpaper).setOnClickListener(this);
        findViewById(R.id.img_share).setOnClickListener(this);
        findViewById(R.id.fab_download).setOnClickListener(this);
        findViewById(R.id.img_pinch_zoom).setOnClickListener(this);
        findViewById(R.id.img_report).setOnClickListener(this);
    }

    public void setAlphaAnimation(View v) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha",  1f, .3f);
        fadeOut.setDuration(200);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeOut);
//
        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                findViewById(R.id.layout_loading).setVisibility(View.GONE);
                showButton();
            }
        });
        mAnimationSet.start();
    }

    public void hideButton()
    {
        findViewById(R.id.menu_add).setVisibility(View.GONE);
        findViewById(R.id.fab_set_wallpaper).setVisibility(View.GONE);
    }

    public void showButton()
    {
        findViewById(R.id.menu_add).setVisibility(View.VISIBLE);
        findViewById(R.id.fab_set_wallpaper).setVisibility(View.VISIBLE);
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

    private boolean isShared,is_set_wallpaper;
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.img_share:
                isShared=true;
                downloadFile("uhd");
//                openBottomSheet();
                break;
            case R.id.fab_download:
                is_set_wallpaper=false;
                isShared=false;
                downloadFile("uhd");
//                openBottomSheet();
                break;
            case R.id.fab_set_wallpaper:
                isShared=false;
                is_set_wallpaper=true;
                downloadFile("uhd");
                break;
            case R.id.img_report:
                CommonFunctions.report(this,post.getPostId());
                break;
            case R.id.img_pinch_zoom:

                if (!CommonFunctions.isNetworkAvailable(this)) {
                    CommonFunctions.showInternetDialog(this);
                    return;
                }

                isShared=false;

                Intent intent1=new Intent(this, PreviewActivity.class);
                String path = CommonFunctions.getDomainImages() + "stock_uhd/" + post.getImg();
                if(post.getImg().startsWith("http")) {
                    path=post.getImg();
                }
                if(!TextUtils.isEmpty(dst))
                    path=dst;

                intent1.putExtra("path",path);
                startActivity(intent1);

                break;
        }
    }

    private void initializeDownload(final String pathToDownload, String downloadURL){

        new Thread(){
            @Override
            public void run() {
                super.run();
                if(imgOriginal!=null)
                    storeImage(imgOriginal,pathToDownload);
            }
        }.start();

        Logger.e(TAG,"downloadURL:"+downloadURL);

        if(false) {
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
    }

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

                downloadCompleted();
            }
        }

        @Override
        public void onDownloadFailed(DownloadRequest request, int errorCode, String errorMessage) {
            final int id = request.getDownloadId();
            if (id == downloadId1) {
                dismissLoadingProgress();
                Toast.makeText(StockWallDetailActivity.this,getString(R.string.unable_download),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onProgress(DownloadRequest request, long totalBytes, long downloadedBytes, int progress) {
            int id = request.getDownloadId();

            System.out.println("######## onProgress ###### "+id+" : "+totalBytes+" : "+downloadedBytes+" : "+progress);
            if (id == downloadId1) {
                try {
                    if(getProgress()!=null){
                        getProgress().setCurrentProgress(progress);
                        getTv().setText(""+progress +"%");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void downloadCompleted(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    if(getProgress()!=null){
                        getProgress().setCurrentProgress(100);
                        getTv().setText("100"+"%");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                 CommonFunctions.scanToGallery(getApplicationContext(),dst);

                dismissLoadingProgress();

                post.setDownloading(false);
//                            updateDownloadButton();

                EventManager.sendEvent("Download","Stock category",""+post.getCategory());
//                Answers.getInstance().logCustom(new CustomEvent("Download")
//                        .putCustomAttribute("category", ""+post.getCategory()));

                if(isShared)
                    CommonFunctions.sharedPost(StockWallDetailActivity.this, "", dst, "", false);
                else {

                    if(!is_set_wallpaper)
                        Toast.makeText(StockWallDetailActivity.this, getString(R.string.dwn_at) + dst, Toast.LENGTH_SHORT).show();
                    else
                        setWallpaper();

                    is_set_wallpaper=false;
                }

//                if(!TextUtils.isEmpty(JesusApplication.getApplication().getSettings().getFb_ad_download()) && JesusApplication.getApplication().getSettings().getFb_ad_download().equalsIgnoreCase("1")) {
//                    if (!JesusApplication.getApplication().isFBAdLoaded()) {
////                        JesusApplication.getApplication().showAdDirectly = true;
//                    }
//                }
            }
        });

    }

    private void setWallpaper(){
        try {
            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(Uri.fromFile(new File(dst)), "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("mimeType", "image/*");
            startActivity(Intent.createChooser(intent, getString(R.string.label_set_dialog)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDownload(){

        try {
            if(userInfoManager.isDownloaded(post.getPostId()))
                return;

            userInfoManager.addedToDownload(post.getPostId());

            if(settingStore.getDownloadCount().equalsIgnoreCase(""))
                settingStore.setDownloadCount(post.getPostId());
            else{
                if(!settingStore.getDownloadCount().contains(post.getPostId())){
                    settingStore.setDownloadCount(settingStore.getDownloadCount()+"_"+post.getPostId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String dst;
    public void downloadFile(String type) {

        if(imgOriginal==null)
            return;

        if (!CommonFunctions.isNetworkAvailable(this)) {
            CommonFunctions.showInternetDialog(this);
            return;
        }

        if(!isPermissionGranted()){
            return;
        }

//        JesusApplication.getApplication().incrementUserClickCounter();

        dst = CommonFunctions.getSavedFilePath()+"/"+post.getPostId()+"_"+type+ CommonFunctions.getExtension(post.getImg(),false);

        File file=new File(dst);

        Logger.e(TAG,"Destination:"+dst);

        if(file!=null && file.exists()){
            if(isShared)
                CommonFunctions.sharedPost(this, "", dst, "", false);
            else {
                if(!is_set_wallpaper)
                    Toast.makeText(this, getString(R.string.label_already_dwn) + dst, Toast.LENGTH_SHORT).show();
                else
                    setWallpaper();

                is_set_wallpaper=false;

            }
            return;
        }

        try {

            showLoadingDialogDownload(getString(R.string.download_text));
            getTv().setText("0%");

            String path = CommonFunctions.getDomainImages()+"stock_uhd/" + post.getImg();
            if (post.getImg().startsWith("http")) {
                path = post.getImg();
            }

            initializeDownload(dst,path);

            post.setProgress("0");
            post.setDownload_id(""+downloadId1);

            setDownload();

//            String lastMsg=settingStore.getImages();
//            if(TextUtils.isEmpty(lastMsg)){
//                settingStore.setImages(dst);
//            }else{
//                settingStore.setImages(lastMsg+"#"+dst);
//            }

            CommonFunctions.addMessageToDatabase(this,post);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,getString(R.string.download_unable),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE);}
    }

    private void storeImage(Bitmap image,String pathToDownload) {
        File pictureFile = new File(pathToDownload);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            downloadCompleted();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,//Method of Fragment
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS_CODE_WRITE_STORAGE
                );
                return false;
            } else {
                return true;
            }
        }else
            return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE_WRITE_STORAGE) {
            if (grantResults != null && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadFile("uhd");
            } else if (grantResults != null && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, getString(R.string.allow_per), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            WallpapersApplication.getApplication().activity_download=null;
            imgOriginal=null;
            decorView=null;
            userInfoManager = null;
            settingStore = null;
            downloadManager = null;
            dst=null;
            downloadManagerThin=null;

            WallpapersApplication.getApplication().isAdDisplayBefore=false;
//            if(WallpapersApplication.isAppRunning){
//                if(WallpapersApplication.getApplication().getSettings().isDownloadAdnormal()){
//                    if(!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getFb_ad_download()) && WallpapersApplication.getApplication().getSettings().getFb_ad_download().equalsIgnoreCase("1")) {
//                        if (WallpapersApplication.getApplication().isGGAdLoadedDownload()) {
//                        }
//                    }
//                }else {
//                    if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getFb_ad_download()) && WallpapersApplication.getApplication().getSettings().getFb_ad_download().equalsIgnoreCase("1")) {
//                        if (WallpapersApplication.getApplication().isFBAdLoaded()) {
//                        }
//                    }
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
