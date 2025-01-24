package com.gpsnavigation.maps.gpsroutefinder.routemap.models

import android.graphics.drawable.Drawable

/**
 * Created by anupamchugh on 11/02/17.
 */
data class MainScreenItemModel(
    var id: Long,
    var title: String,
    var icon: Int,
    var colorDrawable: Drawable,
)
