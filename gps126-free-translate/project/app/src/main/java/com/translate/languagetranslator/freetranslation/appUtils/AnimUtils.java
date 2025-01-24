package com.translate.languagetranslator.freetranslation.appUtils;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import com.translate.languagetranslator.freetranslation.R;


public class AnimUtils {

    public static void slideUp(Context activity, View view, AnimationCalllback calllback) {
        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.bottom_down);

        view.startAnimation(animation);
    }


    public static void slideDown(Context activity, View view, AnimationCalllback calllback) {
        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.bottom_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (calllback != null) {
                    calllback.onAnimationEnds();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        view.startAnimation(animation);
    }


    public static void zoomIn(Context context, View view, AnimationCalllback animationCalllback) {

        try {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.bottom_down);
            if (view == null || animation == null)
                return;
            view.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (animationCalllback != null) {
                        animationCalllback.onAnimationEnds();
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void zoomInCheckBox(Context context, View view, AnimationCalllback animationCalllback) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.zoomin_chk_box);

        view.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (animationCalllback != null) {
                    animationCalllback.onAnimationEnds();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    public static void zoomOut(Context context, View view, AnimationCalllback animationCalllback) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.bottom_up);
        if (view == null || animation == null)
            return;

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (animationCalllback != null) {
                    animationCalllback.onAnimationEnds();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(animation);
    }


    public interface AnimationCalllback {
        void onAnimationEnds();
    }


    public static void zoomInOut(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.zoomin_out);
        animation.setRepeatCount(Animation.INFINITE);
        view.startAnimation(animation);

    }

    public static void animateLeft(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_right);

        animation.setRepeatCount(3);
        view.startAnimation(animation);

    }

    public static void animateRightLeft(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_right_left);

        animation.setRepeatCount(Animation.INFINITE);
        view.startAnimation(animation);

    }


    public void smoothProgressAnimate(ProgressBar progressBar, int progress) {

        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(progressBar, "progress", progress);

        objectAnimator.setDuration(100);

        objectAnimator.setInterpolator(new DecelerateInterpolator());


        objectAnimator.start();

    }


    public static void spalshAnimation(Context context, View view, AnimationCalllback animCallabck) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.splash);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animCallabck.onAnimationEnds();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(animation);
    }


    public static void animateParallexImage(Activity activity, View view) {
        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.parallex_bg);

        view.startAnimation(animation);
    }


    public static void rotate(Activity activity, View view, AnimationCalllback animationCalllback) {
        Animation animation = AnimationUtils.loadAnimation(activity, R.anim.rotation_anim);
        if (view == null || animation == null)
            return;

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (animation != null) {
                    animationCalllback.onAnimationEnds();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }
}
