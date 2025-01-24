package com.lockerroom.face.utils

object MyConstant {

    var onboarding_flag = false
    var paymentcard_flag = false
    var price_plan = "subs_yearly"
    var INTERSTITIAL_TAP = 5
    var tapCount = 0

    var MAIN_MENU_INTERSTITIAL = true
    var EXIT_INTERSTITIAL = true
    var SAVING_INTERSTITIAL = true

    const val LAST_CLICK_TIMESTAMP_FILTER = "last_click_timestamp_filter"
    const val LAST_CLICK_TIMESTAMP_STICKER= "last_click_timestamp_sticker"
    const val LAST_CLICK_TIMESTAMP_SPLASH = "last_click_timestamp_splash"
    const val LAST_CLICK_TIMESTAMP_BEAUTY = "last_click_timestamp_beauty"
    fun interCriteria(): Boolean {
        return tapCount % INTERSTITIAL_TAP == 0

    }
}