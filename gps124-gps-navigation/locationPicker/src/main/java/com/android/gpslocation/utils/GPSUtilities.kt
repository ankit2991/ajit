package com.android.gpslocation.utils

import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat

object GPSUtilities {

    public fun isGPSEnabled(context: Context):Boolean
    {
        val locationManager =  context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

}