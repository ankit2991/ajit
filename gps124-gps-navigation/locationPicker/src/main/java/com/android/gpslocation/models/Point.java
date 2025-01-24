package com.android.gpslocation.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class Point {

@SerializedName("lng")
@Expose
private Double lng;
@SerializedName("lat")
@Expose
private Double lat;

public Double getLng() {
return lng;
}

public void setLng(Double lng) {
this.lng = lng;
}

public Double getLat() {
return lat;
}

public void setLat(Double lat) {
this.lat = lat;
}

}