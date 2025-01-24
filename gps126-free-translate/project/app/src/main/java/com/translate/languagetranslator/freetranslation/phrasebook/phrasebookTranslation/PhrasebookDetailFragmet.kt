package com.translate.languagetranslator.freetranslation.phrasebook.phrasebookTranslation

import android.app.Activity
import android.content.Intent
import android.os.*
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.code4rox.adsmanager.MaxAdManager
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.languages.LanguageSelection
import com.translate.languagetranslator.freetranslation.activities.mainScreen.viewModel.MainViewModel
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import com.translate.languagetranslator.freetranslation.interfaces.AdLoadedCallback
import com.translate.languagetranslator.freetranslation.interfaces.ClickPhrasesItem
import com.translate.languagetranslator.freetranslation.interfaces.LangCodeInterface
import com.translate.languagetranslator.freetranslation.repository.AssetsRepo
import com.translate.languagetranslator.freetranslation.utils.AdsUtill
import com.translate.languagetranslator.freetranslation.utils.Constants
import kotlinx.android.synthetic.main.activity_phrasebook.*
import kotlinx.android.synthetic.main.fragment_phrasebook_detail_fragmet.*
import kotlinx.android.synthetic.main.fragment_phrasebook_detail_fragmet.back_btn
import kotlinx.android.synthetic.main.fragment_phrasebook_detail_fragmet.native_banner_container_home_screen
import kotlinx.android.synthetic.main.fragment_phrasebook_detail_fragmet.tv_title_toolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class PhrasebookDetailFragmet : Fragment() {
    val viewModel: MainViewModel by viewModel()
    private var mLastClickTime: Long = 0
    private lateinit var fromLang: String
    private lateinit var toLang: String
    private  var fromLangFlag: Int = 0
    private  var toLangFlag: Int = 0
    private lateinit var fromLangCode: String
    private lateinit var fromLangCodeSupport: String
    private lateinit var fromLangMeaning: String
    private lateinit var toLangMeaning: String
    private lateinit var toLangCode: String
    private lateinit var toLangCodeSupport: String
    private lateinit var adsUtill: AdsUtill
    lateinit var adapter: ItemDetailAdapter

    private var newTranslationModel: TranslationTable? = null
     var mTTS:TextToSpeech? = null
    var langTo: String = ""
//    var listData = java.util.ArrayList<ItemModel>()
    var listData = ArrayList<ItemModel>()
    lateinit var dataArrayList : ArrayList<String>
    lateinit var dataArrayListTrans : ArrayList<String>
    lateinit var dataItemModel : ArrayList<ItemModel>
     var templist : ArrayList<ItemModel> =ArrayList()
    lateinit var templistArray : ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_phrasebook_detail_fragmet, container, false)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

//        mTTS = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
//            if (status != TextToSpeech.ERROR){
//                //if there is no error then set language
//                mTTS.language = Locale.UK
//            }
//        })
        adsUtill= AdsUtill(requireContext())
        getSelectedLanguages()
//        loadBannerAd()
        setupRv()
        searchItem()
        //getSelectedLanguages()
        back_btn.setOnClickListener {
                 requireActivity().onBackPressed()
        }
        input_layout_lang_from.setOnClickListener {

            if (isDoubleClick()) {
                val intent = Intent(activity, LanguageSelection::class.java)
                intent.putExtra(
                    "origin",
                    com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_PHR
                )
                intent.putExtra(
                    "type",
                    com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TYPE_NORMAL
                )
                intent.putExtra(
                    "from",
                    "phrasebook"
                )
                fromLangLauncher.launch(intent)
            }
//            if (isDoubleClick())
//                startLanguageActivity(requireActivity(), com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM, com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TYPE_NORMAL)
        }

        iv_switch_lang_input.setOnClickListener {
            switchLang()
        }

        input_layout_langs_to.setOnClickListener {
            if (isDoubleClick()) {
                val intent = Intent(activity, LanguageSelection::class.java)
                intent.putExtra(
                    "origin",
                    com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_PHR
                )
                intent.putExtra(
                    "type",
                    com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TYPE_NORMAL
                )
                intent.putExtra(
                    "from",
                    "phrasebook"
                )

                fromLangLauncher.launch(intent)

            }

//            if (isDoubleClick())
//                startLanguageActivity(requireActivity(),
//                    com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO,
//                    com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TYPE_NORMAL
//                )
        }
        if (Constants.PhraseClickedItem == R.string.essentials){
            display_img.setImageResource(R.drawable.chat)
            tv_title_toolbar.setText(R.string.essentials)
            dataList()
        }
        if (Constants.PhraseClickedItem == R.string.whiletravelling){
            display_img.setImageResource(R.drawable.airplane)
            tv_title_toolbar.setText(R.string.whiletravelling)
            getCategoryDataTravelling()
        }
        if (Constants.PhraseClickedItem == R.string.medical){
            display_img.setImageResource(R.drawable.first_aid_kit)
            tv_title_toolbar.setText(R.string.medical)
            getCategoryDataMedical()
        }
        if (Constants.PhraseClickedItem == R.string.hotel){
            display_img.setImageResource(R.drawable.hotel)
            tv_title_toolbar.setText(R.string.hotel)
            getCategoryDataHotel()
        }
        if (Constants.PhraseClickedItem == R.string.restaurant){
            display_img.setImageResource(R.drawable.food)
            tv_title_toolbar.setText(R.string.restaurant)
            getCategoryDataRestaurant()
        }
        if (Constants.PhraseClickedItem == R.string.bar){
            display_img.setImageResource(R.drawable.mirror_ball)
            tv_title_toolbar.setText(R.string.bar)
            getCategoryDataBar()
        }
        if (Constants.PhraseClickedItem == R.string.store){
            display_img.setImageResource(R.drawable.grocery_store)
            tv_title_toolbar.setText(R.string.store)
            getCategoryDataStore()
        }
        if (Constants.PhraseClickedItem == R.string.work){
            display_img.setImageResource(R.drawable.laptop_screen)
            tv_title_toolbar.setText(R.string.work)
            getCategoryDataWork()
        }
        if (Constants.PhraseClickedItem == R.string.time){
            display_img.setImageResource(R.drawable.clock)
            tv_title_toolbar.setText(R.string.time)
            getCategoryDataTime()
        }
       /* val showInstratial = requireActivity().incrementInterstitialCount()
        Handler(Looper.getMainLooper()).postDelayed({
            if (showInstratial) {
                if (!requireActivity().isPremium()) {
                    if (IronSource.isInterstitialReady()) {
                        IronSource.showInterstitial()
                        (requireActivity().application as AppBase).addInterstitialSessionCount = 0
                    }
                }
            }
        }, 3000)*/
    }

    private fun isDoubleClick(): Boolean {
        // mis-clicking prevention, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return false
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return true
    }

    private fun getSelectedLanguages() {

        fromLang = requireActivity().getPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_PHR)
        if (fromLang == "") {
            fromLang = "English"
            requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_PHR, fromLang)
        }
        fromLangFlag = requireActivity().getPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_FLAG_PHR.toString())
        if (fromLangFlag == 0) {
            fromLangFlag = R.drawable.english
            requireActivity().putPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_FLAG_PHR.toString(), fromLangFlag)
        }
        fromLangCode = requireActivity().getPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_PHR)
        if (fromLangCode == "") {
            fromLangCode = "en-GB"
            requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_PHR, fromLangCode)

        }

        fromLangCodeSupport = requireActivity().getPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_SUPPORT_PHR)
        if (fromLangCodeSupport == "") {
            fromLangCodeSupport = "en"
            requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_SUPPORT_PHR, fromLangCodeSupport)

        }

        fromLangMeaning = requireActivity().getPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_MEANING_PHR)
        if (fromLangMeaning == "") {
            fromLangMeaning = "English"
            requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_MEANING_PHR, fromLangMeaning)
        }

        toLangMeaning = requireActivity().getPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_MEANING_PHR)
        if (toLangMeaning == "") {
            toLangMeaning = "Français"
            requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_MEANING_PHR, toLangMeaning)
        }

        toLang = requireActivity().getPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_PHR)
        if (toLang == "") {
            toLang = "French"
            requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_PHR, toLang)

        }
        toLangFlag = requireActivity().getPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_FLAG_PHR.toString())
        if (toLangFlag == 0) {
            toLangFlag = R.drawable.french
            requireActivity().putPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_FLAG_PHR.toString(), toLangFlag)
        }

        toLangCode = requireActivity().getPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_PHR)
        if (toLangCode == "") {
            toLangCode = "fr-FR"
            requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_PHR, toLangCode)
        }
        toLangCodeSupport = requireActivity().getPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_SUPPORT_PHR)

        if (toLangCodeSupport == "") {
            toLangCodeSupport = "fr"
            requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_SUPPORT_PHR, toLangCodeSupport)
        }
        setLanguages()
    }

    private fun setLanguages() {
        tv_langs_from_input.text = fromLang
        tv_langs_to_input.text = toLang
        flag_to.setImageResource(toLangFlag)
        flag_from.setImageResource(fromLangFlag)
    }
    private fun switchLang() {
        val fromLangTemp = fromLang
        val fromFlagTemp = fromLangFlag
        val fromLangCodeTemp = fromLangCode
        val fromLangCodeSupportTemp = fromLangCodeSupport
        val fromLangMeaningTemp = fromLangMeaning

        val toLangTemp = toLang
        val toFlagTemp = toLangFlag
        val toLangCodeTemp = toLangCode
        val toLangCodeSupportTemp = toLangCodeSupport
        val toLangMeaningTemp = toLangMeaning

        fromLang = toLangTemp
        fromLangFlag = toFlagTemp
        fromLangCode = toLangCodeTemp
        fromLangCodeSupport = toLangCodeSupportTemp
        fromLangMeaning = toLangMeaningTemp

        toLang = fromLangTemp
        toLangFlag = fromFlagTemp
        toLangCode = fromLangCodeTemp
        toLangCodeSupport = fromLangCodeSupportTemp
        toLangMeaning = fromLangMeaningTemp

        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_PHR, fromLang)
        requireActivity().putPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_FLAG_PHR.toString(), fromLangFlag)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_PHR, fromLangCode)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_SUPPORT_PHR, fromLangCodeSupport)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_MEANING_PHR, fromLangMeaning)

        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_PHR, toLang)
        requireActivity().putPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_FLAG_PHR.toString(), toLangFlag)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_PHR, toLangCode)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_SUPPORT_PHR, toLangCodeSupport)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_MEANING_PHR, toLangMeaning)
        setLanguages()

        updateDatalist()
    }

    private fun initMicProcess() {
        if (getMicVisibility(fromLangCodeSupport)) {
            initMicInput(fromLangCode)
        } else {
            showToast(requireActivity(), "Language does not support voice to text")
        }
    }

    private fun initMicInput(fromLangCode: String) {

//        if (viewModel.isAppInstalled(this, "com.google.android.googlequicksearchbox")) {
        val micIntent = viewModel.getSpeechRecognitionIntent(fromLangCode)
        try {
            requireActivity().putPrefBoolean("app_killed", true)
            startActivityForResult(micIntent, com.translate.languagetranslator.freetranslation.appUtils.Constants.REQ_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(requireActivity(), resources.getString(R.string.stt_error_device))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == com.translate.languagetranslator.freetranslation.appUtils.Constants.REQ_CODE_LANGUAGE_SELECTION) {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")
                val languageName = data.getStringExtra(com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_NAME)
                val languageFlag = data.getIntExtra(com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_FLAG.toString(),R.drawable.english)
                val languageCode = data.getStringExtra(com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_CODE)
                val languageSupport = data.getStringExtra(com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_SUPPORT)
                val languageMeaning = data.getStringExtra(com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_MEANING)

                if (origin == com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_PHR) {
                    fromLang = languageName!!
                    fromLangFlag = languageFlag
                    fromLangCode = languageCode!!
                    fromLangCodeSupport = languageSupport!!
                    fromLangMeaning = languageMeaning!!
                    tv_langs_from_input.text = fromLang

                    flag_from.setImageResource(fromLangFlag)
                    Log.e("language",fromLang)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_PHR, fromLang)
                    requireActivity().putPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_FLAG_PHR.toString(), fromLangFlag)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_PHR, fromLangCode)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_SUPPORT_PHR, fromLangCodeSupport)
                    requireActivity(). putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_MEANING_PHR, fromLangMeaning)

                } else {
                    toLang = languageName!!
                    toLangFlag = languageFlag
                    toLangCode = languageCode!!
                    toLangCodeSupport = languageSupport!!
                    toLangMeaning = languageMeaning!!
                    tv_langs_to_input.text = toLang
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_PHR, toLang)
                    requireActivity().putPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_FLAG_PHR.toString(), toLangFlag)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_PHR, toLangCode)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_SUPPORT_PHR, toLangCodeSupport)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_MEANING_PHR, toLangMeaning)
                }
//                translateInputWord()
            }
        } else if (requestCode == com.translate.languagetranslator.freetranslation.appUtils.Constants.REQ_CODE_SPEECH_INPUT) {
            super.onActivityResult(requestCode, resultCode, data)

            if (resultCode == AppCompatActivity.RESULT_OK && null != data) {
                val recognizedResult =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val inputWord = recognizedResult?.get(0)
//                setEditTextView(inputWord!!)
            }
        } else if (requestCode == com.translate.languagetranslator.freetranslation.appUtils.Constants.REQ_CODE_BOOKMARK) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")

                val bundle = data.extras!!
                val model: TranslationTable = bundle.getParcelable("detail_object")!!
                setResultInView(model)
            }
        } else if (requestCode == com.translate.languagetranslator.freetranslation.appUtils.Constants.REQ_CODE_HISTORY) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")

                val bundle = data.extras!!
                val model: TranslationTable = bundle.getParcelable("detail_object")!!
                setResultInView(model)
            }
        } else if (requestCode == com.translate.languagetranslator.freetranslation.appUtils.Constants.REQUEST_CODE_OCR_BACK) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")

                val bundle = data.extras!!
                val model: TranslationTable = bundle.getParcelable("detail_object")!!
                setResultInView(model)
            }
        }

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
        val inputLangFlagIndex= languagesName.indexOf(fromLangCodeSupport)
        val translatedLangFlagIndex =  languagesName.indexOf(toLangCodeSupport)

        val inputLangName = allLangs[inputLangIndex].languageName
        val inputLangFlag= allLangs[inputLangFlagIndex].flag

        val inputMeaning = allLangs[inputLangIndex].languageMeaning
        val translatedLangName = allLangs[translatedLangIndex].languageName
        val translatedLangFlag = allLangs[translatedLangFlagIndex].flag
        val translatedMeaning = allLangs[translatedLangIndex].languageMeaning

        this.toLangFlag = translatedLangFlag
        this.toLang = translatedLangName
        this.toLangMeaning = translatedMeaning
        this.fromLang = inputLangName
        this.fromLangFlag = inputLangFlag
        this.fromLangMeaning = inputMeaning


        tv_langs_from_input.text = fromLang
        flag_from.setImageResource(fromLangFlag)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_PHR, fromLang)
        requireActivity().putPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_FLAG_PHR.toString(), fromLangFlag)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_PHR, fromLangCode)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_SUPPORT_PHR, fromLangCodeSupport)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_MEANING_PHR, fromLangMeaning)


        tv_langs_to_input.text = toLang
        flag_to.setImageResource(toLangFlag)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_PHR, toLang)
        requireActivity().putPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_FLAG_PHR.toString(), toLangFlag)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_PHR, toLangCode)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_SUPPORT_PHR, toLangCodeSupport)
        requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_MEANING_PHR, toLangMeaning)



    }

    override fun onResume() {
        super.onResume()
        getSelectedLanguages()
        mTTS = TextToSpeech(requireActivity(),TextToSpeech.OnInitListener { requireActivity() }, "com.google.android.tts")

    }

    val fromLangLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result: ActivityResult ->

        Log.e("result","res"+result.data)

        Log.e("resultcodes","resultcode"+result.resultCode+"   resultok"+Activity.RESULT_OK)
        if (result.resultCode == com.translate.languagetranslator.freetranslation.appUtils.Constants.REQ_CODE_LANGUAGE_SELECTION) {

            val data: Intent? = result.data
            if (result.resultCode != Activity.RESULT_CANCELED) {
                val origin = data!!.getStringExtra("origin")
                val languageName = data.getStringExtra(com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_NAME)
                val languageFlag = data.getIntExtra(com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_FLAG.toString(), R.drawable.english)
                val languageCode = data.getStringExtra(com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_CODE)
                val languageSupport = data.getStringExtra(com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_SUPPORT)
                val languageMeaning = data.getStringExtra(com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_LANG_MEANING)

                if (origin == com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_PHR) {
                    fromLang = languageName!!
                    fromLangFlag = languageFlag
                    fromLangCode = languageCode!!
                    fromLangCodeSupport = languageSupport!!
                    fromLangMeaning = languageMeaning!!

                    tv_langs_from_input.text = fromLang
                    flag_from.setImageResource(fromLangFlag)
                    Log.e("language",fromLang)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_PHR, fromLang)
                    requireActivity().putPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_FLAG_PHR.toString(), fromLangFlag)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_PHR, fromLangCode)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_CODE_SUPPORT_PHR, fromLangCodeSupport)
                    requireActivity(). putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_FROM_MEANING_PHR, fromLangMeaning)

                    updateDatalist()

                    adapter.notifyDataSetChanged()

                } else {
                    toLang = languageName!!
                    toLangFlag = languageFlag
                    toLangCode = languageCode!!
                    toLangCodeSupport = languageSupport!!
                    toLangMeaning = languageMeaning!!
                    tv_langs_to_input.text = toLang
                    flag_to.setImageResource(toLangFlag)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_PHR, toLang)
                    requireActivity().putPrefInt(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_FLAG_PHR.toString(), toLangFlag)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_PHR, toLangCode)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_CODE_SUPPORT_PHR, toLangCodeSupport)
                    requireActivity().putPrefString(com.translate.languagetranslator.freetranslation.appUtils.Constants.LANG_TO_MEANING_PHR, toLangMeaning)
                    Log.e("langTag",">>>> "+toLang)

                    updateDatalist()
                    adapter.notifyDataSetChanged()

                }
//                translateInputWord()
            }
        }

    }

    private fun setupRv() {

        val linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rv_main.setLayoutManager(linearLayoutManager)
         adapter = ItemDetailAdapter(requireContext(), listData, object : LangCodeInterface {
             override fun onItemSpeak(text: String) {
                 speakInputWord(toLangCodeSupport,text)
//                 Log.e("LangCode","code "+toLangCodeSupport)
             }


         },object :ClickPhrasesItem{
             override fun onItemClick() {
                 requireActivity().showInterstitial()
             }

         })
        rv_main.adapter = adapter
        adapter.notifyDataSetChanged()
    }
    fun dataList() {
//        listData.add(ItemModel("GREETING",dataArray(),dataArrayTrans()))
//        listData.add(ItemModel("BASICS",dataArray(),dataArrayTrans()))
        getCategoryData()
    }
    fun getCategoryData(){
       listData.addAll( AssetsRepo.getJsonDataFromAsset(requireContext(), category = "essential",lang=toLang, subHeading = fromLang)!!)
//        for (item in listData){
//            Log.e("datainArray","array"+item.heading)
//            Log.e("datainArray","array"+item.subheading)
//
//        }
        adapter.notifyDataSetChanged()

    }
    fun getCategoryDataTravelling(){
        listData.addAll( AssetsRepo.getJsonDataFromAsset(requireContext(), category = "traveling",lang=toLang, subHeading = fromLang)!!)

        adapter.notifyDataSetChanged()
    }
    fun getCategoryDataMedical(){
        listData.addAll( AssetsRepo.getJsonDataFromAsset(requireContext(), category = "medical",lang=toLang, subHeading = fromLang)!!)
        adapter.notifyDataSetChanged()
    }
    fun getCategoryDataHotel(){
        listData.addAll( AssetsRepo.getJsonDataFromAsset(requireContext(), category = "hotel",lang=toLang, subHeading = fromLang)!!)
        adapter.notifyDataSetChanged()
    }
    fun getCategoryDataRestaurant(){
        listData.addAll( AssetsRepo.getJsonDataFromAsset(requireContext(), category = "restaurant",lang=toLang, subHeading = fromLang)!!)
        adapter.notifyDataSetChanged()
    }
    fun getCategoryDataBar(){
        listData.addAll( AssetsRepo.getJsonDataFromAsset(requireContext(), category = "bar",lang=toLang, subHeading = fromLang)!!)
        adapter.notifyDataSetChanged()
    }
    fun getCategoryDataStore(){
        listData.addAll( AssetsRepo.getJsonDataFromAsset(requireContext(), category = "store",lang=toLang, subHeading = fromLang)!!)
        adapter.notifyDataSetChanged()
    }
    fun getCategoryDataWork(){
        listData.addAll( AssetsRepo.getJsonDataFromAsset(requireContext(), category = "work",lang=toLang, subHeading = fromLang)!!)
        adapter.notifyDataSetChanged()
    }
    fun getCategoryDataTime(){
        listData.addAll( AssetsRepo.getJsonDataFromAsset(requireContext(), category = "time",lang=toLang, subHeading = fromLang)!!)
        adapter.notifyDataSetChanged()
    }
    fun dataArray(): ArrayList<String>{
        dataArrayList=ArrayList()
        dataArrayList.add("Hello")
        dataArrayList.add("My name is__.")
        dataArrayList.add("Excuse me")
        dataArrayList.add("Goodbye")
        dataArrayList.add("How are you?")
        dataArrayList.add("Nice to meet you!")
       return dataArrayList

    }
    fun dataArrayTrans(): ArrayList<String>{
        dataArrayListTrans=ArrayList()
        dataArrayListTrans.add("Привет")
        dataArrayListTrans.add("Меня зовут__.")
        dataArrayListTrans.add("Извините меня")
        dataArrayListTrans.add("До свидания")
        dataArrayListTrans.add("Как дела?")
        dataArrayListTrans.add("Рад встрече!")
        return dataArrayListTrans

    }

    private fun loadBannerAd(){

        if (!requireContext().isPremium()) {
            try {
                MaxAdManager.createBannerAd(requireContext(),native_banner_container_home_screen)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }




  fun  searchItem(){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
//              TODO("Not yet implemented")
return false


            }

            override fun onQueryTextChange(newText: String): Boolean {
//
                filter(newText)
                return false
            }

        })
    }


    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<ItemModel> = ArrayList()

        // running a for loop to compare elements.
        for (item in listData) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.subheading.toString().lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(requireContext(), "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            adapter.filterList(filteredlist)
        }
    }


    public fun updateDatalist(){
        if (Constants.PhraseClickedItem == R.string.essentials){
            listData.clear()
            dataList()
        }
        if (Constants.PhraseClickedItem == R.string.whiletravelling){
            listData.clear()
            getCategoryDataTravelling()
        }
        if (Constants.PhraseClickedItem == R.string.medical){
            listData.clear()
            getCategoryDataMedical()
        }
        if (Constants.PhraseClickedItem == R.string.hotel){
            listData.clear()
            getCategoryDataHotel()
        }
        if (Constants.PhraseClickedItem == R.string.restaurant){
            listData.clear()
            getCategoryDataRestaurant()
        }
        if (Constants.PhraseClickedItem == R.string.bar){
            listData.clear()
            getCategoryDataBar()
        }
        if (Constants.PhraseClickedItem == R.string.store){
            listData.clear()
            getCategoryDataStore()
        }
        if (Constants.PhraseClickedItem == R.string.work){
            listData.clear()
            getCategoryDataWork()
        }
        if (Constants.PhraseClickedItem == R.string.time){
            listData.clear()
            getCategoryDataTime()
        }

    }

    private fun speakInputWord(langCode:String,inputWord: String) {

//            val inputWord = model.inputStr
            speakWord(langCode, inputWord)


    }
    private fun speakWord(langCode: String, speakingWord: String) {
        val langLocal = getLangLocale(langCode)
        mTTS?.language = langLocal
//        Log.e("mtts","local>>"+langLocal)
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "UniqueID"
        @Suppress("DEPRECATION")
        mTTS?.speak(speakingWord, TextToSpeech.QUEUE_FLUSH, map)
    }
}