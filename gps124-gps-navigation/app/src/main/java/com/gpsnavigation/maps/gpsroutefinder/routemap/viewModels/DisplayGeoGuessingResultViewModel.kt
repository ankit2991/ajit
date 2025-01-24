package com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.google.android.gms.maps.model.LatLng
import com.gpsnavigation.maps.gpsroutefinder.routemap.uistate.DisplayGeoGuessingResultUiState
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.convertMeterToKm
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.convertMilesToKm
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.distance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DisplayGeoGuessingResultViewModel() : ViewModel() {

    private val _uiState: MutableStateFlow<DisplayGeoGuessingResultUiState> =
        MutableStateFlow(DisplayGeoGuessingResultUiState())
    val uiState: StateFlow<DisplayGeoGuessingResultUiState>
        get() = _uiState.asStateFlow()

    fun calculateDistance(serverKey: String, guessLatLng: LatLng, selectLatLng: LatLng) {
        viewModelScope.launch {

            GoogleDirection.withServerKey(
                serverKey
            ).from(
                guessLatLng
            )
//            .and(getWayPoints())
                .to(
                    selectLatLng
                ).optimizeWaypoints(true).alternativeRoute(true)
//            .optimizeWaypoints(shouldOptimize())
                .transportMode(TransportMode.DRIVING).execute(object : DirectionCallback {
                    @SuppressLint("SetTextI18n")
                    override fun onDirectionSuccess(direction: Direction?) {
                        direction?.let {
                            if (it.isOK) {
                                it.routeList.firstOrNull()?.let { route ->
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            route = route,
                                            distance = convertMeterToKm(route.totalDistance)
                                        )
                                    }
                                } ?: _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        route = null,
                                        distance = calcDistance(guessLatLng, selectLatLng)
                                    )
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        route = null,
                                        distance = calcDistance(guessLatLng, selectLatLng)
                                    )
                                }
                            }
                        }

                    }

                    override fun onDirectionFailure(t: Throwable) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                route = null,
                                distance = calcDistance(guessLatLng, selectLatLng)
                            )
                        }
                    }
                })
        }
    }

    private fun calcDistance(guessLatLng: LatLng, selectLatLng: LatLng): Double =
        convertMilesToKm(
            distance(
                guessLatLng.latitude,
                guessLatLng.longitude,
                selectLatLng.latitude,
                selectLatLng.longitude
            )
        )
}