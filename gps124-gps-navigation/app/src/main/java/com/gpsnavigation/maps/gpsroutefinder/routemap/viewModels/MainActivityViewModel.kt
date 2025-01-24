package com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.repositries.DataRepositry
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel
import java.io.File
import java.util.*

class MainActivityViewModel(private var dataRepositry: DataRepositry) : ViewModel() {

    val refreshRouteList = MutableLiveData<Boolean>()
    val onRouteClickFormRouteList = MutableLiveData<RouteModel>()
    val onDeleteRoute = MutableLiveData<Boolean>()

    fun setRefreshRoute(isRefresh:Boolean){
        refreshRouteList.value=isRefresh
    }

    fun getMapTypeChangeObserver() = dataRepositry.onMapTypeChange

    fun getOnRouteEditObserver() = dataRepositry.onRouteEdited

    fun getDistanceUnitChangeObserver() = dataRepositry.onDistanceUnitChange

    fun getTrafficVisibilityChangeObserver() = dataRepositry.onMapTrafficVisibilityChange

    fun isShowSubscriptionDialogOnStartBtn()  = dataRepositry.isShowSubscriptionDialogOnStartBtn
    fun isShowSubscriptionDialogOnStartBtnAfterTen()  = dataRepositry.isShowSubscriptionDialogOnStartBtnAfterTen
    fun isShowSubscriptionDialogOnOptimizeBtnAfterTen()  = dataRepositry.isShowSubscriptionDialogOnOptimizeBtnAfterTen

    fun isRouteFinishInterEnable()  = dataRepositry.route_finish_interstitial
    fun isDrawerSubscriptionBtnEnable()  = dataRepositry.showSubscriptionBtnInDrawer

    fun getSelectedRoute() = dataRepositry.selectedRoute

    val shakeAddNewRouteButton = MutableLiveData<Boolean>()

    fun isUserSubscribed() = dataRepositry.isUserSubscribed

    fun isAdsRemoved() = dataRepositry.isAdsRemoved

    fun subscription_screen_interval_on_rf_button() = dataRepositry.subscription_screen_interval_on_rf_button
    fun showRemoveAdsPopupInterval() = dataRepositry.showRemoveAdsPopupInterval

    fun setUserSubscribed(isSubscribed:Boolean) {
        dataRepositry.setUserSubscribed(isSubscribed)
    }

    fun setAutoAdsRemoved(isRemoved:Boolean) {
        dataRepositry.setAdsRemoved(isRemoved)
    }

    fun setOptimizeButtonVisibility(isShow:Boolean) {
       dataRepositry.setOptimizeButtonVisibility(isShow)
    }

    fun getOptimizeBtnIndicator() = dataRepositry.optimizeButtonIndicator

    fun notifyAddNewRoute() {
        shakeAddNewRouteButton.value = true
    }

    fun setSelectedRoute(route: RouteModel?) {
        dataRepositry.selectedRoute = route
    }


    fun createNewRoute(listner: (RouteModel)->Unit, routeName: String?) {
        dataRepositry.createNewRoute(listner, routeName)
    }

    fun deleteRoute(routeId: String) {
        dataRepositry.deleteRoute(routeId)
    }

    fun duplicateRoute(routeId: RouteModel?, onDuplicateDone:(RouteModel)->Unit) {
        dataRepositry.duplicateRoute(routeId, onDuplicateDone)
    }

    fun getRouteList(): HashMap<String, RouteModel> {
        return dataRepositry.getRouteList()
    }

    fun getCurrentLocation() = dataRepositry.getCurrentLocation()

    fun setCurrentLocation(latitude: Double, longitude: Double) {
        dataRepositry.setCurrentLocation(latitude, longitude)
    }


    fun updateStopList(route:RouteModel?) {
        route?.let {
            dataRepositry.updateStopList(route)
        }
    }
    fun updateRoute(
        route: RouteModel?,
        onDone: () -> Unit
    ) {
        route?.let {
            dataRepositry.updateRoute(route,onDone)
        }
    }

    fun getAllRoutesFromRoom(onRouteGet:( List<RouteModel>)->Unit){
        dataRepositry.getAllRoutesFromLocalDB(onRouteGet)
    }

    fun getRouteFromRoom(routeId: String?,onRouteGet:(RouteModel)->Unit){
        routeId?.let {
            dataRepositry.getRouteFromLocalDB(routeId,onRouteGet)
        }
    }

    fun getDefaultRouteName() = dataRepositry.getDefaultRouteName()


    fun saveDefaultSettings() {
        dataRepositry.saveDefaultSettings()
    }

    fun getSavedMapType() = dataRepositry.getSavedMapType()

    fun getSavedVehicleType() = dataRepositry.getSavedVehicleType()

    fun getSavedNavMapType() = dataRepositry.getSavedNavMapType()


    fun createFileInDataDir(activityContext: Context) =
        dataRepositry.createFileInDataDir(activityContext)

    fun generateKMLFile(fileToShare: File, routeModel: RouteModel) =
        dataRepositry.generateKMLFile(fileToShare, routeModel)

    fun getDefaultStopStayTime() = dataRepositry.getDefaultStopStayTime()

    fun setUserGaveRating() = dataRepositry.setUserGaveRating()

    fun isUserGaveRating(): Boolean {
        return dataRepositry.isUserGaveRating()
    }



}