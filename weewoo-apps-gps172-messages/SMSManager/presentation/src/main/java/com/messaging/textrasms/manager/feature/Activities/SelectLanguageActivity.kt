package com.messaging.textrasms.manager.feature.Activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.databinding.ActivitySelectLanguageBinding
import com.messaging.textrasms.manager.feature.adapters.CountryAdapter
import com.messaging.textrasms.manager.feature.models.Country
import com.messaging.textrasms.manager.util.Preferences

import com.yariksoffice.lingver.Lingver
import kotlinx.android.synthetic.main.archive_activity.nativeBanner
import kotlinx.android.synthetic.main.layout_big_native_ad.maxAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.nativeAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.textViewRectangleNative
import java.util.Locale

class SelectLanguageActivity : AppCompatActivity() {

    lateinit var binding: ActivitySelectLanguageBinding
    var countryList:List<Country>? = null
    val systemLanguage = Locale.getDefault().language

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_select_language)
        binding = ActivitySelectLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        changeTextColor()
        countriesList()




        binding.rvLanguage.layoutManager = LinearLayoutManager(this)

        val adapter = CountryAdapter(this,countryList!!){

            Lingver.getInstance().setLocale(this, it)
//            SharedPreferences.storeString(SharedPreferences.SELECTED_LANGUAGE,it,this)
            Preferences.setStringVal(this, Preferences.SELECTED_LANGUAGE,it)
            startActivity(Intent(this,MainActivity::class.java))
//            startActivity(Intent(this,PermissionActivity::class.java))
            finish()
        }

        binding.rvLanguage.adapter = adapter



    }


    fun changeTextColor(){
//        val translatedText = getString(R.string.select_language_txt) // Get the translated text dynamically
//
//        val spannable = SpannableString(translatedText)
//
//// Find the position of "Animated" and "Stickers" in the translated text
//        val animatedIndex = translatedText.indexOf(getString(R.string.select_your_txt))
//        val stickersIndex = translatedText.indexOf(getString(R.string.language_txt))
//
//// Set dark green color for "Animated"
//        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.ob_ind_select)), animatedIndex, animatedIndex + getString(R.string.select_your_txt).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//// Set light green color for "Stickers"
//        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.light_green_ob)), stickersIndex, stickersIndex + getString(R.string.language_txt).length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//        binding.txtAppLogo.text = spannable

    }

    fun countriesList(){

        countryList = listOf<Country>(
            Country("English","en"),
            Country("Afrikaans","af"),
            Country("Albanian","sq"),
            Country("Arabic","ar"),
            Country("Azerbaijani","az"),
            Country("Basque","eu"),
            Country("Belarusian","be"),
            Country("Bengali","bn"),
            Country("Bulgarian","bg"),
            Country("Catalan","ca"),
            Country("Chinese Simplified","zh"),
            Country("Chinese Traditional","zh"),
            Country("Croatian","hr"),
            Country("Czech","cs"),
            Country("Danish","da"),
            Country("Dutch","nl"),
            Country("Esperanto","eo"),
            Country("Estonian","et"),
            Country("Filipino","fil"),
            Country("Finnish","fi"),
            Country("French","fr"),
            Country("Galician","gl"),
            Country("Georgian","ka"),
            Country("German","de"),
            Country("Greek","el"),
            Country("Gujarati","gu"),
            Country("Haitian Creole","ht"),
            Country("Hebrew","iw"),
            Country("Hindi","hi"),
            Country("Hungarian","hu"),
            Country("Icelandic","is"),
            Country("Indonesian","in"),
            Country("Irish", "ga"),
            Country("Italian", "it"),
            Country("Japanese", "ja"),
            Country("Korean", "ko"),
            Country("Kannada", "kn"),
            Country("Latin", "la"),
            Country("Latvian", "lv"),
            Country("Lithuanian", "lt"),
            Country("Macedonian", "mk"),
            Country("Malay", "ms"),
            Country("Maltese", "mt"),
            Country("Norwegian", "no"),
            Country("Persian", "fa"),
            Country("Polish", "pl"),
            Country("Portuguese", "pt"),
            Country("Romanian", "ro"),
            Country("Russian", "ru"),
            Country("Serbian", "sr"),
            Country("Slovak", "sk"),
            Country("Slovenian", "sl"),
            Country("Spanish", "es"),
            Country("Swahili", "sw"),
            Country("Swedish", "sv"),
            Country("Tamil", "ta"),
            Country("Telugu", "te"),
            Country("Thai", "th"),
            Country("Turkish", "tr"),
            Country("Ukrainian", "uk"),
            Country("Urdu", "ur"),
            Country("Vietnamese", "vi"),
            Country("Welsh", "cy"),
            Country("Yiddish", "ji")

        )

        // Find the country that matches the system language
        val defaultCountry = countryList?.find { it.code == systemLanguage }

        // If a match is found, reorder the list with the default country at the top
        if (defaultCountry != null) {
            countryList = listOf(defaultCountry) + countryList!!.filter { it.code != systemLanguage }
        }
    }



    private fun isPurchased(): Boolean {
        val purchase = Preferences.getBoolean(this, Preferences.ADSREMOVED)
        return purchase
    }
    private fun showNativeAd() {
        if (!isPurchased()) {

            MaxAdManager.createNativeAd(this,maxAdContainer,nativeAdContainer,textViewRectangleNative,{
                nativeBanner.visibility = View.GONE
            },{
                nativeBanner.visibility = View.VISIBLE
            })

        } else {
            nativeBanner.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        showNativeAd()
    }
}