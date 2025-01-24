package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.android.gpslocation.utils.UrlOpener
import com.gpsnavigation.maps.gpsroutefinder.routemap.BuildConfig
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivitySettingsBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.Constants.SUPPORT_EMAIL
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.hideStatusBar
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openEmailComposer
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openSubscriptionPaywall
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets

class SettingsActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()
        initView()
    }

    private fun initView() {
        binding.toolbar.title.text = getString(R.string.settings)
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium

        binding.address.setOnClickListener(this)
        binding.language.setOnClickListener(this)
        binding.rateUs.setOnClickListener(this)
        binding.feedback.setOnClickListener(this)

        MaxAdManager.showNativeAds(this, binding.nativeAdsContainer)
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.premiumButton.isInvisible =
            if (TinyDB.getInstance(this).isPremium) {
                binding.nativeAdsContainer.isVisible = false
                binding.nativeAdsContainer.tag = null
                true
            } else {
                MaxAdManager.showNativeAds(this, binding.nativeAdsContainer)
                false
            }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
    }

    @Suppress("DEPRECATION")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.navigation_button -> onBackPressed()
            R.id.premium_button -> openSubscriptionPaywall()
            R.id.address -> {
                startActivity(Intent(this, ChangeAddressActivity::class.java))
            }

            R.id.language -> {
                startActivity(Intent(this, LanguageActivity::class.java))
            }

            R.id.rate_us -> {
                UrlOpener.rate_us(this)
            }

            R.id.feedback -> {
                openEmailComposer(
                    context = this,
                    email = SUPPORT_EMAIL,
                    subject = "",
                    body = ""
                )
            }
        }
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