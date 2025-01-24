package com.translate.languagetranslator.freetranslation.quimera

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

abstract class PurchaseItems(val purchaseDetails: Purchase)
{
    val sku = purchaseDetails.skus
    val orderId = purchaseDetails.orderId

}