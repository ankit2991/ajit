package com.translate.languagetranslator.freetranslation.activities.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.FullScreenActivity
import com.translate.languagetranslator.freetranslation.activities.details.viewModel.HistoryDetailViewModel
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.appUtils.Constants.*
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import kotlinx.android.synthetic.main.activity_history_detail.*
import kotlinx.android.synthetic.main.activity_ocr.*
import java.util.*

class HistoryDetailActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private val TAG = "History Detail"
    private var mTts: TextToSpeech? = null
    private var isSpeaking = false
    private var clipboardService: Any? = null
    private var clipboardManager: ClipboardManager? = null
    private var isOutPutSpeakAble: Boolean = false
    private var isInPutSpeakAble: Boolean = false
    private var translatedWordLangCode: String? = null
    private var translatedWord: String? = null
    private var inputWord: String? = null
    private var inputLangCode: String? = null
    private var isFavorite: Boolean = false
    private var unique_id: String? = null
    private var itemId: Int? = null
    private var source: String? = null
    private lateinit var langLocal: Locale

    val viewModel: HistoryDetailViewModel by lazy {
        ViewModelProviders.of(this).get(HistoryDetailViewModel::class.java)
    }


/*    private lateinit var admobUtils: AdmobUtils
    private lateinit var facebookAdsUtils: FacebookAdsUtils*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_detail)

        intiRemoteConfig()
        getIntentData()
        setToolbar()

//        initAds()
        mTts = TextToSpeech(this, this, "com.google.android.tts")

        clipboardService = getSystemService(CLIPBOARD_SERVICE)
        clipboardManager = clipboardService as ClipboardManager

    }

    private fun intiRemoteConfig() {
        val mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(200)
            .build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)

    }


    private fun setToolbar() {

        if (source.equals(DETAIL_SOURCE_MAIN)) {
            title_toolbar.text = "History Detail"
        } else {
            title_toolbar.text = "Detail Result"

        }
        iv_toolbar_back_history_detail.setOnClickListener {
            onBackPressed()
        }

        animatePro()
        setClickListeners()

    }

    private fun animatePro() {
        if (!isPremium()) {
            Handler().postDelayed({ AnimUtils.zoomInOut(applicationContext, iv_crown_detail) }, 600)

        } else {
            iv_crown_detail.visibility = View.GONE
        }


    }

    private fun setClickListeners() {

        iv_speaker_translated_word_history.setOnClickListener {
            if (isOutPutSpeakAble) {
                onSpeakerClicked()
            } else {
                showToast(this, "This language does not support text to speech")
            }
        }

        iv_speak_history_input.setOnClickListener {
            if (isInPutSpeakAble) {
                if (isSpeaking) {
                    mTts?.stop()
                    isSpeaking = false
                } else {

                    speakWord(inputLangCode!!, inputWord!!)
                }
            } else {
                showToast(this, "This language does not support text to speech")
            }
        }
        iv_copy_history_input.setOnClickListener {
            val inputWord = tv_history_input_word.text.toString().trim()
            copyWord(inputWord)
        }

        iv_more_options_history.setOnClickListener {
            showPopUpView(it)
        }

        iv_toggle_favorite_history.setOnClickListener {
            toggleFavorite()
        }
    }


    private fun showPopUpView(view: View) {
        val popup = PopUpUtils.getPopUpView(view)

        popup.menuInflater.inflate(R.menu.menu_history, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.getItemId()) {
                R.id.action_share -> shareOutPut()
                R.id.action_copy -> callCopyText()
                R.id.action_full_screen -> callFullScreen()
            }
            true
        }
        popup.show()
    }

    private fun callFullScreen() {
        val translatedWord = tv_history_output_word.text.toString()

        val intent = Intent(this, FullScreenActivity::class.java)
        intent.putExtra("translation", translatedWord)
        startActivity(intent)
    }

    private fun callCopyText() {
        val translatedWord = tv_history_output_word.text.toString()
        copyWord(translatedWord)
    }

    private fun shareOutPut() {
        val translatedWord = tv_history_output_word.text.toString()
        viewModel.shareResult(this, translatedWord)
    }

    private fun copyWord(word: String) {

        val clipData = ClipData.newPlainText("Source Text", word)
        putPrefBoolean("is_from_app", true)
        clipboardManager?.setPrimaryClip(clipData)
        showToast(this, resources.getString(R.string.text_copied))
    }

    private fun onSpeakerClicked() {

        if (isSpeaking) {
            mTts?.stop()
            isSpeaking = false
        } else {

            speakWord(translatedWordLangCode!!, translatedWord!!)
        }


    }

    private fun speakWord(langCode: String, speakingWord: String) {
        val langLocal = viewModel.getLangLocale(langCode)
        mTts?.language = langLocal
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        @Suppress("DEPRECATION")
        mTts?.speak(speakingWord, TextToSpeech.QUEUE_FLUSH, map)
    }


    private fun getIntentData() {
        var bundle = Bundle()
        bundle = intent.extras!!
        source = bundle.getString("source")
        val model: TranslationTable = bundle.getParcelable("detail_object")!!
        setDetailData(model)
//        setDetails(inputWord, inputLanguageName, translatedWord, translatedLanguageName)
    }

    private fun setDetailData(model: TranslationTable) {
        inputWord = model.inputStr
        translatedWord = model.outputStr
        inputLangCode = model.getInputLanguage()
        translatedWordLangCode = model.getOutputLanguage()
        isFavorite = model.isfav
        unique_id = model.unique_iden
        itemId = model.id


        val inputLanguageName = viewModel.getLanguageName(inputLangCode!!)
        val translatedLanguageName = viewModel.getLanguageName(translatedWordLangCode!!)
        val inputMeaning = viewModel.getLanguageMeaning(inputLangCode!!)
        val outPutMeaning = viewModel.getLanguageMeaning(translatedWordLangCode!!)

        isOutPutSpeakAble = if (isSpeakerVisible(translatedWordLangCode!!)) {
            iv_speaker_translated_word_history.setImageResource(R.drawable.ic_speak_word)
            true
        } else {
            iv_speaker_translated_word_history.setImageResource(R.drawable.ic_speak_disabled)
            false
        }

        isInPutSpeakAble = if (isSpeakerVisible(inputLangCode!!)) {
            iv_speak_history_input.setImageResource(R.drawable.ic_speak_word)
            true
        } else {
            iv_speak_history_input.setImageResource(R.drawable.ic_speak_disabled)

            false
        }

        tv_lang_detail_input.text = "$inputLanguageName ( $inputMeaning )"
        tv_history_input_word.text = inputWord
        tv_lang_detail_output.text = "$translatedLanguageName ( $outPutMeaning )"
        tv_history_output_word.text = translatedWord

        setFavoriteImage()

    }

    private fun setFavoriteImage() {
        if (isFavorite) {
            iv_toggle_favorite_history.setImageResource(R.drawable.ic_favorite)
        } else {
            iv_toggle_favorite_history.setImageResource(R.drawable.ic_un_favorite)

        }
    }

    private fun toggleFavorite() {
        isFavorite = if (isFavorite) {
            iv_toggle_favorite_history.setImageResource(R.drawable.ic_un_favorite)
            false
        } else {
            iv_toggle_favorite_history.setImageResource(R.drawable.ic_favorite)
            true
        }
        unique_id?.let {
            viewModel.setFavorite(isFavorite, it)
        } ?: run {
            itemId?.let { id ->
                viewModel.setFavoriteById(isFavorite, id)

            }
        }
    }

//    private fun setDetails(
//        inputWord: String?,
//        inputLanguageName: String,
//        translatedWord: String?,
//        translatedLanguageName: String
//    ) {
//        tv_lang_detail_input.text = inputLanguageName
//        tv_history_input_word.text = inputWord
//        tv_lang_detail_output.text = translatedLanguageName
//        tv_history_output_word.text = translatedWord
//
//        container_detail_input.visibility = View.VISIBLE
//        container_detail_output.visibility = View.VISIBLE
//
//
//    }


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
                            this@HistoryDetailActivity,
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
                            this@HistoryDetailActivity,
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

    override fun onPause() {
        super.onPause()
        mTts?.let { tts ->

            if (tts.isSpeaking)
                tts.stop()
            tts.shutdown()
        }

    }

    override fun onResume() {
        super.onResume()
    }
}
