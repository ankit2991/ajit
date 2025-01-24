package com.messaging.textrasms.manager.common.util

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ArrayRes
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.InterstitialConditionDisplay
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.resolveThemeColor
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.menu_list_item.*
import kotlinx.android.synthetic.main.menu_list_item.view.*
import java.util.*
import javax.inject.Inject

data class MenuItem(val title: String, val actionId: Int)

class MenuItemAdapter @Inject constructor(
    private val context: Context,
    private val colors: Colors,
    val preferences: Preferences
) : QkAdapter<MenuItem>() {

    val menuItemClicks: Subject<Int> = PublishSubject.create()

    private val disposables = CompositeDisposable()

    @Inject
    lateinit var prefs: Preferences

    var selectedItem: Int? = null
        set(value) {
            val old = data.map { it.actionId }.indexOfFirst { it == field }
            val new = data.map { it.actionId }.indexOfFirst { it == value }

            field = value

            old.let { notifyItemChanged(it) }
            new.let {
                notifyItemChanged(it)
            }
        }

    fun setData(@ArrayRes titles: Int, @ArrayRes values: Int = -1) {
        if( Preferences.getBoolean(context,Preferences.IS_LANG_SELECTED)) {
            changelanguge(preferences.Language.get())
        }
        val valueInts = if (values != -1) context.resources.getIntArray(values) else null

        data = context.resources.getStringArray(titles)
            .mapIndexed { index, title ->
                MenuItem(
                    title, valueInts?.getOrNull(index)
                        ?: index
                )
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.menu_list_item, parent, false)
        if( Preferences.getBoolean(context,Preferences.IS_LANG_SELECTED)) {
            changelanguge(preferences.Language.get())
        }
        val states = arrayOf(
            intArrayOf(android.R.attr.state_activated),
            intArrayOf(-android.R.attr.state_activated)
        )

        val text = parent.context.resolveThemeColor(android.R.attr.textColorTertiary)
        view.check.imageTintList = ColorStateList(states, intArrayOf(colors.theme().theme, text))

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val menuItem = getItem(adapterPosition)
                menuItemClicks.onNext(menuItem.actionId)
                InterstitialConditionDisplay.getInstance().increaseClicked()
            }
        }
    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val menuItem = getItem(position)
        if( Preferences.getBoolean(context,Preferences.IS_LANG_SELECTED)) {
            changelanguge(prefs.Language.get())
        }

        holder.title.text = menuItem.title
        holder.check.isActivated = (menuItem.actionId == selectedItem)
        holder.check.setVisible(selectedItem != null)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        disposables.clear()
    }

    fun changelanguge(languageCode: Int) {
        val config = context.resources.configuration
        var Language = "es"

        Language = when (languageCode) {
            0 -> Resources.getSystem().configuration.locale.language
            1 -> "ar"
            2 -> "bg"
            3 -> "bn"
            4 -> "cs"
            5 -> "de"
            6 -> "el"
            7 -> "en"
            8 -> "en-CA"
            9 -> "en-GB"
            10 -> "es"
            11 -> "fi"
            12 -> "fr"
            13 -> "gu"
            14 -> "hi"
            15 -> "hr"
            16 -> "hu"
            17 -> "in"
            18 -> "it"
            19 -> "ja"
            20 -> "ko"
            21 -> "nl"
            22 -> "nn"
            23 -> "pl"
            24 -> "pt-BR"
            25 -> "pt-PT"
            26 -> "ro"
            27 -> "ru"
            28 -> "sv"
            29 -> "tr"
            30 -> "uk"
            31 -> "vi"
            32 -> "zh"
            else -> Resources.getSystem().configuration.locale.language
        }
        val locale = Locale(Language)
        config.setLocale(locale)
        AppUtils.updateConfig(context, config)
        //  recreate()
    }

}