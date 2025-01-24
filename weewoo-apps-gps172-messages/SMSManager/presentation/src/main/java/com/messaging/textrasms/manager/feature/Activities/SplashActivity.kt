package com.messaging.textrasms.manager.feature.Activities


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import androidx.core.os.postDelayed
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.ump.FormError
import com.messaging.textrasms.manager.BuildConfig
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.FirebaseHelper
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.interfaces.moveNextCallBack
import com.messaging.textrasms.manager.common.interfaces.onAdfailedToLoadListner
import com.messaging.textrasms.manager.common.maxAdManager.AppOpenCallback
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdListener
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.common.maxAdManager.MaxAppOpenAdManager
import com.messaging.textrasms.manager.common.util.MaxMainAdsManger
import com.messaging.textrasms.manager.common.util.PermissionHandler
import com.messaging.textrasms.manager.common.util.RemoteConfigHelper
import com.messaging.textrasms.manager.feature.ads.AdvertiseHandler
import com.messaging.textrasms.manager.feature.quimera.QuimeraInit
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.utils.AdsConsentManager
import com.messaging.textrasms.manager.utils.Constants
import com.messaging.textrasms.manager.utils.RemoteConfig
import dagger.android.AndroidInjection
import games.moisoni.google_iab.BillingConnector
import games.moisoni.google_iab.BillingEventListener
import games.moisoni.google_iab.enums.ErrorType
import games.moisoni.google_iab.enums.ProductType
import games.moisoni.google_iab.models.BillingResponse
import games.moisoni.google_iab.models.ProductInfo
import games.moisoni.google_iab.models.PurchaseInfo
import xyz.teamgravity.checkinternet.CheckInternet
import java.util.Arrays
import java.util.Locale
import java.util.Timer
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.schedule


class SplashActivity : QkThemedActivity(), moveNextCallBack {
    var adsIntSuccess = false
    var appOpenShown = false;
    var inappsChecked = false;
    var ironSourceLoaded = false;
    val ORGANIC_REFERRAL_KEY = "utm_medium=organic"

    private lateinit var referrerClient: InstallReferrerClient
    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }

    var activity: Activity? = null
    private var timer: CountDownTimer? = null
    private var alertDialog:AlertDialog? = null
    var moved = false;

    private val TAG = SplashActivity::class.java.simpleName
    private var SPLASH_SCREEN_TIME: Long = 3500
    private val APP_UPDATE_CODE = 2036
    private var appUpdateManager: AppUpdateManager? = null
    private var isAppChecked = false
    var adsConsentManager: AdsConsentManager? = null
    var appOpenAdManager: MaxAppOpenAdManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.splash_activity)

        adsConsentManager = AdsConsentManager(this)
//        AppUtils.showNoInternetDialog(this,lifecycle)

        if (savedInstanceState == null) {
            QuimeraInit.initQuimeraSdk(applicationContext)
        }

        RemoteConfig.getRemoteconfig()



        initData()


        activity = this@SplashActivity
        initBilling()
        AdvertiseHandler.getInstance(this).isNeedOpenAdRequest = false
        MaxMainAdsManger.initSdk(this)
//        IntegrationHelper.validateIntegration(this)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        //checkUpdate()
        Log.e("TAG", "check splashActivity create")
        FirebaseHelper.sendSplashViewEvent()
        if (!Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
            MaxAdManager.loadInterAd(this, object : MaxAdListener {
                override fun onAdLoaded(adLoad: Boolean) {

                }

                override fun onAdShowed(adShow: Boolean) {
                }

                override fun onAdHidden(adHidden: Boolean) {
                }

                override fun onAdLoadFailed(adLoadFailed: Boolean) {
                    MaxAdManager.loadInterAd(this@SplashActivity)
                }

                override fun onAdDisplayFailed(adDisplayFailed: Boolean) {
                }

            })
        }

    }


    private fun initData(){
        val canRequestAds =
            if (adsConsentManager != null) adsConsentManager!!.canRequestAds else null
        if (canRequestAds != null && !canRequestAds && !Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
            adsConsentManager!!.showGDPRConsent(
                this@SplashActivity,
                BuildConfig.DEBUG
            ) { formError: FormError? ->
                if (formError != null) {
                    Log.w(
                        "ConsentManger",
                        formError.errorCode.toString() + ": " + formError.message
                    )
                }
                CheckInternet().check(){
                        connected ->
                    if (connected){
                        compaignTracking()
                    }else{
                        showDialogNoInternet()
                    }
                }
                null
            }
        } else {
            CheckInternet().check(){
                    connected ->
                if (connected){
                    compaignTracking()
                }else{
                    showDialogNoInternet()
                }
            }
        }

    }


    private val installStateUpdatedListener: InstallStateUpdatedListener =
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(state: InstallState) {
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                    popupSnackBarForCompleteUpdate()
                } else if (state.installStatus() == InstallStatus.INSTALLED) {
                    if (appUpdateManager != null) {
                        appUpdateManager!!.unregisterListener(this)
                    }
                } else {
                    Log.i(TAG, "InstallStateUpdatedListener: state: " + state.installStatus())
                }
            }
        }

    private fun popupSnackBarForCompleteUpdate() {

        Log.i(TAG, "popupSnackBarForCompleteUpdate: ")

        val snackBar = Snackbar.make(
            findViewById(R.id.main_contain),
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        )


        snackBar.setAction("RESTART") {
            if (appUpdateManager != null) {
                appUpdateManager!!.completeUpdate()
            }
        }

        snackBar.setActionTextColor(Color.GREEN)

        snackBar.show()

    }
/*
    private fun checkUpdate() {
        Log.i(TAG, "checkUpdate: ")
        if (appUpdateManager != null && !isAppChecked) {
            isAppChecked = true
            appUpdateManager!!.registerListener(installStateUpdatedListener)
            val appUpdateInfoTask = appUpdateManager!!.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                Log.i(TAG, "onSuccess: ")
                if (appUpdateManager != null && appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    || appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    try {
                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) || appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                            appUpdateManager!!.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this@SplashActivity,
                                APP_UPDATE_CODE
                            )
                        } else {
                            appUpdateManager!!.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.FLEXIBLE,
                                this@SplashActivity,
                                APP_UPDATE_CODE
                            )
                        }
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                        Log.i(TAG, "onCreate: error :- " + e.message)
                        updateProgressFinish()
                    }
                } else {
                    updateProgressFinish()
                }
            }.addOnFailureListener { e: Exception ->
                Log.i(TAG, "onFailure: e :- " + e.message)
                updateProgressFinish()
            }
        } else {
            updateProgressFinish()
        }
    }*/

    private fun updateProgressFinish() {
        gotoFinal()
    }

    private fun gotoFinal() {
        val admobAdManager = AdvertiseHandler.getInstance(this)
        admobAdManager.isAppStartUpAdsEnabled = true
        admobAdManager.isAppStartUpAdsPause = false

        handleAd()


    }

    private fun moveToHome() {
//        if (!this.isDestroyed) {
//
//            Timer().schedule(4000) {
//
//                if (RemoteConfigHelper.initialized) {
//
//                    val mandatory: Long = RemoteConfigHelper.get("GPS172_forced_update", 0L)
//                    val voluntary: Long = RemoteConfigHelper.get("GPS172_voluntary_update", 0L)
//
//                    runOnUiThread {
//                        if (BuildConfig.VERSION_CODE < mandatory) {
//
//                            showUpdateDialog(true)
//                        } else if (BuildConfig.VERSION_CODE < voluntary) {
//
//                            showUpdateDialog(false)
//                        } else {
//
//                            nextScreen()
//                        }
//                    }
//                } else {
                    nextScreen()
//                }
//            }
//        }

    }

    fun showUpdateDialog(force: Boolean) {
        val builder = AlertDialog.Builder(this@SplashActivity)
            .setTitle(if (force) R.string.force_update_title else R.string.sugest_update_title)
            .setMessage(if (force) R.string.force_update_message else R.string.sugest_update_message)
            .setPositiveButton(R.string.update_ok) { _, _ ->
                val store = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                )
                startActivity(store)
            }
        if (!force) {
            builder.setNegativeButton(
                R.string.update_no
            ) { dialog, _ ->
                dialog.dismiss()
                nextScreen()
            }
        }
        builder.setCancelable(false)
        builder.show()
    }

    private var nextScreenCalled = false

    private fun nextScreen() {
        if (!nextScreenCalled && appOpenShown && inappsChecked ) {
            nextScreenCalled = true

            val numberOfPaymentCardAfterOnBoard: Long =
                RemoteConfigHelper.get(RemoteConfigHelper.GPS172_PAYMENT_CARD_POSITION, 6L)
            if (numberOfPaymentCardAfterOnBoard <= 0L && !Preferences.getBoolean(this, Preferences.INTRODUCTION))
            {
                Preferences.setBoolean(this, Preferences.HAS_DISPLAYED_FIRST_PAYMENT_CARD, false)
                startActivity(Intent(this@SplashActivity, PurchaseActivity::class.java))
            }
            else
            {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            finish()
        }

//        else{
//            val intent = Intent(this@SplashActivity, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
//            finish()
//        }
    }

    private fun handleAd() {
        ///interstitial ad load
        if (getConnectionType(this) == 1 || getConnectionType(this) == 2 || getConnectionType(this) == 3) {
            MaxMainAdsManger.initiateAd(this@SplashActivity, object : onAdfailedToLoadListner {
                override fun onAdFailedToLoad() {
                    ironSourceLoaded = true
                    moveToHome()
                    Log.e("TAG", "ad load failed")
                }
                override fun onSuccess() {
                    ironSourceLoaded = true
                    moveToHome()
                    Log.e("TAG", "ad load successfully in splash")
                    MaxMainAdsManger.canShowInter = true;
                }
                override fun adClose() {
                }
            })

        } else {
            ironSourceLoaded = true
            moveToHome()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        AdvertiseHandler.getInstance(this).isNeedOpenAdRequest = false
        if (requestCode == PermissionHandler.PERMISSION_REQ_CODE) {
            gotoFinal()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            APP_UPDATE_CODE -> if (resultCode != RESULT_OK) {
                Log.e(TAG, "Update flow failed! Result code: \$resultCode")
            } else {
                //checkUpdate()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop: ")
        if (appUpdateManager != null) {
            appUpdateManager!!.unregisterListener(installStateUpdatedListener)
        }
    }

//    @Throws(InterruptedException::class, IOException::class)
//    fun isConnected(): Boolean {
//        val command = "ping -c 1 google.com"
//        return Runtime.getRuntime().exec(command).waitFor() == 0
//    }

//    override fun moveToNext() {
//
//        Timer().schedule(5000) {
//
//            moveToHome()
//        }
//    }

    ///billing method
    private fun updatePrefBoolean(isPurchased: Boolean) {
        Preferences.setBoolean(activity!!, Preferences.ADSREMOVED, isPurchased)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (billingConnector != null) {
            billingConnector?.release();
        }

        if (timer !=null){
            timer!!.cancel()
        }
        if (alertDialog !=null && alertDialog!!.isShowing){
            alertDialog!!.dismiss()
        }

//        if(referrerClient != null){
//            referrerClient.endConnection()
//        }
    }

    private var billingConnector: BillingConnector? = null

    private fun initSubFromRemoteConfig() {
        RemoteConfigHelper.initialize()
        val numberOfPaymentCardAfterOnBoard = RemoteConfigHelper.get(
            RemoteConfigHelper.GPS172_PAYMENT_CARD_POSITION,
            6L
        )
        Log.d(
            "RemoteConfig",
            "${RemoteConfigHelper.initialized} -- GPS172_paymentcard_onboardingposition: --- $numberOfPaymentCardAfterOnBoard"
        )
    }

    fun getConnectionType(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
//        val cm: ConnectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (cm != null) {
//                val capabilities: NetworkCapabilities? =
//                    cm.getNetworkCapabilities(cm.getActiveNetwork())
//                if (capabilities != null) {
//                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                        result = 2
//                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                        result = 1
//                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
//                        result = 3
//                    }
//                }
//            }
//        } else {
//            if (cm != null) {
//                val activeNetwork: NetworkInfo? = cm.getActiveNetworkInfo()
//                if (activeNetwork != null) {
//                    // connected to the internet
//                    if (activeNetwork.getType() === ConnectivityManager.TYPE_WIFI) {
//                        result = 2
//                    } else if (activeNetwork.getType() === ConnectivityManager.TYPE_MOBILE) {
//                        result = 1
//                    } else if (activeNetwork.getType() === ConnectivityManager.TYPE_VPN) {
//                        result = 3
//                    }
//                }
//            }
//        }

        CheckInternet().check(){
                connected ->
            if (connected){
                result = 1
            }else{
                result = 0
            }
        }
        return result
    }

    private val SKUs = Arrays.asList(
        "com.messaging.textrasms.manager_29.99",
        "com.messaging.textrasms.manager_9.99",
        "com.messaging.textrasms.manager_2.99"
    )

    private fun initBilling() {


        billingConnector = BillingConnector(this, getString(R.string.base64key))
//            .setConsumableIds(nonConsumableIds)
//            .setNonConsumableIds(SKUS_IN_APP)
            .setSubscriptionIds(SKUs)
            .autoAcknowledge()
            .autoConsume()
            .enableLogging()
            .connect()



        billingConnector?.setBillingEventListener(object : BillingEventListener {
            override fun onProductsFetched(skuDetails: List<ProductInfo>) {
                /*Provides a list with fetched products*/

                if (skuDetails.isNotEmpty()) {

                }
            }

            override fun onPurchasedProductsFetched(
                skuType: ProductType,
                purchases: MutableList<PurchaseInfo>
            ) {

                /*Provides a list with fetched purchased products*/
                if(skuType==ProductType.SUBS) {

                    if (purchases.isEmpty()) {
                        updatePrefBoolean(false)
                        Log.e("Tag", "inappPurchase>>>notpurchase")
                    } else {
                        updatePrefBoolean(true)
                        Log.e("Tag", "inappPurchase>>>purchase")
                    }

                    inappsChecked = true
                    moveToHome()
                }

            }

            override fun onProductsPurchased(purchases: List<PurchaseInfo>) {


            }

            override fun onPurchaseAcknowledged(purchase: PurchaseInfo) {

                /*Callback after a purchase is acknowledged*/

                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */
            }

            override fun onPurchaseConsumed(purchase: PurchaseInfo) {
                /*Callback after a purchase is consumed*/

                /*
                 * CONSUMABLE products entitlement can be granted either here or in onProductsPurchased
                 * */
            }

            override fun onBillingError(
                billingConnector: BillingConnector,
                response: BillingResponse
            ) {

                inappsChecked = true
                moveToHome()
                showLogs(response.debugMessage)
//                Toast.makeText(this@SubscriptionActivity,"${response.debugMessage}",Toast.LENGTH_LONG).show()
                /*Callback after an error occurs*/
                when (response.errorType) {
                    ErrorType.CLIENT_NOT_READY -> {
                    }
                    ErrorType.CLIENT_DISCONNECTED -> {
                    }

                    ErrorType.CONSUME_ERROR -> {
                    }
                    ErrorType.ACKNOWLEDGE_ERROR -> {
                    }
                    ErrorType.ACKNOWLEDGE_WARNING -> {
                    }
                    ErrorType.FETCH_PURCHASED_PRODUCTS_ERROR -> {
                    }
                    ErrorType.BILLING_ERROR -> {
                    }
                    ErrorType.USER_CANCELED -> {
                    }
                    ErrorType.SERVICE_UNAVAILABLE -> {
                    }
                    ErrorType.BILLING_UNAVAILABLE -> {
                    }
                    ErrorType.ITEM_UNAVAILABLE -> {
                    }
                    ErrorType.DEVELOPER_ERROR -> {
                    }
                    ErrorType.ERROR -> {
                    }
                    ErrorType.ITEM_ALREADY_OWNED -> {
                    }
                    ErrorType.ITEM_NOT_OWNED -> {
                    }

                    else -> {}
                }
            }
        })

    }


    private fun showLogs(msg: String) {

        Log.e("billing ", "=============================\n")
        Log.e("billing ", msg)
        Log.e("billing ", "\n=============================")

    }


    fun compaignTracking(){
        Log.e("calldorado","Run")
//        Calldorado.start(this, object: Calldorado.FullCallback{
//            override fun onInitDone(
//                mayUseCallerID: Boolean,
//                permissionNames: Array<String?>?,
//                permissionResponse: IntArray?
//            ) {
//                val hostAppDataConfig = Calldorado.getHostAppDataFromServer(applicationContext)
//                var isCampaignUser = false
//
//                if (!hostAppDataConfig.list.isEmpty()) {
//                    for (hostAppData in hostAppDataConfig.list) {
//                        Log.e("campaign_name",">"+hostAppData.key)
//                        if (hostAppData.key.contains("SOC-MSG")) {
//                            val campaignName = hostAppData.value
//
//                            Log.d(TAG, "User comes from $campaignName campaign")
//                            isCampaignUser = true
//                            break
//                        }
//                    }
//                    if (!isCampaignUser) {
//                        // Disable CDO here
//                        // Treat the user as organic and do your own stuff
//                        Constants.IS_COMPAIGN_USER = false
//                        Log.e("calldorado","not come from campaign<")
//
//                        showAppOpen()
//                    }else{
//                        Constants.IS_COMPAIGN_USER = true
//                        Log.e("calldorado","come from campaign")
//                        var compaignUserSession = Preferences.getIntVal(this,Preferences.COMPAIGN_USER_SESSION)
//                        var session = compaignUserSession + 1
//                        Preferences.setIntVal(this,Preferences.COMPAIGN_USER_SESSION,session)
//
//                        showAppOpen()
//                    }
//                } else {
//                    // Disable CDO here
//                    // Treat the user as organic and do your own stuff
//                    Constants.IS_COMPAIGN_USER = false
//                    Log.e("calldorado","not come from compaing>")
//                    showAppOpen()
//                }
//            }
//        });


        //check update

        if (!this.isDestroyed) {

            Timer().schedule(1000) {

                if (RemoteConfigHelper.initialized) {

                    val mandatory: Long = RemoteConfigHelper.get("GPS172_forced_update", 0L)
                    val voluntary: Long = RemoteConfigHelper.get("GPS172_voluntary_update", 0L)

                    runOnUiThread {
                        if (BuildConfig.VERSION_CODE < mandatory) {

                            showUpdateDialog(true)
                        } else if (BuildConfig.VERSION_CODE < voluntary) {

                            showUpdateDialog(false)
                        } else {

                            showAppOpen()
                        }
                    }
                } else {
                    showAppOpen()
                }
            }
        }




    }


    fun showAppOpen(){

//        Log.e("appopencall","here")
        if (!Preferences.getBoolean(this,Preferences.IS_FIRST_SESSION)) {

            appOpenShown = true
            moveToHome()


        }else{

            if (!Preferences.getBoolean(applicationContext, Preferences.ADSREMOVED)){
                appOpenAdManager = MaxAppOpenAdManager(applicationContext)
                //MaxAdAppOpenShow>>done
                appOpenAdManager?.loadAd(this@SplashActivity,object:AppOpenCallback{
                    override fun isAdLoad(isLoad: Boolean) {
                    }

                    override fun isAdShown(isShow: Boolean) {
                    }

                    override fun isAdDismiss(isShow: Boolean) {
                        appOpenShown = true
                        moveToHome()
                    }

                })
            }else {
//            Preferences.getBoolean(applicationContext, Preferences.ADSREMOVED)

                appOpenShown = true
                moveToHome()
            }



        }
    }

    private val TIMER_DURATION: Long = 5000 // 5 seconds


    fun showDialogNoInternet(){

        if (isFinishing) {
            return  // Activity is finishing, don't show the dialog
        }
        // Create an instance of the AlertDialog.Builder
        val builder = AlertDialog.Builder(this@SplashActivity)
            builder.setCancelable(false)

        // Inflate the custom dialog layout
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.no_internet_dialog, null)

        // Set the custom view for the dialog
        builder.setView(dialogView)

        // Find and customize the dialog components
        val closeButton = dialogView.findViewById<Button>(R.id.btn_retry)


        // Create and show the dialog
         alertDialog = builder.create()
        if (!isFinishing) {
            alertDialog!!.show()
        }

        closeButton.isEnabled = false
        closeButton.setOnClickListener {

            CheckInternet().check(){
                connected ->
                if (connected){
                    val intent = Intent(this@SplashActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }else{
                    Handler(Looper.getMainLooper()).postDelayed(2000) {
//                       alertDialog.show()
                        showDialogNoInternet()
                    }
                }
            }
            if (!isFinishing) {
                alertDialog!!.dismiss()
            }
        }

        // Start the timer
        timer = object : CountDownTimer(TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the retry button text with the remaining time
                closeButton.setText(
                    String.format(
                        Locale.getDefault(),
                        "Retry (%d)",
                        millisUntilFinished / 1000
                    )
                )
            }

            override fun onFinish() {
                // Timer finished, dismiss the dialog
                closeButton.isEnabled = true
                closeButton.setText("Retry")
                closeButton.setTextColor(resources.getColor(R.color.white))
                closeButton.setBackgroundColor(resources.getColor(com.llollox.androidtoggleswitch.R.color.blue))
            }
        }.start()
    }


}