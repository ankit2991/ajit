package com.messaging.textrasms.manager.common.util.extensions

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView


var ViewGroup.animateLayoutChanges: Boolean
    get() = layoutTransition != null
    set(value) {
        layoutTransition = if (value) LayoutTransition() else null
    }

fun EditText.showKeyboard() {
    requestFocus()
    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun EditText.showKeyboardnumber() {
    requestFocus()
    inputType = InputType.TYPE_CLASS_PHONE
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun EditText.hideKeyboard() {
    requestFocus()

    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun ImageView.setTint(color: Int) {
    imageTintList = ColorStateList.valueOf(color)
}

fun ProgressBar.setTint(color: Int) {
    indeterminateTintList = ColorStateList.valueOf(color)
    progressTintList = ColorStateList.valueOf(color)
}

fun View.setBackgroundTint(color: Int) {

    // API 21 doesn't support this
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
        background?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    backgroundTintList = ColorStateList.valueOf(color)
}

fun View.setPadding(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    setPadding(
        left ?: paddingLeft, top ?: paddingTop, right ?: paddingRight, bottom
            ?: paddingBottom
    )
}

fun View.setVisible(visible: Boolean, invisible: Int = View.GONE) {
    visibility = if (visible) View.VISIBLE else invisible
}

/**
 * If a view captures clicks at all, then the parent won't ever receive touch events. This is a
 * problem when we're trying to capture link clicks, but tapping or long pressing other areas of
 * the view no longer work. Also problematic when we try to long press on an image in the message
 * view
 */
fun View.forwardTouches(parent: View) {
    var isLongClick = false

    setOnLongClickListener {
        isLongClick = true
        true
    }

    setOnTouchListener { v, event ->
        parent.onTouchEvent(event)

        when {
            event.action == MotionEvent.ACTION_UP && isLongClick -> {
                isLongClick = true
                true
            }

            event.action == MotionEvent.ACTION_DOWN -> {
                isLongClick = false
                v.onTouchEvent(event)
            }

            else -> v.onTouchEvent(event)
        }
    }
}

fun RecyclerView.scrapViews() {
    recycledViewPool.clear()
    adapter?.notifyDataSetChanged()
}
