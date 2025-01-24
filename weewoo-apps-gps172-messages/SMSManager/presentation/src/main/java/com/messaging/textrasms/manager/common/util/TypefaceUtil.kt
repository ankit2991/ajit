package com.messaging.textrasms.manager.common.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import com.messaging.textrasms.manager.R
import java.util.*
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.math.sqrt


object TypefaceUtil {

    fun setDefaultFont(context: Context) {
        val am = context.applicationContext.assets
        try {
            val regular =
                Typeface.createFromAsset(am, context.resources.getString(R.string.fontName))
            replaceFont(regular)
        } catch (e: Exception) {

        }

    }

    fun getDisplaySize(activity: Activity): Double? {
        var x = 0.0
        var y = 1.0
        val mWidthPixels: Int
        val mHeightPixels: Int
        try {
            val windowManager: WindowManager = activity.windowManager
            val display: Display = windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            val realSize = Point()
            Display::class.java.getMethod("getRealSize", Point::class.java)
                .invoke(display, realSize)
            mWidthPixels = realSize.x
            mHeightPixels = realSize.y
            val dm = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(dm)
            x = (mWidthPixels.toDouble() / dm.xdpi).pow(2.1)
            y = (mHeightPixels.toDouble() / dm.ydpi).pow(2.1)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return sqrt(x + y)
    }

    private fun replaceFont(newTypeface: Typeface) {
        try {
            val staticField = Typeface::class.java.getDeclaredField("SERIF")
            staticField.isAccessible = true
            staticField.set(null, newTypeface)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("DefaultLocale")
    fun otpFind(body: String, address: String): String {
        var otp = ""

        var bodyTemp = body.lowercase(Locale.getDefault())

        val b = (bodyTemp.contains("otp") || bodyTemp.contains("one time password")
                || bodyTemp.contains("code") || bodyTemp.contains("pin") || bodyTemp.startsWith("<#> "))

        if (b && bodyTemp.length > 15) {
            try {
                val p12 = Pattern.compile("(|^)\\d{12}")
                val m12 = p12.matcher(bodyTemp)
                while (m12.find()) {
                    otp = m12.group()
                }
                bodyTemp = bodyTemp.replace(otp, "")
                otp = ""

                val p11 = Pattern.compile("(|^)\\d{11}")
                val m11 = p11.matcher(bodyTemp)
                while (m11.find()) {
                    otp = m11.group()
                }
                bodyTemp = bodyTemp.replace(otp, "")
                otp = ""

                val p10 = Pattern.compile("(|^)\\d{10}")
                val m10 = p10.matcher(bodyTemp)
                while (m10.find()) {
                    otp = m10.group()
                }
                bodyTemp = bodyTemp.replace(otp, "")
                otp = ""

                val p9 = Pattern.compile("(|^)\\d{9}")
                val m9 = p9.matcher(bodyTemp)
                while (m9.find()) {
                    otp = m9.group()
                }
                bodyTemp = bodyTemp.replace(otp, "")
                otp = ""

                if (otp == "") {
                    val pattern = Pattern.compile("(|^)\\d{8}")
                    val matcher = pattern.matcher(bodyTemp)
                    while (matcher.find()) {
                        otp = matcher.group()
                    }
                }

                if (otp == "") {
                    val pattern = Pattern.compile("(|^)\\d{7}")
                    val matcher = pattern.matcher(bodyTemp)
                    while (matcher.find()) {
                        otp = matcher.group()
                    }
                }

                if (otp == "") {
                    val pattern = Pattern.compile("(|^)\\d{6}")
                    val matcher = pattern.matcher(bodyTemp)
                    while (matcher.find()) {
                        otp = matcher.group()
                    }
                }

                if (otp == "") {
                    val pattern = Pattern.compile("(|^)\\d{5}")
                    val matcher = pattern.matcher(bodyTemp)
                    while (matcher.find()) {
                        otp = matcher.group()
                    }
                }

                if (otp == "") {
                    val pattern = Pattern.compile("(|^)\\d{4}")
                    val matcher = pattern.matcher(bodyTemp)
                    while (matcher.find()) {
                        otp = matcher.group()
                    }
                }
            } catch (ex: java.lang.Exception) {
                ex.printStackTrace()
            }
            val b1 =
                (otp.length == 4 || otp.length == 5 || otp.length == 6 || otp.length == 7 || otp.length == 8)
            if (!b1) {
                otp = ""
            }
        }

        return otp
    }

    fun setOverflowButtonColor(activity: Activity, colorFilter: PorterDuffColorFilter) {
        val overflowDescription = activity.getString(com.google.android.material.R.string.abc_action_menu_overflow_description)
        val decorView = activity.window.decorView as ViewGroup
        val viewTreeObserver = decorView.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener {
            val outViews = ArrayList<View>()
            decorView.findViewsWithText(
                outViews,
                overflowDescription,
                View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION
            )
            if (outViews.isEmpty()) {
                return@OnGlobalLayoutListener
            }
            val overflow = outViews[0] as ImageView
            overflow.colorFilter = colorFilter
        })
    }
}