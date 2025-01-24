package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.play.core.review.ReviewManagerFactory
import com.gpsnavigation.maps.gpsroutefinder.routemap.BuildConfig
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.PaywallUi
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.*
import java.util.*

fun AppCompatActivity.showRatingDialog() {
    val manager = ReviewManagerFactory.create(this)

    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // We got the ReviewInfo object
            val reviewInfo = task.result
            val flow = manager.launchReviewFlow(this, reviewInfo)
            flow.addOnCompleteListener { _ ->

                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
            }
        } else {
            // There was some problem, log or handle the error code.
            Log.d("IN-APP-REVIEW", "showRatingDialog: " + task.exception?.message)
            //@ReviewErrorCode val reviewErrorCode = (task.getException() as TaskException).errorCode
        }
    }
}

/**
 * Sets user as premium
 * Saves buy time
 */
fun AppCompatActivity.setUserAsPremium(time: Long) {
    TinyDB.getInstance(this).putBoolean(IS_PREMIUM, true)
    TinyDB.getInstance(this).putLong(SUBSCRIPTION_ID_YEAR_BUY_TIME, time)
}

fun AppCompatActivity.handleAppOpenTimes() {
    val openTimes = getPrefInt(APP_OPEN_TIMES)

    //If its subscribed, 1st open after 1st subscription period renovation
    if (isPremium()) {

        putPrefInt(APP_OPEN_TIMES, openTimes + 1)
        val subscriptionBuyTime =
            getPrefLong(SUBSCRIPTION_ID_YEAR_BUY_TIME, System.currentTimeMillis());
        val calHoy = Calendar.getInstance();
        val calSubs = Calendar.getInstance()
        calSubs.timeInMillis = subscriptionBuyTime
        if (BuildConfig.DEBUG)
            calSubs.add(Calendar.MINUTE, 30)
        else
            calSubs.add(Calendar.YEAR, 1)


        val showedAfterFirstRenewal = getPrefBool(SHOWED_RATING_DIALOG_AFTER_FIRST_RENEWAL)
        if (!showedAfterFirstRenewal
            && calHoy.after(calSubs)
        ) {
            putPrefBoolean(SHOWED_RATING_DIALOG_AFTER_FIRST_RENEWAL, true)
            showRatingDialog()
        }


        //Also show rating dialog at 4th open having the yearly subscription active
        if (openTimes == YEARLY_SUBSCRIPTION_APP_OPEN_TIMES_TO_SHOW_RATING_DIALOG)
            showRatingDialog()


    }

}


fun Context.isPremium(): Boolean {
    return TinyDB.getInstance(this).getBoolean(Constants.IS_PREMIUM)
}

fun Context.isOnline(c: Context): Boolean {
    val cmg = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+
        cmg.getNetworkCapabilities(cmg.activeNetwork)?.let { networkCapabilities ->
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
    } else {
        return cmg.activeNetworkInfo?.isConnectedOrConnecting == true
    }

    return false
}

fun Context.getPrefBool(key: String): Boolean {
    return TinyDB.getInstance(this).getBoolean(key)

}

fun Context.getPrefBoolFirst(key: String): Boolean {
    return TinyDB.getInstance(this).getFirstBoolean(key)

}

fun Context.getPrefInt(key: String): Int {
    return TinyDB.getInstance(this).getInt(key)

}

fun Context.getDefaultLang(key: String): Int {
    return TinyDB.getInstance(this).getIntDictionary(key)

}

fun Context.getPrefIntOne(key: String): Int {
    return TinyDB.getInstance(this).getIntOne(key)

}

fun Context.getPrefString(key: String): String {
    return TinyDB.getInstance(this).getString(key)

}

fun Context.getPrefLong(key: String, default: Long): Long {
    return TinyDB.getInstance(this).getLong(key, default)
}

fun Context.putPrefBoolean(key: String, value: Boolean) {
    TinyDB.getInstance(this).putBoolean(key, value)
}

fun Context.putPrefInt(key: String, value: Int) {
    TinyDB.getInstance(this).putInt(key, value)
}

fun Context.putPrefString(key: String, value: String) {
    TinyDB.getInstance(this).putString(key, value)
}

fun Context.putPrefLong(key: String, value: Long) {
    TinyDB.getInstance(this).putLong(key, value)
}

fun interAdCountCriteria(currentCount: Int, defaultCount: Int = 0): Boolean {
    return if (defaultCount!=0)
        currentCount % defaultCount == 0
    else
        false
}

fun systemBarsInsets(insets: WindowInsets): Insets {
    return WindowInsetsCompat.toWindowInsetsCompat(insets)
        .getInsets(WindowInsetsCompat.Type.systemBars())
}

fun ComponentActivity.hideStatusBar() {
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.hide(WindowInsetsCompat.Type.statusBars())
}

fun ComponentActivity.openSubscriptionPaywall(placementId: String = "Default") {
    val intent = Intent(this, PaywallUi::class.java)
    intent.putExtra("paywallType", placementId)
    startActivity(intent)
}

