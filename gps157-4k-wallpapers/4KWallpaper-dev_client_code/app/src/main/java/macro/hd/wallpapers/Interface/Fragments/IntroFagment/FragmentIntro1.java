package macro.hd.wallpapers.Interface.Fragments.IntroFagment;

import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

public class FragmentIntro1 extends SlideFragment {

    public FragmentIntro1() {
    }

    boolean isAuto;
    public static FragmentIntro1 newInstance(boolean isAuto) {
        FragmentIntro1 fragmentFirst = new FragmentIntro1();
        Bundle args = new Bundle();
        args.putBoolean("isAuto", isAuto);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    Animation animationFadeIn;
    Animation animationFadeOut;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle=getArguments();
        if(bundle!=null){
            isAuto=bundle.getBoolean("isAuto");
        }

        if(isAuto) {
            animationFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
            animationFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fadeout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View rootView = inflater.inflate(R.layout.fragment_intro1, container,
                false);
        return rootView;
    }

    ImageView img_1;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        img_1=view.findViewById(R.id.img);

        CardView card_view=view.findViewById(R.id.card_view);


        try {
            Point windowDimensions = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(windowDimensions);
            Logger.e("DoubleFragment","y:"+windowDimensions.y + " X:"+windowDimensions.x);
            int itemHeight = Math.round(windowDimensions.y * 0.54f);
            int itemWidht = Math.round(windowDimensions.x * 0.56f);

            card_view.getLayoutParams().height = itemHeight;
            card_view.getLayoutParams().width = itemWidht;
        } catch (Exception e) {
            e.printStackTrace();
            card_view.getLayoutParams().width = (int) getResources().getDimension(R.dimen.intro_witdh);
            card_view.getLayoutParams().height = (int) getResources().getDimension(R.dimen.intro_hieght);
        }

        TextView txt_title=view.findViewById(R.id.txt_title);
        TextView txt_sub=view.findViewById(R.id.txt_sub);

        if(isAuto){
            txt_title.setText(getResources().getString(R.string.title_material_auto));
            txt_sub.setText(getResources().getString(R.string.description_material_auto));
        }else{
            txt_title.setText(getResources().getString(R.string.title_material_metaphor));
            txt_sub.setText(getResources().getString(R.string.description_material_metaphor));
        }

        Glide.with(getActivity())
                .load(Uri.parse("file:///android_asset/wallpaper.webp")).transition(DrawableTransitionOptions.withCrossFade())
                .into(img_1);

        if(isAuto) {
            i++;
            handler.postDelayed(runnable, DELAY); //for initial delay..
        }
    }


    Animation.AnimationListener animListener = new Animation.AnimationListener(){

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
//            img_1.setImageResource(R.drawable.hsc);
            try {
                if(isNew){
                    Glide.with(getActivity())
                            .load(Uri.parse(imageArray[i])).transition(DrawableTransitionOptions.withCrossFade())
                            .into(img_1);
                    img_1.startAnimation(animationFadeIn);

                    i++;
                    if(i>imageArray.length-1)
                    {
                        i=0;
                    }
                    handler.postDelayed(runnable, DELAY);  //for interval...

                    isNew=false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    boolean isNew;
    int i=0;
    private final int DELAY=1000;
    String []imageArray={"file:///android_asset/intro_wallpaper4.webp","file:///android_asset/intro_wallpaper5.webp","file:///android_asset/intro_wallpaper6.webp"};
    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {

        public void run() {
            try {
                if(img_1!=null && getActivity()!=null){
                    isNew=true;
                    animationFadeOut.setAnimationListener(animListener);
                    img_1.startAnimation(animationFadeOut);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        animationFadeIn=null;
        animationFadeOut=null;
        img_1=null;
    }

}
