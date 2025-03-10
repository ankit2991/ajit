package com.lockerroom.face.features.adjust;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lockerroom.face.Constants;
import com.lockerroom.face.activities.MainActivity;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.photoeditor.PhotoEditor;
import com.lockerroom.face.R;
import com.lockerroom.face.utils.MyConstant;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class AdjustAdapter extends RecyclerView.Adapter<AdjustAdapter.ViewHolder> {
    public String FILTER_CONFIG_TEMPLATE = "@adjust brightness 0 @adjust contrast 1 @adjust saturation 1 @adjust sharpen 0";
    public AdjustListener adjustListener;
    private Context context;
    private FragmentActivity fragmentActivity;
    public List<AdjustModel> lstAdjusts;
    public int selectedFilterIndex = 0;

    public AdjustAdapter(Context context2, AdjustListener adjustListener2, FragmentActivity fragmentActivity) {
        this.context = context2;
        this.adjustListener = adjustListener2;
        this.fragmentActivity = fragmentActivity;
        init();
    }

    public void setSelectedAdjust(int i) {
        this.adjustListener.onAdjustSelected(this.lstAdjusts.get(i));
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_adjust_view, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.toolName.setText(this.lstAdjusts.get(i).name);
        viewHolder.icon.setImageDrawable(this.selectedFilterIndex != i ? this.lstAdjusts.get(i).icon : this.lstAdjusts.get(i).selectedIcon);
        if (this.selectedFilterIndex == i) {
            viewHolder.toolName.setTextColor(ContextCompat.getColor(context, R.color.black));
        } else {
            viewHolder.toolName.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
    }

    public int getItemCount() {
        return this.lstAdjusts.size();
    }

    public String getFilterConfig() {
        String str = this.FILTER_CONFIG_TEMPLATE;
        return MessageFormat.format(str, this.lstAdjusts.get(0).originValue + "", this.lstAdjusts.get(1).originValue + "", this.lstAdjusts.get(2).originValue + "", Float.valueOf(this.lstAdjusts.get(3).originValue));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView toolName;

        ViewHolder(View view) {
            super(view);
            this.icon = view.findViewById(R.id.icon);
            this.toolName = view.findViewById(R.id.tool_name);
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Activity activity = getActivityFromView(view);
                    if (activity != null) {
                        MaxAdManager.INSTANCE.checkTap(activity, () -> {
                            AdjustAdapter.this.selectedFilterIndex = ViewHolder.this.getLayoutPosition();
                            AdjustAdapter.this.adjustListener.onAdjustSelected(AdjustAdapter.this.lstAdjusts.get(AdjustAdapter.this.selectedFilterIndex));
                            AdjustAdapter.this.notifyDataSetChanged();
                            return null;
                        });
                    }else{
                        AdjustAdapter.this.selectedFilterIndex = ViewHolder.this.getLayoutPosition();
                        AdjustAdapter.this.adjustListener.onAdjustSelected(AdjustAdapter.this.lstAdjusts.get(AdjustAdapter.this.selectedFilterIndex));
                        AdjustAdapter.this.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public AdjustModel getCurrentAdjustModel() {
        return this.lstAdjusts.get(this.selectedFilterIndex);
    }

    private void init() {
        this.lstAdjusts = new ArrayList();
        this.lstAdjusts.add(new AdjustModel(0, this.context.getString(R.string.brightness), "brightness", this.context.getDrawable(R.drawable.brightness_selected), this.context.getDrawable(R.drawable.brightness), -1.0f, 0.0f, 1.0f));
        this.lstAdjusts.add(new AdjustModel(1, this.context.getString(R.string.contrast), "contrast", this.context.getDrawable(R.drawable.contrast_selected), this.context.getDrawable(R.drawable.contrast), 0.1f, 1.0f, 3.0f));
        this.lstAdjusts.add(new AdjustModel(2, this.context.getString(R.string.saturation), "saturation", this.context.getDrawable(R.drawable.saturation_selected), this.context.getDrawable(R.drawable.saturation), 0.0f, 1.0f, 3.0f));
        this.lstAdjusts.add(new AdjustModel(3, this.context.getString(R.string.sharpen), "sharpen", this.context.getDrawable(R.drawable.sharpen_selected), this.context.getDrawable(R.drawable.sharpen), -1.0f, 0.0f, 10.0f));
        if (Constants.SHOW_ADS) {
            IronSourceAdsManager.INSTANCE.loadInter(fragmentActivity, new IronSourceCallbacks() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFail() {

                }
            });

        }
    }

    public class AdjustModel {
        String code;
        Drawable icon;
        public int index;
        public float intensity;
        public float maxValue;
        public float minValue;
        public String name;
        public float originValue;
        Drawable selectedIcon;
        public float slierIntensity = 0.5f;

        AdjustModel(int i, String str, String str2, Drawable drawable, Drawable drawable2, float f, float f2, float f3) {
            this.index = i;
            this.name = str;
            this.code = str2;
            this.icon = drawable;
            this.minValue = f;
            this.originValue = f2;
            this.maxValue = f3;
            this.selectedIcon = drawable2;
        }

        public void setIntensity(PhotoEditor photoEditor, float f, boolean z) {
            if (photoEditor != null) {
                this.slierIntensity = f;
                this.intensity = calcIntensity(f);
                photoEditor.setFilterIntensityForIndex(this.intensity, this.index, z);
            }
        }


        public float calcIntensity(float f) {
            if (f <= 0.0f) {
                return this.minValue;
            }
            if (f >= 1.0f) {
                return this.maxValue;
            }
            if (f <= 0.5f) {
                return this.minValue + ((this.originValue - this.minValue) * f * 2.0f);
            }
            return this.maxValue + ((this.originValue - this.maxValue) * (1.0f - f) * 2.0f);
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
