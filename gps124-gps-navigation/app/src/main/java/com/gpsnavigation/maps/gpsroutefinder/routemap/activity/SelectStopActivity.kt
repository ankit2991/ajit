
package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.gpslocation.AddressModel
import com.android.gpslocation.SuggestedPlacesAdapter
import com.android.gpslocation.utils.*
import com.example.myapplication.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.PlaceModel
import com.gps.maps.navigation.routeplanner.viewModels.EditRouteActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.RFMainActivity.Companion.DEST
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.RFMainActivity.Companion.STOP
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.REST
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.iconsBits
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityStopPickerBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogStopStayTimeBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.LayoutEditStopBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.CustomDialog
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.LoadingDialog
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.getTimeInAMPMFormat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.coroutines.CoroutineContext


class SelectStopActivity : BaseActivity(), OnMapReadyCallback, CoroutineScope,
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
    var selectedRoute: RouteModel? = null
    val editRouteActivityViewModel: EditRouteActivityViewModel by viewModel()
    lateinit var binding:ActivityStopPickerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStopPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_stop_picker)
        Places.initialize(applicationContext, getString(com.android.gpslocation.R.string.google_maps_key_part1) +getString(com.android.gpslocation.R.string.google_maps_key_part2) + getString(com.android.gpslocation.R.string.google_maps_key_part3) + getString(com.android.gpslocation.R.string.google_maps_key_part4))
        selectedRoute =
            editRouteActivityViewModel.getRouteList()
                .get(intent.getStringExtra(Constants.EDIT_ROUTE_FLAG))
        init()
        setActions()
    }


    private fun init() {
        mapFragment = supportFragmentManager.findFragmentById(com.android.gpslocation.R.id.map) as SupportMapFragment
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
        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            KeyboardUtil.hideKeyBoad(this,binding.etSearch)
            binding.etSearch.text?.let {
                getAddressAndShowList(it.toString())
            }
            return@OnEditorActionListener true
        })
        binding.etSearch.setOnFocusChangeListener{ view: View, hasFocus: Boolean ->
           if (hasFocus){
               binding.ivSearchCancel.visibility = View.VISIBLE
               binding.etSearch.hint = ""
               binding.btnExit.visibility = View.GONE
               binding.btnSearch.visibility = View.GONE
               KeyboardUtil.openKeyBoad(this@SelectStopActivity,binding.etSearch)
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
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               /* p0?.let {
                    if (it.isNotEmpty()){
                        ivSearchCancel.visibility = View.VISIBLE
                    } else {
                        ivSearchCancel.visibility = View.GONE
                    }
                }*/
            }
        })
        binding.btnSearch.setOnClickListener {
            binding.etSearch.requestFocus()
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
                            com.android.gpslocation.R.raw.night_mode_map
                        )
                    )
                    selectedMapStype = NIGHT
                } else if (selectedMapStype == NIGHT) {
                    googleMap?.setMapType(DEFAULT)
                    googleMap?.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(
                            this,
                            com.android.gpslocation.R.raw.normal_mode_map
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
                    this@SelectStopActivity,
                    myLoaction!!.latitude,
                    myLoaction!!.longitude
                ) {
                    if (it != null && it.size > 0) {
                        showEditStopBSDialog(it, myLoaction!!)
                    } else {
                        toast(getString(com.android.gpslocation.R.string.no_address_found))
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
                    this@SelectStopActivity,
                    query
                )
            }.await()
            if (!addressList.isNullOrEmpty()) {
                setResultPlaceOnMap(addressList!![0])
                binding.pBSugesstedPlaces.visibility = View.GONE
                binding.rvSuggestion.visibility = View.VISIBLE
                if (suggestedPlacesAdapter == null) {
                    suggestedPlacesAdapter = SuggestedPlacesAdapter {
                        setResultPlaceOnMap(it)
                    }
                    suggestedPlacesAdapter?.setData(addressList)
                    binding.rvSuggestion.layoutManager = LinearLayoutManager(this@SelectStopActivity)
                    binding.rvSuggestion.adapter = suggestedPlacesAdapter
                } else {
                    suggestedPlacesAdapter?.setData(addressList)
                    suggestedPlacesAdapter?.notifyDataSetChanged()
                }
            } else {
                binding.pBSugesstedPlaces.visibility = View.GONE
                binding.rvSuggestion.visibility = View.GONE
                toast(getString(com.android.gpslocation.R.string.no_address_found))
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
            it.setPadding(0, 0, 0, resources.getDimensionPixelSize(com.android.gpslocation.R.dimen._20sdp))
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
        dialogPermission!!.setContentView(com.android.gpslocation.R.layout.layout_permission_dialog)
        val notNow = dialogPermission!!.findViewById(com.android.gpslocation.R.id.btn_notnow) as Button
        notNow.setOnClickListener {
            if (dialogPermission!!.isShowing) {
                dialogPermission!!.cancel()
            }
        }
        val allow = dialogPermission!!.findViewById(com.android.gpslocation.R.id.btn_continue) as Button
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
                    else USafeToast.show(this, getString(com.android.gpslocation.R.string.no_address_found))
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
                this@SelectStopActivity,
                it.target.latitude,
                it.target.longitude
            ) {addressList->
                binding.doteMarker?.visibility = View.VISIBLE
                myLoaction?.latitude = it.target.latitude
                myLoaction?.longitude = it.target.longitude
                if (addressList != null && addressList.size > 0) {
                    binding.tvPlaceAdress.text = addressList[1]
                    binding.tvPlaceName.text = addressList[0]
                } else {
                    toast(getString(com.android.gpslocation.R.string.no_address_found))
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
                USafeToast.show(content, getString(com.android.gpslocation.R.string.no_address_found))
            }
        }
    }


    private fun showEditStopBSDialog(listOFPlaces: ArrayList<String>, myLoaction: Location) {

        val place = PlaceModel(
            iconsBits[1],
            listOFPlaces[0],
            listOFPlaces[1],
            STOP,
            com.gpsnavigation.maps.gpsroutefinder.routemap.models.LatLng(
                myLoaction.latitude,
                myLoaction.longitude
            ),
            REST
        )
        var arrivalTimeFrom: String? = null
        var arrivalTimeTo: String? = null
        var arrivalTime: String? = null
        val editStopBinding:LayoutEditStopBinding = LayoutEditStopBinding.inflate(layoutInflater)
        val view = editStopBinding.root
//        val view = layoutInflater.inflate(R.layout.layout_edit_stop, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        editStopBinding.tvStopPlaceName.text = listOFPlaces[0]
        editStopBinding.tvDefaultStopDuration.hint =
            editRouteActivityViewModel.getDefaultStopStayTime().toString() + getString(R.string.minutes)
        editStopBinding.etAddNotes.setText(place.stopNotes)
        editStopBinding.etAddNotes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count <= 0)
                    changeDoneButtonStatus(true, editStopBinding.btnDone)
                else changeDoneButtonStatus(false, editStopBinding.btnDone)
            }
        })
        dialog.setOnShowListener {
            val d = dialog
            val coordinatorLayout = editStopBinding.rootViewBSDialog
            val bottomSheetInternal = editStopBinding.bsInternal
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetInternal)
            BottomSheetBehavior.from(coordinatorLayout.parent as View).peekHeight =
                bottomSheetInternal.height
            bottomSheetBehavior.peekHeight = bottomSheetInternal.height
            coordinatorLayout.parent.requestLayout()
        }

        if (place.isASAP) {
            editStopBinding.btnASAP.requestFocus()
            changeNormalAndASAPButtonStatus(true, editStopBinding.btnNormal, editStopBinding.btnASAP)
        } else {
            editStopBinding.btnNormal.requestFocus()
            changeNormalAndASAPButtonStatus(false, editStopBinding.btnNormal, editStopBinding.btnASAP)
        }

        editStopBinding.btnNormal.setOnClickListener {
            place.isASAP = false
            changeNormalAndASAPButtonStatus(false, editStopBinding.btnNormal, editStopBinding.btnASAP)
        }

        editStopBinding.btnASAP.setOnClickListener {
            place.isASAP = true
            changeNormalAndASAPButtonStatus(true, editStopBinding.btnNormal, editStopBinding.btnASAP)
        }
        editStopBinding.tvArrivalTimeFrom.setOnClickListener {
            val cal = Calendar.getInstance()
            val tpd = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    var hour = hourOfDay
                    if (hour == 0)
                        hour = 12
                    arrivalTimeFrom = getTimeInAMPMFormat(hourOfDay, minute)
                    editStopBinding.tvArrivalTimeFrom.text = arrivalTimeFrom
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            )
            tpd.show()
        }
        editStopBinding.tvArrivalTimeTo.setOnClickListener {
            val cal = Calendar.getInstance()
            val tpd = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    var hour = hourOfDay
                    if (hour == 0)
                        hour = 12
                    arrivalTimeTo = getTimeInAMPMFormat(hourOfDay, minute)
                    editStopBinding.tvArrivalTimeTo.text = arrivalTimeTo
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
            )
            tpd.show()
        }

        editStopBinding.tvDefaultStopDuration.setOnClickListener {
            val stopStayTimeBinding:DialogStopStayTimeBinding = DialogStopStayTimeBinding.inflate(layoutInflater)
            val viewStayTime = stopStayTimeBinding.root
//            val viewStayTime = layoutInflater.inflate(R.layout.dialog_stop_stay_time, null)
            val etTime = stopStayTimeBinding.etTime
            val btnSetTime = stopStayTimeBinding.btnSetTime
            val btnCancel = stopStayTimeBinding.btnCancel
            CustomDialog.getInstance(this)?.setContentView(viewStayTime, false)?.showDialog()
            btnSetTime.setOnClickListener {
                if (etTime.text.isEmpty()) {
                    etTime.error = "Enter time"
                    etTime.requestFocus()
                } else {
                    if (Integer.parseInt(etTime.text.toString()) > 60 || Integer.parseInt(etTime.text.toString()) < 1)
                        btnSetTime.context.toast("Minutes must be in range between 1 to 60!")
                    else {
                        editStopBinding.tvDefaultStopDuration.text =
                            etTime.text.toString() + "  " + getString(R.string.minutes)
                        place.stopStayTime = etTime.text.toString().toInt()
                        CustomDialog.getInstance(this)?.dismissDialog()
                    }
                }
            }
            btnCancel.setOnClickListener {
                CustomDialog.getInstance(this)?.dismissDialog()
            }
        }
        editStopBinding.btnDeleteStop.visibility = View.VISIBLE
        editStopBinding.btnDeleteStop.text = getString(R.string.cancel)
        editStopBinding.btnDeleteStop.setTextColor(ContextCompat.getColor(this,R.color.red_200))
        editStopBinding.btnDeleteStop.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_cancel_red,0,0,0)
        editStopBinding.btnDeleteStop.setOnClickListener {
            dialog.dismiss()
        }
        editStopBinding.btnDone.setOnClickListener {
            place.stopNotes = editStopBinding.etAddNotes.text.toString()
            place.userSelectedArrivalTime = "${arrivalTimeFrom}-${arrivalTimeTo}"
            LoadingDialog.getInstance(this)?.showDialog(null)
            selectedRoute?.let {
                if (isDestLoationExist())
                    it.stopsList?.add(it.stopsList!!.size - 1, place)
                else it.stopsList.add(place)
            }
            selectedRoute?.stopsList?.sort()
            selectedRoute?.let {
                editRouteActivityViewModel.updateStopList(selectedRoute)
            }
            LoadingDialog.getInstance(this)?.dismissDialog()
            dialog.dismiss()
            setResult(Activity.RESULT_OK)
            finish()
        }

        dialog.show()
    }

    fun changeDoneButtonStatus(isChangeToAdded: Boolean, btnDone: Button) {
        if (isChangeToAdded) {
            btnDone.text = "Added"
            btnDone.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            btnDone.setBackgroundResource(R.color.transpresnt)
            btnDone.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                this.getDrawable(R.drawable.ic_added),
                null
            )
        } else {
            btnDone.text = "Done"
            btnDone.setTextColor(ContextCompat.getColor(this, R.color.white))
            btnDone.setBackgroundResource(R.drawable.bg_blue_solid_rounded_25sdp)
            btnDone.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
        }
    }

    fun changeNormalAndASAPButtonStatus(isASAPButton: Boolean, normal: Button, asap: Button) {
        if (isASAPButton) {
            normal.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            asap.setTextColor(ContextCompat.getColor(this, R.color.white))
            normal.setBackgroundResource(R.drawable.bg_blue_corner_rounded_25sdp)
            asap.setBackgroundResource(R.drawable.bg_blue_solid_rounded_25sdp)
        } else {
            normal.setTextColor(ContextCompat.getColor(this, R.color.white))
            asap.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            normal.setBackgroundResource(R.drawable.bg_blue_solid_rounded_25sdp)
            asap.setBackgroundResource(R.drawable.bg_blue_corner_rounded_25sdp)
        }
    }

    private fun isDestLoationExist(): Boolean {
        selectedRoute?.let {
            it.stopsList?.let { stopList ->
                stopList.forEach { stop ->
                    if (stop.placeType == DEST)
                        return true
                }
            }
        }
        return false
    }



    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main



}




