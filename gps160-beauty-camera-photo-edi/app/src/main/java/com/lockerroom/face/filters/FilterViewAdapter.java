package com.lockerroom.face.filters;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lockerroom.face.Constants;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.utils.SystemUtil;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.lockerroom.face.R;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FilterViewAdapter extends RecyclerView.Adapter<FilterViewAdapter.ViewHolder> {
    private List<Bitmap> bitmaps;
    private int borderWidth;
    private Context context;

    public List<FilterUtils.FilterBean> filterEffects;

    public FilterListener mFilterListener;

    public int selectedFilterIndex = 0;

    public FilterViewAdapter(List<@Nullable Bitmap> list, FilterListener filterListener, Context context2, List<FilterUtils.FilterBean> list2) {
        this.mFilterListener = filterListener;
        this.bitmaps = list;
        this.context = context2;
        this.filterEffects = list2;
        this.borderWidth = SystemUtil.dpToPx(context2, Constants.BORDER_WIDTH_DP);
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_filter_view, viewGroup, false));
    }

    public void reset() {
        this.selectedFilterIndex = 0;
        notifyDataSetChanged();
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        try {
            viewHolder.mTxtFilterName.setText(this.filterEffects.get(i).getName());
            viewHolder.mImageFilterView.setImageBitmap(this.bitmaps.get(i));
            viewHolder.mImageFilterView.setBorderColor(ContextCompat.getColor(context, R.color.blue_selected));
            if (this.selectedFilterIndex == i) {
                viewHolder.mImageFilterView.setBorderColor(ContextCompat.getColor(context, R.color.blue_selected));
                viewHolder.mImageFilterView.setBorderWidth(this.borderWidth);
                return;
            }
            viewHolder.mImageFilterView.setBorderColor(0);
            viewHolder.mImageFilterView.setBorderWidth(this.borderWidth);
        }catch (Exception e){e.printStackTrace();}


    }

    public int getItemCount() {
        return this.bitmaps.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView mImageFilterView;
        TextView mTxtFilterName;
        ConstraintLayout wrapFilterItem;

        ViewHolder(View view) {
            super(view);
            this.mImageFilterView = view.findViewById(R.id.imgFilterView);
            this.mTxtFilterName = view.findViewById(R.id.txtFilterName);
            this.wrapFilterItem = view.findViewById(R.id.wrapFilterItem);
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Activity activity = getActivityFromView(view);

                    if (activity != null) {
                        MaxAdManager.INSTANCE.checkTap(activity, () -> {
                            try{
                                FilterViewAdapter.this.selectedFilterIndex = ViewHolder.this.getLayoutPosition();
                                FilterViewAdapter.this.mFilterListener.onFilterSelected(FilterViewAdapter.this.filterEffects.get(FilterViewAdapter.this.selectedFilterIndex).getConfig());
                                FilterViewAdapter.this.notifyDataSetChanged();
                            }catch (Exception e){e.printStackTrace();}

                            return null;
                        });
                    }else{
                        try{
                            FilterViewAdapter.this.selectedFilterIndex = ViewHolder.this.getLayoutPosition();
                            FilterViewAdapter.this.mFilterListener.onFilterSelected(FilterViewAdapter.this.filterEffects.get(FilterViewAdapter.this.selectedFilterIndex).getConfig());
                            FilterViewAdapter.this.notifyDataSetChanged();
                        }catch (Exception e){e.printStackTrace();}
                    }



            }
            });
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
