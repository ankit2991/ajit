package com.translate.languagetranslator.freetranslation.activities.adptypaywall

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adapty.Adapty
import com.adapty.models.AdaptyPaywall
import com.adapty.models.AdaptyPaywallProduct
import com.adapty.ui.AdaptyUI
import com.adapty.utils.AdaptyResult
import com.code4rox.adsmanager.TinyDB
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.getRemoteConfig
import com.translate.languagetranslator.freetranslation.appUtils.showInterstitial
import com.translate.languagetranslator.freetranslation.utils.RateUs
import kotlinx.android.synthetic.main.activity_purchase.btn_continue
import kotlinx.android.synthetic.main.activity_purchase.parentLayout


class PaywallUi : AppCompatActivity() {

    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(this)
    }

    var paywallType: String = "gps_unlimited"
    private var fromStart = false
    private var flagNavigate = false;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_main_paywal)

        if (!isOnline(this)) {

            Toast.makeText(
                applicationContext,
                getString(R.string.internet_not_connected),
                Toast.LENGTH_SHORT
            )
                .show()

            finish()
            return
        }

        val intent: Intent = intent

        if (intent != null) {
            if (intent.hasExtra(Constants.PAYWALL_TYPE)) {
                paywallType = intent.getStringExtra(Constants.PAYWALL_TYPE).toString()
                Log.i("CheckPaywallType", "onCreate: " + paywallType)
            }
            if (intent.hasExtra("from")) {
                fromStart = intent.getBooleanExtra("from", false)
            }
        }

//        Log.e("paywallUi",">"+paywallType)
//        Log.e("paywallUi","from>"+from)

//        Toast.makeText(this, ">"+paywallType, Toast.LENGTH_SHORT).show()

        loadPaywall()

        val window: Window? = window
        if (window != null) {
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                )
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                )
            }
        }


//        if (savedInstanceState == null) {
//            supportFragmentManager
//                .beginTransaction()
//                .add(android.R.id.content, MainPaywallFragment.newInstance(paywallType,from))
//                .commit()
//        }
    }


    override fun onResume() {
        super.onResume()
        if (flagNavigate) {
            startActivity(Intent(this@PaywallUi, HomeActivity::class.java))
        }
    }

    private fun loadPaywall() {


//        Toast.makeText(this, "from>"+paywallType, Toast.LENGTH_SHORT).show()


        if (paywallType == Constants.GPS_ONBOARDING) {
            if (Constants.adaptyConfigOnboarding != null) {
                presentPaywall(
                    Constants.adaptyPaywallOnboarding!!,
                    Constants.adaptyProductOnboarding!!,
                    Constants.adaptyConfigOnboarding!!
                )
//                Toast.makeText(this, "from>"+ adapty_paywall_Unlimited!!.placementId, Toast.LENGTH_SHORT).show()
            } else {
                loadPaywallOnline()
            }
        } else if (paywallType == Constants.GPS_PREMIUM) {
            if (Constants.adaptyConfigPremium != null) {
                presentPaywall(
                    Constants.adaptyPaywallPremium!!,
                    Constants.adaptyProductPremium!!,
                    Constants.adaptyConfigPremium!!
                )
//                Toast.makeText(this, "from>"+ Constants.adapty_paywall_AppOpen!!.placementId, Toast.LENGTH_SHORT).show()
            } else {
                loadPaywallOnline()
            }
        } else if (paywallType == Constants.GPS_DEFAULT) {
            if (Constants.adaptyConfigDefault != null) {
                presentPaywall(
                    Constants.adaptyPaywallDefault!!,
                    Constants.adaptyProductDefault!!,
                    Constants.adaptyConfigDefault!!
                )
//                Toast.makeText(this, "from>"+ Constants.adapty_paywall_Default!!.placementId, Toast.LENGTH_SHORT).show()

            } else {
                loadPaywallOnline()
            }
        }


    }

    private fun loadPaywallOnline() {
        progressDialog.setCancelable(false)
        progressDialog.show()

        Adapty.getPaywall(paywallType) { paywallResult ->
            when (paywallResult) {
                is AdaptyResult.Success -> {
                    val paywall = paywallResult.value
                    Adapty.getPaywallProducts(paywall) { productResult ->
//                        progressDialog.cancel()

                        when (productResult) {
                            is AdaptyResult.Success -> {
                                val products = productResult.value

//                                    progressDialog.show()


                                AdaptyUI.getViewConfiguration(paywall) { configResult ->
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.cancel()
                                    }
                                    when (configResult) {
                                        is AdaptyResult.Success -> {
                                            val viewConfig = configResult.value


                                            presentPaywall(paywall, products, viewConfig)
                                        }

                                        is AdaptyResult.Error -> {
                                            if (progressDialog != null && progressDialog.isShowing()) {
                                                progressDialog.cancel()
                                            }

                                            Log.e(
                                                "paywallUiError",
                                                "config>" + configResult.error.message
                                            )
//                                                errorView.text =
//                                                    "error:\n${configResult.error.message}"
                                            if (fromStart) {
                                                startActivity(
                                                    Intent(
                                                        this,
                                                        HomeActivity::class.java
                                                    ).setFlags(
                                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    )
                                                )
                                                this.finish()
                                            } else {
                                                this.finish()
                                            }
                                            Toast.makeText(
                                                this,
                                                "something went wrong",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                        else -> {}
                                    }
                                }


//                                if (paywall.hasViewConfiguration) {
//                                    with(presentPaywall) {
//                                        isEnabled = true
//                                        text = "Present paywall"
//                                    }
//
//                                } else {
//                                    visualValue.text = "no"
//                                    with(presentPaywall) {
//                                        isEnabled = false
//                                        text = "No view configuration"
//                                    }
//                                }
                            }

                            is AdaptyResult.Error -> {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.cancel()
                                }

                                Log.e("paywallUiError", "product>" + productResult.error)
                                /**
                                 * If the error code is `AdaptyErrorCode.NO_PRODUCT_IDS_FOUND`, please make sure you have changed your applicationId.
                                 *
                                 * In order to receive products and open visual paywalls,
                                 * please change sample's applicationId in app/build.gradle to yours
                                 */
                                if (fromStart) {
                                    startActivity(
                                        Intent(this, HomeActivity::class.java).setFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        )
                                    )
                                    this.finish()
                                } else {
                                    this.finish()
                                }
                                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT)
                                    .show()
//                                errorView.text = "error:\n${productResult.error.message}"
                            }
                        }
                    }
                }

                is AdaptyResult.Error -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.cancel()
                    }
                    Log.e("paywallUiError", "paywall>" + paywallResult.error.message)
//                    errorView.text = "error:\n${paywallResult.error.message}"
                    if (fromStart) {
                        startActivity(
                            Intent(this, HomeActivity::class.java).setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            )
                        )
                        this.finish()
                    } else {
                        this.finish()
                    }
                    Toast.makeText(
                        this,
                        "something went wrong" + paywallResult.error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun presentPaywall(
        paywall: AdaptyPaywall,
        products: List<AdaptyPaywallProduct>,
        viewConfiguration: AdaptyUI.ViewConfiguration
    ) {
        val paywallFragment =
            PaywallUiFragment.newInstance(
                paywall,
                products,
                viewConfiguration,
                fromStart
            )


        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_up,
                R.anim.slide_down,
                R.anim.slide_up,
                R.anim.slide_down
            )
            .addToBackStack(null)
            .add(android.R.id.content, paywallFragment)
            .commitAllowingStateLoss();


    }

    override fun onBackPressed() {
        if (fromStart) {
            val config = getRemoteConfig().getString("GPS126_rate_us_placement")
            if (config == "Open" && TinyDB.getInstance(this@PaywallUi)
                    .getInt(Constants.RATEUS_FIRST_TIME) % 5 == 1
            ) {
                RateUs.showDialog(this@PaywallUi, object : RateUs.CallBackClick {
                    override fun onPositive() {
                        flagNavigate = true
                        TinyDB.getInstance(this@PaywallUi)
                            .putInt(Constants.RATEUS_FIRST_TIME, 1)
//                        parentLayout.background=null
                    }

                    override fun onNegitive() {
                        flagNavigate = true
//                        parentLayout.background=null
                        TinyDB.getInstance(this@PaywallUi)
                            .putInt(Constants.RATEUS_FIRST_TIME, 1)
                        startActivity(Intent(this@PaywallUi, HomeActivity::class.java))
                    }

                    override fun onload() {
                        parentLayout.visibility = View.VISIBLE
                        btn_continue.visibility = View.GONE
//                        startActivity(Intent(this@PurchaseActivity, SplashNewActivity::class.java))
//                        finish()
                    }

                })

            } else {
                startActivity(Intent(this@PaywallUi, HomeActivity::class.java))
            }
        } else {
            showInterstitial {
                finish()
            }

        }

    }

}