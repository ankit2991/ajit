package com.lockerroom.face.features.puzzle.photopicker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.lockerroom.face.Constants;
import com.lockerroom.face.activities.EditImageActivity;
import com.lockerroom.face.activities.MainActivity;
import com.lockerroom.face.activities.PuzzleViewActivity;
//import com.rebel.photoeditor.ads.FacebookAds;
import com.lockerroom.face.ads.IronSourceAdsManager;
import com.lockerroom.face.features.puzzle.photopicker.adapter.AlbumAdapter;
import com.lockerroom.face.features.puzzle.photopicker.adapter.ListAlbumAdapter;
import com.lockerroom.face.features.puzzle.photopicker.model.ImageModel;
import com.lockerroom.face.features.puzzle.photopicker.myinterface.OnAlbum;
import com.lockerroom.face.features.puzzle.photopicker.myinterface.OnListAlbum;
import com.lockerroom.face.R;
import com.lockerroom.face.firebaseAdsConfig.RemoteConfigData;
import com.lockerroom.face.interfaces.IronSourceCallbacks;
import com.lockerroom.face.maxAdManager.BannerAdListener;
import com.lockerroom.face.maxAdManager.MaxAdManager;
import com.lockerroom.face.utils.MyConstant;
import com.lockerroom.face.utils.SharePreferenceUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PickImageActivity extends AppCompatActivity implements View.OnClickListener, OnAlbum, OnListAlbum {
    public static final String KEY_DATA_RESULT = "KEY_DATA_RESULT";
    public static final String KEY_LIMIT_MAX_IMAGE = "KEY_LIMIT_MAX_IMAGE";
    public static final String KEY_LIMIT_MIN_IMAGE = "KEY_LIMIT_MIN_IMAGE";

    private final String TAG = "PickImageActivity";
    AlbumAdapter albumAdapter;
    ArrayList<ImageModel> dataAlbum = new ArrayList<>();
    ArrayList<ImageModel> dataListPhoto = new ArrayList<>();
    GridView gridViewAlbum;
    GridView gridViewListAlbum;
    HorizontalScrollView horizontalScrollView;
    LinearLayout layoutListItemSelect;
    int limitImageMax = 30;
    int limitImageMin = 2;
    ListAlbumAdapter listAlbumAdapter;
    ArrayList<ImageModel> listItemSelect = new ArrayList<>();
    private Handler mHandler;
    int pWHBtnDelete;
    int pWHItemSelected;
    ArrayList<String> pathList = new ArrayList<>();
    private RelativeLayout loadingView;



//    public ProgressDialog pd;

    public int position = 0;
    AlertDialog sortDialog;
    TextView txtTotalImage;
    FrameLayout app_ad;
    TextView tvLoading;
    RelativeLayout mainConaitner;

    private class GetItemAlbum extends AsyncTask<Void, Void, String> {

        public void onPreExecute() {
            PickImageActivity.this.showLoading(true);

        }


        public void onProgressUpdate(Void... voidArr) {
        }

        private GetItemAlbum() {
        }


        public String doInBackground(Void... voidArr) {
            Cursor query = PickImageActivity.this.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{"_data", "bucket_display_name"}, (String) null, (String[]) null, (String) null);
            if (query == null) {
                return "";
            }
            int columnIndexOrThrow = query.getColumnIndexOrThrow("_data");
            while (query.moveToNext()) {
                String string = query.getString(columnIndexOrThrow);
                File file = new File(string);
                if (file.exists()) {
                    boolean access$000 = PickImageActivity.this.checkFile(file);
                    if (!PickImageActivity.this.Check(file.getParent(), PickImageActivity.this.pathList) && access$000) {
                        PickImageActivity.this.pathList.add(file.getParent());
                        PickImageActivity.this.dataAlbum.add(new ImageModel(file.getParentFile().getName(), string, file.getParent()));
                    }
                }
            }
            Collections.sort(PickImageActivity.this.dataAlbum);
            query.close();
            return "";
        }


        public void onPostExecute(String str) {
            PickImageActivity.this.gridViewAlbum.setAdapter(PickImageActivity.this.albumAdapter);
            PickImageActivity.this.showLoading(false);

        }
    }

    private class GetItemListAlbum extends AsyncTask<Void, Void, String> {
        String pathAlbum;


        public void onPreExecute() {
        }


        public void onProgressUpdate(Void... voidArr) {
        }

        GetItemListAlbum(String str) {
            this.pathAlbum = str;
        }


        public String doInBackground(Void... voidArr) {
            File file = new File(this.pathAlbum);
            if (!file.isDirectory()) {
                return "";
            }
            for (File file2 : file.listFiles()) {
                if (file2.exists()) {
                    boolean access$000 = PickImageActivity.this.checkFile(file2);
                    if (!file2.isDirectory() && access$000) {
                        PickImageActivity.this.dataListPhoto.add(new ImageModel(file2.getName(), file2.getAbsolutePath(), file2.getAbsolutePath()));
                        publishProgress(new Void[0]);
                    }
                }
            }
            return "";
        }


        public void onPostExecute(String str) {

            try {
                Collections.sort(PickImageActivity.this.dataListPhoto, new Comparator<ImageModel>() {
                    @Override
                    public int compare(ImageModel imageModel, ImageModel imageModel2) {
                        File file = new File(imageModel.getPathFolder());
                        File file2 = new File(imageModel2.getPathFolder());
                        if (file.lastModified() > file2.lastModified()) {
                            return -1;
                        }
                        return file.lastModified() < file2.lastModified() ? 1 : 0;
                    }
                });
            } catch (Exception e) {
            }
            PickImageActivity.this.listAlbumAdapter.notifyDataSetChanged();
            PickImageActivity.this.showLoading(false);

        }


    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.piclist_activity_album);
        setSupportActionBar(findViewById(R.id.toolbar));
        app_ad = (FrameLayout) findViewById(R.id.bannerContainer);
        tvLoading = (TextView) findViewById(R.id.bannerTvLoading);
        mainConaitner = (RelativeLayout) findViewById(R.id.maxBannerAdContainer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Constants.SHOW_ADS) {
//            FacebookAds.loadBanner(this);
//            AdmobAds.loadBanner(this);
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
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.limitImageMax = extras.getInt(KEY_LIMIT_MAX_IMAGE, 9);
            this.limitImageMin = extras.getInt(KEY_LIMIT_MIN_IMAGE, 2);
            if (this.limitImageMin > this.limitImageMax) {
                finish();
            }
            if (this.limitImageMin < 1) {
                finish();
            }
        }
        this.pWHItemSelected = (((int) ((((float) getDisplayInfo(this).heightPixels) / 100.0f) * 25.0f)) / 100) * 80;
        this.pWHBtnDelete = (this.pWHItemSelected / 100) * 25;
        getSupportActionBar().setTitle(R.string.text_title_activity_album);
        this.gridViewListAlbum = findViewById(R.id.gridViewListAlbum);
        this.txtTotalImage = findViewById(R.id.txtTotalImage);
        findViewById(R.id.btnDone).setOnClickListener(this);
        this.layoutListItemSelect = findViewById(R.id.layoutListItemSelect);
        this.horizontalScrollView = findViewById(R.id.horizontalScrollView);
        this.horizontalScrollView.getLayoutParams().height = this.pWHItemSelected;
        this.gridViewAlbum = findViewById(R.id.gridViewAlbum);
        this.loadingView = findViewById(R.id.loadingView);

        this.mHandler = new Handler() {
            public void handleMessage(Message message) {
                super.handleMessage(message);

            }
        };
        try {
            Collections.sort(this.dataAlbum, new Comparator<ImageModel>() {
                public int compare(ImageModel imageModel, ImageModel imageModel2) {
                    return imageModel.getName().compareToIgnoreCase(imageModel2.getName());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.albumAdapter = new AlbumAdapter(this, R.layout.piclist_row_album, this.dataAlbum);
        this.albumAdapter.setOnItem(this);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            new GetItemAlbum().execute(new Void[0]);
        }else {
            if (isPermissionGranted("android.permission.READ_EXTERNAL_STORAGE")) {
                new GetItemAlbum().execute(new Void[0]);
            } else {
                requestPermission("android.permission.READ_EXTERNAL_STORAGE", 1001);
            }
        }

        updateTxtTotalImage();
    }

    private boolean isPermissionGranted(String str) {
        return ContextCompat.checkSelfPermission(this, str) == 0;
    }

    private void requestPermission(String str, int i) {
        ActivityCompat.shouldShowRequestPermissionRationale(this, str);
        ActivityCompat.requestPermissions(this, new String[]{str}, i);
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
        if (i == 1001) {
            if (iArr.length <= 0 || iArr[0] != 0) {
                finish();
            } else {
                new GetItemAlbum().execute(new Void[0]);
            }
        } else if (i == 1002 && iArr.length > 0) {
            int i2 = iArr[0];
        }
    }


    public boolean Check(String str, ArrayList<String> arrayList) {
        return !arrayList.isEmpty() && arrayList.contains(str);
    }

    public void showDialogSortAlbum() {
        String[] stringArray = getResources().getStringArray(R.array.array_sort_value);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.text_title_dialog_sort_by_album));
        builder.setSingleChoiceItems(stringArray, this.position, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        MaxAdManager.INSTANCE.checkTap(PickImageActivity.this,()->{
                            PickImageActivity.this.position = i;
                            Collections.sort(PickImageActivity.this.dataAlbum, new Comparator<ImageModel>() {
                                public int compare(ImageModel imageModel, ImageModel imageModel2) {
                                    return imageModel.getName().compareToIgnoreCase(imageModel2.getName());
                                }
                            });
                            PickImageActivity.this.refreshGridViewAlbum();
                            Log.e("TAG", "showDialogSortAlbum by NAME");
                            return null;
                        });

                        break;
                    case 1:
                        MaxAdManager.INSTANCE.checkTap(PickImageActivity.this,()->{
                            PickImageActivity.this.position = i;
                            PickImageActivity.this.doinBackground();
                            Log.e("TAG", "showDialogSortAlbum by Size");
                            return null;
                        });

                        break;
                    case 2:
                        MaxAdManager.INSTANCE.checkTap(PickImageActivity.this,()->{
                            PickImageActivity.this.position = i;
                            Collections.sort(PickImageActivity.this.dataAlbum, new Comparator<ImageModel>() {
                                public int compare(ImageModel imageModel, ImageModel imageModel2) {
                                    int i = (PickImageActivity.getFolderSize(new File(imageModel.getPathFolder())) > PickImageActivity.getFolderSize(new File(imageModel2.getPathFolder())) ? 1 : (PickImageActivity.getFolderSize(new File(imageModel.getPathFolder())) == PickImageActivity.getFolderSize(new File(imageModel2.getPathFolder())) ? 0 : -1));
                                    if (i > 0) {
                                        return -1;
                                    }
                                    return i < 0 ? 1 : 0;
                                }
                            });
                            PickImageActivity.this.refreshGridViewAlbum();
                            Log.e("TAG", "showDialogSortAlbum by Date");

                            return null;
                        });

                        break;
                }
                PickImageActivity.this.sortDialog.dismiss();
            }
        });
        this.sortDialog = builder.create();
        this.sortDialog.show();
    }

    public void refreshGridViewAlbum() {
        this.albumAdapter = new AlbumAdapter(this, R.layout.piclist_row_album, this.dataAlbum);
        this.albumAdapter.setOnItem(this);
        this.gridViewAlbum.setAdapter(this.albumAdapter);
        this.gridViewAlbum.setVisibility(View.GONE);
        this.gridViewAlbum.setVisibility(View.VISIBLE);
    }

    public void showDialogSortListAlbum() {
        String[] stringArray = getResources().getStringArray(R.array.array_sort_value);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.text_title_dialog_sort_by_photo));
        builder.setSingleChoiceItems(stringArray, this.position, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        PickImageActivity.this.position = i;
                        PickImageActivity.this.doinBackgroundPhoto(i);
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        });
        this.sortDialog = builder.create();
        this.sortDialog.show();
    }

    public void refreshGridViewListAlbum() {
        this.listAlbumAdapter = new ListAlbumAdapter(this, R.layout.piclist_row_list_album, this.dataListPhoto);
        this.listAlbumAdapter.setOnListAlbum(this);
        this.gridViewListAlbum.setAdapter(this.listAlbumAdapter);
        this.gridViewListAlbum.setVisibility(View.GONE);
        this.gridViewListAlbum.setVisibility(View.VISIBLE);
    }

    public static long getFolderSize(File file) {
        File[] listFiles;
        boolean z;
        if (file == null || !file.exists() || (listFiles = file.listFiles()) == null || listFiles.length <= 0) {
            return 0;
        }
        long j = 0;
        for (File file2 : listFiles) {
            if (file2.isFile()) {
                int i = 0;
                while (true) {
                    if (i >= Constants.FORMAT_IMAGE.size()) {
                        z = false;
                        break;
                    } else if (file2.getName().endsWith(Constants.FORMAT_IMAGE.get(i))) {
                        z = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (z) {
                    j++;
                }
            }
        }
        return j;
    }


    public void addItemSelect(final ImageModel imageModel) {
        imageModel.setId(this.listItemSelect.size());
        this.listItemSelect.add(imageModel);
        updateTxtTotalImage();
        final View inflate = View.inflate(this, R.layout.piclist_item_selected, (ViewGroup) null);
        ImageView imageView = (ImageView) inflate.findViewById(R.id.imageItem);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ((RequestBuilder) Glide.with((Activity) this).load(imageModel.getPathFile()).placeholder((int) R.drawable.piclist_icon_default)).into(imageView);
        ((ImageView) inflate.findViewById(R.id.btnDelete)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MaxAdManager.INSTANCE.checkTap(PickImageActivity.this,()->{
                    PickImageActivity.this.layoutListItemSelect.removeView(inflate);
                    PickImageActivity.this.listItemSelect.remove(imageModel);
                    PickImageActivity.this.updateTxtTotalImage();

                    return null;
                });

            }
        });
        this.layoutListItemSelect.addView(inflate);
        inflate.startAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_fade_in));
        sendScroll();
    }


    public void updateTxtTotalImage() {
        this.txtTotalImage.setText(String.format(getResources().getString(R.string.text_images), new Object[]{Integer.valueOf(this.listItemSelect.size())}));
    }

    private void sendScroll() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        PickImageActivity.this.horizontalScrollView.fullScroll(66);
                    }
                });
            }
        }).start();
    }


    public void showListAlbum(String str) {
        getSupportActionBar().setTitle((CharSequence) new File(str).getName());
        this.listAlbumAdapter = new ListAlbumAdapter(this, R.layout.piclist_row_list_album, this.dataListPhoto);
        this.listAlbumAdapter.setOnListAlbum(this);
        this.gridViewListAlbum.setAdapter(this.listAlbumAdapter);
        this.gridViewListAlbum.setVisibility(View.VISIBLE);
        new GetItemListAlbum(str).execute(new Void[0]);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btnDone) {
            MaxAdManager.INSTANCE.checkTap(PickImageActivity.this,()->{
                ArrayList<String> listString = getListString(PickImageActivity.this.listItemSelect);
                if (listString.size() >= PickImageActivity.this.limitImageMin) {
                    done(listString);
                    return null;
                }
                Toast.makeText(PickImageActivity.this, "Please select at lease " + PickImageActivity.this.limitImageMin + " images", Toast.LENGTH_SHORT).show();

                return null;
            });


        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_pick_image, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.btnSort) {
            MaxAdManager.INSTANCE.checkTap(PickImageActivity.this,()->{
                if (this.gridViewListAlbum.getVisibility() == View.GONE) {
                    Log.d("tag", "1");
                    showDialogSortAlbum();
                } else {
                    showDialogSortListAlbum();
                    Log.d("tag", ExifInterface.GPS_MEASUREMENT_2D);
                }

                return null;
            });

        } else if (menuItem.getItemId() == 16908332) {
            MaxAdManager.INSTANCE.checkTap(PickImageActivity.this,()->{
                onBackPressed();

                return null;
            });

        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void done(ArrayList<String> arrayList) {
        Intent intent = new Intent(this, PuzzleViewActivity.class);
        intent.putStringArrayListExtra(KEY_DATA_RESULT, arrayList);
        startActivity(intent);
    }


    public ArrayList<String> getListString(ArrayList<ImageModel> arrayList) {
        ArrayList<String> arrayList2 = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            arrayList2.add(arrayList.get(i).getPathFile());
        }
        return arrayList2;
    }


    @Override
    protected void onResume() {
        super.onResume();

        //MaxAdBanner>>done
        if (!SharePreferenceUtil.isPurchased(PickImageActivity.this)) {
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

    public boolean checkFile(File file) {
        if (file == null) {
            return false;
        }
        if (!file.isFile()) {
            return true;
        }
        String name = file.getName();
        if (name.startsWith(".") || file.length() == 0) {
            return false;
        }
        for (int i = 0; i < Constants.FORMAT_IMAGE.size(); i++) {
            if (name.endsWith(Constants.FORMAT_IMAGE.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void onBackPressed() {
        if (this.gridViewListAlbum.getVisibility() == View.VISIBLE) {
            this.dataListPhoto.clear();
            this.listAlbumAdapter.notifyDataSetChanged();
            this.gridViewListAlbum.setVisibility(View.GONE);
            getSupportActionBar().setTitle((CharSequence) getResources().getString(R.string.text_title_activity_album));
            return;
        }
        super.onBackPressed();
    }

    public static DisplayMetrics getDisplayInfo(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }


    public void doinBackgroundPhoto(final int i) {
        new AsyncTask<String, String, Void>() {

            public void onPreExecute() {


                super.onPreExecute();
            }


            public Void doInBackground(String... strArr) {
                if (i == 0) {
                    try {
                        Collections.sort(PickImageActivity.this.dataListPhoto, new Comparator<ImageModel>() {
                            public int compare(ImageModel imageModel, ImageModel imageModel2) {
                                return imageModel.getName().compareToIgnoreCase(imageModel2.getName());
                            }
                        });
                        return null;
                    } catch (Exception e) {
                        return null;
                    }
                } else if (i == 1) {
                    Collections.sort(PickImageActivity.this.dataListPhoto, new Comparator<ImageModel>() {
                        public int compare(ImageModel imageModel, ImageModel imageModel2) {
                            int i = (PickImageActivity.getFolderSize(new File(imageModel.getPathFolder())) > PickImageActivity.getFolderSize(new File(imageModel2.getPathFolder())) ? 1 : (PickImageActivity.getFolderSize(new File(imageModel.getPathFolder())) == PickImageActivity.getFolderSize(new File(imageModel2.getPathFolder())) ? 0 : -1));
                            if (i > 0) {
                                return -1;
                            }
                            return i < 0 ? 1 : 0;
                        }
                    });
                    return null;
                } else if (i != 2) {
                    return null;
                } else {
                    Collections.sort(PickImageActivity.this.dataListPhoto, new Comparator<ImageModel>() {
                        public int compare(ImageModel imageModel, ImageModel imageModel2) {
                            File file = new File(imageModel.getPathFolder());
                            File file2 = new File(imageModel2.getPathFolder());
                            if (file.lastModified() > file2.lastModified()) {
                                return -1;
                            }
                            return file.lastModified() < file2.lastModified() ? 1 : 0;
                        }
                    });
                    return null;
                }
            }


            public void onPostExecute(Void voidR) {
                super.onPostExecute(voidR);
                PickImageActivity.this.refreshGridViewListAlbum();

            }
        }.execute(new String[0]);
    }


    public void doinBackground() {
        new AsyncTask<String, String, Void>() {

            public void onPreExecute() {

                super.onPreExecute();
            }


            public Void doInBackground(String... strArr) {
                Collections.sort(PickImageActivity.this.dataAlbum, new Comparator<ImageModel>() {
                    public int compare(ImageModel imageModel, ImageModel imageModel2) {
                        File file = new File(imageModel.getPathFolder());
                        File file2 = new File(imageModel2.getPathFolder());
                        if (file.lastModified() > file2.lastModified()) {
                            return -1;
                        }
                        return file.lastModified() < file2.lastModified() ? 1 : 0;
                    }
                });
                return null;
            }


            public void onPostExecute(Void voidR) {
                super.onPostExecute(voidR);
                PickImageActivity.this.refreshGridViewAlbum();

            }
        }.execute(new String[0]);
    }

    public void OnItemAlbumClick(int i) {
        showListAlbum(this.dataAlbum.get(i).getPathFolder());
    }

    public void OnItemListAlbumClick(ImageModel imageModel) {
        if (this.listItemSelect.size() < this.limitImageMax) {
            addItemSelect(imageModel);
            return;
        }
        Toast.makeText(this, "Limit " + this.limitImageMax + " images", Toast.LENGTH_SHORT).show();
    }
}
