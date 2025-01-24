package com.translate.languagetranslator.freetranslation.utils

import android.app.Activity
import android.content.Context
import android.os.Bundle


import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.appUtils.TinyDB


class AdsUtill(mContext: Context) {
    var LOG_TAG = "AD_MANAGER"
    var adRequestBundleForConsent: Bundle
    var ctx: Context? = null
    private var consentForAds = true

    companion object {
        const val DEFAULT_INTERSTITIAL = "DefaultInterstitial"
        const val DefaultBanner = "DefaultBanner"
    }

    init {

        this.ctx = mContext
//        log("Initing Mobile Ads SDK...")
        adRequestBundleForConsent = Bundle()
        adRequestBundleForConsent.putString("npa", "1")
        consentForAds = AppBase.isConsentGiven
    }









}