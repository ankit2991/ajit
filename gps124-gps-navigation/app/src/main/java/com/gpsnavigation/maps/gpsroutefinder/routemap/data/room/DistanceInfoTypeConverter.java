package com.gpsnavigation.maps.gpsroutefinder.routemap.data.room;

import androidx.annotation.Keep;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteDistanceInfo;

import java.lang.reflect.Type;

@Keep
public class DistanceInfoTypeConverter {

    private static Gson gson = new Gson();

    @TypeConverter
    public static RouteDistanceInfo stringToSomeObjectList(String data) {
        Type listType = new TypeToken<RouteDistanceInfo>() {
        }.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String someObjectListToString(RouteDistanceInfo someObjects) {
        return gson.toJson(someObjects);
    }
}