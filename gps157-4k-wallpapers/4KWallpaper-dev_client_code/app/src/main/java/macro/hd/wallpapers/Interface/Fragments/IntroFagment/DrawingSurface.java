package macro.hd.wallpapers.Interface.Fragments.IntroFagment;

import android.app.KeyguardManager;
import android.app.WallpaperColors;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.SweepGradient;
import android.os.Build;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.AppConstant;
import macro.hd.wallpapers.Utilily.Logger;

import java.io.InputStream;

class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {

    float[] intervals = new float[]{50.0f, 20.0f};
    float phase = 0;

    int color_a=Color.RED;
    int color_b=Color.YELLOW;
    int color_c=Color.BLUE;
    int color_d=Color.GREEN;

    private Bitmap bitmap;
    private Paint paint2;
    private Paint main_paint;
    private ColorMatrix colormatrix1;
    private ColorMatrixColorFilter colorMatrixColorFilter;
    private ColorMatrix colormatrix2;
    private Runnable runnable = new UpdateRunnable();
    private SweepGradient sweepGradient;
    private Handler handler = new Handler();
    private SurfaceHolder surfaceHolder;
    private Paint paint3;
    private Path path1;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;
    private boolean isTrue;
    private Path path;
    public SharedPreferences sharedPreferences;
    private boolean orientation;
    private long F = 0;
    private int width;
    private int height;
    private long I = 0;
    private boolean J;
    private boolean isVisible;
    Context context;

    public void setIntro(boolean intro) {
        isIntro = intro;
    }

    private boolean isIntro;

    public DrawingSurface(Context context) {
        super(context);
        this.context = context;
        init();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ) {
            this.height = height;
            this.width = width;
            orientation = true;
        } else {
            this.height = width;
            this.width = height;
            orientation = false;
        }
//        this.width=200;
//        this.height=350;
        Logger.e("surfaceChanged","width:"+width+" height:"+height);
        paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setStyle(Paint.Style.FILL);
        paint2.setColor(ViewCompat.MEASURED_STATE_MASK);
        main_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        main_paint.setStyle(Paint.Style.STROKE);
        main_paint.setColor(Color.WHITE);
        paint3 = new Paint();
        paint3.setColor(Color.WHITE);
        colormatrix1 = new ColorMatrix();
        colormatrix2 = new ColorMatrix();
        colormatrix1.postConcat(colormatrix2);
        colorMatrixColorFilter = new ColorMatrixColorFilter(colormatrix1);
        paint3.setColorFilter(colorMatrixColorFilter);
        sweepGradient = new SweepGradient((float) (this.height / 2), (float) (this.width / 2), new int[]{this.color_a, this.color_b, this.color_c, this.color_d}, null);
//            this.sweepGradient = new SweepGradient((float) (this.height / 2), (float) (this.width / 2), new int[]{-16776961, SupportMenu.CATEGORY_MASK, -16711936, -16776961}, null);
        main_paint.setShader(sweepGradient);
        sharedPreferences = context.getSharedPreferences("borderlightwall", 0);
        sharedPreferenceChangeListener = new PreferenceListener();
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        getBitmap();
        draw1();

        this.isVisible = true;
        if (isVisible) {
            F = System.nanoTime();
            isKeyboard();
            J = !isTrue;
            if (J) {
                I = 0;
            }
            handler.post(runnable);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        handler.removeCallbacks(runnable);
    }

    class UpdateRunnable implements Runnable {

        UpdateRunnable() {
        }

        public void run() {
            draw();
        }
    }

    class PreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        PreferenceListener() {

        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            Logger.e("onSharedPreferenceChanged","str:"+str);
            if (str.equals("enablenotch") || str.equals("radiustop") || str.equals("radiusbottom") || str.equals("notchwidth") || str.equals("notchheight") || str.equals("notchradiustop") || str.equals("notchradiusbottom") || str.equals("notchfullnessbottom")) {
                synchronized (this) {
                    draw1();
                }
            } else if ((str.equals("hasnewimage") && sharedPreferences.getBoolean("hasnewimage", false)) || (str.equals("image_path")) || (str.equals("enableimage") && sharedPreferences.getBoolean("enableimage", false))) {
                sharedPreferences.edit().putBoolean("hasnewimage", false).apply();
                getBitmap();
            }
        }
    }

    private float caclulation(float f, float f2, float f3) {
        return ((f2 - f) * f3) + f;
    }


    public void draw1() {
        float f;
        float f2;
        float radiustop = (float) sharedPreferences.getInt("radiustop", 0);
        float radiusbottom = (float) sharedPreferences.getInt("radiusbottom", 0);
        if(isIntro){
            radiustop=30;
            radiusbottom=30;
        }
        float notchheight = (float) sharedPreferences.getInt("notchheight", 0);
        float notchwidth = (float) (sharedPreferences.getInt("notchwidth", 0) * 2);
        float notchradiustop = (float) sharedPreferences.getInt("notchradiustop", 0);
        float notchradiusbottom = (float) sharedPreferences.getInt("notchradiusbottom", 0);
        float f9 = (float) (height + 2);
        float f10 = (float) (width + 2);
        float f11 = f9 - (radiustop + radiustop);
        float f12 = (f11 - notchwidth) / 2.0f;
        float f13 = (1.0f - (((float) sharedPreferences.getInt("notchfullnessbottom", 0)) / 100.0f)) * notchradiusbottom;
        this.path = new Path();
        this.path.moveTo(f9 - 1.0f, -1.0f + radiustop);
        float f14 = -radiustop;
        this.path.rQuadTo(0.0f, f14, f14, f14);
        if (orientation || !sharedPreferences.getBoolean("enablenotch", false)) {
            f = f9;
            f2 = 0.0f;
            path.rLineTo(-f11, 0.0f);
        } else {
            float f15 = (-f12) + notchradiustop;
            path.rLineTo(f15, 0.0f);
            float f16 = -notchradiustop;
            path.rQuadTo(f16, 0.0f, f16, notchradiustop);
            path.rLineTo(0.0f, (notchheight - notchradiustop) - notchradiusbottom);
            float f17 = -f13;
            float f18 = -notchradiusbottom;
            f = f9;
            path.rQuadTo(f17, notchradiusbottom - f13, f18, notchradiusbottom);
            f2 = 0.0f;
            path.rLineTo((-notchwidth) + notchradiusbottom + notchradiusbottom, 0.0f);
            path.rQuadTo(f13 + f18, f17, f18, f18);
            path.rLineTo(0.0f, (-notchheight) + notchradiustop + notchradiusbottom);
            path.rQuadTo(0.0f, f16, f16, f16);
            path.rLineTo(f15, 0.0f);
        }
        path.rQuadTo(f14, f2, f14, radiustop);
        float f19 = f10 - (radiustop + radiusbottom);
        path.rLineTo(f2, f19);
        path.rQuadTo(f2, radiusbottom, radiusbottom, radiusbottom);
        path.rLineTo(f - (radiusbottom + radiusbottom), f2);
        path.rQuadTo(radiusbottom, f2, radiusbottom, -radiusbottom);
        path.rLineTo(f2, -f19);
        path.close();
        path1 = new Path(path);
        path1.setFillType(Path.FillType.INVERSE_EVEN_ODD);
    }

    public void draw() {
        try {
            if (isVisible) {
                isKeyboard();
                Canvas lockHardwareCanvas = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    lockHardwareCanvas = surfaceHolder.getSurface().lockHardwareCanvas();
                }else
                    lockHardwareCanvas = surfaceHolder.lockCanvas();
                if (orientation) {
                    Matrix matrix = new Matrix();
                    matrix.preRotate(-90.0f);
                    matrix.postTranslate(0.0f, (float) height);
                    lockHardwareCanvas.setMatrix(matrix);
                }
                int nanoTime = (int) ((System.nanoTime() - (F + 300000000)) / 1000000);
                float interpolation = new OvershootInterpolator().getInterpolation(((float) Math.max(Math.min(nanoTime, 1000), 0)) / 1000.0f) * caclulation((float) sharedPreferences.getInt("bordersizelockscreen", 20), (float) sharedPreferences.getInt("bordersize", 20), timeCalc());
                lockHardwareCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                if (bitmap != null && true) {
                    float width = (float) ((this.height - bitmap.getWidth()) / 2);
                    float height = (float) ((this.width - bitmap.getHeight()) / 2);
                    float f = 1.0f - (((float) sharedPreferences.getInt("imagedesaturationlocked", 50)) / 100.0f);
                    float f2 = 1.0f - (((float) sharedPreferences.getInt("imagedesaturationunlocked", 50)) / 100.0f);
                    float m2876a = caclulation(((float) sharedPreferences.getInt("imagevisibilitylocked", 80)) / 100.0f, ((float) sharedPreferences.getInt("imagevisibilityunlocked", 80)) / 100.0f, timeCalc());
                    colormatrix1.setSaturation(caclulation(f, f2, timeCalc()));
                    colormatrix2.setScale(m2876a, m2876a, m2876a, 1.0f);
                    colormatrix1.postConcat(new ColorMatrix(colormatrix2));
                    paint3.setColorFilter(new ColorMatrixColorFilter(colormatrix1));
                    lockHardwareCanvas.drawBitmap(bitmap, width, height, paint3);
                }
                double d = (double) nanoTime;
                double pow = Math.pow((double) (21.0f - ((float) sharedPreferences.getInt("cyclespeed", 10))), 1.3d);
                Double.isNaN(d);
                Double.isNaN(d);
                float f3 = (float) (d / pow);
                Matrix matrix2 = new Matrix();
                matrix2.preRotate(f3, (float) (height / 2), (float) (width / 2));
                sweepGradient.setLocalMatrix(matrix2);

                main_paint.setStrokeWidth(interpolation);

                main_paint.setPathEffect(null);

                if (interpolation > 0.001f) {
                    lockHardwareCanvas.drawPath(path, main_paint);
                }
                lockHardwareCanvas.drawPath(path1, paint2);
                surfaceHolder.getSurface().unlockCanvasAndPost(lockHardwareCanvas);
                handler.post(runnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }catch (Error e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromDefaultImg() {
        try {
            InputStream is = null;
            try {

                is = context.getAssets().open(AppConstant.DEFAULT_EDGE);
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

    public void getBitmap() {
        bitmap = getBitmapFromDefaultImg();
    }

    private float timeCalc() {
        float f;
        float f2;
        if (isTrue) {
            return 0.0f;
        }
        if (J) {
            return 1.0f;
        }
        if (I - F < 200000000) {
            f2 = (float) ((System.nanoTime() - I) / 1000000);
            f = 120.0f;
        } else {
            f2 = (float) ((System.nanoTime() - I) / 1000000);
            f = 500.0f;
        }
        return Math.min(1.0f, f2 / f);
    }

    private void isKeyboard() {
        if (((KeyguardManager) context.getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode()) {
            isTrue = true;
        } else if (isTrue) {
            isTrue = false;
            I = System.nanoTime();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    public WallpaperColors onComputeColors() {
        return WallpaperColors.fromBitmap(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
    }


//    @Override
//    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
//        super.onVisibilityChanged(changedView, visibility);
//        this.isVisible = visibility;
//        if (visibility) {
//            F = System.nanoTime();
//            isKeyboard();
//            J = !isTrue;
//            if (J) {
//                I = 0;
//            }
//            handler.post(runnable);
//            return;
//        }
//        handler.removeCallbacks(runnable);
//    }

}