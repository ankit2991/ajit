package macro.hd.wallpapers.Utilily;

import android.os.Environment;
import android.util.Log;

import macro.hd.wallpapers.BuildConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

//

/**
 * Util class for controlling the SDK Log the {@code BuildConfig.DEBUG} flag.
 */
public class Logger {// post_share

//	/**
//	 * It was added for Save Offline feature enable/disable in v4.3.
//	 *
//	 * @deprecated No use of now as Save Offline(Download) is feature for all newer app version.
//	 */
//	@Deprecated
//	public static final boolean isSaveOffline = true;

    /**
     * It's for enableing and disabling device log.<br><br>
     * Set it <b>false</b> for <b>SPA(market)</b> version.
     */
    public static final boolean isDebuggable = BuildConfig.DEBUG;//
    // BuildConfig.DEBUG;//true;

    /**
     * It's for enableing and disabling for writing log file on SDCard.<br><br>
     * Set it <b>false</b> for <b>SPA(market)</b> version.
     */
    private static final boolean isWriteTofile = false;//true;

    //public static final boolean enableAndroidM_Permission = false;

    public static void writetofile(String tag, String message) {
        if (isWriteTofile) {
            File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Jesus_logs.txt");//
            try {
                if (!f.exists())
                    f.createNewFile();
                FileWriter writer = new FileWriter(f, true);
                writer.write("\n\r" + tag + new Date() + " ::: " + message);
                writer.close();
            } catch (IOException e) {
                printStackTrace(e);
            }
        }
    }

    /**
     * Set an info log message.
     *
     * @param tag     for the log message.
     * @param message Log to output to the console.
     */
    public static void i(String tag, String message) {
        try {
            // if (BuildConfig.DEBUG)
            if (isDebuggable) {
                writetofile(tag, message);
                Log.i(tag, message);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Set an info log message.
     *
     * @param Tag
     *            for the log message.
     * @param message
     *            Log to output to the console.
     */
    // public static void ii(String tag, String message) {
    // // if (BuildConfig.DEBUG)
    // try {
    // Log.i(tag, message);
    // } catch (Exception e) {
    // }
    // }

    /**
     * Set an error log message.
     *
     * @param tag     for the log message.
     * @param message Log to output to the console.
     */
    public static void e(String tag, String message) {
        try {
            // if (BuildConfig.DEBUG)
            if (isDebuggable) {
                Log.e(tag, message);
                writetofile(tag, message);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Set a warning log message.
     *
     * @param tag     for the log message.
     * @param message Log to output to the console.
     */

    public static void w(String tag, String message) {
        try {
            // if (BuildConfig.DEBUG)
            if (isDebuggable) {
                Log.w(tag, message);
                writetofile(tag, message);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Set a debug log message.
     *
     * @param tag     for the log message.
     * @param message Log to output to the console.
     */
    public static void d(String tag, String message) {
        try {
            // if (BuildConfig.DEBUG)
            if (isDebuggable) {
                Log.d(tag, message);
                writetofile(tag, message);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Set a verbose log message.
     *
     * @param tag     for the log message.
     * @param message Log to output to the console.
     */
    public static void v(String tag, String message) {
        try {
            // if (BuildConfig.DEBUG)
            if (isDebuggable) {
                Log.v(tag, message);
                writetofile(tag, message);
            }
        } catch (Exception e) {
        }
    }

    public static void s(String message) {
        try {
            // if (BuildConfig.DEBUG)
            if (isDebuggable) {
                System.out.println("safestart : " + message);
                writetofile("safestart : ", message);
            }
        } catch (Exception e) {
        }
    }

    public static void printStackTrace(Exception e) {
        try {
//			Crashlytics.logException(e);

            // if (BuildConfig.DEBUG)
            if (isDebuggable) {
                e.printStackTrace();
//				writetofile("Hungama : ", e.getMessage());
            }
        } catch (Exception e1) {
        }
    }

    public static void printStackTrace(Error e) {
        try {
            // if (BuildConfig.DEBUG)
            if (isDebuggable) {
                e.printStackTrace();
//				writetofile("Hungama : ", e.getMessage());
            }
        } catch (Exception e1) {
        }
    }

    private static final void removeOldFile(String fileName) {
        try {
            File f = new File(fileName);//
            if (f.exists())
                f.delete();
        } catch (Exception e) {
            printStackTrace(e);
        }
    }
}
