package com.zaeem.mytvcast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ironsource.mediationsdk.IronSource
import com.zaeem.mytvcast.Utils.startActivityClearTask
import kotlinx.coroutines.delay

class SplashActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        IronSource.init(this, "112281475", IronSource.AD_UNIT.INTERSTITIAL);
        IronSource.init(this, "112281475", IronSource.AD_UNIT.BANNER);


        lifecycleScope.launchWhenCreated {
            delay(2000)

            startActivityClearTask(MainActivity::class.java)

        }
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

}