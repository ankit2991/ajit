package com.messaging.textrasms.manager.common.base

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.os.Bundle
import android.widget.Toast
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.multidex.MultiDexApplication
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.messaging.textrasms.manager.BuildConfig
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.FirebaseHelper
import com.messaging.textrasms.manager.common.PlayServicesHelper
import com.messaging.textrasms.manager.common.util.RemoteConfigHelper
import com.messaging.textrasms.manager.common.util.TypefaceUtil
import com.messaging.textrasms.manager.feature.ads.AdvertiseHandler
import com.messaging.textrasms.manager.injection.AppComponentManager
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.migration.QkMigration
import com.messaging.textrasms.manager.migration.QkRealmMigration
import com.messaging.textrasms.manager.model.logDebug
import com.messaging.textrasms.manager.util.NightModeManager
import com.messaging.textrasms.manager.util.Preferences
import com.uber.rxdogtag.RxDogTag
import com.uber.rxdogtag.autodispose.AutoDisposeConfigurer
import com.yariksoffice.lingver.Lingver
import dagger.android.*
import dagger.android.support.HasSupportFragmentInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.Locale
import javax.inject.Inject


class QKApplication : MultiDexApplication(), HasActivityInjector, HasBroadcastReceiverInjector,
    HasServiceInjector, HasSupportFragmentInjector, Application.ActivityLifecycleCallbacks {

    @Inject
    lateinit var dispatchingFragmentInjector: DispatchingAndroidInjector<androidx.fragment.app.Fragment>

    @Inject
    lateinit var qkMigration: QkMigration

    @Inject
    lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingBroadcastReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var nightModeManager: NightModeManager

    @Inject
    lateinit var realmMigration: QkRealmMigration


    private var isForeground = true
    private var isOpened = false
    var currentActivity: Activity? = null
        private set
    private var activityReferences = 0
    private var isActivityChangingConfigurations = false

    //    var appOpenManager: AppOpenManager? = null
    override fun onCreate() {
        super.onCreate()

        PlayServicesHelper.isGooglePlayServicesAvailable(this)
        AppComponentManager.init(this)

        logDebug("takingtime" + "3")
        appComponent.inject(this)
        logDebug("takingtime" + "4")
        val appToken = getString(R.string.adjust_id)


        val config = AdjustConfig(this, appToken, if (BuildConfig.DEBUG) { AdjustConfig.ENVIRONMENT_SANDBOX}else{AdjustConfig.ENVIRONMENT_PRODUCTION })



            if (Preferences.getStringVal(this, Preferences.SELECTED_LANGUAGE) == ""
            ) {
                val systemLanguage = Locale.getDefault().language
//                Toast.makeText(this,systemLanguage,Toast.LENGTH_LONG).show()
                Lingver.init(this, systemLanguage)
            } else {
                Lingver.init(
                    this,
                    Preferences.getStringVal(this, Preferences.SELECTED_LANGUAGE)!!
                )
            }

        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance( this ).setMediationProvider( "max" )
        AppLovinSdk.getInstance( this ).initializeSdk({ configuration: AppLovinSdkConfiguration ->
            // AppLovin SDK is initialized, start loading ads

             isSDKInitialized = true

            //disable debugger for release and enable for debug
            if(!BuildConfig.DEBUG) {
                AppLovinSdk.getInstance(this).settings.setCreativeDebuggerEnabled(false)
            }
        })



        Adjust.initSdk(config)
        registerActivityLifecycleCallbacks(this)

//      MobileAds.setRequestConfiguration(RequestConfiguration.Builder().setTestDeviceIds(listOf("EC15AEF446BD7ED9BFA8F4F6B5458E47")).build())
//      MobileAds.initialize(applicationContext) { Log.i("TAG", "onInitializationComplete: ")}

//        Calldorado.start(this)



        AdvertiseHandler.getInstance(this)
        try {
            Realm.init(this)
        }catch (e:Exception){
            e.printStackTrace()
        }

        logDebug("takingtime" + "6")
        Realm.setDefaultConfiguration(
            RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .compactOnLaunch()
                .migration(realmMigration)
                .schemaVersion(QkRealmMigration.SchemaVersion)
                .build()
        )

        qkMigration.performMigration()
        nightModeManager.updateCurrentTheme()

        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            R.array.com_google_android_gms_fonts_certs
        )
        logDebug("takingtime" + "7")
        EmojiCompat.init(FontRequestEmojiCompatConfig(this, fontRequest))

        RxDogTag.builder()
            .configureWith(AutoDisposeConfigurer::configure)
            .install()
        logDebug("takingtime" + "8")
        TypefaceUtil.setDefaultFont(this)
        logDebug("takingtime" + "9")

//        mBilling.addPlayStoreListener {
//            Toast.makeText(
//                this@QKApplication,
//                "Changed",
//                Toast.LENGTH_LONG
//            ).show()
//        }

        RemoteConfigHelper.initialize()
    }

//    private val mBilling = Billing(this, object : DefaultConfiguration() {
//        @Nonnull
//        override fun getPublicKey(): String {
//            val s =
//                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnNVOxUBcFbePOVa7NgEu7wCv9t1SGFtxvMNYwgOJwEp7+HhR6Je59fbqK9FrHwQm7Pxe0EgVrXzXnRGnFJ3RD48btLnkmnUt4cCqAA4tb1gVFwJLpeUUVXkCKdUcWTnyTEVYYKtDgoXdGz5fLq0sppLMP2p9cHFYP7IMw7TtKYRWnoFBkjmbf79g2bIupqDRo2rUdP4MZbhmOdC0f0JfeCoU422rxneiJYMx+fKZYAUPz6th27GoBZg6Hmnu28/ekRdBZ131MxNvFb4jtE/GXOYlin+tA/LYLsmtvmrNO5cBafWE3z5RycPK5raAcgSTl0mwRcyTemDRmgMXo7vVuQIDAQAB"
//            return Encryption.decrypt(s, "messageapp")
//        }
//    })

//    @Nonnull
//    fun getBilling(): Billing? {
//        return mBilling
//    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return dispatchingActivityInjector
    }

    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> {
        return dispatchingBroadcastReceiverInjector
    }

    override fun serviceInjector(): AndroidInjector<Service> {
        return dispatchingServiceInjector
    }

    override fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment> {
        return dispatchingFragmentInjector
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        isOpened = true
    }

    override fun onActivityStarted(activity: Activity) {

        currentActivity = activity
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            if (!isForeground) {
                FirebaseHelper.sendForegroundEvent()
            }
            isForeground = true
        }

    }

    override fun onActivityResumed(activity: Activity) {
        Adjust.onResume()
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        Adjust.onPause()
//        TODO("Not yet implemented")
    }

    override fun onActivityStopped(activity: Activity) {

        isActivityChangingConfigurations = activity.isChangingConfigurations
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            isForeground = false
            FirebaseHelper.sendBackgroundEvent()
        }

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
//        TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(activity: Activity) {
//        TODO("Not yet implemented")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        isForeground = if (level == TRIM_MEMORY_UI_HIDDEN) {
            FirebaseHelper.sendCloseEvent()
            false
        } else {
            true
        }
    }



    companion object
    {
        var isSDKInitialized = false
    }


}