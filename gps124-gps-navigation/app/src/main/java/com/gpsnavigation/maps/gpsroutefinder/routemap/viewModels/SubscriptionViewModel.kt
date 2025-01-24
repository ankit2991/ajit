package com.example.routesmap.viewModels

import androidx.lifecycle.ViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.repositries.DataRepositry


class SubscriptionViewModel(private var dataRepositry: DataRepositry) : ViewModel() {


    fun setUserSubscribed(isRemoved:Boolean) = dataRepositry.setUserSubscribed(isRemoved)
    fun setAutoAdsRemoved(isRemoved:Boolean) {
        dataRepositry.setAdsRemoved(isRemoved)
    }
    fun isUserSubscribed() = dataRepositry.isUserSubscribed

}