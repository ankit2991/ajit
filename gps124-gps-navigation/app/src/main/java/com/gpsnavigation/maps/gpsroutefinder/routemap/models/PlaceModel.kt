package com.gpsnavigation.maps.gpsroutefinder.routemap.models


import androidx.annotation.Keep
import com.akexorcist.googledirection.model.Leg
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.RFMainActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.REST
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.StopStatus

@Keep
class PlaceModel : Comparable<PlaceModel> {

    var placeLatLng: LatLng? = null
    @RFMainActivity.Companion.PlaceType
    var placeType: Int? = null
    var stopIcon: Int? = null
    var placeName: String? = null
    var placeAddress: String? = null

    var stopStayTime = 1
    var stopDuration: String? = null
    var isASAP = false
    var stopStatus = REST
    var isCompletedStop = false
    var userSelectedArrivalTime: String? = null
    var stopNotes:String?=null
    var leg: Leg? = null

    constructor() {

    }

    constructor(
        stopIcon: Int,
        name: String?,
        address: String?, @RFMainActivity.Companion.PlaceType placeType: Int,
        placeLatLng: LatLng,
        @StopStatus isStartDirection: Int
    ) {
        this.stopIcon = stopIcon
        this.placeName = name
        this.placeAddress = address
        this.stopStatus = isStartDirection
        this.placeType = placeType
        this.placeLatLng = placeLatLng
    }


    override fun compareTo(o: PlaceModel): Int {
        if (o.placeType == RFMainActivity.HOME || o.placeType == RFMainActivity.DEST)
            return 0
        else if (this.isASAP && !o.isASAP) {
            return -1
        } else if (!this.isASAP && o.isASAP) {
            return +1
        } else return 0
    }


}
