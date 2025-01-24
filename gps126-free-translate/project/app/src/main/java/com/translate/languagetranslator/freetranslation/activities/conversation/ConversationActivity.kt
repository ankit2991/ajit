package com.translate.languagetranslator.freetranslation.activities.conversation

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.LinearLayoutManager
import com.code4rox.adsmanager.MaxAdManager

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.conversation.adapter.ConversationAdapter
import com.translate.languagetranslator.freetranslation.activities.conversation.interfaces.ConversationListener
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.appUtils.Constants.*
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.database.entity.ConversationModel
import com.translate.languagetranslator.freetranslation.database.entity.SavedChat
import com.translate.languagetranslator.freetranslation.interfaces.AdLoadedCallback

import com.translate.languagetranslator.freetranslation.network.TranslationUtils
import com.translate.languagetranslator.freetranslation.utils.AdsUtill
import com.translate.languagetranslator.freetranslation.viewmodels.ConversationActivityViewModel
import kotlinx.android.synthetic.main.activity_conversation.*
import kotlinx.android.synthetic.main.activity_conversation.layout_progress
import kotlinx.android.synthetic.main.activity_conversation.native_banner_container_home_screen
import kotlinx.android.synthetic.main.activity_dictionary.*
import kotlinx.android.synthetic.main.dlg_clear_history.*
import kotlinx.android.synthetic.main.layout_toolbar_conversation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class ConversationActivity : AppCompatActivity(), View.OnClickListener, ConversationListener,
    TextToSpeech.OnInitListener {
    private val TAG = "Conversation"

    private lateinit var fromLang: String
    private lateinit var fromLangCode: String
    private lateinit var fromLangCodeSupport: String
    private lateinit var toLang: String
    private lateinit var toLangCode: String
    private lateinit var toLangCodeSupport: String
    private lateinit var toLangMeaning: String
    private lateinit var fromLangMeaning: String
    private lateinit var adsUtill: AdsUtill
    private lateinit var translation: String
    private lateinit var speakerLanguageCode: String
    private var clickType = ClickType.NONE
    private var isSpeaking: Boolean = false
    private var isMicClicked: Boolean = false
    private lateinit var recognizedWord: String
    private lateinit var database: TranslationDb
    private var mTts: TextToSpeech? = null
    private lateinit var langLocal: Locale
    private var conversationAdapter: ConversationAdapter? = null
    private var isFromDb = false
    var translationUtils: TranslationUtils? = null
    private lateinit var dialog: Dialog
    private var saveConversationDialog: Dialog? = null
    private var conversationDataList: MutableList<ConversationModel> = ArrayList()


    private lateinit var mFireBaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var configSettings: FirebaseRemoteConfigSettings
    private var maxInterstitialSession: Int = 3;

    private var clipboardService: Any? = null
    private var clipboardManager: ClipboardManager? = null
    private var mLastClickTime: Long = 0


    private var origin: String? = null
    private var conversationListData: String? = null
    private var conversationName: String? = null
    val viewModel: ConversationActivityViewModel by viewModel()
    //var backInterstitialAd: Any? = null
    var showBackInterstitial = false

    enum class ClickType {
        NONE, FROM, TO, BACK, CONVERSATION
    }

    enum class AdClickType {
        CONVERSATION, NONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        adsUtill= AdsUtill(this)
        (application as AppBase).isOnMainMenu=true
        loadBannerAd()
        Logging.adjustEvent("sagory", Logging.currentTime(),"Conversation")

        database = TranslationDb.getInstance(this)
        clipboardService = getSystemService(CLIPBOARD_SERVICE)
        (application as AppBase).thisActivity = true;
        clipboardManager = clipboardService as ClipboardManager
        initBundleData()

        viewModel.conversation_back_interstitial().observe(this, androidx.lifecycle.Observer {
            showBackInterstitial = it.show && !viewModel.isAutoAdsRemoved()
            /*if (it.show && !viewModel.isAutoAdsRemoved()){
                    getInterstitialAdObject(
                        getString(R.string.am_conversation_backpress_intertitial),
                        getString(R.string.fb_conversation_backpress_intertitial),
                        it.priority,
                        { adObject ->
                            adObject?.let { ad ->
                                backInterstitialAd = ad
                            }
                        },
                        {
                            onBackPressed()
                        })
                }*/
        })
       // initRemoteConfig()


/*
        initAds()
*/
        getSelectedLanguages()
        setClickListeners()
        setConversationData()
        setDialogBuilder()
        initSaveDialog()
        initRemoteConfig()

    }


    override fun onBackPressed() {
        if(!isPremium()){
              showInterstitial{
                  //        TODO()4.5.0  banner was being destroyed here

                  //adsUtill.destroyBanner()

                  HomeActivity.showBanner=true
                  super.onBackPressed()
              }

        }else {
            super.onBackPressed()
             setResult(REQ_CODE_RATEUS,Intent())
        }

    }

    private fun initBundleData() {
        intent.extras?.let { bundle ->
            origin = bundle.getString(INTENT_KEY_CONVERSATION_ORIGIN)
            if (origin == CONVERSATION_ORIGIN_LIST) {
                conversationListData = bundle.getString(INTENT_PARAM_CONVERSATION_LIST)
                conversationName = bundle.getString(INTENT_PARAM_CONVERSATION_NAME)
                conversationDataList = getListFromJson(conversationListData!!)
            }
        }
    }

    private fun initRemoteConfig() {

        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(200)
            .build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)

        maxInterstitialSession = mFireBaseRemoteConfig.getDouble(RemoteConfigConstants.MAX_INTERSTITIAL_SESSION).toInt()


    }

    private fun setDialogBuilder() {
        dialog = getClearDataDialog(this)
        dialog.setOnKeyListener { dialog, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog?.dismiss()
                true
            }
            false
        }
    }

    private fun setConversationData() {
        val mLayoutManager = LinearLayoutManager(this)
        recycler_conversation.layoutManager = mLayoutManager
        conversationAdapter = ConversationAdapter()
        conversationAdapter?.setAdapterListener(this)
        recycler_conversation.adapter = conversationAdapter

        if (origin == CONVERSATION_ORIGIN_MAIN) {
            val dataList = database.conversationDao().all
            Handler(Looper.getMainLooper()).postDelayed({
                runOnUiThread {
                    dataList.observe(this, {
                        conversationDataList = it as MutableList<ConversationModel>
                        conversationAdapter?.setConversation(it)
                        layout_progress.visibility = View.GONE
                        if (!it.isNullOrEmpty()) {
                            layout_conversations_list.visibility = View.VISIBLE
                            iv_save_chat.visibility = View.VISIBLE
                            recycler_conversation.layoutManager?.smoothScrollToPosition(
                                recycler_conversation,
                                null,
                                it.size - 1
                            )
                        } else {
                            layout_conversations_list.visibility = View.GONE
                            iv_save_chat.visibility = View.GONE
                        }
                        if (isFromDb) {
                            speakerLanguageCode = it[it.size - 1].translatedWordLangCode
                            translation = it[it.size - 1].translatedWord
                            if (isSpeakerVisible(speakerLanguageCode)) {
                                speakTranslation(translation, speakerLanguageCode)
                            } else {
                                isMicClicked = false
                            }
                        }
                    })
                    layout_progress.visibility = View.GONE
                }
            }, 500)
        } else {
            if (!conversationDataList.isNullOrEmpty()) {
                layout_conversations_list.visibility = View.VISIBLE
                iv_save_chat.visibility = View.VISIBLE
                conversationAdapter?.setConversation(conversationDataList)

                recycler_conversation.layoutManager?.smoothScrollToPosition(
                    recycler_conversation,
                    null,
                    conversationDataList.size - 1
                )

            }
            layout_progress.visibility = View.GONE
        }


    }


    private fun setLanguages() {
        tv_from_lang_conversation.text = fromLang
        tv_to_lang_conversation.text = toLang

    }

    private fun getSelectedLanguages() {
        fromLang = getPrefString(LANG_FROM)
        if (fromLang == "") {
            fromLang = "English"
            putPrefString(LANG_FROM, fromLang)
        }
        fromLangCode = getPrefString(LANG_FROM_CODE)
        if (fromLangCode == "") {
            fromLangCode = "en-GB"
            putPrefString(LANG_FROM_CODE, fromLangCode)

        }

        fromLangCodeSupport = getPrefString(LANG_FROM_CODE_SUPPORT)
        if (fromLangCodeSupport == "") {
            fromLangCodeSupport = "en"
            putPrefString(LANG_FROM_CODE_SUPPORT, fromLangCodeSupport)

        }

        fromLangMeaning = getPrefString(LANG_FROM_MEANING)
        if (fromLangMeaning == "") {
            fromLangMeaning = "English"
            putPrefString(LANG_FROM_MEANING, fromLangMeaning)
        }

        toLangMeaning = getPrefString(LANG_TO_MEANING)
        if (toLangMeaning == "") {
            toLangMeaning = "Français"
            putPrefString(LANG_TO_MEANING, toLangMeaning)
        }

        toLang = getPrefString(LANG_TO)
        if (toLang == "") {
            toLang = "French"
            putPrefString(LANG_TO, toLang)

        }

        toLangCode = getPrefString(LANG_TO_CODE)
        if (toLangCode == "") {
            toLangCode = "fr-FR"
            putPrefString(LANG_TO_CODE, toLangCode)
        }
        toLangCodeSupport = getPrefString(LANG_TO_CODE_SUPPORT)

        if (toLangCodeSupport == "") {
            toLangCodeSupport = "fr"
            putPrefString(LANG_TO_CODE_SUPPORT, toLangCodeSupport)
        }
        setLanguages()
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


        putPrefString(LANG_FROM, fromLang)
        putPrefString(LANG_FROM_CODE, fromLangCode)
        putPrefString(LANG_FROM_CODE_SUPPORT, fromLangCodeSupport)
        putPrefString(LANG_FROM_MEANING, fromLangMeaning)


        putPrefString(LANG_TO, toLang)
        putPrefString(LANG_TO_CODE, toLangCode)
        putPrefString(LANG_TO_CODE_SUPPORT, toLangCodeSupport)
        putPrefString(LANG_TO_MEANING, toLangMeaning)


        setLanguages()

    }

    private fun initMic(langCode: String) {
        when (langCode) {
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

            else -> {
                showToast(this, "This language does not support speech recognition.")
                isMicClicked = false
            }
        }

    }

    private fun speakIn(s: String) {
//        if (isAppInstalled("com.google.android.googlequicksearchbox")) {
        callRecognizer(s)
//        } else {
//            showToast(
//                this,
//                "You need to install Google App to use this feature. Please install and try again."
//            )
//
//        }

    }

    private fun callRecognizer(s: String) {


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
        isMicClicked = true
        putPrefBoolean("app_killed", true)


        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(this, resources.getString(R.string.stt_error_device))
        }

        val bundle = Bundle()
        FirebaseAnalytics.getInstance(this).logEvent("speech_recognition_engine_google", bundle)

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

    private fun insertHistory(
        inputWord: String,
        translatedWord: String,
        toLangCode: String
    ) {
        val conversationModel = ConversationModel()
        conversationModel.inputWord = inputWord
        conversationModel.translatedWord = translatedWord
        if (clickType == ClickType.FROM) {
            conversationModel.origin = "from"
            conversationModel.inputWordLangCode = fromLangCodeSupport
            conversationModel.translatedWordLangCode = toLangCodeSupport

        } else {
            conversationModel.origin = "to"
            conversationModel.inputWordLangCode = toLangCodeSupport
            conversationModel.translatedWordLangCode = fromLangCodeSupport
        }

        isFromDb = true
        database.conversationDao().insert(conversationModel)

        if (origin == CONVERSATION_ORIGIN_LIST) {
            conversationDataList.add(conversationModel)
            conversationAdapter?.setConversation(conversationDataList)

            recycler_conversation.layoutManager?.smoothScrollToPosition(
                recycler_conversation,
                null,
                conversationDataList.size - 1
            )

            speakerLanguageCode =
                conversationDataList[conversationDataList.size - 1].translatedWordLangCode
            translation = conversationDataList[conversationDataList.size - 1].translatedWord
            layout_progress.visibility = View.GONE

            Handler().postDelayed({
                if (isSpeakerVisible(speakerLanguageCode)) {
                    speakTranslation(translation, speakerLanguageCode)

                } else {
                    isMicClicked = false
                }
            }, 500)


        }

//        speakWord(toLangCode, translatedWord)

    }

    private fun callTranslationResult(
        inputSentence: String,
        srcLang: String,
        targetLang: String
    ) {

        callConversationSearch(inputSentence, srcLang, targetLang)


    }

    private fun callConversationSearch(
        recognizedWord: String,
        fromLangCode: String,
        toLangCode: String
    ) {
        layout_progress.visibility = View.VISIBLE


        if (translationUtils != null) {
            translationUtils?.StopBackground()
        }
        translationUtils = TranslationUtils(object : TranslationUtils.ResultCallBack {
            override fun onFailedResult() {
                handleErrorResponse("Network error, please check the network and try again")
            }

            override fun onReceiveResult(result: String?) {
                callHandlerForPref()
                insertHistory(recognizedWord, result!!, toLangCode)

            }
        }, recognizedWord, fromLangCode, toLangCode)
        translationUtils!!.execute()

    }

    private fun handleErrorResponse(message: String) {
        layout_progress.visibility = View.GONE
        isMicClicked = false
        showToast(this, message)
        callHandlerForPref()


    }


    private fun setClickListeners() {
        iv_switch_lang.setOnClickListener(this)
        iv_mic_from_conversation.setOnClickListener(this)
        iv_mic_to_conversation.setOnClickListener(this)
        tv_from_lang_conversation.setOnClickListener(this)
        tv_to_lang_conversation.setOnClickListener(this)
        iv_clear_all.setOnClickListener(this)
        ivToolbarIcon.setOnClickListener(this)
        iv_save_chat.setOnClickListener(this)
        iv_clear_selected_convo.setOnClickListener(this)
        iv_delete_selected_convo.setOnClickListener(this)
    }

    private fun initSaveDialog() {
        saveConversationDialog = getSaveDialog(this)
        saveConversationDialog?.let { dialog ->
            val etInputField: AppCompatEditText =
                dialog.findViewById(R.id.et_input_name_conversation)
            val buttonSave: Button = dialog.findViewById(R.id.btn_save_conversation)
            val buttonCancel: Button = dialog.findViewById(R.id.btn_save_cancel)

            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }
            buttonSave.setOnClickListener {
                if (etInputField.text.toString().trim().isNotEmpty() || etInputField.text.toString()
                        .trim() != ""
                ) {
                    val name = etInputField.text.toString().trim()
                    val jsonData = getJsonConversationFromList(conversationDataList)
                    val savedChatModel = SavedChat(name, jsonData, conversationDataList.size)
                    savePersonalConversation(savedChatModel)
                    dialog.dismiss()


                } else {
                    showToast(this, getString(R.string.message_set_name))
                }
            }

        }
    }

    private fun savePersonalConversation(savedChatModel: SavedChat) {
        database.savedChatDao().insert(savedChatModel)

    }

    private fun showAlertDialog() {
        dialog.title_dialog.text = "Do you want to clear all conversation?"
        dialog.btn_yes.setOnClickListener {
            isFromDb = false
            if (mTts != null) {
                mTts?.stop()
            }

            database.conversationDao().deleteAll()
            dialog.dismiss()
        }
        dialog.btn_no.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()


    }


    override fun onClick(v: View?) {
        if (v != null) {
            if (v.id == R.id.iv_switch_lang) {
                switchLanguages()
            } else if (v.id == R.id.iv_mic_from_conversation) {
                if (isDoubleClick()) {
                    if (isSpeaking || isMicClicked) {
//                    showToast(this, "Please wait")
                        if (mTts != null) {
                            mTts?.stop()
                            isSpeaking = false
                        }
                        clickType = ClickType.FROM
                        initMic(fromLangCodeSupport)

                    } else {
                        clickType = ClickType.FROM
                        initMic(fromLangCodeSupport)
                    }
                }

            } else if (v.id == R.id.iv_mic_to_conversation) {
                if (isDoubleClick()) {
                    if (isSpeaking || isMicClicked) {
                        if (mTts != null) {
                            mTts?.stop()
                            isSpeaking = false
                        }
                        clickType = ClickType.TO
                        initMic(toLangCodeSupport)
                    } else {
                        clickType = ClickType.TO
                        initMic(toLangCodeSupport)
                    }
                }
            } else if (v.id == R.id.tv_to_lang_conversation) {
                if (isDoubleClick()) {
                    if (isSpeaking) {
                        isSpeaking = false
                        mTts?.stop()
                        startLanguageActivity(this, LANG_TO, LANG_TYPE_NORMAL)

                    } else {
                        startLanguageActivity(this, LANG_TO, LANG_TYPE_NORMAL)

                    }
                }

            } else if (v.id == R.id.tv_from_lang_conversation) {
                if (isDoubleClick()) {
                    if (isSpeaking) {
                        isSpeaking = false
                        mTts?.stop()
                        startLanguageActivity(this, LANG_FROM, LANG_TYPE_NORMAL)

                    } else {
                        startLanguageActivity(this, LANG_FROM, LANG_TYPE_NORMAL)

                    }
                }
            } else if (v.id == R.id.iv_clear_all) {
                if (isDoubleClick()) {

                    showInterstitial{
                        showAlertDialog()
                    }
                }



            } else if (v.id == R.id.ivToolbarIcon) {
                if (isDoubleClick())
                    onBackPressed()
            } else if (v.id == R.id.iv_save_chat) {
                showInterstitial{
                    saveConversationDialog?.show()
                }

            } else if (v.id == R.id.iv_clear_selected_convo) {
                conversationAdapter?.clearSelection()
            } else if (v.id == R.id.iv_delete_selected_convo) {
                deleteSelectedConversations()
            }
        }
    }

    private fun deleteSelectedConversations() {
        isFromDb = false
        if (mTts != null) {
            mTts?.stop()
        }

        CoroutineScope(Dispatchers.IO).launch {
            kotlin.runCatching {
                conversationAdapter?.let { adapter ->
                    val conversations = adapter.getAllSelectedItems()
                    for (convo in conversations) {
                        database.conversationDao().delete(convo)
                    }
                }
                withContext(Dispatchers.Main) {
                    conversationAdapter?.clearSelection()
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode != Activity.RESULT_CANCELED) {
                if (data != null) {
                    val recognizedResult =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    recognizedWord = recognizedResult!![0]
                    // showProgressbar
                    if (recognizedWord != null) {

                        if (clickType == ClickType.FROM) {
                            callTranslationResult(
                                recognizedWord,
                                fromLangCodeSupport,
                                toLangCodeSupport
                            )
                        } else {
                            callTranslationResult(
                                recognizedWord,
                                toLangCodeSupport,
                                fromLangCodeSupport
                            )
                        }

                    } else {
                        isMicClicked = false
                        // hideProgressbar
                    }
                } else {
                    isMicClicked = false
                }
            } else {
                isMicClicked = false
            }

        } else if (requestCode == REQ_CODE_LANGUAGE_SELECTION) {
            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")
                val languageName = data.getStringExtra(INTENT_KEY_LANG_NAME)
                val languageCode = data.getStringExtra(INTENT_KEY_LANG_CODE)
                val languageSupport = data.getStringExtra(INTENT_KEY_LANG_SUPPORT)
                val languageMeaning = data.getStringExtra(INTENT_KEY_LANG_MEANING)
                if (origin == LANG_FROM) {
                    fromLang = languageName!!
                    fromLangCode = languageCode!!
                    fromLangCodeSupport = languageSupport!!
                    fromLangMeaning = languageMeaning!!

                    putPrefString(LANG_FROM, fromLang)
                    putPrefString(LANG_FROM_CODE, fromLangCode)
                    putPrefString(LANG_FROM_CODE_SUPPORT, fromLangCodeSupport)
                    putPrefString(LANG_FROM_MEANING, fromLangMeaning)

                    tv_from_lang_conversation.text = fromLang
                } else {
                    toLang = languageName!!
                    toLangCode = languageCode!!
                    toLangCodeSupport = languageSupport!!
                    toLangMeaning = languageMeaning!!

                    putPrefString(LANG_TO, toLang)
                    putPrefString(LANG_TO_CODE, toLangCode)
                    putPrefString(LANG_TO_CODE_SUPPORT, toLangCodeSupport)
                    putPrefString(LANG_TO_MEANING, toLangMeaning)

                    tv_to_lang_conversation.text = toLang

                }
            }
        }
    }

//    private fun speakWord(targetLang: String, translation: String) {
//
//        this.speakerLanguageCode = targetLang
//        this.translation = translation
//        isFromDb = true
//    }

    override fun onSpeakerClicked(word: String?, langCode: String?) {
        if (isSpeaking) {
            mTts?.stop()
            isSpeaking = false
        } else {
            speakTranslation(word, langCode)

        }

    }

    override fun onCopy(word: String?) {
        val clipData = ClipData.newPlainText("Source Text", word)
        putPrefBoolean("is_from_app", true)
        // Set it as primary clip data to copy text to system clipboard.
        clipboardManager?.setPrimaryClip(clipData)
        showToast(this, "Text Copied")

    }

    override fun onChatLongClick(pos: Int) {
        conversationAdapter?.let { adapter ->
            if (!adapter.isSelection())
                adapter.setSelection(true)
            adapter.setChecked(pos)
        }
    }

    override fun onSingleClick(model: ConversationModel?, position: Int) {
        conversationAdapter?.let { adapter ->
            if (adapter.isSelection()) {
                adapter.setChecked(position)
            }
        }
    }

    override fun onSelectionChange(selection: Boolean) {
        if (selection) {
            toolbar_conversation_first.visibility = View.GONE
            toolbar_conversation_second.visibility = View.VISIBLE
        } else {
            toolbar_conversation_first.visibility = View.VISIBLE
            toolbar_conversation_second.visibility = View.GONE
        }
    }

    override fun onSelectItem(selection: Boolean, selectedItem: Int) {
        if (selection) {
            toolbar_conversation_first.visibility = View.GONE
            toolbar_conversation_second.visibility = View.VISIBLE
            val count = conversationAdapter!!.getAllSelectedItems().size
            tv_label_selected_convo_count.text =
                "$count ${getString(R.string.label_selected_total)}"
        } else {
            toolbar_conversation_first.visibility = View.VISIBLE
            toolbar_conversation_second.visibility = View.GONE
        }
    }

    private fun speakTranslation(word: String?, targetLang: String?) {
        isSpeaking = true

        when (targetLang) {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mTts?.voice = getVoice()
                }

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
            "ar" -> {
                langLocal = Locale("ar", "SA")
                mTts?.language = langLocal
            }
            "ur" -> {
                langLocal = Locale("ur", "PK")
                mTts?.language = langLocal
            }

            else -> {
                langLocal = Locale(targetLang!!)
                mTts?.language = langLocal
            }
        }

        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        mTts?.speak(word, TextToSpeech.QUEUE_FLUSH, map)

    }

    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
            mTts?.setSpeechRate(0.8f)
            mTts?.setPitch(1f)
            mTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {


                }

                override fun onDone(utteranceId: String) {
                    isSpeaking = false
                    isMicClicked = false


                }

                override fun onError(utteranceId: String) {
                    isSpeaking = false
                    isMicClicked = false


                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    super.onStop(utteranceId, interrupted)

                    isSpeaking = false
                    isMicClicked = false


                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    super.onError(utteranceId, errorCode)
                    isSpeaking = false
                    isMicClicked = false


                }

                override fun onBeginSynthesis(
                    utteranceId: String?,
                    sampleRateInHz: Int,
                    audioFormat: Int,
                    channelCount: Int
                ) {
                    super.onBeginSynthesis(utteranceId, sampleRateInHz, audioFormat, channelCount)
                    Log.e("tts onBeginSynthesis", "" + utteranceId)

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

    override fun onResume() {
        super.onResume()
        mTts = TextToSpeech(this, this, "com.google.android.tts")
    }

    override fun onPause() {
        super.onPause()
        if (mTts != null) {
            mTts?.stop()
            mTts?.shutdown()

        }
        if (translationUtils != null) {
            translationUtils!!.StopBackground()
        }
    }

    /* override fun onInterstitialAdLoaded() {
     }

     override fun onInterstitialAdClose() {
         database.conversationDao().deleteAll()


     }

     override fun onInterstitialAdFailed() {
     }

     override fun onAdOpened() {
     }*/

    private fun isDoubleClick(): Boolean {
        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.transit_none, R.anim.transit_top_bottom)

    }




    private fun loadBannerAd(){

        if (!isPremium()) {
            try {
                MaxAdManager.createBannerAd(this,native_banner_container_home_screen)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}
