package com.gpsnavigation.maps.gpsroutefinder.routemap.models;


import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;


import com.gpsnavigation.maps.gpsroutefinder.routemap.data.room.DistanceInfoTypeConverter;
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.room.StopListTypeConverters;

import java.util.ArrayList;

@Keep
@Entity
public class RouteModel implements Cloneable {

    @NonNull
    @PrimaryKey
    private String  routeId ;
    @ColumnInfo
    private String routeTime;
    @ColumnInfo
    private String routeName;
    @ColumnInfo
    @TypeConverters(DistanceInfoTypeConverter.class)
    private RouteDistanceInfo routeDistanceInfo;
    @ColumnInfo
    @TypeConverters(StopListTypeConverters.class)
    private ArrayList<PlaceModel>stopsList = new ArrayList<>();


    public RouteModel() {
    }

    public RouteModel(String routeId, String routeName) {
        this.routeId = routeId;
        this.routeName = routeName;
    }

    @Override
    public RouteModel clone() throws CloneNotSupportedException {
        return (RouteModel) super.clone();
    }


    public String getRouteTime() {
        return routeTime;
    }

    public void setRouteTime(String routeTime) {
        this.routeTime = routeTime;
    }

    public RouteDistanceInfo getRouteDistanceInfo() {
        return routeDistanceInfo;
    }

    public void setRouteDistanceInfo(RouteDistanceInfo routeDistance) {
        this.routeDistanceInfo = routeDistance;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }


    public ArrayList<PlaceModel> getStopsList() {
        return stopsList;
    }

    public void setStopsList(ArrayList<PlaceModel> stopsList) {
        this.stopsList = stopsList;
    }


    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String placeName) {
        this.routeName = placeName;
    }

}
