package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun View.onReceiveSystemBarsInsets(action: (insets: Insets) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        ViewCompat.setOnApplyWindowInsetsListener(this, null)
        action(systemBarInsets)
        insets
    }
}


fun androidx.appcompat.widget.SearchView.hideKeyboard() {
    clearFocus()
    val im: InputMethodManager? =
        context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

    im?.hideSoftInputFromWindow(this.windowToken, 0)
}