package com.lockerroom.face.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.lockerroom.face.activities.SaveAndShareActivity;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.SharePreferenceUtil;
import com.lockerroom.face.BuildConfig;
import com.lockerroom.face.R;

public class RateDialog extends Dialog implements View.OnClickListener {
    private final FragmentActivity context;
    private final boolean exit;
    private final ImageView[] imageViewStars = new ImageView[5];
    private LottieAnimationView lottieAnimationView;
    private ViewGroup ratingBar;
    private int star_number;
    private TextView tvSubmit;

    public RateDialog(@NonNull FragmentActivity activity, boolean z) {
        super(activity);
        this.context = activity;
        this.exit = z;
        setContentView(R.layout.dialog_smart_rate);
        initView();
        this.ratingBar.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake));
        this.star_number = 0;
    }

    private void initView() {
        this.tvSubmit = findViewById(R.id.tv_submit);
        TextView tvLater = findViewById(R.id.tv_later);
        this.lottieAnimationView = findViewById(R.id.lottie);
        this.lottieAnimationView.setProgress(0.0f);
        this.ratingBar = findViewById(R.id.scaleRatingBar);
        this.tvSubmit.setOnClickListener(this);
        tvLater.setOnClickListener(this);
        ImageView iv_star_1 = findViewById(R.id.star_1);
        ImageView iv_star_2 = findViewById(R.id.star_2);
        ImageView iv_star_3 = findViewById(R.id.star_3);
        ImageView iv_star_4 = findViewById(R.id.star_4);
        ImageView iv_star_5 = findViewById(R.id.star_5);
        iv_star_1.setOnClickListener(this);
        iv_star_2.setOnClickListener(this);
        iv_star_3.setOnClickListener(this);
        iv_star_4.setOnClickListener(this);
        iv_star_5.setOnClickListener(this);
        this.imageViewStars[0] = iv_star_1;
        this.imageViewStars[1] = iv_star_2;
        this.imageViewStars[2] = iv_star_3;
        this.imageViewStars[3] = iv_star_4;
        this.imageViewStars[4] = iv_star_5;
        IronSourceAdsManager.INSTANCE.loadInter(context, new IronSourceCallbacks() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail() {

            }
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id != R.id.tv_later) {
            if (id != R.id.tv_submit) {
                switch (id) {
                    case R.id.star_1:
                        this.star_number = 1;
                        setStarBar();
                        return;
                    case R.id.star_2:
                        this.star_number = 2;
                        setStarBar();
                        return;
                    case R.id.star_3:
                        this.star_number = 3;
                        setStarBar();
                        return;
                    case R.id.star_4:
                        this.star_number = 4;
                        setStarBar();
                        return;
                    case R.id.star_5:
                        this.star_number = 5;
                        setStarBar();
                        return;
                    default:

                }
            } else if (this.star_number >= 4) {
                this.context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
                SharePreferenceUtil.setRated(this.context, true);
                dismiss();
            } else if (this.star_number > 0) {
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType("message/rfc822");
                intent.putExtra("android.intent.extra.EMAIL", this.context.getResources().getString(R.string.feedback));
                intent.putExtra("android.intent.extra.SUBJECT", this.context.getString(R.string.app_name));
                this.context.startActivity(Intent.createChooser(intent, "Send Email"));
                if (this.exit) {
                    this.context.finish();
                } else {
                    dismiss();
                }
            } else {
                this.ratingBar.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.shake));
            }
        } else if (this.exit) {

            this.context.finish();
        } else {
            dismiss();
        }
    }

    private void setStarBar() {
        for (int i = 0; i < this.imageViewStars.length; i++) {
            if (i < this.star_number) {
                this.imageViewStars[i].setImageResource(R.drawable.ic_star);
            } else {
                this.imageViewStars[i].setImageResource(R.drawable.ic_star_blur);
            }
        }
        if (this.star_number < 4) {
            this.tvSubmit.setText(R.string.rating_dialog_feedback_title);
        } else {
            this.tvSubmit.setText(R.string.rating_dialog_submit);
        }
//        this.lottieAnimationView.setProgress(((float) this.star_number) / 5.0f);
    }
}
