package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.android.gpslocation.utils.UrlOpener
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityChangeAddressBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivitySettingsBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.hideStatusBar
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openSubscriptionPaywall
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets

class ChangeAddressActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityChangeAddressBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()
        initView()
    }

    private fun initView() {
        binding.toolbar.title.text = getString(R.string.address_activity_title)
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium

        binding.address.setText(TinyDB.getInstance(this).getString("MY_ADDRESS", ""))
        binding.tag.setText(TinyDB.getInstance(this).getString("MY_TAG_NAME", ""))

        binding.address.addTextChangedListener( object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                TinyDB.getInstance(this@ChangeAddressActivity).putString("MY_ADDRESS", s.toString().trim())
            }

        })

        binding.tag.addTextChangedListener( object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                TinyDB.getInstance(this@ChangeAddressActivity).putString("MY_TAG_NAME", s.toString().trim())
            }

        })

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