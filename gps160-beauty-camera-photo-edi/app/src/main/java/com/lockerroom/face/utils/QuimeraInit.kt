package com.lockerroom.face.utils

import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails

import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.lockerroom.face.BuildConfig
import com.lockerroom.face.R
import com.lockerroom.face.iab.SubscriptionItem

import com.weewoo.quimera.Quimera
import games.moisoni.google_iab.models.ProductInfo
import games.moisoni.google_iab.models.PurchaseInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object QuimeraInit {
    var singleCall = false;


     fun initQuimeraSdk(context: Context){

        GlobalScope.launch {
            var gaid: String = ""

            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                gaid = adInfo?.id.toString()
            }catch (e: Exception){

            }

            var mmp_adjust = context.getString(R.string.adjust_id)
            val facebook_id: String = context.getString(R.string.facebook_id)
            val firebase_id: String = context.getString(R.string.firebase_id)

            val quimeraMap: MutableMap<String, String?>? = Quimera(context, FirebaseCrashlytics.getInstance()).getConfig()

            if (quimeraMap != null && quimeraMap!!.isNotEmpty()){
                quimeraMap?.forEach{entry ->
                    kotlin.run {
                        val param: String = when (entry.key){
                            "mmp_adjust" -> mmp_adjust
                            "facebook_id" -> facebook_id
                            "gaid" -> gaid?: ""
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

    }


    fun userCancelBilling(context: Context){
        if (singleCall) {
            singleCall = false
            GlobalScope.launch {
                Quimera(context, FirebaseCrashlytics.getInstance())
                    .sendPurchaseInfo(
                        BillingClient.BillingResponseCode.USER_CANCELED.toString(),
                        ""
                    )

//            Log.e("billingv6","usercancelbilling>>")

            }
        }
    }

    fun userPurchased(context: Context, purchases:List<PurchaseInfo> ){
        if (singleCall) {
            singleCall = false
            GlobalScope.launch {
                Quimera(context, FirebaseCrashlytics.getInstance())
                    .sendPurchaseInfo(
                        BillingClient.BillingResponseCode.OK.toString(),
                        Gson().toJson(purchases)
                    )

            }
        }
    }

    fun itemAlreadyOwned(context: Context){
        if (singleCall) {
            singleCall = false
            GlobalScope.launch {
                Quimera(context, FirebaseCrashlytics.getInstance())
                    .sendPurchaseInfo(
                        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED.toString(),
                        ""
                    )

            }
        }
    }

    fun developerError(context: Context){
        if (singleCall) {
            singleCall = false
            GlobalScope.launch {
                Quimera(context, FirebaseCrashlytics.getInstance())
                    .sendPurchaseInfo(
                        BillingClient.BillingResponseCode.DEVELOPER_ERROR.toString(),
                        ""
                    )

            }
        }
    }

    fun billingError(context: Context){
        if (singleCall) {
            singleCall = false
            GlobalScope.launch {
                Quimera(context, FirebaseCrashlytics.getInstance())
                    .sendPurchaseInfo(BillingClient.BillingResponseCode.ERROR.toString(), "")

            }
        }
    }

    fun serviceUnavailable(context: Context){
        if (singleCall) {
            singleCall = false
            GlobalScope.launch {
                Quimera(context, FirebaseCrashlytics.getInstance())
                    .sendPurchaseInfo(
                        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE.toString(),
                        ""
                    )

            }
        }
    }

    fun serviceDisconnected(context: Context){
        if (singleCall) {
            singleCall = false
            GlobalScope.launch {
                Quimera(context, FirebaseCrashlytics.getInstance())
                    .sendPurchaseInfo(
                        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED.toString(),
                        ""
                    )

            }
        }
    }

    fun billingUnavailable(context: Context){
        if (singleCall) {
            singleCall = false
            GlobalScope.launch {
                Quimera(context, FirebaseCrashlytics.getInstance())
                    .sendPurchaseInfo(
                        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE.toString(),
                        ""
                    )

            }
        }
    }
    fun itemUnavailable(context: Context){
        if (singleCall) {
            singleCall = false
            GlobalScope.launch {
                Quimera(context, FirebaseCrashlytics.getInstance())
                    .sendPurchaseInfo(
                        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE.toString(),
                        ""
                    )

            }
        }
    }

    fun itemNotOwned(context: Context){
        if (singleCall) {
            singleCall = false
            GlobalScope.launch {
                Quimera(context, FirebaseCrashlytics.getInstance())
                    .sendPurchaseInfo(
                        BillingClient.BillingResponseCode.ITEM_NOT_OWNED.toString(),
                        ""
                    )
            }
        }
    }

    fun sendSkuDetails(context: Context, skuDetails:SubscriptionItem){
        GlobalScope.launch {
            Quimera(context = context, FirebaseCrashlytics.getInstance())
                .sendPurchaseInfo("-400", Gson().toJson(skuDetails))

            Log.e("billingv5","sendSkuDetail>>"+Gson().toJson(skuDetails))
        }

    }
}