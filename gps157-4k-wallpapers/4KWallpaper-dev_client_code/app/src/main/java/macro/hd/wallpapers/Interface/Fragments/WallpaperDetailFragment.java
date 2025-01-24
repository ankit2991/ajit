package macro.hd.wallpapers.Interface.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import macro.hd.wallpapers.Interface.Adapters.WallDetailAdapter;
import macro.hd.wallpapers.Interface.Activity.PreviewActivity;
import macro.hd.wallpapers.Interface.Activity.BaseBannerActivity;
import macro.hd.wallpapers.BuildConfig;
import macro.hd.wallpapers.Interface.Activity.CategoryListingActivity;
import macro.hd.wallpapers.AppController.RequestManager;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.MyCustomView.GlideImageView;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.BlurImage;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.DiscreteScrollViewOptions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.SnackbarHelper;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.Wallpapers;
import macro.hd.wallpapers.Model.WallInfoModel;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventState;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.IEventListener;
import macro.hd.wallpapers.notifier.ListenerPriority;
import macro.hd.wallpapers.notifier.NotifierFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.RetryPolicy;
import com.thin.downloadmanager.ThinDownloadManager;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WallpaperDetailFragment extends Fragment implements DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder>, View.OnClickListener,IEventListener, NetworkCommunicationManager.CommunicationListnerNew {

    protected static final String TAG = WallpaperDetailFragment.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 1000;
    private View rootView;
    private UserInfoManager userInfoManager;

    SettingStore settingStore;
    DownloadManager downloadManager;
    private boolean isFromTranding;

    private List<Wallpapers> movieList = new ArrayList<Wallpapers>();
    protected DiscreteScrollView mRecyclerView;
    private int selectedPos=0;
    private int filter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userInfoManager = UserInfoManager.getInstance(getActivity().getApplicationContext());
        pagination_count= WallpapersApplication.getApplication().getSettings().getPost_count();
        String posId= null;

        boolean disableMore = false;
        if(getArguments()!=null){
            category_sel=getArguments().getString("category");
            filter=getArguments().getInt("filter");
            disableMore=getArguments().getBoolean("disableMore",false);
        }

        Logger.e(TAG, "category_sel: "+category_sel);

        try {
            post = (Wallpapers) getArguments().getSerializable("post");
            isFromTranding=getArguments().getBoolean("isTrending",false);
            if(getArguments().containsKey("post_list"))
                movieList= (List<Wallpapers>) getArguments().getSerializable("post_list");
            else
                movieList= WallpapersApplication.newList;

//            if (savedInstanceState != null && movieList==null) {
//                movieList= (List<Post>)savedInstanceState.getSerializable("post_list");
//            }

            posId = getArguments().getString("pos","");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                getActivity().finish();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        try {
            AppConstant.ADS_PER_PAGE_DETAIL= WallpapersApplication.getApplication().getSettings().getAdsPerPageDetail();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(movieList.size()%pagination_count==0)
                currentPage=movieList.size()/pagination_count;
            else
                isLastPage=true;
            Logger.e(TAG,"currentPage:"+currentPage+ " isLastPage:"+isLastPage);
        } catch (Exception e) {
            e.printStackTrace();
            isLastPage=true;
        }

        if(disableMore)
            isLastPage=true;

        try {
            if(!TextUtils.isEmpty(posId))
                for (int i = 0; i < movieList.size(); i++) {
                    if(movieList.get(i).getPostId().equalsIgnoreCase(posId)){
                        selectedPos=i;
                        break;
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

        settingStore = SettingStore.getInstance(getActivity().getApplicationContext());
        downloadManager = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);

        registerLoginInfoListener();

        Logger.e(TAG,"selectedPos:"+selectedPos);

        counter_final=selectedPos;
//        if((counter_final % AD_DISPLAY_FREQUENCY)==0)
//            counter_final = counter_final + 1;
//
//        if(((counter_final + 1)  % AD_DISPLAY_FREQUENCY)==0)
//            counter_final = counter_final + 2;

        Logger.e(TAG,"counter_final:"+counter_final);

        updateDataWithAd();
    }

    /**
     * Override method used to initialize the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_jesus_detail_final, container, false);
        return rootView;
    }

    public Wallpapers post;
    FloatingActionMenu actionMenu;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        AdView adView= (AdView) rootView.findViewById(R.id.adView);
//        LinearLayout AdContainer1= (LinearLayout) rootView.findViewById(R.id.AdContainer1);
//        JesusApplication.getApplication().requestBanner(adView,AdContainer1);
        initViewPager();
        actionListner();



    }

    FloatingActionButton fab_cat,fab_like ;
    private void actionListner(){
        FloatingActionButton fab_set_wallpaper,fab_share,fab_download,img_pinch_zoom,img_report;

        fab_cat = rootView.findViewById(R.id.fab_category);
        actionMenu=rootView.findViewById(R.id.menu_add);

        if(CategoryListingActivity.isShowSimilar){
            fab_cat.setVisibility(View.VISIBLE);
        }else
            fab_cat.setVisibility(View.GONE);

        fab_set_wallpaper = rootView.findViewById(R.id.fab_set_wallpaper);
        fab_share = rootView.findViewById(R.id.img_share);
        fab_download = rootView.findViewById(R.id.fab_download);
        fab_like = rootView.findViewById(R.id.fab_like);
        img_pinch_zoom = rootView.findViewById(R.id.img_pinch_zoom);
        img_report = rootView.findViewById(R.id.img_report);
        ImageView img_back = rootView.findViewById(R.id.img_back);
        FloatingActionButton img_info = rootView.findViewById(R.id.img_info);

        try {
            fab_cat.setOnClickListener(this);
            img_back.setOnClickListener(this);
            fab_set_wallpaper.setOnClickListener(this);
            fab_share.setOnClickListener(this);
            fab_download.setOnClickListener(this);
            fab_like.setOnClickListener(this);
            img_pinch_zoom.setOnClickListener(this);
            img_report.setOnClickListener(this);
            img_info.setOnClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageView ivBottomView;
        ivBottomView=rootView.findViewById(R.id.ivBottomView);

        int actionbarHeight = CommonFunctions.getActionBarHeight(getActivity());
        if (CommonFunctions.hasNavBar(getActivity())) {
            if (ivBottomView != null) {
                ivBottomView.getLayoutParams().height = actionbarHeight;
            }
        } else {
        }

        if(post==null)
            return;

        if(!TextUtils.isEmpty(post.getAuthor()) || !TextUtils.isEmpty(post.getLicense()) || !TextUtils.isEmpty(post.getSource_link())) {
            if(actionMenu.isOpened())
                img_info.setVisibility(View.VISIBLE);
            else
                img_info.setVisibility(View.INVISIBLE);
        }else
            img_info.setVisibility(View.GONE);
    }


    private void registerLoginInfoListener() {
        EventNotifier notifier =
                NotifierFactory.getInstance( ).getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_DOWNLOAD_INFO );
        notifier.registerListener( this, ListenerPriority.PRIORITY_HIGH );
    }

    private void unregisterLoginInfoListener( ) {
        EventNotifier notifier =
                NotifierFactory.getInstance( ).getNotifier(
                        NotifierFactory.EVENT_NOTIFIER_DOWNLOAD_INFO);
        notifier.unRegisterListener(this);
    }


    private boolean isForDownload;
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.img_back:
                getActivity().finish();
                break;
            case R.id.fab_category:

                if (!CommonFunctions.isNetworkAvailable(getActivity())) {
                    CommonFunctions.showInternetDialog(getActivity());
                    return;
                }

                final Intent intent=new Intent(getActivity(), CategoryListingActivity.class);
                intent.putExtra("category",post.getCategory());
                intent.putExtra("category_name",post.getCategory());
                WallpapersApplication.getApplication().incrementUserClickCounter();
                startActivity(intent);
                break;
//            case R.id.fab_preview:
//                pos= Integer.parseInt(view.getTag().toString());
//                post=movieList.get(pos);
//                isShared=false;
////              openBottomSheet();
//                break;
            case R.id.img_share:
                isShared=true;
                downloadFile("uhd");
//                openBottomSheet();
                break;
            case R.id.fab_download:
                isShared=false;
                isForDownload=true;
                downloadFile("uhd");
//                openBottomSheet();
                break;
            case R.id.fab_like:
                addToFav(selectedPos);
                break;
            case R.id.img_report:
                CommonFunctions.report(getActivity(),post.getPostId());
                break;
            case R.id.img_info:
                showInfo();
                break;
            case R.id.fab_set_wallpaper:
                isShared=false;
                is_set_wallpaper=true;
                isForDownload=false;
                downloadFile("uhd");
                break;
            case R.id.img_pinch_zoom:

                if (!CommonFunctions.isNetworkAvailable(getActivity())) {
                    CommonFunctions.showInternetDialog(getActivity());
                    return;
                }
                if(post==null)
                    return;

                isShared=false;
                Intent intent1=new Intent(getActivity(), PreviewActivity.class);
                String path = CommonFunctions.getDomainImages() + "uhd/" + post.getImg();
                try {
                    if(post.getImg().startsWith("http")) {
                        path=post.getImg();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                if(!TextUtils.isEmpty(dst))
//                    path=dst;

                intent1.putExtra("path",path);
                startActivity(intent1);
                break;
            case R.id.rl_photos:

//                if(rootView.findViewById(R.id.fab_download).getVisibility()==View.VISIBLE){
//                    rootView.findViewById(R.id.fab_like).setVisibility(View.GONE);
//                    rootView.findViewById(R.id.fab_download).setVisibility(View.GONE);
//                    rootView.findViewById(R.id.menu_add).setVisibility(View.GONE);
//
//                    ((JesusDetailActivity)getActivity()).hideToolbar();
//
//                }else{
//
//                    if(type== AppConstant.TYPE_PIXELS || type== AppConstant.TYPE_PIXABAY){
//                        rootView.findViewById(R.id.fab_like).setVisibility(View.GONE);
//                    }else{
//                        rootView.findViewById(R.id.fab_like).setVisibility(View.VISIBLE);
//                    }
//
//                    rootView.findViewById(R.id.fab_download).setVisibility(View.VISIBLE);
//                    rootView.findViewById(R.id.menu_add).setVisibility(View.VISIBLE);
//
//                    ((JesusDetailActivity)getActivity()).showToolbar();
////                    showStatusBar();
//                }

                break;
        }
    }

    private AlertDialog dialog;
    /**
     * Gender selection pop-up box
     */
    private void showInfo() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_for_info, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        TextView txt_source_link=((TextView)view.findViewById(R.id.txt_source_link));

        if(!TextUtils.isEmpty(post.getAuthor()))
            ((TextView)view.findViewById(R.id.txt_author)).setText(post.getAuthor());
        else {
            ((TextView) view.findViewById(R.id.txt_author)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.title_author)).setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(post.getSource_link()))
            txt_source_link.setText(post.getSource_link());
        else {
            txt_source_link.setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.title_source)).setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(post.getLicense()))
            ((TextView)view.findViewById(R.id.txt_licence)).setText(post.getLicense());
        else {
            ((TextView) view.findViewById(R.id.txt_licence)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.title_licence)).setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(post.getDownload()))
            ((TextView)view.findViewById(R.id.txt_download)).setText(post.getDownload());
        else {
            ((TextView) view.findViewById(R.id.txt_download)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.title_download)).setVisibility(View.GONE);
        }

        txt_source_link.setPaintFlags(txt_source_link.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        txt_source_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri webpage = Uri.parse(post.getSource_link());
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        dialog = builder.create();
        dialog.setView(view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();
    }

    private boolean isShared,is_set_wallpaper;

//    BottomSheetDialog dialog;
//    private void openBottomSheet(){
//        View viewdial = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
//
//        viewdial.findViewById(R.id.ll_4k).setOnClickListener(this);
//        viewdial.findViewById(R.id.ll_hd).setOnClickListener(this);
//
//        dialog = new BottomSheetDialog(getActivity());
//        dialog.setContentView(viewdial);
//        dialog.show();
//    }


    public static boolean isDownloading;
    private String dst;
    public void downloadFile(String type) {

        if (!CommonFunctions.isNetworkAvailable(getActivity())) {
            CommonFunctions.showInternetDialog(getActivity());
            return;
        }

        if(!isPermissionGranted()){
            return;
        }

        if(post==null)
            return;

//        JesusApplication.getApplication().incrementUserClickCounter();
        if(dialog!=null && dialog.isShowing())
            dialog.dismiss();

        dst = CommonFunctions.getSavedFilePath()+"/"+post.getPostId()+"_"+type+ CommonFunctions.getExtension(post.getImg(),false);

        File file=new File(dst);

        Logger.e(TAG,"Destination:"+dst);

        if(file!=null && file.exists()){
            if(isShared)
                CommonFunctions.sharedPost(getActivity(), "", dst, "", false);
            else {
                if(isForDownload){
                    isForDownload = false;
                    showSnackBar();
                }else
                    setWallpaper();
//                if(!is_set_wallpaper)
//                    Toast.makeText(getActivity(), "Already Downloaded at " + dst, Toast.LENGTH_SHORT).show();
                is_set_wallpaper=false;
            }
            return;
        }

        try {

            ((BaseBannerActivity)getActivity()).showLoadingDialogDownload(getActivity().getResources().getString(R.string.download_text));
            ((BaseBannerActivity)getActivity()).getTv().setText("0%");

            String path= CommonFunctions.getDomainImages()+type+"/"+post.getImg();
            if(post.getImg().startsWith("http")) {
                path=post.getImg();
            }
//            Logger.e(TAG,"path:"+path);
            initializeDownload(dst,path);
//            isDownloading=false;

            if(!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getFb_ad_download()) && WallpapersApplication.getApplication().getSettings().getFb_ad_download().equalsIgnoreCase("1")){
                if(!WallpapersApplication.getApplication().fb_display_detail && !WallpapersApplication.getApplication().isAdDisplayBefore) {
                    WallpapersApplication.getApplication().fb_display_detail=true;
                    WallpapersApplication.getApplication().loadAdmobDownloadAd();
                }
            }



//            Uri Download_Uri = Uri.parse(Common.getDomainImages()+type+"/"+post.getImg());
//
//            Logger.e(TAG,"Download "+Common.getDomainImages()+type+"/"+post.getImg());
//
//            DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
//            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
//            request.setAllowedOverRoaming(true);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                request.setAllowedOverMetered(true);
//            }
//            request.setTitle(post.getImg());
//            request.setDescription(post.getCategory());
//            request.setVisibleInDownloadsUi(true);
//
//            request.setDestinationInExternalPublicDir("/4kWallpaper", "/Images/"+post.getPostId() +"_"+type+ AppConstant.DOWNLOAD_EXTENTION);
//
//            long refid = downloadManager.enqueue(request);
//
//            new DownloadProgressCounter(refid).start();


            post.setProgress("0");
            post.setDownload_id(""+downloadId1);

            setDownload();

//            String lastMsg=settingStore.getImages();
//            if(TextUtils.isEmpty(lastMsg)){
//                settingStore.setImages(dst);
//            }else{
//                settingStore.setImages(lastMsg+"#"+dst);
//            }

            CommonFunctions.addMessageToDatabase(getActivity(),post);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.unable_download),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int eventNotify(int eventType, final Object eventObject) {
        int eventState = EventState.EVENT_IGNORED;
        switch (eventType) {
            case EventTypes.EVENT_DOWNLOAD_COMPLETED:

                eventState = EventState.EVENT_PROCESSED;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String postid= (String) eventObject;
                        if(!TextUtils.isEmpty(postid) && !postid.equalsIgnoreCase(post.getPostId())){
                            return;
                        }

                        if(TextUtils.isEmpty(dst))
                            return;

                        Logger.e("eventNotify",postid);
//                        if(postid.equalsIgnoreCase(post.getPostId())){

                            CommonFunctions.scanToGallery(getActivity().getApplicationContext(),dst);

                            ((BaseBannerActivity) getActivity()).dismissLoadingProgress();

                            post.setDownloading(false);
//                            updateDownloadButton();

                            if(!TextUtils.isEmpty(post.getCategory()) && !post.getCategory().equalsIgnoreCase("-1"))
                                EventManager.sendEvent("Download","category",""+post.getCategory());

                            if(isShared)
                                CommonFunctions.sharedPost(getActivity(), "", dst, "", false);
                            else {
                                if(isForDownload){
                                    isForDownload = false;
                                    showSnackBar();
                                }else
                                    setWallpaper();

//                                if(!is_set_wallpaper)
//                                    Toast.makeText(getActivity(), "Downloaded at " + dst, Toast.LENGTH_SHORT).show();

                                is_set_wallpaper=false;
                            }
//                        if(!TextUtils.isEmpty(JesusApplication.getApplication().getSettings().getFb_ad_download()) && JesusApplication.getApplication().getSettings().getFb_ad_download().equalsIgnoreCase("1")) {
//                            if (!JesusApplication.getApplication().isFBAdLoaded()) {
//                                JesusApplication.getApplication().showAdDirectly = true;
//                            }
//                        }
                    }
                });
                break;
        }
        return eventState;
    }


    private void showSnackBar(){

        Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.dwn_at) + dst,Toast.LENGTH_SHORT).show();

//        Snackbar snack = Snackbar.make(rootView.findViewById(R.id.activity_main), getActivity().getResources().getString(R.string.dwn_at) + dst, Snackbar.LENGTH_LONG).setAction(getActivity().getResources().getString(R.string.label_show), new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_VIEW);
//                    intent.setDataAndType(Uri.parse("file://" + dst), "image/*");
//                    startActivity(intent);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        SnackbarHelper.configSnackbar(getActivity(), snack, CommonFunctions.getNavigationBarHeight(getActivity()));
//
//        View view = snack.getView();
//
////                                    view.setBackgroundColor(Color.BLACK);
//        TextView textView = (TextView) view.findViewById(R.id.snackbar_text);
//        Button snackViewButton = (Button) view.findViewById(R.id.snackbar_action);
//
//        textView.setTextColor(Color.WHITE);
//        snackViewButton.setTextColor(getResources().getColor(R.color.snack_action_color));
//
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
//        params.gravity = Gravity.TOP;
//        view.setLayoutParams(params);
//
//        snack.show();
    }

    private void setViewCountToServer(Wallpapers post){

//        if(groupManager.isViewed(post.getPostId()))
//            return;

        userInfoManager.addedToView(post.getPostId());

        SettingStore settingStore=SettingStore.getInstance(getActivity().getApplicationContext());
        if(settingStore.getViewCount().equalsIgnoreCase(""))
            settingStore.setViewCount(post.getPostId());
        else{
            if(!settingStore.getViewCount().contains(post.getPostId())){
                settingStore.setViewCount(settingStore.getViewCount()+"_"+post.getPostId());
            }
        }
    }

    private void setDownload(){

        if(isFromTranding)
            return;

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
    }

    private void setWallpaper(){

        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(Uri.fromFile(new File(dst)), "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("mimeType", "image/*");
        startActivity(Intent.createChooser(intent, getActivity().getResources().getString(R.string.label_set_dialog)));
    }

    public void addToFav(int pos){
        if(movieList==null)
            return;
        try {
            Wallpapers post=movieList.get(pos);
            if(!CommonFunctions.isFavoriteAdded(userInfoManager,post.getPostId())) {
                Logger.e(TAG,"LinkId:"+post.getPostId());
                CommonFunctions.setFavorite(settingStore,post);
                UserInfoManager groupManager = UserInfoManager.getInstance(getActivity().getApplicationContext());
                groupManager.addedToFav(post);
    //            movieList.set(pos,post);
                fab_like.setImageResource(R.mipmap.ic_detail_like_s);
    //            jesusDetailAdapter.notifyItemChanged(pos);
            }else{
                Logger.e(TAG,"unLinkId:"+post.getPostId());
                CommonFunctions.setUnFavorite(settingStore, post);
    //            post.setIs_fav("0");
                UserInfoManager groupManager = UserInfoManager.getInstance(getActivity().getApplicationContext());
                groupManager.removeFromGroup(post);
                fab_like.setImageResource(R.mipmap.ic_detail_like);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initializeDownload(String pathToDownload,String downloadURL){
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
                EventManager.sendEvent("4K Wallpaper","Download Wallpaper","Success");
                EventNotifier notifier =
                        NotifierFactory.getInstance( ).getNotifier(
                                NotifierFactory.EVENT_NOTIFIER_DOWNLOAD_INFO);
                notifier.eventNotify( EventTypes.EVENT_DOWNLOAD_COMPLETED, post.getPostId());

            }
        }

        @Override
        public void onDownloadFailed(DownloadRequest request, int errorCode, String errorMessage) {
            try {
                final int id = request.getDownloadId();
                if (id == downloadId1) {
                    ((BaseBannerActivity) getActivity()).dismissLoadingProgress();
                    Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.unable_download)+errorMessage,Toast.LENGTH_SHORT).show();
                    EventManager.sendEvent("4K Wallpaper","Download Wallpaper","Fail:"+errorMessage);
                    EventManager.sendEvent("4K Wallpaper","Fail Id",""+post.getPostId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProgress(DownloadRequest request, long totalBytes, long downloadedBytes, int progress) {
            int id = request.getDownloadId();

            System.out.println("######## onProgress ###### "+id+" : "+totalBytes+" : "+downloadedBytes+" : "+progress);
            if (id == downloadId1) {
                try {
                    if(((BaseBannerActivity)getActivity()).getProgress()!=null){
                        ((BaseBannerActivity)getActivity()).getProgress().setCurrentProgress(progress);
                        ((BaseBannerActivity)getActivity()).getTv().setText(""+progress +"%");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    WallDetailAdapter jesusDetailAdapter;
    protected void initViewPager() {
        mRecyclerView = (DiscreteScrollView) rootView.findViewById(R.id.viewpager);

        mRecyclerView.setOrientation(DSVOrientation.HORIZONTAL);

        jesusDetailAdapter=new WallDetailAdapter(getActivity(), movieList);
//        jesusDetailAdapter = InfiniteScrollAdapter.wrap(tempAdapter);

        try {
            Point windowDimensions = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(windowDimensions);
            Logger.e("JesusDetailAdapterNew","y:"+windowDimensions.y + " X:"+windowDimensions.x);
            int itemHeight = Math.round(windowDimensions.y * 0.8f);
            int itemWidht = Math.round(windowDimensions.x * 0.75f);
            int itemHeight_diment= (int) getActivity().getResources().getDimension(R.dimen.detail_tile_hieght);
            int itemWidht_diment= (int)  getActivity().getResources().getDimension(R.dimen.detail_tile_witdh);
            Logger.e("JesusDetailAdapterNew","y:"+windowDimensions.y + " X:"+windowDimensions.x+ " itemHeight:"+itemHeight+" itemWidht:"+itemWidht+" itemHeight_diment:"+itemHeight_diment + " itemWidht_diment:"+itemWidht_diment);
            jesusDetailAdapter.setItemWidhtHeight(itemWidht,itemHeight);

            RelativeLayout rl_actions = rootView.findViewById(R.id.rl_actions);
            rl_actions.getLayoutParams().height = itemHeight;
            rl_actions.getLayoutParams().width = itemWidht;
        } catch (Exception e) {
            e.printStackTrace();
        }

        mRecyclerView.setAdapter(jesusDetailAdapter);
        mRecyclerView.addOnItemChangedListener(this);
//        mRecyclerView.setSlideOnFling(true);
//        mRecyclerView.scrollToPosition(2);
        mRecyclerView.setItemTransitionTimeMillis(DiscreteScrollViewOptions.getTransitionTime());
        mRecyclerView.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.85f)
                .build());

        if(movieList!=null)
            lastPos=movieList.size()+1;
        mRecyclerView.scrollToPosition(selectedPos);

    }


    private void getPostListResponse() {

        RequestManager manager = new RequestManager(getActivity());

        String APIFinal=(AppConstant.IS_ASC?AppConstant.URL_POST_LIST_ASC:AppConstant.URL_POST_LIST);
        //Common.getDomain()+ ((!TextUtils.isEmpty(category) && category.equalsIgnoreCase("-1"))?AppConstant.URL_GET_TRENDING:AppConstant.URL_POST_LIST)
        String api= CommonFunctions.getDomain()+ ((!TextUtils.isEmpty(category_sel) && category_sel.equalsIgnoreCase("-1"))?AppConstant.URL_GET_TRENDING:APIFinal);

        String used_ids=getUsedIds();
        manager.getPostListService(api,""+currentPage, CommonFunctions.getHardwareId(getActivity()),"",category_sel,used_ids,"",filter,this);
    }


    private String category_sel="";
    private int currentPage = 2;
    private String getUsedIds(){
        if (currentPage == 1)
            return "";
        String finalString = null;
//		int counter = 0;
        StringBuffer ids = new StringBuffer();
        if (movieList != null)
            for (int i = 0; i < movieList.size(); i++) {
                if (movieList.get(i).getNativeAd() == null && !movieList.get(i).getPostId().equalsIgnoreCase("-99")) {
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
        return finalString;
    }

    private void setDataPageChange(){
//        fab_cat.setLabelText(Common.capitalize(post.getCategory()));

        if (CommonFunctions.isFavoriteAdded(userInfoManager, post.getPostId())) {
            fab_like.setImageResource(R.mipmap.ic_detail_like_s);
        }else
            fab_like.setImageResource(R.mipmap.ic_detail_like);

    }

    private boolean isVideoWall(){
        if(post!=null && !TextUtils.isEmpty(post.getVid()))
            return true;
        return false;
    }

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
                downloadFile("uhd");
            }else if (grantResults!=null && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                downloadFile("uhd");
                Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.allow_per),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStartLoading() {
        ((BaseBannerActivity) getActivity()).showLoadingDialogMsg(getActivity().getResources().getString(R.string.load_more));
    }

    @Override
    public void onFail(final WebServiceError errorMsg) {
        Logger.e("onFail","onFail");
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((BaseBannerActivity) getActivity()).dismissLoadingProgress();
//					Common.showDialog(getActivity(), getString(R.string.error_title), errorMsg.getDescription(), "Ok");
                }
            });
    }

    private boolean isLastPage = false;
    int pagination_count;
    private int dataInsertedCount;
    private int lastPos=0;
    @Override
    public void onSuccess(final IModel response, int operationCode) {

        if (getActivity() != null && !getActivity().isFinishing())
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ((BaseBannerActivity) getActivity()).dismissLoadingProgress();

                    if (getActivity() == null || getActivity().isFinishing())
                        return;
                    try {

                        WallInfoModel model = (WallInfoModel) response;
                        if (model != null && model.getStatus().equalsIgnoreCase("1")) {
                            if(model!=null && model.getPost()!=null ) {
                                if(currentPage==1) {
                                    movieList.clear();
                                }
                                movieList.addAll(model.getPost());
                                dataInsertedCount=model.getPost().size();
                            }

                            updateDataWithAd();
                            boolean pagination_end = false;
                            if(model.getPost()==null || pagination_count != model.getPost().size()){
                                pagination_end=true;
                            }

                            if (!pagination_end) {
                                isLastPage = false;
                            }else {
                                isLastPage = true;
                            }

                            if(jesusDetailAdapter!=null && movieList.size()>0) {
                                Logger.e(TAG,"lastPos: "+lastPos+" dataInsertedCount:"+dataInsertedCount);
                                jesusDetailAdapter.notifyItemRangeInserted(lastPos,dataInsertedCount);
                                lastPos=movieList.size()+1;
                            }

                        } else if (model != null && model.getStatus().equalsIgnoreCase("0")) {
                            CommonFunctions.showDialog(getActivity(), getString(R.string.error_title), model.getMsg(), "Ok");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
    }


    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int newPosition) {
        try {
//                    int newPosition = jesusDetailAdapter.getRealPosition(adapterPosition);
            Logger.e(TAG,"OnPageChanged: "+newPosition+" isLastPage:"+isLastPage);
            Logger.e(TAG,"OnPageChanged: "+movieList.size());

            if ((newPosition == movieList.size() - 1) && !isLastPage) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null && !getActivity().isFinishing()) {
                                Logger.e(TAG, "MoreDataCall: ");
                                currentPage += 1;
                                getPostListResponse();
                            }
                        }
                    },100);
            }


            selectedPos=newPosition;
            post=movieList.get(newPosition);
            if(post.getNativeAd()!=null){
                rootView.findViewById(R.id.rl_actions).setVisibility(View.GONE);
                getActivity().findViewById(R.id.AdContainer1).setVisibility(View.INVISIBLE);
            }else {
                getActivity().findViewById(R.id.AdContainer1).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.rl_actions).setVisibility(View.VISIBLE);

                setDataPageChange();
                if (!isFromTranding)
                    setViewCountToServer(movieList.get(newPosition));

                loadBlurrImage(post);

                if(!TextUtils.isEmpty(post.getAuthor()) || !TextUtils.isEmpty(post.getLicense()) || !TextUtils.isEmpty(post.getSource_link())) {
                    if(actionMenu.isOpened())
                        rootView.findViewById(R.id.img_info).setVisibility(View.VISIBLE);
                    else
                        rootView.findViewById(R.id.img_info).setVisibility(View.INVISIBLE);

                }else
                    rootView.findViewById(R.id.img_info).setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  static Bitmap resource;
    private void loadBlurrImage(Wallpapers post){
        String path = CommonFunctions.getDomainImages() + "thumb/" + post.getImg();
        if(!TextUtils.isEmpty(post.getImg()) && post.getImg().startsWith("http")) {
            path=post.getSearch_thumb();
        }
//		Logger.e("path","path:"+path);
        if (!TextUtils.isEmpty(path))
            Glide.with(getActivity())
					.asBitmap()
					.load(path).apply(new RequestOptions().override(600,300).centerCrop())
					.into(new SimpleTarget<Bitmap>() {
						@Override
						public void onResourceReady(final Bitmap resource, Transition<? super Bitmap> transition) {
							try {
                                WallpaperDetailFragment.this.resource=resource;
                                setBlurrImage(resource);
							} catch (Exception e) {
								e.printStackTrace();
							}catch (Error e) {
								e.printStackTrace();
							}
						}
					});
    }


    public void setBlurrImage(final Bitmap bitmap){
        try {
            final GlideImageView imageOriginal=getActivity().findViewById(R.id.img_blurre);
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
//                                                Drawable d = new BitmapDrawable(getResources(), temp);
                                                imageOriginal.setImageBitmap(temp);
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
    }

    public static final int AD_DISPLAY_FREQUENCY = 2;
    private int addedCount=0,extraLocationCounter=0;
    int counter_final = 0;
    private boolean isAlreadyAdded;


    private void updateDataWithAd() {
        try {
// comment advertise
//            if(JesusApplication.getApplication().getGoogleNativeAdDetail().size()<=0)
//                return;
//
//            int counter = counter_final;
//            Logger.e("counter",""+counter);
//            int final_freq=AD_DISPLAY_FREQUENCY;
//
//            if(movieList==null || movieList.size()==0)
//                return;
//
//            for (int i = counter; i < movieList.size(); i++) {
//                if (extraLocationCounter == final_freq) {
//                    extraLocationCounter=0;
//                    Object ad;
//
//                    if(JesusApplication.getApplication().getGoogleNativeAdDetail().size()<=addedCount){
//                        ad=null;
//                    }else
//                        ad = JesusApplication.getApplication().getGoogleNativeAdDetail().get(JesusApplication.getApplication().getCurrentAdDisplayCountDetail());
//
//                    Logger.e("nextNativeAd","nextNativeAd  pos:"+i+" counter:"+counter);
//                    try {
//                        if(ad!=null) {
//                            Post temp=new Post();
//                            isAlreadyAdded=true;
//                            temp.setNativeAd(ad);
//
//                            movieList.add(counter + addedCount, temp);
//                            addedCount++;
//
//                            if(addedCount==AppConstant.ADS_PER_PAGE_DETAIL)
//                            {
//                                break;
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }else
//                    extraLocationCounter++;
//                counter++;
//            }
//            counter_final=counter;
//
//            Logger.e("results.size() after ",""+movieList.size());
//            Logger.e("counter_final",""+counter_final);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e(TAG, "onDestroy");

        unregisterLoginInfoListener();

        resource=null;
        fab_cat=null;fab_like=null;
        rootView = null;
        userInfoManager = null;
        settingStore = null;
//        if(jesusDetailAdapter!=null)
//            jesusDetailAdapter.destroy();
        jesusDetailAdapter=null;
        mRecyclerView=null;
        dst=null;
        actionMenu=null;
    }
}
