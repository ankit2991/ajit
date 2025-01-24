package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.animation.ValueAnimator.REVERSE
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.CycleInterpolator
import com.gpsnavigation.maps.gpsroutefinder.routemap.R


fun shakeView(view:View,duration:Long,cycles:Float=6f)
{
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.shake)
    animation.duration = duration
    animation.interpolator = CycleInterpolator(cycles)
    view.startAnimation(animation)
}

fun shakeView(view:View,duration:Long,repeatCount:Int,cycles:Float=6f)
{
    val animation = AnimationUtils.loadAnimation(view.context, R.anim.shake)
    animation.duration = duration
    animation.interpolator = CycleInterpolator(cycles)
    animation.repeatCount = repeatCount
    animation.fillAfter = false
    animation.repeatMode = REVERSE
    view.startAnimation(animation)
}

fun bounceView(view: View) {
    val myAnim = AnimationUtils.loadAnimation(view.context, R.anim.bounce)
    val interpolator = MyBounceInterpolator(0.2, 20.0)
    myAnim.interpolator = interpolator
    view.startAnimation(myAnim)
}