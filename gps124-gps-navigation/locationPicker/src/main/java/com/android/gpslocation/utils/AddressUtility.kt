package com.android.gpslocation.utils


import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.android.gpslocation.AddressModel
import com.android.gpslocation.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


object AddressUtility {

    fun getAddress(context: Context?, lat: Double, lng: Double, onComplete: (ArrayList<String>?) -> Unit) {
        context?.let {
            val arrayList = ArrayList<String>()
            doAsync {
                try {
                    val geocoder = Geocoder(it, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    uiThread {
                        var placeName = ""
                        if (addresses != null && addresses.size > 0) {
                            if (addresses[0].thoroughfare != null) {
                                placeName = addresses[0].thoroughfare
                            } else {
                                if (addresses[0].subLocality != null)
                                    placeName = addresses[0].subLocality
                                else if (addresses[0].adminArea != null && addresses[0].adminArea!!.isNotEmpty()) {
                                    placeName = addresses[0].adminArea
                                }
                            }
                        } else {
                            placeName = context.getString(R.string.no_address_name_found)
                        }
                        arrayList.add(placeName)
                        if (addresses != null && addresses.size > 0 && addresses[0].getAddressLine(0) != null) {
                            if (addresses.get(0).getAddressLine(0)!!.contentEquals("")) {
                                arrayList.add(context.getString(R.string.no_address_found))
                            } else {
                                arrayList.add(addresses.get(0).getAddressLine(0).toString())
                            }
                        } else {
                            arrayList.add(context.getString(R.string.no_address_found))
                        }
                        onComplete(arrayList)
                    }
                } catch (e: IOException) {
                    arrayList.add(context.getString(R.string.no_address_name_found))
                    arrayList.add(context.getString(R.string.no_address_found))
                    onComplete(arrayList)
                }
            }
        }

    }

    fun getAddressListFromLocationName(
        context: Context,
        address: String
    ): ArrayList<AddressModel>? {
        var addresses: List<Address>? = null
        val arrayList = ArrayList<AddressModel>()
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocationName(address, 5)
            if (addresses != null && addresses.isNotEmpty()) {
                for (address in addresses) {
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

        } catch (e: IOException) {
            return null
        }
    }

}
