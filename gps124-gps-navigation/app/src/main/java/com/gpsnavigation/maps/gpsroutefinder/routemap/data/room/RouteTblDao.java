package com.gpsnavigation.maps.gpsroutefinder.routemap.data.room;

import androidx.annotation.Keep;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel;

import java.util.List;
@Keep
@Dao
public interface RouteTblDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RouteModel routeModel);


    @Query("select * from RouteModel")
    List<RouteModel> getAllRoutes();


    @Query("delete  from RouteModel")
    void deleteAllRoutes();

    @Query("select * from RouteModel where routeId =:id")
    RouteModel getRoute(String id);

    @Delete
    void deleteRoute(RouteModel translationTables);

    @Query("DELETE FROM RouteModel WHERE routeId = :id")
    abstract void deleteRouteById(String id);
}
