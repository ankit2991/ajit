package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class TimberDebugTree() : Timber.Tree(),KoinComponent {

    val firebaseAnalytics : FirebaseAnalytics by inject()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        sendFirebaseEvent(firebaseAnalytics,tag,message)
    }

    fun sendFirebaseEvent(firebaseAnalytics: FirebaseAnalytics,eventName: String?, eventDescription: String?) {
        if (eventName!=null && eventDescription!=null) {
            val bundle = Bundle()
            bundle.putString(eventName, eventDescription)
            firebaseAnalytics.logEvent(eventName, bundle)
        }
    }
}