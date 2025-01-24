package macro.hd.wallpapers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.Interface.Activity.SplashActivity;
import macro.hd.wallpapers.Model.Wallpapers;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.Logger;

public class NewAppWidget extends AppWidgetProvider {

    SettingStore setting;
    private List<Wallpapers> movieList = new ArrayList<Wallpapers>();
    private static final String changeWall = "change";
    private static final String startApp = "startApp";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Logger.e("NewAppWidget","onUpdate");
        ComponentName thisWidget = new ComponentName(context, NewAppWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : allWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

            Intent intent = new Intent(context, SplashActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

//            views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
            views.setOnClickPendingIntent(R.id.appwidget_text,  getPendingSelfIntent(context, startApp));
            views.setOnClickPendingIntent(R.id.change_back, getPendingSelfIntent(context, changeWall));
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    int count;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        settingStore=SettingStore.getInstance(context);

        Logger.e("NewAppWidget","onReceive := "+intent.getAction());
        if (changeWall.equals(intent.getAction())) {

            Logger.e("onReceive","Enter 1 ");
            FetchImages(context);
            if (movieList.size() == 0 || movieList.size() == 1) {
                return;
            }
            Toast.makeText(context, "Applying wallpaper. Please wait...", Toast.LENGTH_LONG).show();
            setting = SettingStore.getInstance(context);

            count = setting.getLastImageCount();
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);

            String dst = movieList.get(count).getImg();
            Glide.with(context)
                    .asBitmap()
                    .load(dst)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            new Thread() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void run() {
                                    super.run();
                                    try {
                                        if (wallpaperManager == null)
                                            return;

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            if (resource != null && wallpaperManager != null)
                                                wallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM);
                                        } else {
                                            if (resource != null)
                                                wallpaperManager.setBitmap(resource);
                                            else {
                                                InputStream ins = new URL("file://" + dst).openStream();
                                                wallpaperManager.setStream(ins);
                                            }
                                        }

                                        if (count == (movieList.size() - 1)) {
                                            count = 0;
                                        } else {
                                            count++;
                                        }
                                        setting.setLastImageCount(count);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) { }
                    });
        }else if (startApp.equals(intent.getAction())){
            Intent i = new Intent (context, SplashActivity.class);
            i.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity (i);
            Logger.e("NewAppWidget","onReceive 1235468 ");
        }
    }

    SettingStore settingStore;

    private void FetchImages(Context c) {

        if (settingStore.FirstLaunch())
        {
            Intent i = new Intent (c, SplashActivity.class);
            i.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity (i);
            return;
        }
        Logger.e("Fetch image","enter");
        ArrayList<Wallpapers> filenames = new ArrayList<Wallpapers>();
        String path = CommonFunctions.getSavedFilePath();

        File directory = new File(path);
        File[] files = directory.listFiles();

//        Log.e("size",files.length+" : length");
        if (files==null || files.length==0 ||files.length==1 )
        {
            Toast.makeText(c, "Download at least 2 or more Wallpapers before using Wallpaper change", Toast.LENGTH_SHORT).show();
            return;
        }

        Arrays.sort(files, new Comparator<File>(){
            public int compare(File f1, File f2) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        return Long.compare(f2.lastModified(), f1.lastModified());
                    } else {
                        return f2.lastModified() < f1.lastModified() ? -1 : f2.lastModified() > f1.lastModified() ? 1 : 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                String file_name = files[i].getAbsolutePath();
                Logger.e("file_name", "" + file_name);
                // you can store name to arraylist and use it later
                Wallpapers post = new Wallpapers();
                post.setPostId("-100");
                post.setImg(file_name);
                filenames.add(post);
            }
        }

        if (filenames.size() == 0) {
            return;
        }

        movieList.addAll(filenames);
        return;
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Logger.e("NewAppWidget","onEnabled ");
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Logger.e("NewAppWidget","onDisabled ");
    }
}