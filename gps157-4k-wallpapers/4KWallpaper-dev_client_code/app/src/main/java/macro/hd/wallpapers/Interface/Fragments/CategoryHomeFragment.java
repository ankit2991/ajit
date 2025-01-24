package macro.hd.wallpapers.Interface.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import macro.hd.wallpapers.Interface.Adapters.CategoryAdapter;
import macro.hd.wallpapers.Interface.Activity.CategoryListingActivity;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.MyCustomView.SpacesItemDecorationCategory;
import macro.hd.wallpapers.Interface.Activity.DoubleWallpaperActivity;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CategoryCallback;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.SpacesItemDecoration;
import macro.hd.wallpapers.Model.Category;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CategoryHomeFragment extends BaseFragment implements CategoryCallback {

	private static final String TAG = "CategoryHomeFragment";
	private View rootView;
	private List<Category> categoryList = new ArrayList<Category>();
	private RecyclerView recyclerView;
	private CategoryAdapter adapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void refreshContent() {
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser){
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser){
			// Your fragment is visible
		}
		Logger.e(TAG,"setUserVisibleHint:"+isVisibleToUser);
//		setRetainInstance(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
							 final Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_parent_category, container,
					false);
		return rootView;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		Logger.e(TAG,"onHiddenChanged : "+hidden);

	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
		swiperefresh.setEnabled(false);
		List<Category> temp = UserInfoManager.getInstance(getActivity().getApplicationContext()).getUserInfo().getCategory();
		categoryList.addAll(new ArrayList<>(temp));
		fillupAdapter();
	}

	private void fillupAdapter(){

		Logger.e("results.size()",""+ categoryList.size());

		removeRefresh();
		if(categoryList !=null && categoryList.size()>0){

			boolean isAppAdded=false;
			try {
				for (int i = 0; i < categoryList.size(); i++) {
					String link= categoryList.get(i).getLink();
					if(!TextUtils.isEmpty(link)) {
						String packageName=link.substring(link.indexOf("id=")+3,link.indexOf("&"));
						Logger.e("CategoryHome",""+packageName);
						if(!CommonFunctions.isPackageInstalled(getActivity(),packageName)){
							Category temp= categoryList.get(i);
							categoryList.remove(i);
							categoryList.add(0,temp);
							isAppAdded=true;
						}else
							categoryList.remove(i);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Iterator<Category> i = categoryList.iterator();
			while (i.hasNext()) {
				Category s = i.next(); // must be called before you can call i.remove()
				// Do something
				if(!TextUtils.isEmpty(s.getLink())){
					continue;
				}
				if(!TextUtils.isEmpty(s.getStatus()) && s.getStatus().equalsIgnoreCase("0"))
					i.remove();
			}

			Category category1=new Category();
			category1.setCat_id("-1");
			categoryList.add(isAppAdded?1:0,category1);

//			if(Common.isDoubleWallpaperSupported()) {
//				Category categorydouble = new Category();
//				categorydouble.setDisplay_name("Double Wallpapers");
//				categorydouble.setCat_id("-2");
//				try {
//					categorydouble.setImage(JesusApplication.getApplication().getSettings().getDouble_sample());
//				} catch (Exception e) {
//					e.printStackTrace();
//					categorydouble.setImage("double_sample.jpg");
//				}
//				categoryList.add(categorydouble);
//			}

			updateDataWithAd();

			rootView.findViewById(R.id.rl_no_content).setVisibility(View.GONE);
//			UserInfoManager.getInstance(getActivity().getApplicationContext()).getUserInfo().setColorCategory(null);
			if (adapter == null) {

				adapter = new CategoryAdapter(getActivity(), categoryList);
				adapter.setStock_category(UserInfoManager.getInstance(getActivity().getApplicationContext()).getUserInfo().getStock_category());

				adapter.setCategoryCallback(this);

				LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//				linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
				recyclerView.setLayoutManager(linearLayoutManager);

				int spacing = (int) getResources()
						.getDimension(R.dimen.category_padding_recycle);
				recyclerView.addItemDecoration(new SpacesItemDecorationCategory(spacing));
				recyclerView.setAdapter(adapter);
			}else{
				adapter.notifyDataSetChanged();
			}
		}else{
			noDataLayout("No data available. pls try later.");
		}
	}


	public void removeAllAd() {
		try {
			if (categoryList != null) {

				// Remove elements smaller than 10 using
				// Iterator.remove()
				Iterator itr = categoryList.iterator();
				while (itr.hasNext())
				{
					Category x = (Category) itr.next();
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

	private boolean isAlreadyAdded;
	public void adLoaded(){
		Logger.e(TAG,"adLoaded:"+isAlreadyAdded);
		if(!isAlreadyAdded){
			updateDataWithAd();
			if(adapter!=null)
				adapter.notifyDataSetChanged();
		}
	}


	private void updateDataWithAd() {

		try {
// comment advertise
//			if(JesusApplication.getApplication().getGoogleNativeAdDetail().size()<=0)
//				return;
//
//			int counter = 0;
//			Logger.e(TAG,"counter:"+counter);
//			int final_freq= 4;
//
//			for (int i = counter; i < categoryList.size(); i++) {
//				if (counter!=0 && counter % final_freq == 0) {
//					Object ad;
//					ad = JesusApplication.getApplication().getGoogleNativeAdDetail().get(JesusApplication.getApplication().getCurrentAdDisplayCountDetail());
//					Logger.e(TAG,"nextNativeAd  pos:"+i+" counter:"+counter);
//					try {
//						if(ad!=null) {
//							isAlreadyAdded=true;
//							Category temp=new Category();
//							temp.setNativeAd(ad);
//							categoryList.add(counter,temp);
//							Logger.e(TAG,"Added at pos:"+i);
//							break;
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//				counter++;
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void noDataLayout(String msg){
		String values[]=new String[]{msg};
		rootView.findViewById(R.id.rl_no_content).setVisibility(View.VISIBLE);
		((TextView)rootView.findViewById(R.id.txt_no)).setText(values[0]);
	}

	@Override
	public void clickCategory(Category category,boolean isLiveWallpaper,boolean isStockWallpaper,boolean isColorWallpaper) {

		if (!CommonFunctions.isNetworkAvailable(getActivity())) {
			CommonFunctions.showInternetDialog(getActivity());
			return;
		}

		if(!TextUtils.isEmpty(category.getCat_id()) && category.getCat_id().equalsIgnoreCase("-2")) {
			EventManager.sendEvent(EventManager.LBL_DOUBLE_WALLPAPER,EventManager.ATR_KEY_DOUBLE,"Category Screen");
			Intent intent = new Intent(getActivity(), DoubleWallpaperActivity.class);
			if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
				WallpapersApplication.getApplication().showInterstitial(getActivity(), intent, false);
			} else {
				WallpapersApplication.getApplication().incrementUserClickCounter();
				startActivity(intent);
			}
			return;
		}

		if(TextUtils.isEmpty(category.getLink())){
			final Intent intent=new Intent(getActivity(), CategoryListingActivity.class);
			intent.putExtra("category",category.getName());
			intent.putExtra("category_name",category.getDisplay_name());
			intent.putExtra("isVideoWall",isLiveWallpaper);
			intent.putExtra("isStockWallpaper",isStockWallpaper);
			intent.putExtra("isColorWallpaper",isColorWallpaper);

			if(isStockWallpaper)
				EventManager.sendEvent(EventManager.LBL_HOME,EventManager.ATR_KEY_STOCK_CATEGORY_CLICK,category.getName());
			else if(isColorWallpaper)
				EventManager.sendEvent(EventManager.LBL_HOME,EventManager.ATR_KEY_COLOR_CATEGORY_CLICK,category.getName());
			else
				EventManager.sendEvent(EventManager.LBL_HOME,EventManager.ATR_KEY_CATEGORY_CLICK,category.getName());

			if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
				WallpapersApplication.getApplication().showInterstitial(getActivity(), intent, false);
			} else {
				WallpapersApplication.getApplication().incrementUserClickCounter();
				startActivity(intent);
			}
		}else{
			EventManager.sendEvent(EventManager.LBL_HOME,EventManager.ATR_KEY_GOD_CLICK,"Click");
			startActivity(new Intent(
					Intent.ACTION_VIEW,
					Uri.parse(category.getLink())));
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.e(TAG,"onDestroy");

		if(categoryList !=null){
			categoryList.clear();
			categoryList =null;
		}

		recyclerView=null;

		if(adapter!=null){
			adapter.releaseResource();
			adapter=null;
		}

		rootView=null;

		swiperefresh=null;
	}

}
