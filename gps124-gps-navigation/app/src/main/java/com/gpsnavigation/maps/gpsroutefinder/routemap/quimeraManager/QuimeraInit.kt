package com.gpsnavigation.maps.gpsroutefinder.routemap.quimeraManager

import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.gpsnavigation.maps.gpsroutefinder.routemap.BuildConfig
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.ProductItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.isOnline
import com.weewoo.quimera.Quimera
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object QuimeraInit {


    var weeklyDetails: ProductDetails? = null
    var yearlyDetails: ProductDetails? = null
     fun initQuimeraSdk(context: Context){

        GlobalScope.launch {
            try {
                if (context.isOnline(context)) {
                    var gaid: String = ""

                    val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                    gaid = adInfo?.id.toString()


                    var mmp_adjust = context.getString(R.string.adjust_app_id)
                    val facebook_id: String = context.getString(R.string.facebook_app_id)
                    val firebase_id: String = context.getString(R.string.google_app_id)

                    val quimeraMap: MutableMap<String, String?>? =
                        Quimera(context, FirebaseCrashlytics.getInstance()).getConfig()

                    if (quimeraMap != null && quimeraMap!!.isNotEmpty()) {
                        quimeraMap?.forEach { entry ->
                            kotlin.run {
                                val param: String = when (entry.key) {
                                    "mmp_adjust" -> mmp_adjust
                                    "facebook_id" -> facebook_id
                                    "gaid" -> gaid ?: ""
                                    "app_version" -> BuildConfig.VERSION_NAME
                                    "firebase_id" -> firebase_id
                                    else -> ""
                                }
                                quimeraMap[entry.key] = param
                            }
                        }


                        Quimera(context, FirebaseCrashlytics.getInstance()).setConfig(quimeraMap)

                    }
                }
            }catch (e: Exception){

            }
        }

    }


    fun userCancelBilling(context: Context){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.USER_CANCELED.toString(), "")

//            Log.e("billingv6","usercancelbilling>>")

        }
    }

    fun userPurchased(context: Context, purchases:ProductItem){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.OK.toString(), Gson().toJson(purchases))

//            Log.e("billingv6","purchase>>"+Gson().toJson(purchases))
        }
    }

    fun userPurchased_adapty(context: Context, purchases:Purchase){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.OK.toString(), Gson().toJson(purchases))

//            Log.e("billingv6","purchase>>"+Gson().toJson(purchases))
        }
    }

    fun itemAlreadyOwned(context: Context){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED.toString(), "")

        }
    }

    fun developerError(context: Context){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.DEVELOPER_ERROR.toString(), "")

        }
    }

    fun billingError(context: Context){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.ERROR.toString(), "")

        }
    }

    fun serviceUnavailable(context: Context){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE.toString(), "")

        }
    }

    fun serviceDisconnected(context: Context){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.SERVICE_DISCONNECTED.toString(), "")
        }
    }

    fun featureNotSupported(context: Context){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED.toString(), "")
        }
    }

    fun billingUnavailable(context: Context){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.BILLING_UNAVAILABLE.toString(), "")

        }
    }
    fun itemUnavailable(context: Context){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.ITEM_UNAVAILABLE.toString(), "")

        }
    }

    fun itemNotOwned(context: Context){
        GlobalScope.launch {
            Quimera(context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo(BillingClient.BillingResponseCode.ITEM_NOT_OWNED.toString(), "")

        }
    }

    fun sendSkuDetails(context: Context, skuDetails: ProductItem){
        GlobalScope.launch {
            Quimera(context = context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo("-400", Gson().toJson(skuDetails))

//            Log.e("billingv6","sendSkuDetail>>"+Gson().toJson(skuDetails))
        }

    }


    fun sendSkuDetails_adapty(context: Context, skuDetails: ProductDetails){
        GlobalScope.launch {
            Quimera(context = context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo("-400", Gson().toJson(skuDetails))

//            Log.e("billingv6","sendSkuDetail>>"+Gson().toJson(skuDetails))
        }

    }
}