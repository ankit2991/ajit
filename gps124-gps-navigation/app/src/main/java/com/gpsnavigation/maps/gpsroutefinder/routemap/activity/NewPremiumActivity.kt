package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.adapty.Adapty
import com.adapty.utils.AdaptyResult
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.example.routesmap.viewModels.SubscriptionViewModel
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.SUBSCRIBE_BILLING
import com.gpsnavigation.maps.gpsroutefinder.routemap.ads.BaseActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants
import com.gpsnavigation.maps.gpsroutefinder.routemap.data.constants.Constants.SUBSCRIPTION2
import com.gpsnavigation.maps.gpsroutefinder.routemap.databinding.ActivityPremiumNewBinding
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.AppBillingClient
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.ProductItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces.ConnectResponse
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.interfaces.PurchaseResponse
import com.gpsnavigation.maps.gpsroutefinder.routemap.iab.subscription.SubscriptionItem
import com.gpsnavigation.maps.gpsroutefinder.routemap.quimeraManager.QuimeraInit
import com.gpsnavigation.maps.gpsroutefinder.routemap.quimeraManager.SubscriptionItemsQmr
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class NewPremiumActivity : BaseActivity() {

    val subscriptionViewModel: SubscriptionViewModel by viewModel()
    var subscriptionType = SubscriptionType.MONTHLY
    private var fromWhere: Boolean = true
    private var selected: Int = 0
    private var isButtonClickable = true

    enum class SubscriptionType {
        WEEKLY,
        MONTHLY,
        SIX_MONTH,
        YEARLY
    }

//    private var appBillingClient: AppBillingClient? = null
    var monthlySkuDetail: SubscriptionItem? = null
    var weeklySkuDetail: SubscriptionItem? = null
    var yearlySkuDetail: SubscriptionItem? = null
    lateinit var binding:ActivityPremiumNewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityPremiumNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_premium_new)
        fromWhere = intent.getBooleanExtra("from", true)
        val getSubscriptionData = getRemoteconfig()!!.getBoolean("GPS124_01_year_sub_flag")
        val getSubscription1 = getRemoteconfig()!!.getString("GPS124_prices_value1")
        val getSubscription2 = getRemoteconfig()!!.getString("GPS124_prices_value2")
        Constants.Sub_Yearly = getSubscriptionData
        Constants.SUBSCRIPTION1 = getSubscription1
        Constants.SUBSCRIPTION2 = getSubscription2
        Log.e("subscription_Splash", getSubscription1)
        Log.e("subscription_Splash", getSubscription2)

        playVideo()
//        val text =
//            "<font color=#0094FF>Subscribe</font> <font color=#000000>to Unlock</font> <font color=#0094FF>premium</font> "
//        tvDes.text = Html.fromHtml(text)
//        appBillingClient = AppBillingClient()
        setClickListener()
        if (Constants.weeklyPrice.isNotEmpty() && Constants.yearlyPrice.isNotEmpty()) {
            binding.tvContinueAd.text = Constants.weeklyPrice
            binding.btnSubscribe.text = Constants.yearlyPrice
        }
        if (isOnline(this)) {
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
//                getSubscriptionDetails()
            }, 400)

        } else {

            Toast.makeText(
                applicationContext,
                getString(R.string.internet_not_connected),
                Toast.LENGTH_SHORT
            )
                .show()
        }


        Timber.tag("Free_trial_onCreate").i("Free trial onCreate")



    }

    private lateinit var player: SimpleExoPlayer

    fun playVideo() {
        val path =
            "android.resource://" + packageName.toString() + "/" + R.raw.video6
        player = SimpleExoPlayer.Builder(this).build()
        binding.videoView.useController = false
        binding.videoView.player = player
        binding.videoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        val mediaItem = MediaItem.fromUri(path)
//        videoView.scaleX = scaleFactor
//        videoView.scaleY = scaleFactor
//        mediaItem.playbackProperties?.allowToSeekToStart = false
        player.setMediaItem(mediaItem)
        player.repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL

        player.prepare()
        player.playWhenReady = true
    }

    //    private var scaleGestureDetector: ScaleGestureDetector? = null
//    private var scaleFactor = 1.2f
    override fun onResume() {
        super.onResume()



        if (player != null) {
            player.play()
        }

        //the below code is for VideoView
//        videoView.setVideoURI(Uri.parse(path))
//        videoView.requestFocus();
//        videoView.start()
//        videoView.setOnPreparedListener { mp -> mp.isLooping = true }
    }

    override fun onPause() {
        super.onPause()
        if (player != null) {
            player.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player.release()
        }
    }

    private fun getDeviceDensityString(context: Context): String {
        var result = "";
        when (context.resources.displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> return "ldpi"
            DisplayMetrics.DENSITY_MEDIUM -> return "mdpi"
            DisplayMetrics.DENSITY_TV, DisplayMetrics.DENSITY_HIGH -> return "hdpi"
            DisplayMetrics.DENSITY_260, DisplayMetrics.DENSITY_280, DisplayMetrics.DENSITY_300, DisplayMetrics.DENSITY_XHIGH -> return "xhdpi"
            DisplayMetrics.DENSITY_340, DisplayMetrics.DENSITY_360, DisplayMetrics.DENSITY_400, DisplayMetrics.DENSITY_420, DisplayMetrics.DENSITY_440, DisplayMetrics.DENSITY_XXHIGH -> return "xxhdpi"
            DisplayMetrics.DENSITY_560, DisplayMetrics.DENSITY_XXXHIGH -> return "xxxhdpi"
        }
        return result
    }

    private fun setClickListener() {

        binding.linear2.setOnClickListener {
            selected = 1
            binding.linear2.background = getDrawable(R.drawable.bg_blue_stroke_rounded_10sdp)
            binding.linear1.background = getDrawable(R.drawable.bg_black_stroke_rounded_10sdp)
            binding.selectWeekly.setImageDrawable(getDrawable(R.drawable.ic_select_radio))
            binding.selectYearly.setImageDrawable(getDrawable(R.drawable.ic_unselect_radio))
        }
        binding.linear1.setOnClickListener {
            selected = 0
            binding.linear1.background = getDrawable(R.drawable.bg_blue_stroke_rounded_10sdp)
            binding.selectYearly.setImageDrawable(getDrawable(R.drawable.ic_select_radio))
            binding.linear2.background = getDrawable(R.drawable.bg_black_stroke_rounded_10sdp)
            binding.selectWeekly.setImageDrawable(getDrawable(R.drawable.ic_unselect_radio))
        }
        binding.tvRestore.setOnClickListener {
//            getSubscriptionDetails()
        }
        binding.btnContinue.setOnClickListener {
            binding.btnContinue.isEnabled = false

            if (isButtonClickable) {
                // Disable the button
                isButtonClickable = false
                binding.btnContinue.isEnabled = false

                // Enable the button after 2 seconds
                Handler().postDelayed({
                    isButtonClickable = true
                    binding.btnContinue.isEnabled = true
                }, 3000) // 2000 milliseconds = 2 seconds
            }

            isOkCalledOnce = true
            isCalledOnce = true
            isErrorCalledOnce = true
            subscriptionViewModel.setUserSubscribed(true)
            subscriptionViewModel.setAutoAdsRemoved(true)
//            TinyDB.getInstance(this@NewPremiumActivity)
//                .putBoolean(TinyDB.IS_PREMIUM, true)
            if (selected == 0) {
                if (isOnline(this)) {
                    Timber.tag("FreeTrial_upgrade_click").i("Free trial upgrade button clicked")
                    subscriptionType = SubscriptionType.MONTHLY
                    when (subscriptionType) {
                        SubscriptionType.MONTHLY -> {
                            monthlySkuDetail?.let {
//                                appBillingClient?.purchaseSkuItem(this, it)
                            }
                        }
//                        SubscriptionType.SIX_MONTH -> {
//                            //  bp?.subscribe(this@SubscriptionActivity, skuSixMonth)
//                        }
//                        SubscriptionType.YEARLY -> {
//                            yearlySkuDetail?.let {
//                                //appBillingClient?.purchaseSkuItem(this, it)
//                            }
//                        }
                        else -> {}
                    }
                }
            } else {
                if (isOnline(this)) {
                    subscriptionType = SubscriptionType.WEEKLY
                    when (subscriptionType) {
                        SubscriptionType.WEEKLY -> {
                            weeklySkuDetail?.let {
//                                appBillingClient?.purchaseSkuItem(this, it)
                            }
                        }

                        else -> {}
                    }
                }
            }
        }

        binding.buttonClose.setOnClickListener {
            onBackPressed()
        }


        binding.tvPrivacy.setOnClickListener {
            UrlUtils.openUrl(
                this@NewPremiumActivity,
                "https://zedlatino.info/privacy-policy-apps.html"
            )
        }

        binding.tvTerms.setOnClickListener {
            UrlUtils.openUrl(
                this@NewPremiumActivity,
                "https://zedlatino.info/TermsOfUse.html"
            )
        }
    }

//    private fun getWeeklySubscriptionDetails() {
//        appBillingClient?.connect(
//            this,
//            SUBSCRIPTION2,
//            object : ConnectResponse {
//                override fun disconnected() {
//
//                }
//
//                override fun billingUnavailable() {
//
//                }
//
//                override fun developerError() {
//
//                }
//
//                override fun error() {
//
//                }
//
//                override fun featureNotSupported() {
//
//                }
//
//                override fun itemUnavailable() {
//
//                }
//
//                override fun ok(subscriptionItems: List<SubscriptionItem>) {
////                        Log.e("Premium_Data_weekly=> ",Gson().toJson(subscriptionItems))
//                    subscriptionItems.forEach {
//                        weeklySkuDetail = it
//                        it.subscribedItem?.purchaseTime?.let {
//                            setUserAsPremium(it)
//                        }
//                    }
//
//                    weeklySkuDetail?.let {
//                        Thread(Runnable {
//                            runOnUiThread {
//                                try {
//                                    var tvContinueText = ""
//                                    var btnSubscribeText = ""
////                                        Toast.makeText(this@NewPremiumActivity,"Restored Successfully",Toast.LENGTH_SHORT).show()
//                                    if (weeklySkuDetail?.productDetails?.productId?.contains("weekly3") == true &&
//                                        (monthlySkuDetail?.productDetails?.productId?.contains("yearly_subscription3") == true
//                                                || (monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "year_ly"
//                                        ) == true)
//                                                || (monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "yearly"
//                                        ) == true))
//                                    ) {
//                                        val price1 =
//                                            ((monthlySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 100)
//                                        val price2 =
//                                            ((weeklySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 48)
//                                        val discount = price1 / price2
//                                        binding.tvDiscount.text = "$discount% discount!"
//                                        tvContinueText =
//                                            it.pricingPhase?.formattedPrice.orEmpty() + getString(R.string.per_week)
//                                        btnSubscribeText =
//                                            monthlySkuDetail?.pricingPhase?.formattedPrice.orEmpty() + getString(
//                                                R.string.per_year
//                                            )
//
//                                    } else if (weeklySkuDetail?.productDetails?.productId?.contains(
//                                            "weekly3"
//                                        ) == true && ((monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "monthly"
//                                        ) == true) ||
//                                                (monthlySkuDetail?.productDetails?.productId?.contains(
//                                                    "month_ly"
//                                                ) == true))
//                                    ) {
//                                        val price1 =
//                                            ((monthlySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 100)
//                                        val price2 =
//                                            ((weeklySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 4)
//                                        val discount = price1 / price2
//                                        binding.tvDiscount.text = "$discount% discount!"
//                                        tvContinueText =
//                                            it.pricingPhase?.formattedPrice.orEmpty() + getString(R.string.per_week)
//                                        btnSubscribeText =
//                                            monthlySkuDetail?.pricingPhase?.formattedPrice.orEmpty() + getString(
//                                                R.string.per_month
//                                            )
//
//
//                                    } else if (((weeklySkuDetail?.productDetails?.productId?.contains(
//                                            "monthly"
//                                        ) == true) ||
//                                                (weeklySkuDetail?.productDetails?.productId?.contains(
//                                                    "month_ly"
//                                                ) == true)) && (monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "yearly_subscription3"
//                                        ) == true
//                                                || (monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "year_ly"
//                                        ) == true)
//                                                || (monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "yearly"
//                                        ) == true))
//                                    ) {
//                                        val price1 =
//                                            ((monthlySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 100)
//                                        val price2 =
//                                            ((weeklySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 12)
//                                        val discount = price1 / price2
//                                        binding.tvDiscount.text = "$discount% discount!"
//                                        tvContinueText =
//                                            it.pricingPhase?.formattedPrice.orEmpty() + getString(R.string.per_month)
//                                        btnSubscribeText =
//                                            monthlySkuDetail?.pricingPhase?.formattedPrice.orEmpty() + getString(
//                                                R.string.per_year
//                                            )
//
//
//                                    } else if (weeklySkuDetail?.productDetails?.productId?.contains(
//                                            "six_months"
//                                        ) == true && (monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "yearly_subscription3"
//                                        ) == true
//                                                || (monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "year_ly"
//                                        ) == true)
//                                                || (monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "yearly"
//                                        ) == true))
//                                    ) {
//                                        val price1 =
//                                            ((monthlySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 100)
//                                        val price2 =
//                                            ((weeklySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 2)
//                                        val discount = price1 / price2
//                                        binding.tvDiscount.text = "$discount% discount!"
//                                        tvContinueText =
//                                            it.pricingPhase?.formattedPrice.orEmpty() + "/" + getString(
//                                                R.string.six_month
//                                            )
//                                        btnSubscribeText =
//                                            monthlySkuDetail?.pricingPhase?.formattedPrice.orEmpty() + getString(
//                                                R.string.per_year
//                                            )
//
//
//                                    } else if (((weeklySkuDetail?.productDetails?.productId?.contains(
//                                            "monthly"
//                                        ) == true) ||
//                                                (weeklySkuDetail?.productDetails?.productId?.contains(
//                                                    "month_ly"
//                                                ) == true)) && monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "six_months"
//                                        ) == true
//                                    ) {
//                                        val price1 =
//                                            ((monthlySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 100)
//                                        val price2 =
//                                            ((weeklySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 6)
//                                        val discount = price1 / price2
//                                        binding.tvDiscount.text = "$discount% discount!"
//                                        tvContinueText =
//                                            it.pricingPhase?.formattedPrice.orEmpty() + getString(R.string.per_month)
//                                        btnSubscribeText =
//                                            monthlySkuDetail?.pricingPhase?.formattedPrice.orEmpty() + "/" + getString(
//                                                R.string.six_month
//                                            )
//
//
//                                    } else if (weeklySkuDetail?.productDetails?.productId?.contains(
//                                            "weekly3"
//                                        ) == true && monthlySkuDetail?.productDetails?.productId?.contains(
//                                            "six_months"
//                                        ) == true
//                                    ) {
//                                        val price1 =
//                                            ((monthlySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 100)
//                                        val price2 =
//                                            ((weeklySkuDetail?.pricingPhase?.priceAmountMicros
//                                                ?: 0) * 24)
//                                        val discount = price1 / price2
//                                        binding.tvDiscount.text = "$discount% discount!"
//
//                                        tvContinueText =
//                                            it.pricingPhase?.formattedPrice.orEmpty() + getString(R.string.per_week)
//                                        btnSubscribeText =
//                                            monthlySkuDetail?.pricingPhase?.formattedPrice.orEmpty() + "/" + getString(
//                                                R.string.six_month
//                                            )
//                                    }
//                                    var tvFirstTrial = ""
//                                    var tvSecondTrial = ""
//                                    if (it.productDetails.subscriptionOfferDetails?.get(0)?.offerId?.contains(
//                                            "free"
//                                        ) == true
//                                    ) {
//                                        if (it.productDetails.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(
//                                                0
//                                            )?.billingPeriod?.equals("P3D") == true
//                                        ) {
//                                            tvFirstTrial = getString(R.string.three_day_trial) + "/"
//                                        }
//                                    }
//                                    if (monthlySkuDetail?.productDetails?.subscriptionOfferDetails?.get(
//                                            0
//                                        )?.offerId?.contains("free") == true
//                                    ) {
//                                        if (monthlySkuDetail?.productDetails?.subscriptionOfferDetails?.get(
//                                                0
//                                            )?.pricingPhases?.pricingPhaseList?.get(0)?.billingPeriod?.equals(
//                                                "P3D"
//                                            ) == true
//                                        ) {
//                                            tvSecondTrial =
//                                                getString(R.string.three_day_trial) + "/"
//                                        }
//                                    }
//                                    binding.btnSubscribe.text = tvSecondTrial + btnSubscribeText
//                                    binding.tvContinueAd.text = tvFirstTrial + tvContinueText
//                                    TinyDB.getInstance(this@NewPremiumActivity).putString(Constants.WEEKLY_VALUE,  binding.tvContinueAd.text.toString())
//                                    TinyDB.getInstance(this@NewPremiumActivity).putString(Constants.YEARLY_VALUE,  binding.btnSubscribe.text.toString())
//                                    if (tvFirstTrial.isNotEmpty() || tvSecondTrial.isNotEmpty()) {
//                                        binding.btnContinue.text = getString(R.string.start_trial)
//                                    }
//                                } catch (e: java.lang.Exception) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }).start()
//                    }
//                }
//
//                override fun serviceDisconnected() {
//                    Timber.e("InappBilling service disconnected.")
//
//                }
//
//                override fun serviceUnavailable() {
//                    Timber.e("InappBilling service unavailable.")
//
//                }
//
//            },
//            object : PurchaseResponse {
//                override fun isAlreadyOwned() {
//
//                }
//
//                override fun userCancelled(productItem: ProductItem) {
//
//
//                    TinyDB.getInstance(this@NewPremiumActivity)
//                        .putBoolean(TinyDB.IS_PREMIUM, false)
//
//                }
//
//                override fun ok(productItem: ProductItem) {
//
//                    subscriptionViewModel.setUserSubscribed(true)
//                    subscriptionViewModel.setAutoAdsRemoved(true)
//                    TinyDB.getInstance(this@NewPremiumActivity)
//                        .putBoolean(TinyDB.IS_PREMIUM, true)
//
//                    finish()
//                }
//
//                override fun error(error: String) {
//
//
////                       showErrorDialog()
//                }
//
//            })
//
//    }

    private fun showErrorDialog() {
        val dialogBuilder = AlertDialog.Builder(this@NewPremiumActivity)
        dialogBuilder.setMessage(
            "There is a problem with your subscription. Click CONTINUE " +
                    "to go to the Google Play subscription settings to fix your payment method."
        )
            .setCancelable(true)
            .setPositiveButton(
                "Continue",
                DialogInterface.OnClickListener { dialog, which ->
                    val i = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/account/subscriptions")
                    )
                    startActivity(i)
                }
            )
            .setNegativeButton("Cancel") { dialog, id ->
                dialog.cancel()
            }


        val alert = dialogBuilder.create()
        alert.show()
    }


//    private fun getSubscriptionDetails() {
//        appBillingClient?.connect(this, Constants.SUBSCRIPTION1, object : ConnectResponse {
//            override fun disconnected() {
//                Timber.e("InappBilling connection disconnected.")
//
//            }
//
//            override fun billingUnavailable() {
//                Timber.e("InappBilling billing unavailable.")
//
//            }
//
//            override fun developerError() {
//                Timber.e("InappBilling developer error.")
//
//            }
//
//            override fun error() {
//                Timber.e("InappBilling simple error.")
//
//            }
//
//            override fun featureNotSupported() {
//                Timber.e("InappBilling feature not available.")
//
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
////                Log.e("Premium_Data_monthly=> ", Gson().toJson(subscriptionItems))
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
//
//                    Thread(Runnable {
//                        runOnUiThread {
//                            try {
//                                //progress_loader.visibility = View.GONE
//                                //tv_free_try.text = string
////                                    btn_subscribe.text =
////                                        getString(R.string.subscribe_monthly) + " - " + it.pricingPhase?.formattedPrice.orEmpty()
//
////                                if (Sub_Yearly) {
////                                    btn_subscribe.text =
////                                        it.pricingPhase?.formattedPrice.orEmpty() + getString(
////                                            R.string.per_year
////                                        )
////                                } else {
////                                    btn_subscribe.text =
////                                        it.pricingPhase?.formattedPrice.orEmpty() + getString(
////                                            R.string.per_month
////                                        )
////                                }
//
//                            } catch (e: java.lang.Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }).start()
//                }
//
//            }
//
//            override fun serviceDisconnected() {
//                Timber.e("InappBilling service disconnected.")
//            }
//
//            override fun serviceUnavailable() {
//                Timber.e("InappBilling service unavailable.")
//
//            }
//        }, object : PurchaseResponse {
//            override fun isAlreadyOwned() {
//                QuimeraInit.itemAlreadyOwned(applicationContext)
//            }
//
//            override fun userCancelled(productItem: ProductItem) {
//                if (isCalledOnce && productItem != null) {
//                    isCalledOnce = false
//                    Log.e("billingv6", ">>on billing cancel")
//                    QuimeraInit.userCancelBilling(applicationContext)
//                }
//
//                TinyDB.getInstance(this@NewPremiumActivity).putBoolean(TinyDB.IS_PREMIUM, false)
//
//            }
//
//            override fun ok(productItem: ProductItem) {
//                if (isOkCalledOnce) {
//                    isOkCalledOnce = false
//                    Log.e("billingv6", ">>on billing ok")
//                    QuimeraInit.userPurchased(applicationContext, productItem)
//
//                    var price3 = SubscriptionItemsQmr(productItem.productDetails).pricingPhase?.formattedPrice
//                    var  productId= productItem.productDetails.productId
////                    Log.e("purchasePrice",">"+price3+">id"+productId)
//                    /* Adjust Event */
//                    ////////////////////////////////////////////////////////////////////////////////////
//                    val adjustEvent = AdjustEvent("5xko3p")
//                    adjustEvent.addCallbackParameter(SUBSCRIBE_BILLING, "SubId_${productId}_Price_${price3}_Date_${CurrentDateUtil.getCurrentDate2()}_PlacementScreen_PremiumScreen")
//                    adjustEvent.setCallbackId(SUBSCRIBE_BILLING)
//                    Adjust.trackEvent(adjustEvent)
//                    Log.e("AdjustEvent","The event id:5xko3p  name:$SUBSCRIBE_BILLING SubId:$productId Price:$price3 Date:${CurrentDateUtil.getCurrentDate2()} PlacementScreen:PremiumScreen has been sent to Adjust")
//
//                    ////////////////////////////////////////////////////////////////////////////////////
//
//
//                    /* Facebook Event*/
//                    ///////////////////////////////////////////////////////////////////////////////////
//                    FirebaseAnalytics.getInstance(this@NewPremiumActivity).logEvent(
//                        SUBSCRIBE_BILLING, null)
////                    val logger = AppEventsLogger.newLogger(this@NewPremiumActivity)
////                    logger.logEvent(SUBSCRIBE_BILLING)
////                FacebookSdk.setIsDebugEnabled(true);  // TODO: disable debug logs before release
////                FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);  // TODO: disable debug logs before release
////                FacebookSdk.setAdvertiserIDCollectionEnabled(true)
//                    //////////////////////////////////////////////////////////////////////////////////
//                }
//
//                subscriptionViewModel.setUserSubscribed(true)
//                subscriptionViewModel.setAutoAdsRemoved(true)
//                TinyDB.getInstance(this@NewPremiumActivity).putBoolean(TinyDB.IS_PREMIUM, true)
//                finish()
//            }
//
//            override fun error(error: String) {
//                if (isErrorCalledOnce) {
//                    isErrorCalledOnce = false
//                    if (error.equals("SERVICE_TIMEOUT")) {
//
//                    } else if (error.equals("FEATURE_NOT_SUPPORTED")) {
//                        QuimeraInit.featureNotSupported(applicationContext)
//                    } else if (error.equals("SERVICE_DISCONNECTED")) {
//                        QuimeraInit.serviceDisconnected(applicationContext)
//
//                    } else if (error.equals("SERVICE_UNAVAILABLE")) {
//                        QuimeraInit.serviceUnavailable(applicationContext)
//
//                    } else if (error.equals("BILLING_UNAVAILABLE")) {
//                        QuimeraInit.billingUnavailable(applicationContext)
//
//                    } else if (error.equals("ITEM_UNAVAILABLE")) {
//                        QuimeraInit.itemUnavailable(applicationContext)
//
//                    } else if (error.equals("DEVELOPER_ERROR")) {
//                        QuimeraInit.developerError(applicationContext)
//
//                    } else if (error.equals("ERROR")) {
//                        QuimeraInit.billingError(applicationContext)
//
//                    } else if (error.equals("ITEM_NOT_OWNED")) {
//                        QuimeraInit.itemNotOwned(applicationContext)
//
//                    }
//                }
////                showErrorDialog()
//            }
//
//        })
//    }

    var patternPrice: Pattern = Pattern.compile("(\\d+[[\\.,\\s]\\d+]*)")


    fun extractPrice(price: String): Double {
        val matcher: Matcher = patternPrice.matcher(price)
        if (matcher.find()) {
            var match: String = matcher.group()

            // proceed post treatments to convert the String to a Double
            // delete space from price if any
            if (match.contains(" ")) match = match.replace(" ", "")
            if (match.contains(",")) {
                // price contains one comma and one dot: price format is something like that : 1,000,000.00
                // OR
                // there is more than one comma in the price (price is something like that 1,000,000)
                // so delete the , from price
                match = if (match.contains(".") || match.length - match.replace(
                        ",",
                        ""
                    ).length > 1
                ) match.replace(",", "") else match.replace(",", ".")
            }
            try {
                return match.toDouble()
            } catch (ex: Exception) {
                return 0.0
            }

        } else return 0.0
    }

    override fun onBackPressed() {
        finish()
    }

    private fun getCurrentLocale(context: Context): Locale? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
    }

    fun getPrice(price: String): Double {
        val locale: Locale? = getCurrentLocale(this@NewPremiumActivity)
        val currency = Currency.getInstance(locale).currencyCode
        val priceClean: Double =
            locale?.let { NumberFormat.getCurrencyInstance(it).parse(price)?.toDouble() } ?: 0.0

        return priceClean
    }

    private var isCalledOnce = true
    private var isOkCalledOnce = true
    private var isErrorCalledOnce = true
    fun assignValueAfter3Seconds() {
        val handler = Handler()

        handler.postDelayed({
            isCalledOnce = true
        }, 3000)
    }

    fun assignValuetoOk() {
        val handler = Handler()

        handler.postDelayed({
            isOkCalledOnce = true
        }, 3000)
    }

}