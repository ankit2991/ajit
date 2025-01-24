package com.messaging.textrasms.manager.feature.Activities

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.airbnb.lottie.LottieAnimationView
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.PricingPhase
import com.android.billingclient.api.Purchase
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.FirebaseHelper
import com.messaging.textrasms.manager.common.Purchases
import com.messaging.textrasms.manager.common.base.QkActivity
import com.messaging.textrasms.manager.common.iap.IapConnector
import com.messaging.textrasms.manager.common.iap.SubscriptionServiceListener
import com.messaging.textrasms.manager.common.util.RemoteConfigHelper
import com.messaging.textrasms.manager.feature.quimera.QuimeraInit
import com.messaging.textrasms.manager.feature.quimera.SubscriptionItem
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.SubscriptionType
import com.messaging.textrasms.manager.utils.Constants
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_purchase.tv_app_name_pre
import kotlinx.android.synthetic.main.activity_purchase.tv_app_name_suf
import java.lang.String.format


class PurchaseActivity : QkActivity() {

    companion object {

        var purchaseShown = false
            private set
    }

    //    private val SKUS_IN_APP = Arrays.asList("com.messaging.textrasms.manager_49.99")
    private var skus = listOf(
        "com.messaging.textrasms.manager_29.99",
        "com.messaging.textrasms.manager_9.99",
        "com.messaging.textrasms.manager_2.99"
    )
    private var skuConfigCurrent = ""
    private lateinit var tvDesc: TextView
    private lateinit var tvCancelAnyTime: TextView
    private lateinit var tvAppNameSuf: TextView
    private lateinit var tvTitlePre: TextView
    private lateinit var tvTitleSuf: TextView
    private lateinit var imagePreview: LottieAnimationView
    private var isButtonClickable = true
    private var iapConnector: IapConnector? = null
    private var productDetails: ProductDetails? = null

    private var status: PurchaseOption = PurchaseOption.Sub

    private lateinit var purchases: Purchases
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        Log.e("IAP_", "oncreate")
        purchaseShown = true
//        IntegrationHelper.validateIntegration(this@PurchaseActivity)
        setContentView(R.layout.activity_purchase)
        initSubFromRemoteConfig()
        findViewById<TextView>(R.id.tv_continue).setOnClickListener {
            checkClosedFlow()
            FirebaseHelper.sendCTACancelEvent()
        }
        findViewById<TextView>(R.id.tv_privacy).setOnClickListener { openUrl(getString(R.string.privacy_policy_link)) }
        findViewById<TextView>(R.id.tv_terms).setOnClickListener { openUrl(getString(R.string.terms_link)) }
        imagePreview = findViewById(R.id.image)
        if (!Preferences.getBoolean(this, Preferences.HAS_DISPLAYED_FIRST_PAYMENT_CARD)) {
            imagePreview.setAnimation(R.raw.anim_payment_card)
        } else {
            imagePreview.setAnimation(R.raw.anim_on_boarding_2)
        }
        tvDesc = findViewById(R.id.tv_desc)
        tvCancelAnyTime = findViewById(R.id.tv_cancel_anytime)
        tvAppNameSuf = findViewById(R.id.tv_app_name_suf)

        tvTitlePre = findViewById(R.id.txt_title_pre)
        tvTitleSuf = findViewById(R.id.txt_title_suf)

        tvTitlePre.text =
            resources.getString(R.string.payment_card_title).split("\\s+".toRegex())[0]
        tvTitleSuf.text =
            resources.getString(R.string.payment_card_title).split("\\s+".toRegex())[1]

        val paint = tvAppNameSuf.paint
        val width = paint.measureText(tvAppNameSuf.text.toString())
        val textShader: Shader = LinearGradient(
            0f, 0f, width, tvAppNameSuf.textSize, intArrayOf(
                Color.parseColor("#FF44CB"),
                Color.parseColor("#F01B1B"),
            ), null, Shader.TileMode.REPEAT
        )

        tvAppNameSuf.paint.setShader(textShader)
        findViewById<AppCompatButton>(R.id.tv_buy).setOnClickListener {

            if (Preferences.getBoolean(this@PurchaseActivity, Preferences.ADSREMOVED)) {
                hasSubscribedDialog()
                return@setOnClickListener
            }
            checkButtonState()
            FirebaseHelper.sendCTASubscribeEvent()
            if (iapConnector != null && productDetails != null) {
                QuimeraInit.singleCall = true
                QuimeraInit.sendSkuDetails(applicationContext, SubscriptionItem(productDetails!!))

                iapConnector!!.subscribe(this@PurchaseActivity, productDetails!!)
            }
        }

        FirebaseHelper.sendPaymentCardEvent()
        setTextSizeForLanguages()
    }

    private fun setTextSizeForLanguages() {
        val config = resources.configuration
        val Language = config.locale.language
        if(
            Language == "bg" ||
            Language == "ta" ||
            Language == "de" ||
            Language == "ka" ||
            Language == "ga" ||
            Language == "lt" ||
            Language == "sv" ||
            Language == "sl" ||
            Language == "vi" ||
            Language == "fil" ||
            Language == "uk"){
            tv_app_name_pre.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._7sdp).toFloat()
            tv_app_name_suf.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._7sdp).toFloat()
        }
        if (Language == "ru" ||
            Language == "be" ||
            Language == "pl"){
            tv_app_name_pre.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp).toFloat()
            tv_app_name_suf.textSize = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp).toFloat()
        }
    }

    private fun initSubFromRemoteConfig() {
        RemoteConfigHelper.initialize()
//        val subPlan: String = RemoteConfigHelper.get(
//            RemoteConfigHelper.GPS172_PRICE_PLANS,
//            SubscriptionType.yearly_sub.name
//        )


//        val subPlan: String = Constants.PRICE_PLAN
//        Log.e("pricePlane",">"+Constants.PRICE_PLAN)
//        val subConfig: String = SubscriptionType.fromNameToValue(subPlan)
//        Log.d("IAP_RemoteConfig", "${RemoteConfigHelper.initialized} -- $subPlan --- $subConfig")
//        skuConfigCurrent = subConfig
        skuConfigCurrent = "com.messaging.textrasms.manager_2.99"
        setupInApps()
    }

    private fun setupInApps() {
        //in app billing v5
        iapConnector = IapConnector.getInstance(this)
        iapConnector?.addSubscriptionListener(object : SubscriptionServiceListener {
            override fun onSubscriptionRestored(purchaseInfo: Purchase) {
                Log.d("IAP: ", "purchased")
                updatePurchase(true)
            }

            override fun onSubscriptionPurchased(purchaseInfo: Purchase) {
                Log.d("IAP: ", "purchased")
                updatePurchase(true)

                Handler(Looper.getMainLooper()).postDelayed({
                    restartApp()
                },700)

            }

            override fun onSubscriptionsExpired() {
                Log.d("IAP: ", "onProductsExpired")
                updatePurchase(false)
            }

            override fun onPricesUpdated(iapKeyPrices: Map<String, ProductDetails>) {
                productDetails = iapKeyPrices[skuConfigCurrent]
                if (productDetails != null) {
                    val pricingPhase: PricingPhase? =
                        iapConnector?.getOriginPricingPhase(productDetails!!)
                    if (pricingPhase != null) {
                        var priceString = ""
                        Log.e("IAP_check_1", skuConfigCurrent)
                        if (skuConfigCurrent.contains(SubscriptionType.weekly_sub.value)) {
                            priceString = format(
                                resources.getString(R.string.payment_card_price),
                                pricingPhase.formattedPrice,
                                resources.getString(R.string.payment_card_weekly)
                            )
                        } else if (skuConfigCurrent.contains(SubscriptionType.monthly_sub.value)) {
                            priceString = format(
                                resources.getString(R.string.payment_card_price),
                                pricingPhase.formattedPrice,
                                resources.getString(R.string.payment_card_monthly)
                            )
                        } else if (skuConfigCurrent.contains(SubscriptionType.yearly_sub.value)) {
                            priceString = format(
                                resources.getString(R.string.payment_card_price),
                                pricingPhase.formattedPrice,
                                resources.getString(R.string.payment_card_yearly)
                            )
                        }
                        tvDesc.text = getSpannedText(priceString)
                    }
                }
            }
        })

        iapConnector!!.restore()
    }


    private fun openUrl(url: String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e2: Exception) {
                Toast.makeText(this, "Unable to find market app", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun updatePurchase(isPurchased: Boolean) {
        Preferences.setBoolean(this@PurchaseActivity, Preferences.ADSREMOVED, isPurchased)

    }

   fun restartApp(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
    private fun showLogs(msg: String) {

        Log.e("billing ", "=============================\n")
        Log.e("billing ", msg)
        Log.e("billing ", "\n=============================")

    }

    private fun hasSubscribedDialog() {
        if (isDestroyed) return
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> checkClosedFlow()
                }
            }
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setMessage("You are subscribed!").setPositiveButton("OK", dialogClickListener)
            .show()
    }

    private fun getSpannedText(text: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(text)
        }
    }

    override fun onBackPressed() {
        checkClosedFlow()
        Constants.IS_FROM_ACTIVITY = true
        super.onBackPressed()
    }

    private fun checkClosedFlow() {
        if (isTaskRoot) {
            val intent = Intent(this@PurchaseActivity, MainActivity::class.java)
            startActivity(intent)
        }
        Preferences.setBoolean(this, Preferences.HAS_DISPLAYED_FIRST_PAYMENT_CARD, true)
        finish()
    }

    private fun checkButtonState() {
        findViewById<AppCompatButton>(R.id.tv_buy).isEnabled = false
        if (isButtonClickable) {
            isButtonClickable = false
            findViewById<AppCompatButton>(R.id.tv_buy).isEnabled = false
            Handler().postDelayed({
                isButtonClickable = true
                findViewById<AppCompatButton>(R.id.tv_buy).isEnabled = true
            }, 3000)
        }
    }
}

enum class PurchaseOption {
    InApp, Sub
}