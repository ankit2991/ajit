package com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants

import androidx.annotation.IntDef
import androidx.annotation.Keep
import com.gpsnavigation.maps.gpsroutefinder.routemap.R

@Keep
val icons = arrayOf(R.drawable.ic_home_location_marker, R.drawable.ic_stop_marker, R.drawable.ic_dest_location_marker)
//0 for home
//1 for stop
//2 for dest
val iconsBits = arrayOf(0, 1, 2)
var routeIdTagFromNotification = "routeIdTagFromNotification"
const val STARTED = 1
const val REST = 2
const val STARTED_SHOW_SS_BUTTONS = 4
@Keep
@IntDef(
    STARTED,
    REST,
    STARTED_SHOW_SS_BUTTONS
)
@Retention(AnnotationRetention.SOURCE)
annotation class StopStatus



