package macro.hd.wallpapers.Interface.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import macro.hd.wallpapers.Interface.Activity.CategoryListingActivity;
import macro.hd.wallpapers.AppController.RequestManager;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.MyCustomView.RecyclerViewPager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Interface.Activity.FavoriteActivity;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.Interface.Activity.WallpaperDetailActivity;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Interface.Activity.StockWallDetailActivity;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Interface.Activity.LiveWallDetailActivity;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.IModelBase;
import macro.hd.wallpapers.Model.Wallpapers;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.NativeAdLayout;

import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.greedygame.core.adview.general.AdLoadCallback;
import com.greedygame.core.adview.general.GGAdview;
import com.greedygame.core.models.general.AdErrors;
//import com.google.android.gms.ads.VideoController;
//import com.google.android.gms.ads.formats.UnifiedNativeAd;
//import com.google.android.gms.ads.formats.UnifiedNativeAdView;
//import com.greedygame.core.adview.GGAdview;
//import com.greedygame.core.adview.interfaces.AdLoadCallback;
//import com.greedygame.core.adview.modals.AdRequestErrors;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by Admin on 05-03-2017.
 */

public class AllInOneAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{
	private List<Wallpapers> mList;

	public void setmListCoursal(List<Wallpapers> mListCoursal) {
		this.mListCoursal = mListCoursal;
	}

	private List<Wallpapers> mListCoursal;
	private Context activity;

	public static final int PHOTOS_TYPE = 2;

	public static final int NO_DATA_TYPE = 4;

	public static final int HOME_LOADING=7;

	private static final int AD_TYPE = 5;
    private static final int AD_TYPE_GOOGLE = 6;
	private static final int AD_TYPE_GG = 9;
	public static final int DOUBLE_TYPE = 8;

	private boolean isStock_wallpaper;

	public void setExclusiveWallpaper(boolean exclusiveWallpaper) {
		isExclusiveWallpaper = exclusiveWallpaper;
	}

	private boolean isExclusiveWallpaper;

	public void setVideoWall(boolean videoWall) {
		isVideoWall = videoWall;
	}

	private boolean isVideoWall;

	SettingStore settingStore;

	public void setFromTrending(boolean fromTrending) {
		isFromTrending = fromTrending;
	}

	private boolean isFromTrending;
	private UserInfoManager userInfoManager;

	public void setCategory_sel(String category_sel) {
		this.category_sel = category_sel;
	}

	String category_sel;

	private static int itemHeight;
	public void setFilter(int filter) {
		this.filter = filter;
	}

	private int filter;

	public AllInOneAdapter(Context activity, List<Wallpapers> movieItems, int filter) {
		this.activity = activity;
		this.mList = movieItems;
		this.filter=filter;
		userInfoManager = UserInfoManager.getInstance(activity.getApplicationContext());
		settingStore=SettingStore.getInstance(activity);
//		mAdItems = new ArrayList<>();

		try {
			Point windowDimensions = new Point();
			((Activity)activity).getWindowManager().getDefaultDisplay().getSize(windowDimensions);
			Logger.e("OffersRowAdapter","y:"+windowDimensions.y + " X:"+windowDimensions.x);
			itemHeight = Math.round(windowDimensions.y * 0.320f);
			int itemHeight_diment= (int) ((Activity)activity).getResources().getDimension(R.dimen.tile_size);

			Logger.e("OffersRowAdapter","y:"+windowDimensions.y + " X:"+windowDimensions.x+ " itemHeight:"+itemHeight+" itemHeight_diment:"+itemHeight_diment );

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view;
		switch (viewType) {
			case PHOTOS_TYPE:
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row_photos, parent, false);
				return new OfferViewHolder(view,mList);
			case NO_DATA_TYPE:
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.no_data_layout, parent, false);
				return new NoDataRowHolder(view);
            case AD_TYPE:
				NativeAdLayout inflatedView = (NativeAdLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .native_ad_unit_new, parent, false);
                return new AdHolder(inflatedView);
			case HOME_LOADING:
				View v4 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_home, parent, false);
				return new ProgressHolder(v4);
            case AD_TYPE_GOOGLE:
                View inflatedView1 = LayoutInflater.from(activity).inflate(R.layout
                        .google_native_ad, parent, false);
                return new AdHolderGoogle(inflatedView1);
			case AD_TYPE_GG:
				View gg_ad = LayoutInflater.from(activity).inflate(R.layout
						.item_gg_native_ad, parent, false);
				return new AdHolderGG(gg_ad);
			case DOUBLE_TYPE:

				if (carousalViewHolder != null) {
					return carousalViewHolder;
				}
				View carousalItemLayoutView = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_item_category_carousal, null);
				carousalViewHolder = new CarousalViewHolder(carousalItemLayoutView);
				carousalItemLayoutView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				return carousalViewHolder;

//				View v_double = LayoutInflater.from(activity).inflate(R.layout.list_item_doublewall, null);
//				DoubleViewHolder doubleViewHolder = new DoubleViewHolder(v_double);
//				return doubleViewHolder;
		}
		return null;
	}

	public void setStock_wallpaper(boolean stock_wallpaper) {
		isStock_wallpaper = stock_wallpaper;
	}

	private void deleteAlertDialog(final Context context, final String dst, final int pos) {
		try {
			if(((Activity) context).isFinishing()){
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		android.app.AlertDialog.Builder builder = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			SettingStore settingStore = SettingStore.getInstance(context);
			boolean isDarkTheme = false;
			if (settingStore.getTheme()==0) {
				isDarkTheme=false;
			}else if (settingStore.getTheme()==1) {
				isDarkTheme=true;
			}
			if(isDarkTheme)
				builder = new android.app.AlertDialog.Builder(context, R.style.CustomAlertDialog);
			else
				builder = new android.app.AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert);

		} else {
			builder = new android.app.AlertDialog.Builder(context);
		}

		builder.setTitle(activity.getResources().getString(R.string.delete_wallpaper_title));
		builder.setMessage(activity.getResources().getString(R.string.delete_msg));
		final DialogInterface.OnClickListener dialogButtonClickListener =
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							CommonFunctions.deletePostImage(context,  dst);
							dialog.dismiss();
							mList.remove(pos);
							notifyItemRemoved(pos);
							notifyItemRangeChanged(pos,mList.size());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
		builder.setPositiveButton(activity.getResources().getString(R.string.label_yes), dialogButtonClickListener);
		builder.setNegativeButton(activity.getResources().getString(R.string.label_no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
		builder.setCancelable(true);

		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

		final int type=getItemViewType(position);

		final Wallpapers object = mList.get(position);

		switch (type) {
				case PHOTOS_TYPE:
					OfferViewHolder photoViewHolder=((OfferViewHolder) holder);

					if(!TextUtils.isEmpty(object.getImg())) {
						String path;
						if(object.getPostId().equalsIgnoreCase("-100")){
							path=object.getImg();
						}else{
							if(object.getImg().startsWith("http")) {
								path=object.getSearch_small();
							}else{
								path = CommonFunctions.getDomainImages() + "small/" + object.getImg();
								if (!TextUtils.isEmpty(object.getVid()))
									path = CommonFunctions.getDomainImages() + "liveimg/" + object.getImg();

								if (isStock_wallpaper)
									path = CommonFunctions.getDomainImages() + "stock_thumb/" + object.getImg();
							}
						}

//						Logger.e("path",""+path);
						Glide.with(activity)
								.load(path).transition(DrawableTransitionOptions.withCrossFade())
								.apply(new RequestOptions().placeholder(CommonFunctions.getLoadingColor(activity)))
								.into(photoViewHolder.thumbNail);
					}

					photoViewHolder.rl_photos.setTag(""+position);
					photoViewHolder.rl_photos.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {

							if (!CommonFunctions.isNetworkAvailable(activity)) {
								CommonFunctions.showInternetDialog(activity);
								return;
							}

							if (isExclusiveWallpaper && !CommonFunctions.isSensorAvailable(activity)) {
								return;
							}

							try {
								final int pos = Integer.parseInt(view.getTag().toString());
								final Wallpapers temp=mList.get(pos);

								if(temp.getPostId().equalsIgnoreCase("-100")) {

									android.app.AlertDialog.Builder builder = null;
									if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

										boolean isDarkTheme = false;
										if (settingStore.getTheme() == 0) {
											isDarkTheme = false;
										} else if (settingStore.getTheme() == 1) {
											isDarkTheme = true;
										}
										if (isDarkTheme)
											builder = new android.app.AlertDialog.Builder(activity, R.style.CustomAlertDialog);
										else
											builder = new android.app.AlertDialog.Builder(activity, android.R.style.Theme_Material_Light_Dialog_Alert);

									} else {
										builder = new android.app.AlertDialog.Builder(activity);
									}
// add a list
									String[] animals = {activity.getResources().getString(R.string.label_view),activity.getResources().getString(R.string.label_set_as) ,activity.getResources().getString(R.string.label_share), activity.getResources().getString(R.string.label_delete)};
									builder.setItems(animals, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											switch (which) {
												case 0: // horse
													try {
														Intent intent = new Intent();
														intent.setAction(Intent.ACTION_VIEW);
														intent.setDataAndType(Uri.parse("file://" + temp.getImg()), "image/*");
														activity.startActivity(intent);
													} catch (Exception e) {
														e.printStackTrace();
													}
													break;
												case 1: // cow
													Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
													intent.addCategory(Intent.CATEGORY_DEFAULT);
													intent.setDataAndType(Uri.fromFile(new File(temp.getImg())), "image/*");
													intent.putExtra("mimeType", "image/*");
													activity.startActivity(Intent.createChooser(intent,activity.getResources().getString(R.string.label_set_dialog) ));
													break;
												case 2: // cow
													CommonFunctions.sharedPost(activity, "", temp.getImg(), "", false);
													break;
												case 3: // cow
													deleteAlertDialog(activity, temp.getImg(), pos);
													break;
											}
										}
									});
									AlertDialog dialog = builder.create();
									try {
										if (!((Activity) activity).isFinishing())
											dialog.show();
									} catch (Exception e) {
										e.printStackTrace();
									}
									return;
								}

								List<Wallpapers> tempList;
								tempList=new ArrayList<>(mList);

								Iterator itr = tempList.iterator();
								while (itr.hasNext())
								{
									Wallpapers temp11= (Wallpapers) itr.next();
									if(temp11.getNativeAd()!=null || (!TextUtils.isEmpty(temp11.getPostId()) && temp11.getPostId().equalsIgnoreCase("-99")) || (!TextUtils.isEmpty(temp11.getPostId()) && temp11.getPostId().equalsIgnoreCase("-2"))){
										itr.remove();
									}
								}

								if(CategoryListingActivity.isShowSimilar){
									if(WallpapersApplication.newList!=null)
										WallpapersApplication.newList.clear();
									else
										WallpapersApplication.newList=new ArrayList<>();
									WallpapersApplication.newList.addAll(tempList);
								}

								Intent intent;
								if(temp!=null && !TextUtils.isEmpty(temp.getVid())){
									intent=new Intent(activity, LiveWallDetailActivity.class);
//									EventManager.sendEvent(EventManager.LBL_HOME,EventManager.ATR_KEY_LIVE_WALLPAPER_CLICK,"Click");

								}else if(isStock_wallpaper){
									intent=new Intent(activity, StockWallDetailActivity.class);
								}else if(isExclusiveWallpaper){
									intent=new Intent(activity, LiveWallDetailActivity.class);
									intent.putExtra("isExclusive",true);
								}else {
										intent = new Intent(activity, WallpaperDetailActivity.class);
								}
								intent.putExtra("post",temp);
								intent.putExtra("filter",filter);
								if(!CategoryListingActivity.isShowSimilar){
									intent.putExtra("post_list", (Serializable) tempList);
								}
//
								intent.putExtra("isTrending",isFromTrending);
								intent.putExtra("pos",""+temp.getPostId());

								intent.putExtra("category",category_sel);

								if(activity!=null && activity instanceof FavoriteActivity){
									intent.putExtra("disableMore",true);
								}

								try {
									if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
										WallpapersApplication.getApplication().isAdDisplayBefore=true;
										WallpapersApplication.getApplication().showInterstitial((Activity) activity, intent, false);
									} else {
										WallpapersApplication.getApplication().isAdDisplayBefore=false;
										WallpapersApplication.getApplication().incrementUserClickCounter();
										activity.startActivity(intent);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

					if(photoViewHolder.img_delete!=null) {
						photoViewHolder.img_delete.setTag(""+position);
						photoViewHolder.img_delete.setOnClickListener(this);
					}

					if(isExclusiveWallpaper){
						photoViewHolder.img_video.setImageResource(R.mipmap.ic_exclusive_tile);
						photoViewHolder.img_video.setVisibility(View.VISIBLE);
					}else if(isVideoWall){
						photoViewHolder.img_video.setVisibility(View.VISIBLE);
					}else
						photoViewHolder.img_video.setVisibility(View.GONE);

					break;

				case NO_DATA_TYPE:
					NoDataRowHolder noDataRowHolder=((NoDataRowHolder) holder);
					noDataRowHolder.recycler_view_list.setVisibility(View.VISIBLE);
					break;

                case AD_TYPE:
					NativeAd ad= (NativeAd) object.getNativeAd();

					AdHolder adHolder = (AdHolder) holder;
					adHolder.adChoicesContainer.removeAllViews();

					if (ad != null) {

						adHolder.tvAdTitle.setText(ad.getAdvertiserName());
						adHolder.tvAdBody.setText(ad.getAdBodyText());
						adHolder.tvAdSocialContext.setText(ad.getAdSocialContext());
						adHolder.tvAdSponsoredLabel.setText(ad.getSponsoredTranslation());
						adHolder.btnAdCallToAction.setText(ad.getAdCallToAction());
						adHolder.btnAdCallToAction.setVisibility(
								ad.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
//						AdChoicesView adChoicesView = new AdChoicesView(activity,
//								ad, true);
//						adHolder.adChoicesContainer.addView(adChoicesView, 0);

						AdOptionsView adOptionsView =
								new AdOptionsView(activity, ad, adHolder.nativeAdLayout);
						adHolder.adChoicesContainer.addView(adOptionsView, 0);

						List<View> clickableViews = new ArrayList<>();
						clickableViews.add(adHolder.ivAdIcon);
						clickableViews.add(adHolder.mvAdMedia);
						clickableViews.add(adHolder.btnAdCallToAction);
						ad.registerViewForInteraction(
								adHolder.itemView,
								adHolder.mvAdMedia,
								adHolder.ivAdIcon,
								clickableViews);
					}
                    break;
			case HOME_LOADING:
				ProgressHolder progressHolder = (ProgressHolder) holder;

				break;

            case AD_TYPE_GOOGLE:
				com.google.android.gms.ads.nativead.NativeAd unifiedNativeAd= (com.google.android.gms.ads.nativead.NativeAd ) object.getNativeAd();
                AdHolderGoogle adHolderGoogle = (AdHolderGoogle) holder;

				NativeAdView adView = (NativeAdView) LayoutInflater.from(activity)
                        .inflate(R.layout.ad_unified_updated, null);
                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                adHolderGoogle.fl_adplaceholder.removeAllViews();
                adHolderGoogle.fl_adplaceholder.addView(adView);
                break;
			case AD_TYPE_GG:
				String id= (String ) object.getNativeAd();
				AdHolderGG adHoldergg = (AdHolderGG) holder;
				adHoldergg.adUnit.setUnitId(id);
				adHoldergg.adUnit.loadAd(new AdLoadCallback(){
											 @Override
											 public void onReadyForRefresh() {
												 Log.d("GGADS","Ad Ready for refresh");
											 }
											 @Override
											 public void onUiiClosed() {
												 Log.d("GGADS","Uii closed");
											 }
											 @Override
											 public void onUiiOpened() {
												 Log.d("GGADS","Uii Opened");
											 }
											 @Override
											 public void onAdLoaded() {
												 Log.d("GGADS","Ad Loaded");
											 }

											 @Override
											 public void onAdLoadFailed(@NotNull AdErrors adErrors) {
												 Log.d("GGADS","Ad Load Failed ");
											 }
										 }
				);
				break;
			case DOUBLE_TYPE:

				initViewPager((CarousalViewHolder) holder);

//				final DoubleViewHolder doubleViewHolder= (DoubleViewHolder) holder;
//
//				doubleViewHolder.tvTitle.setText(object.getCategory());
//
//				doubleViewHolder.ll_category.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View view) {
//						EventManager.sendEvent(EventManager.LBL_DOUBLE_WALLPAPER,EventManager.ATR_KEY_DOUBLE,"Home Screen");
//
//						Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
//						intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(activity, Border_BorderlightWallpaperService.class));
//						activity.startActivity(intent);
//
////							Intent intent = new Intent(activity, DoubleWallpaperPagerActivity.class);
////							if (JesusApplication.getApplication().isAdLoaded() && !JesusApplication.getApplication().isDisable() && JesusApplication.getApplication().needToShowAd()) {
////								JesusApplication.getApplication().showInterstitial((Activity) activity, intent, false);
////							} else {
////								JesusApplication.getApplication().incrementUserClickCounter();
////								activity.startActivity(intent);
////							}
//
//					}
//				});
//
//				Glide.with(activity)
//						.load(Common.getDomainImages()+"double_thumb/"+object.getImg()).transition(DrawableTransitionOptions.withCrossFade())
//						.apply(new RequestOptions().placeholder(Common.getLoadingColor(activity)).error(R.mipmap.ic_error))
//						.into(doubleViewHolder.imageView);
//
//				break;
			}
	}

    private void populateUnifiedNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView adView) {
        // Set the media view. Media content will be automatically populated in the media view once
        // adView.setNativeAd() is called.
		com.google.android.gms.ads.nativead.MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        try {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.GONE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.GONE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);
    }

	@Override
	public int getItemCount() {
		if (mList == null)
			return 0;
		return mList.size();
//		return mList.size() + mAdItems.size();
	}

	@Override
	public int getItemViewType(int position) {

		if(mList==null || mList.size()==0){
			return HOME_LOADING;
		}
        if(mList.get(position).getNativeAd()!=null){

            if(mList.get(position).getNativeAd() instanceof NativeAd)
                return AD_TYPE;
			else if(mList.get(position).getNativeAd() instanceof String)
				return AD_TYPE_GG;
            else
                return AD_TYPE_GOOGLE;
        }

		if(mList.get(position)!=null && !TextUtils.isEmpty(mList.get(position).getPostId()) &&  mList.get(position).getPostId().equalsIgnoreCase("-2")){
			return DOUBLE_TYPE;
		}

//		 if(position==1 && mList.get(position).getPostId().equalsIgnoreCase("-2"))
//			return NO_DATA_TYPE;
//		else if(mList.get(position).getType().equalsIgnoreCase(AppConstant.TYPE_IMAGE_STRING))
//			return PHOTOS_TYPE;
//		else
	if(mList.get(position).getPostId().equalsIgnoreCase("-99"))
				return HOME_LOADING;
	else
				return PHOTOS_TYPE;
		}

	public static class OfferViewHolder extends RecyclerView.ViewHolder{

		ImageView thumbNail,img_delete,img_video;
		RelativeLayout rl_photos;

		public OfferViewHolder(View itemView,List<Wallpapers> mList) {
			super(itemView);

			thumbNail = (ImageView) itemView
					.findViewById(R.id.img_banner);
			rl_photos=itemView.findViewById(R.id.rl_photos);


			img_delete = (ImageView) itemView
					.findViewById(R.id.img_delete);
			img_video = (ImageView) itemView
					.findViewById(R.id.img_video);

			if(AppConstant.IS_DELETE)
				img_delete.setVisibility(View.VISIBLE);
			else
				img_delete.setVisibility(View.GONE);

			try {
				if(itemHeight!=0) {
					thumbNail.getLayoutParams().height=itemHeight;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
			{
				CardView card_view=itemView.findViewById(R.id.card_view);
				card_view.setMaxCardElevation(0f);
				card_view.setPreventCornerOverlap(false);
			}
		}
    }


	private void setDownload(final Wallpapers post){

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


	public class NoDataRowHolder extends RecyclerView.ViewHolder {
		protected RelativeLayout recycler_view_list;
		public NoDataRowHolder(View view) {
			super(view);
			this.recycler_view_list = (RelativeLayout) view.findViewById(R.id.rl_no_content);
		}
	}


	private static class AdHolder extends RecyclerView.ViewHolder {

		NativeAdLayout nativeAdLayout;
		MediaView mvAdMedia;
		MediaView ivAdIcon;
		TextView tvAdTitle;
		TextView tvAdBody;
		TextView tvAdSocialContext;
		TextView tvAdSponsoredLabel;
		Button btnAdCallToAction;
		LinearLayout adChoicesContainer;

		AdHolder(NativeAdLayout view) {
			super(view);
			nativeAdLayout = view;
			mvAdMedia = (MediaView) view.findViewById(R.id.native_ad_media);
			tvAdTitle = (TextView) view.findViewById(R.id.native_ad_title);
			tvAdBody = (TextView) view.findViewById(R.id.native_ad_body);
			tvAdSocialContext = (TextView) view.findViewById(R.id.native_ad_social_context);
			tvAdSponsoredLabel = (TextView) view.findViewById(R.id.native_ad_sponsored_label);
			btnAdCallToAction = (Button) view.findViewById(R.id.native_ad_call_to_action);
			ivAdIcon = (MediaView) view.findViewById(R.id.native_ad_icon);
			adChoicesContainer = (LinearLayout) view.findViewById(R.id.ad_choices_container);

		}
	}

	public void releaseResource(){
		if(mList!=null) {
			mList.clear();
			mList=null;
		}
		userInfoManager=null;
		settingStore=null;
		activity=null;
		handler=null;
		category_sel="";
	}
	protected class ProgressHolder extends RecyclerView.ViewHolder {
		CircularProgressBar mPocketBar;
		public ProgressHolder(View itemView) {
			super(itemView);
			mPocketBar = (CircularProgressBar) itemView.findViewById(R.id.pocket);
		}
	}

    private static class AdHolderGoogle extends RecyclerView.ViewHolder {

        FrameLayout fl_adplaceholder;

        AdHolderGoogle(View view) {
            super(view);

            fl_adplaceholder = (FrameLayout) view.findViewById(R.id.fl_adplaceholder);

        }
    }

	private static class AdHolderGG extends RecyclerView.ViewHolder {

		GGAdview adUnit;

		AdHolderGG(View view) {
			super(view);
			adUnit = view.findViewById(R.id.placeImageAd);
		}
	}

	public void setDeleteToServer(final Wallpapers post, final int pos){

		android.app.AlertDialog.Builder builder = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			SettingStore settingStore = SettingStore.getInstance(activity);
			boolean isDarkTheme = false;
			if (settingStore.getTheme()==0) {
				isDarkTheme=false;
			}else if (settingStore.getTheme()==1) {
				isDarkTheme=true;
			}
			if(isDarkTheme)
				builder = new android.app.AlertDialog.Builder(activity, R.style.CustomAlertDialog);
			else
				builder = new android.app.AlertDialog.Builder(activity, android.R.style.Theme_Material_Light_Dialog_Alert);

		} else {
			builder = new android.app.AlertDialog.Builder(activity);
		}

		builder.setTitle(activity.getResources().getString(R.string.delete_wallpaper_title));
		builder.setMessage(activity.getResources().getString(R.string.delete_msg));
		final DialogInterface.OnClickListener dialogButtonClickListener =
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							String api = CommonFunctions.getDomainLocal()+ (isVideoWall?AppConstant.URL_DELETE_LIVE:AppConstant.URL_DELETE);
							if(isStock_wallpaper)
								api= CommonFunctions.getDomainLocal()+ AppConstant.URL_DELETE_STOCK;

							RequestManager manager = new RequestManager(activity);
							manager.deletePostService(post.getPostId(),api, new NetworkCommunicationManager.CommunicationListnerNew() {
								@Override
								public void onSuccess(IModel response, int operationCode) {
									IModelBase base_model = (IModelBase) response;
									try {
										if(base_model!=null && base_model.getStatus().equalsIgnoreCase("1")){
											Logger.e("delete pos",""+pos);
											Logger.e("delete post id",""+post.getPostId());
											Logger.e("delete image name",""+post.getImg());
											if(mList!=null)
												mList.remove(pos);
											notifyDataSetChanged();
//											notifyItemRemoved(pos);
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								@Override
								public void onStartLoading() {
								}
								@Override
								public void onFail(WebServiceError errorMsg) {

								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
		builder.setPositiveButton(activity.getResources().getString(R.string.label_yes), dialogButtonClickListener);
		builder.setNegativeButton(activity.getResources().getString(R.string.label_no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
		builder.setCancelable(true);

		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onClick(View view) {
		int pos= Integer.parseInt(view.getTag().toString());
		Wallpapers post=mList.get(pos);
		switch (view.getId()){
			case R.id.img_delete:
				setDeleteToServer(post,pos);
				break;
		}
	}


	private class CarousalViewHolder extends RecyclerView.ViewHolder {
		public RecyclerViewPager vpRecyclerView;

		public CarousalViewHolder(View itemView) {
			super(itemView);
			vpRecyclerView = itemView.findViewById(R.id.vpRecyclerView);

			int screenWidth = CommonFunctions.getScreenWidth((Activity) activity);// - 2 * ((int) activity.getResources().getDimension(R.dimen.padding_carousal_item));
			int height = (int) (screenWidth / 2.7f);

			LinearLayout llCarousalParent = itemView.findViewById(R.id.llCarousalParent);
			llCarousalParent.getLayoutParams().height = height;

		}
	}

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			try {
				if (carousalAdapter != null && carousalViewHolder != null && carousalViewHolder.vpRecyclerView != null && mListCoursal!=null && mListCoursal.size()>0) {
					if (carousalAdapter.getItemCount() == carousalViewHolder.vpRecyclerView.getCurrentPosition() + 1) {
						if (AppConstant.CAROUSEL_AUTO_ROTATE) {
							carousalViewHolder.vpRecyclerView.setMillisecondsPerInch(25f);
							carousalViewHolder.vpRecyclerView.smoothScrollToPosition(0);
							//carousalViewHolder.vpRecyclerView.getLayoutManager().smoothScrollToPosition();scrollToPosition(0);
						}
						return;
					}
					carousalViewHolder.vpRecyclerView.smoothScrollToPosition(carousalViewHolder.vpRecyclerView.getCurrentPosition() + 1);
					//startCarouselScroll();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};


	public void startCarouselScroll() {
		if (carousalAdapter != null && carousalViewHolder != null && carousalViewHolder.vpRecyclerView != null && carousalAdapter.getItemCount() > 1) {
			stopCarouselScroll();
			Logger.i("HomeContentListFragment", "Carousel Scrolling Start");
			if(handler!=null)
				handler.postDelayed(runnable, AppConstant.CAROUSEL_ROTATE_TIME);
		}
	}

	public void stopCarouselScroll() {
		Logger.i("HomeContentListFragment", "Carousel Scrolling Stop");
		if(handler!=null)
			handler.removeCallbacks(runnable);
	}

	RecyclerViewPager mRecyclerView;
	private CarousalViewHolder carousalViewHolder;
	private CarousalAdapter carousalAdapter;
	protected void initViewPager(final CarousalViewHolder carousalViewHolder) {
		if (mListCoursal != null && mListCoursal.size() > 0) {
			carousalViewHolder.itemView.findViewById(R.id.llCarousalParent).setVisibility(View.VISIBLE);
			if (carousalAdapter != null)
				return;
			mRecyclerView = carousalViewHolder.vpRecyclerView;

			final LinearLayoutManager layout = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL,
					false);
			mRecyclerView.setLayoutManager(layout);
			layout.setSmoothScrollbarEnabled(true);
			carousalAdapter = new CarousalAdapter(activity,  mListCoursal);
			mRecyclerView.setAdapter(carousalAdapter);
			mRecyclerView.setHasFixedSize(true);
			mRecyclerView.setLongClickable(true);

//			int spacing = (int) activity.getResources()
//					.getDimension(R.dimen.content_padding_recycle);
//			mRecyclerView.addItemDecoration(new HorizontalItemDecoration(spacing));

			mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				boolean isUserScrolled = false;


				@Override
				public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
					switch (newState) {
						case RecyclerView.SCROLL_STATE_IDLE:
							System.out.println("Spotlight Scroll SCROLL_STATE_IDLE ::::" + isUserScrolled);
							isUserScrolled = false;
							//updateTopBottomColor();
							break;
						case RecyclerView.SCROLL_STATE_DRAGGING:
							isUserScrolled = true;
							System.out.println("Spotlight Scroll SCROLL_STATE_DRAGGING ::::" + isUserScrolled);
							break;
						case RecyclerView.SCROLL_STATE_SETTLING:
							System.out.println("Spotlight Scroll SCROLL_STATE_SETTLING ::::" + isUserScrolled);
							break;
					}
				}

				@Override
				public void onScrolled(RecyclerView recyclerView, int i, int i2) {

				}
			});
			mRecyclerView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
				@Override
				public void OnPageChanged(int oldPosition, int newPosition) {
					Log.d("test", "oldPosition:" + oldPosition + " newPosition:" + newPosition);
					if (newPosition == 0) {
						carousalViewHolder.vpRecyclerView.setMillisecondsPerInch(100f);
					}
					startCarouselScroll();
				}
			});

			mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {


				}
			});
			//if (homeListingData != null && homeListingData.getContent() != null && homeListingData.getContent().size() > 1) {
			startCarouselScroll();
			//}
		} else {
			carousalViewHolder.itemView.findViewById(R.id.llCarousalParent).setVisibility(View.GONE);
		}
	}

}
