package com.lockerroom.face.tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lockerroom.face.Constants;
import com.lockerroom.face.PhotoApp;
import com.lockerroom.face.R;
import com.lockerroom.face.activities.MainActivity;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.interfaces.RewardedAdCallbck;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.utils.CheckRewardedAdTime;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.SharePreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class EditingToolsAdapter extends RecyclerView.Adapter<EditingToolsAdapter.ViewHolder> {

    public OnItemSelected mOnItemSelected;
    private SharedPreferences sharedPreferences;

    public List<ToolModel> mToolList = new ArrayList<>();

    public interface OnItemSelected {
        void onToolSelected(ToolType toolType);
    }

    public FragmentActivity mContext;

    public EditingToolsAdapter(OnItemSelected onItemSelected, FragmentActivity mContext) {
        this.mContext = mContext;
        this.mOnItemSelected = onItemSelected;
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.crop), R.drawable.ic_crop_two, ToolType.CROP));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.adjust), R.drawable.ic_adjust_two, ToolType.ADJUST));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.filters), R.drawable.ic_filter_two, ToolType.FILTER));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.overlay), R.drawable.ic_overlay_two, ToolType.OVERLAY));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.sticker), R.drawable.ic_sticker_two, ToolType.STICKER));
//        this.mToolList.add(new ToolModel("Text", R.drawable.ic_text_two, ToolType.TEXT));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.fit), R.drawable.ic_insta_two, ToolType.INSTA));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.blur), R.drawable.ic_blur_two, ToolType.BLUR));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.splas), R.drawable.ic_splash_two, ToolType.SPLASH));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.brush), R.drawable.ic_paint_two, ToolType.BRUSH));
//        this.mToolList.add(new ToolModel("Mosaic", R.drawable.mosaic, ToolType.MOSAIC));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.beauty_tool), R.drawable.beauty, ToolType.BEAUTY));

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (Constants.SHOW_ADS) {
            IronSourceAdsManager.INSTANCE.loadInter(mContext, new IronSourceCallbacks() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFail() {

                }
            });

        }
    }

    public EditingToolsAdapter(FragmentActivity mContext, OnItemSelected onItemSelected, boolean z) {
        this.mContext = mContext;
        this.mOnItemSelected = onItemSelected;
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.layout), R.drawable.ic_collage, ToolType.LAYOUT));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.border), R.drawable.ic_border, ToolType.BORDER));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.ratio), R.drawable.ic_ratio, ToolType.RATIO));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.filters), R.drawable.ic_filter_two, ToolType.FILTER));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.sticker), R.drawable.ic_sticker_two, ToolType.STICKER));
//        this.mToolList.add(new ToolModel("Text", R.drawable.ic_text_two, ToolType.TEXT));
        this.mToolList.add(new ToolModel(PhotoApp.resources.getString(R.string.bg), R.drawable.background_icon, ToolType.BACKGROUND));

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
//        if (Constants.SHOW_ADS) {
//            IronSourceAdsManager.INSTANCE.loadInter(mContext, new IronSourceCallbacks() {
//                @Override
//                public void onSuccess() {
//
//                }
//
//                @Override
//                public void onFail() {
//
//                }
//            });
//
//        }
    }

    class ToolModel {

        public int mToolIcon;

        public String mToolName;

        public ToolType mToolType;

        ToolModel(String str, int i, ToolType toolType) {
            this.mToolName = str;
            this.mToolIcon = i;
            this.mToolType = toolType;
        }
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_editing_tools, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ToolModel toolModel = this.mToolList.get(i);
        viewHolder.txtTool.setText(toolModel.mToolName);
        viewHolder.imgToolIcon.setImageResource(toolModel.mToolIcon);

        if (EditingToolsAdapter.this.mToolList.get(i).mToolName.equals(PhotoApp.resources.getString(R.string.filters)) || EditingToolsAdapter.this.mToolList.get(i).mToolName.equals(PhotoApp.resources.getString(R.string.sticker))|| EditingToolsAdapter.this.mToolList.get(i).mToolName.equals(PhotoApp.resources.getString(R.string.splas)) || EditingToolsAdapter.this.mToolList.get(i).mToolName.equals(PhotoApp.resources.getString(R.string.beauty_tool))){
            viewHolder.adIcon.setVisibility(View.VISIBLE);
            viewHolder.adUnlockText.setVisibility(View.VISIBLE);
        }else{
            viewHolder.adIcon.setVisibility(View.GONE);
            viewHolder.adUnlockText.setVisibility(View.GONE);
        }

        if (EditingToolsAdapter.this.mToolList.get(i).mToolName.equals(PhotoApp.resources.getString(R.string.filters))){
            Glide.with(mContext).load(R.drawable.ad_icon_blue).into(viewHolder.adIcon);
            viewHolder.adUnlockText.setText(R.string.unlock_filter);
            CheckRewardedAdTime.INSTANCE.checkAdTime(mContext,MyConstant.LAST_CLICK_TIMESTAMP_FILTER,viewHolder.adIcon);
        }
        if (EditingToolsAdapter.this.mToolList.get(i).mToolName.equals(PhotoApp.resources.getString(R.string.sticker))){
            Glide.with(mContext).load(R.drawable.ad_icon_pink).into(viewHolder.adIcon);
            viewHolder.adUnlockText.setText(R.string.unlock_sticker);
            CheckRewardedAdTime.INSTANCE.checkAdTime(mContext,MyConstant.LAST_CLICK_TIMESTAMP_STICKER,viewHolder.adIcon);
        }
        if (EditingToolsAdapter.this.mToolList.get(i).mToolName.equals(PhotoApp.resources.getString(R.string.splas))){
            Glide.with(mContext).load(R.drawable.ad_icon_orange).into(viewHolder.adIcon);
            viewHolder.adUnlockText.setText(R.string.unlock_splash);
            CheckRewardedAdTime.INSTANCE.checkAdTime(mContext,MyConstant.LAST_CLICK_TIMESTAMP_SPLASH,viewHolder.adIcon);
        }
        if (EditingToolsAdapter.this.mToolList.get(i).mToolName.equals(PhotoApp.resources.getString(R.string.beauty_tool))){
            Glide.with(mContext).load(R.drawable.ad_icon).into(viewHolder.adIcon);
            viewHolder.adUnlockText.setText(R.string.unlock_beauty);
            CheckRewardedAdTime.INSTANCE.checkAdTime(mContext,MyConstant.LAST_CLICK_TIMESTAMP_BEAUTY,viewHolder.adIcon);
        }
    }

    public int getItemCount() {
        return this.mToolList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgToolIcon;
        ImageView adIcon;
        TextView adUnlockText;
        TextView txtTool;
        ConstraintLayout wrapTool;

        ViewHolder(View view) {
            super(view);
            this.imgToolIcon = view.findViewById(R.id.imgToolIcon);
            this.adIcon = view.findViewById(R.id.adIcon);
            this.txtTool = view.findViewById(R.id.txtTool);
            this.adUnlockText = view.findViewById(R.id.adUnlockText);
            this.wrapTool = view.findViewById(R.id.wrapTool);

            //setting ad icon

            this.wrapTool.setOnClickListener(view1 -> {
                MyConstant.INSTANCE.setTapCount(MyConstant.INSTANCE.getTapCount() + 1);
//                Log.i("RemoteVallue", "onClick: "+MyConstant.INSTANCE.getTapCount());
//                if (MyConstant.INSTANCE.interCriteria()) {
//                    IronSourceAdsManager.INSTANCE.showInter(mContext, new IronSourceCallbacks() {
//
//                        @Override
//                        public void onSuccess() {
//                            Toast.makeText(mContext, ">"+((EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition())).mToolName), Toast.LENGTH_SHORT).show();
//                            EditingToolsAdapter.this.mOnItemSelected.onToolSelected((EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition())).mToolType);
//                        }
//
//                        @Override
//                        public void onFail() {
//                            Toast.makeText(mContext, ">"+((EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition())).mToolName), Toast.LENGTH_SHORT).show();
//                            EditingToolsAdapter.this.mOnItemSelected.onToolSelected((EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition())).mToolType);
//                        }
//                    });
//                } else {
//                    Toast.makeText(mContext, ">"+((EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition())).mToolName), Toast.LENGTH_SHORT).show();
//                    EditingToolsAdapter.this.mOnItemSelected.onToolSelected((EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition())).mToolType);
//                }


                //rewarded ad
                if (!SharePreferenceUtil.isPurchased(mContext)) {
                    if (EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition()).mToolName.equals(PhotoApp.resources.getString(R.string.filters)) || EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition()).mToolName.equals(PhotoApp.resources.getString(R.string.sticker)) || EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition()).mToolName.equals(PhotoApp.resources.getString(R.string.splas)) || EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition()).mToolName.equals(PhotoApp.resources.getString(R.string.beauty_tool))) {

                        if (Constants.IS_SHOW_REWARDED_AD_FILTER && EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition()).mToolName.equals(PhotoApp.resources.getString(R.string.filters))) {
                            showRewardedAd(ViewHolder.this.getLayoutPosition(), adIcon);
                        } else if (Constants.IS_SHOW_REWARDED_AD_SPLASH && EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition()).mToolName.equals(PhotoApp.resources.getString(R.string.splas))) {
                            showRewardedAd(ViewHolder.this.getLayoutPosition(), adIcon);
                        } else if (Constants.IS_SHOW_REWARDED_AD_STICKER && EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition()).mToolName.equals(PhotoApp.resources.getString(R.string.sticker))) {
                            showRewardedAd(ViewHolder.this.getLayoutPosition(), adIcon);
                        } else if (Constants.IS_SHOW_REWARDED_AD_BEAUTY && EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition()).mToolName.equals(PhotoApp.resources.getString(R.string.beauty_tool))) {
                            showRewardedAd(ViewHolder.this.getLayoutPosition(), adIcon);
                        } else {
                            EditingToolsAdapter.this.mOnItemSelected.onToolSelected((EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition())).mToolType);
                        }


                    } else {
//                        Log.e("ToolType",">"+(EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition())).mToolType);
                        MaxAdManager.INSTANCE.checkTap(mContext,()->{
                            EditingToolsAdapter.this.mOnItemSelected.onToolSelected((EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition())).mToolType);
                            return null;
                        });
                    }
                }else{
                    EditingToolsAdapter.this.mOnItemSelected.onToolSelected((EditingToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition())).mToolType);
                }

            });

        }
    }

    public void showRewardedAd(int position,ImageView adIcon){

        //MaxAdRewardedAd>>done
        if (!SharePreferenceUtil.isPurchased(mContext)) {
            MaxAdManager.INSTANCE.showRewardedAd(mContext, () -> {
                        return null;
                    },
                    () -> {
                        Toast.makeText(mContext, "please try again later", Toast.LENGTH_SHORT).show();
                        return null;
                    },
                    () -> {
                        EditingToolsAdapter.this.mOnItemSelected.onToolSelected((EditingToolsAdapter.this.mToolList.get(position).mToolType));
                        checkAdShowValues(position, adIcon);
                        return null;
                    });
        }else{
            checkAdShowValues(position, adIcon);
            EditingToolsAdapter.this.mOnItemSelected.onToolSelected((EditingToolsAdapter.this.mToolList.get(position).mToolType));
        }
//        Boolean isShow = true;
//        if (isShow) {
//
//        } else {
//            Toast.makeText(mContext, "please watch Full ad", Toast.LENGTH_SHORT).show();
//        }
    }

    public void checkAdShowValues(int position,ImageView adIcon){
        String timeStamp = "";
        if (EditingToolsAdapter.this.mToolList.get(position).mToolName.equals(PhotoApp.resources.getString(R.string.filters))) {
            long currentTimestamp = System.currentTimeMillis();
            sharedPreferences.edit().putLong(MyConstant.LAST_CLICK_TIMESTAMP_FILTER, currentTimestamp).apply();
            timeStamp = MyConstant.LAST_CLICK_TIMESTAMP_FILTER;
        } else if (EditingToolsAdapter.this.mToolList.get(position).mToolName.equals(PhotoApp.resources.getString(R.string.sticker))) {
            long currentTimestamp = System.currentTimeMillis();
            sharedPreferences.edit().putLong(MyConstant.LAST_CLICK_TIMESTAMP_STICKER, currentTimestamp).apply();
            timeStamp = MyConstant.LAST_CLICK_TIMESTAMP_STICKER;
        } else if (EditingToolsAdapter.this.mToolList.get(position).mToolName.equals(PhotoApp.resources.getString(R.string.splas))) {
            long currentTimestamp = System.currentTimeMillis();
            sharedPreferences.edit().putLong(MyConstant.LAST_CLICK_TIMESTAMP_SPLASH, currentTimestamp).apply();
            timeStamp = MyConstant.LAST_CLICK_TIMESTAMP_SPLASH;
        } else if (EditingToolsAdapter.this.mToolList.get(position).mToolName.equals(PhotoApp.resources.getString(R.string.beauty_tool))) {
            long currentTimestamp = System.currentTimeMillis();
            sharedPreferences.edit().putLong(MyConstant.LAST_CLICK_TIMESTAMP_BEAUTY, currentTimestamp).apply();
            timeStamp = MyConstant.LAST_CLICK_TIMESTAMP_BEAUTY;
        }
        CheckRewardedAdTime.INSTANCE.checkAdTime(mContext, timeStamp, adIcon);
    }
}
