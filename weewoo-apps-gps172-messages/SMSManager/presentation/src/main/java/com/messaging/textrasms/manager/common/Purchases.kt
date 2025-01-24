package com.messaging.textrasms.manager.common

import android.app.Activity
import com.android.billingclient.api.*
import com.weewoo.sdkproject.billing.PurchasesInterface

class Purchases(private val activity: Activity, private val onEntitledPurchases: (List<Purchase>) -> Unit, private val onPurchase: (Purchase?) -> Unit, private val didConnect: (Boolean, Int) -> Unit) : PurchasesInterface {

    private var purchasedSubs: List<Purchase>? = null
        set(value) {
            field = value
            returnPurchases()
        }
    private var purchasedInApp: List<Purchase>? = null
        set(value) {

            field = value
            returnPurchases()
        }

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let {
                    for (purchase in purchases) {
                        when (purchase.purchaseState) {
                            Purchase.PurchaseState.PURCHASED -> {

                                onPurchase(purchase)
                                if (!purchase.isAcknowledged) {
                                    val acknowledgePurchaseParams =
                                        AcknowledgePurchaseParams.newBuilder()
                                            .setPurchaseToken(purchase.purchaseToken)
                                            .build()
                                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                                        print("acknowledgePurchase(), billingResult=$billingResult")
                                    }
                                }
                            }
                            Purchase.PurchaseState.PENDING -> {
                                // Here you can confirm to the user that they've started the pending
                                // purchase, and to complete it, they should follow instructions that
                                // are given to them. You can also choose to remind the user in the
                                // future to complete the purchase if you detect that it is still
                                // pending.
                            }
                        }
                    }
                }
                print("onPurchasesUpdated(), $purchases")
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {

                print("onPurchasesUpdated() - user cancelled the purchase flow - skipping")
                onPurchase(null)
            }
            else -> {

                print("onPurchasesUpdated() got unknown resultCode: ${billingResult.responseCode}")
                onPurchase(null)
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(activity)
        .enablePendingPurchases()
        .setListener(purchasesUpdatedListener)
        .build()

    private var isBillingServiceConnected = false

    init {
        startServiceConnection {
            queryPurchases()
        }
    }

    private fun startServiceConnection(task: () -> Unit) {
        if (isBillingServiceConnected) {
            task()
        } else {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    log("onBillingSetupFinished(...), billingResult=$billingResult")
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                    {
                        isBillingServiceConnected = true
                        task()
                    }

                    didConnect(billingResult.responseCode == BillingClient.BillingResponseCode.OK, billingResult.responseCode)
                }

                override fun onBillingServiceDisconnected() {
                    log("onBillingServiceDisconnected()")
                    isBillingServiceConnected = false
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
        }
    }

    private fun log(message: String) {
        println("BillingManager$message")
    }

    private fun returnPurchases() {

        if (purchasedSubs != null && purchasedInApp != null) {

            onEntitledPurchases.invoke(purchasedSubs!! + purchasedInApp!!)
        }
    }

    private fun querySubscriptionSkuDetails(
        skus: List<String>, onSuccess: (List<SkuDetails>) -> Unit,
        onError: (code: Int, message: String) -> Unit, inApp: Boolean
    )
    {

        val params = SkuDetailsParams.newBuilder().setSkusList(skus).setType(
            if (inApp) BillingClient.SkuType.INAPP
            else BillingClient.SkuType.SUBS
        )

        billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                onSuccess(skuDetailsList)
            } else {
                onError(billingResult.responseCode, billingResult.debugMessage)
            }
        }
    }


    override fun queryPurchases() {

        billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS) { billingResult, subs ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchasedSubs = subs
            }
        }

        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP) { billingResult, subs ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchasedInApp = subs
            }
        }
    }

    override fun querySkusSubsDetails(
        skus: List<String>,
        onSuccess: (List<SkuDetails>) -> Unit,
        onError: (code: Int, message: String) -> Unit
    ) {
        startServiceConnection {
            querySubscriptionSkuDetails(skus, onSuccess, onError, false)
        }
    }

    override fun querySkusInAppDetails(
        skus: List<String>,
        onSuccess: (List<SkuDetails>) -> Unit,
        onError: (code: Int, message: String) -> Unit
    ) {
        startServiceConnection {
            querySubscriptionSkuDetails(skus, onSuccess, onError, true)
        }
    }

    override fun startPurchaseFlow(sku: SkuDetails)
    {

        startServiceConnection{
            val flowParams = BillingFlowParams.newBuilder().setSkuDetails(sku).build()
            val billingResult = billingClient.launchBillingFlow(activity, flowParams)
            log("startPurchaseFlow(...), billingResult=$billingResult")
        }
    }

    override fun destroy() {
        log("destroy()")
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }


}