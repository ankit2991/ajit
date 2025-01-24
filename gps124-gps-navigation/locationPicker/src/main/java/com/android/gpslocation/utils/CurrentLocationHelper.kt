package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.os.Looper
import com.example.myapplication.utils.ActivateGps.Companion.LOCATION_FASTEST_INTERVAL
import com.example.myapplication.utils.ActivateGps.Companion.LOCATION_INTERVAL
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import timber.log.Timber
import java.lang.ref.WeakReference

class CurrentLocationHelper(ctx: Activity, isCancelOnLocationRetrieve: Boolean, var locationCallBack: (LocationResult?)->Unit) : LocationCallback() {

    private var activity: WeakReference<Activity> = WeakReference(ctx)
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var isCancelOnREsult = false


    init {
        if (activity.get() != null) {
            initApiAndRequest()
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity.get()!!)
            isCancelOnREsult = isCancelOnLocationRetrieve
            startLocationUpdate()
        }
    }


    private fun initApiAndRequest() {
        if (mLocationRequest == null) {
            Timber.d("GPS: location request initiated")
            mLocationRequest = LocationRequest()
                .setInterval(LOCATION_INTERVAL)
                .setFastestInterval(LOCATION_FASTEST_INTERVAL)
                .setPriority(getLocationRequestPriority(activity.get()!!))
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdate() {

        mLocationRequest?.let { mFusedLocationClient?.requestLocationUpdates(it, this, Looper.myLooper()) }
        mFusedLocationClient?.lastLocation
        val builder = LocationSettingsRequest.Builder()
        mLocationRequest?.let { builder.addLocationRequest(it) }
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(activity.get()!!)
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                Timber.e("${response?.locationSettingsStates}")
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvable = e as ResolvableApiException
                        resolvable.startResolutionForResult(activity.get()!!, 2001)
                    } catch (sie: Exception) {
                        // Ignore the error.
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Timber.e("LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE")
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }


    override fun onLocationAvailability(p0: LocationAvailability) {
        super.onLocationAvailability(p0)
    }

    override fun onLocationResult(result: LocationResult) {
        super.onLocationResult(result)
        locationCallBack(result)
        if (isCancelOnREsult) {
            disconnectGettingLocation()
        }
    }


    fun disconnectGettingLocation() {
        Timber.d("GPS: destroy")
        mFusedLocationClient?.removeLocationUpdates(this)
    }

    private fun getLocationRequestPriority(context: Context): Int {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationRequest.PRIORITY_HIGH_ACCURACY
        } else {
            LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }


}