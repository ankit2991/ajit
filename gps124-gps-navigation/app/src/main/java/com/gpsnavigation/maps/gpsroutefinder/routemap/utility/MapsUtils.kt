package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.content.Context
import android.content.Intent
import android.content.res.Resources.getSystem
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.akexorcist.googledirection.constant.TransportMode
import com.android.gpslocation.AddressModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.MyConstant
import java.io.IOException
import java.util.*

const val DEFAULT_ZOOM = 5.0F
const val DEFAULT_PADDING = 56
const val FULL_SCREEN_PADDING = 16
fun getBounds(lats: List<Double>, longs: List<Double>): LatLngBounds {

    Pair(lats.minOrNull() ?: -180.0, longs.minOrNull() ?: -180.0)
    Pair(lats.maxOrNull() ?: 180.0, longs.maxOrNull() ?: 180.0)

    val bounds = LatLngBounds(
        LatLng(lats.minOrNull() ?: -180.0, longs.minOrNull() ?: -180.0),  // SW bounds
        LatLng(lats.maxOrNull() ?: 180.0, longs.maxOrNull() ?: 180.0) // NE bounds
    )
    return bounds
}
fun GoogleMap.setMapBounds(
    mapWidth: Int,
    mapHeight: Int,
    lastLocation: LatLng?,
    bounds: LatLngBounds?,
    fullScreenMode: Boolean = true
): LatLngBounds? {
    if (lastLocation == null && bounds == null) return null

    val lats = mutableListOf<Double>()
    val longs = mutableListOf<Double>()

    bounds?.let {
        it.northeast.apply {
            lats.add(latitude)
            longs.add(longitude)
        }
        it.southwest.apply {
            lats.add(latitude)
            longs.add(longitude)
        }
    }

    val routeBounds = getBounds(lats, longs)

    lastLocation?.apply {
        lats.add(latitude)
        longs.add(longitude)
    }

    val mapBounds = getBounds(lats, longs)

    val cameraUpdate = if (lats.size == 1) {
        CameraUpdateFactory.newLatLngZoom(
            LatLng(lats[0], longs[0]),
            DEFAULT_ZOOM
        )
    } else {
        CameraUpdateFactory.newLatLngBounds(
            mapBounds,
            mapWidth,
            mapHeight,
            if (fullScreenMode) FULL_SCREEN_PADDING.dpToPx else DEFAULT_PADDING.dpToPx
        )
    }

    moveCamera(cameraUpdate)

    return routeBounds
}

val Int.pxToDp: Int get() = (this / getSystem().displayMetrics.density).toInt()
val Int.dpToPx: Int get() = (this * getSystem().displayMetrics.density).toInt()

suspend fun getAddressFromLatLng(context: Context?, lat: Double, lng: Double): AddressModel? {
    var addresses: List<Address>? = null
    val arrayList = ArrayList<AddressModel>()
    context?.let {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.let {
                if (it.isNotEmpty()) {
                    val addressModel = AddressModel()
                    addressModel.lat = it[0].latitude
                    addressModel.lng = it[0].longitude
                    addressModel.address = it[0].getAddressLine(0)
                    if (it[0].featureName != null)
                        addressModel.placeName = it[0].featureName
                    else if (it[0].subLocality != null)
                        addressModel.placeName = it[0].subLocality
                    else if (it[0].locality != null)
                        addressModel.placeName = it[0].locality
                    else if (it[0].subAdminArea != null)
                        addressModel.placeName = it[0].subAdminArea
                    else if (it[0].adminArea != null)
                        addressModel.placeName = it[0].adminArea
                    arrayList.add(addressModel)

                    return addressModel
                } else return null
            }
            return null
        } catch (e: IOException) {
            return null
        }
    }
    return null
}

fun getAddressListFromLocationName(
    context: Context?,
    address: String
): ArrayList<AddressModel>? {
    var addresses: List<Address>? = null
    val arrayList = ArrayList<AddressModel>()
    context?.let {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocationName(address, 5)
            addresses?.let {
                if (it.isNotEmpty()) {
                    for (address in it) {
                        val addressModel = AddressModel()
                        addressModel.lat = address.latitude
                        addressModel.lng = address.longitude
                        addressModel.address = address.getAddressLine(0)
                        if (address.featureName != null)
                            addressModel.placeName = address.featureName
                        else if (address.subLocality != null)
                            addressModel.placeName = address.subLocality
                        else if (address.locality != null)
                            addressModel.placeName = address.locality
                        else if (address.subAdminArea != null)
                            addressModel.placeName = address.subAdminArea
                        else if (address.adminArea != null)
                            addressModel.placeName = address.adminArea
                        arrayList.add(addressModel)
                    }

                    return arrayList
                } else return null
            }
        } catch (e: IOException) {
            return null
        }
    }
   return null

}


fun bitmapDescriptorFromVector(context: Context?, vectorResId: Int): BitmapDescriptor? {
    return context?.let {
        ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}

fun openWaze(
    context: Context,
    startLatitude: Double?,
    startLongitude: Double?,
    endLatitude: Double?,
    endLongitude: Double?
) {
    context.packageManager?.let {

        MyConstant.IS_APPOPEN_BG_IMPLICIT = true
        val uri =
            Uri.parse("waze://?ll=" + startLatitude + "," + startLongitude + "&ll=" + endLatitude + "," + endLongitude + "&navigate=yes")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.resolveActivity(it)?.let {
            context.startActivity(intent)
        } ?: run {
            Toast.makeText(context, "No waze App fond", Toast.LENGTH_SHORT).show()
            openGoogleMap(context, endLatitude, endLongitude, "Car")
        }
    }
}

fun openGoogleMap(context: Context, endLatitude: Double?, endLongitude: Double?, mode: String) {
    var travelingMode = "d"
    if (mode == TransportMode.BICYCLING)
        travelingMode = "l"
    context.packageManager?.let {
        MyConstant.IS_APPOPEN_BG_IMPLICIT = true
        val uri =
            Uri.parse("google.navigation:q=" + endLatitude + "," + endLongitude + "&dirflg=" + travelingMode)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.setPackage("com.google.android.apps.maps")
        intent.resolveActivity(it)?.let {
            context.startActivity(intent)
        } ?: run {
            Toast.makeText(context, "No Google Map App fond", Toast.LENGTH_SHORT).show()
        }
    }
}