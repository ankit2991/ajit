package macro.hd.wallpapers.Utilily;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import macro.hd.wallpapers.BuildConfig;
import macro.hd.wallpapers.AppController.UserInfoManager;
import macro.hd.wallpapers.DB.DataBase;
import macro.hd.wallpapers.DB.SettingStore;
import macro.hd.wallpapers.WallpapersApplication;
import macro.hd.wallpapers.R;
import macro.hd.wallpapers.NetworkManager.WebServiceConstants;
import macro.hd.wallpapers.Model.Wallpapers;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CommonFunctions {

    private static String mHardwareId;

    public static long getTime() {
        return System.currentTimeMillis();
    }

    public static String getTimeStamp() {
        return "" + System.currentTimeMillis();
    }

    /**
     * Encodes a string to be send as part of the URL. Uses {@link URLEncoder}.
     */
    public static String encode(String str) {
        if (TextUtils.isEmpty(str)) return str;
        try {
            return URLEncoder.encode(str, WebServiceConstants.CHARSET_DEFAULT);
        } catch (UnsupportedEncodingException e) {
            // ignore; this exception is never thrown since we use default charset
            return str.replace(" ", "%20");
        }
    }

    public static String encodeSpaces(String str) {
        if (TextUtils.isEmpty(str)) return "";
        return str.replace(" ", "%20");
    }

    /**
     * Provides the width of screen display
     *
     * @param context
     * @return Screen display width
     */
    public static int getDisplayWidth(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * Provides the height of screen display
     *
     * @param context
     * @return Screen display height
     */
    public static int getDisplayHeight(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }


    public static boolean isNetworkAvailable(Context context) {
//        return true;
        try {

            try {
                if (WallpapersApplication.getApplication().getNetworkChecker().isNetworkAvailable()) {
                    return true;
                }
            } catch (Exception e) {
            }

            final ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()
                    && networkInfo.isAvailable()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * check network is available or not.
     *
     * @return Return true or false as network avaibility
     */

    @SuppressWarnings("deprecation")
//	public static boolean isNetworkAvailable(Context mContext) {
//
//		try {
//			ConnectivityManager connec = (ConnectivityManager) mContext
//					.getSystemService(Context.CONNECTIVITY_SERVICE);
//			if (connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null
//					&& connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
//					.getState() == NetworkInfo.State.CONNECTED)
//				return true;
//
//			TelephonyManager tm = (TelephonyManager) mContext
//					.getSystemService(Context.TELEPHONY_SERVICE);
//			if (tm.getDataState() == TelephonyManager.DATA_CONNECTED
//					|| tm.getDataState() == TelephonyManager.DATA_CONNECTING)
//				return true;
//
//			if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
//				if (connec.getNetworkInfo(ConnectivityManager.TYPE_WIMAX) != null
//						&& connec
//						.getNetworkInfo(ConnectivityManager.TYPE_WIMAX)
//						.getState() == NetworkInfo.State.CONNECTED)
//					return true;
//			}
//		} catch (Exception e) {
//			return true;
//		}
//		return false;
//	}

    public static int convertDpToPixel(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }


    @SuppressLint("HardwareIds")
    public static String getHardwareId(Context mContext) {

        if (mHardwareId == null) {

            mHardwareId = Settings.Secure.getString(WallpapersApplication.sContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            if (TextUtils.isEmpty(mHardwareId)) {
                mHardwareId = Build.SERIAL;
//                mDeviceHardwareIdType = DeviceHardwareIdType.SERIAL_ID;
            }

            if (TextUtils.isEmpty(mHardwareId)) {
                mHardwareId = Settings.Secure.getString(WallpapersApplication.sContext.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
//                mDeviceHardwareIdType = DeviceHardwareIdType.ANDROID_ID;
            }
        }

        return mHardwareId;// "64551335898452230";//"645513358984522310";//"6455133589845";//
    }


    public static String capitalize(String s) {
        if (s == null) return null;
        if (s.length() == 1) {
            return s.toUpperCase();
        }
        if (s.length() > 1) {
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }
        return s;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getDeviceResolution(Context context) {
        String value = "xxxhdpi";
        int density = context.getResources().getDisplayMetrics().densityDpi;

        switch (density) {
            case DisplayMetrics.DENSITY_MEDIUM:
                value = "mdpi";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                value = "hdpi";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                value = "xhdpi";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                value = "xxhdpi";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                value = "xxxhdpi";
                break;
        }

        return value;
    }

    /**
     * Encrypts the given string with MD5 algorithm.</br> If it doesn't success,
     * an empty String will be returned.
     */
    public static final String toMD5(String stringToConvert) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte result[] = md5.digest(stringToConvert.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < result.length; i++) {
                String s = Integer.toHexString(result[i]);
                int length = s.length();
                if (length >= 2) {
                    sb.append(s.substring(length - 2, length));
                } else {
                    sb.append("0");
                    sb.append(s);
                }
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static float getFloatValue(Context context, int value) {
        float size = 0.0f;
        try {
            TypedValue outValue = new TypedValue();
            context.getResources().getValue(value, outValue, true);
            size = outValue.getFloat();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static String removeSpecialChar(String mString) {
        return Normalizer.normalize(mString, Normalizer.Form.NFD).replaceAll("[^a-zA-Z0-9^-]", "").toLowerCase().trim().replace(" ", "-");
    }

    public static void displayShortToastMessage(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    public static View getParentViewCustom(View rootView) {
        return (View) rootView.getRootView();
    }


    public static boolean isAndroidM() {
        return  /*Logger.enableAndroidM_Permission && */Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isAndroidJellyBean() {
        return  /*Logger.enableAndroidM_Permission && */Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }


    /**
     * Will read the content from a given {@link InputStream} and return it as a {@link String}.
     *
     * @param inputStream The {@link InputStream} which should be read.
     * @return Returns <code>null</code> if the the {@link InputStream} could not be read. Else
     * returns the content of the {@link InputStream} as {@link String}.
     */
    public static String inputStreamToString(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, bytes.length);
            String json = new String(bytes);
            return json;
        } catch (IOException e) {
            return null;
        }
    }


    public static boolean isEmailValid(String emailInput) {
        return Patterns.EMAIL_ADDRESS.matcher(emailInput)
                .matches();
    }

    public static boolean isMobileNumberValid(String numberInput) {

        return Patterns.PHONE.matcher(numberInput)
                .matches();

		/*if (numberInput.matches("\\d{10}") && numberInput.length() == 10) {
			return true;
		}
		return false;*/
    }

    public static boolean validateMobileNum(String numberInput) {
        return Patterns.PHONE.matcher(numberInput).matches();
    }

    /**
     * Converts each word seperated by space in to Camel case
     *
     * @param text
     * @return
     */
    public static String toCamelCase(final String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        final StringBuilder ret = new StringBuilder(text.length());
        for (final String word : text.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase());
                ret.append(word.substring(1).toLowerCase());
            }
            if (!(ret.length() == text.length()))
                ret.append(" ");
        }

        return ret.toString();
    }


    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    public static void showDialog(Context mContext, String title, String message, String stringOfOkButton) {

        try {

            try {
                if (((Activity) mContext).isFinishing()) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final Dialog dialog = new Dialog(mContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog);

            TextView txt_dialog_title = (TextView) dialog.findViewById(R.id.txt_dialog_title);
            TextView txt_dialog_description = (TextView) dialog.findViewById(R.id.txt_dialog_description);
            TextView btn_dialog_positive = (TextView) dialog.findViewById(R.id.btn_dialog_positive);
            TextView btn_dialog_negative = (TextView) dialog.findViewById(R.id.btn_dialog_negative);
            btn_dialog_negative.setVisibility(View.GONE);

            txt_dialog_title.setText(title);
            txt_dialog_description.setText(message);
            btn_dialog_positive.setText(stringOfOkButton);
            btn_dialog_positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public interface DialogOnButtonClickListener {
        public void onOKButtonCLick();

        public void onCancelButtonCLick();
    }

    public static void showDialogOk(Context mContext, String title, String message, final DialogOnButtonClickListener dialogOnButtonClickListener) {

        try {
            if (((Activity) mContext).isFinishing()) {
                return;
            }
            android.app.AlertDialog.Builder builder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SettingStore settingStore = SettingStore.getInstance(mContext);
                boolean isDarkTheme = false;
                if (settingStore.getTheme() == 0) {
                    isDarkTheme = false;
                } else if (settingStore.getTheme() == 1) {
                    isDarkTheme = true;
                }
                if (isDarkTheme)
                    builder = new android.app.AlertDialog.Builder(mContext, R.style.CustomAlertDialog);
                else
                    builder = new android.app.AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert);

            } else {
                builder = new android.app.AlertDialog.Builder(mContext);
            }

            builder.setTitle(title)
                    .setMessage(message).setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            if (dialogOnButtonClickListener != null) {
                                dialog.dismiss();
                                dialogOnButtonClickListener.onOKButtonCLick();
                            }

                        }
                    })
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDialogConfirmation(Context mContext, String title, String message, final DialogOnButtonClickListener dialogOnButtonClickListener) {
        try {
            if (((Activity) mContext).isFinishing()) {
                return;
            }


            android.app.AlertDialog.Builder builder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SettingStore settingStore = SettingStore.getInstance(mContext);
                boolean isDarkTheme = false;
                if (settingStore.getTheme() == 0) {
                    isDarkTheme = false;
                } else if (settingStore.getTheme() == 1) {
                    isDarkTheme = true;
                }
                if (isDarkTheme)
                    builder = new android.app.AlertDialog.Builder(mContext, R.style.CustomAlertDialog);
                else
                    builder = new android.app.AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert);

            } else {
                builder = new android.app.AlertDialog.Builder(mContext);
            }
            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            if (dialogOnButtonClickListener != null) {
                                dialog.dismiss();
                                dialogOnButtonClickListener.onOKButtonCLick();
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static float pixelsToSp(float px, Context context) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    public static float convertFahrenheitToCelcius(float fahrenheit) {
        return ((fahrenheit - 32) * 5 / 9);
    }

    public static int convertSpToPixels(float sp, Context context) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, sp, context.getResources().getDisplayMetrics());
        return px;
    }

//    public static float convertSpToPixels(float sp, Context context) {
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
//    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String formatHoursAndMinutes(int totalMinutes) {
        try {
            String minutes = Integer.toString(totalMinutes % 60);
            minutes = minutes.length() == 1 ? "0" + minutes : minutes;
            String finalString = "";
            if ((totalMinutes / 60) > 0)
                finalString += (totalMinutes / 60) + " hr ";

            return finalString + minutes + " min" + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static boolean isStoreVersion(Context context) {
        boolean result = false;
        try {
            String installer = context.getPackageManager()
                    .getInstallerPackageName(context.getPackageName());
            Log.e("installer: ", "" + installer);
            if (TextUtils.isEmpty(installer)) {
                result = false;
            } else if (installer.contains("com.android.vending")) {
                result = true;
            }
        } catch (Throwable e) {
        }
        //  return true;
        return result;
    }

    public static void garbageCollector(boolean isCalled) {
        if (!isCalled)
            return;

        try {
            System.runFinalization();
            Runtime.getRuntime().gc();
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ProgressDialog createProgressDialog(Context mContext) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        try {
            dialog.show();
        } catch (Exception e) {

        }
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressdialog);
        TextView textView = dialog.findViewById(R.id.txt_msg);
        textView.setVisibility(View.GONE);

        TextView tv = dialog.findViewById(R.id.tv);
        RelativeLayout rl_main = dialog.findViewById(R.id.rl_main);
        SettingStore settingStore = SettingStore.getInstance(mContext);
        if (settingStore.getTheme() == 0) {
            tv.setTextColor(mContext.getResources().getColor(R.color.textColorPrimary));
            textView.setTextColor(mContext.getResources().getColor(R.color.textColorPrimary));
            rl_main.setBackgroundResource(R.drawable.progressbar_bg);
        } else if (settingStore.getTheme() == 1) {
            tv.setTextColor(mContext.getResources().getColor(R.color.textColorPrimary1));
            textView.setTextColor(mContext.getResources().getColor(R.color.textColorPrimary1));
            rl_main.setBackgroundResource(R.drawable.progressbar_bg_dark);
        }

        // dialog.setMessage(Message);
        return dialog;
    }

    public static ProgressDialog createProgressDialogMsg(Context mContext, String msg) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {

        }
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressdialog);
        dialog.findViewById(R.id.circularProgressbar).setVisibility(View.GONE);
        dialog.findViewById(R.id.progress).setVisibility(View.VISIBLE);
        TextView textView = dialog.findViewById(R.id.txt_msg);
        textView.setVisibility(View.VISIBLE);
        textView.setText(msg);

        TextView tv = dialog.findViewById(R.id.tv);
        RelativeLayout rl_main = dialog.findViewById(R.id.rl_main);
        SettingStore settingStore = SettingStore.getInstance(mContext);
        if (settingStore.getTheme() == 0) {
            tv.setTextColor(mContext.getResources().getColor(R.color.textColorPrimary));
            textView.setTextColor(mContext.getResources().getColor(R.color.textColorPrimary));
            rl_main.setBackgroundResource(R.drawable.progressbar_bg);
        } else if (settingStore.getTheme() == 1) {
            tv.setTextColor(mContext.getResources().getColor(R.color.textColorPrimary1));
            textView.setTextColor(mContext.getResources().getColor(R.color.textColorPrimary1));
            rl_main.setBackgroundResource(R.drawable.progressbar_bg_dark);
        }

        // dialog.setMessage(Message);
        return dialog;
    }

    private static final NavigableMap< Long, String > suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(String count) {

        try {
            long value = Long.parseLong(count);
            //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
            if (value == Long.MIN_VALUE) return format("" + Long.MIN_VALUE + 1);

            if (value < 0) return "-" + format("" + -value);
            if (value < 1000) return Long.toString(value); //deal with easy case

            Map.Entry< Long, String > e = suffixes.floorEntry(value);
            Long divideBy = e.getKey();
            String suffix = e.getValue();

            long truncated = value / (divideBy / 10); //the number part of the output times 10
            boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
            return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
        } catch (NumberFormatException e1) {
            e1.printStackTrace();
        }
        return count;
    }

    public static String createBaseDirectory() {
        //renameFolderIfAvailable();
        File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!pictureFolder.exists()) {
            pictureFolder.mkdirs();
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + NEW_FOLDER_NAME);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                EventManager.sendEvent(EventManager.LBL_TRANSFER_FILE, "Picture", "Problem creating Image folder");
                Log.e("getReceiveFilePath :: ", "Problem creating Image folder");
            }
        }
        Logger.e("file.getAbsolutePath()", file.getAbsolutePath());
        return file.getAbsolutePath();
    }


    public static String getSavedFilePath() {

        File file = new File(createBaseDirectory());
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("getReceiveFilePath :: ", "Problem creating Image folder");

            }
        }
        Logger.e("file.getSavedFilePath()", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static String getStatusSaverFilePath() {
        File file = new File(createBaseDirectory(), "4K Status Saver");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("getReceiveFilePath :: ", "Problem creating Image folder");

            }
        }
        Logger.e("file.getSavedFilePath()", file.getAbsolutePath());
        return file.getAbsolutePath();
    }


    public static String saveImageToSD(Context context, Bitmap finalBitmap, String fileName) {

        String root = createBaseDirectory();
        File myDir = new File(root);
        myDir.mkdirs();

        String fname = fileName + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

//			context.sendBroadcast(new Intent(
//					Intent.ACTION_MEDIA_MOUNTED,
//					Uri.parse("file://" + root)));

        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.e("saveImageToSD", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static void showInternetDialog(Context context) {
        try {
            if (((Activity) context).isFinishing()) {
                return;
            }

            android.app.AlertDialog.Builder builder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SettingStore settingStore = SettingStore.getInstance(context);
                boolean isDarkTheme = false;
                if (settingStore.getTheme() == 0) {
                    isDarkTheme = false;
                } else if (settingStore.getTheme() == 1) {
                    isDarkTheme = true;
                }
                if (isDarkTheme)
                    builder = new android.app.AlertDialog.Builder(context, R.style.CustomAlertDialog);
                else
                    builder = new android.app.AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert);

            } else {
                builder = new android.app.AlertDialog.Builder(context);
            }
            builder.setMessage(context.getResources().getString(R.string.label_utility_net))
                    .setCancelable(false)
                    .setPositiveButton(context.getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isPackageInstalled(Context context, String packagename) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context != null && context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        return statusBarHeight;
    }

    public static boolean isFavoriteAdded(UserInfoManager userInfoManager, String id) {
        boolean isFavAdded = userInfoManager.isFavorite(id);
        return isFavAdded;
    }

    public static void setFavorite(SettingStore settingStore, final Wallpapers post) {
        if (settingStore.getLikeCount().equalsIgnoreCase(""))
            settingStore.setLikeCount(post.getPostId());
        else {
            if (!settingStore.getLikeCount().contains(post.getPostId())) {
                settingStore.setLikeCount(settingStore.getLikeCount() + "_" + post.getPostId());
            }
        }
    }

    public static void setUnFavorite(SettingStore settingStore, final Wallpapers post) {
        if (settingStore.getLikeCount().contains("_" + post.getPostId() + "_")) {
            settingStore.setLikeCount(settingStore.getLikeCount().replace("_" + post.getPostId(), ""));
        } else if (settingStore.getLikeCount().contains(post.getPostId() + "_")) {
            settingStore.setLikeCount(settingStore.getLikeCount().replace(post.getPostId() + "_", ""));
        } else if (settingStore.getLikeCount().contains("_" + post.getPostId())) {
            settingStore.setLikeCount(settingStore.getLikeCount().replace("_" + post.getPostId(), ""));
        }

        if (settingStore.getUnLikeCount().equalsIgnoreCase(""))
            settingStore.setUnLikeCount(post.getPostId());
        else {
            if (!settingStore.getUnLikeCount().contains(post.getPostId())) {
                settingStore.setUnLikeCount(settingStore.getUnLikeCount() + "_" + post.getPostId());
            }
        }
    }

    public static boolean isFavoriteAddedExclusive(UserInfoManager userInfoManager, String id) {
        boolean isFavAdded = userInfoManager.isFavoriteExclusive(id);
        return isFavAdded;
    }

    public static void setFavoritExclusive(SettingStore settingStore, final Wallpapers post) {
        if (settingStore.getLikeExclusiveCount().equalsIgnoreCase(""))
            settingStore.setLikeExclusiveCount(post.getPostId());
        else {
            if (!settingStore.getLikeExclusiveCount().contains(post.getPostId())) {
                settingStore.setLikeExclusiveCount(settingStore.getLikeExclusiveCount() + "_" + post.getPostId());
            }
        }
    }

    public static void setUnFavoriteExclusive(SettingStore settingStore, final Wallpapers post) {
        if (settingStore.getLikeExclusiveCount().contains("_" + post.getPostId() + "_")) {
            settingStore.setLikeExclusiveCount(settingStore.getLikeExclusiveCount().replace("_" + post.getPostId(), ""));
        } else if (settingStore.getLikeExclusiveCount().contains(post.getPostId() + "_")) {
            settingStore.setLikeExclusiveCount(settingStore.getLikeExclusiveCount().replace(post.getPostId() + "_", ""));
        } else if (settingStore.getLikeExclusiveCount().contains("_" + post.getPostId())) {
            settingStore.setLikeExclusiveCount(settingStore.getLikeExclusiveCount().replace("_" + post.getPostId(), ""));
        }

        if (settingStore.getUnLikeCountExclusive().equalsIgnoreCase(""))
            settingStore.setUnLikeCountExclusive(post.getPostId());
        else {
            if (!settingStore.getUnLikeCountExclusive().contains(post.getPostId())) {
                settingStore.setUnLikeCountExclusive(settingStore.getUnLikeCountExclusive() + "_" + post.getPostId());
            }
        }
    }

    public static boolean isFavoriteAddedLive(UserInfoManager userInfoManager, String id) {
        boolean isFavAdded = userInfoManager.isFavoriteLive(id);
        return isFavAdded;
    }

    public static void setFavoriteLive(SettingStore settingStore, final Wallpapers post) {
        if (settingStore.getLikeLiveCount().equalsIgnoreCase(""))
            settingStore.setLikeLiveCount(post.getPostId());
        else {
            if (!settingStore.getLikeLiveCount().contains(post.getPostId())) {
                settingStore.setLikeLiveCount(settingStore.getLikeLiveCount() + "_" + post.getPostId());
            }
        }
    }

    public static void setUnFavoriteLive(SettingStore settingStore, final Wallpapers post) {
        if (settingStore.getLikeLiveCount().contains("_" + post.getPostId() + "_")) {
            settingStore.setLikeLiveCount(settingStore.getLikeLiveCount().replace("_" + post.getPostId(), ""));
        } else if (settingStore.getLikeLiveCount().contains(post.getPostId() + "_")) {
            settingStore.setLikeLiveCount(settingStore.getLikeLiveCount().replace(post.getPostId() + "_", ""));
        } else if (settingStore.getLikeLiveCount().contains("_" + post.getPostId())) {
            settingStore.setLikeLiveCount(settingStore.getLikeLiveCount().replace("_" + post.getPostId(), ""));
        }

        if (settingStore.getUnLikeCountLive().equalsIgnoreCase(""))
            settingStore.setUnLikeCountLive(post.getPostId());
        else {
            if (!settingStore.getUnLikeCountLive().contains(post.getPostId())) {
                settingStore.setUnLikeCountLive(settingStore.getUnLikeCountLive() + "_" + post.getPostId());
            }
        }
    }

    public static void setSearchKeyword(SettingStore settingStore, final String keyword) {
        if (settingStore.getSearchKeyword().equalsIgnoreCase(""))
            settingStore.setSearchKeyword(keyword);
        else {
            if (!settingStore.getSearchKeyword().contains(keyword)) {
                settingStore.setSearchKeyword(settingStore.getSearchKeyword() + "_" + keyword);
            }
        }
    }

    public static ArrayList< Wallpapers > getList(Context context) {
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.fetchAllAlerts(DataBase.table_download,
                DataBase.Int_download, "sr_no DESC");

        ArrayList< Wallpapers > data_tracker_route = new ArrayList< Wallpapers >();

        try {
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    Wallpapers obj = fillUpDataTracker(c);
                    data_tracker_route.add(obj);
                    c.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (c != null)
            c.close();
        if (db != null)
            db.close();
        return data_tracker_route;
    }

    private static Wallpapers fillUpDataTracker(Cursor c) {
        Wallpapers pack = new Wallpapers();

        int sr_no = c.getInt(0);
        String id = c.getString(1);
        String type = c.getString(2);
        String category = c.getString(3);
        String img = c.getString(6);
        String progress = c.getString(13);
        String download_id = c.getString(14);


        pack.setSr_no(sr_no);
        pack.setPostId(id);
        pack.setType(type);
        pack.setCategory(category);
        pack.setImg(img);
        pack.setProgress(progress);
        pack.setDownload_id(download_id);

        return pack;

    }

    public static void addMessageToDatabase(Context context, Wallpapers message) {
        DataBase d1 = null;
        try {
            d1 = new DataBase(context);
            d1.open();

            boolean flag = false;
            Cursor cursor = null;
            try {
                cursor = d1.fetchAlert(DataBase.table_download, DataBase.Int_download, "id='" + message.getPostId() + "'");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cursor != null && cursor.getCount() > 0)
                flag = true;

            if (cursor != null)
                cursor.close();

            if (flag) {
                boolean istru = d1.updateAlert_one_colunm(DataBase.table_download, DataBase.Int_download, "id='" + message.getPostId() + "'", message.getDownload_id(), 6);
                Logger.e("addMessageToDatabase", "update " + istru);
            } else {
                String values2[] = new String[]{message.getPostId(), message.getType(), message.getCategory(), message.getImg(), message.getProgress(), message.getDownload_id()};

                d1.createAlert(DataBase.table_download,
                        DataBase.Int_download, values2);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (d1 != null)
            d1.close();
    }

    public static void updateMessageStatus(Context context, String m_id, String status) {

        DataBase d1 = null;
        try {
            d1 = new DataBase(context);
            d1.open();
            boolean flag = false;
            try {
                flag = d1.updateAlert_one_colunm(DataBase.table_download, DataBase.Int_download, "id='" + m_id + "'", status, 5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (d1 != null)
            d1.close();
    }

    public static String isDownloadAdded(Context context, String refid) {

        Cursor cursor = null;
        DataBase d1 = null;
        try {
            d1 = new DataBase(context);
            d1.open();
            String flag = "";

            try {
                cursor = d1.fetchAlert(DataBase.table_download, DataBase.Int_download, "download_id='" + refid + "'");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                flag = cursor.getString(1);
            }

            return flag;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (cursor != null)
            cursor.close();

        if (d1 != null)
            d1.close();

        return "";
    }

    public static String capitalizeSentence(String capString) {

        try {
            if (TextUtils.isEmpty(capString))
                return "";
            StringBuffer capBuffer = new StringBuffer();
            Matcher capMatcher = Pattern.compile("([a-z])([a-z]*)", Pattern.CASE_INSENSITIVE).matcher(capString);
            while (capMatcher.find()) {
                capMatcher.appendReplacement(capBuffer, capMatcher.group(1).toUpperCase() + capMatcher.group(2).toLowerCase());
            }

            return capMatcher.appendTail(capBuffer).toString();
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getRandomAd(Context context, boolean needToSet, boolean fromSplash) {
        SettingStore settings = SettingStore.getInstance(context);

        String lastValue = settings.getRandomNumber();
        try {
            if (WallpapersApplication.getApplication().getSettings().getIsRandom().equalsIgnoreCase("1")) {
                String[] values;
                if (fromSplash)
                    values = WallpapersApplication.getApplication().getSettings().getAdFlag().split("_");
                else
                    values = WallpapersApplication.getApplication().getSettings().getSecond_ad().split("_");

                int length = values.length;
                Logger.e("length", "" + length);
                if (length == 1) {
                    lastValue = values[0];
//					if(needToSet)
//						settings.setIS_ONE_AD(true);
                } else {
//					if(needToSet)
//						settings.setIS_ONE_AD(false);
                    while (true) {
                        Random r = new Random();
                        int randomNumber = r.nextInt(length);
                        Logger.e("randomNumber", "" + randomNumber);
                        Logger.e("lastValue", "" + lastValue);
                        if (TextUtils.isEmpty(lastValue)) {
                            lastValue = values[randomNumber];
                            break;
                        }
                        if (!lastValue.equalsIgnoreCase(values[randomNumber])) {
                            lastValue = values[randomNumber];
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            lastValue = WallpapersApplication.AD_AM;
        }
        Logger.e("lastValue final", "" + lastValue);
        if (needToSet)
            settings.setRandomNumber(lastValue);
        return lastValue;
    }


    public static String appVerstion(Context c) {
        PackageInfo pInfo = null;
        String version = "";
        try {
            pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = "" + pInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String getPackgename(Context c) {
        String version = "";
        try {
            version = c.getPackageName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * Draw a text bubble
     */
    private static void drawBubble(
            final Canvas c,
            final int x,
            final int y,
            final int width,
            final int height,
            final String text, int textSize) {
        final TextRect textRect;

        // set up font
        {
            final Paint fontPaint = new Paint();
            fontPaint.setColor(Color.WHITE);
            fontPaint.setAntiAlias(true);
            fontPaint.setTextSize(textSize);

            textRect = new TextRect(fontPaint);
        }

        final int h = textRect.prepare(
                text,
                width - 8,
                height - 8);

        // draw bubble
        {
            final Paint p = new Paint();
            p.setColor(Color.TRANSPARENT);
            p.setStyle(Paint.Style.FILL);
            p.setAntiAlias(true);

            c.drawRoundRect(
                    new RectF(x, y, x + width, y + h + 8),
                    4,
                    4,
                    p);
        }

        textRect.draw(c, x + 4, y + 4);
    }

    public static String getDomain() {
        SettingStore settingStore = SettingStore.getInstance(WallpapersApplication.sContext);
        return settingStore.getDomain();
    }

    public static String getDomainLocal() {
        return "http://206.189.141.200/jesuswallpaper/";
    }

    public static String getDomainImages() {
        SettingStore settingStore = SettingStore.getInstance(WallpapersApplication.sContext);
        return settingStore.getImageDomain();
    }

    public static boolean isAutoRotateEnable(Context context) {
        if (android.provider.Settings.System.getInt(context.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            return true;
        } else {
            return false;
        }
    }


    public static void deletePostImage(Context activity, String downloadedPath) {
        File fdelete = new File(downloadedPath);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.e("-->", "file Deleted :" + downloadedPath);
                scanToGallery(activity, downloadedPath);
            } else {
                Log.e("-->", "file not Deleted :" + downloadedPath);
            }
        }
    }

    public static void sharedPostWhatsApp(Context activity, String downloadedPath, boolean isVideo) {

        Logger.e("downloadedPath", "" + downloadedPath);

//		Uri imageUri = Uri.parse(downloadedPath);
        try {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);

            File file = new File(downloadedPath);
            Uri photoURI = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);

            //Add text and then Image URI
            shareIntent.putExtra(Intent.EXTRA_TEXT, "");
            shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
            if (isVideo)
                shareIntent.setType("video/mp4");
            else
                shareIntent.setType("image/jpg");
            String package_name = "com.whatsapp";
            if (CommonFunctions.isPackageInstalled(activity, package_name)) {
                shareIntent.setPackage(package_name);
                try {
                    activity.startActivity(shareIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(activity, activity.getResources().getString(R.string.label_wa_not_install), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, activity.getResources().getString(R.string.label_appnotinstall) + "", Toast.LENGTH_SHORT).show();
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + package_name)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sharedPost(Context activity, String text, String downloadedPath, String package_name, boolean isVideo) {

        Logger.e("downloadedPath", "" + downloadedPath);

        if (TextUtils.isEmpty(text)) {
//			if(isVideo)
            text = activity.getResources().getString(R.string.label_beautiful_uniq_wall) + AppConstant.STORE_LINK;
//			else {
//				if(isShortText)
//					text="Most Beautiful and Unique collection of 4K and Live Wallpapers. Available on Play Store, Download Now - " + AppConstant.STORE_LINK;
//				else
//					text = "Most Beautiful and Unique collection of 4K Wallpapers. This App has Live Wallpaper, Double Wallpaper, Edge Lighting Wallpaper, Exclusive Moving Wallpaper and Auto Wallpaper Changer which can change background Image Automatically after specified time interval. Available on Play Store, Download Now - " + AppConstant.STORE_LINK;
//			}
        }
//		Uri imageUri = Uri.parse(downloadedPath);
        try {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);

            File file = new File(downloadedPath);
            Uri photoURI = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);

            //Add text and then Image URI
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
            shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
            if (isVideo)
                shareIntent.setType("video/mp4");
            else
                shareIntent.setType("image/jpg");
//		shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (TextUtils.isEmpty(package_name)) {
                try {
                    activity.startActivity(shareIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(activity, activity.getResources().getString(R.string.label_wa_not_install), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (package_name.equalsIgnoreCase("-1")) {
                Toast.makeText(activity, activity.getResources().getString(R.string.dwn_at) + downloadedPath, Toast.LENGTH_SHORT).show();

            } else if (CommonFunctions.isPackageInstalled(activity, package_name)) {
                shareIntent.setPackage(package_name);
                try {
                    activity.startActivity(shareIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(activity, activity.getResources().getString(R.string.label_wa_not_install), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, activity.getResources().getString(R.string.label_appnotinstall), Toast.LENGTH_SHORT).show();
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + package_name)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + package_name)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getNavigationBarHeight(Context context) {
        if (!hasNavBar(context))
            return 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static boolean hasNavBar(Context context) {
        Resources resources = context.getResources();
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            return resources.getBoolean(id);
        } else {    // Check for keys
            boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            return !hasMenuKey && !hasBackKey;
        }
    }

    public static void downloadFile(Context context, String url, File outputFile) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            return; // swallow a 404
        } catch (IOException e) {
            return; // swallow a 404
        }
    }

    public static void saveToSettingData(SettingStore setting) {
        try {
            File path = new File(CommonFunctions.getSavedFilePath());
            path.mkdirs();
            if (path.exists()) {
            }
            File files[] = path.listFiles();

            StringBuilder finalString = new StringBuilder();
            for (int i = 0; i < files.length; i++) {
                finalString.append(files[i]);
                finalString.append("#");

            }
            String store = finalString.toString();
            if (!TextUtils.isEmpty(store))
                setting.setImages(store.substring(0, store.length() - 1));
            else
                setting.setImages("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getExternalCacheDir() {
        try {
            return WallpapersApplication.sContext.getExternalFilesDir(null).getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return createBaseDirectory();
        }
//		return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getCacheDir() {
        try {
            return WallpapersApplication.sContext.getCacheDir().getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return createBaseDirectory();
        }
//		return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String createBaseDirectoryCache() {
        File file = new File(getCacheDir(), "Videos");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("getReceiveFilePath :: ", "Problem creating Image folder");

            }
        }
        Logger.e("file.getAbsolutePath()", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static String createFourKDirectoryCache() {
        File file = new File(getCacheDir(), "4K");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("getReceiveFilePath :: ", "Problem creating Image folder");

            }
        }
        Logger.e("file.getAbsolutePath()", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static String createBaseDirectoryCacheDoubleWall() {
        File file = new File(getCacheDir(), "Double");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("getReceiveFilePath :: ", "Problem creating Image folder");

            }
        }
        Logger.e("file.getAbsolutePath()", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static void scanToGallery(Context context, String filePath) {


        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//				Uri contentUri = Uri.fromFile(new File(filePath));
//
//				Intent mediaScanIntent = new Intent(
//						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//				mediaScanIntent.setData(contentUri);
//				context.sendBroadcast(mediaScanIntent);

                String[] strArr = new String[1];
                strArr[0] = new File(filePath).getAbsolutePath();
                MediaScannerConnection.scanFile(context, strArr, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String str, Uri uri) {
                    }
                });

            } else {
                context.sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void report(final Context context, final String post_id) {

        try {
            if (((Activity) context).isFinishing()) {
                return;
            }


            android.app.AlertDialog.Builder builder = null;
            boolean isDarkTheme = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SettingStore settingStore = SettingStore.getInstance(context);

                if (settingStore.getTheme() == 0) {
                    isDarkTheme = false;
                } else if (settingStore.getTheme() == 1) {
                    isDarkTheme = true;
                }
                if (isDarkTheme)
                    builder = new android.app.AlertDialog.Builder(context, R.style.CustomAlertDialog);
                else
                    builder = new android.app.AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert);

            } else {
                builder = new android.app.AlertDialog.Builder(context);
            }

//        builderSingle.setIcon(R.drawable.ic_launcher);
            builder.setTitle(context.getResources().getString(R.string.reportItem) + post_id);

            final boolean finalIsDarkTheme = isDarkTheme;
            final ArrayAdapter< String > arrayAdapter = new ArrayAdapter< String >(context, android.R.layout.select_dialog_item) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    // Initialize a TextView for ListView each Item
                    TextView tv = (TextView) view.findViewById(android.R.id.text1);
                    if (finalIsDarkTheme) {
                        // Set the text color of TextView (ListView Item)
                        tv.setTextColor(Color.GRAY);
                    }
                    tv.setTextSize(context.getResources().getDimension(R.dimen.small_text_size66));

                    // Generate ListView Item using TextView
                    return view;
                }
            };

            arrayAdapter.add(context.getResources().getString(R.string.label_1));
            arrayAdapter.add(context.getResources().getString(R.string.label_2));
            arrayAdapter.add(context.getResources().getString(R.string.label_3));
            arrayAdapter.add(context.getResources().getString(R.string.label_4));


            builder.setNegativeButton(context.getResources().getString(R.string.label_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String strName = arrayAdapter.getItem(which);

                    EventManager.sendEvent(EventManager.LBL_HOME, EventManager.ATR_KEY_REPORT, EventManager.ATR_VALUE_REPORT);

                    try {
                        final Intent intent = new Intent(Intent.ACTION_SEND);
//                    intent.setClassName("com.google.android.gm","com.google.android.gm.ComposeActivityGmail");
                        intent.setType("plain/text");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConstant.EMAIL});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Report Item ID: " + post_id);
                        intent.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.label_why) + strName);
                        intent.setPackage("com.google.android.gm");
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            final Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("plain/text");
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConstant.EMAIL});
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Report Item ID: " + post_id);
                            intent.putExtra(Intent.EXTRA_TEXT, context.getResources().getString(R.string.label_why) + strName);
                            context.startActivity(intent);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                }
            });
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getLoadingColor(Context context) {
        SettingStore settingStore = SettingStore.getInstance(context);
//		return R.drawable.placeholder;
        if (settingStore.getTheme() == 0)
            return R.drawable.placeholder;
        else
            return R.drawable.placeholder_dark;

//		return settingStore.getTheme()==0?R.drawable.placeholder:R.drawable.placeholder_dark;
    }

    public static int getLoadingColorBlack(Context context) {
        SettingStore settingStore = SettingStore.getInstance(context);
//		return R.drawable.placeholder;
        if (settingStore.getTheme() == 0)
            return R.drawable.placeholder_black;
        else
            return R.drawable.placeholder_dark;

//		return settingStore.getTheme()==0?R.drawable.placeholder:R.drawable.placeholder_dark;
    }

//	public static Drawable getLoadingColorChange(Context context,int color){
//		SettingStore settingStore=SettingStore.getInstance(context);
////		return R.drawable.placeholder;
//		Drawable background=context.getDrawable(R.drawable.placeholder);
////		if(settingStore.getTheme()==0)
////			return R.drawable.placeholder;
////		else
////			return R.drawable.placeholder_dark;
//
//		 ((GradientDrawable)background).setColor(ContextCompat.getColor(context,color));
//		return background;
////		return settingStore.getTheme()==0?R.drawable.placeholder:R.drawable.placeholder_dark;
//	}

    public static Bitmap fastblur1(Bitmap input, int radius, Context context) {
        try {
            // input = getResizedBitmap(input, input.getWidth() / 2,
            // input.getHeight() / 2);
            // System.gc();
            RenderScript rsScript = RenderScript.create(context);
            Allocation alloc = Allocation.createFromBitmap(rsScript, input);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript,
                    Element.U8_4(rsScript));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                blur.setRadius(radius);
            }
            blur.setInput(alloc);
            Bitmap result;

            try {
                result = Bitmap.createBitmap(input.getWidth(),
                        input.getHeight(), AppConstant.bitmapConfig8888);
            } catch (OutOfMemoryError e) {
                result = Bitmap.createBitmap(input.getWidth() / 2,
                        input.getHeight() / 2, AppConstant.bitmapConfig8888);
            }
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);

            rsScript.destroy();
            return result;
        } catch (Exception e) {
            Logger.printStackTrace(e);
            return input;
        } catch (Error e) {
            Logger.printStackTrace(e);
            return input;
        }
    }

    public static String createSpashDirCache() {
        File file = new File(getExternalCacheDir(), "splash");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("getReceiveFilePath :: ", "Problem creating Image folder");

            }
        }
        Logger.e("file.getAbsolutePath()", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static String createEventDirCache() {
        File file = new File(getExternalCacheDir(), "events");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("getReceiveFilePath :: ", "Problem creating Image folder");

            }
        }
        Logger.e("file.getAbsolutePath()", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static void setOverlayAction(Activity activity) {
        if (activity.getWindow() != null)
            activity.getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
    }

    public static void updateWindow(AppCompatActivity appCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = appCompatActivity.getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            int actionbarHeight = CommonFunctions.getNavigationBarHeight(appCompatActivity);
            if (CommonFunctions.hasNavBar(appCompatActivity)) {
                ImageView ivBottomView = appCompatActivity.findViewById(R.id.ivBottomView);
                if (ivBottomView != null) {
                    ivBottomView.getLayoutParams().height = actionbarHeight;
                }
//				View viewBottomDivider = appCompatActivity.findViewById(R.id.viewBottomDivider);
//				if (viewBottomDivider != null) {
//					viewBottomDivider.setVisibility(View.VISIBLE);
//				}
            } else {
//				View viewBottomDivider = appCompatActivity.findViewById(R.id.viewBottomDivider);
//				if (viewBottomDivider != null) {
//					viewBottomDivider.setVisibility(View.GONE);
//				}
            }
        } else {
            View decorView = appCompatActivity.getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

    }

//	public static void updateWindow(AppCompatActivity appCompatActivity) {
//
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			Window w = appCompatActivity.getWindow();
//			w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//			int actionbarHeight = Common.getActionBarHeight(appCompatActivity);
//			if (Common.hasNavBar(appCompatActivity)) {
//				ImageView ivBottomView = appCompatActivity.findViewById(R.id.ivBottomView);
//				if (ivBottomView != null) {
//					ivBottomView.getLayoutParams().height = actionbarHeight;
//				}
////				View viewBottomDivider = appCompatActivity.findViewById(R.id.viewBottomDivider);
////				if (viewBottomDivider != null) {
////					viewBottomDivider.setVisibility(View.VISIBLE);
////				}
//			} else {
////				View viewBottomDivider = appCompatActivity.findViewById(R.id.viewBottomDivider);
////				if (viewBottomDivider != null) {
////					viewBottomDivider.setVisibility(View.GONE);
////				}
//			}
//		} else {
//			View decorView = appCompatActivity.getWindow().getDecorView();
//			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//					View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//		}
//
////		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
////			Window w = appCompatActivity.getWindow();
////			w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
////			int actionbarHeight = Common.getActionBarHeight(appCompatActivity);
////			/*if (Common.hasNavBar(appCompatActivity)) {
////				ImageView ivBottomView = appCompatActivity.findViewById(R.id.ivBottomView);
////				if (ivBottomView != null) {
////					ivBottomView.getLayoutParams().height = actionbarHeight;
////				}
////				View viewBottomDivider = appCompatActivity.findViewById(R.id.viewBottomDivider);
////				if (viewBottomDivider != null) {
////					viewBottomDivider.setVisibility(View.VISIBLE);
////				}
////			} else {
////				View viewBottomDivider = appCompatActivity.findViewById(R.id.viewBottomDivider);
////				if (viewBottomDivider != null) {
////					viewBottomDivider.setVisibility(View.GONE);
////				}
////			}*/
////		} else {
////			View decorView = appCompatActivity.getWindow().getDecorView();
////			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
////					View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
////		}
//	}

    public static boolean isDoubleWallpaperSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_double_wall()) && WallpapersApplication.getApplication().getSettings().getIs_double_wall().equalsIgnoreCase("0")) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public static boolean isEdgeWallpaperSupported() {
        try {
            if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_edge_wall()) && WallpapersApplication.getApplication().getSettings().getIs_edge_wall().equalsIgnoreCase("0")) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean isGradientSupported() {
        try {
            if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_gradient_wall()) && WallpapersApplication.getApplication().getSettings().getIs_gradient_wall().equalsIgnoreCase("0")) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean isStatusSaverSupported(Context context) {
        try {
            if (!TextUtils.isEmpty(WallpapersApplication.getApplication().getSettings().getIs_status_saver()) && WallpapersApplication.getApplication().getSettings().getIs_status_saver().equalsIgnoreCase("0"))
                return false;
            if (!isPackageInstalled(context, "com.whatsapp")) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

//	public static int getPalletColor(Context context, Palette p) {
//		int color;
//		try {
//
//			Palette.Swatch vibrantSwatch = p.getDarkVibrantSwatch();
//			if (vibrantSwatch == null) {
//				vibrantSwatch = p.getDarkMutedSwatch();
//			}
//			if (vibrantSwatch == null) {
//				vibrantSwatch = p.getMutedSwatch();
//			}
//			if (vibrantSwatch == null) {
//				vibrantSwatch = p.getLightMutedSwatch();
//			}
//			if (vibrantSwatch == null) {
//				color = context.getResources().getColor(R.color.white);
//			} else {
//				color = vibrantSwatch.getRgb();
//			}
//		} catch (Exception e) {
//			Logger.printStackTrace(e);
//			color = context.getResources().getColor(R.color.white);
//		}
//		return color;
//	}
//
//	public static int getPalletColorLight(Context context,Palette p) {
//		int color;
//		try {
//
//			Palette.Swatch vibrantSwatch = p.getLightVibrantSwatch();
//
//			if (vibrantSwatch == null) {
//				vibrantSwatch = p.getLightMutedSwatch();
//			}
//			if (vibrantSwatch == null) {
//				vibrantSwatch = p.getMutedSwatch();
//			}
//
//			if (vibrantSwatch == null) {
//				color = context.getResources().getColor(R.color.transparent);
//			} else {
//				color = vibrantSwatch.getRgb();
//			}
//		} catch (Exception e) {
//			Logger.printStackTrace(e);
//			color = context.getResources().getColor(R.color.transparent);
//		}
//		return color;
//	}


    public static boolean checkIfVPNIsActive() {
        boolean isVPNcheck = false;
        try {
            isVPNcheck = WallpapersApplication.getApplication().getSettings().isVPN_Check();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isVPNcheck) {
            try {
                List< String > networkList = new ArrayList<>();
                try {
                    for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                        if (networkInterface.isUp())
                            networkList.add(networkInterface.getName());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                boolean isEnable = networkList.contains("tun0") || networkList.contains("ppp0");
                EventManager.sendEvent(EventManager.LBL_HOME, EventManager.LBL_VPN, "" + isEnable);
                return isEnable;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isEmpty(String value) {
        if (TextUtils.isEmpty(value)) {
            return true;
        }
        if (value.toLowerCase().equals("null")) {
            return true;
        }
        value = value.trim();
        if (TextUtils.isEmpty(value)) {
            return true;
        }
        return false;
    }

    public static String getHashKey(Context context) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                if (something != null) {
                    something = something.trim();
                }
                if (BuildConfig.DEBUG) {
                    return "CTYllJxLac/QdPCunrQb8SiS1to=";
                }
                return something;
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
        return null;
    }

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isTestAdEnable() {
        return BuildConfig.DEBUG;
//		return true;
    }

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static int getScreenWidth(Activity mActivity) {
        final WindowManager w = (WindowManager) mActivity
                .getSystemService(Context.WINDOW_SERVICE);
        final Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getScreenHeight(Activity mActivity) {
        final WindowManager w = (WindowManager) mActivity
                .getSystemService(Context.WINDOW_SERVICE);
        final Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        return metrics.heightPixels;
    }

    public static boolean isSensorAvailable(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//		sensor=null;
        if (sensor == null) {
            displayAlertDialog(context);
            return false;
        }
        return true;
    }

    public static void displayAlertDialog(Context context) {
        try {
            if (((Activity) context).isFinishing()) {
                return;
            }

            android.app.AlertDialog.Builder builder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SettingStore settingStore = SettingStore.getInstance(context);
                boolean isDarkTheme = false;
                if (settingStore.getTheme() == 0) {
                    isDarkTheme = false;
                } else if (settingStore.getTheme() == 1) {
                    isDarkTheme = true;
                }
                if (isDarkTheme)
                    builder = new android.app.AlertDialog.Builder(context, R.style.CustomAlertDialog);
                else
                    builder = new android.app.AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert);

            } else {
                builder = new android.app.AlertDialog.Builder(context);
            }

            builder.setTitle(context.getResources().getString(R.string.toast_sensor_error));
            builder.setMessage(context.getResources().getString(R.string.label_sensornotsupport));
            final DialogInterface.OnClickListener dialogButtonClickListener =
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    };
            builder.setPositiveButton(context.getResources().getString(R.string.label_ok), dialogButtonClickListener);
            builder.setCancelable(true);

            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void displaySnackBarWithBottomMargin(Snackbar snackbar, int sideMargin, int marginBottom) {
        final View snackBarView = snackbar.getView();
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) snackBarView.getLayoutParams();

        params.setMargins(params.leftMargin + sideMargin,
                params.topMargin,
                params.rightMargin + sideMargin,
                params.bottomMargin + marginBottom);

        snackBarView.setLayoutParams(params);
        snackbar.show();
    }

    public static void showDialogCustom(Context mContext, String title, String message, String ok, String cancel, boolean isCancelable, final DialogOnButtonClickListener dialogOnButtonClickListener) {
        try {
            if (((Activity) mContext).isFinishing()) {
                return;
            }

            android.app.AlertDialog.Builder builder = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SettingStore settingStore = SettingStore.getInstance(mContext);
                boolean isDarkTheme = false;
                if (settingStore.getTheme() == 0) {
                    isDarkTheme = false;
                } else if (settingStore.getTheme() == 1) {
                    isDarkTheme = true;
                }
                if (isDarkTheme)
                    builder = new android.app.AlertDialog.Builder(mContext, R.style.CustomAlertDialog);
                else
                    builder = new android.app.AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light_Dialog_Alert);

            } else {
                builder = new android.app.AlertDialog.Builder(mContext);
            }
            builder.setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            if (dialogOnButtonClickListener != null) {
                                dialog.dismiss();
                                dialogOnButtonClickListener.onOKButtonCLick();
                            }

                        }
                    });
            if (!TextUtils.isEmpty(cancel))
                builder.setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            builder.setCancelable(isCancelable);

            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String createAutoChagerDirCache() {
        File file = new File(getCacheDir(), "Auto");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("getReceiveFilePath :: ", "Problem creating Image folder");

            }
        }
        Logger.e("file.getAbsolutePath()", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public static boolean isNightMode(Context context) {
        try {
            boolean isNightMode = false;
            int nightModeFlags =
                    context.getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;
            Logger.e("isNightMode", "" + nightModeFlags);
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    isNightMode = true;
                    break;

                case Configuration.UI_MODE_NIGHT_NO:
                    isNightMode = false;
                    break;

                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    isNightMode = false;
                    break;
            }
            return isNightMode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isNewLayout(Activity context) {

        try {
            WindowManager windowManager =
                    (WindowManager) WallpapersApplication.getApplication().getSystemService(Context.WINDOW_SERVICE);
            final Display display = windowManager.getDefaultDisplay();
            Point outPoint = new Point();
            if (Build.VERSION.SDK_INT >= 19) {
                // include navigation bar
                display.getRealSize(outPoint);
            } else {
                // exclude navigation bar
                display.getSize(outPoint);
            }
            int mRealSizeHeight, mRealSizeWidth;
            if (outPoint.y > outPoint.x) {
                mRealSizeHeight = outPoint.y;
                mRealSizeWidth = outPoint.x;
            } else {
                mRealSizeHeight = outPoint.x;
                mRealSizeWidth = outPoint.y;
            }
            if (mRealSizeHeight > 2200) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getDpi(Context context) {

        int dpi = 0;
        try {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();

            @SuppressWarnings("rawtypes")
            Class c;
            try {
                c = Class.forName("android.view.Display");
                @SuppressWarnings("unchecked")
                Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, displayMetrics);
                dpi = displayMetrics.heightPixels;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return dpi;
    }


    public static String getExtension(String path, boolean isVideo) {
        try {
            int strLength = path.lastIndexOf(".");
            if (strLength > 0)
                return path.substring(strLength).toLowerCase();
            return isVideo ? AppConstant.DOWNLOAD_EXTENTION_VIDEO : AppConstant.DOWNLOAD_EXTENTION;
        } catch (Exception e) {
            e.printStackTrace();
            return isVideo ? AppConstant.DOWNLOAD_EXTENTION_VIDEO : AppConstant.DOWNLOAD_EXTENTION;
        }
    }

    public static String getFileNameFromURL(String path) {
        try {
            String filename = path.substring(path.lastIndexOf("/") + 1);
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    public static void copyFile(Context context, String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath + "/" + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

            Toast.makeText(context, context.getResources().getString(R.string.label_dwn_s), Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public static boolean isProIconShow() {
        try {
            if (WallpapersApplication.getApplication().getSettings() != null && WallpapersApplication.getApplication().getSettings().is_pro_show()) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isAdManagerShow() {
//		return true;
        try {
            if (WallpapersApplication.getApplication().getSettings() != null && WallpapersApplication.getApplication().getSettings().getIs_AdManger_show_failed()) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isGreedyGameShow() {
        try {
            if (WallpapersApplication.getApplication().getSettings() != null && WallpapersApplication.getApplication().getSettings().isGGshowOnFailed()) {
                return true;
            } else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    private static final String NEW_FOLDER_NAME = "4K Wallgenic Wallpapers";
    private static final String OLD_FOLDER_NAME = "Craft Wallpapers";

    public static void renameSavedFolderIfAvailable() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File old_file = new File(Environment.getExternalStorageDirectory(), OLD_FOLDER_NAME);
                        File newFolder = new File(createBaseDirectory());
                        if (old_file != null && old_file.exists() && newFolder != null && newFolder.exists()) {
                            String[] file_list = old_file.list();
                            for (int i = 0; i < file_list.length; i++) {
                                String file_name = file_list[i];
                                String filePath = old_file.getAbsolutePath() + "/" + file_name;
                                String filePathNew = newFolder.getAbsolutePath() + "/" + file_name;
                                if (new File(filePathNew).exists()) {
                                    deleteFileFromPath(filePath);
                                    continue;
                                }
                                if (new File(filePath).exists()) {
                                    boolean success = false;
                                    success = new File(filePath).renameTo(new File(filePathNew));
                                    Logger.i("FileMove", "FileMove Success 1:: " + success);
                                    if (!success) {
                                        success = moveFileIndirect(old_file.getParent() + "/", old_file.getName(), newFolder.getParent() + "/");
                                    }
                                    Logger.i("FileMove", "FileMove Success 2:: " + success);
                                }
                            }
                            scanToGallery(WallpapersApplication.getApplication(), newFolder.getAbsolutePath());
                            scanToGallery(WallpapersApplication.getApplication(), old_file.getAbsolutePath());
                        }
                        try {
                            if (old_file != null && old_file.exists() && old_file.list() != null
                                    && old_file.list().length == 0) {
                                old_file.delete();
                            }
                        } catch (Exception e) {
                            //e.printStackTrace();
                            EventManager.sendEvent(EventManager.LBL_TRANSFER_FILE, "Picture", "Delete:" + e.getMessage());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        EventManager.sendEvent(EventManager.LBL_TRANSFER_FILE, "Picture", "Outer:" + e.getMessage());
                    }
                }
            }).start();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private static boolean moveFileIndirect(String inputPath, String inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File dir1 = new File(outputPath + inputFile);
            if (!dir1.exists()) {
                dir1.createNewFile();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;
            // delete the original file
            new File(inputPath + inputFile).delete();
            return true;
        } catch (Exception fnfe1) {
            fnfe1.printStackTrace();
            Log.e("tag", fnfe1.getMessage());
        }
        return false;
    }

    private static void deleteFileFromPath(String filePath) {
        try {
            File fdelete = new File(filePath);
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    System.out.println("file Deleted :" + filePath);
                } else {
                    System.out.println("file not Deleted :" + filePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
