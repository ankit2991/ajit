package com.translate.languagetranslator.freetranslation.activities.onboarding


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.code4rox.adsmanager.TinyDB
import com.rd.PageIndicatorView
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.adptypaywall.PaywallUi
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.RemoteConfigConstants
import com.translate.languagetranslator.freetranslation.appUtils.getRemoteConfig
import com.translate.languagetranslator.freetranslation.appUtils.isPremium
import com.translate.languagetranslator.freetranslation.extension.ConstantsKT


class OnboardingActivity : BaseBillingActivity() {

    var isAdsOnly = false

    var nonSwipeableViewPager: NonSwipeableViewPager? = null
    var pageIndicatorView: PageIndicatorView? = null
    var tv_skip: TextView? = null


//    public fun startBilling() {
//        super.initBilling()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_onboarding)



        nonSwipeableViewPager = findViewById<NonSwipeableViewPager>(R.id.nwvp_stepper)
        pageIndicatorView = findViewById(R.id.pageIndicatorView)
        tv_skip = findViewById(R.id.tv_skip)
        val fragments = ArrayList<Fragment>()
        fragments.add(getFragment(0))
        fragments.add(getFragment(1))
        fragments.add(getFragment(2))
        val adapter = OnboardingViewPagerAdapter(supportFragmentManager)
        nonSwipeableViewPager?.also {


            adapter.setFragments(fragments)
            adapter.onContinueClick = { currentItem ->
                setItem(fragments, it, currentItem)
            }

            pageIndicatorView?.setViewPager(it)
            it.adapter = adapter


        }

        tv_skip?.setOnClickListener {

            nonSwipeableViewPager?.run {
                setItem(fragments, this, currentItem)
            }

        }
    }


    private fun setItem(
        fragments: ArrayList<Fragment>,
        nonSwipeableViewPager: NonSwipeableViewPager,
        currentItem: Int
    ) {
        if (fragments.count() == 3 && currentItem == 2) {
            val showPaymentOneTime =
                TinyDB.getInstance(this).getBoolean(ConstantsKT.SHOW_PAYMENT_ONE_TIME)
            val isCompaignUser = TinyDB.getInstance(this).getBoolean(ConstantsKT.IS_COMPAIGN_USER)
            if (!showPaymentOneTime && !isPremium() && getRemoteConfig().getBoolean(RemoteConfigConstants.PaymentCard) && !isCompaignUser) {
//                val intent = intentFor<PurchaseActivity>()
//                intent.putExtra("fromStart", true)
//                startActivity(intent)

                val intent = Intent(
                    this@OnboardingActivity,
                    PaywallUi::class.java
                )
                intent.putExtra(
                    Constants.PAYWALL_TYPE,
                    Constants.GPS_ONBOARDING
                )
                intent.putExtra("from", true)
                startActivity(intent)

            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            finish()
        } else {
            nonSwipeableViewPager.currentItem = currentItem + 1
        }
    }

    private fun getFragment(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putInt(OnboardingFragment.stepParam, position)
        val retValue = OnboardingFragment()
        retValue.arguments = bundle
        //  retValue.onClick = onContinueClick
        return retValue

    }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun openUrl(url: String) {

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}