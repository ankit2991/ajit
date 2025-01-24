package com.lockerroom.face.maxAdManager

interface OnAdShowCallback {

    fun onAdHidden(ishow: Boolean)
    fun onAdfailed()
    fun onAdDisplay()
}