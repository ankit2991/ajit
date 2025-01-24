@file:Suppress("DEPRECATION")

package com.translate.languagetranslator.freetranslation.activities.fictionalLanguage

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.code4rox.adsmanager.MaxAdManager

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.FullScreenActivity
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.SubscriptionActivityN
import com.translate.languagetranslator.freetranslation.activities.adptypaywall.PaywallUi
import com.translate.languagetranslator.freetranslation.activities.conversation.ConversationActivity
import com.translate.languagetranslator.freetranslation.activities.conversation.SavedChatActivity
import com.translate.languagetranslator.freetranslation.activities.dictionary.DictionaryActivity
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.activities.mainScreen.adapter.MainHistoryAdapter
import com.translate.languagetranslator.freetranslation.activities.mainScreen.viewModel.MainViewModel
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.appUtils.Constants.*
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable

import com.translate.languagetranslator.freetranslation.interfaces.SlideAdInterface
import com.translate.languagetranslator.freetranslation.interfaces.TranslationHistoryInterface
import com.translate.languagetranslator.freetranslation.network.TranslationUtils
import com.translate.languagetranslator.freetranslation.utils.AdsUtill
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_dictionary.native_banner_container_home_screen
import kotlinx.android.synthetic.main.activity_fictional_lang.*
import kotlinx.android.synthetic.main.activity_fictional_lang.et_input_word
import kotlinx.android.synthetic.main.activity_fictional_lang.input_layout_lang_to
import kotlinx.android.synthetic.main.activity_fictional_lang.iv_clear_input
import kotlinx.android.synthetic.main.activity_fictional_lang.iv_ocr
import kotlinx.android.synthetic.main.activity_fictional_lang.iv_speak_translated
import kotlinx.android.synthetic.main.activity_fictional_lang.iv_speak_word_input
import kotlinx.android.synthetic.main.activity_fictional_lang.iv_toggle_favorite
import kotlinx.android.synthetic.main.activity_fictional_lang.tv_lang_to_input
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.appbar_home.*
import kotlinx.android.synthetic.main.dlg_clear_history.*
import kotlinx.android.synthetic.main.layout_input_area.*
import kotlinx.android.synthetic.main.layout_nav_drawer.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class FictionalLangActivity : AppCompatActivity(), View.OnClickListener,
    TranslationHistoryInterface,
    SlideAdInterface, TextToSpeech.OnInitListener {


    val viewModel: MainViewModel by viewModel()
    private lateinit var fromLang: String
    private lateinit var fromLangCode: String
    private lateinit var fromLangCodeSupport: String
    private lateinit var toLang: String
    private lateinit var toLangCode: String
    private lateinit var toLangCodeSupport: String
    private lateinit var toLangMeaning: String
    private lateinit var fromLangMeaning: String
    private lateinit var historyAdapter: MainHistoryAdapter
    private lateinit var database: TranslationDb
    private lateinit var dialog: Dialog
    private var homeButtonCount: Int = 0
    private var clickType = ClickType.NONE
    private var maxInterstitialSession: Int = 3

    var url: String = ""
    var apiMethod: String = ""
    var hostApi: String = ""
    var sunSign = "Aries"
    var resultView: TextView? = null

    private var readyToPurchase = false
    var translationUtils: TranslationUtils? = null


    //    private var adAdapter: SliderAdAdapter? = null
    private lateinit var mFireBaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var configSettings: FirebaseRemoteConfigSettings
    private var mLastClickTime: Long = 0
    private lateinit var removeAdsDialog: Dialog
    private lateinit var rateUsDialog: Dialog
    private var isRemoveAdShownTwice = false
    private var removeAdCount = 0

    // new AdsPlaceMent
    private var isNativeShow = false
    private var nativePrioritySliderOne: String? = null
    private var nativePrioritySliderTwo: String? = null
    private var isSpeakAble = false
    private var isDetailVisible = false
    private var isHistoryAvailable = false
    private var isFavorite = false
    private var historyItems: List<TranslationTable> = ArrayList()
    private var newTranslationModel: TranslationTable? = null
    private var mTts: TextToSpeech? = null
    private var isSpeaking = false
    private var clipboardService: Any? = null
    private var clipboardManager: ClipboardManager? = null

    private lateinit var layoutOutputTranslation: ConstraintLayout
    private var isTranslateInterAdShow = false
    private var translateInterAdPriority: String? = null
    private var translateInterAdCount: Int = 1
    private var localTranslateCount = 1

    //private var translatinInterAdPair: InterAdPair? = null
    private var controlledFrom: String? = null

    private var mCM: ClipboardManager? = null
    private var mOldClip: String? = null
    private var copiedWord: String? = null

    /*var interstitialAd: Any? = null
    var backInterstitialAd: Any? = null*/
    var showInterstitialAd = false
    var showBackInterstitialAd = false
    private var isAdsOnly = true
    private lateinit var adsUtill: AdsUtill

    enum class ClickType {
        NONE, HOME
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fictional_lang)

        et_input_word.isFocusable = true
        et_input_word.requestFocus()

        layoutOutputTranslation = findViewById(R.id.layout_output_translate)

        Logging.adjustEvent("rloihg", Logging.currentTime(), "FictionalLanguage")


        adsUtill = AdsUtill(this)
        database = TranslationDb.getInstance(this)

        initRemoveAdsDialog()

        initRemoteConfig()
        (application as AppBase).isOnMainMenu = true
        getSelectedLanguages()
        checkLangParam()
        checkAdsOnly()


        initRateUsDialog()
        (application as AppBase).thisActivity = true;


        viewModel.translate_top_native().observe(this, androidx.lifecycle.Observer({
            if (it.show && !viewModel.isAutoAdsRemoved()) {

                MaxAdManager.createNativeAd(this, native_container, iv_no_ad_fictional)


//                getNativeAdObject(
//                    getString(R.string.am_native_main_top),
//                    getString(R.string.fb_native_main_top),
//                    it.priority,
//                    native_container
//                ) {
//                    it?.let {
//                        if (it is NativeAd) {
//                            inflateAdMobNAtiveAd(it)
//                        } /*else if (it is NativeAd) {//Removed facebook native ads
//                            inflateFbNAtiveAd(it)
//                        }*/
//                    }
//                }
            }
        }))
        viewModel.translate_back_interstitial().observe(this, androidx.lifecycle.Observer {
            showBackInterstitialAd = it.show && !viewModel.isAutoAdsRemoved()
            //TODO()4.5.0   and was loading here to maybeshow later
        })
        viewModel.translate_button_interstitial().observe(this, androidx.lifecycle.Observer {

            translateInterAdCount = it.counter
            showBackInterstitialAd = it.show
            //TODO()4.5.0 and ad was beading load here maybe to showing here

        })

        setClickListeners()
        animatePro()
//        setHistoryData()
        createDialogBuilder()
        setInputChangeListener()
        initClipBoard()
//        initBundleData()
        mCM = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        homeButtonCount = getPrefInt(HOME_BUTTON_COUNT)

    }


    /*private fun inflateFbNAtiveAd(fbNAtiveAd: NativeAd) {
        val adTitle = tvAdTitle_Fb
        val adIcon = ivAdIcon_Fb
        val callToAction = btnAdCallToAction_Fb

        val adOptions = AdOptionsView(this, fbNAtiveAd, topFbNative)
        val ad_choices_container1 = ad_choices_containerFb
        ad_choices_container1.removeAllViews()
        ad_choices_container1.addView(adOptions, 0)

        adTitle.text = fbNAtiveAd.advertiserName


        *//*if (fbNAtiveAd.adIcon == null)
            adIcon.visibility = View.GONE
        else {
            adIcon.visibility = View.VISIBLE
        }*//*
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
            topFbNative,
            ivAdIcon_Fb,
            adIcon,
            clickableViews
        )
        topFbNative.visibility = View.VISIBLE
        topAdMobNative.visibility = View.GONE
    }*/

    /*
    private fun inflateAdMobNAtiveAd(it: NativeAd) {

        val view = layoutInflater.inflate(R.layout.small_native, null, false) as NativeAdView
        if (it.headline == null) {
            view.findViewById<TextView>(R.id.tvAdTitle_IT).visibility = View.GONE
        } else {
            view.findViewById<TextView>(R.id.tvAdTitle_IT).text = it.headline
        }
        if (it.icon == null)
            view.findViewById<ImageView>(R.id.ivAdIcon_IT).visibility = View.GONE
        else {
            view.findViewById<ImageView>(R.id.ivAdIcon_IT).visibility = View.VISIBLE
            view.findViewById<ImageView>(R.id.ivAdIcon_IT).setImageDrawable(it.icon!!.drawable)
        }
        if (it.callToAction == null)
            view.findViewById<Button>(R.id.btnAdCallToAction_IT).visibility = View.GONE
        else {
            view.findViewById<Button>(R.id.btnAdCallToAction_IT).visibility = View.VISIBLE
            view.findViewById<Button>(R.id.btnAdCallToAction_IT).text = it.callToAction
            view.callToActionView = view.findViewById<Button>(R.id.btnAdCallToAction_IT)
        }
        view.setNativeAd(it)
        native_container.addView(view)
        native_container.visibility = View.VISIBLE
//        tvAdTitle_IT.text = it.headline
//        if (it.icon == null)
//            ivAdIcon_IT.visibility = View.GONE
//        else {
//            ivAdIcon_IT.visibility = View.VISIBLE
//            ivAdIcon_IT.setImageDrawable(it.icon!!.drawable)
//        }
//        if (it.callToAction == null)
//            btnAdCallToAction_IT.visibility = View.GONE
//        else {
//            btnAdCallToAction_IT.visibility = View.VISIBLE
//            btnAdCallToAction_IT.text = it.callToAction
//            topAdMobNative.callToActionView = btnAdCallToAction_IT
//        }
//        topAdMobNative.setNativeAd(it)
//        topAdMobNative.visibility = View.VISIBLE
        topFbNative.visibility = View.GONE
    }
*/

    private fun initBundleData() {
        var bundle = Bundle()
        bundle = intent.extras!!
        val source = bundle.getString("opened")
        controlledFrom = bundle.getString("controlled")
        if (source == "with_data") {
            val model: TranslationTable = bundle.getParcelable("detail_object")!!
//            setResultInView(model)
        }
        if (bundle.getString("textFromDictionar") != null) {
            et_input_word.setText(bundle.getString("textFromDictionar"))
        }

    }

    /*private fun initAds() {
        when (val adPriority = getAdsPriority(translateInterAdPriority)) {
            AdsPriority.ADMOB, AdsPriority.ADMOB_FACEBOOK -> {
                loadInterAdmob(adPriority)
            }
            AdsPriority.FACEBOOK, AdsPriority.FACEBOOK_ADMOB -> {
                loadInterFacebook(adPriority)
            }
        }
    }*/

    /*private fun loadInterAdmob(adPriority: AdsPriority) {
        var admobA = AdmobUtils(this)
        var admobB = AdmobUtils(this)
        admobA = AdmobUtils(this, object : AdmobUtils.AdmobInterstitialListener {
            override fun onInterstitialAdClose() {

            }

            override fun onInterstitialAdLoaded() {
                translatinInterAdPair = InterAdPair(interAM = admobA.adMobInterAd)

            }

            override fun onInterstitialAdFailed() {
                admobB =
                    AdmobUtils(
                        this@MainActivity,
                        object : AdmobUtils.AdmobInterstitialListener {
                            override fun onInterstitialAdClose() {

                            }

                            override fun onInterstitialAdLoaded() {
                                translatinInterAdPair =
                                    InterAdPair(interAM = admobB.adMobInterAd)


                            }

                            override fun onInterstitialAdFailed() {
                                if (adPriority == AdsPriority.FACEBOOK_ADMOB || adPriority == AdsPriority.FACEBOOK) {
                                    loadInterFacebook(adPriority)
                                }

                            }

                            override fun onAdOpened() {

                            }
                        },
                        InterAdsIdType.TRANSLATION_INTER_AM_B,
                        true
                    )

            }

            override fun onAdOpened() {

            }
        }, InterAdsIdType.TRANSLATION_INTER_AM_A, true)


    }*/

    /* private fun loadInterFacebook(adPriority: AdsPriority) {
         var facebookAd = FacebookAdsUtils(this)
         facebookAd =
             FacebookAdsUtils(this, object : FacebookAdsUtils.FacebookInterstitialListner {
                 override fun onFbInterstitialAdClose() {

                 }

                 override fun onFbInterstitialAdLoaded() {
                     translatinInterAdPair =
                         InterAdPair(interFB = facebookAd.facebookInterstitialAd)


                 }

                 override fun onFbInterstitialAdFailed() {
                     if (adPriority == AdsPriority.ADMOB || adPriority == AdsPriority.ADMOB_FACEBOOK) {
                         loadInterAdmob(adPriority)
                     }

                 }
             }, InterAdsIdType.TRANSLATION_INTER_FB, true)


     }*/

    private fun initClipBoard() {
        clipboardService = getSystemService(CLIPBOARD_SERVICE)
        clipboardManager = clipboardService as ClipboardManager
    }

    private fun setInputChangeListener() {
        et_input_word.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s.toString().trim().isNotEmpty()) {
                    iv_clear_input.visibility = View.VISIBLE
                    iv_mic_input_main.visibility = View.GONE
                    iv_ocr.visibility = View.GONE
                    layout_input_search_action.visibility = View.VISIBLE

                } else {
                    iv_clear_input.visibility = View.GONE
                    iv_mic_input_main.visibility = View.VISIBLE
                    iv_ocr.visibility = View.VISIBLE
                    layout_input_search_action.visibility = View.GONE
                    if (isDetailVisible) {
//                        layout_output_translation_con.visibility = View.GONE
                        iv_speak_word_input.visibility = View.GONE
                    }
//                    if (isHistoryAvailable) {
//                        history_container_main.visibility = View.VISIBLE
//                    }
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun initRemoteConfig() {
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(200)
            .build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)
//        isNativeShow = mFireBaseRemoteConfig.getBoolean(RemoteConfigConstants.MAIN_NATIVE_SHOW)
//        nativePrioritySliderOne =
//            mFireBaseRemoteConfig.getString(RemoteConfigConstants.SLIDER_ONE_PRIORITY)
//        nativePrioritySliderTwo =
//            mFireBaseRemoteConfig.getString(RemoteConfigConstants.SLIDER_TWO_PRIORITY)
//        isTranslateInterAdShow =
//            mFireBaseRemoteConfig.getBoolean(RemoteConfigConstants.TRANSLATION_INTER_SHOW)
//        translateInterAdCount =
//            mFireBaseRemoteConfig.getDouble(RemoteConfigConstants.TRANSLATION_INTER_COUNT)
//                .toInt()
//        translateInterAdPriority =
//            mFireBaseRemoteConfig.getString(RemoteConfigConstants.TRANSLATION_INTER_PRIORITY)
        maxInterstitialSession =
            mFireBaseRemoteConfig.getDouble(RemoteConfigConstants.MAX_INTERSTITIAL_SESSION).toInt()
    }

    fun hideViews() {
        runOnUiThread {
            iv_crown_pro_banner.clearAnimation()
            iv_crown_pro_banner.visibility = View.GONE
        }
    }


    private fun createDialogBuilder() {
        dialog = getClearDataDialog(this)
        dialog.setOnKeyListener { dialog, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog?.dismiss()
            }
            false
        }
    }

    private fun setHistoryData() {
        val mLayoutManager = LinearLayoutManager(this)
        rv_history_main.layoutManager = mLayoutManager
        rv_history_main.isNestedScrollingEnabled = false
        ViewCompat.setNestedScrollingEnabled(rv_history_main, false)
        historyAdapter = MainHistoryAdapter(
        )
        historyAdapter.setAdapterListener(this)
        rv_history_main.adapter = historyAdapter
        viewModel.getMainScreenHistory {
            val historyData = it

            historyData.observe(this, androidx.lifecycle.Observer {
                isHistoryAvailable = true
                historyItems = it
                if (!isDetailVisible) {
                    historyAdapter.setData(historyItems)
//                    history_container_main.visibility = View.VISIBLE
                    if (historyItems.isNotEmpty()) {
                        tv_label_history.visibility = View.VISIBLE
                        iv_history_label.visibility = View.GONE


                    } else {
                        tv_label_history.visibility = View.GONE
                        iv_history_label.visibility = View.GONE

                    }

                    if (historyItems.size >= 10) {
                        btn_show_more.visibility = View.VISIBLE
                    } else {
                        btn_show_more.visibility = View.GONE
                    }
                }

            })
        }


    }

    private fun animatePro() {
        if (isAdsOnly) {
            if (!isPremium()) {
                iv_crown_pro.visibility = View.VISIBLE
                iv_crown_pro_banner.visibility = View.VISIBLE
                Handler().postDelayed(
                    { AnimUtils.zoomInOut(applicationContext, iv_crown_pro) },
                    600
                )

            } else {
                iv_crown_pro_banner.visibility = View.GONE
            }
        } else {
            iv_crown_pro_banner.visibility = View.GONE

        }
    }

    private fun getSelectedLanguages() {
        fromLang = getPrefString(LANG_FROM_FIC)
        if (fromLang == "") {
            fromLang = "Sith"
            putPrefString(LANG_FROM_FIC, fromLang)
        }
        fromLangCode = getPrefString(LANG_FROM_CODE_FIC)
        if (fromLangCode == "") {
            fromLangCode = "si_Th"
            putPrefString(LANG_FROM_CODE_FIC, fromLangCode)

        }

        fromLangCodeSupport = getPrefString(LANG_FROM_CODE_SUPPORT_FIC)
        if (fromLangCodeSupport == "") {
            fromLangCodeSupport = "sith"
            putPrefString(LANG_FROM_CODE_SUPPORT_FIC, fromLangCodeSupport)

        }

        fromLangMeaning = getPrefString(LANG_FROM_MEANING_FIC)
        if (fromLangMeaning == "") {
            fromLangMeaning = "Sith"
            putPrefString(LANG_FROM_MEANING_FIC, fromLangMeaning)
        }

        toLangMeaning = getPrefString(LANG_TO_MEANING_FIC)
        if (toLangMeaning == "") {
            toLangMeaning = "Shakespear"
            putPrefString(LANG_TO_MEANING_FIC, toLangMeaning)
        }

        toLang = getPrefString(LANG_TO_FIC)
        if (toLang == "") {
            toLang = "Shakespeare"
            putPrefString(LANG_TO_FIC, toLang)

        }

        toLangCode = getPrefString(LANG_TO_CODE_FIC)
        if (toLangCode == "") {
            toLangCode = "shake-Spear"
            putPrefString(LANG_TO_CODE_FIC, toLangCode)
        }
        toLangCodeSupport = getPrefString(LANG_TO_CODE_SUPPORT_FIC)

        if (toLangCodeSupport == "") {
            toLangCodeSupport = "shake"
            putPrefString(LANG_TO_CODE_SUPPORT_FIC, toLangCodeSupport)
        }
        setLanguages()
    }

    private fun setLanguages() {
//        tv_lang_from_input.text = fromLang
        tv_lang_to_input.text = toLang
    }


    private fun toggleNavDrawer() {
        if (!drawer_main.isDrawerOpen(GravityCompat.START))
            drawer_main.openDrawer(GravityCompat.START)
        else
            drawer_main.closeDrawer(GravityCompat.START)
    }

    private fun switchLang() {
        val fromLangTemp = fromLang
        val fromLangCodeTemp = fromLangCode
        val fromLangCodeSupportTemp = fromLangCodeSupport
        val fromLangMeaningTemp = fromLangMeaning

        val toLangTemp = toLang
        val toLangCodeTemp = toLangCode
        val toLangCodeSupportTemp = toLangCodeSupport
        val toLangMeaningTemp = toLangMeaning

        fromLang = toLangTemp
        fromLangCode = toLangCodeTemp
        fromLangCodeSupport = toLangCodeSupportTemp
        fromLangMeaning = toLangMeaningTemp

        toLang = fromLangTemp
        toLangCode = fromLangCodeTemp
        toLangCodeSupport = fromLangCodeSupportTemp
        toLangMeaning = fromLangMeaningTemp

        putPrefString(LANG_FROM_FIC, fromLang)
        putPrefString(LANG_FROM_CODE_FIC, fromLangCode)
        putPrefString(LANG_FROM_CODE_SUPPORT_FIC, fromLangCodeSupport)
        putPrefString(LANG_FROM_MEANING, fromLangMeaning)

        putPrefString(LANG_TO_FIC, toLang)
        putPrefString(LANG_TO_CODE_FIC, toLangCode)
        putPrefString(LANG_TO_CODE_SUPPORT_FIC, toLangCodeSupport)
        putPrefString(LANG_TO_MEANING_FIC, toLangMeaning)
        setLanguages()

    }

    private fun inAppSubscription() {


        startActivity(Intent(this, SubscriptionActivityN::class.java))

        /*     billingProcess?.let { process ->
                 if (process.isBillingProcessReady) {
                     skuDetailInApp?.let {
                         putPrefBoolean("is_feedback", true)

                         process.launchPurchaseFlow(it)
                     }
                 } else showToast(this, "Billing process not initialized. Please wait")
             }*/

//        } else

    }

    private fun showHistoryDialog(type: Int) {
        val message: String = if (type == 1)
            "Do you want to clear all translation?"
        else
            "Do you want to clear all conversation?"

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

    private fun setClickListeners() {
        iv_menu_drawer.setOnClickListener(this)


        iv_crown_pro_banner.setOnClickListener(this)
//        iv_switch_lang_input.setOnClickListener(this)
        iv_clear_input.setOnClickListener(this)
//        input_layout_lang_from.setOnClickListener(this)
        input_layout_lang_to.setOnClickListener(this)
        layout_input_search_action.setOnClickListener(this)
        iv_mic_input_main.setOnClickListener(this)
        iv_ocr.setOnClickListener(this)
        iv_toggle_favorite.setOnClickListener(this)
        iv_speak_word_input.setOnClickListener(this)
        iv_speak_translated.setOnClickListener(this)


        btn_camera_main.setOnClickListener(this)
        btn_conversation.setOnClickListener(this)
        btn_home_main.setOnClickListener(this)
        btn_dictionary_main.setOnClickListener(this)
        btn_cam_bottom_nav.setOnClickListener(this)

        btn_show_more.setOnClickListener(this)


//        nav_translation_history.setOnClickListener(this)
//        nav_translation_share.setOnClickListener(this)
//        nav_saved_chat.setOnClickListener(this)
//        nav_bookmark.setOnClickListener(this)
//        nav_remove_ads.setOnClickListener(this)
//        nav_clip_board.setOnClickListener(this)
//        nav_clip_auto.setOnClickListener(this)
//
//        iv_more_options.setOnClickListener(this)


//        switch_clip_board.isChecked = getPrefBool("is_clip_on")

//        switch_clip_board_auto.isChecked = getPrefBool("is_auto_clip")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            nav_clip_board.visibility = View.GONE;
//        }


    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_menu_drawer -> {
                    onBackPressed()
                    //toggleNavDrawer()
                }

                R.id.iv_crown_pro_banner -> {

                    if (isDoubleClick()) {

                        //removeAdsDialog.show()
//                        startActivity(Intent(this, PurchaseActivity::class.java))
                        val intent = Intent(
                            this@FictionalLangActivity,
                            PaywallUi::class.java
                        )
                        intent.putExtra(
                            PAYWALL_TYPE,
                            GPS_PREMIUM
                        )
                        startActivity(intent)
                    }
//                        inAppSubscription()
                    // inapp
                }
//                R.id.input_layout_lang_from -> {
//                    if (isDoubleClick())
//                      //  startLanguageActivity(this, LANG_FROM, LANG_TYPE_NORMAL)
//
//                    startLanguageActivityFromFictional(this, LANG_FROM_FIC,LANG_TYPE_NORMAL,"fictional")
//                }
                R.id.input_layout_lang_to -> {
                    if (isDoubleClick())
//                        startLanguageActivity(this, LANG_TO, LANG_TYPE_NORMAL)
                        startLanguageActivityFromFictional(
                            this,
                            LANG_TO_FIC,
                            LANG_TYPE_NORMAL,
                            "fictional"
                        )

                }

                R.id.btn_show_more -> {
                    if (isDoubleClick())
                        openHistoryActivity(this, INTENT_KEY_SOURCE_HISTORY)

                }

                R.id.btn_camera_main -> {
                    if (isDoubleClick())
                        handleCameraActivity()
//                        startActivity(Intent(this, OcrCaptureActivity::class.java))
                }

                R.id.iv_ocr -> {
                    if (isDoubleClick())
                        handleCameraActivity()
                }

                R.id.btn_cam_bottom_nav -> {
                    if (isDoubleClick())
                        handleCameraActivity()
                }

                R.id.iv_mic_input_main -> {
                    initMicProcess()
                }

                R.id.btn_conversation -> {
                    if (isDoubleClick()) {
                        val intent = Intent(this, ConversationActivity::class.java)
                        intent.putExtra(
                            INTENT_KEY_CONVERSATION_ORIGIN,
                            CONVERSATION_ORIGIN_MAIN
                        )
                        startActivity(intent)
                        overridePendingTransition(
                            R.anim.transit_right_left,
                            R.anim.transit_none
                        )

                    }

                }

                R.id.btn_home_main -> {
                    if (isDoubleClick()) {
                        openHistoryActivity(this, INTENT_KEY_SOURCE_HISTORY)
                    }
                }

                R.id.btn_dictionary_main -> {
                    if (isDoubleClick()) {
                        startActivity(Intent(this, DictionaryActivity::class.java))
                        overridePendingTransition(
                            R.anim.transit_right_left,
                            R.anim.transit_none
                        )
                    }

                }
//                R.id.iv_switch_lang_input -> {
//
//                    switchLang()
//                }
                R.id.iv_clear_input -> {
                    clearInputArea()
                }

                R.id.layout_input_search_action -> {
                    showInterstitial {
                        if (NetworkUtils.isNetworkConnected(this)) {
                            progress_main.visibility = View.VISIBLE
                            GlobalScope.async {
                                getPredictions()
                            }
                        } else {
                            showToast(this, "Turn on Internet Connection")
                        }
                    }
                }

                R.id.nav_bookmark -> {
                    drawer_main.closeDrawer(GravityCompat.START)
                    if (isDoubleClick())
                        openBookMarkActivity()
                }

                R.id.nav_remove_ads -> {
                    drawer_main.closeDrawer(GravityCompat.START)
                    if (isDoubleClick())
                        removeAdsDialog?.show()
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

                R.id.nav_saved_chat -> {
                    drawer_main.closeDrawer(GravityCompat.START)

                    startActivity(Intent(this, SavedChatActivity::class.java))
                    overridePendingTransition(R.anim.transit_right_left, R.anim.transit_none)

                }

                R.id.nav_translation_share -> {
                    drawer_main.closeDrawer(GravityCompat.START)
                    if (isDoubleClick())
                        callShare()
                }

                R.id.iv_toggle_favorite -> {
                    toggleFavorite()
                }

                R.id.iv_speak_word_input -> {
                    speakInputWord()
                }

                R.id.iv_speak_translated -> {
                    speakTranslatedWord()
                }

                R.id.iv_more_options -> {
                    showPopupWindow(v)
                }

            }
        }
    }

    private fun speakTranslatedWord() {
//        newTranslationModel?.let { model ->
//            val word = model.outputStr
        val word = tv_translated_word.text.toString()
        speakWord(fromLangCodeSupport, word)
//        }
    }

    private fun speakInputWord() {
//        newTranslationModel?.let { model ->

        val inputWord = et_input_word.text.toString()
        speakWord(toLangCodeSupport, inputWord)
//        }

    }

    private fun speakWord(langCode: String, speakingWord: String) {
        val langLocal = viewModel.getLangLocale(langCode)
        mTts?.language = langLocal
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        @Suppress("DEPRECATION")
        mTts?.speak(speakingWord, TextToSpeech.QUEUE_FLUSH, map)
    }

    private fun toggleFavorite() {
        isFavorite = if (isFavorite) {
            iv_toggle_favorite.setImageResource(R.drawable.ic_un_favorite)
            false
        } else {
            iv_toggle_favorite.setImageResource(R.drawable.ic_favorite)
            true

        }
        if (isFavorite) {
            showToast(this, "Added to bookmarks")
        } else {
            showToast(this, "Removed from bookmarks")

        }

        viewModel.setFavorite(isFavorite, newTranslationModel!!.unique_iden)

    }

    private fun initMicProcess() {
        if (getMicVisibility(fromLangCodeSupport)) {
            initMicInput(fromLangCode)
        } else {
            showToast(this, "Language does not support voice to text")
        }
    }

    private fun initMicInput(fromLangCode: String) {

//        if (viewModel.isAppInstalled(this, "com.google.android.googlequicksearchbox")) {
        val micIntent = viewModel.getSpeechRecognitionIntent(fromLangCode)
        try {
            putPrefBoolean("app_killed", true)
            startActivityForResult(micIntent, Constants.REQ_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(this, resources.getString(R.string.stt_error_device))
        }
//        } else {
//            showToast(this, resources.getString(R.string.package_install))
//        }
//        if (viewModel.isAppInstalled(this, "com.google.android.googlequicksearchbox")) {
//            val recognitionIntent = viewModel.getSpeechRecognitionIntent(fromLangCode)
//            kotlin.runCatching {
//                putPrefBoolean("app_killed", true)
//                startActivityForResult(recognitionIntent, REQ_CODE_SPEECH_INPUT)
//
//
//            }
//
//        } else {
//
//            showToast(this, resources.getString(R.string.package_install))
//
//        }

    }

    override fun onBackPressed() {
        if (!isPremium()) {
            showInterstitial {
                if (et_input_word.text?.isNotEmpty() == true) {
                    clearInputArea()
                } else {
                    //        TODO()4.5.0  banner was being destroyed here

                    //adsUtill.destroyBanner()
                    HomeActivity.showBanner = true
                    super.onBackPressed()
                    setResult(REQ_CODE_RATEUS, Intent())
                }
            }

        } else {
            super.onBackPressed()
            setResult(REQ_CODE_RATEUS, Intent())
        }
    }

    private fun exitScreen() {
        if ((controlledFrom ?: "") == "activity") {
            try {
                super.onBackPressed()
            } catch (e: Exception) {
            }
        } else {
            val intent = Intent(applicationContext, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun translateInputWord() {
        try {
            if (NetworkUtils.isNetworkConnected(this)) {
                if (isDetailVisible) {
//                    layout_output_translation_con.visibility = View.GONE
                    iv_speak_word_input.visibility = View.GONE
                }
                val textToTranslate = et_input_word.text.toString().trim()
                hideSoftKeyboard()
                if (translationUtils != null) {
                    translationUtils?.StopBackground()
                }
                if (isHistoryAvailable) {
                    history_container_main.visibility = View.GONE
                }

                progress_main.visibility = View.VISIBLE

                translationUtils = TranslationUtils(object : TranslationUtils.ResultCallBack {
                    override fun onReceiveResult(result: String?) {
                        result?.let { response ->
                            insertHistory(textToTranslate, response)
                        } ?: run {
                            handleErrorResponse("Network error, please check the network and try again")
                        }
                    }

                    override fun onFailedResult() {
                        handleErrorResponse("Network error, please check the network and try again")
                    }

                }, textToTranslate, fromLangCodeSupport, toLangCodeSupport)
                translationUtils?.execute()

            } else {
                showToast(this, "Turn on Internet Connection")

            }
        } catch (e: UninitializedPropertyAccessException) {
        }
    }

    private fun insertHistory(inputWord: String, translatedWord: String) {

        val uniqueId = fromLangCodeSupport + inputWord + toLangCodeSupport
        isFavorite = viewModel.getFavorite(uniqueId)

        val model = TranslationTable(
            fromLangCodeSupport,
            toLangCodeSupport,
            inputWord,
            translatedWord,
            fromLangCode,
            toLangCode, uniqueId, isFavorite
        )
        progress_main.visibility = View.GONE
        newTranslationModel = model

        viewModel.insertHistoryTranslation(newTranslationModel!!)
        setTranslation(translatedWord)

    }

    private fun setTranslation(response: String) {
        tv_translated_word.text = response
        tv_lang_translated_word.text = "$toLang ( $toLangMeaning )"
        isSpeakAble = isSpeakerVisible(toLangCodeSupport)
        if (isSpeakerVisible(fromLangCodeSupport)) {
            iv_speak_word_input.visibility = View.VISIBLE
        } else {
            iv_speak_word_input.visibility = View.GONE
        }
        if (isSpeakerVisible(toLangCodeSupport)) {
            iv_speak_translated.visibility = View.VISIBLE
        } else {
            iv_speak_translated.visibility = View.GONE
        }
        layoutOutputTranslation.visibility = View.VISIBLE
        isDetailVisible = true
        if (isHistoryAvailable) {
            history_container_main.visibility = View.GONE
        }
        if (isFavorite) {
            iv_toggle_favorite.setImageResource(R.drawable.ic_favorite)
        } else {
            iv_toggle_favorite.setImageResource(R.drawable.ic_un_favorite)
        }
        svMain.post {
            svMain.fullScroll(View.FOCUS_DOWN)
        }

    }

    private fun handleErrorResponse(message: String) {
        progress_main.visibility = View.GONE
        showToast(this, message)
    }

    private fun hideSoftKeyboard() {
        (Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE)) as InputMethodManager).hideSoftInputFromWindow(
            et_input_word.applicationWindowToken,
            0
        )
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        et_input_word.clearFocus()
    }

    private fun setEditTextView(inputWord: String) {
        AnimUtils.animateLeft(this, iv_mic_arrow)
        et_input_word.setText(inputWord)
    }

    private fun clearInputArea() {
        et_input_word.setText("")
        layoutOutputTranslation.visibility = View.GONE
        iv_speak_word_input.visibility = View.GONE
//        isDetailVisible = false
//        if (isHistoryAvailable) {
////            presentHistory()
//        }
        if (isSpeaking) {
            mTts?.stop()
            isSpeaking = false
        }

    }

    private fun presentHistory() {
        history_container_main.visibility = View.VISIBLE

        historyAdapter.setData(historyItems)

        if (historyItems.isNotEmpty()) {
            tv_label_history.visibility = View.VISIBLE
            iv_history_label.visibility = View.GONE


        } else {
            tv_label_history.visibility = View.GONE
            iv_history_label.visibility = View.GONE

        }

        if (historyItems.size >= 10) {
            btn_show_more.visibility = View.VISIBLE
        } else {
            btn_show_more.visibility = View.GONE
        }
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
//        showToast(this,"Working new ocr screen")
        openCameraActivity(this)

//        openOcrActivity(this)
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
                    deniedPermissions: ArrayList<String>
                ) {
                    super.onDenied(context, deniedPermissions)
                    callHandlerForPref()
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_LANGUAGE_SELECTION) {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")
                val languageName = data.getStringExtra(INTENT_KEY_LANG_NAME)
                val languageCode = data.getStringExtra(INTENT_KEY_LANG_CODE)
                val languageSupport = data.getStringExtra(INTENT_KEY_LANG_SUPPORT)
                val languageMeaning = data.getStringExtra(INTENT_KEY_LANG_MEANING)

                if (origin == LANG_FROM_FIC) {
                    fromLang = languageName!!
                    fromLangCode = languageCode!!
                    fromLangCodeSupport = languageSupport!!
                    fromLangMeaning = languageMeaning!!
//                    tv_lang_from_input.text = fromLang
                    Log.e("language", fromLang)
                    putPrefString(LANG_FROM_FIC, fromLang)
                    putPrefString(LANG_FROM_CODE_FIC, fromLangCode)
                    putPrefString(LANG_FROM_CODE_SUPPORT_FIC, fromLangCodeSupport)
                    putPrefString(LANG_FROM_MEANING_FIC, fromLangMeaning)

                    checkLangParam()

                    if (et_input_word.text.isNotEmpty()) {
                        if (NetworkUtils.isNetworkConnected(this)) {
                            progress_main.visibility = View.VISIBLE
                            GlobalScope.async {
                                getPredictions()
                            }
                        } else {
                            showToast(this, "Turn on Internet Connection")
                        }
                    }

                } else {
                    toLang = languageName!!
                    toLangCode = languageCode!!
                    toLangCodeSupport = languageSupport!!
                    toLangMeaning = languageMeaning!!
                    tv_lang_to_input.text = toLang
                    putPrefString(LANG_TO_FIC, toLang)
                    putPrefString(LANG_TO_CODE_FIC, toLangCode)
                    putPrefString(LANG_TO_CODE_SUPPORT_FIC, toLangCodeSupport)
                    putPrefString(LANG_TO_MEANING_FIC, toLangMeaning)

                    checkLangParam()
                }
//                translateInputWord()

            }
        } else if (requestCode == REQ_CODE_SPEECH_INPUT) {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode == RESULT_OK && null != data) {
                val recognizedResult =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val inputWord = recognizedResult?.get(0)
                setEditTextView(inputWord!!)
            }
        } else if (requestCode == REQ_CODE_BOOKMARK) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")

                val bundle = data.extras!!
                val model: TranslationTable = bundle.getParcelable("detail_object")!!
//                setResultInView(model)
            }
        } else if (requestCode == REQ_CODE_HISTORY) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")

                val bundle = data.extras!!
                val model: TranslationTable = bundle.getParcelable("detail_object")!!
//                setResultInView(model)
            }
        } else if (requestCode == REQUEST_CODE_OCR_BACK) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")

                val bundle = data.extras!!
                val model: TranslationTable = bundle.getParcelable("detail_object")!!
//                setResultInView(model)
            }
        }

    }


    override fun onResume() {
        super.onResume()
        if (isPremium()) {
            hideViews()
        }
        getSelectedLanguages()
        mTts = TextToSpeech(this, this, "com.google.android.tts")
    }

    override fun onFavorite(translationTable: TranslationTable?) {
        database.translationTblDao().updateFAv(translationTable!!.isfav, translationTable.id)
    }

    override fun onSelectHistory(translationTable: TranslationTable?) {
//        setResultInView(translationTable)
//        openHistoryDetailActivity(this@MainActivity, translationTable!!, DETAIL_SOURCE_MAIN)
    }


    override fun onDelete(translationTable: TranslationTable) {
        viewModel.deleteHistoryItem(translationTable)
    }

    override fun slideAdLoaded() {
    }

    override fun slideAddFailed() {
    }


    private fun isDoubleClick(): Boolean {
        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    private var isRateable = false
    private fun initRateUsDialog() {
        rateUsDialog = getRateUsDialog(this)
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
        }


    }


    private fun initRemoveAdsDialog() {
        isRemoveAdShownTwice = getPrefBool("is_remove_ad_twice")
        removeAdCount = getPrefInt("remove_ad_show_count")
        removeAdsDialog = getRemoveAdsDialog(this)
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            mTts?.setSpeechRate(0.7f)
            mTts?.setPitch(1f)
            mTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    isSpeaking = true
                }

                override fun onDone(utteranceId: String) {
                    isSpeaking = false
                }

                override fun onError(utteranceId: String) {
                    runOnUiThread {
                        showToast(
                            this@FictionalLangActivity,
                            resources.getString(R.string.tts_error_device)
                        )
                    }


                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    super.onError(utteranceId, errorCode)
                    runOnUiThread {
                        showToast(
                            this@FictionalLangActivity,
                            resources.getString(R.string.tts_error_device)
                        )
                    }


                }

            })
        }
    }

    override fun onPause() {
        super.onPause()
        if (mTts?.isSpeaking!!)
            mTts?.stop()
        mTts?.shutdown()
    }

    private fun showPopupWindow(view: View) {
        val popup = PopUpUtils.getPopUpView(view)

        popup.menuInflater.inflate(R.menu.menu_output, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.getItemId()) {
                R.id.action_share -> shareOutPut()
                R.id.action_copy -> callCopyText()
                R.id.action_reverse -> callReverse()
                R.id.action_full_screen -> callFullScreen()
            }
            true
        }
        popup.show()
    }

    private fun shareOutPut() {
        val translatedWord = tv_translated_word.text.toString().trim()

        viewModel.shareResult(this, translatedWord)
    }

    private fun callFullScreen() {
        val translatedWord = tv_translated_word.text.toString().trim()

        val intent = Intent(this, FullScreenActivity::class.java)
        intent.putExtra("translation", translatedWord)
        startActivity(intent)
    }

    private fun callCopyText() {
        val translatedWord = tv_translated_word.text.toString().trim()
        putPrefBoolean("is_from_app", true)
        val clipData = ClipData.newPlainText("Source Text", translatedWord)
        clipboardManager?.setPrimaryClip(clipData)
        showToast(this, resources.getString(R.string.text_copied))
    }

    private fun callReverse() {
        val translatedWord = tv_translated_word.text.toString().trim()
        val inputWord = et_input_word.text.toString().trim()
        tv_translated_word.text = inputWord
        et_input_word.setText(translatedWord)
        switchLang()

    }

    private fun setResultInView(translationTable: TranslationTable?) {
        translationTable?.apply {
            val inputWord = inputStr
            val translatedWord = outputStr
            val inputCodeSupport = inputLanguage
            val translatedCodeSupport = outputLanguage
            val inputLangCode = sourceLanCode
            val translatedLangCode = destLanCode
            val uniqueId = unique_iden
            val isFavorite = isfav
            setLanguageChanges(
                inputCodeSupport,
                translatedCodeSupport,
                inputLangCode,
                translatedLangCode
            )

            et_input_word.setText(inputWord)

            tv_translated_word.text = translatedWord
            tv_lang_translated_word.text = "$toLang ( $toLangMeaning )"
            isSpeakAble = isSpeakerVisible(toLangCodeSupport)
            if (isSpeakerVisible(fromLangCodeSupport)) {
                iv_speak_word_input.visibility = View.VISIBLE
            } else {
                iv_speak_word_input.visibility = View.GONE

            }
            if (isSpeakerVisible(toLangCodeSupport)) {
                iv_speak_translated.visibility = View.VISIBLE
            } else {
                iv_speak_translated.visibility = View.GONE

            }

            layoutOutputTranslation.visibility = View.VISIBLE
            isDetailVisible = true
            if (isHistoryAvailable) {
                history_container_main.visibility = View.GONE
            }
            if (isFavorite) {
                iv_toggle_favorite.setImageResource(R.drawable.ic_favorite)
            } else {
                iv_toggle_favorite.setImageResource(R.drawable.ic_un_favorite)

            }
            newTranslationModel = this

        }


    }

    private fun setLanguageChanges(
        inputCodeSupport: String,
        translatedCodeSupport: String,
        inputLangCode: String,
        translatedLangCode: String
    ) {
        this.fromLangCodeSupport = inputCodeSupport
        this.toLangCodeSupport = translatedCodeSupport
        this.fromLangCode = inputLangCode
        this.toLangCode = translatedLangCode

        var allLangs = fetchLanguages()
        val languagesName: MutableList<String> = ArrayList()
        val languagesMeaning: MutableList<String> = ArrayList()
        for (languageModel in allLangs) {
            languagesName.add(languageModel.supportedLangCode)
//            languagesMeaning.add(languageModel.languageMeaning)
        }
        val inputLangIndex = languagesName.indexOf(fromLangCodeSupport)
        val translatedLangIndex = languagesName.indexOf(toLangCodeSupport)
        val inputLangName = allLangs[inputLangIndex].languageName
        val inputMeaning = allLangs[inputLangIndex].languageMeaning
        val translatedLangName = allLangs[translatedLangIndex].languageName
        val translatedMeaning = allLangs[translatedLangIndex].languageMeaning
        this.toLang = translatedLangName
        this.toLangMeaning = translatedMeaning
        this.fromLang = inputLangName
        this.fromLangMeaning = inputMeaning


//        tv_lang_from_input.text = fromLang

        putPrefString(LANG_FROM_FIC, fromLang)
        putPrefString(LANG_FROM_CODE_FIC, fromLangCode)
        putPrefString(LANG_FROM_CODE_SUPPORT_FIC, fromLangCodeSupport)
        putPrefString(LANG_FROM_MEANING_FIC, fromLangMeaning)


        tv_lang_to_input.text = toLang
        putPrefString(LANG_TO_FIC, toLang)
        putPrefString(LANG_TO_CODE_FIC, toLangCode)
        putPrefString(LANG_TO_CODE_SUPPORT_FIC, toLangCodeSupport)
        putPrefString(LANG_TO_MEANING_FIC, toLangMeaning)


    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            if (getPrefBool("is_auto_clip"))
                getCopy()
        }
    }

    private fun getCopy() {
        mCM?.let {
            if (it.hasPrimaryClip() && it.primaryClip != null) {
                val mClip = it.primaryClip
                var newClip = ""
                val itemCount = mClip!!.itemCount
                if (itemCount > 0) {
                    val item = mClip.getItemAt(0)
                    if (item != null) {
                        newClip = mClip.toString()
                        if (newClip != mOldClip && !newClip.contains("NULL")) {
                            mOldClip = newClip
                            checkText()
                        } else {
                        }
                    }
                }
            }
        }
    }


    private fun checkText() {
        kotlin.runCatching {
            if (mCM != null) {
                val charSequence = mCM!!.text
                if (charSequence != null) {
                    copiedWord = charSequence.toString().trim { it <= ' ' }
                }
                if (copiedWord != null && copiedWord!!.isNotEmpty() && copiedWord != "") {
                    et_input_word?.setText(copiedWord)
                    translateInputWord()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        mCM?.clearPrimaryClip()
                    } else {
                        val data = ClipData.newPlainText("", "")
                        mCM?.setPrimaryClip(data)
                    }
                }
            } else {
            }
        }


//        kotlin.runCatching {
//            if (mCM != null) {
//                val charSequence = mCM!!.text
//                if (charSequence != null) {
//                    copiedWord = charSequence.toString().trim { it <= ' ' }
//                }
//                if (copiedWord != null && !copiedWord!!.isEmpty()) {
//                    et_input_word.setText(copiedWord)
//                    translateInputWord()
//                }
//            }
//        }
    }

    fun checkAdsOnly() {
        if (isAdsOnly) {
            iv_crown_pro_banner.visibility = View.VISIBLE
        } else {
            iv_crown_pro_banner.visibility = View.GONE
        }
    }

    private fun loadBannerAd() {
        if (!isPremium()) {
            try {
                MaxAdManager.createBannerAd(this, native_banner_container_fictional)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    public suspend fun getPredictions() {
        val textToTranslate = et_input_word.text.toString().trim()
        try {
            val result = GlobalScope.async {

//                val client = OkHttpClient()
//
//                val request = Request.Builder()
//                    .url("https://yodish.p.rapidapi.com/yoda.json?text=Master%20Obiwan%20has%20lost%20a%20planet.")
//                    .get()
//                    .addHeader("X-RapidAPI-Key", "2913c960demsh152869a09259f04p191ce7jsn574c327ff9fc")
//                    .addHeader("X-RapidAPI-Host", "yodish.p.rapidapi.com")
//                    .build()
//
//                val response = client.newCall(request).execute()
//                callAztroAPI("https://sith.p.rapidapi.com/sith.json?text=I%20went%20to%20coruscant%20which%20was%20ten%20parsec%20away!")
                callAztroAPI(url + textToTranslate)
            }.await()

            onResponse(result)

//            if (result != null){
//                progress_main.visibility = View.GONE
//
//                try {
//                    layoutOutputTranslation.visibility = View.VISIBLE
//                    val resultJson = JSONObject(result)
//                    val translatedData = resultJson.getJSONObject("contents").getString("translated")
//
//
//                    tv_translated_word.setText(translatedData)
//
//                }catch (e:Exception){
//                    e.printStackTrace()
//                    progress_main.visibility = View.GONE
//                    Log.e("result","Oops!! something went wrong, please try again")
//
//                    Log.e("Response Catch>>", "method")
//
//                    layoutOutputTranslation.visibility = View.VISIBLE
//                    runOnUiThread { Toast.makeText(applicationContext, "Oops!! something went wrong, please try again", Toast.LENGTH_LONG).show() }
//                }
//            }else{
//                progress_main.visibility = View.GONE
//                layoutOutputTranslation.visibility = View.VISIBLE
//
//                runOnUiThread { Toast.makeText(applicationContext, "Oops!! something went wrong, please try again", Toast.LENGTH_LONG).show() }
//
//            }
//            if (result == null){
//                Toast.makeText(this@FictionalLangActivity,"Oops!! something went wrong, please try again",Toast.LENGTH_LONG).show()
//
//            }
//            Log.e("mainResult","==>"+result.toString())
            Log.e("url data", url + textToTranslate)
            Log.e("TO>>>>", toLang + apiMethod + url + hostApi)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //    private fun okHttpcall(apiUrl:String ):String?{
//        val client = OkHttpClient()
//
//        val request = Request.Builder()
//            .url("https://valspeak.p.rapidapi.com/valspeak.json?text=Good%20Morning.%20Come%20on%20man%2C%20just%20saying!")
//            .get()
//            .addHeader("X-RapidAPI-Key", "db4dde7205mshce8c30d49fd289ap1b7c75jsn66e3eab4ed9a")
//            .addHeader("X-RapidAPI-Host", "yodish.p.rapidapi.com")
//            .build()
//
//        val response = client.newCall(request).execute()
//        Log.e("response","f"+response.toString())
//        return response.toString()
//    }
    private fun callAztroAPI(apiUrl: String): String? {
        var result: String? = ""
        val url: URL;
        var connection: HttpURLConnection? = null
        try {
            url = URL(apiUrl)
            connection = url.openConnection() as HttpURLConnection
            // set headers for the request
            // set host name
//            connection.setRequestProperty("x-rapidapi-host", "sith.p.rapidapi.com")
            connection.setRequestProperty("x-rapidapi-host", hostApi)
            // set the rapid-api key
            connection.setRequestProperty(
                "x-rapidapi-key",
                "db4dde7205mshce8c30d49fd289ap1b7c75jsn66e3eab4ed9a"
            )
//            connection.setRequestProperty("content-type", "application/x-www-form-urlencoded")
            // set the request method - POST
//            connection.requestMethod = "GET"
            connection.requestMethod = apiMethod
            val `in` = connection.inputStream
            val reader = InputStreamReader(`in`)
            // read the response data
            var data = reader.read()
            while (data != -1) {
                val current = data.toChar()
                result += current
                data = reader.read()
            }
            Log.e("result", "Result>>" + result.toString())
//            val resultJson = JSONObject(result)
//            val translatedData = resultJson.getJSONObject("contents").getString("translated")
//            setTranslation(translatedData)

            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // if not able to retrieve data return null
        return null
    }

    private fun onResponse(result: String?) {

        runOnUiThread {
            progress_main.visibility = View.GONE

        }

        if (result != null) {
            try {
                // convert the string to JSON object for better reading
                val resultJson = JSONObject(result)
                // Initialize prediction text
//            var prediction ="Today's prediction nn"
//            prediction += this.sunSign+"n"
                // Update text with various fields from response
//            prediction += resultJson.getString("date_range")+"nn"
//            prediction += resultJson.getString("description")
                //Update the prediction to the view
//            setText(this.resultView,prediction)
//            Log.e("result","apiPrediction"+prediction)

                val translatedData =
                    resultJson.getJSONObject("contents").getString("translated")


//            setTranslation(translatedData)

                runOnUiThread {
//                    layoutOutputTranslation.setVisibility(View.VISIBLE)
//                    tv_translated_word.setText(translatedData)

                    setTranslation(translatedData)
                }

//            runOnUiThread {  }

//
//                Log.e("result", "Result>>" + resultJson)
//                Log.e("from", "Lang>>" + fromLang)
//                Log.e("To", "Lang>>" + toLang)
//                Log.e("Response Try>>", "method")
//        }else{
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { progress_main.visibility = View.GONE }
                Log.e("result", "Oops!! something went wrong, please try again")

                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Oops!! something went wrong, please try again",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            runOnUiThread {
                Toast.makeText(
                    applicationContext,
                    "Oops!! something went wrong, please try again",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setText(text: TextView?, value: String) {
        runOnUiThread { text!!.text = value }
    }

    fun setVisibilities() {
        layoutOutputTranslation.visibility = View.VISIBLE

    }

    fun checkLangParam() {
        if (toLang == "Jar Jar Binks") {
            url = "https://gungan.p.rapidapi.com/gungan.json?text="
            apiMethod = "GET"
            hostApi = "gungan.p.rapidapi.com"
        }
        if (toLang == "Sith") {
            url = "https://sith.p.rapidapi.com/sith.json?text="
            apiMethod = "GET"
            hostApi = "sith.p.rapidapi.com"
        }
        if (toLang == "Shakespeare") {
            url = "https://shakespeare.p.rapidapi.com/shakespeare.json?text="
            apiMethod = "GET"
            hostApi = "shakespeare.p.rapidapi.com"
        }
        if (toLang == "Valley Girl") {
            url = "https://valspeak.p.rapidapi.com/valspeak.json?text="
            apiMethod = "POST"
            hostApi = "valspeak.p.rapidapi.com"
        }
    }
}

