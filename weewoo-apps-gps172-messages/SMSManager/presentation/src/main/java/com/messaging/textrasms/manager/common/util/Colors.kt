package com.messaging.textrasms.manager.common.util

import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.core.content.res.getColorOrThrow
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.model.Recipient
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.getColorCompat
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

@Singleton
class Colors @Inject constructor(
    private val context: Context,
    private val prefs: Preferences
) {

    data class Theme(val theme: Int, private val colors: Colors) {
        val highlight by lazy { colors.highlightColorForTheme(theme) }
        val textPrimary by lazy { colors.textPrimaryOnThemeForColor(theme) }
        val textSecondary by lazy { colors.textSecondaryOnThemeForColor(theme) }
        val textTertiary by lazy { colors.textTertiaryOnThemeForColor(theme) }
        val textPrimaryprefix by lazy { colors.textPrimaryOnThemeForColorprefix(theme) }
    }

    data class Themep(val themep: Int, private val colors: Colors) {
        val highlight by lazy { colors.highlightColorForTheme(themep) }
        val textPrimary by lazy { colors.textPrimaryOnThemeForColor(themep) }
        val textSecondary by lazy { colors.textSecondaryOnThemeForColor(themep) }
        val textTertiary by lazy { colors.textTertiaryOnThemeForColor(themep) }
        val textPrimaryprefix by lazy { colors.textPrimaryOnThemeForColorprefix(themep) }
    }

    val materialColors: List<List<Int>> = listOf(
        R.array.material_red,
        R.array.material_pink,
        R.array.material_purple,
        R.array.material_deep_purple,
        R.array.material_indigo,
        R.array.material_blue,
        R.array.material_light_blue,
        R.array.material_cyan,
        R.array.material_teal,
        R.array.material_green,
        R.array.material_light_green,
        R.array.material_lime,
        R.array.material_yellow,
        R.array.material_amber,
        R.array.material_orange,
        R.array.material_deep_orange,
        R.array.material_brown,
        R.array.material_gray,
        R.array.material_blue_gray
    )
        .map { res -> context.resources.obtainTypedArray(res) }
        .map { typedArray -> (0 until typedArray.length()).map(typedArray::getColorOrThrow) }

    private val randomColorsprefix: List<Int> =
        context.resources.obtainTypedArray(R.array.random_colors_prefix)
            .let { typedArray -> (0 until typedArray.length()).map(typedArray::getColorOrThrow) }


    private val randomColors: List<Int> = context.resources.obtainTypedArray(R.array.random_colors)
        .let { typedArray -> (0 until typedArray.length()).map(typedArray::getColorOrThrow) }

    private val minimumContrastRatio = 2

    // Cache these values so they don't need to be recalculated
    private val primaryTextLuminance =
        measureLuminance(context.getColorCompat(R.color.textPrimaryDark))
    private val secondaryTextLuminance =
        measureLuminance(context.getColorCompat(R.color.textSecondaryDark))
    private val tertiaryTextLuminance =
        measureLuminance(context.getColorCompat(R.color.textTertiaryDark))

    fun theme(recipient: Recipient? = null): Theme {
        val pref = prefs.theme(recipient?.id ?: 0)
        //logDebug("theme>>"+recipient+">>"+prefs.autoColor.get())
        val color = when {
            recipient == null || !prefs.autoColor.get() || pref.isSet -> pref.get()
            else -> generateColor(recipient)
        }
        return Theme(color, this)
    }

    fun themeprefix(recipient: Recipient? = null): Themep {
        val pref = prefs.theme(recipient?.id ?: 0)
        //logDebug("theme>>"+recipient+">>"+prefs.autoColor.get())
        val color = when {
            recipient == null || !prefs.autoColor.get() || pref.isSet -> pref.get()
            else -> generateColorprefix(recipient)
        }
        return Themep(color, this)
    }

    fun themeObservable(recipient: Recipient? = null): Observable<Theme> {
        val pref = when {
            recipient == null -> prefs.theme()
            prefs.autoColor.get() -> prefs.theme(recipient.id, generateColor(recipient))
            else -> prefs.theme(recipient.id, prefs.theme().get())
        }
        return pref.asObservable()
            .map { color -> Theme(color, this) }
    }


    fun highlightColorForTheme(theme: Int): Int = FloatArray(3)
        .apply { Color.colorToHSV(theme, this) }
        .let { hsv -> hsv.apply { set(2, 0.75f) } } // 75% value
        .let { hsv -> Color.HSVToColor(85, hsv) } // 33% alpha

    fun textPrimaryOnThemeForColor(color: Int): Int = color
        .let { theme -> measureLuminance(theme) }
        .let { themeLuminance -> primaryTextLuminance / themeLuminance }
        .let { contrastRatio -> contrastRatio < minimumContrastRatio }
        .let { contrastRatio -> if (contrastRatio) R.color.textPrimary else R.color.textPrimaryDark }
        .let { res -> context.getColorCompat(res) }

    fun textSecondaryOnThemeForColor(color: Int): Int = color
        .let { theme -> measureLuminance(theme) }
        .let { themeLuminance -> secondaryTextLuminance / themeLuminance }
        .let { contrastRatio -> contrastRatio < minimumContrastRatio }
        .let { contrastRatio -> if (contrastRatio) R.color.textSecondary else R.color.textSecondaryDark }
        .let { res -> context.getColorCompat(res) }

    fun textTertiaryOnThemeForColor(color: Int): Int = color
        .let { theme -> measureLuminance(theme) }
        .let { themeLuminance -> tertiaryTextLuminance / themeLuminance }
        .let { contrastRatio -> contrastRatio < minimumContrastRatio }
        .let { contrastRatio -> if (contrastRatio) R.color.textTertiary else R.color.textTertiaryDark }
        .let { res -> context.getColorCompat(res) }

    fun textPrimaryOnThemeForColorprefix(color: Int): Int = color
        .let { theme -> measureLuminance(theme) }
        .let { themeLuminance -> primaryTextLuminance / themeLuminance }
        .let { contrastRatio -> contrastRatio < minimumContrastRatio }
        .let { contrastRatio -> if (contrastRatio) R.color.textPrimary else R.color.textPrimaryDark }
        .let { res -> context.getColorCompat(res) }

    /**
     * Measures the luminance value of a color to be able to measure the contrast ratio between two materialColors
     * Based on https://stackoverflow.com/a/9733420
     */
    private fun measureLuminance(color: Int): Double {
        val array = intArrayOf(Color.red(color), Color.green(color), Color.blue(color))
            .map { if (it < 0.03928) it / 12.92 else Math.pow((it + 0.055) / 1.055, 2.4) }

        return 0.2126 * array[0] + 0.7152 * array[1] + 0.0722 * array[2] + 0.05
    }

    private fun generateColor(recipient: Recipient): Int {
        val index = recipient.address.hashCode().absoluteValue % randomColors.size
        return randomColors[index]
    }

    private fun generateColorprefix(recipient: Recipient): Int {
        val index = recipient.address.hashCode().absoluteValue % randomColorsprefix.size
        //logDebug(">>color"+index+">>>"+randomColors[index])
        return randomColorsprefix[index]
    }

    internal var mDebugLog = true
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(msg, msg)
    }
}
