package com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces

import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.ProductItem

interface QueryResponse<in T : ProductItem>
{
    fun error(responseCode: Int)
    fun ok(skuItems: List<T>)
}