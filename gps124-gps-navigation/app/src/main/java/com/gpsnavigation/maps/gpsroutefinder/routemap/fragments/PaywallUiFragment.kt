package com.gpsnavigation.maps.gpsroutefinder.routemap.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.adapty.errors.AdaptyError
import com.adapty.models.AdaptyPaywall
import com.adapty.models.AdaptyPaywallProduct
import com.adapty.models.AdaptyProfile
import com.adapty.models.AdaptyPurchasedInfo
import com.adapty.models.AdaptyViewConfiguration
import com.adapty.ui.AdaptyPaywallInsets
import com.adapty.ui.AdaptyPaywallView
import com.adapty.ui.AdaptyUI
import com.adapty.ui.listeners.AdaptyUiDefaultEventListener
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.example.routesmap.viewModels.SubscriptionViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.SUBSCRIBE_BILLING
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.subscription.SubscriptionItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.quimeraManager.QuimeraInit
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.CurrentDateUtil
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.isOnline
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.onReceiveSystemBarsInsets
import org.koin.androidx.viewmodel.ext.android.viewModel


class PaywallUiFragment : Fragment(R.layout.fragment_paywall_ui) {
    val subscriptionViewModel: SubscriptionViewModel by viewModel()

    companion object {
        fun newInstance(
            paywall: AdaptyPaywall,
            products: List<AdaptyPaywallProduct>,
            viewConfig: AdaptyViewConfiguration,
        ) =
            PaywallUiFragment().apply {
                this.paywall = paywall
                this.products = products
                this.viewConfiguration = viewConfig
            }
    }

    private var viewConfiguration: AdaptyViewConfiguration? = null
    private var paywall: AdaptyPaywall? = null
    private var products = listOf<AdaptyPaywallProduct>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paywallView = view as? AdaptyPaywallView ?: return
        val viewConfig = viewConfiguration ?: return
        val paywall = paywall ?: return


        paywallView.setEventListener(
            object : AdaptyUiDefaultEventListener() {

                /**
                 * You can override more methods if needed
                 */

                override fun onPurchaseSuccess(
                    purchasedInfo: AdaptyPurchasedInfo?,
                    product: AdaptyPaywallProduct,
                    view: AdaptyPaywallView
                ) {
                    super.onPurchaseSuccess(purchasedInfo, product, view)

                    if (purchasedInfo != null) {
                        try {
                            if (context != null) {
                                if (context!!.isOnline(context!!)) {
                                    QuimeraInit.userPurchased_adapty(
                                        context!!,
                                        purchasedInfo.purchase
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }


                        var productId = purchasedInfo.purchase.products?.get(0)
                        var price3 = products?.get(0)?.price?.localizedString
                        val adjustEvent = AdjustEvent("5xko3p")
                        adjustEvent.addCallbackParameter(
                            SUBSCRIBE_BILLING,
                            "SubId_${productId}_Price_${price3}_Date_${CurrentDateUtil.getCurrentDate2()}_PlacementScreen_PremiumScreen"
                        )
                        adjustEvent.setCallbackId(SUBSCRIBE_BILLING)
                        Adjust.trackEvent(adjustEvent)
                        Log.e(
                            "AdjustEvent",
                            "The event id:5xko3p  name:$SUBSCRIBE_BILLING SubId:$productId Price:$price3 Date:${CurrentDateUtil.getCurrentDate2()} PlacementScreen:PremiumScreen has been sent to Adjust"
                        )

                        subscriptionViewModel.setUserSubscribed(true)
                        subscriptionViewModel.setAutoAdsRemoved(true)
                        TinyDB.getInstance(requireActivity()).putBoolean(TinyDB.IS_PREMIUM, true)
                        activity?.finish()
                    }
                }

                override fun onProductSelected(
                    product: AdaptyPaywallProduct,
                    view: AdaptyPaywallView
                ) {
                    super.onProductSelected(product, view)


                    if (context != null) {
                        try {
                            if (context!!.isOnline(context!!)) {
                                QuimeraInit.sendSkuDetails(
                                    context!!,
                                    SubscriptionItem(product.productDetails)
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onPurchaseFailure(
                    error: AdaptyError,
                    product: AdaptyPaywallProduct,
                    view: AdaptyPaywallView
                ) {
                    super.onPurchaseFailure(error, product, view)

                    if (context != null) {

                        try {
                            if (context!!.isOnline(context!!)) {
                                QuimeraInit.billingError(context!!)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (activity != null) {
                        Toast.makeText(activity, "something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }


                override fun onActionPerformed(action: AdaptyUI.Action, view: AdaptyPaywallView) {
                    super.onActionPerformed(action, view)

                    action?.let {
                        if (it.equals(AdaptyUI.Action.Close)) {
                            activity?.finish()
                        } else {

                        }
                    }

                }

                override fun onPurchaseStarted(
                    product: AdaptyPaywallProduct,
                    view: AdaptyPaywallView
                ) {
                    super.onPurchaseStarted(product, view)

                    Log.e("onPurchaseStarted", ">")
                    if (context != null) {
                        try {
                            if (context!!.isOnline(context!!)) {
                                QuimeraInit.sendSkuDetails(
                                    context!!,
                                    SubscriptionItem(product.productDetails)
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }


                override fun onPurchaseCanceled(
                    product: AdaptyPaywallProduct,
                    view: AdaptyPaywallView
                ) {
                    super.onPurchaseCanceled(product, view)

                    if (context != null) {
                        try {
                            if (context!!.isOnline(context!!)) {
                                QuimeraInit.userCancelBilling(context!!)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            e.printStackTrace()
                        }
                    }
                }

                override fun onRestoreSuccess(
                    profile: AdaptyProfile,
                    view: AdaptyPaywallView,
                ) {
                    if (profile.accessLevels["premium"]?.isActive == true) {
                        subscriptionViewModel.setUserSubscribed(true)
                        subscriptionViewModel.setAutoAdsRemoved(true)
                        TinyDB.getInstance(requireActivity()).putBoolean(TinyDB.IS_PREMIUM, true)
                        activity?.finish()
                    }
                }
            }
        )

        /**
         * You need the `onReceiveSystemBarsInsets` callback only in case the status bar or
         * navigation bar overlap the view otherwise it may not be called, so simply
         * call `paywallView.showPaywall(paywall, products, viewConfig, AdaptyPaywallInsets.NONE)`
         */
        paywallView.onReceiveSystemBarsInsets { insets ->
            val paywallInsets = AdaptyPaywallInsets.of(insets.top, insets.bottom)
            val customTags = mapOf("USERNAME" to "test")
            paywallView.showPaywall(
                paywall,
                products,
                viewConfig,
                paywallInsets,
                tagResolver = { tag -> customTags[tag] }
            )
        }

        /**
         * Also you can get the `AdaptyPaywallView` and set eventListener and paywall right away
         * by calling `AdaptyUi.getPaywallView()`
         */
    }


}