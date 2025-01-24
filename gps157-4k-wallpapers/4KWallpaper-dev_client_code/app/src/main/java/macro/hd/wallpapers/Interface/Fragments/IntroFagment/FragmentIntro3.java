package macro.hd.wallpapers.Interface.Fragments.IntroFagment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.Logger;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FragmentIntro3 extends SlideFragment {

    public FragmentIntro3() {
    }

    public static FragmentIntro3 newInstance() {
        return new FragmentIntro3();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       rootView = inflater.inflate(R.layout.fragment_intro3, container,
                false);
//        this.rootView=rootView;
        return rootView;
    }

    View rootView;
    ImageView img_1,img_2;
    View layout_loading;
    RelativeLayout ll_img1,ll_img2;
    RelativeLayout rl_photos;
    TextView txt_time,txt_date,txt_date2;

    String strTime,strDate;
    boolean startedDefault;


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Logger.e("DoubleFragment","onCreate");
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => "+c.getTime());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        strTime = simpleDateFormatTime.format(calendar.getTime());


        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("E, MMM dd", Locale.getDefault());
        strDate = simpleDateFormatDate.format(calendar.getTime());


        startedDefault=false;
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.e("DoubleFragment","onPause ");
        stopAnimation();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Logger.e("DoubleFragment","onHiddenChanged "+hidden);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.e("DoubleFragment","onResume ");
        startAnimation();
    }

    public void startAnimation(){

        if(animatorSet!=null) {
//            if(!animatorSet.isRunning())
                animatorSet.start();
        }
    }

    public void stopAnimation(){
        if(animatorSet!=null ) {
            animatorSet.end();
        }
    }

    private AnimatorSet animatorSet;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.e("DoubleFragment","onViewCreated:"+ " animatorSet:"+animatorSet);

        RelativeLayout card_view=view.findViewById(R.id.rl_photos);

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

        this.layout_loading = rootView.findViewById(R.id.layout_loading);
        img_1=rootView.findViewById(R.id.img_1);
        img_2=rootView.findViewById(R.id.img_2);
        ll_img1=rootView.findViewById(R.id.ll_img1);
        ll_img2=rootView.findViewById(R.id.ll_img2);
        rl_photos=rootView.findViewById(R.id.rl_photos);

        txt_time=rootView.findViewById(R.id.txt_time);
        txt_date=rootView.findViewById(R.id.txt_date);
        txt_date2=rootView.findViewById(R.id.txt_date2);

        layout_loading.setVisibility(View.VISIBLE);

        String path = "file:///android_asset/double1_11.jpg";
        String path1 ="file:///android_asset/double1_22.jpg";


        txt_date.setText(strDate);
        txt_date2.setText(strDate);
        txt_time.setText(strTime);

        Glide.with(getActivity())
                .load(path)
                .into(img_1);

        Glide.with(getActivity())
                .load(path1)
                .into(img_2);

        layout_loading.setVisibility(View.GONE);
//        if(animatorSet==null){
        float hiehgt=getResources().getDimension(R.dimen.animation_hight);
//		Logger.e("animatorSet",""+animatorSet);
        ll_img1.clearAnimation();
        if(animatorSet!=null)
            animatorSet.cancel();

            Animator translationAnimator = ObjectAnimator
                    .ofFloat(ll_img1, View.TRANSLATION_Y, 0f, -hiehgt).setDuration(1000);

            final Animator alphaAnimator = ObjectAnimator
                    .ofFloat(ll_img1, View.ALPHA, 1f, 0f).setDuration(1000);


            animatorSet = new AnimatorSet();
            animatorSet.playTogether(
                    translationAnimator,
                    alphaAnimator
            );



        animatorSet.addListener(new Animator.AnimatorListener() {
            private boolean mCanceled;
            @Override
            public void onAnimationStart(Animator animator) {
                mCanceled = false;
            }

            @Override
            public void onAnimationEnd(final Animator animator) {
//                    Logger.e("DoubleFragment","onAnimationEnd:"+page+" mCanceled:"+mCanceled+ " selected_pos:"+selected_pos);

                try {
                    if (!mCanceled) {

                        if(ll_img1==null) {
                            return;
                        }

                        ll_img1.setTranslationY(0);
                        final Animator alphaAnimatorFad = ObjectAnimator
                                .ofFloat(ll_img1, View.ALPHA, 0f, 1f).setDuration(800);
                        alphaAnimatorFad.start();
                        alphaAnimatorFad.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator1) {
    //                                Logger.e("DoubleFragment","onAnimationEnd inner:"+page);
                                try {
                                    if(!mCanceled) {
                                        if(animator!=null) {
                                            animator.setStartDelay(1000);
                                            animator.start();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mCanceled = true;
//                    Logger.e("DoubleFragment","onAnimationCancel:"+page);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
//        }

        if(startedDefault)
            animatorSet.start();


        Glide.with(getActivity())
                .asBitmap().load(path).apply(new RequestOptions().placeholder(R.drawable.placeholder))
                .listener(new RequestListener<Bitmap>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                  layout_loading.setVisibility(View.GONE);
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(final Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                  try {

//                                      hashMap.put(i,bitmap);


                                  } catch (Exception e) {
                                      e.printStackTrace();
                                  }catch (Error e) {
                                      e.printStackTrace();
                                  }
                                  return false;
                              }
                          }
                ).submit();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.e("DoubleFragment","onDestroy ");
        img_1=null;
        img_2=null;
        ll_img1=null;
        ll_img2=null;
        rl_photos=null;

        txt_time=null;
        txt_date=null;
        txt_date2=null;

        layout_loading=null;
        animatorSet=null;


        rootView=null;

    }


}
