package com.translate.languagetranslator.freetranslation.phrasebook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.code4rox.adsmanager.MaxAdManager
import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.adptypaywall.PaywallUi
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants.GPS_PREMIUM
import com.translate.languagetranslator.freetranslation.appUtils.Constants.PAYWALL_TYPE
import com.translate.languagetranslator.freetranslation.appUtils.Constants.REQ_CODE_RATEUS
import com.translate.languagetranslator.freetranslation.appUtils.Logging
import com.translate.languagetranslator.freetranslation.appUtils.isPremium
import com.translate.languagetranslator.freetranslation.appUtils.showInterstitial
import com.translate.languagetranslator.freetranslation.interfaces.AdLoadedCallback
import com.translate.languagetranslator.freetranslation.interfaces.PhraseitemCallback
import com.translate.languagetranslator.freetranslation.phrasebook.phrasebookTranslation.PhrasebookDetailFragmet
import com.translate.languagetranslator.freetranslation.utils.AdsUtill
import com.translate.languagetranslator.freetranslation.utils.Constants
import kotlinx.android.synthetic.main.activity_phrasebook.*
import kotlinx.android.synthetic.main.activity_phrasebook.back_btn
import kotlinx.android.synthetic.main.activity_phrasebook.iv_crown_pro_banner

class PhrasebookActivity : AppCompatActivity() {
    var adapter: PhrasebookAdapter? = null
    private var mLastClickTime: Long = 0
    private lateinit var adsUtill: AdsUtill
    private var isAdsOnly = true

    var listData = java.util.ArrayList<PhareseModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phrasebook)
        phraseRv.visibility = View.VISIBLE
        feature_banner.visibility = View.VISIBLE

        Logging.adjustEvent("9tt4mm", Logging.currentTime(), "Phrasebook")

        adsUtill = AdsUtill(this)
        (application as AppBase).isOnMainMenu = true

        checkAdsOnly()
        loadBannerAd()
        setupRv()
        initclick()
        /*val showInstratial=incrementInterstitialCount()
        Handler(Looper.getMainLooper()).postDelayed({
            if (showInstratial){
                if (!isPremium()) {
                    if (IronSource.isInterstitialReady()) {
                        IronSource.showInterstitial()
                        (application as AppBase).addInterstitialSessionCount=0
                    }
                }
            }
        }, 2000)*/
    }


    fun initclick() {
        back_btn.setOnClickListener {
            onBackPressed()
        }

        feature_banner.setOnClickListener {
            if (isDoubleClick()) {
//                startActivity(Intent(this, PurchaseActivity::class.java))

                val intent = Intent(
                    this@PhrasebookActivity,
                    PaywallUi::class.java
                )
                intent.putExtra(
                    PAYWALL_TYPE,
                    GPS_PREMIUM
                )
                startActivity(intent)
            }
        }


    }

    private fun setupRv() {
        dataList()
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        phraseRv.layoutManager = linearLayoutManager
        val adapter = PhrasebookAdapter(this, listData, object : PhraseitemCallback {
            override fun onItemClick(item: Int) {
                showInterstitial {
                    ItemClick(item)
                }

            }

        })
        phraseRv.adapter = adapter
    }

    fun dataList() {
        listData.add(PhareseModel(R.drawable.chat, R.string.essentials))
        listData.add(PhareseModel(R.drawable.airplane, R.string.whiletravelling))
        listData.add(PhareseModel(R.drawable.first_aid_kit, R.string.medical))
        listData.add(PhareseModel(R.drawable.hotel, R.string.hotel))
        listData.add(PhareseModel(R.drawable.food, R.string.restaurant))
        listData.add(PhareseModel(R.drawable.mirror_ball, R.string.bar))
        listData.add(PhareseModel(R.drawable.grocery_store, R.string.store))
        listData.add(PhareseModel(R.drawable.laptop_screen, R.string.work))
        listData.add(PhareseModel(R.drawable.clock, R.string.time))

    }

    fun ItemClick(item: Int) {

        if (item == R.string.essentials) {
//            Toast.makeText(this, "click", Toast.LENGTH_LONG).show()

//            val mainIntent = Intent(this, PhrasebookTranslation::class.java)
//            startActivity(mainIntent)

            Constants.PhraseClickedItem = R.string.essentials
            openDetailFrag()
            phraseRv.visibility = View.GONE
            feature_banner.visibility = View.GONE
            main_framelayout.visibility = View.VISIBLE
        }

        if (item == R.string.whiletravelling) {
            Constants.PhraseClickedItem = R.string.whiletravelling
            phraseRv.visibility = View.GONE
            feature_banner.visibility = View.GONE
            main_framelayout.visibility = View.VISIBLE
            openDetailFrag()
        }
        if (item == R.string.medical) {
            Constants.PhraseClickedItem = R.string.medical
            phraseRv.visibility = View.GONE
            feature_banner.visibility = View.GONE
            main_framelayout.visibility = View.VISIBLE
            openDetailFrag()
        }
        if (item == R.string.hotel) {
            Constants.PhraseClickedItem = R.string.hotel
            phraseRv.visibility = View.GONE
            feature_banner.visibility = View.GONE
            main_framelayout.visibility = View.VISIBLE
            openDetailFrag()
        }
        if (item == R.string.restaurant) {
            Constants.PhraseClickedItem = R.string.restaurant
            phraseRv.visibility = View.GONE
            feature_banner.visibility = View.GONE
            main_framelayout.visibility = View.VISIBLE
            openDetailFrag()
        }
        if (item == R.string.bar) {
            Constants.PhraseClickedItem = R.string.bar
            phraseRv.visibility = View.GONE
            feature_banner.visibility = View.GONE
            main_framelayout.visibility = View.VISIBLE
            openDetailFrag()
        }
        if (item == R.string.store) {
            Constants.PhraseClickedItem = R.string.store
            phraseRv.visibility = View.GONE
            feature_banner.visibility = View.GONE
            main_framelayout.visibility = View.VISIBLE
            openDetailFrag()
        }
        if (item == R.string.work) {
            Constants.PhraseClickedItem = R.string.work
            phraseRv.visibility = View.GONE
            feature_banner.visibility = View.GONE
            main_framelayout.visibility = View.VISIBLE
            openDetailFrag()
        }
        if (item == R.string.time) {
            Constants.PhraseClickedItem = R.string.time
            phraseRv.visibility = View.GONE
            feature_banner.visibility = View.GONE
            main_framelayout.visibility = View.VISIBLE
            openDetailFrag()
        }

    }

    fun openDetailFrag() {
        try {
            val manager: FragmentManager = supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            transaction.add(R.id.main_framelayout, PhrasebookDetailFragmet(), "d")
            transaction.addToBackStack(null)
            transaction.commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onBackPressed() {
        setResult(REQ_CODE_RATEUS, Intent())
        super.onBackPressed()
        if (main_framelayout.visibility == View.VISIBLE) {
            phraseRv.visibility = View.VISIBLE
            feature_banner.visibility = View.VISIBLE
            main_framelayout.visibility = View.GONE
        } else {
            showInterstitial {
                //        TODO()4.5.0  banner was being destroyed here
                //adsUtill.destroyBanner()
                HomeActivity.showBanner = true
            }
        }

    }

    private fun isDoubleClick(): Boolean {
        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    private fun loadBannerAd() {

        if (!isPremium()) {
            try {
                MaxAdManager.createBannerAd(this, native_banner_container_phone_book)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun checkAdsOnly() {
        if (isAdsOnly) {
            if (!isPremium()) {
                iv_crown_pro_banner.visibility = View.VISIBLE
            } else {
                iv_crown_pro_banner.visibility = View.GONE
            }
        } else {
            iv_crown_pro_banner.visibility = View.GONE
        }
    }
}