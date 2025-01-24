package com.gps.maps.navigation.routeplanner.viewModels

import androidx.lifecycle.ViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.repositries.DataRepositry
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel
import java.util.*

class EditRouteActivityViewModel(val dataRepositry: DataRepositry): ViewModel() {

    fun getOnRouteEditObserver() = dataRepositry.onRouteEdited

    fun getRouteList(): HashMap<String, RouteModel> {
        return dataRepositry.getRouteList()
    }

    fun getSelectedRoute() = dataRepositry.selectedRoute

    fun updateStopList(route:RouteModel?) {
        route?.let {
            dataRepositry.updateStopList(route)
        }
    }

    fun getDefaultRouteName() = dataRepositry.getDefaultRouteName()

    fun updateRoute(route: RouteModel?,onDone: () -> Unit) = dataRepositry.updateRoute(route, onDone)


    fun setOptimizeButtonVisibility(isShow:Boolean) {
        dataRepositry.setOptimizeButtonVisibility(isShow)
    }

    fun getDefaultStopStayTime() = dataRepositry.getDefaultStopStayTime()

}