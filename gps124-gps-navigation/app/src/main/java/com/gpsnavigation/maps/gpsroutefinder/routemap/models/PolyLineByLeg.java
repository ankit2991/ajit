package com.gpsnavigation.maps.gpsroutefinder.routemap.models;


import androidx.annotation.Keep;

import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
@Keep
public class PolyLineByLeg {
    ArrayList<Polyline> polylinesList = new ArrayList<>();

    public ArrayList<Polyline> getPolylinesList() {
        return polylinesList;
    }

    public void setPolylinesList(ArrayList<Polyline> polylinesList) {
        this.polylinesList = polylinesList;
    }
}
