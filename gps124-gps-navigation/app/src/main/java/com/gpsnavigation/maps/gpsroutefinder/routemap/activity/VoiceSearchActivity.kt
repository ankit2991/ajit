package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.viewModels.MainActivityViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.VoiceAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.MyConstant
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityVoiceSearchBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.models.VoiceModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.dpToPx
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.hideStatusBar
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.openSubscriptionPaywall
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets
import org.koin.androidx.viewmodel.ext.android.viewModel


class VoiceSearchActivity : BaseActivity(), VoiceAdapter.ItemListener, View.OnClickListener {

    val mainActivityViewModel: MainActivityViewModel by viewModel()
    private val RC_VOICE = 1
    private var locationsList = ArrayList<VoiceModel>()
    var adapter: VoiceAdapter? = null
    lateinit var binding: ActivityVoiceSearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoiceSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()

        binding.toolbar.title.text = getString(R.string.voice_search)
        binding.toolbar.navigationButton.setOnClickListener(this)
        binding.toolbar.premiumButton.setOnClickListener(this)
        binding.toolbar.premiumButton.isInvisible = TinyDB.getInstance(this).isPremium

        MaxAdManager.showNativeAds(this, binding.nativeAdsContainer)

        binding.lottieAnimationView.setOnClickListener(this)
        binding.microphone.setOnClickListener(this)

        adapter = VoiceAdapter(locationsList, this, this)
        binding.rvResults.layoutManager = LinearLayoutManager(this)
        binding.rvResults.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        binding.rvResults.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.premiumButton.isInvisible =
            if (TinyDB.getInstance(this).isPremium) {
                binding.nativeAdsContainer.isVisible = false
                binding.nativeAdsContainer.tag = null
                true
            } else {
                binding.nativeAdsContainer.isVisible = true
                MaxAdManager.showNativeAds(this, binding.nativeAdsContainer)
                false
            }
    }

      override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_VOICE && RESULT_OK == resultCode && null != data) {

            locationsList.clear()
            binding.initialLayout.isVisible = false
            binding.result.isVisible = true

            val size = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.size ?: 0
            if (size > 0) {
                for (i in 0 until size) {
                    locationsList.add(
                        VoiceModel(
                            data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                                ?.get(i)
                        )
                    )
                }
                adapter?.notifyDataSetChanged()

            }
        } else {
            showMicrophone()
        }
    }

    override fun onItemClick(item: VoiceModel?) {
        openLocationByName(item?.location!!)
        showMicrophone()
    }


    private fun showMicrophone() {
        binding.initialLayout.isVisible = true
        binding.result.isVisible = false
        binding.lottieAnimationView.playAnimation()
    }

    private fun openLocationByName(locationName: String) {
        try {
            MyConstant.IS_APPOPEN_BG_IMPLICIT = true
            val uri = Uri.parse("http://maps.google.com/maps?q=$locationName&iwloc=A&hl=es")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {

        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        TinyDB.getInstance(this).setCurrentTime(0)
    }

    private fun setInset() {
        binding.nativeAdsContainer.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = systemBarsInsets(insets)
            (binding.nativeAdsContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                bottomMargin = systemBarsInsets.bottom
            }

            insets
        }

        binding.guidelineBottom.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = systemBarsInsets(insets)

            (view.layoutParams as ConstraintLayout.LayoutParams).apply {
                bottomMargin = systemBarsInsets.bottom
            }

            insets
        }
    }

    @Suppress("DEPRECATION")
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.navigation_button -> onBackPressed()
            R.id.premium_button -> openSubscriptionPaywall()
            R.id.microphone,
            R.id.lottie_animation_view -> {
                binding.lottieAnimationView.cancelAnimation()
                binding.lottieAnimationView.isInvisible = false
                recordAudio()
            }
        }
    }

    private fun recordAudio() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak to search location")
        try {
            startActivityForResult(intent, RC_VOICE)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(
                this@VoiceSearchActivity, "Your device doesn't support Speech to Text",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
