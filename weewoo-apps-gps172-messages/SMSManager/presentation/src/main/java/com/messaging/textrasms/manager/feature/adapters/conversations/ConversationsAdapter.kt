package com.messaging.textrasms.manager.feature.adapters.conversations

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.messaging.textrasms.manager.InterstitialConditionDisplay
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkRealmAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.util.AppUtils
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.Utils
import com.messaging.textrasms.manager.common.util.extensions.setTint
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.Activities.Promotionalactivity
import com.messaging.textrasms.manager.feature.Activities.SpamActivity
import com.messaging.textrasms.manager.feature.Activities.Transactionactivity
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.util.PhoneNumberUtils
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.resolveThemeColor
import io.realm.RealmResults
import kotlinx.android.synthetic.main.conversation_list_item.*
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import java.util.*
import javax.inject.Inject

class ConversationsAdapter @Inject constructor(
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

    lateinit var spamActivity: SpamActivity
    lateinit var promotionalactivity: Promotionalactivity
    lateinit var transactionactivity: Transactionactivity

    companion object {
        var isMultiSelectall = false
        var Clickconversation = true
    }

    fun setactivity(activity1: SpamActivity) {
        spamActivity = activity1
    }

    fun setactivitypromotinal(activity1: Promotionalactivity) {
        promotionalactivity = activity1
    }

    fun setactivitytransactional(activity1: Transactionactivity) {
        transactionactivity = activity1
    }

    lateinit var random: Random
    var isMultiSelect: Boolean = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.conversation_list_item, parent, false)
        if( Preferences.getBoolean(context,Preferences.IS_LANG_SELECTED)) {
            changelanguge(prefs.Language.get())
        }
        if (viewType == 1) {
            val textColorPrimary = parent.context.resolveThemeColor(android.R.attr.textColorPrimary)

            view.title.setTypeface(view.title.typeface, Typeface.BOLD)

            view.snippet.setTypeface(view.snippet.typeface, Typeface.BOLD)
            view.snippet.setTextColor(textColorPrimary)
            view.snippet.maxLines = 3

            view.date.setTypeface(view.date.typeface, Typeface.BOLD)
            view.date.setTextColor(textColorPrimary)
        }

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnClickListener
                when (toggleSelection(conversation.id, false)) {
                    true -> {
                        view.isActivated = isSelected(conversation.id)
                        // view.select.setVisible(true)
                        if (selection.isEmpty()) {
                            isMultiSelect = false
                            notifyDataSetChanged()
                        }
                        if (view.isActivated) {
                            view.select.setVisible(true)
                        } else {
                            view.select.setVisible(false)

                        }
                    }
                    false -> {
                        isMultiSelect = false
                        view.select.setVisible(false)
                        random = Random()
                        val i = random.nextInt(5)

                        if (Clickconversation) {
                            Log.d("count", "count" + i)
                            if (i == 1) {
                                if (::spamActivity.isInitialized && spamActivity != null) {
                                    spamActivity.adshowing(conversation.id)
                                }
                                if (::promotionalactivity.isInitialized && promotionalactivity != null) {
                                    promotionalactivity.adshowing(conversation.id)
                                }
                                if (::transactionactivity.isInitialized && transactionactivity != null) {
                                    transactionactivity.adshowing(conversation.id)
                                }
                            } else {
                                navigator.showConversation(conversation.id)
                                InterstitialConditionDisplay.getInstance().increaseClicked()
                                Clickconversation = false
                            }
                        }


                    }
                }
            }
            view.setOnLongClickListener {
                val conversation = getItem(adapterPosition) ?: return@setOnLongClickListener true

                toggleSelection(conversation.id)

                view.isActivated = isSelected(conversation.id)

                if (view.isActivated) {
                    view.select.setVisible(true)
                } else {
                    view.select.setVisible(false)

                }
                true
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
        holder.title.collapseEnabled = conversation.recipients.size > 1
        holder.title.text = conversation.getTitle()
        if (conversation.issending) {
            holder.failed.isVisible = true
            holder.error.isVisible = false
            if (conversation.lastMessage!!.isMms()) {
                holder.failed.setImageResource(R.drawable.ic_pending)
            } else {
                holder.failed.setImageResource(R.drawable.ic_process)
            }
        } else if (conversation.iserror) {
            holder.failed.isVisible = false
            holder.error.isVisible = true
            holder.error.setImageResource(R.drawable.ic_error)
        } else {
            holder.failed.isVisible = false
            holder.error.isVisible = false

        }
        holder.date.text =
            conversation.date.takeIf { it > 0 }?.let(dateFormatter::getConversationTimestamp)
        holder.snippet.text = when {

            conversation.draft.isNotEmpty() -> context.getString(
                R.string.main_draft,
                conversation.draft
            )
            conversation.me -> {
                if (conversation.snippet!!.contains("https://jkcdns")) {
                    holder.sticker.setVisible(true)
                    context.getString(R.string.main_sender_you, "Sticker")
                } else {
                    holder.sticker.setVisible(false)
                    context.getString(R.string.main_sender_you, conversation.snippet)
                }
            }
            else -> {
                if (conversation.snippet!!.contains("https://jkcdns")) {
                    holder.sticker.setVisible(true)
                    "Sticker"
                } else {
                    holder.sticker.setVisible(false)
                    conversation.snippet
                }
            }
        }
        holder.pinned.isVisible = conversation.pinned
        // If the last message wasn't incoming, then the colour doesn't really matter anyway
        val lastMessage = conversation.lastMessage
        val recipient = when {
            conversation.recipients.size == 1 || lastMessage == null -> conversation.recipients.firstOrNull()
            else -> conversation.recipients.find { recipient ->
                phoneNumberUtils.compare(recipient.address, lastMessage.address)
            }
        }
        if (conversation.unread) {
            holder.unread.setVisible(true)
            holder.unread.setTint(context.resources.getColor(R.color.tools_theme))
        } else {
            holder.unread.setVisible(false)
            holder.unread.setTint(context.resources.getColor(R.color.tools_theme))
        }
        if (selection.size.equals(0)) {
            isMultiSelect = false
        }
        if (isSelected(conversation.id)) {
            holder.select.setVisible(true)
        } else {
            holder.select.setVisible(false)

        }
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
