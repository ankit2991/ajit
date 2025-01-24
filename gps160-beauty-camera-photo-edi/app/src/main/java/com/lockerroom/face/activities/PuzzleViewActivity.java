package com.lockerroom.face.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hold1.keyboardheightprovider.KeyboardHeightProvider;
import com.lockerroom.face.Constants;
//import com.rebel.photoeditor.ads.FacebookAds;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.base.BaseActivity;
import com.lockerroom.face.features.addtext.AddTextProperties;
import com.lockerroom.face.features.addtext.TextEditorDialogFragment;
import com.lockerroom.face.features.crop.CropDialogFragment;
import com.lockerroom.face.features.crop.adapter.AspectRatioPreviewAdapter;
import com.lockerroom.face.features.picker.PhotoPicker;
import com.lockerroom.face.features.picker.utils.PermissionsUtils;
import com.lockerroom.face.features.puzzle.PuzzleLayout;
import com.lockerroom.face.features.puzzle.PuzzleLayoutParser;
import com.lockerroom.face.features.puzzle.PuzzlePiece;
import com.lockerroom.face.features.puzzle.PuzzleView;
import com.lockerroom.face.features.puzzle.adapter.PuzzleAdapter;
import com.lockerroom.face.features.puzzle.adapter.PuzzleBackgroundAdapter;
import com.lockerroom.face.features.puzzle.photopicker.activity.PickImageActivity;
import com.lockerroom.face.features.sticker.adapter.RecyclerTabLayout;
import com.lockerroom.face.features.sticker.adapter.StickerAdapter;
import com.lockerroom.face.features.sticker.adapter.TopTabAdapter;
import com.lockerroom.face.filters.FilterDialogFragment;
import com.lockerroom.face.filters.FilterListener;
import com.lockerroom.face.filters.FilterUtils;
import com.lockerroom.face.filters.FilterViewAdapter;
import com.lockerroom.face.firebaseAdsConfig.RemoteConfigData;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.BannerAdListener;
import com.lockerroom.face.maxAdManager.MaxAdConstants;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.maxAdManager.OnAdShowCallback;
import com.lockerroom.face.sticker.BitmapStickerIcon;
import com.lockerroom.face.sticker.DrawableSticker;
import com.lockerroom.face.sticker.Sticker;
import com.lockerroom.face.sticker.StickerView;
import com.lockerroom.face.sticker.TextSticker;
import com.lockerroom.face.sticker.event.DeleteIconEvent;
import com.lockerroom.face.sticker.event.FlipHorizontallyEvent;
import com.lockerroom.face.sticker.event.ZoomIconEvent;
import com.lockerroom.face.tools.EditingToolsAdapter;
import com.lockerroom.face.tools.PieceToolsAdapter;
import com.lockerroom.face.tools.ToolType;
import com.lockerroom.face.utils.AssetUtils;
import com.lockerroom.face.utils.FileUtils;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.PuzzleUtils;
import com.lockerroom.face.utils.SharePreferenceUtil;
import com.lockerroom.face.utils.SystemUtil;
import com.lockerroom.face.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.steelkiwi.cropiwa.AspectRatio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.wysaid.nativePort.CGENativeLibrary;

@SuppressLint("StaticFieldLeak")
public class PuzzleViewActivity extends BaseActivity implements EditingToolsAdapter.OnItemSelected, AspectRatioPreviewAdapter.OnNewSelectedListener, StickerAdapter.OnClickStickerListener, PuzzleBackgroundAdapter.BackgroundChangeListener, FilterListener, CropDialogFragment.OnCropPhoto, FilterDialogFragment.OnFilterSavePhoto, PieceToolsAdapter.OnPieceFuncItemSelected, PuzzleAdapter.OnItemClickListener {
    private static PuzzleViewActivity instance;
    public static PuzzleViewActivity puzzle;
    public ImageView addNewSticker;
    public ImageView addNewText;
    private View adsContainer;
    public ConstraintLayout changeBackgroundLayout;
    private LinearLayout changeBorder;
    public ConstraintLayout changeLayoutLayout;
    public AspectRatio currentAspect;
    public PuzzleBackgroundAdapter.SquareView currentBackgroundState;
    public ToolType currentMode;
    private int deviceHeight = 0;
    public int deviceWidth = 0;
    private static boolean initializedPicasso = false;
    public ConstraintLayout filterLayout;
    private KeyboardHeightProvider keyboardHeightProvider;
    private RelativeLayout loadingView;
    public List<Bitmap> lstBitmapWithFilter = new ArrayList<>();
    public List<Drawable> lstOriginalDrawable = new ArrayList<>();
    public List<String> lstPaths;
    private EditingToolsAdapter mEditingToolsAdapter;
    public CGENativeLibrary.LoadImageCallback mLoadImageCallback = new CGENativeLibrary.LoadImageCallback() {
        public Bitmap loadImage(String str, Object obj) {
            try {
                return BitmapFactory.decodeStream(PuzzleViewActivity.this.getAssets().open(str));
            } catch (IOException io) {
                return null;
            }
        }

        public void loadImageOK(Bitmap bitmap, Object obj) {
            bitmap.recycle();
        }
    };

    public RecyclerView mRvTools;
    private ConstraintLayout mainActivity;
    public View.OnClickListener onClickListener = view -> {
        switch (view.getId()) {
            case R.id.imgCloseBackground:
            case R.id.imgCloseFilter:
            case R.id.imgCloseLayout:
            case R.id.imgCloseSticker:
            case R.id.imgCloseText:
                PuzzleViewActivity.this.slideDownSaveView();
                PuzzleViewActivity.this.onBackPressed();
                return;
            case R.id.imgSaveBackground:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.slideDown(PuzzleViewActivity.this.changeBackgroundLayout);
                    PuzzleViewActivity.this.slideUp(PuzzleViewActivity.this.mRvTools);
                    PuzzleViewActivity.this.slideDownSaveView();
                    PuzzleViewActivity.this.showDownFunction();
                    PuzzleViewActivity.this.puzzleView.setLocked(true);
                    PuzzleViewActivity.this.puzzleView.setTouchEnable(true);
                    if (PuzzleViewActivity.this.puzzleView.getBackgroundResourceMode() == 0) {
                        PuzzleViewActivity.this.currentBackgroundState.isColor = true;
                        PuzzleViewActivity.this.currentBackgroundState.isBitmap = false;
                        PuzzleViewActivity.this.currentBackgroundState.drawableId = ((ColorDrawable) PuzzleViewActivity.this.puzzleView.getBackground()).getColor();
                        PuzzleViewActivity.this.currentBackgroundState.drawable = null;
                    } else if (PuzzleViewActivity.this.puzzleView.getBackgroundResourceMode() == 1) {
                        PuzzleViewActivity.this.currentBackgroundState.isColor = false;
                        PuzzleViewActivity.this.currentBackgroundState.isBitmap = false;
                        PuzzleViewActivity.this.currentBackgroundState.drawable = PuzzleViewActivity.this.puzzleView.getBackground();
                    } else {
                        PuzzleViewActivity.this.currentBackgroundState.isColor = false;
                        PuzzleViewActivity.this.currentBackgroundState.isBitmap = true;
                        PuzzleViewActivity.this.currentBackgroundState.drawable = PuzzleViewActivity.this.puzzleView.getBackground();
                    }
                    PuzzleViewActivity.this.currentMode = ToolType.NONE;
//                AdmobAds.showFullAds((AdmobAds.OnAdsCloseListener) null);

                    return null;
                });

                return;
            case R.id.imgSaveFilter:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.slideDown(PuzzleViewActivity.this.filterLayout);
                    PuzzleViewActivity.this.slideUp(PuzzleViewActivity.this.mRvTools);
                    PuzzleViewActivity.this.currentMode = ToolType.NONE;

                    return null;
                });

                return;
            case R.id.imgSaveLayout:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.slideUp(PuzzleViewActivity.this.mRvTools);
                    PuzzleViewActivity.this.slideDown(PuzzleViewActivity.this.changeLayoutLayout);
                    PuzzleViewActivity.this.slideDownSaveView();
                    PuzzleViewActivity.this.showDownFunction();
                    PuzzleViewActivity.this.puzzleLayout = PuzzleViewActivity.this.puzzleView.getPuzzleLayout();
                    PuzzleViewActivity.this.pieceBorderRadius = PuzzleViewActivity.this.puzzleView.getPieceRadian();
                    PuzzleViewActivity.this.piecePadding = PuzzleViewActivity.this.puzzleView.getPiecePadding();
                    PuzzleViewActivity.this.puzzleView.setLocked(true);
                    PuzzleViewActivity.this.puzzleView.setTouchEnable(true);
                    PuzzleViewActivity.this.currentAspect = PuzzleViewActivity.this.puzzleView.getAspectRatio();
                    PuzzleViewActivity.this.currentMode = ToolType.NONE;

                    return null;
                });


//                if ((IronSource.isInterstitialReady() || IronSourceAdsManager.INSTANCE.getMInterstitialAd() != null) && RemoteConfigData.INSTANCE.getAllAdsEnabled_()) {
//
//                    IronSourceAdsManager.INSTANCE.showInter(this, new IronSourceCallbacks() {
//                        @Override
//                        public void onSuccess() {
//
//                        }
//
//                        @Override
//                        public void onFail() {
//
//                        }
//                    });
//                }
                return;
            case R.id.imgSaveSticker:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.puzzleView.setHandlingSticker(null);
                    PuzzleViewActivity.this.stickerAlpha.setVisibility(View.GONE);
                    PuzzleViewActivity.this.addNewSticker.setVisibility(View.GONE);
                    PuzzleViewActivity.this.slideUp(PuzzleViewActivity.this.wrapStickerList);
                    PuzzleViewActivity.this.slideDown(PuzzleViewActivity.this.stickerLayout);
                    PuzzleViewActivity.this.slideUp(PuzzleViewActivity.this.mRvTools);
                    PuzzleViewActivity.this.slideDownSaveView();
                    PuzzleViewActivity.this.puzzleView.setLocked(true);
                    PuzzleViewActivity.this.puzzleView.setTouchEnable(true);
                    PuzzleViewActivity.this.currentMode = ToolType.NONE;

                    return null;
                });

//                if ((IronSource.isInterstitialReady() || IronSourceAdsManager.INSTANCE.getMInterstitialAd() != null) && RemoteConfigData.INSTANCE.getAllAdsEnabled_()) {
//
//                    IronSourceAdsManager.INSTANCE.showInter(this, new IronSourceCallbacks() {
//                        @Override
//                        public void onSuccess() {
//
//                        }
//
//                        @Override
//                        public void onFail() {
//
//                        }
//                    });
//                }

                return;
            case R.id.imgSaveText:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.puzzleView.setHandlingSticker(null);
                    PuzzleViewActivity.this.puzzleView.setLocked(true);
                    PuzzleViewActivity.this.addNewText.setVisibility(View.GONE);
                    PuzzleViewActivity.this.slideDown(PuzzleViewActivity.this.textLayout);
                    PuzzleViewActivity.this.slideUp(PuzzleViewActivity.this.mRvTools);
                    PuzzleViewActivity.this.slideDownSaveView();
                    PuzzleViewActivity.this.puzzleView.setLocked(true);
                    PuzzleViewActivity.this.puzzleView.setTouchEnable(true);
                    PuzzleViewActivity.this.currentMode = ToolType.NONE;

                    return null;
                });

                return;
            case R.id.tv_blur:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.selectBackgroundBlur();

                    return null;
                });

                return;
            case R.id.tv_change_border:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.selectBorderTool();

                    return null;
                });

//                AdmobAds.showFullAds((AdmobAds.OnAdsCloseListener) null);

                return;
            case R.id.tv_change_layout:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.selectLayoutTool();
                    return null;
                });

                return;
            case R.id.tv_change_ratio:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.selectRadiusTool();
                    return null;
                });

//                AdmobAds.showFullAds((AdmobAds.OnAdsCloseListener) null);

                return;
            case R.id.tv_color:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.selectBackgroundColorTab();
                    return null;
                });

                return;
            case R.id.tv_radian:
                MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                    PuzzleViewActivity.this.selectBackgroundGradientTab();
                    return null;
                });

                return;
            default:
        }
    };
    public SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar seekBar) {
            switch (seekBar.getId()) {
                case R.id.sk_border:
                    sbChangeBorderSize.getProgressDrawable().setColorFilter(Color.parseColor("#ccf1d2"), PorterDuff.Mode.MULTIPLY);
                    break;
                case R.id.sk_border_radius:
                    sbChangeBorderRadius.getProgressDrawable().setColorFilter(Color.parseColor("#ccf1d2"), PorterDuff.Mode.MULTIPLY);
                    break;
            }
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            switch (seekBar.getId()) {
                case R.id.sk_border:
                    PuzzleViewActivity.this.puzzleView.setPiecePadding((float) i);
                    break;
                case R.id.sk_border_radius:
                    PuzzleViewActivity.this.puzzleView.setPieceRadian((float) i);
                    break;
            }
            PuzzleViewActivity.this.puzzleView.invalidate();
        }
    };
    StickerView.OnStickerOperationListener onStickerOperationListener = new StickerView.OnStickerOperationListener() {
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
            PuzzleViewActivity.this.stickerAlpha.setVisibility(View.VISIBLE);
            PuzzleViewActivity.this.stickerAlpha.setProgress(sticker.getAlpha());
        }

        public void onStickerClicked(@NonNull Sticker sticker) {
            PuzzleViewActivity.this.stickerAlpha.setVisibility(View.VISIBLE);
            PuzzleViewActivity.this.stickerAlpha.setProgress(sticker.getAlpha());
        }

        public void onStickerDeleted(@NonNull Sticker sticker) {
            PuzzleViewActivity.this.stickerAlpha.setVisibility(View.GONE);
        }

        public void onStickerTouchOutside() {
            PuzzleViewActivity.this.stickerAlpha.setVisibility(View.GONE);
        }

        public void onStickerDoubleTapped(@NonNull Sticker sticker) {
            if (sticker instanceof TextSticker) {
                sticker.setShow(false);
                PuzzleViewActivity.this.puzzleView.setHandlingSticker(null);
                PuzzleViewActivity.this.textEditorDialogFragment = TextEditorDialogFragment.show(PuzzleViewActivity.this, ((TextSticker) sticker).getAddTextProperties());
                PuzzleViewActivity.this.textEditor = new TextEditorDialogFragment.TextEditor() {
                    public void onDone(AddTextProperties addTextProperties) {
                        PuzzleViewActivity.this.puzzleView.getStickers().remove(PuzzleViewActivity.this.puzzleView.getLastHandlingSticker());
                        PuzzleViewActivity.this.puzzleView.addSticker(new TextSticker(PuzzleViewActivity.this, addTextProperties));
                    }

                    public void onBackButton() {
                        PuzzleViewActivity.this.puzzleView.showLastHandlingSticker();
                    }
                };
                PuzzleViewActivity.this.textEditorDialogFragment.setOnTextEditorListener(PuzzleViewActivity.this.textEditor);
            }
        }
    };

    public float pieceBorderRadius;

    public float piecePadding;
    private PieceToolsAdapter pieceToolsAdapter;

    public PuzzleLayout puzzleLayout;
    private RecyclerView puzzleList;

    public PuzzleView puzzleView;
    private RecyclerView radiusLayout;
    private RecyclerView rvBackgroundBlur;
    private RecyclerView rvBackgroundColor;
    private RecyclerView rvBackgroundGradient;

    public RecyclerView rvFilterView;

    public RecyclerView rvPieceControl;
    private TextView saveBitmap;
    private ConstraintLayout saveControl;
    private SeekBar sbChangeBorderRadius;
    private SeekBar sbChangeBorderSize;

    public SeekBar stickerAlpha;

    public ConstraintLayout stickerLayout;

    public List<Target> targets = new ArrayList();

    public TextEditorDialogFragment.TextEditor textEditor;

    public TextEditorDialogFragment textEditorDialogFragment;

    public ConstraintLayout textLayout;
    private TextView tvChangeBackgroundBlur;
    private TextView tvChangeBackgroundColor;
    private TextView tvChangeBackgroundGradient;
    private TextView tvChangeBorder;
    private TextView tvChangeLayout;
    private TextView tvChangeRatio;
    private ConstraintLayout wrapPuzzleView;

    public LinearLayout wrapStickerList;

    public static PuzzleViewActivity getInstance() {
        return instance;
    }

    FrameLayout app_ad;
    TextView tvLoading;
    RelativeLayout mainConaitner;
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.puzzle_layout);
        if (Build.VERSION.SDK_INT < 26) {
            getWindow().setSoftInputMode(48);
        }
        this.adsContainer = findViewById(R.id.adsContainer);
        this.deviceWidth = getResources().getDisplayMetrics().widthPixels;
        this.deviceHeight = getResources().getDisplayMetrics().heightPixels;
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
//            findViewById(R.id.adsContainer).setVisibility(View.GONE);
        }

        initPicasso(this);
        findViewById(R.id.exitEditMode).setOnClickListener(view -> PuzzleViewActivity.this.onBackPressed());
        this.loadingView = findViewById(R.id.loadingView);
        this.puzzleView = findViewById(R.id.puzzle_view);
        this.wrapPuzzleView = findViewById(R.id.wrapPuzzleView);
        this.filterLayout = findViewById(R.id.filterLayout);
        this.rvFilterView = findViewById(R.id.rvFilterView);
        this.mRvTools = findViewById(R.id.rvConstraintTools);
        mEditingToolsAdapter = new EditingToolsAdapter(PuzzleViewActivity.this, this, true);
        this.mRvTools.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mRvTools.setAdapter(this.mEditingToolsAdapter);
        this.rvPieceControl = findViewById(R.id.rvPieceControl);
        pieceToolsAdapter = new PieceToolsAdapter(this, PuzzleViewActivity.this);
        this.rvPieceControl.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.rvPieceControl.setAdapter(this.pieceToolsAdapter);
        this.sbChangeBorderSize = findViewById(R.id.sk_border);
        this.sbChangeBorderSize.setOnSeekBarChangeListener(this.onSeekBarChangeListener);
        this.sbChangeBorderRadius = findViewById(R.id.sk_border_radius);
        this.sbChangeBorderRadius.setOnSeekBarChangeListener(this.onSeekBarChangeListener);
        this.lstPaths = getIntent().getStringArrayListExtra(PickImageActivity.KEY_DATA_RESULT);
        this.puzzleLayout = PuzzleUtils.getPuzzleLayouts(this.lstPaths.size()).get(0);
        this.puzzleView.setPuzzleLayout(this.puzzleLayout);
        this.puzzleView.setTouchEnable(true);
        this.puzzleView.setNeedDrawLine(false);
        this.puzzleView.setNeedDrawOuterLine(false);
        this.puzzleView.setLineSize(4);
        this.puzzleView.setPiecePadding(6.0f);
        this.puzzleView.setPieceRadian(15.0f);
        this.puzzleView.setLineColor(ContextCompat.getColor(this, R.color.white));
        this.puzzleView.setSelectedLineColor(ContextCompat.getColor(this, R.color.colorAccent));
        this.puzzleView.setHandleBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        this.puzzleView.setAnimateDuration(300);
        this.puzzleView.setOnPieceSelectedListener((puzzlePiece, i) -> {
            PuzzleViewActivity.this.slideDown(PuzzleViewActivity.this.mRvTools);
            PuzzleViewActivity.this.slideUp(PuzzleViewActivity.this.rvPieceControl);
            PuzzleViewActivity.this.slideUpSaveView();
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) PuzzleViewActivity.this.rvPieceControl.getLayoutParams();
            layoutParams.bottomMargin = SystemUtil.dpToPx(PuzzleViewActivity.this.getApplicationContext(), 10);
            PuzzleViewActivity.this.rvPieceControl.setLayoutParams(layoutParams);
            PuzzleViewActivity.this.currentMode = ToolType.PIECE;
        });
        this.puzzleView.setOnPieceUnSelectedListener(() -> {
            slideDown(rvPieceControl);
            slideUp(mRvTools);
            slideDownSaveView();
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) rvPieceControl.getLayoutParams();
            layoutParams.bottomMargin = 0;
            rvPieceControl.setLayoutParams(layoutParams);
            currentMode = ToolType.NONE;
        });
        this.saveControl = findViewById(R.id.saveControl);
        this.puzzleView.post(() -> PuzzleViewActivity.this.loadPhoto());
        findViewById(R.id.imgCloseLayout).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgSaveLayout).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgCloseSticker).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgCloseFilter).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgCloseBackground).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgSaveSticker).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgCloseText).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgSaveText).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgSaveFilter).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgSaveBackground).setOnClickListener(this.onClickListener);
        this.changeLayoutLayout = findViewById(R.id.changeLayoutLayout);
        this.changeBorder = findViewById(R.id.change_border);
        this.tvChangeLayout = findViewById(R.id.tv_change_layout);
        this.tvChangeLayout.setOnClickListener(this.onClickListener);
        this.tvChangeBorder = findViewById(R.id.tv_change_border);
        this.tvChangeBorder.setOnClickListener(this.onClickListener);
        this.tvChangeRatio = findViewById(R.id.tv_change_ratio);
        this.tvChangeRatio.setOnClickListener(this.onClickListener);
        this.tvChangeBackgroundColor = findViewById(R.id.tv_color);
        this.tvChangeBackgroundColor.setOnClickListener(this.onClickListener);
        this.tvChangeBackgroundGradient = findViewById(R.id.tv_radian);
        this.tvChangeBackgroundGradient.setOnClickListener(this.onClickListener);
        this.tvChangeBackgroundBlur = findViewById(R.id.tv_blur);
        this.tvChangeBackgroundBlur.setOnClickListener(this.onClickListener);
        PuzzleAdapter puzzleAdapter = new PuzzleAdapter();
        this.puzzleList = findViewById(R.id.puzzleList);
        this.puzzleList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.puzzleList.setAdapter(puzzleAdapter);
        puzzleAdapter.refreshData(PuzzleUtils.getPuzzleLayouts(this.lstPaths.size()), null);
        puzzleAdapter.setOnItemClickListener(this);
        AspectRatioPreviewAdapter aspectRatioPreviewAdapter = new AspectRatioPreviewAdapter(true);
        aspectRatioPreviewAdapter.setListener(this);
        this.radiusLayout = findViewById(R.id.radioLayout);
        this.radiusLayout.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.radiusLayout.setAdapter(aspectRatioPreviewAdapter);
        this.textLayout = findViewById(R.id.textControl);
        this.addNewText = findViewById(R.id.addNewText);
        this.addNewText.setVisibility(View.GONE);
        this.addNewText.setOnClickListener(view -> {
            MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                puzzleView.setHandlingSticker(null);
                openTextFragment();
                return null;
            });

        });
        this.wrapStickerList = findViewById(R.id.wrapStickerList);
        ViewPager viewPager = findViewById(R.id.sticker_viewpaper);
        this.stickerLayout = findViewById(R.id.stickerLayout);
        this.stickerAlpha = findViewById(R.id.stickerAlpha);
        this.stickerAlpha.setVisibility(View.GONE);
        this.stickerAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                Sticker currentSticker = PuzzleViewActivity.this.puzzleView.getCurrentSticker();
                if (currentSticker != null) {
                    currentSticker.setAlpha(i);
                }
            }
        });
        this.saveBitmap = findViewById(R.id.save);
        this.saveBitmap.setOnClickListener(view -> {

            MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    Bitmap createBitmap = FileUtils.createBitmap(PuzzleViewActivity.this.puzzleView, 1920);
                    Bitmap createBitmap2 = PuzzleViewActivity.this.puzzleView.createBitmap();
                    new SavePuzzleAsFile().execute(new Bitmap[]{createBitmap, createBitmap2});
                } else {
                    if (PermissionsUtils.checkWriteStoragePermission(PuzzleViewActivity.this)) {
                        Bitmap createBitmap = FileUtils.createBitmap(PuzzleViewActivity.this.puzzleView, 1920);
                        Bitmap createBitmap2 = PuzzleViewActivity.this.puzzleView.createBitmap();
                        new SavePuzzleAsFile().execute(new Bitmap[]{createBitmap, createBitmap2});
                    }
                }

                return null;
            });

        });
        this.addNewSticker = findViewById(R.id.addNewSticker);
        this.addNewSticker.setVisibility(View.GONE);
        this.addNewSticker.setOnClickListener(view -> {
            MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                addNewSticker.setVisibility(View.GONE);
                slideUp(wrapStickerList);

                return null;
            });

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
//        bitmapStickerIcon6.setIconEvent(new Al());
        this.puzzleView.setIcons(Arrays.asList(new BitmapStickerIcon[]{bitmapStickerIcon, bitmapStickerIcon2, bitmapStickerIcon3}));
        this.puzzleView.setConstrained(true);
        this.puzzleView.setOnStickerOperationListener(this.onStickerOperationListener);
        viewPager.setAdapter(new PagerAdapter() {
            public int getCount() {
                return 6;
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
                View inflate = LayoutInflater.from(PuzzleViewActivity.this.getBaseContext()).inflate(R.layout.sticker_items, null, false);
                RecyclerView recyclerView = inflate.findViewById(R.id.rv);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(PuzzleViewActivity.this.getApplicationContext(), 4));
                switch (i) {
                    case 0:
                        recyclerView.setAdapter(new StickerAdapter(PuzzleViewActivity.this, AssetUtils.lstBeards(), i, PuzzleViewActivity.this));
                        break;
                    case 1:
                        recyclerView.setAdapter(new StickerAdapter(PuzzleViewActivity.this, AssetUtils.lstCkeeks(), i, PuzzleViewActivity.this));
                        break;
                    case 2:
                        recyclerView.setAdapter(new StickerAdapter(PuzzleViewActivity.this, AssetUtils.lstGlasses(), i, PuzzleViewActivity.this));
                        break;
                    case 3:
                        recyclerView.setAdapter(new StickerAdapter(PuzzleViewActivity.this, AssetUtils.lstTatoos(), i, PuzzleViewActivity.this));
                        break;
                    case 4:
                        recyclerView.setAdapter(new StickerAdapter(PuzzleViewActivity.this, AssetUtils.lstEmoj(), i, PuzzleViewActivity.this));
                        break;
                    case 5:
                        recyclerView.setAdapter(new StickerAdapter(PuzzleViewActivity.this, AssetUtils.lstCatFaces(), i, PuzzleViewActivity.this));
                        break;
                }
                viewGroup.addView(inflate);
                return inflate;
            }
        });
        RecyclerTabLayout recyclerTabLayout = findViewById(R.id.recycler_tab_layout);
        recyclerTabLayout.setUpWithAdapter(new TopTabAdapter(viewPager, getApplicationContext()));
        recyclerTabLayout.setPositionThreshold(0.5f);
        recyclerTabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        this.changeBackgroundLayout = findViewById(R.id.changeBackgroundLayout);
        this.mainActivity = findViewById(R.id.puzzle_layout);
        this.changeLayoutLayout.setAlpha(0.0f);
        this.stickerLayout.setAlpha(0.0f);
        this.textLayout.setAlpha(0.0f);
        this.filterLayout.setAlpha(0.0f);
        this.changeBackgroundLayout.setAlpha(0.0f);
        this.rvPieceControl.setAlpha(0.0f);
        this.mainActivity.post(() -> {
            slideDown(changeLayoutLayout);
            slideDown(stickerLayout);
            slideDown(textLayout);
            slideDown(changeBackgroundLayout);
            slideDown(filterLayout);
            slideDown(rvPieceControl);
        });
        new Handler().postDelayed(() -> {
            changeLayoutLayout.setAlpha(1.0f);
            stickerLayout.setAlpha(1.0f);
            textLayout.setAlpha(1.0f);
            filterLayout.setAlpha(1.0f);
            changeBackgroundLayout.setAlpha(1.0f);
            rvPieceControl.setAlpha(1.0f);
        }, 1000);
        SharePreferenceUtil.setHeightOfKeyboard(getApplicationContext(), 0);
        this.keyboardHeightProvider = new KeyboardHeightProvider(this);
        this.keyboardHeightProvider.addKeyboardListener(i -> {

            if (i < 0) {
                SharePreferenceUtil.setHeightOfNotch(getApplicationContext(), -i);
            } else if (textEditorDialogFragment != null) {
                textEditorDialogFragment.updateAddTextBottomToolbarHeight(SharePreferenceUtil.getHeightOfNotch(getApplicationContext()) + i);
                SharePreferenceUtil.setHeightOfKeyboard(getApplicationContext(), i + SharePreferenceUtil.getHeightOfNotch(getApplicationContext()));
            }
        });
        showLoading(false);
        this.currentBackgroundState = new PuzzleBackgroundAdapter.SquareView(Color.parseColor("#ffffff"), "", true);
        this.rvBackgroundColor = findViewById(R.id.colorList);
        this.rvBackgroundColor.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
        this.rvBackgroundColor.setHasFixedSize(true);
        this.rvBackgroundColor.setAdapter(new PuzzleBackgroundAdapter(PuzzleViewActivity.this, this));
        this.rvBackgroundGradient = findViewById(R.id.radianList);
        this.rvBackgroundGradient.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
        this.rvBackgroundGradient.setHasFixedSize(true);
        this.rvBackgroundGradient.setAdapter(new PuzzleBackgroundAdapter(PuzzleViewActivity.this, (PuzzleBackgroundAdapter.BackgroundChangeListener) this, true));
        this.rvBackgroundBlur = findViewById(R.id.backgroundList);
        this.rvBackgroundBlur.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
        this.rvBackgroundBlur.setHasFixedSize(true);
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) this.puzzleView.getLayoutParams();
        layoutParams.height = point.x;
        layoutParams.width = point.x;
        this.puzzleView.setLayoutParams(layoutParams);
        this.currentAspect = new AspectRatio(1, 1);
        this.puzzleView.setAspectRatio(new AspectRatio(1, 1));
        puzzle = this;
        this.currentMode = ToolType.NONE;
        CGENativeLibrary.setLoadImageCallback(this.mLoadImageCallback, (Object) null);
        instance = this;
    }


    private void openTextFragment() {
        this.textEditorDialogFragment = TextEditorDialogFragment.show(this);
        this.textEditor = new TextEditorDialogFragment.TextEditor() {
            public void onDone(AddTextProperties addTextProperties) {
                PuzzleViewActivity.this.puzzleView.addSticker(new TextSticker(PuzzleViewActivity.this.getApplicationContext(), addTextProperties));
            }

            public void onBackButton() {
                if (PuzzleViewActivity.this.puzzleView.getStickers().isEmpty()) {
                    PuzzleViewActivity.this.onBackPressed();
                }
            }
        };
        this.textEditorDialogFragment.setOnTextEditorListener(this.textEditor);
    }

    public void isPermissionGranted(boolean z, String str) {
        if (z) {
            Bitmap createBitmap = FileUtils.createBitmap(this.puzzleView, 1920);
            Bitmap createBitmap2 = this.puzzleView.createBitmap();
            new SavePuzzleAsFile().execute(createBitmap, createBitmap2);
        }
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

    public void selectBackgroundBlur() {
        ArrayList arrayList = new ArrayList();
        for (PuzzlePiece drawable : this.puzzleView.getPuzzlePieces()) {
            arrayList.add(drawable.getDrawable());
        }
        PuzzleBackgroundAdapter puzzleBackgroundAdapter = new PuzzleBackgroundAdapter(PuzzleViewActivity.this, this, (List<Drawable>) arrayList);
        puzzleBackgroundAdapter.setSelectedSquareIndex(-1);
        this.rvBackgroundBlur.setAdapter(puzzleBackgroundAdapter);
        this.rvBackgroundBlur.setVisibility(View.VISIBLE);
        this.tvChangeBackgroundBlur.setBackgroundResource(R.drawable.border_bottom);
        this.tvChangeBackgroundBlur.setTextColor(ContextCompat.getColor(this, R.color.black));
        this.rvBackgroundGradient.setVisibility(View.GONE);
        this.tvChangeBackgroundGradient.setBackgroundResource(0);
        this.tvChangeBackgroundGradient.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.rvBackgroundColor.setVisibility(View.GONE);
        this.tvChangeBackgroundColor.setBackgroundResource(0);
        this.tvChangeBackgroundColor.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
    }

    public void selectBackgroundColorTab() {
        this.rvBackgroundColor.setVisibility(View.VISIBLE);
        this.tvChangeBackgroundColor.setBackgroundResource(R.drawable.border_bottom);
        this.tvChangeBackgroundColor.setTextColor(ContextCompat.getColor(this, R.color.black));
        this.rvBackgroundColor.scrollToPosition(0);
        ((PuzzleBackgroundAdapter) this.rvBackgroundColor.getAdapter()).setSelectedSquareIndex(-1);
        this.rvBackgroundColor.getAdapter().notifyDataSetChanged();
        this.rvBackgroundGradient.setVisibility(View.GONE);
        this.tvChangeBackgroundGradient.setBackgroundResource(0);
        this.tvChangeBackgroundGradient.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.rvBackgroundBlur.setVisibility(View.GONE);
        this.tvChangeBackgroundBlur.setBackgroundResource(0);
        this.tvChangeBackgroundBlur.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
    }

    public void selectBackgroundGradientTab() {
        this.rvBackgroundGradient.setVisibility(View.VISIBLE);
        this.tvChangeBackgroundGradient.setBackgroundResource(R.drawable.border_bottom);
        this.tvChangeBackgroundGradient.setTextColor(ContextCompat.getColor(this, R.color.black));
        this.rvBackgroundGradient.scrollToPosition(0);
        ((PuzzleBackgroundAdapter) this.rvBackgroundGradient.getAdapter()).setSelectedSquareIndex(-1);
        this.rvBackgroundGradient.getAdapter().notifyDataSetChanged();
        this.rvBackgroundColor.setVisibility(View.GONE);
        this.tvChangeBackgroundColor.setBackgroundResource(0);
        this.tvChangeBackgroundColor.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.rvBackgroundBlur.setVisibility(View.GONE);
        this.tvChangeBackgroundBlur.setBackgroundResource(0);
        this.tvChangeBackgroundBlur.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
    }


    public void selectLayoutTool() {
        this.puzzleList.setVisibility(View.VISIBLE);
        this.tvChangeLayout.setBackgroundResource(R.drawable.border_bottom);
        this.tvChangeLayout.setTextColor(ContextCompat.getColor(this, R.color.black));
        this.changeBorder.setVisibility(View.GONE);
        this.tvChangeBorder.setBackgroundResource(0);
        this.tvChangeBorder.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.radiusLayout.setVisibility(View.GONE);
        this.tvChangeRatio.setBackgroundResource(0);
        this.tvChangeRatio.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
    }


    public void selectRadiusTool() {
        this.radiusLayout.setVisibility(View.VISIBLE);
        this.tvChangeRatio.setTextColor(ContextCompat.getColor(this, R.color.black));
        this.tvChangeRatio.setBackgroundResource(R.drawable.border_bottom);
        this.puzzleList.setVisibility(View.GONE);
        this.tvChangeLayout.setBackgroundResource(0);
        this.tvChangeLayout.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.changeBorder.setVisibility(View.GONE);
        this.tvChangeBorder.setBackgroundResource(0);
        this.tvChangeBorder.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
    }


    public void selectBorderTool() {
        this.changeBorder.setVisibility(View.VISIBLE);
        this.tvChangeBorder.setBackgroundResource(R.drawable.border_bottom);
        this.tvChangeBorder.setTextColor(ContextCompat.getColor(this, R.color.black));
        this.puzzleList.setVisibility(View.GONE);
        this.tvChangeLayout.setBackgroundResource(0);
        this.tvChangeLayout.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.radiusLayout.setVisibility(View.GONE);
        this.tvChangeRatio.setBackgroundResource(0);
        this.tvChangeRatio.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.sbChangeBorderRadius.setProgress((int) this.puzzleView.getPieceRadian());
        this.sbChangeBorderSize.setProgress((int) this.puzzleView.getPiecePadding());
    }

    private void showUpFunction(View view) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this.mainActivity);
        constraintSet.connect(this.wrapPuzzleView.getId(), 3, this.adsContainer.getId(), 4, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 1, this.mainActivity.getId(), 1, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 4, view.getId(), 3, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 2, this.mainActivity.getId(), 2, 0);
        constraintSet.applyTo(this.mainActivity);
    }


    public void showDownFunction() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this.mainActivity);
        constraintSet.connect(this.wrapPuzzleView.getId(), 3, this.adsContainer.getId(), 4, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 1, this.mainActivity.getId(), 1, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 4, this.mRvTools.getId(), 3, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 2, this.mainActivity.getId(), 2, 0);
        constraintSet.applyTo(this.mainActivity);
    }

    public void onToolSelected(ToolType toolType) {
        this.currentMode = toolType;
        switch (toolType) {
            case LAYOUT:
                this.puzzleLayout = this.puzzleView.getPuzzleLayout();
                this.currentAspect = this.puzzleView.getAspectRatio();
                this.pieceBorderRadius = this.puzzleView.getPieceRadian();
                this.piecePadding = this.puzzleView.getPiecePadding();
                this.puzzleList.scrollToPosition(0);
                ((PuzzleAdapter) this.puzzleList.getAdapter()).setSelectedIndex(-1);
                this.puzzleList.getAdapter().notifyDataSetChanged();
                this.radiusLayout.scrollToPosition(0);
                ((AspectRatioPreviewAdapter) this.radiusLayout.getAdapter()).setLastSelectedView(-1);
                this.radiusLayout.getAdapter().notifyDataSetChanged();
                selectLayoutTool();
                slideUpSaveView();
                slideUp(this.changeLayoutLayout);
                slideDown(this.mRvTools);
                showUpFunction(this.changeLayoutLayout);
                this.puzzleView.setLocked(false);
                this.puzzleView.setTouchEnable(false);
                return;
            case BORDER:
                this.puzzleLayout = this.puzzleView.getPuzzleLayout();
                this.currentAspect = this.puzzleView.getAspectRatio();
                this.pieceBorderRadius = this.puzzleView.getPieceRadian();
                this.piecePadding = this.puzzleView.getPiecePadding();
                this.puzzleList.scrollToPosition(0);
                ((PuzzleAdapter) this.puzzleList.getAdapter()).setSelectedIndex(-1);
                this.puzzleList.getAdapter().notifyDataSetChanged();
                this.radiusLayout.scrollToPosition(0);
                ((AspectRatioPreviewAdapter) this.radiusLayout.getAdapter()).setLastSelectedView(-1);
                this.radiusLayout.getAdapter().notifyDataSetChanged();
                selectBorderTool();
                slideUpSaveView();
                slideUp(this.changeLayoutLayout);
                slideDown(this.mRvTools);
                showUpFunction(this.changeLayoutLayout);
                this.puzzleView.setLocked(false);
                this.puzzleView.setTouchEnable(false);
                return;
            case RATIO:
                this.puzzleLayout = this.puzzleView.getPuzzleLayout();
                this.currentAspect = this.puzzleView.getAspectRatio();
                this.pieceBorderRadius = this.puzzleView.getPieceRadian();
                this.piecePadding = this.puzzleView.getPiecePadding();
                this.puzzleList.scrollToPosition(0);
                ((PuzzleAdapter) this.puzzleList.getAdapter()).setSelectedIndex(-1);
                this.puzzleList.getAdapter().notifyDataSetChanged();
                this.radiusLayout.scrollToPosition(0);
                ((AspectRatioPreviewAdapter) this.radiusLayout.getAdapter()).setLastSelectedView(-1);
                this.radiusLayout.getAdapter().notifyDataSetChanged();
                selectRadiusTool();
                slideUpSaveView();
                slideUp(this.changeLayoutLayout);
                slideDown(this.mRvTools);
                showUpFunction(this.changeLayoutLayout);
                this.puzzleView.setLocked(false);
                this.puzzleView.setTouchEnable(false);
                return;
            case FILTER:
                if (this.lstOriginalDrawable.isEmpty()) {
                    for (PuzzlePiece drawable : this.puzzleView.getPuzzlePieces()) {
                        this.lstOriginalDrawable.add(drawable.getDrawable());
                    }
                }
                new LoadFilterBitmap().execute();
                slideDown(this.mRvTools);
                slideUp(this.filterLayout);
                slideDownSaveView();
                return;
            case STICKER:
                this.puzzleView.setTouchEnable(false);
                slideUpSaveView();
                slideDown(this.mRvTools);
                slideUp(this.stickerLayout);
                this.puzzleView.setLocked(false);
                this.puzzleView.setTouchEnable(false);
                return;
            case TEXT:
                this.puzzleView.setTouchEnable(false);
                slideUpSaveView();
                this.puzzleView.setLocked(false);
                openTextFragment();
                slideDown(this.mRvTools);
                slideUp(this.textLayout);
                this.addNewText.setVisibility(View.VISIBLE);
                return;
            case BACKGROUND:
                this.puzzleView.setLocked(false);
                this.puzzleView.setTouchEnable(false);
                slideUpSaveView();
                selectBackgroundColorTab();
                slideDown(this.mRvTools);
                slideUp(this.changeBackgroundLayout);
                showUpFunction(this.changeBackgroundLayout);
                if (this.puzzleView.getBackgroundResourceMode() == 0) {
                    this.currentBackgroundState.isColor = true;
                    this.currentBackgroundState.isBitmap = false;
                    this.currentBackgroundState.drawableId = ((ColorDrawable) this.puzzleView.getBackground()).getColor();
                    return;
                } else if (this.puzzleView.getBackgroundResourceMode() == 2 || (this.puzzleView.getBackground() instanceof ColorDrawable)) {
                    this.currentBackgroundState.isBitmap = true;
                    this.currentBackgroundState.isColor = false;
                    this.currentBackgroundState.drawable = this.puzzleView.getBackground();
                    return;
                } else if (this.puzzleView.getBackground() instanceof GradientDrawable) {
                    this.currentBackgroundState.isBitmap = false;
                    this.currentBackgroundState.isColor = false;
                    this.currentBackgroundState.drawable = this.puzzleView.getBackground();
                    return;
                } else {
                    return;
                }
            default:
        }
    }


    public void loadPhoto() {
        final int i;
        final ArrayList arrayList = new ArrayList();
        if (this.lstPaths.size() > this.puzzleLayout.getAreaCount()) {
            i = this.puzzleLayout.getAreaCount();
        } else {
            i = this.lstPaths.size();
        }
        for (int i2 = 0; i2 < i; i2++) {
            Target r4 = new Target() {
                public void onBitmapFailed(Exception exc, Drawable drawable) {
                }

                public void onPrepareLoad(Drawable drawable) {
                }

                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                    int width = bitmap.getWidth();
                    float f = (float) width;
                    float height = (float) bitmap.getHeight();
                    float max = Math.max(f / f, height / f);
                    if (max > 1.0f) {
                        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (f / max), (int) (height / max), false);
                    }
                    arrayList.add(bitmap);
                    if (arrayList.size() == i) {
                        if (PuzzleViewActivity.this.lstPaths.size() < PuzzleViewActivity.this.puzzleLayout.getAreaCount()) {
                            for (int i = 0; i < PuzzleViewActivity.this.puzzleLayout.getAreaCount(); i++) {
                                PuzzleViewActivity.this.puzzleView.addPiece((Bitmap) arrayList.get(i % i));
                            }
                        } else {
                            PuzzleViewActivity.this.puzzleView.addPieces(arrayList);
                        }
                    }
                    PuzzleViewActivity.this.targets.remove(this);
                }
            };
            Picasso picasso = Picasso.get();
            picasso.load("file:///" + this.lstPaths.get(i2)).resize(this.deviceWidth, this.deviceWidth).centerInside().config(Bitmap.Config.RGB_565).into((Target) r4);
            this.targets.add(r4);
        }
    }

    public void slideUp(View view) {
        ObjectAnimator.ofFloat(view, "translationY", new float[]{(float) view.getHeight(), 0.0f}).start();
    }


    public void onDestroy() {
        super.onDestroy();
        try {
            this.puzzleView.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void slideUpSaveView() {
        this.saveControl.setVisibility(View.GONE);
    }

    public void slideDownSaveView() {
        this.saveControl.setVisibility(View.VISIBLE);
    }

    public void slideDown(View view) {
        ObjectAnimator.ofFloat(view, "translationY", 0.0f, (float) view.getHeight()).start();
    }

    public void onBackPressed() {
        MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
            if (this.currentMode == null) {

//                if (MyConstant.INSTANCE.getEXIT_INTERSTITIAL()) {
//
//                    //MaxAdInterShow>>done
//
//                } else {
//
//                }
                PuzzleViewActivity.super.onBackPressed();
//            super.onBackPressed();
                return null;
            }
            try {
                switch (this.currentMode) {
                    case LAYOUT:
                    case BORDER:
                    case RATIO:
                        slideDown(PuzzleViewActivity.this.changeLayoutLayout);
                        slideUp(PuzzleViewActivity.this.mRvTools);
                        slideDownSaveView();
                        showDownFunction();
                        PuzzleViewActivity.this.puzzleView.updateLayout(PuzzleViewActivity.this.puzzleLayout);
                        PuzzleViewActivity.this.puzzleView.setPiecePadding(PuzzleViewActivity.this.piecePadding);
                        PuzzleViewActivity.this.puzzleView.setPieceRadian(PuzzleViewActivity.this.pieceBorderRadius);
                        PuzzleViewActivity.this.currentMode = ToolType.NONE;
                        getWindowManager().getDefaultDisplay().getSize(new Point());
                        onNewAspectRatioSelected(PuzzleViewActivity.this.currentAspect);
                        PuzzleViewActivity.this.puzzleView.setAspectRatio(PuzzleViewActivity.this.currentAspect);
                        PuzzleViewActivity.this.puzzleView.setLocked(true);
                        PuzzleViewActivity.this.puzzleView.setTouchEnable(true);

                        return null;
//                case FILTER:
//                    slideUp(PuzzleViewActivity.this.mRvTools);
//                    slideDown(PuzzleViewActivity.this.filterLayout);
//                    PuzzleViewActivity.this.puzzleView.setLocked(true);
//                    PuzzleViewActivity.this.puzzleView.setTouchEnable(true);
//                    for (int i = 0; i < PuzzleViewActivity.this.lstOriginalDrawable.size(); i++) {
//                        PuzzleViewActivity.this.puzzleView.getPuzzlePieces().get(i).setDrawable(PuzzleViewActivity.this.lstOriginalDrawable.get(i));
//                    }
//                    PuzzleViewActivity.this.puzzleView.invalidate();
//                    slideDownSaveView();
//                    PuzzleViewActivity.this.currentMode = ToolType.NONE;
//                    return;
                    case STICKER:
                        if (PuzzleViewActivity.this.puzzleView.getStickers().size() <= 0) {
                            slideUp(PuzzleViewActivity.this.wrapStickerList);
                            slideDown(PuzzleViewActivity.this.stickerLayout);
                            PuzzleViewActivity.this.addNewSticker.setVisibility(View.GONE);
                            PuzzleViewActivity.this.puzzleView.setHandlingSticker((Sticker) null);
                            slideUp(PuzzleViewActivity.this.mRvTools);
                            PuzzleViewActivity.this.puzzleView.setLocked(true);
                            PuzzleViewActivity.this.currentMode = ToolType.NONE;
                        } else if (PuzzleViewActivity.this.addNewSticker.getVisibility() == View.VISIBLE) {
                            PuzzleViewActivity.this.puzzleView.getStickers().clear();
                            PuzzleViewActivity.this.addNewSticker.setVisibility(View.GONE);
                            PuzzleViewActivity.this.puzzleView.setHandlingSticker(null);
                            slideUp(PuzzleViewActivity.this.wrapStickerList);
                            slideDown(PuzzleViewActivity.this.stickerLayout);
                            slideUp(PuzzleViewActivity.this.mRvTools);
                            PuzzleViewActivity.this.puzzleView.setLocked(true);
                            PuzzleViewActivity.this.puzzleView.setTouchEnable(true);
                            PuzzleViewActivity.this.currentMode = ToolType.NONE;
                        } else {
                            slideDown(PuzzleViewActivity.this.wrapStickerList);
                            PuzzleViewActivity.this.addNewSticker.setVisibility(View.VISIBLE);
                        }
                        slideDownSaveView();

                        return null;
                    case TEXT:
                        if (!PuzzleViewActivity.this.puzzleView.getStickers().isEmpty()) {
                            PuzzleViewActivity.this.puzzleView.getStickers().clear();
                            PuzzleViewActivity.this.puzzleView.setHandlingSticker(null);
                        }
                        slideDown(PuzzleViewActivity.this.textLayout);
                        PuzzleViewActivity.this.addNewText.setVisibility(View.GONE);
                        PuzzleViewActivity.this.puzzleView.setHandlingSticker(null);
                        slideUp(PuzzleViewActivity.this.mRvTools);
                        slideDownSaveView();
                        PuzzleViewActivity.this.puzzleView.setLocked(true);
                        PuzzleViewActivity.this.currentMode = ToolType.NONE;
                        PuzzleViewActivity.this.puzzleView.setTouchEnable(true);
                        return null;
                    case BACKGROUND:
                        slideUp(PuzzleViewActivity.this.mRvTools);
                        slideDown(PuzzleViewActivity.this.changeBackgroundLayout);
                        PuzzleViewActivity.this.puzzleView.setLocked(true);
                        PuzzleViewActivity.this.puzzleView.setTouchEnable(true);
                        if (PuzzleViewActivity.this.currentBackgroundState.isColor) {
                            PuzzleViewActivity.this.puzzleView.setBackgroundResourceMode(0);
                            PuzzleViewActivity.this.puzzleView.setBackgroundColor(PuzzleViewActivity.this.currentBackgroundState.drawableId);
                        } else if (PuzzleViewActivity.this.currentBackgroundState.isBitmap) {
                            PuzzleViewActivity.this.puzzleView.setBackgroundResourceMode(2);
                            PuzzleViewActivity.this.puzzleView.setBackground(PuzzleViewActivity.this.currentBackgroundState.drawable);
                        } else {
                            PuzzleViewActivity.this.puzzleView.setBackgroundResourceMode(1);
                            if (PuzzleViewActivity.this.currentBackgroundState.drawable != null) {
                                PuzzleViewActivity.this.puzzleView.setBackground(PuzzleViewActivity.this.currentBackgroundState.drawable);
                            } else {
                                PuzzleViewActivity.this.puzzleView.setBackgroundResource(PuzzleViewActivity.this.currentBackgroundState.drawableId);
                            }
                        }
                        slideDownSaveView();
                        showDownFunction();
                        PuzzleViewActivity.this.currentMode = ToolType.NONE;

                        return null;
                    case PIECE:
                        slideDown(PuzzleViewActivity.this.rvPieceControl);
                        slideUp(PuzzleViewActivity.this.mRvTools);
                        slideDownSaveView();
                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) PuzzleViewActivity.this.rvPieceControl.getLayoutParams();
                        layoutParams.bottomMargin = 0;
                        PuzzleViewActivity.this.rvPieceControl.setLayoutParams(layoutParams);
                        PuzzleViewActivity.this.currentMode = ToolType.NONE;
                        PuzzleViewActivity.this.puzzleView.setHandlingPiece(null);
                        PuzzleViewActivity.this.puzzleView.setPreviousHandlingPiece(null);
                        PuzzleViewActivity.this.puzzleView.invalidate();
                        PuzzleViewActivity.this.currentMode = ToolType.NONE;
                        return null;
                    case NONE:

                        showDiscardDialog();
                        return null;
                    default:
//                        if (MyConstant.INSTANCE.getEXIT_INTERSTITIAL()) {
//
//                            //MaxAdInterShow>>done
//
//
//                        } else {
//
//                        }
                        PuzzleViewActivity.super.onBackPressed();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        });


    }

    private void showDiscardDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.dialog_discard_title).setPositiveButton(R.string.discard, (dialogInterface, i) -> {

//            if (MyConstant.INSTANCE.getEXIT_INTERSTITIAL()) {
//
//                //MaxAdInterShow>>done
//
//            } else {
//                PuzzleViewActivity.this.currentMode = null;
//                PuzzleViewActivity.this.finish();
//            }

            MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                PuzzleViewActivity.this.currentMode = null;
                PuzzleViewActivity.this.finish();

                return null;
            });


        }).setNegativeButton("Cancel", (dialogInterface, i) -> {

            MaxAdManager.INSTANCE.checkTap(PuzzleViewActivity.this,()->{
                dialogInterface.dismiss();

                return null;
            });

        }).create().show();
    }

    public void onItemClick(PuzzleLayout puzzleLayout2, int i) {
        PuzzleLayout parse = PuzzleLayoutParser.parse(puzzleLayout2.generateInfo());
        puzzleLayout2.setRadian(this.puzzleView.getPieceRadian());
        puzzleLayout2.setPadding(this.puzzleView.getPiecePadding());
        this.puzzleView.updateLayout(parse);
    }

    public void onNewAspectRatioSelected(AspectRatio aspectRatio) {
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int[] calculateWidthAndHeight = calculateWidthAndHeight(aspectRatio, point);
        this.puzzleView.setLayoutParams(new ConstraintLayout.LayoutParams(calculateWidthAndHeight[0], calculateWidthAndHeight[1]));
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this.wrapPuzzleView);
        constraintSet.connect(this.puzzleView.getId(), 3, this.wrapPuzzleView.getId(), 3, 0);
        constraintSet.connect(this.puzzleView.getId(), 1, this.wrapPuzzleView.getId(), 1, 0);
        constraintSet.connect(this.puzzleView.getId(), 4, this.wrapPuzzleView.getId(), 4, 0);
        constraintSet.connect(this.puzzleView.getId(), 2, this.wrapPuzzleView.getId(), 2, 0);
        constraintSet.applyTo(this.wrapPuzzleView);
        this.puzzleView.setAspectRatio(aspectRatio);
    }

    public void replaceCurrentPiece(String str) {
        new OnLoadBitmapFromUri().execute(str);
    }

    private int[] calculateWidthAndHeight(AspectRatio aspectRatio, Point point) {
        int height = this.wrapPuzzleView.getHeight();
        if (aspectRatio.getHeight() > aspectRatio.getWidth()) {
            int ratio = (int) (aspectRatio.getRatio() * ((float) height));
            if (ratio < point.x) {
                return new int[]{ratio, height};
            }
            return new int[]{point.x, (int) (((float) point.x) / aspectRatio.getRatio())};
        }
        int ratio2 = (int) (((float) point.x) / aspectRatio.getRatio());
        if (ratio2 > height) {
            return new int[]{(int) (((float) height) * aspectRatio.getRatio()), height};
        }
        return new int[]{point.x, ratio2};
    }

    public void onPause() {
        super.onPause();
        this.keyboardHeightProvider.onPause();
    }

    public void onResume() {
        super.onResume();
        this.keyboardHeightProvider.onResume();

        //MaxAdBanner>>done
        if (!SharePreferenceUtil.isPurchased(PuzzleViewActivity.this)) {
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

    public void addSticker(Bitmap bitmap) {
        this.puzzleView.addSticker(new DrawableSticker(new BitmapDrawable(getResources(), bitmap)));
        slideDown(this.wrapStickerList);
        this.addNewSticker.setVisibility(View.VISIBLE);
    }

    public void onBackgroundSelected(final PuzzleBackgroundAdapter.SquareView squareView) {
        if (squareView.isColor) {
            this.puzzleView.setBackgroundColor(squareView.drawableId);
            this.puzzleView.setBackgroundResourceMode(0);
        } else if (squareView.drawable != null) {
            this.puzzleView.setBackgroundResourceMode(2);
            new AsyncTask<Void, Bitmap, Bitmap>() {

                public void onPreExecute() {
                    PuzzleViewActivity.this.showLoading(true);
                }


                public Bitmap doInBackground(Void... voidArr) {
                    return FilterUtils.getBlurImageFromBitmap(((BitmapDrawable) squareView.drawable).getBitmap(), 5.0f);
                }


                public void onPostExecute(Bitmap bitmap) {
                    PuzzleViewActivity.this.showLoading(false);
                    PuzzleViewActivity.this.puzzleView.setBackground(new BitmapDrawable(PuzzleViewActivity.this.getResources(), bitmap));
                }
            }.execute();
        } else {
            this.puzzleView.setBackgroundResource(squareView.drawableId);
            this.puzzleView.setBackgroundResourceMode(1);
        }
    }

    public void onFilterSelected(String str) {
        new LoadBitmapWithFilter().execute(new String[]{str});
    }


    public void finishCrop(Bitmap bitmap) {
        this.puzzleView.replace(bitmap, "");
    }

    public void onSaveFilter(Bitmap bitmap) {
        this.puzzleView.replace(bitmap, "");
    }

    @Override
    public void onPieceFuncSelected(ToolType toolType) {
        switch (toolType) {
            case REPLACE:
                PhotoPicker.builder().setPhotoCount(1).setPreviewEnabled(false).setShowCamera(false).setForwardMain(true).start(this);
                return;
            case H_FLIP:
                this.puzzleView.flipHorizontally();
                slideDownSaveView();
                return;
            case V_FLIP:
                this.puzzleView.flipVertically();
                slideDownSaveView();
                return;
            case ROTATE:
                this.puzzleView.rotate(90.0f);
                slideDownSaveView();
                return;
            case CROP:
                CropDialogFragment.show(this, this, ((BitmapDrawable) this.puzzleView.getHandlingPiece().getDrawable()).getBitmap());
                slideDownSaveView();
                return;
            case FILTER:
                new LoadFilterBitmapForCurrentPiece().execute();

        }
    }

    class LoadFilterBitmap extends AsyncTask<Void, Void, Void> {
        LoadFilterBitmap() {
        }


        public void onPreExecute() {
            PuzzleViewActivity.this.showLoading(true);
        }


        @SuppressLint("WrongThread")
        public Void doInBackground(Void... voidArr) {
            PuzzleViewActivity.this.lstBitmapWithFilter.clear();
            PuzzleViewActivity.this.lstBitmapWithFilter.addAll(FilterUtils.getLstBitmapWithFilter(ThumbnailUtils.extractThumbnail(((BitmapDrawable) PuzzleViewActivity.this.puzzleView.getPuzzlePieces().get(0).getDrawable()).getBitmap(), 100, 100), PuzzleViewActivity.this));
            return null;
        }


        public void onPostExecute(Void voidR) {
            PuzzleViewActivity.this.rvFilterView.setAdapter(new FilterViewAdapter(PuzzleViewActivity.this.lstBitmapWithFilter, PuzzleViewActivity.this, PuzzleViewActivity.this.getApplicationContext(), Arrays.asList(FilterUtils.EFFECT_CONFIGS)));
            PuzzleViewActivity.this.slideDown(PuzzleViewActivity.this.mRvTools);
            PuzzleViewActivity.this.slideUp(PuzzleViewActivity.this.filterLayout);
            PuzzleViewActivity.this.showLoading(false);
            PuzzleViewActivity.this.puzzleView.setLocked(false);
            PuzzleViewActivity.this.puzzleView.setTouchEnable(false);
        }
    }

    class LoadFilterBitmapForCurrentPiece extends AsyncTask<Void, List<Bitmap>, List<Bitmap>> {
        LoadFilterBitmapForCurrentPiece() {
        }

        public void onPreExecute() {

            PuzzleViewActivity.this.showLoading(true);

        }

        @SuppressLint("WrongThread")
        public List<Bitmap> doInBackground(Void... voidArr) {

            try {
                return FilterUtils.getLstBitmapWithFilter(ThumbnailUtils.extractThumbnail(((BitmapDrawable) PuzzleViewActivity.this.puzzleView.getHandlingPiece().getDrawable()).getBitmap(), 100, 100), PuzzleViewActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<Bitmap>();

            }

        }


        public void onPostExecute(List<Bitmap> list) {
            PuzzleViewActivity.this.showLoading(false);

            if (list == null) {
                return;
            }
            if (PuzzleViewActivity.this.puzzleView.getHandlingPiece() != null) {
                FilterDialogFragment.show(PuzzleViewActivity.this, PuzzleViewActivity.this, ((BitmapDrawable) PuzzleViewActivity.this.puzzleView.getHandlingPiece().getDrawable()).getBitmap(), list);
            }

        }
    }

    class LoadBitmapWithFilter extends AsyncTask<String, List<Bitmap>, List<Bitmap>> {
        LoadBitmapWithFilter() {
        }


        public void onPreExecute() {
            PuzzleViewActivity.this.showLoading(true);
        }


        public List<Bitmap> doInBackground(String... strArr) {
            ArrayList arrayList = new ArrayList();
            for (Drawable drawable : PuzzleViewActivity.this.lstOriginalDrawable) {
                arrayList.add(FilterUtils.getBitmapWithFilter(((BitmapDrawable) drawable).getBitmap(), strArr[0]));
            }
            return arrayList;
        }


        public void onPostExecute(List<Bitmap> list) {
            for (int i = 0; i < list.size(); i++) {
                BitmapDrawable bitmapDrawable = new BitmapDrawable(PuzzleViewActivity.this.getResources(), list.get(i));
                bitmapDrawable.setAntiAlias(true);
                bitmapDrawable.setFilterBitmap(true);
                PuzzleViewActivity.this.puzzleView.getPuzzlePieces().get(i).setDrawable(bitmapDrawable);
            }
            PuzzleViewActivity.this.puzzleView.invalidate();
            PuzzleViewActivity.this.showLoading(false);
        }
    }

    class OnLoadBitmapFromUri extends AsyncTask<String, Bitmap, Bitmap> {
        OnLoadBitmapFromUri() {
        }


        public void onPreExecute() {
            PuzzleViewActivity.this.showLoading(true);
        }


        public Bitmap doInBackground(String... strArr) {
            try {
                Uri fromFile = Uri.fromFile(new File(strArr[0]));

                Bitmap rotateBitmap = SystemUtil.rotateBitmap(MediaStore.Images.Media.getBitmap(PuzzleViewActivity.this.getContentResolver(), fromFile), new ExifInterface(PuzzleViewActivity.this.getContentResolver().openInputStream(fromFile)).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1));

                float width = (float) rotateBitmap.getWidth();
                float height = (float) rotateBitmap.getHeight();
                float max = Math.max(width / 1280.0f, height / 1280.0f);
                return max > 1.0f ? Bitmap.createScaledBitmap(rotateBitmap, (int) (width / max), (int) (height / max), false) : rotateBitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        public void onPostExecute(Bitmap bitmap) {
            PuzzleViewActivity.this.showLoading(false);
            PuzzleViewActivity.this.puzzleView.replace(bitmap, "");
        }
    }

    class SavePuzzleAsFile extends AsyncTask<Bitmap, String, String> {
        SavePuzzleAsFile() {
        }


        public void onPreExecute() {
            PuzzleViewActivity.this.showLoading(true);
        }


        public String doInBackground(Bitmap... bitmapArr) {
            Bitmap bitmap = bitmapArr[0];
            Bitmap bitmap2 = bitmapArr[1];
            Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            canvas.drawBitmap(bitmap, null, new RectF(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight()), (Paint) null);
            canvas.drawBitmap(bitmap2, null, new RectF(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight()), (Paint) null);
            bitmap.recycle();
            bitmap2.recycle();


            String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
            try {
                File image = FileUtils.saveImage(PuzzleViewActivity.this, createBitmap, name);
                createBitmap.recycle();
                return image.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


        }


        public void onPostExecute(String str) {
            if (MyConstant.INSTANCE.getSAVING_INTERSTITIAL()) {

                //MaxAdInterShow>>done
                PuzzleViewActivity.this.showLoading(false);
                Intent intent = new Intent(PuzzleViewActivity.this, SaveAndShareActivity.class);
                intent.putExtra("path", str);
                PuzzleViewActivity.this.startActivity(intent);

            } else {
                PuzzleViewActivity.this.showLoading(false);
                Intent intent = new Intent(PuzzleViewActivity.this, SaveAndShareActivity.class);
                intent.putExtra("path", str);
                PuzzleViewActivity.this.startActivity(intent);
            }

        }
    }

    public final void notifyMediaStoreScanner(Context mContext, final File file) {
        try {
            MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
            mContext.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void initPicasso(Context context) {
        Log.i("TAG", "initializedPicasso:" + initializedPicasso);
        if (initializedPicasso) {
            return;
        }
        try {
            Picasso.setSingletonInstance(new Picasso.Builder(context).build());
        } catch (Exception e) {
            Log.e("TAG", "Error:" + e.toString());
        }

        initializedPicasso = true;
    }
}
