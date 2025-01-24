package macro.hd.wallpapers.Interface.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import macro.hd.wallpapers.R;
import macro.hd.wallpapers.Utilily.CommonFunctions;
import macro.hd.wallpapers.Utilily.EventManager;
import macro.hd.wallpapers.Utilily.Logger;

public class GradientActivity extends BaseActivity implements View.OnClickListener {

    private ImageView imageView, color1, color2, color3, color4;
    private ColorPicker colorPicker;
    private int colorString1 = 0, colorString2 = 0, colorString3 = 0, colorString4 = 0;
    private int colorClicked = 0;
    private ImageView right_left, top_left, bottom_top, top_right, left_right, top_b_right, toop_bottom, top_b_left;
    private RadioButton linear, radial, sweep;
    private int selectedRadio, selectedAngle;
    private SeekBar radiusSeekbar;
    private boolean isUp = true;
    private float radius;
    private boolean isDownloaded;
    private int[] colorArr = new int[2];
    private String destination;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gradient);

        radiusSeekbar = findViewById(R.id.seekbar);
        right_left = findViewById(R.id.rl);
        top_left = findViewById(R.id.top_left);
        bottom_top = findViewById(R.id.bt);
        top_right = findViewById(R.id.top_right);
        left_right = findViewById(R.id.lr);
        top_b_right = findViewById(R.id.top_b_right);
        toop_bottom = findViewById(R.id.tb);
        top_b_left = findViewById(R.id.top_b_left);

        findViewById(R.id.save).setOnClickListener(view -> downloadFile(false));
        findViewById(R.id.setImage).setOnClickListener(view -> downloadFile(true));

        findViewById(R.id.random).setOnClickListener(view -> {
            if (colorArr.length == 4) {
                colorString1 = getRandomColor();
                colorString2 = getRandomColor();
                colorString3 = getRandomColor();
                colorString4 = getRandomColor();

                colorArr[0] = colorString1;
                colorArr[1] = colorString2;
                colorArr[2] = colorString3;
                colorArr[3] = colorString4;

                color1.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString1))));
                color2.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString2))));
                color3.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString3))));
                color4.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString4))));
            } else if (colorArr.length == 3) {
                colorString1 = getRandomColor();
                colorString2 = getRandomColor();
                colorString3 = getRandomColor();

                colorArr[0] = colorString1;
                colorArr[1] = colorString2;
                colorArr[2] = colorString3;

                color1.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString1))));
                color2.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString2))));
                color3.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString3))));
            } else if (colorArr.length == 2) {
                colorString1 = getRandomColor();
                colorString2 = getRandomColor();

                colorArr[0] = colorString1;
                colorArr[1] = colorString2;

                color1.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString1))));
                color2.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString2))));

            }
            setGradientColor();
        });

        radiusSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radius = (float) i;
                setGradientColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        right_left.setOnClickListener(view -> {
            selectedAngle = 1;
            setGradientColor();
        });
        top_left.setOnClickListener(view -> {
            selectedAngle = 2;
            setGradientColor();
        });
        bottom_top.setOnClickListener(view -> {
            selectedAngle = 3;
            setGradientColor();
        });
        top_right.setOnClickListener(view -> {
            selectedAngle = 4;
            setGradientColor();
        });
        left_right.setOnClickListener(view -> {
            selectedAngle = 5;
            setGradientColor();
        });
        top_b_right.setOnClickListener(view -> {
            selectedAngle = 6;
            setGradientColor();
        });
        toop_bottom.setOnClickListener(view -> {
            selectedAngle = 7;
            setGradientColor();
        });
        top_b_left.setOnClickListener(view -> {
            selectedAngle = 8;
            setGradientColor();
        });

        findViewById(R.id.seekLL).setVisibility(View.GONE);
        findViewById(R.id.angleLL).setVisibility(View.GONE);

        linear = findViewById(R.id.linear);
        radial = findViewById(R.id.radial);
        sweep = findViewById(R.id.sweep);
        color1 = findViewById(R.id.color1);
        color2 = findViewById(R.id.color2);
        color3 = findViewById(R.id.color3);
        color4 = findViewById(R.id.color4);
        imageView = findViewById(R.id.image);


        if (colorString1 == 0 && colorString2 == 0) {
            colorString1 = getRandomColor();
            colorString2 = getRandomColor();
            color1.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString1))));
            color2.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & colorString2))));
        }
        colorArr[0] = colorString1;
        colorArr[1] = colorString2;

        imageView.setOnTouchListener((view, motionEvent) -> {

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Logger.e("touch", "ACTION_DOWN");
                if (isUp) {
                    isUp = false;
                    fadeOut(findViewById(R.id.mainBottomLL));
//                   findViewById(R.id.mainBottomLL).setVisibility(View.GONE);
                    fadeOut(findViewById(R.id.random));
//                    findViewById(R.id.random).setVisibility(View.GONE);
                    fadeOut(findViewById(R.id.setImage));
//                    findViewById(R.id.setImage).setVisibility(View.GONE);
                    fadeOut(findViewById(R.id.save));
//                    findViewById(R.id.save).setVisibility(View.GONE);
                } else {
                    isUp = true;

                    fadeIn(findViewById(R.id.mainBottomLL));
                    fadeIn(findViewById(R.id.random));
                    fadeIn(findViewById(R.id.setImage));
                    fadeIn(findViewById(R.id.save));

//                    findViewById(R.id.random).setVisibility(View.VISIBLE);
//                    findViewById(R.id.mainBottomLL).setVisibility(View.VISIBLE);
//                    findViewById(R.id.setImage).setVisibility(View.VISIBLE);
//                    findViewById(R.id.save).setVisibility(View.VISIBLE);
                }
                return true;
            }
            return false;
        });

        color1.setOnClickListener(this);
        color2.setOnClickListener(this);
        color3.setOnClickListener(this);
        color4.setOnClickListener(this);

        linear.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                findViewById(R.id.seekLL).setVisibility(View.GONE);
                findViewById(R.id.angleLL).setVisibility(View.VISIBLE);
                selectedRadio = 1;
                setGradientColor();
            }
        });

        radial.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                findViewById(R.id.seekLL).setVisibility(View.VISIBLE);
                findViewById(R.id.angleLL).setVisibility(View.GONE);
                selectedRadio = 2;
                radiusSeekbar.setProgress(500);
                setGradientColor();
            }
        });

        sweep.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                findViewById(R.id.seekLL).setVisibility(View.GONE);
                findViewById(R.id.angleLL).setVisibility(View.GONE);
                selectedRadio = 3;
                setGradientColor();
            }
        });

        colorPicker = new ColorPicker(GradientActivity.this, 200, 100, 60);
        colorPicker.setCallback(color -> {
            // Do whatever you want
            // Examples
//                Log.e("Alpha", Integer.toString(Color.alpha(color)));
//                Log.e("Red", Integer.toString(Color.red(color)));
//                Log.e("Green", Integer.toString(Color.green(color)));-1
//                Log.e("Blue", Integer.toString(Color.blue(color)));-16777216
//
            Logger.e("Pure color", color + " : color");
//                Log.e("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));
//                Log.e("#Hex with alpha", String.format("#%08X", (0xFFFFFFFF & color)));

            // If the auto-dismiss option is not enable (disabled as default) you have to manually dismiss the dialog
            setImageColor(colorClicked, color);
            colorPicker.dismiss();
        });

        setGradientColor();
    }

    public void fadeOut(View v) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha", 1f, 0f);
        fadeOut.setDuration(300);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        mAnimationSet.start();
    }

    public void fadeIn(View v) {

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f);
        fadeIn.setDuration(300);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        mAnimationSet.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (imageView != null) {
                imageView.requestFocus();
                imageView.requestLayout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(boolean isSetWallpaper) {
        Logger.e("download", "image");
        if(isSetWallpaper)
            EventManager.sendEvent(EventManager.LBL_GRADIENT_WALLPAPER,EventManager.ATR_GRADIENT_SET,"Set");
        else
            EventManager.sendEvent(EventManager.LBL_GRADIENT_WALLPAPER,EventManager.ATR_GRADIENT_SET,"Download");

        Bitmap bitmap = createBitmapFromLayout(imageView);
        if (isDownloaded && !isSetWallpaper) {
            imageView.requestFocus();
            imageView.requestLayout();

            Toast.makeText(GradientActivity.this, "Downloaded at-" + destination, Toast.LENGTH_SHORT).show();
            return;
        } else if (isDownloaded && isSetWallpaper) {
            setSystemWallpaper();
            return;
        }

        FileOutputStream outStream = null;
        destination = CommonFunctions.getSavedFilePath() + "/" + String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(destination);

        isDownloaded = true;
        showLoadingDialogMsg("Downloading...");

        try {
            outStream = new FileOutputStream(outFile);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("error", " error" + e.getMessage());
        }
        try {
            Logger.e("output streame", outStream.toString());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Logger.e("path", "output file" + outFile.getPath());
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(outFile));
        sendBroadcast(intent);

//        CommonFunctions.getSavedFilePath()+"/"+post.getPostId()+"_"+type+ CommonFunctions.getExtension(post.getImg(),false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissLoadingProgress();
                if (isSetWallpaper) {
                    setSystemWallpaper();
                } else {
                    Toast.makeText(GradientActivity.this, "Downloaded at-" + outFile.getPath(), Toast.LENGTH_SHORT).show();
                }
            }
        }, 2000);

        imageView.requestFocus();
        imageView.requestLayout();
//        imageView.notify();
    }

    private void setSystemWallpaper() {
        Logger.e(" : ", destination);
        try {
            Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(Uri.fromFile(new File(destination)), "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("mimeType", "image/*");
            startActivity(Intent.createChooser(intent, getString(R.string.label_set_dialog)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int[] addElement(int[] a, int e) {
        a = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;

        Logger.e("length", a.length + " : size");
        return a;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.color1:
                colorClicked = 1;
                colorPicker.show();
                break;
            case R.id.color2:
                colorClicked = 2;
                colorPicker.show();
                break;
            case R.id.color3:
                colorClicked = 3;
                colorPicker.show();
                break;
            case R.id.color4:
                if (colorString3 == 0) {
                    Toast.makeText(GradientActivity.this, "Please select previous color.", Toast.LENGTH_SHORT).show();
                    return;
                }
                colorClicked = 4;
                colorPicker.show();
                break;
        }
    }

    private void setImageColor(int colorClicked, int color) {

        switch (colorClicked) {
            case 1:
                colorString1 = color;
                colorArr[0] = color;
                color1.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & color))));
                break;
            case 2:
                colorArr[1] = color;
                colorString2 = color;
                color2.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & color))));
                break;
            case 3:
                if (colorString3 == 0) {
                    colorArr = addElement(colorArr, color);
                }
                colorArr[2] = color;
                colorString3 = color;
                color3.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & color))));
                break;
            case 4:

                if (colorString4 == 0) {
                    colorArr = addElement(colorArr, color);
                }

                colorArr[3] = color;
                colorString4 = color;
                color4.setBackgroundColor(Color.parseColor(String.format("#%06X", (0xFFFFFF & color))));
                break;
        }
        setGradientColor();
    }

    public void setGradientColor() {

        if (!linear.isChecked() && !radial.isChecked() && !sweep.isChecked()) {
            linear.setChecked(true);
        }

        isDownloaded = false;
        setDrawableToImage(colorArr);
    }

    private int getRandomColor() {
        int random = (new Random()).nextInt((16777216 - 1) + 1) + 1;
        return (~(random - 1));
    }

    public void setDrawableToImage(int colorArr[]) {

        // Initialize a new GradientDrawable
        GradientDrawable gd = new GradientDrawable();

        Logger.e("length", colorArr.length + " : l ");
        // Set the color array to draw gradient
        gd.setColors(colorArr);

        if (selectedRadio == 1) {

            // Set the GradientDrawable gradient type linear gradient
            gd.setGradientType(GradientDrawable.LINEAR_GRADIENT);

            switch (selectedAngle) {
                case 1:
                    gd.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
                    break;
                case 2:
                    gd.setOrientation(GradientDrawable.Orientation.BR_TL);
                    break;
                case 3:
                    gd.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
                    break;
                case 4:
                    gd.setOrientation(GradientDrawable.Orientation.BL_TR);
                    break;
                case 5:
                    gd.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                    break;
                case 6:
                    gd.setOrientation(GradientDrawable.Orientation.TL_BR);
                    break;
                case 7:
                    gd.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                    break;
                case 8:
                    gd.setOrientation(GradientDrawable.Orientation.TR_BL);
                    break;
            }
        } else if (selectedRadio == 2) {

            // Set the GradientDrawable gradient type linear gradient
            gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);

            gd.setGradientRadius(radius);

        } else if (selectedRadio == 3) {

            // Set the GradientDrawable gradient type linear gradient
            gd.setGradientType(GradientDrawable.SWEEP_GRADIENT);
        }

        // Set GradientDrawable shape is a rectangle
        gd.setShape(GradientDrawable.RECTANGLE);

        // Set 3 pixels width solid blue color border
//        gd.setStroke(3, Color.BLUE);

        // Set GradientDrawable width and in pixels
        gd.setSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); // Width 450 pixels and height 150 pixels

        imageView.setImageDrawable(gd);
    }

    private Bitmap createBitmapFromLayout(View tv) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        tv.measure(spec, spec);
        tv.layout(0, 0, width, height);
        Bitmap b = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate((-tv.getScrollX()), (-tv.getScrollY()));
        tv.draw(c);
        return b;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       imageView=null;
       color1=null;
        color2=null;
        color3=null;
        color4=null;
        colorPicker=null;
        colorString1 = 0;
        colorString2 = 0;
        colorString3 = 0;
        colorString4 = 0;
        colorClicked = 0;
        right_left=null; top_left=null; bottom_top=null; top_right=null;
        left_right=null;
        top_b_right=null;
        toop_bottom=null;
        top_b_left=null;
        linear=null; radial=null; sweep=null;
        selectedRadio=0;
        selectedAngle=0;
        radiusSeekbar=null;;
        isUp = true;
        radius=0f;
        isDownloaded=false;
        colorArr =null;
        destination=null;
    }
}