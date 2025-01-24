package com.messaging.textrasms.manager.common.iap

import com.android.billingclient.api.Purchase
import com.messaging.textrasms.manager.common.iap.BillingServiceListener

interface SubscriptionServiceListener : BillingServiceListener {

    fun onSubscriptionRestored(purchaseInfo: Purchase)

    fun onSubscriptionPurchased(purchaseInfo: Purchase)

    fun onSubscriptionsExpired()
}