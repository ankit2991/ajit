package com.messaging.textrasms.manager.common.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.extensions.withAlpha
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.resolveThemeColor
import javax.inject.Inject

class QkSwitch @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SwitchCompat(context, attrs) {

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var prefs: Preferences

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode) {
            val states = arrayOf(
                intArrayOf(-android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            )

            thumbTintList = ColorStateList(
                states, intArrayOf(
                    context.resolveThemeColor(R.attr.switchThumbDisabled),
                    colors.theme().theme,
                    context.resolveThemeColor(R.attr.switchThumbEnabled)
                )
            )

            trackTintList = ColorStateList(
                states, intArrayOf(
                    context.resolveThemeColor(R.attr.switchTrackDisabled),
                    colors.theme().theme.withAlpha(0x4D),
                    context.resolveThemeColor(R.attr.switchTrackEnabled)
                )
            )
        }
    }
}