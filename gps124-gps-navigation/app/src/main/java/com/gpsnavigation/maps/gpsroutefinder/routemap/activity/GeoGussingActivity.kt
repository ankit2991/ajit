package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.android.gpslocation.utils.PermissionUtils
import com.android.gpslocation.utils.requestAllPermissions
import com.example.myapplication.utils.ActivateGps
import com.android.gpslocation.utils.GPSUtilities
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.StreetViewSource
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityGeoGuessingGameBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.LocationHelper
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.dpToPx
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.getRandomLocation
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openSubscriptionPaywall
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets
import org.koin.androidx.viewmodel.ext.android.viewModel


class GeoGussingActivity : BaseActivity(), OnStreetViewPanoramaReadyCallback, View.OnClickListener {

    val mainActivityViewModel: MainActivityViewModel by viewModel()
    var mapFragment: SupportStreetViewPanoramaFragment? = null
    var gMap: StreetViewPanorama? = null
    var streetLocation: LatLng? = null
    val RC_GPS = 5

    internal var dialogPermission: Dialog? = null
    lateinit var binding: ActivityGeoGuessingGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeoGuessingGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()

        init()
        initViews()
    }


    private fun init() {

        dialogPermission = Dialog(this)
        dialogPermission!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogPermission!!.setCancelable(true)
        if (PermissionUtils.hasLocationPermissions(this)) initMap()
        else showPermissionDialog()
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

    private fun initMap() {
        binding.progressBar.isVisible = true
        mapFragment =
            supportFragmentManager.findFragmentById(R.id.streetMap) as SupportStreetViewPanoramaFragment?

        mapFragment?.getStreetViewPanoramaAsync(this)

    }

    override fun onStreetViewPanoramaReady(gMap: StreetViewPanorama) {
        binding.progressBar.isVisible = false
        this.gMap = gMap
        //val longitude = Math.random() * Math.PI * 2
        //val latitude = Math.acos(Math.random() * 2 - 1)
        /*gMap.animateTo(
            StreetViewPanoramaCamera.Builder().zoom(gMap.panoramaCamera.zoom.plus(0.5f) ?: 0.5f)
                .build(), 2000
        )*/

        val sanFrancisco = LatLng(40.4637, 3.7492)
        streetLocation = getRandomLocation(sanFrancisco, 10000000)
        if (PermissionUtils.hasLocationPermissions(this)) {
            if (GPSUtilities.isGPSEnabled(this)) {
                streetLocation?.let { it1 ->
                    gMap.setOnStreetViewPanoramaChangeListener {
                        Log.d("GeoGame", "${it.position}")
                        LocationHelper.guessLatLng = it.position
                    }
                    gMap.setPosition(
                        it1,
                        5000000,
                        StreetViewSource.OUTDOOR
                    )
                }

                /*CurrentLocationHelper(this, true) {
                    streetLocation?.let { it1 ->
                        gMap.setPosition(
                            it1,
                            50000,
                            StreetViewSource.OUTDOOR
                        )
                    }
                    gMap.setOnStreetViewPanoramaChangeListener {

                        LocationHelper.guesslatLng =
                            com.gpsnavigation.maps.gpsroutefinder.routemap.models.LatLng(
                                it.position.latitude, it.position.longitude
                            )
                    }
                }*/
            } else {
                ActivateGps(this, false, RC_GPS)
            }
        }
    }

    private fun showPermissionDialog() {
        dialogPermission?.setContentView(R.layout.layout_permission_dialog)
        val notNow = dialogPermission!!.findViewById(R.id.btn_notnow) as Button
        notNow.setOnClickListener {
            if (dialogPermission!!.isShowing) {
                dialogPermission!!.cancel()
                finish()
            }
        }
        val allow = dialogPermission!!.findViewById(R.id.btn_continue) as Button
        allow.setOnClickListener {

            // Set position with LaLng, radius and source.
            checkLocationPermission {
                if (it) {
                    if (dialogPermission!!.isShowing) {
                        dialogPermission!!.cancel()
                    }
                    initMap()

                } else {
                    if (dialogPermission!!.isShowing) {
                        dialogPermission!!.cancel()
                        finish()
                    }
                }

            }

        }
        dialogPermission?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        if (!isFinishing) {
            dialogPermission!!.show()
        }
    }


    fun checkLocationPermission(onSuccess: ((Boolean) -> Unit)) =
        if (PermissionUtils.hasLocationPermissions(this)) {
            onSuccess.invoke(true)
        } else {
            requestAllPermissions({
                onSuccess.invoke(true)
            }, {
                onSuccess.invoke(false)
            }, *PermissionUtils.locationPerms)

        }


    override fun onDestroy() {
        super.onDestroy()
        TinyDB.getInstance(this).setCurrentTime(0)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101) {
            if (PermissionUtils.hasLocationPermissions(this)) {
                dialogPermission?.cancel()
                initMap()

            }
        }
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
                startActivity(Intent(this, LocationPickerGeoGuessingActivity::class.java))
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
