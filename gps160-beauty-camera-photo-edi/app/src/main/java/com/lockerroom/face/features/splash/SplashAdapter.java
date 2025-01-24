package com.lockerroom.face.features.splash;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.siyamed.shapeimageview.RoundedImageView;
import com.lockerroom.face.Constants;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.sticker.SplashSticker;
import com.lockerroom.face.utils.AssetUtils;
import com.lockerroom.face.utils.SystemUtil;
import com.lockerroom.face.R;

import java.util.ArrayList;
import java.util.List;

public class SplashAdapter extends RecyclerView.Adapter<SplashAdapter.ViewHolder> {
    private int borderWidth;
    private Activity context;

    public int selectedSquareIndex;

    public SplashChangeListener splashChangeListener;

    public List<SplashItem> splashList = new ArrayList();

    interface SplashChangeListener {
        void onSelected(SplashSticker splashSticker);
    }

    SplashAdapter(Activity context2, SplashChangeListener splashChangeListener2, boolean z) {
        this.context = context2;
        this.splashChangeListener = splashChangeListener2;
        this.borderWidth = SystemUtil.dpToPx(context2, Constants.BORDER_WIDTH_DP);
        if (z) {
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask1.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame1.webp")), R.drawable.splash_1));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask2.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame2.webp")), R.drawable.splash_2));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask3.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame3.webp")), R.drawable.splash_3));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask4.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame4.webp")), R.drawable.splash_4));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask5.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame5.webp")), R.drawable.splash_5));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask6.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame6.webp")), R.drawable.splash_6));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask7.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame7.webp")), R.drawable.splash_7));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask8.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame8.webp")), R.drawable.splash_8));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask9.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame9.webp")), R.drawable.splash_9));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask11.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame11.webp")), R.drawable.splash_10));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask12.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame12.webp")), R.drawable.splash13));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask14.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame14.webp")), R.drawable.splash14));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask17.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame17.webp")), R.drawable.splash_12));
            this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "splash/icons/mask18.webp"), AssetUtils.loadBitmapFromAssets(context2, "splash/icons/frame18.webp")), R.drawable.splash_11));
            return;
        }
        this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_1_mask.webp"), AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_1_shadow.webp")), R.drawable.blur_1));
        this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_2_mask.webp"), AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_2_shadow.webp")), R.drawable.blur_3));
        this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_3_mask.webp"), AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_3_shadow.webp")), R.drawable.blur_2));
        this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_4_mask.webp"), AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_4_shadow.webp")), R.drawable.blur_4));
        this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_5_mask.webp"), AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_5_shadow.webp")), R.drawable.blur_5));
        this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_7_mask.webp"), AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_7_shadow.webp")), R.drawable.blur_8));
        this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_8_mask.webp"), AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_8_shadow.webp")), R.drawable.blur_7));
        this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_9_mask.webp"), AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_9_shadow.webp")), R.drawable.blur_9));
        this.splashList.add(new SplashItem(new SplashSticker(AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_10_mask.webp"), AssetUtils.loadBitmapFromAssets(context2, "blur/icons/blur_10_shadow.webp")), R.drawable.blur_10));
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.splash_view, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.splash.setImageResource(this.splashList.get(i).drawableId);
        if (this.selectedSquareIndex == i) {
            viewHolder.splash.setBorderColor(ContextCompat.getColor(context, R.color.colorAccent));
            viewHolder.splash.setBorderWidth(this.borderWidth);
            return;
        }
        viewHolder.splash.setBorderColor(0);
        viewHolder.splash.setBorderWidth(this.borderWidth);
    }

    public int getItemCount() {
        return this.splashList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public RoundedImageView splash;

        public ViewHolder(View view) {
            super(view);
            this.splash = view.findViewById(R.id.splash);
            view.setOnClickListener(this);
        }

        public void onClick(View view) {

            MaxAdManager.INSTANCE.checkTap(context,()->{
                SplashAdapter.this.selectedSquareIndex = getAdapterPosition();
                if (SplashAdapter.this.selectedSquareIndex < 0) {
                    SplashAdapter.this.selectedSquareIndex = 0;
                }
                if (SplashAdapter.this.selectedSquareIndex >= SplashAdapter.this.splashList.size()) {
                    SplashAdapter.this.selectedSquareIndex = SplashAdapter.this.splashList.size() - 1;
                }
                SplashAdapter.this.splashChangeListener.onSelected((SplashAdapter.this.splashList.get(SplashAdapter.this.selectedSquareIndex)).splashSticker);
                SplashAdapter.this.notifyDataSetChanged();
                return null;
            });

        }
    }

    class SplashItem {
        int drawableId;
        SplashSticker splashSticker;

        SplashItem(SplashSticker splashSticker2, int i) {
            this.splashSticker = splashSticker2;
            this.drawableId = i;
        }
    }
}
