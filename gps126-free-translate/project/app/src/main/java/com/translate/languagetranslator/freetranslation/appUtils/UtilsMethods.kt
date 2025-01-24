package com.translate.languagetranslator.freetranslation.appUtils

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AndroidRuntimeException
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.code4rox.adsmanager.MaxAdCallbacks
import com.code4rox.adsmanager.MaxAdManager
import com.code4rox.adsmanager.OnAdShowCallback
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.camera.CameraActivity
import com.translate.languagetranslator.freetranslation.activities.camera.OcrActivity
import com.translate.languagetranslator.freetranslation.activities.clipboard.services.ClipBoardDataManager
import com.translate.languagetranslator.freetranslation.activities.fictionalLanguage.FictionalLangActivity
import com.translate.languagetranslator.freetranslation.activities.historyBookmark.BookmarkActivity
import com.translate.languagetranslator.freetranslation.activities.historyBookmark.HistoryActivity
import com.translate.languagetranslator.freetranslation.activities.languages.LanguageSelection
import com.translate.languagetranslator.freetranslation.activities.mainScreen.MainActivity
import com.translate.languagetranslator.freetranslation.activities.onboarding.PurchaseActivity
import com.translate.languagetranslator.freetranslation.activities.translate.TranslateActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants.*
import com.translate.languagetranslator.freetranslation.database.entity.ConversationModel
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import com.translate.languagetranslator.freetranslation.dismissProgressDialog
import com.translate.languagetranslator.freetranslation.models.DictionaryLanguageModel
import com.translate.languagetranslator.freetranslation.models.LanguageModel
import com.translate.languagetranslator.freetranslation.showProgressBlurDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

fun openPrivacyPolicyView(context: Context, url: String) {

    try {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
    }
}

//fun openHistoryDetailActivity(
//    activity: Activity,
//    translationTable: TranslationTable,
//    source: String
//) {
//
//    val intent = Intent(activity, HistoryDetailActivity::class.java)
//    intent.putExtra("source", source)
//    intent.putExtra("detail_object", translationTable)
//    if (source == DETAIL_SOURCE_CAM)
//        activity.startActivityForResult(intent, REQUEST_CODE_OCR_BACK)
//    else
//        activity.startActivity(intent)
//
//
//}

fun Activity.openMainActivity(translationModel: TranslationTable?, source: String) {
    val mainIntent = Intent(this, MainActivity::class.java)
    mainIntent.putExtra("controlled", source)
    translationModel?.let {
        mainIntent.putExtra("opened", "with_data")
        mainIntent.putExtra("detail_object", translationModel)

    } ?: run {
        mainIntent.putExtra("opened", "without_data")

    }
    startActivity(mainIntent)
}

fun Activity.openFictionalActivity(translationModel: TranslationTable?, source: String) {
    val mainIntent = Intent(this, FictionalLangActivity::class.java)
    mainIntent.putExtra("controlled", source)
    translationModel?.let {
        mainIntent.putExtra("opened", "with_data")
        mainIntent.putExtra("detail_object", translationModel)

    } ?: run {
        mainIntent.putExtra("opened", "without_data")

    }
    startActivity(mainIntent)
}


fun openHistoryActivity(activity: Activity, source: String) {
    val intent = Intent(activity, HistoryActivity::class.java)
    intent.putExtra("history_source", source)
    activity.startActivityForResult(intent, REQ_CODE_HISTORY)
}


fun Activity.openBookMarkActivity() {
    val intent = Intent(this, BookmarkActivity::class.java)
    startActivityForResult(intent, REQ_CODE_BOOKMARK)
}

fun Activity.openSubscriptionActivity(from: String) {
    val intent = Intent(this, PurchaseActivity::class.java)
    intent.putExtra("from", from)
    startActivity(intent)
}


fun openOcrActivity(activity: Activity) {
    val intent = Intent(activity, OcrActivity::class.java)
    activity.startActivity(intent)
}

fun openCameraActivity(activity: Activity) {
    val intent = Intent(activity, CameraActivity::class.java)
    activity.startActivityForResult(intent, REQUEST_CODE_OCR_BACK)
}


fun openTranslateActivityWithText(activity: Activity, type: String, origin: String, data: String) {
    val intent = Intent(activity, TranslateActivity::class.java)
    intent.putExtra(TRANSLATE_TYPE, type)
    intent.putExtra(TRANSLATE_ORIGIN, origin)
    intent.putExtra(TRANSLATE_INTENT_DATA, data)
    activity.startActivity(intent)
    activity.overridePendingTransition(R.anim.transit_right_left, R.anim.transit_none)


}

fun Activity.callRateUs() {
    putPrefBoolean("is_feedback", true)

    val packageName = this.applicationContext.packageName
    val uri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    startActivity(intent)
}

fun Activity.callShare() {

    val packageName = applicationContext.packageName
    val appLink = "https://play.google.com/store/apps/details?id=$packageName"
    putPrefBoolean("is_feedback", true)

//    val appLink = "https://bit.ly/3pYvF58"
    val appUri = Uri.parse(appLink)

    val shareBody = "I suggest you to try this Language Translator app, Its free"
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    //        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Today i drink "+ mGlasses +" glasses ( "+mProgress + munit + " ) of water!"  );
    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody + "\n" + appUri)
    startActivity(
        Intent.createChooser(sharingIntent, getString(R.string.share_using))
    )

}

fun showToast(context: Context, message: String) {
    val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
    toast.show()
}

fun showShortToast(context: Context, message: String) {
    val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
    toast.show()
}

fun fetchLanguages(): List<LanguageModel> {
    val langList = ArrayList<LanguageModel>()
    langList.add(LanguageModel("en", "English", "en-US", R.drawable.english, "English"))
    langList.add(LanguageModel("ur", "Urdu", "ur-PK", R.drawable.urdu, "اردو"))
    langList.add(LanguageModel("af", "Afrikaans", "af-ZA", R.drawable.afrikaans, "Afrikaans"))
    langList.add(LanguageModel("sq", "Albanian", "al-SQ", R.drawable.albanian, "shqiptar"))
    langList.add(LanguageModel("am", "Amharic", "am-ET", R.drawable.amharic, "አማርኛ"))
    langList.add(LanguageModel("ar", "Arabic", "ar-SA", R.drawable.arabic, "العربية"))
    langList.add(LanguageModel("hy", "Armenian", "hy-AM", R.drawable.armenian, "հայերեն"))
    langList.add(
        LanguageModel(
            "az",
            "Azerbaijani",
            "az-AZ",
            R.drawable.azeerbaijani,
            "Azərbaycan"
        )
    )
    langList.add(LanguageModel("eu", "Basque", "eu-ES", R.drawable.basque, "Euskal"))
    langList.add(LanguageModel("be", "Belarusian", "ru-BG", R.drawable.belarusian, "Беларус"))
    langList.add(LanguageModel("bn", "Bengali", "bn-BD", R.drawable.bengali, "বাংলা"))
    langList.add(LanguageModel("bs", "Bosnian", "Bosnian", R.drawable.bosnian, "bosanski"))
    langList.add(LanguageModel("bg", "Bulgarian", "bg-BG", R.drawable.bulgarian_new, "Български"))
    langList.add(LanguageModel("ca", "Catalan", "ca-ES", R.drawable.catalan_new, "Català"))
    langList.add(LanguageModel("ceb", "Cebuano", "ceb-PHL", R.drawable.cebuano_new, "Cebuano"))
    langList.add(LanguageModel("zh", "Chinese", "yue-Hant-HK", R.drawable.chinese, "中文"))
    langList.add(LanguageModel("zh", "Chinese(Traditional)", "zh-Hant", R.drawable.chinese, "中文"))
    langList.add(LanguageModel("co", "Corsican", "fr-FR", R.drawable.corsican, "Corsu"))
    langList.add(LanguageModel("hr", "Croatian", "hr-HR", R.drawable.croatian, "Hrvatski"))
    langList.add(LanguageModel("cs", "Czech", "cs-CZ", R.drawable.czech, "Čeština"))
    langList.add(LanguageModel("da", "Danish", "da-DK", R.drawable.danish, "Dansk"))
    langList.add(LanguageModel("nl", "Dutch", "nl-NL", R.drawable.dutch_new, "Nederlands"))
    langList.add(LanguageModel("eo", "Esperanto", "eo-DZ", R.drawable.esperanto, "Esperanto"))
    langList.add(LanguageModel("et", "Estonian", "et-EST", R.drawable.estonian_new, "Eesti"))
    langList.add(LanguageModel("fi", "Finnish", "fi-FI", R.drawable.finnish, "Suomi"))
    langList.add(LanguageModel("fr", "French", "fr-FR", R.drawable.french, "Français"))
    langList.add(LanguageModel("fy", "Frisian", "fy-DEU", R.drawable.frisian_new, "Frysk"))
    langList.add(LanguageModel("gl", "Galician", "gl-ES", R.drawable.galician, "Galego"))
    langList.add(LanguageModel("ka", "Georgian", "ka-GE", R.drawable.georgian, "ქართული"))
    langList.add(LanguageModel("de", "German", "de-DE", R.drawable.german_new, "Deutsch"))
    langList.add(LanguageModel("el", "Greek", "el-GR", R.drawable.greek, "Ελληνικά"))
    langList.add(LanguageModel("gu", "Gujarati", "gu-IN", R.drawable.gujarati, "ગુજરાતી"))
    langList.add(
        LanguageModel(
            "ht",
            "Haitian",
            "ht-HTI",
            R.drawable.haitiancreole,
            "Haitian Creole"
        )
    )
    langList.add(LanguageModel("ha", "Hausa", "ha-HUA", R.drawable.hausa_new, "Hausa"))
    langList.add(LanguageModel("haw", "Hawaiian", "haw-US", R.drawable.hawaiian, "ʻ .lelo Hawaiʻi"))
    langList.add(LanguageModel("he", "Hebrew", "he-HEB", R.drawable.hebrew, "עברית"))
    langList.add(LanguageModel("hi", "Hindi", "hi-IN", R.drawable.hindi, "हिंदी"))
    langList.add(LanguageModel("hmn", "Hmong", "hmn-Hm", R.drawable.hmong_new, "Hmong"))
    langList.add(LanguageModel("hu", "Hungarian", "hu-HU", R.drawable.hungarian, "Magyar"))
    langList.add(LanguageModel("is", "Icelandic", "is-IS", R.drawable.icelandic, "Íslensku"))
    langList.add(LanguageModel("ig", "Igbo", "Igbo", R.drawable.igbo, "Ndi Igbo"))
    langList.add(LanguageModel("id", "Indonesian", "id-ID", R.drawable.indonesian, "Indonesia"))
    langList.add(LanguageModel("ga", "Irish", "Irish", R.drawable.irish, "Gaeilge"))
    langList.add(LanguageModel("it", "Italian", "it-IT", R.drawable.itallian, "Italiano"))
    langList.add(LanguageModel("ja", "Japanese", "ja-JP", R.drawable.japanese, "日本語"))
    langList.add(LanguageModel("jw", "Javanese", "jv-ID", R.drawable.javanese, "Basa jawa"))
    langList.add(LanguageModel("kn", "Kannada", "kn-IN", R.drawable.kannada, "ಕನ್ನಡ"))
    langList.add(LanguageModel("kk", "Kazakh", "Kazakh", R.drawable.kazakh, "Қазақ"))
    langList.add(LanguageModel("km", "Khmer", "km-KH", R.drawable.khmer, "ខខ្មែរ"))
    langList.add(LanguageModel("ko", "Korean", "ko-KR", R.drawable.korean_new, "한국어"))
    langList.add(LanguageModel("ku", "Kurdish", "ku-KUR", R.drawable.kurdish, "Kurdî"))
    langList.add(LanguageModel("ky", "Kyrgyz", "ky-KGZ", R.drawable.kyrgyz, "Кыргызча"))
    langList.add(LanguageModel("lo", "Lao", "lo-LA", R.drawable.lao, "ລາວ"))
    langList.add(LanguageModel("la", "Latin", "Latin", R.drawable.latin_new, "Latine"))
    langList.add(LanguageModel("lv", "Latvian", "lv-LV", R.drawable.khmer, "Latviešu valoda"))
    langList.add(LanguageModel("lt", "Lithuanian", "lt-LT", R.drawable.lithuanian, "Lietuvių"))
    langList.add(
        LanguageModel(
            "lb",
            "Luxembourgish",
            "mk-MDK",
            R.drawable.luxembourgish,
            "Lëtzebuergesch"
        )
    )
    langList.add(LanguageModel("mk", "Macedonian", "mg-MGD", R.drawable.macedonian, "Македонски"))
    langList.add(LanguageModel("mg", "Malagasy", "Malay", R.drawable.malagasy, "Malagasy"))
    langList.add(LanguageModel("ms", "Malay", "ms-MY", R.drawable.malay, "Bahasa Melayu"))
    langList.add(LanguageModel("ml", "Malayalam", "ml-IN", R.drawable.malayalam, "മലയാളം"))
    langList.add(LanguageModel("mt", "Maltese", "mt-MLT", R.drawable.maltese, "Il-Malti"))
    langList.add(LanguageModel("mi", "Maori", "mi-MIO", R.drawable.maori, "Maori"))
    langList.add(LanguageModel("mr", "Marathi", "mr-IN", R.drawable.marathi, "मराठी"))
    langList.add(LanguageModel("mn", "Mongolian", "mn-MNG", R.drawable.mongolian, "Монгол"))
    langList.add(LanguageModel("my", "Myanmar", "my-MMR", R.drawable.myanmar, "မြန်မာ"))
    langList.add(LanguageModel("ne", "Nepali", "ne-NP", R.drawable.nepali, "नेपाली"))
    langList.add(LanguageModel("nb", "Norwegian", "nb-NO", R.drawable.norwegian_new, "Norsk"))
    langList.add(LanguageModel("ny", "Nyanja", "Nyanja", R.drawable.nyanja, "Nyanja"))
    langList.add(LanguageModel("ps", "Pashto", "ps-PK", R.drawable.pashto, "پښتو"))
    langList.add(LanguageModel("fa", "Persian", "fa-IR", R.drawable.persian, "فارسی"))
    langList.add(LanguageModel("pl", "Polish", "pl-POL", R.drawable.polish, "Polski"))
    langList.add(LanguageModel("pt", "Portuguese", "pt-PT", R.drawable.portuguese, "Português"))
    langList.add(LanguageModel("pa", "Punjabi", "pa-IN", R.drawable.punjabi, "ਪੰਜਾਬੀ"))
    langList.add(LanguageModel("ro", "Romanian", "ro-RO", R.drawable.romanian, "Română"))
    langList.add(LanguageModel("ru", "Russian", "ru-RU", R.drawable.russian, "Pусский"))

    langList.add(LanguageModel("smo", "Samoan", "sm-WSM", R.drawable.samoan, "Sāmoa"))
    langList.add(LanguageModel("nso", "Sesotho", "st-SOT", R.drawable.sesotho, "Sesotho"))
    langList.add(LanguageModel("sin", "Sinhala", "si-LK", R.drawable.sinhala, "සිංහල"))


    langList.add(LanguageModel("gd", "Scots", "gd-GBR", R.drawable.scotsgaelic, "Albannaich"))
    langList.add(LanguageModel("sr", "Serbian", "sr-RS", R.drawable.serbian, "Српски"))
    langList.add(LanguageModel("sn", "Shona", "sn-SNA", R.drawable.shona, "Shona"))
    langList.add(LanguageModel("sd", "Sindhi", "sd-Pk", R.drawable.sindhi, "سنڌي"))
    langList.add(LanguageModel("sk", "Slovak", "sk-SK", R.drawable.slovak, "Slovenský"))
    langList.add(LanguageModel("sl", "Slovenian", "sl-SI", R.drawable.slovenian_new, "Slovenščina"))
    langList.add(LanguageModel("so", "Somali", "so-SOM", R.drawable.somali, "Soomaali"))
    langList.add(LanguageModel("es", "Spanish", "es-ES", R.drawable.spanish, "Español"))
    langList.add(LanguageModel("su", "Sundanese", "su-ID", R.drawable.samoan, "Urang Sunda"))
    langList.add(LanguageModel("sw", "Swahili", "sw-TZ", R.drawable.swahili, "Kiswahili"))
    langList.add(LanguageModel("sv", "Swedish", "sv-SE", R.drawable.swedish, "Svenska"))

//    langList.add(LanguageModel("tgl", "Tagalog", "tl-TGL", R.drawable.tagalog))


    langList.add(LanguageModel("tg", "Tajik", "tg-TJK", R.drawable.uzbek, "Точик"))
    langList.add(LanguageModel("ta", "Tamil", "ta-IN", R.drawable.tamil, "தமிழ்"))
    langList.add(LanguageModel("te", "Telugu", "te-IN", R.drawable.telugu, "తెలుగు"))
    langList.add(LanguageModel("th", "Thai", "th-TH", R.drawable.thai, "ไทย"))
    langList.add(LanguageModel("tr", "Turkish", "tr-TR", R.drawable.turkish, "Türk"))

    langList.add(LanguageModel("uk", "Ukrainian", "uk-UA", R.drawable.ukrainian, "Українська"))
    langList.add(LanguageModel("uz", "Uzbek", "uz-UZB", R.drawable.uzbek, "O'zbek"))
    langList.add(LanguageModel("vi", "Vietnamese", "vi-VN", R.drawable.vietnamese, "Tiếng Việt"))
    langList.add(LanguageModel("cy", "Welsh", "cy-GBR", R.drawable.welsh, "Cymraeg"))
    langList.add(LanguageModel("xh", "Xhosa", "xh-XHO", R.drawable.xhosa, "isiXhosa"))
    langList.add(LanguageModel("yo", "Yoruba", "Yoruba-yo", R.drawable.yoruba, "Yoruba"))

    langList.sortWith(Comparator { o1, o2 ->
        o1.languageName.compareTo(
            o2.languageName,
            ignoreCase = true
        )
    })

    return langList
}

/*
fictional languages
 */
fun fetchFictionalLanguages(): List<LanguageModel> {
    val langList = ArrayList<LanguageModel>()
    langList.add(
        LanguageModel(
            "jar",
            "Jar Jar Binks",
            "jar_Bink",
            R.drawable.jar_jar_binks,
            "Jar Jar Binks"
        )
    )
//    langList.add(LanguageModel("yoda", "Yoda", "yo_Da", R.drawable.yoda, "Yoda"))
    langList.add(LanguageModel("sith", "Sith", "si_Th", R.drawable.sith, "Sith"))
    langList.add(
        LanguageModel(
            "shake",
            "Shakespeare",
            "shake-Spear",
            R.drawable.shakespear,
            "Shakespear"
        )
    )
    langList.add(
        LanguageModel(
            "valley",
            "Valley Girl",
            "valley_girl",
            R.drawable.valley_girl,
            "Valley Girl"
        )
    )



    langList.sortWith(Comparator { o1, o2 ->
        o1.languageName.compareTo(
            o2.languageName,
            ignoreCase = true
        )
    })

    return langList
}

fun phraseBookLanguages(): List<LanguageModel> {
    val langList = ArrayList<LanguageModel>()
    langList.add(LanguageModel("en", "English", "en-US", R.drawable.english, "English"))
    langList.add(LanguageModel("af", "Afrikaans", "af-ZA", R.drawable.afrikaans, "Afrikaans"))
    langList.add(LanguageModel("es", "Spanish", "es-ES", R.drawable.spanish, "Español"))
    langList.add(LanguageModel("fr", "French", "fr-FR", R.drawable.french, "Français"))
    langList.add(LanguageModel("ar", "Arabic", "ar-SA", R.drawable.arabic, "العربية"))
    langList.add(LanguageModel("ro", "Romanian", "ro-RO", R.drawable.romanian, "Română"))
    langList.add(LanguageModel("ru", "Russian", "ru-RU", R.drawable.russian, "Pусский"))
    langList.add(LanguageModel("nl", "Dutch", "nl-NL", R.drawable.dutch, "Nederlands"))
    langList.add(LanguageModel("de", "German", "de-DE", R.drawable.germany, "Deutsch"))
    langList.add(LanguageModel("id", "Indonesian", "id-ID", R.drawable.indonesian, "Indonesia"))
    langList.add(LanguageModel("it", "Italian", "it-IT", R.drawable.itallian, "Italiano"))
    langList.add(LanguageModel("ja", "Japanese", "ja-JP", R.drawable.japanese, "日本語"))
    langList.add(LanguageModel("pl", "Polish", "pl-POL", R.drawable.polish, "Polski"))
    langList.add(LanguageModel("sv", "Swedish", "sv-SE", R.drawable.swedish, "Svenska"))
    langList.add(LanguageModel("th", "Thai", "th-TH", R.drawable.thai, "ไทย"))
    langList.add(LanguageModel("tr", "Turkish", "tr-TR", R.drawable.turkish, "Türk"))
    langList.add(LanguageModel("pt", "Portuguese", "pt-PT", R.drawable.portuguese, "Português"))
    langList.sortWith(Comparator { o1, o2 ->
        o1.languageName.compareTo(
            o2.languageName,
            ignoreCase = true
        )
    })

    return langList

}

fun fetchOCRLanguages(): List<LanguageModel> {
    val langList = ArrayList<LanguageModel>()
    langList.add(LanguageModel("af", "Afrikaans", "af-ZA", R.drawable.afrikaans, "Afrikaans"))
    langList.add(LanguageModel("sq", "Albanian", "al-SQ", R.drawable.albanian, "shqiptar"))
    langList.add(
        LanguageModel(
            "az",
            "Azerbaijani",
            "az-AZ",
            R.drawable.azeerbaijani,
            "Azərbaycan"
        )
    )
    langList.add(LanguageModel("eu", "Basque", "eu-ES", R.drawable.spanish, "Euskal"))
    langList.add(LanguageModel("bs", "Bosnian", "Bosnian", R.drawable.bosnian, "bosanski"))
    langList.add(LanguageModel("ca", "Catalan", "ca-ES", R.drawable.catalan, "Català"))
    langList.add(LanguageModel("ceb", "Cebuano", "ceb-PHL", R.drawable.corsican, "Cebuano"))
    langList.add(LanguageModel("co", "Corsican", "fr-FR", R.drawable.corsican, "Corsu"))
    langList.add(LanguageModel("hr", "Croatian", "hr-HR", R.drawable.croatian, "Hrvatski"))
    langList.add(LanguageModel("cs", "Czech", "cs-CZ", R.drawable.czech, "Čeština"))

    langList.add(LanguageModel("da", "Danish", "da-DK", R.drawable.danish, "Dansk"))
    langList.add(LanguageModel("nl", "Dutch", "nl-NL", R.drawable.dutch, "Nederlands"))

    langList.add(LanguageModel("en", "English", "en-US", R.drawable.english, "English"))
    langList.add(LanguageModel("eo", "Esperanto", "eo-DZ", R.drawable.esperanto, "Esperanto"))
    langList.add(LanguageModel("et", "Estonian", "et-EST", R.drawable.estonian, "Eesti"))

    langList.add(LanguageModel("fi", "Finnish", "fi-FI", R.drawable.finnish, "Suomi"))
    langList.add(LanguageModel("fr", "French", "fr-FR", R.drawable.french, "Français"))
    langList.add(LanguageModel("fy", "Frisian", "fy-DEU", R.drawable.frisian, "Frysk"))

    langList.add(LanguageModel("gl", "Galician", "gl-ES", R.drawable.galician, "Galego"))
    langList.add(LanguageModel("de", "German", "de-DE", R.drawable.germany, "Deutsch"))
    langList.add(
        LanguageModel(
            "haw",
            "Hawaiian",
            "haw-US",
            R.drawable.hawaiian,
            "ʻ .lelo Hawaiʻi"
        )
    )
    langList.add(LanguageModel("he", "Hebrew", "he-HEB", R.drawable.hebrew, "עברית"))
    langList.add(LanguageModel("hmn", "Hmong", "hmn-Hm", R.drawable.hmong, "Hmong"))
    langList.add(LanguageModel("hu", "Hungarian", "hu-HU", R.drawable.hungarian, "Magyar"))
    langList.add(LanguageModel("ha", "Hausa", "ha-HUA", R.drawable.hausa, "Hausa"))
    langList.add(LanguageModel("is", "Icelandic", "is-IS", R.drawable.icelandic, "Íslensku"))
    langList.add(LanguageModel("ig", "Igbo", "Igbo", R.drawable.igbo, "Ndi Igbo"))
    langList.add(LanguageModel("id", "Indonesian", "id-ID", R.drawable.indonesian, "Indonesia"))
    langList.add(LanguageModel("ga", "Irish", "Irish", R.drawable.irish, "Gaeilge"))
    langList.add(LanguageModel("it", "Italian", "it-IT", R.drawable.itallian, "Italiano"))
    langList.add(LanguageModel("jw", "Javanese", "jv-ID", R.drawable.javanese, "Basa jawa"))
    langList.add(LanguageModel("ku", "Kurdish", "ku-KUR", R.drawable.kurdish, "Kurdî"))
    langList.add(LanguageModel("la", "Latin", "Latin", R.drawable.latin, "Latine"))
    langList.add(LanguageModel("lv", "Latvian", "lv-LV", R.drawable.khmer, "Latviešu valoda"))
    langList.add(LanguageModel("lt", "Lithuanian", "lt-LT", R.drawable.lithuanian, "Lietuvių"))
    langList.add(
        LanguageModel(
            "lb",
            "Luxembourgish",
            "mk-MDK",
            R.drawable.luxembourgish,
            "Lëtzebuergesch"
        )
    )
    langList.add(LanguageModel("mg", "Malagasy", "Malay", R.drawable.malagasy, "Malagasy"))
    langList.add(LanguageModel("ms", "Malay", "ms-MY", R.drawable.malay, "Bahasa Melayu"))
    langList.add(LanguageModel("mt", "Maltese", "mt-MLT", R.drawable.maltese, "Il-Malti"))
    langList.add(LanguageModel("mi", "Maori", "mi-MIO", R.drawable.maori, "Maori"))
    langList.add(LanguageModel("pl", "Polish", "pl-POL", R.drawable.polish, "Polski"))
    langList.add(LanguageModel("pt", "Portuguese", "pt-PT", R.drawable.portuguese, "Português"))
    langList.add(LanguageModel("ro", "Romanian", "ro-RO", R.drawable.romanian, "Română"))
    langList.add(LanguageModel("gd", "Scots", "gd-GBR", R.drawable.scotsgaelic, "Albannaich"))
    langList.add(LanguageModel("sn", "Shona", "sn-SNA", R.drawable.shona, "Shona"))
    langList.add(LanguageModel("sk", "Slovak", "sk-SK", R.drawable.slovak, "Slovenský"))
    langList.add(LanguageModel("sl", "Slovenian", "sl-SI", R.drawable.slovak, "Slovenščina"))

    langList.add(LanguageModel("so", "Somali", "so-SOM", R.drawable.somali, "Soomaali"))
    langList.add(LanguageModel("es", "Spanish", "es-ES", R.drawable.spanish, "Español"))
    langList.add(LanguageModel("su", "Sundanese", "su-ID", R.drawable.samoan, "Urang Sunda"))
    langList.add(LanguageModel("sw", "Swahili", "sw-TZ", R.drawable.swahili, "Kiswahili"))

    langList.add(LanguageModel("tr", "Turkish", "tr-TR", R.drawable.turkish, "Türk"))
    langList.add(LanguageModel("uz", "Uzbek", "uz-UZB", R.drawable.uzbek, "O'zbek"))
    langList.add(
        LanguageModel(
            "vi",
            "Vietnamese",
            "vi-VN",
            R.drawable.vietnamese,
            "Tiếng Việt"
        )
    )
    langList.add(LanguageModel("cy", "Welsh", "cy-GBR", R.drawable.welsh, "Cymraeg"))
    langList.add(LanguageModel("yo", "Yoruba", "Yoruba-yo", R.drawable.yoruba, "Yoruba"))

    langList.sortWith(Comparator { o1, o2 ->
        o1.languageName.compareTo(
            o2.languageName,
            ignoreCase = true
        )
    })

    return langList

}


fun isSpeakerVisible(translatedTarLangCode: String): Boolean {
    return !(/*translatedTarLangCode == "ar" ||*/ translatedTarLangCode == "am" || translatedTarLangCode == "hy" ||
            translatedTarLangCode == "be" || translatedTarLangCode == "bg" || translatedTarLangCode == "ceb" ||
            translatedTarLangCode == "co" /*|| translatedTarLangCode == "tl"*/ || translatedTarLangCode == "ka" ||
            translatedTarLangCode == "gu" || translatedTarLangCode == "ht" || translatedTarLangCode == "he" ||
            translatedTarLangCode == "hmn" /*|| translatedTarLangCode == "id"*/ || translatedTarLangCode == "jw" ||
            translatedTarLangCode == "kn" || translatedTarLangCode == "kk" || translatedTarLangCode == "ku" ||
            translatedTarLangCode == "ky" || translatedTarLangCode == "lo" || translatedTarLangCode == "la" ||
            translatedTarLangCode == "lv" || translatedTarLangCode == "lt" || translatedTarLangCode == "mk" ||
            translatedTarLangCode == "ml" || translatedTarLangCode == "mi" || translatedTarLangCode == "mr" ||
            translatedTarLangCode == "mn" || translatedTarLangCode == "my" || translatedTarLangCode == "ny" ||
            translatedTarLangCode == "ps" || translatedTarLangCode == "fa" || translatedTarLangCode == "pa" ||
            translatedTarLangCode == "sd" || translatedTarLangCode == "su" || translatedTarLangCode == "tg" ||
            translatedTarLangCode == "sl" || translatedTarLangCode == "te" || /*translatedTarLangCode == "ur" ||*/
            translatedTarLangCode == "xh" || translatedTarLangCode == "yi"
            )
}

fun startLanguageActivity(activity: Activity, origin: String, type: String) {
    val intent = Intent(activity, LanguageSelection::class.java)
    intent.putExtra("origin", origin)
    intent.putExtra("type", type)
    activity.startActivityForResult(intent, REQ_CODE_LANGUAGE_SELECTION)

}

fun startLanguageActivityFromFictional(
    activity: Activity,
    origin: String,
    type: String,
    from: String
) {
    val intent = Intent(activity, LanguageSelection::class.java)
    intent.putExtra("origin", origin)
    intent.putExtra("type", type)
    intent.putExtra("from", from)
    activity.startActivityForResult(intent, REQ_CODE_LANGUAGE_SELECTION)

}

fun getMicVisibility(srcCode: String): Boolean {
    return !(srcCode == "sq" || srcCode == "be" || srcCode == "bs" || srcCode == "ceb" || srcCode == "co" || srcCode == "eo" ||
            srcCode == "fy" || srcCode == "ht" || srcCode == "ha" || srcCode == "haw" || srcCode == "hmn" || srcCode == "ig" ||
            srcCode == "ga" || srcCode == "kk" || srcCode == "ku" || srcCode == "ky" || srcCode == "la" ||
            srcCode == "lb" || srcCode == "mx" || srcCode == "mg" || srcCode == "mt" || srcCode == "mi" ||
            srcCode == "mn" || srcCode == "nf" || srcCode == "ps" || srcCode == "pa" || srcCode == "gd" || srcCode == "sd" ||
            srcCode == "sn" || srcCode == "so" || srcCode == "tg" || srcCode == "cy" || srcCode == "xh" || srcCode == "yi" ||
            srcCode == "yu")
}

fun getClearDataDialog(activity: Activity): Dialog {
    val dialog = Dialog(activity, R.style.CustomDialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)
    dialog.setContentView(R.layout.dlg_clear_history)

    activity.window.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    val window = dialog.window
    if (window != null) {
        //            window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val windowLayoutParams = window.attributes
        windowLayoutParams.dimAmount = 0.7f
    }
    return dialog

}

fun getFreeTrialDialog(activity: Activity): Dialog {
    val dialog = Dialog(activity, R.style.CustomDialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)
    dialog.setContentView(R.layout.dlg_free_trial)

    activity.window.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    val window = dialog.window
    if (window != null) {
        //            window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val windowLayoutParams = window.attributes
        windowLayoutParams.dimAmount = 0.7f
    }
    return dialog

}

fun Activity.getLanguageDialog(): Dialog {
    val dialog = Dialog(this, R.style.CustomDialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(true)
    dialog.setContentView(R.layout.dictionary_language_fragment)
    val lp = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window?.attributes)
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    lp.gravity = Gravity.CENTER
    lp.dimAmount = 0.7f
//    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//    val window = dialog.window
//    if (window != null) {
//        //            window.requestFeature(Window.FEATURE_NO_TITLE);
//        window.setBackgroundDrawableResource(android.R.color.transparent)
//
//        val windowLayoutParams = window.attributes
//        windowLayoutParams.dimAmount = 0.7f
//    }
    return dialog

}


fun getExitDialogNew(activity: Activity): Dialog {
    val exitDialog = Dialog(activity, R.style.customDialog)
    exitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    Objects.requireNonNull<Window>(exitDialog.window)
        .setBackgroundDrawableResource(android.R.color.transparent)
    exitDialog.setContentView(R.layout.dlg_exit_new)
    exitDialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    exitDialog.window?.setDimAmount(0.5f)
    exitDialog.setCancelable(false)
    exitDialog?.window?.attributes?.windowAnimations = R.style.dialogueStyle
    return exitDialog
}

fun getRateUsDialog(activity: Activity): Dialog {
    val rateUsDialog = Dialog(activity, R.style.exitDialog)
    rateUsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    Objects.requireNonNull<Window>(rateUsDialog.window)
        .setBackgroundDrawableResource(android.R.color.transparent)
    rateUsDialog.setContentView(R.layout.dlg_rate_us)
    rateUsDialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    rateUsDialog.window?.setDimAmount(0.5f)
    rateUsDialog.setCancelable(false)

    return rateUsDialog
}


fun getRemoveAdsDialog(activity: Activity): Dialog {
    val dialog = Dialog(activity, R.style.CustomDialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.dlg_remove_ads_new)

    activity.window.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    val window = dialog.window
    if (window != null) {
        //            window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val windowLayoutParams = window.attributes
        windowLayoutParams.dimAmount = 0.7f
    }
    return dialog

}

fun <T> getWithAdsList(itemList: ArrayList<T>, adObject: T, adPosition: Int): ArrayList<T> {
    val finalList = ArrayList<T>()


    when (itemList.size) {
        1 -> {

            finalList.add(0, adObject)
            finalList.addAll(itemList)
        }
        adPosition -> {
            finalList.add(0, adObject)
            finalList.addAll(itemList)
            finalList.add(adObject)
        }
        else -> {
            for ((i, item) in itemList.withIndex()) {
                if (i % adPosition == 0) {
                    finalList.add(adObject)
                }
                finalList.add(item)
            }
        }

    }

    return finalList
}

fun Activity.checkNetworkState(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        val nwInfo = connectivityManager.activeNetworkInfo ?: return false
        return nwInfo.isConnected
    }
}

fun getLanguages(): List<DictionaryLanguageModel>? {
    val languageModels: MutableList<DictionaryLanguageModel> = ArrayList()
    languageModels.add(DictionaryLanguageModel("English", "en", false))
    languageModels.add(DictionaryLanguageModel("Hindi", "hi", false))
    languageModels.add(DictionaryLanguageModel("Spanish", "es", false))
    languageModels.add(DictionaryLanguageModel("French", "fr", false))
    languageModels.add(DictionaryLanguageModel("Japanese", "ja", false))
    languageModels.add(DictionaryLanguageModel("Russian", "ru", false))
    languageModels.add(DictionaryLanguageModel("German", "de", false))
    languageModels.add(DictionaryLanguageModel("Italian", "it", false))
    languageModels.add(DictionaryLanguageModel("Korean", "ko", false))
    languageModels.add(DictionaryLanguageModel("Brazilian Portuguese", "pt-BR", false))
    languageModels.add(DictionaryLanguageModel("Chinese (Simplified)", "zh-CN", false))
    languageModels.add(DictionaryLanguageModel("Arabic", "ar", false))
    languageModels.add(DictionaryLanguageModel("Turkish", "tr", false))
    return languageModels
}

fun cameraPermissionAlreadyGranted(activity: Activity): Boolean {

    val result = ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.CAMERA
    )

    return result == PackageManager.PERMISSION_GRANTED

}

fun permissionAlreadyGranted(activity: Activity, permission: String): Boolean {

    val result = ContextCompat.checkSelfPermission(
        activity,
        permission
    )

    return result == PackageManager.PERMISSION_GRANTED

}

fun getRemoteConfig(): FirebaseRemoteConfig {

    val mFireBaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    val configSettings: FirebaseRemoteConfigSettings =
        FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1).build()
    mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
    mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)
    mFireBaseRemoteConfig.fetchAndActivate()

    return mFireBaseRemoteConfig

}

fun Activity.sendEmail(
    to: Array<String>?,
    subject: String, @Suppress("SameParameterValue") body: String
) {

    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
    if (to != null && to.isNotEmpty()) {
        intent.putExtra(Intent.EXTRA_EMAIL, to)
    }
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, body)
    putPrefBoolean("is_feedback", true)
    launchIntent(Intent.createChooser(intent, "Send email"))
}


private fun Activity.launchIntent(intent: Intent) {
    try {
        rawLaunchIntent(intent)
    } catch (ignored: ActivityNotFoundException) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(R.string.in_app_name)
        builder.setMessage("Message intent failed!")
        builder.setPositiveButton("OK", null)
        builder.show()
    }

}

private fun Activity.rawLaunchIntent(intent: Intent?) {
    if (intent != null) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (ignored: AndroidRuntimeException) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle(R.string.in_app_name)
            builder.setMessage("Message intent failed!")
            builder.setPositiveButton("OK", null)
            builder.show()
        }

    }
}

fun Context.isPremium(): Boolean {
    return TinyDB.getInstance(this).getBoolean(Constants.IS_PREMIUM)
}

fun Context.getPrefBool(key: String): Boolean {
    return TinyDB.getInstance(this).getBoolean(key)

}

fun Context.getPrefBoolFirst(key: String): Boolean {
    return TinyDB.getInstance(this).getFirstBoolean(key)

}

fun Context.getPrefInt(key: String): Int {
    return TinyDB.getInstance(this).getInt(key)

}

fun Context.getDefaultLang(key: String): Int {
    return TinyDB.getInstance(this).getIntDictionary(key)

}

fun Context.getPrefIntOne(key: String): Int {
    return TinyDB.getInstance(this).getIntOne(key)

}

fun Context.getPrefString(key: String): String {
    return TinyDB.getInstance(this).getString(key)

}

fun Context.getPrefLong(key: String, default: Long): Long {
    return TinyDB.getInstance(this).getLong(key, default)
}

fun Context.putPrefBoolean(key: String, value: Boolean) {
    TinyDB.getInstance(this).putBoolean(key, value)
}

fun Context.putPrefInt(key: String, value: Int) {
    TinyDB.getInstance(this).putInt(key, value)
}

fun Context.putPrefString(key: String, value: String) {
    TinyDB.getInstance(this).putString(key, value)
}

fun Context.putPrefLong(key: String, value: Long) {
    TinyDB.getInstance(this).putLong(key, value)
}

fun Activity.callHandlerForPref() {
    Handler().postDelayed({
        runOnUiThread(Runnable {
            putPrefBoolean("app_killed", false)
        })
    }, 500)
}

fun AppCompatActivity.getDeleteDialogAlert(): AlertDialog.Builder {
    val builder = AlertDialog.Builder(this)
    builder.setMessage("Are you sure to delete this file?")
    builder.setNegativeButton("Cancel") { dialog, which ->
        dialog.dismiss()
    }

    return builder

}

fun getSaveDialog(activity: Activity): Dialog {
    val saveDialog = Dialog(activity, R.style.customDialog)
    saveDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    Objects.requireNonNull<Window>(saveDialog.window)
        .setBackgroundDrawableResource(android.R.color.transparent)
    saveDialog.setContentView(R.layout.dlg_save_conversation)
    saveDialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    saveDialog.window?.setDimAmount(0.5f)
    saveDialog.setCancelable(false)
    saveDialog.window?.attributes?.windowAnimations = R.style.exitDialogTheme

    return saveDialog
}

fun getJsonConversationFromList(dataList: List<ConversationModel>): String {
    val map: MutableMap<String, List<ConversationModel>> = HashMap()
    map["conversationList:"] = dataList
    return Gson().toJson(map)
}

fun getListFromJson(stringList: String): MutableList<ConversationModel> {
    return Constants.convertJsonToList(stringList)
}

fun isServiceRunning(activity: Activity, serviceClass: Class<*>): Boolean {
    val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    try {
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }

    return false
}

fun startClipboardService(activity: Activity) {
    kotlin.runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(Intent(activity, ClipBoardDataManager::class.java))
        } else {
            activity.startService(Intent(activity, ClipBoardDataManager::class.java))

        }
    }
}

fun Activity.showInterstitial(callback: (() -> Unit)? =null) {
    if (isPremium()) {
        callback?.invoke()
        return
    }
    val config = getRemoteConfig().getDouble("GPS126_taps_interstitial")
    val maxInter = getRemoteConfig().getDouble("gps126_max_interstitial_session")
    (application as AppBase).addInterstitialSessionCount++
    Log.d("adShow->","addInterstitialSessionCount= "+ (application as AppBase).addInterstitialSessionCount)
    Log.d("adShow->","config= "+ config)
    Log.d("adShow->","maxintersatitial= "+ config)
    Log.d("adShow->","sessionCount= "+ (application as AppBase).interstitialSessionCount)
    if ((applicationContext as AppBase).addInterstitialSessionCount >= config.toInt()&&maxInter.toInt()>(application as AppBase).interstitialSessionCount) {
        showProgressBlurDialog(this)
        Handler(Looper.getMainLooper()).postDelayed(Runnable {


            if (MaxAdManager.checkIsInterIsReady()) {
                MaxAdManager.showInterAd(this,object :OnAdShowCallback{
                    override fun onAdShow(ishow: Boolean) {
                        dismissProgressDialog()
                        callback?.invoke()
                        (application as AppBase).addInterstitialSessionCount = 0
                        (application as AppBase).interstitialSessionCount++
                    }
                })
            }
            else{
                dismissProgressDialog()
                callback?.invoke()
                CoroutineScope(Dispatchers.Default).launch {
                    if (TinyDB.getInstance(this@showInterstitial).isConsentGivenGDPR) {
                        MaxAdManager.loadInterAd(this@showInterstitial, object : MaxAdCallbacks {
                            override fun onAdLoaded(adLoad: Boolean) {
                            }

                            override fun onAdShowed(adShow: Boolean) {
                            }

                            override fun onAdHidden(adHidden: Boolean) {
                            }

                            override fun onAdLoadFailed(adLoadFailed: Boolean) {
                            }

                            override fun onAdDisplayFailed(adDisplayFailed: Boolean) {
                            }
                        })
                    }
                }
            }

        },2000)

    } else {

        callback?.invoke()

    }


}

fun stopClipBoard(activity: Activity) {
    activity.stopService(Intent(activity, ClipBoardDataManager::class.java))
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