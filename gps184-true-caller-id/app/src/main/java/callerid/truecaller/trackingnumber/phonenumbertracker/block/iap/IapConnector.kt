package callerid.truecaller.trackingnumber.phonenumbertracker.block.iap

import android.app.Activity
import android.content.Context
import callerid.truecaller.trackingnumber.phonenumbertracker.block.google.Utils
import com.android.billingclient.api.ProductDetails
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
        const val PURCHASE_WEEKLY_SUBSCRIPTION = "weekly_subscription3"
        const val PURCHASE_YEARLY_SUBSCRIPTION = "yearly_subscription3"
        private var manager: IapConnector? = null

        fun getInstance(context: Context): IapConnector {
            if (manager == null) {
                val subscriptionKeys = listOf(
                    PURCHASE_WEEKLY_SUBSCRIPTION,
                    PURCHASE_YEARLY_SUBSCRIPTION
                )
                manager = IapConnector(context, subscriptionKeys = subscriptionKeys)
            }
            return manager!!
        }
    }

    fun getOriginPricingPhase(productDetails: ProductDetails): ProductDetails.PricingPhase? {
        return productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.lastOrNull()
    }
}
