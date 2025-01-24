package com.messaging.textrasms.manager.experiment

import android.content.Context
import android.preference.PreferenceManager
import java.util.*

abstract class Experiment<T>(val context: Context) {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private val prefKey: String
        get() = "experiment_$key"

    protected abstract val key: String
    protected abstract val variants: List<Variant<T>>
    protected abstract val default: T

    protected open val qualifies: Boolean by lazy { Locale.getDefault().language.startsWith("en") }

    val variant: T by lazy {
        when {
            !qualifies -> null

            prefs.contains(prefKey) -> {
                variants.firstOrNull { it.key == prefs.getString(prefKey, null) }?.value
            }

            else -> {
                variants[Random().nextInt(variants.size)].also { variant ->
                    prefs.edit().putString(prefKey, variant.key).apply()
                }.value
            }
        } ?: default
    }

}