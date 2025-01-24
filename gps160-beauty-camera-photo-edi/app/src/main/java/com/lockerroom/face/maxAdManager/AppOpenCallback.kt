package com.lockerroom.face.maxAdManager

interface AppOpenCallback {

    fun isAdLoad(isLoad: Boolean)
    fun isAdShown(isShow: Boolean)
    fun isAdDismiss(isShow: Boolean)

}