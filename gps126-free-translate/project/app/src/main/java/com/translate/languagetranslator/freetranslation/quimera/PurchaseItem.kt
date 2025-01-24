package com.translate.languagetranslator.freetranslation.quimera


import com.android.billingclient.api.AccountIdentifiers
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

class PurchaseItem(list: Purchase,productDetail: ProductDetails?) : PurchaseItems(list)
{
//    var subscribedItem: SubscribedItem? = null

    var order_Id = getPurchaseDetail().orderId

    // get the price of base plan
    var pricingPhase: ProductDetails.PricingPhase? = productDetail!!.subscriptionOfferDetails?.first()?.pricingPhases?.pricingPhaseList?.last()
    var purchaseTime = getPurchaseDetail().purchaseTime
    var PurchaseState = getPurchaseDetail().purchaseState
    var PurchaseToken = getPurchaseDetail().purchaseToken
    var AccountIdentifiers = getPurchaseDetail().accountIdentifiers
    var DeveloperPayload = getPurchaseDetail().developerPayload
    var IsAutoRenewing = getPurchaseDetail().isAutoRenewing
    var OriginalJson = getPurchaseDetail().originalJson
    var PackageName = getPurchaseDetail().packageName

    // get first of offers if have
    private fun getPurchaseDetail(): Purchase {
        return purchaseDetails
    }
}