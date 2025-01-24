package com.translate.languagetranslator.freetranslation.activities.details.viewModel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.appUtils.fetchLanguages
import com.translate.languagetranslator.freetranslation.appUtils.putPrefBoolean
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.models.LanguageModel
import java.util.*

class HistoryDetailViewModel(application: Application) : AndroidViewModel(application) {
    private var context: Context? = null
    private var translationDb: TranslationDb? = null


    init {
        this.context = application.applicationContext
        translationDb = TranslationDb.getInstance(context)

    }

    fun shareResult(activity: Activity, outPutWord: String) {

        activity.apply {
            putPrefBoolean("is_feedback", true)

        }

        val packageName = activity.applicationContext.packageName
        val appLink = "https://play.google.com/store/apps/details?id=$packageName"
        //val appLink = "https://bit.ly/3pYvF58"
        val appUri = Uri.parse(appLink)

        val shareBody = "I suggest you to try this Language Translator app, Its free"
        val word = outPutWord
        val body =
            "${outPutWord} \n ${shareBody}"

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Use this amazing translator")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, body + "\n" + appUri)
        activity.startActivity(
            Intent.createChooser(sharingIntent, activity.resources.getString(R.string.share_using))
        )
    }

    fun getLangLocale(langCode: String?): Locale {
        val langLocal: Locale
        when (langCode) {
            "tl" -> {
                langLocal = Locale("fil", "PH")

            }
            "id" -> {
                langLocal = Locale("id", "ID")

            }
            "en" -> {
                langLocal = Locale("en", "US")

            }
            "sq" -> {
                langLocal = Locale("sq", "AL")

            }

            "fr" -> {
                langLocal = Locale.FRANCE
            }
            "zh" -> {
                langLocal = Locale.CHINA
            }

            else -> {
                langLocal = Locale(langCode)

            }
        }
        return langLocal

    }

    fun getLanguageName(langCode: String): String {
        val langList: List<LanguageModel> = fetchLanguages()
        val countries = ArrayList<String>()
        val codes = ArrayList<String>()
        for (languageModel in langList) {
            countries.add(languageModel.languageName)
            codes.add(languageModel.supportedLangCode)
        }
        val selectedId = codes.indexOf(langCode)
        return countries[selectedId]

    }

    fun getLanguageMeaning(langCode: String): String {
        val langList: List<LanguageModel> = fetchLanguages()
        val meaning = ArrayList<String>()
        val codes = ArrayList<String>()
        for (languageModel in langList) {
            meaning.add(languageModel.languageMeaning)
            codes.add(languageModel.supportedLangCode)
        }
        val selectedId = codes.indexOf(langCode)
        return meaning[selectedId]

    }

    fun setFavorite(favorite: Boolean, uniqueId: String) {
        translationDb!!.translationTblDao().setFavorite(favorite, uniqueId)

    }
    fun setFavoriteById(favorite: Boolean, iD: Int) {
        translationDb!!.translationTblDao().updateFAv(favorite, iD)

    }
}