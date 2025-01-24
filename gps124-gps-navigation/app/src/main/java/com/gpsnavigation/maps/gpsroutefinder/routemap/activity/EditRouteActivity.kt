package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.gpslocation.LocationPickerActivity
import com.android.gpslocation.utils.LocationPicker.Companion.PlaceAddress
import com.android.gpslocation.utils.LocationPicker.Companion.PlaceLatitude
import com.android.gpslocation.utils.LocationPicker.Companion.PlaceLongitude
import com.android.gpslocation.utils.LocationPicker.Companion.PlaceName
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.PlaceModel
import com.gps.maps.navigation.routeplanner.view.adapters.StopsOnEditScreenAdapter
import com.gps.maps.navigation.routeplanner.viewModels.EditRouteActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.REST
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.iconsBits
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityAddRouteBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogConformContinueBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogDeleteBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogRenameRouteBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogStopLimitExceededBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogStopStayTimeBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.LayoutEditStopBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.LatLng
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.RouteModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.*
import com.takisoft.datetimepicker.TimePickerDialog

import org.jetbrains.anko.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


class EditRouteActivity : BaseActivity(), View.OnClickListener {

    val RC_HOME_LOCATION = 1
    val RC_DESTINATION_LOCATION = 2
    val RC_ADD_STOP = 3
    var selectedRoute: RouteModel? = null
    var stopsAdapter: StopsOnEditScreenAdapter? = null
    var isHomeLoationSet = false
    var isDestLoationSet = false
    var isStopAdded = false
    val editRouteActivityViewModel: EditRouteActivityViewModel by viewModel()
    lateinit var binding: ActivityAddRouteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()

        Timber.tag("Edit_Route_activity").i("User enter on edit route screen")
        selectedRoute = editRouteActivityViewModel.getRouteList()
            .get(intent.getStringExtra(Constants.EDIT_ROUTE_FLAG))
        selectedRoute?.let {
            binding.tvRouteName.text = it.routeName
            it.stopsList.forEach {
                if (it.placeType == RFMainActivity.HOME) {
                    binding.tvPlaceName.text = it.placeName ?: ""
                    binding.tvPlaceAddress.text = it.placeAddress ?: ""
                } else if (it.placeType == RFMainActivity.DEST) {
                    binding.tvDestPlaceName.text = it.placeName ?: ""
                    binding.tvDestPlaceAddress.text = it.placeAddress ?: ""
                }
            }
        }

        initViews()
        setViews()

    }


    private fun initViews() {
        binding.btnEditRouteName.setOnClickListener(this)
        binding.btnClearStops.setOnClickListener(this)
        binding.btnAddStops.setOnClickListener(this)
        binding.btnAddMoreStop.setOnClickListener(this)
        binding.btnDoneEditing.setOnClickListener(this)
        binding.btnSelectHomeLocation.setOnClickListener(this)
        binding.btnSelectDestLocation.setOnClickListener(this)
        binding.llSAddress.setOnClickListener(this)
        binding.llDestAddress.setOnClickListener(this)

        binding.toolbar.title.text = getString(R.string.route_planner)
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
                binding.adsContainer.removeAllViews()
                binding.adsContainer.isVisible = false
                binding.adsContainer.tag = null
                (binding.btnDoneEditing.layoutParams as ConstraintLayout.LayoutParams).apply {
                    bottomMargin = 26.dpToPx
                }
                true
            } else {
                binding.adsContainer.isVisible = true
                MaxAdManager.showBannerAds(this, binding.adsContainer)
                (binding.btnDoneEditing.layoutParams as ConstraintLayout.LayoutParams).apply {
                    bottomMargin = 16.dpToPx
                }
                false
            }
    }

    private fun setViews() {
        selectedRoute?.stopsList?.forEach {
            if (it.placeType == RFMainActivity.HOME) {
                binding.tvPlaceName.text = it.placeName ?: ""
                binding.tvPlaceAddress.text = it.placeAddress ?: ""
                binding.tvPlaceAddress.visibility = View.VISIBLE
                isHomeLoationSet = true
            } else if (it.placeType == RFMainActivity.DEST) {
                binding.tvDestPlaceName.text = it.placeName ?: ""
                binding.tvDestPlaceAddress.text = it.placeAddress ?: ""
                binding.tvDestPlaceAddress.visibility = View.VISIBLE
                isDestLoationSet = true
            } else if (it.placeType == RFMainActivity.STOP) {
                isStopAdded = true
            }
        }
        stopsAdapter =
            StopsOnEditScreenAdapter(editRouteActivityViewModel.getDefaultStopStayTime()) { placeModel: PlaceModel, i: Int ->
                showEditStopBSDialog(placeModel, i)
            }
        binding.rvStops.layoutManager = LinearLayoutManager(this)
        stopsAdapter?.setData(getStopListWithoutHomeORDest(selectedRoute?.stopsList))
        binding.rvStops.adapter = stopsAdapter

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.navigation_button -> onBackPressed()
            R.id.premium_button -> openSubscriptionPaywall()
            R.id.btnEditRouteName -> {

                var dialogBinding: DialogRenameRouteBinding
                dialogBinding = DialogRenameRouteBinding.inflate(layoutInflater)
                val dialogView = dialogBinding.root
//                val dialogView = layoutInflater.inflate(dialogBinding.root, null)
                dialogBinding.etRenameRoute.hint = getString(R.string.route_name)
                dialogBinding.etRenameRoute.setText(selectedRoute?.routeName)
                dialogBinding.etRenameRoute.setSelection(dialogBinding.etRenameRoute.text.length)
                dialogBinding.etRenameRoute.requestFocus()
                dialogBinding.btnCancel.setOnClickListener {
                    CustomDialog.getInstance(this)?.dismissDialog()
                }
                dialogBinding.labelHeaderRename.text = getString(R.string.route_name)
                dialogBinding.btnSave.text = getString(R.string.save)
                dialogBinding.btnSave.setOnClickListener {
                    selectedRoute?.routeName = dialogBinding.etRenameRoute.text.toString()
                    binding.tvRouteName.text = selectedRoute?.routeName
                    editRouteActivityViewModel.updateRoute(selectedRoute) {

                    }
                    CustomDialog.getInstance(this)?.dismissDialog()
                }
                CustomDialog.getInstance(this)?.setContentView(dialogView, false)
                    ?.showDialog()
            }

            R.id.btnClearStops -> {

                val dltDialogBinding:DialogDeleteBinding = DialogDeleteBinding.inflate(layoutInflater)
                val deleteView = dltDialogBinding.root
//                val deleteView = layoutInflater.inflate(R.layout.dialog_delete, null)
                dltDialogBinding.labelHeaderDelete.text = getString(R.string.clear_stop)
                dltDialogBinding.etDeleteDescription.text =
                    getString(R.string.are_you_sure_to_remote_all_stop)
                dltDialogBinding.btnCancel.setOnClickListener {
                    CustomDialog.getInstance(this)?.dismissDialog()
                }
                dltDialogBinding.btnDelete.setOnClickListener {
                    binding.btnClearStops.visibility = View.GONE
                    selectedRoute?.let {
                        clearStopList(it.stopsList)
                    }
                    binding.btnAddMoreStop.visibility = View.GONE
                    binding.layoutNoStopAdded.visibility = View.VISIBLE
                    CustomDialog.getInstance(this)?.dismissDialog()
                }
                CustomDialog.getInstance(this)?.setContentView(deleteView, false)?.showDialog()

            }

            R.id.btnAddStops -> {

                if (checkStopLimit()) {
                    val intent = Intent(this, SelectStopActivity::class.java)
                    intent.putExtra(Constants.EDIT_ROUTE_FLAG, selectedRoute?.routeId)
                    startActivityForResult(intent, RC_ADD_STOP)
                }
            }

            R.id.btnAddMoreStop -> {

                if (checkStopLimit()) {
                    val intent = Intent(this, SelectStopActivity::class.java)
                    intent.putExtra(Constants.EDIT_ROUTE_FLAG, selectedRoute?.routeId)
                    startActivityForResult(intent, RC_ADD_STOP)
                }
            }

            R.id.btnSelectHomeLocation -> {

                startActivityForResult(
                    Intent(
                        applicationContext,
                        LocationPickerActivity::class.java
                    ), RC_HOME_LOCATION
                )

            }

            R.id.btnSelectDestLocation -> {

                startActivityForResult(
                    Intent(applicationContext, LocationPickerActivity::class.java),
                    RC_DESTINATION_LOCATION
                )

            }

            R.id.llSAddress -> {
                startActivityForResult(
                    Intent(
                        applicationContext,
                        LocationPickerActivity::class.java
                    ), RC_HOME_LOCATION
                )
            }

            R.id.llDestAddress -> {
                startActivityForResult(
                    Intent(applicationContext, LocationPickerActivity::class.java),
                    RC_DESTINATION_LOCATION
                )
            }

            R.id.btnDoneEditing -> {

                if (!isHomeLoationSet || !isDestLoationSet || !isStopAdded) {
                    val confirmDialogBinding:DialogConformContinueBinding = DialogConformContinueBinding.inflate(layoutInflater)
                    val view = confirmDialogBinding.root
//                    val view = layoutInflater.inflate(R.layout.dialog_conform_continue, null)
                    CustomDialog.getInstance(this)?.setContentView(view, false)?.showDialog()
                    confirmDialogBinding.tvDescription.text =
                        getString(R.string.continue_without_adding_destination)
                    confirmDialogBinding.btnYes.setOnClickListener {
                        if (!isHomeLoationSet && !isDestLoationSet && !isStopAdded)
                            editRouteActivityViewModel.setOptimizeButtonVisibility(false)
                        else editRouteActivityViewModel.setOptimizeButtonVisibility(true)
//                        Log.e("donebtnelse>>>","home"+isHomeLoationSet+"desti"+isDestLoationSet+"isstop"+isHomeLoationSet)
                        CustomDialog.getInstance(this)?.dismissDialog()
                        onBackPressed()

//                        Log.e("end>>>>","home"+isHomeLoationSet+"desti"+isDestLoationSet+"isstop"+isHomeLoationSet)

                    }
                    confirmDialogBinding.btnNo.setOnClickListener {
                        CustomDialog.getInstance(this)?.dismissDialog()
                    }
                } else {
                    editRouteActivityViewModel.setOptimizeButtonVisibility(true)
                    onBackPressed()
                }
                editRouteActivityViewModel.getOnRouteEditObserver().value = true
            }
        }
    }

    override fun onBackPressed() {
        if (!isFinishing) {
            setResult(Activity.RESULT_OK)
            super.onBackPressed()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_HOME_LOCATION && data != null) run {
                try {
                    val placeAddess = data.getStringExtra(PlaceAddress)
                    val placeName = data.getStringExtra(PlaceName)
                    val lat = data.getDoubleExtra(PlaceLatitude, 0.0)
                    val lng = data.getDoubleExtra(PlaceLongitude, 0.0)
                    binding.tvPlaceName.text = placeName
                    binding.tvPlaceAddress.text = placeAddess
                    binding.tvPlaceAddress.visibility = View.VISIBLE
                    val place = PlaceModel(
                        iconsBits[0],
                        placeName,
                        placeAddess,
                        RFMainActivity.HOME,
                        LatLng(
                            lat,
                            lng
                        ),
                        REST
                    )
                    var isHomeExistInList = false
                    for (index in 0 until selectedRoute?.stopsList!!.size) {
                        if (selectedRoute?.stopsList!![index].placeType == RFMainActivity.HOME) {
                            selectedRoute?.stopsList!![index] = place
                            isHomeExistInList = true
                            break
                        }
                    }
                    if (!isHomeExistInList)
                        selectedRoute?.stopsList!!.add(0, place)

                    isHomeLoationSet = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == RC_DESTINATION_LOCATION && data != null) run {
                try {
                    val placeAddess = data.getStringExtra(PlaceAddress)
                    val placeName = data.getStringExtra(PlaceName)
                    val lat = data.getDoubleExtra(PlaceLatitude, 0.0)
                    val lng = data.getDoubleExtra(PlaceLongitude, 0.0)
                    binding.tvDestPlaceName.text = placeName
                    binding.tvDestPlaceAddress.text = placeAddess
                    binding.tvDestPlaceAddress.visibility = View.VISIBLE
                    val place = PlaceModel(
                        iconsBits[2],
                        placeName,
                        placeAddess,
                        RFMainActivity.DEST,
                        LatLng(
                            lat,
                            lng
                        ),
                        REST
                    )
                    var isDestExistInList = false
                    for (index in 0 until selectedRoute?.stopsList!!.size) {
                        if (selectedRoute?.stopsList!![index].placeType == RFMainActivity.DEST) {
                            selectedRoute?.stopsList!![index] = place
                            isDestExistInList = true
                            break
                        }
                    }
                    if (!isDestExistInList)
                        selectedRoute?.stopsList!!.add(selectedRoute?.stopsList?.size!!, place)
                    else selectedRoute?.stopsList!!.set(selectedRoute?.stopsList?.size!! - 1, place)

                    isDestLoationSet = true
                    shakeView(binding.btnAddStops, 4000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == RC_ADD_STOP) run {
                try {
                    shakeView(binding.btnAddMoreStop, 5000)
                    binding.layoutNoStopAdded.visibility = View.GONE
                    binding.btnAddMoreStop.visibility = View.VISIBLE
                    binding.btnClearStops.visibility = View.VISIBLE
                    selectedRoute = editRouteActivityViewModel.getRouteList().get(
                        intent.getStringExtra(Constants.EDIT_ROUTE_FLAG)
                    )
                    stopsAdapter?.setData(getStopListWithoutHomeORDest(selectedRoute?.stopsList))
                    isStopAdded = true
                    shakeView(binding.btnDoneEditing, 4000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getStopListWithoutHomeORDest(stopsList: ArrayList<PlaceModel>?): ArrayList<PlaceModel>? {
        val stopListWithoutHomeOrDest = ArrayList<PlaceModel>()
        stopsList?.let {
            stopListWithoutHomeOrDest.addAll(stopsList)
            stopsList.forEach {
                if (it.placeType == RFMainActivity.HOME || it.placeType == RFMainActivity.DEST)
                    stopListWithoutHomeOrDest.remove(it)
            }
            if (stopListWithoutHomeOrDest.isNotEmpty()) {
                binding.layoutNoStopAdded.visibility = View.GONE
                binding.btnClearStops.visibility = View.VISIBLE
                binding.btnAddMoreStop.visibility = View.VISIBLE
            } else {
                binding.layoutNoStopAdded.visibility = View.VISIBLE
                binding.btnClearStops.visibility = View.GONE
                binding.btnAddMoreStop.visibility = View.GONE
            }
        }
        return stopListWithoutHomeOrDest
    }

    private fun clearStopList(stopsList: ArrayList<PlaceModel>?) {
        if (stopsList == null)
            return
        val tempList = ArrayList<PlaceModel>()
        tempList.addAll(stopsList)
        tempList.forEach {
            if (it.placeType == RFMainActivity.STOP)
                stopsList.remove(it)
        }
        stopsList.forEach {
            if (it.leg != null)
                it.leg = null
        }

        stopsAdapter?.setData(ArrayList<PlaceModel>())
        isStopAdded = false
    }


    @SuppressLint("SetTextI18n")
    private fun showEditStopBSDialog(stopModel: PlaceModel?, pos: Int) {
        var arrivalTimeFrom: String? = null
        var arrivalTimeTo: String? = null
        val editStopBinding:LayoutEditStopBinding = LayoutEditStopBinding.inflate(layoutInflater)
        val view = editStopBinding.root
//        val view = layoutInflater.inflate(R.layout.layout_edit_stop, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        editStopBinding.btnDeleteStop.visibility = View.VISIBLE
        editStopBinding.tvStopPlaceName.text = stopModel?.placeName ?: ""
        if (stopModel?.isASAP!!) {
            editStopBinding.btnASAP.requestFocus()
            changeNormalAndASAPButtonStatus(true, editStopBinding.btnNormal, editStopBinding.btnASAP)
        } else {
            editStopBinding.btnNormal.requestFocus()
            changeNormalAndASAPButtonStatus(false, editStopBinding.btnNormal, editStopBinding.btnASAP)
        }
        changeDoneButtonStatus(true, editStopBinding.btnDone)
        editStopBinding.tvArrivalTimeFrom.text = stopModel.leg?.departureTime?.text
        editStopBinding.tvArrivalTimeTo.text = stopModel.leg?.arrivalTime?.text
        editStopBinding.tvDefaultStopDuration.text = stopModel.stopStayTime.toString()
        editStopBinding.etAddNotes.setText(stopModel.stopNotes)
        editStopBinding.etAddNotes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count <= 0)
                    changeDoneButtonStatus(true, editStopBinding.btnDone)
                else changeDoneButtonStatus(false, editStopBinding.btnDone)
            }
        })
        editStopBinding.btnNormal.setOnClickListener {
            changeNormalAndASAPButtonStatus(false, editStopBinding.btnNormal, editStopBinding.btnASAP)
            changeDoneButtonStatus(false, editStopBinding.btnDone)
            stopModel.isASAP = false
        }

        editStopBinding.btnASAP.setOnClickListener {
            changeNormalAndASAPButtonStatus(true, editStopBinding.btnNormal, editStopBinding.btnASAP)
            changeDoneButtonStatus(false, editStopBinding.btnDone)
            stopModel.isASAP = true
        }

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
        editStopBinding.tvArrivalTimeFrom.setOnClickListener {
            val cal = Calendar.getInstance()
            val tpd = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    changeDoneButtonStatus(false, editStopBinding.btnDone)
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
                    changeDoneButtonStatus(false, editStopBinding.btnDone)
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
            val stayTimeBinding: DialogStopStayTimeBinding = DialogStopStayTimeBinding.inflate(layoutInflater)
            val viewStayTime = stayTimeBinding.root
//            val viewStayTime = layoutInflater.inflate(R.layout.dialog_stop_stay_time, null)
            val etTime = stayTimeBinding.etTime
            val btnSetTime = stayTimeBinding.btnSetTime
            val btnCancel = stayTimeBinding.btnCancel
            etTime.hint = stopModel.stopStayTime.toString() + getString(R.string.minutes)
            CustomDialog.getInstance(this)?.setContentView(viewStayTime, false)?.showDialog()
            btnSetTime.setOnClickListener {
                if (etTime.text.isEmpty()) {
                    etTime.error = getString(R.string.enter_time)
                    etTime.requestFocus()
                } else {
                    if (Integer.parseInt(etTime.text.toString()) > 60 || Integer.parseInt(etTime.text.toString()) < 1)
                        btnSetTime.context.toast(getString(R.string.minutes_mustbe_between_1_to_60))
                    else {
                        changeDoneButtonStatus(false, editStopBinding.btnDone)
                        editStopBinding.tvDefaultStopDuration.text =
                            etTime.text.toString() + "  " + getString(R.string.minutes)
                        stopModel.stopStayTime = etTime.text.toString().toInt()
                        CustomDialog.getInstance(this)?.dismissDialog()
                    }
                }
            }
            btnCancel.setOnClickListener {
                editStopBinding.tvDefaultStopDuration.text = "1" + " " + getString(R.string.minutes)
                CustomDialog.getInstance(this)?.dismissDialog()
            }
        }
        editStopBinding.btnDeleteStop.setOnClickListener {
            LoadingDialog.getInstance(this)?.showDialog(null)
            selectedRoute?.stopsList?.remove(stopModel)
            stopsAdapter?.setData(getStopListWithoutHomeORDest(selectedRoute?.stopsList))
            editRouteActivityViewModel.updateStopList(
                selectedRoute
            )
            LoadingDialog.getInstance(this)?.dismissDialog()
            dialog.dismiss()

        }

        editStopBinding.btnDone.setOnClickListener {
            LoadingDialog.getInstance(this)?.showDialog(null)
            stopModel.stopNotes = editStopBinding.etAddNotes.text.toString()
            stopModel.userSelectedArrivalTime = "${arrivalTimeFrom}-${arrivalTimeTo}"
            selectedRoute?.stopsList!!.sort()
            stopsAdapter?.setData(getStopListWithoutHomeORDest(selectedRoute?.stopsList))
            editRouteActivityViewModel.updateStopList(
                selectedRoute
            )
            LoadingDialog.getInstance(this)?.dismissDialog()
            dialog.dismiss()
        }

        dialog.show()
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

    fun changeDoneButtonStatus(isChangeToAdded: Boolean, btnDone: Button) {
        if (isChangeToAdded) {
            btnDone.text = getString(R.string.added)
            btnDone.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            btnDone.setBackgroundResource(R.color.transpresnt)
            btnDone.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                getDrawable(R.drawable.ic_added),
                null
            )
        } else {
            btnDone.text = getString(R.string.done)
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

    private fun checkStopLimit(): Boolean {
        var limit = 27
        if (isHomeLoationSet && isDestLoationSet)
            limit = 27
        else if (isHomeLoationSet || isDestLoationSet)
            limit = 25
        else limit = 25
        if (editRouteActivityViewModel.getSelectedRoute() != null && editRouteActivityViewModel.getSelectedRoute()!!.stopsList.size >= limit) {
            val limitExceededBinding: DialogStopLimitExceededBinding = DialogStopLimitExceededBinding.inflate(layoutInflater)
            val diaView = limitExceededBinding.root

//            val diaView = layoutInflater.inflate(R.layout.dialog_stop_limit_exceeded, null)
            limitExceededBinding.btnUnderstand.setOnClickListener {
                CustomDialog.getInstance(this)?.dismissDialog()
            }
            CustomDialog.getInstance(this)?.setContentView(diaView, true)?.showDialog()
            return false
        } else return true
    }
    private fun setInset() {
        binding.guidelineBottom.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = systemBarsInsets(insets)

            binding.guidelineBottom.setGuidelineEnd(systemBarsInsets.bottom)

            insets
        }
    }
}

