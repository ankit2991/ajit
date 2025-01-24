package com.messaging.textrasms.manager.feature.adapters

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.messaging.textrasms.manager.InterstitialConditionDisplay
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.extensions.removeAccents
import com.messaging.textrasms.manager.model.SearchResult
import kotlinx.android.synthetic.main.search_list_item.*
import javax.inject.Inject

class SearchAdapter @Inject constructor(
    colors: Colors,
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator
) : QkAdapter<SearchResult>() {

    private val highlightColor: Int by lazy { colors.theme().highlight }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.search_list_item, parent, false)
        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val result = getItem(adapterPosition)
                navigator.showConversation(
                    result.conversation.id,
                    result.query.takeIf { result.messages > 0 })
                InterstitialConditionDisplay.getInstance().increaseClicked()
            }
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val previous = data.getOrNull(position - 1)
        val result = getItem(position)
        holder.resultsHeader.setVisible(result.messages > 0 && previous?.messages == 0)

        val query = result.query
        val title = SpannableString(result.conversation.getTitle())
        var index = title.removeAccents().indexOf(query, ignoreCase = true)

        while (index >= 0) {
            try {
                title.setSpan(
                    BackgroundColorSpan(highlightColor),
                    index,
                    index + query.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                index = title.indexOf(query, index + query.length, true)
            } catch (E: Exception) {

            }

        }
        holder.title.text = title

        holder.avatars.recipients = result.conversation.recipients

        when (result.messages == 0) {
            true -> {
                holder.date.setVisible(true)
                holder.date.text = dateFormatter.getConversationTimestamp(result.conversation.date)
                holder.snippet.text = when (result.conversation.me) {
                    true -> context.getString(R.string.main_sender_you, result.conversation.snippet)
                    false -> result.conversation.snippet
                }
            }

            false -> {
                holder.date.setVisible(false)
                holder.snippet.text =
                    context.getString(R.string.main_message_results, result.messages)
            }
        }
    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(msg, msg)
    }

    override fun areItemsTheSame(old: SearchResult, new: SearchResult): Boolean {
        return old.conversation.id == new.conversation.id && old.messages > 0 == new.messages > 0
    }

    override fun areContentsTheSame(old: SearchResult, new: SearchResult): Boolean {
        return old.query == new.query && // Queries are the same
                old.conversation.id == new.conversation.id // Conversation id is the same
                && old.messages == new.messages // Result count is the same
    }
}