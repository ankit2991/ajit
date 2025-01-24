package macro.hd.wallpapers.LightWallpaperService;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import macro.hd.wallpapers.Interface.Activity.BaseActivity;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropFragment;
import com.yalantis.ucrop.UCropFragmentCallback;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.view.CropImageView;

import java.io.File;

public class Edge_ImgSettings extends BaseActivity implements UCropFragmentCallback {
    
    public SharedPreferences sharedPreferences;
    private int requestMode = 1;
    Uri uri;

    private void setSeekValue(int id, final String str) {
        SeekBar seekBar = (SeekBar) findViewById(id);
        seekBar.setProgress(sharedPreferences.getInt(str, 0));
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean z) {
                sharedPreferences.edit().putInt(str, progress).apply();
            }
        });
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

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setTheme();
        setLocale();
        setContentView(R.layout.edge_img_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.back_img_setting));

        TextView setBgImage = (TextView) findViewById(R.id.setBgImage);

        sharedPreferences = getSharedPreferences("borderlightwall", 0);
        setSeekValue(R.id.seekBarVisibilityUnlocked, "imagevisibilityunlocked");
        setSeekValue(R.id.seekBarDesaturationLocked, "imagedesaturationlocked");
        setSeekValue(R.id.seekBarDesaturationUnlocked, "imagedesaturationunlocked");
        setBgImage.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {

                pickFromGallery();
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction("android.intent.action.GET_CONTENT");
//                startActivityForResult(Intent.createChooser(intent, "Select image"), REQUEST_CODE);
            }
        });
        sharedPreferences.edit().putBoolean("enableimage", true).apply();

        LinearLayout linearLayout=findViewById(R.id.ll_border);
        SettingStore settingStore=SettingStore.getInstance(this);
        if(settingStore.getTheme()==0)
            linearLayout.setBackgroundResource(R.drawable.settings_border);
        else
            linearLayout.setBackgroundResource(R.drawable.settings_border_dark);

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
                            ActivityCompat.requestPermissions(Edge_ImgSettings.this,
                                    new String[]{permission}, requestCode);
                        }
                    }, getString(R.string.label_ok), null, getString(R.string.label_cancel));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
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

        try {
            AlertDialog.Builder builder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SettingStore settingStore = SettingStore.getInstance(this);
                boolean isDarkTheme = false;
                if (settingStore.getTheme()==0) {
                    isDarkTheme=false;
                }else if (settingStore.getTheme()==1) {
                    isDarkTheme=true;
                }
                if(isDarkTheme)
                    builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
                else
                    builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);

            } else {
                builder = new AlertDialog.Builder(this);
            }


            builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButton(positiveText, onPositiveButtonClickListener);
            builder.setNegativeButton(negativeText, onNegativeButtonClickListener);
            mAlertDialog = builder.show();
        } catch (Exception e) {
            e.printStackTrace();
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
        uCrop.start(Edge_ImgSettings.this);
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

        options.setAspectRatioOptions(0,
                new AspectRatio("9:16", 9, 16),
                new AspectRatio(getResources().getString(R.string.label_original), CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO));

        return uCrop.withOptions(options);
    }

    private void handleCropResult(@NonNull Intent result) {
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            new ImageUtility().setImage(resultUri, this);
            sharedPreferences.edit().putBoolean("enableimage", true).apply();
            Toast.makeText(Edge_ImgSettings.this, R.string.toast_set_image, Toast.LENGTH_SHORT).show();
//            startWithUri(resultUri);
        } else {
            Toast.makeText(Edge_ImgSettings.this, R.string.toast_cannot_retrieve_cropped_image, Toast.LENGTH_SHORT).show();
        }
    }
    private static final String TAG = "ResultActivity";

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Log.e(TAG, "handleCropError: ", cropError);
            Toast.makeText(Edge_ImgSettings.this, cropError.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(Edge_ImgSettings.this, R.string.toast_unexpected_error, Toast.LENGTH_SHORT).show();
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String[] mimeTypes = {"image/jpeg", "image/png"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }

            try {
                startActivityForResult(Intent.createChooser(intent, getString(R.string.label_select_picture)), requestMode);
            } catch (Exception e) {
                Toast.makeText(this, "Your device doesn't have gallery",
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
                    Toast.makeText(Edge_ImgSettings.this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            }else{
//                settingStore.setExclusiveWallpaperPath(finalFile);
            }
        }
        if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAlertDialog=null;
        sharedPreferences=null;
        uri=null;
    }
}
