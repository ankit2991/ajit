package macro.hd.wallpapers.Interface.Fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import macro.hd.wallpapers.Interface.Activity.DoubleWallpaperActivity;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Model.DoubleWallpaper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DoubleFragment extends Fragment {
    // Store instance variables
    private int page;

    public static int selected_pos=-1;

    DoubleWallpaper post;
    View rootView;
    ImageView img_1,img_2;
    View layout_loading;
    RelativeLayout ll_img1,ll_img2;
    RelativeLayout rl_photos;
    TextView txt_time,txt_date,txt_date2;

    String strTime,strDate;
    boolean startedDefault;

    // newInstance constructor for creating fragment with arguments
    public static DoubleFragment newInstance(int page, DoubleWallpaper postDouble) {
        DoubleFragment fragmentFirst = new DoubleFragment();
        Bundle args = new Bundle();
        args.putInt("pos", page);
        args.putSerializable("post", (Serializable) postDouble);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("pos", 0);
        post = (DoubleWallpaper) getArguments().get("post");

        Logger.e("DoubleFragment","onCreate:"+page );

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => "+c.getTime());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        strTime = simpleDateFormatTime.format(calendar.getTime());


        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("E, MMM dd", Locale.getDefault());
        strDate = simpleDateFormatDate.format(calendar.getTime());

        if(page==0){
            startedDefault=true;
            selected_pos=0;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.e("DoubleFragment","onPause:"+page);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Logger.e("DoubleFragment","onHiddenChanged "+hidden);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.e("DoubleFragment","onResume:"+page);
    }

    public void startAnimation(){
        if(animatorSet!=null) {
            animatorSet.start();
            Logger.e("DoubleFragment","startAnimation:"+page);
        }
    }

    public void stopAnimation(){
        if(animatorSet!=null ) {
            animatorSet.end();
            Logger.e("DoubleFragment","stopAnimation:"+page);
        }
    }
    int count = 0;
    private AnimatorSet animatorSet;
    int itemHeight;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.e("DoubleFragment","onViewCreated:"+page + " animatorSet:"+animatorSet);
        rootView=view;
        count = 0;
        this.layout_loading = view.findViewById(R.id.layout_loading);
        img_1=view.findViewById(R.id.img_1);
        img_2=view.findViewById(R.id.img_2);
        ll_img1=view.findViewById(R.id.ll_img1);
        ll_img2=view.findViewById(R.id.ll_img2);
        rl_photos=view.findViewById(R.id.rl_photos);

        try {
            Point windowDimensions = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(windowDimensions);
            Logger.e("DoubleFragment","y:"+windowDimensions.y + " X:"+windowDimensions.x);
            itemHeight = Math.round(windowDimensions.y * 0.59f);
            int itemWidht = Math.round(windowDimensions.x * 0.56f);
            int itemHeight_diment= (int) getActivity().getResources().getDimension(R.dimen.double_wall_hight);
            int itemWidht_diment= (int)  getActivity().getResources().getDimension(R.dimen.double_wall_width);
            Logger.e("DoubleFragment","y:"+windowDimensions.y + " X:"+windowDimensions.x+ " itemHeight:"+itemHeight+" itemWidht:"+itemWidht+" itemHeight_diment:"+itemHeight_diment + " itemWidht_diment:"+itemWidht_diment);

            rl_photos.getLayoutParams().height = itemHeight;
            rl_photos.getLayoutParams().width = itemWidht;
        } catch (Exception e) {
            e.printStackTrace();
        }

        txt_time=view.findViewById(R.id.txt_time);
        txt_date=view.findViewById(R.id.txt_date);
        txt_date2=view.findViewById(R.id.txt_date2);

        layout_loading.setVisibility(View.VISIBLE);

        String path = CommonFunctions.getDomainImages() + "double_thumb/" + post.getImg1();
        String path1 = CommonFunctions.getDomainImages() + "double_thumb/" + post.getImg2();


        if(post.getImg1().startsWith("http")) {
            path=post.getImg1();
        }

        if(post.getImg2().startsWith("http")) {
            path1=post.getImg2();
        }

        txt_date.setText(strDate);
        txt_date2.setText(strDate);
        txt_time.setText(strTime);


        Glide.with(getActivity())
                .load(path)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        count = count + 1;
                        startAnimationIfImgsLoaded();
                        return false;
                    }
                })
                .into(img_1);

        Glide.with(getActivity())
                .load(path1)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        count = count + 1;
                        startAnimationIfImgsLoaded();
                        return false;
                    }
                })
                .into(img_2);


        layout_loading.setVisibility(View.GONE);
//        if(animatorSet==null){

            Animator translationAnimator = ObjectAnimator
                    .ofFloat(ll_img1, View.TRANSLATION_Y, 0f, -itemHeight).setDuration(1000);

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

                    if (!mCanceled) {

                        if(ll_img1==null) {
                            return;
                        }

                        if(selected_pos!=page) {
                            ll_img1.setTranslationY(0);
                            final Animator alphaAnimatorFad = ObjectAnimator
                                    .ofFloat(ll_img1, View.ALPHA, 0f, 1f).setDuration(400);
                            alphaAnimatorFad.start();
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
                                if(!mCanceled) {
                                    if(animator!=null) {
                                        animator.setStartDelay(1000);
                                        animator.start();
                                    }
                                }
//									new Handler().postDelayed(new Runnable() {
//										@Override
//										public void run() {
//
//										}
//									},1000);


                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        });

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

        if(count == 2 && startedDefault && page==selected_pos)
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

                                      if(((DoubleWallpaperActivity)getActivity())!=null)
                                          ((DoubleWallpaperActivity)getActivity()).updateBG(page);

                                  } catch (Exception e) {
                                      e.printStackTrace();
                                  }catch (Error e) {
                                      e.printStackTrace();
                                  }
                                  return false;
                              }
                          }
                ).submit();


//        rl_photos.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // TODO: 12/30/2019
//                if(getActivity()!=null){
//                    ((DoubleWallpaperPagerActivity)getActivity()).setWallpaper(page);
//                }
//            }
//        });

        rootView.findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity()!=null){
                    ((DoubleWallpaperActivity)getActivity()).setWallpaper(page);
                }
            }
        });
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        view = inflater.inflate(R.layout.list_item_double_wall_pager, container, false);
        Logger.e("DoubleFragment","onCreateView:"+page);
        return view;
    }

    private void startAnimationIfImgsLoaded(){
        if(count == 2 && startedDefault && page==selected_pos)
            animatorSet.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    }
}
