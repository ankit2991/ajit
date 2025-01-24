package com.messaging.textrasms.manager.feature.adapters.conversations

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.RecyclerView
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkRealmAdapterpersonal
import com.messaging.textrasms.manager.common.base.QkViewHolder
import com.messaging.textrasms.manager.common.interfaces.onAdfailedToLoadListner
import com.messaging.textrasms.manager.common.maxAdManager.BannerAdListener
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.MaxMainAdsManger
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.extensions.setTint
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.databinding.ListItemPremmyBinding
import com.messaging.textrasms.manager.feature.Activities.PurchaseActivity
import com.messaging.textrasms.manager.feature.ads.AdvertiseHandler
import com.messaging.textrasms.manager.feature.fragments.personal_fragment
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.Preferences.Companion.ADSREMOVED
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.messaging.textrasms.manager.util.tryOrNull
import com.messaging.textrasms.manager.utils.Constants
import io.realm.RealmResults
import kotlinx.android.synthetic.main.conversation_list_item.*
import kotlinx.android.synthetic.main.conversation_list_item.view.*
import kotlinx.android.synthetic.main.drawer_view.price_layout
import kotlinx.android.synthetic.main.drawer_view.price_txt
import kotlinx.android.synthetic.main.drawer_view.tv_go_premium
import kotlinx.android.synthetic.main.drawer_view.tv_unlock_everything
import kotlinx.android.synthetic.main.list_item_premmy.view.title_tv
import kotlinx.android.synthetic.main.list_item_premmy.view.title_tv_lng
import kotlinx.android.synthetic.main.max_banner_layout.view.bannerContainer
import kotlinx.android.synthetic.main.max_banner_layout.view.maxAdContainer
import kotlinx.android.synthetic.main.max_banner_layout.view.tvLoadingTextBanner
import xyz.teamgravity.checkinternet.CheckInternet
import javax.inject.Inject


class ConversationsAdapterpersonal @Inject constructor(
    private val context: Context,
    private val dateFormatter: DateFormatter,
    private val navigator: Navigator,
    val personalFragment: personal_fragment,
    private val prefs: Preferences

) : QkRealmAdapterpersonal<Conversation>() {

    var activity: Activity? = null
    var listSize: Int = 0

    private val TYPE_ITEM_READ = 0
    private val TYPE_ITEM_UNREAD = 1
    private val TYPE_HEADER = 2
    private val TYPE_AD = 3

    var isCalldorado = false

    fun setactivity(activity1: Activity) {
        activity = activity1
    }
    fun setDataSize(dataSize: Int) {
        Log.e("listSize",">"+dataSize)
        listSize = dataSize
//        notifyDataSetChanged()
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

        return if (Preferences.getBoolean(context, ADSREMOVED)) super.getItemCount()
        else super.getItemCount() + 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        advertiseHandler = AdvertiseHandler.getInstance(activity)
        if (viewType == TYPE_HEADER) {

            val view = layoutInflater.inflate(R.layout.list_item_premmy, parent, false)
//            setFeatureTextSizes(view)
            setFeatureTextSizesForLangs(view)
            return QkViewHolder(view).apply {
                view.setOnClickListener {

                    activity?.startActivity(Intent(activity, PurchaseActivity::class.java))
                }
            }
        }
//        else if(viewType == TYPE_AD){
//            val view = layoutInflater.inflate(R.layout.max_banner_layout, parent, false)
//
//            return QkViewHolder(view)
//        }
        else {
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
                view.main_content.setOnClickListener {

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
                    //  !Constants.conversationArraylist.contains(conversation.id

                    activity?.let { activity ->

                        if (getConnectionType(activity) == 1 || getConnectionType(activity) == 2 || getConnectionType(
                                activity
                            ) == 3
                        ) {


                            if (1 > Constants.conversationArraylist.size && !MaxMainAdsManger.isPurchased(
                                    activity
                                )
                            ) {


//                                showInterstitialAd() {
//                                    if (it) {
                                        Constants.conversationArraylist.add(conversation.id)

                                        finishAppStartUpAds()

                                        when (toggleSelection(
                                            conversation.takeIf { it.isValid }?.id!!,
                                            false
                                        )) {
                                            true -> {
                                                view.main_content.isActivated =
                                                    isSelected(conversation.takeIf { it.isValid }?.id!!)

                                                Toast.makeText(
                                                    activity,
                                                    "id is" + isSelected(conversation.takeIf { it.isValid }?.id!!).toString(),
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                // view.select.setVisible(true)
                                                if (selection.isEmpty()) {
                                                    isMultiSelect = false
                                                    notifyDataSetChanged()
                                                }
                                                if (view.main_content.isActivated) {
                                                    view.select.setVisible(true)
                                                } else {
                                                    view.select.setVisible(false)

                                                }
                                            }
                                            false -> {
                                                isMultiSelect = false
                                                view.select.setVisible(false)

                                                if (processClick) {
                                                    personalFragment.adshowing(conversation.takeIf { it.isValid }?.id!!)
                                                }
                                            }
                                        }

//                                    }
//                                }

                            } else {
                                when (toggleSelection(
                                    conversation.takeIf { it.isValid }?.id!!,
                                    false
                                )) {
                                    true -> {
                                        view.main_content.isActivated =
                                            isSelected(conversation.takeIf { it.isValid }?.id!!)

                                        Toast.makeText(
                                            activity,
                                            "id is" + isSelected(conversation.takeIf { it.isValid }?.id!!).toString(),
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // view.select.setVisible(true)
                                        if (selection.isEmpty()) {
                                            isMultiSelect = false
                                            notifyDataSetChanged()
                                        }
                                        if (view.main_content.isActivated) {
                                            view.select.setVisible(true)
                                        } else {
                                            view.select.setVisible(false)

                                        }
                                    }
                                    false -> {
                                        isMultiSelect = false
                                        view.select.setVisible(false)

                                        if (processClick) {
                                            personalFragment.adshowing(conversation.takeIf { it.isValid }?.id!!)
                                        }
                                    }
                                }

                            }
                        } else {
                            when (toggleSelection(conversation.takeIf { it.isValid }?.id!!, false)) {
                                true -> {
                                    view.main_content.isActivated =
                                        isSelected(conversation.takeIf { it.isValid }?.id!!)

                                    Toast.makeText(
                                        activity,
                                        "id is" + isSelected(conversation.takeIf { it.isValid }?.id!!).toString(),
                                        Toast.LENGTH_LONG
                                    ).show()

                                    // view.select.setVisible(true)
                                    if (selection.isEmpty()) {
                                        isMultiSelect = false
                                        notifyDataSetChanged()
                                    }
                                    if (view.main_content.isActivated) {
                                        view.select.setVisible(true)
                                    } else {
                                        view.select.setVisible(false)

                                    }
                                }
                                false -> {
                                    isMultiSelect = false
                                    view.select.setVisible(false)

                                    if (processClick) {
                                        personalFragment.adshowing(conversation.takeIf { it.isValid }?.id!!)
                                    }
                                }
                            }
                        }
                    }
                }
                view.main_content.setOnLongClickListener {

                    val realPosition = if (!Preferences.getBoolean(context, Preferences.ADSREMOVED)) {

                        adapterPosition - 1
                    } else {

                        adapterPosition
                    }
                    val conversation =
                        getItem(realPosition) ?: return@setOnLongClickListener true

                    toggleSelection(conversation.takeIf { it.isValid }?.id!!)
                    try {
                        view.main_content.isActivated = isSelected(conversation.takeIf { it.isValid }?.id!!)
                    } catch (e: java.lang.Exception) {

                    }


                    if (view.main_content.isActivated) {
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
        }
//        else if (((position + 1) % 6 == 0 && position != 0) && !Preferences.getBoolean(context, Preferences.ADSREMOVED)){
//
//            activity?.let { MaxAdManager.createBannerAd(it.applicationContext,holder.containerView.maxAdContainer,holder.containerView.bannerContainer,holder.containerView.tvLoadingTextBanner,object:BannerAdListener{
//                override fun bannerAdLoaded(isLoad: Boolean) {
//                }
//
//            } ) }
//        }
        else {
            val conversation = getItem(position-1) ?: return


            if ((position  % 5 == 0 && position != 0) && !Preferences.getBoolean(context, Preferences.ADSREMOVED)){
                holder.maxAdContainer.visibility = View.VISIBLE
                if (MaxAdManager.BANNERTYPE == 1) {
                    activity?.let {
                        MaxAdManager.BANNERTYPE = 2
                        MaxAdManager.createBannerAd(
                            it.applicationContext,
                            holder.containerView.maxAdContainer,
                            holder.containerView.bannerContainer,
                            holder.containerView.tvLoadingTextBanner,
                            object : BannerAdListener {
                                override fun bannerAdLoaded(isLoad: Boolean) {


                                }

                            })
                    }
                }else{
                    activity?.let {
                        MaxAdManager.BANNERTYPE = 1
                        MaxAdManager.createBannerAd2(
                            it.applicationContext,
                            holder.containerView.maxAdContainer,
                            holder.containerView.bannerContainer,
                            holder.containerView.tvLoadingTextBanner,
                            object : BannerAdListener {
                                override fun bannerAdLoaded(isLoad: Boolean) {


                                }

                            })
                    }
                }
            }else{
                holder.maxAdContainer.visibility = View.GONE
            }


            holder.containerView.main_content.isActivated = isSelected(conversation.id)


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
        }
//        else if(((position + 1) % 6 == 0 && position != 0) && !Preferences.getBoolean(context, ADSREMOVED)){
//            -9
//        }
        else {

            getItem(position - 1)?.id ?: -1
        }
    }

    private fun getAdCount(): Int {
        // Calculate the number of ad items based on SMS count
        if (listSize == 0){
            return 0
        }else{
            return listSize / 5
        }

    }

    override fun getItemViewType(position: Int): Int {

        return if (position == 0 && !Preferences.getBoolean(context, Preferences.ADSREMOVED) && !isCalldorado) {

            TYPE_HEADER
        }
//        else if(((position + 1) % 6 == 0 && position != 0) && !Preferences.getBoolean(context, Preferences.ADSREMOVED)){
//            TYPE_AD
//        }
        else {

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

    private fun showInterstitialAd(adShown: (shown: Boolean) -> Unit) {

        activity?.let {

            advertiseHandler!!.showProgress(it)
            //MaxAdInterAdShow>done
//            if (MaxInterAd.isInterstitialReady()) {
            if (MaxAdManager.checkIsInterIsReady()) {
                MaxMainAdsManger.showInterstitial(
                    activity
                ) { adShown(true) }
            } else {
                MaxMainAdsManger.initiateAd(it, object : onAdfailedToLoadListner {
                    override fun onAdFailedToLoad() {
                        adShown(true)
                    }

                    override fun onSuccess() {
                        advertiseHandler!!.showProgress(it)
                        MaxMainAdsManger.showInterstitial(it) {
                            adShown(true)
                        }
                    }

                    override fun adClose() {

                    }
                })

            }
        }
    }

    private fun finishAppStartUpAds() {

        activity?.let {

            advertiseHandler!!.hideProgress(it)
        }
    }

//    @Throws(InterruptedException::class, IOException::class)
//    fun isConnected(): Boolean {
//        val command = "ping -c 1 google.com"
//        return Runtime.getRuntime().exec(command).waitFor() == 0
//    }


    // @IntRange(from = 0, to = 3)
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

    private fun setFeatureTextSizesForLangs(view: View){
        val config = personalFragment.resources.configuration
        val Language = config.locale.language
        if ( Language != "en"){
            view.title_tv.visibility = View.GONE
            view.title_tv_lng.visibility = View.VISIBLE
        }else{
            view.title_tv.visibility = View.VISIBLE
            view.title_tv_lng.visibility = View.GONE
        }
    }
    private fun setFeatureTextSizes(view: View) {
        val config = personalFragment.resources.configuration
        val Language = config.locale.language
        if (
            Language == "az" ||
            Language == "ro" ||
            Language == "af" ||
            Language == "ur" ||
            Language == "da" ||
            Language == "nl" ||
            Language == "no" ||
            Language == "fa" ||
            Language == "cs" ||
            Language == "eo" ||
            Language == "et" ||
            Language == "fi" ||
            Language == "sq" ||
            Language == "sw" ||
            Language == "sv" ||
            Language == "sk" ||
            Language == "sl" ||
            Language == "te" ||
            Language == "ka" ||
            Language == "be" ||
            Language == "bg" ||
            Language == "bn" ||
            Language == "ga" ||
            Language == "gu" ||
            Language == "it" ||
            Language == "iw" ||
            Language == "is" ||
            Language == "in" ||
            Language == "hi" ||
            Language == "hr" ||
            Language == "ht" ||
            Language == "ko" ||
            Language == "la" ||
            Language == "lv" ||
            Language == "ms" ||
            Language == "mt" ||
            Language == "vi" ||
            Language == "cy" ||
            Language == "th"
        )
        {
            view.title_tv.textSize = context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._4sdp).toFloat()
        }
        if (
            Language == "es" ||
            Language == "be" ||
            Language == "bg" ||
            Language == "ca" ||
            Language == "fil" ||
            Language == "fr" ||
            Language == "gl" ||
            Language == "eu" ||
            Language == "de" ||
            Language == "el" ||
            Language == "hu" ||
            Language == "ja" ||
            Language == "kn" ||
            Language == "lt" ||
            Language == "mk" ||
            Language == "pl" ||
            Language == "pt" ||
            Language == "ru" ||
            Language == "sr" ||
            Language == "ta" ||
            Language == "tr" ||
            Language == "uk"){
            view.title_tv.textSize = context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._3sdp).toFloat()
        }
        if (Language == "ji"){
            view.title_tv.setPadding(0,0, context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._10sdp),0)
            view.title_tv.textSize = context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp).toFloat()
        }
    }


    class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // You can initialize ad-related views and set up ad-specific functionality here
        // For example:
        // private val adImageView: ImageView = itemView.findViewById(R.id.ad_image)
        // private val adTitleTextView: TextView = itemView.findViewById(R.id.ad_title)
        // ...
    }
}
