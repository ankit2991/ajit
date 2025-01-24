package com.messaging.textrasms.manager.feature.adapters.conversations

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.messaging.textrasms.manager.InterstitialConditionDisplay
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkRealmAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.util.AppUtils
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.Utils
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.util.PhoneNumberUtils
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.resolveThemeColor
import io.realm.RealmResults
import kotlinx.android.synthetic.main.conversation_list_item.*
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import java.util.*
import javax.inject.Inject

class RecentAdapter @Inject constructor(
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator,
    private val prefs: Preferences,
    private val phoneNumberUtils: PhoneNumberUtils
) : QkRealmAdapter<Conversation>() {
    @Inject
    lateinit var utli: Utils

    init {
        // This is how we access the threadId for the swipe actions
//        setHasStableIds(true)
    }

    companion object {
        var isMultiSelectall = false
        var Clickconversation = true
    }

    var isMultiSelect: Boolean = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.layout_recent, parent, false)
        if( Preferences.getBoolean(context,Preferences.IS_LANG_SELECTED)) {
            changelanguge(prefs.Language.get())
        }
        if (viewType == 1) {
            val textColorPrimary = parent.context.resolveThemeColor(android.R.attr.textColorPrimary)
//
            view.title.setTypeface(view.title.typeface, Typeface.BOLD)

        }

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnClickListener

                isMultiSelect = false
                navigator.showConversation(conversation.id)
                InterstitialConditionDisplay.getInstance().increaseClicked()


            }
        }

    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(msg, msg)
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {

        val conversation = getItem(position) ?: return

        holder.containerView.isActivated = isSelected(conversation.id)

        holder.avatars.recipients = conversation.recipients
        holder.title.text = conversation.getTitle()
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: -1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.unread == false) 0 else 1
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

    fun selectall(conversations: RealmResults<Conversation>) {
        for (brandsData in conversations.indices) {
            val conversation = getItem(brandsData)
            if (!isSelected(conversation!!.id)) {
                addelection(conversation.id)
            }
        }
        BackgroundTask(conversations).execute()


    }

    lateinit var conversations: RealmResults<Conversation>

    private inner class BackgroundTask(conversations1: RealmResults<Conversation>) :
        AsyncTask<Void, Void, String>() {

        init {
            conversations = conversations1
        }

        override fun onPreExecute() {
        }

        override fun doInBackground(vararg params: Void): String? {
            selectionChanges.onNext(selection)
            isMultiSelectall = true
            return ""
        }

        override fun onPostExecute(result: String) {
            notifyDataSetChanged()
        }
    }

}
