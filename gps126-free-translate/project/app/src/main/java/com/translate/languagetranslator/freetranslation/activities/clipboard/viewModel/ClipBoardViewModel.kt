package com.translate.languagetranslator.freetranslation.activities.clipboard.viewModel

import android.app.Application
import android.content.Context
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.TinyDB
import com.translate.languagetranslator.freetranslation.appUtils.fetchLanguages
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import com.translate.languagetranslator.freetranslation.models.LanguageModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


class ClipBoardViewModel(application: Application) : AndroidViewModel(application) {

    private var context: Context? = null
    private var isCanceled = false
    private var translateJob: Job? = null

    init {
        this.context = application.applicationContext
    }

    fun getClipRightLangName(): String? {
        var langName = TinyDB.getInstance(context).getString(Constants.CLIP_RIGHT_LANG_NAME)
        if (langName == "") {
            langName = "French"
            TinyDB.getInstance(context).putString(Constants.CLIP_RIGHT_LANG_NAME, langName)
        }
        return langName
    }

    fun getClipRightLangCode(): String? {
        var langCode = TinyDB.getInstance(context).getString(Constants.CLIP_RIGHT_LANG_CODE)
        if (langCode == "") {
            langCode = "fr-FR"
            TinyDB.getInstance(context).putString(Constants.CLIP_RIGHT_LANG_CODE, langCode)
        }
        return langCode
    }

    fun getClipRightLangMeaning(): String? {
        var langMeaning = TinyDB.getInstance(context).getString(Constants.CLIP_RIGHT_LANG_MEANING)
        if (langMeaning == "") {
            langMeaning = "Français"
            TinyDB.getInstance(context).putString(Constants.CLIP_RIGHT_LANG_MEANING, langMeaning)

        }
        return langMeaning
    }

    fun getClipSupportLangCode():String?{

        var langMeaning = TinyDB.getInstance(context).getString(Constants.CLIP_RIGHT_LANG_SUPPORT)
        if (langMeaning == "") {
            langMeaning = "fr"
            TinyDB.getInstance(context).putString(Constants.CLIP_RIGHT_LANG_SUPPORT, langMeaning)

        }
        return langMeaning

    }

    fun setTextToTextView(textView: TextView, langName: String?) {
        textView.text = langName
    }

    fun translateWord(
        inputWord: String?,
        leftCode: String?,
        rightCode: String?,
        result: (String?) -> Unit
    ) {
        translateJob = viewModelScope.launch {
            withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    val translatedString = callUrlAndParseResult(leftCode!!, rightCode!!, inputWord)
                    withContext(Dispatchers.Main) {
                        result.invoke(translatedString)
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }

        }


    }

    @Throws(java.lang.Exception::class)
    private fun callUrlAndParseResult(
        langFrom: String, langTo: String,
        word: String?
    ): String? {
        val url = "https://translate.googleapis.com/translate_a/single?client=gtx&" +
                "sl=" + langFrom +
                "&tl=" + langTo +
                "&dt=t&q=" + URLEncoder.encode(word, "UTF-8")
        val obj = URL(url)
        val con = obj.openConnection() as HttpURLConnection
        con.setRequestProperty("User-Agent", "Mozilla/5.0")
        val `in` = BufferedReader(
            InputStreamReader(con.inputStream)
        )
        var inputLine: String?
        val response = StringBuffer()
        while (`in`.readLine().also { inputLine = it } != null) {
            if (!isCanceled)
                response.append(inputLine) else return null
        }
        `in`.close()
        return parseResult(response.toString(), word)
    }

    @Throws(Exception::class)
    private fun parseResult(inputJson: String, word: String?): String? {
        val jsonArray = JSONArray(inputJson)
        val jsonArray2 = jsonArray[0] as JSONArray
        var outputWord: String? = ""
        for (i in 0 until jsonArray2.length()) {
            if (!isCanceled) {
                val parseResult = jsonArray2[i] as JSONArray
                val result = parseResult[0].toString()
                if (result != null && !result.contains("null")) {
                    outputWord += result
                }
            } else {
                return null
            }
        }
        return outputWord
    }

    fun stopTask() {
        isCanceled = true
    }

    fun fetchAllLanguages(): List<LanguageModel> {


        return fetchLanguages()
    }

    fun slideUp(view: View) {
        view.visibility = View.VISIBLE
        val animate = TranslateAnimation(
            0f,  // fromXDelta
            0f,  // toXDelta
            view.height.toFloat(),  // fromYDelta
            0f
        ) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    fun slideDown(view: View) {
        val animate = TranslateAnimation(
            0f,  // fromXDelta
            0f,  // toXDelta
            0f,  // fromYDelta
            view.height.toFloat()
        ) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
        view.visibility = View.GONE

    }

    fun setClipLangInTinyDb(
        rightName: String?,
        rightCode: String?,
        rightMeaning: String?,
        support: String?
    ) {
        TinyDB.getInstance(context).putString(Constants.CLIP_RIGHT_LANG_NAME, rightName)
        TinyDB.getInstance(context).putString(Constants.CLIP_RIGHT_LANG_CODE, rightCode)
        TinyDB.getInstance(context).putString(Constants.CLIP_RIGHT_LANG_MEANING, rightMeaning)
        TinyDB.getInstance(context).putString(Constants.CLIP_RIGHT_LANG_SUPPORT, support)


    }

    fun getFavorite(id: String): Boolean {

        return TranslationDb.getInstance(context).translationTblDao().isFavorite(id)
    }

    fun setTranslationInDb(translationModel: TranslationTable) {
        TranslationDb.getInstance(context).translationTblDao().insert(translationModel)
    }
}