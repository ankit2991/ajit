package com.lockerroom.face.features.splash;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.lockerroom.face.Constants;
import com.lockerroom.face.activities.EditImageActivity;
import com.lockerroom.face.activities.MainActivity;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.filters.FilterUtils;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.BannerAdListener;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.sticker.SplashSticker;
import com.lockerroom.face.utils.AssetUtils;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.SharePreferenceUtil;
import com.lockerroom.face.R;

public class SplashDialog extends DialogFragment implements SplashAdapter.SplashChangeListener {
    private static final String TAG = "SplashDialog";
    private ImageView backgroundView;

    public Bitmap bitmap;
    private Bitmap blackAndWhiteBitmap;
    private Bitmap blurBitmap;
    private ElegantNumberButton blurNumber;
    private SplashSticker blurSticker;
    private SeekBar brushIntensity;

    public TextView draw;

    public LinearLayout drawLayout;
    private FrameLayout frameLayout;

    public boolean isSplashView;
    private RelativeLayout loadingView;
    private ImageView redo;

    public RecyclerView rvSplashView;

    public TextView shape;

    public SplashDialogListener splashDialogListener;
    private SplashSticker splashSticker;

    public SplashView splashView;
    FrameLayout app_ad;
    TextView tvLoading;
    RelativeLayout mainConaitner;
    private ImageView undo;
    private ViewGroup viewGroup;

    public interface SplashDialogListener {
        void onSaveSplash(Bitmap bitmap);
    }

    public void setSplashView(boolean z) {
        this.isSplashView = z;
    }

    public void setBitmap(Bitmap bitmap2) {
        this.bitmap = bitmap2;
    }

    public static void show(@NonNull AppCompatActivity appCompatActivity, Bitmap bitmap2, Bitmap bitmap3, Bitmap bitmap4, SplashDialogListener splashDialogListener2, boolean z) {
        try {
            // MyActivity.kt
            Fragment thisFrag = appCompatActivity.getSupportFragmentManager()
                    .findFragmentByTag(TAG);
            if (thisFrag != null) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            SplashDialog splashDialog = new SplashDialog();
            splashDialog.setBlurBitmap(bitmap3);
            splashDialog.setBitmap(bitmap2);
            splashDialog.setBlackAndWhiteBitmap(bitmap4);
            splashDialog.setSplashDialogListener(splashDialogListener2);
            splashDialog.setSplashView(z);
            splashDialog.show(appCompatActivity.getSupportFragmentManager(), TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBlackAndWhiteBitmap(Bitmap bitmap2) {
        this.blackAndWhiteBitmap = bitmap2;
    }

    public void setBlurBitmap(Bitmap bitmap2) {
        this.blurBitmap = bitmap2;
    }

    public void setSplashDialogListener(SplashDialogListener splashDialogListener2) {
        this.splashDialogListener = splashDialogListener2;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        IronSourceAdsManager.INSTANCE.loadInter(requireActivity(), new IronSourceCallbacks() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail() {

            }
        });
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup2, @Nullable Bundle bundle) {
        getDialog().getWindow().requestFeature(1);
        getDialog().getWindow().setFlags(1024, 1024);
        View inflate = layoutInflater.inflate(R.layout.splash_layout, viewGroup2, false);

        this.viewGroup = viewGroup2;
        this.backgroundView = inflate.findViewById(R.id.backgroundView);
        this.splashView = inflate.findViewById(R.id.splashView);
        this.drawLayout = inflate.findViewById(R.id.drawLayout);
        this.drawLayout.setVisibility(View.GONE);
        this.frameLayout = inflate.findViewById(R.id.frameLayout);
        this.undo = inflate.findViewById(R.id.undo);
        app_ad = (FrameLayout) inflate.findViewById(R.id.bannerContainer);
        tvLoading = (TextView) inflate.findViewById(R.id.bannerTvLoading);
        mainConaitner = (RelativeLayout) inflate.findViewById(R.id.adsContainer);
        this.undo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                    SplashDialog.this.splashView.undo();
                    return null;
                });

            }
        });
        this.redo = inflate.findViewById(R.id.redo);
        this.redo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                    SplashDialog.this.splashView.redo();
                    return null;
                });
            }
        });
        this.brushIntensity = inflate.findViewById(R.id.brushIntensity);
        this.brushIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                SplashDialog.this.splashView.setBrushBitmapSize(i + 25);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                SplashDialog.this.splashView.updateBrush();
            }
        });
        this.loadingView = inflate.findViewById(R.id.loadingView);
        this.loadingView.setVisibility(View.GONE);
        this.blurNumber = inflate.findViewById(R.id.blurNumber);
        this.backgroundView.setImageBitmap(this.bitmap);
        this.shape = inflate.findViewById(R.id.shape);
        this.draw = inflate.findViewById(R.id.draw);
        if (this.isSplashView) {
            this.splashView.setImageBitmap(this.blackAndWhiteBitmap);
            this.blurNumber.setVisibility(View.GONE);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) this.shape.getLayoutParams();
            layoutParams.horizontalBias = 0.3f;
            this.shape.setLayoutParams(layoutParams);
            ConstraintLayout.LayoutParams layoutParams2 = (ConstraintLayout.LayoutParams) this.draw.getLayoutParams();
            layoutParams2.horizontalBias = 0.7f;
            this.draw.setLayoutParams(layoutParams2);
        } else {
            this.splashView.setImageBitmap(this.blurBitmap);
            this.blurNumber.setRange(0, 10);
        }
        this.blurNumber.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            public void onValueChange(ElegantNumberButton elegantNumberButton, int i, int i2) {
                new LoadBlurBitmap((float) i2).execute(new Void[0]);
            }
        });
        this.rvSplashView = inflate.findViewById(R.id.rvSplashView);
        this.rvSplashView.setLayoutManager(new LinearLayoutManager(getContext(), 0, false));
        this.rvSplashView.setHasFixedSize(true);
        this.rvSplashView.setAdapter(new SplashAdapter(getActivity(), this, this.isSplashView));
        if (this.isSplashView) {
            this.splashSticker = new SplashSticker(AssetUtils.loadBitmapFromAssets(getContext(), "splash/icons/mask1.webp"), AssetUtils.loadBitmapFromAssets(getContext(), "splash/icons/frame1.webp"));
            this.splashView.addSticker(this.splashSticker);
        } else {
            this.blurSticker = new SplashSticker(AssetUtils.loadBitmapFromAssets(getContext(), "blur/icons/blur_1_mask.webp"), AssetUtils.loadBitmapFromAssets(getContext(), "blur/icons/blur_1_shadow.webp"));
            this.splashView.addSticker(this.blurSticker);
        }
        this.splashView.refreshDrawableState();
        this.splashView.setLayerType(2, null);
        this.shape.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                    SplashDialog.this.draw.setBackgroundResource(0);
                    SplashDialog.this.draw.setTextColor(ContextCompat.getColor(getContext(), R.color.white_blur));
                    SplashDialog.this.shape.setBackgroundResource(R.drawable.border_bottom);
                    SplashDialog.this.shape.setTextColor(ContextCompat.getColor(getContext(), R.color.font_color));
                    SplashDialog.this.splashView.setCurrentSplashMode(0);
                    SplashDialog.this.drawLayout.setVisibility(View.GONE);
                    SplashDialog.this.rvSplashView.setVisibility(View.VISIBLE);
                    SplashDialog.this.splashView.refreshDrawableState();
                    SplashDialog.this.splashView.invalidate();
                    if (SplashDialog.this.isSplashView) {
                        if (SharePreferenceUtil.isFirstShapeSplash(SplashDialog.this.getContext())) {
                            SplashDialog.this.showShapeTutorial();
                        }
                    } else if (SharePreferenceUtil.isFirstShapeBlur(SplashDialog.this.getContext())) {
                        SplashDialog.this.showShapeTutorial();
                    }

                    return null;
                });


            }
        });
        this.draw.setOnClickListener(view -> {

            MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                SplashDialog.this.splashView.refreshDrawableState();
                SplashDialog.this.splashView.setLayerType(1, (Paint) null);
                SplashDialog.this.shape.setBackgroundResource(0);
                SplashDialog.this.shape.setTextColor(ContextCompat.getColor(getContext(), R.color.white_blur));
                SplashDialog.this.draw.setBackgroundResource(R.drawable.border_bottom);
                SplashDialog.this.draw.setTextColor(ContextCompat.getColor(getContext(), R.color.font_color));
                SplashDialog.this.splashView.setCurrentSplashMode(1);
                SplashDialog.this.drawLayout.setVisibility(View.VISIBLE);
                SplashDialog.this.rvSplashView.setVisibility(View.GONE);
                SplashDialog.this.splashView.invalidate();
                if (SplashDialog.this.isSplashView) {
                    if (SharePreferenceUtil.isFirstDrawSplash(SplashDialog.this.getContext())) {
                        SplashDialog.this.showDrawTutorial();
                    }
                } else if (SharePreferenceUtil.isFirstDrawBlur(SplashDialog.this.getContext())) {
                    SplashDialog.this.showDrawTutorial();
                }
                return null;
            });


        });

        inflate.findViewById(R.id.imgSave).setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                    try{

                        SplashDialog.this.splashDialogListener.onSaveSplash(SplashDialog.this.splashView.getBitmap(SplashDialog.this.bitmap));
                    }catch (Exception e){e.printStackTrace();}
//                if (IronSource.isInterstitialReady()) {

                    SplashDialog.this.dismiss();
//                } else {
//
//                    SplashDialog.this.dismiss();
//                }
                    return null;
                });

            }
        });
        inflate.findViewById(R.id.imgClose).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                    SplashDialog.this.dismiss();
                    return null;
                });
            }
        });
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (SplashDialog.this.isSplashView) {
                    if (SharePreferenceUtil.isFirstShapeSplash(SplashDialog.this.getContext())) {
                        SplashDialog.this.showShapeTutorial();
                    }
                } else if (SharePreferenceUtil.isFirstShapeBlur(SplashDialog.this.getContext())) {
                    SplashDialog.this.showShapeTutorial();
                }
            }
        }, 1000);
        return inflate;
    }


    public void showDrawTutorial() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.draw_splash, this.viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setView(inflate);
        final AlertDialog create = builder.create();
        inflate.findViewById(R.id.btnDone).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                    if (SplashDialog.this.isSplashView) {
                        SharePreferenceUtil.setFirstDrawSplash(SplashDialog.this.getContext(), false);
                    } else {
                        SharePreferenceUtil.setFirstDrawBlur(SplashDialog.this.getContext(), false);
                    }
                    create.dismiss();
                    return null;
                });

            }
        });
        create.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        create.show();
    }


    public void showShapeTutorial() {
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.pinch_to_zoom_splash, this.viewGroup, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setView(inflate);
        final AlertDialog create = builder.create();
        inflate.findViewById(R.id.btnDone).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                    if (SplashDialog.this.isSplashView) {
                        SharePreferenceUtil.setFirstShapeSplash(SplashDialog.this.getContext(), false);
                    } else {
                        SharePreferenceUtil.setFirstShapeBlur(SplashDialog.this.getContext(), false);
                    }
                    create.dismiss();
                    return null;
                });

            }
        });
        create.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        create.show();
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
    }

    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(-1, -1);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(-16777216));
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.splashView.getSticker().release();
        if (this.blurBitmap != null) {
            this.blurBitmap.recycle();
        }
        this.blurBitmap = null;
        if (this.blackAndWhiteBitmap != null) {
            this.blackAndWhiteBitmap.recycle();
        }
        this.blackAndWhiteBitmap = null;
        this.bitmap = null;
    }

    public void onSelected(SplashSticker splashSticker2) {
        this.splashView.addSticker(splashSticker2);
    }

    class LoadBlurBitmap extends AsyncTask<Void, Bitmap, Bitmap> {
        private float intensity;

        public LoadBlurBitmap(float f) {
            this.intensity = f;
        }


        public void onPreExecute() {
            SplashDialog.this.showLoading(true);
        }


        public Bitmap doInBackground(Void... voidArr) {
            return FilterUtils.getBlurImageFromBitmap(SplashDialog.this.bitmap, this.intensity);
        }


        public void onPostExecute(Bitmap bitmap) {
            SplashDialog.this.showLoading(false);
            SplashDialog.this.splashView.setImageBitmap(bitmap);
        }
    }

    public void showLoading(boolean z) {
        if (z) {
            getActivity().getWindow().setFlags(16, 16);
            this.loadingView.setVisibility(View.VISIBLE);
            return;
        }
        getActivity().getWindow().clearFlags(16);
        this.loadingView.setVisibility(View.GONE);
    }


    @Override
    public void onResume() {
        super.onResume();
        //MaxAdBanner>>done
        if (getActivity() != null && !SharePreferenceUtil.isPurchased(getActivity())) {
            mainConaitner.setVisibility(View.VISIBLE);
            MaxAdManager.INSTANCE.createBannerAd(getActivity(), mainConaitner,app_ad , tvLoading, false, new BannerAdListener() {
                @Override
                public void bannerAdLoaded(boolean isLoad) {
                }
            });
        }else{
            mainConaitner.setVisibility(View.GONE);
        }
    }
}
