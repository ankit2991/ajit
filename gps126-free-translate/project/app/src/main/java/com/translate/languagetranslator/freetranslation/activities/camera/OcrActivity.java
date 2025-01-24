package com.translate.languagetranslator.freetranslation.activities.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.translate.languagetranslator.freetranslation.R;
import com.translate.languagetranslator.freetranslation.activities.camera.viewModel.OcrViewModel;
import com.translate.languagetranslator.freetranslation.appUtils.AnimUtils;
import com.translate.languagetranslator.freetranslation.appUtils.Constants;
import com.translate.languagetranslator.freetranslation.appUtils.NetworkUtils;
import com.translate.languagetranslator.freetranslation.appUtils.UtilsMethodsKt;
import com.translate.languagetranslator.freetranslation.database.TranslationDb;
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable;
import com.translate.languagetranslator.freetranslation.models.LanguageModel;
import com.translate.languagetranslator.freetranslation.network.TranslationUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_CODE;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_MEANING;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_NAME;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_SUPPORT;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.REQUEST_CODE_IMAGE_SELECTOR;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.REQUEST_CODE_LANGUAGE_OCR;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.REQ_CODE_LANGUAGE_SELECTION;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.SOURCE_LANG_CODE_OCR;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.SOURCE_LANG_CODE_PAIR_OCR;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.SOURCE_LANG_NAME_OCR;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.TARGET_LANG_CODE_OCR;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.TARGET_LANG_CODE_PAIR_OCR;
import static com.translate.languagetranslator.freetranslation.appUtils.Constants.TARGET_LANG_NAME_OCR;
import static com.translate.languagetranslator.freetranslation.appUtils.UtilsMethodsKt.fetchLanguages;
import static com.translate.languagetranslator.freetranslation.appUtils.UtilsMethodsKt.permissionAlreadyGranted;
import static com.translate.languagetranslator.freetranslation.appUtils.UtilsMethodsKt.showToast;
import static com.translate.languagetranslator.freetranslation.appUtils.UtilsMethodsKt.startLanguageActivity;

public class OcrActivity extends AppCompatActivity implements View.OnClickListener, CropImageView.OnCropImageCompleteListener {

    @SuppressWarnings("deprecation")
    Camera camera;
    @SuppressWarnings("deprecation")
    private Camera.Parameters parameters;

    public Display display;

    int bitMapWidth;
    public Bitmap croppedImage;
    StringBuilder strBuilder;

    private TextView mSourceLanguage;
    private TextView mTargetLanguage;


    private FrameLayout mCameraLayout;
    private CropImageView mImage;
    private RelativeLayout mImageLayout, topBar;
    private ImageView mFlashlight;
    private RelativeLayout camViewBottom;
    private RelativeLayout capturedViewBottom, progressBarLayout;
    private RelativeLayout adLayout;
    private FrameLayout adContainer;
    private RelativeLayout ocrResultInputView, ocrResultOutPutView, ocrResultMore;
    private TextView tvOcrResultSrcLang, tvOcrResultTargetLang;
    boolean isResultAvailable = false;
    //    private ProgressBar mProgressCircular;
//    private Dialog cropedResultDialog;
//    private TextView tvInputDialog;
//    private TextView tvOutPutDialog;
    private ConstraintLayout ocrResultView;
    private ImageView ivMoreOcr;
    private TextView tvOcrInput;
    private TextView tvOcrOutput;
    private ProgressBar ocrProgress;
    //    View lineView;
    String inputWord;


    int statusBarHeight = 0;
    int actionBarHeight = 0;
    int navigationBarHeight = 0;

    private String srcLangName = null;
    private String srcLangCode = null;
    private String srcLangPairCode = null;
    private String tarLangName = null;
    private String tarLangCode = null;
    private String tarLangPairCode = null;
    private boolean isFirstClick = true;
    private boolean isFlashLightOn = false;

    private boolean isImageCaptured;

    boolean isHistoryInserted;
    boolean isDialogVisibile;
    private boolean isAdLoaded;
    private boolean isGalleryOpened = false;
    TranslationUtils translationUtils;
    TranslationTable translationTable;
    /*    AdmobUtils admobUtils, adMobUtilsForward, adMobUtilsBackWard;
        FacebookAdsUtils facebookAdsUtils, facebookAdsUtilsForward, facebookAdsUtilsBackWard;*/
    FrameLayout facebookAdContainer;
    Dialog permissionDialog;
    OcrViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);


        viewModel = ViewModelProviders.of(this).get(OcrViewModel.class);
        initRemoteConfig();

        new Handler().postDelayed(() -> runOnUiThread(() -> {
            setViews();
            getTinyDbData();
        }), 100);


    }

    private void initRemoteConfig() {
        FirebaseRemoteConfig remoteConfig = UtilsMethodsKt.getRemoteConfig();


    }


    @SuppressWarnings("deprecation")
    private void captureImage() {
        if (camera != null) {
            camViewBottom.setVisibility(GONE);
            if (isAdLoaded) {
                adLayout.setVisibility(View.VISIBLE);
//                lineView.setVisibility(View.VISIBLE);
            }
            if (isFlashLightOn) {

                flashLightOff();
            }

            try {
                camera.takePicture(null, null, (bytes, camera) -> new Thread(() -> {
                    Bitmap imageBitMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    if (imageBitMap != null) {
                        convert(imageBitMap);
                    } else {
                        runOnUiThread(() -> {
                            showToast(OcrActivity.this, "Try Again");
                            resetAll();
                        });

                    }


                }).start());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @SuppressWarnings("deprecation")
    public void resetAll() {
        isGalleryOpened = false;
        mImageLayout.setVisibility(GONE);
        topBar.setBackgroundResource(0);
        mCameraLayout.setVisibility(View.VISIBLE);

        capturedViewBottom.setVisibility(GONE);
        camViewBottom.setVisibility(View.VISIBLE);
        topBar.setVisibility(View.VISIBLE);
        adLayout.setVisibility(GONE);
//        lineView.setVisibility(GONE);
        if (Build.VERSION.SDK_INT < 23) {
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }
        openCameraPreview();

    }

    @SuppressLint("SwitchIntDef")
    public void fetchImageFromSource(Bitmap imageBitMap) {
        if (display != null) {
            int rotation = display.getRotation();
            if (rotation != 0) {
                switch (rotation) {
                    case Surface.ROTATION_180:
                        imageBitMap = getImageBitMap(imageBitMap, 270);
                        break;
                    case Surface.ROTATION_270:
                        imageBitMap = getImageBitMap(imageBitMap, 180);
                        break;
                }
            } else {
                imageBitMap = getImageBitMap(imageBitMap, 90);
            }
        }
        bitMapWidth = imageBitMap.getWidth();
        Bitmap finalImageBitMap = imageBitMap;

        runOnUiThread(() -> processCapturedImage(finalImageBitMap));

    }

    public void getTinyDbData() {
        srcLangName = UtilsMethodsKt.getPrefString(this, SOURCE_LANG_NAME_OCR);
        if (srcLangName == null || srcLangName.equals("")) {
            srcLangName = Constants.NAME_SOURCE_LANG;
            UtilsMethodsKt.putPrefString(this, SOURCE_LANG_NAME_OCR, srcLangName);
        }
        srcLangCode = UtilsMethodsKt.getPrefString(this, SOURCE_LANG_CODE_OCR);
        if (srcLangCode == null || srcLangCode.equals("")) {
            srcLangCode = "en";
            UtilsMethodsKt.putPrefString(this, SOURCE_LANG_CODE_OCR, srcLangCode);
        }
        srcLangPairCode = UtilsMethodsKt.getPrefString(this, SOURCE_LANG_CODE_PAIR_OCR);
        if (srcLangPairCode == null || srcLangPairCode.equals("")) {
            srcLangPairCode = "en-GB";
            UtilsMethodsKt.putPrefString(this, SOURCE_LANG_CODE_PAIR_OCR, srcLangPairCode);
        }


        tarLangName = UtilsMethodsKt.getPrefString(this, TARGET_LANG_NAME_OCR);
        if (tarLangName == null || tarLangName.equals("")) {
            tarLangName = Constants.NAME_TARGET_LANG;
            UtilsMethodsKt.putPrefString(this, TARGET_LANG_NAME_OCR, tarLangName);
        }
        tarLangCode = UtilsMethodsKt.getPrefString(this, TARGET_LANG_CODE_OCR);
        if (tarLangCode == null || tarLangCode.equals("")) {
            tarLangCode = "fr";
            UtilsMethodsKt.putPrefString(this, TARGET_LANG_CODE_OCR, tarLangCode);
        }
        tarLangPairCode = UtilsMethodsKt.getPrefString(this, TARGET_LANG_CODE_PAIR_OCR);
        if (tarLangPairCode == null || tarLangPairCode.equals("")) {
            tarLangPairCode = "fr-FR";
            UtilsMethodsKt.putPrefString(this, TARGET_LANG_CODE_PAIR_OCR, tarLangPairCode);
        }

        mTargetLanguage.setText(tarLangName);
        mSourceLanguage.setText(srcLangName);

    }


    public void geoToPermission() {
        Permissions.check(this, Manifest.permission.CAMERA, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                openCameraPreview();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
            }
        });
    }


    private void setViews() {
        ImageView mBack = findViewById(R.id.iv_back_ocr);
        ImageView mDone = findViewById(R.id.iv_done_capture);
        ImageView mRetake = findViewById(R.id.iv_again);
        ImageView mChooseImage = findViewById(R.id.choose_image);
        ImageView mTakePhoto = findViewById(R.id.take_photo);
        ImageView ivClearOcr = findViewById(R.id.iv_clear_ocr);

        mSourceLanguage = findViewById(R.id.tv_ocr_src_lang);
        mTargetLanguage = findViewById(R.id.tv_ocr_tar_lang);
        mCameraLayout = findViewById(R.id.camera_layout);
        mImage = findViewById(R.id.image);
        progressBarLayout = findViewById(R.id.progress_bar_layout);
        mImageLayout = findViewById(R.id.image_layout);
        topBar = findViewById(R.id.title_bar_ocr);
        mFlashlight = findViewById(R.id.flashlight);
        camViewBottom = findViewById(R.id.layout_cam_view_holder_bottom);
        capturedViewBottom = findViewById(R.id.layout_captured_image_bottom);
        adLayout = findViewById(R.id.ad_layout_camera);
        adContainer = findViewById(R.id.ad_container_camera);
//        lineView = findViewById(R.id.view);
        ocrResultView = findViewById(R.id.layout_ocr_result_view);

        tvOcrInput = findViewById(R.id.tv_ocr_input);
        ocrProgress = findViewById(R.id.progress_ocr);
        tvOcrOutput = findViewById(R.id.tv_ocr_output);
        ivMoreOcr = findViewById(R.id.iv_more_ocr);
        ocrResultInputView = findViewById(R.id.ocr_result_container_input);
        ocrResultOutPutView = findViewById(R.id.ocr_result_container_output);
        ocrResultMore = findViewById(R.id.layout_ocr_result_more);
        tvOcrResultSrcLang = findViewById(R.id.tv_ocr_result_src_lang);
        tvOcrResultTargetLang = findViewById(R.id.tv_ocr_result_tar_lang);
        facebookAdContainer = findViewById(R.id.native_ad_container);
        mCameraLayout.setOnClickListener(this);
        ocrResultView.setOnClickListener(this);
        mTargetLanguage.setOnClickListener(this);
        mFlashlight.setOnClickListener(this);
        mSourceLanguage.setOnClickListener(this);
        ivMoreOcr.setOnClickListener(this);
        mDone.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mTakePhoto.setOnClickListener(this);
        mChooseImage.setOnClickListener(this);
        mRetake.setOnClickListener(this);
        ivClearOcr.setOnClickListener(this);
        getStatusBarHeight();

    }


    private void processCapturedImage(Bitmap finalImageBitMap) {
        stopCameraPreview();
        mImage.setImageBitmap(finalImageBitMap);
        mImageLayout.setVisibility(View.VISIBLE);
        topBar.setBackgroundResource(R.color.black_translucent);
        progressBarLayout.setVisibility(GONE);
        capturedViewBottom.setVisibility(View.VISIBLE);
        isImageCaptured = true;

    }

    @SuppressWarnings({"deprecation", "rawtypes"})
    private void openCameraPreview() {
        try {
            this.camera = Camera.open();
            UtilsMethodsKt.callHandlerForPref(OcrActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.ocr_open_camera_fail, Toast.LENGTH_SHORT).show();
            finish();
        }
        if (this.camera == null) {
            Toast.makeText(this, R.string.ocr_open_camera_fail, Toast.LENGTH_SHORT).show();
            finish();
        }
        try {
            CustomCamView aVar = new CustomCamView(this, this.camera);
            this.mCameraLayout.removeAllViews();
            this.mCameraLayout.addView(aVar);
            Camera.Parameters parameters = this.camera.getParameters();
            parameters.setPictureFormat(256);

            this.display = getWindowManager().getDefaultDisplay();
            switch (this.display.getRotation()) {
                case Surface.ROTATION_0:
                    this.camera.setDisplayOrientation(90);
                    break;
                case Surface.ROTATION_90:
                    this.camera.setDisplayOrientation(0);
                    break;
                case Surface.ROTATION_180:
                    this.camera.setDisplayOrientation(270);
                    break;
                case Surface.ROTATION_270:
                    this.camera.setDisplayOrientation(180);
                    break;
            }

            List supportedPictureSizes = parameters.getSupportedPictureSizes();
            for (int i = 0; i < supportedPictureSizes.size() - 1; i++) {
                int i2 = 0;
                while (i2 < (supportedPictureSizes.size() - 1) - i) {
                    int i3 = i2 + 1;
                    if (((Camera.Size) supportedPictureSizes.get(i2)).height > ((Camera.Size) supportedPictureSizes.get(i3)).height) {
                        Camera.Size size = (Camera.Size) supportedPictureSizes.get(i2);
                        supportedPictureSizes.set(i2, supportedPictureSizes.get(i3));
                        supportedPictureSizes.set(i3, size);
                    }
                    i2 = i3;
                }
            }
            if (supportedPictureSizes.size() > 1) {
                int i4 = 0;
                while (true) {
                    if (i4 >= supportedPictureSizes.size()) {
                        break;
                    } else if (((Camera.Size) supportedPictureSizes.get(i4)).height >= getWindowManager().getDefaultDisplay().getWidth()) {
                        parameters.setPictureSize(((Camera.Size) supportedPictureSizes.get(i4)).width, ((Camera.Size) supportedPictureSizes.get(i4)).height);
                        break;
                    } else {
                        i4++;
                    }
                }
            }
            this.camera.setParameters(parameters);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        this.handler.sendEmptyMessageDelayed(0, 500);
    }


    @SuppressWarnings("deprecation")
    public void setCamParams() {
        try {
            Camera.Parameters parameters = this.camera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            this.camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getResultFromCropedImage() {
        if (isDialogVisibile) {
            hideOcrResultView();
        } else {
            if (NetworkUtils.isNetworkConnected(this)) {
                strBuilder = new StringBuilder();
                mImage.setOnCropImageCompleteListener(this);
                mImage.getCroppedImageAsync();
            } else {
                showToast(this, "Check Internet connection");
            }

        }
    }

    public void hideOcrResultView() {

        ocrResultView.setVisibility(GONE);
        ocrResultMore.setVisibility(GONE);
        isDialogVisibile = false;
    }


    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(@NotNull Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case 0:
                    setCamParams();
                    return;
                case 1:
                    Toast.makeText(OcrActivity.this, "Error", Toast.LENGTH_LONG).show();
                    return;
                default:
            }
        }
    };


    public void convert(Bitmap bitmap) {
        viewModel.decodeBitMap(bitmap, bitmap1 -> {
            fetchImageFromSource(bitmap1);
            return null;
        });
    }

    public void convertGalleryBitmap(Bitmap bitmap) {
        viewModel.decodeBitMap(bitmap, bitmap1 -> {
            fetchImageFromGallery(bitmap1);
            return null;
        });
    }


    public Bitmap getImageBitMap(Bitmap bitmap, int i) {
        if (bitmap == null) {
            return null;
        }
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate((float) i);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    private void setDataToPresent() {

//        UtilsMethodsKt.openHistoryDetailActivity(this, translationTable, DETAIL_SOURCE_CAM);
    }


    public void getStatusBarHeight() {

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        final TypedArray styledAttributes = this.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize}
        );
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        int navResource = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (navResource > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(navResource);
        }
    }

    public void showResultDialog(String input, String languageCode) {

        String spacedRemoved = input.replaceAll("[\\t\\n\\r]+", " ").trim();
        String newInputWord = spacedRemoved.replaceAll("&", "and");

        List<LanguageModel> langList = fetchLanguages();

        List<String> countries = new ArrayList<>();
        for (LanguageModel languageModel : langList) {
            countries.add(languageModel.getSupportedLangCode());
        }


        int selectedId = countries.indexOf(languageCode);
        if (selectedId >= 0) {
            srcLangName = langList.get(selectedId).getLanguageName();
            mSourceLanguage.setText(srcLangName);

            inputWord = newInputWord;
            tvOcrResultSrcLang.setText(srcLangName);
            tvOcrInput.setText(inputWord);
            tvOcrOutput.setText("");
            ocrProgress.setVisibility(View.VISIBLE);
            ocrResultView.setVisibility(View.VISIBLE);

            isDialogVisibile = true;

            callFreeTranslation(newInputWord, languageCode);


        } else {
            Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show();
        }


    }

    private void callFreeTranslation(String newInputWord, String languageCode) {
        translationUtils = new TranslationUtils(new TranslationUtils.ResultCallBack() {
            @Override
            public void onReceiveResult(String result) {
                runOnUiThread(() -> {
                    String newTranslation = result.replaceAll("[\\t\\n\\r]+", " ").trim();
                    if (isDialogVisibile) {
                        insertHistory(newInputWord, newTranslation);
                    }
                });
            }

            @Override
            public void onFailedResult() {
                runOnUiThread(() -> hideOcrResultView());

            }
        }, newInputWord, languageCode, tarLangCode);
        translationUtils.execute();

    }

    public void showResult() {
        if (isResultAvailable)
            setDataToPresent();
        else
            showToast(OcrActivity.this, "Wait");
    }


    private void insertHistory(String input, String translatedWord) {

        tvOcrOutput.setText(translatedWord);
        ocrProgress.setVisibility(GONE);
        tvOcrResultTargetLang.setText(tarLangName);
        ocrResultOutPutView.setVisibility(View.VISIBLE);
        ocrResultMore.setVisibility(View.VISIBLE);
        isResultAvailable = true;
        Handler animHandler = new Handler();
        animHandler.postDelayed(animRunnable, 600);
        String uniqueId = srcLangCode + inputWord + tarLangCode;
        TranslationDb translationDb = TranslationDb.getInstance(this);
        boolean isFavorite = translationDb.translationTblDao().isFavorite(uniqueId);

        translationTable = new TranslationTable(srcLangCode, tarLangCode, input, translatedWord, srcLangPairCode, tarLangPairCode, uniqueId, isFavorite);
        translationDb.translationTblDao().insert(translationTable);


    }

    Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            AnimUtils.animateLeft(OcrActivity.this, ivMoreOcr);
        }
    };

//    public boolean isLangRtl() {
//        return tarLangCode.equals("ar") || tarLangCode.equals("ur") || tarLangCode.equals("ps") || tarLangCode.equals("fa") || tarLangCode.equals("sd");
//    }

//    private void insertRecord(String primaryId, String translatedWord) {
//        TranslationHistory translationHistory = new TranslationHistory();
//        translationHistory.setInputWord(inputWord);
//        translationHistory.setTranslatedWord(translatedWord.trim());
//        translationHistory.setSrcLang(srcLangName);
//        translationHistory.setTargetLang(tarLangName);
//        translationHistory.setSrcCode(srcLangCode);
//        translationHistory.setTrCode(tarLangCode);
//        translationHistory.setPrimaryId(primaryId);
//        boolean isFavorite = myDatabase.translationDao().isStared(primaryId);
//        translationHistory.setFavorite(isFavorite);
//        myDatabase.translationDao().insert(translationHistory);
//    }
//
//    private int wordInTheList(List<TranslationHistory> mList, String primaryId) {
//        try {
//            if (mList != null && mList.size() > 0) {
//                for (TranslationHistory translationHistory : mList) {
//                    if (translationHistory.getPrimaryId().equals(primaryId)) {
//                        return mList.indexOf(translationHistory);
//                    }
//                }
//            }
//            return -1;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return -1;
//        }
//
//    }


    private void openGalleryForImage() {
        if (isDialogVisibile) {
            hideOcrResultView();
            UtilsMethodsKt.callHandlerForPref(OcrActivity.this);
        } else {
            final Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
            final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Image");
            startActivityForResult(chooserIntent, REQUEST_CODE_IMAGE_SELECTOR);

        }
    }

    private void toggleFlashLight() {
        hideOcrResultView();
        if (isFlashLightOn) {
            flashLightOff();
        } else {

            flashLightOn();

        }
    }

    @SuppressWarnings("deprecation")
    public void flashLightOn() {
        try {
            if (camera != null) {
                parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                mFlashlight.setImageResource(R.drawable.ic_flash_on);
                isFlashLightOn = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("deprecation")
    public void flashLightOff() {
        try {
            if (camera != null) {
                parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                mFlashlight.setImageResource(R.drawable.ic_flash_off);
                isFlashLightOn = false;
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }


/*    public void rotateImage() {


        mImage.rotateImage(90);

    }*/

    private String getLanguageNameFromList(String languageDetected) {

        List<LanguageModel> langList = fetchLanguages();

        List<String> countries = new ArrayList<>();
        for (LanguageModel languageModel : langList) {
            countries.add(languageModel.getSupportedLangCode());
        }


        int selectedId = countries.indexOf(languageDetected);
        if (selectedId >= 0) {
            return langList.get(selectedId).getLanguageName();
        } else return null;
    }

    private void getImageFromGallery(Uri selectedImageUri) {
        try {
            progressBarLayout.setVisibility(View.VISIBLE);
            camViewBottom.setVisibility(GONE);
            if (isAdLoaded) {
                adLayout.setVisibility(View.VISIBLE);
            }
            Bitmap bitmap = uriToBitmap(selectedImageUri);
            if (bitmap != null) {
                convertGalleryBitmap(bitmap);
            } else {
                showToast(this, "Select Another Image");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public enum ClickType {
        NONE, CLEAR, MORE, BACK
    }

    public void fetchImageFromGallery(Bitmap imageBitMap) {
        imageBitMap = getImageBitMap(imageBitMap, 0);

        Bitmap finalImageBitMap = imageBitMap;

        runOnUiThread(() -> processCapturedImage(finalImageBitMap));

    }


    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(() -> runOnUiThread(() -> {
            if (!isGalleryOpened) {
                if (Build.VERSION.SDK_INT < 23) {
                    if (camera != null) {
                        camera.release();
                        camera = null;
                    }
                    openCameraPreview();
                } else {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        openCameraPreview();
                    } else {
                        geoToPermission();
                    }
                }
            }

            isFirstClick = true;
        }), 200);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (translationUtils != null) {
            translationUtils.StopBackground();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void showErrorReponse(String message) {
        hideOcrResultView();
        Toast.makeText(OcrActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle oldInstanceState) {
        super.onSaveInstanceState(oldInstanceState);
        oldInstanceState.clear();

    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        try {
            croppedImage = result.getBitmap();
            String languageDetected = "";

            TextRecognizer txtRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            if (!txtRecognizer.isOperational()) {
                Toast.makeText(OcrActivity.this, "Detector dependencies are not yet available", Toast.LENGTH_SHORT).show();
            } else {
                Frame frame = new Frame.Builder().setBitmap(croppedImage).build();
                SparseArray items = txtRecognizer.detect(frame);
                for (int i = 0; i < items.size(); i++) {
                    TextBlock item = (TextBlock) items.valueAt(i);
                    strBuilder.append(item.getValue()).append(" ");
                    languageDetected = item.getLanguage();
                }

                String inputWord = strBuilder.toString();
                Log.v("Extracted text", "" + inputWord);

                if (inputWord.length() > 0 && languageDetected.length() > 0 && !languageDetected.equals("und")) {
                    if (languageDetected.equals(srcLangCode)) {
                        showResultDialog(inputWord, srcLangCode);
                    } else {
                        String languageName = getLanguageNameFromList(languageDetected);
                        if (languageName != null) {
                            Toast.makeText(this, "Detected Language " + languageName + " not same as selected language " + srcLangName, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Could not detect language,please try again", Toast.LENGTH_SHORT).show();

                        }
                    }
                } else {
                    Toast.makeText(this, "Image not clear. Please capture clear image", Toast.LENGTH_SHORT).show();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.take_photo) {

            captureImage();
        } else if (view.getId() == R.id.camera_layout) {
            if (isDialogVisibile) {
                hideOcrResultView();
            }
            this.handler.sendEmptyMessageDelayed(0, 500);

        } else if (view.getId() == R.id.iv_done_capture) {
            getResultFromCropedImage();
        } else if (view.getId() == R.id.iv_back_ocr) {
            onBackPressed();
        } else if (view.getId() == R.id.choose_image) {
            openGallery();

        } else if (view.getId() == R.id.flashlight) {
            toggleFlashLight();
        } else if (view.getId() == R.id.tv_ocr_tar_lang) {
            if (isDialogVisibile) {
                hideOcrResultView();
            } else if (isFirstClick) {
                startLanguageActivity(this, Constants.LANG_TO, Constants.LANG_TYPE_NORMAL);
                isFirstClick = false;
            }
        } else if (view.getId() == R.id.iv_again) {
            if (isDialogVisibile) {
                hideOcrResultView();
            } else
                onBackPressed();
        } else if (view.getId() == R.id.tv_ocr_src_lang) {
            if (isDialogVisibile) {
                hideOcrResultView();
            } else
                startLanguageActivity(this, Constants.LANG_FROM, Constants.LANG_TYPE_OCR);
        } /*else if (view.getId() == R.id.layout_ocr_result_view) {
            hideOcrResultView();
        }*/ else if (view.getId() == R.id.iv_more_ocr) {
            showDetailView();


        } else if (view.getId() == R.id.iv_clear_ocr) {
            hideOcrResultView();


        }


    }

    private void showDetailView() {
        showResult();
    }

    private void openGallery() {
        UtilsMethodsKt.putPrefBoolean(this, "app_killed", true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!permissionAlreadyGranted(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                goToPermission();
            } else {
                openGalleryForImage();
            }
        } else {
            openGalleryForImage();
        }
    }

    public void goToPermission() {
        Permissions.check(this, Manifest.permission.READ_EXTERNAL_STORAGE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                openGalleryForImage();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
                UtilsMethodsKt.callHandlerForPref(OcrActivity.this);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_LANGUAGE_SELECTION) {

            if (resultCode != Activity.RESULT_CANCELED) {
                assert data != null;
                String source = data.getStringExtra("origin");
                String langName = data.getStringExtra(INTENT_KEY_LANG_NAME);
                String langCode = data.getStringExtra(INTENT_KEY_LANG_CODE);
                String languageSupport = data.getStringExtra(INTENT_KEY_LANG_SUPPORT);
                String languageMeaning = data.getStringExtra(INTENT_KEY_LANG_MEANING);
                assert source != null;
                if (!source.equals(Constants.LANG_FROM)) {
                    tarLangName = langName;
                    mTargetLanguage.setText(tarLangName);
                    tarLangCode = languageSupport;
                    tarLangPairCode = langCode;
                    UtilsMethodsKt.putPrefString(this, TARGET_LANG_CODE_PAIR_OCR, tarLangPairCode);
                    UtilsMethodsKt.putPrefString(this, TARGET_LANG_NAME_OCR, tarLangName);
                    UtilsMethodsKt.putPrefString(this, TARGET_LANG_CODE_OCR, tarLangCode);

                } else {
                    srcLangName = langName;
                    srcLangCode = languageSupport;
                    srcLangPairCode = langCode;
                    mSourceLanguage.setText(srcLangName);
                    UtilsMethodsKt.putPrefString(this, SOURCE_LANG_NAME_OCR, srcLangName);
                    UtilsMethodsKt.putPrefString(this, SOURCE_LANG_CODE_OCR, srcLangCode);
                    UtilsMethodsKt.putPrefString(this, SOURCE_LANG_CODE_PAIR_OCR, srcLangPairCode);


                }

            }
//            resetAll();
        }

        else if (requestCode == REQUEST_CODE_IMAGE_SELECTOR) {
            if (resultCode != Activity.RESULT_CANCELED) {
                isGalleryOpened = true;
                Uri selectedImageUri;
                selectedImageUri = data == null ? null : data.getData();
                if (selectedImageUri != null) {
                    getImageFromGallery(selectedImageUri);
                } else {
                    showToast(this, "Try Again");
                }
                if (isFlashLightOn) {

                    flashLightOff();
                }

                UtilsMethodsKt.callHandlerForPref(OcrActivity.this);

            }
        } else if (requestCode == REQUEST_CODE_LANGUAGE_OCR) {
            if (resultCode != Activity.RESULT_CANCELED) {
                assert data != null;
                String langName = data.getStringExtra("language_name");
                String langCode = data.getStringExtra("language_code");
//                String flagCode = data.getStringExtra("country_code");

                srcLangName = langName;
                srcLangCode = langCode;
                UtilsMethodsKt.putPrefString(this, SOURCE_LANG_CODE_OCR, srcLangCode);
                UtilsMethodsKt.putPrefString(this, SOURCE_LANG_NAME_OCR, srcLangName);

                mSourceLanguage.setText(srcLangName);
            }

        }

    }

    private Bitmap uriToBitmap(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    public void stopCameraPreview() {
        if (camera != null) {
            try {

                camera.cancelAutoFocus();
                camera.setPreviewCallback(null);
                camera.stopPreview();
                mCameraLayout.setVisibility(GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    public void onBackPressed() {
        if (isDialogVisibile) {
            hideOcrResultView();
        } else if (isImageCaptured) {
            isImageCaptured = false;
            resetAll();
        } else {
            super.onBackPressed();
        }
    }



}