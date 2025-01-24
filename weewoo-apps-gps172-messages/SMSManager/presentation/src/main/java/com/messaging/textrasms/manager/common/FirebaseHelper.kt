package com.messaging.textrasms.manager.common

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object FirebaseHelper {

    const val first_open = "first_init"
    const val open_app = "open_app"
    const val app_foreground = "app_foreground"
    const val app_background = "app_background"
    const val close_app = "close_app"
    const val splash_view = "splash_view"
    const val onboarding = "onboarding"
    const val payment_card = "payment_card"
    const val home = "home"
    const val contact = "contact"
    const val CTA_onboarding = "CTA_onboarding"
    const val CTA_subscribe = "CTA_subscribe"
    const val cancel = "cancel"
    const val ad_click = "ad_click"
    const val ad_view = "ad_view"

    private val firebaseAnalytics: FirebaseAnalytics?
        get() {

            return if (PlayServicesHelper.gpsAvailable) {

                Firebase.analytics
            } else {

                null
            }
        }

    fun sendFirstEvent() {

        sendLog(first_open, null)
    }

    fun sendOpenAppEvent() {

        sendLog(open_app, null)
    }

    fun sendForegroundEvent() {

        sendLog(app_foreground, null)
    }

    fun sendBackgroundEvent() {

        sendLog(app_background, null)
    }

    fun sendCloseEvent() {

        sendLog(close_app, null)
    }

    fun sendSplashViewEvent() {

        sendLog(splash_view, null)
    }

    fun sendOnBoardingEvent(view: String) {
        val bundle = Bundle()
        bundle.putString("view", view)
        sendLog(onboarding, bundle)
    }

    fun sendPaymentCardEvent() {

        sendLog(payment_card, null)
    }

    fun sendCTASubscribeEvent() {

        sendLog(CTA_subscribe, null)
    }

    fun sendCTACancelEvent() {

        sendLog(cancel, null)
    }

    private fun sendLog(event: String, params: Bundle?) {

        firebaseAnalytics?.logEvent(event, params)
    }
}

object PlayServicesHelper {

    var gpsAvailable: Boolean = false
        private set

    fun isGooglePlayServicesAvailable(context: Context): Boolean {

        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(context)
        gpsAvailable = status == ConnectionResult.SUCCESS
        return gpsAvailable
    }
}