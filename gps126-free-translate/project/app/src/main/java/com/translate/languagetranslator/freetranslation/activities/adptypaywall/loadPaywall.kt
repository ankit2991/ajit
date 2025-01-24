package com.translate.languagetranslator.freetranslation.activities.adptypaywall

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.adapty.Adapty
import com.adapty.ui.AdaptyUI
import com.adapty.utils.AdaptyResult
import com.translate.languagetranslator.freetranslation.appUtils.Constants


fun isOnline(c: Context): Boolean {
    val cmg = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+
        cmg.getNetworkCapabilities(cmg.activeNetwork)?.let { networkCapabilities ->
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
    } else {
        return cmg.activeNetworkInfo?.isConnectedOrConnecting == true
    }

    return false
}
fun loadPaywall(paywallType: String) {

        Adapty.getPaywall(paywallType) { paywallResult ->
            when (paywallResult) {
                is AdaptyResult.Success -> {
                    val paywall = paywallResult.value

                    if (paywallType == Constants.GPS_ONBOARDING) {
                        Constants.adaptyPaywallOnboarding = paywall
                    } else if (paywallType == Constants.GPS_PREMIUM) {
                        Constants.adaptyPaywallPremium = paywall
                    } else if (paywallType == Constants.GPS_DEFAULT) {
                        Constants.adaptyPaywallDefault = paywall
                    }
                    Adapty.getPaywallProducts(paywall) { productResult ->

                        when (productResult) {
                            is AdaptyResult.Success -> {
                                val products = productResult.value

                                if (paywallType == Constants.GPS_ONBOARDING) {
                                    Constants.adaptyProductOnboarding = products
                                } else if (paywallType == Constants.GPS_PREMIUM) {
                                    Constants.adaptyProductPremium = products
                                } else if (paywallType == Constants.GPS_DEFAULT) {
                                    Constants.adaptyProductDefault = products
                                }

                                AdaptyUI.getViewConfiguration(paywall) { configResult ->
                                    when (configResult) {
                                        is AdaptyResult.Success -> {
                                            val viewConfig = configResult.value

                                            if (paywallType == Constants.GPS_ONBOARDING) {
                                                Constants.adaptyConfigOnboarding = viewConfig
                                            } else if (paywallType == Constants.GPS_PREMIUM) {
                                                Constants.adaptyConfigPremium = viewConfig
                                            } else if (paywallType == Constants.GPS_DEFAULT) {
                                                Constants.adaptyConfigDefault = viewConfig
                                            }
                                        }

                                        is AdaptyResult.Error -> {
//                                                errorView.text =
//                                                    "error:\n${configResult.error.message}"

                                        }

                                        else -> {}
                                    }
                                }


//                                if (paywall.hasViewConfiguration) {
//                                    with(presentPaywall) {
//                                        isEnabled = true
//                                        text = "Present paywall"
//                                    }
//
//                                } else {
//                                    visualValue.text = "no"
//                                    with(presentPaywall) {
//                                        isEnabled = false
//                                        text = "No view configuration"
//                                    }
//                                }
                            }

                            is AdaptyResult.Error -> {

                            }
                        }
                    }
                }

                is AdaptyResult.Error -> {
//                    errorView.text = "error:\n${paywallResult.error.message}"

                }
            }
        }
    }