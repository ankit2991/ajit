package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.ScrollingMovementMethod
import android.text.style.TypefaceSpan
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.example.routesmap.viewModels.SubscriptionViewModel
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.Sub_Yearly
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.AppBillingClient
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.ProductItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.enums.SubscriptionSku
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces.ConnectResponse
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces.PurchaseResponse
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.subscription.SubscriptionItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.*

import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


//class PremiumActivity : BaseActivity() {
//
//    val subscriptionViewModel: SubscriptionViewModel by viewModel()
//    var subscriptionType = SubscriptionType.MONTHLY
//    private var fromWhere: Boolean = true
//
//    enum class SubscriptionType {
//        WEEKLY,
//        MONTHLY,
//        SIX_MONTH,
//        YEARLY
//    }
//
//    private var appBillingClient: AppBillingClient? = null
//    var monthlySkuDetail: SubscriptionItem? = null
//    var weeklySkuDetail: SubscriptionItem? = null
//    var yearlySkuDetail: SubscriptionItem? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        fromWhere = intent.getBooleanExtra("from", true)
//
//        if (fromWhere) {
//            setContentView(R.layout.activity_premium_paywall)
//            tv_onboarding.text = getString(R.string.full_access_caps)
//            tv_features1.text = getString(R.string.subs_feature_one)
//            tv_features2.text = getString(R.string.subs_feature_two)
//            tv_features3.text = getString(R.string.subs_feature_three)
//            tv_features4.text = getString(R.string.subs_feature_four)
//            tv_continue_ad.text = getString(R.string.continue_to_trial)
//            tv_cancel_time.text = getString(R.string.cancel_renewal)
////            btn_subscribe.text = getString(R.string.subscribe_monthly)
//
//            if (Sub_Yearly) {
//                btn_subscribe.text = getString(R.string.subscribe_yearly)
//            } else {
//                btn_subscribe.text = getString(R.string.subscribe_monthly)
//            }
//        } else {
//            setContentView(R.layout.activity_premium)
//            tv_onboarding.text = getString(R.string.gps_navigation_route_amp_map)
//            tv_premium_access.text = getString(R.string.premium_access)
//            tv_features.text = getString(R.string.content_feature)
//            tv_continue_ad.text = getString(R.string.continue_with_ads)
//            tv_continue_ad.paintFlags = tv_continue_ad.paintFlags or Paint.UNDERLINE_TEXT_FLAG
//            btn_subscribe.text = getString(R.string.continue_to_trial)
//            tv_free_try.movementMethod = ScrollingMovementMethod()
//        }
//
//        tv_terms.text = getString(R.string.terms_and_conditions)
//        tv_privacy.text = getString(R.string.privacy_policies)
//
//        appBillingClient = AppBillingClient()
//        setClickListener()
//
//        if (isOnline(this)) {
//            getSubscriptionDetails()
//        } else {
//            if (!fromWhere) {
//                progress_loader.visibility = View.GONE
//            }
//            Toast.makeText(
//                applicationContext,
//                getString(R.string.internet_not_connected),
//                Toast.LENGTH_SHORT
//            )
//                .show()
//        }
//
//        Timber.tag("Free_trial_onCreate").i("Free trial onCreate")
//    }
//
//    private fun getDeviceDensityString(context: Context): String? {
//        var result = "";
//        when (context.resources.displayMetrics.densityDpi) {
//            DisplayMetrics.DENSITY_LOW -> return "ldpi"
//            DisplayMetrics.DENSITY_MEDIUM -> return "mdpi"
//            DisplayMetrics.DENSITY_TV, DisplayMetrics.DENSITY_HIGH -> return "hdpi"
//            DisplayMetrics.DENSITY_260, DisplayMetrics.DENSITY_280, DisplayMetrics.DENSITY_300, DisplayMetrics.DENSITY_XHIGH -> return "xhdpi"
//            DisplayMetrics.DENSITY_340, DisplayMetrics.DENSITY_360, DisplayMetrics.DENSITY_400, DisplayMetrics.DENSITY_420, DisplayMetrics.DENSITY_440, DisplayMetrics.DENSITY_XXHIGH -> return "xxhdpi"
//            DisplayMetrics.DENSITY_560, DisplayMetrics.DENSITY_XXXHIGH -> return "xxxhdpi"
//        }
//        return result
//    }
//
//    private fun setClickListener() {
//        btn_subscribe.setOnClickListener {
//            if (isOnline(this)) {
//                Timber.tag("FreeTrial_upgrade_click").i("Free trial upgrade button clicked")
//                subscriptionType = SubscriptionType.MONTHLY
//                when (subscriptionType) {
//                    SubscriptionType.MONTHLY -> {
//                        monthlySkuDetail?.let {
//                            appBillingClient?.purchaseSkuItem(this, it)
//                        }
//                    }
//
//                    SubscriptionType.SIX_MONTH -> {
//                        //  bp?.subscribe(this@SubscriptionActivity, skuSixMonth)
//                    }
//
//                    SubscriptionType.YEARLY -> {
//                        yearlySkuDetail?.let {
//                            //appBillingClient?.purchaseSkuItem(this, it)
//                        }
//                    }
//
//                    else -> {}
//                }
//            }
//        }
//
//        tv_continue_ad.setOnClickListener {
//            if (fromWhere) {
//                if (isOnline(this)) {
//                    subscriptionType = SubscriptionType.WEEKLY
//                    when (subscriptionType) {
//                        SubscriptionType.WEEKLY -> {
//                            weeklySkuDetail?.let {
//                                appBillingClient?.purchaseSkuItem(this, it)
//                            }
//                        }
//
//                        else -> {}
//                    }
//                }
//            } else {
//                onBackPressed()
//            }
//        }
//        if (fromWhere) {
//            button_close.setOnClickListener {
//                onBackPressed()
//            }
//
//            tv_free_try.setOnClickListener {
//                if (isOnline(this)) {
//                    subscriptionType = SubscriptionType.WEEKLY
//                    when (subscriptionType) {
//                        SubscriptionType.WEEKLY -> {
//                            weeklySkuDetail?.let {
//                                appBillingClient?.purchaseSkuItem(this, it)
//                            }
//                        }
//
//                        else -> {}
//                    }
//                }
//            }
//        }
//
//        tv_privacy.setOnClickListener {
//            UrlUtils.openUrl(
//                this@PremiumActivity,
//                "https://zedlatino.info/privacy-policy-apps.html"
//            )
//        }
//
//        tv_terms.setOnClickListener {
//            UrlUtils.openUrl(
//                this@PremiumActivity,
//                "https://zedlatino.info/TermsOfUse.html"
//            )
//        }
//    }
//
//    private fun getWeeklySubscriptionDetails() {
//        if (fromWhere) {
//            appBillingClient?.connect(
//                this,
//                SubscriptionSku.WEEKLY.sku,
//                object : ConnectResponse {
//                    override fun disconnected() {
//
//                    }
//
//                    override fun billingUnavailable() {
//
//                    }
//
//                    override fun developerError() {
//
//                    }
//
//                    override fun error() {
//
//                    }
//
//                    override fun featureNotSupported() {
//
//                    }
//
//                    override fun itemUnavailable() {
//
//                    }
//
//                    override fun ok(subscriptionItems: List<SubscriptionItem>) {
//                        subscriptionItems.forEach {
//                            weeklySkuDetail = it
//                            it.subscribedItem?.purchaseTime?.let {
//                                setUserAsPremium(it)
//                            }
//                        }
//
//                        weeklySkuDetail?.let {
//                            if (!fromWhere) {
//
//                            } else {
//                                val string = SpannableString(
//                                    getString(R.string.enjoy_a) + " " + getString(R.string.three_day_trial) + " " + it.pricingPhase?.formattedPrice.orEmpty() + " " + getString(
//                                        R.string.per_week
//                                    )
//                                )
//
//                                string.setSpan(
//                                    object : TypefaceSpan(null) {
//                                        override fun updateDrawState(ds: TextPaint) {
//                                            ds.typeface = Typeface.create(
//                                                ResourcesCompat.getFont(
//                                                    this@PremiumActivity,
//                                                    R.font.arial_normal
//                                                ),
//                                                Typeface.NORMAL
//                                            ) // To change according to your need
//                                        }
//                                    },
//                                    0,
//                                    getString(R.string.enjoy_a).length,
//                                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//                                )
//
//                                Thread(Runnable {
//                                    runOnUiThread {
//                                        try {
//                                            tv_free_try.text = string
//                                        } catch (e: java.lang.Exception) {
//                                            e.printStackTrace()
//                                        }
//                                    }
//                                }).start()
//                            }
//                        }
//                    }
//
//                    override fun serviceDisconnected() {
//                        //  TODO("Not yet implemented")
//                    }
//
//                    override fun serviceUnavailable() {
//                        // TODO("Not yet implemented")
//                    }
//
//                },
//                object : PurchaseResponse {
//                    override fun isAlreadyOwned() {
//                        // TODO("Not yet implemented")
//                    }
//
//                    override fun userCancelled(productItem: ProductItem) {
//                        TinyDB.getInstance(this@PremiumActivity)
//                            .putBoolean(TinyDB.IS_PREMIUM, false)
//                    }
//
//                    override fun ok(productItem: ProductItem) {
//                        subscriptionViewModel.setUserSubscribed(true)
//                        subscriptionViewModel.setAutoAdsRemoved(true)
//                        TinyDB.getInstance(this@PremiumActivity)
//                            .putBoolean(TinyDB.IS_PREMIUM, true)
//                        finish()
//                    }
//
//                    override fun error(error: String) {
//                        // TODO("Not yet implemented")
//                    }
//
//                })
//        }
//    }
//
//    var quimeraIsCalled: Boolean = false
//
//    private fun getSubscriptionDetails() {
//        appBillingClient?.connect(this, Constants.SUBSCRIPTION1, object : ConnectResponse {
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
//            @SuppressLint("SetTextI18n")
//            override fun ok(
//                /*    oneTimePurchaseItems: List<OneTimePurchaseItem>,*/
//                subscriptionItems: List<SubscriptionItem>
//            ) {
//                Timber.e("InappBilling connection ok do other .")
//                Timber.d(subscriptionItems.toString())
//                subscriptionItems.forEach {
//                    monthlySkuDetail = it
//                    it.subscribedItem?.purchaseTime?.let {
//                        setUserAsPremium(it)
//                    }
//
//                }
//                monthlySkuDetail?.let {
//                    //tv_free_try?.text = getString(R.string.enjoy_trial) + " " + it.price + " " + getString(R.string.per_month)
//                    getWeeklySubscriptionDetails()
//                    if (!fromWhere) {
//                        var string: SpannableString
//
//                        if (Sub_Yearly) {
//                            string = SpannableString(
//                                getString(R.string.enjoy_a) + " " + getString(R.string.three_day_trial) + " " + it.pricingPhase?.formattedPrice.orEmpty() + " " + getString(
//                                    R.string.per_year
//                                ) + " \n" + getString(R.string.cancel_anytime)
//                            )
//                        } else {
//                            string = SpannableString(
//                                getString(R.string.enjoy_a) + " " + getString(R.string.three_day_trial) + " " + it.pricingPhase?.formattedPrice.orEmpty() + " " + getString(
//                                    R.string.per_month
//                                ) + " \n" + getString(R.string.cancel_anytime)
//                            )
//                        }
//
//                        string.setSpan(
//                            object : TypefaceSpan(null) {
//                                override fun updateDrawState(ds: TextPaint) {
//                                    ds.typeface = Typeface.create(
//                                        ResourcesCompat.getFont(
//                                            this@PremiumActivity,
//                                            R.font.arial_normal
//                                        ),
//                                        Typeface.NORMAL
//                                    ) // To change according to your need
//                                }
//                            },
//                            0,
//                            getString(R.string.enjoy_a).length,
//                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//                        )
//
//                        string.setSpan(
//                            object : TypefaceSpan(null) {
//                                override fun updateDrawState(ds: TextPaint) {
//                                    ds.typeface = Typeface.create(
//                                        ResourcesCompat.getFont(
//                                            this@PremiumActivity,
//                                            R.font.arial_normal
//                                        ),
//                                        Typeface.NORMAL
//                                    ) // To change according to your need
//                                }
//                            },
//                            string.indexOf(getString(R.string.cancel_anytime)),
//                            string.indexOf(getString(R.string.cancel_anytime)) + getString(R.string.cancel_anytime).length,
//                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
//                        )
//
//                        runOnUiThread {
//                            try {
//                                if (!fromWhere) {
//                                    progress_loader.visibility = View.GONE
//                                }
//                                tv_free_try.text = string
//                            } catch (e: java.lang.Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//                    } else {
//                        Thread(Runnable {
//                            runOnUiThread {
//                                try {
//                                    //progress_loader.visibility = View.GONE
//                                    //tv_free_try.text = string
////                                    btn_subscribe.text =
////                                        getString(R.string.subscribe_monthly) + " - " + it.pricingPhase?.formattedPrice.orEmpty()
//
//                                    if (Sub_Yearly) {
//                                        btn_subscribe.text =
//                                            getString(R.string.subscribe_yearly) + " - " + it.pricingPhase?.formattedPrice.orEmpty()
//                                    } else {
//                                        btn_subscribe.text =
//                                            getString(R.string.subscribe_monthly) + " - " + it.pricingPhase?.formattedPrice.orEmpty()
//                                    }
//
//                                } catch (e: java.lang.Exception) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }).start()
//                    }
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
//                TinyDB.getInstance(this@PremiumActivity).putBoolean(TinyDB.IS_PREMIUM, false)
//
//            }
//
//            override fun ok(productItem: ProductItem) {
//                subscriptionViewModel.setUserSubscribed(true)
//                subscriptionViewModel.setAutoAdsRemoved(true)
//                TinyDB.getInstance(this@PremiumActivity).putBoolean(TinyDB.IS_PREMIUM, true)
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
//        this@PremiumActivity.finish()
//    }
//
//}