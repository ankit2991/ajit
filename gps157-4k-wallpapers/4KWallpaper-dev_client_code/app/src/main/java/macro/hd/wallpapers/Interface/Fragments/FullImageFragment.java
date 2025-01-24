package macro.hd.wallpapers.Interface.Fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import macro.hd.wallpapers.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class FullImageFragment extends DialogFragment {
    private ImageView imageView;
    private RelativeLayout rl_progress;
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_full_image, viewGroup, false);
        this.imageView = (ImageView) inflate.findViewById(R.id.image_preview);
        this.rl_progress = (RelativeLayout) inflate.findViewById(R.id.rl_progress);
        this.imageView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                FullImageFragment.this.dismiss();
            }
        });
        inflate.findViewById(R.id.exit).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                FullImageFragment.this.dismiss();
            }
        });
        String lightUrl=getArguments().getString("url_light");

//        Glide.with(getActivity()).load(lightUrl).addListener(new RequestListener<Drawable>() {
//            @Override
//            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                return false;
//            }
//
//            @Override
//            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                return false;
//            }
//        }).into(this.imageView);

        loadImage(getArguments().getString("url"));
        return inflate;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    private void loadImage(String str) {
        Glide.with(getActivity()).load(str).addListener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                rl_progress.setVisibility(View.GONE);
                return false;
            }
        }).into(this.imageView);
    }
}
