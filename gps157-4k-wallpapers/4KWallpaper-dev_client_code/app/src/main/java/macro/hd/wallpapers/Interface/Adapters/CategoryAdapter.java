package macro.hd.wallpapers.Interface.Adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CategoryCallback;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Model.Category;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
//import com.google.android.gms.ads.VideoController;
//import com.google.android.gms.ads.formats.UnifiedNativeAd;
//import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Category> itemsList;
    private Context mContext;

    public void setStock_category(List<Category> stock_category) {
        if (this.stock_category == null)
            this.stock_category = new ArrayList<Category>();

        if (this.stock_category != null)
            this.stock_category.clear();

        if (stock_category != null)
            this.stock_category.addAll(new ArrayList<>(stock_category));
    }


    private List<Category> stock_category = new ArrayList<Category>();

    public static final int NORMAL_TYPE = 1;
    public static final int ADVERTISE_TYPE = 2;
    public static final int HORIZONTAL_TYPE = 3;
    public static final int DOUBLE_TYPE = 4;
    private static final int AD_TYPE_GOOGLE = 6;
    private int height;

    public void setCategoryCallback(CategoryCallback categoryCallback) {
        this.categoryCallback = categoryCallback;
    }

    private CategoryCallback categoryCallback;
    boolean isTitleShow;
    private String category_folder;

    public CategoryAdapter(Context context, List<Category> itemsList) {
        this.itemsList = itemsList;
        this.mContext = context;

        try {
            isTitleShow = WallpapersApplication.getApplication().getSettings().isCatTitleEnable();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            category_folder = WallpapersApplication.getApplication().getSettings().getCategoryFolder();
        } catch (Exception e) {
            e.printStackTrace();
            category_folder = "category/newcat/";
        }

        try {
            int screenWidth = CommonFunctions.getScreenWidth((Activity) context);// - 2 * ((int) activity.getResources().getDimension(R.dimen.padding_carousal_item));
            height = (int) (screenWidth / 2.8f);
            int itemWidht_diment = (int) context.getResources().getDimension(R.dimen.category_item_width);
            Logger.e("CategoryAdapterNew", "height:" + height + " itemHeight_diment:" + itemWidht_diment);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        switch (viewType) {

            case NORMAL_TYPE:
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_category_new, null);
                SingleItemRowHolder mh = new SingleItemRowHolder(v);
                return mh;
            case ADVERTISE_TYPE:
                View v_ad = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_advertise, null);
                AdvertisementHolder mh1 = new AdvertisementHolder(v_ad);
                return mh1;
            case DOUBLE_TYPE:
                View v_double = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_doublewall, null);
                DoubleViewHolder doubleViewHolder = new DoubleViewHolder(v_double);
                return doubleViewHolder;
            case HORIZONTAL_TYPE:
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_category_horizantal_list, null);
                return new OfferViewHolder(view);
            case AD_TYPE_GOOGLE:
                View inflatedView1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout
                        .google_native_ad, null);
                return new AdHolderGoogle(inflatedView1);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int i) {

        int type = getItemViewType(i);

        switch (type) {

            case NORMAL_TYPE:
                final Category singleItem = itemsList.get(i);
                final SingleItemRowHolder singleItemRowHolder = (SingleItemRowHolder) holder;

                if (isTitleShow) {
                    singleItemRowHolder.tvTitle.setVisibility(View.VISIBLE);
                } else
                    singleItemRowHolder.tvTitle.setVisibility(View.GONE);

                singleItemRowHolder.tvTitle.setText((singleItem.getDisplay_name()));

                singleItemRowHolder.ll_category.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (categoryCallback != null) {
                            categoryCallback. clickCategory(singleItem, false, false,false);
                            notifyDataSetChanged();
                        }
                    }
                });

//				int pos=i%4;
//				int drawable=0;
//				if(pos==0)
//					drawable=R.drawable.image1;
//				else if(pos==1)
//					drawable=R.drawable.image2;
//				else if(pos==2)
//					drawable=R.drawable.image3;
//				else if(pos==3)
//					drawable=R.drawable.image4;

//				Glide.with(mContext)
//						.load(drawable).transition(DrawableTransitionOptions.withCrossFade())
//						.apply(new RequestOptions().error(R.mipmap.ic_error))
//						.into(singleItemRowHolder.imageView);


//				Logger.e("img",""+Common.getDomainImages()+"category/new/"+singleItem.getImage_new());
                if (!TextUtils.isEmpty(singleItem.getImage_new())) {
                    Glide.with(mContext)
                            .load(CommonFunctions.getDomainImages() + category_folder + singleItem.getImage_new()).transition(DrawableTransitionOptions.withCrossFade()).thumbnail(0.1f)
                            .apply(new RequestOptions().placeholder(R.drawable.img_placeholder).error(R.drawable.img_placeholder)).into(singleItemRowHolder.imageView);
                } else {
                    Glide.with(mContext)
                            .load(R.drawable.img_placeholder).transition(DrawableTransitionOptions.withCrossFade()).thumbnail(0.1f)
                            .into(singleItemRowHolder.imageView);
                }
//				if(i%2==0)
//					Glide.with(mContext)
//						.load(R.mipmap.cat_space7).transition(DrawableTransitionOptions.withCrossFade())
//						.into(singleItemRowHolder.imageView);
//				else
//					Glide.with(mContext)
//							.load(R.mipmap.cat_nature2).transition(DrawableTransitionOptions.withCrossFade())
//							.into(singleItemRowHolder.imageView);
                break;

            case ADVERTISE_TYPE:
                final Category category = itemsList.get(i);


                final AdvertisementHolder advertisementHolder = (AdvertisementHolder) holder;

//                if(i%3==0){
//                    setMargins(advertisementHolder.card_view,(int) mContext.getResources().getDimension(R.dimen.value_6),(int) mContext.getResources().getDimension(R.dimen.value_6), (int) mContext.getResources().getDimension(R.dimen.value_6),0);
//                }else{
//                    setMargins(advertisementHolder.card_view,(int) mContext.getResources().getDimension(R.dimen.value_6),(int) mContext.getResources().getDimension(R.dimen.value_6), 0,0);
//                }


                advertisementHolder.tvTitle.setText(category.getDisplay_name());

                advertisementHolder.ll_category.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (categoryCallback != null) {
                            categoryCallback.clickCategory(category, false, false,false);
                            notifyDataSetChanged();
                        }
                    }
                });

                if (!TextUtils.isEmpty(category.getImage()))
                    Glide
                            .with(mContext)
                            .load(CommonFunctions.getDomainImages() + "category/" + category.getImage()).transition(DrawableTransitionOptions.withCrossFade())
                            .apply(new RequestOptions().placeholder(CommonFunctions.getLoadingColor(mContext)).error(R.mipmap.ic_error))
                            .into(advertisementHolder.imageView);


                if (TextUtils.isEmpty(category.getLink())) {
                    advertisementHolder.txt_ad.setVisibility(View.GONE);
                } else
                    advertisementHolder.txt_ad.setVisibility(View.VISIBLE);
                break;
            case DOUBLE_TYPE:
                final Category double_category = itemsList.get(i);

                final DoubleViewHolder doubleViewHolder = (DoubleViewHolder) holder;

                doubleViewHolder.tvTitle.setText(double_category.getDisplay_name());

                doubleViewHolder.ll_category.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (categoryCallback != null) {
                            categoryCallback.clickCategory(double_category, false, false,false);
                        }
                    }
                });

                Glide.with(mContext)
                        .load(CommonFunctions.getDomainImages() + "double_thumb/" + double_category.getImage()).transition(DrawableTransitionOptions.withCrossFade())
                        .apply(new RequestOptions().placeholder(CommonFunctions.getLoadingColor(mContext)).error(R.mipmap.ic_error))
                        .into(doubleViewHolder.imageView);

//					Glide.with(mContext)
//							.load(Common.getDomainImages()+"category/"+double_category.getImage())
//							.apply(new RequestOptions().placeholder(Common.getLoadingColor(mContext)).error(R.mipmap.ic_error))
//							.into(doubleViewHolder.imageView);

                break;
            case HORIZONTAL_TYPE:

                final OfferViewHolder offerViewHolder = ((OfferViewHolder) holder);

                if (SettingStore.getInstance(mContext).getTheme() == 1) {
                    offerViewHolder.txt_color_header.setTextColor((mContext.getResources().getColor(R.color.white)));
                    offerViewHolder.txt_hashtag.setTextColor(mContext.getResources().getColor(R.color.white));
                } else {
                    offerViewHolder.txt_color_header.setTextColor((mContext.getResources().getColor(R.color.black)));
                    offerViewHolder.txt_hashtag.setTextColor(mContext.getResources().getColor(R.color.black));
                }

                StockWallpaperAdapter storyAdapter = new StockWallpaperAdapter(mContext, stock_category);
                storyAdapter.setCategoryCallback(categoryCallback);
                LinearLayoutManager layoutManager
                        = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
                offerViewHolder.rvStoryList.setLayoutManager(layoutManager);
                offerViewHolder.rvStoryList.setAdapter(storyAdapter);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return (null != itemsList ? itemsList.size() : 0);
    }

    public class AdvertisementHolder extends RecyclerView.ViewHolder {

        protected TextView tvTitle;
        protected TextView txt_ad;
        private RelativeLayout ll_category;
        private ImageView imageView;
        private CardView card_view;

        public AdvertisementHolder(View view) {
            super(view);
            this.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            this.txt_ad = (TextView) view.findViewById(R.id.txt_ad);
            imageView = view.findViewById(R.id.img_cat);
            card_view = view.findViewById(R.id.card_view);
            ll_category = (RelativeLayout) view.findViewById(R.id.ll_category);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                CardView card_view = view.findViewById(R.id.card_view);
                card_view.setMaxCardElevation(0f);
                card_view.setPreventCornerOverlap(false);
            }
        }
    }

    public class DoubleViewHolder extends RecyclerView.ViewHolder {

        protected TextView tvTitle;
        private RelativeLayout ll_category;
        private ImageView imageView;
        private CardView card_view;

        public DoubleViewHolder(View view) {
            super(view);
            this.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            imageView = view.findViewById(R.id.img_cat);
            card_view = view.findViewById(R.id.card_view);
            ll_category = (RelativeLayout) view.findViewById(R.id.ll_category);
        }
    }

    public class SingleItemRowHolder extends RecyclerView.ViewHolder {

        protected TextView tvTitle;
        private RelativeLayout ll_category;
        private ImageView imageView, img_placeholder;

        public SingleItemRowHolder(View view) {
            super(view);
            this.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            imageView = view.findViewById(R.id.img_cat);
            img_placeholder = view.findViewById(R.id.img_placeholder);
            ll_category = (RelativeLayout) view.findViewById(R.id.ll_category);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), tvTitle.getText(), Toast.LENGTH_SHORT).show();
                }
            });

            try {
                if (height != 0) {
                    ll_category.getLayoutParams().height = height;
                }
            } catch (Exception e) {
                e.printStackTrace();
                ll_category.getLayoutParams().height = (int) mContext.getResources().getDimension(R.dimen.category_item_width);
            }

//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
//			{
//				CardView card_view=view.findViewById(R.id.card_view);
//				card_view.setMaxCardElevation(0f);
//				card_view.setPreventCornerOverlap(false);
//			}

//			Typeface face = Typeface.createFromAsset(mContext.getAssets(),
//					"RAVIE.TTF");
//			tvTitle.setTypeface(face);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (itemsList.get(position).getNativeAd() != null) {
            return AD_TYPE_GOOGLE;
        }

        if (itemsList.get(position) != null && !TextUtils.isEmpty(itemsList.get(position).getCat_id()) && itemsList.get(position).getCat_id().equalsIgnoreCase("-1")) {
            return HORIZONTAL_TYPE;
        } else if (itemsList.get(position) != null && !TextUtils.isEmpty(itemsList.get(position).getCat_id()) && itemsList.get(position).getCat_id().equalsIgnoreCase("-2")) {
            return DOUBLE_TYPE;
        }else if (itemsList.get(position) != null && !TextUtils.isEmpty(itemsList.get(position).getLink())) {
            return ADVERTISE_TYPE;
        } else
            return NORMAL_TYPE;
    }

    public class OfferViewHolder extends RecyclerView.ViewHolder {

        RecyclerView rvStoryList;
        TextView txt_hashtag, txt_color_header;

        public OfferViewHolder(View itemView) {
            super(itemView);
            rvStoryList = (RecyclerView) itemView.findViewById(R.id.rvStoryList);
            txt_hashtag = itemView.findViewById(R.id.txt_stock_hashtag);
            txt_color_header = itemView.findViewById(R.id.other_category_txt);
        }
    }


    private static class AdHolderGoogle extends RecyclerView.ViewHolder {

        FrameLayout fl_adplaceholder;

        AdHolderGoogle(View view) {
            super(view);

            fl_adplaceholder = (FrameLayout) view.findViewById(R.id.fl_adplaceholder);

        }
    }

    public void releaseResource() {
        mContext = null;
    }
}
