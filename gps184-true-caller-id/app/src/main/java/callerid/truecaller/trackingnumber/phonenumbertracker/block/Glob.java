package callerid.truecaller.trackingnumber.phonenumbertracker.block;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Glob {

    public static final String MAIN_PREFS = "main_prefs";
    public static SharedPreferences questionanswersharedpreferences;
    public static boolean fromprivacy = false;
    public static int i = 0;


    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
