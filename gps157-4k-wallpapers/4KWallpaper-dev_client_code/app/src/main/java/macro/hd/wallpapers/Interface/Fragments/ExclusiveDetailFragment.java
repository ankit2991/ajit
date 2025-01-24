package macro.hd.wallpapers.Interface.Fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieFrameInfo;
import com.airbnb.lottie.value.SimpleLottieValueCallback;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.ExclusiveService.ExclusiveLiveWallpaperRenderer;
import macro.hd.wallpapers.ExclusiveService.ExclusiveLiveWallpaperService;
import macro.hd.wallpapers.ExclusiveService.sensor.RotationSensor;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.BlurImage;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.SnackbarHelper;
import macro.hd.wallpapers.Model.Wallpapers;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.RetryPolicy;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static macro.hd.wallpapers.ExclusiveService.GLWallpaperService.GLEngine.RENDERMODE_WHEN_DIRTY;

public class ExclusiveDetailFragment extends Fragment implements View.OnClickListener,RotationSensor.Callback,ExclusiveLiveWallpaperRenderer.Callbacks {

    protected static final String TAG = ExclusiveDetailFragment.class.getSimpleName();
    private View rootView;
    private UserInfoManager userInfoManager;

    public static final int SENSOR_RATE = 60;
    public static int SpeedMaxValue = 31;
    public static int RangeValue = 30;

    SettingStore settingStore;
    DownloadManager downloadManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userInfoManager = UserInfoManager.getInstance(getActivity().getApplicationContext());

        post = (Wallpapers) getArguments().getSerializable("post");

        settingStore = SettingStore.getInstance(getActivity().getApplicationContext());
        downloadManager = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);

        EventManager.sendEvent(EventManager.LBL_EXCLUSIVE_WALLPAPER,EventManager.ATR_KEY_OPEN_EXCLUSIVE_WALLPAPER,"OPEN");
        try {
            if(AppConstant.isWallSetDirectly) {
                WallpaperManager wpm = WallpaperManager.getInstance(getActivity());
                old_info = wpm.getWallpaperInfo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Error e) {
            e.printStackTrace();
        }
        isDownloadAgain=false;
    }

    private WallpaperInfo old_info;
    /**
     * Override method used to initialize the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_exclusive_detail, container, false);
        return rootView;
    }

    TextView title;
    ImageView thumbNail;
    public Wallpapers post;
    View layout_loading;
    ProgressBar videoProgress;
    FloatingActionButton fab_like ;
    TextView txt_curr_size,txt_total_size;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        thumbNail = (ImageView) rootView
                .findViewById(R.id.img_banner);

        layout_loading = rootView.findViewById(R.id.layout_loading);

        title = (TextView) rootView.findViewById(R.id.title);
        txt_curr_size=rootView.findViewById(R.id.txt_curr_size);
        txt_total_size=rootView.findViewById(R.id.txt_total_size);


        videoProgress=rootView.findViewById(R.id.pBar);
        rootView.findViewById(R.id.rl_showcase).setOnClickListener(this);

        fab_like=rootView.findViewById(R.id.fab_like);

        fab_like.setOnClickListener(this);

        if (CommonFunctions.isFavoriteAddedExclusive(userInfoManager, post.getPostId())) {
            fab_like.setImageResource(R.mipmap.ic_detail_like_s);
        }else
            fab_like.setImageResource(R.mipmap.ic_detail_like);

        int actionbarHeight = CommonFunctions.getActionBarHeight(getActivity());
        if (CommonFunctions.hasNavBar(getActivity())) {
            ImageView ivBottomView = rootView.findViewById(R.id.ivBottomView);
            if (ivBottomView != null) {
                ivBottomView.getLayoutParams().height = actionbarHeight;
            }
        } else {
        }

        FloatingActionButton fab_cat = rootView.findViewById(R.id.fab_set_wallpaper);

        fab_cat.setOnClickListener(this);

        rootView.findViewById(R.id.fab_download).setOnClickListener(this);

        if (!TextUtils.isEmpty(post.getImg())) {
            final ImageView imageOriginal=rootView.findViewById(R.id.img_blurre);
            String path = CommonFunctions.getDomainImages() + "small/" + post.getImg();

            Glide.with(getActivity().getApplicationContext())
                    .asBitmap().load(path)
                    .listener(new RequestListener<Bitmap>() {
                                  @Override
                                  public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                      return false;
                                  }

                                  @Override
                                  public boolean onResourceReady(final Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                      try {
                                          if (bitmap != null) {
                                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                              BlurImage.with(getActivity().getApplicationContext()).load(bitmap).intensity(25).Async(true).into(imageOriginal);
                                          }else {
                                              new Thread(){
                                                  @Override
                                                  public void run() {
                                                      super.run();
                                                      try {
                                                          if(getActivity()!=null && !getActivity().isFinishing()){
                                                              final Bitmap temp = CommonFunctions.fastblur1(bitmap, 25, getActivity());
                                                              getActivity().runOnUiThread(new Runnable() {
                                                                  @Override
                                                                  public void run() {
                                                                      try {
                                                                          Drawable d = new BitmapDrawable(getResources(), temp);
                                                                          imageOriginal.setBackground(d);
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
                                      } catch (Exception e) {
                                          e.printStackTrace();
                                      }catch (Error e) {
                                          e.printStackTrace();
                                      }
                                      return false;
                                  }
                              }
                    ).submit();
        }

        LottieAnimationView animationView = (LottieAnimationView) rootView.findViewById(R.id.animation_view);
        animationView.setAnimation("loading.json");
        animationView.setScale(0.25f);
        animationView.loop(true);

        animationView.playAnimation();


        setViewCountToServer();

        if(post==null) {
            getActivity().finish();
            return;
        }
        if(!TextUtils.isEmpty(post.getImg()))
            downloadFileVideo();


    }

    private boolean isDownloadAgain=false;
    private void deleteAndDownload(){
        try {
            if(!CommonFunctions.isNetworkAvailable(getActivity())){
                Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.no_net),Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return;
            }

            if(downloadRequest1!=null && !isDownloadeded){
                downloadRequest1.cancel();
                File file =new File(dst);
                if(file!=null && file.exists()){
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!isDownloadAgain){
            isDownloadAgain=true;
            downloadFileVideo();
        }else{

            try {
                if(getActivity()!=null && !getActivity().isFinishing()){
                    EventManager.sendEvent(EventManager.LBL_EXCLUSIVE_WALLPAPER,EventManager.ATR_KEY_NOT_DOWNLOAD_LIVE_WALLPAPER,""+post.getPostId());
                    Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.unable_download),Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fab_set_wallpaper:
                EventManager.sendEvent(EventManager.LBL_EXCLUSIVE_WALLPAPER,EventManager.ATR_KEY_SET_EXCLUSIVE_WALLPAPER,"Set");
                set4KWallPaper();
                break;
            case R.id.rl_showcase:
            case R.id.img_close:
                rootView.findViewById(R.id.rl_showcase).setVisibility(View.GONE);
                break;
            case R.id.fab_download:
                if(!isPermissionGranted()){
                    return;
                }
                copyToGallery();
                break;
            case R.id.rl_photos:
                if(!isDownloadeded)
                    return;
                if (rootView.findViewById(R.id.ll_option).getVisibility() == View.VISIBLE) {
                    rootView.findViewById(R.id.ll_option).setVisibility(View.GONE);
                } else {
                    rootView.findViewById(R.id.ll_option).setVisibility(View.VISIBLE);
                }

                break;
            case R.id.fab_like:
//                    settingStore.setVideoLiveWallpaperPath(dst);
                addToFav();
                break;
        }
    }

    private void copyToGallery() {

        String inputPath=dst;

        String outputPath = CommonFunctions.getSavedFilePath()+"/exclusive_"+post.getPostId()+ CommonFunctions.getExtension(post.getImg(),false);

        Logger.e("copyToGallery","inputPath:"+inputPath+" outputPath:"+outputPath);
        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists()) {
                dir.createNewFile();
                in = new FileInputStream(inputPath );
                out = new FileOutputStream(outputPath );

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;

                // write the output file (You have now copied the file)
                out.flush();
                out.close();
                out = null;
                showSnackBar(outputPath);
//                Toast.makeText(getActivity(), "Downloaded at " + outputPath, Toast.LENGTH_SHORT).show();
            }else{
                showSnackBar(outputPath);
//                Toast.makeText(getActivity(), "Already Downloaded at " + outputPath, Toast.LENGTH_SHORT).show();
            }
        }  catch (FileNotFoundException fnfe1) {
            Logger.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Logger.e("tag", e.getMessage());
        }
        CommonFunctions.scanToGallery(getActivity(),outputPath);
    }

    private void showSnackBar(final String outputPath){

        Snackbar snack = Snackbar.make(rootView.findViewById(R.id.activity_friend_request), "Downloaded at " + outputPath, Snackbar.LENGTH_LONG).setAction("SHOW", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + outputPath), "image/*");
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        SnackbarHelper.configSnackbar(getActivity(), snack, CommonFunctions.getNavigationBarHeight(getActivity()));

        View view = snack.getView();

//                                    view.setBackgroundColor(Color.BLACK);
        TextView textView = (TextView) view.findViewById(R.id.snackbar_text);
        Button snackViewButton = (Button) view.findViewById(R.id.snackbar_action);

        textView.setTextColor(Color.WHITE);
        snackViewButton.setTextColor(getResources().getColor(R.color.snack_action_color));

//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
//        params.gravity = Gravity.BOTTOM;


//        view.setLayoutParams(params);
        snack.show();
    }

    private static final int REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 1000;
    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions( //Method of Fragment
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
        if (requestCode == REQUEST_PERMISSIONS_CODE_WRITE_STORAGE) {
            if (grantResults!=null && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                copyToGallery();
            }else if (grantResults!=null && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                downloadFile("uhd");
                Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.allow_per),Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addToFav(){
        if(!CommonFunctions.isFavoriteAddedExclusive(userInfoManager,post.getPostId())) {
            CommonFunctions.setFavoritExclusive(settingStore,post);
            UserInfoManager groupManager = UserInfoManager.getInstance(getActivity().getApplicationContext());
            groupManager.addedToFavExclusive(post);
            fab_like.setImageResource(R.mipmap.ic_detail_like_s);
        }else{
            CommonFunctions.setUnFavoriteExclusive(settingStore, post);
            UserInfoManager groupManager = UserInfoManager.getInstance(getActivity().getApplicationContext());
            groupManager.removeFromGroupExclusive(post);

            fab_like.setImageResource(R.mipmap.ic_detail_like);
        }
    }

    private String dst;

    private void setViewCountToServer() {

//        if(groupManager.isViewed(post.getPostId()))
//            return;

        userInfoManager.addedToView(post.getPostId());

        SettingStore settingStore = SettingStore.getInstance(getActivity().getApplicationContext());
        if (settingStore.getViewCount().equalsIgnoreCase(""))
            settingStore.setViewCount(post.getPostId());
        else {
            if (!settingStore.getViewCount().contains(post.getPostId())) {
                settingStore.setViewCount(settingStore.getViewCount() + "_" + post.getPostId());
            }
        }
    }


    private void setDownload() {

        if (userInfoManager.isDownloaded(post.getPostId()))
            return;

        userInfoManager.addedToDownload(post.getPostId());

        if (settingStore.getDownloadCount().equalsIgnoreCase(""))
            settingStore.setDownloadCount(post.getPostId());
        else {
            if (!settingStore.getDownloadCount().contains(post.getPostId())) {
                settingStore.setDownloadCount(settingStore.getDownloadCount() + "_" + post.getPostId());
            }
        }
    }


    private ThinDownloadManager downloadManagerThin;
    private static final int DOWNLOAD_THREAD_POOL_SIZE = 1;
    int downloadId1;
    private ExclusiveLiveWallpaperRenderer renderer;
    private RotationSensor rotationSensor;
    private SharedPreferences preference;
    GLSurfaceView glSurfaceView ;
    private void playVideo() {
        isDownloadeded=true;
        try {
            WallpapersApplication.getApplication().ratingCounter++;

//            rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
            fab_like.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.fab_set_wallpaper).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.fab_download).setVisibility(View.VISIBLE);

//            glSurfaceView=rootView.findViewById(R.id.glSurfaceView);
            glSurfaceView = new GLSurfaceView(getActivity());

            thumbNail.setVisibility(View.GONE);


            //specify the location of media file

            if(TextUtils.isEmpty(dst)){
                dst = CommonFunctions.createFourKDirectoryCache() + "/" + post.getPostId() + CommonFunctions.getExtension(post.getImg(),false);
            }
//            Uri uri = Uri.parse(dst);

//            Common.loadImage(getActivity(), R.drawable.placeholder, dst , thumbNail);

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
            relativeLayout.setOnClickListener(this);

            setPowerSaverEnabled(preference.getBoolean("power_saver", true));

            onVisibilityChanged(true);

            try {
                rootView.findViewById(R.id.rl_inner_progress).animate().alpha(0.0f);
                if (!rootView.findViewById(R.id.rl_progress).isShown()) {
                    rootView.findViewById(R.id.ll_option).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
                } else {
                    setAlphaAnimation(rootView.findViewById(R.id.rl_progress));
                }
            } catch (Exception e) {
                e.printStackTrace();
                rootView.findViewById(R.id.ll_option).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!settingStore.getShowCaseDisplay()) {
            showCase();
            settingStore.setShowCaseDisplay(true);
        }
    }

    public boolean isPhotoVisible(){
        return rootView.findViewById(R.id.rl_showcase).getVisibility()==View.VISIBLE;
    }

    public void phoneGone(){
         rootView.findViewById(R.id.rl_showcase).setVisibility(View.GONE);
    }

    private void showCase(){
        rootView.findViewById(R.id.rl_showcase).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.img_close).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.rl_showcase).setOnClickListener(this);
        LottieAnimationView animationView = (LottieAnimationView) rootView.findViewById(R.id.animation_view_sample);
        animationView.setAnimation("phone.json");
//        animationView.setScale(0.25f);
        animationView.loop(true);

        animationView.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                new SimpleLottieValueCallback<ColorFilter>() {
                    @Override
                    public ColorFilter getValue(LottieFrameInfo<ColorFilter> frameInfo) {
                        return new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    }
                }
        );

        animationView.playAnimation();
    }

    DownloadRequest downloadRequest1;
    boolean isDownloadeded;
    public void downloadFileVideo() {
//        JesusApplication.getApplication().incrementUserClickCounter();

        dst = CommonFunctions.createFourKDirectoryCache() + "/" + post.getPostId() + CommonFunctions.getExtension(post.getImg(),false);
        Logger.e(TAG, "Destination:" + dst);

        try {

            File file =new File(dst);
            if(file!=null && file.exists()){
                playVideo();
                return;
            }

            rootView.findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
            downloadManagerThin = new ThinDownloadManager(DOWNLOAD_THREAD_POOL_SIZE);

            RetryPolicy retryPolicy = new DefaultRetryPolicy();

            Uri downloadUri = Uri.parse(CommonFunctions.getDomainImages()  + "uhd/" + post.getImg());
//            Logger.e(TAG, "downloadUri:" + downloadUri.toString());
            Uri destinationUri = Uri.parse(dst);
            downloadRequest1 = new DownloadRequest(downloadUri)
                    .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                    .setRetryPolicy(retryPolicy)
                    .setDownloadContext("Download1")
                    .setStatusListener(new DownloadStatusListenerV1() {
                        @Override
                        public void onDownloadComplete(DownloadRequest downloadRequest) {
                            try {
                                if(getActivity()!=null) {
//                                    rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);

                                    post.setDownloading(false);
                                    setDownload();
                                    playVideo();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                            Logger.e(TAG,""+errorMessage+" errorCode:"+errorCode);
                            deleteAndDownload();
                        }

                        @Override
                        public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                            try {
                                videoProgress.setProgress(progress);
                                txt_curr_size.setText(CommonFunctions.getFileSize(downloadedBytes) + " / ");
                                txt_total_size.setText(CommonFunctions.getFileSize(totalBytes));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

            if (downloadManagerThin.query(downloadId1) == com.thin.downloadmanager.DownloadManager.STATUS_NOT_FOUND) {
                downloadId1 = downloadManagerThin.add(downloadRequest1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.unable_download), Toast.LENGTH_SHORT).show();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private BroadcastReceiver powerSaverChangeReceiver;
    private boolean pauseInSavePowerMode = false;
    private boolean savePowerMode = false;
    void setPowerSaverEnabled(boolean enabled) {
//        if (pauseInSavePowerMode == enabled) return;
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

    private void set4KWallPaper() {
        boolean needToCallPrevew=true;
        if(old_info!=null && old_info.getComponent().getClassName().equals(ExclusiveLiveWallpaperService.class.getCanonicalName())){
            needToCallPrevew=false;
        }
        if(AppConstant.isWallSetDirectly && !needToCallPrevew) {
            CommonFunctions.showDialogConfirmation(getActivity(), getActivity().getResources().getString(R.string.dialog_title_info),  getActivity().getResources().getString(R.string.dialog_msg), new CommonFunctions.DialogOnButtonClickListener() {
                @Override
                public void onOKButtonCLick() {
                    settingStore.setExclusiveWallpaperPath(dst);
                    Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.wallpaper_updated),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelButtonCLick() {
                }
            });

        }else{
            settingStore.setExclusiveWallpaperPathTemp(dst);
            try {
                startActivityForResult(new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new
                                        ComponentName(getActivity(), ExclusiveLiveWallpaperService.class))
                        ,200);
            } catch (Exception e) {
                try {
                    startActivityForResult(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new
                            ComponentName(getActivity(), ExclusiveLiveWallpaperService.class))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 200);
                } catch (Exception e2) {
                    Toast.makeText(getActivity(), R.string
                            .toast_failed_launch_wallpaper_chooser, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.e("onActivityResult","resultCode:"+resultCode+" requestCode:"+requestCode);
        if(resultCode==getActivity().RESULT_OK){

            settingStore.setExclusiveWallpaperPath(dst);

        } else if(resultCode==getActivity().RESULT_CANCELED) {
            if(AppConstant.isWallSetDirectly) {
                WallpaperManager wpm = WallpaperManager.getInstance(getActivity());
                WallpaperInfo info = wpm.getWallpaperInfo();

                if(info == null)
                    return;

                if(old_info!=null)
                    Logger.e("onActivityResult","Old:"+old_info.getComponent().getClassName());
                Logger.e("onActivityResult","new:"+info.getComponent().getClassName());
                Logger.e("onActivityResult","service:"+ExclusiveLiveWallpaperService.class.getCanonicalName());

                if (info != null && (old_info == null || (!old_info.getComponent().getClassName().equals(info.getComponent().getClassName())
                        && info.getComponent().getClassName().equals(ExclusiveLiveWallpaperService.class.getCanonicalName())))) {
                    Logger.e("onActivityResult","canceled:"+dst);
                    settingStore.setExclusiveWallpaperPath(dst);
                }
            }
        }
        // -1 success 0 failed
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
    public void onSensorChanged(float[] angle) {
//        Logger.e(TAG, "onSensorChanged:");
        try {
            if (getResources().getConfiguration().orientation == Configuration
                    .ORIENTATION_LANDSCAPE)
                renderer.setOrientationAngle(angle[1], angle[2]);
            else renderer.setOrientationAngle(-angle[2], angle[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestRender() {
//        super.requestRender();
        if(glSurfaceView!=null)
            glSurfaceView.requestRender();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(glSurfaceView!=null)
            glSurfaceView.onPause();
        onVisibilityChanged(false);
    }

    public void setAlphaAnimation(View v) {

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
        fadeOut.setDuration(800);
        fadeOut.setStartDelay(200);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                try {
                    if(rootView!=null)
                        rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
                    if(thumbNail!=null)
                        thumbNail.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
//                    rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
//                    thumbNail.setVisibility(View.GONE);
                }
            }
        });
        mAnimationSet.start();

        rootView.findViewById(R.id.ll_option).setAlpha(0.0f);
        rootView.findViewById(R.id.ll_option).setVisibility(View.VISIBLE);
        setFadeINButton(rootView.findViewById(R.id.ll_option));
    }

    public static void setFadeINButton(View v) {

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f);
        fadeIn.setDuration(300);
        fadeIn.setStartDelay(700);
        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        mAnimationSet.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "onDestroy");

        rootView = null;
        userInfoManager = null;
        settingStore = null;
        txt_curr_size=null;
        txt_total_size=null;
        title = null;
        thumbNail = null;
        layout_loading = null;

        try {
            if(downloadRequest1!=null && !isDownloadeded){
                downloadRequest1.cancel();
                File file =new File(dst);
                if(file!=null && file.exists()){
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dst=null;

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

        old_info=null;
    }

}
