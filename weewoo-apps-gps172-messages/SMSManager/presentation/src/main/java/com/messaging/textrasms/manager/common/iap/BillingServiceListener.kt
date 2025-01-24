package com.messaging.textrasms.manager.common.iap

import com.android.billingclient.api.ProductDetails

interface BillingServiceListener {
    fun onPricesUpdated(iapKeyPrices: Map<String, ProductDetails>)
}