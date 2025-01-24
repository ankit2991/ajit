package com.messaging.textrasms.manager.common.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.util.resolveThemeAttribute
import com.messaging.textrasms.manager.util.resolveThemeColorStateList

class PreferenceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayoutCompat(context, attrs) {

    var title: String? = null
        set(value) {
            field = value

            if (isInEditMode) {
                findViewById<TextView>(R.id.titleView).text = value
            } else {
                findViewById<TextView>(R.id.titleView).text = value
            }
        }

    var summary: String? = null
        set(value) {
            logDebug("language" + summary + ">>>>" + value)
            field = value
            if (isInEditMode) {
                findViewById<TextView>(R.id.summaryView).run {
                    text = value
                    setVisible(value?.isNotEmpty() == true)
                }
            } else {
                findViewById<TextView>(R.id.summaryView).text = value
                findViewById<TextView>(R.id.summaryView).setVisible(value?.isNotEmpty() == true)
            }
        }

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
        }

        View.inflate(context, R.layout.preference_view, this)
        setBackgroundResource(context.resolveThemeAttribute(com.google.android.material.R.attr.selectableItemBackground))
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        // icon.imageTintList = context.resolveThemeColorStateList(android.R.attr.textColorSecondary)

        context.obtainStyledAttributes(attrs, R.styleable.PreferenceView).run {
            title = getString(R.styleable.PreferenceView_title)
            summary = getString(R.styleable.PreferenceView_summary)

            // If there's a custom view used for the preference's widget, inflate it
            getResourceId(R.styleable.PreferenceView_widget, -1).takeIf { it != -1 }?.let { id ->
                View.inflate(context, id, findViewById(R.id.widgetFrame))
            }

            // If an icon is being used, set up the icon view
            getResourceId(R.styleable.PreferenceView_icon, -1).takeIf { it != -1 }?.let { id ->
                findViewById<ImageView>(R.id.icon).setVisible(true)
                findViewById<ImageView>(R.id.icon).setImageResource(id)
                //  icon.setTint(Color.parseColor("#13CD36"))
            }

            recycle()
        }
    }

    fun settint() {
        findViewById<ImageView>(R.id.icon).imageTintList =
            context.resolveThemeColorStateList(android.R.attr.textColorSecondary)
    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

}