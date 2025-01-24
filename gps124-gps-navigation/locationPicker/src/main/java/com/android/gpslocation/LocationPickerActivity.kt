package com.android.gpslocation

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.gpslocation.databinding.ActivityLocationPickerBinding
import com.android.gpslocation.utils.*
import com.example.myapplication.utils.ActivateGps
import com.example.myapplication.utils.CurrentLocationHelper
import com.android.gpslocation.utils.GPSUtilities
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.toast
import kotlin.coroutines.CoroutineContext


class LocationPickerActivity : AppCompatActivity(), OnMapReadyCallback, CoroutineScope,
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener {

    var suggestedPlacesAdapter: SuggestedPlacesAdapter? = null
    val RESULT_SPEECH: Int = 123
    private val DEFAULT = 1
    private val SATTELITE = 2
    private val NIGHT = 4
    val RC_GPS = 5
    internal var selectedMapStype = DEFAULT
    var mapFragment: SupportMapFragment? = null
    var googleMap: GoogleMap? = null
    var myLoaction: Location? = null
    var currentLoaction: Location? = null
    var content: Context = this
    internal var dialogPermission: Dialog? = null

    lateinit var binding:ActivityLocationPickerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_location_picker)
        binding = ActivityLocationPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Places.initialize(applicationContext, getString(R.string.google_maps_key_part1)+ getString(R.string.google_maps_key_part2)+getString(R.string.google_maps_key_part3)+getString(R.string.google_maps_key_part4))
        init()
        setActions()
    }


    private fun init() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (mapFragment != null) {
            mapFragment?.getMapAsync(this)
        }
        myLoaction = Location("")
        dialogPermission = Dialog(this)
        dialogPermission!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogPermission!!.setCancelable(true)
    }

    private fun setActions() {

        binding.btnExit.setOnClickListener {
            finish()
        }
        binding.btnSpeak.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
            try {
                startActivityForResult(intent, RESULT_SPEECH);
            } catch (a: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.ivSearchCancel.setOnClickListener {
            binding.etSearch.clearFocus()
        }

        binding.etSearch.setOnFocusChangeListener{ view: View, hasFocus: Boolean ->
            if (hasFocus){
                binding.ivSearchCancel.visibility = View.VISIBLE
                binding.etSearch.hint = ""
                binding.btnExit.visibility = View.GONE
                binding.btnSearch.visibility = View.GONE
                KeyboardUtil.openKeyBoad(this@LocationPickerActivity,binding.etSearch)
            }else{
                binding.etSearch.text.clear()
                binding.etSearch.hint = getString(com.android.gpslocation.R.string.search_here)
                binding.ivSearchCancel.visibility = View.GONE
                binding.rvSuggestion.visibility = View.GONE
                binding.btnExit.visibility = View.VISIBLE
                binding.btnSearch.visibility = View.VISIBLE
                KeyboardUtil.hideKeyBoad(this,binding.etSearch)
            }
        }

        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            KeyboardUtil.hideKeyBoad(this,binding.etSearch)
            binding.etSearch.text?.let {
                getAddressAndShowList(it.toString())
            }
            return@OnEditorActionListener true
        })

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
              /*  p0?.let {
                    if (it.isNotEmpty()) {
                        ivSearchCancel.visibility = View.VISIBLE
                    } else {
                        ivSearchCancel.visibility = View.GONE
                    }
                }*/
            }
        })
        binding.btnSearch.setOnClickListener {
            if (binding.etSearch.text.isEmpty()) {
                binding.etSearch.requestFocus()
            }
        }

        binding.ivChangeMapType.setOnClickListener() {
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
        binding.btnZoomIn!!.setOnClickListener() {
            googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }
        binding.btnZoomOut!!.setOnClickListener() {
            googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }

        binding.btnPickLocationGoogleMap?.setOnClickListener() {

            if (myLoaction != null) {
                AddressUtility.getAddress(
                    this@LocationPickerActivity,
                    myLoaction!!.latitude,
                    myLoaction!!.longitude
                ) {
                    if (it != null && it.size > 0) {
                        callingActivity?.let { callingActivity ->
                            val intent = Intent(
                                this@LocationPickerActivity,
                                callingActivity.className::class.java
                            )
                            intent.putExtra(LocationPicker.PlaceLatitude, myLoaction!!.latitude)
                            intent.putExtra(LocationPicker.PlaceLongitude, myLoaction!!.longitude)
                            intent.putExtra(LocationPicker.PlaceName, it[0])
                            intent.putExtra(LocationPicker.PlaceAddress, it[1])
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                    } else {
                        toast(getString(R.string.no_address_found))
                    }
                }
            }
        }
        binding.ivGetlocation?.setOnClickListener {
            getCurrentLocation()
        }
    }


    private fun getAddressAndShowList(query: String) {
        launch(Dispatchers.Main) {
            var addressList: ArrayList<AddressModel>? = null
            binding.pBSugesstedPlaces.visibility = View.VISIBLE
            async(Dispatchers.IO) {
                addressList = AddressUtility.getAddressListFromLocationName(
                    this@LocationPickerActivity,
                    query
                )
            }.await()
            if (!addressList.isNullOrEmpty()) {
                setResultPlaceOnMap(addressList!![0])
                binding.pBSugesstedPlaces.visibility = View.GONE
                binding. rvSuggestion.visibility = View.VISIBLE
                if (suggestedPlacesAdapter == null) {
                    suggestedPlacesAdapter = SuggestedPlacesAdapter {
                        setResultPlaceOnMap(it)
                    }
                    suggestedPlacesAdapter?.setData(addressList)
                    binding.rvSuggestion.layoutManager = LinearLayoutManager(this@LocationPickerActivity)
                    binding.rvSuggestion.adapter = suggestedPlacesAdapter
                } else {
                    suggestedPlacesAdapter?.setData(addressList)
                    suggestedPlacesAdapter?.notifyDataSetChanged()
                }
            } else {
                binding.pBSugesstedPlaces.visibility = View.GONE
                binding.rvSuggestion.visibility = View.GONE
                toast(getString(R.string.no_address_found))
            }
        }
    }

    private fun setResultPlaceOnMap(it: AddressModel) {
        myLoaction!!.latitude = it.lat
        myLoaction!!.longitude = it.lng
        binding.tvPlaceAdress.text = it.address
        binding.tvPlaceName.text = it.placeName
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
                        getAddressAndShowList(result.get(0))
                    else USafeToast.show(this, getString(R.string.no_address_found))
                }
            }
        }
    }


    override fun onCameraMoveStarted(p0: Int) {
    }

    override fun onCameraIdle() {
        val p0 = googleMap?.cameraPosition
        p0?.let {
            AddressUtility.getAddress(
                this@LocationPickerActivity,
                it.target.latitude,
                it.target.longitude
            ) { addressList ->
                binding.doteMarker?.visibility = View.VISIBLE
                myLoaction?.latitude = it.target.latitude
                myLoaction?.longitude = it.target.longitude
                if (addressList != null && addressList.size > 0) {
                    binding.tvPlaceAdress.text = addressList[1]
                    binding.tvPlaceName.text = addressList[0]
                } else {
                    toast(getString(R.string.no_address_found))
                }
            }
        }
    }

    fun getCurrentLocation() {
        if (PermissionUtils.hasLocationPermissions(this)) {
            if (GPSUtilities.isGPSEnabled(this))
                CurrentLocationHelper(this, true) {
                    currentLoaction = it?.lastLocation
                    setCurrentLocationOnMap()
                }
            else {
                ActivateGps(this, false, RC_GPS)
            }
        } else {
            showPermissionDialog()
        }
    }


    private fun setCurrentLocationOnMap() {
        myLoaction!!.latitude = currentLoaction!!.latitude
        myLoaction!!.longitude = currentLoaction!!.longitude
        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    currentLoaction!!.latitude,
                    currentLoaction!!.longitude
                ), 16f
            )
        )
        AddressUtility.getAddress(
            this,
            currentLoaction!!.latitude,
            currentLoaction!!.longitude
        ) {
            if (it != null && it.size > 0) {
                binding.tvPlaceAdress.text = it[1]
                binding.tvPlaceName.text = it[0]
            } else {
                USafeToast.show(content, getString(R.string.no_address_found))
            }
        }
    }


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main


}
