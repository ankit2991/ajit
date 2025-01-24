package com.translate.languagetranslator.freetranslation

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
//import com.android.billingclient.api.AcknowledgePurchaseParams
//import com.android.billingclient.api.ProductDetails
//import com.android.billingclient.api.Purchase
//import com.android.billingclient.api.SkuDetails
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.appUtils.Constants.IS_PREMIUM
import com.translate.languagetranslator.freetranslation.appUtils.Constants.SUBSCRIPTION_ID_YEAR
//import com.translate.languagetranslator.freetranslation.billing.BillingCallbacks
//import com.translate.languagetranslator.freetranslation.billing.BillingProcess
import com.translate.languagetranslator.freetranslation.viewmodels.SplashViewModel
import com.translate.languagetranslator.freetranslation.views.CustomDialog
import kotlinx.android.synthetic.main.activity_subscription_new_design.*
import kotlinx.android.synthetic.main.item_subscription_detail.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class SubscriptionActivityN : AppCompatActivity()
//    , BillingCallbacks
{

//    val splashViewModel: SplashViewModel by viewModel()
//    private var readyToPurchase = false
//
//    private var billingProcess: BillingProcess? = null
//    private var subsYearly: ProductDetails? = null
//    private var openedFrom: String? = null
//    private var freeTrialDialog: Dialog? = null
//    private var showInterstitial = true
//    private var maxInterstitialSession: Int = 3
//    private lateinit var mFireBaseRemoteConfig: FirebaseRemoteConfig
//    private lateinit var configSettings: FirebaseRemoteConfigSettings
//
//    private val runnable =
//        Runnable { AnimUtils.animateRightLeft(this, btn_start_free_trial) }
//    private var animHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        window.decorView.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        setContentView(R.layout.activity_subscription_new_design)
//        initRemoteConfig()
//        val bundle = intent.extras
//        bundle?.let {
//            openedFrom = it.getString("from")
//        }
//        setViews()
//        initBillingProcess()
//        btnDetail.setOnClickListener {
//            val detailView = layoutInflater.inflate(R.layout.item_subscription_detail, null, false)
//            detailView.ivCancel.setOnClickListener {
//                CustomDialog.getInstance(this)?.dismissDialog()
//            }
//            CustomDialog.getInstance(this)?.setContentView(detailView, true)?.showDialog()
//        }
    }

    private fun setViews() {

////        initDialog()
//        btn_start_free_trial.setOnClickListener {
//            subscribeFreeTrial(SUBSCRIPTION_ID_YEAR)
//        }
//        iv_cross_subscription_plan.setOnClickListener {
//            goToNext()
//        }
//        tv_terms_of_services.setOnClickListener {
//            openPrivacyPolicyView(
//                this,"https://zedlatino.info/TermsOfUse.html"
//
//            )
//        }
//        animHandler = Handler()
//        animHandler?.postDelayed(runnable, 600)

    }


//    private fun subscribeFreeTrial(subscriptionKey: String) {
////        if (readyToPurchase) {
//        billingProcess?.let { process ->
//            if (process.isBillingProcessReady) {
//                subsYearly?.let {
//                    putPrefBoolean("is_feedback", true)
//                    process.launchPurchaseFlow(it)
//                }
//            } else showToast(this, "Billing process not initialized. Please wait")
//
//        }
////        } else {
////            showToast(this, "Billing process not initialized. Please wait")
////
////        }
//    }
//    private fun initRemoteConfig() {
//        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
//        configSettings =
//            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(200).build()
//        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
//        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)
//        maxInterstitialSession = mFireBaseRemoteConfig.getDouble(RemoteConfigConstants.MAX_INTERSTITIAL_SESSION).toInt()
//
//
//    }
//
//    private fun initBillingProcess() {
//
//        val skuList = ArrayList<String>()
//        skuList.add(SUBSCRIPTION_ID_YEAR)
//        billingProcess = BillingProcess(this, this, null, skuList, false)
//
//
////        billingProcessor = BillingProcessor(
////            this,
////            Constants.LICENSE_KEY,
////            Constants.MERCHANT_ID,
////            object : BillingProcessor.IBillingHandler {
////                override fun onProductPurchased(
////                    productId: String,
////                    details: TransactionDetails?
////                ) {
////                    putPrefBoolean(Constants.IS_PREMIUM, true)
////                    goToNext()
////                }
////
////                override fun onPurchaseHistoryRestored() {
////                    /*         for (sku in billingProcessor!!.listOwnedProducts()) {
////                                 when (sku) {
////                                     getString(R.string.in_app_key) -> {
////                                         putPrefBoolean(Constants.IS_PREMIUM, true)
////                                     }
////                                     else -> {
////                                         putPrefBoolean(Constants.IS_PREMIUM, false)
////                                     }
////                                 }
////                             }
////                             for (sku in billingProcessor!!.listOwnedSubscriptions()) {
////                                 if (sku == SUBSCRIPTION_ID_YEAR) {
////                                     putPrefBoolean(Constants.IS_PREMIUM, true)
////                                 } else {
////                                     putPrefBoolean(Constants.IS_PREMIUM, false)
////                                 }
////                             }*/
////                }
////
////                override fun onBillingError(
////                    errorCode: Int,
////                    error: Throwable?
////                ) {
////                }
////
////                override fun onBillingInitialized() {
////                    readyToPurchase = billingProcessor!!.isOneTimePurchaseSupported
////                    CoroutineScope(Dispatchers.IO).launch {
////                        val removeAdsYear =
////                            billingProcessor!!.getSubscriptionListingDetails(SUBSCRIPTION_ID_YEAR)
////                        if (removeAdsYear != null) {
////                            val price = removeAdsYear.priceText
////                            val monthlyPrice = "3 days free trial then $price /year, cancel anytime"
////                            withContext(Dispatchers.Main) {
////                                tv_free_trial.text = monthlyPrice
////                            }
////                        }
////
////
////                    }
////                }
////            })
////        billingProcessor!!.initialize()
//    }
//
//    override fun onBackPressed() {
//        goToNext()
//    }
//
//    fun goToNext() {
//        putPrefBoolean(Constants.SPLASH_FIRST, false)
//            finish()
//
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//    }
//
//    override fun billingInitialized() {
//
//        billingProcess?.let { process ->
//
//            val subscriptionDetailList = process.getSubscriptionDetailList()
//            subscriptionDetailList?.let { skuDetailsList ->
//
//                for (sku in skuDetailsList) {
//
//                    if (sku!!.productId == SUBSCRIPTION_ID_YEAR) {
//                        subsYearly = sku
//                    }
//                }
//            }
//
//        }
//
//
//    }
//
//
//
//
//    override fun billingPurchased(purchases: MutableList<Purchase>?) {
//        purchases?.let { purchaseList ->
//            for (sku in purchaseList) {
//                handlePurchase(sku)
//            }
//        }
//    }
//
//    override fun billingCanceled() {
//        TinyDB.getInstance(this).putBoolean(IS_PREMIUM, false)
//
//    }
//
//    override fun itemAlreadyOwned() {
//
//    }
//
//    override fun onQuerySkuDetailsSubscription(skuDetailsList: MutableList<ProductDetails>?) {
//        if (skuDetailsList != null) {
//
//            for (sku in skuDetailsList) {
//                if (sku.productId == SUBSCRIPTION_ID_YEAR) {
//                    subsYearly = sku
//
//                    val price = subsYearly!!.oneTimePurchaseOfferDetails!!.formattedPrice
//                    val monthlyPrice = "3 days free trial then $price /year, cancel anytime"
//                    tvPrice.text = monthlyPrice
//
////                            withContext(Dispatchers.Main) {
////                            }
//
////                    setMonthlyPrices(subsMonthly)
//                }
//            }
//        }
//
//    }
//
//    override fun onQuerySkuDetailsInApp(skuDetailsList: MutableList<ProductDetails>?) {
//
//    }
//
//    override fun queryPurchaseResultInApp(purchases: MutableList<Purchase>?) {
//
//    }
//
//    override fun queryPurchaseResultSubscription(purchases: MutableList<Purchase>?) {
//        purchases?.let { purchaseList ->
//            for (sku in purchaseList) {
//                handlePurchase(sku)
//            }
//        }
//    }
//
//    private fun handlePurchase(purchase: Purchase) {
//        if (purchase.skus.contains(SUBSCRIPTION_ID_YEAR) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//            if (!purchase.isAcknowledged) {
//
//                val params = AcknowledgePurchaseParams.newBuilder()
//                    .setPurchaseToken(purchase.purchaseToken)
//                    .build()
//
//                billingProcess!!.getBillingClient()?.acknowledgePurchase(
//                    params
//                ) { billingResult ->
//                    //TinyDB.getInstance(this).putBoolean(Constants.IS_PREMIUM, true)
//                    Log.e("billing", "if splash+ Purchase"+billingResult.responseCode)
//                    setUserAsPremium(purchase.purchaseTime)
//                    goToNext()
//
//
//                }
//
//            } else {
//                Log.e("billing", "if subscritionactivity+ Purchase")
//                //TinyDB.getInstance(this).putBoolean(Constants.IS_PREMIUM, true)
//                setUserAsPremium(purchase.purchaseTime)
//                goToNext()
//            }
//        }
//
//    }





}