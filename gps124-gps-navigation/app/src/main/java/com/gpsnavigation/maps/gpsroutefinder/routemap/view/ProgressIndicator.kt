package com.gpsnavigation.maps.gpsroutefinder.routemap.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.gpsnavigation.maps.gpsroutefinder.routemap.R

class ProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.progress_indicator_layout, this)
    }
}