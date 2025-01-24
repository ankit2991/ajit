package com.translate.languagetranslator.freetranslation.activities.mainScreen.viewModel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.appUtils.putPrefBoolean
import com.translate.languagetranslator.freetranslation.database.DataRepository
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainViewModel(private var dataRepository: DataRepository) : ViewModel()  {

    fun isAutoAdsRemoved() = dataRepository.isAdsRemoved()

    fun translate_button_interstitial() = dataRepository.translate_button_interstitial

    fun translate_back_interstitial() = dataRepository.translate_back_interstitial

    fun translate_top_native() = dataRepository.translate_top_native

    private var context: Context? = null
    private var translationDb: TranslationDb? = null

    init {
        this.context = dataRepository.applicationClass
        translationDb = TranslationDb.getInstance(context)
    }

    fun insertHistoryTranslation(model: TranslationTable) {
        CoroutineScope(Dispatchers.IO).launch {
            translationDb?.translationTblDao()?.insert(model)
        }
    }

    fun getMainScreenHistory(mainHistory: (LiveData<List<TranslationTable>>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val history = translationDb!!.translationTblDao().getLimitList(10)
            withContext(Dispatchers.Main) {
                mainHistory.invoke(history)
            }
        }
    }

    fun deleteHistoryItem(translationTable: TranslationTable) {
        CoroutineScope(Dispatchers.IO).launch {
            translationDb!!.translationTblDao().delete(translationTable)
        }
    }

    fun getSpeechRecognitionIntent(s: String?): Intent {
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
        return intent

    }

    fun isAppInstalled(activity: Activity, packageName: String): Boolean {
        val pm = activity.packageManager
        return try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            pm.getApplicationInfo(packageName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }

    }

    fun getFavorite(id: String): Boolean {
        return translationDb!!.translationTblDao().isFavorite(id)
    }

    fun setFavorite(favorite: Boolean, unique_iden: String) {
        translationDb!!.translationTblDao().setFavorite(favorite, unique_iden)

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
            "ar" -> {
                langLocal = Locale("ar", "SA")

            }
            "ur" -> {
                langLocal = Locale("ur", "PK")
            }

            else -> {
                langLocal = Locale(langCode)

            }
        }
        return langLocal

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

}