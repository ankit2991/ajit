package macro.hd.wallpapers.Interface.Fragments.IntroFagment;

import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.Logger;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;

public class FragmentIntroSaver extends SlideFragment {

    public FragmentIntroSaver() {
    }

    public static FragmentIntroSaver newInstance(int img_id) {
        FragmentIntroSaver fragmentFirst = new FragmentIntroSaver();
        Bundle args = new Bundle();
        args.putInt("img_id", img_id);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    int img_id;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle=getArguments();
        if(bundle!=null){
            img_id=bundle.getInt("img_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View rootView = inflater.inflate(R.layout.fragment_intro_saver, container,
                false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView img_1=view.findViewById(R.id.img);

        CardView card_view=view.findViewById(R.id.card_view);

        try {
            Point windowDimensions = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(windowDimensions);
            Logger.e("DoubleFragment","y:"+windowDimensions.y + " X:"+windowDimensions.x);
            int itemHeight = Math.round(windowDimensions.y * 0.60f);
            int itemWidht = Math.round(windowDimensions.x * 0.56f);

            card_view.getLayoutParams().height = itemHeight;
            card_view.getLayoutParams().width = itemWidht;

        } catch (Exception e) {
            e.printStackTrace();

            card_view.getLayoutParams().width = (int) getResources().getDimension(R.dimen.intro_witdh);
            card_view.getLayoutParams().height = (int) getResources().getDimension(R.dimen.intro_hieght);

        }

        TextView txt_sub=view.findViewById(R.id.txt_sub);
        TextView txt_title=view.findViewById(R.id.txt_title);
        if(img_id==0){
            txt_title.setText(getResources().getString(R.string.status1_title));
            txt_sub.setText(getResources().getString(R.string.status1_msg));
            Glide.with(getActivity())
                    .load(Uri.parse("file:///android_asset/status_help1.webp")).transition(DrawableTransitionOptions.withCrossFade())
                    .into(img_1);
        }else  if(img_id==1){
            txt_title.setText(getResources().getString(R.string.status2_title));
            txt_sub.setText(getResources().getString(R.string.status2_msg));
            Glide.with(getActivity())
                    .load(Uri.parse("file:///android_asset/status_help2.webp")).transition(DrawableTransitionOptions.withCrossFade())
                    .into(img_1);
        }else  if(img_id==2){
            txt_title.setText(getResources().getString(R.string.status3_title));
            txt_sub.setText(getResources().getString(R.string.status3_msg));
            Glide.with(getActivity())
                    .load(Uri.parse("file:///android_asset/status_help3.webp")).transition(DrawableTransitionOptions.withCrossFade())
                    .into(img_1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
