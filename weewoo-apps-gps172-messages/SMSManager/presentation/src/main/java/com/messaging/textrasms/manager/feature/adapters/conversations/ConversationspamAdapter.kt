package com.messaging.textrasms.manager.feature.adapters.conversations

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.messaging.textrasms.manager.InterstitialConditionDisplay
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkRealmAdapterspam
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.interfaces.onAdfailedToLoadListner
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.common.util.*
import com.messaging.textrasms.manager.common.util.extensions.setTint
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.Activities.PurchaseActivity
import com.messaging.textrasms.manager.feature.ads.AdvertiseHandler
import com.messaging.textrasms.manager.feature.fragments.spam_fragment
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.messaging.textrasms.manager.util.tryOrNull
import com.messaging.textrasms.manager.utils.Constants
import io.realm.RealmResults
import kotlinx.android.synthetic.main.conversation_list_item.*
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import xyz.teamgravity.checkinternet.CheckInternet
import java.util.*
import javax.inject.Inject

class ConversationspamAdapter @Inject constructor(
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator,
    val spamFragment: spam_fragment,
    private val prefs: Preferences
) : QkRealmAdapterspam<Conversation>() {
    @Inject
    lateinit var utli: Utils

    init {
        // This is how we access the threadId for the swipe actions
//        setHasStableIds(true)
    }

    lateinit var activity: Activity
    fun setactivity(activity1: Activity) {
        activity = activity1
    }

    companion object {
        var isMultiSelectall = false
        var Clickconversationspam = true
    }

    var advertiseHandler: AdvertiseHandler? = null

    lateinit var random: Random
    var isMultiSelect: Boolean = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        if (viewType == TYPE_HEADER) {

            val view = layoutInflater.inflate(R.layout.list_item_premmy, parent, false)
            return QkViewHolder(view).apply {

                view.setOnClickListener {

                    activity.startActivity(Intent(activity, PurchaseActivity::class.java))
                }
            }
        } else {
            val view = layoutInflater.inflate(R.layout.conversation_list_item, parent, false)

            advertiseHandler = AdvertiseHandler.getInstance(activity)
            if( Preferences.getBoolean(context,Preferences.IS_LANG_SELECTED)) {
                changelanguge(prefs.Language.get())
            }
            if (viewType == 1) {
                val textColorPrimary =
                    parent.context.resolveThemeColor(android.R.attr.textColorPrimary)

                view.title.setTypeface(view.title.typeface, Typeface.BOLD)

                view.snippet.setTypeface(view.snippet.typeface, Typeface.BOLD)
                view.snippet.setTextColor(textColorPrimary)
                view.snippet.maxLines = 3

                view.date.setTypeface(view.date.typeface, Typeface.BOLD)
                view.date.setTextColor(textColorPrimary)

            }

            return QkViewHolder(view).apply {
                view.setOnClickListener {
                    val realPosition = if (!Preferences.getBoolean(context, Preferences.ADSREMOVED)) {

                        adapterPosition - 1
                    } else {

                        adapterPosition
                    }
                    val conversation = getItem(realPosition) ?: return@setOnClickListener

                    /*
      store conversation id to constant arraylist..when user   first time click on conversation it store conversation
      id to constant array list and first time show interstitial ad and second time does not show interstitial for
      that conversation
       */
                    //!Constants.conversationArraylist.contains(conversation.id
                    if (getConnectionType(activity) == 1 || getConnectionType(activity) == 2 || getConnectionType(
                            activity
                        ) == 3
                    ) {


                        if (1 > Constants.conversationArraylist.size && !MaxMainAdsManger.isPurchased(
                                activity
                            )
                        ) {

//                            showInterstitialAd() {
//                                if (it) {
                                    Constants.conversationArraylist.add(conversation.id)
                                    finishAppStartUpAds()
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
                                            Log.d("count", "count" + i)
                                            if (Clickconversationspam) {
                                                if (i == 1) {
                                                    spamFragment.adshowing(conversation.takeIf { it.isValid }?.id!!)
                                                } else {
                                                    navigator.showConversation(
                                                        activity,
                                                        conversation.id
                                                    )
                                                    InterstitialConditionDisplay.getInstance().increaseClicked()
                                                    Clickconversationspam = false
                                                }
                                            }

                                        }
                                    }
//                                }
//                            }
                        } else {
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
                                    Log.d("count", "count" + i)
                                    if (Clickconversationspam) {
                                        if (i == 1) {
                                            spamFragment.adshowing(conversation.takeIf { it.isValid }?.id!!)
                                        } else {
                                            navigator.showConversation(activity, conversation.id)
                                            Clickconversationspam = false
                                        }
                                    }

                                }
                            }

                        }
                    } else {
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
                                Log.d("count", "count" + i)
                                if (Clickconversationspam) {
                                    if (i == 1) {
                                        spamFragment.adshowing(conversation.takeIf { it.isValid }?.id!!)
                                    } else {
                                        navigator.showConversation(activity, conversation.id)
                                        Clickconversationspam = false
                                    }
                                }

                            }
                        }

                    }

                }
                view.setOnLongClickListener {

                    val realPosition = if (!Preferences.getBoolean(context, Preferences.ADSREMOVED)) {

                        adapterPosition - 1
                    } else {

                        adapterPosition
                    }

                    val conversation =
                        getItem(realPosition) ?: return@setOnLongClickListener true

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
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {

        if (position == 0 && !Preferences.getBoolean(context, Preferences.ADSREMOVED)) {


        } else {

            val realPosition = if (!Preferences.getBoolean(context, Preferences.ADSREMOVED)) {

                position - 1
            } else {

                position
            }
            val conversation = getItem(realPosition) ?: return
            holder.containerView.isActivated = isSelected(conversation.id)
            holder.avatars.recipients = conversation.recipients
            holder.avatars.setVisible(true)
            holder.avatarsimg.setVisible(false)
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

    override fun getItemCount(): Int {
        return if (Preferences.getBoolean(context, Preferences.ADSREMOVED)) super.getItemCount()
        else super.getItemCount() + 1
    }

    override fun getItemId(position: Int): Long {
        return if (position == 0 && !Preferences.getBoolean(context, Preferences.ADSREMOVED)) {

            -10
        } else {

            getItem(position - 1)?.id ?: -1
        }
    }

    private val TYPE_ITEM_READ = 0
    private val TYPE_ITEM_UNREAD = 1
    private val TYPE_HEADER = 2

    override fun getItemViewType(position: Int): Int {

        return if (position == 0 && !Preferences.getBoolean(context, Preferences.ADSREMOVED)) {

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

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(msg, msg)
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
        logDebug("checklanguge" + Language + ">>>" + languageCode)
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

    //interstitial ad

    private fun showInterstitialAd(adShown: (shown: Boolean) -> Unit) {

        advertiseHandler!!.showProgress(activity)

//        Toast.makeText(this,"Inter ads show Called",Toast.LENGTH_SHORT).show()
        //MaxAdInterAdShow>done
//        if (MaxAdInter.isInterstitialReady()) {
        if (MaxAdManager.checkIsInterIsReady()) {
            MaxMainAdsManger.showInterstitial(
                activity
            ) { adShown(true) }
        } else {
            MaxMainAdsManger.initiateAd(activity, object : onAdfailedToLoadListner {
                override fun onAdFailedToLoad() {
                    adShown(true)
                }

                override fun onSuccess() {
                    advertiseHandler!!.showProgress(activity)
                    MaxMainAdsManger.showInterstitial(activity) {
                        adShown(true)
                    }
                }

                override fun adClose() {

                }
            })

        }

    }

    private fun finishAppStartUpAds() {
        advertiseHandler!!.hideProgress(activity)
    }

//    @Throws(InterruptedException::class, IOException::class)
//    fun isConnected(): Boolean {
//        val command = "ping -c 1 google.com"
//        return Runtime.getRuntime().exec(command).waitFor() == 0
//    }

    fun getConnectionType(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
//        val cm: ConnectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (cm != null) {
//                val capabilities: NetworkCapabilities? =
//                    cm.getNetworkCapabilities(cm.getActiveNetwork())
//                if (capabilities != null) {
//                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                        result = 2
//                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                        result = 1
//                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
//                        result = 3
//                    }
//                }
//            }
//        } else {
//            if (cm != null) {
//                val activeNetwork: NetworkInfo? = cm.getActiveNetworkInfo()
//                if (activeNetwork != null) {
//                    // connected to the internet
//                    if (activeNetwork.getType() === ConnectivityManager.TYPE_WIFI) {
//                        result = 2
//                    } else if (activeNetwork.getType() === ConnectivityManager.TYPE_MOBILE) {
//                        result = 1
//                    } else if (activeNetwork.getType() === ConnectivityManager.TYPE_VPN) {
//                        result = 3
//                    }
//                }
//            }
//        }

        CheckInternet().check(){
                connected ->
            if (connected){
                result = 2
            }else{
                result = 0
            }
        }
        return result
    }
}
