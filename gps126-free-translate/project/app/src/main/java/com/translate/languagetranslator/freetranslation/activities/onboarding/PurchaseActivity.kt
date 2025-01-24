package com.translate.languagetranslator.freetranslation.activities.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.code4rox.adsmanager.TinyDB
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.RemoteConfigConstants
import com.translate.languagetranslator.freetranslation.appUtils.getRemoteConfig
import com.translate.languagetranslator.freetranslation.appUtils.showInterstitial
import com.translate.languagetranslator.freetranslation.appUtils.showToast
import com.translate.languagetranslator.freetranslation.extension.ConstantsKT
import com.translate.languagetranslator.freetranslation.quimera.QuimeraInit
import com.translate.languagetranslator.freetranslation.quimera.SubscriptionItem
import com.translate.languagetranslator.freetranslation.utils.RateUs
import kotlinx.android.synthetic.main.activity_purchase.btn_continue
import kotlinx.android.synthetic.main.activity_purchase.parentLayout


class PurchaseActivity : BaseBillingActivity() {

    //   private val eventHelper = EventHelper()
    private var isYearSub = false;
    private var flagNavigate = false;
    private var isButtonClickable = true

    private var remoteSkuValue = "weekly_sub"

    private var tv_price: TextView? = null

    private var fromStart = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)
        fromStart = intent.getBooleanExtra("fromStart", false)

        isYearSub = getRemoteConfig().getBoolean(RemoteConfigConstants.Year_Sub)
        tv_price = findViewById<TextView>(R.id.tv_price)
        TinyDB.getInstance(this).putBoolean(ConstantsKT.SHOW_PAYMENT_ONE_TIME, true)
        setPriceText()

        findViewById<ImageView>(R.id.closeIv).setOnClickListener {
            onBackPressed()
        }


        findViewById<TextView>(R.id.policies).setOnClickListener { openUrl("https://zedlatino.info/privacy-policy-apps.html") }
        findViewById<TextView>(R.id.terms).setOnClickListener { openUrl("https://zedlatino.info/TermsOfUse.html") }
        findViewById<Button>(R.id.btn_continue).setOnClickListener {

            findViewById<Button>(R.id.btn_continue).isEnabled = false
            if (isButtonClickable) {
                isButtonClickable = false
                findViewById<Button>(R.id.btn_continue).isEnabled = false
                Handler().postDelayed({
                    isButtonClickable = true
                    findViewById<Button>(R.id.btn_continue).isEnabled = true
                }, 3000)
            }

//            if (QuimeraInit.skuDetail != null){
//                QuimeraInit.sendSkuDetails(this@PurchaseActivity, SubscriptionItem(QuimeraInit.skuDetail!!))
//            }

//            subMonthly?.let {
//                if (billingProcess.isBillingProcessReady) {
//
//                    QuimeraInit.singleCall = true
//                    QuimeraInit.sendSkuDetails(this@PurchaseActivity, SubscriptionItem(it))
//
//                    billingProcess.launchPurchaseFlow(it)
//                } else {
//
//                    showToast(this, "Billing process not initialized. Please wait")
//                }
//            } ?: run {
//
//                // showToast(this, "It seems there are no products to purchase.")
//            }
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun setPriceText() {
        // Toast.makeText(this,"price"+ skuPrice,Toast.LENGTH_LONG).show()
        if (skuPrice.isBlank()) {
            Handler(Looper.getMainLooper()).postDelayed({ setPriceText() }, 1700)
        } else {
            tv_price?.text = skuPrice
        }

    }

    override fun onResume() {
        super.onResume()
        if (flagNavigate) {
            startActivity(Intent(this@PurchaseActivity, HomeActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (fromStart) {
            val config = getRemoteConfig().getString("GPS126_rate_us_placement")
            if (config == "Open"&&TinyDB.getInstance(this@PurchaseActivity)
                    .getInt(Constants.RATEUS_FIRST_TIME)%5 == 1) {
                RateUs.showDialog(this@PurchaseActivity, object : RateUs.CallBackClick {
                    override fun onPositive() {
                        flagNavigate = true
                        TinyDB.getInstance(this@PurchaseActivity)
                            .putInt(Constants.RATEUS_FIRST_TIME, 1)
//                        parentLayout.background=null
                    }

                    override fun onNegitive() {
                        flagNavigate = true
//                        parentLayout.background=null
                        TinyDB.getInstance(this@PurchaseActivity)
                            .putInt(Constants.RATEUS_FIRST_TIME, 1)
                        startActivity(Intent(this@PurchaseActivity, HomeActivity::class.java))
                    }

                    override fun onload() {
                        parentLayout.visibility = View.VISIBLE
                        btn_continue.visibility = View.GONE
//                        startActivity(Intent(this@PurchaseActivity, SplashNewActivity::class.java))
//                        finish()
                    }

                })

            } else {
                startActivity(Intent(this@PurchaseActivity, HomeActivity::class.java))
            }
        } else {
            showInterstitial{
                super.onBackPressed()
            }

        }
    }


}