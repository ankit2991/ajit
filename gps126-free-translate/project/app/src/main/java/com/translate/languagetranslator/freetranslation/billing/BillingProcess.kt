//package com.translate.languagetranslator.freetranslation.billing
//
//import android.app.Activity
//import android.util.Log
//import android.widget.Toast
//import com.android.billingclient.api.*
//import com.code4rox.adsmanager.TinyDB
//import com.google.firebase.crashlytics.FirebaseCrashlytics
//import com.google.gson.Gson
//import com.translate.languagetranslator.freetranslation.appUtils.Constants
//import com.translate.languagetranslator.freetranslation.appUtils.RemoteConfigConstants
//import com.translate.languagetranslator.freetranslation.appUtils.getRemoteConfig
//import com.translate.languagetranslator.freetranslation.quimera.PurchaseItem
//import com.translate.languagetranslator.freetranslation.quimera.QuimeraInit
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import java.util.*
//
//
//class BillingProcess(
//    private val activity: Activity,
//    private val billingCallbacks: BillingCallbacks,
//    private val skuListInApp: ArrayList<String>? = null,
//    private val skuListSubscription: ArrayList<String>? = null,
//    retryOnConnectionFailed: Boolean
//) : PurchasesUpdatedListener {
//    private var billingClient: BillingClient? = null
//    private var retryOnConnectionFailed = false
//    private var subscriptionDetailList: List<ProductDetails?>? = null
//    private var inAppDetailList: List<ProductDetails?>? = null
//    private fun initBillingProcess() {
//        billingClient = BillingClient.newBuilder(activity)
//            .setListener(this)
//            .enablePendingPurchases()
//            .build()
//
//        establishConnection()
//    }
//
//    private fun establishConnection() {
//        billingClient!!.startConnection(object : BillingClientStateListener {
//            override fun onBillingSetupFinished(billingResult: BillingResult) {
//                when (billingResult.responseCode) {
//                    BillingClient.BillingResponseCode.OK -> {
//                        querySkuDetails()
//                        queryPurchases()
//                    }
//                    BillingClient.BillingResponseCode.ITEM_NOT_OWNED->{
//                        billingCallbacks.billingCanceled()
//                        TinyDB.getInstance(activity).putBoolean(Constants.IS_PREMIUM, false)
//                    }
//                    BillingClient.BillingResponseCode.USER_CANCELED -> {
//                        billingCallbacks.billingCanceled()
//                        Log.e("billing","billingprocess+ Purchase cancel")
//                            TinyDB.getInstance(activity).putBoolean(Constants.IS_PREMIUM, false)
//
//                    }
//                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
//                        billingCallbacks.itemAlreadyOwned()
//                    }
//
//                    BillingClient.BillingResponseCode.ERROR->{
//                        Log.e("billing","billingprocess+ Purchase Error")
//                    }
//
//                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE->{
//                        Log.e("billing","billingprocess+ item_Unavailable")
//                    }
//                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED->{
//                        Log.e("billing","billingprocess+ FeatureNotSupported")
//                    }
//                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE->{
//                        Log.e("billing","billingprocess+ BillingUnavailable")
//                    }
//                }
//            }
//
//            override fun onBillingServiceDisconnected() {
//                if (retryOnConnectionFailed) {
//                    establishConnection()
//                } else {
//                    //showToastMessage("billing failed to initialize")
//                }
//            }
//        })
//    }
//
//    private fun querySkuDetails() {
//        Log.e("skudetail result","product in  progress")
//        /////in app////
//        skuListInApp?.let { skulist ->
//            val productList =
//                listOf(
//                    QueryProductDetailsParams.Product.newBuilder()
//                        .setProductId(skulist[0])
//                        .setProductType(BillingClient.ProductType.INAPP)
//                        .build()
//                )
//
//            val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
//
//            billingClient!!.queryProductDetailsAsync(params.build()) { billingResult,
//                                                                       productDetailsList ->
//                // Process the result
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                    Log.e("skudetail result","product"+productDetailsList)
//                    inAppDetailList = productDetailsList
//                    billingCallbacks.onQuerySkuDetailsInApp(productDetailsList as MutableList<ProductDetails>?)
//                }
//            }
//        }
//            ////////Subs/////
//        skuListSubscription?.let { skulist ->
//            val productList =
//                listOf(
//                    QueryProductDetailsParams.Product.newBuilder()
//                        .setProductId(skulist[0])
//                        .setProductType(BillingClient.ProductType.SUBS)
//                        .build()
//                )
////            Log.e("skudetail result", "skulist" + skulist[0])
//            val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
//
//            billingClient!!.queryProductDetailsAsync(params.build()) { billingResult,
//                                                                       productDetailsList ->
//                // Process the result
//              //  Log.e("skudetail result", "product" + productDetailsList)
//
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//
//                    subscriptionDetailList = productDetailsList
//                    billingCallbacks.onQuerySkuDetailsSubscription(productDetailsList as MutableList<ProductDetails>?)
//                }
//            }
//        }
//        /////////////////////////////////////////////////
////        skuListInApp?.let { skulist ->
////            val params = SkuDetailsParams.newBuilder()
////            params.setSkusList(skulist).setType(BillingClient.SkuType.INAPP)
////            billingClient!!.querySkuDetailsAsync(
////                params.build()
////            ) { billingResult: BillingResult, skuDetailsList: List<SkuDetails>? ->
////
////                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
////                    inAppDetailList = skuDetailsList
////                    billingCallbacks.onQuerySkuDetailsInApp(skuDetailsList as MutableList<SkuDetails>?)
////                }
////            }
////        }
//
////        skuListSubscription?.let { skulist ->
////            val params = SkuDetailsParams.newBuilder()
////            params.setSkusList(skulist).setType(BillingClient.SkuType.SUBS)
////            billingClient!!.querySkuDetailsAsync(
////                params.build()
////            ) { billingResult: BillingResult, skuDetailsList: List<SkuDetails>? ->
////
////                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
////                    subscriptionDetailList = skuDetailsList
////                    billingCallbacks.onQuerySkuDetailsSubscription(skuDetailsList as MutableList<SkuDetails>?)
////                }
////            }
////        }
//
//    }
//
//    private fun queryPurchases() {
//
//        /////in app purchases/////
//        skuListInApp?.let {
//            billingClient?.let { client ->
//                 client!!.queryPurchasesAsync(
//                BillingClient.ProductType.INAPP,
//                PurchasesResponseListener { billingResult, mutableList ->
//
//                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                        if (mutableList == null) {
//                            billingCallbacks.queryPurchaseResultInApp(null)
////                            Log.e("mutableList result","product"+mutableList)
//                        } else {
//                            if (mutableList.size > 0) {
////                                Log.e("mutableList result","product"+mutableList)
//                                billingCallbacks.queryPurchaseResultInApp(mutableList)
//                            } else {
//                                billingCallbacks.queryPurchaseResultInApp(null)
//                            }
//                        }
//                    }
//                })
//                ?: run {
//                    billingCallbacks.queryPurchaseResultInApp(null)
//                }
//        }
//        }
//     //////subs purchases/////
//
//        skuListSubscription?.let {
//            billingClient?.let { client ->
//            client!!.queryPurchasesAsync(
//                BillingClient.ProductType.SUBS,
//                PurchasesResponseListener { billingResult, mutableList ->
//
//                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//                        if (mutableList == null) {
////                            Log.e("mutableList result","product"+mutableList)
//                            billingCallbacks.queryPurchaseResultSubscription(null)
//                        } else {
//                            if (mutableList.size > 0) {
////                                Log.e("mutableList result","product"+mutableList)
//                                billingCallbacks.queryPurchaseResultSubscription(mutableList)
//                            } else {
//                                billingCallbacks.queryPurchaseResultSubscription(null)
//                            }
//                        }
//
//
//                    }
//                    else {
//                        billingCallbacks.queryPurchaseResultSubscription(null)
//
//                    }
//                })
//                ?: run {
//                    billingCallbacks.queryPurchaseResultSubscription(null)
//                }
//        }
//        }
//        /////////old methods////////
////        skuListInApp?.let {
////            billingClient?.let { client ->
////                val inAppResult = client.queryPurchasesAsync(BillingClient.ProductType.INAPP)
////                inAppResult?.let { result ->
////                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
////                        if (result.purchasesList == null) {
////                            billingCallbacks.queryPurchaseResultInApp(null)
////                        } else {
////                            if (result.purchasesList!!.size > 0) {
////                                billingCallbacks.queryPurchaseResultInApp(result.purchasesList)
////                            } else {
////                                billingCallbacks.queryPurchaseResultInApp(null)
////                            }
////                        }
////
////                    } else {
////                        billingCallbacks.queryPurchaseResultInApp(null)
////
////                    }
////                } ?: run {
////                    billingCallbacks.queryPurchaseResultInApp(null)
////                }
////
////            }
////
////        }
////
////        skuListSubscription?.let {
////            billingClient?.let { client ->
////                val inAppResult = client.queryPurchases(BillingClient.SkuType.SUBS)
////                inAppResult?.let { result ->
////                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
////                        if (result.purchasesList == null) {
////                            billingCallbacks.queryPurchaseResultSubscription(null)
////                        } else {
////                            if (result.purchasesList!!.size > 0) {
////                                billingCallbacks.queryPurchaseResultSubscription(result.purchasesList)
////                            } else {
////                                billingCallbacks.queryPurchaseResultSubscription(null)
////                            }
////                        }
////
////                    } else {
////                        billingCallbacks.queryPurchaseResultSubscription(null)
////
////                    }
////                } ?: run {
////                    billingCallbacks.queryPurchaseResultSubscription(null)
////                }
////
////            }
////        }
//
//    }
//
//    fun launchPurchaseFlow(productDetails: ProductDetails?) {
//     //   Toast.makeText(activity,"toast message with gravity",Toast.LENGTH_SHORT).show()
//
////        val billingFlowParams = BillingFlowParams.newBuilder()
////            .setSkuDetails(productDetails!!)
////            .build()
////        launchBillingFlow(activity, billingFlowParams)
//
//        ////////////////////////////v5///////////
//        val offerToken = productDetails!!.subscriptionOfferDetails?.get(0)?.offerToken ?: return
//
//        val productDetailsParamsList =
//            listOf(
//                BillingFlowParams.ProductDetailsParams.newBuilder()
//                    .setProductDetails(productDetails!!)
//                    .setOfferToken(offerToken)
//                    .build()
//            )
//        val billingFlowParams =
//            BillingFlowParams.newBuilder()
//                .setProductDetailsParamsList(productDetailsParamsList)
//                .build()
//
//// Launch the billing flow
//        val billingResult = billingClient!!.launchBillingFlow(activity, billingFlowParams)
//
//    }
//
//    fun launchBillingFlow(activity: Activity?, params: BillingFlowParams): Int {
//        val billingResult = billingClient!!.launchBillingFlow(activity!!, params)
//        val responseCode = billingResult.responseCode
//        return responseCode
//    }
//
//    private fun showToastMessage(message: String) {
//        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
//        when (billingResult.responseCode) {
//
//            BillingClient.BillingResponseCode.OK -> if (list != null) {
//                subscriptionDetailList?.let { productDetail ->
//                    for (sku in productDetail) {
//                        if(sku!!.productId.contains(list.get(0).products.get(0))) {
//                            QuimeraInit.userPurchased(
//                                activity,
//                                PurchaseItem(list.get(0), sku)
//                            )
//                        }
//                        }
//                    }
//
//
//                billingCallbacks.billingPurchased(list as MutableList<Purchase>?)
//            }
//
//            BillingClient.BillingResponseCode.USER_CANCELED -> {
//                QuimeraInit.userCancelBilling(activity)
//                billingCallbacks.billingCanceled()
//                Log.e("billing", "billingprocess+ Purchase cancel")
//                TinyDB.getInstance(activity)
//                    .putBoolean(Constants.IS_PREMIUM, false)
//            }
//
//            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
//                QuimeraInit.itemAlreadyOwned(activity)
//                billingCallbacks.itemAlreadyOwned()
//            }
//
//            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
//                QuimeraInit.developerError(activity)
//            }
//
//            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
//                QuimeraInit.serviceDisconnected(activity)
//                Log.e("onPurchasesUpdated:", "service disconnected")
//            }
//
//            BillingClient.BillingResponseCode.ERROR -> {
//                QuimeraInit.billingError(activity)
//                Log.e("onPurchasesUpdated:"," billing error")
//            }
//
//            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
//                QuimeraInit.serviceUnavailable(activity)
//                Log.e("onPurchasesUpdated:"," service unavailable")
//            }
//
//            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
//                QuimeraInit.billingUnavailable(activity)
//                Log.e("onPurchasesUpdated:"," billing unavailable")
//            }
//
//            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
//                QuimeraInit.itemUnavailable(activity)
//                Log.e("onPurchasesUpdated:"," item unavailable")
//            }
//
//            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
//                QuimeraInit.itemNotOwned(activity)
//                Log.e("onPurchasesUpdated:"," item not owned")
//            }
//        }
//    }
//
//    val isBillingProcessReady: Boolean
//        get() = billingClient!!.isReady
//
//    fun getInAppDetailList(): List<ProductDetails?>? {
//        return inAppDetailList
//
//    }
//
//    fun getSubscriptionDetailList(): List<ProductDetails?>? {
//        return subscriptionDetailList
//    }
//
//    fun getPurchasedInfo(){
//        queryPurchases()
//    }
//
//    init {
//        this.retryOnConnectionFailed = retryOnConnectionFailed
//        initBillingProcess()
//    }
//
//    fun getBillingClient(): BillingClient? {
//        return billingClient
//    }
//}