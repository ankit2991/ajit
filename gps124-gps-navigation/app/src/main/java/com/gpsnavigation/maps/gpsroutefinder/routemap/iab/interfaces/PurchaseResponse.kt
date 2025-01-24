package com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces

import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.ProductItem

interface PurchaseResponse
{
    fun isAlreadyOwned()
    fun userCancelled(productItem: ProductItem)
    fun ok(productItem: ProductItem)
    fun error(error: String)
}