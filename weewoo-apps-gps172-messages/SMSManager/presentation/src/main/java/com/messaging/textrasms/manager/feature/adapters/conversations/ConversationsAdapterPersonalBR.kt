package com.messaging.textrasms.manager.feature.adapters.conversations

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkRealmAdapterpersonal
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.extensions.setTint
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.Activities.PurchaseActivity
import com.messaging.textrasms.manager.feature.ads.AdvertiseHandler
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.Preferences.Companion.ADSREMOVED
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.messaging.textrasms.manager.util.tryOrNull
import io.realm.RealmResults
import kotlinx.android.synthetic.main.conversation_list_item.*
import kotlinx.android.synthetic.main.conversation_list_item.view.*


class ConversationsAdapterPersonalBR(
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator
) : QkRealmAdapterpersonal<Conversation>() {




    var activity: Activity? = null

    private val TYPE_ITEM_READ = 0
    private val TYPE_ITEM_UNREAD = 1
    private val TYPE_HEADER = 2

    var isCalldorado = false

    fun setactivity(activity1: Activity) {
        activity = activity1
    }

    init {
        // This is how we access the threadId for the swipe actions
//        setHasStableIds(true)

    }

    var isMultiSelect: Boolean = false
    var advertiseHandler: AdvertiseHandler? = null

    companion object {
        var isMultiSelectall: Boolean = false
        var processClick = true
    }

    override fun getItemCount(): Int {
        /* return if (Preferences.getBoolean(context, ADSREMOVED)) super.getItemCount()
         else super.getItemCount()*/


        return super.getItemCount()



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        advertiseHandler = AdvertiseHandler.getInstance(activity)
        if (viewType == TYPE_HEADER) {

//            val view = layoutInflater.inflate(R.layout.list_item_premmy_calldarado_, parent, false)
            val view = layoutInflater.inflate(R.layout.list_item_premmy, parent, false)
            return QkViewHolder(view).apply {

                view.setOnClickListener {

                    activity?.startActivity(Intent(activity, PurchaseActivity::class.java))
                }
            }
        } else {
            val view = layoutInflater.inflate(R.layout.conversation_list_item, parent, false)
            if (viewType == 1) {
                val textColorPrimary =
                    parent.context.resolveThemeColor(android.R.attr.textColorPrimary)

                view.title.setTypeface(view.title.typeface, Typeface.BOLD)

                view.snippet.setTypeface(view.snippet.typeface, Typeface.BOLD)
                view.snippet.setTextColor(textColorPrimary)
                view.snippet.maxLines = 3
            }

            return QkViewHolder(view).apply {
                view.setOnClickListener {

                    val realPosition =
                        if (!Preferences.getBoolean(context, Preferences.ADSREMOVED)) {

                            adapterPosition - 1
                        } else {

                            adapterPosition
                        }
                    val conversation = getItem(realPosition) ?: return@setOnClickListener

                    navigator.showConversation(conversation.takeIf {  it2->
                        it2.isValid }?.id!!)
                }
                view.setOnLongClickListener {

                    val realPosition =
                        if (!Preferences.getBoolean(context, Preferences.ADSREMOVED)) {

                            adapterPosition - 1
                        } else {

                            adapterPosition
                        }
                    val conversation =
                        getItem(realPosition) ?: return@setOnLongClickListener true

                    toggleSelection(conversation.takeIf { it.isValid }?.id!!)
                    try {
                        view.isActivated = isSelected(conversation.takeIf { it.isValid }?.id!!)
                    } catch (e: java.lang.Exception) {

                    }


                    if (view.isActivated) {
                        view.select.setVisible(true)
                    } else {
                        view.select.setVisible(false)

                    }
                    true
                }
            }
        }
    }


    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {

        if (position == 0 && !Preferences.getBoolean(context, ADSREMOVED) && !isCalldorado) {

            Log.e("TAG", "BINDING!")
        } else {
            val conversation = getItem(position - 1) ?: return

            holder.containerView.isActivated = isSelected(conversation.id)


            holder.avatars.recipients = conversation.recipients
            holder.title.collapseEnabled = conversation.recipients.size > 1
            try {
                holder.title.text = conversation.getTitle()
            } catch (e: Exception) {
            }

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
                    tryOrNull {
                        if (conversation.snippet!!.contains("https://jkcdns")) {
                            holder.sticker.setVisible(true)
                            "Sticker"
                        } else {
                            holder.sticker.setVisible(false)
                            conversation.snippet
                        }
                    }
                }
            }
            holder.pinned.isVisible = conversation.pinned

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
            if (isMultiSelectall) {
                holder.select.setVisible(true)

            } else {
                holder.select.setVisible(false)
            }
        }
    }


    internal var mDebugLog = true
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(msg, msg)
    }

    override fun getItemId(position: Int): Long {
        return if (position == 0 && !Preferences.getBoolean(context, ADSREMOVED) && !isCalldorado) {

            -10
        } else {

            getItem(position - 1)?.id ?: -1
        }
    }

    override fun getItemViewType(position: Int): Int {

        return if (position == 0 && !Preferences.getBoolean(
                context,
                Preferences.ADSREMOVED
            ) && !isCalldorado
        ) {

            TYPE_HEADER
        } else {

            val realPosition = if (!Preferences.getBoolean(context, Preferences.ADSREMOVED)) {

                position - 1
            } else {

                position
            }
            if (getItem(realPosition)?.unread == false) {

                TYPE_ITEM_READ
            } else {

                TYPE_ITEM_UNREAD
            }
        }
    }

    fun requestStoragePermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    fun selectall(conversations: RealmResults<Conversation>) {
        tryOrNull {
            for (brandsData in conversations.indices) {
                val conversation = getItem(brandsData)
                if (!isSelected(conversation!!.id)) {
                    addelection(conversation.takeIf { it.isValid }?.id!!)
                }
            }
            BackgroundTask(conversations).execute()
        }

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



    // @IntRange(from = 0, to = 3)
    fun getConnectionType(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                val capabilities: NetworkCapabilities? =
                    cm.getNetworkCapabilities(cm.getActiveNetwork())
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = 3
                    }
                }
            }
        } else {
            if (cm != null) {
                val activeNetwork: NetworkInfo? = cm.getActiveNetworkInfo()
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() === ConnectivityManager.TYPE_WIFI) {
                        result = 2
                    } else if (activeNetwork.getType() === ConnectivityManager.TYPE_MOBILE) {
                        result = 1
                    } else if (activeNetwork.getType() === ConnectivityManager.TYPE_VPN) {
                        result = 3
                    }
                }
            }
        }
        return result
    }
}
