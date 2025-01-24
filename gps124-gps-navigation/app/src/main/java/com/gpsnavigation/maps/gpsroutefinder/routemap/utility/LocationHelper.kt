package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import com.google.android.gms.maps.model.LatLng


object LocationHelper {
    var guessLatLng = LatLng(0.0, 0.0)
    var selectLatLng = LatLng(0.0, 0.0)

    fun clear() {
        guessLatLng = LatLng(0.0, 0.0)
        selectLatLng = LatLng(0.0, 0.0)
    }
}