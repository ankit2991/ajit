package com.lockerroom.face.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.internal.view.SupportMenu;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hold1.keyboardheightprovider.KeyboardHeightProvider;
import com.lockerroom.face.Constants;
import com.lockerroom.face.PhotoApp;
import com.lockerroom.face.R;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.base.BaseActivity;
import com.lockerroom.face.features.ColorSplashDialog;
import com.lockerroom.face.features.addtext.AddTextProperties;
import com.lockerroom.face.features.addtext.TextEditorDialogFragment222;
import com.lockerroom.face.features.adjust.AdjustAdapter;
import com.lockerroom.face.features.adjust.AdjustListener;
import com.lockerroom.face.features.beauty.BeautyDialog;
import com.lockerroom.face.features.crop.CropDialogFragment;
import com.lockerroom.face.features.draw.BrushColorListener;
import com.lockerroom.face.features.draw.BrushMagicListener;
import com.lockerroom.face.features.draw.ColorAdapter;
import com.lockerroom.face.features.draw.MagicBrushAdapter;
import com.lockerroom.face.features.insta.InstaDialog;
import com.lockerroom.face.features.mosaic.MosaicDialog;
import com.lockerroom.face.features.picker.PhotoPicker;
import com.lockerroom.face.features.picker.utils.PermissionsUtils;
import com.lockerroom.face.features.splash.SplashDialog;
import com.lockerroom.face.features.sticker.adapter.RecyclerTabLayout;
import com.lockerroom.face.features.sticker.adapter.StickerAdapter;
import com.lockerroom.face.features.sticker.adapter.TopTabEditAdapter;
import com.lockerroom.face.filters.FilterListener;
import com.lockerroom.face.filters.FilterUtils;
import com.lockerroom.face.filters.FilterViewAdapter;
import com.lockerroom.face.firebaseAdsConfig.RemoteConfigData;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.BannerAdListener;
import com.lockerroom.face.maxAdManager.MaxAdConstants;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.maxAdManager.OnAdShowCallback;
import com.lockerroom.face.photoeditor.DrawBitmapModel;
import com.lockerroom.face.photoeditor.OnPhotoEditorListener;
import com.lockerroom.face.photoeditor.PhotoEditor;
import com.lockerroom.face.photoeditor.PhotoEditorView;
import com.lockerroom.face.photoeditor.ViewType;
import com.lockerroom.face.sticker.BitmapStickerIcon;
import com.lockerroom.face.sticker.DrawableSticker;
import com.lockerroom.face.sticker.Sticker;
import com.lockerroom.face.sticker.StickerView;
import com.lockerroom.face.sticker.TextSticker;
import com.lockerroom.face.sticker.event.DeleteIconEvent;
import com.lockerroom.face.sticker.event.FlipHorizontallyEvent;
import com.lockerroom.face.sticker.event.ZoomIconEvent;
import com.lockerroom.face.tools.EditingToolsAdapter;
import com.lockerroom.face.tools.ToolType;
import com.lockerroom.face.utils.AssetUtils;
import com.lockerroom.face.utils.FileUtils;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.SharePreferenceUtil;
import com.lockerroom.face.utils.SystemUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wysaid.nativePort.CGENativeLibrary;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


@SuppressLint("StaticFieldLeak")
public class EditImageActivity extends BaseActivity implements OnPhotoEditorListener, View.OnClickListener, StickerAdapter.OnClickStickerListener,
        CropDialogFragment.OnCropPhoto, BrushColorListener, BrushMagicListener, InstaDialog.InstaSaveListener, SplashDialog.SplashDialogListener,
        BeautyDialog.OnBeautySave, MosaicDialog.MosaicDialogListener, EditingToolsAdapter.OnItemSelected, FilterListener, AdjustListener {
    private static final String TAG = "EditImageActivity";
    public ImageView addNewSticker;
    private ImageView addNewText;
    private ConstraintLayout adjustLayout;
    private SeekBar adjustSeekBar;
    private TextView brush;
    private TextView brushBlur;
    private ConstraintLayout brushLayout;
    private SeekBar brushSize;
    private ImageView compareAdjust;
    public ImageView compareFilter;
    public ImageView compareOverlay;
    public ToolType currentMode = ToolType.NONE;
    private ImageView erase;
    private SeekBar eraseSize;
    public SeekBar filterIntensity;
    public ConstraintLayout filterLayout;
    private KeyboardHeightProvider keyboardHeightProvider;
    private RelativeLayout loadingView;
    protected RelativeLayout abc;

    public ArrayList<@Nullable Bitmap> lstBitmapWithFilter = new ArrayList<@Nullable Bitmap>();

    public List<@Nullable Bitmap> lstBitmapWithOverlay = new ArrayList<>();

    public AdjustAdapter mAdjustAdapter;
    private RecyclerView mColorBush;
    private EditingToolsAdapter mEditingToolsAdapter;

    public CGENativeLibrary.LoadImageCallback mLoadImageCallback = new CGENativeLibrary.LoadImageCallback() {
        public Bitmap loadImage(String str, Object obj) {
            try {

                return BitmapFactory.decodeStream(EditImageActivity.this.getAssets().open(str));

            } catch (IOException io) {

                return null;

            }
        }

        public void loadImageOK(Bitmap bitmap, Object obj) {
            bitmap.recycle();
        }
    };

    private RecyclerView mMagicBrush;
    public PhotoEditor mPhotoEditor;
    public PhotoEditorView mPhotoEditorView;
    private ConstraintLayout mRootView;
    private RecyclerView mRvAdjust;
    public RecyclerView mRvFilters;

    public RecyclerView mRvOverlays;

    public RecyclerView mRvTools;
    private TextView magicBrush;
    View.OnTouchListener onCompareTouchListener = (view, motionEvent) -> {
        switch (motionEvent.getAction()) {
            case 0:
                EditImageActivity.this.mPhotoEditorView.getGLSurfaceView().setAlpha(0.0f);
                return true;
            case 1:
                EditImageActivity.this.mPhotoEditorView.getGLSurfaceView().setAlpha(1.0f);
                return false;
            default:
                return true;
        }
    };

    public SeekBar overlayIntensity;

    public ConstraintLayout overlayLayout;
    private ImageView redo;
    private ConstraintLayout saveControl;

    public SeekBar stickerAlpha;
    private ConstraintLayout stickerLayout;

    public TextEditorDialogFragment222.TextEditor textEditor;

    public TextEditorDialogFragment222 textEditorDialogFragment;
    private ConstraintLayout textLayout;
    private ImageView undo;

    public RelativeLayout wrapPhotoView;

    public LinearLayout wrapStickerList;
//    public static MaxInterstitialAd interstitialAd;
//    private int retryAttempt;
//    void createInterstitialAd()
//    {
//        interstitialAd = new MaxInterstitialAd( "762f04ccdbb983c6", this );
//        interstitialAd.setListener( this );
//
//        // Load the first ad
//        interstitialAd.loadAd();
//    }
     FrameLayout app_ad;
     TextView tvLoading;
     RelativeLayout mainConaitner;
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        makeFullScreen();
        setContentView(R.layout.activity_edit_image);
//        createInterstitialAd();

         app_ad = (FrameLayout) findViewById(R.id.bannerContainer);
         tvLoading = (TextView) findViewById(R.id.bannerTvLoading);
         mainConaitner = (RelativeLayout) findViewById(R.id.maxBannerAdContainer);
        if (Constants.SHOW_ADS) {

            IronSourceAdsManager.INSTANCE.loadInter(this, new IronSourceCallbacks() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFail() {

                }
            });

        } else {
            findViewById(R.id.adsContainer).setVisibility(View.GONE);
        }

        initViews();


//        CGENativeLibrary.setLoadImageCallback(this.mLoadImageCallback, null);
//        if (Build.VERSION.SDK_INT < 26) {
//            getWindow().setSoftInputMode(48);
//        }
        mEditingToolsAdapter = new EditingToolsAdapter(this, EditImageActivity.this);
        this.mRvTools.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mRvTools.setAdapter(this.mEditingToolsAdapter);
        this.mRvFilters.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mRvFilters.setHasFixedSize(true);
        this.mRvOverlays.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mRvOverlays.setHasFixedSize(true);
        new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        this.mRvAdjust.setLayoutManager(new GridLayoutManager(this, 4));
        this.mRvAdjust.setHasFixedSize(true);
        this.mAdjustAdapter = new AdjustAdapter(getApplicationContext(), this,EditImageActivity.this);
        this.mRvAdjust.setAdapter(this.mAdjustAdapter);
        this.mColorBush.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mColorBush.setHasFixedSize(true);
        this.mColorBush.setAdapter(new ColorAdapter(EditImageActivity.this, this));
        this.mMagicBrush.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mMagicBrush.setHasFixedSize(true);
        this.mMagicBrush.setAdapter(new MagicBrushAdapter(EditImageActivity.this, this));
        this.mPhotoEditor = new PhotoEditor.Builder(this, this.mPhotoEditorView).setPinchTextScalable(true).build();
        this.mPhotoEditor.setOnPhotoEditorListener(this);
        toogleDrawBottomToolbar(false);
        this.brushLayout.setAlpha(0.0f);
        this.adjustLayout.setAlpha(0.0f);
        this.filterLayout.setAlpha(0.0f);
        this.stickerLayout.setAlpha(0.0f);
        this.textLayout.setAlpha(0.0f);
        this.overlayLayout.setAlpha(0.0f);
        findViewById(R.id.activitylayout).post(() -> {
            slideDown(brushLayout);
            slideDown(adjustLayout);
            slideDown(filterLayout);
            slideDown(stickerLayout);
            slideDown(textLayout);
            slideDown(overlayLayout);
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            brushLayout.setAlpha(1.0f);
            adjustLayout.setAlpha(1.0f);
            filterLayout.setAlpha(1.0f);
            stickerLayout.setAlpha(1.0f);
            textLayout.setAlpha(1.0f);
            overlayLayout.setAlpha(1.0f);
        }, 1000);

//        SharePreferenceUtil.setHeightOfKeyboard(getApplicationContext(), 0);
//        this.keyboardHeightProvider = new KeyboardHeightProvider(this);
//        this.keyboardHeightProvider.addKeyboardListener(i -> {
//            if (i <= 0) {
//                SharePreferenceUtil.setHeightOfNotch(getApplicationContext(), -i);
//            } else if (textEditorDialogFragment != null) {
//                textEditorDialogFragment.updateAddTextBottomToolbarHeight(SharePreferenceUtil.getHeightOfNotch(getApplicationContext()) + i);
//                SharePreferenceUtil.setHeightOfKeyboard(getApplicationContext(), i + SharePreferenceUtil.getHeightOfNotch(getApplicationContext()));
//            }
//        });
//        if (SharePreferenceUtil.isPurchased(getApplicationContext())) {
//            ((ConstraintLayout.LayoutParams) this.wrapPhotoView.getLayoutParams()).topMargin = SystemUtil.dpToPx(getApplicationContext(), 5);
//        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {


            /////////////////////////////////////////////////////////////////////////////////////////////////
// error here
            Log.e("camerapicture",">"+extras.getString(PhotoPicker.KEY_SELECTED_PHOTOS));
            if (extras.getString(PhotoPicker.KEY_SELECTED_PHOTOS) == null){
                return;
            }
            Glide.with(this)
                    .asBitmap()
                    .load(Uri.fromFile(new File(extras.getString(PhotoPicker.KEY_SELECTED_PHOTOS))))
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @androidx.annotation.Nullable Transition<? super Bitmap> transition) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    EditImageActivity.this.mPhotoEditorView.setImageSource(resource);
                                    EditImageActivity.this.updateLayout();
                                }
                            });
                        }

                        @Override
                        public void onLoadCleared(@androidx.annotation.Nullable Drawable placeholder) {

                        }
                    });

            /////////////////////////////////////////////////////////////////////////////////////////////////


            new OnLoadCGEF().execute(extras.getString(PhotoPicker.KEY_SELECTED_PHOTOS));

//            new OnLoadBitmapFromUri().execute(extras.getString(PhotoPicker.KEY_SELECTED_PHOTOS));

        }
    }


    private void toogleDrawBottomToolbar(boolean z) {
        int i = !z ? View.GONE : View.VISIBLE;
        this.brush.setVisibility(i);
        this.magicBrush.setVisibility(i);
        this.brushBlur.setVisibility(i);
        this.erase.setVisibility(i);
        this.undo.setVisibility(i);
        this.redo.setVisibility(i);
    }


    public void showEraseBrush() {
        this.brushSize.setVisibility(View.GONE);
        this.mColorBush.setVisibility(View.GONE);
        this.eraseSize.setVisibility(View.VISIBLE);
        this.mMagicBrush.setVisibility(View.GONE);
        this.brush.setBackgroundResource(0);
        this.brush.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.magicBrush.setBackgroundResource(0);
        this.magicBrush.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.brushBlur.setBackgroundResource(0);
        this.brushBlur.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.erase.setImageResource(R.drawable.erase_selected);
        this.mPhotoEditor.brushEraser();
        this.eraseSize.setProgress(20);
    }


    public void showColorBlurBrush() {
        this.brushSize.setVisibility(View.VISIBLE);
        this.mColorBush.setVisibility(View.VISIBLE);
        ColorAdapter colorAdapter = (ColorAdapter) this.mColorBush.getAdapter();
        if (colorAdapter != null) {
            colorAdapter.setSelectedColorIndex(0);
        }
        this.mColorBush.scrollToPosition(0);
        if (colorAdapter != null) {
            colorAdapter.notifyDataSetChanged();
        }
        this.eraseSize.setVisibility(View.GONE);
        this.mMagicBrush.setVisibility(View.GONE);
        this.erase.setImageResource(R.drawable.erase);
        this.magicBrush.setBackgroundResource(0);
        this.magicBrush.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.brush.setBackgroundResource(0);
        this.brush.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.brushBlur.setBackground(ContextCompat.getDrawable(this, R.drawable.border_bottom));
        this.brushBlur.setTextColor(ContextCompat.getColor(this, R.color.black));
        this.mPhotoEditor.setBrushMode(2);
        this.mPhotoEditor.setBrushDrawingMode(true);
        this.brushSize.setProgress(20);
    }


    public void showColorBrush() {
        this.brushSize.setVisibility(View.VISIBLE);
        this.mColorBush.setVisibility(View.VISIBLE);
        this.mColorBush.scrollToPosition(0);
        ColorAdapter colorAdapter = (ColorAdapter) this.mColorBush.getAdapter();
        if (colorAdapter != null) {
            colorAdapter.setSelectedColorIndex(0);
        }
        if (colorAdapter != null) {
            colorAdapter.notifyDataSetChanged();
        }
        this.eraseSize.setVisibility(View.GONE);
        this.mMagicBrush.setVisibility(View.GONE);
        this.erase.setImageResource(R.drawable.erase);
        this.magicBrush.setBackgroundResource(0);
        this.magicBrush.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.brush.setBackground(ContextCompat.getDrawable(this, R.drawable.border_bottom));
        this.brush.setTextColor(ContextCompat.getColor(this, R.color.black));
        this.brushBlur.setBackgroundResource(0);
        this.brushBlur.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.mPhotoEditor.setBrushMode(1);
        this.mPhotoEditor.setBrushDrawingMode(true);
        this.brushSize.setProgress(20);
    }


    public void showMagicBrush() {
        this.brushSize.setVisibility(View.VISIBLE);
        this.mColorBush.setVisibility(View.GONE);
        this.eraseSize.setVisibility(View.GONE);
        this.mMagicBrush.setVisibility(View.VISIBLE);
        this.erase.setImageResource(R.drawable.erase);
        this.magicBrush.setBackground(ContextCompat.getDrawable(this, R.drawable.border_bottom));
        this.magicBrush.setTextColor(ContextCompat.getColor(this, R.color.black));
        this.brush.setBackgroundResource(0);
        this.brush.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.brushBlur.setBackgroundResource(0);
        this.brushBlur.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.mPhotoEditor.setBrushMagic(MagicBrushAdapter.lstDrawBitmapModel(getApplicationContext()).get(0));
        this.mPhotoEditor.setBrushMode(3);
        this.mPhotoEditor.setBrushDrawingMode(true);
        MagicBrushAdapter magicBrushAdapter = (MagicBrushAdapter) this.mMagicBrush.getAdapter();
        if (magicBrushAdapter != null) {
            magicBrushAdapter.setSelectedColorIndex(0);
        }
        this.mMagicBrush.scrollToPosition(0);
        if (magicBrushAdapter != null) {
            magicBrushAdapter.notifyDataSetChanged();
        }
    }

    private void initViews() {
        this.wrapStickerList = findViewById(R.id.wrapStickerList);
        this.mPhotoEditorView = findViewById(R.id.photoEditorView);
        this.mPhotoEditorView.setVisibility(View.INVISIBLE);
        this.mRvTools = findViewById(R.id.rvConstraintTools);
        this.mRvFilters = findViewById(R.id.rvFilterView);
        this.mRvOverlays = findViewById(R.id.rvOverlayView);
        this.mRvAdjust = findViewById(R.id.rvAdjustView);
        this.mRootView = findViewById(R.id.rootView);
        this.filterLayout = findViewById(R.id.filterLayout);
        this.overlayLayout = findViewById(R.id.overlayLayout);
        this.adjustLayout = findViewById(R.id.adjustLayout);
        this.stickerLayout = findViewById(R.id.stickerLayout);
        this.textLayout = findViewById(R.id.textControl);
        ViewPager viewPager = findViewById(R.id.sticker_viewpaper);
        this.filterIntensity = findViewById(R.id.filterIntensity);
        this.overlayIntensity = findViewById(R.id.overlayIntensity);
        this.stickerAlpha = findViewById(R.id.stickerAlpha);
        this.stickerAlpha.setVisibility(View.GONE);
        this.brushLayout = findViewById(R.id.brushLayout);
        this.mColorBush = findViewById(R.id.rvColorBush);
        this.mMagicBrush = findViewById(R.id.rvMagicBush);
        this.wrapPhotoView = findViewById(R.id.wrap_photo_view);
        this.brush = findViewById(R.id.draw);
        this.magicBrush = findViewById(R.id.brush_magic);
        this.erase = findViewById(R.id.erase);
        this.undo = findViewById(R.id.undo);
        this.undo.setVisibility(View.GONE);
        this.redo = findViewById(R.id.redo);
        this.redo.setVisibility(View.GONE);
        this.brushBlur = findViewById(R.id.brush_blur);
        this.brushSize = findViewById(R.id.brushSize);
        this.eraseSize = findViewById(R.id.eraseSize);
        this.loadingView = findViewById(R.id.loadingView);
        this.loadingView.setVisibility(View.VISIBLE);
        TextView saveBitmap = findViewById(R.id.save);
        this.saveControl = findViewById(R.id.saveControl);
        saveBitmap.setOnClickListener(view -> {
            MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                    new SaveBitmapAsFile().execute();
                }else {
                    if (PermissionsUtils.checkWriteStoragePermission(EditImageActivity.this)) {
                        new SaveBitmapAsFile().execute();

                    }
                }
                return null;
            });

        });
        this.compareAdjust = findViewById(R.id.compareAdjust);
        this.compareAdjust.setOnTouchListener(this.onCompareTouchListener);
        this.compareAdjust.setVisibility(View.GONE);

        this.compareFilter = findViewById(R.id.compareFilter);
        this.compareFilter.setOnTouchListener(this.onCompareTouchListener);
        this.compareFilter.setVisibility(View.GONE);

        this.compareOverlay = findViewById(R.id.compareOverlay);
        this.compareOverlay.setOnTouchListener(this.onCompareTouchListener);
        this.compareOverlay.setVisibility(View.GONE);
        findViewById(R.id.exitEditMode).setOnClickListener(view -> EditImageActivity.this.onBackPressed());
        this.erase.setOnClickListener(view -> {
            MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                EditImageActivity.this.showEraseBrush();
                return null;
            });

        });
        this.brush.setOnClickListener(view -> {

            MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                EditImageActivity.this.showColorBrush();
                return null;
            });

        });
        this.magicBrush.setOnClickListener(view -> {
            MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                EditImageActivity.this.showMagicBrush();
                return null;
            });
        });
        this.brushBlur.setOnClickListener(view -> {
            MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                EditImageActivity.this.showColorBlurBrush();
                return null;
            });
        });
        this.eraseSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                EditImageActivity.this.mPhotoEditor.setBrushEraserSize((float) i);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                EditImageActivity.this.mPhotoEditor.brushEraser();
            }
        });
        this.brushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                EditImageActivity.this.mPhotoEditor.setBrushSize((float) (i + 10));
            }
        });
        this.stickerAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                Sticker currentSticker = EditImageActivity.this.mPhotoEditorView.getCurrentSticker();
                if (currentSticker != null) {
                    currentSticker.setAlpha(i);
                }
            }
        });
        this.addNewSticker = findViewById(R.id.addNewSticker);
        this.addNewSticker.setVisibility(View.GONE);
        this.addNewSticker.setOnClickListener(view -> {
            MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                EditImageActivity.this.addNewSticker.setVisibility(View.GONE);
                EditImageActivity.this.slideUp(EditImageActivity.this.wrapStickerList);
                return null;
            });

        });
        this.addNewText = findViewById(R.id.addNewText);
        this.addNewText.setVisibility(View.GONE);
        this.addNewText.setOnClickListener(view -> {
            MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                EditImageActivity.this.mPhotoEditorView.setHandlingSticker(null);
                EditImageActivity.this.openTextFragment();
                return null;
            });
        });
        this.adjustSeekBar = findViewById(R.id.adjustLevel);
        this.adjustSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                EditImageActivity.this.mAdjustAdapter.getCurrentAdjustModel().setIntensity(EditImageActivity.this.mPhotoEditor, ((float) i) / ((float) seekBar.getMax()), true);
            }
        });
        BitmapStickerIcon bitmapStickerIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.sticker_ic_close_white_18dp), 0, BitmapStickerIcon.REMOVE);
        bitmapStickerIcon.setIconEvent(new DeleteIconEvent());
        BitmapStickerIcon bitmapStickerIcon2 = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_sticker_ic_scale_black_18dp), 3, BitmapStickerIcon.ZOOM);
        bitmapStickerIcon2.setIconEvent(new ZoomIconEvent());
        BitmapStickerIcon bitmapStickerIcon3 = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_sticker_ic_flip_black_18dp), 1, BitmapStickerIcon.FLIP);
        bitmapStickerIcon3.setIconEvent(new FlipHorizontallyEvent());
//        BitmapStickerIcon bitmapStickerIcon4 = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_rotate_black_18dp), 3, BitmapStickerIcon.ROTATE);
//        bitmapStickerIcon4.setIconEvent(new ZoomIconEvent());
//        BitmapStickerIcon bitmapStickerIcon5 = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_edit_black_18dp), 1, BitmapStickerIcon.EDIT);
//        bitmapStickerIcon5.setIconEvent(new EditTextIconEvent());
//        BitmapStickerIcon bitmapStickerIcon6 = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_center_black_18dp), 2, BitmapStickerIcon.ALIGN_HORIZONTALLY);
//        bitmapStickerIcon6.setIconEvent(new AlignHorizontallyEvent());
        this.mPhotoEditorView.setIcons(Arrays.asList(bitmapStickerIcon, bitmapStickerIcon2, bitmapStickerIcon3));
        this.mPhotoEditorView.setBackgroundColor(-16777216);
        this.mPhotoEditorView.setLocked(false);
        this.mPhotoEditorView.setConstrained(true);
        this.mPhotoEditorView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            public void onStickerDragFinished(@NonNull Sticker sticker) {
            }

            public void onStickerFlipped(@NonNull Sticker sticker) {
            }

            public void onStickerTouchedDown(@NonNull Sticker sticker) {
            }

            public void onStickerZoomFinished(@NonNull Sticker sticker) {
            }

            public void onTouchDownForBeauty(float f, float f2) {
            }

            public void onTouchDragForBeauty(float f, float f2) {
            }

            public void onTouchUpForBeauty(float f, float f2) {
            }

            public void onStickerAdded(@NonNull Sticker sticker) {
                EditImageActivity.this.stickerAlpha.setVisibility(View.VISIBLE);
                EditImageActivity.this.stickerAlpha.setProgress(sticker.getAlpha());
            }

            public void onStickerClicked(@NonNull Sticker sticker) {

                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    if (sticker instanceof TextSticker) {
                        ((TextSticker) sticker).setTextColor(SupportMenu.CATEGORY_MASK);
                        EditImageActivity.this.mPhotoEditorView.replace(sticker);
                        EditImageActivity.this.mPhotoEditorView.invalidate();
                    }
                    EditImageActivity.this.stickerAlpha.setVisibility(View.VISIBLE);
                    EditImageActivity.this.stickerAlpha.setProgress(sticker.getAlpha());
                    return null;
                });


            }

            public void onStickerDeleted(@NonNull Sticker sticker) {
                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    EditImageActivity.this.stickerAlpha.setVisibility(View.GONE);
                    return null;
                });

            }

            public void onStickerTouchOutside() {
                EditImageActivity.this.stickerAlpha.setVisibility(View.GONE);
            }

            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
                if (sticker instanceof TextSticker) {
                    sticker.setShow(false);
                    EditImageActivity.this.mPhotoEditorView.setHandlingSticker(null);
                    EditImageActivity.this.textEditorDialogFragment = TextEditorDialogFragment222.show(EditImageActivity.this, ((TextSticker) sticker).getAddTextProperties());
                    EditImageActivity.this.textEditor = new TextEditorDialogFragment222.TextEditor() {
                        public void onDone(AddTextProperties addTextProperties) {
                            EditImageActivity.this.mPhotoEditorView.getStickers().remove(EditImageActivity.this.mPhotoEditorView.getLastHandlingSticker());
                            EditImageActivity.this.mPhotoEditorView.addSticker(new TextSticker(EditImageActivity.this, addTextProperties));
                        }

                        public void onBackButton() {
                            EditImageActivity.this.mPhotoEditorView.showLastHandlingSticker();
                        }
                    };
                    EditImageActivity.this.textEditorDialogFragment.setOnTextEditorListener(EditImageActivity.this.textEditor);
                }
            }
        });
        this.filterIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                EditImageActivity.this.mPhotoEditorView.setFilterIntensity(((float) i) / 100.0f);
            }
        });
        this.overlayIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                EditImageActivity.this.mPhotoEditorView.setFilterIntensity(((float) i) / 100.0f);
            }
        });
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        viewPager.setAdapter(new PagerAdapter() {
            public int getCount() {
                return 7;
            }

            public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
                return view.equals(obj);
            }


            @Override
            public void destroyItem(@NonNull @NotNull ViewGroup container, int position, @NonNull @NotNull Object object) {
                (container).removeView((View) object);
            }

            @NonNull
            public Object instantiateItem(@NonNull ViewGroup viewGroup, int i) {
                View inflate = LayoutInflater.from(EditImageActivity.this.getBaseContext()).inflate(R.layout.sticker_items, null, false);
                RecyclerView recyclerView = inflate.findViewById(R.id.rv);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(EditImageActivity.this.getApplicationContext(), 4));
                switch (i) {
                    case 0:
                        recyclerView.setAdapter(new StickerAdapter(EditImageActivity.this, AssetUtils.lstCatFaces(), i, EditImageActivity.this));
                        break;
                    case 1:
                        recyclerView.setAdapter(new StickerAdapter(EditImageActivity.this, AssetUtils.lstCkeeks(), i, EditImageActivity.this));
                        break;
                    case 2:
                        recyclerView.setAdapter(new StickerAdapter(EditImageActivity.this, AssetUtils.lstGlasses(), i, EditImageActivity.this));
                        break;
                    case 3:
                        recyclerView.setAdapter(new StickerAdapter(EditImageActivity.this, AssetUtils.lstHairFaces(), i, EditImageActivity.this));
                        break;
                    case 4:
                        recyclerView.setAdapter(new StickerAdapter(EditImageActivity.this, AssetUtils.lstEmoj(), i, EditImageActivity.this));
                        break;
                    case 5:
                        recyclerView.setAdapter(new StickerAdapter(EditImageActivity.this, AssetUtils.lstBeards(), i, EditImageActivity.this));
                        break;
                    case 6:
                        recyclerView.setAdapter(new StickerAdapter(EditImageActivity.this, AssetUtils.lstTatoos(), i, EditImageActivity.this));
                        break;
                }
                viewGroup.addView(inflate);
                return inflate;
            }
        });
        RecyclerTabLayout recyclerTabLayout = findViewById(R.id.recycler_tab_layout);
        recyclerTabLayout.setUpWithAdapter(new TopTabEditAdapter(viewPager, getApplicationContext()));
        recyclerTabLayout.setPositionThreshold(0.5f);
        recyclerTabLayout.setIndicatorColor(PhotoApp.resources.getColor(R.color.blue_selected));
        recyclerTabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
    }

    public void showLoading(boolean z) {
        if (z) {
            getWindow().setFlags(16, 16);
            this.loadingView.setVisibility(View.VISIBLE);
            return;
        }
        getWindow().clearFlags(16);
        this.loadingView.setVisibility(View.GONE);
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
    }

    public void onAddViewListener(ViewType viewType, int i) {
        Log.d(TAG, "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + i + "]");
    }

    public void onRemoveViewListener(int i) {
        Log.d(TAG, "onRemoveViewListener() called with: numberOfAddedViews = [" + i + "]");
    }

    public void onRemoveViewListener(ViewType viewType, int i) {
        Log.d(TAG, "onRemoveViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + i + "]");
    }

    public void onStartViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    public void onStopViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgCloseAdjust:
            case R.id.imgCloseBrush:
            case R.id.imgCloseFilter:
            case R.id.imgCloseOverlay:
            case R.id.imgCloseSticker:
            case R.id.imgCloseText:
//                if (MyConstant.INSTANCE.getEXIT_INTERSTITIAL()) {
//                    MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                        slideDownSaveView();
                        onBackPressed();
//                        return null;
//                    });
//                }else{
//                    slideDownSaveView();
//                    onBackPressed();
//                }
                return;
            case R.id.imgSaveAdjust:

                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    EditImageActivity.this.compareAdjust.setVisibility(View.GONE);
                    slideDown(EditImageActivity.this.adjustLayout);
                    slideUp(EditImageActivity.this.mRvTools);
                    slideDownSaveView();
                    EditImageActivity.this.currentMode = ToolType.NONE;
                    return null;
                });

                return;
            case R.id.imgSaveBrush:
                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    showLoading(true);
                    runOnUiThread(() -> {
                        mPhotoEditor.setBrushDrawingMode(false);
                        undo.setVisibility(View.GONE);
                        redo.setVisibility(View.GONE);
                        erase.setVisibility(View.GONE);
                        slideDown(brushLayout);
                        slideUp(mRvTools);
                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(mRootView);
                        if (!SharePreferenceUtil.isPurchased(getApplicationContext())) {
                            constraintSet.connect(wrapPhotoView.getId(), 3, mRootView.getId(), 3, SystemUtil.dpToPx(getApplicationContext(), 50));
                        } else {
                            constraintSet.connect(wrapPhotoView.getId(), 3, mRootView.getId(), 3, 0);
                        }
                        constraintSet.connect(wrapPhotoView.getId(), 1, mRootView.getId(), 1, 0);
                        constraintSet.connect(wrapPhotoView.getId(), 4, mRvTools.getId(), 3, 0);
                        constraintSet.connect(wrapPhotoView.getId(), 2, mRootView.getId(), 2, 0);
                        constraintSet.applyTo(mRootView);
                        mPhotoEditorView.setImageSource(mPhotoEditor.getBrushDrawingView().getDrawBitmap(mPhotoEditorView.getCurrentBitmap()));
                        mPhotoEditor.clearBrushAllViews();
                        showLoading(false);
                        updateLayout();
                    });
                    slideDownSaveView();
                    EditImageActivity.this.currentMode = ToolType.NONE;
                    return null;
                });

                return;
            case R.id.imgSaveFilter:

                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    new SaveFilterAsBitmap().execute();
                    EditImageActivity.this.compareFilter.setVisibility(View.GONE);
                    slideDown(EditImageActivity.this.filterLayout);
                    slideUp(EditImageActivity.this.mRvTools);
                    slideDownSaveView();
                    EditImageActivity.this.currentMode = ToolType.NONE;
                    return null;
                });

                return;
            case R.id.imgSaveOverlay:

                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    new SaveFilterAsBitmap().execute();
                    slideDown(EditImageActivity.this.overlayLayout);
                    slideUp(EditImageActivity.this.mRvTools);
                    EditImageActivity.this.compareOverlay.setVisibility(View.GONE);
                    slideDownSaveView();
                    EditImageActivity.this.currentMode = ToolType.NONE;
                    return null;
                });

                return;
            case R.id.imgSaveSticker:
                //   new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                                @Override
//                                public void run() {

                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    EditImageActivity.this.mPhotoEditorView.setHandlingSticker(null);
                    EditImageActivity.this.mPhotoEditorView.setLocked(true);
                    EditImageActivity.this.stickerAlpha.setVisibility(View.GONE);
                    EditImageActivity.this.addNewSticker.setVisibility(View.GONE);

                    if (!EditImageActivity.this.mPhotoEditorView.getStickers().isEmpty()) {
                        new SaveStickerAsBitmap().execute();
                    }
                    slideUp(EditImageActivity.this.wrapStickerList);
                    slideDown(EditImageActivity.this.stickerLayout);
                    slideUp(EditImageActivity.this.mRvTools);
                    slideDownSaveView();
                    EditImageActivity.this.currentMode = ToolType.NONE;
                    return null;
                });

//                                }
//                            },700);


                return;
            case R.id.imgSaveText:

                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    EditImageActivity.this.mPhotoEditorView.setHandlingSticker(null);
                    EditImageActivity.this.mPhotoEditorView.setLocked(true);
                    EditImageActivity.this.addNewText.setVisibility(View.GONE);
                    if (!EditImageActivity.this.mPhotoEditorView.getStickers().isEmpty()) {
                        new SaveStickerAsBitmap().execute();
                    }
                    slideDown(EditImageActivity.this.textLayout);
                    slideUp(EditImageActivity.this.mRvTools);
                    slideDownSaveView();
                    EditImageActivity.this.currentMode = ToolType.NONE;
                    return null;
                });


                return;
            case R.id.redo:
                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    EditImageActivity.this.mPhotoEditor.redoBrush();
                    return null;
                });


                return;
            case R.id.undo:
                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    EditImageActivity.this.mPhotoEditor.undoBrush();
                    return null;
                });

                return;
            default:
        }
    }


    public void onPause() {
        super.onPause();

//        this.keyboardHeightProvider.onPause();
    }

    public void onResume() {
        super.onResume();

        Log.i("RemoteVallue", "onClick: "+MyConstant.INSTANCE.getTapCount());
//        this.keyboardHeightProvider.onResume();

        //MaxAdBanner>>done
        if (!SharePreferenceUtil.isPurchased(EditImageActivity.this)) {
            mainConaitner.setVisibility(View.VISIBLE);
            MaxAdManager.INSTANCE.createBannerAd(this, mainConaitner,app_ad , tvLoading, false, new BannerAdListener() {
                @Override
                public void bannerAdLoaded(boolean isLoad) {
                }
            });
        }else{
            mainConaitner.setVisibility(View.GONE);
        }

    }

    public void isPermissionGranted(boolean z, String str) {
        if (z) {
            new SaveBitmapAsFile().execute();
        }
    }

    public void onFilterSelected(String str) {
        this.mPhotoEditor.setFilterEffect(str);
        this.filterIntensity.setProgress(100);
        this.overlayIntensity.setProgress(70);
        if (this.currentMode == ToolType.OVERLAY) {
            this.mPhotoEditorView.getGLSurfaceView().setFilterIntensity(0.7f);
        }
    }


    public void openTextFragment() {
        this.textEditorDialogFragment = TextEditorDialogFragment222.show(this);
        this.textEditor = new TextEditorDialogFragment222.TextEditor() {
            public void onDone(AddTextProperties addTextProperties) {
                EditImageActivity.this.mPhotoEditorView.addSticker(new TextSticker(EditImageActivity.this.getApplicationContext(), addTextProperties));
            }

            public void onBackButton() {
                if (EditImageActivity.this.mPhotoEditorView.getStickers().isEmpty()) {
                    EditImageActivity.this.onBackPressed();
                }
            }
        };
        this.textEditorDialogFragment.setOnTextEditorListener(this.textEditor);
    }

    public void onToolSelected(ToolType toolType) {
        this.currentMode = toolType;
        switch (toolType) {
            case BRUSH:

//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                    showColorBrush();
                    this.mPhotoEditor.setBrushDrawingMode(true);
                    slideDown(this.mRvTools);
                    slideUp(this.brushLayout);
                    slideUpSaveControl();
                    toogleDrawBottomToolbar(true);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(this.mRootView);
                    if (!SharePreferenceUtil.isPurchased(getApplicationContext())) {
                        constraintSet.connect(this.wrapPhotoView.getId(), 3, this.mRootView.getId(), 3, SystemUtil.dpToPx(getApplicationContext(), 50));
                    } else {
                        constraintSet.connect(this.wrapPhotoView.getId(), 3, this.mRootView.getId(), 3, 0);
                    }
                    constraintSet.connect(this.wrapPhotoView.getId(), 1, this.mRootView.getId(), 1, 0);
                    constraintSet.connect(this.wrapPhotoView.getId(), 4, this.brushLayout.getId(), 3, 0);
                    constraintSet.connect(this.wrapPhotoView.getId(), 2, this.mRootView.getId(), 2, 0);
                    constraintSet.applyTo(this.mRootView);
                    this.mPhotoEditor.setBrushMode(1);
                    updateLayout();
//                    return null;
//                });

                break;
//            case TEXT:
//                slideUpSaveView();
//                this.mPhotoEditorView.setLocked(false);
//                openTextFragment();
//                slideDown(this.mRvTools);
//                slideUp(this.textLayout);
//                this.addNewText.setVisibility(View.VISIBLE);
//                break;
            case ADJUST:
//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()-> {
                    slideUpSaveView();
                    this.compareAdjust.setVisibility(View.VISIBLE);
                    this.mAdjustAdapter = new AdjustAdapter(getApplicationContext(), this, EditImageActivity.this);
                    this.mRvAdjust.setAdapter(this.mAdjustAdapter);
                    this.mAdjustAdapter.setSelectedAdjust(0);
                    this.mPhotoEditor.setAdjustFilter(this.mAdjustAdapter.getFilterConfig());
                    slideUp(this.adjustLayout);
                    slideDown(this.mRvTools);

//                    return null;
//                });
                break;
            case FILTER:
//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()-> {
                    slideUpSaveView();
                    new LoadFilterBitmap().execute();

//                    return null;
//                });
                break;
            case STICKER:
//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()-> {
                    slideUpSaveView();
                    this.mPhotoEditorView.setLocked(false);
                    slideDown(this.mRvTools);
                    slideUp(this.stickerLayout);

//                    return null;
//                });
                break;
            case OVERLAY:
//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()-> {
                    slideUpSaveView();
                    new LoadOverlayBitmap().execute();

//                    return null;
//                });
                break;
            case INSTA:
//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()-> {
                    new ShowInstaDialog().execute();

//                    return null;
//                });
                break;
            case SPLASH:
//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()-> {
                    new ShowSplashDialog(true).execute();

//                    return null;
//                });
                break;
            case BLUR:
//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()-> {
                    new ShowSplashDialog(false).execute();

//                    return null;
//                });
                break;
//            case MOSAIC:
//                new ShowMosaicDialog().execute();
//                break;
            case COLOR:
//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()-> {
                    ColorSplashDialog.show(this, this.mPhotoEditorView.getCurrentBitmap());

//                    return null;
//                });
                break;
            case CROP:
//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()-> {
                    CropDialogFragment.show(this, this, this.mPhotoEditorView.getCurrentBitmap());

//                    return null;
//                });
                break;
            case BEAUTY:
//                MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()-> {
                    BeautyDialog.show(this, this.mPhotoEditorView.getCurrentBitmap(), this);

//                    return null;
//                });
                break;
        }
        this.mPhotoEditorView.setHandlingSticker(null);
    }

    public void slideUp(View view) {
        ObjectAnimator.ofFloat(view, "translationY", (float) view.getHeight(), 0.0f).start();
    }

    public void slideUpSaveView() {
        this.saveControl.setVisibility(View.INVISIBLE);
    }

    public void slideUpSaveControl() {
        this.saveControl.setVisibility(View.GONE);
    }

    public void slideDownSaveControl() {
        this.saveControl.setVisibility(View.VISIBLE);
    }

    public void slideDownSaveView() {
        this.saveControl.setVisibility(View.VISIBLE);
    }

    public void slideDown(View view) {
        ObjectAnimator.ofFloat(view, "translationY", 0.0f, (float) view.getHeight()).start();
    }

    public void onBackPressed() {
        MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
            if (this.currentMode != null) {
                try {

                    switch (this.currentMode) {
                        case BRUSH:
                            slideDown(EditImageActivity.this.brushLayout);
                            slideUp(EditImageActivity.this.mRvTools);
                            slideDownSaveControl();
                            EditImageActivity.this.undo.setVisibility(View.GONE);
                            EditImageActivity.this.redo.setVisibility(View.GONE);
                            EditImageActivity.this.erase.setVisibility(View.GONE);
                            EditImageActivity.this.mPhotoEditor.setBrushDrawingMode(false);
                            ConstraintSet constraintSet = new ConstraintSet();
                            constraintSet.clone(EditImageActivity.this.mRootView);
                            if (!SharePreferenceUtil.isPurchased(getApplicationContext())) {
                                constraintSet.connect(EditImageActivity.this.wrapPhotoView.getId(), 3, EditImageActivity.this.mRootView.getId(), 3, SystemUtil.dpToPx(getApplicationContext(), 50));
                            } else {
                                constraintSet.connect(EditImageActivity.this.wrapPhotoView.getId(), 3, EditImageActivity.this.mRootView.getId(), 3, 0);
                            }
                            constraintSet.connect(EditImageActivity.this.wrapPhotoView.getId(), 1, EditImageActivity.this.mRootView.getId(), 1, 0);
                            constraintSet.connect(EditImageActivity.this.wrapPhotoView.getId(), 4, EditImageActivity.this.mRvTools.getId(), 3, 0);
                            constraintSet.connect(EditImageActivity.this.wrapPhotoView.getId(), 2, EditImageActivity.this.mRootView.getId(), 2, 0);
                            constraintSet.applyTo(EditImageActivity.this.mRootView);
                            EditImageActivity.this.mPhotoEditor.clearBrushAllViews();
                            slideDownSaveView();
                            EditImageActivity.this.currentMode = ToolType.NONE;
                            updateLayout();

                            return null;
                        case TEXT:
                            if (!EditImageActivity.this.mPhotoEditorView.getStickers().isEmpty()) {
                                EditImageActivity.this.mPhotoEditorView.getStickers().clear();
                                EditImageActivity.this.mPhotoEditorView.setHandlingSticker(null);
                            }
                            slideDown(EditImageActivity.this.textLayout);
                            EditImageActivity.this.addNewText.setVisibility(View.GONE);
                            EditImageActivity.this.mPhotoEditorView.setHandlingSticker(null);
                            slideUp(EditImageActivity.this.mRvTools);
                            EditImageActivity.this.mPhotoEditorView.setLocked(true);
                            slideDownSaveView();
                            EditImageActivity.this.currentMode = ToolType.NONE;

                            return null;
                        case ADJUST:
                            EditImageActivity.this.mPhotoEditor.setFilterEffect("");
                            EditImageActivity.this.compareAdjust.setVisibility(View.GONE);
                            slideDown(EditImageActivity.this.adjustLayout);
                            slideUp(EditImageActivity.this.mRvTools);
                            slideDownSaveView();
                            EditImageActivity.this.currentMode = ToolType.NONE;

                            return null;
                        case FILTER:
                            slideDown(EditImageActivity.this.filterLayout);
                            slideUp(EditImageActivity.this.mRvTools);
                            slideDownSaveView();
                            EditImageActivity.this.mPhotoEditor.setFilterEffect("");
                            EditImageActivity.this.compareFilter.setVisibility(View.GONE);
                            EditImageActivity.this.lstBitmapWithFilter.clear();
                            if (EditImageActivity.this.mRvFilters.getAdapter() != null) {
                                EditImageActivity.this.mRvFilters.getAdapter().notifyDataSetChanged();
                            }
                            EditImageActivity.this.currentMode = ToolType.NONE;

                            return null;
                        case STICKER:
                            if (EditImageActivity.this.mPhotoEditorView.getStickers().size() <= 0) {
                                slideUp(EditImageActivity.this.wrapStickerList);
                                slideDown(EditImageActivity.this.stickerLayout);
                                EditImageActivity.this.addNewSticker.setVisibility(View.GONE);
                                EditImageActivity.this.mPhotoEditorView.setHandlingSticker(null);
                                slideUp(EditImageActivity.this.mRvTools);
                                EditImageActivity.this.mPhotoEditorView.setLocked(true);
                                EditImageActivity.this.currentMode = ToolType.NONE;
                            } else if (EditImageActivity.this.addNewSticker.getVisibility() == View.VISIBLE) {
                                EditImageActivity.this.mPhotoEditorView.getStickers().clear();
                                EditImageActivity.this.addNewSticker.setVisibility(View.GONE);
                                EditImageActivity.this.mPhotoEditorView.setHandlingSticker(null);
                                slideUp(EditImageActivity.this.wrapStickerList);
                                slideDown(EditImageActivity.this.stickerLayout);
                                slideUp(EditImageActivity.this.mRvTools);
                                EditImageActivity.this.currentMode = ToolType.NONE;
                            } else {
                                slideDown(EditImageActivity.this.wrapStickerList);
                                EditImageActivity.this.addNewSticker.setVisibility(View.VISIBLE);
                            }
                            slideDownSaveView();

                            return null;
                        case OVERLAY:
                            EditImageActivity.this.mPhotoEditor.setFilterEffect("");
                            EditImageActivity.this.compareOverlay.setVisibility(View.GONE);
                            EditImageActivity.this.lstBitmapWithOverlay.clear();
                            slideUp(EditImageActivity.this.mRvTools);
                            slideDown(EditImageActivity.this.overlayLayout);
                            slideDownSaveView();
                            EditImageActivity.this.mRvOverlays.getAdapter().notifyDataSetChanged();
                            EditImageActivity.this.currentMode = ToolType.NONE;

                            return null;
                        case SPLASH:
                        case BLUR:
                        case MOSAIC:
                        case CROP:
                        case BEAUTY:
                        case NONE:

                            showDiscardDialog();
                            return null;
                        default:
                            EditImageActivity.super.onBackPressed();
//                        MyConstant.INSTANCE.getEXIT_INTERSTITIAL()
                            //MaxAdInterShow>>done
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            return null;
        });

    }

    private void showDiscardDialog() {
        new AlertDialog.Builder(this, R.style.AlertDialogCustom).setMessage(R.string.dialog_discard_title).setPositiveButton(R.string.discard, (dialogInterface, i) -> {

            MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
                EditImageActivity.this.currentMode = null;
                EditImageActivity.this.finish();

                return null;
            });


        }).setNegativeButton("Cancel", (dialogInterface, i) -> {
            MaxAdManager.INSTANCE.checkTap(EditImageActivity.this,()->{
            dialogInterface.dismiss();
                return null;
            });

        }).create().show();


    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onAdjustSelected(AdjustAdapter.AdjustModel adjustModel) {
        Log.d("XXXXXXXX", "onAdjustSelected " + adjustModel.slierIntensity + " " + this.adjustSeekBar.getMax());
        this.adjustSeekBar.setProgress((int) (adjustModel.slierIntensity * ((float) this.adjustSeekBar.getMax())));
    }

    public void addSticker(Bitmap bitmap) {
        this.mPhotoEditorView.addSticker(new DrawableSticker(new BitmapDrawable(getResources(), bitmap)));
        slideDown(this.wrapStickerList);
        this.addNewSticker.setVisibility(View.VISIBLE);
    }

    public void finishCrop(Bitmap bitmap) {
        this.mPhotoEditorView.setImageSource(bitmap);
        this.currentMode = ToolType.NONE;
        updateLayout();
    }

    public void onColorChanged(String str) {
        this.mPhotoEditor.setBrushColor(Color.parseColor(str));
    }

    public void instaSavedBitmap(Bitmap bitmap) {
        this.mPhotoEditorView.setImageSource(bitmap);
        this.currentMode = ToolType.NONE;
        updateLayout();
    }

    public void onMagicChanged(DrawBitmapModel drawBitmapModel) {
        this.mPhotoEditor.setBrushMagic(drawBitmapModel);
    }

    public void onSaveSplash(Bitmap bitmap) {
        this.mPhotoEditorView.setImageSource(bitmap);
        this.currentMode = ToolType.NONE;
    }

    public void onSaveMosaic(Bitmap bitmap) {
        this.mPhotoEditorView.setImageSource(bitmap);
        this.currentMode = ToolType.NONE;
    }

    public void onBeautySave(Bitmap bitmap) {
        this.mPhotoEditorView.setImageSource(bitmap);
        this.currentMode = ToolType.NONE;
    }

    class LoadFilterBitmap extends AsyncTask<Void, Void, Void> {
        LoadFilterBitmap() {
        }


        public void onPreExecute() {
            EditImageActivity.this.showLoading(true);
        }


        public Void doInBackground(Void... voidArr) {
            EditImageActivity.this.lstBitmapWithFilter.clear();
            EditImageActivity.this.lstBitmapWithFilter.addAll(FilterUtils.getLstBitmapWithFilter(ThumbnailUtils.extractThumbnail(EditImageActivity.this.mPhotoEditorView.getCurrentBitmap(), 100, 100), EditImageActivity.this));

            return null;
        }


        public void onPostExecute(Void voidR) {
            EditImageActivity.this.mRvFilters.setAdapter(new FilterViewAdapter(EditImageActivity.this.lstBitmapWithFilter, EditImageActivity.this, EditImageActivity.this.getApplicationContext(), Arrays.asList(FilterUtils.EFFECT_CONFIGS)));
            EditImageActivity.this.slideDown(EditImageActivity.this.mRvTools);
            EditImageActivity.this.slideUp(EditImageActivity.this.filterLayout);
            EditImageActivity.this.compareFilter.setVisibility(View.VISIBLE);
            EditImageActivity.this.filterIntensity.setProgress(80);
            EditImageActivity.this.showLoading(false);
        }
    }

    class LoadOverlayBitmap extends AsyncTask<Void, Void, Void> {
        LoadOverlayBitmap() {
        }


        public void onPreExecute() {
            EditImageActivity.this.showLoading(true);
        }


        public Void doInBackground(Void... voidArr) {
            EditImageActivity.this.lstBitmapWithOverlay.clear();
            EditImageActivity.this.lstBitmapWithOverlay.addAll(FilterUtils.getLstBitmapWithOverlay(ThumbnailUtils.extractThumbnail(EditImageActivity.this.mPhotoEditorView.getCurrentBitmap(), 100, 100), EditImageActivity.this));
            return null;
        }


        public void onPostExecute(Void voidR) {
            EditImageActivity.this.mRvOverlays.setAdapter(new FilterViewAdapter(EditImageActivity.this.lstBitmapWithOverlay, EditImageActivity.this, EditImageActivity.this.getApplicationContext(), Arrays.asList(FilterUtils.OVERLAY_CONFIG)));
            EditImageActivity.this.slideDown(EditImageActivity.this.mRvTools);
            EditImageActivity.this.slideUp(EditImageActivity.this.overlayLayout);
            EditImageActivity.this.compareOverlay.setVisibility(View.VISIBLE);
            EditImageActivity.this.overlayIntensity.setProgress(100);
            EditImageActivity.this.showLoading(false);
        }
    }


    class ShowInstaDialog extends AsyncTask<Void, Bitmap, Bitmap> {
        ShowInstaDialog() {
        }


        public void onPreExecute() {
            EditImageActivity.this.showLoading(true);
        }


        public Bitmap doInBackground(Void... voidArr) {
            return FilterUtils.getBlurImageFromBitmap(EditImageActivity.this.mPhotoEditorView.getCurrentBitmap(), 5.0f);

        }


        public void onPostExecute(Bitmap bitmap) {
            EditImageActivity.this.showLoading(false);
            InstaDialog.show(EditImageActivity.this, EditImageActivity.this,
                    EditImageActivity.this.mPhotoEditorView.getCurrentBitmap(), bitmap);
        }
    }

    class ShowMosaicDialog extends AsyncTask<Void, List<Bitmap>, List<Bitmap>> {
        ShowMosaicDialog() {
        }


        public void onPreExecute() {
            EditImageActivity.this.showLoading(true);
        }


        public List<Bitmap> doInBackground(Void... voidArr) {
            List<Bitmap> arrayList = new ArrayList<>();
            arrayList.add(FilterUtils.cloneBitmap(EditImageActivity.this.mPhotoEditorView.getCurrentBitmap()));
            arrayList.add(FilterUtils.getBlurImageFromBitmap(EditImageActivity.this.mPhotoEditorView.getCurrentBitmap(), 8.0f));
            return arrayList;
        }


        public void onPostExecute(List<Bitmap> list) {
            EditImageActivity.this.showLoading(false);
            MosaicDialog.show(EditImageActivity.this, list.get(0), list.get(1), EditImageActivity.this);
        }
    }


    class ShowSplashDialog extends AsyncTask<Void, List<Bitmap>, List<Bitmap>> {
        boolean isSplash;

        public ShowSplashDialog(boolean z) {
            this.isSplash = z;
        }


        public void onPreExecute() {
            EditImageActivity.this.showLoading(true);
        }


        public List<Bitmap> doInBackground(Void... voidArr) {
            Bitmap currentBitmap = EditImageActivity.this.mPhotoEditorView.getCurrentBitmap();
            List<Bitmap> arrayList = new ArrayList<>();
            arrayList.add(currentBitmap);
            if (this.isSplash) {
                arrayList.add(FilterUtils.getBlackAndWhiteImageFromBitmap(currentBitmap));
            } else {

                arrayList.add(FilterUtils.getBlurImageFromBitmap(currentBitmap, 3.0f));
            }
            return arrayList;
        }


        public void onPostExecute(List<Bitmap> list) {
            if (this.isSplash) {
                SplashDialog.show(EditImageActivity.this, list.get(0), null, list.get(1), EditImageActivity.this, true);
            } else {
                SplashDialog.show(EditImageActivity.this, list.get(0), list.get(1), null, EditImageActivity.this, false);
            }
            EditImageActivity.this.showLoading(false);
        }
    }

    class SaveFilterAsBitmap extends AsyncTask<Void, Void, Bitmap> {
        SaveFilterAsBitmap() {
        }


        public void onPreExecute() {
            EditImageActivity.this.showLoading(true);
        }


        public Bitmap doInBackground(Void... voidArr) {
            final Bitmap[] bitmapArr = {null};
            EditImageActivity.this.mPhotoEditorView.saveGLSurfaceViewAsBitmap(bitmap -> bitmapArr[0] = bitmap);
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
            EditImageActivity.this.mPhotoEditorView.setImageSource(bitmap);
            EditImageActivity.this.mPhotoEditorView.setFilterEffect("");
            EditImageActivity.this.showLoading(false);
        }
    }

    class SaveStickerAsBitmap extends AsyncTask<Void, Void, Bitmap> {
        SaveStickerAsBitmap() {
        }


        public void onPreExecute() {
            EditImageActivity.this.mPhotoEditorView.getGLSurfaceView().setAlpha(0.0f);
            EditImageActivity.this.showLoading(true);
        }


        public Bitmap doInBackground(Void... voidArr) {
            final Bitmap[] bitmapArr = {null};
            while (bitmapArr[0] == null) {
                try {
                    EditImageActivity.this.mPhotoEditor.saveStickerAsBitmap(bitmap -> bitmapArr[0] = bitmap);
                    while (bitmapArr[0] == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                }
            }
            return bitmapArr[0];
        }


        public void onPostExecute(Bitmap bitmap) {
            EditImageActivity.this.mPhotoEditorView.setImageSource(bitmap);
            EditImageActivity.this.mPhotoEditorView.getStickers().clear();
            EditImageActivity.this.mPhotoEditorView.getGLSurfaceView().setAlpha(1.0f);
            EditImageActivity.this.showLoading(false);
            EditImageActivity.this.updateLayout();
        }
    }


//    public void onActivityResult(int i, int i2, @Nullable Intent intent) {
//        super.onActivityResult(i, i2, intent);
//        if (i == 123) {
//            if (i2 == -1) {
//                try {
//                    InputStream openInputStream = getContentResolver().openInputStream(intent.getData());
//                    Bitmap decodeStream = BitmapFactory.decodeStream(openInputStream);
//                    float width = (float) decodeStream.getWidth();
//                    float height = (float) decodeStream.getHeight();
//                    float max = Math.max(width / 1280.0f, height / 1280.0f);
//                    if (max > 1.0f) {
//                        decodeStream = Bitmap.createScaledBitmap(decodeStream, (int) (width / max), (int) (height / max), false);
//                    }
//                    if (SystemUtil.rotateBitmap(decodeStream, new ExifInterface(openInputStream).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) != decodeStream) {
//                        decodeStream.recycle();
//                        decodeStream = null;
//                    }
//                    this.mPhotoEditorView.setImageSource(decodeStream);
//                    updateLayout();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    MsgUtil.toastMsg(this, "Error: Can not open image");
//                }
//            } else {
//                finish();
//            }
//        }
//    }

    class OnLoadBitmapFromUri extends AsyncTask<String, Bitmap, Bitmap> {
        OnLoadBitmapFromUri() {
        }


        public void onPreExecute() {
            EditImageActivity.this.showLoading(true);
        }


        public Bitmap doInBackground(String... strArr) {
            try {
                Uri fromFile = Uri.fromFile(new File(strArr[0]));

                ParcelFileDescriptor parcelFileDescriptor = EditImageActivity.this.getContentResolver().openFileDescriptor(fromFile, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();

//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(EditImageActivity.this.getContentResolver(), fromFile);
                float width = (float) bitmap.getWidth();
                float height = (float) bitmap.getHeight();
                float max = Math.max(width / 1024.0f, height / 1024.0f);
                if (max > 1.0f) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) (width / max), (int) (height / max), false);
                }
                Bitmap rotateBitmap = SystemUtil.rotateBitmap(bitmap, new ExifInterface(EditImageActivity.this.getContentResolver().openInputStream(fromFile)).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1));
                if (rotateBitmap != bitmap) {
                    bitmap.recycle();
                }
                return rotateBitmap;
            } catch (OutOfMemoryError | Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        public void onPostExecute(Bitmap bitmap) {
            EditImageActivity.this.mPhotoEditorView.setImageSource(bitmap);
            EditImageActivity.this.updateLayout();
        }
    }


    class OnLoadCGEF extends AsyncTask<String, Bitmap, Bitmap> {
        OnLoadCGEF() {
        }


        public void onPreExecute() {
            EditImageActivity.this.showLoading(true);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {

            CGENativeLibrary.setLoadImageCallback(EditImageActivity.this.mLoadImageCallback, null);

            return null;
        }


        public void onPostExecute(Bitmap bitmap) {
            Log.d("CGE Initiatlize", "CGE Initiatlize");
//            Toast.makeText(EditImageActivity.this,"CGE Initiatlize",Toast.LENGTH_LONG).show();
        }
    }


//    public void updateLayout() {
////        new UpdateLayoutStatus().execute();
//
//        this.mPhotoEditorView.postDelayed(() -> {
//            try {
//                Display defaultDisplay = EditImageActivity.this.getWindowManager().getDefaultDisplay();
//                Point point = new Point();
//                defaultDisplay.getSize(point);
//                int i = point.x;
//                int height = EditImageActivity.this.wrapPhotoView.getHeight();
//                int i2 = EditImageActivity.this.mPhotoEditorView.getGLSurfaceView().getRenderViewport().width;
//                float f = (float) EditImageActivity.this.mPhotoEditorView.getGLSurfaceView().getRenderViewport().height;
//                float f2 = (float) i2;
//                if (((int) ((((float) i) * f) / f2)) <= height) {
//                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -2);
//                    layoutParams.addRule(13);
//                    EditImageActivity.this.mPhotoEditorView.setLayoutParams(layoutParams);
//                    EditImageActivity.this.mPhotoEditorView.setVisibility(View.VISIBLE);
//                } else {
//                    RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams((int) ((((float) height) * f2) / f), -1);
//                    layoutParams2.addRule(13);
//                    EditImageActivity.this.mPhotoEditorView.setLayoutParams(layoutParams2);
//                    EditImageActivity.this.mPhotoEditorView.setVisibility(View.VISIBLE);
//                }
//            } catch (Exception ignored) {
//                ignored.printStackTrace();
//            }
//            EditImageActivity.this.showLoading(false);
//        }, 300);
//    }
public void updateLayout() {
    this.mPhotoEditorView.postDelayed(() -> {
        try {
            Display defaultDisplay = EditImageActivity.this.getWindowManager().getDefaultDisplay();
            Point point = new Point();
            defaultDisplay.getSize(point);
            int i = point.x;
            int height = EditImageActivity.this.wrapPhotoView.getHeight();
            int i2 = EditImageActivity.this.mPhotoEditorView.getGLSurfaceView().getRenderViewport().width;
            float f = (float) EditImageActivity.this.mPhotoEditorView.getGLSurfaceView().getRenderViewport().height;
            float f2 = (float) i2;
            if (((int) ((((float) i) * f) / f2)) <= height) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -2);
                layoutParams.addRule(13);
                EditImageActivity.this.mPhotoEditorView.setLayoutParams(layoutParams);
            } else {
                RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams((int) ((((float) height) * f2) / f), -1);
                layoutParams2.addRule(13);
                EditImageActivity.this.mPhotoEditorView.setLayoutParams(layoutParams2);
            }
            EditImageActivity.this.mPhotoEditorView.setVisibility(View.VISIBLE);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        EditImageActivity.this.showLoading(false);
    }, 300);
}

    class UpdateLayoutStatus extends AsyncTask<Void, String, String> {


        UpdateLayoutStatus() {
        }


        public void onPreExecute() {
            EditImageActivity.this.showLoading(true);
        }


        public String doInBackground(Void... voidArr) {

            try {
                Display defaultDisplay = EditImageActivity.this.getWindowManager().getDefaultDisplay();
                Point point = new Point();
                defaultDisplay.getSize(point);
                int i = point.x;
                int height = EditImageActivity.this.wrapPhotoView.getHeight();
                int i2 = EditImageActivity.this.mPhotoEditorView.getGLSurfaceView().getRenderViewport().width;
                float f = (float) EditImageActivity.this.mPhotoEditorView.getGLSurfaceView().getRenderViewport().height;
                float f2 = (float) i2;
                if (((int) ((((float) i) * f) / f2)) <= height) {
                    EditImageActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -2);
                            layoutParams.addRule(13);
                            EditImageActivity.this.mPhotoEditorView.setLayoutParams(layoutParams);
                            EditImageActivity.this.mPhotoEditorView.setVisibility(View.VISIBLE);
                        }
                    });

                } else {
                    EditImageActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams((int) ((((float) height) * f2) / f), -1);
                            layoutParams2.addRule(13);
                            EditImageActivity.this.mPhotoEditorView.setLayoutParams(layoutParams2);
                            EditImageActivity.this.mPhotoEditorView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

            return null;
        }

        public void onPostExecute(String str) {
            EditImageActivity.this.showLoading(false);
            Log.d("mission", "mission successful");
        }
    }


    class SaveBitmapAsFile extends AsyncTask<Void, String, String> {


        SaveBitmapAsFile() {
        }


        public void onPreExecute() {
            EditImageActivity.this.showLoading(true);
        }


        public String doInBackground(Void... voidArr) {
            String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
            File image;
            try {
                image = FileUtils.saveImage(EditImageActivity.this, EditImageActivity.this.mPhotoEditorView.getCurrentBitmap(), name);
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
                return null;
            }


            return image.getAbsolutePath();

        }

        public void onPostExecute(String str) {
            EditImageActivity.this.showLoading(false);

            if (str == null) {
                Toast.makeText(EditImageActivity.this.getApplicationContext(), "Oop! Something went wrong", Toast.LENGTH_LONG).show();
                return;
            }

//            if (MyConstant.INSTANCE.getSAVING_INTERSTITIAL()) {
//
//                //MaxAdInterShow>>done
//
//            }else{
                Intent intent = new Intent(EditImageActivity.this, SaveAndShareActivity.class);
                intent.putExtra("path", str);
                EditImageActivity.this.startActivity(intent);
//            }


        }
    }

    private void notifyMediaStoreScanner(Context mContext, File file) {
        try {
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
            mContext.sendBroadcast(new Intent(
                    "android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
//    @Override
//    public void onAdLoaded(MaxAd ad) {
//        // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'
//
//        // Reset retry attempt
//        retryAttempt = 0;
//    }
//
//    @Override
//    public void onAdDisplayed(MaxAd ad) {
//
//    }
//
//    @Override
//    public void onAdLoadFailed(String adUnitId, MaxError error) {
//        // Interstitial ad failed to load
//        // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)
//
////        retryAttempt++;
////        long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );
////
////        new Handler().postDelayed( new Runnable()
////        {
////            @Override
////            public void run()
////            {
////                interstitialAd.loadAd();
////            }
////        }, delayMillis );
//        retryAttempt++;
//        long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );
//
//        new Handler().postDelayed( new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                interstitialAd.loadAd();
//            }
//        }, delayMillis );
//
//    }
//
//    @Override
//    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
//        // Interstitial ad failed to display. We recommend loading the next ad
//        interstitialAd.loadAd();
//
//
//    }
//    @Override
//    public void onAdHidden(final MaxAd maxAd)
//    {
//        interstitialAd.loadAd();
//        // Interstitial ad is hidden. Pre-load the next ad
//    }
//
//    @Override
//    public void onAdClicked(MaxAd ad) {
//
//    }
}
