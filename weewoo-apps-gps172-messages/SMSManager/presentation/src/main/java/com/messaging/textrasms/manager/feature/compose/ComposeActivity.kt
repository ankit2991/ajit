package com.messaging.textrasms.manager.feature.compose

import android.Manifest
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.text.InputType
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.location.*
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.common.maxAdManager.OnAdShowCallback
import com.messaging.textrasms.manager.common.messgeData.ViewPagerAdapter
import com.messaging.textrasms.manager.common.util.*
import com.messaging.textrasms.manager.common.util.extensions.*
import com.messaging.textrasms.manager.feature.Activities.MainActivity
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsAdapter.Companion.Clickconversation
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsAdapterpersonal.Companion.processClick
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsOtherAdapter.Companion.Clickconversationother
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationspamAdapter.Companion.Clickconversationspam
import com.messaging.textrasms.manager.feature.ads.AdvertiseHandler
import com.messaging.textrasms.manager.feature.compose.editing.ReceipntAdapter
import com.messaging.textrasms.manager.feature.contacts.ContactsActivity
import com.messaging.textrasms.manager.feature.fragments.Category_fragment
import com.messaging.textrasms.manager.feature.groups.ContactsgroupActivity
import com.messaging.textrasms.manager.feature.simplenotes.NotesListActivity
import com.messaging.textrasms.manager.model.*
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.util.PhoneNumberUtils
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.makeToast
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.messaging.textrasms.manager.utils.Constants.INTER_COUNTER_MESSAGE_CLICK
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.compose_activity.*
import kotlinx.android.synthetic.main.compose_activity.ad_view_container
import kotlinx.android.synthetic.main.compose_activity.backcompose
import kotlinx.android.synthetic.main.compose_activity.recyclerView
import kotlinx.android.synthetic.main.compose_activity.toolbar
import kotlinx.android.synthetic.main.compose_activity.toolbarTitle
import kotlinx.android.synthetic.main.container_activity.*
import kotlinx.android.synthetic.main.layout_big_native_ad.maxAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.nativeAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.textViewRectangleNative
import kotlinx.android.synthetic.main.main_activity.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ComposeActivity : QkThemedActivity(), ComposeView, RecyclerAdapter.OnItemClickListener,
    Category_fragment.OnItemClickListenergifsend {
    override val isvaluestoresticker: Subject<Boolean> = PublishSubject.create()


    override fun requestgif() {
        isvaluestore.onNext(true)
        gallary_attach.setVisible(false)
    }

    override fun onItemClickedgifsend(pic: Uri) {

        picdata = pic

        message.setText("" + pic)

        sticker_main_layout.setVisible(false)
        gallary_attach.setVisible(false)

    }


    override val isvaluestore: Subject<Boolean> = PublishSubject.create()

    override val attachstickerclick by lazy { stickerlayout.clicks() }
    override val attachgifclick by lazy { giflayout.clicks() }

    override fun requestSticker() {
        sticker_main_layout.setVisible(true)
        isvaluestoresticker.onNext(true)

        gallary_attach.setVisible(false)

    }


    override fun hideKeyboard() {
        message.postDelayed({
            message.hideKeyboard()
        }, 200)

    }

    override fun selctmultiple(value: Boolean) {

    }

    fun afterdelete() {
        clearSelection()
        messageAdapter.selection_multiple(false)
        delete_layout.setVisible(false)
        toolbar.menu.findItem(R.id.deletemultiple)?.isVisible = true

    }

    override val attachmentSelectedrecentIntent: Observable<Uri>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun requestrecentpic() {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemClicked(pic: Uri) {
        picdata = pic
        picdata?.let(attachmentSelectedIntent::onNext)
        if (!ismobiledataOn()) {
            makeToast(R.string.pocket_data_on, Toast.LENGTH_LONG)
        }

    }


    companion object {

        private const val SelectContactRequestCode = 0
        private const val TakePhotoRequestCode = 1
        private const val AttachPhotoRequestCode = 2
        private const val AttachContactRequestCode = 3
        private const val notesRequestCode = 4
        private var emojiopen = false
        var attchopen = true
        private const val CameraDestinationKey = "camera_destination"
        var stickerlist = arrayListOf<Sticker>()

    }

    var multiselect: Boolean = false

    @Inject
    lateinit var attachmentAdapter: AttachmentAdapter

    @Inject
    lateinit var chipsAdapter: ReceipntAdapter

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var messageAdapter: MessagesAdapter

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val activityVisibleIntent: Subject<Boolean> = PublishSubject.create()
    override val chipsSelectedIntent: Subject<HashMap<String, String?>> = PublishSubject.create()
    override val chipDeletedIntent: Subject<Recipient> by lazy { chipsAdapter.chipDeleted }
    override val menuReadyIntent: Observable<Unit> = menu.map { Unit }
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val sendAsGroupIntent by lazy { sendAsGroupBackground.clicks() }
    override val messageClickIntent: Subject<Long> by lazy { messageAdapter.clicks }
    override val messagePartClickIntent: Subject<Long> by lazy { messageAdapter.partClicks }
    override val messagesSelectedIntent by lazy { messageAdapter.selectionChanges }
    override val cancelSendingIntent: Subject<Long> by lazy { messageAdapter.cancelSending }
    override val attachmentDeletedIntent: Subject<Attachment> by lazy { attachmentAdapter.attachmentDeleted }
    override val textChangedIntent by lazy { message.textChanges() }
    override val attachIntent by lazy {
        Observable.merge(
            attach.clicks(),
            attachingBackground.clicks()
        )
    }

    override val attachgalleryclick by lazy { image_layout.clicks() }

    override val cameraIntent by lazy {
        Observable.merge(
            camera.clicks(),
            camera_recent.clicks(),
            cameraLabel.clicks(),
            cameraLayout.clicks()
        )
    }
    override val galleryIntent by lazy {
        Observable.merge(
            gallery.clicks(),
            galleryLabel.clicks(),
            galleryLayout.clicks()
        )
    }
    override val scheduleIntent by lazy {
        Observable.merge(
            schedule.clicks(),
            scheduleLabel.clicks(),
            scheduleLayout.clicks()
        )
    }
    override val attachContactIntent by lazy {
        Observable.merge(
            contact.clicks(),
            contactLabel.clicks(),
            contactLayout.clicks()
        )
    }
    override val notesIntent by lazy {
        Observable.merge(
            notes.clicks(),
            notesLabel.clicks(),
            notesLayout.clicks()
        )
    }
    override val locationIntent by lazy {
        Observable.merge(
            location.clicks(),
            locationLabel.clicks(),
            locationLayout.clicks()
        )
    }
    override val attachmentSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val attachmentrecentSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val contactSelectedIntent: Subject<Uri> = PublishSubject.create()
    override val inputContentIntent by lazy { message.inputContentSelected }
    override val scheduleSelectedIntent: Subject<Long> = PublishSubject.create()
    override val changeSimIntent by lazy { sim.clicks() }
    override val emojiintent by lazy { emoji.clicks() }
    override val scheduleCancelIntent by lazy { scheduledCancel.clicks() }
    override val sendIntent by lazy { send.clicks() }
    override val backPressedIntent: Subject<Unit> = PublishSubject.create()
    override val reciepnt by lazy { plus.clicks() }
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            viewModelFactory
        )[ComposeViewModel::class.java]
    }

    private var cameraDestination: Uri? = null
    private var picdata: Uri? = null
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private lateinit var adaptergif: GifAdapter
    private lateinit var photos: ArrayList<Uri>

    private lateinit var sticker: ArrayList<Sticker>

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {

    }

    lateinit var wallpaperDirectory: File
    lateinit var dialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_activity)

        dialog = ProgressDialog(this)
        wallpaperDirectory =
            File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), ".sticker")

        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }
        // showBackButton(true)
        viewModel.bindView(this)

        INTER_COUNTER_MESSAGE_CLICK++
        if(INTER_COUNTER_MESSAGE_CLICK == 4) {
            Log.e("INTER_COUNTER", ">"+INTER_COUNTER_MESSAGE_CLICK)
            INTER_COUNTER_MESSAGE_CLICK = 0
            if (!Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
                MaxAdManager.showInterAd(this, object : OnAdShowCallback {
                    override fun onAdHidden(ishow: Boolean) {
                    }

                    override fun onAdfailed() {
                    }

                    override fun onAdDisplay() {
                    }

                })
            }
        }


/*
        if (!IronSourceAdsManger.isPurchased(this)){
            IronSourceAdsManger.showBanner(this,ad_view_container,"cmps")
        }
*/
//        loadBanner()


        //Old banner implementation......
        /*
        if (!Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
            AdvertiseHandler.getInstance(this)
                .loadBannerAds(this, UtilsData.banner_ad_unit_id, ad_view_container, null)
        }*/




        if (isNetworkAvailable()) {
            viewModel.LoardUrl(this)
        }

        contentView.layoutTransition = LayoutTransition().apply {
            disableTransitionType(LayoutTransition.CHANGING)
        }
        toolbarTitle.textSize = 18f

        chipsAdapter.view = chips

        chips.itemAnimator = null
        chips.layoutManager = FlexboxLayoutManager(this)

        messageAdapter.autoScrollToStart(messageList)
        messageAdapter.emptyView = messagesEmpty
        messageAdapter.setactivity(this)

        messageList.setHasFixedSize(true)
        messageList.adapter = messageAdapter


        attachments.adapter = attachmentAdapter



        message.supportsInputContent = true

        theme.doOnNext { loading.setTint(it.theme) }
            //  .doOnNext { attach.setBackgroundTint(resources.getColor(R.color.tools_theme)) }
            //.doOnNext { attach.setTint(it.textPrimary) }
            .doOnNext { messageAdapter.theme = it }
            .autoDispose(scope())
            .subscribe()

        window.callback = ComposeWindowCallback(window.callback, this)

        if (Build.VERSION.SDK_INT <= 22) {
            messageBackground.setBackgroundTint(resolveThemeColor(R.attr.messageBackground))
        }
        ThemeChangeTask().execute()
        emoji.setOnClickListener {
            if (!emojiopen) {
                message.inputType = InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
                emojiopen = true
            } else {
                message.inputType =
                    InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

                emojiopen = false
            }
        }

        backcompose.setOnClickListener {
            onBackPressed()
        }
        delete_layout.setOnClickListener {
            askForDelete(R.id.deletemultiple)
        }

        message.setOnClickListener {
            attaching?.setVisible(false)
            viewModel.setstate()
            showKeyboard()
        }

        if (Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
            schedule.setImageResource(R.drawable.schedual)
        } else {
            schedule.setImageResource(R.drawable.schedual_pro)
        }

        viewModel.sendClicked.observe(this){
            Log.d("showInterAd","showInterAd")
//            Handler().postDelayed({
//                if (viewModel.sendClicked.value == true){
//                    if (!Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
//                        MaxAdManager.showInterAd(this, object : OnAdShowCallback {
//                            override fun onAdHidden(ishow: Boolean) {
//                            }
//
//                            override fun onAdfailed() {
//                            }
//
//                            override fun onAdDisplay() {
//                            }
//
//                        })
//                    }
//                }
//            },2000)
        }
    }






    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Loading ads once if not loaded
     * there will be max three tries if once ad loaded it will not be loaded again but if not code will ask
     */
    var canShowNativeAd = false
    var adsReloadTry = 0
    private fun isPurchased(): Boolean {
        val purchase = Preferences.getBoolean(this, Preferences.ADSREMOVED)
        Log.i("Compose Screen", "isPurchased: purchase :- $purchase")
        return purchase
    }



    private fun showNativeAd() {
        if (!isPurchased()) {

//                ad_view_container.visibility = View.VISIBLE

                MaxAdManager.createNativeAd(
                    this,
                    maxAdContainer,
                    nativeAdContainer,
                    textViewRectangleNative,
                    {
                        ad_view_container.visibility = View.GONE
                    },
                    {
                        ad_view_container.visibility = View.VISIBLE
                    })
            } else {
                ad_view_container.visibility = View.GONE
        }
    }


    private fun getExternalFilesDirEx(context: Context, type: String): File {
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()){
            val ef = context.getExternalFilesDir(type)
            if (ef != null && ef.isDirectory) {
                return ef
            }
        }
        return File(Environment.getExternalStorageDirectory(), type)
    }

    private var isTitleRefresh = true

    override fun onResume() {
        super.onResume()

        showNativeAd()

        MainActivity.comesresume = true
        processClick = true
        Clickconversation = true
        Clickconversationspam = true
        Clickconversationother = true
        try {
            if (isTitleRefresh && PhoneNumberUtils(this).isPossibleNumber(toolbarTitle.text.toString())) {
                isTitleRefresh = false
                val uri = Uri.withAppendedPath(
                    ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(toolbarTitle.text.trim().toString())
                )
                val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val cursor = contentResolver.query(uri, projection, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val name = cursor.getString(nameIndex)
                    logDebug("titlemessage" + nameIndex + ">>>>>>>" + name + ">>>>>>>>>" + toolbarTitle.text)
                    if (name != "") {
                        toolbarTitle.textSize = 18f
                        toolbarTitle.text = name.trim()

                    }
                    cursor.close()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }


    }


    @SuppressLint("StaticFieldLeak")
    inner class ThemeChangeTask : AsyncTask<Void, String, String>() {
        override fun doInBackground(vararg avoid: Void): String {
            return ""
        }

    }

    override fun onStart() {
        super.onStart()

        activityVisibleIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityVisibleIntent.onNext(false)
    }

    override fun render(state: ComposeState) {
        if (state.hasError) {
            AdvertiseHandler.getInstance(this).isAppStartUpAdsPause = true
            finish()
        }

        threadId.onNext(state.threadId)

        title = when {
            state.selectedMessages > 0 -> getString(
                R.string.compose_title_selected,
                state.selectedMessages
            )
            state.query.isNotEmpty() -> state.query
            else -> state.conversationtitle
        }

        toolbarSubtitle.setVisible(state.query.isNotEmpty())
        toolbarSubtitle.text = getString(
            R.string.compose_subtitle_results, state.searchSelectionPosition,
            state.searchResults
        )

        toolbarTitle.setVisible(!state.editingMode)


        chips.setVisible(state.editingMode)
        composeBar.setVisible(!state.loading)
        try {
            val currentAddress =
                conversationRepo.getConversation(state.threadId)!!.recipients.get(0)!!.address
            val isPossibleNumber = PhoneNumberUtils(this).isValidPhonecheck(currentAddress)
                    && currentAddress != "57575858" && currentAddress != "57575353"
            val isVisible = !state.loading && isPossibleNumber

            composeBar.setVisible(isVisible)
            ad_view_container.setVisible(!isVisible)

            try {


                if (conversationRepo.getConversation(state.threadId)!!.recipients.isNotEmpty()) {
                    if (!conversationRepo.getConversation(state.threadId)!!.recipients.get(0)!!.contact!!.photoUri.equals(
                            null
                        )
                    ) {
                        avatars.setVisible(avatars.recipients.size == 1 && !state.editingMode)
                        avatars.recipients =
                            conversationRepo.getConversation(state.threadId)!!.recipients
                    }
                }
            } catch (E: Exception) {
                Log.d("render", "render: " + E.message)

            }

            composeBarNotSupport.setVisible(!composeBar.isVisible && !state.editingMode)
        } catch (ex: Exception) {
            ex.printStackTrace()
            Log.d("render", "render: " + ex.message)
        }

        if (state.editingMode && chips.adapter == null) chips.adapter = chipsAdapter

        toolbar.menu.findItem(R.id.deletemultiple)?.isVisible = false
        toolbar.menu.findItem(R.id.info)?.isVisible =
            !state.editingMode && state.selectedMessages == 0 && state.query.isEmpty()
        toolbar.menu.findItem(R.id.add)?.isVisible = state.editingMode && composeBar.isVisible
        toolbar.menu.findItem(R.id.call)?.isVisible =
            !state.editingMode && state.selectedMessages == 0 && state.query.isEmpty() && composeBar.isVisible && !state.isgroup
        toolbar.menu.findItem(R.id.copy)?.isVisible =
            !state.editingMode && state.selectedMessages > 0
        toolbar.menu.findItem(R.id.details)?.isVisible =
            !state.editingMode && state.selectedMessages == 1 && composeBar.isVisible
        toolbar.menu.findItem(R.id.delete)?.isVisible =
            !state.editingMode && state.selectedMessages > 0

        toolbar.menu.findItem(R.id.forward)?.isVisible =
            !state.editingMode && state.selectedMessages == 1
        toolbar.menu.findItem(R.id.previous)?.isVisible =
            state.selectedMessages == 0 && state.query.isNotEmpty() && composeBar.isVisible
        toolbar.menu.findItem(R.id.next)?.isVisible =
            state.selectedMessages == 0 && state.query.isNotEmpty() && composeBar.isVisible
        toolbar.menu.findItem(R.id.clear)?.isVisible =
            state.selectedMessages == 0 && state.query.isNotEmpty() && composeBar.isVisible
        if (!state.hasstorage) {
            image_layout.setBackgroundResource(R.drawable.pic_bg)
        } else {
            if (state.attaching) {
                runOnUiThread()
                {
                    if (state.setupgif || state.setupsticker) {
                        val from: Boolean
                        if (state.setupgif) {
                            from = true
                        } else {
                            from = false
                            setupviewpager(from)
                        }

                        gallary_attach.setVisible(false)

                    } else {
                        sticker_main_layout.setVisible(false)
                        gallary_attach.setVisible(true)
                        val imageViewModel =
                            ViewModelProvider(this).get(ImageViewModel::class.java)
                        imageViewModel.getAllImages(this)
                        photos = recentImages(imageViewModel)

                        linearLayoutManager =
                            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        recyclerView.layoutManager = linearLayoutManager

                        adapter = RecyclerAdapter(photos, this)
                        recyclerView.adapter = adapter
                    }
                }
                image_layout.setBackgroundResource(0)
            }

        }



        chipsAdapter.data = state.selectedChips
        logDebug("mnbdsajkffV" + state.messages)
        loading.setVisible(state.loading)

        messageList.setVisible(state.sendAsGroup)
        messageAdapter.data = state.messages
        messageAdapter.highlight = state.searchSelectionId

        scheduledGroup.isVisible = state.scheduled != 0L
        scheduledTime.text = dateFormatter.getScheduledTimestamp(state.scheduled)

        attachments.setVisible(state.attachments.isNotEmpty())

        attachmentAdapter.data = state.attachments

        attach.animate().rotation(if (state.attaching) 90f else 0f).start()
        attaching.isVisible = state.attaching
        logDebug("attach" + state.attaching)
        if (state.attaching) {
            hideKeyboard()
        }
        counter.text = state.remaining
        counter.setVisible(counter.text.isNotBlank())

        sim.setVisible(state.subscription != null && composeBar.isVisible)
        sim.contentDescription = getString(R.string.compose_sim_cd, state.subscription?.displayName)
        simIndex.text = "${state.subscription?.simSlotIndex?.plus(1)}"

        send.isEnabled = state.canSend
        send.imageAlpha = if (state.canSend) 255 else 128
    }

    override fun clearSelection() {
        messageAdapter.clearSelection()
        if (multiselect) {
        }
    }

    override fun showDetails(message: Message) {

        val conversation: Conversation? = conversationRepo.getConversation(message.threadId)
        val formatter = MessageDetailsFormatter(this, dateFormatter)

        val address: String = formatter.format(message, conversation)

        AlertDialog.Builder(this)
            .setTitle(R.string.compose_details_title)
            .setMessage(address)
            .setCancelable(true)
            .show()
    }

    override fun requestNotes() {
        val intent = Intent(this, NotesListActivity::class.java)
        startActivityForResult(intent, notesRequestCode)
    }

    override fun requestDefaultSms() {
        navigator.showDefaultSmsDialog(this)
    }

    override fun requestStoragePermission(requestCode: Int) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            requestCode
        )
    }

    override fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS
            ), 0
        )
    }

    override fun requestDatePicker() {
        if (Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    TimePickerDialog(
                        this,
                        { _, hour, minute ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, day)
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            scheduleSelectedIntent.onNext(calendar.timeInMillis)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(this)
                    )
                        .show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        } else {
            navigator.showRemoveAds()
        }
    }

    override fun requestContact() {
        val intent = Intent(Intent.ACTION_PICK)
            .setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)

        startActivityForResult(Intent.createChooser(intent, null), AttachContactRequestCode)
    }

    override fun showContacts(sharing: Boolean, chips: List<Recipient>) {
        message.hideKeyboard()

        val serialized =
            HashMap(chips.associate { chip -> chip.address to chip.contact?.lookupKey })
        logDebug("getinfo" + sharing + ">>" + serialized.keys)
        val intent = Intent(this, ContactsActivity::class.java)
            .putExtra(ContactsActivity.SharingKey, sharing)
            .putExtra(ContactsActivity.ChipsKey, serialized)
        startActivityForResult(intent, SelectContactRequestCode)
    }

    override fun showContactsgroup(sharing: Boolean, chips: List<Recipient>) {
        message.hideKeyboard()
        val serialized =
            HashMap(chips.associate { chip -> chip.address to chip.contact?.lookupKey })
        logDebug("getinfo" + sharing + ">>" + serialized.keys)
        val intent = Intent(this, ContactsgroupActivity::class.java)
            .putExtra(ContactsActivity.SharingKey, sharing)
            .putExtra(ContactsActivity.ChipsKey, serialized)
        startActivityForResult(intent, SelectContactRequestCode)
    }

    override fun themeChanged() {
        messageList.scrapViews()
    }

    override fun showKeyboard() {

        message.postDelayed({
            message.showKeyboard()
        }, 200)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    lateinit var mFusedLocationClient: FusedLocationProviderClient

    @SuppressLint("SetTextI18n")
    fun attachingBackgroundHide(mLastLocation: Location) {
        logDebug("location" + "" + mLastLocation)
        if (message.text!!.isNotEmpty()) {
            message.append("\n")
        }
        message.setText("[Current Location]:\nhttps://www.google.com/maps/search/?api=1&query=" + mLastLocation.latitude.toString() + "," + mLastLocation.longitude.toString())
        message.setSelection(message.text!!.length)
        message.showKeyboard()


    }


    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1
        mLocationRequest.fastestInterval = 1
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        Looper.myLooper()?.let {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                it
            )
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (dialog != null && dialog.isShowing) {
                dialog.dismiss()
            }
            locationResult.lastLocation?.let { attachingBackgroundHide(it) }

        }
    }

    override fun requestLocation() {
        if (!isConnected()) {
            makeToast("Please check your internet connection !")
            return
        }
        if (isLocationEnabled()) {


            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                logDebug("location" + "" + location)
                if (location == null) {
                    dialog.setMessage("Location Fetching !")
                    try {
                        if (!isFinishing) {
                            dialog.show()
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                    requestNewLocationData()
                } else {
                    if (!isFinishing && dialog.isShowing) {
                        dialog.dismiss()
                    }

                    attachingBackgroundHide(location)
                }
            }
        } else {

            makeToast("Turn on location", Toast.LENGTH_LONG)
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(intent, 7)
        }
    }

    private fun isConnected(): Boolean {
        val connectivityManager =
            (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
        return connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnected
//       var result = false
//        CheckInternet().check(){
//                connected ->
//            if (connected){
//                result = true
//            }else{
//                result = false
//            }
//        }
//        return result
    }

    override fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 0
        )
    }


    override fun requestCamera() {
        try {


            cameraDestination =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    .let { timestamp ->
                        ContentValues().apply {
                            put(
                                MediaStore.Images.Media.TITLE,
                                timestamp
                            )
                        }
                    }
                    .let { cv ->
                        contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cv
                        )
                    }

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, cameraDestination)
            startActivityForResult(Intent.createChooser(intent, null), TakePhotoRequestCode)
        } catch (E: java.lang.Exception) {

        }
    }

    override fun requestGallery() {
        val intent = Intent(Intent.ACTION_PICK)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .putExtra(Intent.EXTRA_LOCAL_ONLY, false)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setType("image/*")
        startActivityForResult(Intent.createChooser(intent, null), AttachPhotoRequestCode)
    }

    override fun setDraft(draft: String) = message.setText(draft)

    override fun scrollToMessage(id: Long) {
        messageAdapter.data?.second
            ?.indexOfLast { message -> message.id == id }
            ?.takeIf { position -> position != -1 }
            ?.let(messageList::scrollToPosition)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.compose, menu)
        if (menu != null) {
            for (i in 0 until menu.size()) {
                menu.getItem(i).icon?.colorFilter = PorterDuffColorFilter(
                    resolveThemeColor(android.R.attr.textColorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
            }
            menu.findItem(R.id.call).icon?.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(this, R.color.tools_theme),
                PorterDuff.Mode.SRC_IN
            )

        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete) {
            askForDelete(item)
        } else {
            try {
                optionsItemIntent.onNext(item.itemId)
            } catch (e: java.lang.Exception) {

            }

        }
        return true
    }

    private fun askForDelete(item: MenuItem) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_delete_title)
        builder.setCancelable(false)
        builder.setMessage(resources.getString(R.string.dialog_delete_message))
        builder.setPositiveButton("Delete", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                optionsItemIntent.onNext(item.itemId)
                dialog!!.dismiss()
            }
        })
        builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                onBackPressed()
                dialog!!.dismiss()
            }
        })
        val alert = builder.create()
        try {
            alert.show()
        } catch (e: Exception) {

        }

    }

    private fun askForDelete(item: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_delete_title)
        builder.setCancelable(false)
        builder.setMessage(resources.getString(R.string.dialog_delete_message))
        builder.setPositiveButton("Delete", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                optionsItemIntent.onNext(item)
                dialog!!.dismiss()
                afterdelete()

            }
        })
        builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                onBackPressed()
                dialog!!.dismiss()

                // messageAdapter.selection_multiple(false)
            }
        })
        val alert = builder.create()
        alert.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 120 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            runOnUiThread()
            {
                gallary_attach.setVisible(true)
                image_layout.setBackgroundResource(0)
                val imageViewModel = ViewModelProvider(this).get(ImageViewModel::class.java)
                imageViewModel.getAllImages(this)
                photos = recentImages(imageViewModel)

                linearLayoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                recyclerView.layoutManager = linearLayoutManager
                try {


                    adapter = RecyclerAdapter(photos, this)
                    recyclerView.adapter = adapter
                } catch (e: Exception) {

                }
            }
        } else
            if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                image_layout.setBackgroundResource(0)
                wallpaperDirectory =
                    File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), ".sticker")
                if (!wallpaperDirectory.exists()) {
                    wallpaperDirectory.mkdirs()
                }
                try {
                    setupviewpager(false)
                } catch (e: Exception) {

                }

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == SelectContactRequestCode -> {
                if (data == null) {
                    // finish()
                }
                chipsSelectedIntent.onNext(data?.getSerializableExtra(ContactsActivity.ChipsKey)
                    ?.let { serializable -> serializable as? HashMap<String, String?> }
                    ?: hashMapOf())
            }
            requestCode == TakePhotoRequestCode && resultCode == Activity.RESULT_OK -> {
                cameraDestination?.let(attachmentSelectedIntent::onNext)
                if (!ismobiledataOn()) {
                    makeToast(R.string.pocket_data_on, Toast.LENGTH_LONG)
                }
            }
            requestCode == AttachPhotoRequestCode && resultCode == Activity.RESULT_OK -> {
                data?.clipData?.itemCount
                    ?.let { count -> 0 until count }
                    ?.mapNotNull { i -> data.clipData?.getItemAt(i)?.uri }
                    ?.forEach(attachmentSelectedIntent::onNext)
                    ?: data?.data?.let(attachmentSelectedIntent::onNext)
                if (!ismobiledataOn()) {
                    makeToast(R.string.pocket_data_on, Toast.LENGTH_LONG)
                }
            }
            requestCode == AttachContactRequestCode && resultCode == Activity.RESULT_OK -> {
//                data?.data?.let(contactSelectedIntent::onNext)
                try {


                    val cursor = contentResolver.query(data!!.data!!, null, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        val nameIndex =
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val name = cursor.getString(nameIndex)
                        val phoneIndex =
                            cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val phoneNo = cursor.getString(phoneIndex)
                        message.setText("[Name]: $name\n[Number]: $phoneNo")
                        message.setSelection(message.text!!.length)
                        cursor.close()
                    }
                } catch (E: Exception) {

                }
            }
            requestCode == notesRequestCode && resultCode == Activity.RESULT_OK -> {
                if (message.text!!.isNotEmpty()) {
                    message.append("\n" + data!!.getStringExtra("noteDetails"))
                } else {
                    message.setText(data!!.getStringExtra("noteDetails"))
                }
                message.setSelection(message.text!!.length)
                message.showKeyboard()
            }
            requestCode == 7 ->
                if (isLocationEnabled()) {

                    Dexter.withContext(this)
                        .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(object : MultiplePermissionsListener {
                            @SuppressLint("MissingPermission")
                            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                                // check if all permissions are granted
                                if (report.areAllPermissionsGranted()) {
                                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@ComposeActivity)
                                    mFusedLocationClient.lastLocation.addOnCompleteListener(this@ComposeActivity) { task ->
                                        val location: Location? = task.result
                                        logDebug("location" + "" + location)
                                        if (location == null) {
                                            dialog.setMessage("Location Fetching !")
                                            try {
                                                if (dialog != null && !isFinishing)
                                                    dialog.show()
                                            } catch (ex: Exception)
                                            {
                                                ex.printStackTrace()
                                            }
                                            requestNewLocationData()
                                        } else {
                                            dialog.dismiss()
                                            attachingBackgroundHide(location)
                                        }
                                    }
                                }
                                // check for permanent denial of any permission
                                if (!report.areAllPermissionsGranted()) {
                                    Toast.makeText(this@ComposeActivity,"Permissions Are Necessery!",Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: List<PermissionRequest?>?,
                                token: PermissionToken
                            )
                            {
                                Toast.makeText(this@ComposeActivity,"Permission Denied",Toast.LENGTH_SHORT).show()
                                token.continuePermissionRequest()
                            }


                        })
                        .onSameThread()
                        .check()


                }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(CameraDestinationKey, cameraDestination)
        super.onSaveInstanceState(outState)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        cameraDestination = savedInstanceState.getParcelable(CameraDestinationKey)
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onBackPressed() {
//        Constants.IS_FROM_ACTIVITY = true
        AdvertiseHandler.getInstance(this).isAppStartUpAdsPause = true
        backPressedIntent.onNext(Unit)
    }


    private fun recentImages(imageViewModel: ImageViewModel): ArrayList<Uri> {


        photos = imageViewModel.getImageList()
        // load images


        return photos
    }


    private fun setupviewpager(from: Boolean) {
        val stickerdata = resources.getStringArray(R.array.sticker)
        val tabLayout = findViewById<TabLayout>(R.id.tabsgif)
        val viewPager11 = findViewById<View>(R.id.viewpager_gif) as ViewPager
        val adapter = ViewPagerAdapter(supportFragmentManager)

        if (from) {
            for (j in 0 until stickerlist.size) {

                val TextView = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                    .inflate(R.layout.text_tab, null, false)
                tabLayout.getTabAt(j)?.customView = TextView

                var firstFragmet: Category_fragment =
                    Category_fragment.newInstance(stickerlist, j, this)
                adapter.addFragment(firstFragmet, stickerlist.get(j).getName())
            }

        } else {

            for (j in 0 until stickerlist.size) {
                val TextView = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                    .inflate(R.layout.text_tab, null, false)
                tabLayout.getTabAt(j)?.customView = TextView
                val name = stickerlist.get(j).getName()


                if (j < stickerlist.size) {
                    var firstFragmet: Category_fragment =
                        Category_fragment.newInstance(stickerlist, j, this)
                    adapter.addFragment(firstFragmet, stickerdata.get(j))
                }
            }


        }

        viewPager11.adapter = adapter
        tabLayout.setupWithViewPager(viewPager11)
        viewPager11.offscreenPageLimit = 1
        viewPager11.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(tabLayout)
        )
        tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {


            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                when (tab.position) {

                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })
        if (!isNetworkAvailable()) {
            no_internet.setVisible(true)
        } else {
            no_internet.setVisible(false)

        }

    }

    fun check(name: String): Boolean {
        var gpath: String = this.getExternalFilesDir(null)!!.absolutePath
        var spath = ".sticker"
        //        if (array.size!=0) {
        var fullpath = File(gpath + File.separator + spath + File.separator + name)
        Log.w("fullpath", "" + fullpath)
        return imageReaderNew(fullpath)
    }

    fun imageReaderNew(root: File): Boolean {
        val fileList: ArrayList<File> = ArrayList()
        val listAllFiles = root.listFiles()

        if (listAllFiles != null && listAllFiles.size > 0) {
            for (currentFile in listAllFiles) {

                fileList.add(currentFile.absoluteFile)

            }
        }
        return fileList.size > 0

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null

    }

    fun toast() {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.dialog_delete_title)
            builder.setCancelable(false)
            builder.setMessage(resources.getString(com.messaging.textrasms.manager.data.R.string.text_copy))
            val alert = builder.create()
            try {
                alert.show()
            } catch (E: Exception) {

            }


        } catch (e: Exception) {

        }
    }

    internal var mobileDataEnabled = false
    private fun ismobiledataOn(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            val cmClass = Class.forName(cm.javaClass.name)
            val method = cmClass.getDeclaredMethod("getMobileDataEnabled")
            method.isAccessible = true // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = method.invoke(cm) as Boolean
        } catch (e: Exception) {
            return false
            // Some problem accessible private API and do whatever error handling you want here
        }

        return return mobileDataEnabled

    }

}


