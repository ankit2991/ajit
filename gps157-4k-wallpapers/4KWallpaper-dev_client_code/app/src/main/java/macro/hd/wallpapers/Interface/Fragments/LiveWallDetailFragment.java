package macro.hd.wallpapers.Interface.Fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DownloadManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.BlurImage;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.LiveWallpaperService;
import macro.hd.wallpapers.Model.Wallpapers;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.clans.fab.FloatingActionButton;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.RetryPolicy;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;

public class LiveWallDetailFragment extends Fragment implements View.OnClickListener {

    protected static final String TAG = LiveWallDetailFragment.class.getSimpleName();
    private View rootView;
    private UserInfoManager userInfoManager;

    SettingStore settingStore;
    DownloadManager downloadManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userInfoManager = UserInfoManager.getInstance(getActivity().getApplicationContext());

        post = (Wallpapers) getArguments().getSerializable("post");

        settingStore = SettingStore.getInstance(getActivity().getApplicationContext());
        downloadManager = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);

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

    /**
     * Override method used to initialize the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_video_detail, container, false);
        return rootView;
    }

    TextView title;
    ImageView thumbNail;
    public Wallpapers post;
    View layout_loading;
    ProgressBar videoProgress;
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

        int actionbarHeight = CommonFunctions.getActionBarHeight(getActivity());
        if (CommonFunctions.hasNavBar(getActivity())) {
            ImageView ivBottomView = rootView.findViewById(R.id.ivBottomView);
            if (ivBottomView != null) {
                ivBottomView.getLayoutParams().height = actionbarHeight;
            }
        } else {
        }

        if(post==null) {
            getActivity().finish();
            return;
        }
        FloatingActionButton fab_cat = rootView.findViewById(R.id.fab_set_wallpaper);

        fab_cat.setOnClickListener(this);

        if (!TextUtils.isEmpty(post.getImg())) {
            final ImageView imageOriginal=rootView.findViewById(R.id.img_blurre);
            String path = CommonFunctions.getDomainImages() + "liveimg/" + post.getImg();

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

        fab_like=rootView.findViewById(R.id.fab_like);

        fab_like.setOnClickListener(this);
        rootView.findViewById(R.id.fab_share).setOnClickListener(this);
        rootView.findViewById(R.id.rl_photos).setOnClickListener(this);

        setViewCountToServer();

        if(post==null) {
            getActivity().finish();
            return;
        }
        if(!TextUtils.isEmpty(post.getVid()))
            downloadFileVideo();

        if (CommonFunctions.isFavoriteAddedLive(userInfoManager, post.getPostId())) {
            fab_like.setImageResource(R.mipmap.ic_detail_like_s);
        }else
            fab_like.setImageResource(R.mipmap.ic_detail_like);

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if(videoView!=null){
                playVideo();
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if(videoView!=null){
                videoView.pause();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
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
                    EventManager.sendEvent(EventManager.LBL_LIVE_WALLPAPER,EventManager.ATR_KEY_NOT_DOWNLOAD_LIVE_WALLPAPER,""+post.getPostId());
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

                EventManager.sendEvent(EventManager.LBL_LIVE_WALLPAPER,EventManager.ATR_KEY_SET_LIVE_WALLPAPER,"Set");

                boolean needToCallPrevew=true;
                if(old_info!=null && old_info.getComponent().getClassName().equals(LiveWallpaperService.class.getCanonicalName())){
                    needToCallPrevew=false;
                }
                if(AppConstant.isWallSetDirectly && !needToCallPrevew) {
                    CommonFunctions.showDialogConfirmation(getActivity(), getActivity().getResources().getString(R.string.dialog_title_info),  getActivity().getResources().getString(R.string.update_live_wall), new CommonFunctions.DialogOnButtonClickListener() {
                        @Override
                        public void onOKButtonCLick() {
                            settingStore.setVideoLiveWallpaperPath(dst);
                            Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.wallpaper_updated),Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelButtonCLick() {

                        }
                    });

                }else{
                    Intent intentVideo = null;
                    try {
//                        JesusApplication.getApplication().incrementUserClickCounter();
                        intentVideo = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        intentVideo.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        intentVideo.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                new ComponentName(getActivity(),
                                        LiveWallpaperService.class));
                        startActivityForResult(intentVideo,200);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            startActivityForResult(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new
                                    ComponentName(getActivity(), LiveWallpaperService.class))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 200);
                        } catch (Exception e2) {
                            Toast.makeText(getActivity(), R.string
                                    .toast_failed_launch_wallpaper_chooser, Toast.LENGTH_LONG).show();
                        }
                    }
                }


                break;

            case R.id.fab_like:
//                    settingStore.setVideoLiveWallpaperPath(dst);
                addToFav();
                break;
            case R.id.fab_share:
                CommonFunctions.sharedPost(getActivity(), "", dst, "", true);
                break;

            case R.id.rl_photos:
                if(!isDownloadeded)
                    return;
                if (rootView.findViewById(R.id.fab_like).getVisibility() == View.VISIBLE) {

                    rootView.findViewById(R.id.fab_like).setVisibility(View.GONE);
                    rootView.findViewById(R.id.fab_set_wallpaper).setVisibility(View.GONE);
                    rootView.findViewById(R.id.fab_share).setVisibility(View.GONE);

//                    rootView.findViewById(R.id.AdContainer1).setVisibility(View.GONE);


//                    ((JesusDetailActivity) getActivity()).hideToolbar();

                } else {
                    rootView.findViewById(R.id.fab_like).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.fab_set_wallpaper).setVisibility(View.VISIBLE);
                    if(!TextUtils.isEmpty(post.getIs_share()) && post.getIs_share().equalsIgnoreCase("1"))
                        rootView.findViewById(R.id.fab_share).setVisibility(View.VISIBLE);
//                    rootView.findViewById(R.id.AdContainer1).setVisibility(View.VISIBLE);

//                    ((JesusDetailActivity) getActivity()).showToolbar();
//                    showStatusBar();
                }

                break;
        }
    }

    FloatingActionButton fab_like ;
    public void addToFav(){
        Logger.e(TAG,"addToFav:"+post.getPostId());
        if(!CommonFunctions.isFavoriteAddedLive(userInfoManager,post.getPostId())) {
            CommonFunctions.setFavoriteLive(settingStore,post);
            UserInfoManager groupManager = UserInfoManager.getInstance(getActivity().getApplicationContext());
            groupManager.addedToFavLive(post);
            fab_like.setImageResource(R.mipmap.ic_detail_like_s);
        }else{
            CommonFunctions.setUnFavoriteLive(settingStore, post);
            UserInfoManager groupManager = UserInfoManager.getInstance(getActivity().getApplicationContext());
            groupManager.removeFromGroupLive(post);
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

    private boolean isVideoWall(){
        if(post!=null && !TextUtils.isEmpty(post.getVid()))
            return true;
        return false;
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

    private void setWallpaper() {
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(Uri.fromFile(new File(dst)), "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("mimeType", "image/*");
        startActivity(Intent.createChooser(intent,  getActivity().getResources().getString(R.string.label_set_dialog)));
    }


    private ThinDownloadManager downloadManagerThin;
    private static final int DOWNLOAD_THREAD_POOL_SIZE = 1;
    int downloadId1;




    VideoView videoView;
    private void playVideo() {
        isDownloadeded=true;
        try {
            EventManager.sendEvent(EventManager.LBL_LIVE_WALLPAPER,EventManager.ATR_KEY_SET_LIVE_WALLPAPER,"Open");
//            rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
            fab_like.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.fab_set_wallpaper).setVisibility(View.VISIBLE);

            if(!TextUtils.isEmpty(post.getIs_share()) && post.getIs_share().equalsIgnoreCase("1"))
                rootView.findViewById(R.id.fab_share).setVisibility(View.VISIBLE);
            else
                rootView.findViewById(R.id.fab_share).setVisibility(View.GONE);

            thumbNail.setVisibility(View.GONE);
            videoView = (VideoView) rootView.findViewById(R.id.videoView1);
            videoView.setVisibility(View.VISIBLE);

            //Creating MediaController
//        MediaController mediaController = new MediaController(getActivity());
//        mediaController.setAnchorView(videoView);

            //specify the location of media file

            if(TextUtils.isEmpty(dst)){
                dst = CommonFunctions.createBaseDirectoryCache() + "/" + post.getPostId() + CommonFunctions.getExtension(post.getVid(),true);
            }
            Uri uri = Uri.parse(dst);

            //Setting MediaController and URI, then starting the videoView
//        videoView.setMediaController(mediaController);
            videoView.setVideoURI(uri);
            videoView.requestFocus();

            videoView.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setVolume(0,0);
                    mp.setLooping(true);
//                    mp.start();
                }
            });

            videoView.start();

//            rootView.findViewById(R.id.rl_inner_progress).setVisibility(View.GONE);
            try {
                rootView.findViewById(R.id.rl_inner_progress).animate().alpha(0.0f);
                if (!rootView.findViewById(R.id.rl_progress).isShown()) {
                    rootView.findViewById(R.id.ll_user_option).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
                } else {
                    setAlphaAnimation(rootView.findViewById(R.id.rl_progress));
                }
            } catch (Exception e) {
                e.printStackTrace();
                rootView.findViewById(R.id.ll_user_option).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
            }

            settingStore.setVideoLiveWallpaperPathTemp(dst);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    DownloadRequest downloadRequest1;

    boolean isDownloadeded;
    public void downloadFileVideo() {
        if(getActivity()==null || getActivity().isFinishing())
            return;
//        JesusApplication.getApplication().incrementUserClickCounter();
        WallpapersApplication.counter++;
        dst = CommonFunctions.createBaseDirectoryCache() + "/" + post.getPostId() + CommonFunctions.getExtension(post.getVid(),true);
//        dst = Common.getSavedFilePath() + "/live/"+JesusApplication.counter+".mp4";
        Logger.e(TAG, "Destination:" + dst);
        Logger.e(TAG, "Source:" + (CommonFunctions.getDomainImages()  + "live/" + post.getVid()));

        try {

            File file =new File(dst);
            if(file!=null && file.exists()){
                playVideo();
                return;
            }

            rootView.findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
            downloadManagerThin = new ThinDownloadManager(DOWNLOAD_THREAD_POOL_SIZE);

            RetryPolicy retryPolicy = new DefaultRetryPolicy();

            Uri downloadUri = Uri.parse(CommonFunctions.getDomainImages()  + "live/" + post.getVid());
//            Uri downloadUri = Uri.parse(Common.getDomainImages()  + "live/live"+JesusApplication.counter+".mp4");
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
                                    WallpapersApplication.getApplication().ratingCounter++;
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
//                                if(progress>50)
//                                    downloadManagerThin.cancelAll();
                                if(videoProgress!=null)
                                    videoProgress.setProgress(progress);
                                if(txt_curr_size!=null)
                                    txt_curr_size.setText(CommonFunctions.getFileSize(downloadedBytes) + " / ");
                                if(txt_total_size!=null)
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

    private WallpaperInfo old_info;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.e("onActivityResult","resultCode:"+resultCode+" requestCode:"+requestCode);
        try {
            if(requestCode==200) {
                if (resultCode == getActivity().RESULT_OK) {
                    settingStore.setVideoLiveWallpaperPath(dst);
                } else if (resultCode == getActivity().RESULT_CANCELED) {
                    if(AppConstant.isWallSetDirectly) {
                        WallpaperManager wpm = WallpaperManager.getInstance(getActivity());
                        WallpaperInfo info = wpm.getWallpaperInfo();

                        if(info == null)
                            return;
                        if(old_info!=null)
                            Logger.e("onActivityResult","Old:"+old_info.getComponent().getClassName());
                        Logger.e("onActivityResult","new:"+info.getComponent().getClassName());
                        Logger.e("onActivityResult","service:"+ LiveWallpaperService.class.getCanonicalName());

                        if (info != null && (old_info == null || (!old_info.getComponent().getClassName().equals(info.getComponent().getClassName())
                                && info.getComponent().getClassName().equals(LiveWallpaperService.class.getCanonicalName())))) {
                            Logger.e("onActivityResult","canceled:"+dst);
                            settingStore.setVideoLiveWallpaperPath(dst);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // -1 success 0 failed
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
                    if(rootView!=null) {
                        rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
                        videoView = (VideoView) rootView.findViewById(R.id.videoView1);
                        videoView.setVisibility(View.VISIBLE);
                    }
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

        rootView.findViewById(R.id.ll_user_option).setAlpha(0.0f);
        rootView.findViewById(R.id.ll_user_option).setVisibility(View.VISIBLE);
        setFadeINButton(rootView.findViewById(R.id.ll_user_option));
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

        title = null;
        thumbNail = null;
        layout_loading = null;
        txt_curr_size=null;
        txt_total_size=null;
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
//        dst=null;

        try {
            if(videoView!=null) {
                videoView.stopPlayback();
                videoView.setVideoURI(null);
                videoView=null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        old_info=null;
        videoProgress=null;
    }
}
