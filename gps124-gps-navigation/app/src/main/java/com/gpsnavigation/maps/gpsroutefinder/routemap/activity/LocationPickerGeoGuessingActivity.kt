package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.android.gpslocation.SuggestedPlacesAdapter
import com.android.gpslocation.utils.PermissionUtils
import com.android.gpslocation.utils.USafeToast
import com.example.myapplication.utils.ActivateGps
import com.example.myapplication.utils.CurrentLocationHelper
import com.android.gpslocation.utils.GPSUtilities
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityGeoGuessingLocationPickerBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.LocationHelper.selectLatLng
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.dpToPx
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openSubscriptionPaywall
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.anko.toast
import kotlin.coroutines.CoroutineContext


class LocationPickerGeoGuessingActivity :
    BaseActivity(),
    OnMapReadyCallback,
    CoroutineScope,
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener,
    View.OnClickListener {

    var suggestedPlacesAdapter: SuggestedPlacesAdapter? = null
    val RESULT_SPEECH: Int = 123
    private val DEFAULT = 1
    private val SATTELITE = 2
    private val NIGHT = 4
    val RC_GPS = 5
    internal var selectedMapStype = DEFAULT
    var mapFragment: SupportMapFragment? = null
    var googleMap: GoogleMap? = null
    //var myLoaction: Location? = null
    //var currentLoaction: Location? = null
    var content: Context = this
    internal var dialogPermission: Dialog? = null

    lateinit var binding: ActivityGeoGuessingLocationPickerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeoGuessingLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()

        init()
        initViews()
        setListeners()
    }


    private fun init() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (mapFragment != null) {
            mapFragment?.getMapAsync(this)
        }
        //myLoaction = Location("")
        dialogPermission = Dialog(this)
        dialogPermission!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogPermission!!.setCancelable(true)
    }

    private fun initViews() {
        binding.toolbar.title.text = getString(R.string.geo_game_activity_title)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium

        if (!TinyDB.getInstance(this).isPremium) {
            binding.adsContainer.isVisible = true
            MaxAdManager.showBannerAds(this, binding.adsContainer)
        }
    }

    private fun setListeners() {
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)

        binding.pickLocationButton.setOnClickListener(this)

        //binding.ivChangeMapType.setOnClickListener(this)
        binding.btnZoomIn.setOnClickListener(this)
        binding.btnZoomOut.setOnClickListener(this)
        binding.ivGetlocation.setOnClickListener(this)
    }


/*    private fun getAddressAndShowList(query: String) {
        launch(Dispatchers.Main) {
            var addressList: ArrayList<AddressModel>? = null
//            pBSugesstedPlaces.visibility = View.VISIBLE
            async(Dispatchers.IO) {
                addressList = AddressUtility.getAddressListFromLocationName(
                    this@LocationPickerGeoGuessingActivity,
                    query
                )
            }.await()
            if (!addressList.isNullOrEmpty()) {
                setResultPlaceOnMap(addressList!![0])
//                pBSugesstedPlaces.visibility = View.GONE
//                rvSuggestion.visibility = View.VISIBLE
                if (suggestedPlacesAdapter == null) {
                    suggestedPlacesAdapter = SuggestedPlacesAdapter {
                        setResultPlaceOnMap(it)
                    }
                    suggestedPlacesAdapter?.setData(addressList)
//                    rvSuggestion.layoutManager =
//                        LinearLayoutManager(this@LocationPickerGeoGuessingActivity)
//                    rvSuggestion.adapter = suggestedPlacesAdapter
                } else {
                    suggestedPlacesAdapter?.setData(addressList)
                    suggestedPlacesAdapter?.notifyDataSetChanged()
                }
            } else {
//                pBSugesstedPlaces.visibility = View.GONE
//                rvSuggestion.visibility = View.GONE
                toast(getString(R.string.no_address_found))
            }
        }
    }*/

    /*private fun setResultPlaceOnMap(it: AddressModel) {
        myLoaction!!.latitude = it.lat
        myLoaction!!.longitude = it.lng
//        tv_place_adress.text = it.address
//        tv_place_name.text = it.placeName
        *//* if (myLoaction != null)
             googleMap?.animateCamera(
                 CameraUpdateFactory.newLatLng(
                     LatLng(
                         myLoaction!!.latitude,
                         myLoaction!!.longitude
                     )
                 )
             )*//*
    }*/

    override fun onMapReady(gMap: GoogleMap) {
        this.googleMap = gMap
        googleMap?.let {
            it.setPadding(0, 0, 0, resources.getDimensionPixelSize(R.dimen._20sdp))
            if (PermissionUtils.hasLocationPermissions(this)) {
                getCurrentLocation()
            } else {
                showPermissionDialog()
            }
            it.uiSettings.isCompassEnabled = false
            it.uiSettings?.isMapToolbarEnabled = false
            it.mapType = GoogleMap.MAP_TYPE_NORMAL
            it.uiSettings!!.isZoomControlsEnabled = false
            it.setOnCameraIdleListener(this)
            it.setOnCameraMoveStartedListener(this)
        }
    }

    private fun showPermissionDialog() {
        dialogPermission!!.setContentView(R.layout.layout_permission_dialog)
        val notNow = dialogPermission!!.findViewById(R.id.btn_notnow) as Button
        notNow.setOnClickListener {
            if (dialogPermission!!.isShowing) {
                dialogPermission!!.cancel()
            }
        }
        val allow = dialogPermission!!.findViewById(R.id.btn_continue) as Button
        allow.setOnClickListener {
            if (dialogPermission!!.isShowing) {
                dialogPermission!!.cancel()
            }
            getCurrentLocation()
        }
        dialogPermission!!.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent)
        if (!isFinishing()) {
            dialogPermission!!.show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            RC_GPS -> {
                getCurrentLocation()
            }

            RESULT_SPEECH -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (result != null && result.size > 0)
//                        getAddressAndShowList(result.get(0))
                    else USafeToast.show(this, getString(R.string.no_address_found))
                }
            }
        }
    }


    override fun onCameraMoveStarted(p0: Int) {
    }

    override fun onCameraIdle() {
        //val p0 = googleMap?.cameraPosition

        /*p0?.let {
            AddressUtility.getAddress(
                this@LocationPickerGeoGuessingActivity,
                it.target.latitude,
                it.target.longitude
            ) { addressList ->
                binding.doteMarker?.visibility = View.VISIBLE
                myLoaction?.latitude = it.target.latitude
                myLoaction?.longitude = it.target.longitude
                if (addressList != null && addressList.size > 0) {
//                    tv_place_adress.text = addressList[1]
//                    tv_place_name.text = addressList[0]
                } else {
                    toast(getString(R.string.no_address_found))
                }
            }
        }*/
    }

    private fun getCurrentLocation() {
        if (PermissionUtils.hasLocationPermissions(this)) {
            if (GPSUtilities.isGPSEnabled(this))
                CurrentLocationHelper(this, true) {
                    it?.lastLocation?.also { location ->
                        googleMap?.apply {
                            animateCamera(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition(
                                        LatLng(location.latitude, location.longitude),
                                        cameraPosition.zoom,
                                        0f,
                                        0f
                                    )
                                ),
                                300,
                                null
                            )
                        }
                    }
//                    currentLoaction = it?.lastLocation
//                    setCurrentLocationOnMap()
                }
            else {
                ActivateGps(this, false, RC_GPS)
            }
        } else {
            showPermissionDialog()
        }
    }


    /*private fun setCurrentLocationOnMap() {
        myLoaction!!.latitude = currentLoaction!!.latitude
        myLoaction!!.longitude = currentLoaction!!.longitude
        *//* googleMap?.animateCamera(
             CameraUpdateFactory.newLatLngZoom(
                 LatLng(
                     currentLoaction!!.latitude,
                     currentLoaction!!.longitude
                 ), 16f
             )
         )*//*
        AddressUtility.getAddress(
            this,
            currentLoaction!!.latitude,
            currentLoaction!!.longitude
        ) {
            if (it != null && it.size > 0) {
//                tv_place_adress.text = it[1]
//                tv_place_name.text = it[0]
            } else {
                USafeToast.show(content, getString(R.string.no_address_found))
            }
        }
    }*/


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

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
                (binding.pickLocationButton.layoutParams as LinearLayoutCompat.LayoutParams).apply {
                    bottomMargin = 26.dpToPx
                }
                true
            } else {
                binding.adsContainer.isVisible = true
                MaxAdManager.showBannerAds(this, binding.adsContainer)
                (binding.pickLocationButton.layoutParams as LinearLayoutCompat.LayoutParams).apply {
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
            R.id.pick_location_button -> {
                googleMap?.cameraPosition?.let {
                    Log.d("GeoGame", "${it.target}")
                    selectLatLng = it.target

                    startActivity(
                        Intent(
                            this@LocationPickerGeoGuessingActivity,
                            DisplayGeoGuessingResultActivity::class.java
                        )
                    )
                } ?: toast("Please select location on Map")
            }

            /*R.id.ivChangeMapType -> {
                if (googleMap != null) {
                    if (selectedMapStype == DEFAULT) {
                        googleMap?.setMapType(SATTELITE)
                        selectedMapStype = SATTELITE
                    } else if (selectedMapStype == SATTELITE) {
                        googleMap?.setMapType(DEFAULT)
                        googleMap?.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                this,
                                R.raw.night_mode_map
                            )
                        )
                        selectedMapStype = NIGHT
                    } else if (selectedMapStype == NIGHT) {
                        googleMap?.setMapType(DEFAULT)
                        googleMap?.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                this,
                                R.raw.normal_mode_map
                            )
                        )
                        selectedMapStype = DEFAULT
                    }
                }
            }*/

            R.id.btnZoomIn -> {
                googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
            }

            R.id.btnZoomOut -> {
                googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
            }

            R.id.iv_getlocation -> {
                getCurrentLocation()
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
