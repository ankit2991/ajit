package com.lockerroom.face.iab

import games.moisoni.google_iab.models.ProductInfo
import kotlin.math.sign

object ProductPrice {


  fun  getProductPrice(productInfo: ProductInfo):String{

      var price = "$5"
      if (productInfo != null) {
          var size = productInfo.productDetails.subscriptionOfferDetails!!.size

           price = productInfo.productDetails.subscriptionOfferDetails!![size-1].pricingPhases.pricingPhaseList.last().formattedPrice
               .toString()
      }

      return price
  }
}