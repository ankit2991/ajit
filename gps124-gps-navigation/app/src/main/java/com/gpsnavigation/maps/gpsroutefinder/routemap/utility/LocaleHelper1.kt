package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.Log
import com.gpsnavigation.maps.gpsroutefinder.routemap.SELECTED_LANGUAGE
import timber.log.Timber
import java.util.*

object LocaleHelper1 {


    @JvmStatic
    @SuppressLint("DefaultLocale", "ObsoleteSdkInt")
    fun setAppLocale(context: Context, localeCode: String) {

        persist(context, localeCode)
        Timber.e("setAppLocale ${localeCode}")
        Log.d("LocaleHelper", "setAppLocale ${localeCode}")

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
              updateResources(context, localeCode)
        }else{
            try {
                val resources = context.resources
                val dm = resources.displayMetrics
                val config = resources.configuration
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    config.setLocale(Locale(localeCode.toLowerCase()))
                } else {
                    config.locale = Locale(localeCode.toLowerCase())
                }
                resources.updateConfiguration(config, dm)
            }catch (ex:Exception){
                Timber.e("ExLanguageSet ${ex}")
            }
        }*/
        try {
            val resources = context.resources
            val dm = resources.displayMetrics
            val config = resources.configuration

            val locale = Locale(localeCode)
            Locale.setDefault(locale)


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLocale(locale)
            } else {
                config.locale = locale
            }

            //Locale.setDefault(config.locale)

            resources.updateConfiguration(config, dm)


        } catch (ex: Exception) {
            Timber.e("ExLanguageSet ${ex}")
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    @JvmStatic
    fun setDefaultAppLocale(context: Context) {
        val resources = context.resources
        val dm = resources.displayMetrics
        val config = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(Locale(Locale.getDefault().getLanguage()))
        } else {
            config.locale = Locale(Locale.getDefault().getLanguage())
        }
        resources.updateConfiguration(config, dm)
    }

    private fun persist(context: Context, language: String) {
        TinyDB.getInstance(context).commitString(SELECTED_LANGUAGE, language)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String? {
        return TinyDB.getInstance(context).getString(SELECTED_LANGUAGE, defaultLanguage)
    }

    fun getLanguage(context: Context): String? {
        return getPersistedData(context, Locale.getDefault().language)
    }
}