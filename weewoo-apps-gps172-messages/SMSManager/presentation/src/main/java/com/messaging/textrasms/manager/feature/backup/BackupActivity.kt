package com.messaging.textrasms.manager.feature.backup

import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction

import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.container_activity.*


class BackupActivity : QkThemedActivity() {

    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_activity)

        conversations.visibility = View.GONE
        backcompose.visibility = View.GONE
        showBackButton(true)

        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(BackupController()))
        }


        /*if (!IronSourceAdsManger.isPurchased(this)) {
            IronSourceAdsManger.showBanner(this, ad_view_container, "bkp")
        }*/
        loadBanner()
//
//        Old Inter Ads Implementation
//        if (!Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
//            AdvertiseHandler.getInstance(this)
//                .loadBannerAds(this, UtilsData.banner_ad_unit_id, ad_view_container, null)
//        }
//

    }




    private fun loadBanner(){

      //MaxAdBannerAdShow
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

}