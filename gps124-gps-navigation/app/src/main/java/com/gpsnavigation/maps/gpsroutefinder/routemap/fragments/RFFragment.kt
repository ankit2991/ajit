package com.gpsnavigation.maps.gpsroutefinder.routemap.fragments

//import com.google.android.gms.ads.AdListener
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.InterstitialAd
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.model.Leg
import com.akexorcist.googledirection.util.DirectionConverter
import com.android.gpslocation.utils.AddressUtility
import com.android.gpslocation.utils.MapUtils
import com.example.myapplication.utils.CurrentLocationHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.EditRouteActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.PaywallUi
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.RFMainActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.RFMainActivity.Companion.DEST
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.RFMainActivity.Companion.HOME
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.RFMainActivity.Companion.STOP
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.*
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Car
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Google_Maps
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Night
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Satellite
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Terrian
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Waze
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.default
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.services.NotificationService
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogDeleteBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogStopLimitExceededBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogStopStayTimeBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.FragmentMapBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.LayoutEditStopBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.PlaceModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.*
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.Constants.PREF_ROUTE_ID
import com.gpsnavigation.maps.gpsroutefinder.routemap.view.adapters.StopsOnMapScreenAdapter
import com.takisoft.datetimepicker.TimePickerDialog

import kotlinx.coroutines.*
import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import timber.log.Timber
import java.util.*
import kotlin.coroutines.CoroutineContext


class MapFragment : Fragment(), CoroutineScope, View.OnClickListener, OnMapReadyCallback {

    val activityViewModel: MainActivityViewModel by sharedViewModel()
    var showRemoveAdsPopupInterval = 0
    var isUserSubscribed = false

    //    var fragmentView: View? = null
    var googleMap: GoogleMap? = null
    var isMapLoaded = false
    var stopsAdapter: StopsOnMapScreenAdapter? = null
    var activityContext: Context? = null
    var selectedMApType = MapTypes.Normal
    var isFullscreenMap = false
    var mapFragment: SupportMapFragment? = null
    var isShowSubscriptionDialogOnStartBtn = false
    var isShowSubscriptionDialogOnOptimizeBtnAfterTen = false
    var isShowSubscriptionDialogOnStartBtnAfterTen = false

    //    var finishRouteInterstitial: InterstitialAd? = null
    var sheetBehavior: BottomSheetBehavior<*>? = null
    lateinit var fragmentView: FragmentMapBinding

    enum class MapTypes {
        Normal,
        Terrian,
        Sattelite,
        Night
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = FragmentMapBinding.inflate(inflater, container, false)
        return fragmentView.root
//        fragmentView = inflater.inflate(R.layout.fragment_map, container, false)
//        return fragmentView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentView.loadingProgressBar?.visibility = View.VISIBLE
        sheetBehavior = BottomSheetBehavior.from(fragmentView.bottomsheet.bottomSheetLayout)
        sheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    sheetBehavior?.setState(BottomSheetBehavior.STATE_EXPANDED)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
        fragmentView.bottomsheet.btnGotoSubscribe.setOnClickListener {
            requireActivity().openSubscriptionPaywall()
        }
        setObservers()
        try {
            if (googleMap == null) {
                mapFragment =
                    (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
                mapFragment?.getMapAsync(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        activityViewModel.getAllRoutesFromRoom {
            if (it.isEmpty()) {
                activityViewModel.createNewRoute({
                    TinyDB.getInstance(activityContext!!).putInt(PREF_ROUTE_ID, it.routeId.toInt())
                    activityViewModel.getRouteList()[it.routeId] = it
                    activityViewModel.setSelectedRoute(it)
                    val intent = Intent(activityContext, EditRouteActivity::class.java)
                    intent.putExtra(Constants.EDIT_ROUTE_FLAG, it.routeId)
                    startActivity(intent)
                    activityViewModel.setRefreshRoute(true)
                    setScreenContents(null)
                }, getDefaultRouteName())
            } else {
                if (activity != null) {
                    activityViewModel.getRouteList().clear()
                    it.forEach { route ->
                        activityViewModel.getRouteList()[route.routeId] = route
                    }
                    activityViewModel.getRouteList()
                    activityViewModel.setSelectedRoute(it.get(0))
                    setScreenContents(null)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (activityViewModel.getSelectedRoute() != null) {
            showOrHideNoStopFoundLabel()
            stopsAdapter?.setData(activityViewModel.getSelectedRoute()!!.stopsList)
            fragmentView.tvStopCount.text = getStopsCount()
            drawRouteOnMap()
            sheetBehavior?.let {
                if (it.state == BottomSheetBehavior.STATE_EXPANDED) {
                    if (isUserSubscribed)
                        it.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setObservers() {
        /*finishRouteInterstitial = AdUtility.newAdMobInterstitialAd(
            activityContext!!,
            getString(R.string.interstitial_ad_unit_id)
        )*/
        activityViewModel.showRemoveAdsPopupInterval()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                showRemoveAdsPopupInterval = it
            })
        activityViewModel.isUserSubscribed()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                isUserSubscribed = it
            })
        activityViewModel.isShowSubscriptionDialogOnStartBtn()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                it?.let {
                    isShowSubscriptionDialogOnStartBtn = it
                }
            })
        activityViewModel.isShowSubscriptionDialogOnStartBtnAfterTen()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                it?.let {
                    isShowSubscriptionDialogOnStartBtnAfterTen = it
                }
            })
        activityViewModel.isShowSubscriptionDialogOnOptimizeBtnAfterTen()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                it?.let {
                    isShowSubscriptionDialogOnOptimizeBtnAfterTen = it
                }
            })

        /*activityViewModel.getTrafficVisibilityChangeObserver()
            .observe(this, androidx.lifecycle.Observer {
                googleMap?.isTrafficEnabled = it
            })
        activityViewModel.getDistanceUnitChangeObserver().observe(this,
            androidx.lifecycle.Observer {
                it?.let {
                    if (it == Kilometers) {
                        val distance =
                            DecimalFormat("#.###", DecimalFormatSymbols(Locale.US)).format(
                                convertMilesToKm(activityViewModel.getSelectedRoute()?.routeDistanceInfo?.value)
                            )
                        activityViewModel.getSelectedRoute()?.routeDistanceInfo?.text =
                            distance + "Km."
                        activityViewModel.getSelectedRoute()?.routeDistanceInfo?.value =
                            distance.toDouble()
                        if (activityViewModel.getSelectedRoute()?.routeTime != null && activityViewModel.getSelectedRoute()?.routeDistanceInfo?.text != null)
                            fragmentView?.tvTotallTime?.text =
                                "${activityViewModel.getSelectedRoute()?.routeTime} • ${activityViewModel.getSelectedRoute()?.routeDistanceInfo?.text}"
                        activityViewModel.getSelectedRoute()?.routeId?.let {
                            activityViewModel.updateRouteDistance(
                                it,
                                activityViewModel.getSelectedRoute()?.routeDistanceInfo
                            )
                        }
                    } else {
                        val distance =
                            DecimalFormat("#.###", DecimalFormatSymbols(Locale.US)).format(
                                convertKmToMiles(activityViewModel.getSelectedRoute()?.routeDistanceInfo?.value)
                            )
                        activityViewModel.getSelectedRoute()?.routeDistanceInfo?.text =
                            distance + "Miles."
                        activityViewModel.getSelectedRoute()?.routeDistanceInfo?.value =
                            distance.toDouble()
                        if (activityViewModel.getSelectedRoute()?.routeTime != null && activityViewModel.getSelectedRoute()?.routeDistanceInfo?.text != null)
                            fragmentView?.tvTotallTime?.text =
                                "${activityViewModel.getSelectedRoute()?.routeTime} • ${activityViewModel.getSelectedRoute()?.routeDistanceInfo?.text}"
                        activityViewModel.getSelectedRoute()?.routeId?.let {
                            activityViewModel.updateRouteDistance(
                                it,
                                activityViewModel.getSelectedRoute()?.routeDistanceInfo
                            )
                        }
                    }
                }
            })
        activityViewModel.getMapTypeChangeObserver().observe(this,
            androidx.lifecycle.Observer {
                setGoogleType(it)
            })*/
        activityViewModel.onRouteClickFormRouteList.observe(viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it?.let {
                    launch(Dispatchers.Main) {
                        sheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                        activityViewModel.setSelectedRoute(activityViewModel.getRouteList()[it.routeId])
                        if (it.stopsList.isNotEmpty() && it.stopsList[0].leg == null)
                            fragmentView.btnOptimizeRoute.visibility = View.VISIBLE
                        else fragmentView.btnOptimizeRoute.visibility = View.GONE
                        fragmentView.btnRoutePopUpMenu?.visibility = View.VISIBLE
                        fragmentView.btnEditRoute?.visibility = View.VISIBLE

                        fragmentView.tvRouteName?.text =
                            activityViewModel.getSelectedRoute()?.routeName
                        if (activityViewModel.getSelectedRoute()?.routeTime != null && activityViewModel.getSelectedRoute()?.routeDistanceInfo != null)
                            fragmentView.tvTotallTime?.text =
                                "${activityViewModel.getSelectedRoute()?.routeTime} • ${activityViewModel.getSelectedRoute()?.routeDistanceInfo?.text}"
                        else fragmentView.tvTotallTime?.text = ""
                        fragmentView.btnFinishRoute?.visibility = View.GONE
                        stopsAdapter?.setData(activityViewModel.getSelectedRoute()?.stopsList)
                        fragmentView.tvStopCount?.text = getStopsCount()
                        drawRouteOnMap()
                        showOrHideNoStopFoundLabel()
                        activityViewModel.getSelectedRoute()?.stopsList?.let {
                            if (it.isNotEmpty())
                                animateMap()
                        }
                    }
                }
            })
        activityViewModel.onDeleteRoute.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.let {
                if (it) {
                    googleMap?.clear()
                    stopsAdapter?.setData(null)
                    fragmentView.btnEditRoute?.visibility = View.GONE
                    activityViewModel.setOptimizeButtonVisibility(false)
                    fragmentView.btnRoutePopUpMenu?.visibility = View.GONE
                    fragmentView.layoutNoStopFound?.visibility = View.VISIBLE
                    fragmentView.tvStopCount?.text = ""
                    fragmentView.tvTotallTime?.text = ""
                    fragmentView.tvRouteName?.text = ""
                    activityViewModel.setRefreshRoute(true)
                }
            }
        })
        activityViewModel.getOptimizeBtnIndicator()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                it?.let {
                    if (it)
                        fragmentView?.btnOptimizeRoute?.visibility = View.VISIBLE
                    else fragmentView?.btnOptimizeRoute?.visibility = View.GONE
                }
            })
        activityViewModel.getOnRouteEditObserver()
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                it?.let {
                    if (it) {
                        optimizeRoute()
                        activityViewModel.getOnRouteEditObserver().value = null
                    }
                    animateMap()//on route edited
                }
            })
    }


    @SuppressLint("MissingPermission")
    fun setScreenContents(sharedRoute: RouteModel?) {
        CurrentLocationHelper(activity as Activity, true) {
            it?.let {
                it.lastLocation?.latitude?.let { it1 ->
                    it.lastLocation?.longitude?.let { it2 ->
                        activityViewModel.setCurrentLocation(
                            it1,
                            it2
                        )
                    }
                }
            }
            googleMap?.isMyLocationEnabled = true
            initScreenContents()
        }
    }


    private fun initScreenContents() {
        launch(Dispatchers.Main) {
            fragmentView?.loadingProgressBar?.visibility = View.GONE
            if (activityViewModel.getSelectedRoute()!!.stopsList!!.size > 0)
                animateMap()
            else animateMapToPosition(
                activityViewModel.getCurrentLocation().latitude,
                activityViewModel.getCurrentLocation().longitude,
                16f
            )
            initViews()
            drawRouteOnMap()
            showOrHideNoStopFoundLabel()
            activityViewModel.setRefreshRoute(true)
        }
    }

    private fun showOrHideNoStopFoundLabel() {
        if (activityViewModel.getSelectedRoute() != null && activityViewModel.getSelectedRoute()!!.stopsList.size == 0)
            fragmentView.layoutNoStopFound?.visibility = View.VISIBLE
        else fragmentView.layoutNoStopFound?.visibility = View.INVISIBLE
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setGoogleLogo()
        setGoogleType(activityViewModel.getSavedMapType())
        drawRouteOnMap()
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.setOnMapLoadedCallback {
            isMapLoaded = true
        }
        googleMap?.setOnCameraIdleListener {
            val viewPagerMain = requireActivity().findViewById<ViewPager2>(R.id.viewPagerMain)

            viewPagerMain.isUserInputEnabled = true
        }
        googleMap?.setOnCameraMoveStartedListener {
            val viewPagerMain = requireActivity().findViewById<ViewPager2>(R.id.viewPagerMain)
            viewPagerMain.isUserInputEnabled = true
//            activity?.viewPagerMain?.isUserInputEnabled = false
        }
        googleMap?.setOnMapLongClickListener {
            if (activityViewModel.getSelectedRoute() == null)
                return@setOnMapLongClickListener
            if (checkStopsLimit()) {
                LoadingDialog.getInstance(activityContext)?.showDialog("")
                GlobalScope.launch {
                    val address = getAddressFromLatLng(activityContext, it.latitude, it.longitude)
                    (activityContext as RFMainActivity).runOnUiThread {
                        if (address != null && address.address.isNotEmpty()) {
                            val newPlace = PlaceModel(
                                iconsBits[1],
                                address.placeName,
                                address.address,
                                STOP,
                                com.gpsnavigation.maps.gpsroutefinder.routemap.models.LatLng(
                                    address.lat,
                                    address.lng
                                ),
                                REST
                            )
                            if (isDestinationExistInRoute(activityViewModel.getSelectedRoute()?.stopsList))
                                activityViewModel.getSelectedRoute()?.stopsList?.add(
                                    activityViewModel.getSelectedRoute()?.stopsList!!.size - 1,
                                    newPlace
                                )
                            else activityViewModel.getSelectedRoute()?.stopsList?.add(newPlace)
                            fragmentView.tvStopCount?.text = getStopsCount()
                            stopsAdapter?.setData(activityViewModel.getSelectedRoute()?.stopsList)
                            fragmentView.btnFinishRoute?.visibility = View.GONE
                            showOrHideNoStopFoundLabel()
                        }
                        LoadingDialog.getInstance(activityContext)?.dismissDialog()
                        activityViewModel.setOptimizeButtonVisibility(true)
                    }
                }
                googleMap?.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
                    ?.setIcon(
                        bitmapDescriptorFromVector(activityContext, R.drawable.ic_stop_marker)
                    )
                animateMapToPosition(it.latitude, it.longitude, googleMap?.cameraPosition!!.zoom)
            }
        }
    }

    private fun isHomeExistInRoute(stopsList: java.util.ArrayList<PlaceModel>?): Boolean {
        stopsList!!.forEach {
            if (it.placeType == RFMainActivity.HOME)
                return true
        }
        return false
    }

    private fun isDestinationExistInRoute(stopsList: java.util.ArrayList<PlaceModel>?): Boolean {
        stopsList?.forEach {
            if (it.placeType == RFMainActivity.DEST)
                return true
        }
        return false
    }


    fun setGoogleType(it: String?) {
        it?.let {
            when (it) {
                default -> {
                    selectedMApType = MapTypes.Normal
                    googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    googleMap?.setMapStyle(
                        activityContext?.let { it1 ->
                            MapStyleOptions.loadRawResourceStyle(
                                it1,
                                R.raw.normal_mode_map
                            )
                        }
                    )
                }

                Terrian -> {
                    selectedMApType =
                        MapTypes.Terrian
                    googleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
                }

                Satellite -> {
                    selectedMApType =
                        MapTypes.Sattelite
                    googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                }

                Night -> {
                    selectedMApType =
                        MapTypes.Night
                    googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    googleMap?.setMapStyle(
                        activityContext?.let { it1 ->
                            MapStyleOptions.loadRawResourceStyle(
                                it1,
                                R.raw.night_mode_map
                            )
                        }
                    )
                }

                else -> {
                    selectedMApType = MapTypes.Normal
                    googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    googleMap?.setMapStyle(
                        activityContext?.let { it1 ->
                            MapStyleOptions.loadRawResourceStyle(
                                it1,
                                R.raw.normal_mode_map
                            )
                        }
                    )
                }
            }
        }
    }


    private fun setGoogleLogo() {
        val googleLogo = mapFragment?.requireView()?.findViewWithTag<View>("GoogleWatermark")
        val glLayoutParams = googleLogo?.layoutParams as RelativeLayout.LayoutParams
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0)
        googleLogo.layoutParams = glLayoutParams
    }

    private fun getStopsCount(): String? {
        activity?.let {
            var totalStops = 0
            if (activityViewModel.getSelectedRoute()?.stopsList?.size != null && activityViewModel.getSelectedRoute()?.stopsList?.size!! > 0) {
                for (stop in activityViewModel.getSelectedRoute()?.stopsList!!) {
                    if (stop.placeType == RFMainActivity.STOP)
                        totalStops += 1
                }
                return "$totalStops " + getString(R.string.stops) + "•"
            } else return "$totalStops " + getString(R.string.stops) + "•"
        }
        return ""
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        fragmentView?.let {
            it.btnRoutePopUpMenu?.setOnClickListener(this)
            it.btnCurrentLocation?.setOnClickListener(this)
            it.btnFullScreen?.setOnClickListener(this)
            it.btnMapType?.setOnClickListener(this)
            it.btnEditRoute?.setOnClickListener(this)
            it.btnOptimizeRoute?.setOnClickListener(this)
            it.sheetHeader?.setOnClickListener(this)
            activityViewModel.getSelectedRoute()?.let { routeModel ->
                if (routeModel.stopsList.isNotEmpty() && routeModel.stopsList[0]?.leg == null)
                    it.btnOptimizeRoute?.visibility = View.VISIBLE
                else it.btnOptimizeRoute?.visibility = View.GONE
            }
            it.tvRouteName?.text = activityViewModel.getSelectedRoute()?.routeName
            if (activityViewModel.getSelectedRoute()?.routeTime != null && activityViewModel.getSelectedRoute()?.routeDistanceInfo != null)
                it.tvTotallTime?.text =
                    "${activityViewModel.getSelectedRoute()?.routeTime} • ${activityViewModel.getSelectedRoute()?.routeDistanceInfo!!.text}"
            it.tvStopCount?.text = getStopsCount()
            stopsAdapter = StopsOnMapScreenAdapter(activityViewModel.getDefaultStopStayTime(),
                { placeModel: PlaceModel, i: Int -> onEditStopClick(placeModel, i) },
                { placeModel: PlaceModel, i: Int -> onStartButtonClick(placeModel, i) },
                { placeModel: PlaceModel, i: Int -> onStopButtonClick(placeModel, i) },
                { placeModel: PlaceModel, i: Int -> onStopClick(placeModel, i) })
            it.rvStops?.layoutManager =
                LinearLayoutManager(activityContext) as RecyclerView.LayoutManager?
            it.rvStops?.adapter = stopsAdapter
            stopsAdapter?.setData(activityViewModel.getSelectedRoute()?.stopsList)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnFullScreen -> {
                if (isFullscreenMap) {
                    fragmentView?.mapContainer?.let {
                        val lp = it.layoutParams as ConstraintLayout.LayoutParams
                        lp.matchConstraintPercentHeight = 0.4.toFloat()
                        it.layoutParams = lp
                        TransitionManager.beginDelayedTransition(it)
                        isFullscreenMap = false
                    }
                    fragmentView?.rlBottomContent?.let {
                        val lp = it.layoutParams as ConstraintLayout.LayoutParams
                        lp.matchConstraintPercentHeight = 0.6.toFloat()
                        it.layoutParams = lp
                        isFullscreenMap = false
                    }
                } else {
                    fragmentView?.mapContainer?.let {
                        val lp = it.layoutParams as ConstraintLayout.LayoutParams
                        lp.matchConstraintPercentHeight = 0.6.toFloat()
                        it.layoutParams = lp
                        TransitionManager.beginDelayedTransition(it)
                        isFullscreenMap = true
                    }
                    fragmentView?.rlBottomContent?.let {
                        val lp = it.layoutParams as ConstraintLayout.LayoutParams
                        lp.matchConstraintPercentHeight = 0.4.toFloat()
                        it.layoutParams = lp
                        isFullscreenMap = true
                    }
                }
                animateMap()
            }

            R.id.btnMapType -> {
                when (selectedMApType) {
                    MapTypes.Normal -> {
                        selectedMApType = MapTypes.Sattelite
                        googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    }

                    MapTypes.Sattelite -> {
                        selectedMApType = MapTypes.Terrian
                        googleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    }

                    MapTypes.Terrian -> {
                        selectedMApType = MapTypes.Night
                        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                        googleMap?.setMapStyle(
                            activityContext?.let {
                                MapStyleOptions.loadRawResourceStyle(
                                    it,
                                    R.raw.night_mode_map
                                )
                            }
                        )
                    }

                    MapTypes.Night -> {
                        selectedMApType = MapTypes.Normal
                        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                        googleMap?.setMapStyle(
                            activityContext?.let {
                                MapStyleOptions.loadRawResourceStyle(
                                    it,
                                    R.raw.normal_mode_map
                                )
                            }
                        )
                    }
                }
            }

            R.id.btnEditRoute -> {
                /* if (finishRouteInterstitial != null && finishRouteInterstitial!!.isLoaded) {
                     if (AdUtility.canWeShowRemoveAdsPopup(activity!!,showRemoveAdsPopupInterval)) {
                         AdUtility.showRemoveAdsPopup(activity!!)
                     } else {
                         finishRouteInterstitial?.show()
                         finishRouteInterstitial?.adListener = object : AdListener() {
                             override fun onAdClosed() {
                                 super.onAdClosed()
                                 finishRouteInterstitial?.loadAd(AdRequest.Builder().build())
                             }
                         }
                     }
                 }*/
                Timber.tag("Main_map_screen").i("User click on edit route button")
                activityViewModel.getSelectedRoute()?.let {
                    val intent = Intent(activityContext, EditRouteActivity::class.java)
                    intent.putExtra(Constants.EDIT_ROUTE_FLAG, it.routeId)
                    startActivity(intent)
                }
            }

            R.id.btnOptimizeRoute -> {
                Timber.tag("Main_map_screen").i("User click on optimize route button")
                optimizeRoute()
            }

            R.id.btnRoutePopUpMenu -> {
                showPopMenu(fragmentView.btnRoutePopUpMenu)
            }

            R.id.btnCurrentLocation -> {
                if (activityViewModel.getCurrentLocation().latitude == 0.0) {
                    CurrentLocationHelper(activity as Activity, true) {
                        it?.let {
                            val location = it.lastLocation!!
                            activityViewModel.setCurrentLocation(
                                location.latitude,
                                location.longitude
                            )
                            googleMap?.isMyLocationEnabled = true
                            googleMap?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        it.lastLocation?.latitude!!,
                                        it.lastLocation?.longitude!!
                                    ), 16f
                                )
                            )
                        }
                    }

                } else {
                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                activityViewModel.getCurrentLocation().latitude!!,
                                activityViewModel.getCurrentLocation().longitude!!
                            ), 16f
                        )
                    )
                }

            }

        }
    }


    private fun optimizeRoute() {
        if (activityViewModel.getSelectedRoute()?.stopsList.isNullOrEmpty()) {
            activityContext?.toast(getString(R.string.add_atleast_one_stop))
            return
        } else if (activityViewModel.getSelectedRoute()?.stopsList!!.size == 1 && activityViewModel.getSelectedRoute()?.stopsList!![0].stopStatus == HOME) {
            //not found any stop or destination
            activityContext?.toast(getString(R.string.add_destination))
            return
        }
        if (isShowSubscriptionDialogOnOptimizeBtnAfterTen) {
            if (!isUserSubscribed) {
                if (isStopsMoreThan10()) {
                    fragmentView?.bottomsheet?.tvBSDescription?.text =
                        getString(R.string.subscribe_to_create_routes_with_more_than_10_stops)
                    sheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                    return
                }
            }
        }
        googleMap?.clear()
        showStatusProgress()
        var transportMode = "d"
        if (activityViewModel.getSavedVehicleType() == Car)
            transportMode = TransportMode.DRIVING
        else transportMode = TransportMode.DRIVING
        var homeLatLng: LatLng? = null
        if (isHomeExistInRoute(activityViewModel.getSelectedRoute()!!.stopsList)) {
            homeLatLng = LatLng(
                activityViewModel.getSelectedRoute()?.stopsList!![0].placeLatLng!!.latitude,
                activityViewModel.getSelectedRoute()?.stopsList!![0].placeLatLng!!.longitude
            )
        } else {
            homeLatLng = LatLng(
                activityViewModel.getCurrentLocation().latitude!!,
                activityViewModel.getCurrentLocation().longitude!!
            )
            AddressUtility.getAddress(
                activityContext,
                activityViewModel.getCurrentLocation().latitude!!,
                activityViewModel.getCurrentLocation().longitude
            ) {
                val homePlace = PlaceModel(
                    iconsBits[0], it!![0], it[1],
                    HOME,
                    com.gpsnavigation.maps.gpsroutefinder.routemap.models.LatLng(
                        homeLatLng.latitude,
                        homeLatLng.longitude
                    ),
                    REST
                )
                activityViewModel.getSelectedRoute()?.stopsList?.add(0, homePlace)
            }
        }

        var starting = homeLatLng
        var ending = LatLng(
            activityViewModel.getSelectedRoute()?.stopsList!![activityViewModel.getSelectedRoute()?.stopsList?.size!! - 1].placeLatLng!!.latitude,
            activityViewModel.getSelectedRoute()?.stopsList!![activityViewModel.getSelectedRoute()?.stopsList?.size!! - 1].placeLatLng!!.longitude
        )
        GoogleDirection.withServerKey(
            getString(R.string.google_maps_key_part1) + getString(R.string.google_maps_key_part2) + getString(
                R.string.google_maps_key_part3
            ) + getString(R.string.google_maps_key_part4)
        )
            .from(
                homeLatLng
            )
            .and(getWayPoints())
            .to(
                LatLng(
                    activityViewModel.getSelectedRoute()?.stopsList!![activityViewModel.getSelectedRoute()?.stopsList?.size!! - 1].placeLatLng!!.latitude,
                    activityViewModel.getSelectedRoute()?.stopsList!![activityViewModel.getSelectedRoute()?.stopsList?.size!! - 1].placeLatLng!!.longitude
                )
            )
            .optimizeWaypoints(shouldOptimize())
            .transportMode(transportMode)
            .execute(object : DirectionCallback {
                @SuppressLint("SetTextI18n")
                override fun onDirectionSuccess(direction: Direction?) {
                    direction?.let {
                        if (it.isOK) {
                            sheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                            val route = it.routeList[0]
                            activityViewModel.getSelectedRoute()?.routeDistanceInfo =
                                calculateTotallRouteDistance(activityContext, route.totalDistance)
                            activityViewModel.getSelectedRoute()?.routeTime =
                                convertSecondsToTime(route.totalDuration)
                            fragmentView.tvTotallTime.text =
                                "${activityViewModel.getSelectedRoute()?.routeDistanceInfo?.text} • ${activityViewModel.getSelectedRoute()?.routeTime}"
                            launch(Dispatchers.IO) {
                                shuffleStopList(route.waypointOrderList, route.legList)
                            }
                        } else {
                            Log.e(
                                "directionFailed",
                                "d>>>>" + it.errorMessage + direction.routeList
                            )
                            showNoRouteFoundedDialog()

//                            drawPolyLineOnMapSecond(starting,ending,Color.GRAY)
                        }
                        activityViewModel.setOptimizeButtonVisibility(false)
                    }
                }

                override fun onDirectionFailure(t: Throwable) {
                    activity?.toast(getString(R.string.optimization_fail_try_again))
                }
            })
    }

    private fun isStopsMoreThan10(): Boolean {
        var stops = 0
        activityViewModel.getSelectedRoute()?.stopsList?.forEachIndexed { index, placeModel ->
            if (placeModel.placeType == STOP)
                stops += 1
        }
        return stops > 10
    }

    private fun shouldOptimize(): Boolean {
        val optimize = true
        val stopsList = activityViewModel.getSelectedRoute()!!.stopsList
        for (i in 0 until stopsList.size) {
            if (stopsList[i].placeType == RFMainActivity.STOP && i < stopsList.size - 1) {
                if (stopsList[i].isASAP) {
                    return false
                }

            }
        }
        return optimize
    }

    private fun showNoRouteFoundedDialog() {
        activityContext?.toast(getString(R.string.can_not_fint_closest_route))
        /* CustomDialog.getInstance(activityContext!!)?.dismissDialog()
         val view = layoutInflater.inflate(R.layout.dialog_no_route_find, null)
         view.tvDescription.text = getString(R.string.can_not_fint_closest_route)
         view.btnYes.setOnClickListener {
             CustomDialog.getInstance(activityContext)?.dismissDialog()
         }
         CustomDialog.getInstance(activityContext)?.setContentView(view, true)?.showDialog()*/
    }

    private fun showStatusProgress() {
        try {
            val view = layoutInflater.inflate(R.layout.dialog_optimising_route, null)
            view?.let {
                /*it.lottieAnimationView?.setAnimation("optimize_animation.json")
                it.lottieAnimationView.repeatCount = Animation.INFINITE
                it.lottieAnimationView.speed = it.lottieAnimationView.speed - 2
                it.lottieAnimationView.playAnimation()*/
                val progressBar = it.findViewById<ProgressBar>(R.id.progressBar)
                val progressText = it.findViewById<TextView>(R.id.tvStatus)
                CustomDialog.getInstance(activityContext)?.setContentView(it, true)?.showDialog()
                var progressStatus = 0
                launch(Dispatchers.IO) {
                    while (progressStatus < 100) {
                        progressStatus += 1
                        launch(Dispatchers.Main) {
                            activity?.let {
                                progressBar?.progress = progressStatus
                                if (progressStatus == 30)
                                    progressText?.text = getString(R.string.arranging_stops)
                                if (progressStatus == 80)
                                    progressText?.text = getString(R.string.finalizing_optimising)
                                if (progressStatus == 100) {
                                    CustomDialog.getInstance(activityContext)?.dismissDialog()
                                    stopsAdapter?.notifyDataSetChanged()
                                }
                            }
                        }
                        delay(100)
                    }
                }
            }
        } catch (ee: java.lang.Exception) {
            Timber.e(ee)
        }
    }

    private fun shuffleStopList(waypointsOrder: MutableList<Long>, legList: List<Leg>) {
        val optimizedStopList = ArrayList<PlaceModel>()
        waypointsOrder.forEachIndexed { index, value ->
            if (index <= activityViewModel.getSelectedRoute()!!.stopsList.lastIndex)
                optimizedStopList.add(activityViewModel.getSelectedRoute()!!.stopsList[value.toInt() + 1])
        }
        //add place at 0 pos in list as home
        optimizedStopList.add(0, activityViewModel.getSelectedRoute()!!.stopsList[0])
        //add place as dest  to the end of list  as destination
        optimizedStopList.add(
            optimizedStopList.size,
            activityViewModel.getSelectedRoute()!!.stopsList[activityViewModel.getSelectedRoute()!!.stopsList.size - 1]
        )
        for (index in legList.indices) {
            optimizedStopList[index].leg = legList[index]
            optimizedStopList[index + 1].stopDuration = legList[index].duration.value.toString()
        }
        activityViewModel.getSelectedRoute()!!.stopsList.clear()
        activityViewModel.getSelectedRoute()!!.stopsList.addAll(optimizedStopList)
        //to enable home to first stop started status,polyline blue and show start button
        activityViewModel.getSelectedRoute()!!.stopsList?.forEachIndexed { index, placeModel ->
            when (index) {
                0 -> placeModel.stopStatus = STARTED
                1 -> placeModel.stopStatus = STARTED_SHOW_SS_BUTTONS
                else -> placeModel.stopStatus = REST
            }
        }
        //...........

        activityViewModel.updateStopList(
            activityViewModel.getSelectedRoute()
        )
        launch(Dispatchers.Main) {
            stopsAdapter?.setData(activityViewModel.getSelectedRoute()!!.stopsList)
            drawRouteOnMap()
            animateMap()
        }
    }


    private fun drawRouteOnMap() {
        googleMap?.clear()
        if (activityViewModel.getSelectedRoute()?.stopsList == null)
            return
        var startedRouteIndex: Int? = null
        try {
            for (index in 0 until activityViewModel.getSelectedRoute()!!.stopsList.size) {
                if (activityViewModel.getSelectedRoute()!!.stopsList[index].leg != null) {
                    googleMap?.addMarker(
                        MarkerOptions().position(activityViewModel.getSelectedRoute()!!.stopsList[index].leg!!.startLocation.coordination)
                            .icon(
                                bitmapDescriptorFromVector(
                                    activityContext!!,
                                    icons[activityViewModel.getSelectedRoute()!!.stopsList[index].stopIcon!!]
                                )
                            )
                    )
                    if (activityViewModel.getSelectedRoute()!!.stopsList[index].stopStatus == STARTED)
                        startedRouteIndex = index
                    else drawPolyLineOnMap(
                        activityViewModel.getSelectedRoute()!!.stopsList[index].leg,
                        Color.GRAY
                    )
                } else {
                    googleMap?.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                activityViewModel.getSelectedRoute()!!.stopsList[index].placeLatLng!!.latitude,
                                activityViewModel.getSelectedRoute()!!.stopsList[index].placeLatLng!!.longitude
                            )
                        ).icon(
                            bitmapDescriptorFromVector(
                                activityContext!!,
                                icons[activityViewModel.getSelectedRoute()!!.stopsList[index].stopIcon!!]
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        startedRouteIndex?.let {
            drawPolyLineOnMap(
                activityViewModel.getSelectedRoute()!!.stopsList[startedRouteIndex].leg,
                Color.BLUE
            )
        }
    }

    private fun drawPolyLineOnMap(leg: Leg?, color: Int) {
        leg?.let {
            DirectionConverter.createPolyline(activityContext!!, leg.directionPoint, 5, color)
        }?.let {
            googleMap?.addPolyline(
                it
            )
        }
    }

    private fun drawPolyLineOnMapSecond(starting: LatLng, ending: LatLng, color: Int) {

        // inside on map ready method
        // we will be displaying polygon on Google Maps.
        // on below line we will be adding polyline on Google Maps.
        // inside on map ready method
        // we will be displaying polygon on Google Maps.
        // on below line we will be adding polyline on Google Maps.
        googleMap!!.addPolyline(
            PolylineOptions().add(starting, ending)
                .width // below line is use to specify the width of poly line.
                    (5f) // below line is use to add color to our poly line.
                .color(Color.RED) // below line is to make our poly line geodesic.
                .geodesic(true)
        )
        // on below line we will be starting the drawing of polyline.
        // on below line we will be starting the drawing of polyline.
//        googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(Brisbane, 13f))
    }


    private fun getWayPoints(): ArrayList<LatLng> {
        val waypoints = ArrayList<LatLng>()
        val stopsList = activityViewModel.getSelectedRoute()!!.stopsList
        for (i in 0 until stopsList.size) {
            if (stopsList[i].placeType == STOP && i < stopsList.size - 1)
                waypoints.add(
                    LatLng(
                        stopsList[i].placeLatLng!!.latitude,
                        stopsList[i].placeLatLng!!.longitude
                    )
                )
        }
        return waypoints
    }


    private fun animateMap() {
        if (isMapLoaded) {
            if (activityViewModel.getSelectedRoute() != null && activityViewModel.getSelectedRoute()?.stopsList!!.size > 0) {
                val boundsBuilder = LatLngBounds.builder()
                activityViewModel.getSelectedRoute()?.stopsList!!.forEach {
                    boundsBuilder.include(
                        LatLng(
                            it.placeLatLng!!.latitude,
                            it.placeLatLng!!.longitude
                        )
                    )
                }

                googleMap?.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(),
                        100
                    )
                )
            }
        }
    }

    private fun animateMapToPosition(lat: Double, lng: Double, zoom: Float) {
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom))
    }


    fun onEditStopClick(placeModel: PlaceModel?, pos: Int) {
        Timber.tag("Main_map_screen").i("User click on edit stop button")
        showEditStopBSDialog(placeModel, pos)
    }

    @SuppressLint("SetTextI18n")
    private fun showEditStopBSDialog(stopModel: PlaceModel?, pos: Int) {
        var arrivalTimeFrom: String? = null
        var arrivalTimeTo: String? = null
        val eidtStopBinding: LayoutEditStopBinding = LayoutEditStopBinding.inflate(layoutInflater)
        val view = eidtStopBinding.root
//        val view = layoutInflater.inflate(R.layout.layout_edit_stop, null)
        val dialog = BottomSheetDialog(activityContext!!)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        eidtStopBinding.btnDeleteStop.visibility = View.VISIBLE
        eidtStopBinding.tvStopPlaceName.text = stopModel?.placeName ?: ""
        changeDoneButtonStatus(true, eidtStopBinding.btnDone)
        if (stopModel?.isASAP!!) {
            changeNormalAndASAPButtonStatus(
                true,
                eidtStopBinding.btnNormal,
                eidtStopBinding.btnASAP
            )
        } else {
            changeNormalAndASAPButtonStatus(
                false,
                eidtStopBinding.btnNormal,
                eidtStopBinding.btnASAP
            )
        }
        eidtStopBinding.tvArrivalTimeFrom.text = stopModel.leg?.departureTime?.text
        eidtStopBinding.tvArrivalTimeTo.text = stopModel.leg?.arrivalTime?.text
        eidtStopBinding.tvDefaultStopDuration.text = stopModel.stopStayTime.toString()
        eidtStopBinding.etAddNotes.setText(stopModel.stopNotes)
        eidtStopBinding.etAddNotes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count <= 0)
                    changeDoneButtonStatus(true, eidtStopBinding.btnDone)
                else changeDoneButtonStatus(false, eidtStopBinding.btnDone)
            }
        })

        eidtStopBinding.btnNormal.setOnClickListener {
            changeNormalAndASAPButtonStatus(
                false,
                eidtStopBinding.btnNormal,
                eidtStopBinding.btnASAP
            )
            changeDoneButtonStatus(false, eidtStopBinding.btnDone)
            stopModel.isASAP = false
        }

        eidtStopBinding.btnASAP.setOnClickListener {
            changeNormalAndASAPButtonStatus(
                true,
                eidtStopBinding.btnNormal,
                eidtStopBinding.btnASAP
            )
            changeDoneButtonStatus(false, eidtStopBinding.btnDone)
            stopModel.isASAP = true
        }

        dialog.setOnShowListener {
            val d = dialog
            val coordinatorLayout = eidtStopBinding.rootViewBSDialog
            val bottomSheetInternal = eidtStopBinding.bsInternal
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetInternal)
            BottomSheetBehavior.from(coordinatorLayout.parent as View).peekHeight =
                bottomSheetInternal.height
            bottomSheetBehavior.peekHeight = bottomSheetInternal.height
            coordinatorLayout.parent.requestLayout()
        }
        eidtStopBinding.tvArrivalTimeFrom.setOnClickListener {
            val cal = Calendar.getInstance()
            val tpd = TimePickerDialog(
                activityContext,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    changeDoneButtonStatus(false, eidtStopBinding.btnDone)
                    var hour = hourOfDay
                    if (hour == 0)
                        hour = 12
                    arrivalTimeFrom = getTimeInAMPMFormat(hourOfDay, minute)
                    eidtStopBinding.tvArrivalTimeFrom.text = arrivalTimeFrom
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            )
            tpd.show()
        }
        eidtStopBinding.tvArrivalTimeTo.setOnClickListener {
            val cal = Calendar.getInstance()
            val tpd = TimePickerDialog(
                activityContext,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    changeDoneButtonStatus(false, eidtStopBinding.btnDone)
                    var hour = hourOfDay
                    if (hour == 0)
                        hour = 12
                    arrivalTimeTo = getTimeInAMPMFormat(hourOfDay, minute)
                    eidtStopBinding.tvArrivalTimeTo.text = arrivalTimeTo
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            )
            tpd.show()
        }

        eidtStopBinding.tvDefaultStopDuration.setOnClickListener {
            val stopStayTimeBinding: DialogStopStayTimeBinding =
                DialogStopStayTimeBinding.inflate(layoutInflater)
            val viewStayTime = stopStayTimeBinding.root
//            val viewStayTime = layoutInflater.inflate(R.layout.dialog_stop_stay_time, null)
            val etTime = stopStayTimeBinding.etTime
            val btnSetTime = stopStayTimeBinding.btnSetTime
            val btnCancel = stopStayTimeBinding.btnCancel
            etTime.hint = stopModel.stopStayTime.toString() + getString(R.string.minutes)
            CustomDialog.getInstance(activityContext!!)?.setContentView(viewStayTime, false)
                ?.showDialog()
            btnSetTime.setOnClickListener {
                if (etTime.text.isEmpty()) {
                    etTime.error = getString(R.string.enter_time)
                    etTime.requestFocus()
                } else {
                    changeDoneButtonStatus(false, eidtStopBinding.btnDone)
                    if (Integer.parseInt(etTime.text.toString()) > 60 || Integer.parseInt(etTime.text.toString()) < 1)
                        activityContext?.toast(getString(R.string.minutes_mustbe_between_1_to_60))
                    else {
                        eidtStopBinding.tvDefaultStopDuration.text =
                            etTime.text.toString() + "  " + getString(R.string.minutes)
                        stopModel.stopStayTime = etTime.text.toString().toInt()
                        CustomDialog.getInstance(activityContext!!)?.dismissDialog()
                    }
                }
            }
            btnCancel.setOnClickListener {
                eidtStopBinding.tvDefaultStopDuration.text = "1" + " " + getString(R.string.minutes)
                CustomDialog.getInstance(activityContext!!)?.dismissDialog()
            }
        }
        eidtStopBinding.btnDeleteStop.setOnClickListener {
            LoadingDialog.getInstance(activityContext)?.showDialog(null)
            activityViewModel.getSelectedRoute()?.stopsList?.remove(stopModel)
            stopsAdapter?.notifyDataSetChanged()
            if (activityViewModel.getSelectedRoute()!!.stopsList.size == 0) {
                fragmentView.layoutNoStopFound.visibility = View.VISIBLE
                fragmentView.tvStopCount?.text = getStopsCount()
            }
            fragmentView.btnFinishRoute.visibility = View.GONE
            activityViewModel.setOptimizeButtonVisibility(true)
            activityViewModel.updateStopList(
                activityViewModel.getSelectedRoute()
            )
            LoadingDialog.getInstance(activityContext!!)?.dismissDialog()
            dialog.dismiss()
            drawRouteOnMap()
        }

        eidtStopBinding.btnDone.setOnClickListener {
            LoadingDialog.getInstance(activityContext)?.showDialog(null)
            stopModel.stopNotes = eidtStopBinding.etAddNotes.text.toString()
            stopModel.userSelectedArrivalTime = "${arrivalTimeFrom}-${arrivalTimeTo}"
            activityViewModel.getSelectedRoute()?.stopsList!!.sort()
            stopsAdapter?.notifyDataSetChanged()
            activityViewModel.updateStopList(
                activityViewModel.getSelectedRoute()
            )
            LoadingDialog.getInstance(activityContext!!)?.dismissDialog()
            dialog.dismiss()
            drawRouteOnMap()
        }
        dialog.show()
    }

    fun changeNormalAndASAPButtonStatus(isASAPButton: Boolean, normal: Button, asap: Button) {
        if (isASAPButton) {
            normal.setTextColor(ContextCompat.getColor(activityContext!!, R.color.colorAccent))
            asap.setTextColor(ContextCompat.getColor(activityContext!!, R.color.white))
            normal.setBackgroundResource(R.drawable.bg_blue_corner_rounded_25sdp)
            asap.setBackgroundResource(R.drawable.bg_blue_solid_rounded_25sdp)
        } else {
            normal.setTextColor(ContextCompat.getColor(activityContext!!, R.color.white))
            asap.setTextColor(ContextCompat.getColor(activityContext!!, R.color.colorAccent))
            normal.setBackgroundResource(R.drawable.bg_blue_solid_rounded_25sdp)
            asap.setBackgroundResource(R.drawable.bg_blue_corner_rounded_25sdp)
        }
    }

    fun changeDoneButtonStatus(isChangeToAdded: Boolean, btnDone: Button) {
        if (isChangeToAdded) {
            btnDone.text = getString(R.string.added)
            btnDone.setTextColor(ContextCompat.getColor(activityContext!!, R.color.colorAccent))
            btnDone.setBackgroundResource(R.color.transpresnt)
            btnDone.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                activityContext!!.getDrawable(R.drawable.ic_added),
                null
            )
        } else {
            btnDone.text = getString(R.string.done)
            btnDone.setTextColor(ContextCompat.getColor(activityContext!!, R.color.white))
            btnDone.setBackgroundResource(R.drawable.bg_blue_solid_rounded_25sdp)
            btnDone.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun onStartButtonClick(placeModel: PlaceModel?, pos: Int) {
        Timber.tag("Main_map_screen").i("User click on start route button")
        if (isShowSubscriptionDialogOnStartBtn) {
            if (isUserSubscribed) {
                startRoute(placeModel, pos)
            } else {
                fragmentView?.bottomsheet?.tvBSDescription?.text =
                    getString(R.string.start_your_trial_to_unlock_your_route)
                sheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        } else {
            if (isStopsMoreThan10()) {
                if (isShowSubscriptionDialogOnStartBtnAfterTen) {
                    if (isUserSubscribed) {
                        startRoute(placeModel, pos)
                    } else {
                        fragmentView?.bottomsheet?.tvBSDescription?.text =
                            getString(R.string.subscribe_to_create_routes_with_more_than_10_stops)
                        sheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                } else
                    startRoute(placeModel, pos)
            } else startRoute(placeModel, pos)
        }
    }

    fun startRoute(placeModel: PlaceModel?, pos: Int) {
        Log.e("CheckPosition", "startRoute: " + pos)
        GlobalScope.launch(Dispatchers.IO) {
            val previousIndex = pos - 1
            activityViewModel.getSelectedRoute()?.stopsList?.forEach {
                if (it.stopStatus == STARTED || it.stopStatus == STARTED_SHOW_SS_BUTTONS)
                    it.stopStatus = REST
            }
            if (previousIndex >= activityViewModel.getSelectedRoute()?.stopsList!!.size)
                activityViewModel.getSelectedRoute()?.stopsList!![previousIndex].stopStatus =
                    STARTED
            placeModel?.stopStatus = STARTED_SHOW_SS_BUTTONS
            activityViewModel.updateStopList(
                activityViewModel.getSelectedRoute()
            )
        }
        launch {
            drawRouteOnMap()
            stopsAdapter?.notifyDataSetChanged()
            //notification
            val intent = Intent(activityContext, NotificationService::class.java)
            intent.action = Constants.STARTFOREGROUND_ACTION
            intent.putExtra(
                Constants.EDIT_ROUTE_FLAG,
                activityViewModel.getSelectedRoute()?.routeId
            )
            intent.putExtra(Constants.CURRENT_STOP_NUMBER, pos)
            activityContext?.startService(intent)
            openNavigationApp(placeModel!!, pos)
        }
    }

    fun onStopButtonClick(placeModel: PlaceModel?, pos: Int) {
        Timber.tag("Main_map_screen").i("User click on stop route button")
        launch(Dispatchers.IO) {
            activityViewModel.getSelectedRoute()?.stopsList!!.forEach {
                if (it.stopStatus == STARTED || it.stopStatus == STARTED_SHOW_SS_BUTTONS)
                    it.stopStatus = REST
            }
            activityViewModel.getSelectedRoute()?.stopsList!![pos].isCompletedStop = true
            activityViewModel.getSelectedRoute()?.stopsList!![pos].stopStatus = STARTED
            if (pos + 1 <= activityViewModel.getSelectedRoute()?.stopsList!!.size - 1)
                activityViewModel.getSelectedRoute()?.stopsList!![pos + 1].stopStatus =
                    STARTED_SHOW_SS_BUTTONS
            activityViewModel.updateStopList(
                activityViewModel.getSelectedRoute()
            )
        }
        launch(Dispatchers.Main) {
            if (placeModel?.placeType == RFMainActivity.DEST || pos == activityViewModel.getSelectedRoute()!!.stopsList.size - 1) {
                if (isUserSubscribed) {
                    finishRoute()
                } else {
//                    if (finishRouteInterstitial != null && finishRouteInterstitial!!.isLoaded) {
//                        if (AdUtility.canWeShowRemoveAdsPopup(
//                                activity!!,
//                                showRemoveAdsPopupInterval
//                            )
//                        ) {
//                            AdUtility.showRemoveAdsPopup(activity!!)
//                            finishRoute()
//                        } else {
////                            finishRouteInterstitial?.show()
////                            finishRouteInterstitial?.adListener = object : AdListener() {
////                                override fun onAdClosed() {
////                                    super.onAdClosed()
////                                    finishRouteInterstitial?.loadAd(AdRequest.Builder().build())
////                                    finishRoute()
////                                }
////                            }
//                        }
//
//                    } else
//                        finishRoute()

                    finishRoute()
                }
            }
            drawRouteOnMap()
            activityViewModel.setOptimizeButtonVisibility(false)
            stopsAdapter?.notifyDataSetChanged()
        }
    }

    fun onStopClick(stopModel: PlaceModel?, pos: Int) {
        stopModel?.let {
            animateMapToPosition(
                it.placeLatLng!!.latitude,
                it.placeLatLng!!.longitude,
                googleMap?.cameraPosition!!.zoom
            )
        }
        if (stopModel?.placeType == HOME || pos <= 0)
            return
        launch(Dispatchers.IO) {
            activityViewModel.getSelectedRoute()?.stopsList!!.forEach {
                if (it.stopStatus == STARTED || it.stopStatus == STARTED_SHOW_SS_BUTTONS)
                    it.stopStatus = REST
            }
            activityViewModel.getSelectedRoute()?.stopsList!![pos - 1].stopStatus = STARTED
            stopModel?.stopStatus = STARTED_SHOW_SS_BUTTONS
        }
        launch(Dispatchers.Main) {
            fragmentView.btnFinishRoute.visibility = View.GONE
            stopsAdapter?.wholeStopItemClickedPos = pos
            stopsAdapter?.completedStopTime = 0.0
            stopsAdapter?.notifyDataSetChanged()
            drawRouteOnMap()
        }
    }

    private fun openNavigationApp(placeModel: PlaceModel, pos: Int) {
        var startLatLng: LatLng? = null
        if (placeModel.leg == null)
            startLatLng =
                LatLng(placeModel.placeLatLng!!.latitude, placeModel.placeLatLng!!.longitude)
        else startLatLng = LatLng(
            placeModel.leg!!.startLocation.latitude,
            placeModel.leg!!.startLocation.longitude
        )
        if (activityViewModel.getSavedNavMapType() == Google_Maps) {
            val drivingMode: String
            if (activityViewModel.getSavedVehicleType() == Car)
                drivingMode = TransportMode.DRIVING
            else drivingMode = TransportMode.BICYCLING
            openGoogleMap(
                activityContext!!,
                startLatLng.latitude,
                startLatLng.longitude,
                drivingMode
            )
        } else if (activityViewModel.getSavedNavMapType() == Waze) {
            openWaze(
                activityContext!!,
                activityViewModel.getCurrentLocation().latitude,
                activityViewModel.getCurrentLocation().longitude,
                startLatLng.latitude,
                startLatLng.longitude
            )
        } else {
            if (pos > 0) {
                val position = pos - 1
                val latlng = activityViewModel.getSelectedRoute()?.stopsList?.get(
                    position
                )?.placeLatLng?.longitude?.let {
                    activityViewModel.getSelectedRoute()?.stopsList?.get(pos - 1)?.placeLatLng?.latitude?.let { it1 ->
                        LatLng(
                            it1,
                            it
                        )
                    }
                }
                context?.let {
                    latlng?.let { it1 ->
                        MyConstant.IS_APPOPEN_BG_IMPLICIT = true
                        MapUtils.openNavigationBetweenTwoPlaces(
                            it,
                            it1, startLatLng
                        )
                    }
                }
            } else {
                Toast.makeText(requireContext(), "No Route Found", Toast.LENGTH_SHORT).show()
            }
//            activityContext?.browse("geo:0,0?q=${startLatLng.latitude},${startLatLng.longitude}")
        }
    }


    private fun finishRoute() {
        activityContext?.toast(getString(R.string.route_finished))
        //notify
        val intent = Intent(activityContext, NotificationService::class.java)
        intent.action = Constants.STOPFOREGROUND_ACTION
        activityContext?.startService(intent)
//        activity?.let {
//            if (it is RFMainActivity)
//                it.viewPagerMain.currentItem = 1
//        }
        if (requireActivity() != null) {
            val viewPagerMain =
                requireActivity().findViewById<ViewPager2>(R.id.viewPagerMain)

            viewPagerMain.currentItem = 1
        }
        activityViewModel.notifyAddNewRoute()
    }


    private fun showPopMenu(view: View) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.route_popup_menu, popup.menu)
        popup.menu.findItem(R.id.action_duplicate_route).isVisible = false
        popup.menu.findItem(R.id.action_rename_route).isVisible = false
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete_route -> deleteRoute(activityViewModel.getSelectedRoute())
                R.id.action_share_route -> {

                }
                /*activityViewModel.shareRoute(activityViewModel.getSelectedRoute()) {
                    DynamicLinksUtility.shareLink(activityContext!!, it)
                }*/
            }
            true
        }
        popup.show()
    }


    fun deleteRoute(routeModel: RouteModel?) {
        Timber.tag("Main_map_screen").i("User click on delete route button")
        routeModel?.let {
            val deleteDialogBinding: DialogDeleteBinding =
                DialogDeleteBinding.inflate(layoutInflater)
            val deleteView = deleteDialogBinding.root
//            val deleteView = layoutInflater.inflate(R.layout.dialog_delete, null)
            deleteDialogBinding.btnCancel.setOnClickListener {
                CustomDialog.getInstance(activityContext)?.dismissDialog()
            }
            deleteDialogBinding.btnDelete.setOnClickListener {
                LoadingDialog.getInstance(activityContext)?.showDialog(null)
                activityViewModel.deleteRoute(routeModel.routeId)
                activityContext?.toast(getString(R.string.deleted))
                activityViewModel.getRouteList().remove(routeModel.routeId)
                activityViewModel.setSelectedRoute(null)
                LoadingDialog.getInstance(activityContext)?.dismissDialog()
                CustomDialog.getInstance(activityContext)?.dismissDialog()
                activityViewModel.onDeleteRoute.value = true
//                activity?.let {
//                    if (it is RFMainActivity)
//                        it.viewPagerMain.currentItem = 1
//                }
                if (requireActivity() != null) {
                    val viewPagerMain =
                        requireActivity().findViewById<ViewPager2>(R.id.viewPagerMain)

                    viewPagerMain.currentItem = 1
                }
                activityViewModel.notifyAddNewRoute()
            }
            CustomDialog.getInstance(activityContext)?.setContentView(deleteView, false)
                ?.showDialog()
        }

    }


    private fun checkStopsLimit(): Boolean {
        val isHomeLoationSet = isHomeSet()
        val isDestLoationSet = isDestinationSet()
        var limit = 27
        if (isHomeLoationSet && isDestLoationSet)
            limit = 27
        else if (isHomeLoationSet || isDestLoationSet)
            limit = 25
        else limit = 25
        if (activityViewModel.getSelectedRoute()!!.stopsList.size >= limit) {
            val stopLimitExceededBinding: DialogStopLimitExceededBinding =
                DialogStopLimitExceededBinding.inflate(layoutInflater)
            val diaView = stopLimitExceededBinding.root
//            val diaView = layoutInflater.inflate(R.layout.dialog_stop_limit_exceeded, null)
            stopLimitExceededBinding.btnUnderstand.setOnClickListener {
                CustomDialog.getInstance(activityContext)?.dismissDialog()
            }
            CustomDialog.getInstance(activityContext)?.setContentView(diaView, true)?.showDialog()
            return false
        } else return true
    }


    private fun isHomeSet(): Boolean {
        activityViewModel?.getSelectedRoute()?.stopsList?.forEach {
            if (it.placeType == HOME)
                return true
        }
        return false
    }

    private fun isDestinationSet(): Boolean {
        activityViewModel?.getSelectedRoute()?.stopsList?.forEach {
            if (it.placeType == DEST)
                return true
        }
        return false
    }


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main


}

