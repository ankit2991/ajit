package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.akexorcist.googledirection.model.Leg
import com.akexorcist.googledirection.model.Route
import com.akexorcist.googledirection.util.DirectionConverter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.gpsnavigation.maps.gpsroutefinder.routemap.GEO_GUESSING_RESTART
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.icons
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityDisplayResultMapBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.*
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.DisplayGeoGuessingResultViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import kotlin.coroutines.CoroutineContext


class DisplayGeoGuessingResultActivity : BaseActivity(), CoroutineScope, OnMapReadyCallback,
    View.OnClickListener {

    val viewModel: DisplayGeoGuessingResultViewModel by viewModel()

    var googleMap: GoogleMap? = null
    var mapFragment: SupportMapFragment? = null

    lateinit var binding: ActivityDisplayResultMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayResultMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()
        initViews()
        collectUiState()

        try {
            if (googleMap == null) {
                mapFragment =
                    (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
                mapFragment?.getMapAsync(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initViews() {
        binding.btnGuessing.setOnClickListener(this)

        binding.toolbar.title.text = getString(R.string.geo_game_activity_title)
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium

        if (!TinyDB.getInstance(this).isPremium) {
            binding.adsContainer.isVisible = true
            MaxAdManager.showBannerAds(this, binding.adsContainer)
        }
    }

    private fun collectUiState() {

        lifecycleScope.launch {
            viewModel.uiState
                .flowWithLifecycle(lifecycle)
                .distinctUntilChanged()
                .collect { uiState ->

                    with(uiState) {
                        showProgressBar(isLoading)
                        binding.infoLayout.isVisible = !isLoading

                        if (!isLoading) {
                            if (route != null) {
                                drawRouteOnMap(route)
                            } else {
                                drawLineRote()
                            }

                            binding.distance.text = String.format("%.2f", distance) + " kms"
                        }
                    }
                }
        }
    }

    private fun drawLineRote() {
        googleMap?.apply {
            addMarker(
                MarkerOptions().position(
                    LatLng(
                        LocationHelper.selectLatLng.latitude,
                        LocationHelper.selectLatLng.longitude
                    )
                ).icon(
                    bitmapDescriptorFromVector(
                        this@DisplayGeoGuessingResultActivity, icons[2]
                    )
                )
            )
            addMarker(
                MarkerOptions().position(
                    LatLng(
                        LocationHelper.guessLatLng.latitude,
                        LocationHelper.guessLatLng.longitude
                    )
                ).icon(
                    bitmapDescriptorFromVector(
                        this@DisplayGeoGuessingResultActivity, icons[0]
                    )
                )
            )
            addPolyline(
                PolylineOptions().add(
                    LatLng(
                        LocationHelper.guessLatLng.latitude,
                        LocationHelper.guessLatLng.longitude
                    ), LatLng(
                        LocationHelper.selectLatLng.latitude,
                        LocationHelper.selectLatLng.longitude
                    )
                ).width // below line is use to specify the width of poly line.
                    (5f) // below line is use to add color to our poly line.
                    .color(Color.RED) // below line is to make our poly line geodesic.
                    .geodesic(true)
            )

            val lats = listOf(LocationHelper.guessLatLng.latitude, LocationHelper.selectLatLng.latitude)
            val longs = mutableListOf(LocationHelper.guessLatLng.longitude, LocationHelper.selectLatLng.longitude)

            setMapBounds(
                mapWidth = binding.mapLayout.width,
                mapHeight = binding.mapLayout.height,
                lastLocation = null,
                bounds = getBounds(lats, longs)
            )
        }
    }
    private fun showProgressBar(visible: Boolean) {
        binding.progressBar.isVisible = visible
    }

    private fun drawRouteOnMap(route: Route) {
        googleMap?.clear()

        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                route.legList[0].startLocation.coordination,
                5F
            )
        )

        var startedRouteIndex: Int? = null
        try {
            if (route.legList.size > 0) {
                googleMap?.addMarker(
                    MarkerOptions().position(route.legList.get(0).endLocation.coordination)
                        .icon(
                            bitmapDescriptorFromVector(
                                this,
                                icons[2]
                            )
                        )
                )
                googleMap?.addMarker(
                    MarkerOptions().position(route.legList.get(0).startLocation.coordination)
                        .icon(
                            bitmapDescriptorFromVector(
                                this,
                                icons[0]
                            )
                        )
                )

                drawPolyLineOnMap(
                    route.legList[0],
                    Color.GRAY
                )
            } else {
                googleMap?.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            route.legList[0].endLocation.latitude,
                            route.legList[0].endLocation.longitude
                        )
                    ).icon(
                        bitmapDescriptorFromVector(
                            this,
                            icons[2]
                        )
                    )
                )

            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        drawPolyLineOnMap(
            route.legList.get(0),
            Color.BLUE
        )
    }

    private fun drawPolyLineOnMap(leg: Leg?, color: Int) {
        leg?.let {
            DirectionConverter.createPolyline(this, leg.directionPoint, 5, color)
                .geodesic(true)
        }?.let {
            googleMap?.addPolyline(
                it
            )
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        val key = getString(R.string.google_maps_key_part1) +
                getString(R.string.google_maps_key_part2) +
                getString(R.string.google_maps_key_part3) +
                getString(R.string.google_maps_key_part4)
        viewModel.calculateDistance(key, LocationHelper.guessLatLng, LocationHelper.selectLatLng)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.premiumButton.isInvisible =
            if (TinyDB.getInstance(this).isPremium) {
                binding.adsContainer.removeAllViews()
                binding.adsContainer.isVisible = false
                binding.adsContainer.tag = null
                (binding.btnGuessing.layoutParams as LinearLayoutCompat.LayoutParams).apply {
                    bottomMargin = 26.dpToPx
                }
                true
            } else {
                binding.adsContainer.isVisible = true
                MaxAdManager.showBannerAds(this, binding.adsContainer)
                (binding.btnGuessing.layoutParams as LinearLayoutCompat.LayoutParams).apply {
                    bottomMargin = 16.dpToPx
                }
                false
            }
    }

    @Suppress("DEPRECATION")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.navigation_button -> onBackPressed()
            R.id.premium_button -> openSubscriptionPaywall()
            R.id.btnGuessing -> {
                /* Adjust Event */
                ////////////////////////////////////////////////////////////////////////////////////
                val adjustEvent = AdjustEvent("9p5ef1")
                adjustEvent.addCallbackParameter(
                    GEO_GUESSING_RESTART,
                    " Date_${CurrentDateUtil.getCurrentDate2()}_PlacementScreen_GeoGuesserResultScreen"
                )
                adjustEvent.setCallbackId(GEO_GUESSING_RESTART);
                Adjust.trackEvent(adjustEvent)
                Log.e(
                    "AdjustEvent",
                    "The event id:9p5ef1  name:$GEO_GUESSING_RESTART Date:${CurrentDateUtil.getCurrentDate2()} PlacementScreen:GeoGuesserResultScreen has been sent to Adjust"
                )
                ////////////////////////////////////////////////////////////////////////////////////
                //firebase event
                FirebaseAnalytics.getInstance(this).logEvent(GEO_GUESSING_RESTART, null)
//            Log.e("comet", "GEO_GUESSING_RESTART->");
                /////////////

                LocationHelper.clear()


                startActivity(
                    Intent(
                        Intent(
                            this@DisplayGeoGuessingResultActivity,
                            MainActivity::class.java
                        )
                    ).addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                )
                finish()
            }
        }
    }

    private fun setInset() {
        binding.guidelineBottom.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = systemBarsInsets(insets)

            binding.guidelineBottom.setGuidelineEnd(systemBarsInsets.bottom)

            insets
        }
    }

}

