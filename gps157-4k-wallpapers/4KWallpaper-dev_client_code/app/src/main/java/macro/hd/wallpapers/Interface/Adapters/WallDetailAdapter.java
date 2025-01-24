package macro.hd.wallpapers.Interface.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import macro.hd.wallpapers.Interface.Activity.PreviewActivity;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Model.Wallpapers;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
//import com.google.android.gms.ads.VideoController;
//import com.google.android.gms.ads.formats.UnifiedNativeAd;
//import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.util.List;

public class WallDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private List<Wallpapers> itemsList;
	private Context mContext;
	private static final int AD_TYPE_GOOGLE = 6;
	public static final int PHOTOS_TYPE = 2;

	public static void setItemWidhtHeight(int itemWidht,int itemHeight) {
		WallDetailAdapter.itemHeight = itemHeight;
		WallDetailAdapter.itemWidht = itemWidht;
	}

	private static int itemHeight,itemWidht;

	public WallDetailAdapter(Context context, List<Wallpapers> itemsList) {
		this.itemsList = itemsList;
		this.mContext = context;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_photo_detail_new, null);
//		SingleItemRowHolder mh = new SingleItemRowHolder(v);
//		return mh;

		View view;
		switch (viewType) {
			case PHOTOS_TYPE:
				view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_photo_detail_new, null);
				return new SingleItemRowHolder(view);

			case AD_TYPE_GOOGLE:
				View inflatedView1 = LayoutInflater.from(mContext).inflate(R.layout
						.google_native_ad_resize, viewGroup, false);
				return new AdHolderGoogle(inflatedView1);

		}
		return null;
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

		final int type=getItemViewType(position);
		final Wallpapers post=itemsList.get(position);
		switch (type) {
			case PHOTOS_TYPE:
				final SingleItemRowHolder photoViewHolder=((SingleItemRowHolder) holder);

				photoViewHolder.layout_loading.setVisibility(View.VISIBLE);

				String path = CommonFunctions.getDomainImages() + "thumb/" + post.getImg();
				if(!TextUtils.isEmpty(post.getImg()) && post.getImg().startsWith("http")) {
					path=post.getSearch_thumb();
				}
//		Logger.e("path","path:"+path);
				photoViewHolder.rl_photos.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						Intent intent1=new Intent(mContext, PreviewActivity.class);
						String path = CommonFunctions.getDomainImages() + "uhd/" + post.getImg();
						try {
							if(post.getImg().startsWith("http")) {
								path=post.getImg();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						intent1.putExtra("path",path);
						mContext.startActivity(intent1);
					}
				});

				if (!TextUtils.isEmpty(path))
					Glide.with(mContext)
							.load(path).apply(new RequestOptions().placeholder(CommonFunctions.getLoadingColorBlack(mContext))).transition(DrawableTransitionOptions.withCrossFade())
							.listener(new RequestListener<Drawable>() {
								@Override
								public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
									photoViewHolder.layout_loading.setVisibility(View.GONE);
									return false;
								}

								@Override
								public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
									photoViewHolder.layout_loading.setVisibility(View.GONE);
//							jesusDetailNewFragment.setBlurrImage(((BitmapDrawable)resource).getBitmap());
									return false;
								}
							})
							.into(photoViewHolder.thumbNail);
				break;
			// comment advertise
//			case AD_TYPE_GOOGLE:
//				UnifiedNativeAd unifiedNativeAd= (UnifiedNativeAd ) post.getNativeAd();
//				AdHolderGoogle adHolderGoogle = (AdHolderGoogle) holder;
//
//				UnifiedNativeAdView adView = (UnifiedNativeAdView) LayoutInflater.from(mContext)
//						.inflate(R.layout.ad_unified_detail, null);
//				populateUnifiedNativeAdView(unifiedNativeAd, adView);
//				adHolderGoogle.fl_adplaceholder.removeAllViews();
//				adHolderGoogle.fl_adplaceholder.addView(adView);
//				break;
		}
	}

	// comment advertise
//	private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
//		// Set the media view. Media content will be automatically populated in the media view once
//		// adView.setNativeAd() is called.
//		com.google.android.gms.ads.formats.MediaView mediaView = adView.findViewById(R.id.ad_media);
//		adView.setMediaView(mediaView);
//
//		// Set other ad assets.
//		adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
//		adView.setBodyView(adView.findViewById(R.id.ad_body));
//		adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
//		adView.setIconView(adView.findViewById(R.id.ad_app_icon));
//		adView.setPriceView(adView.findViewById(R.id.ad_price));
//		adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
//		adView.setStoreView(adView.findViewById(R.id.ad_store));
//		adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
//
//		// The headline is guaranteed to be in every UnifiedNativeAd.
//		try {
//			((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		// These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
//		// check before trying to display them.
//		if (nativeAd.getBody() == null) {
//			adView.getBodyView().setVisibility(View.INVISIBLE);
//		} else {
//			adView.getBodyView().setVisibility(View.VISIBLE);
//			((TextView) adView.getBodyView()).setText(nativeAd.getBody());
//		}
//
//		if (nativeAd.getCallToAction() == null) {
//			adView.getCallToActionView().setVisibility(View.INVISIBLE);
//		} else {
//			adView.getCallToActionView().setVisibility(View.VISIBLE);
//			((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
//		}
//
//		if (nativeAd.getIcon() == null) {
//			adView.getIconView().setVisibility(View.GONE);
//		} else {
//			((ImageView) adView.getIconView()).setImageDrawable(
//					nativeAd.getIcon().getDrawable());
//			adView.getIconView().setVisibility(View.VISIBLE);
//		}
//
////		if (nativeAd.getPrice() == null) {
////			adView.getPriceView().setVisibility(View.GONE);
////		} else {
////			adView.getPriceView().setVisibility(View.VISIBLE);
////			((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
////		}
//
////		if (nativeAd.getStore() == null) {
////			adView.getStoreView().setVisibility(View.GONE);
////		} else {
////			adView.getStoreView().setVisibility(View.VISIBLE);
////			((TextView) adView.getStoreView()).setText(nativeAd.getStore());
////		}
//
//		if (nativeAd.getStarRating() == null) {
//			adView.getStarRatingView().setVisibility(View.INVISIBLE);
//		} else {
//			((RatingBar) adView.getStarRatingView())
//					.setRating(nativeAd.getStarRating().floatValue());
//			adView.getStarRatingView().setVisibility(View.VISIBLE);
//		}
//
//		if (nativeAd.getAdvertiser() == null) {
//			adView.getAdvertiserView().setVisibility(View.INVISIBLE);
//		} else {
//			((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
//			adView.getAdvertiserView().setVisibility(View.VISIBLE);
//		}
//
//		// This method tells the Google Mobile Ads SDK that you have finished populating your
//		// native ad view with this native ad. The SDK will populate the adView's MediaView
//		// with the media content from this native ad.
//		adView.setNativeAd(nativeAd);
//
//		// Get the video controller for the ad. One will always be provided, even if the ad doesn't
//		// have a video asset.
//		VideoController vc = nativeAd.getVideoController();
//
//		// Updates the UI to say whether or not this ad has a video asset.
//		if (vc.hasVideoContent()) {
////			videoStatus.setText(String.format(Locale.getDefault(),
////					"Video status: Ad contains a %.2f:1 video asset.",
////					vc.getAspectRatio()));
//
//			// Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
//			// VideoController will call methods on this object when events occur in the video
//			// lifecycle.
//			vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
//				@Override
//				public void onVideoEnd() {
//					// Publishers should allow native ads to complete video playback before
//					// refreshing or replacing them with another ad in the same UI location.
////					refresh.setEnabled(true);
////					videoStatus.setText("Video status: Video playback has ended.");
//					super.onVideoEnd();
//				}
//			});
//		} else {
////			videoStatus.setText("Video status: Ad does not contain a video asset.");
////			refresh.setEnabled(true);
//		}
//	}

	@Override
	public int getItemViewType(int position) {
		if(itemsList.get(position).getNativeAd()!=null){
			return AD_TYPE_GOOGLE;
		}
		return PHOTOS_TYPE;
	}


	@Override
	public int getItemCount() {
		return (null != itemsList ? itemsList.size() : 0);
	}

	public static class SingleItemRowHolder extends RecyclerView.ViewHolder{
		View rootView;
		ImageView thumbNail;
		View layout_loading;
		CardView rl_photos;

		public SingleItemRowHolder(View view) {
			super(view);
			rootView=view;
			this.layout_loading = view.findViewById(R.id.layout_loading);
			thumbNail=view.findViewById(R.id.img_banner);
			rl_photos=view.findViewById(R.id.rl_photos);

			try {
				if(itemWidht!=0 && itemHeight!=0) {
					thumbNail.getLayoutParams().height=itemHeight;
					thumbNail.getLayoutParams().width=itemWidht;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
				{
					rl_photos.setMaxCardElevation(0f);
					rl_photos.setPreventCornerOverlap(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static class AdHolderGoogle extends RecyclerView.ViewHolder {

		FrameLayout fl_adplaceholder;

		AdHolderGoogle(View view) {
			super(view);
			fl_adplaceholder = (FrameLayout) view.findViewById(R.id.fl_adplaceholder);
		}
	}

}