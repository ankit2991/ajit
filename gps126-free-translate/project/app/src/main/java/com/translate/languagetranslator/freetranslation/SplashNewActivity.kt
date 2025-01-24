package com.translate.languagetranslator.freetranslation


import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.RemoteException
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.adapty.Adapty
import com.adapty.utils.AdaptyResult
//import com.android.billingclient.api.AcknowledgePurchaseParams
//import com.android.billingclient.api.ProductDetails
//import com.android.billingclient.api.Purchase
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.calldorado.ui.aftercall.CallerIdActivity
import com.code4rox.adsmanager.AppOpenCallback
import com.code4rox.adsmanager.MaxAdCallbacks
import com.code4rox.adsmanager.MaxAdManager
import com.code4rox.adsmanager.MaxAppOpenAdManager
import com.code4rox.adsmanager.TinyDB
import com.google.gson.Gson
import com.translate.languagetranslator.freetranslation.activities.adptypaywall.PaywallUi
import com.translate.languagetranslator.freetranslation.activities.adptypaywall.isOnline
import com.translate.languagetranslator.freetranslation.activities.adptypaywall.loadPaywall
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.NetworkUtils
import com.translate.languagetranslator.freetranslation.appUtils.RemoteConfigConstants
import com.translate.languagetranslator.freetranslation.appUtils.fetchLanguages
import com.translate.languagetranslator.freetranslation.appUtils.getPrefBoolFirst
import com.translate.languagetranslator.freetranslation.appUtils.getRemoteConfig
import com.translate.languagetranslator.freetranslation.appUtils.isPremium
import com.translate.languagetranslator.freetranslation.appUtils.putPrefString
//import com.translate.languagetranslator.freetranslation.billing.BillingCallbacks
//import com.translate.languagetranslator.freetranslation.billing.BillingProcess
import com.translate.languagetranslator.freetranslation.extension.ConstantsKT
import com.translate.languagetranslator.freetranslation.models.countriesData.Countries
import com.translate.languagetranslator.freetranslation.models.countriesData.Location
import com.translate.languagetranslator.freetranslation.network.NetworkController
import com.translate.languagetranslator.freetranslation.quimera.QuimeraInit
import com.translate.languagetranslator.freetranslation.utils.RateUs
import com.translate.languagetranslator.freetranslation.utils.RemoteConfig
import com.translate.languagetranslator.freetranslation.viewmodels.SplashViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.intentFor
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStreamReader
import java.io.Reader
import java.util.Date


class SplashNewActivity : AppCompatActivity(),
    androidx.lifecycle.LifecycleObserver {
//    private var skuDetailInApp: ProductDetails? = null
//    retryOnConnectionFailed: Boolean

    private var readyToPurchase = false
    val splashViewModel: SplashViewModel by viewModel()
    var isPause = false
    var TIME_TO_WAIT = 9000L
    var delayJob: Job? = null
    val ORGANIC_REFERRAL_KEY = "utm_medium=organic"
    var showInterstitial = false

    //    private lateinit var billingProcess: BillingProcess
//    lateinit var mFirebaseAnalytics: FirebaseAnalytics
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_new)


        checkBillingStatus()
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
//        AppLovinCommunicator.getInstance(this)
//            .subscribe(this, "max_revenue_events");
        if (savedInstanceState == null) {
            QuimeraInit.initQuimeraSdk(applicationContext)
        }


        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        RemoteConfig.getRemoteconfig()
//        initBillingProcess()
        try {

            CoroutineScope(Dispatchers.Default).launch {
                if (TinyDB.getInstance(this@SplashNewActivity).isConsentGivenGDPR) {
                    MaxAdManager.loadInterAd(this@SplashNewActivity, object : MaxAdCallbacks {
                        override fun onAdLoaded(adLoad: Boolean) {
                        }

                        override fun onAdShowed(adShow: Boolean) {
                        }

                        override fun onAdHidden(adHidden: Boolean) {
                        }

                        override fun onAdLoadFailed(adLoadFailed: Boolean) {
                        }

                        override fun onAdDisplayFailed(adDisplayFailed: Boolean) {
                        }
                    })
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        if (!TinyDB.getInstance(this).getBoolean(Constants.IS_PREMIUM)) {
            if (isOnline(this)) {
                loadPaywall(Constants.GPS_ONBOARDING);
                loadPaywall(Constants.GPS_DEFAULT);
                loadPaywall(Constants.GPS_PREMIUM);
            }
        }

        if (getPrefBoolFirst(Constants.SPLASH_FIRST)) {
            getLocationInfo()
        }

        handleAppOpenTimes()

        compaignTracking()

    }

    //    private void initinApp() {
    //        billingProcessor = new BillingProcessor(this, Constants.LICENSE_KEY, Constants.MERCHANT_ID, this);
    //        billingProcessor.initialize();
    //    }

    override fun onPause() {
        super.onPause()
        delayJob?.cancel()
        isPause = true

    }

    override fun onResume() {
        super.onResume()
        isPause = false

    }


    private fun navigateToNextScreen() {
        if (isPause)
            return

        checkSessionCompaign()
        val onBoardingValue = getRemoteConfig().getBoolean(RemoteConfigConstants.OnBoarding)
        val paymentCardValue = getRemoteConfig().getBoolean(RemoteConfigConstants.PaymentCard)
        Log.d("King", "onBoardingValue: $onBoardingValue")
        Log.d("King", "paymentCardValue: $paymentCardValue")
        val showPaymentOneTime =
            TinyDB.getInstance(this).getBoolean(ConstantsKT.SHOW_PAYMENT_ONE_TIME)

        val isCompaignUser =
            TinyDB.getInstance(this@SplashNewActivity).getBoolean(ConstantsKT.IS_COMPAIGN_USER)

        if (!splashViewModel.isSubscriptionScreenShown() /*&& onBoardingValue*/) {
            splashViewModel.setSubscriptionScreenShown(true)
//            startActivity(Intent(this, OnboardingActivity::class.java))
            moveToPaywallOrHome()
        } else if (!showPaymentOneTime && !isPremium() && getRemoteConfig().getBoolean(
                RemoteConfigConstants.PaymentCard
            ) && !isCompaignUser
        ) {
            val intent = Intent(
                this@SplashNewActivity,
                PaywallUi::class.java
            )
            intent.putExtra(
                Constants.PAYWALL_TYPE,
                Constants.GPS_ONBOARDING
            )
            intent.putExtra("from", true)
            startActivity(intent)
        } else {
            val config = getRemoteConfig().getString("GPS126_rate_us_placement")
            if (!TinyDB.getInstance(this@SplashNewActivity)
                    .getBoolean(Constants.RATEUS_FIRST_COMPLETE)
            ) {
                if (config == "Open" && TinyDB.getInstance(this@SplashNewActivity)
                        .getInt(Constants.RATEUS_FIRST_TIME) % 5 == 1
                ) {
                    RateUs.showDialog(this@SplashNewActivity, object : RateUs.CallBackClick {
                        override fun onPositive() {
                            navigateHome()
                        }

                        override fun onNegitive() {
                            navigateHome()
                        }

                        override fun onload() {
                        }

                    })

                } else {
                    navigateHome()
                }

            } else {
                navigateHome()
            }
        }
    }

    private fun moveToPaywallOrHome(){
        val showPaymentOneTime =
            TinyDB.getInstance(this).getBoolean(ConstantsKT.SHOW_PAYMENT_ONE_TIME)
        val isCompaignUser = TinyDB.getInstance(this).getBoolean(ConstantsKT.IS_COMPAIGN_USER)
        if (!showPaymentOneTime && !isPremium() && getRemoteConfig().getBoolean(RemoteConfigConstants.PaymentCard) && !isCompaignUser) {

            val intent = Intent(
                this,
                PaywallUi::class.java
            )
            intent.putExtra(
                Constants.PAYWALL_TYPE,
                Constants.GPS_ONBOARDING
            )
            intent.putExtra("from", true)
            startActivity(intent)

        } else {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        finish()
    }

    fun navigateHome() {
        val paywallOnce = TinyDB.getInstance(this@SplashNewActivity)
            .getBoolean(ConstantsKT.SHOW_PAYWALL_ONCE_COMPAIGN_USER)
        if (!isPremium() && getRemoteConfig().getBoolean(RemoteConfigConstants.PaymentCard) && ConstantsKT.COMPAIGN_3RD_DAY && !paywallOnce) {
            TinyDB.getInstance(this@SplashNewActivity)
                .putBoolean(ConstantsKT.IS_COMPAIGN_USER, false)
            TinyDB.getInstance(this@SplashNewActivity)
                .putBoolean(ConstantsKT.SHOW_PAYWALL_ONCE_COMPAIGN_USER, true)
            val intent = Intent(
                this@SplashNewActivity,
                PaywallUi::class.java
            )
            intent.putExtra(
                Constants.PAYWALL_TYPE,
                Constants.GPS_ONBOARDING
            )
            intent.putExtra("from", true)
            startActivity(intent)


        } else {
            startActivities(
                arrayOf(
                    intentFor<HomeActivity>()
                )
            )
            finish()
        }
    }

    private fun isInOnboardingCountry(): Boolean {

//        val current = if (android.os.Build.VERSION.SDK_INT > 23) {
//
//            resources.configuration.locales[0]
//        } else {
//
//            resources.configuration.locale
//        }
//
//        return current.country.uppercase().contains("TH") ||
//                current.country.uppercase().contains("MY") ||
//                current.country.uppercase().contains("ID")

        return true;
    }

    private fun getLocationInfo() {
        if (NetworkUtils.isNetworkConnected(this)) {
            NetworkController.callLocationApi().enqueue(object : Callback<Location> {
                override fun onResponse(call: Call<Location>, response: Response<Location>) {
                    if (response.isSuccessful) {
                        val location = response.body()
                        location?.let {
                            val countryCode = it.countryCode
                            compareCountry(countryCode)

                        } ?: kotlin.run {
                            initSplash()
                        }
                    } else {
                        initSplash()
                    }
                }

                override fun onFailure(call: Call<Location>, t: Throwable) {
                    initSplash()
                }

            })
        } else {
            initSplash()
        }
    }

    private fun compareCountry(countryCode: String?) {
        var count = 0
        getLocation("countries.txt") { countriesdata ->
            kotlin.runCatching {
                val countriesList = countriesdata.countries
                for (country in countriesList) {
                    val countryCodeJson = country.countryCode
                    val langName = country.languageName
                    val langSupportCode = country.languageCode
                    val langMean = country.languageMean

                    if (countryCodeJson == countryCode) {
                        var allLangs = fetchLanguages()
                        val countries: MutableList<String> = ArrayList()
                        for (languageModel in allLangs) {
                            countries.add(languageModel.supportedLangCode)
                        }
                        val selectedId = countries.indexOf(langSupportCode)
                        val langCode = allLangs[selectedId].languageCode

                        setLangData(langName, langSupportCode, langCode, langMean)
                        break
                    } else {
                        if (count >= countriesList.size) {
                            setLangData("French", "fr", "fr-FR", "Français")
                        }
                    }
                    count++
                }
            }.onFailure {
                initSplash()
            }

        }

    }

    private fun setLangData(
        langName: String?,
        langsupportCode: String?,
        langCode: String?,
        langMean: String?,
    ) {
        putPrefString(Constants.LANG_TO_MEANING, langMean!!)
        putPrefString(Constants.LANG_TO, langName!!)
        putPrefString(Constants.LANG_TO_CODE, langCode!!)
        putPrefString(Constants.LANG_TO_CODE_SUPPORT, langsupportCode!!)
    }

    fun getLocation(fileName: String, countryData: (Countries) -> Unit) {
        var countryInfo: Countries? = null
        CoroutineScope(Dispatchers.IO).launch {
            countryInfo = readFile(fileName)
            withContext(Dispatchers.Main) {
                countryInfo?.let {
                    countryData.invoke(it)

                }
            }
        }
    }

    fun readFile(fileName: String): Countries? {
        var reader: Reader
        var countries: Countries? = null
        kotlin.runCatching {
            val gson = Gson()
            val inputStream = assets.open(fileName)
            reader = InputStreamReader(inputStream)
            countries = gson.fromJson(reader, Countries::class.java)


        }.onFailure {
            return null
        }
        return countries

    }

    private fun initSplash() {
        setLangData("French", "fr", "fr-FR", "Français")
    }


    /**
     * Create the countdown timer, which counts down to zero and show the app open ad.
     *
     * @param seconds the number of seconds that the timer counts down from
     */
    private fun createTimer(seconds: Long) {

        if (TinyDB.getInstance(this@SplashNewActivity)
                .getInt(ConstantsKT.COMP_SESSION_APPOPEN) == 2
        ) {
            var compaignUserSessionAppOpen =
                TinyDB.getInstance(this@SplashNewActivity).getInt(ConstantsKT.COMP_SESSION_APPOPEN)
            var session = compaignUserSessionAppOpen + 1
            TinyDB.getInstance(this@SplashNewActivity)
                .putInt(ConstantsKT.COMP_SESSION_APPOPEN, session)
        }

        if (ConstantsKT.FROM_COMPAIGN && TinyDB.getInstance(this@SplashNewActivity)
                .getInt(ConstantsKT.COMP_SESSION_APPOPEN) == 1
        ) {
            var compaignUserSessionAppOpen =
                TinyDB.getInstance(this@SplashNewActivity).getInt(ConstantsKT.COMP_SESSION_APPOPEN)
            var session = compaignUserSessionAppOpen + 1
            TinyDB.getInstance(this@SplashNewActivity)
                .putInt(ConstantsKT.COMP_SESSION_APPOPEN, session)

            navigateToNextScreen()
        } else {


            val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {

                    val application = application

                    // If the application is not an instance of MyApplication, log an error message and
                    // start the MainActivity without showing the app open ad.
                    if (application !is AppBase) {
                        Log.e("", "Failed to cast application to MyApplication.")
                        navigateToNextScreen()
                        return
                    }
                    val preferences = getSharedPreferences("PREF_NAME", MODE_PRIVATE)
                    if (!splashViewModel.isAutoAdsRemoved()) {
                        if (!com.translate.languagetranslator.freetranslation.appUtils.TinyDB.getInstance(AppBase.currentActivity).isConsentGivenGDPR) {

                            navigateToNextScreen()
                        } else {
                            Log.e("", "open_ad ad showing.")
                            (application as AppBase)
                                .showAppopenAd(

                                    this@SplashNewActivity, object : AppOpenCallback {
                                        override fun isAdLoad(isLoad: Boolean) {

                                        }

                                        override fun isAdShown(isShow: Boolean) {

                                        }

                                        override fun isAdDismiss(isShow: Boolean) {
                                            navigateToNextScreen()
                                        }
                                    })
                        }
                        // Show the app open ad.

                    } else {
                        navigateToNextScreen()
                    }
                }

            }
            countDownTimer.start()
        }
    }


//    override fun billingInitialized() {
//        Log.e("billingInitialized", "true")
//        billingProcess.let { process ->
//
//            val inAppDetailList = process.getInAppDetailList()
//            inAppDetailList?.let { skuDetailListInApp ->
//                for (sku in skuDetailListInApp) {
//                    if (sku!!.productId == getString(R.string.in_app_key)) {
//                        skuDetailInApp = sku
//                        readyToPurchase = true
//                    }
//                }
//            }
//
//        }
//    }
//
//    override fun billingPurchased(purchases: MutableList<Purchase>?) {
////        TODO("Not yet implemented")
//        Log.e("billing", "if splash+ Purchase calback")
//
//        purchases?.let { purchasedList ->
//            for (purchase in purchasedList) {
//                handlePurchase(purchase)
//            }
//        }
//    }
//
//    override fun billingCanceled() {
////        TODO("Not yet implemented")
//
//        Log.e("billing", "splash+ Purchase cancel")
//        TinyDB.getInstance(this)
//            .putBoolean(Constants.IS_PREMIUM, false)
//    }
//
//    override fun itemAlreadyOwned() {
////        TODO("Not yet implemented")
//        Log.d("itemAlreadyOwned", "true")
//    }
//
//    override fun onQuerySkuDetailsSubscription(skuDetailsList: MutableList<ProductDetails>?) {
////        TODO("Not yet implemented")
//        Log.d("onQuerySkuDetailsSubscription", "true")
//    }
//
//    override fun onQuerySkuDetailsInApp(skuDetailsList: MutableList<ProductDetails>?) {
////
//        Log.d("onQuerySkuDetailsInApp", "true")
//        if (skuDetailsList != null) {
//
//            for (sku in skuDetailsList) {
//                if (sku.productId == getString(R.string.in_app_key)) {
//                    skuDetailInApp = sku
//                    readyToPurchase = true
//                }
//            }
//        }
//    }
//
//    override fun queryPurchaseResultInApp(purchases: MutableList<Purchase>?) {
//        if (purchases != null) {
//
//            Log.d("INAPPpurchase", "true" + purchases.size)
//        } else {
//            TinyDB.getInstance(this)
//                .putBoolean(Constants.IS_PREMIUM, false)
//        }
//        Log.e("queryPurchaseResultInApp", "true")
//        purchases?.let { purchaseList ->
//            for (sku in purchaseList) {
//                handlePurchase(sku)
//            }
//        }
//    }
//
//    override fun queryPurchaseResultSubscription(purchases: MutableList<Purchase>?) {
//        if (purchases != null) {
//
//            Log.d("SUBpurchase", "true" + purchases.size)
//        } else {
//            TinyDB.getInstance(this)
//                .putBoolean(Constants.IS_PREMIUM, false)
//        }
//        purchases?.let { purchaseList ->
//            for (sku in purchaseList) {
//                handlePurchase(sku)
//            }
//        }
//    }
//
//
//    private fun handlePurchase(purchase: Purchase) {
//
//
//        if (purchase.skus.contains(Constants.SUBSCRIPTION_ID_YEAR) || purchase.skus.contains(
//                Constants.SUBSCRIPTION_ID_MONTH
//            ) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
//        ) {
//            if (!purchase.isAcknowledged) {
//
//                val params = AcknowledgePurchaseParams.newBuilder()
//                    .setPurchaseToken(purchase.purchaseToken)
//                    .build()
//
//                billingProcess.getBillingClient()?.acknowledgePurchase(
//                    params
//                ) { billingResult ->
//
//                    //TinyDB.getInstance(this).putBoolean(Constants.IS_PREMIUM, true)
//                    setUserAsPremium(purchase.purchaseTime)
////                    goToNext()
//
//                }
//
//            } else {
//                //TinyDB.getInstance(this).putBoolean(Constants.IS_PREMIUM, true)
//                setUserAsPremium(purchase.purchaseTime)
////                goToNext()
//            }
//        }
//
//    }
//
//
//    private fun initBillingProcess() {
//        /*val skuList = ArrayList<String>()
//        skuList.add(getString(R.string.in_app_key))
//        billingProcess = BillingProcess(this, this, skuList, null, false)*/
//
//        val skuList = ArrayList<String>()
//
//        val billingValue = getRemoteConfig().getBoolean(RemoteConfigConstants.Monthly_Sub)
//        Log.d("getRemoteConfig_value", "billingValue: $billingValue")
//        if (billingValue) {
//            skuList.add(Constants.SUBSCRIPTION_ID_MONTH)
//
//        } else {
//            skuList.add(Constants.SUBSCRIPTION_ID_YEAR)
//        }
//        billingProcess = BillingProcess(this, this, null, skuList, false)
//    }

    fun compaignTracking() {

        val referrerClient = InstallReferrerClient.newBuilder(this).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                    var response: ReferrerDetails? = null
                    response = try {
                        referrerClient.installReferrer
                    } catch (e: RemoteException) {
                        throw RuntimeException(e)
                    }
                    val referrer = response!!.installReferrer
//                    val gclid: String = extractGclid(referrer)!!

                    val gclid = "gclid"
//                    Log.e("ReferrerClient",">>"+referrer)
                    if (referrer == null || referrer.isEmpty()
                        || response.getInstallReferrer().contains(ORGANIC_REFERRAL_KEY)
                    ) {
                        // Organic user
//                        TinyDB.getInstance(this@SplashNewActivity).putBoolean(ConstantsKT.IS_COMPAIGN_USER, false)
                        Log.e("ReferrerClient", "not come from campaign <organic user>")
                        ConstantsKT.FROM_COMPAIGN = false
                        createTimer(4L)

                    } else if (!referrer.contains(gclid)
                        || response.installReferrer.contains(ORGANIC_REFERRAL_KEY)
                    ) {
                        // Organic user
//                        TinyDB.getInstance(this@SplashNewActivity).putBoolean(ConstantsKT.IS_COMPAIGN_USER, false)
                        Log.e("ReferrerClient", "not come from campaign <organic user>")
                        ConstantsKT.FROM_COMPAIGN = false
                        createTimer(4L)
                    } else {
                        // Is campaign user
                        TinyDB.getInstance(this@SplashNewActivity)
                            .putBoolean(ConstantsKT.IS_COMPAIGN_USER, true)
                        Log.e("ReferrerClient", "come from campaign<")

                        ConstantsKT.FROM_COMPAIGN = true
                        var compaignUserSessionAppOpen = TinyDB.getInstance(this@SplashNewActivity)
                            .getInt(ConstantsKT.COMP_SESSION_APPOPEN)
                        var session = compaignUserSessionAppOpen + 1
                        TinyDB.getInstance(this@SplashNewActivity)
                            .putInt(ConstantsKT.COMP_SESSION_APPOPEN, session)

                        createTimer(4L)
                    }
                    referrerClient.endConnection()
                } else {
                    // Error occurred during setup
//                    TinyDB.getInstance(this@SplashNewActivity).putBoolean(ConstantsKT.IS_COMPAIGN_USER, false)
                    Log.e("ReferrerClient", "Error during connection ")
                    ConstantsKT.FROM_COMPAIGN = false
                    createTimer(4L)
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Disconnected from the Install Referrer service

            }
        })
    }

    private fun checkSessionCompaign() {
        val isCompaignUser =
            TinyDB.getInstance(this@SplashNewActivity).getBoolean(ConstantsKT.IS_COMPAIGN_USER)

        if (isCompaignUser && TinyDB.getInstance(this).getLong(ConstantsKT.COMPAIGN_DATE, 0) <= 0) {
            val compaignDate = Date().time
            TinyDB.getInstance(this@SplashNewActivity)
                .putLong(ConstantsKT.COMPAIGN_DATE, compaignDate)
            TinyDB.getInstance(this).putBoolean(ConstantsKT.SHOW_PAYMENT_ONE_TIME, true)
        }


        val compaignDateMillis =
            TinyDB.getInstance(this@SplashNewActivity).getLong(ConstantsKT.COMPAIGN_DATE, 0)

        if (compaignDateMillis > 0) {
            val currentDateMillis = Date().time
            val daysInMillis = 24 * 60 * 60 * 1000
            val daysInstalled = ((currentDateMillis - compaignDateMillis) / daysInMillis).toInt()

//            Log.e("ReferrerClient","days"+daysInstalled+">>"+compaignDateMillis+">>"+currentDateMillis)
            if (daysInstalled == 3) {

//                TinyDB.getInstance(this@SplashNewActivity).putBoolean(ConstantsKT.COMPAIGN_3RD_DAY,true)
                ConstantsKT.COMPAIGN_3RD_DAY = true
            } else {
//                TinyDB.getInstance(this@SplashNewActivity).putBoolean(ConstantsKT.COMPAIGN_3RD_DAY,false)
                ConstantsKT.COMPAIGN_3RD_DAY = false
            }
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
                    if (profile.accessLevels["premium"] != null && profile.accessLevels["premium"]!!.isActive) {
                        // grant access to premium features
                        Log.e(
                            "ispurchase",
                            ">3" + profile.accessLevels["premium"]!!.isActive
                        )
                        isPurActive = true
                    }
                    if (isPurActive) {
                        TinyDB.getInstance(this)
                            .putBoolean(Constants.IS_PREMIUM, true)

                        Log.e("isSubActive", ">true")
                    } else {
                        TinyDB.getInstance(this)
                            .putBoolean(Constants.IS_PREMIUM, false)

//                        EventBus.getDefault().post(PurchaseFinishedEvent())
                        Log.e("isSubActive", ">false")
                    }
                }

                is AdaptyResult.Error -> {
                    val error = result.error
                    // handle the error
                }
            }
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected fun onMoveToForeground() {
        Log.i(
            "AppOpenAdManager",
            "onMoveToForeground:  " + AppBase.currentActivity!!.localClassName
        )
        if (AppBase.currentActivity == null) {
            return
        }
        // Show the ad (if available) when the app moves to foreground.
//        if (!TinyDB.getInstance(this).getBoolean(com.code4rox.adsmanager.Constants.IS_FIRST_TIME)) {
//            return
//        }
        if (MaxAppOpenAdManager.IS_OVERLAY_PERMISSION) {
            return
        }
        if (!com.translate.languagetranslator.freetranslation.appUtils.TinyDB.getInstance(AppBase.currentActivity).isConsentGivenGDPR) {
            return
        }
        if (MaxAppOpenAdManager.IS_CALLDORADO_SCREEN) {
            MaxAppOpenAdManager.IS_CALLDORADO_SCREEN = false
            return
        }
        if (AppBase.currentActivity is CallerIdActivity
        ) {
            return
        }
        if (TinyDB.getInstance(this)
                .getInt(ConstantsKT.COMP_SESSION_APPOPEN) == 2 && ConstantsKT.FROM_COMPAIGN
        ) {
            return
        }
        if (AppBase.currentActivity is SplashNewActivity
        ) {
            return
        }

        //show ad if user is not premium
        if (!TinyDB.getInstance(this).getBoolean(Constants.IS_PREMIUM)) {
            //appOpenAdManager!!.showAdIfAvailable(currentActivity!!)
            (application as AppBase).appOpenAdManager
                .loadAd(
                    AppBase.currentActivity!!, object : AppOpenCallback {
                        override fun isAdLoad(isLoad: Boolean) {

                        }

                        override fun isAdShown(isShow: Boolean) {

                        }

                        override fun isAdDismiss(isShow: Boolean) {
                        }
                    })
        }
    }
}