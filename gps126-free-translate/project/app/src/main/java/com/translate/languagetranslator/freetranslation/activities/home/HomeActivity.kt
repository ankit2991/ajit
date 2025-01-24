package com.translate.languagetranslator.freetranslation.activities.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.calldorado.Calldorado
import com.code4rox.adsmanager.AppOpenCallback
import com.code4rox.adsmanager.Constants.IS_FIRST_TIME
import com.code4rox.adsmanager.MaxAdManager
import com.code4rox.adsmanager.MaxAppOpenAdManager
import com.code4rox.adsmanager.TinyDB
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.adptypaywall.PaywallUi
import com.translate.languagetranslator.freetranslation.activities.clipboard.services.ClipBoardDataManager
import com.translate.languagetranslator.freetranslation.activities.conversation.ConversationActivity
import com.translate.languagetranslator.freetranslation.activities.conversation.SavedChatActivity
import com.translate.languagetranslator.freetranslation.activities.dictionary.DictionaryActivity
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.appUtils.Constants.*
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import com.translate.languagetranslator.freetranslation.extension.ConstantsKT
import com.translate.languagetranslator.freetranslation.interfaces.SlideAdInterface
import com.translate.languagetranslator.freetranslation.phrasebook.PhrasebookActivity
import com.translate.languagetranslator.freetranslation.utils.AdsUtill
import com.translate.languagetranslator.freetranslation.utils.ConsentManger
import com.translate.languagetranslator.freetranslation.utils.Constants.isOnHomeScreen
import com.translate.languagetranslator.freetranslation.utils.OverlayPermissionManager
import com.translate.languagetranslator.freetranslation.utils.RateUs
import com.translate.languagetranslator.freetranslation.viewmodels.MainActivityViewModel
import com.translate.languagetranslator.freetranslation.views.SimpleRatingBar
import kotlinx.android.synthetic.main.activity_conversation.native_banner_container_home_screen
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.appbar_home.*
import kotlinx.android.synthetic.main.dlg_clear_history.*
import kotlinx.android.synthetic.main.home_items_container.*
import kotlinx.android.synthetic.main.layout_nav_drawer.*
import kotlinx.android.synthetic.main.layout_permission.btn_continue
import kotlinx.android.synthetic.main.layout_permission.terms_policy
import kotlinx.android.synthetic.main.layout_permission.policies
import kotlinx.android.synthetic.main.layout_permission.terms
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class HomeActivity : AppCompatActivity(), View.OnClickListener, SlideAdInterface {

    val mainActivityViewModel: MainActivityViewModel by viewModel()

    private lateinit var exitDialog: Dialog
    private lateinit var removeAdsDialog: Dialog
    private val permissionRequestCode = 356
    var overlayPermissionManager: OverlayPermissionManager? = null

    companion object {
        public var showBanner: Boolean = true
        var calldoradoInitialized = false
    }
    //private lateinit var rateUsDialog: Dialog

    private lateinit var dialog: Dialog
    private lateinit var database: TranslationDb
    private lateinit var adsUtill: AdsUtill
    private lateinit var mFireBaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var configSettings: FirebaseRemoteConfigSettings
    private var clickType: ClickType = ClickType.NONE
    private var backClickCount = 1

    private var isRemoveAdShownTwice = false
    private var removeAdCount = 0

    private var mLastClickTime: Long = 0


    private var isSplashInterShow = false
    private var nativeShow = false
    private var nativePrioritySliderOne: String? = null
    private var nativePrioritySliderTwo: String? = null
    private var interPriorityBack: String? = null
    private var interCounterBack: Int? = null
    private var isPermissionScreen = true
    private var maxInterstitialSession: Int = 3
    private var isInterBackShow = false
    private var isAdsOnly = true;
    private var calldoradoInit = true
    private var isFirstTimeAfterOverlayPermission = true
    private var isPermissionGranted = ""
    var mFirebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)


    enum class ClickType {
        NONE, TRANSLATION, CONVERSATION, DICTIONARY, CAMERA, BOOKMARK, HISTORY, FICTIONAL, PHRASEBOOK
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        putPrefBoolean("app_killed", false)
        adsUtill = AdsUtill(this)

        // Toast.makeText(this,getRemoteConfig().getBoolean("GPS126_01_ads_only").toString(),Toast.LENGTH_LONG).show()

        overlayPermissionManager = OverlayPermissionManager(this@HomeActivity)
        lay1.visibility = View.VISIBLE
        lay2.visibility = View.VISIBLE
        lay3.visibility = View.VISIBLE
        initRemoteConfig()
        checkPermissions()
        database = TranslationDb.getInstance(this)
        initBillingProcess()
        //  disableBtn()
        checkAdsOnly()
        //initAds()
        initExitDialog()
        initRateUsDialog()
        initRemoveAdsDialog()
        createDialogBuilder()
        setSupportActionBar(toolbar_home)
        // Set the ImpressionDataListener
        setClickListeners()
        animatePro()
        val config = getRemoteConfig().getString("GPS126_rate_us_placement")
        Log.d("countSession", "" + TinyDB.getInstance(this@HomeActivity).getInt(RATEUS_FIRST_TIME))
        Log.d("config", "" + config)
        if (!TinyDB.getInstance(this@HomeActivity).getBoolean(RATEUS_FIRST_COMPLETE)) {
            if ((TinyDB.getInstance(this@HomeActivity)
                    .getInt(RATEUS_FIRST_TIME) % 5 == 2) && (config == "Init")
            ) {

                RateUs.showDialog(this@HomeActivity, object : RateUs.CallBackClick {
                    override fun onPositive() {

                    }

                    override fun onNegitive() {

                    }

                    override fun onload() {

                    }
                })
            }
        }

        if (mainActivityViewModel.isAutoAdsRemoved()) {
            clBottomSubscriptionAd.visibility = View.GONE
        }
        clBottomSubscriptionAd.setOnClickListener {
            val intent = Intent(
                this@HomeActivity,
                PaywallUi::class.java
            )
            intent.putExtra(
                PAYWALL_TYPE,
                GPS_DEFAULT
            )
            startActivity(intent)
//            startActivity(Intent(this, PurchaseActivity::class.java))
        }

        FirebaseApp.initializeApp(this)

//                firebaseEvent()

        /*if (isSplashInterShow) {
            if (!isPremium())
                showAds()
        }*/
    }


    fun initNative() {
        mainActivityViewModel.home_native().observe(this, androidx.lifecycle.Observer {
            if (!mainActivityViewModel.isAutoAdsRemoved() && it.show) {
                pre_load_layout.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    pre_load_layout.visibility = View.GONE
                }, 3000)

                MaxAdManager.createNativeAd(this, native_admob_container, iv_no_ad_home)

            }
        })


    }

//    fun firebaseEvent(){
//        //for admob
//        val bundle = Bundle()
//        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "ad_impression")
//        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,  this.getString(R.string.am_native_translator_home))
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
//
//
//        val bundle2 = Bundle()
//        bundle2.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "ad_impression")
//        bundle2.putString(FirebaseAnalytics.Param.ITEM_ID, getString(R.string.am_native_main_top))
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle2)
//
//
//    }


    /*private fun inflateFbNAtiveAd(fbNAtiveAd: NativeAd) {
        val view = layoutInflater.inflate(R.layout.fb_native_main_top, null, false)
        view?.let {
            val adTitle = it.findViewById<TextView>(R.id.native_ad_title)
            val adIcon = it.findViewById<AdIconView>(R.id.native_ad_icon)
            val callToAction = it.findViewById<Button>(R.id.native_ad_call_to_action)
            val native_ad_media = it.findViewById<com.facebook.ads.MediaView>(R.id.native_ad_media)

            val adOptions = AdOptionsView(this, fbNAtiveAd, native_ad_container)
            val ad_choices_container1 = it.findViewById<ViewGroup>(R.id.ad_choices_container)
            ad_choices_container1.removeAllViews()
            ad_choices_container1.addView(adOptions, 0)

            adTitle.text = fbNAtiveAd.advertiserName


            if (fbNAtiveAd.adIcon == null)
                adIcon.visibility = View.GONE
            else {
                adIcon.visibility = View.VISIBLE
            }
            if (!fbNAtiveAd.hasCallToAction())
                callToAction.visibility = View.GONE
            else {
                callToAction.visibility = View.VISIBLE
                callToAction.text = fbNAtiveAd.adCallToAction
            }
            // Create a list of clickable views
            val clickableViews = ArrayList<View>()
            // clickableViews.add(adTitle)
            clickableViews.add(callToAction)
            fbNAtiveAd.registerViewForInteraction(
                native_ad_container,
                native_ad_media,
                adIcon,
                clickableViews
            )
            native_admob_container.addView(view)
            native_ad_container.visibility = View.VISIBLE
            iv_no_ad_home.visibility = View.GONE
        }
    }*/

    /* private fun inflateAdMobNAtiveAd(it: NativeAd) {
         native_admob_container.removeAllViews()

         val view = layoutInflater.inflate(R.layout.main_native, null, false) as NativeAdView
         view.findViewById<TextView>(R.id.ad_headline).text = it.headline
         //view.mediaView = view.findViewById(R.id.ad_media_main)
         view.mediaView?.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
             override fun onChildViewAdded(parent: View?, child: View?) {
                 if (child is ImageView) child.scaleType = ImageView.ScaleType.CENTER_INSIDE
             }

             override fun onChildViewRemoved(parent: View?, child: View?) {
             }
         })

         if (it.icon == null)
             view.findViewById<ImageView>(R.id.ad_app_icon).visibility = View.GONE
         else {
             view.findViewById<ImageView>(R.id.ad_app_icon).visibility = View.VISIBLE
             view.findViewById<ImageView>(R.id.ad_app_icon).setImageDrawable(it.icon!!.drawable)
         }

         if (it.callToAction == null)
             view.findViewById<Button>(R.id.ad_call_to_action).visibility = View.GONE
         else {
             view.findViewById<Button>(R.id.ad_call_to_action).visibility = View.VISIBLE
             view.findViewById<Button>(R.id.ad_call_to_action).text = it.callToAction
             view.callToActionView = view.findViewById<Button>(R.id.ad_call_to_action)
         }

         view.setNativeAd(it)
         native_admob_container.addView(view)
         native_admob_container.visibility = View.VISIBLE
         iv_no_ad_home.visibility = View.GONE
     }*/

    private fun initRemoteConfig() {
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(200)
            .build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)
        //get firebase ads_only


        nativeShow = mFireBaseRemoteConfig.getBoolean(RemoteConfigConstants.HOME_NATIVE_SHOW)
        nativePrioritySliderOne =
            mFireBaseRemoteConfig.getString(RemoteConfigConstants.SLIDER_ONE_PRIORITY)
        nativePrioritySliderTwo =
            mFireBaseRemoteConfig.getString(RemoteConfigConstants.SLIDER_TWO_PRIORITY)
        isSplashInterShow =
            mFireBaseRemoteConfig.getBoolean(RemoteConfigConstants.SPLASH_INTER_SHOW)
        isInterBackShow =
            mFireBaseRemoteConfig.getBoolean(RemoteConfigConstants.HOME_BACK_INTER_SHOW)
        interCounterBack =
            mFireBaseRemoteConfig.getDouble(RemoteConfigConstants.HOME_BACK_INTER_COUNT).toInt()
        interPriorityBack =
            mFireBaseRemoteConfig.getString(RemoteConfigConstants.HOME_BACK_INTER_PRIORITY)

        maxInterstitialSession =
            mFireBaseRemoteConfig.getDouble(RemoteConfigConstants.MAX_INTERSTITIAL_SESSION).toInt()
        isPermissionScreen =
            mFireBaseRemoteConfig.getBoolean(RemoteConfigConstants.CalldoradoPermissionScreen)
    }

    private fun createDialogBuilder() {
        dialog = getClearDataDialog(this)
        dialog.setOnKeyListener { dialog, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog?.dismiss()
                true
            }
            false
        }
    }

    private fun initBillingProcess() {
        /* val skuList = ArrayList<String>()
         skuList.add(getString(R.string.in_app_key))
         billingProcess = BillingProcess(this, this, skuList, null, false)*/

        //Angel. Removed. No using
        //billingProcess = BillingProcess(this, this, skuList,null,  false)

    }

    //disable button when ads only true or enable when false
    fun disableBtn() {
//        checkAdsOnly(tv_pro_home_banner)
//        checkAdsOnly(clBottomSubscriptionAd)
//        checkAdsOnly(nav_remove_ads)

    }

    fun hideViews() {
        runOnUiThread {
            tv_pro_home_banner.clearAnimation()
            tv_pro_home_banner.visibility = View.GONE
            nav_remove_ads.visibility = View.GONE
            ad_choices_container.visibility = View.GONE
            iv_no_ad_home.visibility = View.VISIBLE
            native_banner_container_home_screen.visibility = View.GONE

        }
    }

    private fun initRemoveAdsDialog() {
        isRemoveAdShownTwice = getPrefBool("is_remove_ad_twice")
        removeAdCount = getPrefInt("remove_ad_show_count")
        // val price = skuDetailInApp?.oneTimePurchaseOfferDetails!!.formattedPrice
        removeAdsDialog = getRemoveAdsDialog(this)
        val tvPrice = removeAdsDialog.findViewById<TextView>(R.id.tvPrice)
        //  tvPrice.text = price + "/One Time Purchase"
        val ivCross = removeAdsDialog.findViewById<ImageView>(R.id.iv_cross_dlg)
        val btnRemoveAds = removeAdsDialog.findViewById<RelativeLayout>(R.id.layout_remove_ads)

        ivCross.setOnClickListener {

            removeAdsDialog.dismiss()
        }
        btnRemoveAds.setOnClickListener {
            removeAdsDialog.dismiss()
            inAppSubscription()
        }


        removeAdsDialog.setOnDismissListener {
            if (removeAdCount == 1) {
                isRemoveAdShownTwice = true
                putPrefBoolean("is_remove_ad_twice", isRemoveAdShownTwice)
            } else {
                removeAdCount++
                putPrefInt("remove_ad_show_count", removeAdCount)
            }
        }

    }

    private fun showRemoveAdDialog() {
        if (!isPremium()) {
            if (!isRemoveAdShownTwice) {
                removeAdsDialog.show()
            }
        }
    }

    var isRateable = false
    private fun initRateUsDialog() {
        /*FacturarateUsDialog = getRateUsDialog(this)
        val later = rateUsDialog.findViewById<Button>(R.id.btn_later)
        val ratingBar = rateUsDialog.findViewById<RatingBar>(R.id.rating_bar)


        later.setOnClickListener {
            rateUsDialog.dismiss()
        }
        val emailAddress = "satstudio.inc@gmail.com"
        val subject = "Feedback for Translate All App"
        ratingBar?.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { _, rating, _ ->
                if (isRateable) {
                    if (rating < 5) {
                        sendEmail(
                            arrayOf(emailAddress),
                            subject,
                            ""
                        )
                        rateUsDialog.dismiss()
                    } else {
                        callRateUs()
                        rateUsDialog.dismiss()


                    }
                }

            }

        rateUsDialog.setOnDismissListener {
            isRateable = false
            ratingBar?.rating = 0f
        }*/
        // TODO: 7/7/21 Meter in app rate dialog

    }

    //Removed by in-app review API
    private fun initExitDialog() {
//        val exitAdmob = AdmobUtils(this)
        exitDialog = getExitDialogNew(this)

        val ratingLayout: ConstraintLayout = exitDialog.findViewById(R.id.rating_layout)
        val tvLabelExitNow: TextView = exitDialog.findViewById(R.id.tv_label_exit_now)
        val ratingBar: SimpleRatingBar = exitDialog.findViewById(R.id.rating_exit)
        val exitYes: Button = exitDialog.findViewById(R.id.btn_exit)
        val exitNo: Button = exitDialog.findViewById(R.id.btn_exit_cancel)

        exitYes.setOnClickListener {
            putPrefBoolean("app_killed", true)
            finish()
        }
        exitNo.setOnClickListener { exitDialog.dismiss() }

        if (getPrefBool("is_exit_rating")) {
            ratingLayout.visibility = View.GONE
            tvLabelExitNow.visibility = View.VISIBLE
//            starsAnimation.visibility = View.GONE
        } else {
            ratingLayout.visibility = View.VISIBLE
            tvLabelExitNow.visibility = View.GONE
        }
        val emailAddress = "satstudio.inc@gmail.com"

        val subject = "Feedback for Translate All App"
        ratingBar.setOnRatingBarChangeListener { simpleRatingBar, rating, fromUser ->
            if (fromUser) {
                if (rating < 4) {
                    sendEmail(
                        arrayOf(emailAddress),
                        subject,
                        ""
                    )
                    exitDialog.dismiss()
                } else {
                    callRateUs()
                    exitDialog.dismiss()


                }
                putPrefBoolean("is_exit_rating", true)
            }
        }
        exitDialog.setOnDismissListener {
            if (getPrefBool("is_exit_rating")) {
                ratingLayout.visibility = View.GONE
                tvLabelExitNow.visibility = View.VISIBLE
            } else {
                ratingLayout.visibility = View.VISIBLE
                tvLabelExitNow.visibility = View.GONE

            }
        }


    }

    private fun showExit() {
        exitDialog.show()
    }

    private fun initAds() {
        if (!isPremium()) {
            if (isInterBackShow)
                initBackPressAdPair()
        }
    }

    private fun initBackPressAdPair() {
        /*when (val adsPriority = getAdsPriority(interPriorityBack)) {
            AdsPriority.ADMOB, AdsPriority.ADMOB_FACEBOOK -> {
                loadInterAm(adsPriority)
            }
            AdsPriority.FACEBOOK, AdsPriority.FACEBOOK_ADMOB -> {
                loadInterFb(adsPriority)
            }
        }*/
    }

    /*private fun loadInterFb(adsPriority: AdsPriority) {
        backPressFb = FacebookAdsUtils(this)
        backPressFb = FacebookAdsUtils(this, object : FacebookAdsUtils.FacebookInterstitialListner {
            override fun onFbInterstitialAdClose() {
            }

            override fun onFbInterstitialAdLoaded() {
                backPressAdPair = InterAdPair(interFB = backPressFb?.interstitialAd)

            }

            override fun onFbInterstitialAdFailed() {
                if (adsPriority == AdsPriority.FACEBOOK_ADMOB) {
                    loadInterAm(adsPriority)
                }
            }
        }, InterAdsIdType.INTER_FB_MAIN_BACK, true)
    }*/

    /*private fun loadInterAm(adsPriority: AdsPriority) {
        backPressAdMob = AdmobUtils(this)

        backPressAdMob = AdmobUtils(this, object : AdmobUtils.AdmobInterstitialListener {
            override fun onInterstitialAdClose() {

            }

            override fun onInterstitialAdLoaded() {
                backPressAdPair = InterAdPair(interAM = backPressAdMob?.adMobInterAd)
            }

            override fun onInterstitialAdFailed() {
                if (adsPriority == AdsPriority.ADMOB_FACEBOOK) {
                    loadInterFb(adsPriority)
                }
            }

            override fun onAdOpened() {
            }
        }, InterAdsIdType.INTER_AM_MAIN_BACK, true)


    }*/


    /*private fun showAds() {


        globarInterAdPair?.let {
            if (it.isLoaded())
                it.showAd(this)
        }

        checkForSubscriptionScreen()
    }*/

    private fun loadNativeAd() {

        /*val adsPriorityOne = getAdsPriority(nativePrioritySliderOne)
        val adsPriorityTwo = getAdsPriority(nativePrioritySliderTwo)

        if (nativeShow && !isPremium()) {
            adAdapter =
                SliderAdAdapter(
                    this,
                    NativeAdsIdType.NATIVE_AM_MAIN,
                    NativeAdsIdType.NATIVE_FB_MAIN,
                    adsPriorityOne,
                    adsPriorityTwo
                )
            adAdapter!!.setAdInterface(this)
            ad_choices_container.setSliderAdapter(adAdapter!!)
            ad_choices_container.isAutoCycle = false
            //ad_choices_container.setIndicatorAnimation(IndicatorAnimations.WORM)
            //ad_choices_container.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            //ad_choices_container.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
            ad_choices_container.indicatorSelectedColor = Color.parseColor("#3781D6")
            ad_choices_container.indicatorUnselectedColor = Color.GRAY
            //ad_choices_container.startAutoCycle()
        } else {
            ad_choices_container.visibility = View.GONE
            iv_no_ad_home.visibility = View.VISIBLE
        }*/


    }


    private fun setClickListeners() {
        if (isAdsOnly) {
            nav_remove_ads.visibility = View.VISIBLE
        } else {
            nav_remove_ads.visibility = View.GONE
        }


        navigation_home.setOnClickListener(this)
        tv_pro_home_banner.setOnClickListener(this)

//        nav_clear_translation_home.setOnClickListener(this)
//        nav_clear_conversation_home.setOnClickListener(this)
//        layout_nav_remove_ad.setOnClickListener(this)
        holder_translate.setOnClickListener(this)
        holder_fictional_lang.setOnClickListener(this)
        holder_camera.setOnClickListener(this)
        holder_conversation.setOnClickListener(this)
        holder_dictionary.setOnClickListener(this)
        holder_phrasebook.setOnClickListener(this)

        nav_translation_share.setOnClickListener(this)
        nav_caller_setting.setOnClickListener(this)
        nav_translation_history.setOnClickListener(this)
        nav_bookmark.setOnClickListener(this)
        nav_remove_ads.setOnClickListener(this)
        nav_saved_chat.setOnClickListener(this)
        nav_clip_board.setOnClickListener(this)
        nav_clip_auto.setOnClickListener(this)

        val isServiceOn = getPrefBool("is_clip_on")
        val isAutoClipBoard = getPrefBool("is_auto_clip")


        switch_clip_board.isChecked = isServiceOn
        switch_clip_board_auto.isChecked = isAutoClipBoard


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            nav_clip_board.visibility = View.GONE
        } else {
            if (isServiceOn && !isServiceRunning(this, ClipBoardDataManager::class.java)) {
                startClipboardService(this)
            }
        }


    }

    private fun toggleDrawer() {
        if (!drawer_home.isDrawerOpen(GravityCompat.START))
            drawer_home.openDrawer(GravityCompat.START)
        else
            drawer_home.closeDrawer(GravityCompat.START)
    }

    private fun inAppSubscription() {
        putPrefBoolean("is_feedback", true)
    }

    private fun showHistoryDialog(type: Int) {
        val message: String = if (type == 1)
            "Do you want to clear translation history"
        else
            "Do you want to clear conversation history"

        dialog.title_dialog.text = message

        dialog.btn_yes.setOnClickListener {
            if (type == 1) {
                clearHistory()
            } else {
                clearConversation()
            }
            dialog.dismiss()
        }
        dialog.btn_no.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun clearHistory() {
        database.translationTblDao().deleteAll()


    }

    private fun clearConversation() {
        database.conversationDao().deleteAll()

    }

    private fun animatePro() {
        if (isAdsOnly) {
            if (!isPremium()) {
                tv_pro_home.visibility = View.VISIBLE
                tv_pro_home_banner.visibility = View.VISIBLE
                Handler().postDelayed(
                    {
                        AnimUtils.zoomInOut(applicationContext, tv_pro_home)
                    }, 800
                )
            } else {
                tv_pro_home_banner.visibility = View.GONE
                nav_remove_ads.visibility = View.GONE
            }
        } else {
            tv_pro_home_banner.visibility = View.GONE
            nav_remove_ads.visibility = View.GONE
        }
    }

    fun checkAdsOnly() {
        if (isAdsOnly) {
            tv_pro_home_banner.visibility = View.VISIBLE
            clBottomSubscriptionAd.visibility = View.VISIBLE
            nav_remove_ads.visibility = View.VISIBLE
        } else {
            tv_pro_home_banner.visibility = View.GONE
            clBottomSubscriptionAd.visibility = View.GONE
            nav_remove_ads.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.navigation_home -> {

                    toggleDrawer()
                }

                R.id.tv_pro_home_banner -> {
                    if (isDoubleClick()) {
                        //startActivity(Intent(this, SubscriptionActivityN::class.java))
//                        startActivity(Intent(this, PurchaseActivity::class.java))

                        val intent = Intent(
                            this@HomeActivity,
                            PaywallUi::class.java
                        )
                        intent.putExtra(
                            PAYWALL_TYPE,
                            GPS_PREMIUM
                        )
                        startActivity(intent)

                        //initRemoveAdsDialog()
                        //removeAdsDialog.show()
                    }
//                    inAppSubscription()
                }

                R.id.nav_translation_history -> {

                    drawer_home.closeDrawer(GravityCompat.START)
                    if (isDoubleClick()) {
                        clickType = ClickType.HISTORY
                        showInterstitial {
                            openHistoryActivity(this, INTENT_KEY_SOURCE_HISTORY)
                        }
                    }

                }

                R.id.nav_bookmark -> {
                    drawer_home.closeDrawer(GravityCompat.START)
                    if (isDoubleClick()) {
                        clickType = ClickType.BOOKMARK
                        showInterstitial {
                            openBookMarkActivity()
                        }

                    }
                }

                R.id.nav_remove_ads -> {
                    drawer_home.closeDrawer(GravityCompat.START)
                    if (isDoubleClick()) {
//                        startActivity(Intent(this, PurchaseActivity::class.java))

                        val intent = Intent(
                            this@HomeActivity,
                            PaywallUi::class.java
                        )
                        intent.putExtra(
                            PAYWALL_TYPE,
                            GPS_DEFAULT
                        )
                        startActivity(intent)
                        // removeAdsDialog?.show()
                    }
                }

                R.id.nav_saved_chat -> {

                    drawer_home.closeDrawer(GravityCompat.START)
                    if (isDoubleClick()) {
                        showInterstitial {
                            startActivity(Intent(this, SavedChatActivity::class.java))
                            overridePendingTransition(
                                R.anim.transit_right_left,
                                R.anim.transit_none
                            )
                        }
                    }

                }

                R.id.nav_clip_board -> {
                    if (getPrefBool("is_clip_on")) {
                        putPrefBoolean("is_clip_on", false)
                        switch_clip_board.isChecked = false
                        stopClipBoard(this)
                    } else {
                        putPrefBoolean("is_clip_on", true)
                        switch_clip_board.isChecked = true
                        startClipboardService(this)
                    }

                }

                R.id.nav_clip_auto -> {
                    if (getPrefBool("is_auto_clip")) {
                        putPrefBoolean("is_auto_clip", false)
                        switch_clip_board_auto.isChecked = false
                    } else {
                        putPrefBoolean("is_auto_clip", true)
                        switch_clip_board_auto.isChecked = true
                    }

                }

                R.id.nav_translation_share -> {

                    drawer_home.closeDrawer(GravityCompat.START)
                    if (isDoubleClick())
                        callShare()

                }

                R.id.nav_caller_setting -> {

                    cdoSettingsActivity(this@HomeActivity)

                }
//                R.id.nav_clear_translation_home -> {
//                    drawer_home.closeDrawer(GravityCompat.START)
//                    if (isDoubleClick())
//                        showHistoryDialog(1)
//                }
//                R.id.nav_clear_conversation_home -> {
//                    drawer_home.closeDrawer(GravityCompat.START)
//                    if (isDoubleClick())
//                        showHistoryDialog(2)
//
//
//                }
//                R.id.layout_nav_remove_ad -> {
//                    drawer_home.closeDrawer(GravityCompat.START)
//                    if (isDoubleClick())
//                        openSubscriptionActivity("home")
////                    inAppSubscription()
//
//                }
                R.id.holder_translate -> {
                    if (isDoubleClick()) {
                        clickType = ClickType.TRANSLATION
                        showInterstitial {
                            //adsUtill.destroyBanner()
                            showBanner = false
                            openMainActivity(null, "activity")
                        }

                    }
                }

                R.id.holder_fictional_lang -> {
                    if (isDoubleClick()) {
                        clickType = ClickType.FICTIONAL
                        showInterstitial {
                            //        TODO()4.5.0  banner was being destroyed here
                            //adsUtill.destroyBanner()
                            showBanner = false
                            openFictionalActivity(null, "activity")
                        }

//                        val mainIntent = Intent(this, FictionalLangActivity::class.java)
//                        startActivity(mainIntent)
                    }
                }

                R.id.holder_phrasebook -> {
                    if (isDoubleClick()) {
                        clickType = ClickType.PHRASEBOOK
                        // openFictionalActivity(null, "activity")'
                        showInterstitial {
                            //        TODO()4.5.0  banner was being destroyed here
                            //adsUtill.destroyBanner()
                            showBanner = false
                            val mainIntent = Intent(this, PhrasebookActivity::class.java)
                            startActivity(mainIntent)
                        }
                    }
                }

                R.id.holder_camera -> {
                    if (isDoubleClick()) {
                        showInterstitial {
                            //        TODO()4.5.0  banner was being destroyed here
                            //adsUtill.destroyBanner()
                            showBanner = false
                            handleCameraActivity()
                        }

                    }

                }

                R.id.holder_conversation -> {
                    if (isDoubleClick()) {
                        clickType = ClickType.CONVERSATION
                        showInterstitial {
                            //        TODO()4.5.0  banner was being destroyed here
                            //adsUtill.destroyBanner()
                            showBanner = false
                            val intent = Intent(this, ConversationActivity::class.java)
                            intent.putExtra(
                                Constants.INTENT_KEY_CONVERSATION_ORIGIN,
                                Constants.CONVERSATION_ORIGIN_MAIN
                            )
                            startActivity(intent)
                            overridePendingTransition(
                                R.anim.transit_right_left,
                                R.anim.transit_none
                            )
                        }

//
                    }
                }

                R.id.holder_dictionary -> {
                    if (isDoubleClick()) {
                        clickType = ClickType.DICTIONARY

                        showInterstitial {
                            //        TODO()4.5.0  banner was being destroyed here
                            //adsUtill.destroyBanner()
                            showBanner = false
                            val intent = Intent(this, DictionaryActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(
                                R.anim.transit_right_left,
                                R.anim.transit_none
                            )
                        }
                    }

                }
            }
        }
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (drawer_home.isDrawerOpen(GravityCompat.START)) {
            drawer_home.closeDrawer(GravityCompat.START)
        } else {
            if (getPrefBool("is_exit_rating")) {
//                if (doubleBackToExitPressedOnce) {
//                    super.onBackPressed()
//                    return
//                }
//                this.doubleBackToExitPressedOnce = true
//                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
//                Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)

                showExitDialog()
            } else {
//                showExit()
                showExitDialog()
            }
        }
    }

    private fun showExitDialog() {
        val exitDialog = AlertDialog.Builder(this)
        exitDialog.setTitle("Exit App")
        exitDialog.setMessage("Are you sure you want to exit?")

        // Set up the "Cancel" button
        exitDialog.setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        // Set up the "Exit" button
        exitDialog.setPositiveButton("Exit") { _: DialogInterface, _: Int ->
            // Exit the app
            finishAffinity() // This will close all activities and exit the app
        }

        // Create and show the dialog
        exitDialog.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQ_CODE_BOOKMARK) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")

                val bundle = data.extras!!
                val model: TranslationTable = bundle.getParcelable("detail_object")!!
                openMainActivity(model, "activity")
            }
        } else if (requestCode == Constants.REQ_CODE_HISTORY) {
            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")

                val bundle = data.extras!!
                val model: TranslationTable = bundle.getParcelable("detail_object")!!
                openMainActivity(model, "activity")
            }
        } else if (requestCode == Constants.REQUEST_CODE_OCR_BACK) {
            if (resultCode != Activity.RESULT_CANCELED) {
                if (data != null) {
                    val origin = data.getStringExtra("origin")

                    val bundle = data.extras
                    if (bundle != null) {
                        val model: TranslationTable = bundle.getParcelable("detail_object")!!
                        if (model != null) {
                            openMainActivity(model, "activity")
                        }
                    }
                }
            }
        } else if (requestCode == Constants.REQ_CODE_RATEUS) {
            if (resultCode != Activity.RESULT_CANCELED) {
                /* val config = "Use"//getRemoteConfig().getString("GPS126_rate_us_placement")
                 if (!TinyDB.getInstance(this@HomeActivity).getBoolean(RATEUS_FIRST_COMPLETE)) {
                     if (config == "Use" && (application as AppBase).isFirstTimeOnMainMenu) {
                         RateUs.showDialog(this@HomeActivity, object : RateUs.CallBackClick {
                             override fun onPositive() {

                             }

                             override fun onNegitive() {

                             }
                         })
                         (application as AppBase).isFirstTimeOnMainMenu = false
                     }
                 }*/

            }
        }
    }

    override fun slideAdLoaded() {
    }

    override fun slideAddFailed() {
        // ad_choices_container.stopAutoCycle()
        // ad_choices_container.visibility = View.GONE
        // iv_no_ad_home.visibility = View.VISIBLE
    }


    override fun onResume() {
        super.onResume()

        if (MaxAppOpenAdManager.Companion.IS_OVERLAY_PERMISSION && overlayPermissionManager!!.isGranted) {
            Handler(Looper.getMainLooper()).postDelayed({
                MaxAppOpenAdManager.Companion.IS_OVERLAY_PERMISSION = false
                TinyDB.getInstance(this).putBoolean(IS_FIRST_TIME, true)
            }, 5000)
        }
        (application as AppBase).thisActivity = false
        if (isPremium()) {
            hideViews()
            iv_no_ad_home.visibility = View.VISIBLE
        } else {
            native_banner_container_home_screen.visibility = View.VISIBLE
//            if (showBanner) {
//                loadBannerAd()
//            }
            initNative()
        }

        if (!calldoradoInitialized) {
            calldoradoInitialized = true
            Log.e("calldorado", "acceptOptin")
            val conditions = Calldorado.getAcceptedConditions(this)
            if (conditions.containsKey(Calldorado.Condition.EULA)) {
                if (conditions[Calldorado.Condition.EULA] == null) {
                    val conMap = HashMap<Calldorado.Condition, Boolean>()
                    conMap[Calldorado.Condition.EULA] = true
                    conMap[Calldorado.Condition.PRIVACY_POLICY] = true
                    Calldorado.acceptConditions(this, conMap)
                } else {

//                    if(!Preferences.getBoolean(this, Preferences.FirstLaunch)){
//                    onBoardingSuccess()
//                    }
                }
            } else {
//                if(!Preferences.getBoolean(this, Preferences.FirstLaunch)){
//                onBoardingSuccess()
//                }
            }

        }
//        if (clickType == ClickType.DICTIONARY || clickType == ClickType.CONVERSATION ||
//            clickType == ClickType.CAMERA || clickType == ClickType.TRANSLATION ||
//            clickType == ClickType.BOOKMARK || clickType == ClickType.HISTORY
//        ) {
//            if (!isPremium()) {
//                if (backClickCount >= interCounterBack!!) {
//                    backClickCount = 1
//
//
//                    IronSource.setInterstitialListener(getInterstitialListener({
//                        //onAddLoaded
//                        if (it)
//                            if((application as AppBase).interstitialSessionCount<maxInterstitialSession) {
//                                (application as AppBase).interstitialSessionCount++
//                                IronSource.showInterstitial()
//                            }
//
//                    }, {
//                        //onAdClosed
//
//                    }))
//                    IronSource.loadInterstitial();
//
//                    /*backPressAdPair?.let {
//                        if (it.isLoaded()) {
//                            it.showAd(this)
//                        }
//                    }*/
//
//                } else {
//                    backClickCount++
//                }
//            }
//
//
//        }

        clickType = ClickType.NONE

        val config = getRemoteConfig().getString("GPS126_rate_us_placement")
        if (!TinyDB.getInstance(this@HomeActivity).getBoolean(RATEUS_FIRST_COMPLETE)) {

            if (config == "Use" && (application as AppBase).isOnMainMenu && (application as AppBase).isFirstTimeOnMainMenu && (TinyDB.getInstance(
                    this@HomeActivity
                )
                    .getInt(RATEUS_FIRST_TIME) % 5 == 1)
            ) {
                (application as AppBase).isFirstTimeOnMainMenu = false
                RateUs.showDialog(this@HomeActivity, object : RateUs.CallBackClick {
                    override fun onPositive() {

                    }

                    override fun onNegitive() {

                    }

                    override fun onload() {

                    }
                })
                (application as AppBase).isOnMainMenu = false
            }
        }


        if (isReadPhoneStatePermissionGranted() && overlayPermissionManager!!.isGranted) {
            TinyDB.getInstance(this@HomeActivity).putBoolean(
                com.translate.languagetranslator.freetranslation.utils.Constants.IS_CONSENT,
                true
            )
            checkPermissions()
        }
    }


    private fun checkForSubscriptionScreen() {
        val mills = Calendar.getInstance().timeInMillis - getPrefLong(
            "subscription_time",
            Calendar.getInstance().timeInMillis
        )
        val timerCheck = 86400000
//        val timerCheck = 120000

        if (mills >= timerCheck) {
            if (!getPrefBool("is_remove_dialog_show")) {
                putPrefBoolean("is_remove_dialog_show", true)
                removeAdsDialog.show()
                putPrefLong("subscription_time", Calendar.getInstance().timeInMillis)

            }
        }

    }

    override fun onDestroy() {
//        if (AdUtility.countDownTimer != null) {
//            AdUtility.countDownTimer.cancel()
//
//        }
        super.onDestroy()

    }


    private fun isDoubleClick(): Boolean {
        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    private fun handleCameraActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!cameraPermissionAlreadyGranted(this)) {
                goToCameraPermission()
            } else {
                startCameraActivity()

            }

        } else {
            startCameraActivity()
        }
    }

    private fun startCameraActivity() {
        clickType = ClickType.CAMERA
//        showToast(this,"Working new ocr screen")
//        openOcrActivity(this)
        openCameraActivity(this)
    }

    private fun goToCameraPermission() {
        putPrefBoolean("app_killed", true)


        Permissions.check(
            this/*context*/,
            Manifest.permission.CAMERA,
            null,
            object : PermissionHandler() {
                override fun onGranted() {


                    startCameraActivity()

                    // do your task.
                }

                override fun onDenied(
                    context: Context,
                    deniedPermissions: java.util.ArrayList<String>
                ) {
                    super.onDenied(context, deniedPermissions)
                    callHandlerForPref()

                }
            })
    }

    override fun onPause() {
        super.onPause()
    }

    fun checkRemoteAds() {
        mFireBaseRemoteConfig.getBoolean("ads")
    }

    private fun loadBannerAd() {
        if (!mainActivityViewModel.isAutoAdsRemoved()) {
            try {
                MaxAdManager.createBannerAd(this, native_banner_container_home_screen)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }


    private fun checkPermissions() {

        if (!TinyDB.getInstance(this@HomeActivity)
                .getBoolean(com.translate.languagetranslator.freetranslation.utils.Constants.IS_CONSENT) &&
            isPermissionScreen) {
            permission_layout.visibility = View.VISIBLE
            home_layout.visibility = View.GONE

        } else {
            permission_layout.visibility = View.GONE
            home_layout.visibility = View.VISIBLE
            if (isFirstTimeAfterOverlayPermission) {
                isFirstTimeAfterOverlayPermission = false
                ConsentManger.lookUpForAdsConsentForm(this@HomeActivity)
            }
        }

        btn_continue.setOnClickListener {
            cdoConditions()
        }


        if (permission_layout.visibility == View.GONE) {
            home_layout.visibility = View.VISIBLE
            if (isPermissionScreen) {
                onBoardingSuccess()
            }
            if (!overlayPermissionManager!!.isGranted) {
                if (isPermissionScreen) {
                    overlayPermissionManager!!.requestOverlay()
                }
            }
        }


        termsAndPolices()


    }

    private fun requestCdoPermissions() {
        val permissionList = ArrayList<String>()
        //Essential for Calldorado to work -One request (Phone)
        permissionList.add(Manifest.permission.READ_PHONE_STATE)
        // Optimal (Phone)
        permissionList.add(Manifest.permission.CALL_PHONE)
        permissionList.add(Manifest.permission.ANSWER_PHONE_CALLS)
        // Optimal -Needed to read phone number on Pie+ devices. Requires permission from Google to use from early 2019
//        permissionList.add(Manifest.permission.READ_CALL_LOG)
        // TODO this permission needs permission from Google to use (no permission -> exclude)
        ActivityCompat.requestPermissions(
            this,
            permissionList.toTypedArray(),
            permissionRequestCode
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestCode) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!overlayPermissionManager!!.isGranted) {
                    overlayPermissionManager!!.requestOverlay()
                }

                isPermissionGranted = "granted"
//                permission_layout.visibility = View.GONE
//                home_layout.visibility = View.VISIBLE
            } else {
                isPermissionGranted = "notGranted"
            }
        }
    }


//    private fun requestCdoPermissions() {
//        // List of permissions you want to check
//        val permissionList: MutableList<String> = ArrayList()
//        permissionList.add(Manifest.permission.READ_PHONE_STATE)
////        permissionList.add(Manifest.permission.CALL_PHONE)
////        permissionList.add(Manifest.permission.ANSWER_PHONE_CALLS)
//        permissionList.add(Manifest.permission.READ_CALL_LOG)
//
//// Check each permission in the list
//        for (permission in permissionList) {
//            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
//            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//                // Permission is granted
//            } else {
//                // Permission is not granted
//                isPermissionGranted = false
//            }
//        }
//
//        if (!isPermissionGranted){
//            startActivity(Intent(this@HomeActivity, PermissionActivity::class.java))
//        }
//    }


    private fun cdoConditions() {
        eulaAccepted()

        val cdoConditions = Calldorado.getAcceptedConditions(this)
        if (cdoConditions.containsKey(Calldorado.Condition.EULA)) {
            if (cdoConditions[Calldorado.Condition.EULA]!!) {
                // TODO User has already accepted conditions. No need for you to show Optin
                if (isPermissionScreen) {
                    onBoardingSuccess()
                }
                Log.e("calldorado", "EULAAcceptAleady")
//                requestCdoPermissions()
            } else {
            }
            // TODO show your means of boarding the user
            if (calldoradoInit) {
                calldoradoInit = false
                // Step 2:
                Log.e("calldorado", "EULAAccept")
                val conditionsMap = HashMap<Calldorado.Condition, Boolean>()
                conditionsMap[Calldorado.Condition.EULA] = true
                conditionsMap[Calldorado.Condition.PRIVACY_POLICY] = true
                Calldorado.acceptConditions(this, conditionsMap)
                // Step 5
                if (isPermissionScreen) {
                    onBoardingSuccess()
                }
//                requestCdoPermissions()
            }
        }
    }

    private fun onBoardingSuccess() {
        // Step 3:
//        Calldorado.start(this)
        // Step 4:

        if (isReadPhoneStatePermissionGranted()) {
            if (!overlayPermissionManager!!.isGranted) {
                overlayPermissionManager!!.requestOverlay()
            }
        } else {

            if (isPermissionGranted.contains("granted")) {
                if (!overlayPermissionManager!!.isGranted) {
                    overlayPermissionManager!!.requestOverlay()
                }
            } else if (isPermissionGranted.contains("notGranted")) {
                requestPermission()
            } else {
                requestCdoPermissions()
            }
        }

        if (isReadPhoneStatePermissionGranted() && overlayPermissionManager!!.isGranted) {
            permission_layout.visibility = View.GONE
            home_layout.visibility = View.VISIBLE
        }
    }

    private fun eulaAccepted() {
        val conditionsMap = HashMap<Calldorado.Condition, Boolean>()
        conditionsMap[Calldorado.Condition.EULA] = true
        conditionsMap[Calldorado.Condition.PRIVACY_POLICY] = true
        Calldorado.acceptConditions(this, conditionsMap)
    }


    //calldorado setting
    fun cdoSettingsActivity(mActivity: Activity?) {

        Calldorado.createSettingsActivity(mActivity ?: this)
    }


    fun termsAndPolices() {
        val termsOfUse = "terms of use"
        val privacyPolicy = "privacy policy"
        val fullText =
            "By continuing, you accept and approve the $termsOfUse and the $privacyPolicy."

        val spannableString = SpannableString(fullText)

        val termsOfUseClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                openUrl("https://zedlatino.info/TermsOfUse.html")
            }
        }

        val privacyPolicyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                // Handle Privacy Policy click
                openUrl("https://zedlatino.info/privacy-policy-apps.html")
            }
        }

        val termsOfUseStart = fullText.indexOf(termsOfUse)
        val privacyPolicyStart = fullText.indexOf(privacyPolicy)

        spannableString.setSpan(
            termsOfUseClickableSpan,
            termsOfUseStart,
            termsOfUseStart + termsOfUse.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            privacyPolicyClickableSpan,
            privacyPolicyStart,
            privacyPolicyStart + privacyPolicy.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply black color to the clickable spans
        spannableString.setSpan(
            ForegroundColorSpan(Color.BLACK),
            termsOfUseStart,
            termsOfUseStart + termsOfUse.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.BLACK),
            privacyPolicyStart,
            privacyPolicyStart + privacyPolicy.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        terms_policy.text = spannableString
        terms_policy.movementMethod = LinkMovementMethod.getInstance()


        policies.setOnClickListener {
            openUrl("https://zedlatino.info/privacy-policy-apps.html")
        }

        terms.setOnClickListener {
            openUrl("https://zedlatino.info/TermsOfUse.html")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    @SuppressLint("NewApi")
    private fun requestPermission() {
        // Check if the permission is granted
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
            // User has denied permission previously, show a rationale and request again
            // ...
            openAppSettings()
        } else {
            // Permission has been denied and "Don't ask again" is checked
            // Direct the user to the app's settings to manually enable the permission
            openAppSettings()
        }
    }

    private fun isReadPhoneStatePermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, permissionRequestCode)
    }


}
