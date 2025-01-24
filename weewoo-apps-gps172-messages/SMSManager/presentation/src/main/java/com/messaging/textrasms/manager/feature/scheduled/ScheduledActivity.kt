package com.messaging.textrasms.manager.feature.scheduled

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

import com.jakewharton.rxbinding2.view.clicks
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkDialog
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.util.FontProvider
import com.messaging.textrasms.manager.utils.Constants
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.scheduled_activity.*
import kotlinx.android.synthetic.main.scheduled_activity.compose
import kotlinx.android.synthetic.main.scheduled_activity.empty
import javax.inject.Inject

class ScheduledActivity : QkThemedActivity(), ScheduledView {

    @Inject
    lateinit var dialog: QkDialog

    @Inject
    lateinit var fontProvider: FontProvider

    @Inject
    lateinit var messageAdapter: ScheduledMessageAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val messageClickIntent by lazy { messageAdapter.clicks }
    override val messageMenuIntent by lazy { dialog.adapter.menuItemClicks }
    override val composeIntent by lazy { compose.clicks() }

    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[ScheduledViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scheduled_activity)
        setTitle(R.string.scheduled_title)
        showBackButton(true)
        viewModel.bindView(this)

        showBackButton(true)

        dialog.title = getString(R.string.scheduled_options_title)
        dialog.adapter.setData(R.array.scheduled_options)

        messageAdapter.emptyView = empty
        messageAdapter.ScheduleView = schedule_guide



//        messages.adapter = messageAdapter

        colors.theme().let { theme ->
        }
/*
        if (!IronSourceAdsManger.isPurchased(this)) {
            IronSourceAdsManger.showBanner(this, ad_view_container, "schdl")
        }*/
//        loadBanner()
        //old banner

//        if (!Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
//            AdvertiseHandler.getInstance(this)
//                .loadBannerAds(this, UtilsData.banner_ad_unit_id, ad_view_container, null)
//        }


    }



    private fun loadBanner(){

        //MaxAdBannerAdShow>pro_feature_no_need_to_load
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun render(state: ScheduledState) {
        messageAdapter.updateData(state.scheduledMessages)
    }

    override fun showMessageOptions() {
        dialog.show(this)
    }

    override fun onBackPressed() {
        Constants.IS_FROM_ACTIVITY = true
        super.onBackPressed()
    }

}