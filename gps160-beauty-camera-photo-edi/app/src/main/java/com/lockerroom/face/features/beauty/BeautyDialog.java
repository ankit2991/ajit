package com.lockerroom.face.features.beauty;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.lockerroom.face.Constants;
import com.lockerroom.face.activities.EditImageActivity;
import com.lockerroom.face.activities.MainActivity;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.filters.DegreeSeekBar;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.BannerAdListener;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.photoeditor.OnSaveBitmap;
import com.lockerroom.face.photoeditor.PhotoEditorView;
import com.lockerroom.face.sticker.BeautySticker;
import com.lockerroom.face.sticker.BitmapStickerIcon;
import com.lockerroom.face.sticker.Sticker;
import com.lockerroom.face.sticker.StickerView;
import com.lockerroom.face.sticker.event.ZoomIconEvent;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.SharePreferenceUtil;
import com.lockerroom.face.utils.SystemUtil;
import com.lockerroom.face.R;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.wysaid.nativePort.CGEDeformFilterWrapper;
import org.wysaid.nativePort.CGEImageHandler;
import org.wysaid.texUtils.TextureRenderer;
import org.wysaid.view.ImageGLSurfaceView;

public class BeautyDialog extends DialogFragment {
    private Bitmap bitmap;
    private ImageView boobs;
    private ImageView compare;
    public int currentType = 7;
    private ImageView face;
    public ImageGLSurfaceView glSurfaceView;
    public SeekBar intensitySmooth;
    private DegreeSeekBar intensityTwoDirection;
    public RelativeLayout loadingView;
    private List<Retouch> lstRetouch;
    public CGEDeformFilterWrapper mDeformWrapper;
    private float mTouchRadiusForWaist;
    private LinearLayout magicLayout;
    public OnBeautySave onBeautySave;
    View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.resetWaist:
                    MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                        BeautyDialog.this.glSurfaceView.flush(true, new Runnable() {
                            public void run() {
                                if (BeautyDialog.this.mDeformWrapper != null) {
                                    BeautyDialog.this.mDeformWrapper.restore();
                                    BeautyDialog.this.glSurfaceView.requestRender();
                                }
                            }
                        });
                        return null;
                    });

                    return;
                case R.id.wrapBoobs:
                    MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                        BeautyDialog.this.showAdjustBoobs();
                        return null;
                    });

                    return;
                case R.id.wrapFace:
                    MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                        BeautyDialog.this.showAdjustFace();
                        return null;
                    });

                    return;
                case R.id.wrapHip:
                    MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                        BeautyDialog.this.showAdjustHipOne();
                        return null;
                    });

                    return;
                case R.id.wrapWaist:
                    MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                        BeautyDialog.this.showWaist();
                        return null;
                    });

                    return;
                default:
                    return;
            }
        }
    };
    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (BeautyDialog.this.intensitySmooth.getProgress() == 0) {
                BeautyDialog.this.glSurfaceView.setFilterWithConfig("");
                return;
            }
            ImageGLSurfaceView imageGLSurfaceView = BeautyDialog.this.glSurfaceView;
            imageGLSurfaceView.setFilterWithConfig(MessageFormat.format("@beautify face 1 {0} 640", new Object[]{BeautyDialog.this.intensitySmooth.getProgress() + ""}));
        }
    };
    public PhotoEditorView photoEditorView;
    private RelativeLayout resetWaist;
    private ImageView seat;
    public float startX;
    public float startY;
    private TextView tvBoobs;
    private TextView tvFace;
    private TextView tvSeat;
    private TextView tvWaise;
    private ViewGroup viewGroup;
    private ImageView waise;
    private RelativeLayout wrapBoobs;
    private RelativeLayout wrapFace;
    private RelativeLayout wrapHip;
    private RelativeLayout wrapWaist;
    FrameLayout app_ad;
    TextView tvLoading;
    RelativeLayout mainConaitner;
    public interface OnBeautySave {
        void onBeautySave(Bitmap bitmap);
    }

    public void setBitmap(Bitmap bitmap2) {
        this.bitmap = bitmap2;
    }

    public void showWaist() {
        if (SharePreferenceUtil.isFirstAdjustWaise(getContext())) {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.waise_instruction, this.viewGroup, false);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setCancelable(false);
            builder.setView(inflate);
            AlertDialog create = builder.create();
            inflate.findViewById(R.id.btnDone).setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                        create.dismiss();
                        SharePreferenceUtil.setFirstAdjustWaist(getContext(), false);
                        return null;
                    });

                }
            });
            create.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            create.show();
        }
        saveCurrentState();
        selectFunction(1);
        this.magicLayout.setVisibility(View.GONE);
        this.photoEditorView.setHandlingSticker(null);
        this.photoEditorView.setDrawCirclePoint(true);
        this.resetWaist.setVisibility(View.VISIBLE);
        this.intensityTwoDirection.setVisibility(View.GONE);
        this.currentType = 3;
        this.intensityTwoDirection.setCurrentDegrees(0);
        this.mTouchRadiusForWaist = (float) SystemUtil.dpToPx(getContext(), 20);
        this.photoEditorView.setCircleRadius((int) this.mTouchRadiusForWaist);
        this.photoEditorView.getStickers().clear();
    }


    private void saveCurrentState() {
        new SaveCurrentState().execute();
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    public void showAdjustBoobs() {
        if (SharePreferenceUtil.isFirstAdjustBoob(getContext())) {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.boobs_instruction, this.viewGroup, false);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setCancelable(false);
            builder.setView(inflate);
            final AlertDialog create = builder.create();
            inflate.findViewById(R.id.btnDone).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                        create.dismiss();
                        SharePreferenceUtil.setFirstAdjustBoob(BeautyDialog.this.getContext(), false);
                        return null;
                    });

                }
            });
            create.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            create.show();
        }
        saveCurrentState();
        this.intensityTwoDirection.setVisibility(View.VISIBLE);
        this.intensityTwoDirection.setDegreeRange(-30, 30);
        this.resetWaist.setVisibility(View.GONE);
        this.photoEditorView.setDrawCirclePoint(false);
        selectFunction(0);
        this.magicLayout.setVisibility(View.GONE);
        this.currentType = 7;
        this.intensityTwoDirection.setCurrentDegrees(0);
        this.photoEditorView.getStickers().clear();
        this.photoEditorView.addSticker(new BeautySticker(getContext(), 0, ContextCompat.getDrawable(getContext(), R.drawable.circle)));
        this.photoEditorView.addSticker(new BeautySticker(getContext(), 1, ContextCompat.getDrawable(getContext(), R.drawable.circle)));
    }

    public void showAdjustFace() {
        if (SharePreferenceUtil.isFirstAdjusFace(getContext())) {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.chin_instruction, this.viewGroup, false);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setCancelable(false);
            builder.setView(inflate);
            AlertDialog create = builder.create();
            inflate.findViewById(R.id.btnDone).setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                        create.dismiss();
                        SharePreferenceUtil.setFirstAdjustFace(getContext(), false);
                        return null;
                    });

                }
            });
            create.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            create.show();
        }
        saveCurrentState();
        this.intensityTwoDirection.setVisibility(View.VISIBLE);
        this.intensityTwoDirection.setDegreeRange(-15, 15);
        this.resetWaist.setVisibility(View.GONE);
        this.photoEditorView.setDrawCirclePoint(false);
        selectFunction(3);
        this.currentType = 4;
        this.magicLayout.setVisibility(View.GONE);
        this.intensityTwoDirection.setCurrentDegrees(0);
        this.photoEditorView.getStickers().clear();
        this.photoEditorView.addSticker(new BeautySticker(getContext(), 4, ContextCompat.getDrawable(getContext(), R.drawable.chin)));
    }


    public void showAdjustHipOne() {
        if (SharePreferenceUtil.isFirstAdjusHip(getContext())) {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.hip_instruction, this.viewGroup, false);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setCancelable(false);
            builder.setView(inflate);
            AlertDialog create = builder.create();
            inflate.findViewById(R.id.btnDone).setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                        create.dismiss();
                        SharePreferenceUtil.setFirstAdjustHip(getContext(), false);
                        return null;
                    });

                }
            });
            create.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            create.show();
        }
        saveCurrentState();
        this.intensityTwoDirection.setVisibility(View.VISIBLE);
        this.intensityTwoDirection.setDegreeRange(-30, 30);
        this.resetWaist.setVisibility(View.GONE);
        this.photoEditorView.setDrawCirclePoint(false);
        selectFunction(2);
        this.intensityTwoDirection.setCurrentDegrees(0);
        this.currentType = 9;
        this.photoEditorView.getStickers().clear();
        this.photoEditorView.addSticker(new BeautySticker(getContext(), 2, ContextCompat.getDrawable(getContext(), R.drawable.hip_1)));
    }


    public void hideAllFunction() {
        this.intensityTwoDirection.setVisibility(View.GONE);
        this.resetWaist.setVisibility(View.GONE);
        this.magicLayout.setVisibility(View.GONE);
        this.loadingView.setVisibility(View.GONE);
    }

    public void setOnBeautySave(OnBeautySave onBeautySave2) {
        this.onBeautySave = onBeautySave2;
    }

    public static BeautyDialog show(@NonNull AppCompatActivity appCompatActivity, Bitmap bitmap2, OnBeautySave onBeautySave2) {
        BeautyDialog beautyDialog = new BeautyDialog();
        beautyDialog.setBitmap(bitmap2);
        beautyDialog.setOnBeautySave(onBeautySave2);
        beautyDialog.show(appCompatActivity.getSupportFragmentManager(), "BeautyDialog");
        return beautyDialog;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
    }

    @Nullable
    public View onCreateView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup2, @Nullable Bundle bundle) {
        getDialog().getWindow().requestFeature(1);
        getDialog().getWindow().setFlags(1024, 1024);
        View inflate = layoutInflater.inflate(R.layout.beauty_layout, viewGroup2, false);

        this.intensityTwoDirection = inflate.findViewById(R.id.intensityTwoDirection);

        this.intensityTwoDirection.setDegreeRange(-20, 20);
        this.photoEditorView = inflate.findViewById(R.id.photoEditorView);
        this.glSurfaceView = this.photoEditorView.getGLSurfaceView();
        this.loadingView = inflate.findViewById(R.id.loadingView);
        this.boobs = inflate.findViewById(R.id.boobs);
        this.wrapBoobs = inflate.findViewById(R.id.wrapBoobs);
        this.wrapBoobs.setOnClickListener(this.onClickListener);
        this.tvBoobs = inflate.findViewById(R.id.tvBoobs);
        this.waise = inflate.findViewById(R.id.waist);
        this.wrapWaist = inflate.findViewById(R.id.wrapWaist);
        this.wrapWaist.setOnClickListener(this.onClickListener);
        this.tvWaise = inflate.findViewById(R.id.tvWaist);
        this.resetWaist = inflate.findViewById(R.id.resetWaist);
        this.resetWaist.setOnClickListener(this.onClickListener);
        this.seat = inflate.findViewById(R.id.seat);
        this.wrapHip = inflate.findViewById(R.id.wrapHip);
        this.wrapHip.setOnClickListener(this.onClickListener);
        this.tvSeat = inflate.findViewById(R.id.tvSeat);
        this.face = inflate.findViewById(R.id.face);
        this.wrapFace = inflate.findViewById(R.id.wrapFace);
        this.wrapFace.setOnClickListener(this.onClickListener);
        this.tvFace = inflate.findViewById(R.id.tvFace);
        this.magicLayout = inflate.findViewById(R.id.magicLayout);
        this.viewGroup = viewGroup2;
        this.intensitySmooth = inflate.findViewById(R.id.intensitySmooth);
        this.intensitySmooth.setOnSeekBarChangeListener(this.onSeekBarChangeListener);
        this.lstRetouch = new ArrayList();
        this.lstRetouch.add(new Retouch(this.boobs, this.tvBoobs, R.drawable.boobs, R.drawable.boobs_selected));
        this.lstRetouch.add(new Retouch(this.waise, this.tvWaise, R.drawable.waist, R.drawable.waist_selected));
        this.lstRetouch.add(new Retouch(this.seat, this.tvSeat, R.drawable.seat, R.drawable.seat_selected));
        this.lstRetouch.add(new Retouch(this.face, this.tvFace, R.drawable.beauty_face, R.drawable.beauty_face_selected));

        app_ad = (FrameLayout) inflate.findViewById(R.id.bannerContainer);
        tvLoading = (TextView) inflate.findViewById(R.id.bannerTvLoading);
        mainConaitner = (RelativeLayout) inflate.findViewById(R.id.adsContainer);
        this.intensityTwoDirection.setScrollingListener(new DegreeSeekBar.ScrollingListener() {
            public void onScrollStart() {
                Iterator<Sticker> it = BeautyDialog.this.photoEditorView.getStickers().iterator();
                while (it.hasNext()) {
                    ((BeautySticker) it.next()).updateRadius();
                }
            }

            public void onScroll(final int currentDegrees) {
                TextureRenderer.Viewport renderViewport = BeautyDialog.this.glSurfaceView.getRenderViewport();
                final float w = (float) renderViewport.width;
                final float h = (float) renderViewport.height;
                if (BeautyDialog.this.currentType == 7) {
                    BeautyDialog.this.glSurfaceView.lazyFlush(true, new Runnable() {
                        public final void run() {
                            if (mDeformWrapper != null) {
                                mDeformWrapper.restore();
                                for (Sticker next : photoEditorView.getStickers()) {
                                    PointF mappedCenterPoint2 = ((BeautySticker) next).getMappedCenterPoint2();
                                    Log.i("CURRENT", currentType + "");
                                    for (int i2 = 0; i2 < Math.abs(currentType); i2++) {
                                        if (currentType > 0) {
                                            mDeformWrapper.bloatDeform(mappedCenterPoint2.x, mappedCenterPoint2.y, w, h, (float) (next.getWidth() / 2), 0.03f);
                                        } else if (currentType < 0) {
                                            mDeformWrapper.wrinkleDeform(mappedCenterPoint2.x, mappedCenterPoint2.y, w, h, (float) (next.getWidth() / 2), 0.03f);
                                        }
                                    }
                                }
                            }
                        }
                    });
                } else if (BeautyDialog.this.currentType == 9) {
                    BeautyDialog.this.glSurfaceView.lazyFlush(true, new Runnable() {
                        public void run() {
                            if (BeautyDialog.this.mDeformWrapper != null) {
                                BeautyDialog.this.mDeformWrapper.restore();
                                Iterator<Sticker> it = BeautyDialog.this.photoEditorView.getStickers().iterator();
                                while (it.hasNext()) {
                                    BeautySticker beautySticker = (BeautySticker) it.next();
                                    PointF mappedCenterPoint2 = beautySticker.getMappedCenterPoint2();
                                    RectF mappedBound = beautySticker.getMappedBound();
                                    for (int i = 0; i < Math.abs(currentDegrees); i++) {
                                        if (currentDegrees > 0) {
                                            float f = (float) 20;
                                            float f2 = f;
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(mappedBound.right - f, mappedCenterPoint2.y, mappedBound.right + f, mappedCenterPoint2.y, f, f2, (float) beautySticker.getRadius(), 0.01f);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(mappedBound.left + f2, mappedCenterPoint2.y, mappedBound.left - f2, mappedCenterPoint2.y, f, f2, (float) beautySticker.getRadius(), 0.01f);
                                        } else {
                                            float f3 = (float) 20;
                                            float f4 = f3;
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(mappedBound.right + f3, mappedCenterPoint2.y, mappedBound.right - f3, mappedCenterPoint2.y, w, h, (float) beautySticker.getRadius(), 0.01f);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(mappedBound.left - f4, mappedCenterPoint2.y, mappedBound.left + f4, mappedCenterPoint2.y, w, h, (float) beautySticker.getRadius(), 0.01f);
                                        }
                                    }
                                }
                            }
                        }
                    });
                } else if (BeautyDialog.this.currentType == 4)
                    BeautyDialog.this.glSurfaceView.lazyFlush(true, new Runnable() {
                        public void run() {
                            if (BeautyDialog.this.mDeformWrapper == null)
                                return;
                            BeautyDialog.this.mDeformWrapper.restore();
                            Iterator<Sticker> iterator = BeautyDialog.this.photoEditorView.getStickers().iterator();
                            label17:
                            while (iterator.hasNext()) {
                                BeautySticker beautySticker = (BeautySticker) iterator.next();
                                PointF pointF = beautySticker.getMappedCenterPoint2();
                                RectF rectF = beautySticker.getMappedBound();
                                int i = beautySticker.getRadius() / 2;
                                float f1 = (rectF.left + pointF.x) / 2.0F;
                                float f2 = rectF.left + (f1 - rectF.left) / 2.0F;
                                float f3 = (rectF.bottom + rectF.top) / 2.0F;
                                float f4 = rectF.top + (f3 - rectF.top) / 2.0F;
                                float f5 = (rectF.right + pointF.x) / 2.0F;
                                float f6 = rectF.right - (rectF.right - f5) / 2.0F;
                                float f7 = (rectF.bottom + rectF.top) / 2.0F;
                                float f8 = rectF.top + (f7 - rectF.top) / 2.0F;
                                int j = 0;
                                Iterator<Sticker> iterator1 = iterator;
                                while (true) {
                                    iterator = iterator1;
                                    if (j < Math.abs(currentDegrees)) {
                                        if (currentDegrees > 0) {
                                            CGEDeformFilterWrapper cGEDeformFilterWrapper = BeautyDialog.this.mDeformWrapper;
                                            float f9 = rectF.right;
                                            float f10 = rectF.top;
                                            float f11 = rectF.right;
                                            float f12 = i;
                                            cGEDeformFilterWrapper.forwardDeform(f9, f10, f11 - f12, rectF.top, w, h, beautySticker.getRadius(), 0.002F);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(f6, f8, f6 - f12, f8, w, h, beautySticker.getRadius(), 0.005F);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(f5, f7, f5 - f12, f7, w, h, beautySticker.getRadius(), 0.007F);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(rectF.left, rectF.top, rectF.left + f12, rectF.top, w, h, beautySticker.getRadius(), 0.002F);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(f2, f4, f2 + f12, f4, w, h, beautySticker.getRadius(), 0.005F);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(f1, f3, f1 + f12, f3, w, h, beautySticker.getRadius(), 0.007F);
                                        } else {
                                            CGEDeformFilterWrapper cGEDeformFilterWrapper = BeautyDialog.this.mDeformWrapper;
                                            float f9 = rectF.right;
                                            float f10 = rectF.top;
                                            float f11 = rectF.right;
                                            float f12 = i;
                                            cGEDeformFilterWrapper.forwardDeform(f9, f10, f11 + f12, rectF.top, w, h, beautySticker.getRadius(), 0.002F);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(f6, f8, f6 + f12, f8, w, h, beautySticker.getRadius(), 0.005F);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(f5, f7, f5 + f12, f7, w, h, beautySticker.getRadius(), 0.007F);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(rectF.left + f12, rectF.top, rectF.left, rectF.top, w, h, beautySticker.getRadius(), 0.002F);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(f2, f4, f2 - f12, f4, w, h, beautySticker.getRadius(), 0.005F);
                                            BeautyDialog.this.mDeformWrapper.forwardDeform(f1, f3, f1 - f12, f3, w, h, beautySticker.getRadius(), 0.007F);
                                        }
                                        j++;
                                        continue;
                                    }
                                    continue label17;
                                }
                            }
                        }
                    });
            }


            public void onScrollEnd() {
                BeautyDialog.this.glSurfaceView.requestRender();
            }
        });
        BitmapStickerIcon bitmapStickerIcon = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_sticker_ic_scale_black_18dp), 3, BitmapStickerIcon.ZOOM);
        bitmapStickerIcon.setIconEvent(new ZoomIconEvent());
        BitmapStickerIcon bitmapStickerIcon2 = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_sticker_ic_scale_black_2_18dp), 2, BitmapStickerIcon.ZOOM);
        bitmapStickerIcon2.setIconEvent(new ZoomIconEvent());
        this.photoEditorView.setIcons(Arrays.asList(bitmapStickerIcon, bitmapStickerIcon2));
        this.photoEditorView.setBackgroundColor(-16777216);
        this.photoEditorView.setLocked(false);
        this.photoEditorView.setConstrained(true);
        this.photoEditorView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            public void onStickerAdded(@NonNull Sticker sticker) {
            }

            public void onStickerClicked(@NonNull Sticker sticker) {
            }

            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
            }

            public void onStickerDragFinished(@NonNull Sticker sticker) {
            }

            public void onStickerFlipped(@NonNull Sticker sticker) {
            }

            public void onStickerTouchOutside() {
            }

            public void onStickerTouchedDown(@NonNull Sticker sticker) {
            }

            public void onStickerZoomFinished(@NonNull Sticker sticker) {
            }

            public void onTouchUpForBeauty(float f, float f2) {
            }

            public void onStickerDeleted(@NonNull Sticker sticker) {
                BeautyDialog.this.loadingView.setVisibility(View.GONE);
            }

            public void onTouchDownForBeauty(float f, float f2) {
                BeautyDialog.this.startX = f;
                BeautyDialog.this.startY = f2;
            }

            public void onTouchDragForBeauty(float f, float f2) {
                TextureRenderer.Viewport renderViewport = BeautyDialog.this.glSurfaceView.getRenderViewport();
                float f3 = (float) renderViewport.height;
                BeautyDialog.this.glSurfaceView.lazyFlush(true, new Runnable() {


                    public final void run() {

                        if (mDeformWrapper != null) {
                            mDeformWrapper.forwardDeform(BeautyDialog.this.startX, BeautyDialog.this.startY, f, f2, renderViewport.width, f3, 200.0f, 0.02f);
                        }
                    }
                });
                BeautyDialog.this.startX = f;
                BeautyDialog.this.startY = f2;
            }


        });
        this.compare = inflate.findViewById(R.id.compare);
        this.compare.setOnTouchListener(new View.OnTouchListener() {
            public final boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getActionMasked()) {
                    case 0:
                        photoEditorView.getGLSurfaceView().setAlpha(0.0f);
                        return true;
                    case 1:
                        photoEditorView.getGLSurfaceView().setAlpha(1.0f);
                        return false;
                    default:
                        return true;
                }

            }
        });
        inflate.findViewById(R.id.imgSave).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                    new SaveCurrentState(true).execute(new Void[0]);
                    return null;
                });

            }
        });
        inflate.findViewById(R.id.imgClose).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(getActivity(),()->{
                    BeautyDialog.this.dismiss();
                    return null;
                });

            }


        });
        this.photoEditorView.setImageSource(this.bitmap, new ImageGLSurfaceView.OnSurfaceCreatedCallback() {
            public final void surfaceCreated() {

                if (bitmap == null) {
                    return;
                }
                glSurfaceView.setImageBitmap(bitmap);
                glSurfaceView.queueEvent(new Runnable() {
                    public final void run() {

                        float width = (float) bitmap.getWidth();
                        float height = (float) bitmap.getHeight();
                        float min = Math.min(((float) glSurfaceView.getRenderViewport().width) / width, ((float) glSurfaceView.getRenderViewport().height) / height);
                        if (min < 1.0f) {
                            width *= min;
                            height *= min;
                        }
                        mDeformWrapper = CGEDeformFilterWrapper.create((int) width, (int) height, 10.0f);
                        mDeformWrapper.setUndoSteps(ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION);
                        if (mDeformWrapper != null) {
                            CGEImageHandler imageHandler = glSurfaceView.getImageHandler();
                            imageHandler.setFilterWithAddres(mDeformWrapper.getNativeAddress());
                            imageHandler.processFilters();
                        }
                    }
                });
            }
        });
        this.photoEditorView.post(new Runnable() {
            public final void run() {

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(glSurfaceView.getRenderViewport().width, glSurfaceView.getRenderViewport().height);
                layoutParams.addRule(13);
                photoEditorView.setLayoutParams(layoutParams);
            }
        });
        hideAllFunction();
        return inflate;
    }


    private void selectFunction(int i) {
        for (int i2 = 0; i2 < this.lstRetouch.size(); i2++) {
            if (i2 == i) {
                Retouch retouch = this.lstRetouch.get(i2);
                retouch.imageView.setImageResource(retouch.drawableSelected);
                retouch.textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            } else {
                Retouch retouch2 = this.lstRetouch.get(i2);
                retouch2.imageView.setImageResource(retouch2.drawable);
                retouch2.textView.setTextColor(ContextCompat.getColor(getContext(), R.color.unselected_color));
            }
        }
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle bundle) {
        super.onViewCreated(view, bundle);
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onPause() {
        super.onPause();
    }

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

    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(-1, -1);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(-16777216));
        }
    }

    class Retouch {
        int drawable;
        int drawableSelected;
        ImageView imageView;
        TextView textView;

        Retouch(ImageView imageView2, TextView textView2, int i, int i2) {
            this.drawable = i;
            this.drawableSelected = i2;
            this.imageView = imageView2;
            this.textView = textView2;
        }
    }

    class SaveCurrentState extends AsyncTask<Void, Void, Bitmap> {
        boolean isCloseDialog;

        SaveCurrentState() {
        }

        SaveCurrentState(boolean z) {
            this.isCloseDialog = z;
        }


        public void onPreExecute() {
            BeautyDialog.this.getDialog().getWindow().setFlags(16, 16);
            BeautyDialog.this.loadingView.setVisibility(View.VISIBLE);
        }


        public Bitmap doInBackground(Void... voidArr) {
            final Bitmap[] bitmapArr = {null};
            BeautyDialog.this.photoEditorView.saveGLSurfaceViewAsBitmap(new OnSaveBitmap() {
                public void onFailure(Exception exc) {
                }

                public void onBitmapReady(Bitmap bitmap) {
                    bitmapArr[0] = bitmap;
                }
            });
            while (bitmapArr[0] == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return bitmapArr[0];
        }


        public void onPostExecute(Bitmap bitmap) {
            BeautyDialog.this.photoEditorView.setImageSource(bitmap);
            BeautyDialog.this.loadingView.setVisibility(View.GONE);
            try {
                BeautyDialog.this.getDialog().getWindow().clearFlags(16);
            } catch (Exception e) {
            }
            BeautyDialog.this.glSurfaceView.flush(true, new Runnable() {
                public final void run() {
                    if (BeautyDialog.this.mDeformWrapper != null) {
                        BeautyDialog.this.mDeformWrapper.restore();
                        BeautyDialog.this.glSurfaceView.requestRender();
                    }
                }
            });
            if (isCloseDialog) {
                BeautyDialog.this.onBeautySave.onBeautySave(bitmap);
                BeautyDialog.this.dismiss();
            }
        }


    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mDeformWrapper != null) {
            this.mDeformWrapper.release(true);
            this.mDeformWrapper = null;
        }
        this.glSurfaceView.release();
        this.glSurfaceView.onPause();
    }
}
