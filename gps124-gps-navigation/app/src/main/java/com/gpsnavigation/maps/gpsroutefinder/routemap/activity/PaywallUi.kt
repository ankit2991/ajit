package com.gpsnavigation.maps.gpsroutefinder.routemap.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gpsnavigation.maps.gpsroutefinder.routemap.R
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.MainPaywallFragment
import com.gpsnavigation.maps.gpsroutefinder.routemap.utility.isOnline


class PaywallUi: AppCompatActivity() {


    var paywallType: String = "Unlock"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (isOnline(this)) {

        } else {

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

        if (intent != null){
            paywallType = intent.getStringExtra("paywallType").toString()
        }


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



        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(android.R.id.content, MainPaywallFragment.newInstance(paywallType))
                .commit()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()


        finish()
    }
}