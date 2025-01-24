package com.lockerroom.face.features.sticker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.utils.AssetUtils;
import com.lockerroom.face.R;

import java.util.List;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder> {

    public Activity context;

    public int screenWidth;

    public OnClickStickerListener stickerListener;

    public List<String> stickers;

    public interface OnClickStickerListener {
        void addSticker(Bitmap bitmap);
    }

    public StickerAdapter(Activity context2, List<String> list, int i, OnClickStickerListener onClickStickerListener) {
        this.context = context2;
        this.stickers = list;
        this.screenWidth = i;
        this.stickerListener = onClickStickerListener;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.context).inflate(R.layout.sticker_item, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Bitmap bitmap = AssetUtils.loadBitmapFromAssets(this.context, this.stickers.get(i));
        Glide.with(context).load(bitmap).into(viewHolder.sticker);
    }

    public int getItemCount() {
        return this.stickers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView sticker;

        public ViewHolder(View view) {
            super(view);
            this.sticker = view.findViewById(R.id.txt_vp_item_list);
            view.setOnClickListener(this);
        }

        public void onClick(View view) {
            if (context != null) {
                MaxAdManager.INSTANCE.checkTap(context, () -> {
            StickerAdapter.this.stickerListener.addSticker(AssetUtils.loadBitmapFromAssets(StickerAdapter.this.context, (String) StickerAdapter.this.stickers.get(getAdapterPosition())));
            return null;
                });
            }else{
                StickerAdapter.this.stickerListener.addSticker(AssetUtils.loadBitmapFromAssets(StickerAdapter.this.context, (String) StickerAdapter.this.stickers.get(getAdapterPosition())));
            }
        }
    }


    public static Activity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
