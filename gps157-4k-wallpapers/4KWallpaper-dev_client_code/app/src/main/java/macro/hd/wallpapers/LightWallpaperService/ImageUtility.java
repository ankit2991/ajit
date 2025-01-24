package macro.hd.wallpapers.LightWallpaperService;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import macro.hd.wallpapers.Utilily.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtility {

    public Bitmap getBitmapImage(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getFilesDir());
//        sb.append(Environment.getExternalStorageDirectory());
        sb.append(File.separator);

        String str = context.getSharedPreferences("borderlightwall", 0).getString("image_path", "");

//        String str = "bg2.png";
        Logger.e("getBitmapImage",sb.toString());
        sb.append(str);
        if (!new File(sb.toString()).exists()) {
            return null;
        }

        StringBuilder sb2 = new StringBuilder();
        sb2.append(context.getFilesDir());
        sb2.append(File.separator);
        sb2.append(str);
        return BitmapFactory.decodeFile(sb2.toString());
    }

    public void setImage(Uri uri, Context context) {
        try {
            Bitmap bitmap;
            Bitmap bitmap2;
            StringBuilder sb = new StringBuilder();
            sb.append(context.getFilesDir());
//        sb.append(Environment.getExternalStorageDirectory());
            sb.append(File.separator);
            String fileName="bg_"+System.currentTimeMillis()+".png";
            sb.append(fileName);

            context.getSharedPreferences("borderlightwall", 0).edit().putString("image_path", fileName).apply();

            Logger.e("saved",sb.toString());
            File file = new File(sb.toString());

            StringBuilder sb2 = new StringBuilder();
            sb2.append("Decode start: ");
            sb2.append(System.nanoTime());
            String str = "BL##";
            Log.e(str, sb2.toString());
            try {
                bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                bitmap = null;
            }
            StringBuilder sb3 = new StringBuilder();
            sb3.append("Decode End: ");
            sb3.append(System.nanoTime());
            Log.e(str, sb3.toString());
            Point point = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ((Activity) context).getWindowManager().getDefaultDisplay().getRealSize(point);
            }else
                ((Activity) context).getWindowManager().getDefaultDisplay().getSize(point);
            int i = point.x;
            int i2 = point.y;
            Log.e(str, String.format("DW: %d, OW: %d, DH: %d, OH: %d", new Object[]{Integer.valueOf(i), Integer.valueOf(bitmap.getWidth()), Integer.valueOf(i2), Integer.valueOf(bitmap.getHeight())}));
//        bitmap2=bitmap;
            Logger.e("size",bitmap.getWidth()+" "+ bitmap.getHeight());
            if (i < bitmap.getWidth() || i2 < bitmap.getHeight()) {
                Log.e(str, "Image resized");
                bitmap2 = ThumbnailUtils.extractThumbnail(bitmap, i, i2);
            } else {
                Log.e(str, "Image not resized");
                bitmap2 = ThumbnailUtils.extractThumbnail(bitmap, i, i2);
            }

            Logger.e("resize size",bitmap2.getWidth()+" "+ bitmap2.getHeight());
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                StringBuilder sb4 = new StringBuilder();
                sb4.append("Compress start: ");
                sb4.append(System.nanoTime());
                Log.e(str, sb4.toString());
                bitmap2.compress(CompressFormat.PNG, 100, fileOutputStream);
                StringBuilder sb5 = new StringBuilder();
                sb5.append("Compress end: ");
                sb5.append(System.nanoTime());
                Log.e(str, sb5.toString());
                context.getSharedPreferences("borderlightwall", 0).edit().putBoolean("hasnewimage", true).apply();
                fileOutputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Error e) {
            e.printStackTrace();
        }
    }

    public boolean m2888b(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append(context.getFilesDir());
        sb.append(File.separator);
        String str = context.getSharedPreferences("borderlightwall", 0).getString("image_path", "");
        sb.append(str);
        Log.e("Path >>", sb.toString());
        return new File(sb.toString()).exists();
    }
}
