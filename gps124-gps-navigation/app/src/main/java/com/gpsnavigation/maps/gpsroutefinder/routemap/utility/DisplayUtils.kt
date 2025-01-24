package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager


fun dpToPx(dp: Float, context: Context): Int {
    return dpToPx(dp, context.resources)
}

private fun dpToPx(dp: Float, resources: Resources): Int {
    val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    return px.toInt()
}

fun dip2px(context: Context, dpValue: Float): Int {
    val scale = context.getResources().getDisplayMetrics().density
    return (dpValue * scale + 0.5f).toInt()
}


fun px2dip(context: Context, pxValue: Float): Int {
    val scale = context.getResources().getDisplayMetrics().density
    return (pxValue / scale + 0.5f).toInt()
}


fun px2sp(context: Context, pxValue: Float): Int {
    val fontScale = context.getResources().getDisplayMetrics().scaledDensity
    return (pxValue / fontScale + 0.5f).toInt()
}


fun sp2px(context: Context, spValue: Float): Int {
    val fontScale = context.getResources().getDisplayMetrics().scaledDensity
    return (spValue * fontScale + 0.5f).toInt()
}


fun getDialogW(activity: Activity): Int {
    val dm: DisplayMetrics
    dm = activity.resources.displayMetrics
    return dm.widthPixels - 100
}


fun getScreenW(activity: Activity): Int {
    val dm: DisplayMetrics
    dm = activity.resources.displayMetrics
    return dm.widthPixels
}


fun getScreenH(activity: Activity): Int {
    var dm = DisplayMetrics()
    dm = activity.resources.displayMetrics
    return dm.heightPixels
}


fun toggleKeyboard(context: Context) {
    val imm = context
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isActive()) {
        imm.toggleSoftInput(
            InputMethodManager.SHOW_IMPLICIT,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}