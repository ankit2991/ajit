package com.messaging.textrasms.manager.feature.compose

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.*
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import com.messaging.textrasms.manager.feature.compose.editing.DetailedChipView

class ComposeWindowCallback(
    private val localCallback: Window.Callback,
    private val activity: Activity
) : Window.Callback {
    override fun onPreparePanel(p0: Int, p1: View?, p2: Menu): Boolean {
        return localCallback.onPreparePanel(p0, p1, p2)
    }

    override fun dispatchKeyEvent(keyEvent: KeyEvent): Boolean {
        return localCallback.dispatchKeyEvent(keyEvent)
    }

    override fun dispatchKeyShortcutEvent(keyEvent: KeyEvent): Boolean {
        return localCallback.dispatchKeyShortcutEvent(keyEvent)
    }

    override fun dispatchTouchEvent(motionEvent: MotionEvent): Boolean {
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            val v = activity.currentFocus
            when (v) {
                is DetailedChipView -> {
                    val rect = Rect().apply { v.getGlobalVisibleRect(this) }
                    if (!rect.contains(motionEvent.rawX.toInt(), motionEvent.rawY.toInt())) {
                        v.hide()
                        return true
                    }
                }
            }
        }
        return localCallback.dispatchTouchEvent(motionEvent)
    }

    override fun dispatchTrackballEvent(motionEvent: MotionEvent): Boolean {
        return localCallback.dispatchTrackballEvent(motionEvent)
    }

    override fun dispatchGenericMotionEvent(motionEvent: MotionEvent): Boolean {
        return localCallback.dispatchGenericMotionEvent(motionEvent)
    }

    override fun dispatchPopulateAccessibilityEvent(accessibilityEvent: AccessibilityEvent): Boolean {
        return localCallback.dispatchPopulateAccessibilityEvent(accessibilityEvent)
    }

    override fun onCreatePanelView(i: Int): View? {
        return localCallback.onCreatePanelView(i)
    }

    override fun onCreatePanelMenu(i: Int, menu: Menu): Boolean {
        return localCallback.onCreatePanelMenu(i, menu)
    }

    override fun onMenuOpened(i: Int, menu: Menu): Boolean {
        return localCallback.onMenuOpened(i, menu)
    }

    override fun onMenuItemSelected(i: Int, menuItem: MenuItem): Boolean {
        return localCallback.onMenuItemSelected(i, menuItem)
    }

    override fun onWindowAttributesChanged(layoutParams: WindowManager.LayoutParams) {
        localCallback.onWindowAttributesChanged(layoutParams)
    }

    override fun onContentChanged() {
        localCallback.onContentChanged()
    }

    override fun onWindowFocusChanged(b: Boolean) {
        localCallback.onWindowFocusChanged(b)
    }

    override fun onAttachedToWindow() {
        localCallback.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        localCallback.onDetachedFromWindow()
    }

    override fun onPanelClosed(i: Int, menu: Menu) {
        localCallback.onPanelClosed(i, menu)
    }

    override fun onSearchRequested(): Boolean {
        return localCallback.onSearchRequested()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onSearchRequested(searchEvent: SearchEvent): Boolean {
        return localCallback.onSearchRequested(searchEvent)
    }

    override fun onWindowStartingActionMode(callback: ActionMode.Callback): ActionMode? {
        return localCallback.onWindowStartingActionMode(callback)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onWindowStartingActionMode(callback: ActionMode.Callback, i: Int): ActionMode? {
        return localCallback.onWindowStartingActionMode(callback, i)
    }

    override fun onActionModeStarted(actionMode: ActionMode) {
        localCallback.onActionModeStarted(actionMode)
    }

    override fun onActionModeFinished(actionMode: ActionMode) {
        localCallback.onActionModeFinished(actionMode)
    }
}
