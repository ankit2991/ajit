package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.gpslocation.SuggestedPlacesAdapter
import com.android.gpslocation.utils.ApiClientHelper
import com.android.gpslocation.utils.PermissionUtils
import com.android.gpslocation.utils.USafeToast
import com.android.gpslocation.utils.requestAllPermissions
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.material.snackbar.Snackbar
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivitySatelliteViewBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.dpToPx
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.hideKeyboard
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.hideStatusBar
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openSubscriptionPaywall
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class LiveTrafficActivity : BaseActivity(), OnMapReadyCallback, View.OnClickListener {

    val mainActivityViewModel: MainActivityViewModel by viewModel()
    var suggestedPlacesAdapter: SuggestedPlacesAdapter? = null
    val RESULT_SPEECH: Int = 123
    var mapFragment: SupportMapFragment? = null
    var mClient: ApiClientHelper? = null
    var googleMap: GoogleMap? = null

    var myLoaction: Location? = null
    var currentLoaction: Location? = null
    var checkForLocation: Boolean = true
    var ivGetCurrentLocation: ImageView? = null
    var content: Context = this

    internal var dialogPermission: Dialog? = null
    lateinit var binding: ActivitySatelliteViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySatelliteViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()
        setInset()

        Places.initialize(applicationContext, getString(R.string.google_maps_key_part1) + getString(R.string.google_maps_key_part2) + getString(R.string.google_maps_key_part3) +getString(R.string.google_maps_key_part4))
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
        ivGetCurrentLocation = binding.ivGetlocation

        dialogPermission = Dialog(this)
        dialogPermission!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogPermission!!.setCancelable(true)

        binding.toolbar.title.text = getString(R.string.live_traffic)
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium

        if (!TinyDB.getInstance(this).isPremium) {
            binding.adsContainer.isVisible = true
            MaxAdManager.showBannerAds(this, binding.adsContainer)
        }
    }
    override fun onResume() {
        super.onResume()
        binding.toolbar.premiumButton.isInvisible =
            if (TinyDB.getInstance(this).isPremium) {
                with(binding.adsContainer) {
                    removeAllViews()
                    isVisible = false
                    tag = null
                }
                (binding.btn3d2d.layoutParams as ConstraintLayout.LayoutParams).apply {
                    bottomMargin = 26.dpToPx
                }
                true
            } else {
                binding.adsContainer.isVisible = true
                MaxAdManager.showBannerAds(this, binding.adsContainer)
                (binding.btn3d2d.layoutParams as ConstraintLayout.LayoutParams).apply {
                    bottomMargin = 16.dpToPx
                }
                false
            }
    }
    private fun setActions() {

        binding.btnSpeakNow.setOnClickListener {

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
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
                binding.searchView.hideKeyboard()
                getAddressAndShowList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                binding.rvSuggestion.visibility = View.GONE
                return true
            }

        })

        binding.btnZoomIn!!.setOnClickListener() {

            googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }
        binding.btnZoomOut!!.setOnClickListener() {

            googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }

        ivGetCurrentLocation!!.setOnClickListener() {

            if (PermissionUtils.hasLocationPermissions(this)) {
                if (ApiClientHelper.isLocationEnabled(this)) {
                    if (currentLoaction != null && googleMap != null) {
                        myLoaction!!.latitude = currentLoaction!!.latitude
                        myLoaction!!.longitude = currentLoaction!!.longitude

                        googleMap!!.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    currentLoaction!!.latitude,
                                    currentLoaction!!.longitude
                                ), googleMap!!.cameraPosition.zoom
                            )
                        )
                    } else {
                        prepareApiClientHelper()
                    }
                } else {
                    ApiClientHelper.showLocationSettingDialog(this, 0)
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
                ApiClientHelper.getLatLngFromAddress(this@LiveTrafficActivity, query.toString())
            withContext(Dispatchers.Main)
            {
                if (list == null) {
                    binding.rvSuggestion.visibility = View.GONE
                    Snackbar.make(
                        binding.root,
                        "No location found!",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
                } else if (list!!.size > 0) {
                    binding.rvSuggestion.visibility = View.VISIBLE
                    if (suggestedPlacesAdapter == null) {
                        suggestedPlacesAdapter = SuggestedPlacesAdapter() {
                            binding.rvSuggestion.visibility = View.GONE
                            myLoaction!!.latitude = it.getLat()
                            if (myLoaction != null) {
                                googleMap?.animateCamera(
                                    CameraUpdateFactory.newLatLng(
                                        LatLng(
                                            myLoaction!!.latitude,
                                            myLoaction!!.longitude
                                        )
                                    )
                                )
                            }
                        }
                        suggestedPlacesAdapter?.setData(list)
                        binding.rvSuggestion.layoutManager = LinearLayoutManager(this@LiveTrafficActivity)
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
            it.isTrafficEnabled = true

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
                if (checkForLocation) {
                    myLoaction!!.latitude = lastLocation.latitude
                    myLoaction!!.longitude = lastLocation.longitude
                    googleMap!!.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                currentLoaction!!.latitude,
                                currentLoaction!!.longitude
                            )
                        ).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.ic_current_loc)
                        )
                    )
                    googleMap!!.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                lastLocation.latitude,
                                lastLocation.longitude
                            ), 15f
                        )
                    )
                    checkForLocation = false
                }
            }
        }
    }


    fun checkLocationPermission(onSuccess: (() -> Unit)): Boolean =
        if (PermissionUtils.hasLocationPermissions(this)) {
            onSuccess.invoke();true
        } else {
            requestAllPermissions(this, {}, {}, *PermissionUtils.locationPerms)
            false
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TinyDB.getInstance(this).setCurrentTime(0)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (binding.searchView.isIconified) {
            finish()
        } else binding.searchView.isIconified = true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.navigation_button -> finish()
            R.id.premium_button -> openSubscriptionPaywall()
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
