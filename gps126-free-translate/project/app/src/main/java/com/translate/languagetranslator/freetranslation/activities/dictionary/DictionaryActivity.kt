package com.translate.languagetranslator.freetranslation.activities.dictionary

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.code4rox.adsmanager.MaxAdManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.dictionary.adapter.SynonymsAdapter
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.activities.mainScreen.MainActivity
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.appUtils.Constants.*
import com.translate.languagetranslator.freetranslation.interfaces.AdLoadedCallback
import com.translate.languagetranslator.freetranslation.interfaces.LanguageSelectedInterface
import com.translate.languagetranslator.freetranslation.models.dictionaryModel.Definition
import com.translate.languagetranslator.freetranslation.models.dictionaryModel.Dictionary
import com.translate.languagetranslator.freetranslation.models.dictionaryModel.Meaning
import com.translate.languagetranslator.freetranslation.models.translationModel.ErrorFromApi
import com.translate.languagetranslator.freetranslation.network.NetworkController
import com.translate.languagetranslator.freetranslation.utils.AdsUtill
import com.translate.languagetranslator.freetranslation.viewmodels.DictionaryActivityViewModel
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_dictionary.*
import kotlinx.android.synthetic.main.activity_dictionary.native_banner_container_home_screen
import kotlinx.android.synthetic.main.activity_ocr.*
import kotlinx.android.synthetic.main.appbar_home.*
import kotlinx.android.synthetic.main.dictionary_language_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DictionaryActivity : AppCompatActivity(), View.OnClickListener, LanguageSelectedInterface,
    TextToSpeech.OnInitListener {
    private var isTextAvailable: Boolean = false
    var languageCode: String? = null
    var languageName: String? = null
    var isDefinitionAvailable = false
    var isExampleAvailable = false
    var isSynonymsAvailable = false
    var isPhoneticAvailable = false
    var isOriginAvailable = false
    private lateinit var mTts: TextToSpeech
    private lateinit var langLocal: Locale
    private var isSpeaking: Boolean = false
    private var maxInterstitialSession: Int = 0
    private lateinit var adsUtill: AdsUtill
    private var clipboardService: Any? = null
    private var clipboardManager: ClipboardManager? = null
    /* private  var admobUtils: AdmobUtils ?= null
     private  var facebookAdsUtils: FacebookAdsUtils?=null*/


    private lateinit var mFireBaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var configSettings: FirebaseRemoteConfigSettings

    /* private  var admobFirstAd: AdmobUtils ?=null
     private  var facebookFirstAd: FacebookAdsUtils?=null
     private  var admobSecondtAd: AdmobUtils?=null
     private  var facebookSecondAd: FacebookAdsUtils?=null*/
    private var isFirstAd = true
    private var mLastClickTime: Long = 0
    private var languageDialog: Dialog? = null
    val viewModel: DictionaryActivityViewModel by viewModel()
    //var backInterstitialAd: Any? = null
    var showBackInterstitial = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictionary)
        adsUtill= AdsUtill(this)
        (application as AppBase).isOnMainMenu=true
        initRemoteConfig()
        loadBannerAd()
        Logging.adjustEvent("7dx5fb", Logging.currentTime(),"Dictionary")

        (application as AppBase).thisActivity = true
        clipboardService = getSystemService(CLIPBOARD_SERVICE)
        clipboardManager = clipboardService as ClipboardManager
        languageCode = getPrefString(DICTIONARY_LANG_CODE)
        languageName = getPrefString(DICTIONARY_LANG_NAME)

        if (languageCode == "") {
            languageCode = "en"
            putPrefString(DICTIONARY_LANG_CODE, languageCode!!)
        }
        if (languageName == "") {
            languageName = "English"
            putPrefString(DICTIONARY_LANG_NAME, languageName!!)

        }
        tv_lang_dic.text = languageName
        viewModel.dictionary_back_interstitial().observe(this, androidx.lifecycle.Observer {
            showBackInterstitial = it.show && !viewModel.isAutoAdsRemoved()
            /*if (it.show && !viewModel.isAutoAdsRemoved()){
                getInterstitialAdObject(
                    getString(R.string.trns_btn_admob_inter_ad1),
                    getString(R.string.trns_btn_fb_inter_ad1),
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
//        initLanguageDialog()

        setClickListeners()
        setInputChangeListener()
//        initAds()

   /*     Handler().postDelayed({
            if (!isPremium()) {
                if (IronSource.isInterstitialReady()) {
                    if ((application as AppBase).interstitialSessionCount < maxInterstitialSession) {
                        (application as AppBase).interstitialSessionCount++
                        IronSource.showInterstitial()
                    }
                }
            }
        }, 3000)
*/
//        incrementInterstitialCount()

        /*if (showInstratial){
            if (!isPremium()) {
                if (IronSource.isInterstitialReady()) {
                    IronSource.showInterstitial()
                    (application as AppBase).addInterstitialSessionCount=0
                }
            }
        }*/

    }

    override fun onBackPressed() {
        try {
            super.onBackPressed()
            //        TODO()4.5.0  banner was being destroyed here

            //adsUtill.destroyBanner()
            HomeActivity.showBanner=true
            setResult(REQ_CODE_RATEUS,Intent())
        } catch (e: Exception) {
            e.printStackTrace()
        }
      /*  if(!isPremium()) {
            if (showBackInterstitial) {
                IronSource.setInterstitialListener(getInterstitialListener({
                    //onAddLoaded
                    if (it) {
                        if((application as AppBase).thisActivity){
                        if ((application as AppBase).interstitialSessionCount < maxInterstitialSession) {
                            showBackInterstitial = false
                            (application as AppBase).interstitialSessionCount++

                            IronSource.showInterstitial()
                        }}
                    } else
                        try {
                            super.onBackPressed()
                            setResult(REQ_CODE_RATEUS,Intent())
                        } catch (e: Exception) {
                        }
                }, {
                    //onAdClosed
                }))
                IronSource.loadInterstitial();
                super.onBackPressed()
                setResult(REQ_CODE_RATEUS,Intent())
            } else
                try {
                    super.onBackPressed()
                    setResult(REQ_CODE_RATEUS,Intent())
                } catch (e: Exception) {
                }
        }else{
            try {
                super.onBackPressed()
                setResult(REQ_CODE_RATEUS,Intent())
            } catch (e: Exception) {
            }
        }*/
    }



    private fun initRemoteConfig() {
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        configSettings =
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(200).build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)

        maxInterstitialSession = mFireBaseRemoteConfig.getDouble(RemoteConfigConstants.MAX_INTERSTITIAL_SESSION).toInt()


    }


    private fun setInputChangeListener() {
        et_input_dictionary.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) {
                    iv_clear_input_dictionary.visibility = View.VISIBLE
                    isTextAvailable = true
                } else {
                    iv_clear_input_dictionary.visibility = View.GONE
                    isTextAvailable = false


                }
            }
        })
    }

    private fun performDictionarySearch() {

        triggerSearch()


    }

    private fun triggerSearch() {
        val inputWord = et_input_dictionary.text.toString().trim()
        hideSoftKeyboard()
        getDictionaryData(inputWord, "en")

    }

    private fun hideSoftKeyboard() {
        (Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE)) as InputMethodManager).hideSoftInputFromWindow(
            et_input_dictionary.applicationWindowToken,
            0
        )
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        et_input_dictionary.clearFocus()
    }

    private fun handleErrorResponse(message: String) {
        layout_progress.visibility = View.GONE
        showToast(this, message)

    }

    private fun getDictionaryData(word: String, lang: String) {

        isDefinitionAvailable = false
        isExampleAvailable = false
        isSynonymsAvailable = false
        if (isSpeaking) {
            mTts.stop()
            isSpeaking = false
        }

        layout_progress.visibility = View.VISIBLE
        val url = getUrl(word, languageCode!!)
        NetworkController.callDictionary(url).enqueue(object : Callback<List<Dictionary>> {
            override fun onFailure(call: Call<List<Dictionary>>, t: Throwable) {
                Log.e("dasd", "" + t.localizedMessage)
                handleErrorResponse(t.localizedMessage)

            }

            override fun onResponse(
                call: Call<List<Dictionary>>,
                response: Response<List<Dictionary>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val word = body[0].word
                        val phonetic = body[0].phonetic
                        val origin = body[0].origin
                        val meanings = body[0].meanings
                        try {
                            if (word != null) {
                                setWord(word)
                            }

                            if (meanings != null) {
                                setMeanings(meanings)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                } else {
                    val error = response.errorBody()!!.string()
                    val gson = Gson()
                    val error_ = gson.fromJson(error, ErrorFromApi::class.java)
                    handleErrorResponse(error_.message)
                }
            }

        })

    }


    var phoneticWord: String? = null


    var originalWord: String? = null

    private fun setWord(word: String?) {
        tv_word_original.text = word
        originalWord = word
        layout_detail_dictionary.visibility = View.VISIBLE
        layout_progress.visibility = View.GONE

    }


    private fun setMeanings(meanings: List<Meaning>) {
        val partsOfSpeech = meanings[0].partOfSpeech

        val definitions: ArrayList<Definition> = ArrayList()

        for (mean in meanings) {
            definitions.addAll(mean.definitions)
        }

        setDefinitions(definitions, partsOfSpeech)


    }

    var posWord: String? = null
    var definitionWord: String? = null
    var exampleWord: String? = null
    var synonymWord: String? = null

    private fun setDefinitions(definitions: List<Definition>?, partsOfSpeech: String?) {


        if (definitions != null && definitions.isNotEmpty()) {

            var wordDefinitionsList: ArrayList<String> = ArrayList()
            var wordexamplesList: ArrayList<String> = ArrayList()
            var wordSynonymsList: ArrayList<String> = ArrayList()

            for (def in definitions) {
                def.definition?.let { defi ->
                    wordDefinitionsList.add(defi)

                }
                def.synonyms?.let { syno ->
                    wordSynonymsList.addAll(syno)

                }
                def.example?.let { examples ->

                    wordexamplesList.add(examples)

                }
            }
            if (wordSynonymsList.isNotEmpty()) {
                if (partsOfSpeech != null && partsOfSpeech.isNotEmpty()) {
                    posWord = partsOfSpeech
                    tv_word_type.text = partsOfSpeech
                }
                val mSynoBuilder = StringBuilder()

                val synonymsAdapter = SynonymsAdapter()
                list_synonyms.adapter = synonymsAdapter
                if (wordSynonymsList.size >= 10) {
                    val newList: ArrayList<String> = ArrayList()
                    for (i in 0..9) {

                        newList.add(wordSynonymsList[i])
                        mSynoBuilder.append(wordSynonymsList[i])
                        mSynoBuilder.append(",")
                    }
                    synonymsAdapter.setData(newList)


                } else {
                    for (syno in wordSynonymsList) {
                        mSynoBuilder.append(syno)
                        mSynoBuilder.append(",")
                    }
                    synonymsAdapter.setData(wordSynonymsList)

                }
                synonymWord = mSynoBuilder.toString()
                isSynonymsAvailable = true
            }

            if (wordexamplesList.isNotEmpty()) {
                val mBuilder = StringBuilder()

                val firstExample = "1   ${wordexamplesList[0]}"
                tv_sample_sentence_first.text = firstExample
                tv_sample_sentence_first.visibility = View.VISIBLE
                mBuilder.append(firstExample)
                if (wordDefinitionsList.size > 1) {
                    val secondExample = "2   ${wordexamplesList[1]}"
                    tv_sample_sentence_second.text = secondExample
                    tv_sample_sentence_second.visibility = View.VISIBLE
                    mBuilder.append("\n")
                    mBuilder.append(secondExample)

                }
                exampleWord = mBuilder.toString()
                isExampleAvailable = true

            }

            if (wordDefinitionsList.isNotEmpty()) {
                val mBuilder = StringBuilder()
                val firstDef = "1   ${wordDefinitionsList[0]}"

                tv_web_first.text = firstDef
                tv_web_first.visibility = View.VISIBLE

                mBuilder.append(firstDef)
                if (wordDefinitionsList.size > 1) {
                    val secondDef = "2   ${wordDefinitionsList[1]}"
                    tv_web_sentence_second.text = secondDef
                    tv_web_sentence_second.visibility = View.VISIBLE
                    mBuilder.append("\n")
                    mBuilder.append(secondDef)

                }
                definitionWord = mBuilder.toString()
                isDefinitionAvailable = true

            }


        }
        if (isExampleAvailable) {
            layout_sample_sentences.visibility = View.VISIBLE


        } else {
            layout_sample_sentences.visibility = View.GONE

        }
        if (isDefinitionAvailable) {
            layout_web_definitions.visibility = View.VISIBLE
        } else {
            layout_web_definitions.visibility = View.GONE
        }
        if (isSynonymsAvailable) {
            layout_synonyms.visibility = View.VISIBLE

        } else {
            layout_synonyms.visibility = View.GONE

        }


    }

    private fun cancelDictionaryWord() {
        tv_word_original.text = ""
        tv_sample_sentence_second.text = ""
        tv_sample_sentence_first.text = ""
        tv_web_first.text = ""
        tv_web_sentence_second.text = ""
        tv_word_type.text = ""

        isDefinitionAvailable = false
        isExampleAvailable = false
        isSynonymsAvailable = false

        layout_sample_sentences.visibility = View.GONE
        layout_web_definitions.visibility = View.GONE
        layout_detail_dictionary.visibility = View.GONE
        layout_synonyms.visibility = View.GONE

        clearEditTextField()

    }

    private fun copyDictionaryWord() {
        val data = presentData()
        val clipData = ClipData.newPlainText("Source Text", data)
        clipboardManager?.setPrimaryClip(clipData)

        showToast(this, "Text Copied")

    }

    private fun presentData(): String {
        var mBuilder = StringBuilder()
        mBuilder.append("Word : ")
        mBuilder.append(originalWord)
        mBuilder.append("\n")
        if (isPhoneticAvailable) {
            mBuilder.append("Phonetic: ")
            mBuilder.append(phoneticWord)
            mBuilder.append("\n")
        }
        /* if (isOriginAvailable) {
             mBuilder.append("Origin: ")
             mBuilder.append(originWord)
             mBuilder.append("\n")
         }*/
        mBuilder.append("Type : ")
        mBuilder.append(posWord)
        mBuilder.append("\n")

        if (isDefinitionAvailable) {
            mBuilder.append("Definition : ")
            mBuilder.append(definitionWord)
            mBuilder.append("\n")
        }
        if (isExampleAvailable) {
            mBuilder.append("Example : ")
            mBuilder.append(exampleWord)
            mBuilder.append("\n")
        }
        if (isSynonymsAvailable) {
            mBuilder.append("Synonyms : ")
            mBuilder.append(synonymWord)
            mBuilder.append("\n")
        }

        return mBuilder.toString()

    }


    private fun getUrl(word: String, lang: String): String {
        return "https://api.dictionaryapi.dev/api/v2/entries/$lang/$word"

    }

    private fun clearEditTextField() {
        et_input_dictionary.setText("")
    }

    private fun openLanguageSheet() {
        val dictionaryLanguageFragment = DictionaryLanguageFragment.newInstance()
        dictionaryLanguageFragment.setInterface(this)
        dictionaryLanguageFragment.show(supportFragmentManager, "fragment")

//        languageDialog?.show()

    }

    private fun setClickListeners() {
        iv_clear_input_dictionary.setOnClickListener(this)
//        btn_search_dictionary.setOnClickListener(this)
        iv_arrow_back_dic.setOnClickListener(this)
        layout_language_select_dictionary.setOnClickListener(this)

        iv_translate_dictionary_word.setOnClickListener(this)
        iv_cancel_dictionary.setOnClickListener(this)
        iv_share_dictionary_word.setOnClickListener(this)
        iv_speak_dictionary_word.setOnClickListener(this)
        iv_copy_dictionary_word.setOnClickListener(this)

        et_input_dictionary.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (isTextAvailable) {
                    val inputWord = et_input_dictionary.text.toString().trim()
                    if (Constants.containsWhiteSpace(inputWord)) {
                        showToast(this@DictionaryActivity, "Please enter single word only")
                    } else {
                        performDictionarySearch()

                    }

                } else {
                    showToast(this@DictionaryActivity, "Enter Single word")
                }
            }
            true
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            if (v.id == R.id.iv_copy_dictionary_word) {
                if (isDoubleClick())
                    copyDictionaryWord()
            } else if (v.id == R.id.iv_speak_dictionary_word) {
                if (isDoubleClick())
                    speakDictionaryWord()
            } else if (v.id == R.id.iv_share_dictionary_word) {
                if (isDoubleClick())
                    shareDictionaryWord()
            } else if (v.id == R.id.iv_cancel_dictionary) {
                if (isDoubleClick())
                    cancelDictionaryWord()
            } else if (v.id == R.id.iv_translate_dictionary_word) {
                if (isDoubleClick())
                    translateDictionaryWord()
            } else if (v.id == R.id.iv_clear_input_dictionary) {
                if (isDoubleClick())
                    clearEditTextField()
            } else if (v.id == R.id.iv_arrow_back_dic) {
                onBackPressed()
            } else if (v.id == R.id.layout_language_select_dictionary) {
                if (isDoubleClick())
                    openLanguageSheet()
            }
        }
    }

    private fun translateDictionaryWord() {
        val inputWord = et_input_dictionary.text.toString().trim()
        val intent = Intent(this,MainActivity::class.java)
        intent.putExtra("textFromDictionar",inputWord)
        startActivity(intent)
        finish()
        /*openTranslateActivityWithText(
            this,
            TRANSLATE_TYPE_TEXT,
            TRANSLATE_ORIGIN_NOT_MAIN,
            inputWord
        )*/
    }


    private fun shareDictionaryWord() {
        val data = presentData()

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        //        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Today i drink "+ mGlasses +" glasses ( "+mProgress + munit + " ) of water!"  );
        sharingIntent.putExtra(Intent.EXTRA_TEXT, data)
        startActivity(
            Intent.createChooser(sharingIntent, resources.getString(R.string.share_using))
        )
    }

    private fun speakDictionaryWord() {
        if (isSpeakerVisible(languageCode!!)) {
            val inputWord = et_input_dictionary.text.toString().trim()

            if (isSpeaking) {
                mTts.stop()
                isSpeaking = false
            }
            speakWord(inputWord, languageCode!!)
//            speakDictionaryWord(inputWord,languageCode!!)
        } else {
            showToast(this, "This language doesn't support text to speech")
        }
    }


    private fun speakWord(inputWord: String, languageCode: String) {
        when (languageCode) {
            "tl" -> {
                langLocal = Locale("fil", "PH")
                mTts.language = langLocal
            }
            "id" -> {
                langLocal = Locale("id", "ID")
                mTts.language = langLocal
            }
            "en" -> {
                langLocal = Locale("en", "US")
                mTts.language = langLocal
            }
            "sq" -> {
                langLocal = Locale("sq", "AL")
                mTts.language = langLocal
            }

            "fr" -> {
                mTts.language = Locale.FRANCE
            }
            "zh" -> {
//                langLocal = Locale(targetLang)
                mTts.language = Locale.CHINA
            }
            "ar" -> {
                langLocal = Locale("ar", "SA")
                mTts.language = langLocal
            }
            "ur" -> {
                langLocal = Locale("ur", "PK")
                mTts.language = langLocal
            }

            else -> {
                langLocal = Locale(languageCode)
                mTts.language = langLocal
            }
        }
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        @Suppress("DEPRECATION")
        (mTts.speak(inputWord, TextToSpeech.QUEUE_FLUSH, map))

    }


    override fun selectedLanguage(langName: String?, langCode: String?) {
        putPrefString(DICTIONARY_LANG_CODE, langCode!!)
        putPrefString(DICTIONARY_LANG_NAME, langName!!)
        this.languageName = langName
        this.languageCode = langCode
        tv_lang_dic.text = languageName


    }

    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
            mTts.setSpeechRate(0.7f)
            mTts.setPitch(1f)
            mTts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
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
                            this@DictionaryActivity,
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
                            this@DictionaryActivity,
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

            val result = mTts.setLanguage(Locale.US)
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
        if (mTts.isSpeaking)
            mTts.stop()
        mTts.shutdown()
    }

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
