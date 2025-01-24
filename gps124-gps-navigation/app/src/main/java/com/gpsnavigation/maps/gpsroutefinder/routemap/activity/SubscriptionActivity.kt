//package com.gpsnavigation.maps.gpsroutefinder.routemap.activity
//
//import android.animation.ValueAnimator
//import android.os.Build
//import android.os.Bundle
//import android.text.Html
//import android.text.SpannableStringBuilder
//import android.text.Spanned
//import android.text.method.LinkMovementMethod
//import android.text.style.ClickableSpan
//import android.text.style.URLSpan
//import android.view.View
//import android.view.animation.Animation
//import android.view.animation.AnimationUtils
//import android.widget.TextView
//import androidx.core.content.ContextCompat
//import com.example.routesmap.viewModels.SubscriptionViewModel
//import com.gpsnavigation.maps.gpsroutefinder.routemap.R
//import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
//import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.AppBillingClient
//import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.ProductItem
//import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.enums.SubscriptionSku
//import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces.ConnectResponse
//import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces.PurchaseResponse
//import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.subscription.SubscriptionItem
//import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB
//import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.TinyDB.IS_PREMIUM
//import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.navigateToNext
//import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.setUserAsPremium
//import kotlinx.android.synthetic.main.activity_subscription_new.*
//import org.jetbrains.anko.browse
//import org.koin.androidx.viewmodel.ext.android.viewModel
//import timber.log.Timber
//import java.text.DecimalFormat
//import java.util.regex.Matcher
//import java.util.regex.Pattern
//
//
//class SubscriptionActivity : BaseActivity() {
//
//    val subscriptionViewModel: SubscriptionViewModel by viewModel()
//    var subscriptionType = SubscriptionType.YEARLY
//
//    enum class SubscriptionType {
//        MONTHLY,
//        SIX_MONTH,
//        YEARLY
//    }
//
//    private var appBillingClient: AppBillingClient? = null
//    var monthlySkuDetail: SubscriptionItem? = null
//    var yearlySkuDetail: SubscriptionItem? = null
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        window?.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
//        window.decorView.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//        super.onCreate(savedInstanceState)
//        Timber.tag("Free_trial_onCreate").i("Free trial onCreate")
//        try {
//            setContentView(R.layout.activity_subscription_new)
//            appBillingClient = AppBillingClient()
//            getSubscriptionDetails()
//            setClickListenr()
//            val strRsc = resources.getString(R.string.our_term_of_services_applies_to_all_purchases)
//            setTextViewHTML(tvBottomTermsOfServices, strRsc)
//            toggleBackgrounds()
//            val animation = AnimationUtils.loadAnimation(this, R.anim.shake_to_15x)
//            animation.duration = 500
//            animation.repeatCount = Animation.INFINITE
//            animation.fillAfter = false
//            animation.repeatMode = ValueAnimator.REVERSE
//            btnUpgradeNow.startAnimation(animation)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    protected fun setTextViewHTML(text: TextView, html: String) {
//        var sequence: Spanned? = null
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            sequence = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
//        } else {
//            sequence = Html.fromHtml(html)
//        }
//        val strBuilder = SpannableStringBuilder(sequence)
//        val urls = strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
//        for (span in urls) {
//            makeLinkClickable(strBuilder, span)
//        }
//        text.text = strBuilder
//        text.movementMethod = LinkMovementMethod.getInstance()
//    }
//
//    protected fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan) {
//        val start = strBuilder.getSpanStart(span)
//        val end = strBuilder.getSpanEnd(span)
//        val flags = strBuilder.getSpanFlags(span)
//        val clickable = object : ClickableSpan() {
//            override fun onClick(view: View) {
//                try {
//                    browse(span.url)
//                } catch (e: java.lang.Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//        strBuilder.setSpan(clickable, start, end, flags)
//        strBuilder.removeSpan(span)
//    }
//
//
//    fun setClickListenr() {
//        btnUpgradeNow?.setOnClickListener { v: View? ->
//            Timber.tag("FreeTrial_upgrade_click").i("Free trial upgrade button clicked")
//            when (subscriptionType) {
//                SubscriptionType.MONTHLY -> {
//                    monthlySkuDetail?.let {
//                        appBillingClient?.purchaseSkuItem(this, it)
//                    }
//                }
//                SubscriptionType.SIX_MONTH -> {
//                    //  bp?.subscribe(this@SubscriptionActivity, skuSixMonth)
//                }
//                SubscriptionType.YEARLY -> {
//                    yearlySkuDetail?.let {
//                        appBillingClient?.purchaseSkuItem(this, it)
//                    }
//                }
//            }
//        }
//
//        rlMonthy.setOnClickListener {
//            subscriptionType = SubscriptionType.MONTHLY
//            toggleBackgrounds()
//        }
//
//        rlSixMonths.setOnClickListener {
//            subscriptionType = SubscriptionType.SIX_MONTH
//            toggleBackgrounds()
//        }
//
//        rlYearly.setOnClickListener {
//            subscriptionType = SubscriptionType.YEARLY
//            toggleBackgrounds()
//        }
//
//        closeImg?.setOnClickListener { v: View? ->
//            finish()
//        }
//    }
//
//    private fun toggleBackgrounds() {
//        when (subscriptionType) {
//            SubscriptionType.MONTHLY -> {
//                rlMonthy.setBackgroundResource(R.drawable.bg_yearly_free_trial)
//                tvMonthlyHeader.setTextColor(ContextCompat.getColor(this, R.color.white))
//                rlYearly.setBackgroundResource(R.drawable.bg_monthly_free_trial)
//                tvYearlyHeader.setTextColor(ContextCompat.getColor(this, R.color.dark_grey))
//                rlSixMonths.setBackgroundResource(R.drawable.bg_monthly_free_trial)
//                tvSixMonthsHeader.setTextColor(ContextCompat.getColor(this, R.color.dark_grey))
//            }
//            SubscriptionType.SIX_MONTH -> {
//                rlSixMonths.setBackgroundResource(R.drawable.bg_yearly_free_trial)
//                tvSixMonthsHeader.setTextColor(ContextCompat.getColor(this, R.color.white))
//                rlYearly.setBackgroundResource(R.drawable.bg_monthly_free_trial)
//                tvYearlyHeader.setTextColor(ContextCompat.getColor(this, R.color.dark_grey))
//                rlMonthy.setBackgroundResource(R.drawable.bg_monthly_free_trial)
//                tvMonthlyHeader.setTextColor(ContextCompat.getColor(this, R.color.dark_grey))
//            }
//            SubscriptionType.YEARLY -> {
//                rlYearly.setBackgroundResource(R.drawable.bg_yearly_free_trial)
//                tvYearlyHeader.setTextColor(ContextCompat.getColor(this, R.color.white))
//                rlSixMonths.setBackgroundResource(R.drawable.bg_monthly_free_trial)
//                tvSixMonthsHeader.setTextColor(ContextCompat.getColor(this, R.color.dark_grey))
//                rlMonthy.setBackgroundResource(R.drawable.bg_monthly_free_trial)
//                tvMonthlyHeader.setTextColor(ContextCompat.getColor(this, R.color.dark_grey))
//            }
//        }
//    }
//
//
//    private fun getSubscriptionDetails() {
//
//        appBillingClient?.connect(this, SubscriptionSku.MONTHLY.sku, object : ConnectResponse {
//            override fun disconnected() {
//                Timber.e("InappBilling connection disconnected.")
//            }
//
//            override fun billingUnavailable() {
//                Timber.e("InappBilling billing unavailable.")
//            }
//
//            override fun developerError() {
//                Timber.e("InappBilling developer error.")
//            }
//
//            override fun error() {
//                Timber.e("InappBilling simple error.")
//            }
//
//            override fun featureNotSupported() {
//                Timber.e("InappBilling feature not available.")
//            }
//
//            override fun itemUnavailable() {
//                Timber.e("InappBilling item not available.")
//            }
//
//            override fun ok(
//                //oneTimePurchaseItems: List<OneTimePurchaseItem>,
//                subscriptionItems: List<SubscriptionItem>
//            ) {
//                Timber.e("InappBilling connection ok do other .")
//                subscriptionItems.forEach {
//                    monthlySkuDetail = it
//                    it.subscribedItem?.purchaseTime?.let {
//                        setUserAsPremium(it)
//                    }
//
//                }
//                monthlySkuDetail?.let {
//                    tvMonthlyPrice?.text = "${it.pricingPhase?.priceCurrencyCode.orEmpty()}${it.pricingPhase?.formattedPrice.orEmpty()}"
//                }
//            }
//
//            override fun serviceDisconnected() {
//                Timber.e("InappBilling service disconnected.")
//            }
//
//            override fun serviceUnavailable() {
//                Timber.e("InappBilling service unavailable.")
//            }
//        }, object : PurchaseResponse {
//            override fun isAlreadyOwned() {
//
//            }
//
//            override fun userCancelled(productItem: ProductItem) {
//                TinyDB.getInstance(this@SubscriptionActivity).putBoolean(IS_PREMIUM, false)
//
//            }
//
//            override fun ok(productItem: ProductItem) {
//                subscriptionViewModel.setUserSubscribed(true)
//                subscriptionViewModel.setAutoAdsRemoved(true)
//                TinyDB.getInstance(this@SubscriptionActivity).putBoolean(IS_PREMIUM, true)
//                finish()
//            }
//
//            override fun error(error: String) {
//
//            }
//
//        })
//
//        appBillingClient?.connect(this, SubscriptionSku.YEARLY.sku, object : ConnectResponse {
//            override fun disconnected() {
//                Timber.e("InappBilling connection disconnected.")
//            }
//
//            override fun billingUnavailable() {
//                Timber.e("InappBilling billing unavailable.")
//            }
//
//            override fun developerError() {
//                Timber.e("InappBilling developer error.")
//            }
//
//            override fun error() {
//                Timber.e("InappBilling simple error.")
//            }
//
//            override fun featureNotSupported() {
//                Timber.e("InappBilling feature not available.")
//            }
//
//            override fun itemUnavailable() {
//                Timber.e("InappBilling item not available.")
//            }
//
//            override fun ok(
//                //oneTimePurchaseItems: List<OneTimePurchaseItem>,
//                subscriptionItems: List<SubscriptionItem>
//            ) {
//                Timber.e("InappBilling connection ok do other .")
//                subscriptionItems.forEach {
//                    yearlySkuDetail = it
//                    it.subscribedItem?.purchaseTime?.let {
//                        setUserAsPremium(it)
//                    }
//                }
//                yearlySkuDetail?.let {
//                    tvYearlyPrice?.text = "${it.pricingPhase?.priceCurrencyCode.orEmpty()}${it.pricingPhase?.formattedPrice.orEmpty()}"
//                    val extractedPrice = extractPrice(it.pricingPhase?.formattedPrice.orEmpty())
//                    if (extractedPrice > 0.0) {
//                        val permonthPrice = extractedPrice / 12
//                        val formater = DecimalFormat("0.00")
//                        labelYearlyDiscount?.text =
//                            "${it.pricingPhase?.priceCurrencyCode.orEmpty()}${formater.format(permonthPrice)}/${
//                                getString(
//                                    R.string.month
//                                )
//                            }"
//                    } else labelYearlyDiscount?.text = ""
//
//                }
//            }
//
//            override fun serviceDisconnected() {
//                Timber.e("InappBilling service disconnected.")
//            }
//
//            override fun serviceUnavailable() {
//                Timber.e("InappBilling service unavailable.")
//            }
//        }, object : PurchaseResponse {
//            override fun isAlreadyOwned() {
//
//            }
//
//            override fun userCancelled(productItem: ProductItem) {
//                TinyDB.getInstance(this@SubscriptionActivity).putBoolean(IS_PREMIUM, false)
//
//            }
//
//            override fun ok(productItem: ProductItem) {
//                subscriptionViewModel.setUserSubscribed(true)
//                subscriptionViewModel.setAutoAdsRemoved(true)
//                TinyDB.getInstance(this@SubscriptionActivity).putBoolean(IS_PREMIUM, true)
//
//                finish()
//            }
//
//            override fun error(error: String) {
//
//            }
//
//        })
//    }
//
//    var patternPrice: Pattern = Pattern.compile("(\\d+[[\\.,\\s]\\d+]*)")
//
//
//    fun extractPrice(price: String): Double {
//        val matcher: Matcher = patternPrice.matcher(price)
//        if (matcher.find()) {
//            var match: String = matcher.group()
//
//            // proceed post treatments to convert the String to a Double
//            // delete space from price if any
//            if (match.contains(" ")) match = match.replace(" ", "")
//            if (match.contains(",")) {
//                // price contains one comma and one dot: price format is something like that : 1,000,000.00
//                // OR
//                // there is more than one comma in the price (price is something like that 1,000,000)
//                // so delete the , from price
//                match = if (match.contains(".") || match.length - match.replace(
//                        ",",
//                        ""
//                    ).length > 1
//                ) match.replace(",", "") else match.replace(",", ".")
//            }
//            try {
//                return match.toDouble()
//            } catch (ex: Exception) {
//                return 0.0
//            }
//
//        } else return 0.0
//    }
//
//    override fun onBackPressed() {
//        navigateToNext(isPremium, finish = {
//            finish()
//        })
//    }
//
//
//}