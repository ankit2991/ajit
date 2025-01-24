package com.messaging.textrasms.manager.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.text.bold
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.extensions.dpToPx
import com.messaging.textrasms.manager.feature.Activities.MainActivity
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.model.Contact
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.model.PhoneNumber
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.util.GlideApp
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.getColorCompat
import com.messaging.textrasms.manager.util.tryOrNull
import javax.inject.Inject

class WidgetAdapter(intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    companion object {
        private const val MAX_CONVERSATIONS_COUNT = 25
    }

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var conversationRepo: ConversationRepository

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var prefs: Preferences

    private val appWidgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )
    private val smallWidget = intent.getBooleanExtra("small_widget", false)
    private var conversations: List<Conversation> = listOf()
    private val appWidgetManager by lazy { AppWidgetManager.getInstance(context) }

    private val night get() = prefs.night.get()
    private val black get() = prefs.black.get()
    private val theme get() = colors.theme()
    private val background
        get() = context.getColorCompat(
            when {
                night && black -> R.color.black
                night && !black -> R.color.backgroundDark
                else -> R.color.white
            }
        )
    private val textPrimary
        get() = context.getColorCompat(if (night) R.color.textPrimaryDark else R.color.textPrimary)
    private val textSecondary
        get() = context.getColorCompat(if (night) R.color.textSecondaryDark else R.color.textSecondary)
    private val textTertiary
        get() = context.getColorCompat(if (night) R.color.textTertiaryDark else R.color.textTertiary)

    override fun onCreate() {
        appComponent.inject(this)
    }

    override fun onDataSetChanged() {
        conversations = conversationRepo.getConversationsSnapshot()

        val remoteViews = RemoteViews(context.packageName, R.layout.widget)
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, remoteViews)
    }

    override fun getCount(): Int {
        val count = Math.min(conversations.size, MAX_CONVERSATIONS_COUNT)
        val shouldShowViewMore = count < conversations.size
        return count + if (shouldShowViewMore) 1 else 0
    }

    override fun getViewAt(position: Int): RemoteViews {
        return when {
            position >= MAX_CONVERSATIONS_COUNT -> getOverflowView()
            else -> getConversationView(position)
        }
    }

    private fun getConversationView(position: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_list_item)

        try {


            val conversation = conversations[position]

            remoteViews.setViewVisibility(R.id.avatar, if (smallWidget) View.GONE else View.VISIBLE)
            remoteViews.setInt(R.id.avatar, "setBackgroundColor", theme.theme)
            remoteViews.setTextColor(R.id.initial, theme.textPrimary)
            remoteViews.setInt(R.id.icon, "setColorFilter", theme.textPrimary)
            remoteViews.setInt(R.id.avatarMask, "setColorFilter", background)

            val contact = conversation.recipients.map { recipient ->
                recipient.contact
                    ?: Contact().apply {
                        numbers.add(PhoneNumber().apply {
                            address = recipient.address
                        })
                    }
            }.firstOrNull()

            if (contact?.name.orEmpty().isNotEmpty()) {
                remoteViews.setTextViewText(R.id.initial, contact?.name?.substring(0, 1))
                remoteViews.setViewVisibility(R.id.icon, View.GONE)
            } else {
                remoteViews.setTextViewText(R.id.initial, null)
                remoteViews.setViewVisibility(R.id.icon, View.VISIBLE)
            }

            remoteViews.setImageViewBitmap(R.id.photo, null)
            val futureGet = GlideApp.with(context)
                .asBitmap()
                .load(contact?.photoUri)
                .submit(48.dpToPx(context), 48.dpToPx(context))
            tryOrNull(false) { remoteViews.setImageViewBitmap(R.id.photo, futureGet.get()) }

            remoteViews.setTextColor(R.id.name, textPrimary)
            remoteViews.setTextViewText(
                R.id.name,
                boldText(conversation.getTitle(), conversation.unread)
            )

            val timestamp =
                conversation.date.takeIf { it > 0 }?.let(dateFormatter::getConversationTimestamp)
            remoteViews.setTextColor(
                R.id.date,
                if (conversation.unread) textPrimary else textTertiary
            )
            remoteViews.setTextViewText(R.id.date, boldText(timestamp, conversation.unread))

            val snippet = when {
                conversation.draft.isNotEmpty() -> context.getString(
                    R.string.main_draft,
                    conversation.draft
                )
                conversation.me -> context.getString(R.string.main_sender_you, conversation.snippet)
                else -> conversation.snippet
            }
            remoteViews.setTextColor(
                R.id.snippet,
                if (conversation.unread) textPrimary else textTertiary
            )
            remoteViews.setTextViewText(R.id.snippet, boldText(snippet, conversation.unread))

            val clickIntent = Intent().putExtra("threadId", conversation.id)
            remoteViews.setOnClickFillInIntent(R.id.conversation, clickIntent)
        } catch (e: Exception) {

        }
        return remoteViews
    }

    private fun getOverflowView(): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.widget_loading)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent =
//            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            PendingIntent.getActivity(context,
                PendingIntent.FLAG_MUTABLE,
                intent,
                PendingIntent.FLAG_IMMUTABLE)
        view.setTextColor(R.id.loadingText, textSecondary)
        view.setTextViewText(R.id.loadingText, context.getString(R.string.widget_more))
        view.setOnClickPendingIntent(R.id.loading, pendingIntent)
        return view
    }

    private fun boldText(text: String?, shouldBold: Boolean): CharSequence? = when {
        shouldBold -> SpannableStringBuilder()
            .bold { append(text) }
        else -> text
    }

    override fun getLoadingView(): RemoteViews {
        val view = RemoteViews(context.packageName, R.layout.widget_loading)
        view.setTextViewText(R.id.loadingText, context.getText(R.string.widget_loading))
        return view
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun onDestroy() {
    }

}