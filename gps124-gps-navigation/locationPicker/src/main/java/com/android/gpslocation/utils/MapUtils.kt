package com.android.gpslocation.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import java.util.*


object MapUtils {


    fun openNavigationMaps(context: Context, destinationLatLng: LatLng) {
        val gmmIntentUri =
            Uri.parse("google.navigation:q=" + destinationLatLng.latitude + "," + destinationLatLng.longitude + "&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(mapIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Please install Google maps first.", Toast.LENGTH_LONG).show()
        }

    }

    fun showLocationOnMap(context: Context, destinationLatLng: LatLng) {
        val gmmIntentUri =
            Uri.parse("geo:${destinationLatLng.latitude},${destinationLatLng.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(mapIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Please install Google maps first.", Toast.LENGTH_LONG).show()
        }

    }

    fun openNavigationBetweenTwoPlaces(
        context: Context,
        sourceLatLng: LatLng,
        destinationLatLng: LatLng
    ) {
        val uri = String.format(
            Locale.ENGLISH,
            "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f ",
            sourceLatLng.latitude,
            sourceLatLng.longitude,
            destinationLatLng.latitude,
            destinationLatLng.longitude
        )
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        if (appInstalledOrNot("com.google.android.apps.maps", context))
            intent.setPackage("com.google.android.apps.maps")
        context.startActivity(intent)
    }


    fun openNavigationBetweenWayPoints(
        context: Context,
        sourceLatLng: LatLng,
        latLngs: ArrayList<LatLng>,
        endlatlng: LatLng
    ) {
        var uri =
            "http://maps.google.com/maps?saddr=" + sourceLatLng.latitude + "," + sourceLatLng.longitude + "&daddr=" + endlatlng.latitude.toString() + "," + endlatlng.longitude.toString()

        for (latLng in latLngs) {
            uri += String.format(
                "+to:%s, %s",
                latLng.latitude.toString().replace(",", "."),
                latLng.longitude.toString().replace(",", ".")
            )
        }


        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        if (appInstalledOrNot("com.google.android.apps.maps", context))
            intent.setPackage("com.google.android.apps.maps")
        context.startActivity(intent)
    }

    fun openLocationByName(context: Context, locationName: String) {
        val gmmIntentUri = Uri.parse("http://maps.google.com/maps?q=$locationName&iwloc=A&hl=es")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        if (appInstalledOrNot("com.google.android.apps.maps", context))
            mapIntent.setPackage("com.google.android.apps.maps")
        context.startActivity(mapIntent)
    }

    fun openStreetView(context: Context, lat: Double, lng: Double): Boolean {
        val gmmIntentUri = Uri.parse("google.streetview:cbll=$lat,$lng")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        if (appInstalledOrNot("com.google.android.apps.maps", context)) {

            mapIntent.setPackage("com.google.android.apps.maps")
            context.startActivity(mapIntent)
            return true
        }

        return false
    }

    fun openPopularPlaces(context: Context, query: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=$query")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gmmIntentUri.toString()))
        intent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(context, "Please install Google maps first.", Toast.LENGTH_LONG).show()
        }

    }


    fun openCurrentLocation(context: Context) {
        val gmmIntentUri = Uri.parse("http://maps.google.com/maps?q=Current+Location&iwloc=A&hl=es")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        if (appInstalledOrNot("com.google.android.apps.maps", context))
            mapIntent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No Maps Application or Browser Found", Toast.LENGTH_SHORT)
                .show()
        }

    }

    fun isGpsEnabled(context: Context): Boolean {
        val on: Boolean

        val service = context.getSystemService(LOCATION_SERVICE) as LocationManager
        on = service.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.v("Booleanabc", "on ha$on")
        return on
    }


    fun showLocationSettingDialog(context: Activity, requestCode: Int) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle("GPS not enabled")  // GPS not found

        builder.setMessage("Want to enable?") // Want to enable?
        builder.setPositiveButton("Yes") { dialogInterface, i ->
            context.startActivityForResult(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                requestCode
            )
        }
        builder.setNegativeButton("No", null)
        val alertDialog = builder.create()
        alertDialog.show()
        (alertDialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(Color.BLACK)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)


    }


    fun appInstalledOrNot(uri: String, context: Context): Boolean {
        val pm = context.packageManager
        try {
            val applicationInfo = pm.getApplicationInfo(uri, 0)
            return applicationInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }

    }


}
