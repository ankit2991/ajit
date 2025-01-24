package com.gpsnavigation.maps.gpsroutefinder.routemap.iab.onetime

import com.android.billingclient.api.ProductDetails
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.ProductItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.enums.OneTimePurchaseSku

class OneTimePurchaseItem(productDetails: ProductDetails) : ProductItem(productDetails)
{
    val isConsumable = OneTimePurchaseSku.getConsumableFromSku(sku)
    var purchasedItem: PurchasedItem? = null
    var price = productDetails.oneTimePurchaseOfferDetails!!.formattedPrice
}