package com.translate.languagetranslator.freetranslation.activities.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
//import com.android.billingclient.api.AcknowledgePurchaseParams
//import com.android.billingclient.api.BillingClient.BillingResponseCode
//import com.android.billingclient.api.ProductDetails
//import com.android.billingclient.api.Purchase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.RemoteConfigConstants
import com.translate.languagetranslator.freetranslation.appUtils.TinyDB
import com.translate.languagetranslator.freetranslation.appUtils.getRemoteConfig
//import com.translate.languagetranslator.freetranslation.billing.BillingCallbacks
//import com.translate.languagetranslator.freetranslation.billing.BillingProcess
import com.translate.languagetranslator.freetranslation.setUserAsPremium
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class BaseBillingActivity : AppCompatActivity()
//    , BillingCallbacks
{
    companion object {
        var skuPrice: String = ""
        var p_price: Double = 0.0
        var skuCurrency: String = ""

    }

//    protected lateinit var billingProcess: BillingProcess
//    protected var subMonthly: ProductDetails? = null

    private var remoteSkuValue = "weekly_sub"
    private var isPurchaseInfoSend = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
//        initBillingProcess()


    }
//
//    public fun initBilling() {
//    }
//
//    private fun initBillingProcess() {
//        remoteSkuValue = getRemoteConfig().getString(RemoteConfigConstants.PaymentPricePlan)
//        Log.d("King", "Remote Sku -> $remoteSkuValue")
//
//
//        val skuList = ArrayList<String>()
//        skuList.add(remoteSkuValue)
//
////        billingProcess = BillingProcess(this, this, null, skuList, false)
//
//    }
//
//    override fun billingInitialized() {
//        //  Toast.makeText(applicationContext, "billinginitialized 1", Toast.LENGTH_LONG).show()
//
//
//        billingProcess.let { process ->
///*
//            val subscriptionDetailList = process.getSubscriptionDetailList()
//            subscriptionDetailList?.forEach { sku ->
//                if (sku?.productId!!.contains("year")) {
//                    skuPrice = sku.oneTimePurchaseOfferDetails!!.formattedPrice
//                    p_price = sku.oneTimePurchaseOfferDetails!!.formattedPrice.toDouble()
//                    skuCurrency = sku.oneTimePurchaseOfferDetails!!.priceCurrencyCode
//                    subMonthly = sku
//                } else if (sku?.productId!!.contains("mon")) {
//                    if (sku != null) {
//                        skuPrice = sku.oneTimePurchaseOfferDetails!!.formattedPrice
//                        p_price = sku.oneTimePurchaseOfferDetails!!.formattedPrice.toDouble()
//                        skuCurrency = sku.oneTimePurchaseOfferDetails!!.priceCurrencyCode
//                    }
//                    subMonthly = sku
//                } else {
//                    if (sku != null) {
//                        skuPrice = sku.oneTimePurchaseOfferDetails!!.formattedPrice
//                        p_price = sku.oneTimePurchaseOfferDetails!!.formattedPrice.toDouble()
//                        skuCurrency = sku.oneTimePurchaseOfferDetails!!.priceCurrencyCode
//                    }
//                    subMonthly = sku
//                }
//            }*/
//        }
//    }
//
//    override fun billingPurchased(purchases: MutableList<Purchase>?) {
//        purchases?.forEach { sku ->
//            handlePurchase(sku)
//        }
//
//    }
//
//    override fun billingCanceled() {
//
//        TinyDB.getInstance(this).putBoolean(Constants.IS_PREMIUM, false)
//
//    }
//
//    override fun itemAlreadyOwned() {
//
//        Log.e("billing","billingprocess+ Item Already Owned")
//    }
//
//
//    override fun onQuerySkuDetailsSubscription(skuDetailsList: MutableList<ProductDetails>?) {
//        if (skuDetailsList != null) {
//
//
//
//            for (sku in skuDetailsList) {
//
//                subMonthly = sku
//                var price = ""
//                subMonthly?.subscriptionOfferDetails?.let {
//                    if (it.size > 0) {
//
//                        if (it[0].pricingPhases.pricingPhaseList.size == 1) {
//                            price = it[0].pricingPhases.pricingPhaseList[0]
//                                .formattedPrice
//                            skuCurrency =
//                                it[0].pricingPhases.pricingPhaseList[0].priceCurrencyCode
//                        } else if (it[0].pricingPhases
//                                .pricingPhaseList.size > 1
//                        ) {
//                           price= it[0].pricingPhases.pricingPhaseList[1]
//                                .formattedPrice
//
//                            skuCurrency =
//                                it[0].pricingPhases.pricingPhaseList[1].priceCurrencyCode
//                        } else {
//                            price = "n/a"
//                            skuCurrency="n/a"
//                        }
//                    }
//                }
//
//                if (sku.productId.contains("year")) {
//
//                    val monthlyPrice =
//                        "Try 3 days free, then $price / year. Cancel anytime."
//                    skuPrice = monthlyPrice
//                    p_price = price.toDouble()
//                } else if (sku.productId.contains("mon")) {
//
//                    val monthlyPrice =
//                        "Try 3 days free, then $price / month. Cancel anytime."
//                    skuPrice = monthlyPrice
//                    p_price = price.toDouble()
//
//                } else {
//                    val monthlyPrice =
//                        "Try 3 days free, then $price / week. Cancel anytime."
//                    skuPrice = monthlyPrice
//                    p_price = price.toDouble()
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
//                Log.d("handlePurchase", "queryPurchaseResultSubscription: $purchaseList")
//                handlePurchase(sku)
//            }
//        }
//    }
//
//    private fun handlePurchase(purchase: Purchase) {
//
//        if (purchase.skus[0].contains("year") || purchase.skus[0].contains("mon") || purchase.skus[0].contains(
//                "weekly"
//            ) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
//        ) {
//            if (!purchase.isAcknowledged) {
//                val params = AcknowledgePurchaseParams.newBuilder()
//                    .setPurchaseToken(purchase.purchaseToken)
//                    .build()
//
//
//                billingProcess.getBillingClient()?.acknowledgePurchase(
//                    params
//                ) { billingResult ->
//                    //TinyDB.getInstance(this).putBoolean(Constants.IS_PREMIUM, true)
//                    Log.d("king", "if basebilling+ Purchase" + billingResult.responseCode)
//
//
//
//                    setUserAsPremium(purchase.purchaseTime)
////                    Logging.revenueEvent("35rkqw", skuPrice)
//                    val adjustEvent = AdjustEvent("35rkqw")
//                    adjustEvent.setRevenue(p_price, skuCurrency)
//                    adjustEvent.setOrderId(purchase.orderId)
//                    Adjust.trackEvent(adjustEvent)
//
//                    Intent(this, HomeActivity::class.java).apply {
//                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        startActivity(this)
//                    }
//                    finish()
//                }
//            } else {
//                Log.d("king", "if basebilling+ Purchase")
//                setUserAsPremium(purchase.purchaseTime)
//
//                Intent(this, HomeActivity::class.java).apply {
//                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(this)
//                }
//
//                finish()
//            }
//        }
//    }

}
