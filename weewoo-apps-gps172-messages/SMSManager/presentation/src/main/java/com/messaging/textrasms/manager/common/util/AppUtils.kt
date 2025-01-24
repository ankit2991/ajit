package com.messaging.textrasms.manager.common.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.lifecycle.Lifecycle
import com.messaging.textrasms.manager.feature.Activities.MainActivity



object AppUtils {

    fun updateConfig(context: Context, config: Configuration?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(config!!)
        } else {
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }

    fun showSubscriptions(context: Context, sku: String) {
        try {
            val url =
                "https://play.google.com/store/account/subscriptions?&package=" + context.packageName
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            i.setPackage("com.android.vending")
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(i)
        } catch (error: Exception) {
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${context.packageName}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                    )
                )
            }
        }
    }


    //No Internet Dialog

//    fun showNoInternetDialog(context: Activity, lifecycle: Lifecycle){
//        // No Internet Dialog: Pendulum
//        NoInternetDialogPendulum.Builder(
//            context,
//            lifecycle
//        ).apply {
//            dialogProperties.apply {
//                connectionCallback = object : ConnectionCallback { // Optional
//                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
//                        // ...
//
//                        if (hasActiveConnection){
//                            val intent = Intent(context, MainActivity::class.java)
//                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            context.startActivity(intent)
//                            context.finish()
//
//                        }
//                    }
//                }
//
//                cancelable = false // Optional
//                noInternetConnectionTitle = "No Internet" // Optional
//                noInternetConnectionMessage =
//                    "Check your Internet connection and try again." // Optional
//                showInternetOnButtons = true // Optional
//                pleaseTurnOnText = "Please turn on" // Optional
//                wifiOnButtonText = "Wifi" // Optional
//                mobileDataOnButtonText = "Mobile data" // Optional
//
//                onAirplaneModeTitle = "No Internet" // Optional
//                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
//                pleaseTurnOffText = "Please turn off" // Optional
//                airplaneModeOffButtonText = "Airplane mode" // Optional
//                showAirplaneModeOffButtons = true // Optional
//            }
//        }.build()
//    }
}