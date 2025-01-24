package com.gpsnavigation.maps.gpsroutefinder.routemap.activity


import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.Window
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.gpslocation.SuggestedPlacesAdapter
import com.android.gpslocation.utils.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.material.snackbar.Snackbar
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.MyConstant
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityNavigationBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class NavigationActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener {

    val mainActivityViewModel: MainActivityViewModel by viewModel()
    var suggestedPlacesAdapter: SuggestedPlacesAdapter? = null
    val RESULT_SPEECH: Int = 123
    private val DEFAULT = 1
    private val SATTELITE = 2
    private val NIGHT = 4
    internal var selectedMapStype = DEFAULT
    var mapFragment: SupportMapFragment? = null
    var mClient: ApiClientHelper? = null
    var googleMap: GoogleMap? = null
    val SOURCE_PLACE_PICKER_REQUEST = 12


    var myLoaction: Location? = null
    var currentLoaction: Location? = null
    var checkForLocation: Boolean = true
    var textPickLocatiion: TextView? = null
    var ivGetCurrentLocation: ImageView? = null
    var content: Context = this

    internal var dialogPermission: Dialog? = null
    lateinit var binding:ActivityNavigationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_navigation)
        Places.initialize(applicationContext, getString(R.string.google_maps_key_part1) + getString(R.string.google_maps_key_part2) + getString(R.string.google_maps_key_part3) + getString(R.string.google_maps_key_part4))
        init()
        setActions()

    }

    private fun init() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (mapFragment != null) {
            mapFragment?.getMapAsync(this)
        }
        myLoaction = Location("")
        currentLoaction = Location("")
        textPickLocatiion = binding.btnPickLocationGoogleMap
        ivGetCurrentLocation = binding.ivGetlocation


        dialogPermission = Dialog(this)
        dialogPermission!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogPermission!!.setCancelable(true)


    }

    private fun setActions() {

        binding.btnSpeakNow.setOnClickListener {
            var intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
            try {
                startActivityForResult(intent, RESULT_SPEECH);
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT
                ).show();
            }
        }
        binding.rlSearchView.setOnClickListener {
            binding.labelSearchView.visibility = View.GONE
            binding.searchView.requestFocus()
            binding.searchView.isIconified = false
        }
        binding.searchView.setOnCloseListener {
            binding.labelSearchView.visibility = View.VISIBLE
            binding.rvSuggestion.visibility = View.GONE
            false
        }
        binding.searchView.setOnSearchClickListener {
            binding.labelSearchView.visibility = View.GONE
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                getAddressAndShowList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                binding.rvSuggestion.visibility = View.GONE
                return true
            }

        })
        binding.ivChangeMapType!!.setOnClickListener() {
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
        }
        binding.btnStreetOnOff!!.setOnClickListener() {
            googleMap?.isTrafficEnabled = !googleMap?.isTrafficEnabled!!
        }
        binding.btnZoomIn!!.setOnClickListener() {
            googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }
        binding.btnZoomOut!!.setOnClickListener() {
            googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }

        textPickLocatiion!!.setOnClickListener() {
            if (myLoaction != null) {
                MyConstant.IS_APPOPEN_BG_IMPLICIT = true
                MapUtils.openNavigationMaps(
                    this,
                    LatLng(myLoaction!!.latitude!!, myLoaction!!.longitude!!)
                )
            } else {
                USafeToast.show(content, "No Address Found")
            }
        }

        ivGetCurrentLocation!!.setOnClickListener() {
            if (PermissionUtils.hasLocationPermissions(this)) {
                if (ApiClientHelper.isLocationEnabled(MainActivity@ this)) {
                    if (currentLoaction != null) {
                        myLoaction!!.latitude = currentLoaction!!.latitude
                        myLoaction!!.longitude = currentLoaction!!.longitude
                        if (googleMap != null) {
                            googleMap!!.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        currentLoaction!!.latitude,
                                        currentLoaction!!.longitude
                                    ), googleMap!!.cameraPosition.zoom
                                )
                            )
                        }
                        doAsync {
                            val listOFPlaces = ApiClientHelper.getAddress(
                                this@NavigationActivity,
                                currentLoaction!!.latitude,
                                currentLoaction!!.longitude
                            )
                            uiThread {
                                if (listOFPlaces != null && listOFPlaces.size > 0) {
                                    binding.cardMap.visibility = View.VISIBLE
                                    binding.tvPlaceAdress.setText(listOFPlaces.get(1))
                                    binding.tvPlaceName.setText(listOFPlaces.get(0))
                                } else {
                                    binding.cardMap.visibility = View.INVISIBLE
                                    //  Toast.makeText(LocationPickerActivity@this,"No Address Found",Toast.LENGTH_SHORT).show()
                                    USafeToast.show(content, "No Address Found")
                                }
                            }
                        }
                    } else {
                        prepareApiClientHelper()
                    }
                } else {
                    ApiClientHelper.showLocationSettingDialog(MainActivity@ this, 0)
                }
            } else {
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
                    checkLocationPermission { prepareApiClientHelper() }
                }
                dialogPermission!!.getWindow()!!
                    .setBackgroundDrawableResource(android.R.color.transparent)
                if (!isFinishing()) {
                    dialogPermission!!.show()
                }
            }
        }
    }


    private fun getAddressAndShowList(query: String?) {
        binding.pBSugesstedPlaces.visibility = View.VISIBLE
        GlobalScope.async {
            var list =
                ApiClientHelper.getLatLngFromAddress(this@NavigationActivity, query.toString())
            withContext(Dispatchers.Main)
            {
                if (list == null) {
                    binding.rvSuggestion.visibility = View.GONE
                    Snackbar.make(
                        binding.rootMAp.findViewById(R.id.rootMAp),
                        "No location found!",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                } else if (list!!.size > 0) {
                    binding.rvSuggestion.visibility = View.VISIBLE
                    if (suggestedPlacesAdapter == null) {
                        suggestedPlacesAdapter = SuggestedPlacesAdapter() {
                            binding.rvSuggestion.visibility = View.GONE
                            binding.cardMap!!.visibility = View.VISIBLE
                            myLoaction!!.latitude = it.getLat()
                            myLoaction!!.longitude = it.getLng()
                            binding.tvPlaceAdress.setText(it.address)
                            binding.tvPlaceName.setText(it.getPlaceName())
                            if (myLoaction != null)
                                googleMap?.animateCamera(
                                    CameraUpdateFactory.newLatLng(
                                        LatLng(
                                            myLoaction!!.latitude,
                                            myLoaction!!.longitude
                                        )
                                    )
                                )
                        }
                        suggestedPlacesAdapter?.setData(list)
                        binding.rvSuggestion.layoutManager = LinearLayoutManager(this@NavigationActivity)
                        binding.rvSuggestion.adapter = suggestedPlacesAdapter
                    } else {
                        suggestedPlacesAdapter?.setData(list)
                        suggestedPlacesAdapter?.notifyDataSetChanged()
                    }
                }
                binding.pBSugesstedPlaces.visibility = View.GONE
            }
        }
    }

    override fun onMapReady(gMap: GoogleMap) {
        this.googleMap = gMap
        googleMap?.let {

            if (PermissionUtils.hasLocationPermissions(this)) {
                checkLocationPermission { prepareApiClientHelper() }
            } else {
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
                    checkLocationPermission { prepareApiClientHelper() }
                }
                dialogPermission!!.getWindow()!!
                    .setBackgroundDrawableResource(android.R.color.transparent)
                if (!isFinishing()) {
                    dialogPermission!!.show()
                }
            }
            it.getUiSettings().setCompassEnabled(false)
            it.uiSettings?.isMapToolbarEnabled = false
            it.mapType = GoogleMap.MAP_TYPE_NORMAL
            it.getUiSettings()!!.setZoomControlsEnabled(false)
            it.setOnCameraIdleListener(this)
            it.setOnCameraMoveStartedListener(this)
        }
    }

    private fun prepareApiClientHelper() {
        mClient = this?.let { activity -> ApiClientHelper(activity, mLocationCallback) }
    }

    var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location
            if (locationResult.locations == null)
                return
            if (locationResult.lastLocation == null) {
                return
            } else {
                lastLocation = locationResult.lastLocation!!

                currentLoaction!!.latitude = lastLocation.latitude
                currentLoaction!!.longitude = lastLocation.longitude
                if (checkForLocation == true) {
                    myLoaction!!.latitude = lastLocation.latitude
                    myLoaction!!.longitude = lastLocation.longitude
                    if (googleMap != null) {
                        googleMap!!.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastLocation.latitude,
                                    lastLocation.longitude
                                ), 15f
                            )
                        )
                    }

                    var listOFPlaces = ApiClientHelper.getAddress(
                        this@NavigationActivity,
                        lastLocation.latitude,
                        lastLocation.longitude
                    )
                    if (listOFPlaces != null && listOFPlaces.size > 0) {
                        binding.cardMap.visibility = View.VISIBLE
                        binding.tvPlaceAdress.setText(listOFPlaces.get(1))
                        binding.tvPlaceName.setText(listOFPlaces.get(0))

                    } else {
                        binding.cardMap.visibility = View.INVISIBLE
                        USafeToast.show(content, "No Address Found")
                    }
                    checkForLocation = false
                }
            }
        }
    }


    fun checkLocationPermission(onSuccess: (() -> Unit)): Boolean =
        if (PermissionUtils.hasLocationPermissions(this)) {
            onSuccess.invoke();true
        } else {
            requestAllPermissions(this, {}, {}, *PermissionUtils.locationPerms);false
        }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            RESULT_SPEECH -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    if (result != null && result.size > 0)
                        getAddressAndShowList(result.get(0))
                    else USafeToast.show(this, "No result found")
                }
            }
            SOURCE_PLACE_PICKER_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {

                    try {
                        data?.let {
                            var place = Autocomplete.getPlaceFromIntent(it)
                            if (place != null && place.latLng != null) {
                                myLoaction!!.latitude = place.latLng!!.latitude
                                myLoaction!!.longitude = place.latLng!!.longitude
                                googleMap!!.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            place.latLng!!.latitude,
                                            place.latLng!!.longitude
                                        ), 15f
                                    )
                                )
                                var listOFPlaces = ApiClientHelper.getAddress(
                                    this@NavigationActivity,
                                    place.latLng!!.latitude,
                                    place.latLng!!.longitude
                                )
                                if (listOFPlaces != null && listOFPlaces.size > 0) {
                                    binding.cardMap.visibility = View.VISIBLE
                                    binding.tvPlaceAdress.setText(listOFPlaces.get(1) + "")
                                    binding.tvPlaceName.setText(listOFPlaces.get(0))
                                } else {
                                    binding.cardMap.visibility = View.INVISIBLE
                                    //   Toast.makeText(tv_place_name.context,"No Address Found",Toast.LENGTH_SHORT).show()
                                    USafeToast.show(content, "No Address Found")
                                }
                            } else {
                                binding.cardMap.visibility = View.INVISIBLE
                                // Toast.makeText(tv_place_name.context,"Place Not Found",Toast.LENGTH_SHORT).show()
                                USafeToast.show(content, "Place Not Found")
                            }
                        }
                    } catch (exception: Exception) {

                    }


                }
            }

        }
    }


    override fun onCameraMoveStarted(p0: Int) {
        /* if (dote_dote_marker.translationY == 0f) {
             dote_dote_marker.animate()
                     .translationY(-75f)
                     .setInterpolator(OvershootInterpolator())
                     .setDuration(250)
                     .start()
         }*/
    }

    override fun onCameraIdle() {
        /*dote_dote_marker.animate()
                .translationY(0f)
                .setInterpolator(OvershootInterpolator())
                .setDuration(250)
                .start()*/
        var p0 = googleMap?.cameraPosition
        if (p0 != null) {
            Timber.e("ppppp2")
            doAsync {

                Timber.e(p0.target.latitude.toString() + p0.target.latitude.toString() + "ppppp3" + p0.target.describeContents())
                var listOFPlaces = ApiClientHelper.getAddress(
                    this@NavigationActivity,
                    p0!!.target.latitude,
                    p0!!.target.longitude
                )
                uiThread {
                    binding.doteMarker!!.visibility = View.VISIBLE
                    myLoaction!!.latitude = p0!!.target.latitude
                    myLoaction!!.longitude = p0.target.longitude
                    if (listOFPlaces != null && listOFPlaces.size > 0) {
                        binding.cardMap!!.visibility = View.VISIBLE
                        binding.tvPlaceAdress.setText(listOFPlaces.get(1) + "")
                        binding.tvPlaceName.setText(listOFPlaces.get(0))
                        Timber.e(listOFPlaces.get(1) + "ppppp")
                        Timber.e(listOFPlaces.get(0) + "ppppp1")
                    } else {
                        Timber.e("ppppp0")
                        binding.cardMap.visibility = View.INVISIBLE
                        // Toast.makeText(tv_place_name.context,"No Address Found",Toast.LENGTH_SHORT).show()
                        USafeToast.show(content, "No Address Found")
                    }

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TinyDB.getInstance(this).setCurrentTime(0)
    }

    override fun onBackPressed() {
        if (binding.searchView.isIconified) {
            finish()
        } else binding.searchView.isIconified = true
    }

}
