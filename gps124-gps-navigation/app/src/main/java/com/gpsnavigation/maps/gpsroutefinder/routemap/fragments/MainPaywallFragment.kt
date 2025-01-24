package com.gpsnavigation.maps.gpsroutefinder.routemap.fragments

import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import com.adapty.Adapty
import com.adapty.models.AdaptyPaywall
import com.adapty.models.AdaptyPaywallProduct
import com.adapty.models.AdaptyViewConfiguration
import com.adapty.utils.AdaptyResult
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.quimeraManager.QuimeraInit
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.onReceiveSystemBarsInsets



class MainPaywallFragment : Fragment(R.layout.fragment_main_paywal) {

    companion object {
//        fun newInstance(type:String) = MainPaywallFragment()
        fun newInstance(
            type:String
        ) =
            MainPaywallFragment().apply {
                this.paywallType = type

            }
    }

    var paywallType = ""

    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(context)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.onReceiveSystemBarsInsets { insets ->
            view.setPadding(
                view.paddingStart,
                view.paddingTop + insets.top,
                view.paddingEnd,
                view.paddingBottom + insets.bottom,
            )
        }

        val window: Window? = activity?.window
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
        }

        loadPaywall(paywallType)
    }



    private fun loadPaywall(type:String){
        progressDialog.setCancelable(false)
        progressDialog.show()


        Adapty.getPaywall(type) { paywallResult ->
            when (paywallResult) {
                is AdaptyResult.Success -> {
                    val paywall = paywallResult.value
                    Adapty.getPaywallProducts(paywall) { productResult ->
//                        progressDialog.cancel()

                        when (productResult) {
                            is AdaptyResult.Success -> {
                                val products = productResult.value

//                                    progressDialog.show()


                                Adapty.getViewConfiguration(paywall, "en") { configResult ->
                                    progressDialog.cancel()
                                    when (configResult) {
                                        is AdaptyResult.Success -> {
                                            val viewConfig = configResult.value

//                                            if (requireContext() !=  null && products.get(0).productDetails != null){
//                                                QuimeraInit.sendSkuDetails_adapty(requireContext(),products.get(0).productDetails)
//                                            }
                                            presentPaywall(paywall, products, viewConfig)
                                        }
                                        is AdaptyResult.Error -> {
//                                                errorView.text =
//                                                    "error:\n${configResult.error.message}"
                                            activity?.finish()
                                            Toast.makeText(activity, "something went wrong", Toast.LENGTH_SHORT).show()
                                        }
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
                                /**
                                 * If the error code is `AdaptyErrorCode.NO_PRODUCT_IDS_FOUND`, please make sure you have changed your applicationId.
                                 *
                                 * In order to receive products and open visual paywalls,
                                 * please change sample's applicationId in app/build.gradle to yours
                                 */
                                activity?.finish()
                                Toast.makeText(activity, "something went wrong", Toast.LENGTH_SHORT).show()
//                                errorView.text = "error:\n${productResult.error.message}"
                            }
                        }
                    }
                }
                is AdaptyResult.Error -> {
//                    errorView.text = "error:\n${paywallResult.error.message}"
                    activity?.finish()
                    Toast.makeText(activity, "something went wrong", Toast.LENGTH_SHORT).show()
                    progressDialog.cancel()
                }
            }
        }
    }


    private fun presentPaywall(
        paywall: AdaptyPaywall,
        products: List<AdaptyPaywallProduct>,
        viewConfiguration: AdaptyViewConfiguration
    ) {
        val paywallFragment =
            PaywallUiFragment.newInstance(
                paywall,
                products,
                viewConfiguration,
            )



        if (this.isAdded && !this.isDetached) {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_up,
                    R.anim.slide_down,
                    R.anim.slide_up,
                    R.anim.slide_down,
                )
                .addToBackStack(null)
                .add(android.R.id.content, paywallFragment)
                .commitAllowingStateLoss()
        }
    }




}