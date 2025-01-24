package com.translate.languagetranslator.freetranslation.activities.translate

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
//import com.android.billingclient.api.ProductDetails
import com.code4rox.adsmanager.MaxAdManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.activities.onboarding.BaseBillingActivity
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.appUtils.Constants.TRANSLATE_BUTTON_COUNT
import com.translate.languagetranslator.freetranslation.appUtils.Constants.TRANSLATE_ORIGIN_MAIN
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.interfaces.AdLoadedCallback
import com.translate.languagetranslator.freetranslation.network.TranslationUtils
import com.translate.languagetranslator.freetranslation.utils.AdsUtill
import kotlinx.android.synthetic.main.activity_phrasebook.*
import kotlinx.android.synthetic.main.activity_translate.*
import kotlinx.android.synthetic.main.activity_translate.input_layout_lang_from
import kotlinx.android.synthetic.main.activity_translate.input_layout_lang_to
import kotlinx.android.synthetic.main.activity_translate.iv_clear_input
import kotlinx.android.synthetic.main.activity_translate.iv_switch_lang_input
import kotlinx.android.synthetic.main.activity_translate.tv_lang_from_input
import kotlinx.android.synthetic.main.activity_translate.tv_lang_to_input
import kotlinx.android.synthetic.main.appbar_home.*
import kotlinx.android.synthetic.main.appbar_main.*
import kotlinx.android.synthetic.main.layout_nav_drawer.*
import java.util.*

class TranslateActivity : BaseBillingActivity(), View.OnClickListener, TextToSpeech.OnInitListener {
    private var isTextAvailable: Boolean = false
    private var isDetailAvailable: Boolean = false
    private var isSpeakAble: Boolean = false
    private var isSpeaking: Boolean = false

    private lateinit var fromLang: String
    private lateinit var fromLangCode: String
    private lateinit var fromLangCodeSupport: String
    private lateinit var toLang: String
    private lateinit var toLangCode: String
    private lateinit var toLangCodeSupport: String
    private lateinit var toLangMeaning: String
    private lateinit var fromLangMeaning: String
    private var maxInterstitialSession: Int = 0
    var newString: String? = null
    var origin: String? = null
    lateinit var translationDb: TranslationDb
    private lateinit var mTts: TextToSpeech
    private lateinit var langLocal: Locale

    private var clipboardService: Any? = null
    private var clipboardManager: ClipboardManager? = null
    var translationUtils: TranslationUtils? = null
    private var translateSearchCount: Int = 0
    private var clickType = ClickType.NONE

    private lateinit var mFireBaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var configSettings: FirebaseRemoteConfigSettings

    private var removeAdsDialog: Dialog? = null


    private lateinit var animateHandler: Handler
    private var animateRunnable = Runnable {
        AnimUtils.zoomInOut(applicationContext, iv_mic_controller)
    }


    enum class ClickType {
        NONE, TRANSLATE
    }

    /// new adsplacement
    private var interPriority: String? = null
    private var interCounter: Int? = null
    private var isInterShow = false

    //private var translateAdPair: InterAdPair? = null

    private lateinit var adsUtill: AdsUtill

    //private var translateFb: FacebookAdsUtils? = null
    private var showInterstitialAdd = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adsUtill= AdsUtill(this)

        loadBannerAd()
        try {
            setContentView(R.layout.activity_translate)
            translationDb = TranslationDb.getInstance(this)
            clipboardService = getSystemService(CLIPBOARD_SERVICE)
            clipboardManager = clipboardService as ClipboardManager
            animateHandler = Handler()
            initRemoteConfig()
            initAds()
            setToolbar()
            getSelectedLanguages()
            setClickListeners()
            setEtChangeListener()
            getBundleType()
            initRemoveAdDialog()
            (application as AppBase).thisActivity = true;
        } catch (e: ConcurrentModificationException) {
        }
       /* Handler().postDelayed({
            if (!isPremium()) {
                if (IronSource.isInterstitialReady()) {
                    if ((application as AppBase).interstitialSessionCount < maxInterstitialSession) {
                        (application as AppBase).interstitialSessionCount++
                        IronSource.showInterstitial()
                    }
                }
            }
        }, 3000)*/

    }
//
//    override fun onQuerySkuDetailsSubscription(skuDetailsList: MutableList<ProductDetails>?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onQuerySkuDetailsInApp(skuDetailsList: MutableList<ProductDetails>?) {
//        TODO("Not yet implemented")
//    }
//

    fun initRemoveAdDialog() {
        removeAdsDialog = getRemoveAdsDialog(this)
        val ivCross = removeAdsDialog?.findViewById<ImageView>(R.id.iv_cross_dlg)
        val btnRemoveAds = removeAdsDialog?.findViewById<RelativeLayout>(R.id.layout_remove_ads)

        ivCross?.setOnClickListener {

            removeAdsDialog?.dismiss()
        }
        btnRemoveAds?.setOnClickListener {
            removeAdsDialog?.dismiss()
            inAppSubscription()
        }

    }

    fun hideViews() {
        runOnUiThread {
            tv_pro_translate.clearAnimation()
            tv_pro_translate.visibility = View.GONE
        }
    }

    private fun initRemoteConfig() {
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        configSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(200).build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)

        isInterShow =
            mFireBaseRemoteConfig.getBoolean(RemoteConfigConstants.TRANSLATE_INTER_SHOW)
        interCounter =
            mFireBaseRemoteConfig.getDouble(RemoteConfigConstants.TRANSLATE_INTER_COUNT).toInt()
        interPriority =
            mFireBaseRemoteConfig.getString(RemoteConfigConstants.TRANSLATE_INTER_PRIORITY)

        /////////

        translateSearchCount = getPrefIntOne(TRANSLATE_BUTTON_COUNT)
        maxInterstitialSession = mFireBaseRemoteConfig.getDouble(RemoteConfigConstants.MAX_INTERSTITIAL_SESSION).toInt()


    }

    private fun initAds() {
        showInterstitialAdd = isInterShow && !isPremium()
    }

    private fun setToolbar() {

        iv_toolbar_back_translator.setOnClickListener {
                onBackPressed()
        }
        if (isPremium()) {
            tv_pro_translate.visibility = View.GONE
        } else {
            animatePro()

        }


    }

    override fun onBackPressed() {


        if (!isPremium()) {
            showInterstitial {
                //        TODO()4.5.0  banner was being destroyed here
                //adsUtill.destroyBanner()
                HomeActivity.showBanner = true
                super.onBackPressed()
            }


        } else {
            super.onBackPressed()
        }
    }

    private fun animatePro() {
        Handler().postDelayed(Runnable {
            AnimUtils.zoomInOut(
                applicationContext,
                tv_pro_translate
            )
        }, 600)
    }

    private fun setClickListeners() {
        iv_switch_lang_input.setOnClickListener(this)
        input_layout_lang_from.setOnClickListener(this)
        input_layout_lang_to.setOnClickListener(this)
        layout_input_mic_home.setOnClickListener(this)
        iv_clear_input.setOnClickListener(this)
        iv_speaker_translated_word.setOnClickListener(this)
        iv_copy_translated_text.setOnClickListener(this)
        tv_pro_translate.setOnClickListener(this)
    }

    private fun copyWord() {

        val translatedWord = tv_translated_text.text.toString()

        val clipData = ClipData.newPlainText("Source Text", translatedWord)
        putPrefBoolean("is_from_app", true)
        // Set it as primary clip data to copy text to system clipboard.
        clipboardManager?.setPrimaryClip(clipData)
        showToast(this, "Text Copied")

    }


    private fun getSelectedLanguages() {
        fromLang = getPrefString(Constants.LANG_FROM)
        if (fromLang == "") {
            fromLang = "English"
            putPrefString(Constants.LANG_FROM, fromLang)
        }
        fromLangCode = getPrefString(Constants.LANG_FROM_CODE)
        if (fromLangCode == "") {
            fromLangCode = "en-GB"
            putPrefString(Constants.LANG_FROM_CODE, fromLangCode)

        }

        fromLangCodeSupport = getPrefString(Constants.LANG_FROM_CODE_SUPPORT)
        if (fromLangCodeSupport == "") {
            fromLangCodeSupport = "en"
            putPrefString(Constants.LANG_FROM_CODE_SUPPORT, fromLangCodeSupport)

        }

        fromLangMeaning = getPrefString(Constants.LANG_FROM_MEANING)
        if (fromLangMeaning == "") {
            fromLangMeaning = "English"
            putPrefString(Constants.LANG_FROM_MEANING, fromLangMeaning)
        }

        toLangMeaning = getPrefString(Constants.LANG_TO_MEANING)
        if (toLangMeaning == "") {
            toLangMeaning = "Français"
            putPrefString(Constants.LANG_TO_MEANING, toLangMeaning)
        }

        toLang = getPrefString(Constants.LANG_TO)
        if (toLang == "") {
            toLang = "French"
            putPrefString(Constants.LANG_TO, toLang)

        }

        toLangCode = getPrefString(Constants.LANG_TO_CODE)
        if (toLangCode == "") {
            toLangCode = "fr-FR"
            putPrefString(Constants.LANG_TO_CODE, toLangCode)
        }
        toLangCodeSupport = getPrefString(Constants.LANG_TO_CODE_SUPPORT)

        if (toLangCodeSupport == "") {
            toLangCodeSupport = "fr"
            putPrefString(Constants.LANG_TO_CODE_SUPPORT, toLangCodeSupport)
        }
        setLanguages()
    }

    private fun setLanguages() {

        tv_lang_from_input.text = fromLang
        tv_lang_to_input.text = toLang

    }

    private fun switchLanguages() {
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


        putPrefString(Constants.LANG_FROM, fromLang)
        putPrefString(Constants.LANG_FROM_CODE, fromLangCode)
        putPrefString(Constants.LANG_FROM_CODE_SUPPORT, fromLangCodeSupport)
        putPrefString(Constants.LANG_FROM_MEANING, fromLangMeaning)


        putPrefString(Constants.LANG_TO, toLang)
        putPrefString(Constants.LANG_TO_CODE, toLangCode)
        putPrefString(Constants.LANG_TO_CODE_SUPPORT, toLangCodeSupport)
        putPrefString(Constants.LANG_TO_MEANING, toLangMeaning)


        setLanguages()

    }


    private fun getBundleType() {
        val extras = intent.extras
        if (extras == null) {
            newString = null
            origin = null
        } else {
            newString = extras.getString(Constants.TRANSLATE_TYPE)
            origin = extras.getString(Constants.TRANSLATE_ORIGIN)
        }
        assert(newString != null)
        if (origin.equals(TRANSLATE_ORIGIN_MAIN)) {
            if (newString.equals(Constants.TRANSLATE_TYPE_MIC)) {
                hideSoftKeyboard()
                if (getMicVisibility(fromLangCodeSupport)) {
                    initMicInput(fromLangCodeSupport)
                } else {
                    showToast(this, "Language does not support voice to text")
                }
            } else {
                et_input.isActivated = true
                et_input.isPressed = true
                et_input.isCursorVisible = true
                et_input.requestFocus()
                ads_banner_parent_translation.visibility = View.GONE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    et_input.focusable = View.FOCUSABLE
                    ads_banner_parent_translation.visibility = View.GONE
                }
                et_input.post {
                    et_input.isActivated = true
                    et_input.isPressed = true
                    et_input.isCursorVisible = true
                    et_input.requestFocus()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        et_input.focusable = View.FOCUSABLE
                    }
                    et_input.setSelection(et_input.text.toString().length)
                }
            }
        } else {
            if (newString.equals(Constants.TRANSLATE_TYPE_TEXT)) {
                val data = extras!!.getString(Constants.TRANSLATE_INTENT_DATA)
                et_input.setText(data)
            }
        }


    }

    private fun setEtChangeListener() {
        et_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            @SuppressLint("RestrictedApi")
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim().isNotEmpty()) {
                    isTextAvailable = true
                    iv_clear_input.visibility = View.VISIBLE
                    iv_mic_controller.setImageResource(R.drawable.ic_arrow_forward_black_24dp)


                } else {
                    iv_clear_input.visibility = View.GONE
                    iv_mic_controller.setImageResource(R.drawable.ic_mic_black_24dp)
                    isTextAvailable = false

                }


            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

    }


    override fun onResume() {
        super.onResume()
        mTts = TextToSpeech(this, this, "com.google.android.tts")
    }

    override fun onPause() {
        super.onPause()

        if (mTts.isSpeaking)
            mTts.stop()
        mTts.shutdown()

        if (translationUtils != null) {
            translationUtils?.StopBackground()
        }
    }

    private fun controlMicActionButton() {
        if (!TextUtils.isEmpty(et_input.text)) {
            if (translateSearchCount >= interCounter!!) {
                clickType = ClickType.TRANSLATE
                translateSearchCount = 1
                putPrefInt(TRANSLATE_BUTTON_COUNT, translateSearchCount)
                if (!isPremium()) {
                   /* if (showInterstitialAdd) {
                        IronSource.setInterstitialListener(getInterstitialListener({
                            //onAddLoaded
                            if (it) {
                                if((application as AppBase).thisActivity){

                                if((application as AppBase).interstitialSessionCount<maxInterstitialSession) {
                                    showInterstitialAdd = false
                                    (application as AppBase).interstitialSessionCount++
                                    IronSource.showInterstitial()
                                }
                            }} else
                                callTranslation()
                        }, {
                            //onAdClosed

                        }))
                        callTranslation()
                        IronSource.loadInterstitial();
                    } else*/
                        callTranslation()

                    /* translateAdPair?.let { ads ->
                         if (ads.isLoaded()) {
                             ads.showAd(this)
                         } else {
                             callTranslation()
                         }
                     } ?: run {
                         callTranslation()
                     }*/
                    callTranslation()
                } else {
                    callTranslation()
                }


            } else {
                translateSearchCount++
                putPrefInt(TRANSLATE_BUTTON_COUNT, translateSearchCount)
                callTranslation()
            }
        } else {
            if (getMicVisibility(fromLangCodeSupport)) {
                initMicInput(fromLangCodeSupport)
            } else {
                showToast(this, "Language does not support voice to text")
            }
        }

    }

    private fun callTranslation() {
        if (NetworkUtils.isNetworkConnected(this)) {
            val textToTranslate = et_input.text.toString().trim()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                progress_search.progressTintList = ColorStateList.valueOf(Color.WHITE)
            } else {
                progress_search.indeterminateDrawable.setColorFilter(
                    resources.getColor(R.color.white),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
            progress_search.visibility = View.VISIBLE
            iv_mic_controller.visibility = View.GONE
            hideSoftKeyboard()

            if (translationUtils != null) {
                translationUtils?.StopBackground()
            }

            translationUtils = TranslationUtils(object : TranslationUtils.ResultCallBack {
                override fun onReceiveResult(result: String?) {
                    setTranslation(result!!)
                    insertHistory(textToTranslate, result)
                }

                override fun onFailedResult() {
                    handleErrorResponse("Network error, please check the network and try again")
                }

            }, textToTranslate, fromLangCodeSupport, toLangCodeSupport)
            translationUtils?.execute()


        } else {
            showToast(this, "Turn on Internet Connection")
        }

    }

    private fun insertHistory(input: String, translated: String) {

//        val model = TranslationTable(
//            fromLangCodeSupport,
//            toLangCodeSupport,
//            input,
//            translated,
//            fromLangCode,
//            toLangCode
//        )
//        translationDb.translationTblDao().insert(model)

    }

    private fun setTranslation(translationstr: String) {
        tv_translated_text.text = translationstr
        tv_lang_translated.text = "$toLang ( $toLangMeaning )"
        iv_copy_translated_text.setImageResource(R.drawable.ic_content_copy_black_24dp)
        if (isSpeakerVisible(toLangCodeSupport)) {
            iv_speaker_translated_word.setImageResource(R.drawable.ic_volume_up_blue_24dp)
            isSpeakAble = true
        } else {
            iv_speaker_translated_word.setImageResource(R.drawable.ic_volume_up_gray_24dp)
            isSpeakAble = false

        }
        detail_view.visibility = View.VISIBLE
        progress_search.visibility = View.GONE
        iv_mic_controller.visibility = View.VISIBLE
        isDetailAvailable = true
    }

    private fun handleErrorResponse(message: String) {
        progress_search.visibility = View.GONE
        iv_mic_controller.visibility = View.VISIBLE
        showToast(this, message)


    }

    private fun initMicInput(s: String) {
        when (s) {
            "af" -> speakIn("af-ZA")
            "am" -> speakIn("am-ET")
            "ar" -> speakIn("ar-SA")
            "hy" -> speakIn("hy-AM")
            "az" -> speakIn("az-AZ")
            "eu" -> speakIn("eu-ES")
            "bn" -> speakIn("bn-BD")
            "bg" -> speakIn("bg-BG")
            "ca" -> speakIn("ca-ES")
            "zh" -> speakIn("cmn-Hans-CN")
            "hr" -> speakIn("hr-HR")
            "cs" -> speakIn("cs-CZ")
            "da" -> speakIn("da-DK")
            "nl" -> speakIn("nl-NL")
            "en" -> speakIn("en-US")
            "et" -> speakIn("et-EE")
            "fi" -> speakIn("fi-FI")
            "fr" -> speakIn("fr-FR")
            "tl" -> speakIn("fil-PH")
            "gl" -> speakIn("gl-ES")
            "ka" -> speakIn("ka-GE")
            "de" -> speakIn("de-DE")
            "el" -> speakIn("el-GR")
            "gu" -> speakIn("gu-IN")
            "he" -> speakIn("he-IL")
            "hi" -> speakIn("hi-IN")
            "hu" -> speakIn("hu-HU")
            "is" -> speakIn("is-IS")
            "id" -> speakIn("id-ID")
            "it" -> speakIn("it-IT")
            "ja" -> speakIn("ja-JP")
            "jw" -> speakIn("jw-ID")
            "kn" -> speakIn("kn-IN")
            "km" -> speakIn("km-KH")
            "ko" -> speakIn("ko-KR")
            "lo" -> speakIn("lo-LA")
            "lv" -> speakIn("lv-LV")
            "lt" -> speakIn("lt-LT")
            "ms" -> speakIn("ms-MY")
            "ml" -> speakIn("ml-IN")
            "mr" -> speakIn("mr-IN")
            "my" -> speakIn("my-MM")
            "ne" -> speakIn("ne-NP")
            "nb" -> speakIn("nb-NO")
            "fa" -> speakIn("fa-IR")
            "pl" -> speakIn("pl-PL")
            "pt" -> speakIn("pt-PT")
            "ro" -> speakIn("ro-RO")
            "ru" -> speakIn("ru-RU")
            "sr" -> speakIn("sr-RS")
            "sk" -> speakIn("sk-SK")
            "sl" -> speakIn("sl-SI")
            "es" -> speakIn("es-ES")
            "su" -> speakIn("su-ID")
            "sw" -> speakIn("sw-TZ")
            "sv" -> speakIn("sv-SE")
            "ta" -> speakIn("ta-IN")
            "te" -> speakIn("te-IN")
            "th" -> speakIn("th-TH")
            "tr" -> speakIn("tr-TR")
            "uk" -> speakIn("uk-UA")
            "ur" -> speakIn("ur-PK")
            "uz" -> speakIn("uz-UZ")
            "vi" -> speakIn("vi-VN")
            "zu" -> speakIn("zu-ZA")
        }
    }

    private fun speakIn(s: String) {
        if (isAppInstalled("com.google.android.googlequicksearchbox")) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, s)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, s)
            intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, s)
            intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, s)
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, s)
            intent.putExtra(RecognizerIntent.EXTRA_RESULTS, s)
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putPrefBoolean("app_killed", true)


            try {
                startActivityForResult(intent, Constants.REQ_CODE_SPEECH_INPUT)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                showToast(this, resources.getString(R.string.stt_error_device))
            }
        }

    }

    private fun isAppInstalled(@Suppress("SameParameterValue") packageName: String): Boolean {
        val pm = packageManager
        return try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            pm.getApplicationInfo(packageName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }

    }

    private fun clearEditTextField() {
        et_input.setText("")
        iv_mic_controller.clearAnimation()
        if (isDetailAvailable)
            clearDetailBox()

    }

    private fun clearDetailBox() {
        isDetailAvailable = false
        detail_view.visibility = View.GONE

    }

    private fun hideSoftKeyboard() {

        ads_banner_parent_translation.visibility = View.VISIBLE
        (Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE)) as InputMethodManager).hideSoftInputFromWindow(
            et_input.applicationWindowToken,
            0
        )
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        et_input.clearFocus()
    }

    private fun setEditTextView(result: String) {
        et_input.setText(result)
        setEtChangeListener()
        callHandlerForPref()


    }

    private fun onSpeakerClicked(translatedWord: String, languageCode: String) {
        if (isSpeaking) {
            mTts.stop()
            isSpeaking = false
        } else {

            speakTranslatedWord(translatedWord, languageCode)
        }

    }

    private fun speakTranslatedWord(translatedWord: String, languageCode: String) {
        when (languageCode) {
            "tl" -> {
                langLocal = Locale("fil", "PH")
                mTts?.language = langLocal
            }
            "id" -> {
                langLocal = Locale("id", "ID")
                mTts?.language = langLocal
            }
            "en" -> {
                langLocal = Locale("en", "US")
                mTts?.language = langLocal
            }
            "sq" -> {
                langLocal = Locale("sq", "AL")
                mTts?.language = langLocal
            }

            "fr" -> {
                mTts?.language = Locale.FRANCE
            }
            "zh" -> {
//                langLocal = Locale(targetLang)
                mTts?.language = Locale.CHINA
            }

            else -> {
                langLocal = Locale(languageCode)
                mTts?.language = langLocal
            }
        }
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        @Suppress("DEPRECATION")
        mTts?.speak(translatedWord, TextToSpeech.QUEUE_FLUSH, map)

    }


    override fun onClick(v: View?) {
        if (v != null) {

            when (v.id) {
                R.id.iv_switch_lang_input -> {
                    switchLanguages()
                }
                R.id.input_layout_lang_from -> {
                    startLanguageActivity(this, Constants.LANG_FROM, Constants.LANG_TYPE_NORMAL)
                }
                R.id.input_layout_lang_to -> {
                    startLanguageActivity(this, Constants.LANG_TO, Constants.LANG_TYPE_NORMAL)

                }
                R.id.layout_input_mic_home -> {

                    controlMicActionButton()

                }
                R.id.iv_clear_input -> {

                    clearEditTextField()
                }
                R.id.iv_speaker_translated_word -> {
                    if (isSpeakAble) {
                        val translatedWord = tv_translated_text.text.toString()
                        onSpeakerClicked(translatedWord, toLangCodeSupport)
                    } else {
                        showToast(this, "This language does not support text to speech")

                    }
                }
                R.id.iv_copy_translated_text -> {
                    copyWord()
                }
                R.id.tv_pro_translate -> {
                    removeAdsDialog?.show()
                }
            }
        }
    }

    private fun inAppSubscription() {

//        subMonthly?.let {
//
//            if (billingProcess.isBillingProcessReady) {
//
//                billingProcess.launchPurchaseFlow(it)
//            } else {
//
//                showToast(this, "Billing process not initialized. Please wait")
//            }
//        } ?: run {
//
//            showToast(this, "It seems there are no products to purchase.")
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == Constants.REQ_CODE_LANGUAGE_SELECTION) {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")
                val languageName = data.getStringExtra(Constants.INTENT_KEY_LANG_NAME)
                val languageCode = data.getStringExtra(Constants.INTENT_KEY_LANG_CODE)
                val languageSupport = data.getStringExtra(Constants.INTENT_KEY_LANG_SUPPORT)
                val languageMeaning = data.getStringExtra(Constants.INTENT_KEY_LANG_MEANING)

                if (origin == Constants.LANG_FROM) {
                    fromLang = languageName!!
                    fromLangCode = languageCode!!
                    fromLangCodeSupport = languageSupport!!
                    fromLangMeaning = languageMeaning!!

                    tv_lang_from_input.text = fromLang
                    putPrefString(Constants.LANG_FROM, fromLang)
                    putPrefString(Constants.LANG_FROM_CODE, fromLangCode)
                    putPrefString(Constants.LANG_FROM_CODE_SUPPORT, fromLangCodeSupport)
                    putPrefString(Constants.LANG_FROM_MEANING, fromLangMeaning)

                } else {
                    toLang = languageName!!
                    toLangCode = languageCode!!
                    toLangCodeSupport = languageSupport!!
                    toLangMeaning = languageMeaning!!

                    tv_lang_to_input.text = toLang
                    putPrefString(Constants.LANG_TO, toLang)
                    putPrefString(Constants.LANG_TO_CODE, toLangCode)
                    putPrefString(Constants.LANG_TO_CODE_SUPPORT, toLangCodeSupport)
                    putPrefString(Constants.LANG_TO_MEANING, toLangMeaning)

                    if (et_input.text.toString().trim().isNotEmpty()) {
                        callTranslation()
                    }
                }

            }
        } else if (requestCode == Constants.REQ_CODE_SPEECH_INPUT) {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode == RESULT_OK && null != data) {
                val recognizedResult = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val inputWord = recognizedResult?.get(0)
                animateHandler.postDelayed(animateRunnable, 600)

                setEditTextView(inputWord!!)
            }
        }
    }

    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
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
                    Log.e("tts error", "" + utteranceId)
                    runOnUiThread {
                        showToast(
                            this@TranslateActivity,
                            resources.getString(R.string.tts_error_device)
                        )
                    }


                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    super.onStop(utteranceId, interrupted)
                    Log.d("tts Stop", "" + utteranceId)

                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    super.onError(utteranceId, errorCode)
                    Log.e("tts error with code", "" + utteranceId)
                    runOnUiThread {
                        showToast(
                            this@TranslateActivity,
                            resources.getString(R.string.tts_error_device)
                        )
                    }


                }

                override fun onBeginSynthesis(
                    utteranceId: String?,
                    sampleRateInHz: Int,
                    audioFormat: Int,
                    channelCount: Int
                ) {
                    super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount)
                    Log.i("tts onBeginSynthesis", "" + utteranceId)

                }

            })

            val result = mTts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            }


        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.transit_none, R.anim.transit_top_bottom)

    }
    private fun loadBannerAd(){

        if (!isPremium()) {
            try {
                MaxAdManager.createBannerAd(this,native_banner_container_transation)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




}
