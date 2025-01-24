package com.klinker.android.send_message;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.mms.service_alt.MmsNetworkManager;
import com.android.mms.service_alt.exception.MmsNetworkException;
import com.google.android.mms.util_alt.SqliteWrapper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class Utils {
    public static final String GSM_CHARACTERS_REGEX = "^[A-Za-z0-9 \\r\\n@Ł$ĽčéůěňÇŘřĹĺ\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u03A3\u0398\u039EĆćßÉ!\"#$%&'()*+,\\-./:;<=>?ĄÄÖŃÜ§żäöńüŕ^{}\\\\\\[~\\]|\u20AC]*$";
    public static final int DEFAULT_SUBSCRIPTION_ID = 1;
    private static final Pattern EMAIL_ADDRESS_PATTERN
            = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );
    private static final Pattern NAME_ADDR_EMAIL_PATTERN =
            Pattern.compile("\\s*(\"[^\"]*\"|[^<>\"]+)\\s*<([^<>]+)>\\s*");

    public static String getMyPhoneNumber(Context context) {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    public static <T> T ensureRouteToMmsNetwork(Context context, String url, String proxy, Task<T> task) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ensureRouteToMmsNetworkMarshmallow(context, task);
        } else {
            return ensureRouteToMmsNetworkLollipop(context, task);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static <T> T ensureRouteToMmsNetworkMarshmallow(Context context, Task<T> task) throws IOException {
        final MmsNetworkManager networkManager = new MmsNetworkManager(context.getApplicationContext(), Utils.getDefaultSubscriptionId());
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network network = null;
        try {
            network = networkManager.acquireNetwork();
            connectivityManager.bindProcessToNetwork(network);
            return task.run();
        } catch (MmsNetworkException e) {
            throw new IOException(e);
        } finally {
            if (network != null) {
                connectivityManager.bindProcessToNetwork(null);
            }
            networkManager.releaseNetwork();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static <T> T ensureRouteToMmsNetworkLollipop(Context context, Task<T> task) throws IOException {
        final MmsNetworkManager networkManager = new MmsNetworkManager(context.getApplicationContext(), Utils.getDefaultSubscriptionId());
        Network network = null;
        try {
            network = networkManager.acquireNetwork();
            ConnectivityManager.setProcessDefaultNetwork(network);
            return task.run();
        } catch (MmsNetworkException e) {
            throw new IOException(e);
        } finally {
            if (network != null) {
                ConnectivityManager.setProcessDefaultNetwork(null);
            }
            networkManager.releaseNetwork();
        }
    }

    public static void ensureRouteToHost(Context context, String url, String proxy) throws IOException {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        InetAddress inetAddr;
        if (proxy != null && proxy.trim().length() != 0) {
            try {
                inetAddr = InetAddress.getByName(proxy);
            } catch (UnknownHostException e) {
                throw new IOException("Cannot establish route for " + url +
                        ": Unknown proxy " + proxy);
            }
            try {
                Method requestRoute = ConnectivityManager.class.getMethod("requestRouteToHostAddress", Integer.TYPE, InetAddress.class);
                if (!((Boolean) requestRoute.invoke(connMgr, ConnectivityManager.TYPE_MOBILE_MMS, inetAddr))) {
                    throw new IOException("Cannot establish route to proxy " + inetAddr);
                }
            } catch (Exception e) {
                Timber.e(e, "Cannot establishh route to proxy " + inetAddr);
            }
        } else {
            Uri uri = Uri.parse(url);
            try {
                inetAddr = InetAddress.getByName(uri.getHost());
            } catch (UnknownHostException e) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            }
            try {
                Method requestRoute = ConnectivityManager.class.getMethod("requestRouteToHostAddress", Integer.TYPE, InetAddress.class);
                if (!((Boolean) requestRoute.invoke(connMgr, ConnectivityManager.TYPE_MOBILE_MMS, inetAddr))) {
                    throw new IOException("Cannot establish route to proxy " + inetAddr);
                }
            } catch (Exception e) {
                Timber.e(e, "Cannot establishh route to proxy " + inetAddr + " for " + url);
            }
        }
    }

    public static Boolean isMobileDataEnabled(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            Class<?> c = Class.forName(cm.getClass().getName());
            Method m = c.getDeclaredMethod("getMobileDataEnabled");
            m.setAccessible(true);
            return (Boolean) m.invoke(cm);
        } catch (Exception e) {
            Timber.e(e, "exception thrown");
            return null;
        }
    }

    public static boolean isDataEnabled(TelephonyManager telephonyManager) {
        try {
            Class<?> c = telephonyManager.getClass();
            Method m = c.getMethod("getDataEnabled");
            return (boolean) m.invoke(telephonyManager);
        } catch (Exception e) {
            Timber.e(e, "exception thrown");
            return true;
        }
    }

    public static boolean isDataEnabled(TelephonyManager telephonyManager, int subId) {
        try {
            Class<?> c = telephonyManager.getClass();
            Method m = c.getMethod("getDataEnabled", int.class);
            return (boolean) m.invoke(telephonyManager, subId);
        } catch (Exception e) {
            Timber.e(e, "exception thrown");
            return isDataEnabled(telephonyManager);
        }
    }

    public static void setMobileDataEnabled(Context context, boolean enabled) {
        try {
            TelephonyManager tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(tm);
            c = Class.forName(telephonyService.getClass().getName());
            m = c.getDeclaredMethod("setDataEnabled", Boolean.TYPE);
            m.setAccessible(true);
            m.invoke(telephonyService, enabled);
        } catch (Exception e) {
            Timber.e(e, "error enabling data on lollipop");
        }

    }

    public static int getNumPages(Settings settings, String text) {
        if (settings.getStripUnicode()) {
            text = StripAccents.stripAccents(text);
        }

        int[] data = SmsMessage.calculateLength(text, false);
        return data[0];
    }

    public static long getOrCreateThreadId(Context context, String recipient) {
        Set<String> recipients = new HashSet<String>();

        recipients.add(recipient);
        return getOrCreateThreadId(context, recipients);
    }

    public static long getOrCreateThreadId(
            Context context, Set<String> recipients) {
        Uri.Builder uriBuilder = Uri.parse("content://mms-sms/threadID").buildUpon();

        for (String recipient : recipients) {
            if (isEmailAddress(recipient)) {
                recipient = extractAddrSpec(recipient);
            }

            uriBuilder.appendQueryParameter("recipient", recipient);
        }

        Uri uri = uriBuilder.build();
        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                uri, new String[]{"_id"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long id = cursor.getLong(0);
                    cursor.close();
                    return id;
                }
            } finally {
                cursor.close();
            }
        }

        Random random = new Random();
        return random.nextLong();
    }

    public static boolean doesThreadIdExist(Context context, long threadId) {
        Uri uri = Uri.parse("content://mms-sms/conversations/" + threadId + "/");

        Cursor cursor = context.getContentResolver().query(uri, new String[]{"_id"}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return true;
        } else {
            return false;
        }
    }

    private static boolean isEmailAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return false;
        }

        String s = extractAddrSpec(address);
        Matcher match = EMAIL_ADDRESS_PATTERN.matcher(s);
        return match.matches();
    }

    private static String extractAddrSpec(String address) {
        Matcher match = NAME_ADDR_EMAIL_PATTERN.matcher(address);

        if (match.matches()) {
            return match.group(2);
        }
        return address;
    }

    public static Settings getDefaultSendSettings(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Settings sendSettings = new Settings();

        sendSettings.setMmsc(sharedPrefs.getString("mmsc_url", ""));
        sendSettings.setProxy(sharedPrefs.getString("mms_proxy", ""));
        sendSettings.setPort(sharedPrefs.getString("mms_port", ""));
        sendSettings.setAgent(sharedPrefs.getString("mms_agent", ""));
        sendSettings.setUserProfileUrl(sharedPrefs.getString("mms_user_agent_profile_url", ""));
        sendSettings.setUaProfTagName(sharedPrefs.getString("mms_user_agent_tag_name", ""));
        sendSettings.setStripUnicode(sharedPrefs.getBoolean("strip_unicode", false));

        return sendSettings;
    }

    public static boolean isDefaultSmsApp(Context context) {
        return context.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(context));

    }

    public static boolean isMmsOverWifiEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("mms_over_wifi", false);
    }

    public static int getDefaultSubscriptionId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return SmsManager.getDefaultSmsSubscriptionId();
        } else {
            return DEFAULT_SUBSCRIPTION_ID;
        }
    }

    public interface Task<T> {
        T run() throws IOException;
    }
}
