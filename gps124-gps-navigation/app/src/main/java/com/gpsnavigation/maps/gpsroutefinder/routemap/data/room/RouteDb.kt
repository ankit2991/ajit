package com.gpsnavigation.maps.gpsroutefinder.routemap.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.room.RouteTblDao
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel

@Database(entities = [RouteModel::class], version = 1)
abstract class RouteDb : RoomDatabase() {

    abstract fun routesTblDao(): RouteTblDao?
}
