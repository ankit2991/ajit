package com.gpsnavigation.maps.gpsroutefinder.routemap

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.adapty.Adapty
import com.adapty.utils.AdaptyLogLevel
import com.adapty.utils.AdaptyResult
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.gpsnavigation.maps.gpsroutefinder.routemap.activity.SplashActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants
import com.gpsnavigation.maps.gpsroutefinder.routemap.di.AppModule
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.subscription.SubscriptionItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdAppOpen
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdAppOpenCallback
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdConstants
import com.gpsnavigation.maps.gpsroutefinder.routemap.max_ad_manager.MaxAdManager
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TimberDebugTree
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.getRemoteconfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class AppClass : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {
    public var currentActivity: Activity? = null
    var addInterstitialSessionCount: Int = 0
    var onfirstTime: Boolean = false
    //var onfirstTimeShowLoading: Boolean = false
    var isSplash: Boolean = false
    var isTimerEnd: Boolean = false
    var onfirstsessionPromo: Boolean = false
//    private var appBillingClient: AppBillingClient? = null
    var monthlySkuDetail: SubscriptionItem? = null
    lateinit var context: Context
    var ad_format = ""
    lateinit var appToken: String
    override fun onCreate() {
        super<Application>.onCreate()

        this.context = this

        com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.getInstance(this).putBoolean(
            com.gpsnavigation.maps.gpsroutefinder.routemap.utility.Constants.PrefrenceFirstSession,
            true
        )
        this.appToken = context.resources.getString(R.string.adjust_app_id)
        val environment: String = if (BuildConfig.DEBUG) {
            AdjustConfig.ENVIRONMENT_SANDBOX
        } else {
            AdjustConfig.ENVIRONMENT_PRODUCTION
        }// TODO: change to AdjustConfig.ENVIRONMENT_PRODUCTION for release
        val config = AdjustConfig(this, appToken, environment)
        config.setLogLevel(LogLevel.VERBOSE);
        Adjust.onCreate(config)

        Adapty.activate(applicationContext, getString(R.string.adapty_sdk_key))
        Adapty.logLevel = AdaptyLogLevel.VERBOSE

        checkBillingStatus()

        val getSubscriptionData = getRemoteconfig()!!.getBoolean("GPS124_01_year_sub_flag")
        val getSubscription1 = getRemoteconfig()!!.getString("GPS124_prices_value1")
        val getSubscription2 = getRemoteconfig()!!.getString("GPS124_prices_value2")

        Constants.Sub_Yearly = getSubscriptionData
        Constants.SUBSCRIPTION1 = getSubscription1
        Constants.SUBSCRIPTION2 = getSubscription2

        Log.e("subscription", "" + Constants.Sub_Yearly)
//        appBillingClient = AppBillingClient()
//        getSubscriptionDetails()


        val preLaunchLanguage =
            ConfigurationCompat.getLocales(resources.configuration)[0]?.language
        TinyDB.getInstance(this@AppClass).putString("preLaunchLanguage", preLaunchLanguage)

        if (!TinyDB.getInstance(this)
                .isPremium
        ) {
            MaxAdManager.initAppLovinSdkMax(this)
            MaxAdManager.initAmazonSdk(this)
        }

        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        Timber.plant(TimberDebugTree())
        startKoin {
            androidContext(this@AppClass)
            modules(listOf(AppModule.getModule))
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
//        registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks())

//        FacebookSdk.setIsDebugEnabled(true);  // TODO: disable debug logs before release
        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);  // TODO: disable debug logs before release

    }


//    private fun getSubscriptionDetails() {
//
//
//        appBillingClient?.connect(this, SUBSCRIPTION1, object : ConnectResponse {
//            override fun disconnected() {
//                Timber.e("InappBilling connection disconnected.")
//            }
//
//            override fun billingUnavailable() {
//                Timber.e("InappBilling billing unavailable.")
//            }
//
//            override fun developerError() {
//                Timber.e("InappBilling developer error.")
//            }
//
//            override fun error() {
//                Timber.e("InappBilling simple error.")
//            }
//
//            override fun featureNotSupported() {
//                Timber.e("InappBilling feature not available.")
//            }
//
//            override fun itemUnavailable() {
//                Timber.e("InappBilling item not available.")
//            }
//
//            override fun ok(
//                /*oneTimePurchaseItems: List<OneTimePurchaseItem>,*/
//                subscriptionItems: List<SubscriptionItem>
//            ) {
//                Timber.e("InappBilling connection ok do other .")
//                subscriptionItems.forEach {
//                    if (it.subscribedItem != null) {
//                        monthlySkuDetail = it
//                    }
//                }
//                if (monthlySkuDetail != null) {
//                    monthlySkuDetail?.let {
//                        if (it.subscribedItem != null) {
//                            com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.getInstance(
//                                context
//                            ).putBoolean(
//                                com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.IS_PREMIUM,
//                                true
//                            )
//                            it.subscribedItem?.purchaseTime?.let { it1 ->
//                                com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.getInstance(
//                                    context
//                                ).putLong(
//                                    com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.SUBSCRIPTION_ID_YEAR_BUY_TIME,
//                                    it1
//                                )
//                            }
//
//
//                        } else {
//                            com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.getInstance(
//                                context
//                            ).putBoolean(
//                                com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.IS_PREMIUM,
//                                false
//                            )
//
//                        }
//                    }
//                } else {
//                    com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.getInstance(
//                        context
//                    ).putBoolean(
//                        com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.IS_PREMIUM,
//                        false
//                    )
//                }
//            }
//
//            override fun serviceDisconnected() {
//                Timber.e("InappBilling service disconnected.")
//            }
//
//            override fun serviceUnavailable() {
//                Timber.e("InappBilling service unavailable.")
//            }
//        }, object : PurchaseResponse {
//            override fun isAlreadyOwned() {
//
//            }
//
//            override fun userCancelled(productItem: ProductItem) {
//                com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.getInstance(context)
//                    .putBoolean(
//                        com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.IS_PREMIUM,
//                        false
//                    )
//
//            }
//
//            override fun ok(productItem: ProductItem) {
//            }
//
//            override fun error(error: String) {
//
//            }
//
//        })
//
//    }

    companion object {
        var isTimerFinish = false

    }


    //Declare timer
    var cTimer: CountDownTimer? = null

    //start timer function
    fun startTimer() {
        cTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
//                Log.e("AppOpenAdManager", "${millisUntilFinished / 1000}")
            }

            override fun onFinish() {
                isTimerFinish = true
            }
        }
        cTimer?.start()
    }

    //cancel timer
    fun cancelTimer() {
        if (cTimer != null) {
//            Log.e("AppOpenAdManager", "cancel timer()")
            cTimer!!.cancel()
        }
    }

    fun md5(s: String): String {
        try {
            // Create MD5 Hash
            val digest: MessageDigest = MessageDigest
                .getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest: ByteArray = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) {
                var h = Integer.toHexString(0xFF and messageDigest[i].toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            //Logger.logStackTrace(TAG, e)
        }
        return ""
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        Adjust.onResume();
    }

    override fun onActivityPaused(activity: Activity) {
        Adjust.onPause();
    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    private var appOpenAdMax: MaxAdAppOpen? = null
    private var isAdBusy = false

    private val handler = Handler(Looper.getMainLooper())

    private val callback = Runnable {
        appOpenAdMax?.destroy()
        appOpenAdMax = null
    }

    private fun startAdLoaderTimer(seconds: Long = MaxAdConstants.MAX_AD_LOAD_TIMEOUT) {
        handler.postDelayed(callback, seconds * 1000)
    }

    private fun stopAdLoaderTimer() {
        handler.removeCallbacksAndMessages(null)
    }

    override fun onStart(owner: LifecycleOwner) {
        if (currentActivity == null || TinyDB.getInstance(this).isPremium
            || currentActivity is SplashActivity
        ) {
            appOpenAdMax?.destroy()
            appOpenAdMax = null
            return
        }

        if (appOpenAdMax == null) {
            isAdBusy = true
            appOpenAdMax = MaxAdAppOpen(
                this,
                MaxAdConstants.BACKGROUND_APP_OPEN_AD_ID,
                object : MaxAdAppOpenCallback {
                    override fun onAdDisplayFailed() {
                        isAdBusy = false
                    }

                    override fun onAdLoadFailed() {
                        stopAdLoaderTimer()
                        isAdBusy = false
                    }

                    override fun onAdClicked() {
                    }

                    override fun onAdHidden() {
                        isAdBusy = false
                    }

                    override fun onAdDisplayed() {
                    }

                    override fun onAdLoaded() {
                        stopAdLoaderTimer()
                        if (currentActivity != null && currentActivity is AppCompatActivity
                            && (currentActivity as AppCompatActivity).lifecycle
                                .currentState
                                .isAtLeast(Lifecycle.State.RESUMED)
                        ) {
                            isAdBusy =
                                appOpenAdMax != null && appOpenAdMax?.showAdIfReady(MaxAdConstants.BACKGROUND_APP_OPEN_AD_ID) == true
                        }
                    }
                }
            )
            startAdLoaderTimer()
        } else if (!isAdBusy) {
            isAdBusy = true
            appOpenAdMax?.loadAd()
            startAdLoaderTimer()
        }
    }

    private var isPurActive = false
    private fun checkBillingStatus(){
        Adapty.getProfile { result ->
            when (result) {
                is AdaptyResult.Success -> {
                    val profile = result.value
                    // check the access
                    for (item in profile.subscriptions.values){

                        Log.e("isPurchase_appclass",">2"+item.isActive)
                        if (item.isActive){
                            isPurActive = true
                        }
                    }


                    if (profile.accessLevels["premium"]?.isActive == true) {
                        // grant access to premium features
                        Log.e("ispurchase",">3"+profile.accessLevels["premium"]?.isActive)
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


                    if (isPurActive){
                        TinyDB.getInstance(this).putBoolean(
                            com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.IS_PREMIUM, true
                        )
                        Log.e("isSubActive_appclass",">true")


                    } else {
                        TinyDB.getInstance(this).putBoolean(
                            com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.IS_PREMIUM, false
                        )
                        Log.e("isSubActive_appclass",">false")

                    }


//                val prof= profile.accessLevels.values
//                    for (item in prof){
//                        item.isActive
//                    }


                }
                is AdaptyResult.Error -> {
                    val error = result.error

                    // handle the error
                }
            }
        }
    }
}