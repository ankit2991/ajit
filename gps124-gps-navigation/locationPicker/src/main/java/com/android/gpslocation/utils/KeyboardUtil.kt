package com.android.gpslocation.utils

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import java.util.*


object KeyboardUtil {

    fun openKeyBoad(activity: Activity?,view:View) {
        if (activity==null)return
        if (activity.isFinishing) return
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
    fun hideKeyBoad(activity: Activity?,view:View) {
        if (activity==null)return
        if (activity.isFinishing) return
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
