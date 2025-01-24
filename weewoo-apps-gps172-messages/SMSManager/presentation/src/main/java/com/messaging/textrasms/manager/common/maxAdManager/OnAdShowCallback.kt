package com.messaging.textrasms.manager.common.maxAdManager

interface OnAdShowCallback {

    fun onAdHidden(ishow: Boolean)
    fun onAdfailed()
    fun onAdDisplay()
}