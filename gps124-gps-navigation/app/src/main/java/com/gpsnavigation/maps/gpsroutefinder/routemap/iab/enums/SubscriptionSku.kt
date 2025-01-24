package com.gpsnavigation.maps.gpsroutefinder.routemap.iab.enums

import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.SUBSCRIPTION1

enum class SubscriptionSku(val sku: String)
{
    WEEKLY("weekly3"),
//    MONTHLY("monthly"),
    MONTHLY(SUBSCRIPTION1),
    YEARLY("year_ly");

    companion object
    {
        fun getMutableListOfSkus(): MutableList<String>
        {
            val skus = mutableListOf<String>()

            for (item in values())
            {
                skus.add(item.sku)
            }

            return skus
        }
    }
}