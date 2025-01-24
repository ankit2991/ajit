package com.gpsnavigation.maps.gpsroutefinder.routemap.activity


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.android.gpslocation.utils.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.*
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.MyConstant
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityMyLocationBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.hideStatusBar
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openSubscriptionPaywall
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets
import com.kotlinpermissions.KotlinPermissions
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyLocationActivity : BaseActivity(), View.OnClickListener {

    internal var dialogPermission: Dialog? = null
    var mClient: ApiClientHelper? = null
    var currentLocation: Location? = null
    var context: Context? = null
    val mainActivityViewModel: MainActivityViewModel by viewModel()
    lateinit var binding:ActivityMyLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()
        setListeners()

        binding.toolbar.title.text = getString(R.string.my_location)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium

        MaxAdManager.showNativeAds(this,  binding.nativeAdsContainer)
    }


    private fun setListeners() {
        binding.copyBtn.setOnClickListener(this)
        binding.shareBtn.setOnClickListener(this)
        binding.showMapBtn.setOnClickListener(this)
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.hasLocationPermissions(this)) {
            if (ApiClientHelper.isLocationEnabled(this)) {
                if (currentLocation == null) {
                    prepareApiClientHelper()
                } else {
                    setCurrentAddressInTextField(currentLocation!!)
                }
            } else {
                ApiClientHelper.showLocationSettingDialog(this, 0)
            }
        } else {
            showLocationPermissionDialog()
        }
        binding.toolbar.premiumButton.isInvisible =
            if (TinyDB.getInstance(this).isPremium) {
                binding.nativeAdsContainer.isVisible = false
                binding.nativeAdsContainer.tag = null
                true
            } else {
                MaxAdManager.showNativeAds(this,  binding.nativeAdsContainer)
                false
            }
        hideStatusBar()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        TinyDB.getInstance(this).setCurrentTime(0)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.navigation_button -> onBackPressed()
            R.id.premium_button -> openSubscriptionPaywall()
            R.id.copy_btn -> {

                if (binding.tvCurrentLocation.text.isEmpty())
                    USafeToast.show(this, "No location selected")
                else {
                    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val cData = ClipData.newPlainText("text", binding.tvCurrentLocation.text)
                    cm.setPrimaryClip(cData)
                    Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.share_btn -> {

                if (binding.tvCurrentLocation.text.isEmpty())
                    USafeToast.show(this, "No location selected")
                else {
                    val shreText =
                        "Hy check out my address:\n${binding.tvCurrentLocation.text}\n Address shared by:\n https://play.google.com/store/apps/details?id=${packageName}"
                    UrlOpener.share(this, shreText)
                }
            }
            R.id.show_map_btn -> {

                if (currentLocation == null)
                    USafeToast.show(this, "No location selected")
                else {
                    MyConstant.IS_APPOPEN_BG_IMPLICIT = true
                    MapUtils.showLocationOnMap(
                        this,
                        LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
                    )
                }

            }
        }
    }

    private fun showLocationPermissionDialog() {
        dialogPermission = Dialog(this)
        dialogPermission!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogPermission!!.setCancelable(true)
        dialogPermission!!.setContentView(com.android.gpslocation.R.layout.layout_permission_dialog)
        val notNow =
            dialogPermission!!.findViewById(com.android.gpslocation.R.id.btn_notnow) as Button
        notNow.setOnClickListener {
            if (dialogPermission!!.isShowing) {
                dialogPermission!!.cancel()
            }
        }
        val allow =
            dialogPermission!!.findViewById(com.android.gpslocation.R.id.btn_continue) as Button
        allow.setOnClickListener {
            if (dialogPermission!!.isShowing) {
                dialogPermission!!.cancel()
            }
            allowLocationPermission()
        }
        dialogPermission!!.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent)
        if (!isFinishing()) {
            dialogPermission!!.show()
        }
    }

    private fun allowLocationPermission() {
        KotlinPermissions.with(this)
            .permissions(*PermissionUtils.locationPerms)
            .onAccepted {
                prepareApiClientHelper()
            }.onDenied {
                finish()
            }
            .onForeverDenied {
                openSettings()
            }
            .ask()
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
                currentLocation = Location("")
                currentLocation!!.latitude = lastLocation.latitude
                currentLocation!!.longitude = lastLocation.longitude
                setCurrentAddressInTextField(lastLocation)
            }
        }
    }

    private fun setCurrentAddressInTextField(lastLocation: Location) {
        doAsync {
            val listOFPlaces = ApiClientHelper.getAddress(
                this@MyLocationActivity,
                lastLocation.latitude,
                lastLocation.longitude
            )
            uiThread {
                if (listOFPlaces != null && listOFPlaces.size > 0) {
                    binding.tvCurrentLocation.text = listOFPlaces.get(1)
                } else {
                    USafeToast.show(this@MyLocationActivity, "No Address Found")
                }
            }
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

    private fun setInset() {
        binding.nativeAdsContainer.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = systemBarsInsets(insets)
            (view.layoutParams as ConstraintLayout.LayoutParams).apply {
                bottomMargin = systemBarsInsets.bottom
            }

            insets
        }
    }
}
