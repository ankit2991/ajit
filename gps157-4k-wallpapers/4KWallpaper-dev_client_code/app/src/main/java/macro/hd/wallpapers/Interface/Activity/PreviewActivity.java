package macro.hd.wallpapers.Interface.Activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.MyCustomView.GlideImageView;
import macro.hd.wallpapers.MyCustomView.MyImageView;
import macro.hd.wallpapers.Utilily.BlurImage;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Interface.Fragments.WallpaperDetailFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PreviewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.android_image_zoom_effect);
        CommonFunctions.updateWindow(PreviewActivity.this);

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        String path = getIntent().getStringExtra("path");

        setBlurrImage(WallpaperDetailFragment.resource);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String strTime = simpleDateFormatTime.format(calendar.getTime());

        findViewById(R.id.img_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        final RelativeLayout rl_photos = findViewById(R.id.rl_photos);

        // Now we display formattedDate value in TextView
        TextView txt_time = findViewById(R.id.txt_time);
        txt_time.setText(strTime);


        SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("E, MMM dd", Locale.getDefault());
        String strDate = simpleDateFormatDate.format(calendar.getTime());

        // Now we display formattedDate value in TextView
        TextView txt_date = findViewById(R.id.txt_date);
        txt_date.setText(strDate);

        final LottieAnimationView animationView = (LottieAnimationView) findViewById(R.id.animation_view);
        animationView.setAnimation("loading.json");
        animationView.setScale(0.20f);
        animationView.loop(true);
        animationView.playAnimation();

//        final ImageView fullScreenImageView = (ImageView) findViewById(R.id.fullScreenImageView);

        if (path != null) {

            Glide.with(this)
                    .asBitmap()
                    .load(path).apply(new RequestOptions())
                    .into(new SimpleTarget< Bitmap >() {
                        @Override
                        public void onResourceReady(final Bitmap resource, Transition< ? super Bitmap > transition) {
                            try {
                                findViewById(R.id.ll_progress).setVisibility(View.GONE);
                                findViewById(R.id.img_blurre).setVisibility(View.GONE);
                                findViewById(R.id.view1).setVisibility(View.GONE);
                                Drawable d = new BitmapDrawable(getResources(), resource);
//								holder.thumbNail.setImageDrawable(d);
//								LinearLayout item = (LinearLayout) rootView.findViewById(R.id.llImage);
                                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View child = inflater.inflate(R.layout.item_photo_detail, rl_photos, false);
                                MyImageView thumbNail = (MyImageView) child
                                        .findViewById(R.id.img_banner);
                                thumbNail.setImageDrawable(d);
//								thumbNail.setOnClickListener(this);
                                thumbNail.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
                                    @Override
                                    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                                        if (findViewById(R.id.llPreviewApp).getVisibility() == View.VISIBLE) {
                                            findViewById(R.id.llPreviewApp).setVisibility(View.GONE);
                                            findViewById(R.id.ll_time).setVisibility(View.GONE);
                                            findViewById(R.id.img_close).setVisibility(View.GONE);
                                        } else {
                                            findViewById(R.id.llPreviewApp).setVisibility(View.VISIBLE);
                                            findViewById(R.id.ll_time).setVisibility(View.VISIBLE);
                                            findViewById(R.id.img_close).setVisibility(View.VISIBLE);
                                        }
                                        return false;
                                    }

                                    @Override
                                    public boolean onDoubleTap(MotionEvent motionEvent) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
                                        return false;
                                    }
                                });
                                rl_photos.addView(child);
                                findViewById(R.id.llPreviewApp).setVisibility(View.VISIBLE);
                                findViewById(R.id.ll_time).setVisibility(View.VISIBLE);
                                findViewById(R.id.img_close).setVisibility(View.VISIBLE);

                            } catch (Exception e) {
                                e.printStackTrace();
                            } catch (Error e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    public void setBlurrImage(final Bitmap bitmap) {
        if (bitmap == null)
            return;
        try {
            final GlideImageView imageOriginal = findViewById(R.id.img_blurre);
            if (bitmap != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    BlurImage.with(getApplicationContext()).load(bitmap).intensity(25).Async(true).into(imageOriginal);
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                if (!isFinishing()) {
                                    final Bitmap temp = CommonFunctions.fastblur1(bitmap, 25, PreviewActivity.this);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
//                                                Drawable d = new BitmapDrawable(getResources(), temp);
                                                imageOriginal.setImageBitmap(temp);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
    }

}