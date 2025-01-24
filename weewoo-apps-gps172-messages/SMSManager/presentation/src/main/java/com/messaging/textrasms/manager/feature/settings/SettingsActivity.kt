package com.messaging.textrasms.manager.feature.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.feature.Activities.MainActivity
import com.messaging.textrasms.manager.utils.Constants
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.container_activity.*

class SettingsActivity : QkThemedActivity() {

    private lateinit var router: Router

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_activity)
        backcompose.visibility = View.GONE
        conversations.visibility = View.GONE
        showBackButton(true)

        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(SettingsController()))
        }

    }

    override fun onBackPressed() {
        Constants.IS_FROM_ACTIVITY = true
        Constants.IS_FROM_SETTING = true
        finish()
//        if (!router.handleBack()) {
//            val i = Intent(this, MainActivity::class.java)
//            startActivity(i)
//        }
    }

}