package macro.hd.wallpapers.Interface.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.ExclusiveService.ExclusiveLiveWallpaperService;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.view.CropImageView;
import com.yalantis.ucrop.view.UCropView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;


public class CreateExclusiveActivity extends BaseBannerActivity implements UCropFragmentCallback {

    private static final String TAG = "ResultActivity";
    private int requestMode = 1;
    private Uri uri;
    private ImageView img_result;
    private SettingStore settingStore;
    private WallpaperInfo old_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        setLocale();
        setContentView(R.layout.activity_select_exclusive);

        img_result=findViewById(R.id.img_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.label_create_exclusive));

        img_result.setBackgroundResource(CommonFunctions.getLoadingColor(this));

        settingStore=SettingStore.getInstance(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            CardView card_view=findViewById(R.id.card_view);
            card_view.setMaxCardElevation(0f);
            card_view.setPreventCornerOverlap(false);
        }

        findViewById(R.id.img_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
            }
        });

        findViewById(R.id.btn_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uri!=null) {
                    EventManager.sendEvent(EventManager.LBL_EXCLUSIVE_WALLPAPER, EventManager.ATR_KEY_CREATE_EXCLUSIVE_WALLPAPER, "Set");
                    saveCroppedImage();
                    set4KWallPaper();
                }else{
                    Toast.makeText(CreateExclusiveActivity.this,getResources().getString(R.string.label_select_img),Toast.LENGTH_SHORT).show();
                }
            }
        });

        FrameLayout AdContainer1= (FrameLayout) findViewById(R.id.AdContainer1);
        requestBanner(this,AdContainer1,false,false);


        try {
            RelativeLayout rl_mail= (RelativeLayout) findViewById(R.id.rl_mail);
            Point windowDimensions = new Point();
            getWindowManager().getDefaultDisplay().getSize(windowDimensions);
            Logger.e("JesusDetailAdapterNew","y:"+windowDimensions.y + " X:"+windowDimensions.x);
            int itemHeight = Math.round(windowDimensions.y * 0.62f);
            int itemWidht = Math.round(windowDimensions.x * 0.65f);
            int itemHeight_diment= (int) getResources().getDimension(R.dimen.exclusieve_hight);
            int itemWidht_diment= (int)  getResources().getDimension(R.dimen.exclusieve_width);
            Logger.e("JesusDetailAdapterNew","y:"+windowDimensions.y + " X:"+windowDimensions.x+ " itemHeight:"+itemHeight+" itemWidht:"+itemWidht+" itemHeight_diment:"+itemHeight_diment + " itemWidht_diment:"+itemWidht_diment);

            rl_mail.getLayoutParams().height = itemHeight;
            rl_mail.getLayoutParams().width = itemWidht;
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            if(AppConstant.isWallSetDirectly) {
                WallpaperManager wpm = WallpaperManager.getInstance(this);
                old_info = wpm.getWallpaperInfo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Error e) {
            e.printStackTrace();
        }
    }


    private void set4KWallPaper() {
        boolean needToCallPrevew=true;
        if(old_info!=null && old_info.getComponent().getClassName().equals(ExclusiveLiveWallpaperService.class.getCanonicalName())){
            needToCallPrevew=false;
        }
        if(AppConstant.isWallSetDirectly && !needToCallPrevew) {
            CommonFunctions.showDialogConfirmation(this, getResources().getString(R.string.dialog_title_info), getResources().getString(R.string.label_ask_exclusive), new CommonFunctions.DialogOnButtonClickListener() {
                @Override
                public void onOKButtonCLick() {
                    settingStore.setExclusiveWallpaperPath(finalFile);
                    Toast.makeText(CreateExclusiveActivity.this,getResources().getString(R.string.wallpaper_updated),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelButtonCLick() {

                }
            });

        }else{
            settingStore.setExclusiveWallpaperPathTemp(finalFile);
            try {
                startActivityForResult(new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new
                                        ComponentName(this, ExclusiveLiveWallpaperService.class))
                        ,200);
            } catch (Exception e) {
                try {
                    startActivityForResult(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new
                            ComponentName(this, ExclusiveLiveWallpaperService.class))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 200);
                } catch (Exception e2) {
                    Toast.makeText(this, R.string
                            .toast_failed_launch_wallpaper_chooser, Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    public void startWithUri( @NonNull Uri uri) {

        if (uri != null) {
            try {
                CreateExclusiveActivity.this.uri=uri;
                UCropView uCropView = findViewById(R.id.ucrop);
                uCropView.getCropImageView().setImageUri(uri, null);
                uCropView.getOverlayView().setShowCropFrame(false);
                uCropView.getOverlayView().setShowCropGrid(false);
                uCropView.getOverlayView().setDimmedColor(Color.TRANSPARENT);
            } catch (Exception e) {
                Log.e(TAG, "setImageUri", e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            Glide.with(this)
                    .load(uri).transition(DrawableTransitionOptions.withCrossFade()) .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(img_result);

        }
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);

    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (WallpapersApplication.getApplication() != null) {
            WallpapersApplication.getApplication().onPause(this);
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        try {
            if (WallpapersApplication.getApplication() != null) {
                WallpapersApplication.getApplication().onResume(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveCroppedImage() {
            if (uri != null && uri.getScheme().equals("file")) {
                try {
                    copyFileToDownloads(uri);
                } catch (Exception e) {
                    Toast.makeText(CreateExclusiveActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, uri.toString(), e);
                }
            } else {
                Toast.makeText(CreateExclusiveActivity.this, getString(R.string.toast_unexpected_error), Toast.LENGTH_SHORT).show();
            }
    }

    private String finalFile;
    private void copyFileToDownloads(Uri croppedFileUri) throws Exception {
//        String downloadsDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
//        String filename = String.format("%d_%s", Calendar.getInstance().getTimeInMillis(), croppedFileUri.getLastPathSegment());

        finalFile = CommonFunctions.createFourKDirectoryCache() + "/tempE" + croppedFileUri.getLastPathSegment();

        File saveFile = new File(finalFile);
        finalFile=saveFile.getAbsolutePath();
        FileInputStream inStream = new FileInputStream(new File(croppedFileUri.getPath()));
        FileOutputStream outStream = new FileOutputStream(saveFile);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    protected static final int REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101;
    protected static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102;

    private AlertDialog mAlertDialog;

    /**
     * Hide alert dialog if any.
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }


    /**
     * Requests given permission.
     * If the permission has been denied previously, a Dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    protected void requestPermission(final String permission, String rationale, final int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showAlertDialog(getString(R.string.permission_title_rationale), rationale,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(CreateExclusiveActivity.this,
                                    new String[]{permission}, requestCode);
                        }
                    }, getString(R.string.label_ok), null, getString(R.string.label_cancel));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }



    private void pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getString(R.string.permission_read_storage_rationale),
                    REQUEST_STORAGE_READ_ACCESS_PERMISSION);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    .setType("image/*")
                    .addCategory(Intent.CATEGORY_OPENABLE);

            String[] mimeTypes = {"image/jpeg", "image/png"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            try {
                startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), requestMode);
            } catch (Exception e) {
                Toast.makeText(this, getResources().getString(R.string.no_gallery),
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == requestMode) {
                final Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    startCrop(selectedUri);
                } else {
                    Toast.makeText(CreateExclusiveActivity.this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            }else{
                settingStore.setExclusiveWallpaperPath(finalFile);
            }
        }else if (resultCode == RESULT_CANCELED) {
            if (requestCode == 200) {
                if(AppConstant.isWallSetDirectly) {
                    WallpaperManager wpm = WallpaperManager.getInstance(this);
                    WallpaperInfo info = wpm.getWallpaperInfo();

                    if(info == null)
                        return;

                    if(old_info!=null)
                        Logger.e("onActivityResult","Old:"+old_info.getComponent().getClassName());
                    Logger.e("onActivityResult","new:"+info.getComponent().getClassName());
                    Logger.e("onActivityResult","service:"+ExclusiveLiveWallpaperService.class.getCanonicalName());

                    if (info != null && (old_info == null || (!old_info.getComponent().getClassName().equals(info.getComponent().getClassName())
                            && info.getComponent().getClassName().equals(ExclusiveLiveWallpaperService.class.getCanonicalName())))) {
                        Logger.e("onActivityResult","canceled:"+finalFile);
                        settingStore.setExclusiveWallpaperPath(finalFile);
                    }
                }
                settingStore.setExclusiveWallpaperPath(finalFile);
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }


    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";
    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME;
        destinationFileName += ".jpg";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));

        uCrop = basisConfig(uCrop);
        uCrop = advancedConfig(uCrop);

//        if (requestMode == REQUEST_SELECT_PICTURE_FOR_FRAGMENT) {       //if build variant = fragment
//            setupFragment(uCrop);
//        } else {                                                        // else start uCrop Activity
            uCrop.start(CreateExclusiveActivity.this);
//        }

    }

    /**
     * In most cases you need only to set crop aspect ration and max size for resulting image.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private UCrop basisConfig(@NonNull UCrop uCrop) {
//        try {
//            float ratioX = Float.valueOf(mEditTextRatioX.getText().toString().trim());
//            float ratioY = Float.valueOf(mEditTextRatioY.getText().toString().trim());
//            if (ratioX > 0 && ratioY > 0) {
//                uCrop = uCrop.withAspectRatio(ratioX, ratioY);
//            }
//        } catch (NumberFormatException e) {
//            Log.i(TAG, String.format("Number please: %s", e.getMessage()));
//        }
//
//        if (mCheckBoxMaxSize.isChecked()) {
//            try {
//                int maxWidth = Integer.valueOf(mEditTextMaxWidth.getText().toString().trim());
//                int maxHeight = Integer.valueOf(mEditTextMaxHeight.getText().toString().trim());
//                if (maxWidth > UCrop.MIN_SIZE && maxHeight > UCrop.MIN_SIZE) {
//                    uCrop = uCrop.withMaxResultSize(maxWidth, maxHeight);
//                }
//            } catch (NumberFormatException e) {
//                Log.e(TAG, "Number please", e);
//            }
//        }

        return uCrop;
    }

    /**
     * Sometimes you want to adjust more options, it's done via {@link com.yalantis.ucrop.UCrop.Options} class.
     *
     * @param uCrop - ucrop builder instance
     * @return - ucrop builder instance
     */
    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();

        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

        options.setCompressionQuality(100);

//        options.setHideBottomControls(mCheckBoxHideBottomControls.isChecked());
//        options.setFreeStyleCropEnabled(mCheckBoxFreeStyleCrop.isChecked());
//
//        options.setBrightnessEnabled(mCheckBoxBrigtness.isChecked());
//        options.setContrastEnabled(mCheckBoxContrast.isChecked());
//        options.setSaturationEnabled(mCheckBoxSaturation.isChecked());
//        options.setSharpnessEnabled(mCheckBoxSharpness.isChecked());

        /*
        If you want to configure how gestures work for all UCropActivity tabs

        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        * */

        /*
        This sets max size for bitmap that will be decoded from source Uri.
        More size - more memory allocation, default implementation uses screen diagonal.

        options.setMaxBitmapSize(640);
        * */


       /*

        Tune everything (ﾉ◕ヮ◕)ﾉ*:･ﾟ✧

        options.setMaxScaleMultiplier(5);
        options.setImageToCropBoundsAnimDuration(666);
        options.setDimmedLayerColor(Color.CYAN);
        options.setCircleDimmedLayer(true);
        options.setShowCropFrame(false);
        options.setCropGridStrokeWidth(20);
        options.setCropGridColor(Color.GREEN);
        options.setCropGridColumnCount(2);
        options.setCropGridRowCount(1);
        options.setToolbarCropDrawable(R.drawable.your_crop_icon);
        options.setToolbarCancelDrawable(R.drawable.your_cancel_icon);

        // Color palette
        options.setToolbarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setActiveWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.your_color_res));
        options.setRootViewBackgroundColor(ContextCompat.getColor(this, R.color.your_color_res));

        // Aspect ratio options
        options.setAspectRatioOptions(1,
            new AspectRatio("WOW", 1, 2),
            new AspectRatio("MUCH", 3, 4),
            new AspectRatio("RATIO", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
            new AspectRatio("SO", 16, 9),
            new AspectRatio("ASPECT", 1, 1));

       */

        options.setAspectRatioOptions(0,
                new AspectRatio("9:16", 9, 16),
                new AspectRatio(getString(R.string.label_original), CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO));

        return uCrop.withOptions(options);
    }

    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            startWithUri(resultUri);
        } else {
            Toast.makeText(CreateExclusiveActivity.this, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
            Toast.makeText(CreateExclusiveActivity.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(CreateExclusiveActivity.this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void loadingProgress(boolean showLoader) {
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onCropFinish(UCropFragment.UCropResult result) {
        switch (result.mResultCode) {
            case RESULT_OK:
                handleCropResult(result.mResultData);
                break;
            case UCrop.RESULT_ERROR:
                handleCropError(result.mResultData);
                break;
        }
//        removeFragmentFromScreen();
    }

    /**
     * This method shows dialog with given title & message.
     * Also there is an option to pass onClickListener for positive & negative button.
     *
     * @param title                         - dialog title
     * @param message                       - dialog message
     * @param onPositiveButtonClickListener - listener for positive button
     * @param positiveText                  - positive button text
     * @param onNegativeButtonClickListener - listener for negative button
     * @param negativeText                  - negative button text
     */
    protected void showAlertDialog(@Nullable String title, @Nullable String message,
                                   @Nullable DialogInterface.OnClickListener onPositiveButtonClickListener,
                                   @NonNull String positiveText,
                                   @Nullable DialogInterface.OnClickListener onNegativeButtonClickListener,
                                   @NonNull String negativeText) {

        android.app.AlertDialog.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SettingStore settingStore = SettingStore.getInstance(this);
            boolean isDarkTheme = false;
            if (settingStore.getTheme()==0) {
                isDarkTheme=false;
            }else if (settingStore.getTheme()==1) {
                isDarkTheme=true;
            }
            if(isDarkTheme)
                builder = new android.app.AlertDialog.Builder(this, R.style.CustomAlertDialog);
            else
                builder = new android.app.AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);

        } else {
            builder = new android.app.AlertDialog.Builder(this);
        }

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, onPositiveButtonClickListener);
        builder.setNegativeButton(negativeText, onNegativeButtonClickListener);
        if(!isFinishing()) {
            mAlertDialog = builder.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlertDialog=null;
        img_result=null;
        settingStore=null;
        uri=null;
        finalFile=null;
        old_info=null;
    }
}
