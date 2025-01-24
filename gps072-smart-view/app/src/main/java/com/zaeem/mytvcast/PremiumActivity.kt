package com.zaeem.mytvcast

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ironsource.mediationsdk.IronSource
import com.zaeem.mytvcast.Utils.AdsManager
import com.zaeem.mytvcast.Utils.TinyDB
import com.zaeem.mytvcast.databinding.ActivityShopBinding
import games.moisoni.google_iab.BillingConnector
import games.moisoni.google_iab.BillingEventListener
import games.moisoni.google_iab.enums.ErrorType
import games.moisoni.google_iab.models.BillingResponse
import games.moisoni.google_iab.models.PurchaseInfo
import games.moisoni.google_iab.models.SkuInfo

class PremiumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopBinding
    private val LICENSE_KEY =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg+Fwtfyeb6yG7+LGYNMwaWIHK/rNksb5c/s2VJ1VfT8ugbCjaIL/FfmBUfBMAxfuo0fiVje0uSLfKXiLYee+Hsn2iAZpvpSrOf5qtHvSTAS1JFGhyq0qL6FUz1nc41Lzg+kCdUj4dtMfxfSbJ6DPcxUhQf3hCKQF6EW4RuGo/cc9sRsvzn//tramOYpSR9qil8A9FHTBNsq465psFDAPVaWGV4wAKl7A7LZJlgE42KuiGpZQYUKGgQiwZxTsrF34iBA5G/IIQuGn1/auSCWRQZFbrLL6iYO5Loo54KKkAUpDX+ChpVKnCipKBT8pxbT7akiBku1kgapdlc6bK8KvfQIDAQAB"

    private val SKU = BuildConfig.APPLICATION_ID + ".premium"
    private val skus = listOf(SKU)
    private var billingConnector: BillingConnector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initIAB()

        binding.btnClose.setOnClickListener {
            AdsManager.showInter(this, {finish()}, {finish()})
        }

        binding.privacyPolicy.setOnClickListener {
            startActivity(
                WebViewActivity.getIntent(
                    this,
                    getString(R.string.privacy_policy),
                    "https://www.socem.com/PrivacyPolicy.html"
                )
            )

        }
        binding.termsOfUse.setOnClickListener {

            startActivity(
                WebViewActivity.getIntent(
                    this,
                    getString(R.string.privacy_policy),
                    "https://www.socem.com/TermsOfUse.html"
                )
            )
        }

        binding.purchaseBtn.setOnClickListener {

            billingConnector?.purchase(this, SKU)
        }
    }

    private fun initIAB() {
        billingConnector = BillingConnector(this, LICENSE_KEY)
//            .setConsumableIds(consumableIds)
//            .setNonConsumableIds(nonConsumableIds)
            .setSubscriptionIds(skus)
            .autoAcknowledge()
            .autoConsume()
            .enableLogging()
            .connect()

        billingConnector?.setBillingEventListener(object : BillingEventListener {
            override fun onProductsFetched(skuDetails: List<SkuInfo>) {
                /*Provides a list with fetched products*/

                skuDetails.first { it.sku == SKU }.let {
                    binding.purchaseBtn.text ="Subscribe now for ${it.price} ${it.priceCurrencyCode} / month"
                }
            }

            override fun onPurchasedProductsFetched(purchases: List<PurchaseInfo>) {
                /*Provides a list with fetched purchased products*/

                if (purchases.isEmpty()) {
                    TinyDB.getInstance(this@PremiumActivity)
                        .savePremium(this@PremiumActivity, false)
                } else {
                    TinyDB.getInstance(this@PremiumActivity).savePremium(this@PremiumActivity, true)

                }
            }

            override fun onProductsPurchased(purchases: List<PurchaseInfo>) {

                if (purchases.isEmpty()) {
                    TinyDB.getInstance(this@PremiumActivity)
                        .savePremium(this@PremiumActivity, false)
                } else {
                    TinyDB.getInstance(this@PremiumActivity).savePremium(this@PremiumActivity, true)
                    Toast.makeText(this@PremiumActivity,"Purchases successfully!",Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            override fun onPurchaseAcknowledged(purchase: PurchaseInfo) {

                /*Callback after a purchase is acknowledged*/

                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */
            }

            override fun onPurchaseConsumed(purchase: PurchaseInfo) {
                /*Callback after a purchase is consumed*/

                /*
                 * CONSUMABLE products entitlement can be granted either here or in onProductsPurchased
                 * */
            }

            override fun onBillingError(
                billingConnector: BillingConnector,
                response: BillingResponse
            ) {
                /*Callback after an error occurs*/
                when (response.errorType) {
                    ErrorType.CLIENT_NOT_READY -> {
                    }
                    ErrorType.CLIENT_DISCONNECTED -> {
                    }
                    ErrorType.SKU_NOT_EXIST -> {
                    }
                    ErrorType.CONSUME_ERROR -> {
                    }
                    ErrorType.ACKNOWLEDGE_ERROR -> {
                    }
                    ErrorType.ACKNOWLEDGE_WARNING -> {
                    }
                    ErrorType.FETCH_PURCHASED_PRODUCTS_ERROR -> {
                    }
                    ErrorType.BILLING_ERROR -> {
                    }
                    ErrorType.USER_CANCELED -> {
                    }
                    ErrorType.SERVICE_UNAVAILABLE -> {
                    }
                    ErrorType.BILLING_UNAVAILABLE -> {
                    }
                    ErrorType.ITEM_UNAVAILABLE -> {
                    }
                    ErrorType.DEVELOPER_ERROR -> {
                    }
                    ErrorType.ERROR -> {
                    }
                    ErrorType.ITEM_ALREADY_OWNED -> {
                    }
                    ErrorType.ITEM_NOT_OWNED -> {
                    }
                }
            }
        })

    }

    override fun onResume() {
        super.onResume()

        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

}