package callerid.truecaller.trackingnumber.phonenumbertracker.block.iap

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

abstract class IBillingService {

    private val purchaseServiceListeners: MutableList<PurchaseServiceListener> = mutableListOf()
    private val subscriptionServiceListeners: MutableList<SubscriptionServiceListener> =
        mutableListOf()
    private val billingClientConnectedListeners: MutableList<BillingClientConnectionListener> = mutableListOf()


    fun addBillingClientConnectionListener(billingClientConnectionListener: BillingClientConnectionListener) {
        billingClientConnectedListeners.add(billingClientConnectionListener)
    }

    fun isBillingClientConnected(status: Boolean, responseCode: Int) {
        findUiHandler().post {
            for (billingServiceListener in billingClientConnectedListeners) {
                billingServiceListener.onConnected(status, responseCode)
            }
        }
    }

    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.add(purchaseServiceListener)
    }

    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.remove(purchaseServiceListener)
    }

    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        subscriptionServiceListeners.add(subscriptionServiceListener)
    }

    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        subscriptionServiceListeners.remove(subscriptionServiceListener)
    }

    fun productOwned(purchase: Purchase, isRestore: Boolean) {
        findUiHandler().post {
            productOwnedInternal(purchase, isRestore)
        }
    }

    private fun productOwnedInternal(purchase: Purchase, isRestore: Boolean) {
        for (purchaseServiceListener in purchaseServiceListeners) {
            if (isRestore) {
                purchaseServiceListener.onProductRestored(purchase)
            } else {
                purchaseServiceListener.onProductPurchased(purchase)
            }
        }
    }

    fun subscriptionOwned(purchase: Purchase, isRestore: Boolean) {
        findUiHandler().post {
            subscriptionOwnedInternal(purchase, isRestore)
        }
    }

    fun subscriptionsExpired() {
        findUiHandler().post {
            for (subscriptionServiceListener in subscriptionServiceListeners) {
                subscriptionServiceListener.onSubscriptionsExpired()
            }
        }
    }

    private fun subscriptionOwnedInternal(purchase: Purchase, isRestore: Boolean) {
        for (subscriptionServiceListener in subscriptionServiceListeners) {
            if (isRestore) {
                subscriptionServiceListener.onSubscriptionRestored(purchase)
            } else {
                subscriptionServiceListener.onSubscriptionPurchased(purchase)
            }
        }
    }

    fun updatePrices(iapKeyPrices: Map<String, ProductDetails>) {
        findUiHandler().post {
            updatePricesInternal(iapKeyPrices)
        }
    }

    private fun updatePricesInternal(iapKeyPrices: Map<String, ProductDetails>) {
        for (billingServiceListener in purchaseServiceListeners) {
            billingServiceListener.onPricesUpdated(iapKeyPrices)
        }
        for (billingServiceListener in subscriptionServiceListeners) {
            billingServiceListener.onPricesUpdated(iapKeyPrices)
        }
    }

    abstract fun init(key: String?)
    abstract fun buy(activity: Activity, productDetails: ProductDetails)
    abstract fun subscribe(activity: Activity, productDetails: ProductDetails)
    abstract fun enableDebugLogging(enable: Boolean)
    abstract fun restore()

    @CallSuper
    open fun close() {
        subscriptionServiceListeners.clear()
        purchaseServiceListeners.clear()
    }
}

fun findUiHandler(): Handler {
    return Handler(Looper.getMainLooper())
}