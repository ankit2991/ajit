package com.gpsnavigation.maps.gpsroutefinder.routemap.models

import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView

abstract class BaseItem {
    abstract fun itemType(): Int
    abstract fun bind(@NonNull holder: RecyclerView.ViewHolder, position: Int)
}