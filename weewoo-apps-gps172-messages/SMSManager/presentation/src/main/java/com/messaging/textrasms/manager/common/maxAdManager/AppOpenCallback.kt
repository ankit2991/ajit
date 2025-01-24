package com.messaging.textrasms.manager.common.maxAdManager

interface AppOpenCallback {

    fun isAdLoad(isLoad: Boolean)
    fun isAdShown(isShow: Boolean)
    fun isAdDismiss(isShow: Boolean)

}