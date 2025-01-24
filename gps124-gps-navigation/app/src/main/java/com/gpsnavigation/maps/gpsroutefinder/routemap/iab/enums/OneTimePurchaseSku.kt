package com.gpsnavigation.maps.gpsroutefinder.routemap.iab.enums


enum class OneTimePurchaseSku(val sku: String, val isConsumable: Boolean)
{
    REMOVE_ADS("ads_remove", true);

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

        fun getConsumableFromSku(sku: String): Boolean
        {
            for (item in values())
            {
                if (item.sku == sku)
                {
                    return item.isConsumable
                }
            }
            return false
        }
    }
}