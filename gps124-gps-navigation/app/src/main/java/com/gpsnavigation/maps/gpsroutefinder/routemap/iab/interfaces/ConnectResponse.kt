package com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces

import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.onetime.OneTimePurchaseItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.subscription.SubscriptionItem

interface ConnectResponse
{
    fun disconnected()
    fun billingUnavailable()
    fun developerError()
    fun error()
    fun featureNotSupported()
    fun itemUnavailable()
    //fun ok(oneTimePurchaseItems: List<OneTimePurchaseItem>, subscriptionItems: List<SubscriptionItem>)
    fun ok(subscriptionItems: List<SubscriptionItem>)
    fun serviceDisconnected()
    fun serviceUnavailable()
}