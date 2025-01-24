package callerid.truecaller.trackingnumber.phonenumbertracker.block.iap

import com.android.billingclient.api.ProductDetails

interface BillingServiceListener {
    fun onPricesUpdated(iapKeyPrices: Map<String, ProductDetails>)
}