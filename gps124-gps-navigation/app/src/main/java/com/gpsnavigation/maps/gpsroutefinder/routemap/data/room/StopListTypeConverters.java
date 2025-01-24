package com.gpsnavigation.maps.gpsroutefinder.routemap.data.room;

import androidx.annotation.Keep;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.PlaceModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
@Keep
public class StopListTypeConverters {

    private static Gson gson = new Gson();

    @TypeConverter
    public static ArrayList<PlaceModel> stringToSomeObjectList(String data) {
        if (data == null) {
            return null;
        }
        Type listType = new TypeToken<List<PlaceModel>>() {
        }.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String someObjectListToString(ArrayList<PlaceModel> someObjects) {
        return gson.toJson(someObjects);
    }
}