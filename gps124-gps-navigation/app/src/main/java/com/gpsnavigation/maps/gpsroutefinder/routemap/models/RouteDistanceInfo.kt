package com.gpsnavigation.maps.gpsroutefinder.routemap.models

import androidx.annotation.Keep

@Keep
class RouteDistanceInfo {

    var text: String?=null
    var value: Double? = null

    constructor() {}

    constructor(text: String, value: Double?) {
        this.text = text
        this.value = value
    }
}
