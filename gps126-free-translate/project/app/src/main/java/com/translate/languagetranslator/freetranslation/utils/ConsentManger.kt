package com.translate.languagetranslator.freetranslation.utils

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.translate.languagetranslator.freetranslation.BuildConfig
import com.translate.languagetranslator.freetranslation.appUtils.TinyDB


object ConsentManger {


    lateinit var consentInformation: ConsentInformation
    lateinit var consentForm: ConsentForm
     fun lookUpForAdsConsentForm(context: Activity) {

        var params = if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(context)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("D3090E33EEFC1B23CC2984CDBC231FCC")
                .setForceTesting(true)
                .build()

            // Set tag for under age of consent. false means users are not under
            // age.

             ConsentRequestParameters.Builder()
            .setConsentDebugSettings(debugSettings)// this is only for debug mode
            .setTagForUnderAgeOfConsent(false)
            .build()
        }else {
             ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()
        }
        consentInformation = UserMessagingPlatform.getConsentInformation(context)
        consentInformation.requestConsentInfoUpdate(
            context,
            params,
            {

                // The consent information state was updated.
                // You are now ready to check if a form is available.
                if (consentInformation.isConsentFormAvailable) {
                    loadForm(context)
                }
            },
            {
                Log.e("Error Loading Consent===>", it.message)
                // Handle the error.
//                Toast.makeText(
//                    this@BaseNavControllerActivity,
//                    "Err Loding Form",
//                    Toast.LENGTH_SHORT
//                ).show()
            })
    }

    //loading consent form for ads consent
    fun loadForm(context: Activity) {
        // Loads a consent form. Must be called on the main thread.
        UserMessagingPlatform.loadConsentForm(
            context,
            {
                consentForm = it
                if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                    consentForm.show(context, ConsentForm.OnConsentFormDismissedListener {
                        if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                            // App can start requesting ads.
                            TinyDB.getInstance(context).isConsentGivenGDPR = true
                            com.code4rox.adsmanager.TinyDB.getInstance(context).isConsentGivenGDPR = true  //this is for adManager class
                        } else {
                            // Handle dismissal by reloading form.
                            loadForm(context)
                        }
                    }
                    )
                }
            },
            {
                Log.e("Error Showing Consent===>", it.message)
                // Handle the error.
//                Toast.makeText(
//                    this@BaseNavControllerActivity,
//                    "Error Showing Form",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
        )
    }



    fun presentForm(context: Activity) {
        consentForm.show(context
        ) { loadForm(context) }
    }
}