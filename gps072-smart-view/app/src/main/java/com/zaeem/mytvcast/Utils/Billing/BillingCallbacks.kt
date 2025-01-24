package com.zaeem.rokuremote.Billing

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails

interface BillingCallbacks {

    fun billingPurchased(purchases: MutableList<Purchase>?)
    fun billingCanceled()
    fun itemAlreadyOwned()
    fun onQuerySkuDetailsSubscription(skuDetailsList: MutableList<SkuDetails>?)
    fun onQuerySkuDetailsInApp(skuDetailsList: MutableList<SkuDetails>?)
    fun queryPurchaseResultInApp(purchases: MutableList<Purchase>?)
    fun queryPurchaseResultSubscription(purchases: MutableList<Purchase>?)
}