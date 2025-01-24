package com.messaging.textrasms.manager.feature.quimera

import com.android.billingclient.api.ProductDetails

abstract class ProductItem(val productDetails: ProductDetails)
{
    val sku = productDetails.productId
    val title = productDetails.title

}