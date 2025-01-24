package com.translate.languagetranslator.freetranslation.activities.onboarding

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonSwipeableViewPager @JvmOverloads constructor(

    context: Context,
    attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    override fun onTouchEvent(event: MotionEvent?) = false
    override fun onInterceptTouchEvent(event: MotionEvent?) = false

}