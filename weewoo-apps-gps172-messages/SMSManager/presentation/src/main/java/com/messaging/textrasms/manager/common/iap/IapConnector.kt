package com.messaging.textrasms.manager.common.iap

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.ProductDetails
import com.messaging.textrasms.manager.R
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * Initialize billing service.
 *
 * @param context Application context.
 * @param consumableKeys SKU list for consumable one-time products.
 * @param subscriptionKeys SKU list for subscriptions.
 * @param key Key to verify purchase messages. Leave it empty if you want to skip verification.
 * @param enableLogging Log operations/errors to the logcat for debugging purposes.
 */
@OptIn(DelicateCoroutinesApi::class)
class IapConnector @JvmOverloads constructor(
    context: Context,
    consumableKeys: List<String> = emptyList(),
    subscriptionKeys: List<String> = emptyList(),
    key: String? = null,
    enableLogging: Boolean = false
) {

    private var mBillingService: IBillingService? = null

    init {
        val contextLocal = context.applicationContext ?: context
        mBillingService =
            BillingService(contextLocal, consumableKeys, subscriptionKeys)
        getBillingService().init(key)
        getBillingService().enableDebugLogging(enableLogging)
    }

    fun addBillingConnectedListener(billingClientConnectionListener: BillingClientConnectionListener) {
        getBillingService().addBillingClientConnectionListener(billingClientConnectionListener)
    }

    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        getBillingService().addPurchaseListener(purchaseServiceListener)
    }

    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        getBillingService().removePurchaseListener(purchaseServiceListener)
    }

    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        getBillingService().addSubscriptionListener(subscriptionServiceListener)
    }

    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        getBillingService().removeSubscriptionListener(subscriptionServiceListener)
    }

    fun purchase(activity: Activity, productDetails: ProductDetails) {
        getBillingService().buy(activity, productDetails)
    }

    fun subscribe(activity: Activity, productDetails: ProductDetails) {
        getBillingService().subscribe(activity, productDetails)
    }

    fun restore() {
        getBillingService().restore()
    }

    fun destroy() {
        getBillingService().close()
    }

    private fun getBillingService(): IBillingService {
        return mBillingService ?: let {
            throw RuntimeException("Call IapConnector to initialize billing service")
        }
    }




    companion object {
        //        const val PURCHASE_MONTHLY_SUBSCRIPTION = "monthly_subscription3"
        private var manager: IapConnector? = null

        fun getInstance(context: Context): IapConnector {
            if (manager == null) {
                val subscriptionKeys = listOf(
                    "com.messaging.textrasms.manager_29.99",
                    "com.messaging.textrasms.manager_9.99",
                    "com.messaging.textrasms.manager_2.99"
                )
                manager =
                    IapConnector(context, subscriptionKeys = subscriptionKeys, key = context.getString(R.string.base64key))
            }
            return manager!!
        }
    }

    fun getOriginPricingPhase(productDetails: ProductDetails): ProductDetails.PricingPhase? {
        return productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()
    }
}
