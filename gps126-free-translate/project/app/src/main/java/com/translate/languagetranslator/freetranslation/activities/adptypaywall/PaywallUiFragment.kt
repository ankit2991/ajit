package com.translate.languagetranslator.freetranslation.activities.adptypaywall

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.adapty.errors.AdaptyError
import com.adapty.models.AdaptyPaywall
import com.adapty.models.AdaptyPaywallProduct
import com.adapty.models.AdaptyProfile
import com.adapty.models.AdaptyPurchasedInfo
import com.adapty.ui.AdaptyPaywallInsets
import com.adapty.ui.AdaptyPaywallView
import com.adapty.ui.AdaptyUI
import com.adapty.ui.listeners.AdaptyUiDefaultEventListener
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.TinyDB
import com.translate.languagetranslator.freetranslation.quimera.QuimeraInit
import com.translate.languagetranslator.freetranslation.quimera.SubscriptionItem

class PaywallUiFragment : Fragment(R.layout.fragment_paywall_ui) {

    companion object {
        fun newInstance(
            paywall: AdaptyPaywall,
            products: List<AdaptyPaywallProduct>,
            viewConfig: AdaptyUI.ViewConfiguration,
            fromScreen:Boolean
        ) =
            PaywallUiFragment().apply {
                this.paywall = paywall
                this.products = products
                this.viewConfiguration = viewConfig
                this.from = fromScreen
            }
    }

    private var viewConfiguration: AdaptyUI.ViewConfiguration? = null
    private var paywall: AdaptyPaywall? = null
    private var products = listOf<AdaptyPaywallProduct>()
    var from  =false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val paywallView = view as? AdaptyPaywallView ?: return
        val viewConfig = viewConfiguration ?: return
        val paywall = paywall ?: return

        paywallView.setEventListener(
            object: AdaptyUiDefaultEventListener() {

                /**
                 * You can override more methods if needed
                 */

                override fun onPurchaseSuccess(
                    purchasedInfo: AdaptyPurchasedInfo?,
                    product: AdaptyPaywallProduct,
                    view: AdaptyPaywallView
                ) {
                    super.onPurchaseSuccess(purchasedInfo, product, view)

                    if (purchasedInfo != null){

//                        var productId = purchasedInfo.purchase.products?.get(0)
//                        var price3 = products?.get(0)?.price?.localizedString

                        if (purchasedInfo != null){
                            if (context != null){
                                QuimeraInit.userPurchased(context!!,purchasedInfo.purchase)
                            }
                        }
                        if (activity != null) {
                            TinyDB.getInstance(activity!!).putBoolean(Constants.IS_PREMIUM, true)
                            val intent = Intent(activity, HomeActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            activity!!.finish()
                        }
                    }
                }

                override fun onProductSelected(
                    product: AdaptyPaywallProduct,
                    view: AdaptyPaywallView
                ) {
                    super.onProductSelected(product, view)
                    if (context != null) {
                        QuimeraInit.sendSkuDetails(context!!, SubscriptionItem(product.productDetails))
                    }

                }

                override fun onPurchaseFailure(
                    error: AdaptyError,
                    product: AdaptyPaywallProduct,
                    view: AdaptyPaywallView
                ) {
                    super.onPurchaseFailure(error, product, view)
                    if (context != null){
                        QuimeraInit.billingError(context!!)
                    }

                    if (activity != null) {
                        Toast.makeText(activity, "something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }


                override fun onActionPerformed(action: AdaptyUI.Action, view: AdaptyPaywallView) {
                    super.onActionPerformed(action, view)

                   action?.let {
                       if (it.equals(AdaptyUI.Action.Close)){
                           if (from) {
                               startActivity(
                                   Intent(activity, HomeActivity::class.java).setFlags(
                                       Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                   )
                               )
                               activity?.finish()
                           }else{
                               activity?.finish()
                           }
                       }else{

                       }
                   }

                }

                override fun onPurchaseStarted(
                    product: AdaptyPaywallProduct,
                    view: AdaptyPaywallView
                ) {
                    super.onPurchaseStarted(product, view)

                    Log.e("onPurchaseStarted",">")
                    if (context != null) {
                        QuimeraInit.sendSkuDetails(context!!, SubscriptionItem(product.productDetails))
                    }
                }


                override fun onPurchaseCanceled(
                    product: AdaptyPaywallProduct,
                    view: AdaptyPaywallView
                ) {
                    super.onPurchaseCanceled(product, view)
                    if (context != null){
                        QuimeraInit.userCancelBilling(context!!)
                    }

                }



                override fun onRestoreSuccess(
                    profile: AdaptyProfile,
                    view: AdaptyPaywallView,
                ) {
                    if (profile.accessLevels["premium"]?.isActive == true) {
                        if (activity != null) {
                            TinyDB.getInstance(activity!!).putBoolean(Constants.IS_PREMIUM, true)
                            val intent = Intent(activity, HomeActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            activity!!.finish()
                        }
//                        parentFragmentManager.popBackStack()
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
                viewConfig,
                products,
                paywallInsets,
                tagResolver = { tag -> customTags[tag] }
            )
        }

        /**
         * Also you can get the `AdaptyPaywallView` and set eventListener and paywall right away
         * by calling `AdaptyUi.getPaywallView()`
         */
    }

    fun View.onReceiveSystemBarsInsets(action: (insets: Insets) -> Unit) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            ViewCompat.setOnApplyWindowInsetsListener(this, null)
            action(systemBarInsets)
            insets
        }
    }

}