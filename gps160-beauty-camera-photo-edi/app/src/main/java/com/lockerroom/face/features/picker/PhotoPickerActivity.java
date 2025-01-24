package com.lockerroom.face.features.picker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.lockerroom.face.activities.EditImageActivity;
import com.lockerroom.face.activities.PuzzleViewActivity;
import com.lockerroom.face.features.picker.entity.Photo;
import com.lockerroom.face.features.picker.event.OnItemCheckListener;
import com.lockerroom.face.features.picker.fragment.ImagePagerFragment;
import com.lockerroom.face.features.picker.fragment.PhotoPickerFragment;
import com.lockerroom.face.R;

import java.util.ArrayList;

public class PhotoPickerActivity extends AppCompatActivity {
    private boolean forwardMain;
    private ImagePagerFragment imagePagerFragment;
    private int maxCount = 9;
    private ArrayList<String> originalPhotos = null;
    private PhotoPickerFragment pickerFragment;
    private boolean showGif = false;

    public PhotoPickerActivity getActivity() {
        return this;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        boolean booleanExtra = getIntent().getBooleanExtra(com.lockerroom.face.features.picker.PhotoPicker.EXTRA_SHOW_CAMERA, true);
        boolean booleanExtra2 = getIntent().getBooleanExtra(com.lockerroom.face.features.picker.PhotoPicker.EXTRA_SHOW_GIF, false);
        boolean booleanExtra3 = getIntent().getBooleanExtra(com.lockerroom.face.features.picker.PhotoPicker.EXTRA_PREVIEW_ENABLED, true);
        this.forwardMain = getIntent().getBooleanExtra(com.lockerroom.face.features.picker.PhotoPicker.MAIN_ACTIVITY, false);
        setShowGif(booleanExtra2);
        setContentView(R.layout.__picker_activity_photo_picker);
        setSupportActionBar(findViewById(R.id.toolbar));
        setTitle(getResources().getString(R.string.tap_to_select));
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            supportActionBar.setElevation(25.0f);
        }
        this.maxCount = getIntent().getIntExtra(com.lockerroom.face.features.picker.PhotoPicker.EXTRA_MAX_COUNT, 9);
        int intExtra = getIntent().getIntExtra(com.lockerroom.face.features.picker.PhotoPicker.EXTRA_GRID_COLUMN, 3);
        this.originalPhotos = getIntent().getStringArrayListExtra(com.lockerroom.face.features.picker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS);
        this.pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (this.pickerFragment == null) {
            this.pickerFragment = PhotoPickerFragment.newInstance(booleanExtra, booleanExtra2, booleanExtra3, intExtra, this.maxCount, this.originalPhotos);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, this.pickerFragment, "tag").commit();
            getSupportFragmentManager().executePendingTransactions();
        }
        this.pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            public final boolean onItemCheck(int i, Photo photo, int i2) {
                if (!forwardMain) {
                    Intent intent = new Intent(PhotoPickerActivity.this, EditImageActivity.class);
                    intent.putExtra(PhotoPicker.KEY_SELECTED_PHOTOS, photo.getPath());
                    startActivity(intent);
                    finish();
                    return true;
                }
                PuzzleViewActivity.getInstance().replaceCurrentPiece(photo.getPath());
                finish();
                return true;
            }
        });
    }


    public void onBackPressed() {
        if (this.imagePagerFragment == null || !this.imagePagerFragment.isVisible()) {
            super.onBackPressed();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 16908332) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }


    public void setShowGif(boolean z) {
        this.showGif = z;
    }
}
