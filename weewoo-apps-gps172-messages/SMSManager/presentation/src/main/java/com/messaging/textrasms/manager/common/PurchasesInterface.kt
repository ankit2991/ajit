package com.weewoo.sdkproject.billing

import com.android.billingclient.api.SkuDetails

interface PurchasesInterface {

    fun queryPurchases()
    fun querySkusSubsDetails(skus: List<String>, onSuccess: (List<SkuDetails>) -> Unit, onError: (code: Int, message: String) -> Unit)
    fun querySkusInAppDetails(skus: List<String>, onSuccess: (List<SkuDetails>) -> Unit, onError: (code: Int, message: String) -> Unit)
    fun startPurchaseFlow(sku: SkuDetails)
    fun destroy()
}