package macro.hd.wallpapers.ExclusiveService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.text.TextUtils;
import android.util.Log;

import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ExclusiveLiveWallpaperRenderer implements GLSurfaceView.Renderer {
    private final static int REFRESH_RATE = 60;
    private final static float MAX_BIAS_RANGE = 0.003f;
    //private final static float MAX_BIAS_RANGE = 0.15f;
    String TAG = "ExclusiveLiveWallpaperRenderer";

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final Context mContext;
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private final float transitionStep = REFRESH_RATE / ExclusiveLiveWallpaperService.SENSOR_RATE;
    private ExclusiveKWallpaper wallpaper;
    private float scrollStep = 1f;
    /*private Queue<Float> scrollOffsetXQueue = new CircularFifoQueue<>(10);*/
    private Queue<Float> scrollOffsetXQueue = new ArrayDeque<>(10);
    private float scrollOffsetX = 0.5f;// , offsetY = 0.5f;
    private float scrollOffsetXBackup = 0.5f;
    private float currentOrientationOffsetX, currentOrientationOffsetY;
    private float orientationOffsetX, orientationOffsetY;
    private Callbacks mCallbacks;
    private float screenAspectRatio;
    private int screenH;
    private float wallpaperAspectRatio;
    private float biasRange;
    private float scrollRange;
    private boolean scrollMode = true;
    private int delay = 3;
    private final Runnable transition = new Runnable() {
        @Override
        public void run() {
            try {
                transitionCal();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private ScheduledFuture<?> transitionHandle;
    private boolean needsRefreshWallpaper;
    private boolean isDefaultWallpaper;
    private float preA;
    private float preB;

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    private String tempPath;

    public ExclusiveLiveWallpaperRenderer(Context context, Callbacks callbacks) {
        mContext = context;
        mCallbacks = callbacks;
    }

    public void release() {
        // TODO stuff to release
        if (wallpaper != null)
            wallpaper.destroy();
        stopTransition();
        scheduler.shutdown();
    }

    /**
     * The Surface is created/init()
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Logger.e("ExclusiveLiveWallpaperRenderer","onSurfaceCreated: "+tempPath);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFuncSeparate(GLES20.GL_SRC_ALPHA,
                GLES20.GL_ONE_MINUS_SRC_ALPHA, GLES20.GL_ONE, GLES20.GL_ONE);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        ExclusiveKWallpaper.initGl();
    }

    public void startTransition() {
        stopTransition();
        try {
            transitionHandle =
                    scheduler.scheduleAtFixedRate(transition, 0, 1000 / REFRESH_RATE, TimeUnit
                            .MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopTransition() {
        if (transitionHandle != null) transitionHandle.cancel(true);
    }

    /**
     * Here we do our drawing
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        if (needsRefreshWallpaper) {
            loadTexture();
            needsRefreshWallpaper = false;
        }
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        float x = preA * (-2 * scrollOffsetX + 1) + currentOrientationOffsetX;
        float y = currentOrientationOffsetY;
        Matrix.setLookAtM(mViewMatrix, 0, x, y, preB, x, y, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Draw square
        if(wallpaper!=null)
            wallpaper.draw(mMVPMatrix);

    }


    /**
     * If the surface changes, reset the view
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Logger.e("ExclusiveLiveWallpaperRenderer","onSurfaceChanged:"+height);
        if (height == 0) { // Prevent A Divide By Zero By
            height = 1; // Making Height Equal One
        }

        screenAspectRatio = (float) width / (float) height;
        screenH = height;

        GLES20.glViewport(0, 0, width, height);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -0.1f * screenAspectRatio,
                0.1f * screenAspectRatio, -0.1f, 0.1f, 0.1f, 2);

        needsRefreshWallpaper = true;
//        loadTexture();
        mCallbacks.requestRender();

//        setIsDefaultWallpaper(false);
    }

    void setOffset(float offsetX, float offsetY) {
        if (scrollMode) {
            scrollOffsetXBackup = offsetX;
            scrollOffsetXQueue.offer(offsetX);
        } else {
            scrollOffsetXBackup = offsetX;
        }
    }


    private void preCalculate() {
        if (scrollStep > 0) {
            if (wallpaperAspectRatio > (1 + 1 / (3 * scrollStep))
                    * screenAspectRatio) {
                scrollRange = 1 + 1 / (3 * scrollStep);
            } else if (wallpaperAspectRatio >= screenAspectRatio) {
                scrollRange = wallpaperAspectRatio / screenAspectRatio;
            } else {
                scrollRange = 1;
            }
        } else {
            scrollRange = 1;
        }
        // ------------------------------------------------------
        preA = screenAspectRatio * (scrollRange - 1);
        // preB = -1f;
        if (screenAspectRatio < 1)
            preB = -1.0f + (biasRange / screenAspectRatio);
        else
            preB = -1.0f + (biasRange * screenAspectRatio);
    }

    void setOffsetStep(float offsetStepX, float offsetStepY) {
        if (scrollStep != offsetStepX) {
            scrollStep = offsetStepX;
            preCalculate();
        }
    }

    public void setScrollMode(boolean scrollMode) {
        this.scrollMode = scrollMode;
        if (scrollMode)
            scrollOffsetXQueue.offer(scrollOffsetXBackup);
        else {
            scrollOffsetXQueue.clear();
            scrollOffsetXQueue.offer(0.5f);
        }
//        noScroll = false;
    }

    public void setOrientationAngle(float roll, float pitch) {
        orientationOffsetX = (float) (biasRange * Math.sin(roll));
        orientationOffsetY = (float) (biasRange * Math.sin(pitch));

//        Logger.e("setOrientationAngle","orientationOffsetX: "+orientationOffsetX);
    }



    private void transitionCal() {
        boolean needRefresh = false;
        if (Math.abs(currentOrientationOffsetX - orientationOffsetX) > .0001
                || Math.abs(currentOrientationOffsetY - orientationOffsetY) > .0001) {
            float tinyOffsetX = (orientationOffsetX - currentOrientationOffsetX)
                    / (transitionStep * delay);
            float tinyOffsetY = (orientationOffsetY - currentOrientationOffsetY)
                    / (transitionStep * delay);
            currentOrientationOffsetX += tinyOffsetX;
            currentOrientationOffsetY += tinyOffsetY;
//            EventBus.getDefault().post(new BiasChangeEvent(currentOrientationOffsetX / biasRange,
//                    currentOrientationOffsetY / biasRange));
            needRefresh = true;
        }
        if (!scrollOffsetXQueue.isEmpty()) {
            scrollOffsetX = scrollOffsetXQueue.poll();
            needRefresh = true;
        }
        if (needRefresh) mCallbacks.requestRender();
    }

    private void loadTexture() {

        Bitmap src = null;
        String tempPath_local = "";
        if (!isDefaultWallpaper) {

            SettingStore preferenceStore=SettingStore.getInstance(mContext);
            tempPath_local=TextUtils.isEmpty(tempPath)?preferenceStore.getExclusiveWallpaperPath():tempPath;
            src = getBitmapFromFile(tempPath_local);
            if (src == null) {
                isDefaultWallpaper = true;
                src = getBitmapFromDefaultImg();
            }
        }

        if (src == null) return;
        if (wallpaper != null)
            wallpaper.destroy();

        try {
            wallpaper = new ExclusiveKWallpaper(cropBitmap(src));
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
            try {
                String fileName = CommonFunctions.getFileNameFromURL(tempPath_local);
                if (!TextUtils.isEmpty(fileName)) {
                    EventManager.sendEvent(EventManager.LBL_EXCLUSIVE_WALLPAPER, EventManager.LBL_FOURK_ERROR, fileName);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        preCalculate();
        System.gc();

    }

    private Bitmap getBitmapFromFile(String filePath) {
        Bitmap src = null;

            try {
                if (!filePath.contains("file://")) {
                    filePath = "file://" + filePath;
                }
                src = Glide.with(mContext).asBitmap().apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)).load(filePath).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();

            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            }
            if (src == null) {
                InputStream is = null;
                try {
                    File file = new File(filePath.replace("file://", ""));
                    Log.i("LoadFile", "FourK File: " + file.getAbsolutePath());
                    FileInputStream fileInputStream = new FileInputStream(file);
                    is = fileInputStream;// mContext.openFileInput("/storage/emulated/0/AI Wallpapers/1363_uhd.jpg");
                    src = BitmapFactory.decodeStream(is);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("loadTexture()", "loadTexture:: Exceptions");
                    //isDefaultWallpaper = true;
                }catch (Error e) {
                    e.printStackTrace();
                    try {
                        String fileName = CommonFunctions.getFileNameFromURL(tempPath);
                        if (!TextUtils.isEmpty(fileName)) {
                            EventManager.sendEvent(EventManager.LBL_EXCLUSIVE_WALLPAPER, EventManager.LBL_FOURK_ERROR, fileName);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        return src;
    }

    private Bitmap getBitmapFromDefaultImg() {
        try {
            InputStream is = null;
            try {
                is = mContext.getAssets().open(AppConstant.DEFAULT);
            } catch (Exception e) {
            }
            Bitmap src = BitmapFactory.decodeStream(is);
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return src;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setIsDefaultWallpaper(boolean isDefault) {
        isDefaultWallpaper = isDefault;
        needsRefreshWallpaper = true;
//        loadTexture();
        mCallbacks.requestRender();
    }

    public void setBiasRange(int multiples) {

        // Log.d("tinyOffset", tinyOffsetX + ", " + tinyOffsetY);
        biasRange = multiples * MAX_BIAS_RANGE + 0.03f;
        preCalculate();
        mCallbacks.requestRender();
    }

    public void setDelay(int delay) {
        Log.i("onProgressChanged", "Renderrer delay: "+delay);
        this.delay = delay;
    }

    public interface Callbacks {
        void requestRender();
    }

    public static class BiasChangeEvent {
        float x, y;

        public BiasChangeEvent(float x, float y) {
            if (x > 1) this.x = 1;
            else if (x < -1) this.x = -1;
            else this.x = x;
            if (y > 1) this.y = 1;
            else if (y < -1) this.y = -1;
            else this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }


    private Bitmap cropBitmap(Bitmap src) {
        /*int reqWidth = Common.getScreenWidthFourK(mContext);
        reqWidth = (int) (reqWidth * 1.5);
        int reqHeight = Common.getScreenHeightFourK(mContext);
        reqHeight = (int) (reqHeight * 1.5);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Bitmap src = BitmapFactory.decodeStream(is, null, options);*/
        if (src == null)
            return null;
        boolean needToResize = WallpapersApplication.needToResizeFourkWallpaper();
        Log.i("needToResize", ""+ needToResize);
        if (needToResize) {
            final float width = src.getWidth();
            final float height = src.getHeight();
            Log.i("CropBitmap", "CropBitmap:width : " + width + " :: Height:" + height);
            wallpaperAspectRatio = width / height;
            if (wallpaperAspectRatio < screenAspectRatio) {
                scrollRange = 1;
                Bitmap tmp = Bitmap.createBitmap(src, 0, (int) (height - width
                                / screenAspectRatio) / 2, (int) width,
                        (int) (width / screenAspectRatio));
                src.recycle();
                if (tmp.getHeight() > 1.1 * screenH) {
                    Bitmap result = Bitmap.createScaledBitmap(tmp,
                            (int) (1.1 * screenH * screenAspectRatio),
                            (int) (1.1 * screenH), true);
                    tmp.recycle();
                    return result;
                } else
                    return tmp;
            } else {
                if (src.getHeight() > 1.1 * screenH) {
                    Bitmap result = Bitmap.createScaledBitmap(src,
                            (int) (1.1 * screenH * wallpaperAspectRatio),
                            (int) (1.1 * screenH), true);
                    src.recycle();
                    return result;
                } else
                    return src;
            }
        } else {
            return src;
        }
    }

}
