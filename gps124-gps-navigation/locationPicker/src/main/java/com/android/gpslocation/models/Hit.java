package com.android.gpslocation.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class Hit {

@SerializedName("osm_id")
@Expose
private Long osmId;
@SerializedName("osm_type")
@Expose
private String osmType;
@SerializedName("country")
@Expose
private String country;
@SerializedName("osm_key")
@Expose
private String osmKey;
@SerializedName("city")
@Expose
private String city;
@SerializedName("osm_value")
@Expose
private String osmValue;
@SerializedName("postcode")
@Expose
private String postcode;
@SerializedName("name")
@Expose
private String name;
@SerializedName("point")
@Expose
private Point point;
@SerializedName("extent")
@Expose
private List<Double> extent = null;
@SerializedName("street")
@Expose
private String street;
@SerializedName("housenumber")
@Expose
private String housenumber;

public Long getOsmId() {
return osmId;
}

public void setOsmId(Long osmId) {
this.osmId = osmId;
}

public String getOsmType() {
return osmType;
}

public void setOsmType(String osmType) {
this.osmType = osmType;
}

public String getCountry() {
return country;
}

public void setCountry(String country) {
this.country = country;
}

public String getOsmKey() {
return osmKey;
}

public void setOsmKey(String osmKey) {
this.osmKey = osmKey;
}

public String getCity() {
return city;
}

public void setCity(String city) {
this.city = city;
}

public String getOsmValue() {
return osmValue;
}

public void setOsmValue(String osmValue) {
this.osmValue = osmValue;
}

public String getPostcode() {
return postcode;
}

public void setPostcode(String postcode) {
this.postcode = postcode;
}

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

public Point getPoint() {
return point;
}

public void setPoint(Point point) {
this.point = point;
}

public List<Double> getExtent() {
return extent;
}

public void setExtent(List<Double> extent) {
this.extent = extent;
}

public String getStreet() {
return street;
}

public void setStreet(String street) {
this.street = street;
}

public String getHousenumber() {
return housenumber;
}

public void setHousenumber(String housenumber) {
this.housenumber = housenumber;
}

}