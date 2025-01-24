package com.translate.languagetranslator.freetranslation


import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.adapty.Adapty
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.calldorado.Calldorado
import com.code4rox.adsmanager.AppOpenCallback
import com.code4rox.adsmanager.BGMaxAppOpenAdManager
import com.code4rox.adsmanager.Constants.IS_FIRST_TIME
import com.code4rox.adsmanager.MaxAdManager.maxInit
import com.code4rox.adsmanager.MaxAppOpenAdManager
import com.code4rox.adsmanager.TinyDB
import com.google.firebase.FirebaseApp
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.di.AppModule
import com.translate.languagetranslator.freetranslation.extension.ConstantsKT
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class AppBase : MultiDexApplication(),
    Application.ActivityLifecycleCallbacks {

    var isAdLoaded = false
    var isAdFailed = false
    var interstitialSessionCount: Int = 0;
    var addInterstitialSessionCount: Int = 0;

    var thisActivity: Boolean = true
    var wasInBackground = false
    var isOnMainMenu = false
    var isFirstTimeOnMainMenu = true
//    private val eventHelper = EventHelper()

    var isOpened = false
        private set
    var isForeground = true
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    lateinit var appOpenAdManager: MaxAppOpenAdManager

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AppBase)
            modules(listOf(AppModule.getMainModule(this@AppBase)))
        }


        maxInit(this)
        appOpenAdManager = MaxAppOpenAdManager(this)
        TinyDB.getInstance(this)
            .putInt(
                Constants.RATEUS_FIRST_TIME, TinyDB.getInstance(this)
                    .getInt(Constants.RATEUS_FIRST_TIME) + 1
            )
        registerActivityLifecycleCallbacks(this)


        FirebaseApp.initializeApp(this)
        val environment = if (BuildConfig.DEBUG) {
            AdjustConfig.ENVIRONMENT_SANDBOX

        } else {
            AdjustConfig.ENVIRONMENT_PRODUCTION
        }
        val config = AdjustConfig(this, getString(R.string.adjust_id), environment)
        Adjust.initSdk(config)
        config.setLogLevel(LogLevel.VERBOSE)
        registerActivityLifecycleCallbacks(AdjustLifecycleCallbacks());
        Calldorado.start(this)
        Adapty.activate(getApplicationContext(), getString(R.string.adapty_sdk_key));
    }

    override fun onTrimMemory(level: Int) {

        super.onTrimMemory(level)
        isForeground = if (level == TRIM_MEMORY_UI_HIDDEN) {

            //  eventHelper.sendCloseEvent()
            false
        } else {
            true
        }

    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        if (activity.localClassName.contains("MainActivity")) {

            isOpened = true
        }
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {

            if (!isForeground) {

                //  eventHelper.sendForegroundEvent()
            }
            isForeground = true
        }

        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
//        if (!appOpenAdManager!!.isShowingAd) {
//            currentActivity = activity
//        }
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        wasInBackground = false
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            isForeground = false
            //  eventHelper.sendBackgroundEvent()
        }
        wasInBackground = true
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { /*Nothing*/
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activity.localClassName.contains("MainActivity")) {
            isOpened = false
        }
    }

    private class AdjustLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {

        }

        override fun onActivityStarted(p0: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            Adjust.onResume()
        }

        override fun onActivityPaused(activity: Activity) {
            Adjust.onPause()
        } //...

        override fun onActivityStopped(p0: Activity) {
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityDestroyed(p0: Activity) {
        }
    }


    fun showAppopenAd(
        activity: Activity,
        onShowAdCompleteListener: AppOpenCallback
    ) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        // class

        appOpenAdManager!!.loadAd(activity, onShowAdCompleteListener)
    }

    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }

    companion object {

        var currentActivity: Activity? = null
        var isSDKInitialized = true
        var isConsentGiven = false
        lateinit var appContext: Context
        var isIronSourceInit = false
    }

}