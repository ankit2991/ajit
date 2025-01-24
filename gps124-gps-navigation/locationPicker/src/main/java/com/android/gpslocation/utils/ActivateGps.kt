package com.example.myapplication.utils

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.text.format.DateUtils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

/**
 * Created by Abc on 2/12/2019.
 */

class ActivateGps(internal var context: Context, private var isShowDialogue: Boolean, private var REQUEST_GPS_SETTINGS: Int) {

    private var activateGpsCallBack: ActivateGpsCallBack? = null

    companion object {
        const val LOCATION_INTERVAL = 6 * DateUtils.SECOND_IN_MILLIS
        const val LOCATION_FASTEST_INTERVAL = 3 * DateUtils.SECOND_IN_MILLIS
    }

    init {
        createLocationRequest()
    }

    private fun createLocationRequest() {
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = LOCATION_INTERVAL
        mLocationRequest.fastestInterval =
            LOCATION_FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener(context as Activity) {
            if (context is ActivateGpsCallBack) {
                activateGpsCallBack = context as ActivateGpsCallBack
                activateGpsCallBack?.gpsActivated(isShowDialogue)
            }
        }

        task.addOnFailureListener(context as Activity) { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(context as Activity, REQUEST_GPS_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }

            }
        }
    }

    interface ActivateGpsCallBack {
        fun gpsActivated(isShowDialogue: Boolean)
    }

}