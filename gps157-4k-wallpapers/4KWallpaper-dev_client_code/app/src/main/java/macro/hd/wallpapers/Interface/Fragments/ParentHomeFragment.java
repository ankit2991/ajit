package macro.hd.wallpapers.Interface.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import macro.hd.wallpapers.AppController.RequestManager;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Interface.Activity.CategoryListingActivity;
import macro.hd.wallpapers.Interface.Activity.MainNavigationActivity;
import macro.hd.wallpapers.Interface.Adapters.AllInOneAdapter;
import macro.hd.wallpapers.Model.Category;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.WallInfoModel;
import macro.hd.wallpapers.Model.Wallpapers;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.OperationIds;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.NetworkManager.WebServices.WallpaperListWebservice;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EndlessRecyclerViewAdapter;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.SpacesItemDecoration;
import macro.hd.wallpapers.WallpapersApplication;

public class ParentHomeFragment extends BaseFragment implements NetworkCommunicationManager.CommunicationListnerNew, EndlessRecyclerViewAdapter.RequestToLoadMoreListener {

	public static final String TYPE_HOME = "1";
	public static final String TYPE_LIVE_WALLPAPER = "2";
	public static final String TYPE_TRENDING = "3";
	public static final String TYPE_EXCLUSIVE = "4";

	private static final String TAG = "ParentHomeFragment";
	private View rootView;

	private List<Wallpapers> movieList = new ArrayList<Wallpapers>();
	private List<Wallpapers> carousalList = new ArrayList<Wallpapers>();
	private RecyclerView recyclerView;
	private AllInOneAdapter adapter;
	private SettingStore settingStore;
	int pagination_count;
	private String category_sel="";
	private boolean moreData = true,isNeedToDisplayLoading=true;
	private boolean isCalledWebService;
	private boolean isVideoWall,isStockWallpaper;

	public String getScreenType() {
		return screenType;
	}

	private String screenType;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(getArguments()!=null){
			category_sel=getArguments().getString("category");
			screenType=getArguments().getString("screenType");
			isVideoWall=getArguments().getBoolean("isVideoWall");
			isStockWallpaper=getArguments().getBoolean("isStockWallpaper");
		}
		settingStore=SettingStore.getInstance(getActivity().getApplicationContext());
		Logger.e(TAG,"onCreate:"+screenType);
		pagination_count= WallpapersApplication.getApplication().getSettings().getPost_count();
		Logger.e("pagination_count",""+pagination_count);
		Logger.e("screenType",""+screenType);

		try {
			AppConstant.ADS_PER_PAGE= WallpapersApplication.getApplication().getSettings().getAdsPerPage();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(!TextUtils.isEmpty(screenType) && screenType.equalsIgnoreCase(ParentHomeFragment.TYPE_HOME))
			filter= Integer.parseInt(WallpapersApplication.getApplication().getSettings().getFilter_home());
		else if(!TextUtils.isEmpty(screenType) && screenType.equalsIgnoreCase(ParentHomeFragment.TYPE_LIVE_WALLPAPER))
			filter= Integer.parseInt(WallpapersApplication.getApplication().getSettings().getFilter_live());
		else if(!TextUtils.isEmpty(screenType) && screenType.equalsIgnoreCase(ParentHomeFragment.TYPE_EXCLUSIVE))
			filter= Integer.parseInt(WallpapersApplication.getApplication().getSettings().getFilter_exclusive());

//		setRetainInstance(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
							 final Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_parent_notification, container,
					false);
		Logger.e(TAG,"onCreateView:"+screenType);
			// Initialise your layout here
		return rootView;
	}

	private boolean isPendingForLoad;
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser){
		super.setUserVisibleHint(isVisibleToUser);
		Logger.e(TAG,"setUserVisibleHint:"+screenType+ " hidden:"+isVisibleToUser);
		if(isVisibleToUser){
			// Your fragment is visible
			if(screenType==null)
				isPendingForLoad=true;

			if(!isAPICalling && (movieList==null || movieList.size()==0) && screenType!=null) {
				if(swiperefresh!=null)
					swiperefresh.setEnabled(false);
				CallToGetList(false);
			}
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		Logger.e(TAG,"onHiddenChanged:"+screenType+ " hidden:"+hidden);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
		Logger.e(TAG,"onViewCreated:"+screenType);

		if(getActivity()!=null && getActivity() instanceof MainNavigationActivity){
			if(isPendingForLoad) {
				isPendingForLoad=false;
				CallToGetList(false);
			}
		}else
			CallToGetList(false);
	}


	@Override
	public void refreshContent() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(getActivity()!=null) {
					isNeedToReset = true;
					isNeedToDisplayLoading = false;
					refreshCall(false);
				}
			}
		}, AppConstant.REFERESH_TIME_OUT);
	}

	public void refreshCall(boolean needToShowLoading){

		if(swiperefresh!=null)
			swiperefresh.post(new Runnable() {
				@Override
				public void run() {
					if(swiperefresh!=null)
						swiperefresh.setRefreshing(true);
				}
			});
		isNeedToDisplayLoading=needToShowLoading;
		if(needToShowLoading){
			isNeedToReset=true;
			isNeedToDisplayLoading=false;
		}
		CallToGetList(true);
	}


	public void resetContent(){
//        results.clear();
		currentPage = 1;
		isLoading = true;
		isLastPage = false;
		moreData = true;
		lastPos=0;
		/*isNeedToDisplayLoading=true;*/
		counter_final=0;
		addedCount=0;
		ADVERTISE_END=false;
	}

	public void needToCallData() {
		Logger.e(TAG,"needToCallData");
		if(getActivity()==null)
			return;
		if (!isCalledWebService) {
			if (CommonFunctions.isNetworkAvailable(WallpapersApplication.sContext)) {
//				isCalledWebService=true;
				isNeedToDisplayLoading=false;
				counter_final=0;
				getPostListResponse();
			}
		}
	}

	private boolean isAPICalling;
	private boolean isNeedToReset;
	private void CallToGetList(boolean refresh) {

	    Logger.e(TAG,"CallToGetList");

		if(isNeedToReset) {
			resetContent();
			isNeedToReset=false;
		}

		if(getActivity()==null)
			return;

		isLoading = true;
		if(settingStore==null)
			settingStore= SettingStore.getInstance(getActivity().getApplicationContext());

		if(currentPage==1 && (TextUtils.isEmpty(category_sel) || (!TextUtils.isEmpty(category_sel) && category_sel.equalsIgnoreCase("-1"))) /*&& !isVideoWall*/){

			String response_string=settingStore.getSaveResponse(screenType+"_"+OperationIds.GET_POST_LIST_OPERATION_ID);

			if(refresh){
				if(CommonFunctions.isNetworkAvailable(getActivity())){
					getPostListResponse();
				}else{
					isAPICalling=false;
					Toast.makeText(getActivity(),getString(R.string.no_internet_connected),Toast.LENGTH_SHORT).show();
				}
				return;
			}
			if (!TextUtils.isEmpty(response_string)) {
				if(CommonFunctions.isNetworkAvailable(getActivity())){
					getPostListResponse();
				}else
					parsFillAdapter(response_string);
			}else {
				if(CommonFunctions.isNetworkAvailable(getActivity())){
					getPostListResponse();
				} else {
					isAPICalling=false;
					noDataLayout(getString(R.string.error_msg_no_network));
				}
			}
		}else if(CommonFunctions.isNetworkAvailable(getActivity())){
			getPostListResponse();
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

	private  void parsFillAdapter(String response){
		WallInfoModel postInfoModel= WallpaperListWebservice.parseResponse(response);
		onSuccess(postInfoModel,OperationIds.GET_POST_LIST_OPERATION_ID);
	}

	private void getPostListResponse() {
		isAPICalling=true;
		isLoading=true;
		isCalledWebService=true;
		RequestManager manager = new RequestManager(getActivity());

		String APIFinal=(AppConstant.IS_ASC?AppConstant.URL_POST_LIST_ASC:AppConstant.URL_POST_LIST);
//		String APIFinal=AppConstant.URL_POST_LIST;
		//Common.getDomain()+ ((!TextUtils.isEmpty(category) && category.equalsIgnoreCase("-1"))?AppConstant.URL_GET_TRENDING:AppConstant.URL_POST_LIST)
		String api= CommonFunctions.getDomain()+ ((!TextUtils.isEmpty(category_sel) && category_sel.equalsIgnoreCase("-1"))?AppConstant.URL_GET_TRENDING:APIFinal);
		if(isVideoWall){
			String APIFinalVideo=(AppConstant.IS_ASC?AppConstant.URL_IN_VIDEO_WALLPAPER_ASC:AppConstant.URL_IN_VIDEO_WALLPAPER);
			api= CommonFunctions.getDomain()+APIFinalVideo;
		}
		if(!TextUtils.isEmpty(screenType) && screenType.equalsIgnoreCase(ParentHomeFragment.TYPE_EXCLUSIVE)){
//			api=Common.getDomain()+AppConstant.URL_EXCLUSIVE_WALLPAPER;
			api=(AppConstant.IS_ASC?(CommonFunctions.getDomain()+AppConstant.URL_EXCLUSIVE_WALLPAPER_ASC):(CommonFunctions.getDomain()+AppConstant.URL_EXCLUSIVE_WALLPAPER));
		}

		String used_ids=getUsedIds();
		manager.getPostListService(api,""+currentPage, CommonFunctions.getHardwareId(getActivity()),"",category_sel,used_ids,screenType,filter,this);
	}

	public void showDialogue() {
		if(rootView==null)
			 return;
		if(currentPage==1)
			rootView.findViewById(R.id.rl_progress).setVisibility(View.VISIBLE);
	}

	public void dismissDialogue() {
		rootView.findViewById(R.id.rl_progress).setVisibility(View.GONE);
	}

	@Override
	public void onSuccess(final IModel response, int operationCode) {
		removeRefresh();
		isAPICalling=false;
		if(swiperefresh!=null)
			swiperefresh.setEnabled(true);
		if (getActivity() != null && !getActivity().isFinishing())
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (getActivity() == null || getActivity().isFinishing())
						return;
					try {

						dismissDialogue();
						removeLoading();
						WallInfoModel model = (WallInfoModel) response;
						if (model != null && model.getStatus().equalsIgnoreCase("1")) {

							try {
								if(getActivity() instanceof CategoryListingActivity)
									((CategoryListingActivity)getActivity()).displayBannerAd();
							} catch (Exception e) {
								e.printStackTrace();
							}

							if(model!=null && model.getPost()!=null ) {
								if(currentPage==1) {
									movieList.clear();
								}

								List<Wallpapers> temp=model.getPost();
//								int k=0;
//								for (int i = 0; i < temp.size(); i++) {
//									temp.get(i).setColor_placeholder(AppConstant.colorWhite[k]);
//
//									if(k==AppConstant.colorWhite.length-1)
//										k=0;
//									else
//										k++;
//								}
								movieList.addAll(temp);
								dataInsertedCount=temp.size();
							}

							if(currentPage==1 && !TextUtils.isEmpty(screenType) && screenType.equalsIgnoreCase(ParentHomeFragment.TYPE_HOME)){

									List<Category> temp = UserInfoManager.getInstance(getActivity().getApplicationContext()).getUserInfo().getCategory_feature();

									if(temp!=null)
										for (int i = 0; i < temp.size(); i++) {
											Category cat=temp.get(i);
											Wallpapers post_category = new Wallpapers();
											post_category.setCategory(cat.getName());
											post_category.setCategory_title(cat.getDisplay_name());
											post_category.setPostId("-9");
											try {
												post_category.setImg(cat.getImg_feature());
											} catch (Exception e) {
												e.printStackTrace();
											}
											carousalList.add(post_category);
										}

									if(CommonFunctions.isDoubleWallpaperSupported() || CommonFunctions.isEdgeWallpaperSupported()) {
										Wallpapers categorydouble = new Wallpapers();
										categorydouble.setCategory(getActivity().getResources().getString(R.string.txtdouble));
										categorydouble.setPostId("-2");
										try {
											categorydouble.setImg(WallpapersApplication.getApplication().getSettings().getDouble_sample());
										} catch (Exception e) {
											e.printStackTrace();
											categorydouble.setImg("double_sample.jpg");
										}
										movieList.add(0,categorydouble);

										if(CommonFunctions.isDoubleWallpaperSupported()){
											Wallpapers carousaldouble = new Wallpapers();
											carousaldouble.setCategory(getActivity().getResources().getString(R.string.txtdouble));
											carousaldouble.setPostId("-1");
											try {
												carousaldouble.setImg(WallpapersApplication.getApplication().getSettings().getDouble_sample());
											} catch (Exception e) {
												e.printStackTrace();
												carousaldouble.setImg("double_sample1.jpg");
											}
											carousaldouble.setImg("double_sample1.jpg");
											carousalList.add(carousaldouble);
										}

										if(CommonFunctions.isEdgeWallpaperSupported()){
											Wallpapers carousaldouble = new Wallpapers();
											carousaldouble.setCategory(getActivity().getResources().getString(R.string.edgetxt));
											carousaldouble.setPostId("-2");
											try {
												carousaldouble.setImg(WallpapersApplication.getApplication().getSettings().getEdge_sample());
											} catch (Exception e) {
												e.printStackTrace();
												carousaldouble.setImg("edge_sample.jpg");
											}
											carousalList.add(carousaldouble);
										}

										if(CommonFunctions.isGradientSupported()){
											Wallpapers carousaldouble = new Wallpapers();
											carousaldouble.setCategory(getActivity().getResources().getString(R.string.gradienttxt));
											carousaldouble.setPostId("-4");
											try {
												if(TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getGradient_img()))
													carousaldouble.setImg("gradient5.jpg");
												else
													carousaldouble.setImg(WallpapersApplication.getApplication().getSettings().getGradient_img());
											} catch (Exception e) {
												e.printStackTrace();
												carousaldouble.setImg("gradient5.jpg");
											}
											carousalList.add(carousaldouble);
										}

										if(CommonFunctions.isStatusSaverSupported(getActivity())){
											Wallpapers carousaldouble = new Wallpapers();
											carousaldouble.setCategory(getActivity().getResources().getString(R.string.statustxt));
											carousaldouble.setPostId("-3");
											try {
												carousaldouble.setImg(WallpapersApplication.getApplication().getSettings().getStatus_sample());
											} catch (Exception e) {
												e.printStackTrace();
												carousaldouble.setImg("status_saver.jpg");
											}
											carousalList.add(carousaldouble);
										}
									}
							}

							boolean pagination_end = false;
							if(model.getPost()==null || pagination_count != model.getPost().size()){
								pagination_end=true;
							}

							if (!pagination_end) {
								isLastPage = false;
								moreData = true;
							}else {
								isLastPage = true;
								moreData = false;
							}

							if(currentPage>1 && (model.getPost()==null || model.getPost().size()==0)){
								if(adapter!=null && movieList.size()>0)
									adapter.notifyDataSetChanged();
							}else
								fillupAdapter();
						} else if (model != null && model.getStatus().equalsIgnoreCase("0")) {
							if(adapter!=null){
								adapter.notifyDataSetChanged();
							}
							CommonFunctions.showDialog(getActivity(), getString(R.string.error_title), model.getMsg(), getActivity().getResources().getString(R.string.label_ok));
						}
					} catch (Exception e) {
						e.printStackTrace();
						if(adapter!=null){
							adapter.notifyDataSetChanged();
						}
					}
				}
			});
	}

	public void setfilter(int fourdfilter) {
		this.filter = fourdfilter;
		if(adapter!=null)
			adapter.setFilter(fourdfilter);
	}
	public int getfilter() {
		return filter;
	}
	private int filter=0;

	private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);

			int visibleItemCount = linearLayoutManager.getChildCount();
			int totalItemCount = linearLayoutManager.getItemCount();
			int firstVisibleItemPositions = linearLayoutManager.findFirstVisibleItemPosition();


			if ((firstVisibleItemPositions + visibleItemCount) >= totalItemCount
					&& firstVisibleItemPositions >= 0) {
				if(CommonFunctions.isNetworkAvailable(getActivity()))
					onLoadMoreRequested();
				else
					Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.error_msg_no_network),Toast.LENGTH_SHORT).show();
			}
		}
	};

	private void noDataLayout(String msg){
		if(rootView==null)
			return;
		String values[]=new String[]{msg};
		rootView.findViewById(R.id.rl_no_content).setVisibility(View.VISIBLE);
		rootView.findViewById(R.id.list).setVisibility(View.GONE);
		((TextView)rootView.findViewById(R.id.txt_no)).setText(values[0]);

		isLastPage = true;
		moreData = false;
	}


	GridLayoutManager linearLayoutManager;
	private int dataInsertedCount;
	private void fillupAdapter(){

		Logger.e("currentPage",""+currentPage);
		Logger.e("lastPos",""+lastPos);
		Logger.e("results.size()",""+movieList.size());
		Logger.e("difference",""+(movieList.size()-lastPos));
		Logger.e("isCalledWebService",""+isCalledWebService);

		removeRefresh();

		if(movieList!=null && movieList.size()>0){

			if(isCalledWebService)
				updateDataWithAd();

			if(moreData) {
				Wallpapers chatMessage = new Wallpapers();
				chatMessage.setPostId("-99");
				movieList.add(chatMessage);
				dataInsertedCount++;
			}

			rootView.findViewById(R.id.list).setVisibility(View.VISIBLE);
			rootView.findViewById(R.id.rl_no_content).setVisibility(View.GONE);
			if (adapter == null) {
				lastPos=movieList.size()+1;
				adapter = new AllInOneAdapter(getActivity(), movieList,filter);

				if(!TextUtils.isEmpty(screenType) && screenType.equalsIgnoreCase(ParentHomeFragment.TYPE_EXCLUSIVE)){
					adapter.setExclusiveWallpaper(true);
				}
				adapter.setStock_wallpaper(isStockWallpaper);
				adapter.setVideoWall(isVideoWall);
				adapter.setCategory_sel(category_sel);
				adapter.setmListCoursal(carousalList);
				adapter.setFromTrending(category_sel.equalsIgnoreCase("-1")?true:false);
//				adapter.setNativeAdsManager(mNativeAdsManager);

				linearLayoutManager = new GridLayoutManager(getActivity(),AppConstant.TILE_SIZE);
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
								else if(movieList!=null && movieList.size()>0 && !TextUtils.isEmpty(movieList.get(position).getPostId()) && (movieList.get(position).getPostId().equalsIgnoreCase("-2")))
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
//				int spacingInPixels = 1;
				recyclerView.addItemDecoration(new SpacesItemDecoration(spacing));

				recyclerView.addOnScrollListener(recyclerViewOnScrollListener);

				recyclerView.setAdapter(adapter);
				isLoading = false;

//                if(TextUtils.isEmpty(category_sel))
//                    needToCallData();
			}else{
				if(currentPage==1)
					adapter.notifyDataSetChanged();
				else
					adapter.notifyItemRangeInserted(lastPos,dataInsertedCount);

				lastPos=movieList.size()+1;
				isLoading=false;
			}

		}else{
			noDataLayout(getResources().getString(R.string.no_data_available));
		}

		Logger.e("lastPos final",""+lastPos);
	}

	@Override
	public void onStartLoading() {
		if(isNeedToDisplayLoading) {
			isNeedToDisplayLoading=false;
			showDialogue();
		}
	}

	@Override
	public void onFail(final WebServiceError errorMsg) {
		Logger.e("onFail","onFail:");
		isAPICalling=false;
		if (getActivity() != null)
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					removeRefresh();
					removeLoading();
					dismissDialogue();
					isLoading=false;
					if(adapter!=null){
						adapter.notifyDataSetChanged();
					}

					if(currentPage==1){
						if(swiperefresh!=null)
							swiperefresh.setEnabled(true);
					}
//					Common.showDialog(getActivity(), getString(R.string.error_title), errorMsg.getDescription(), "Ok");
				}
			});
	}

	private boolean isLastPage = false;
//	private int TOTAL_PAGES  ;
	private int currentPage = PAGE_START;
	private static final int PAGE_START = 1;
	private boolean isLoading = false;
	private int lastPos=0;

		public void onLoadMoreRequested() {
		Logger.e(TAG, "onLoadMoreRequested isLoading: " + isLoading + " isLastPage "+isLastPage +" currentPage "+currentPage);
		if (!isLoading && !isLastPage) {
			isLoading = true;
			currentPage += 1;

			getPostListResponse();
		}
	}

	private int DISPLAY_AD_COUNT=5,addedCount=0;
	int counter_final = 0;
	private boolean ADVERTISE_END;
	private boolean isAlreadyAdded;

	private void updateDataWithAd() {
		try {
/*
			if(ADVERTISE_END)
				return;

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
			int increment=0;
			try {
				if(!TextUtils.isEmpty(screenType) && screenType.equalsIgnoreCase(ParentHomeFragment.TYPE_HOME)){
					if(CommonFunctions.isDoubleWallpaperSupported()) {
						increment=1;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (int i = counter; i < movieList.size(); i++) {
				if (counter!=0 && counter % final_freq == 0) {
					Object ad = null;

					//TODO lets ignore data, always take admob
					if(WallpapersApplication.getApplication().getGoogleNativeAd()==null || WallpapersApplication.getApplication().getGoogleNativeAd().size()==0){
						ad=null;
					}else
						ad = WallpapersApplication.getApplication().getGoogleNativeAd().get(WallpapersApplication.getApplication().getCurrentAdDisplayCount());
					/*
                	if(WallpapersApplication.getApplication().isAdmobLoaded){
						if(WallpapersApplication.getApplication().getGoogleNativeAd()==null || WallpapersApplication.getApplication().getGoogleNativeAd().size()==0){
							ad=null;
						}else
							ad = WallpapersApplication.getApplication().getGoogleNativeAd().get(WallpapersApplication.getApplication().getCurrentAdDisplayCount());
					}else if(WallpapersApplication.getApplication().isGGLoaded){
						if(WallpapersApplication.getApplication().getGGNativeAds()==null || WallpapersApplication.getApplication().getGGNativeAds().size()==0){
							ad=null;
						}else
							ad = WallpapersApplication.getApplication().getGGNativeAds().get(WallpapersApplication.getApplication().getCurrentAdDisplayCountGG());
					}else{
						ad = WallpapersApplication.getApplication().getNativeAdsManager().nextNativeAd();
					}
					*/
					Logger.e("nextNativeAd","nextNativeAd  pos:"+i+" counter:"+counter);
					try {
						if(ad!=null) {
							Wallpapers temp=new Wallpapers();
							isAlreadyAdded=true;
							temp.setNativeAd(ad);
							movieList.add(counter+addedCount+increment,temp);
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

	public void removeAllAd() {
		try {
			if (movieList != null) {

				// Remove elements smaller than 10 using
				// Iterator.remove()
				Iterator itr = movieList.iterator();
				while (itr.hasNext())
				{
					Wallpapers x = (Wallpapers) itr.next();
					if (x.getNativeAd()!=null)
						itr.remove();
				}
				if(adapter!=null)
					adapter.notifyDataSetChanged();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		if(category_sel!=null){
			category_sel=null;
		}

//		if(carousalList!=null){
//			carousalList.clear();
//			carousalList=null;
//		}
		screenType=null;
		settingStore=null;
		recyclerView=null;
		rootView=null;
		swiperefresh=null;
		linearLayoutManager=null;
		recyclerViewOnScrollListener=null;
	}
}
