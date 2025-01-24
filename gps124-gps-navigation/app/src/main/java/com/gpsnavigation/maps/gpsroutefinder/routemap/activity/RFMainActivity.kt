package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.IntDef
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.android.gpslocation.utils.PermissionUtils
import com.android.gpslocation.utils.requestAllPermissions
import com.example.myapplication.utils.ActivateGps
import com.android.gpslocation.utils.GPSUtilities
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.MainPagerAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityRfMainBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogGpsNotEnabledBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.DialogLocationPermissionBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.CustomDialog
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openSubscriptionPaywall
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets

import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class RFMainActivity : BaseActivity(), View.OnClickListener {


    val RC_GPS_ENABLE = 2

    companion object {
        @IntDef(HOME, STOP, DEST)
        @Retention(AnnotationRetention.SOURCE)
        annotation class PlaceType

        const val HOME = 1
        const val STOP = 2
        const val DEST = 3
    }

    val mainActivityViewModel: MainActivityViewModel by viewModel()
    var isUserSubscribed = false

    enum class MethodsTrack {
        CHECK_USER_AUTHENTICATION
    }
    lateinit var binding:ActivityRfMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRfMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()

        Timber.tag("Main_activity_onCreate").i("User enter on main screen")
        mainActivityViewModel.isUserSubscribed().observe(this, Observer {
            isUserSubscribed = it
        })
        checkLocationPermission()
    }


    private fun showGPSDisabledDialog() {
        val gpsNotEnabledBinding:DialogGpsNotEnabledBinding = DialogGpsNotEnabledBinding.inflate(layoutInflater)
        val diaView = gpsNotEnabledBinding.root
//        val diaView = layoutInflater.inflate(R.layout.dialog_gps_not_enabled, null)
        gpsNotEnabledBinding.btnOK.setOnClickListener {
            ActivateGps(this, true, RC_GPS_ENABLE)
        }
        CustomDialog.getInstance(this)?.setContentView(diaView, false)?.showDialog()
    }

    private fun checkLocationPermission() {
        if (!GPSUtilities.isGPSEnabled(this))
            showGPSDisabledDialog()
        else {
            if (PermissionUtils.hasLocationPermissions(this)) {
                initActivityContents()
            } else {
                showLocationPermissionDialog()
            }
        }
    }

    private fun showLocationPermissionDialog() {
        val locationPermissionBinding:DialogLocationPermissionBinding = DialogLocationPermissionBinding.inflate(layoutInflater)
        val diaView = locationPermissionBinding.root
//        val diaView = layoutInflater.inflate(R.layout.dialog_location_permission, null)
        locationPermissionBinding.btnOK.setOnClickListener {
            CustomDialog.getInstance(this)?.dismissDialog()
            requestAllPermissions(this, {
                if (it == android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    checkLocationPermission()
            }, {
            }, *PermissionUtils.locationPerms)
        }
        CustomDialog.getInstance(this)?.setContentView(diaView, false)?.showDialog()
    }


    private fun initActivityContents() {
        initViews()
        binding.viewPagerMain.adapter = MainPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPagerMain) { tab, position ->
            when (position) {
                0 ->{
                    tab.text = getString(R.string.map)
                }
                1 -> {
                    tab.text = getString(R.string.routes)
                }
                else -> tab.text = getString(R.string.routes)
            }
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object :OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.toolbar.title.text = getString(R.string.navigation_activity_title)
                    }
                    1 -> {
                        binding.toolbar.title.text = getString(R.string.routes)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        for (i in 0 until binding.tabLayout.tabCount) {
            if (i == 0) {
                val tab = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
                val p = tab.layoutParams as MarginLayoutParams
                p.setMargins(0, 0, resources.getDimensionPixelOffset(R.dimen._7sdp), 0)
                tab.requestLayout()
            } else {
                val tab = (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(i)
                val p = tab.layoutParams as MarginLayoutParams
                p.setMargins(resources.getDimensionPixelOffset(R.dimen._7sdp), 0, 0, 0)
                tab.requestLayout()
            }

        }

    }

    private fun initViews() {
        binding.toolbar.title.text = getString(R.string.navigation_activity_title)
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium

        if (!TinyDB.getInstance(this).isPremium) {
            binding.adsContainer.isVisible = true
            MaxAdManager.showBannerAds(this, binding.adsContainer)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_GPS_ENABLE -> {
                if (resultCode == Activity.RESULT_OK) {
                    CustomDialog.getInstance(this)?.dismissDialog()
                    checkLocationPermission()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (binding.viewPagerMain.currentItem == 1) {
            binding.viewPagerMain.currentItem = 0
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.premiumButton.isInvisible =
            if (TinyDB.getInstance(this).isPremium) {
                binding.adsContainer.removeAllViews()
                binding.adsContainer.isVisible = false
                binding.adsContainer.tag = null
                true
            } else {
                binding.adsContainer.isVisible = true
                MaxAdManager.showBannerAds(this, binding.adsContainer)
                false
            }
    }

    @Suppress("DEPRECATION")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.navigation_button -> onBackPressed()
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


