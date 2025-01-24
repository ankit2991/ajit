package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import android.provider.Settings
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.adapty.Adapty
import com.adapty.utils.AdaptyResult
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.*
import com.gpsnavigation.maps.gpsroutefinder.routemap.AppClass
import com.gpsnavigation.maps.gpsroutefinder.routemap.BuildConfig
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.SELECTED_LANGUAGE
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivitySplashBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.subscription.SubscriptionItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdAppOpen
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdAppOpenCallback
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdConstants
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdInterstitialListener
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.quimeraManager.QuimeraInit
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.*
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.IS_PREMIUM
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class SplashActivity : BaseActivity() {
    private var countDownTimer: CountDownTimer? = null
    private val maxProgress: Long = 6000
    private var preLanguageLocale = "";

    //    private var appBillingClient: AppBillingClient? = null
    var monthlySkuDetail: SubscriptionItem? = null

    private val TAG_CMP = "AdMob CMP"
    private lateinit var consentInformation: ConsentInformation
    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val isAdsInited = AtomicBoolean(false)


    lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Glide.with(this)
            .load(R.drawable.splash_title)
            .into(binding.splashImageView)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        hideStatusBar()

        checkUserConsent()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        setContentView(R.layout.activity_splash)
//        appBillingClient = AppBillingClient()

        checkBillingStatus()
//        getSubscriptionDetails()

        Constants.weeklyPrice = TinyDB.getInstance(this).getString(Constants.WEEKLY_VALUE)
        Constants.yearlyPrice = TinyDB.getInstance(this).getString(Constants.YEARLY_VALUE)

        Log.e("subscription_Splash", Constants.weeklyPrice)
        Log.e("subscription_Splash", Constants.yearlyPrice)


        (application as AppClass).isSplash = true

        if (savedInstanceState == null) {
            QuimeraInit.initQuimeraSdk(applicationContext)
        }

        if (!com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.getInstance(this)
                .getBoolean("onboardingAfterSplash")
        ) {
            //getUserCountryForOnboarding()
        }
        val getSubscriptionData = getRemoteconfig()!!.getBoolean("GPS124_01_year_sub_flag")
        var getSubscription1 = getRemoteconfig()!!.getString("GPS124_prices_value1")
        var getSubscription2 = getRemoteconfig()!!.getString("GPS124_prices_value2")

        Constants.Sub_Yearly = getSubscriptionData
        Constants.SUBSCRIPTION1 = getSubscription1
        Constants.SUBSCRIPTION2 = getSubscription2
        Log.e("subscription_Splash", getSubscription1)
        Log.e("subscription_Splash", getSubscription2)
        //isOnboarding = getRemoteconfig()!!.getBoolean("GPS124_onboarding")
        //isPaymentCard = getRemoteconfig()!!.getBoolean("GPS124_payment_card")

//        if (TinyDB.getInstance(this).getBoolean("isPolicyAccepted")) {
        /*        if (true) {

                    binding.numberProgressBar.visibility = View.VISIBLE
                    binding.loadingTxt.visibility = View.VISIBLE
                    binding.rlPrivacyContainer.visibility = View.GONE
                    initCounter()
                } else {
                    setTextViewHTML(binding.tvPrivacyPolicy, getString(R.string.privacy_text))
                    binding.numberProgressBar.visibility = View.GONE
                    binding.loadingTxt.visibility = View.GONE
                    binding.rlPrivacyContainer.visibility = View.VISIBLE
                }*/

        /*
                binding.btnGrantPermission.setOnClickListener {
                    com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.getInstance(this)
                        .putBoolean("isPolicyAccepted", true)
                    binding.numberProgressBar.visibility = View.VISIBLE
                    binding.loadingTxt.visibility = View.VISIBLE
                    binding.rlPrivacyContainer.visibility = View.GONE
                    initCounter()
                    val getSubscriptionData = getRemoteconfig()!!.getBoolean("GPS124_01_year_sub_flag")
                    getSubscription1 = getRemoteconfig()!!.getString("GPS124_prices_value1")
                    getSubscription2 = getRemoteconfig()!!.getString("GPS124_prices_value2")
                    Constants.Sub_Yearly = getSubscriptionData
                    Constants.SUBSCRIPTION1 = getSubscription1
                    Constants.SUBSCRIPTION2 = getSubscription2

                }
        */
        TinyDB.getInstance(this).putBoolean("isPolicyAccepted", true)

        initCounter()

        handleAppOpenTimes()
        TinyDB.getInstance(this).putBoolean("isInterstitialReqSent", false)
    }

    private fun checkUserConsent() {
        // Set tag for under age of consent. false means users are not under age
        // of consent.

        val params = if (BuildConfig.DEBUG) {
            val android_id =
                Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
            val deviceId: String? = md5(android_id)?.uppercase(Locale.getDefault())
            Log.i("Skype=", deviceId.toString())
            val debugSettings = ConsentDebugSettings.Builder(this)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("D3090E33EEFC1B23CC2984CDBC231FCC")
                .setForceTesting(true)
                .addTestDeviceHashedId(deviceId)
                .build()

            // Set tag for under age of consent. false means users are not under
            // age.
            ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .setConsentDebugSettings(debugSettings)
                .build()
        } else {
            ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()
        }

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                ) { loadAndShowError: FormError? ->
                    if (loadAndShowError != null) {
                        // Consent gathering failed.
                        Log.w(
                            TAG_CMP, String.format(
                                "%s: %s",
                                loadAndShowError.errorCode,
                                loadAndShowError.message
                            )
                        )
                    }
                    // Consent has been gathered.
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsSdk()
                        initAds()
                    } else {
                        initAds()
                    }
                }
            },
            { requestConsentError: FormError ->
                // Consent gathering failed.
                Log.w(
                    TAG_CMP, String.format(
                        "%s: %s",
                        requestConsentError.errorCode,
                        requestConsentError.message
                    )
                )
                initAds()
            })
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Google Mobile Ads SDK.
        MobileAds.initialize(this)
    }

    fun initAds() {
        if (isAdsInited.getAndSet(true)) {
            return
        }
        /*if (!TinyDB.getInstance(this).isPremium) {
            MaxAdManager.initAmazonSdk(this)
            MaxAdManager.initAppLovinSdkMax(this)
        }*/
    }

    private fun md5(s: String): String {
        try {
            // Create MD5 Hash
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(
                Integer.toHexString(
                    0xFF and messageDigest[i]
                        .toInt()
                )
            )
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    private val handler = Handler(Looper.getMainLooper())
    private var appOpenAdSplash: MaxAdAppOpen? = null

    private val callback = Runnable {
        appOpenAdSplash?.destroy()
        appOpenAdSplash = null
        finishSplash()
    }

    private fun startAdLoaderTimer(seconds: Long) {
        handler.postDelayed(callback, seconds * 1000)
    }

    private fun stopAdLoaderTimer() {
        handler.removeCallbacksAndMessages(null)
    }

    @SuppressLint("SetTextI18n")
    private fun initCounter(timeout: Long = maxProgress) {
        //binding.numberProgressBar.max = maxProgress.toInt()
        countDownTimer = object : CountDownTimer(maxProgress, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                /*binding.numberProgressBar.progress = (maxProgress - millisUntilFinished).toInt()
                binding.counterText.text = "Skip ${millisUntilFinished / 1000}"*/
            }

            override fun onFinish() {
                /*binding.numberProgressBar.progress = maxProgress.toInt()
                binding.counterText.text = "Skip 0"*/
//======================================================================
                if (isAdsInited.get()) {
                    showAppOpenAd()
                } else {
                    initCounter(1000)
                }
//======================================================================
            }
        }.start()
    }

    private fun showAppOpenAd() {
        // Show the app open ad.
        if (!TinyDB.getInstance(this@SplashActivity).isPremium) {
            appOpenAdSplash = MaxAdAppOpen(
                this@SplashActivity,
                MaxAdConstants.APP_OPEN_AD_ID,
                object : MaxAdAppOpenCallback {
                    override fun onAdDisplayFailed() {
                        finishSplash()
                    }

                    override fun onAdLoadFailed() {
                        stopAdLoaderTimer()
                        finishSplash()
                    }

                    override fun onAdClicked() {
                    }

                    override fun onAdHidden() {
                        finishSplash()
                    }

                    override fun onAdDisplayed() {
                    }

                    override fun onAdLoaded() {
                        stopAdLoaderTimer()
                        Log.d("counter onAdLoaded", Date().time.toString())
                        if (appOpenAdSplash != null && lifecycle.currentState
                                .isAtLeast(Lifecycle.State.RESUMED)
                        ) {
                            appOpenAdSplash!!.showAdIfReady(MaxAdConstants.APP_OPEN_AD_ID)
                        } else {
                            finishSplash()
                        }
                    }
                }
            )
            startAdLoaderTimer(MaxAdConstants.MAX_AD_LOAD_TIMEOUT)
        } else {
            finishSplash()
        }
    }

    protected fun setTextViewHTML(text: TextView, html: String) {
        var sequence: Spanned? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            sequence = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            sequence = Html.fromHtml(html)
        }

        val strBuilder = SpannableStringBuilder(sequence)
        val urls = strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
        for (span in urls) {
            makeLinkClickable(strBuilder, span)
        }
        text.text = strBuilder
        text.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        val clickable = object : ClickableSpan() {
            override fun onClick(view: View) {
                UrlUtils.openUrl(
                    this@SplashActivity,
                    "https://zedlatino.info/privacy-policy-apps.html"
                )
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)
    }

    private fun userDeviceLanguage(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            TinyDB.getInstance(this@SplashActivity).getString("preLaunchLanguage")
        } else {
            this@SplashActivity.resources.configuration.locale.language;

        }
    }

    fun finishSplash() {
//        val it = Intent(this@SplashActivity, NewPremiumActivity::class.java)
//        it.putExtra("from", false)

        //val paywall = Intent(this,PaywallUi::class.java)
        //paywall.putExtra("paywallType","Default")

        // Handle Onboarding for Thailand, Malaysia and Indonesia
        userDeviceLanguage()

        val isOnboarding = getRemoteconfig()!!.getBoolean("GPS124_onboarding")
                && !TinyDB.getInstance(this).getBoolean("onboardingAfterSplash")
        //val isPaymentCard = getRemoteconfig()!!.getBoolean("GPS124_payment_card")

        when {
            isOnboarding -> {
                TinyDB.getInstance(this).putBoolean("onboardingAfterSplash", true)
                startActivity(Intent(this, OnboardingActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                finish()
            }
            /*isPaymentCard -> {
                startActivityForResult(paywall, PAYWALL_CODE)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            }*/
            else -> {
                startMain()
            }
        }
    }

    /*@Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYWALL_CODE) {
            if (!TinyDB.getInstance(this).isPremium) {
                binding.progressBar.isVisible = true
                MaxAdManager.createInterstitialAd(this, MaxAdConstants.INTER_AD_ID,
                    object : MaxAdInterstitialListener {
                        override fun onAdLoaded(adLoad: Boolean) {
                            if (!adLoad) {
                                startMain()
                            }
                        }

                        override fun onAdShowed(adShow: Boolean) {
                            startMain()
                        }
                    })
            } else {
                startMain()
            }
        }
    }*/

    private fun startMain() {
        //binding.progressBar.isVisible = false
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        finish()
    }

    /*private fun getUserCountryForOnboarding() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "http://ip-api.com/json"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                // Display the first 500 characters of the response string.
                //Log.d("King", "Response is: ${response}")
                var result: JSONObject = JSONObject(response.toString())
                var status = result.getString("status")
                if (status == "success") {
                    countryCode = result.getString("countryCode")
                }
            },
            {
                countryCode = ""
            })

        queue.add(stringRequest)
    }*/

    private fun checkForInternet(context: Context): Boolean {
        // register activity with the connectivity manager service
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }


    override fun onDestroy() {
        destroyTimers()
        super.onDestroy()
    }

    private fun destroyTimers() {
        countDownTimer?.cancel()
        handler.removeCallbacksAndMessages(null)
    }

    override fun attachBaseContext(newBase: Context?) {
        // fetch from shared preference also save the same when applying. Default here is en = English
        newBase?.let {
            val language: String = TinyDB.getInstance(newBase).getString(SELECTED_LANGUAGE, "en")
            super.attachBaseContext(MyContextWrapper.wrap(newBase, language))
        }
    }


    private var isPurActive = false
    private fun checkBillingStatus() {
        Adapty.getProfile { result ->
            when (result) {
                is AdaptyResult.Success -> {
                    val profile = result.value
                    // check the access
                    for (item in profile.subscriptions.values) {

                        Log.e("isPurchase", ">2" + item.isActive)
                        if (item.isActive) {
                            isPurActive = true
                        }
                    }


                    if (profile.accessLevels["premium"]?.isActive == true) {
                        // grant access to premium features
                        Log.e("ispurchase", ">3" + profile.accessLevels["premium"]?.isActive)
                        isPurActive = true
                    }

//               var isSubActive =  profile.accessLevels.values.find { it.isActive && !it.isLifetime }
//                        ?.let {currentSubscription ->
//
//                            if (currentSubscription.isActive){
//                                TinyDB.getInstance(this@SplashActivity).putBoolean(
//                                    IS_PREMIUM, true
//                                )
//                                Log.e("isSubActive",">true")
//
//                                refreshAdsStatus()
//
//                                return@getProfile
//                            }
//
//                        }


                    if (isPurActive) {
                        TinyDB.getInstance(this@SplashActivity).putBoolean(
                            IS_PREMIUM, true
                        )
                        Log.e("isSubActive", ">true")

                        refreshAdsStatus()

                    } else {
                        TinyDB.getInstance(this@SplashActivity).putBoolean(
                            IS_PREMIUM, false
                        )
                        Log.e("isSubActive", ">false")

                    }


//                val prof= profile.accessLevels.values
//                    for (item in prof){
//                        item.isActive
//                    }

                    refreshAdsStatus()
                }

                is AdaptyResult.Error -> {
                    val error = result.error
                    Log.e("isPurchase", ">error" + error.message)
                    // handle the error
                }
            }
        }
    }

    override fun onBackPressed() {
        destroyTimers()
        super.onBackPressed()
    }

    companion object {
        private const val PAYWALL_CODE = 100500
    }
}
