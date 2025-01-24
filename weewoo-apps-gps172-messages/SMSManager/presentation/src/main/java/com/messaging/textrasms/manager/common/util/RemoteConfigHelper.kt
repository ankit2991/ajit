package com.messaging.textrasms.manager.common.util

import android.os.Build
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.messaging.textrasms.manager.BuildConfig
import com.messaging.textrasms.manager.R
import timber.log.Timber

object RemoteConfigHelper {

    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    val GPS172_PRICE_PLANS: String = "GPS172_price_plans"
    val GPS172_PAYMENT_CARD_POSITION: String = "GPS172_paymentcard_onboardingposition"
    val GPS172_INTERSTITIAL_TAP: String = "GPS172_interstital_taps"
    val GPS172_RATE_APP_PROMPT: String = "GPS172_rate_app_prompt"
    var initialized = false
        private set

    fun initialize() {
        val minFetchIntervalSecond = if (BuildConfig.DEBUG) 0L else 3600L

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(minFetchIntervalSecond).build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    initialized = true
                }
            }
    }

    private fun readValues() {

        remoteConfig.all.forEach { t, u ->

            Timber.e("$t - $u")
        }
    }

    fun <T> get(key: String, default: T) : T {
        val value = remoteConfig.all[key]
        return when (default) {

            is Long -> (value?.asLong() ?: default) as T
            is Double -> (value?.asDouble() ?: default) as T
            is String -> (value?.asString() ?: default) as T
            is Boolean -> (value?.asBoolean() ?: default) as T
            else -> (value.toString()) as T
        }
    }
}