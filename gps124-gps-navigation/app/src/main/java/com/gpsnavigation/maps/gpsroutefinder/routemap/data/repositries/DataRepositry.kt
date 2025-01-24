package com.gpsnavigation.maps.gpsroutefinder.routemap.data.repositries

import android.content.Context
import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.Constants.PREF_ROUTE_ID
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.room.RouteTblDao
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.LatLng
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.RemoteAdValues
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.RemoteConfigUtil
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

@Keep
class DataRepositry(
    private var applicationContext: Context,
    private var firebaseRemoteConfig: FirebaseRemoteConfig,
    private var routeTblDao: RouteTblDao,
    private var tinyDB: TinyDB,
    private var routesHashMap: HashMap<String, RouteModel>,
    private var currentLocation: LatLng
) {


    init {
        getAllRemoteValues()
    }

    val showSubscriptionBtnInDrawer = MutableLiveData<Boolean>()
    val splash_close_interstitial = MutableLiveData<Boolean>()
    val route_finish_interstitial = MutableLiveData<Boolean>()
    val subscription_screen_interval_on_rf_button = MutableLiveData<Int>()
    val showRemoveAdsPopupInterval = MutableLiveData<Int>()

    val isShowSubscriptionDialogOnStartBtn = MutableLiveData<Boolean>()
    val isShowSubscriptionDialogOnStartBtnAfterTen = MutableLiveData<Boolean>()
    val isShowSubscriptionDialogOnOptimizeBtnAfterTen = MutableLiveData<Boolean>()

    val KEY_RATING = "rating"
    val KEY_SUBSCRIBED = "user_subscribed"
    val optimizeButtonIndicator = MutableLiveData<Boolean>()
    var selectedRoute: RouteModel? = null
    val onRouteEdited = MutableLiveData<Boolean>()
    val onMapTypeChange = MutableLiveData<String>()
    val onDistanceUnitChange = MutableLiveData<String>()
    val onMapTrafficVisibilityChange = MutableLiveData<Boolean>()


    private fun getAllRemoteValues() {
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener {
            val adData = firebaseRemoteConfig.getValue(RemoteConfigUtil.AD_SETTING)
            Timber.e("config server data is ${adData.asString()}")
            val allAdsSettings = Gson().fromJson(adData.asString(), RemoteAdValues::class.java)
            showSubscriptionBtnInDrawer.postValue(allAdsSettings.showSubscriptionBtnInDrawer)
            splash_close_interstitial.postValue(allAdsSettings.splash_close_interstitial)
            route_finish_interstitial.postValue(allAdsSettings.route_finish_interstitial)
            isShowSubscriptionDialogOnStartBtn.postValue(allAdsSettings.isShowSubscriptionDialogOnStartBtn)
            isShowSubscriptionDialogOnStartBtnAfterTen.postValue(allAdsSettings.isShowSubscriptionDialogOnStartBtnAfterTen)
            isShowSubscriptionDialogOnOptimizeBtnAfterTen.postValue(allAdsSettings.isShowSubscriptionDialogOnOptimizeBtnAfterTen)
            subscription_screen_interval_on_rf_button.postValue(allAdsSettings.subscription_screen_interval_on_rf_button)
            showRemoveAdsPopupInterval.postValue(allAdsSettings.showRemoveAdsPopupInterval)
        }
    }


    fun setOptimizeButtonVisibility(isShow: Boolean) {
        optimizeButtonIndicator.value = isShow
    }

    val isUserSubscribed = MutableLiveData<Boolean>()
    val isAdsRemoved = MutableLiveData<Boolean>()

    fun setAdsRemoved(isRemoved: Boolean) {
        isAdsRemoved.postValue(isRemoved)
    }

    fun setUserSubscribed(isRemoved: Boolean) {
        isUserSubscribed.postValue(isRemoved)
    }

    fun getRouteList(): HashMap<String, RouteModel> {
        return routesHashMap
    }

    fun getCurrentLocation(): LatLng {
        return currentLocation
    }

    fun setCurrentLocation(latitude: Double, longitude: Double) {
        currentLocation = LatLng(
            latitude,
            longitude
        )
    }

    fun createNewRoute(listner: (RouteModel) -> Unit, routeName: String?) {
        val lastRouteId = TinyDB.getInstance(applicationContext).getInt(PREF_ROUTE_ID)
        val newRoute = RouteModel((lastRouteId + 1).toString(), routeName)
        doAsync {
            routeTblDao.insert(newRoute)
            uiThread {
                listner(newRoute)
            }
        }
    }


    fun getDefaultRouteName(): String {
        return "My Route " + Calendar.getInstance()
            .get(Calendar.DATE) + "." + (Calendar.getInstance().get(
            Calendar.MONTH
        ) + 1) + "." + Calendar.getInstance().get(
            Calendar.YEAR
        )
    }


    fun deleteRoute(routeId: String) {
        doAsync {
            routeTblDao.deleteRouteById(routeId)
        }
    }


    fun updateStopList(route: RouteModel?) {
        route?.let {
            doAsync {
                routeTblDao.insert(route)
            }
        }
    }

    fun updateRoute(
        route: RouteModel?,
        onDone: () -> Unit
    ) {
        route?.let {
            doAsync {
                routeTblDao.insert(route)
                uiThread {
                    onDone()
                }
            }
        }
    }

    fun getAllRoutesFromLocalDB(onRouteGet: (List<RouteModel>) -> Unit) {
        doAsync {
            val list = routeTblDao.allRoutes
            uiThread {
                onRouteGet(list)
            }
        }
    }


    fun getRouteFromLocalDB(routeId: String, onRouteGet: (RouteModel) -> Unit) {
        val route = routeTblDao.getRoute(routeId)
        onRouteGet(route)
    }


    public fun createFileInDataDir(context: Context): File {
        val filePath = context.applicationInfo.dataDir + "/files/routeFileForShare.kml"
        val file = File(filePath)
        if (!file.exists())
            file.createNewFile()
        return file
    }

    public fun generateKMLFile(file: File, routeModel: RouteModel?): File? {

        if (routeModel != null && routeModel.stopsList != null && routeModel.stopsList.size > 0) {
            var xmlSerializer: XmlSerializer? = null
            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            try {
                xmlSerializer = XmlPullParserFactory.newInstance().newSerializer()
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            }

            try {
                xmlSerializer!!.setOutput(fileOutputStream, "UTF-8")
                xmlSerializer.setFeature(
                    "http://xmlpull.org/v1/doc/features.html#indent-output",
                    true
                )
                xmlSerializer.startDocument(null, null)
                xmlSerializer.startTag(null, "kml")
                xmlSerializer.startTag(null, "Document")
                xmlSerializer.startTag(null, "name")
                xmlSerializer.text("kmlFile")
                xmlSerializer.endTag(null, "name")
                xmlSerializer.startTag(null, "open")
                xmlSerializer.text("1")
                xmlSerializer.endTag(null, "open")
                xmlSerializer.startTag(null, "description")
                xmlSerializer.text("Description of kml file")
                xmlSerializer.endTag(null, "description")
                xmlSerializer.startTag(null, "Style")
                xmlSerializer.attribute(null, "id", "transGreenPoly")
                xmlSerializer.startTag(null, "LineStyle")
                xmlSerializer.startTag(null, "width")
                xmlSerializer.text("1")
                xmlSerializer.endTag(null, "width")
                xmlSerializer.startTag(null, "color")
                xmlSerializer.text("7dff0000")
                xmlSerializer.endTag(null, "color")
                xmlSerializer.startTag(null, "colorMode")
                xmlSerializer.text("random")
                xmlSerializer.endTag(null, "colorMode")
                xmlSerializer.endTag(null, "LineStyle")
                xmlSerializer.endTag(null, "Style")
                xmlSerializer.startTag(null, "Folder")
                xmlSerializer.startTag(null, "name")
                xmlSerializer.text("Google Campus")
                xmlSerializer.endTag(null, "name")
                xmlSerializer.startTag(null, "visibility")
                xmlSerializer.text("1")
                xmlSerializer.endTag(null, "visibility")
                xmlSerializer.startTag(null, "description")
                xmlSerializer.text("Your Data")
                xmlSerializer.endTag(null, "description")
                xmlSerializer.startTag(null, "Placemark")
                //polyline color
                xmlSerializer.startTag(null, "Style")
                xmlSerializer.startTag(null, "LineStyle")
                xmlSerializer.startTag(null, "color")
                xmlSerializer.text("7f00ff00")
                xmlSerializer.endTag(null, "color")
                xmlSerializer.endTag(null, "LineStyle")
                xmlSerializer.endTag(null, "Style")
                //end polyline color
                xmlSerializer.startTag(null, "name")
                xmlSerializer.text("Data")
                xmlSerializer.endTag(null, "name")
                xmlSerializer.startTag(null, "visibility")
                xmlSerializer.text("1")
                xmlSerializer.endTag(null, "visibility")
                xmlSerializer.startTag(null, "styleUrl")
                xmlSerializer.text("#transRedPoly")
                xmlSerializer.endTag(null, "styleUrl")
                xmlSerializer.startTag(null, "LineString")
                xmlSerializer.startTag(null, "extrude")
                xmlSerializer.text("1")
                xmlSerializer.endTag(null, "extrude")
                xmlSerializer.startTag(null, "altitudeMode")
                xmlSerializer.text("relativeToGround")
                xmlSerializer.endTag(null, "altitudeMode")
                xmlSerializer.startTag(null, "coordinates")
                routeModel.stopsList.forEach {
                    if (it.leg != null) {
                        it.leg!!.directionPoint.forEach {
                            xmlSerializer.text(it.longitude.toString() + "," + it.latitude + ",17 \n")

                        }
                    } else {
                        xmlSerializer.text(it.placeLatLng!!.longitude.toString() + "," + it.placeLatLng!!.latitude.toString() + ",17 \n")
                    }
                }
                xmlSerializer.endTag(null, "coordinates")
                // xmlSerializer.endTag(null, "LinearRing");
                //xmlSerializer.endTag(null, "outerBoundaryIs");
                xmlSerializer.endTag(null, "LineString")
                xmlSerializer.endTag(null, "Placemark")

                //add home marker
                xmlSerializer.startTag(null, "Placemark")
                xmlSerializer.startTag(null, "Style")
                xmlSerializer.attribute(null, "id", "homeMarker")
                xmlSerializer.startTag(null, "IconStyle")
                xmlSerializer.startTag(null, "Icon")
                xmlSerializer.startTag(null, "href")
                xmlSerializer.text("https://img.icons8.com/dusk/64/000000/order-delivered.png")
                xmlSerializer.endTag(null, "href")
                xmlSerializer.endTag(null, "Icon")
                xmlSerializer.endTag(null, "IconStyle")
                xmlSerializer.endTag(null, "Style")
                xmlSerializer.startTag(null, "name")
                xmlSerializer.text("Home")
                xmlSerializer.endTag(null, "name")
                xmlSerializer.startTag(null, "Point")
                xmlSerializer.startTag(null, "coordinates")
                xmlSerializer.text(routeModel.stopsList[0].leg!!.directionPoint[0].longitude.toString() + "," + routeModel.stopsList[0].leg!!.directionPoint[0].latitude + ",0 \n")
                xmlSerializer.endTag(null, "coordinates")
                xmlSerializer.endTag(null, "Point")
                xmlSerializer.endTag(null, "Placemark")
                //end home marker
                //
                // add destination marker
                xmlSerializer.startTag(null, "Placemark")
                xmlSerializer.startTag(null, "Style")
                xmlSerializer.attribute(null, "id", "destMarker")
                xmlSerializer.startTag(null, "IconStyle")
                xmlSerializer.startTag(null, "Icon")
                xmlSerializer.startTag(null, "href")
                xmlSerializer.text("https://img.icons8.com/plasticine/100/000000/flag.png")
                xmlSerializer.endTag(null, "href")
                xmlSerializer.endTag(null, "Icon")
                xmlSerializer.endTag(null, "IconStyle")
                xmlSerializer.endTag(null, "Style")
                xmlSerializer.startTag(null, "name")
                xmlSerializer.text("Destination")
                xmlSerializer.endTag(null, "name")
                xmlSerializer.startTag(null, "Point")
                xmlSerializer.startTag(null, "coordinates")
                xmlSerializer.text(routeModel.stopsList[routeModel.stopsList.size - 1].placeLatLng!!.longitude.toString() + "," + routeModel.stopsList[routeModel.stopsList.size - 1].placeLatLng!!.latitude.toString() + ",0 \n")
                xmlSerializer.endTag(null, "coordinates")
                xmlSerializer.endTag(null, "Point")
                xmlSerializer.endTag(null, "Placemark")
                //end home marker
                xmlSerializer.endTag(null, "Folder")
                xmlSerializer.endTag(null, "Document")
                xmlSerializer.endTag(null, "kml")
                xmlSerializer.endDocument()
                xmlSerializer.flush()
                fileOutputStream!!.close()
                return file
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }
        return null
    }


    fun saveDefaultSettings() {
        tinyDB.putString(
            applicationContext.getString(R.string.pref_vehicle_type),
            applicationContext.getString(R.string.car)
        )
        tinyDB.putString(
            applicationContext.getString(R.string.pref_distance_unit),
            applicationContext.getString(R.string.kilometers)
        )
        tinyDB.putString(
            applicationContext.getString(R.string.pref_map_type),
            applicationContext.getString(R.string.defaut)
        )
        tinyDB.putString(
            applicationContext.getString(R.string.pref_navigation_app),
            applicationContext.getString(R.string.google_maps)
        )
        tinyDB.putString(applicationContext.getString(R.string.pref_average_stop_time), "1")
        tinyDB.putBoolean(Constants.KEY_HAS_SET_DEFAULT_SETTINGS, true)
    }

    fun getSavedMapType(): String {
        return tinyDB.getString(applicationContext.getString(R.string.pref_map_type))!!
    }

    fun getSavedVehicleType(): String {
        return tinyDB.getString(applicationContext.getString(R.string.pref_vehicle_type))!!
    }

    fun getSavedNavMapType(): String {
        return tinyDB.getString(applicationContext.getString(R.string.pref_navigation_app))!!
    }


    fun getDefaultStopStayTime(): Int {
        var time = tinyDB.getString(applicationContext.getString(R.string.pref_average_stop_time))
        if (time == null || time == "")
            time = "1"
        return time.toInt()
    }

    fun setUserGaveRating() {
        tinyDB.putBoolean(KEY_RATING, true)
    }

    fun isUserGaveRating(): Boolean {
        return tinyDB.getBoolean(KEY_RATING)
    }

    fun duplicateRoute(route: RouteModel?, onDuplicateDone: (RouteModel) -> Unit) {
        route?.let{
            val lastRouteId = TinyDB.getInstance(applicationContext).getInt(PREF_ROUTE_ID)
            val newRoute = it.clone()
            val newId = (lastRouteId+1).toString()
            newRoute.routeId = newId
            TinyDB.getInstance(applicationContext).putInt(PREF_ROUTE_ID, newId.toInt())
            doAsync {
                routeTblDao.insert(newRoute)
                uiThread {
                    onDuplicateDone(newRoute)
                }
            }

        }
    }


}
