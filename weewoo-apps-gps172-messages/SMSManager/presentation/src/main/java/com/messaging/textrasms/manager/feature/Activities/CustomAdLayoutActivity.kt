package com.messaging.textrasms.manager.feature.Activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.feature.Activities.MainActivity.Companion.itemList
import com.messaging.textrasms.manager.feature.adapters.CustomAdListAdapter
import com.messaging.textrasms.manager.model.logDebug
import com.messaging.textrasms.manager.util.Preferences
import dagger.android.AndroidInjection

class CustomAdLayoutActivity : QkThemedActivity() {
    companion object {
        var fromcustom: Boolean = false
    }

    lateinit var recyclerView: RecyclerView

    lateinit var customadapter: CustomAdListAdapter
    lateinit var share_btn: Button
    lateinit var Show_btn: Button
    lateinit var coin_txt: TextView
    lateinit var back: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_ad_layout)
        recyclerView = findViewById(R.id.recycler_view)
        val layoutManager = LinearLayoutManager(this)
        customadapter = CustomAdListAdapter(this, itemList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = customadapter
        share_btn = findViewById(R.id.share_btn)
        back = findViewById(R.id.back)
        Show_btn = findViewById(R.id.watch_icon_btn)
        coin_txt = findViewById(R.id.coin_txt)
        coin_txt.text = "" + Preferences.getIntVal(this, Preferences.Coinsvalue, 0)
        share_btn.setOnClickListener {
            openShare()
        }

        back.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })
        checkcoinvalue()
    }

    private fun openShare() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=" + packageName
        )
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Share with"))
        var count = Preferences.getIntVal(this, Preferences.Coinsvalue, 0)
        count = count + 25
        Preferences.setIntVal(this, Preferences.Coinsvalue, count)
    }

    override fun onResume() {
        super.onResume()
        checkcoinvalue()
    }

    private fun checkcoinvalue() {
        var count = Preferences.getIntVal(this, Preferences.Coinsvalue, 0)
        logDebug("coins" + "coins" + count)

        if (itemList.size != 0) {
            for (i in 0 until itemList.size) {
                if (isAppInstalled(itemList.get(i).app_id, this)) {
                    logDebug(
                        "coins" + "coinsinstalled" + count + ">>" + Preferences.Coinsvalue + itemList.get(
                            i
                        ).app_id
                    )
                    if (!Preferences.getBoolean(
                            this,
                            Preferences.Coinsvalue + itemList.get(i).app_id
                        )
                    ) {
                        Preferences.setIntVal(
                            this,
                            Preferences.Coinsvalue,
                            count + itemList.get(i).coin
                        )
                        Preferences.setBoolean(
                            this,
                            Preferences.Coinsvalue + itemList.get(i).app_id,
                            true
                        )
                        count = Preferences.getIntVal(this, Preferences.Coinsvalue, 0)
                        if (coin_txt != null) {
                            coin_txt.text = "" + count
                        }
                        customadapter.notifyDataSetChanged()
                        logDebug("coins" + "coinsinstalled" + count)
                        if (count >= 100) {
                            if (!Preferences.getBoolean(this, Preferences.FirstTimeRemoved)) {
                                Handler().postDelayed(Runnable {
                                    Preferences.setBoolean(this, Preferences.FirstTimeRemoved, true)
                                    fromcustom = true
                                    finish()
                                }, 600)
                            }
                        } else {
                            Preferences.setBoolean(this, Preferences.FirstTimeRemoved, false)
                        }

                    }


                } else {
                    if (Preferences.getBoolean(
                            this,
                            Preferences.Coinsvalue + itemList.get(i).app_id
                        )
                    ) {
                        Preferences.setBoolean(
                            this,
                            Preferences.Coinsvalue + itemList.get(i).app_id,
                            false
                        )
                        count = Preferences.getIntVal(this, Preferences.Coinsvalue, 0)
                        logDebug(
                            "count" + "count" + count + ">" + Preferences.Coinsvalue + itemList.get(
                                i
                            ).app_id
                        )

                        Preferences.setIntVal(
                            this,
                            Preferences.Coinsvalue,
                            (count - itemList.get(i).coin)
                        )
                        count = Preferences.getIntVal(this, Preferences.Coinsvalue, 0)
                        logDebug(
                            "count" + "count" + count + ">" + Preferences.Coinsvalue + itemList.get(
                                i
                            ).app_id
                        )
                        customadapter.notifyDataSetChanged()

                    }


                    if (coin_txt != null) {
                        coin_txt.text = "" + count
                    }
                }
            }
        }
        if (coin_txt != null) {
            coin_txt.text = "" + count
        }
    }

    fun isAppInstalled(packageName: String, context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            logDebug("packagename" + ">" + packageName)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            logDebug("packagename" + e.message)
            false
        }
    }
}