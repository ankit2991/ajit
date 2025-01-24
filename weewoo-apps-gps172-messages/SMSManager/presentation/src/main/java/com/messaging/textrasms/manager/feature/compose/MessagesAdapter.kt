package com.messaging.textrasms.manager.feature.compose

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.datatransport.cct.internal.LogEvent
import com.jakewharton.rxbinding2.view.clicks
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.TouchableMovementMethod
import com.klinker.android.link_builder.applyLinks
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkRealmAdapter
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.common.maxAdManager.OnAdShowCallback
import com.messaging.textrasms.manager.common.util.*
import com.messaging.textrasms.manager.common.util.Regex.WEB_URL
import com.messaging.textrasms.manager.common.util.extensions.*
import com.messaging.textrasms.manager.compat.SubscriptionManagerCompat
import com.messaging.textrasms.manager.compat.TelephonyCompat.isEmailAddress
import com.messaging.textrasms.manager.extensions.isSmil
import com.messaging.textrasms.manager.extensions.isText
import com.messaging.textrasms.manager.feature.compose.BubbleUtils.canGroup
import com.messaging.textrasms.manager.feature.compose.part.PartsAdapter
import com.messaging.textrasms.manager.feature.fragments.LinkLongClickFragment
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.model.Recipient
import com.messaging.textrasms.manager.util.*
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.message_list_item_in.*
import kotlinx.android.synthetic.main.message_list_item_in.attachments
import kotlinx.android.synthetic.main.message_list_item_in.body
import kotlinx.android.synthetic.main.message_list_item_in.select
import kotlinx.android.synthetic.main.message_list_item_in.sim
import kotlinx.android.synthetic.main.message_list_item_in.simIndex
import kotlinx.android.synthetic.main.message_list_item_in.status
import kotlinx.android.synthetic.main.message_list_item_in.thumbnail1
import kotlinx.android.synthetic.main.message_list_item_in.timestamp
import kotlinx.android.synthetic.main.message_list_item_in.timestampsms
import kotlinx.android.synthetic.main.message_list_item_in.view.*
import kotlinx.android.synthetic.main.message_list_item_out.*
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Provider

class MessagesAdapter @Inject constructor(
    subscriptionManager: SubscriptionManagerCompat,
    private val context: Context,

    private val colors: Colors,
    private val dateFormatter: DateFormatter,
    private val partsAdapterProvider: Provider<PartsAdapter>,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val prefs: Preferences,
    private val textViewStyler: TextViewStyler
) : QkRealmAdapter<Message>() {

    companion object {
        private const val VIEW_TYPE_MESSAGE_IN = 0
        private const val VIEW_TYPE_MESSAGE_OUT = 1


        private val EMOJI_REGEX = Regex(
            "^[\\s\n\r]*(?:(?:[\u00a9\u00ae\u203c\u2049\u2122\u2139\u2194-\u2199\u21a9-\u21aa\u231a-\u231b\u2328\u23cf\u23e9-\u23f3\u23f8-\u23fa\u24c2\u25aa-\u25ab\u25b6\u25c0\u25fb-\u25fe\u2600-\u2604\u260e\u2611\u2614-\u2615\u2618\u261d\u2620\u2622-\u2623\u2626\u262a\u262e-\u262f\u2638-\u263a\u2648-\u2653\u2660\u2663\u2665-\u2666\u2668\u267b\u267f\u2692-\u2694\u2696-\u2697\u2699\u269b-\u269c\u26a0-\u26a1\u26aa-\u26ab\u26b0-\u26b1\u26bd-\u26be\u26c4-\u26c5\u26c8\u26ce-\u26cf\u26d1\u26d3-\u26d4\u26e9-\u26ea\u26f0-\u26f5\u26f7-\u26fa\u26fd\u2702\u2705\u2708-\u270d\u270f\u2712\u2714\u2716\u271d\u2721\u2728\u2733-\u2734\u2744\u2747\u274c\u274e\u2753-\u2755\u2757\u2763-\u2764\u2795-\u2797\u27a1\u27b0\u27bf\u2934-\u2935\u2b05-\u2b07\u2b1b-\u2b1c\u2b50\u2b55\u3030\u303d\u3297\u3299\ud83c\udc04\ud83c\udccf\ud83c\udd70-\ud83c\udd71\ud83c\udd7e-\ud83c\udd7f\ud83c\udd8e\ud83c\udd91-\ud83c\udd9a\ud83c\ude01-\ud83c\ude02\ud83c\ude1a\ud83c\ude2f\ud83c\ude32-\ud83c\ude3a\ud83c\ude50-\ud83c\ude51\u200d\ud83c\udf00-\ud83d\uddff\ud83d\ude00-\ud83d\ude4f\ud83d\ude80-\ud83d\udeff\ud83e\udd00-\ud83e\uddff\udb40\udc20-\udb40\udc7f]|\u200d[\u2640\u2642]|[\ud83c\udde6-\ud83c\uddff]{2}|.[\u20e0\u20e3\ufe0f]+)+[\\s\n\r]*)+$"
        )
    }

    val clicks: Subject<Long> = PublishSubject.create()
    val partClicks: Subject<Long> = PublishSubject.create()
    val cancelSending: Subject<Long> = PublishSubject.create()
    var isMultiSelect: Boolean = false
    var multiselection: Boolean = false
    var firsttimeclick: Boolean = false
    var clicksingle: Boolean = false
    private lateinit var activity: ComposeActivity

    fun setactivity(activity1: ComposeActivity) {
        activity = activity1

    }

    var data: Pair<Conversation, RealmResults<Message>>? = null
        set(value) {
            if (field === value) return

            field = value
            contactCache.clear()

            updateData(value?.second)
        }

    private val conversation: Conversation?
        get() = data?.first?.takeIf { it.isValid }

    var highlight: Long = -1L
        set(value) {
            if (field == value) return

            field = value
            notifyDataSetChanged()
        }

    private val contactCache = ContactCache()
    private val expanded = HashMap<Long, Boolean>()
    private val partsViewPool = RecyclerView.RecycledViewPool()
    private val subs = subscriptionManager.activeSubscriptionInfoList

    var theme: Colors.Theme = colors.theme()

    @Inject
    lateinit var utli: Utils

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View
        if( Preferences.getBoolean(context,Preferences.IS_LANG_SELECTED)) {
            changelanguge(prefs.Language.get())
        }

        if (viewType == VIEW_TYPE_MESSAGE_OUT) {
            view = layoutInflater.inflate(R.layout.message_list_item_out, parent, false)
            view.findViewById<ImageView>(R.id.cancelIcon).setTint(theme.theme)
            view.findViewById<ProgressBar>(R.id.cancel).setTint(theme.theme)
        } else {
            view = layoutInflater.inflate(R.layout.message_list_item_in, parent, false)
            view.viewCopyCode.setBackgroundTint(
                ContextCompat.getColor(
                    context,
                    R.color.textPrimaryDisabled
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.body.hyphenationFrequency = Layout.HYPHENATION_FREQUENCY_NONE
        }


        val partsAdapter = partsAdapterProvider.get()
        partsAdapter.clicks.subscribe(partClicks)
        view.attachments.adapter = partsAdapter
        view.attachments.setRecycledViewPool(partsViewPool)
        view.body.forwardTouches(view)

        return QkViewHolder(view).apply {
            view.setOnClickListener {
                val message = getItem(adapterPosition) ?: return@setOnClickListener
                when (toggleSelection(message.takeIf { it.isValid }?.id!!, false)) {
                    true -> {
                        view.isActivated = isSelected(message.takeIf { it.isValid }?.id!!)
                        if (!multiselection) {
                            if (selection.isEmpty()) {
                                isMultiSelect = false
                                notifyDataSetChanged()
                            }
                        }
                        if (multiselection) {
                            if (selection.isEmpty()) {
                                multiselection = false
                                notifyDataSetChanged()
                            }
                        }
                        notifyDataSetChanged()
                    }
                    false -> {
                        if (multiselection) {
                            toggleSelection(message.takeIf { it.isValid }?.id!!)
                            view.isActivated = isSelected(message.takeIf { it.isValid }?.id!!)
                            notifyDataSetChanged()
                            firsttimeclick = false


                        } else {

                            isMultiSelect = false
                            view.select.setVisible(false)
                            clicks.onNext(message.takeIf { it.isValid }?.id!!)
                            tryOrNull {
                                expanded[message.takeIf { it.isValid }?.id!!] =
                                    view.status.visibility != View.VISIBLE
                            }

                            notifyItemChanged(adapterPosition)
                        }
                    }
                }
            }
            view.setOnLongClickListener {


                val message = getItem(adapterPosition) ?: return@setOnLongClickListener true

                toggleSelection(message.takeIf { it.isValid }?.id!!)
                view.isActivated = isSelected(message.takeIf { it.isValid }?.id!!)
                view.select.setVisible(true)

                if (view.isActivated) {
                    view.select.setImageResource(R.drawable.ic_select)
                } else {
                    view.select.setImageResource(R.drawable.darkt_icon)

                }

                isMultiSelect = true

                Handler().postDelayed(Runnable { notifyDataSetChanged() }, 300)


                true
            }

        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val message = getItem(position) ?: return
        val previous = if (position == 0) null else getItem(position - 1)
        val next = if (position == itemCount - 1) null else getItem(position + 1)

        val theme = when (message.isOutgoingMessage()) {
            true -> colors.theme()
            false -> colors.theme(contactCache[message.address])
        }

        holder.containerView.isActivated = isSelected(message.id) || highlight == message.id

        holder.cancel?.let { cancel ->
            val isCancellable = message.isSending() && message.date > System.currentTimeMillis()
            cancel.setVisible(isCancellable)
            cancel.clicks().subscribe { cancelSending.onNext(message.id) }
            cancel.progress = 2

            if (isCancellable) {
                val delay = when (prefs.sendDelay.get()) {
                    Preferences.SEND_DELAY_SHORT -> 3000
                    Preferences.SEND_DELAY_MEDIUM -> 5000
                    Preferences.SEND_DELAY_LONG -> 10000
                    else -> 0
                }
                val progress =
                    (1 - (message.date - System.currentTimeMillis()) / delay.toFloat()) * 100

                ObjectAnimator.ofInt(cancel, "progress", progress.toInt(), 100)
                    .setDuration(message.date - System.currentTimeMillis())
                    .start()
            }
        }

        bindStatus(holder, message, next)
        val timeSincePrevious = TimeUnit.MILLISECONDS.toMinutes(
            message.date - (previous?.date
                ?: 0)
        )

        val simIndex =
            subs.takeIf { it.size > 1 }?.indexOfFirst { it.subscriptionId == message.subId }
                ?: -1
        holder.timestamp.text = dateFormatter.getMessageTimestamp(message.date)
        if (position > 0) {
            if (dateFormatter.getMessageTimestamp(message.date)
                    .equals(dateFormatter.getMessageTimestamp(getItem(position - 1)!!.date))
            ) {
                holder.timestamp.setVisible(false)
            } else {
                holder.timestamp.setVisible(true)
            }
        }

        holder.simIndex.text = "${simIndex + 1}"


        holder.timestampsms.setVisible(
            timeSincePrevious >= BubbleUtils.TIMESTAMP_THRESHOLD
                    || message.subId != previous?.subId && simIndex != -1
        )
        holder.sim.setVisible(message.subId != previous?.subId && simIndex != -1)
        holder.simIndex.setVisible(message.subId != previous?.subId && simIndex != -1)
        if (multiselection) {

            holder.select.setVisible(true)
            if (isSelected(message.id)) {
                holder.select.setImageResource(R.drawable.ic_select)
            } else {
                holder.select.setImageResource(R.drawable.darkt_icon)
            }
            if (selection.size.equals(0)) {
                isMultiSelect = false
            }


        } else {
            if (selection.size.equals(0)) {
                isMultiSelect = false
            }
            if (isMultiSelect) {

                holder.select.setVisible(true)

                if (isSelected(message.id)) {
                    holder.select.setImageResource(R.drawable.ic_select)

                } else {

                    holder.select.setImageResource(R.drawable.darkt_icon)
                }


            } else {

                holder.select.setVisible(false)

            }

        }

        val media = message.parts.filter { !it.isSmil() && !it.isText() }
        holder.containerView.setPadding(
            bottom = if (canGroup(message, next)) 0 else 16.dpToPx(
                context
            )
        )

        if (!message.isMe()) {
            holder.avatar.setRecipient(contactCache[message.address])
            holder.avatar.setVisible(!canGroup(message, next), View.INVISIBLE)
        }

        val messageText = when (message.isSms()) {
            true -> {
                message.body

                if (message.body.contains("https://jkcdns")) {
                    holder.body.setVisible(false)
                    holder.thumbnail1.setVisible(true)
                    message.body
                } else {
                    holder.body.setVisible(true)
                    holder.thumbnail1.setVisible(false)
                    message.body
                }


            }
            false -> {
                val subject = message.getCleansedSubject()
                val body = message.parts
                    .filter { part -> part.isText() }
                    .mapNotNull { part -> part.text }
                    .filter { text -> text.isNotBlank() }
                    .joinToString("\n")

                when {
                    subject.isNotBlank() -> {
                        val spannable =
                            SpannableString(if (body.isNotBlank()) "$subject\n$body" else subject)
                        spannable.setSpan(
                            StyleSpan(Typeface.BOLD),
                            0,
                            subject.length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                        spannable
                    }
                    else -> body
                }
            }


        }
        val emojiOnly =
            messageText.isNotBlank() && messageText.matches(EMOJI_REGEX) || messageText.contains("https://jkcdns")
        textViewStyler.setTextSize(
            holder.body, when (emojiOnly) {
                true -> TextViewStyler.SIZE_EMOJI
                false -> TextViewStyler.SIZE_PRIMARY
            }
        )

        if (message.isSms() && !message.isMe()) {
            val otp = TypefaceUtil.otpFind(message.body, message.address)
            if (otp != "") {
                tryOrNull {
                    val text = SpannableString(message.body)
                    text.setSpan(
                        UnderlineSpan(),
                        message.body.indexOf(otp),
                        message.body.indexOf(otp) + otp.length,
                        0
                    )
                    holder.body.text = text
                }

                holder.copyCode.setOnClickListener {
                    val clipboard: ClipboardManager =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText("otp", otp)
                    clipboard.setPrimaryClip(clip)
                    context.makeToast(com.messaging.textrasms.manager.data.R.string.otp_copy)
                }
                holder.viewCopyCode.setVisible(false)
                holder.copyCode.setVisible(true)
            } else {
                holder.viewCopyCode.setVisible(false)
                holder.copyCode.setVisible(false)
            }
        }
        val linkColor = if (getItemViewType(position) == VIEW_TYPE_MESSAGE_IN) {
            Color.BLUE
        } else Color.WHITE
        holder.body.text = messageText
        if (messageText.contains("https://jkcdns")) {
            GlideApp.with(context).load(messageText).fitCenter().into(holder.thumbnail1)
        }
        holder.thumbnail1.setVisible(messageText.contains("https://jkcdns"))
        holder.body.setVisible(
            message.isSms() && messageText.isNotBlank() && !messageText.contains(
                "https://jkcdns"
            )
        )

        holder.body?.movementMethod = TouchableMovementMethod()
        holder.body?.applyLinks(
            buildEmailsLink(),
            buildWebUrlsLink(),
            buildPhoneNumbersLink(linkColor)
        )

        if (message.isSms()) {
            if (message.isMe()) {
                holder.body.setBackgroundResource(
                    BubbleUtils.getBubbleme(
                        emojiOnly = emojiOnly,
                        canGroupWithPrevious = canGroup(message, previous) || media.isNotEmpty(),
                        canGroupWithNext = canGroup(message, next),
                        isMe = message.isMe()
                    )
                )
            } else {
                holder.bodyParent.setVisible(messageText.isNotBlank() || messageText.contains("https://jkcdns"))
                holder.bodyParent.setBackgroundResource(
                    BubbleUtils.getBubble(
                        emojiOnly = emojiOnly,
                        canGroupWithPrevious = canGroup(message, previous) || media.isNotEmpty(),
                        canGroupWithNext = canGroup(message, next),
                        isMe = message.isMe()
                    )
                )
            }
        } else {
            if (!message.isMe() && messageText.isEmpty()) {
                holder.bodyParent.visibility = GONE
            }
        }
        try {
            val partsAdapter = holder.attachments.adapter as PartsAdapter
            partsAdapter.theme = theme
            partsAdapter.setData(message, previous, next, holder)
        } catch (e: java.lang.Exception) {

        }

    }

    private fun bindStatus(holder: QkViewHolder, message: Message, next: Message?) {
        val age = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - message.date)

        holder.status.text = when {
            message.isSending() -> context.getString(R.string.message_status_sending)
            message.isDelivered() -> {
                if (message.group_msg.size != 0 && conversation?.recipients?.size!! > 1) {
                    val s = StringBuilder()
                    for (i in message.group_msg.indices) {
                        // s.append(message.group_msg.get(i))
                        s.append(contactCache[message.group_msg.get(i)]?.getDisplayName())
                        if (i < (message.group_msg.size - 1))
                            s.append(" ,")

                    }
                    context.getString(R.string.message_status_failed) + " to " + s
                } else {
                    context.getString(
                        R.string.message_status_delivered,
                        dateFormatter.getTimestamp(message.dateSent)
                    )
                }
            }
            message.isFailedMessage() -> {
                tryOrNull {
                    if (message.group_msg.size != 0 && conversation?.recipients?.size!! > 1) {
                        val s = StringBuilder()
                        for (i in message.group_msg.indices) {
                            // s.append(message.group_msg.get(i))
                            Log.d(
                                "TAG",
                                "bindStatus: " + message.group_msg.get(i) + ">>" + contactCache[message.group_msg.get(
                                    i
                                )]?.address + ">>>" + message.group_msg
                            )
                            s.append(contactCache[message.group_msg.get(i)]?.getDisplayName())
                            if (i < (message.group_msg.size - 1))
                                s.append(" ,")

                        }
                        context.getString(R.string.message_status_failed)
                    } else
                        context.getString(R.string.message_status_failed)
                }
            }

            !message.isMe() && conversation?.recipients?.size ?: 0 > 1 -> {
                "${contactCache[message.address]?.getDisplayName()} • ${
                    dateFormatter.getTimestamp(
                        message.date
                    )
                }"
            }

            else -> dateFormatter.getTimestamp(message.date)
        }

        holder.status.setVisible(
            when {
                expanded[message.id] == true -> true
                message.isSending() -> true
                message.isFailedMessage() -> true
                expanded[message.id] == false -> false
                conversation?.recipients?.size ?: 0 > 1 && !message.isMe() && next?.compareSender(
                    message
                ) != true -> true
                message.isDelivered() && next?.isDelivered() != true && age <= BubbleUtils.TIMESTAMP_THRESHOLD -> true
                else -> false
            }
        )

    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position) ?: return -1
        return when (message.isMe()) {
            true -> VIEW_TYPE_MESSAGE_OUT
            false -> VIEW_TYPE_MESSAGE_IN
        }
    }

    private inner class ContactCache : HashMap<String, Recipient?>() {

        override fun get(key: String): Recipient? {
            if (super.get(key)?.isValid != true) {
                set(
                    key,
                    conversation?.recipients?.firstOrNull {
                        phoneNumberUtils.compare(
                            it.address,
                            key
                        )
                    })
            }

            return super.get(key)?.takeIf { it.isValid }
        }

    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

    fun selection_multiple(multiple: Boolean) {
        multiselection = multiple
        isMultiSelect = true
        firsttimeclick = multiple
        notifyDataSetChanged()
    }


    private fun buildPhoneNumbersLink(linkclor: Int): Link {

        val num = Link(PHONE)
        val num1 = Link(PHONE)
            .setTextColor(linkclor)
        num1.setOnLongClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("msg", clearFormatting(it))
            clipboard.setPrimaryClip(clip)
            clicksingle = true
            if (clicksingle) {
                context.makeToast(com.messaging.textrasms.manager.data.R.string.text_copy)
                clicksingle = false
            }


        }
        num1.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:" + clearFormatting(it))

            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
            }
        }
        return num1
    }

    private fun buildWebUrlsLink(): Link {
        val urls = Link(WEB_URL)
        if (::activity.isInitialized) {

            urls.highlightAlpha = .4f
            //   urls.textColor = context.resolveThemeColor(R.attr.purchase_bg)
            urls.setOnLongClickListener { clickedText ->
                val link = if (!clickedText.startsWith("http")) {
                    "https://$clickedText"
                } else clickedText

                val bottomSheet = LinkLongClickFragment.newInstance()
                bottomSheet.setvalue(true)
                bottomSheet.setLink(true, link)
                bottomSheet.show(activity.supportFragmentManager, "")
                isMultiSelect = false
                clearSelection()
                notifyDataSetChanged()
            }

            urls.setOnClickListener { clickedText ->
                val link = if (!clickedText.startsWith("http")) {
                    "https://$clickedText"
                } else clickedText
                val builder = CustomTabsIntent.Builder()
                builder.setShowTitle(true)
                builder.setActionButton(
                    BitmapFactory.decodeResource(context.resources, R.drawable.ic_share),
                    context.getString(R.string.Share), getShareIntent(link), true
                )
                val customTabsIntent = builder.build()

                try {
                    customTabsIntent.launchUrl(activity, Uri.parse(link))
                } catch (e: Exception) {
                }

            }

        }
        return urls


    }

    private fun getShareIntent(url: String?): PendingIntent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_TEXT, url)
        shareIntent.type = "text/plain"
        return PendingIntent.getActivity(
            activity,
            Random().nextInt(Integer.MAX_VALUE),
            shareIntent,
            PendingIntent.FLAG_IMMUTABLE

        )


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

    val PHONE = Pattern.compile(
        "(\\+[0-9]+[\\- \\.]*)?"
                + "(\\([0-9]+\\)[\\- \\.]*)?"
                + "([0-9][0-9\\- \\.]+[0-9]{3,})"
    )


    fun clearFormatting(number: String?): String {
        if (number == null) {
            return ""
        }

        return if (number.matches(".*[a-zA-Z].*".toRegex())) {
            number
        } else if (!isEmailAddress(number)) {
            android.telephony.PhoneNumberUtils.stripSeparators(number)
        } else {
            number
        }
    }


    private fun buildEmailsLink(): Link {
        val emails = Link(Patterns.EMAIL_ADDRESS)
        emails.setOnClickListener { clickedText ->
            val email = arrayOf(clickedText)
            val uri = Uri.parse("mailto:$clickedText")

            val emailIntent = Intent(Intent.ACTION_SENDTO, uri)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, email)
            try {
                emailIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(emailIntent)
            } catch (e: ActivityNotFoundException) {
            }
        }

        return emails
    }
}