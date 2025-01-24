package macro.hd.wallpapers.Interface.Activity;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Interface.Adapters.AllInOneAdapter;
import macro.hd.wallpapers.AppController.RequestManager;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EndlessRecyclerViewAdapter;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.SpacesItemDecoration;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.Category;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.Wallpapers;
import macro.hd.wallpapers.Model.WallInfoModel;
import macro.hd.wallpapers.Model.SearchInfoModel;

import com.mancj.materialsearchbar.MaterialSearchBar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SearchActivity extends BaseActivity implements  MaterialSearchBar.OnSearchActionListener , NetworkCommunicationManager.CommunicationListnerNew, EndlessRecyclerViewAdapter.RequestToLoadMoreListener{

    MaterialSearchBar searchBar;
    private String search_last="";
    int type=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setLocale();
        setContentView(R.layout.activity_search);
        settingStore= SettingStore.getInstance(this);
        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(this);

        Resources.Theme themes = getTheme();
        TypedValue storedValueInTheme = new TypedValue();
        if (themes.resolveAttribute(R.attr.my_textColor, storedValueInTheme, true)) {
            searchBar.setNavIconTint(storedValueInTheme.data);
        }

        try {
            searchBar.setRoundedSearchBarEnabled(true);

            EditText yourEditText=searchBar.getSearchEditText();
            yourEditText.setCursorVisible(true);
            // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(yourEditText, R.drawable.search_cursor);
        } catch (Exception ignored) {
        }
//        type = AppConstant.TYPE_PIXELS;
        searchBar.setClearIconTint(getResources().getColor(R.color.main_text_color));
        searchBar.setHint(getString(R.string.label_searchbytext));
        Log.d("LOG_TAG", getClass().getSimpleName() + ": text " + searchBar.getText());
//        searchBar.setCardViewElevation(30);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            searchBar.setElevation(30);
//        }
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Logger.e(TAG, getClass().getSimpleName() + " text changed " + searchBar.getText());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

//        searchBar.enableSearch();
//        searchBar.setSpeechMode(true);
        searchBar.openSearch();

        recyclerView = (RecyclerView) findViewById(R.id.list);
        try {
            pagination_count= WallpapersApplication.getApplication().getSettings().getPost_count();
        } catch (Exception e) {
            e.printStackTrace();
            pagination_count=30;
        }

        EventManager.sendEvent(EventManager.LBL_SEARCH,EventManager.ATR_KEY_SEARCH_OPEN,"Search Open");

        try {
            AppConstant.ADS_PER_PAGE= WallpapersApplication.getApplication().getSettings().getAdsPerPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String category_id;
    private void getCategoryId(){
        category_id="";
        try {
            List<Category> temp = UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getCategory();
            if(temp!=null && temp.size()>0){
                for (int i = 0; i < temp.size(); i++) {
                    Category category=temp.get(i);
                    if(!TextUtils.isEmpty(category.getTags())) {
                        String[] split = category.getTags().split("#");
                        List<String> stringList = new ArrayList<String>(Arrays.asList(split)); //new ArrayList is only needed if you absolutely need an ArrayList
                        if(stringList.contains(search_last)){
                            category_id=category.getName();
                            Logger.e(TAG, " getCategoryId:" + category_id);
                            Logger.e(TAG, " getCategorynamed:" + category.getDisplay_name());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        Logger.e(TAG, " onSearchStateChanged:" + enabled);
        if(!enabled)
            finish();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        CommonFunctions.hideKeyboard(this);
        finish();

    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        Logger.e(TAG, " onSearchConfirmed:" + text);
        if(!TextUtils.isEmpty(text)) {
            resetContent();
            search_last = text.toString().trim().toLowerCase();
            CommonFunctions.hideKeyboard(this);
            listTypeCalled.clear();
            if(isValidKeyword(search_last)) {
                CommonFunctions.setSearchKeyword(settingStore,search_last);
                getPostListResponse();
            }else{
                if(movieList!=null)
                    movieList.clear();
                if(adapter!=null)
                    adapter.notifyDataSetChanged();
                noDataLayout(getString(R.string.label_try_something_else));
            }
            EventManager.sendEvent(EventManager.LBL_SEARCH,EventManager.ATR_KEY_SEARCH_KEYWORD,"Search Perform");
        }
    }

    private boolean isValidKeyword(String searchKey){
        String keyword= WallpapersApplication.getApplication().getSettings().getSearch_keywords();
        String[] split=keyword.split(",");
        for (int i = 0; i < split.length; i++) {
            if(!TextUtils.isEmpty(split[i]))
                if(searchKey!=null && searchKey.contains(split[i].toLowerCase())) {
                    return false;
                }
        }
        return true;
    }

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            try {
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int firstVisibleItemPositions = linearLayoutManager.findFirstVisibleItemPosition();

                if ((firstVisibleItemPositions + visibleItemCount) >= totalItemCount
                        && firstVisibleItemPositions >= 0) {
                    if(CommonFunctions.isNetworkAvailable(SearchActivity.this))
                        onLoadMoreRequested();
                    else
                        Toast.makeText(SearchActivity.this,getString(R.string.error_msg_no_network),Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private boolean isLiveWallpaper(String searchKey){
        boolean isFound = false;
        String keyword="live,live wallpaper,video,video wallpaper";
        String[] split=keyword.split(",");
        for (int i = 0; i < split.length; i++) {
            if(!TextUtils.isEmpty(split[i]))
                if(searchKey!=null && searchKey.contains(split[i].toLowerCase())) {
                    isFound=true;
                    break;
                }
        }
        return isFound;
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
//                finish();
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                break;
            case MaterialSearchBar.BUTTON_BACK:
                CommonFunctions.hideKeyboard(this);
                finish();
//                searchBar.disableSearch();
                break;
        }
    }



    private boolean isVideoWall;
    private void getPostListResponse() {

        try {
            if(WallpapersApplication.getApplication().getSettings().Is_search_category()==1)
                getCategoryId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            isVideoWall=isLiveWallpaper(search_last);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestManager manager = new RequestManager(this);
        if(!TextUtils.isEmpty(category_id)){
            isVideoWall=false;
            isCalledWebService=true;
            findViewById(R.id.rl_no_content).setVisibility(View.GONE);
            isLoading=true;
            String api= CommonFunctions.getDomain()+ AppConstant.URL_SEARCH;
            String used_ids=getUsedIds();

            manager.getPostListService(api,""+currentPage, CommonFunctions.getHardwareId(this),search_last,category_id,used_ids,"",2,this);
        }else if(isVideoWall){
            isCalledWebService=true;
            findViewById(R.id.rl_no_content).setVisibility(View.GONE);
            isLoading=true;
            String api= CommonFunctions.getDomain()+ AppConstant.URL_IN_VIDEO_WALLPAPER;
            String used_ids=getUsedIds();
            manager.getPostListService(api,""+currentPage, CommonFunctions.getHardwareId(this),search_last,category_id,used_ids,"",2,this);
        }else{
            isVideoWall=false;
            isCalledWebService=true;
            findViewById(R.id.rl_no_content).setVisibility(View.GONE);
            isLoading=true;
            String api= CommonFunctions.getDomain()+ AppConstant.URL_SEARCH_WALLPAPER;
            String used_ids=getUsedIds();
            manager.getSearchService(api,""+currentPage, CommonFunctions.getHardwareId(this),search_last,used_ids,this);

//            if(WallpapersApplication.getApplication().getSettings().Is_search_external_api()==0){
//                 if(movieList!=null)
//                    movieList.clear();
//                if(adapter!=null)
//                    adapter.notifyDataSetChanged();
//                noDataLayout(getString(R.string.label_try_something_else));
//            }else{
//                isVideoWall=false;
//                if(!listTypeCalled.contains(type))
//                    listTypeCalled.add(type);
//                findViewById(R.id.rl_no_content).setVisibility(View.GONE);
//                isLoading=true;
//                isCalledWebService=true;
//
//                String api="";
//                String final_search=search_last.replace(" ","%20");
//                if(type==AppConstant.TYPE_PIXABAY)
//                    api = AppConstant.PIXABAY_SEARCH.replace("%query%",final_search).replace("currentpage",""+currentPage);
//                else if(type==AppConstant.TYPE_PIXELS)
//                    api = AppConstant.PIXELS_SEARCH.replace("%query%",final_search).replace("currentpage",""+currentPage);
//
//                manager.getSearch(api,type,this);
//            }
        }
    }

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

    public void showDialogue() {
        if(currentPage==1)
            findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
    }

    public void dismissDialogue() {
        findViewById(R.id.rl_progress).setVisibility(View.GONE);
    }

    @Override
    public void onStartLoading() {
        if(isNeedToDisplayLoading) {
            isNeedToDisplayLoading=false;
            showDialogue();
        }
    }

    ArrayList<Integer> listTypeCalled=new ArrayList<>();
    @Override
    public void onFail(final WebServiceError errorMsg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.e("onFail","");
                if(listTypeCalled.size()<2){
                    getPostListResponse();
                    return;
                }

                removeLoading();
                dismissDialogue();
                isLoading=false;
                if(adapter!=null){
                    adapter.notifyDataSetChanged();
                }
//					Common.showDialog(getActivity(), getString(R.string.error_title), errorMsg.getDescription(), "Ok");
            }
        });
    }

    @Override
    public void onSuccess(final IModel response, int operationCode) {
        if (isFinishing()) {
            return;
        }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isFinishing())
                        return;
                    dismissDialogue();
                    try {
                        removeLoading();

                        IModel iModel=response;
                        List<Wallpapers> temp;
                        String status,msg;
                        if(response instanceof WallInfoModel) {
                             WallInfoModel model = (WallInfoModel) response;
                            temp=model.getPost();
                            status=model.getStatus();
                            msg=model.getMsg();
                        }else {
                            SearchInfoModel model = (SearchInfoModel) response;
                            temp=model.getPost();
                            status=model.getStatus();
                            msg=model.getMsg();
                        }

                        Logger.e("onSuccess",""+status);
                        if (iModel != null && status.equalsIgnoreCase("1")) {
                            if(currentPage==1 && movieList!=null) {
                                movieList.clear();
                            }
                            if(temp!=null && temp.size()>0) {

                                int size_count=temp.size();

                                try {
                                    Iterator itr = temp.iterator();
                                    while (itr.hasNext()) {
                                        Wallpapers post = (Wallpapers) itr.next();
                                        if (post !=null){
                                            if(!isValidKeyword(post.getTags()))
                                                itr.remove();
                                            else
                                                Logger.e("onSuccess",post.getTags());
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                movieList.addAll(temp);

                                boolean pagination_end = false;
                                if(pagination_count != size_count){
                                    pagination_end=true;
                                }

                                dataInsertedCount=temp.size();

                                if (!pagination_end) {
                                    isLastPage = false;
                                    moreData = true;
                                }else {
                                    isLastPage = true;
                                    moreData = false;
                                }

                                if(currentPage>1 && (temp==null || temp.size()==0)){
                                    if(adapter!=null && movieList.size()>0)
                                        adapter.notifyDataSetChanged();
                                }else {
                                    fillupAdapter();
                                }
                                if(currentPage==1)
                                    recyclerView.scrollToPosition(0);
                            }else{
                                if(adapter!=null){
                                    adapter.notifyDataSetChanged();
                                }
                                if(movieList==null || movieList.size()==0)
                                    noDataLayout(getString(R.string.label_try_something_else));
                            }
                        } else if (iModel != null && status.equalsIgnoreCase("0")) {
                            if(adapter!=null){
                                adapter.notifyDataSetChanged();
                            }
                            CommonFunctions.showDialog(SearchActivity.this, getString(R.string.error_title), msg, getString(R.string.label_ok));
                        }else{
                            if(adapter!=null){
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception e) {
                        if(adapter!=null){
                            adapter.notifyDataSetChanged();
                        }
                        e.printStackTrace();
                    }
                }
            });
    }

    GridLayoutManager linearLayoutManager;
    private int dataInsertedCount;
    private void fillupAdapter(){

        Logger.e("currentPage",""+currentPage);
        Logger.e("lastPos",""+lastPos);
        Logger.e("results.size()",""+movieList.size());
        Logger.e("difference",""+(movieList.size()-lastPos));
        Logger.e("pagination_count",""+pagination_count);

        if(movieList!=null && movieList.size()>0){

            if(isCalledWebService)
                updateDataWithAd();

            if(moreData) {
                Wallpapers chatMessage = new Wallpapers();
                chatMessage.setPostId("-99");
                movieList.add(chatMessage);
                dataInsertedCount++;
            }

            findViewById(R.id.rl_no_content).setVisibility(View.GONE);
            if(recyclerView!=null)
                recyclerView.setVisibility(View.VISIBLE);
            if (adapter == null) {
                lastPos=movieList.size()+1;
                adapter = new AllInOneAdapter(this, movieList,2);
                adapter.setFromTrending(false);
                adapter.setVideoWall(isVideoWall);
//				adapter.setNativeAdsManager(mNativeAdsManager);

                linearLayoutManager = new GridLayoutManager(this,AppConstant.TILE_SIZE);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

                if(linearLayoutManager!=null)
                    linearLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
//								Logger.e("position",""+position);
                            try {
                                if(movieList!=null && movieList.size()>0 && !TextUtils.isEmpty(movieList.get(position).getPostId()) && movieList.get(position).getPostId().equalsIgnoreCase("-99"))
                                    return AppConstant.TILE_SIZE;
                                if(movieList!=null && movieList.size()>0 && movieList.get(position).getNativeAd()!=null)
                                    return AppConstant.TILE_SIZE;
                                if(movieList!=null && movieList.size()>0 && movieList.get(position).getPostId().equalsIgnoreCase("-111"))
                                    return AppConstant.TILE_SIZE;
                                else
                                    return 1;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return 1;
                        }
                    });

                recyclerView.setLayoutManager(linearLayoutManager);

                int spacing = (int) getResources()
                        .getDimension(R.dimen.content_padding_recycle);
                recyclerView.addItemDecoration(new SpacesItemDecoration(spacing));

                recyclerView.addOnScrollListener(recyclerViewOnScrollListener);

                recyclerView.setAdapter(adapter);
                isLoading = false;
            }else{
                adapter.setVideoWall(isVideoWall);
                if(currentPage==1) {
                    adapter.notifyDataSetChanged();
                }else
                    adapter.notifyItemRangeInserted(lastPos,dataInsertedCount);

                lastPos=movieList.size()+1;
                isLoading=false;
            }
        }else{
            noDataLayout(getString(R.string.label_try_something_else));
        }
        Logger.e("lastPos final",""+lastPos);
    }

     private static final String TAG = "SearchActivity";
    private List<Wallpapers> movieList = new ArrayList<Wallpapers>();
    private RecyclerView recyclerView;
    private AllInOneAdapter adapter;
    private SettingStore settingStore;
    private boolean moreData = true,isNeedToDisplayLoading=true;
    private boolean isCalledWebService;
    private boolean isHome;
    int pagination_count=30;


    private void noDataLayout(String msg){
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
        String values[]=new String[]{msg};
        findViewById(R.id.rl_no_content).setVisibility(View.VISIBLE);
        if(recyclerView!=null)
            recyclerView.setVisibility(View.GONE);
        ((TextView)findViewById(R.id.txt_no)).setText(values[0]);

        isLastPage = true;
        moreData = false;
    }

    private boolean isLastPage = false;

    private int currentPage = PAGE_START;
    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private int lastPos=0;

    public void onLoadMoreRequested() {
        Logger.e(TAG, "onLoadMoreRequested isLoading: " + isLoading + " isLastPage "+isLastPage +" currentPage "+currentPage);
        if (!isLoading && !isLastPage) {
            isLoading = true;
            currentPage += 1;

//			if (currentPage > TOTAL_PAGES)
//			{
//				isLastPage = true;
//				return;
//			}
            getPostListResponse();
        }
    }

    private int DISPLAY_AD_COUNT=5,addedCount=0;
    int counter_final = 0;
    private boolean ADVERTISE_END;
    private boolean isAlreadyAdded;

    private void updateDataWithAd() {

        try {

            if(ADVERTISE_END)
                return;
/*
            if(!WallpapersApplication.getApplication().isNativeAdEnable) {
                ADVERTISE_END=true;
                return;
            }

            if(WallpapersApplication.getApplication().isAdmobLoaded){
                if(WallpapersApplication.getApplication().getGoogleNativeAd().size()<=0) {
                    ADVERTISE_END=true;
                    return;
                }
            }else if (WallpapersApplication.getApplication().isGGLoaded) {
                if (WallpapersApplication.getApplication().getGGNativeAds().size() <= 0) {
                    ADVERTISE_END=true;
                    return;
                }
            }else {
                if (WallpapersApplication.getApplication().getNativeAdsManager()!=null && !WallpapersApplication.getApplication().getNativeAdsManager().isLoaded()) {
                    ADVERTISE_END=true;
                    return;
                }
            }
*/
            int counter = counter_final;
            Logger.e("counter",""+counter);
            int final_freq=AppConstant.AD_DISPLAY_FREQUENCY;

            if(movieList==null || movieList.size()==0)
                return;

            for (int i = counter; i < movieList.size(); i++) {
                if (counter!=0 && counter % final_freq == 0) {
                    Object ad = null;
                    if(WallpapersApplication.getApplication().isAdmobLoaded){
                        if(WallpapersApplication.getApplication().getGoogleNativeAd().size()<=addedCount){
                            ad=null;
                        }else
                            ad = WallpapersApplication.getApplication().getGoogleNativeAd().get(WallpapersApplication.getApplication().getCurrentAdDisplayCount());
                    }else if(WallpapersApplication.getApplication().isGGLoaded){
                        if(WallpapersApplication.getApplication().getGGNativeAds().size()<=addedCount){
                            ad=null;
                        }else
                            ad = WallpapersApplication.getApplication().getGGNativeAds().get(WallpapersApplication.getApplication().getCurrentAdDisplayCountGG());
                    }else{
                        ad = WallpapersApplication.getApplication().getNativeAdsManager().nextNativeAd();
                    }

                    Logger.e("nextNativeAd","nextNativeAd  pos:"+i+" counter:"+counter);
                    try {
                        if(ad!=null) {
                            Wallpapers temp=new Wallpapers();
                            isAlreadyAdded=true;
                            temp.setNativeAd(ad);
                            movieList.add(counter+addedCount,temp);
                            dataInsertedCount++;
                            addedCount++;
                            if(AppConstant.ADS_PER_PAGE!=-1 && addedCount==AppConstant.ADS_PER_PAGE)
                            {
                                counter++;
                                ADVERTISE_END=true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                counter++;
                //			if(DISPLAY_AD_COUNT<=addedCount)
                //				break;
            }
            counter_final=counter;
            if(addedCount==0){
                ADVERTISE_END=true;
            }
            Logger.e("results.size() after ",""+movieList.size());
            Logger.e("counter_final",""+counter_final);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void adLoaded(){
        Logger.e("adLoaded",""+isAlreadyAdded);
        if(!isAlreadyAdded){
            updateDataWithAd();
            if(adapter!=null)
                adapter.notifyDataSetChanged();

            lastPos=movieList.size()+1;
            Logger.e("lastPos adLoaded",""+lastPos);
        }
    }



    private void removeLoading() {

        if (movieList != null && movieList.size() > 0) {
            if (!TextUtils.isEmpty(movieList.get(movieList.size() - 1).getPostId()) && movieList.get(movieList.size() - 1).getPostId().equalsIgnoreCase("-99")) {
                movieList.remove(movieList.size() - 1);
                dataInsertedCount--;
            }
        }

    }

    public void resetContent(){
//        results.clear();
        currentPage = 1;
        isNeedToDisplayLoading=true;
        isLoading = true;
        isLastPage = false;
        moreData = true;
        lastPos=0;
        /*isNeedToDisplayLoading=true;*/
        counter_final=0;
        addedCount=0;
        dataInsertedCount=0;
        ADVERTISE_END=false;
        isVideoWall=false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Logger.e(TAG,"onDestroy");

        if(adapter!=null){
            adapter.releaseResource();
            adapter=null;
        }

        if(movieList!=null){
            movieList.clear();
            movieList=null;
        }

        recyclerView=null;
        linearLayoutManager=null;
    }
}
