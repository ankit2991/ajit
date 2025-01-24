package com.code4rox.adsmanager

interface AppOpenCallback {

    fun isAdLoad(isLoad: Boolean)
    fun isAdShown(isShow: Boolean)
    fun isAdDismiss(isShow: Boolean)

}