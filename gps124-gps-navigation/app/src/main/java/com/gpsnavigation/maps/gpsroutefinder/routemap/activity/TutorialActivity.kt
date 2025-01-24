package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

//import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.OnBoardingAdapter

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.ImageView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.TutorialAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityTutorialBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.TutorialFragment
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdConstants
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdInterstitialListener
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.dpToPx
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.getRemoteconfig
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.hideStatusBar
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets


class TutorialActivity : BaseActivity(), TutorialFragment.OnClickListener {
    private var dots: ArrayList<ImageView>? = null

    enum class TutorialScreens {
        Screen1, Screen2, Screen3, Screen4
    }

    private var screenList: MutableList<TutorialScreens> = mutableListOf()
    private lateinit var adapter: TutorialAdapter
    private lateinit var binding: ActivityTutorialBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        hideStatusBar()

        setInset()
        init()

        binding.adsLayout.isVisible = if (!TinyDB.getInstance(this).isPremium) {
            MaxAdManager.showBannerAds(this, binding.adsContainer)
            (binding.btnContinue.layoutParams as ConstraintLayout.LayoutParams).apply {
                bottomMargin = 16.dpToPx
            }
            true
        } else {
            (binding.btnContinue.layoutParams as ConstraintLayout.LayoutParams).apply {
                bottomMargin = 26.dpToPx
            }
            false
        }
    }

    private fun init() {
        initTutorialContent()
        initTutorialAdapter()
        setActions()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (binding.vpTutorial.currentItem == 3) {
            binding.btnContinue.isVisible = false
            if (!TinyDB.getInstance(this@TutorialActivity).isPremium) {
                binding.adsLayout.isVisible = false
                MaxAdManager.destroyBannerAd(binding.adsContainer)
                binding.adsContainer.removeAllViews()
                binding.adsContainer.tag = null
            }
        }
    }

    private fun setActions() {
        binding.vpTutorial.setUserInputEnabled(false)

        binding.btnContinue.apply {
            text = getString(R.string.button_text_continue)
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            setOnClickListener {
                when (binding.vpTutorial.currentItem) {
                    1 -> {
                        val myFragment =
                            supportFragmentManager.findFragmentByTag("f1") as? TutorialFragment
                        val data = myFragment?.getAddressText();
                        if (!data.isNullOrEmpty()) {
                            TinyDB.getInstance(this@TutorialActivity)
                                .putString("MY_ADDRESS", data.toString().trim())
                        }
                        binding.vpTutorial.currentItem++
                    }

                    2 -> {
                        val myFragment =
                            supportFragmentManager.findFragmentByTag("f2") as? TutorialFragment
                        val data = myFragment?.getTagText();
                        if (!data.isNullOrEmpty()) {
                            TinyDB.getInstance(this@TutorialActivity)
                                .putString("MY_TAG_NAME", data.toString().trim())
                        }
                        binding.vpTutorial.currentItem++
                        binding.btnContinue.isVisible = false
                        if (!TinyDB.getInstance(this@TutorialActivity).isPremium) {
                            binding.adsLayout.isVisible = false
                            MaxAdManager.destroyBannerAd(binding.adsContainer)
                            binding.adsContainer.removeAllViews()
                            binding.adsContainer.tag = null
                        }
                    }

                    4 -> {
                        finish()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                    }

                    else -> {
                        binding.vpTutorial.currentItem++
                    }
                }
            }
        }
    }

    private fun initTutorialContent() {
        screenList.add(TutorialScreens.Screen1)
        screenList.add(TutorialScreens.Screen2)
        screenList.add(TutorialScreens.Screen3)
        screenList.add(TutorialScreens.Screen4)
    }

    private fun initTutorialAdapter() {
        adapter =
            TutorialAdapter(
                supportFragmentManager,
                lifecycle
            )
        binding.vpTutorial.adapter = adapter
//        vp_onboarding.setPageTransformer(false,FadePageTransformer())
    }

    override fun onSkipClick() {
        // Handle the click event here
        navigateToMain()
    }

    override fun onPinImageClick(pinResourceId: Int?) {
        if (pinResourceId != null) {
            TinyDB.getInstance(this@TutorialActivity)
                .putString("MY_PIN_RESOURCE_ID", pinResourceId.toString().trim())
        }
        navigateToMain()
    }

    private fun navigateToMain() {
        val paywall = Intent(this, PaywallUi::class.java)
        paywall.putExtra("paywallType", "Default")

        val isPaymentCard =
            getRemoteconfig()!!.getBoolean("GPS124_payment_card") && !TinyDB.getInstance(this).isPremium

        when {
            isPaymentCard -> {
                startActivityForResult(paywall, PAYWALL_CODE)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            }

            else -> {
                startMain()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYWALL_CODE) {
            if (!TinyDB.getInstance(this).isPremium) {
                startMain()
            } else {
                startMain()
            }
        }
    }

    private fun startMain() {
        binding.progressBar.isVisible = false
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        finish()
    }

    private fun setInset() {
        binding.guidelineBottom.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = systemBarsInsets(insets)

            binding.guidelineBottom.setGuidelineEnd(systemBarsInsets.bottom)

            insets
        }
    }

    companion object {
        private const val PAYWALL_CODE = 100500
    }
}