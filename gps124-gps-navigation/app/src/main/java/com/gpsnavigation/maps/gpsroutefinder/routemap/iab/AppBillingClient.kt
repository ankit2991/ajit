package com.gpsnavigation.maps.gpsroutefinder.routemap.iab

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces.ConnectResponse
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces.PurchaseResponse
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces.QueryResponse
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.onetime.OneTimePurchaseItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.subscription.SubscribedItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.subscription.SubscriptionItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.quimeraManager.QuimeraInit
import com.gpsnavigation.maps.gpsroutefinder.routemap.quimeraManager.SubscriptionItemsQmr
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber


class AppBillingClient {
    private lateinit var billingClient: BillingClient
    private var lastItemRequestedForPurchase: ProductItem? = null
    private var skuItem: ProductItem? = null
    fun connect(
        context: Context,
        productId: String ,
        connectResponse: ConnectResponse,
        purchaseResponse: PurchaseResponse
    ) {

        billingClient = BillingClient
            .newBuilder(context)
            // Purchase listener
            .setListener {
                    billingResult: BillingResult, purchases: MutableList<Purchase>? ->


                if (billingResult.responseCode != OK) {
                    when (billingResult.responseCode) {
                        ITEM_ALREADY_OWNED -> { purchaseResponse.isAlreadyOwned()}
                        USER_CANCELED -> {
                            skuItem?.let { purchaseResponse.userCancelled(it) }
                        }
                        SERVICE_TIMEOUT -> {Log.e("billingError","service timeout")
                        purchaseResponse.error("SERVICE_TIMEOUT")}
                        FEATURE_NOT_SUPPORTED -> {Log.e("billingError","feature not supported ")
                            purchaseResponse.error("FEATURE_NOT_SUPPORTED")}
                        SERVICE_DISCONNECTED -> {Log.e("billingError","Service Desconnected")
                            purchaseResponse.error("SERVICE_DISCONNECTED")  }
                        SERVICE_UNAVAILABLE -> {Log.e("billingError","Service Unavailable")
                            purchaseResponse.error("SERVICE_UNAVAILABLE")  }
                        BILLING_UNAVAILABLE -> {Log.e("billingError","Billing unavaiable")
                            purchaseResponse.error("BILLING_UNAVAILABLE")  }
                        ITEM_UNAVAILABLE -> {Log.e("billingError","item unavailable")
                            purchaseResponse.error("ITEM_UNAVAILABLE")  }
                        DEVELOPER_ERROR -> {Log.e("billingError","developer error")
                            purchaseResponse.error("DEVELOPER_ERROR") }
                        ERROR -> {Log.e("billingError","error")
                            purchaseResponse.error("ERROR")  }
                        ITEM_NOT_OWNED -> {Log.e("billingError","item not owned")
                            purchaseResponse.error("ITEM_NOT_OWNED")   }

                    }

                    return@setListener
                }

                /* Adjust Event */
                ////////////////////////////////////////////////////////////////////////////////////
//                val adjustEvent = AdjustEvent("5xko3p")
//                adjustEvent.addCallbackParameter("Subscribe", "")
//                adjustEvent.setCallbackId("Subscribed");
//                Adjust.trackEvent(adjustEvent)
                ////////////////////////////////////////////////////////////////////////////////////


                /* Facebook Event*/
                ///////////////////////////////////////////////////////////////////////////////////
//                val logger = AppEventsLogger.newLogger(context)
//                logger.logEvent("Subscribe")
//                FacebookSdk.setIsDebugEnabled(true);  // TODO: disable debug logs before release
//                FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);  // TODO: disable debug logs before release
//                FacebookSdk.setAdvertiserIDCollectionEnabled(true)
                //////////////////////////////////////////////////////////////////////////////////
                purchases?.forEach { purchase ->
                    // Just to be on safe side, we'll acknowledge every purchased/subscribed item.
                    if (!purchase.isAcknowledged) {
                        billingClient.acknowledgePurchase(
                            AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken).build()
                        ) {}
                    }

                    lastItemRequestedForPurchase?.let {


                        for (purchaseSku in purchase.skus) {
                            if (purchaseSku == it.sku) {
                                /*  if (it is OneTimePurchaseItem) {
                                      it.purchasedItem = PurchasedItem(
                                          purchaseSku,
                                          purchase.purchaseTime,
                                          purchase.purchaseToken
                                      )
                                  } else {*/
                                (it as SubscriptionItem).subscribedItem = SubscribedItem(
                                    purchaseSku,
                                    purchase.purchaseTime,
                                    purchase.purchaseToken
                                )
                            }
                            Timber.tag("PurchaseItem").d(purchase.toString())
                            Log.d("PurchaseItem", "" + purchase.toString())
                            purchaseResponse.ok(it)
                            lastItemRequestedForPurchase = null
                            // Break the loop when you're done
                            return@forEach
//                            }
                        }

                    }
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                connectResponse.disconnected()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                /*  queryPurchases(skuToGet, object : QueryResponse<OneTimePurchaseItem> {
                      override fun ok(oneTimePurchaseItems: List<OneTimePurchaseItem>) {*/

                querySubscriptions(productId, object : QueryResponse<SubscriptionItem> {
                    override fun ok(subscriptionItems: List<SubscriptionItem>) {
                        // Check if any item is bought or subscribed to, then acknowledge it.
                        /*   for (item in oneTimePurchaseItems) {
                               billingClient.acknowledgePurchase(
                                   AcknowledgePurchaseParams.newBuilder().setPurchaseToken(
                                       item.purchasedItem?.purchaseToken ?: continue
                                   ).build()
                               ) {}
                           }*/
                        for (item in subscriptionItems) {
                            billingClient.acknowledgePurchase(
                                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(
                                    item.subscribedItem?.purchaseToken ?: continue
                                ).build()
                            ) {

                            }
                        }
                        //connectResponse.ok(oneTimePurchaseItems, subscriptionItems)
                        //connectResponse.ok(subscriptionItems)
                        connectResponse.ok(subscriptionItems)
                    }

                    override fun error(responseCode: Int) {
                        log(responseCode)
                        when (responseCode) {
                            BILLING_UNAVAILABLE -> connectResponse.billingUnavailable()
                            DEVELOPER_ERROR -> connectResponse.developerError()
                            ERROR -> connectResponse.error()
                            FEATURE_NOT_SUPPORTED -> connectResponse.featureNotSupported()
                            ITEM_UNAVAILABLE -> connectResponse.itemUnavailable()
                            SERVICE_DISCONNECTED -> connectResponse.serviceDisconnected()
                            SERVICE_UNAVAILABLE -> connectResponse.serviceUnavailable()
                        }
                    }
                }, context)
                /*   }

                   override fun error(responseCode: Int) {
                       log(responseCode)
                       when (responseCode) {
                           BILLING_UNAVAILABLE -> connectResponse.billingUnavailable()
                           DEVELOPER_ERROR -> connectResponse.developerError()
                           ERROR -> connectResponse.error()
                           FEATURE_NOT_SUPPORTED -> connectResponse.featureNotSupported()
                           ITEM_UNAVAILABLE -> connectResponse.itemUnavailable()
                           SERVICE_DISCONNECTED -> connectResponse.serviceDisconnected()
                           SERVICE_UNAVAILABLE -> connectResponse.serviceUnavailable()
                       }
                   }
               })*/
            }

        })



    }

/*    private fun queryPurchases(sku: String?, response: QueryResponse<OneTimePurchaseItem>) {
        // Querying all available items for one-time purchase
        billingClient.querySkuDetailsAsync(
            SkuDetailsParams
                .newBuilder()
                .setSkusList(OneTimePurchaseSku.getMutableListOfSkus())
                .setType(INAPP)
                .build()
        ) { billingResultOfList, skuDetailsList ->

            if (billingResultOfList.responseCode != OK) {
                response.error(billingResultOfList.responseCode)
                return@querySkuDetailsAsync
            }

            val skuPurchaseItems = mutableListOf<OneTimePurchaseItem>()

            // Querying all purchased one-time purchase items
            val purchases = billingClient.queryPurchases(INAPP)
            skuDetailsList?.let {
                for (skuItem in it) {
                    var isItemNotPurchased = true
                    purchases.purchasesList?.let { purchasesList ->

                        purchasesList.forEach() { purchase ->
                            for (purchaseSku in purchase.skus) {
                                if (skuItem.sku == purchaseSku && (sku.isNullOrEmpty() || (skuItem.sku == sku && purchaseSku == sku))) {
                                    isItemNotPurchased = false

                                    skuPurchaseItems.add(
                                        OneTimePurchaseItem(skuItem)
                                            .apply { purchasedItem = PurchasedItem(
                                                purchaseSku,
                                                purchase.purchaseTime,
                                                purchase.purchaseToken
                                            ) }
                                    )
                                    return@forEach
                                }
                            }


                        }
                    }
                    if (isItemNotPurchased && (sku.isNullOrEmpty() || skuItem.sku == sku)) {
                        skuPurchaseItems.add(OneTimePurchaseItem(skuItem))
                    }
                }
            }
            response.ok(skuPurchaseItems)
        }
    }*/

    private fun querySubscriptions(
        productId: String, response: QueryResponse<SubscriptionItem>,
        context: Context
    ) {

        val productList =
            listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("weekly3")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("monthly")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("month_ly")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("six_months")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("year_ly")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("yearly")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId("yearly_subscription3")
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
        billingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->

            if (billingResult.responseCode != OK) {
                response.error(billingResult.responseCode)
//                return@queryProductDetailsAsync

            }
            val skuSubscriptionItems = mutableListOf<SubscriptionItem>()

            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { result, purchaseList ->
                if (result.responseCode != OK) {
                    response.error(result.responseCode)
                    return@queryPurchasesAsync
                }
                ////////////////////////////////////////////////////////////////////


                ///////////////////////////////////////////////////////////////
                for (productDetails in productDetailsList) {
                    var isItemNotPurchased = true

                    purchaseList.forEach() { purchase ->
                        for (product in purchase.products) {
                            if (productDetails.productId == product ) {
                                isItemNotPurchased = false
                                Timber.tag("PurchaseItem").d(purchase.toString())
                                Log.d("PurchaseItem", "" + purchase.toString())
                                skuSubscriptionItems.add(
                                    SubscriptionItem(productDetails)
                                        .apply {
                                            subscribedItem = SubscribedItem(
                                                product,
                                                purchase.purchaseTime,
                                                purchase.purchaseToken
                                            )
                                        }
                                )
                                return@forEach
                            }
                        }


                    }
                    if (isItemNotPurchased && productDetails.productId == productId) {
                        skuSubscriptionItems.add(SubscriptionItem(productDetails))
                    }
                }
                response.ok(skuSubscriptionItems)
            }
        }
    }

    fun consumeBeforePurchasingIfNecessary(
        oneTimePurchaseItem: OneTimePurchaseItem,
        consumeListener: OnConsumeListener
    ) {
        // Check if this item is consumable. If it is, then check if this item is already purchased. If it is, then
        // consume it before buying it again.

        if (!oneTimePurchaseItem.isConsumable) {
            consumeListener.onSuccessfullyConsumed()
        }

        /*  queryPurchases(oneTimePurchaseItem.sku, object : QueryResponse<OneTimePurchaseItem> {

              override fun error(responseCode: Int) {
                  consumeListener.onConsumeError(
                      when (responseCode) {
                          SERVICE_TIMEOUT -> "Timed out, please try again"
                          else -> "Unknown error, please try again"
                      }
                  )
              }

              override fun ok(skuItems: List<OneTimePurchaseItem>) {
                  // After querying, if item is not purchased, return
                  if (skuItems[0].purchasedItem == null) {
                      consumeListener.onSuccessfullyConsumed()
                      return
                  }

                  // But if item is purchased, then consume it
                  consumeSkuItem(skuItems[0], object : OnConsumeListener {

                      override fun onSuccessfullyConsumed() {
                          consumeListener.onSuccessfullyConsumed()
                      }

                      override fun onConsumeError(errorMessage: String) {
                          consumeListener.onConsumeError(errorMessage)
                      }
                  })
              }
          })*/
    }

    fun purchaseSkuItem(baseActivity: Activity, productItem: ProductItem): Boolean {
        if (!billingClient.isReady) {
            return false
        }
        var offerToken: String? = null

        if (productItem is SubscriptionItem) {
            offerToken = productItem.offerToken ?: return false
        }
        val billingParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productItem.productDetails)
        offerToken?.let {
            billingParams.setOfferToken(it)
        }
        val productDetailsParamsList = listOf(billingParams.build())
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList).build()
        val billingResult = billingClient.launchBillingFlow(baseActivity, billingFlowParams)
        if (billingResult.responseCode == OK) {
            lastItemRequestedForPurchase = productItem
        }

        skuItem = productItem

        if (productItem != null){
                QuimeraInit.sendSkuDetails(baseActivity,productItem)
        }


        return billingResult.responseCode == OK
    }

    private fun consumeSkuItem(
        oneTimePurchaseItem: OneTimePurchaseItem,
        listener: OnConsumeListener
    ) {
        val consumeParam = ConsumeParams
            .newBuilder()
            .setPurchaseToken(oneTimePurchaseItem.purchasedItem!!.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParam) { result, _ ->

            if (result.responseCode == OK) {
                listener.onSuccessfullyConsumed()
            } else {
                listener.onConsumeError(
                    when (result.responseCode) {
                        SERVICE_TIMEOUT -> "Timed out, please try again"
                        else -> "Unknown error, please try again"
                    }
                )
            }
        }
    }

    private fun log(responseCode: Int) {
        val message = when (responseCode) {
            SERVICE_TIMEOUT -> "SERVICE_TIMEOUT"
            FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
            SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
//            USER_CANCELED -> "USER_CANCELED"
            SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
            BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
            ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
            DEVELOPER_ERROR -> "DEVELOPER_ERROR"
            ERROR -> "ERROR"
//            ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
            ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
            else -> responseCode.toString()
        }

        Timber.i("In-app billing response: $message")
    }

    interface OnConsumeListener {
        fun onSuccessfullyConsumed();
        fun onConsumeError(errorMessage: String)
    }




}