package com.zaeem.rokuremote.Billing

import android.app.Activity
import android.widget.Toast
import com.android.billingclient.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class BillingProcess(
        private val activity: Activity,
        private val billingCallbacks: BillingCallbacks,
        private val skuListInApp: ArrayList<String>? = null,
        private val skuListSubscription: ArrayList<String>? = null,
        retryOnConnectionFailed: Boolean
) : PurchasesUpdatedListener {
    private var billingClient: BillingClient? = null
    private var retryOnConnectionFailed = false
    private var subscriptionDetailList: List<SkuDetails?>? = null
    private var inAppDetailList: List<SkuDetails?>? = null
    private fun initBillingProcess() {
        billingClient = BillingClient.newBuilder(activity)
                .setListener(this)
                .enablePendingPurchases()
                .build()
        establishConnection()
    }

    private fun establishConnection() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        querySkuDetails()
                        queryPurchases()
                    }
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        billingCallbacks.billingCanceled()
                    }
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        billingCallbacks.itemAlreadyOwned()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                if (retryOnConnectionFailed) {
                    establishConnection()
                } else {
                    //showToastMessage("billing failed to initialize")
                }
            }
        })
    }

    private fun querySkuDetails() {

        skuListInApp?.let { skulist ->
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skulist).setType(BillingClient.SkuType.INAPP)
            billingClient!!.querySkuDetailsAsync(
                    params.build()
            ) { billingResult: BillingResult, skuDetailsList: List<SkuDetails>? ->

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    inAppDetailList = skuDetailsList
                    billingCallbacks.onQuerySkuDetailsInApp(skuDetailsList as MutableList<SkuDetails>?)
                }
            }
        }

        skuListSubscription?.let { skulist ->
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skulist).setType(BillingClient.SkuType.SUBS)
            billingClient!!.querySkuDetailsAsync(
                    params.build()
            ) { billingResult: BillingResult, skuDetailsList: List<SkuDetails>? ->

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    subscriptionDetailList = skuDetailsList
                    billingCallbacks.onQuerySkuDetailsSubscription(skuDetailsList as MutableList<SkuDetails>?)
                }
            }
        }

    }

    private fun queryPurchases() {

        skuListInApp?.let {
            billingClient?.let { client ->
                val inAppResult = client.queryPurchases(BillingClient.SkuType.INAPP)
                inAppResult?.let { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        if (result.purchasesList == null) {
                            billingCallbacks.queryPurchaseResultInApp(null)
                        } else {
                            if (result.purchasesList!!.size > 0) {
                                billingCallbacks.queryPurchaseResultInApp(result.purchasesList)
                            } else {
                                billingCallbacks.queryPurchaseResultInApp(null)
                            }
                        }

                    } else {
                        billingCallbacks.queryPurchaseResultInApp(null)

                    }
                } ?: run {
                    billingCallbacks.queryPurchaseResultInApp(null)
                }

            }

        }

        skuListSubscription?.let {
            billingClient?.let { client ->
                val inAppResult = client.queryPurchases(BillingClient.SkuType.SUBS)
                inAppResult.let { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        if (result.purchasesList == null) {
                            billingCallbacks.queryPurchaseResultSubscription(null)
                        } else {
                            if (result.purchasesList!!.size > 0) {
                                billingCallbacks.queryPurchaseResultSubscription(result.purchasesList)
                            } else {
                                billingCallbacks.queryPurchaseResultSubscription(null)
                            }
                        }

                    } else {
                        billingCallbacks.queryPurchaseResultSubscription(null)

                    }
                } ?: run {
                    billingCallbacks.queryPurchaseResultSubscription(null)
                }

            }
        }

    }

    fun launchPurchaseFlow(skuDetails: SkuDetails?) {
        val billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails!!)
                .build()
        launchBillingFlow(activity, billingFlowParams)
    }

    fun launchBillingFlow(activity: Activity?, params: BillingFlowParams): Int {
        val billingResult = billingClient!!.launchBillingFlow(activity!!, params)
        val responseCode = billingResult.responseCode
        return responseCode
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> if (list != null) {

                acknowledgePurchase(list)
                billingCallbacks.billingPurchased(list as MutableList<Purchase>?)

            }
            BillingClient.BillingResponseCode.USER_CANCELED -> billingCallbacks.billingCanceled()
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> billingCallbacks.itemAlreadyOwned()
        }
    }

    val isBillingProcessReady: Boolean
        get() = billingClient!!.isReady

    fun getInAppDetailList(): List<SkuDetails?>? {
        return inAppDetailList
    }

    fun getSubscriptionDetailList(): List<SkuDetails?>? {
        return subscriptionDetailList
    }

    fun getPurchasedInfo() {
        queryPurchases()
    }

    init {
        this.retryOnConnectionFailed = retryOnConnectionFailed
        initBillingProcess()
    }

    fun getBillingClient(): BillingClient? {
        return billingClient
    }

    private fun acknowledgePurchase(list: List<Purchase>) {

        list.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }.map {

            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
            GlobalScope.launch(Dispatchers.IO) {

                billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build())

            }
        }
    }
}