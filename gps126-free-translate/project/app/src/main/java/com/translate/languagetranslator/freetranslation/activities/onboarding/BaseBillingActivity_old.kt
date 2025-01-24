//package com.translate.languagetranslator.freetranslation.activities.onboarding
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.android.billingclient.api.AcknowledgePurchaseParams
//import com.android.billingclient.api.ProductDetails
//import com.android.billingclient.api.Purchase
//import com.android.billingclient.api.SkuDetails
//import com.translate.languagetranslator.freetranslation.setUserAsPremium
//import com.translate.languagetranslator.freetranslation.appUtils.Constants
//import com.translate.languagetranslator.freetranslation.appUtils.TinyDB
//import com.translate.languagetranslator.freetranslation.billing.BillingCallbacks
//import com.translate.languagetranslator.freetranslation.billing.BillingProcess
//import kotlinx.android.synthetic.main.activity_subscription_new_design.*
//import java.util.ArrayList
//
//open class BaseBillingActivity_old : AppCompatActivity(), BillingCallbacks {
//
//    protected lateinit var billingProcess: BillingProcess
//    protected var subMonthly: ProductDetails? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        super.onCreate(savedInstanceState)
//        initBillingProcess()
//    }
//
//    private fun initBillingProcess() {
//
//        val skuList = ArrayList<String>()
//        skuList.add(Constants.SUBSCRIPTION_ID_MONTH)
//        billingProcess = BillingProcess(this, this, null, skuList, false)
//    }
//    override fun billingInitialized() {
//
//        billingProcess.let { process ->
//
//            val subscriptionDetailList = process.getSubscriptionDetailList()
//            subscriptionDetailList?.forEach { sku ->
//
//                if (sku?.productId == Constants.SUBSCRIPTION_ID_MONTH) {
//
//                    subMonthly = sku
//                }
//            }
//        }
//    }
//
//    override fun billingPurchased(purchases: MutableList<Purchase>?) {
//
//        purchases?.forEach { sku ->
//
//            handlePurchase(sku)
//        }
//    }
//
//    override fun billingCanceled() {
//        TinyDB.getInstance(this).putBoolean(Constants.IS_PREMIUM, false)
//
//    }
//
//    override fun itemAlreadyOwned() {
//
//    }
//
//
//    override fun onQuerySkuDetailsSubscription(skuDetailsList: MutableList<ProductDetails>?) {
//        if (skuDetailsList != null) {
//
//            for (sku in skuDetailsList) {
//                if (sku.productId == Constants.SUBSCRIPTION_ID_MONTH) {
//                    subMonthly = sku
//
//                    val price = subMonthly?.oneTimePurchaseOfferDetails!!.formattedPrice ?: ""
//                    val monthlyPrice = "3 days free trial then $price / month, cancel anytime"
//                    tvPrice.text = monthlyPrice
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
//        if (purchase.skus.contains(Constants.SUBSCRIPTION_ID_MONTH) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//            if (!purchase.isAcknowledged) {
//
//                val params = AcknowledgePurchaseParams.newBuilder()
//                    .setPurchaseToken(purchase.purchaseToken)
//                    .build()
//
//                billingProcess.getBillingClient()?.acknowledgePurchase(
//                    params
//                ) { billingResult ->
//                    //TinyDB.getInstance(this).putBoolean(Constants.IS_PREMIUM, true)
//                    setUserAsPremium(purchase.purchaseTime)
//                    finish()
//
//
//                }
//
//            } else {
//
//                setUserAsPremium(purchase.purchaseTime)
//                finish()
//            }
//        }
//
//    }
//}