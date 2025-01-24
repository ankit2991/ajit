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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.LocalizationAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.callbacks.OnLanguageClicked
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityLanguageBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.LocaleLanguageModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.LocaleHelper1
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.hideStatusBar
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openSubscriptionPaywall
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets

class LanguageActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityLanguageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()
        initView()
    }

    private fun initView() {
        binding.toolbar.title.text = getString(R.string.language_activity_title)
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium

        initLanguages()
        MaxAdManager.showNativeAds(this, binding.nativeAdsContainer)
    }

    private fun initLanguages() {
        val appLangList = binding.appLangList
        val langList = ArrayList<LocaleLanguageModel>()

        val selectedLangCode = LocaleHelper1.getLanguage(this) ?: "en"
        var selectedPosition = 0


        val recyclerLayoutManager = LinearLayoutManager(this)
        appLangList.layoutManager = recyclerLayoutManager

        //val dividerItemDecoration = DividerItemDecoration(appLangList.context, recyclerLayoutManager.orientation)
        //appLangList.addItemDecoration(dividerItemDecoration)

        val appLanguages = resources.getStringArray(R.array.appLanguages)
        val appLangCode = resources.getStringArray(R.array.appLangCode)

        for (i in appLanguages.indices) {
            if (selectedLangCode.equals(appLangCode[i], ignoreCase = true))
                selectedPosition = i

            val languageModel = LocaleLanguageModel(
                appLanguages[i],
                appLangCode[i],
                "online")
            langList.add(languageModel)
        }
        val localizationAdapter =
            LocalizationAdapter(
                langList,
                selectedPosition
            )
        appLangList.adapter = localizationAdapter

        localizationAdapter.setOnLanguageSelected {
            LocaleHelper1.setAppLocale(this, it.code)

            startActivity(
                Intent(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                ).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                )
            )
            finish()
        }
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