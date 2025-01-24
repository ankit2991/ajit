package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.android.gpslocation.LocationPickerActivity
import com.android.gpslocation.utils.LocationPicker
import com.android.gpslocation.utils.MapUtils
import com.google.android.gms.maps.model.LatLng
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.*
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.MyConstant
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityRouteFinderBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.LoadingDialog
import org.koin.androidx.viewmodel.ext.android.viewModel


class RouteFinderActivity : BaseActivity(), View.OnClickListener {

    val mainActivityViewModel: MainActivityViewModel by viewModel()
    val RC_SOURCE_LOCATION = 1
    val RC_DESTINATION_LOCATION = 2
    var sourceLatLng: LatLng? = null
    var destinationLatLng: LatLng? = null
    lateinit var binding:ActivityRouteFinderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteFinderBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_route_finder)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.route_finder)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setActions()
        LoadingDialog.getInstance(this@RouteFinderActivity)?.showDialog(null)
        Handler().postDelayed(Runnable {
            LoadingDialog.getInstance(this@RouteFinderActivity)?.dismissDialog()
        }, 500)

    }


    private fun setActions() {
        binding.cvSourceLocation.setOnClickListener(this)
        binding.cvDestinationLocation.setOnClickListener(this)
        binding.btnFindRoute.setOnClickListener(this)
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.cvSourceLocation -> {
                startActivityForResult(
                    Intent(this, LocationPickerActivity::class.java),
                    RC_SOURCE_LOCATION
                )
            }
            R.id.cvDestinationLocation -> {
                startActivityForResult(
                    Intent(this, LocationPickerActivity::class.java),
                    RC_DESTINATION_LOCATION
                )
            }
            R.id.btnFindRoute -> {
                LoadingDialog.getInstance(this@RouteFinderActivity)?.showDialog(null)
                Handler().postDelayed({
                    LoadingDialog.getInstance(this@RouteFinderActivity)?.dismissDialog()
                    navigate()
                }, 500)


            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        TinyDB.getInstance(this).setCurrentTime(0)
    }


    private fun navigate() {
        if (sourceLatLng == null || destinationLatLng == null) {
            Toast.makeText(this, "Select source and destination", Toast.LENGTH_LONG).show()
        } else {
            MyConstant.IS_APPOPEN_BG_IMPLICIT = true
            MapUtils.openNavigationBetweenTwoPlaces(this, sourceLatLng!!, destinationLatLng!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == RC_SOURCE_LOCATION && data != null) {
            val placeAddess = data?.getStringExtra(LocationPicker.PlaceAddress)
            val placeName = data?.getStringExtra(LocationPicker.PlaceName)
            val lat = data?.getDoubleExtra(LocationPicker.PlaceLatitude, 0.0)
            val lng = data?.getDoubleExtra(LocationPicker.PlaceLongitude, 0.0)
            sourceLatLng = LatLng(lat!!, lng!!)
            binding.sName.text = placeName
            binding.sAddress.text = placeAddess
        } else if (resultCode == Activity.RESULT_OK && requestCode == RC_DESTINATION_LOCATION && data != null) {
            val placeAddess = data?.getStringExtra(LocationPicker.PlaceAddress)
            val placeName = data?.getStringExtra(LocationPicker.PlaceName)
            val lat = data?.getDoubleExtra(LocationPicker.PlaceLatitude, 0.0)
            val lng = data?.getDoubleExtra(LocationPicker.PlaceLongitude, 0.0)
            destinationLatLng = LatLng(lat!!, lng!!)
            binding.dName.text = placeName
            binding.dAddress.text = placeAddess
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }
}
