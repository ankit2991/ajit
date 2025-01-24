package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

//import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.OnBoardingAdapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.text.*
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.adapters.OnBoardingAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityOnboardingBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdConstants.INTER_AD_ID
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdInterstitialListener
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.dpToPx
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.getRemoteconfig
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.hideStatusBar
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.systemBarsInsets
import kotlin.math.max


class OnboardingActivity : BaseActivity() {
    private var dots: ArrayList<ImageView>? = null
    enum class OnBoarding {
        Screen1, Screen2, Screen3
    }

    private var screenList: MutableList<OnBoarding> = mutableListOf()
    private lateinit var  adapter: OnBoardingAdapter
    lateinit var binding: ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        //hideStatusBar()

        setInset()
        init()
       val myVib = this.getSystemService(VIBRATOR_SERVICE) as Vibrator
        binding.btnContinue.text = getString(R.string.button_text_continue)
        binding.btnContinue.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
/*
        binding.tvPrivacyDescription.movementMethod = LinkMovementMethod.getInstance()
        binding.tvPrivacyDescription.text = resources.getText(R.string.terms_and_service).makeUnderlineClickable { index ->

            //handle click here
            if (index==0){
                UrlUtils.openUrl(
                    this@OnboardingActivity,
                    "https://zedlatino.info/TermsOfUse.html"
                )
            }
            else{
                UrlUtils.openUrl(
                    this@OnboardingActivity,
                    "https://zedlatino.info/privacy-policy-apps.html"
                )
            }
        }

        binding.tvPrivacyDescription.movementMethod = LinkMovementMethod.getInstance()
        binding.tvPrivacyDescription.highlightColor = Color.TRANSPARENT
*/
        binding.btnContinue.setOnClickListener {
            myVib.vibrate(50)
            if (binding.vpOnboarding.currentItem == 2) {
                navigateToTutorials()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            } else {
                binding.vpOnboarding.currentItem++
            }
        }

        binding.adsContainer.isVisible = if (!TinyDB.getInstance(this).isPremium) {
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

    private fun navigateToTutorials() {
        val paywall = Intent(this,PaywallUi::class.java)
        paywall.putExtra("paywallType","Onboarding")

        val isPaymentCard = getRemoteconfig()!!.getBoolean("GPS124_payment_card") && !TinyDB.getInstance(this).isPremium

        when {
            isPaymentCard -> {
                startActivityForResult(paywall, PAYWALL_CODE)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            }
            else -> {
                startTutorials()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYWALL_CODE) {
            if (!TinyDB.getInstance(this).isPremium) {
                startTutorials()
            } else {
                startTutorials()
            }
        }
    }

    private fun startTutorials() {
        binding.progressBar.isVisible = false
        startActivity(Intent(this, TutorialActivity::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        finish()
    }

    private fun init() {
        initOnboardingContent()
        initOnboardingAdapter()
        addDots()
        binding.vpOnboarding.setUserInputEnabled(false)
    }

    private fun initOnboardingContent() {
        screenList.add(OnBoarding.Screen1)
        screenList.add(OnBoarding.Screen2)
        screenList.add(OnBoarding.Screen3)
    }

    private fun initOnboardingAdapter() {
         adapter =
             OnBoardingAdapter(
                 supportFragmentManager,
                 lifecycle
             )
        binding.vpOnboarding.adapter = adapter
//        vp_onboarding.setPageTransformer(false,FadePageTransformer())
    }
    fun addDots() {
        dots = ArrayList()

        for (i in 0 until screenList.size) {
            val dot = ImageView(this)
            dot.setImageDrawable(resources.getDrawable(R.drawable.item_unselected))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(10,5,10,5)
            //binding.dotsLayout.addView(dot, params)
            //dots?.add(dot)
        }
        binding.vpOnboarding.registerOnPageChangeCallback(object : OnPageChangeCallback() {
             override fun onPageScrolled(
                 position: Int,
                 positionOffset: Float,
                 positionOffsetPixels: Int
             ) {
                 super.onPageScrolled(position, positionOffset, positionOffsetPixels)
             }

             override fun onPageSelected(position: Int) {
                 super.onPageSelected(position)
                 Log.e("Selected_Page", position.toString())
                 //selectDot(position)
                 adapter.notifyDataSetChanged()
                 /*if (position==0){
                     binding.tvPrivacyDescription.visibility=View.VISIBLE
                     binding.btnContinue.text = getString(R.string.get_started)
                 }
                 else{
                     binding.btnContinue.text = getString(R.string.button_text_continue)
                     binding.tvPrivacyDescription.visibility=View.GONE
                 }*/
             }

             override fun onPageScrollStateChanged(state: Int) {
                 super.onPageScrollStateChanged(state)
             }
         })

    }

  /*  fun selectDot(idx: Int) {
        val res: Resources = resources
        for (i in 0 until screenList.size) {
            if (idx == 2) {
                val drawableId: Int = R.drawable.item_selected
                val drawable: Drawable = res.getDrawable(drawableId)
                dots?.get(i)?.setImageDrawable(drawable)
            }
            else if (idx==1){
                val drawableId: Int =
                    if (i == 2) R.drawable.item_unselected else R.drawable.item_selected
                val drawable: Drawable = res.getDrawable(drawableId)
                dots?.get(i)?.setImageDrawable(drawable)
            }
            else if (idx==0){
                val drawableId: Int =
                    if (i == 0) R.drawable.item_selected else R.drawable.item_unselected
                val drawable: Drawable = res.getDrawable(drawableId)
                dots?.get(i)?.setImageDrawable(drawable)
            }
        }
    }*/
    private fun CharSequence.makeUnderlineClickable(listener: (index: Int) -> Unit): SpannableString {
        val spannedString = SpannableString(this)
        spannedString.getSpans(0, length, UnderlineSpan::class.java)?.forEachIndexed { index, underlineSpan ->
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    listener.invoke(index)
                }

                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = true
                }
            }
            spannedString.setSpan(
                clickableSpan,
                spannedString.getSpanStart(underlineSpan),
                spannedString.getSpanEnd(underlineSpan),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannedString
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