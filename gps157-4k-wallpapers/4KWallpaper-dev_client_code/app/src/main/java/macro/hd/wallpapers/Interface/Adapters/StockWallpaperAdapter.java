package macro.hd.wallpapers.Interface.Adapters;

import android.app.Activity;
import android.content.Context;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CategoryCallback;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Model.Category;
import macro.hd.wallpapers.Utilily.Logger;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by urmil on 3/15/2017.
 */

public class StockWallpaperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context activity;
    private List<Category> picList = new ArrayList<>();
    private LayoutInflater layoutInflater;

    public void setCategoryCallback(CategoryCallback categoryCallback) {
        this.categoryCallback = categoryCallback;
    }

    private CategoryCallback categoryCallback;
    private int height;

    public StockWallpaperAdapter(Context activity, List<Category> picList) {
        this.activity = activity;
        layoutInflater = LayoutInflater.from(activity);
        this.picList = picList;

        try {
            int screenWidth = CommonFunctions.getScreenWidth((Activity) activity);// - 2 * ((int) activity.getResources().getDimension(R.dimen.padding_carousal_item));
            height = (int) (screenWidth / 3f);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.item_discover_img, parent, false);
        return new ImageHolder(view);

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {


        final Category object = picList.get(position);

        final ImageHolder offerViewHolder = ((ImageHolder) holder);

        RequestOptions options = new RequestOptions();
        options.centerCrop();
        options.placeholder(CommonFunctions.getLoadingColor(activity));

//        Glide.with(activity).
//                load(Common.getDomainImages() + "category/" + object.getImage())
//                .apply(options)
//                .into(offerViewHolder.postImage);

//        offerViewHolder.tvTitle.setText(object.getDisplay_name());
        Logger.e("path",CommonFunctions.getDomainImages() + "category/stock/" + object.getImage_new());

        if (!TextUtils.isEmpty(object.getImage_new())) {

            Glide.with(activity)
                    .load(CommonFunctions.getDomainImages() + "category/stock/" + object.getImage_new()).transition(DrawableTransitionOptions.withCrossFade()).thumbnail(0.1f)
                    .apply(new RequestOptions().placeholder(CommonFunctions.getLoadingColor(activity)).error(R.mipmap.ic_error))
                    .into(offerViewHolder.postImage);
        }

        offerViewHolder.progress.setVisibility(View.GONE);

        offerViewHolder.ll_selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonFunctions.isNetworkAvailable(activity)) {
                    CommonFunctions.showInternetDialog(activity);
                    return;
                }
                if (categoryCallback != null) {
                    categoryCallback.clickCategory(object,false,true,false);
//                            notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        if (picList == null)
            return 0;
        return picList.size();
    }

    public class ImageHolder extends RecyclerView.ViewHolder {

        private View view;
        ImageView postImage;
        ProgressBar progress;
        RelativeLayout ll_selector;
        private View view_gradiant;
        protected TextView tvTitle;

        public ImageHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            postImage = itemView.findViewById(R.id.postImage);
            progress = itemView.findViewById(R.id.progress);
            ll_selector = itemView.findViewById(R.id.ll_selector);
            view_gradiant = itemView.findViewById(R.id.view_gradiant);
            this.tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);

            if(height!=0) {
                view.getLayoutParams().height = height;
                view.getLayoutParams().width = height;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            {
                CardView card_view=itemView.findViewById(R.id.card_view);
                card_view.setMaxCardElevation(0f);
                card_view.setPreventCornerOverlap(false);
            }
        }
    }
}
