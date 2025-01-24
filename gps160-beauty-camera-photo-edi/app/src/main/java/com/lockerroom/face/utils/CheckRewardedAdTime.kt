package com.lockerroom.face.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageView
import com.lockerroom.face.Constants

object CheckRewardedAdTime {

    private var sharedPreferences: SharedPreferences? = null
    private const val HOURS_24 = (24 * 60 * 60 * 1000 ).toLong()  // 24 hours in milliseconds

     fun checkAdTime(context: Context,value:String,adIcon:ImageView) {
         if (!SharePreferenceUtil.isPurchased(context)) {
             sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
             val lastClickTimestamp: Long = sharedPreferences!!.getLong(value, 0)
             val currentTimestamp = System.currentTimeMillis()
             val elapsedMillis = currentTimestamp - lastClickTimestamp
             if (elapsedMillis >= HOURS_24) {
                 // More than 24 hours have passed, show ad
                 adIcon.setVisibility(View.VISIBLE)
                 if (value.contains("last_click_timestamp_filter")) {
                     Constants.IS_SHOW_REWARDED_AD_FILTER = true
                 } else if (value.contains("last_click_timestamp_sticker")) {
                     Constants.IS_SHOW_REWARDED_AD_STICKER = true
                 } else if (value.contains("last_click_timestamp_splash")) {
                     Constants.IS_SHOW_REWARDED_AD_SPLASH = true
                 } else if (value.contains("last_click_timestamp_beauty")) {
                     Constants.IS_SHOW_REWARDED_AD_BEAUTY = true
                 }

             } else {
                 // Less than 24 hours have passed, now show ad
                 adIcon.setVisibility(View.GONE)
                 if (value.contains("last_click_timestamp_filter")) {
                     Constants.IS_SHOW_REWARDED_AD_FILTER = false
                 } else if (value.contains("last_click_timestamp_sticker")) {
                     Constants.IS_SHOW_REWARDED_AD_STICKER = false
                 } else if (value.contains("last_click_timestamp_splash")) {
                     Constants.IS_SHOW_REWARDED_AD_SPLASH = false
                 } else if (value.contains("last_click_timestamp_beauty")) {
                     Constants.IS_SHOW_REWARDED_AD_BEAUTY = false
                 }

             }
         }else{
             //premium user
             adIcon.setVisibility(View.GONE)
             if (value.contains("last_click_timestamp_filter")) {
                 Constants.IS_SHOW_REWARDED_AD_FILTER = false
             } else if (value.contains("last_click_timestamp_sticker")) {
                 Constants.IS_SHOW_REWARDED_AD_STICKER = false
             } else if (value.contains("last_click_timestamp_splash")) {
                 Constants.IS_SHOW_REWARDED_AD_SPLASH = false
             } else if (value.contains("last_click_timestamp_beauty")) {
                 Constants.IS_SHOW_REWARDED_AD_BEAUTY = false
             }
         }
     }

}

