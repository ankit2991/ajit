package com.gpsnavigation.maps.gpsroutefinder.routemap.data.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.ForegroundInfo
import com.akexorcist.googledirection.constant.TransportMode
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.repositries.DataRepositry
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.PlaceModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.RFMainActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.RFMainActivity.Companion.DEST
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.*
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Car
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Google_Maps
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Waze
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openGoogleMap
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openWaze
import org.jetbrains.anko.browse
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.lang.Exception

class NotificationService : Service() {

    var selectedRoute: RouteModel? = null
    val dataRepositry: DataRepositry by inject()
    var currentStopNumber: Int = 0
    var stop: PlaceModel? = null
    var remoteView: RemoteViews? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            if (intent.action ==  Constants.STARTFOREGROUND_ACTION) {
                selectedRoute = dataRepositry.getRouteList()[intent.getStringExtra(
                    Constants.EDIT_ROUTE_FLAG)]
                currentStopNumber = intent.getIntExtra( Constants.CURRENT_STOP_NUMBER, 0)
                showNotification(false)
            } else if (intent.action ==  Constants.STOPFOREGROUND_ACTION) {
                stopService(intent)
                //openAppLication()
            } else if (intent.action ==  Constants.DONE_ACTION) {
                moveToNextStop(intent)
            }
        }
        return START_STICKY
    }

    private fun openAppLication() {
        val mainActivityIntent = Intent(this, RFMainActivity::class.java)
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(mainActivityIntent)
    }

    private fun moveToNextStop(intent: Intent) {
        try {
            selectedRoute?.let {
                currentStopNumber += 1
                if (currentStopNumber >= it.stopsList.size) {
                    stopService(intent)
                    openAppLication()
                } else {
                    if (it.stopsList[currentStopNumber].placeType == DEST)
                        remoteView?.setTextViewText(R.id.btnDone, getString(R.string.finish))
                    it.stopsList?.forEach {
                        if (it.stopStatus == STARTED || it.stopStatus == STARTED_SHOW_SS_BUTTONS)
                            it.stopStatus = REST
                    }
                    it.stopsList[currentStopNumber-1].isCompletedStop = true
                    it.stopsList[currentStopNumber-1].stopStatus = STARTED
                    stop = it.stopsList[currentStopNumber]
                    stop?.stopStatus = STARTED_SHOW_SS_BUTTONS
                    showNotification(true)
                    openNavigationApp()
                }
                dataRepositry.updateStopList(selectedRoute)
            }
        }catch (ee:Exception)
        {
            Timber.e(ee)
        }
    }

    private fun openNavigationApp() {
        if (dataRepositry.getSavedNavMapType().equals(Google_Maps)) {
            var drivingMode = TransportMode.DRIVING
            if (dataRepositry.getSavedVehicleType().equals(Car))
                drivingMode = TransportMode.DRIVING
            else
                drivingMode = TransportMode.BICYCLING
            if (stop!!.leg == null) {
                openGoogleMap(
                    this,
                    stop!!.placeLatLng!!.latitude,
                    stop!!.placeLatLng!!.longitude,
                    drivingMode
                )
            } else {
                openGoogleMap(
                    this,
                    stop!!.leg!!.startLocation.latitude,
                    stop!!.leg!!.startLocation!!.longitude,
                    drivingMode
                )
            }

        } else if (dataRepositry.getSavedNavMapType().equals(Waze)) {
            if (stop!!.leg == null) {
                openWaze(
                    this,
                    dataRepositry.getCurrentLocation().latitude,
                    dataRepositry.getCurrentLocation().longitude,
                    stop!!.placeLatLng!!.latitude,
                    stop!!.placeLatLng!!.longitude
                )
            } else {
                openWaze(
                    this,
                    dataRepositry.getCurrentLocation().latitude,
                    dataRepositry.getCurrentLocation().longitude,
                    stop!!.leg!!.startLocation.latitude,
                    stop!!.leg!!.startLocation!!.longitude
                )
            }
        }else {
            if (stop!!.leg == null) {
                browse("geo:0,0?q=${ stop!!.placeLatLng!!.latitude},${stop!!.placeLatLng!!.longitude}")
            } else {
                browse("geo:0,0?q=${stop!!.leg!!.startLocation.latitude},${stop!!.leg!!.startLocation!!.longitude}")
            }
        }

    }


    private fun showNotification(isUpdate: Boolean) {

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mainActivityIntent = Intent(this, RFMainActivity::class.java)
        mainActivityIntent.putExtra(routeIdTagFromNotification, selectedRoute?.routeId)
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            mainActivityIntent, PendingIntent.FLAG_MUTABLE
        )

        val stopServiceIntent = Intent(this, NotificationService::class.java)
        stopServiceIntent.action =  Constants.STOPFOREGROUND_ACTION
        val stopServicePendingIntent = PendingIntent.getService(
            this, 0,
            stopServiceIntent, PendingIntent.FLAG_MUTABLE
        )

        val doneActionIntent = Intent(this, NotificationService::class.java)
        doneActionIntent.action =  Constants.DONE_ACTION
        val doneActionPendingIntent = PendingIntent.getService(
            this, 0,
            doneActionIntent, PendingIntent.FLAG_MUTABLE
        )

        val channelId = "1"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(manager, channelId)
        }
        remoteView = RemoteViews(packageName, R.layout.item_notification)
        selectedRoute?.let {
             remoteView?.setTextViewText(R.id.tvStopNumber, currentStopNumber.toString())
             if (it.stopsList.size>currentStopNumber){
                 remoteView?.setTextViewText(R.id.tvPlaceName, it.stopsList[currentStopNumber].placeName?: "")
                 remoteView?.setTextViewText(R.id.tvPlaceAddress, it.stopsList[currentStopNumber].placeAddress?: "")
             }
         }
        selectedRoute?.let {
            if (it.stopsList!=null && it.stopsList[currentStopNumber].placeType == DEST)
                remoteView?.setTextViewText(R.id.btnDone, getString(R.string.finish))
        }
        remoteView?.setOnClickPendingIntent(R.id.btnStop, stopServicePendingIntent)
        remoteView?.setOnClickPendingIntent(R.id.btnDone, doneActionPendingIntent)
        remoteView?.setOnClickPendingIntent(R.id.btnAllStops, pendingIntent)


        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setCustomContentView(remoteView)
            .setCustomBigContentView(remoteView)
            .setContentIntent(pendingIntent)
            .setCustomHeadsUpContentView(remoteView)
            .setOngoing(true)
            .setDefaults(Notification.DEFAULT_ALL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.priority = NotificationManager.IMPORTANCE_HIGH
        } else {
            notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
        }

        val notification = notificationBuilder.build()

        if (isUpdate)
            manager.notify(1, notification)
        else
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


                } else {
                    // Start your service
                    ForegroundInfo(1, notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
                }



            }else{
                startForeground(1, notification)
            }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(manager: NotificationManager, channelId: String) {
        val name = "Notification name"
        val descriptionText = "Description"
        val importance: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            importance = NotificationManager.IMPORTANCE_HIGH
        } else {
            importance = NotificationCompat.PRIORITY_HIGH
        }

        val mChannel = NotificationChannel(channelId, name, importance)
        mChannel.setShowBadge(true)
        mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        mChannel.description = descriptionText
        manager.createNotificationChannel(mChannel)
    }


    override fun onDestroy() {
        super.onDestroy()
        Timber.i("Stop Service")
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}




