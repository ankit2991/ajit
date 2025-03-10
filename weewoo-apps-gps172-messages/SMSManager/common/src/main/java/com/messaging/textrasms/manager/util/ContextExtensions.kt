package com.messaging.textrasms.manager.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

fun Context.getColorCompat(colorRes: Int): Int {
    return tryOrNull { ContextCompat.getColor(this, colorRes) } ?: Color.BLACK
}

fun Context.getColorStateListCompat(colorStateListRes: Int): ColorStateList {
    return tryOrNull { ContextCompat.getColorStateList(this, colorStateListRes) }
        ?: ColorStateList.valueOf(Color.BLACK)
}

fun Context.resolveThemeAttribute(attributeId: Int, default: Int = 0): Int {
    val outValue = TypedValue()
    val wasResolved = theme.resolveAttribute(attributeId, outValue, true)

    return if (wasResolved) outValue.resourceId else default
}

fun Context.resolveThemeBoolean(attributeId: Int, default: Boolean = false): Boolean {
    val outValue = TypedValue()
    val wasResolved = theme.resolveAttribute(attributeId, outValue, true)

    return if (wasResolved) outValue.data != 0 else default
}

fun Context.resolveThemeColor(attributeId: Int, default: Int = 0): Int {
    val outValue = TypedValue()
    val wasResolved = theme.resolveAttribute(attributeId, outValue, true)

    return if (wasResolved) getColorCompat(outValue.resourceId) else default
}

fun Context.resolveThemeColorStateList(attributeId: Int, default: Int = 0): ColorStateList {
    val outValue = TypedValue()
    val wasResolved = theme.resolveAttribute(attributeId, outValue, true)


    return getColorStateListCompat(if (wasResolved) outValue.resourceId else default)
}

fun Context.makeToast(@StringRes res: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, res, duration).show()
}

fun Context.makeToast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

fun Context.isInstalled(packageName: String): Boolean {
    return tryOrNull(false) { packageManager.getApplicationInfo(packageName, 0).enabled } ?: false
}

val Context.versionCode: Int
    get() = packageManager.getPackageInfo(packageName, 0).versionCode
