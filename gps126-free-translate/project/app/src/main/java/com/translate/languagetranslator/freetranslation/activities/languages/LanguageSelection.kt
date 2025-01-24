package com.translate.languagetranslator.freetranslation.activities.languages

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.adptypaywall.PaywallUi
import com.translate.languagetranslator.freetranslation.activities.languages.adapter.LanguageAdapter
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.appUtils.Constants.*
import com.translate.languagetranslator.freetranslation.interfaces.LanguageInterface
import com.translate.languagetranslator.freetranslation.models.LanguageModel
import kotlinx.android.synthetic.main.activity_language_selection.*
import java.util.*

class LanguageSelection : AppCompatActivity(), SearchView.OnQueryTextListener, LanguageInterface,
    TextToSpeech.OnInitListener {

    lateinit var languageAdapter: LanguageAdapter
    private lateinit var langList: List<LanguageModel>
    private lateinit var origin: String
    private var type: String? = null
    private var from: String? = null
    private var isAdsOnly = true

    private var mTts: TextToSpeech? = null
    var isSpeaking: Boolean = false
    private lateinit var langLocal: Locale

    private lateinit var mFireBaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var configSettings: FirebaseRemoteConfigSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        initRemoteConfig()


        var bundle = Bundle()
        bundle = intent.extras!!
        origin = bundle.getString("origin")!!
        type = bundle.getString("type")
        from = bundle.getString("from")
        initToolbar()
        initLanguages()
    }

    private fun initRemoteConfig() {
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(200)
            .build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)


    }

    private fun initLanguages() {
        val mLayoutManager = LinearLayoutManager(this)
        recycler_language.layoutManager = mLayoutManager
        recycler_language.isNestedScrollingEnabled = false
        ViewCompat.setNestedScrollingEnabled(recycler_language, false)
        languageAdapter = LanguageAdapter()
        recycler_language.adapter = languageAdapter
        recycler_language.itemAnimator = DefaultItemAnimator()
        recycler_language.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        languageAdapter.setAdapterInterface(this)
        langList = if (type == LANG_TYPE_NORMAL)
            if (from == "fictional") {
                fetchFictionalLanguages()
            } else if (from == "phrasebook") {
                phraseBookLanguages()
            } else
                fetchLanguages()
        else
            fetchOCRLanguages()
        languageAdapter.setData(langList)
    }


    private fun initToolbar() {
        setSupportActionBar(toolbar_language)
        if (origin == Constants.LANG_FROM) {
            Objects.requireNonNull(supportActionBar)!!.title = "Translate From"
            toolbar_language.title = "Translate From"
        } else {
            Objects.requireNonNull(supportActionBar)!!.title = "Translate To"
            toolbar_language.title = "Translate To"
        }
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_language.setNavigationOnClickListener {
            onBackPressed()
        }

        if (from == "fictional" && isAdsOnly) {
            iv_crown_pro_banner.visibility = View.VISIBLE
            iv_crown_pro_banner.setOnClickListener {
//                startActivity(Intent(this, PurchaseActivity::class.java))

                val intent = Intent(
                    this@LanguageSelection,
                    PaywallUi::class.java
                )
                intent.putExtra(
                    PAYWALL_TYPE,
                    GPS_PREMIUM
                )
                startActivity(intent)
            }
        } else {
            iv_crown_pro_banner.visibility = View.GONE

        }
    }


    private fun filter(text: String) {
        val filteredList = java.util.ArrayList<LanguageModel>()
        for (la in langList) {
            if (la.languageName.toLowerCase().startsWith(text.toLowerCase())) {
                filteredList.add(la)
            }
            languageAdapter.setData(filteredList)
        }


    }

    private fun speakTranslation(translation: String, targetLang: String) {
//        var lang: Int? = null

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
                langLocal = Locale(targetLang)
                mTts?.language = langLocal
            }
        }
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        @Suppress("DEPRECATION")
        mTts?.speak(translation, TextToSpeech.QUEUE_FLUSH, map)


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_selec_lang, menu)
        val searchItem = menu!!.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search Language"
        searchView.setOnQueryTextListener(this)
        searchView.isIconified = true
        if (from == "fictional" && !isAdsOnly) {
            return false
        }
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        filter(newText!!)
        return true
    }

    override fun onSpeakerCLicked(text: String?, languageCode: String?) {
        if (isSpeaking) {
            mTts?.stop()
            isSpeaking = false
        } else {
            speakTranslation(text!!, languageCode!!)
        }

    }

    override fun onLanguageSelect(
        languageName: String?,
        languageCode: String?,
        languageSupportCode: String?,
        languageMeaning: String,
        flag: Int
    ) {
        val intent = Intent()
        intent.putExtra(INTENT_KEY_LANG_NAME, languageName)
        intent.putExtra(INTENT_KEY_LANG_CODE, languageCode)
        intent.putExtra(INTENT_KEY_LANG_SUPPORT, languageSupportCode)
        intent.putExtra(INTENT_KEY_LANG_MEANING, languageMeaning)
        intent.putExtra(INTENT_KEY_LANG_FLAG.toString(), flag)
        intent.putExtra("origin", origin)
        setResult(Constants.REQ_CODE_LANGUAGE_SELECTION, intent)
        showInterstitial {
            finish()
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
                            this@LanguageSelection,
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
                            this@LanguageSelection,
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

    override fun onResume() {
        super.onResume()
        mTts = TextToSpeech(this, this, "com.google.android.tts")
    }

    override fun onPause() {
        super.onPause()

        if (mTts?.isSpeaking!!)
            mTts?.stop()
        mTts?.shutdown()
    }

}
