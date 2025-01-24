package com.gpsnavigation.maps.gpsroutefinder.routemap.uistate

import com.akexorcist.googledirection.model.Route

data class DisplayGeoGuessingResultUiState(
    val isLoading: Boolean = true,
    val route: Route? = null,
    val distance: Double? = null,
)