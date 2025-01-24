package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.content.Context
import android.location.Location
import android.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteDistanceInfo
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Kilometers
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


fun convertMeterToKm(mtrs: Long): Double {
    val kilometers = mtrs.toDouble() / 1000
    return unitFormat(kilometers)
}

fun convertKmToMiles(km: Double?): Double {
    km?.let {
        val miles = km * 0.62137
        return unitFormat(miles)
    }
    return 0.0
}

fun convertMilesToKm(miles: Double?): Double {
    miles?.let {
        val km = miles / 0.62137
        return unitFormat(km)
    }
    return 0.0
}

public fun unitFormat(i: Double): Double {
    var retStr: String? = null
    if (i >= 0 && i < 10)
        retStr = "0" + i
    else
        retStr = "" + i
    return retStr.toDouble()
}

fun convertMeterToMiles(mtrs: Long?): Double {
    mtrs?.let {
        val miles = mtrs.toInt() * 0.00062137
        return DecimalFormat("##.00", DecimalFormatSymbols(Locale.US)).format(miles).toDouble()
    }
    return 0.0
}

fun calculateTotallRouteDistance(context: Context?, distanceInMeter:Long): RouteDistanceInfo {
    val routeDistanceInfo= RouteDistanceInfo()
    val distance: Double
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    if (preferences.getString(context?.getString(R.string.pref_distance_unit), "") == Kilometers
    ) {
        distance = convertMeterToKm(distanceInMeter)
        routeDistanceInfo.text = distance.toString()+"km"
        routeDistanceInfo.value = distance
    } else {
        distance = convertMeterToMiles(distanceInMeter)
        routeDistanceInfo.text = distance.toString()+"Miles"
        routeDistanceInfo.value = distance
    }

    return routeDistanceInfo
}

fun getRandomLocation(point: LatLng, radius: Int): LatLng? {
    val randomPoints: MutableList<LatLng> = ArrayList()
    val randomDistances: MutableList<Float> = ArrayList()
    val myLocation = Location("")
    myLocation.latitude = point.latitude
    myLocation.longitude = point.longitude

    //This is to generate 10 random points
    for (i in 0..9) {
        val x0 = point.latitude
        val y0 = point.longitude
        val random = Random()

        // Convert radius from meters to degrees
        val radiusInDegrees = (radius / 111000f).toDouble()
        val u: Double = random.nextDouble()
        val v: Double = random.nextDouble()
        val w = radiusInDegrees * Math.sqrt(u)
        val t = 2 * Math.PI * v
        val x = w * Math.cos(t)
        val y = w * Math.sin(t)

        // Adjust the x-coordinate for the shrinking of the east-west distances
        val new_x = x / Math.cos(y0)
        val foundLatitude = new_x + x0
        val foundLongitude = y + y0
        val randomLatLng = LatLng(foundLatitude, foundLongitude)
        randomPoints.add(randomLatLng)
        val l1 = Location("")
        l1.latitude = randomLatLng.latitude
        l1.longitude = randomLatLng.longitude
        randomDistances.add(l1.distanceTo(myLocation))
    }
    //Get nearest point to the centre
    val indexOfNearestPointToCentre = randomDistances.indexOf(Collections.min(randomDistances))
    return randomPoints[indexOfNearestPointToCentre]
}
 fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val theta = lon1 - lon2
    var dist = (sin(deg2rad(lat1))
            * sin(deg2rad(lat2))
            + (cos(deg2rad(lat1))
            * cos(deg2rad(lat2))
            * cos(deg2rad(theta))))
    dist = acos(dist)
    dist = rad2deg(dist)
    dist *= 60 * 1.1515
    return dist
}

 fun deg2rad(deg: Double): Double {
    return deg * Math.PI / 180.0
}

 fun rad2deg(rad: Double): Double {
    return rad * 180.0 / Math.PI
}
