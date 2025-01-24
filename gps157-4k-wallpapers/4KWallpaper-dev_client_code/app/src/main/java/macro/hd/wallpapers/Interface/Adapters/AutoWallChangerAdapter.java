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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.ImagesCallback;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Model.Category;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class AutoWallChangerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Category> list = new ArrayList<>();
    private Context context;

    public void setImagesCallback(ImagesCallback imagesCallback) {
        this.imagesCallback = imagesCallback;
    }

    private ImagesCallback imagesCallback;
    private int height;
    public AutoWallChangerAdapter(Context context, ArrayList<Category> list) {
        this.list = list;
        this.context=context;

        try {
            int screenWidth = CommonFunctions.getScreenWidth((Activity) context);// - 2 * ((int) activity.getResources().getDimension(R.dimen.padding_carousal_item));
            height = (int) (screenWidth / 2.2f);
            int itemHeight_diment= (int) context.getResources().getDimension(R.dimen.auto_Wall_height);
            Logger.e("AutoWallChangerAdapter","height:"+height + " itemHeight_diment:"+itemHeight_diment );
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public ImageView ivItem;
        CheckBox checkbox;
        RelativeLayout rl_selection,rl_add_one;
        protected TextView tvTitle;

        public MyViewHolder(View view) {
            super(view);
            this.view = view;
            this.ivItem = view.findViewById(R.id.ivItem);
            checkbox = view.findViewById(R.id.checkBox);
            this.tvTitle =  view.findViewById(R.id.tvTitle);
            checkbox.setClickable(false);
            rl_selection=view.findViewById(R.id.rl_selection);
            rl_add_one=view.findViewById(R.id.rl_add_one);

            if(height!=0) {
                view.getLayoutParams().height = height;
//                view.getLayoutParams().width = height;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            {
                CardView card_view=view.findViewById(R.id.card_view);
                card_view.setMaxCardElevation(0f);
                card_view.setPreventCornerOverlap(false);

            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_explor, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(itemView);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final MyViewHolder exploreViewHolder=((MyViewHolder) holder);

        exploreViewHolder.view.setTag(position);

//        Bitmap bitTempResize = BitmapFactory
//                .decodeFile(list.get(position));
//        BitmapDrawable ob = new BitmapDrawable(bitTempResize);
//        exploreViewHolder.ivItem.setBackgroundDrawable(ob);

        final Category images=list.get(position);

//        File file = new File(images.getPath());
//        Uri imageUri = Uri.fromFile(file);
//
//        Glide.with(context)
//                .load(imageUri)
//                .into(exploreViewHolder.ivItem);

        exploreViewHolder.tvTitle.setText((images.getDisplay_name()));

        if(!TextUtils.isEmpty(images.getImage()))
            Glide.with(context)
                    .load(CommonFunctions.getDomainImages()+"category/"+images.getImage()).transition(DrawableTransitionOptions.withCrossFade())
                    .apply(new RequestOptions().placeholder(CommonFunctions.getLoadingColor(context)).error(R.mipmap.ic_error))
                    .into(exploreViewHolder.ivItem);
        else
            exploreViewHolder.ivItem.setImageResource(CommonFunctions.getLoadingColor(context));

        if(images.isSelected())
            exploreViewHolder.checkbox.setChecked(true);
        else
            exploreViewHolder.checkbox.setChecked(false);

//        if(position==0){
//            exploreViewHolder.rl_selection.setVisibility(View.GONE);
//            exploreViewHolder.rl_add_one.setVisibility(View.VISIBLE);
//        }else{
            exploreViewHolder.rl_selection.setVisibility(View.VISIBLE);
            exploreViewHolder.rl_add_one.setVisibility(View.GONE);
//        }

        exploreViewHolder.rl_selection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MyViewHolder) exploreViewHolder).checkbox.setChecked(
                        !((MyViewHolder) exploreViewHolder).checkbox.isChecked());
                if (((MyViewHolder) exploreViewHolder).checkbox.isChecked()) {
                    exploreViewHolder.checkbox.setChecked(true);
                    images.setSelected(true);
//                    onItemClick.onItemCheck(currentItem);
                } else {
                    exploreViewHolder.checkbox.setChecked(false);
                    images.setSelected(false);
//                    onItemClick.onItemUncheck(currentItem);
                }
            }
        });

        exploreViewHolder.rl_add_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imagesCallback!=null)
                    imagesCallback.click();
            }
        });
    }

    public  void onDestroy(){
        imagesCallback=null;
        context=null;
        if(list!=null)
            list.clear();
        list=null;
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
}