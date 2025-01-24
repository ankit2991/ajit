package com.lockerroom.face.filters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.lockerroom.face.activities.EditImageActivity;
import com.lockerroom.face.utils.AssetUtils;

import org.jetbrains.annotations.Nullable;
import org.wysaid.common.SharedContext;
import org.wysaid.nativePort.CGEImageHandler;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class FilterUtils {
    public static final FilterBean[] EFFECT_CONFIGS = {new FilterBean("", "Original"), new FilterBean("@adjust lut filters/bright01.webp", "Fresh 01"), new FilterBean("@adjust lut filters/bright02.webp", "Fresh 02"), new FilterBean("@adjust lut filters/bright03.webp", "Fresh 03"), new FilterBean("@adjust lut filters/bright05.webp", "Fresh 04"), new FilterBean("@adjust lut filters/euro01.webp", "Euro 01"), new FilterBean("@adjust lut filters/euro02.webp", "Euro 02"), new FilterBean("@adjust lut filters/euro05.webp", "Euro 03"), new FilterBean("@adjust lut filters/euro04.webp", "Euro 04"), new FilterBean("@adjust lut filters/euro06.webp", "Euro 05"), new FilterBean("@adjust lut filters/euro07.webp", "Euro 06"), new FilterBean("@adjust lut filters/film01.webp", "Film 01"), new FilterBean("@adjust lut filters/film02.webp", "Film 02"), new FilterBean("@adjust lut filters/film03.webp", "Film 03"), new FilterBean("@adjust lut filters/film04.webp", "Film 04"), new FilterBean("@adjust lut filters/film05.webp", "Film 05"), new FilterBean("@adjust lut filters/lomo1.webp", "Lomo 01"), new FilterBean("@adjust lut filters/lomo2.webp", "Lomo 02"), new FilterBean("@adjust lut filters/lomo3.webp", "Lomo 03"), new FilterBean("@adjust lut filters/lomo4.webp", "Lomo 04"), new FilterBean("@adjust lut filters/lomo5.webp", "Lomo 05"), new FilterBean("@adjust lut filters/movie01.webp", "Movie 01"), new FilterBean("@adjust lut filters/movie02.webp", "Movie 02"), new FilterBean("@adjust lut filters/movie03.webp", "Movie 03"), new FilterBean("@adjust lut filters/movie04.webp", "Movie 04"), new FilterBean("@adjust lut filters/movie05.webp", "Movie 05")};
    public static final FilterBean[] OVERLAY_CONFIG = {new FilterBean("", ""), new FilterBean("#unpack @krblend sr overlay/o_1_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o_3_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o_4_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o_5_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-6_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-7_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-8_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-9_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-10_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-11_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-13_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-12_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-14_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-15_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-16_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-17_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-18_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-19_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-20_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-21_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-22_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-23_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-25_optimized.webp 100", ""), new FilterBean("#unpack @krblend sr overlay/o-24_optimized.webp 100", "")};

    public static class FilterBean {
        private String config;
        private String name;

        FilterBean(String str, String str2) {
            this.config = str;
            this.name = str2;
        }

        public String getConfig() {
            return this.config;
        }

        public void setConfig(String str) {
            this.config = str;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String str) {
            this.name = str;
        }
    }

    public static Bitmap getBlurImageFromBitmap(Bitmap bitmap) {
        try {
            SharedContext create = SharedContext.create();
            create.makeCurrent();
            CGEImageHandler cGEImageHandler = new CGEImageHandler();
            cGEImageHandler.initWithBitmap(bitmap);
            cGEImageHandler.setFilterWithConfig("@blur lerp 0.6");
            cGEImageHandler.processFilters();
            Bitmap resultBitmap = cGEImageHandler.getResultBitmap();
            create.release();
            return resultBitmap;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    public static Bitmap getBlurImageFromBitmap(Bitmap bitmap, float f) {
        try {
            SharedContext create = SharedContext.create();
            create.makeCurrent();
            CGEImageHandler cGEImageHandler = new CGEImageHandler();
            cGEImageHandler.initWithBitmap(bitmap);
            cGEImageHandler.setFilterWithConfig(MessageFormat.format("@blur lerp {0}", new Object[]{(f / 10.0f) + ""}));
            cGEImageHandler.processFilters();
            Bitmap resultBitmap = cGEImageHandler.getResultBitmap();
            create.release();
            return resultBitmap;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return bitmap;
        }

    }

    public static Bitmap cloneBitmap(Bitmap bitmap) {
        try {
            SharedContext create = SharedContext.create();
            create.makeCurrent();
            CGEImageHandler cGEImageHandler = new CGEImageHandler();
            cGEImageHandler.initWithBitmap(bitmap);
            cGEImageHandler.setFilterWithConfig("");
            cGEImageHandler.processFilters();
            Bitmap resultBitmap = cGEImageHandler.getResultBitmap();
            create.release();
            return resultBitmap;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    public static Bitmap getBlackAndWhiteImageFromBitmap(Bitmap bitmap) {
        try {
            SharedContext create = SharedContext.create();
            create.makeCurrent();
            CGEImageHandler cGEImageHandler = new CGEImageHandler();
            cGEImageHandler.initWithBitmap(bitmap);
            cGEImageHandler.setFilterWithConfig("@adjust saturation 0");
            cGEImageHandler.processFilters();
            Bitmap resultBitmap = cGEImageHandler.getResultBitmap();
            create.release();
            return resultBitmap;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return bitmap;
        }

    }

    public static Bitmap getBitmapWithFilter(Bitmap bitmap, String str) {
        try {
            SharedContext create = SharedContext.create();
            create.makeCurrent();
            CGEImageHandler cGEImageHandler = new CGEImageHandler();
            cGEImageHandler.initWithBitmap(bitmap);
            cGEImageHandler.setFilterWithConfig(str);
            cGEImageHandler.setFilterIntensity(0.8f);
            cGEImageHandler.processFilters();
            Bitmap resultBitmap = cGEImageHandler.getResultBitmap();
            create.release();
            return resultBitmap;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    public static List<Bitmap> getLstBitmapWithFilter(Bitmap bitmap, Context context) {
//        Changed While fixing the issues  and crashes (nullable anotations)
        ArrayList<@Nullable Bitmap> arrayList = new ArrayList<@Nullable Bitmap>();
        try {
            SharedContext create = SharedContext.create();
            create.makeCurrent();
            CGEImageHandler cGEImageHandler = new CGEImageHandler();
            cGEImageHandler.initWithBitmap(bitmap);
            for (FilterBean config : EFFECT_CONFIGS) {
                cGEImageHandler.setFilterWithConfig(config.getConfig());
                cGEImageHandler.processFilters();
                arrayList.add(cGEImageHandler.getResultBitmap());
            }
            create.release();
            return arrayList;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            arrayList.add(bitmap);
            arrayList.add(loadBitmapFromAssets(context, "overlay/fresh01.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/fresh02.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/fresh03.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/fresh04.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/euro01.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/euro02.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/euro03.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/euro04.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/euro05.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/euro06.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/film01.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/film02.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/film03.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/film04.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/film05.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/lomo01.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/lomo02.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/lomo03.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/lomo04.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/lomo05.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/movie01.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/movie02.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/movie03.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/movie04.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/movie05.webp"));
            return arrayList;
        }
    }


    public static List<Bitmap> getLstBitmapWithOverlay(Bitmap bitmap, Context context) {

        ArrayList arrayList = new ArrayList();
        try {
            SharedContext create = SharedContext.create();
            create.makeCurrent();
            CGEImageHandler cGEImageHandler = new CGEImageHandler();
            cGEImageHandler.initWithBitmap(bitmap);
            for (FilterBean config : OVERLAY_CONFIG) {
                cGEImageHandler.setFilterWithConfig(config.getConfig());
                cGEImageHandler.processFilters();
                arrayList.add(cGEImageHandler.getResultBitmap());
            }
            create.release();
            return arrayList;
        } catch (IllegalArgumentException e) {
            arrayList.add(bitmap);
            arrayList.add(loadBitmapFromAssets(context, "overlay/o_1_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o_3_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o_4_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o_5_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-6_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-7_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-8_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-9_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-10_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-11_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-13_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-12_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-14_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-15_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-16_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-17_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-18_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-19_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-20_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-21_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-22_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-23_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-25_optimized.webp"));
            arrayList.add(loadBitmapFromAssets(context, "overlay/o-24_optimized.webp"));

            return arrayList;
        }


    }

    public static Bitmap loadBitmapFromAssets(Context context, String str) {
        InputStream inputStream;
        try {
            inputStream = context.getAssets().open(str);
            Bitmap decodeStream = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return decodeStream;
        } catch (Exception e) {
            return null;
        }
    }


}
