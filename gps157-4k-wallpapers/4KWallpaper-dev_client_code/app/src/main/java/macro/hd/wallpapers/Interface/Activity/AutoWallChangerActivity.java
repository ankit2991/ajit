package macro.hd.wallpapers.Interface.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import macro.hd.wallpapers.AutoWallpaperChangerService;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Interface.Adapters.AutoWallChangerAdapter;
import macro.hd.wallpapers.AppController.RequestManager;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.ImagesCallback;
import macro.hd.wallpapers.Utilily.Logger;
import macro.hd.wallpapers.Utilily.MarshMallowPermission;
import macro.hd.wallpapers.Utilily.SpacesItemDecoration;
import macro.hd.wallpapers.NetworkManager.NetworkCommunicationManager;
import macro.hd.wallpapers.NetworkManager.WebServiceError;
import macro.hd.wallpapers.Model.Category;
import macro.hd.wallpapers.Model.IModel;
import macro.hd.wallpapers.Model.Wallpapers;
import macro.hd.wallpapers.Model.WallInfoModel;
import macro.hd.wallpapers.notifier.EventNotifier;
import macro.hd.wallpapers.notifier.EventTypes;
import macro.hd.wallpapers.notifier.NotifierFactory;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

//import org.jetbrains.annotations.NotNull;


public class AutoWallChangerActivity extends BaseActivity implements
        OnItemSelectedListener, ImagesCallback {

    private static final String TAG = "AutoWallChangerActivity__";

    Spinner spinner_frame_duration;
    SettingStore setting;
    private WallpaperInfo old_info;
    private static final int SELECT_FILE = 1;
    Bitmap bm_gallery_camera = null;
    Bitmap bm_original = null;
    Uri selectedImageUri;
    private RecyclerView recyclerView;
    ArrayList< Category > list = new ArrayList<>();
    AutoWallChangerAdapter homeExploreAdapter;
    MarshMallowPermission marshMallowPermission;
    SwitchCompat chx_double;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setTheme();
        setLocale();
        setContentView(R.layout.activity_auto_wall_changer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        marshMallowPermission = new MarshMallowPermission(AutoWallChangerActivity.this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.menu_auto_wallpaper));

        setting = SettingStore.getInstance(getApplicationContext());

        recyclerView = findViewById(R.id.idRecyclerViewHorizontalList);
        chx_double = findViewById(R.id.chx_double);
        if (setting.getIsDoubleTap())
            chx_double.setChecked(true);
        else
            chx_double.setChecked(false);

        int[] int_array = AppConstant.time_frame_name;
        Log.e("int array", "" + int_array);

        String[] str_array = new String[int_array.length];
        for (int k = 0; k < int_array.length; k++) {
            str_array[k] = getString(int_array[k]);
            Log.e("string array", k + " :" + str_array[k]);
        }

        spinner_frame_duration = (Spinner) findViewById(R.id.spinner_frame_duration);
        ArrayAdapter< String > adapter = new ArrayAdapter< String >(this,
                R.layout.list_spinner_item, str_array);
        spinner_frame_duration.setAdapter(adapter);
        spinner_frame_duration.setOnItemSelectedListener(this);

        try {
            spinner_frame_duration.setSelection(setting.getTimeDurationIndex());
        } catch (Exception e) {
            e.printStackTrace();
        }

//		spinner_frame_duration.getBackground().setColorFilter(ContextCompat.getColor(this,
//				R.color.white), PorterDuff.Mode.SRC_ATOP);

        WallpaperManager wpm = WallpaperManager.getInstance(AutoWallChangerActivity.this);
        if (wpm != null)
            old_info = wpm.getWallpaperInfo();

        fillUpList();


        findViewById(R.id.btn_set_wallpaper).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (marshMallowPermission != null && !marshMallowPermission.checkPermissionForExternalStorage()) {
                    isAutoChangerCalled = true;
                    checkPermission();
                    return;
                }

                boolean needToCallPrevew = true;
                if (old_info != null && old_info.getComponent().getClassName().equals(AutoWallpaperChangerService.class.getCanonicalName())) {
                    needToCallPrevew = false;
                }

                if (AppConstant.isWallSetDirectly && !needToCallPrevew) {
                    CommonFunctions.showDialogConfirmation(AutoWallChangerActivity.this, getString(R.string.dialog_title_info), getString(R.string.label_update_ask), new CommonFunctions.DialogOnButtonClickListener() {
                        @Override
                        public void onOKButtonCLick() {
                            saveToStore(true);
                        }

                        @Override
                        public void onCancelButtonCLick() {
                        }
                    });

                } else
                    saveToStore(false);
            }
        });
    }


//	private ProgressDialog progressDialog;

    private void showLoader() {
//		progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
//		progressDialog.setMessage("Downloading wallpaper");
//		progressDialog.setCancelable(false);
//		progressDialog.show();
        showLoadingDialogMsg(getString(R.string.get_wall));
    }

    private void dismissLoader() {
//		if (progressDialog != null && progressDialog.isShowing()) {
//			progressDialog.dismiss();
//			progressDialog = null;
//		}
        try {
            hideLoadingDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onItemSelected(AdapterView< ? > arg0, View view, int position,
                               long id) {
//		setting.setTimeDurationIndex(position);
    }

    @Override
    public void onNothingSelected(AdapterView< ? > arg0) {
        // TODO Auto-generated method stub
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

    private void fillUpList() {

        if (!isPermissionGranted()) {
            return;
        }

        String lastResponse = setting.getCatIdAuto();
        Logger.e(TAG, "" + lastResponse);
        String[] splite = lastResponse.split(",");
        List< Category > temp = new ArrayList<>();
        try {
            temp = UserInfoManager.getInstance(getApplicationContext()).getUserInfo().getCategory();
            Collections.sort(temp, new Comparator< Category >() {
                public int compare(Category p1, Category p2) {
                    return Integer.valueOf(p1.getPriority()).compareTo(Integer.valueOf(p2.getPriority()));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Remove elements smaller than 10 using
        // Iterator.remove()
        Iterator itr = temp.iterator();
        while (itr.hasNext()) {
            Category category = (Category) itr.next();

            String link = category.getLink();
            if (!TextUtils.isEmpty(link)) {
                String packageName = link.substring(link.indexOf("id=") + 3, link.indexOf("&"));
                Logger.e("CategoryHome", "" + packageName);
//				temp.remove(i);
                itr.remove();
            } else if (!TextUtils.isEmpty(category.getStatus()) && category.getStatus().equalsIgnoreCase("0")) {
//				temp.remove(i);
                itr.remove();
            } else {
                boolean status = false;
                if (setting.getIsFirst()) {
                    status = true;
                } else {
                    String pathtemp = category.getName();
                    for (int j = 0; j < splite.length; j++) {
                        if (splite[j].equalsIgnoreCase("'" + pathtemp + "'")) {
                            status = true;
                            break;
                        }
                    }
                }
                Logger.e(TAG, "Name:" + category.getName() + " status:" + status);
                category.setSelected(status);
            }
        }
        list.clear();
        list.addAll(temp);
        setting.setIsFirst(false);

        homeExploreAdapter = new AutoWallChangerAdapter(this, list);
        homeExploreAdapter.setImagesCallback(this);
//		recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        GridLayoutManager horizontalLayoutManager = new GridLayoutManager(this, 2);
//		LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(SettingActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);

        int spacing = (int) getResources()
                .getDimension(R.dimen.auto_padding_recycle);
//				int spacingInPixels = 1;
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacing));

        recyclerView.setAdapter(homeExploreAdapter);

    }

    private void updateButton() {
        try {

            WallpaperManager wpm = WallpaperManager.getInstance(AutoWallChangerActivity.this);
            old_info = wpm.getWallpaperInfo();

            boolean needToCallPrevew = true;
            if (old_info != null && old_info.getComponent().getClassName().equals(AutoWallpaperChangerService.class.getCanonicalName())) {
                needToCallPrevew = false;
            }

            final TextView btn_set_wallpaper = findViewById(R.id.btn_set_wallpaper);
            if (AppConstant.isWallSetDirectly && !needToCallPrevew) {
                btn_set_wallpaper.setText(getString(R.string.sav_apply));
            } else
                btn_set_wallpaper.setText(getString(R.string.set_auto_Wallpaper));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean saveToStore(final boolean isDirect) {

        StringBuilder finalString = new StringBuilder();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isSelected()) {
                    finalString.append("'" + list.get(i).getName() + "'");
                    finalString.append(",");
                }
            }
        }
        if (CommonFunctions.isEmpty(finalString.toString())) {
            Toast.makeText(AutoWallChangerActivity.this, getString(R.string.select_atleast_one), Toast.LENGTH_SHORT).show();
            return false;
        }

        showLoader();

        String store = finalString.toString();
        store = store.substring(0, store.length() - 1);

        if (!TextUtils.isEmpty(store))
            setting.setAutoCategoryTemp(store);
        else
            setting.setAutoCategoryTemp("");


        setting.setTimeDurationIndexTemp(spinner_frame_duration.getSelectedItemPosition());

        setting.setIsDoubleTap(chx_double.isChecked());


        RequestManager manager = new RequestManager(this);
        manager.autoWallpaperService(store, new NetworkCommunicationManager.CommunicationListnerNew() {
            @Override
            public void onSuccess(IModel response, int operationCode) {
                try {
                    WallInfoModel model = (WallInfoModel) response;
                    if (model != null && model.getStatus().equalsIgnoreCase("1")) {
                        final Wallpapers post = model.getPost().get(0);
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    final boolean result = downloadImage(post);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (result) {
                                                setting.setLastAutoChangedTimeTemp();
                                                if (!isDirect)
                                                    openAutoWallChanger();
                                                else {
                                                    wallpaperChanged();
                                                    Toast.makeText(AutoWallChangerActivity.this, getString(R.string.wallpaper_updated), Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                CommonFunctions.displayShortToastMessage(AutoWallChangerActivity.this, getString(R.string.unable_download));
                                            }
                                            dismissLoader();
                                        }
                                    });
                                } catch (Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dismissLoader();
                                        }
                                    });
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CommonFunctions.displayShortToastMessage(AutoWallChangerActivity.this, getString(R.string.error_download_wallpaper));
                            dismissLoader();
                        }
                    });
                }
            }

            @Override
            public void onStartLoading() {

            }

            @Override
            public void onFail(WebServiceError errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommonFunctions.displayShortToastMessage(AutoWallChangerActivity.this, getString(R.string.error_download_wallpaper));
                        dismissLoader();
                    }
                });
            }
        });

        return true;
    }


    private String saveImage(String id, Bitmap finalBitmap) {
        String name = CommonFunctions.createAutoChagerDirCache() + "/" + AppConstant.AUTO_WALLPAPER_TEMP_FILE_NAME;
        File file = new File(name);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.WEBP, 90, out);
            out.flush();
            out.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean downloadImage(Wallpapers post) {
        Bitmap bitmapImage = null;
        try {

            String path = CommonFunctions.getDomainImages() + "uhd/" + post.getImg();
            Logger.i("Notiifcation", "Notification ImgPath: " + path);
            bitmapImage = Glide
                    .with(AutoWallChangerActivity.this)
                    .asBitmap()
                    .load(path)
                    .submit()
                    .get();

            String local_path = saveImage(post.getPostId(), bitmapImage);
            if (!TextUtils.isEmpty(local_path)) {
                setting.setAutoWallImageTemp(local_path);
            }
            return new File(local_path).exists();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public String getPath(Uri uri, Activity activity) {
        String[] projection = {MediaColumns.DATA};
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    String filePath = "";

    public void saveBitmap(Bitmap bitmap, boolean saveTemp) {

        File myDir = new File(CommonFunctions.getSavedFilePath());
        myDir.mkdirs();
        if (myDir.exists()) {
        }
        String fname;
        if (saveTemp) {
            fname = "temp.jpg";
        } else {
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);
            fname = "Screenshot-" + n + ".jpg";
        }
        File imagePath = new File(myDir, fname);
        filePath = imagePath.getAbsolutePath();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            if (!saveTemp)
                Toast.makeText(AutoWallChangerActivity.this, getString(R.string.save_img_success),
                        Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }
    }

    private void wallpaperChanged() {
        EventManager.sendEvent(EventManager.ATR_KEY_AUTO_WALL, EventManager.ATR_KEY_LIVE_ACTION, "Set");
        int temp_time_duration = setting.getTimeDurationIndexTemp();
        String temp_auto_category = setting.getAutoCategoryTemp();
        deleteAndReplaceIfFileExist();
        setting.setTimeDurationIndex(temp_time_duration);
        setting.setCatIdAuto(temp_auto_category);
//			updateButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.e("onActivityResult", "resultCode:" + resultCode + " requestCode:" + requestCode);
        if (requestCode == 200) {
            boolean isChanged = false;
            if (resultCode == RESULT_OK) {
                isChanged = true;
            } else if (resultCode == RESULT_CANCELED) {
                WallpaperManager wpm = WallpaperManager.getInstance(AutoWallChangerActivity.this);
                WallpaperInfo info = wpm.getWallpaperInfo();
                if (info != null && (old_info == null || (!old_info.getComponent().getClassName().equals(info.getComponent().getClassName())
                        && info.getComponent().getClassName().equals(AutoWallpaperChangerService.class.getCanonicalName())))) {
                    isChanged = true;
                }
            }
//			isChanged = true;
            if (isChanged) {
                Logger.e(TAG, "onActivityResult:" + isChanged);
                wallpaperChanged();
            }
        } else {
            if (resultCode == RESULT_OK) {

                if (requestCode == SELECT_FILE) {
                    selectedImageUri = data.getData();
                    String tempPath = getPath(selectedImageUri,
                            AutoWallChangerActivity.this);

                    BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
                    bm_original = null;
                    bm_gallery_camera = BitmapFactory.decodeFile(tempPath,
                            btmapOptions);
                    saveBitmap(bm_gallery_camera, false);

                    fillUpList();

                } else if (requestCode == 200) {
                    EventNotifier notifier =
                            NotifierFactory.getInstance().getNotifier(
                                    NotifierFactory.EVENT_NOTIFIER_UPDATE_WALL);
                    notifier.eventNotify(EventTypes.EVENT_UPDATE_WALL, null);
                    updateButton();
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (requestCode == 200 && AppConstant.isWallSetDirectly) {
                    EventNotifier notifier =
                            NotifierFactory.getInstance().getNotifier(
                                    NotifierFactory.EVENT_NOTIFIER_UPDATE_WALL);
                    notifier.eventNotify(EventTypes.EVENT_UPDATE_WALL, null);
                    updateButton();
                }
            }
        }

    }

    private void deleteAndReplaceIfFileExist() {
        String name = CommonFunctions.createAutoChagerDirCache() + "/" + AppConstant.AUTO_WALLPAPER_TEMP_FILE_NAME;
        File temp_file = new File(name);
        if (temp_file.exists()) {
            String name1 = CommonFunctions.createAutoChagerDirCache() + "/" + AppConstant.AUTO_WALLPAPER_FILE_NAME;
            File file1 = new File(name1);
            if (file1.exists()) {
                file1.delete();
            }
            boolean result = temp_file.renameTo(file1);
            if (result) {
                if (file1.exists()) {
                    setting.setAutoWallImage(name1);
                }
            }
            Logger.e(TAG, "Autowallpaper changer FIle Renamed:" + result);
        }
    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        updateButton();
    }

    private static final int REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 1000;

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions( //Method of Fragment
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS_CODE_WRITE_STORAGE
                );
                return false;
            } else {
                return true;
            }
        } else
            return true;
    }


    @Override
    public void click() {
    }

    boolean isAutoChangerCalled;

    private void openAutoWallChanger() {
        try {

            File path = new File(CommonFunctions.getSavedFilePath());
            path.mkdirs();

            EventManager.sendEvent(EventManager.ATR_KEY_AUTO_WALL, EventManager.ATR_KEY_LIVE_ACTION, "Click");

            try {
                Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(AutoWallChangerActivity.this,
                                AutoWallpaperChangerService.class));
//				JesusApplication.getApplication().incrementUserClickCounter();
                startActivityForResult(intent, 200);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    startActivityForResult(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new
                            ComponentName(AutoWallChangerActivity.this, AutoWallpaperChangerService.class))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 200);
                } catch (Exception e2) {
                    Toast.makeText(AutoWallChangerActivity.this, R.string
                            .toast_failed_launch_wallpaper_chooser, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPermission() {

        if (!marshMallowPermission.checkPermissionForExternalStorage()) {
            marshMallowPermission.requestPermissionForExternalStorage();
        } else {
//				Common.renameFolder(this);
//                getCategoryListResponse(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Logger.e("onRequestPermissionsResult", "Activity");
        if (requestCode == REQUEST_PERMISSIONS_CODE_WRITE_STORAGE) {
            if (grantResults != null && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fillUpList();
            } else if (grantResults != null && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, getString(R.string.per_set_Auto), Toast.LENGTH_SHORT).show();
                isPermissionGranted();
            }
        } else {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//			Common.renameFolder(this);
                if (isAutoChangerCalled)
                    openAutoWallChanger();
//			checkPermission();
            } else if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (isAutoChangerCalled)
                    Toast.makeText(this, getString(R.string.per_set_Auto), Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        if (homeExploreAdapter != null) {
            homeExploreAdapter.onDestroy();
            homeExploreAdapter = null;
        }
        marshMallowPermission = null;
        chx_double = null;
        super.onDestroy();
    }


}
