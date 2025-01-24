package com.android.gpslocation.utils


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.format.DateUtils
import androidx.appcompat.app.AlertDialog
import com.android.gpslocation.AddressModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import timber.log.Timber
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList


class ApiClientHelper(var ctx: Activity, locationCallBack: LocationCallback) : GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    companion object {
        private const val INTERVAL = 2 * DateUtils.MINUTE_IN_MILLIS
        private const val FASTEST_INTERVAL = 10 * DateUtils.SECOND_IN_MILLIS


        fun getAddress(context: Context, lat: Double, lng: Double): ArrayList<String>? {
            var addresses: List<Address>? = null
            val arrayList = ArrayList<String>()
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                addresses = geocoder.getFromLocation(lat, lng, 1)
                if (addresses != null && addresses.size > 0) {
                    if (addresses.get(0).subLocality != null) {
                        arrayList.add(addresses.get(0).subLocality)
                    } else if (addresses.get(0).subAdminArea != null) {
                        arrayList.add(addresses.get(0).subAdminArea)
                    } else {
                        if (addresses.get(0).adminArea != null && addresses.get(0).adminArea!!.contentEquals("")) {
                            arrayList.add("No Address Name Found")
                        } else {
                            arrayList.add(addresses.get(0).adminArea)
                        }

                    }
                } else {
                    arrayList.add("No Address Name Found ")
                }

                if (addresses != null && addresses.size > 0 && addresses!!.get(0).getAddressLine(0) != null) {
                    if (addresses!!.get(0).getAddressLine(0)!!.contentEquals("")) {
                        arrayList.add("No Area Name Found")
                    } else {
                        arrayList.add(addresses!!.get(0).getAddressLine(0).toString())
                    }

                } else {
                    arrayList.add("No Area Name Found")
                }
            } catch (e: IOException) {
                arrayList.add("No Area Found")
                arrayList.add("No Address Name Found")
            }
            return arrayList

        }


        fun getLatLngFromAddress(context: Context, address: String): ArrayList<AddressModel>? {
            var addresses: List<Address>? = null
            val arrayList = ArrayList<AddressModel>()
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                addresses = geocoder.getFromLocationName(address, 5)
                if (addresses != null && addresses.size > 0) {
                    val addressModel = AddressModel()
                    addressModel.lat = addresses.get(0).latitude
                    addressModel.lng = addresses.get(0).longitude
                    addressModel.address = addresses.get(0).getAddressLine(0)
                    if (addresses.get(0).featureName != null)
                        addressModel.placeName = addresses.get(0).featureName
                    else if (addresses.get(0).subLocality != null)
                        addressModel.placeName = addresses.get(0).subLocality
                    else if (addresses.get(0).locality != null)
                        addressModel.placeName = addresses.get(0).locality
                    else if (addresses.get(0).subAdminArea != null)
                        addressModel.placeName = addresses.get(0).subAdminArea
                    else if (addresses.get(0).adminArea != null)
                        addressModel.placeName = addresses.get(0).adminArea
                    arrayList.add(addressModel)
                    return arrayList
                } else return null

            } catch (e: IOException) {
                return null
            }
        }


        fun isLocationEnabled(context: Context): Boolean {
            var locationMode = 0
            try {
                locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                return false
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF
        }

        fun showLocationSettingDialog(context: Activity, requestCode: Int) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("GPS not enabled")  // GPS not found
            builder.setMessage("Want to enable?") // Want to enable?
            builder.setPositiveButton("Yes") { dialogInterface, i -> context.startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), requestCode) }
            builder.setNegativeButton("No", null)
            val alertDialog = builder.create()
            alertDialog.show()
            (alertDialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)


        }


    }

    private var activity: WeakReference<Activity> = WeakReference(ctx)
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationCallBack: LocationCallback? = null

    init {
        if (activity.get() != null) {
            initApiAndRequest()
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity.get()!!)
            mLocationCallBack = locationCallBack
        }
    }


    private fun initApiAndRequest() {
        if (mLocationRequest == null) {
            Timber.d("GPS: location request initiated")
            mLocationRequest = LocationRequest()
                    .setInterval(INTERVAL)
                    .setFastestInterval(FASTEST_INTERVAL)
                    .setPriority(getLocationRequestPriority(activity.get()!!))
        }

        if (mGoogleApiClient == null) {
            Timber.d("GPS: google api client initiated")
            mGoogleApiClient = GoogleApiClient.Builder(activity.get()!!)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()
            mGoogleApiClient?.connect()
        }

    }

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        mLocationRequest?.let { mLocationCallBack?.let { it1 ->
            mFusedLocationClient?.requestLocationUpdates(it,
                it1, Looper.myLooper())
        } }
        mFusedLocationClient?.lastLocation?.addOnSuccessListener { mLocationCallBack?.onLocationResult(LocationResult.create(arrayListOf(it))) }

        val builder = LocationSettingsRequest.Builder()
        mLocationRequest?.let { builder.addLocationRequest(it) } // mLocationRequest is a Object of LocationRequest
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(activity.get()!!)
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnCompleteListener { task ->
            try {
                var response = task?.getResult(ApiException::class.java)
                Timber.e("${response?.locationSettingsStates}")
                mLocationRequest?.let { mLocationCallBack?.let { it1 ->
                    mFusedLocationClient?.requestLocationUpdates(it,
                        it1, Looper.myLooper())
                } }
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

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (connectionResult.hasResolution() && mGoogleApiClient != null) {
            mGoogleApiClient?.connect()
        } else {
            Timber.d("GPS: connection failed")
        }
    }

    fun disconnectApiClient() {
        Timber.d("GPS: destroy")
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
            mLocationCallBack?.let { mFusedLocationClient?.removeLocationUpdates(it) }
        }

    }

    private fun getLocationRequestPriority(context: Context): Int {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationRequest.PRIORITY_HIGH_ACCURACY
        } else {
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }


}