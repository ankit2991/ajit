/*
 * Copyright (C) 2014 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package macro.hd.wallpapers.Interface.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import macro.hd.wallpapers.Interface.Activity.CategoryListingActivity;
import macro.hd.wallpapers.Interface.Activity.DoubleWallpaperActivity;
import macro.hd.wallpapers.Interface.Activity.GradientActivity;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.LightWallpaperService.EdgeSettings;
import macro.hd.wallpapers.Model.Wallpapers;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class CarousalAdapter extends RecyclerView.Adapter<CarousalAdapter.CarousalViewHolder> {
    private Context mContext;
    private List<Wallpapers> homeListingContents = new ArrayList<>();
    private int count = 0;

    public CarousalAdapter(Context context, List<Wallpapers> carousalMediaItems) {
        mContext = context;
        this.homeListingContents = carousalMediaItems;
        count = homeListingContents.size();
    }


    public class CarousalViewHolder extends RecyclerView.ViewHolder {

        protected TextView tvTitle;
        private RelativeLayout ll_category;
        private ImageView imageView;
        private CardView card_view;

        public CarousalViewHolder(View view) {
            super(view);
            this.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            imageView = view.findViewById(R.id.img_cat);
            card_view = view.findViewById(R.id.card_view);
            ll_category = (RelativeLayout) view.findViewById(R.id.ll_category);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            {
                card_view.setMaxCardElevation(0f);
                card_view.setPreventCornerOverlap(false);
            }
        }
    }

    @Override
    public CarousalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(mContext).inflate(R.layout.list_item_doublewall, parent, false);
        return new CarousalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CarousalViewHolder holder, final int pos) {
        final Wallpapers object = homeListingContents.get(pos);

        if(homeListingContents==null || homeListingContents.size()==0)
            return;
        final CarousalViewHolder doubleViewHolder= (CarousalViewHolder) holder;

        doubleViewHolder.ll_category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(object.getPostId()) && object.getPostId().equalsIgnoreCase("-1")){
                    EventManager.sendEvent(EventManager.LBL_DOUBLE_WALLPAPER,EventManager.ATR_KEY_DOUBLE,"Home Screen");
                    Intent intent = new Intent(mContext, DoubleWallpaperActivity.class);
                    if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
                        WallpapersApplication.getApplication().showInterstitial((Activity) mContext, intent, false);
                    } else {
                        WallpapersApplication.getApplication().incrementUserClickCounter();
                        mContext.startActivity(intent);
                    }
                }else if(!TextUtils.isEmpty(object.getPostId()) && object.getPostId().equalsIgnoreCase("-2")){
                    EventManager.sendEvent(EventManager.LBL_EDGE_WALLPAPER,EventManager.ATR_EDGE_WALLPAPER,"Home Screen");

                    Intent intent = new Intent(mContext, EdgeSettings.class);
                    if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
                        WallpapersApplication.getApplication().showInterstitial((Activity) mContext, intent, false);
                    } else {
                        WallpapersApplication.getApplication().incrementUserClickCounter();
                        mContext.startActivity(intent);
                    }

                }else if(!TextUtils.isEmpty(object.getPostId()) && object.getPostId().equalsIgnoreCase("-4")){
                    EventManager.sendEvent(EventManager.LBL_GRADIENT_WALLPAPER,EventManager.ATR_GRADIENT_WALLPAPER,"Home Screen");

                    Intent intent = new Intent(mContext, GradientActivity.class);
                    if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
                        WallpapersApplication.getApplication().showInterstitial((Activity) mContext, intent, false);
                    } else {
                        WallpapersApplication.getApplication().incrementUserClickCounter();
                        mContext.startActivity(intent);
                    }

                }else if(!TextUtils.isEmpty(object.getPostId()) && object.getPostId().equalsIgnoreCase("-9")){
                    final Intent intent=new Intent(mContext, CategoryListingActivity.class);
                    intent.putExtra("category",object.getCategory());
                    intent.putExtra("category_name",object.getCategory_title());
                    if (WallpapersApplication.getApplication().isAdLoaded() && !WallpapersApplication.getApplication().isDisable() && WallpapersApplication.getApplication().needToShowAd()) {
                        WallpapersApplication.getApplication().showInterstitial((Activity) mContext, intent, false);
                    } else {
                        WallpapersApplication.getApplication().incrementUserClickCounter();
                        mContext.startActivity(intent);
                    }
                }
            }
        });

        if(mContext!=null && !((Activity)mContext).isFinishing()){
            if(object.getPostId().equalsIgnoreCase("-9")){
                Glide.with(mContext)
                        .load(CommonFunctions.getDomainImages()+"category/feature/"+object.getImg()).transition(DrawableTransitionOptions.withCrossFade())
                        .apply(new RequestOptions().placeholder(CommonFunctions.getLoadingColor(mContext)).error(R.mipmap.ic_error))
                        .into(doubleViewHolder.imageView);
            }else
                Glide.with(mContext)
                        .load(CommonFunctions.getDomainImages()+"double_thumb/"+object.getImg()).transition(DrawableTransitionOptions.withCrossFade())
                        .apply(new RequestOptions().placeholder(CommonFunctions.getLoadingColor(mContext)).error(R.mipmap.ic_error))
                        .into(doubleViewHolder.imageView);
        }

    }

    public int getCount() {
        return count;
    }

    @Override
    public int getItemCount() {
        if(homeListingContents==null || homeListingContents.size()==0)
            return 0;
        return count;
    }

    public void destroy(){
        if(homeListingContents!=null)
            homeListingContents.clear();
        count = 0;
        homeListingContents=null;
        mContext=null;
    }
}
