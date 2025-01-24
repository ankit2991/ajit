package callerid.truecaller.trackingnumber.phonenumbertracker.block.util.calldorado

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.RelativeLayout
import callerid.truecaller.trackingnumber.phonenumbertracker.block.R
import callerid.truecaller.trackingnumber.phonenumbertracker.block.SearchSpamCallerActivity
import callerid.truecaller.trackingnumber.phonenumbertracker.block.bank_info.BankInfoActivity
import callerid.truecaller.trackingnumber.phonenumbertracker.block.location_info.LocationInfoActivity
import callerid.truecaller.trackingnumber.phonenumbertracker.block.near_by_place.NearByPlaceActivity
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.NumberLocationActivity
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.RechargeDetailActivity
import callerid.truecaller.trackingnumber.phonenumbertracker.block.setting.SettingCalller
import callerid.truecaller.trackingnumber.phonenumbertracker.block.sim_info.MainActivity1
import callerid.truecaller.trackingnumber.phonenumbertracker.block.ussd.SelectUSSDCodeActivity
import com.calldorado.ui.aftercall.CallerIdActivity
import com.calldorado.ui.views.custom.CalldoradoCustomView


/**
 * Created by Hamza Chaudhary
 * Sr. Software Engineer Android
 * Created on 11 Apr,2023 12:11
 * Copyright (c) All rights reserved.
 * @see "<a href="https://www.linkedin.com/in/iamhco/">Linkedin Profile</a>"
 */

class AfterCallCustomView(val context: Context?) : CalldoradoCustomView(context), OnClickListener {


    override fun getRootView(): View {
        val rl =
            View.inflate(context, R.layout.calldorado_screen, relativeViewGroup) as RelativeLayout
        Log.d(TAG, "getRootView: Called")
        rl.initView()
        return rl
    }

    private fun View.initView() {


        findViewById<View>(R.id.btn_num_location).setOnClickListener(this@AfterCallCustomView)
        findViewById<View>(R.id.btn_recharge_plan).setOnClickListener(this@AfterCallCustomView)
        findViewById<View>(R.id.btn_location_info).setOnClickListener(this@AfterCallCustomView)
        findViewById<View>(R.id.btn_ussd_code).setOnClickListener(this@AfterCallCustomView)
        findViewById<View>(R.id.btn_bank).setOnClickListener(this@AfterCallCustomView)
        findViewById<View>(R.id.btn_near_by_place).setOnClickListener(this@AfterCallCustomView)
        findViewById<View>(R.id.btn_sim_info).setOnClickListener(this@AfterCallCustomView)
        findViewById<View>(R.id.btn_setting).setOnClickListener(this@AfterCallCustomView)
        findViewById<View>(R.id.btn_spam_call).setOnClickListener(this@AfterCallCustomView)
    }

    override fun onClick(v: View?) {
        kotlin.runCatching {
            context?.run {
                var intent: Intent? = null
                when (v?.id) {
                    R.id.btn_spam_call -> {
                        intent = Intent(this, SearchSpamCallerActivity::class.java)
                    }
                    R.id.btn_num_location -> {
                        intent = Intent(this, NumberLocationActivity::class.java)
                    }
                    R.id.btn_recharge_plan -> {
                        intent = Intent(this, RechargeDetailActivity::class.java)
                    }
                    R.id.btn_location_info -> {
                        intent = Intent(this, LocationInfoActivity::class.java)
                    }
                    R.id.btn_ussd_code -> {
                        intent = Intent(this, SelectUSSDCodeActivity::class.java)
                    }
                    R.id.btn_bank -> {
                        intent = Intent(this, BankInfoActivity::class.java)
                    }
                    R.id.btn_near_by_place -> {
                        intent = Intent(this, NearByPlaceActivity::class.java)
                    }
                    R.id.btn_sim_info -> {
                        intent = Intent(this, MainActivity1::class.java)
                    }
                    R.id.btn_setting -> {
                        intent = Intent(this, SettingCalller::class.java)
                    }
                }
                intent?.flags = FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                if (calldoradoContext is CallerIdActivity) {
                    (calldoradoContext as CallerIdActivity).finish()
                }
            }
        }.getOrElse {
            Log.e(TAG, "onClick: Exception ${it.message}")
        }

    }


}