package com.messaging.textrasms.manager.feature.compose

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsMessage
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkViewModel
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.common.maxAdManager.OnAdShowCallback
import com.messaging.textrasms.manager.common.util.ClipboardUtils
import com.messaging.textrasms.manager.common.util.MessageDetailsFormatter
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.compat.SubscriptionManagerCompat
import com.messaging.textrasms.manager.compat.TelephonyCompat
import com.messaging.textrasms.manager.extensions.asObservable
import com.messaging.textrasms.manager.extensions.isImage
import com.messaging.textrasms.manager.extensions.isVideo
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.feature.compose.ComposeActivity.Companion.stickerlist
import com.messaging.textrasms.manager.interactor.*
import com.messaging.textrasms.manager.manager.ActiveConversationManager
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.model.*
import com.messaging.textrasms.manager.repository.ContactRepository
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import com.messaging.textrasms.manager.util.*
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.teamgravity.checkinternet.CheckInternet
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Named


class ComposeViewModel @Inject constructor(
    @Named("query") private val query: String,

    @Named("fromgroup") private val fromgroup: Boolean,
    @Named("addresses") private val addresses: List<String>,
    @Named("text") private val sharedText: String,
    @Named("attachments") private val sharedAttachments: Attachments,
    @Named("threadId") private val threadId: Long,
    private val contactRepo: ContactRepository,
    private val context: Context,
    private val activity: ComposeActivity,
    private val activeConversationManager: ActiveConversationManager,
    private val addScheduledMessage: AddScheduledMessage,
    private val cancelMessage: CancelDelayedMessage,
    private val conversationRepo: ConversationRepository,
    private val deleteMessages: DeleteMessages,
    private val markRead: MarkRead,
    private val messageDetailsFormatter: MessageDetailsFormatter,
    private val messageRepo: MessageRepository,
    private val navigator: Navigator,
    private val permissionManager: PermissionManager,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val prefs: Preferences,
    private val retrySending: RetrySending,
    private val sendMessage: SendMessage,
    private val subscriptionManager: SubscriptionManagerCompat
) : QkViewModel<ComposeView, ComposeState>(
    ComposeState(
        editingMode = threadId == 0L && addresses.isEmpty(),
        threadId = threadId, query = query, hasstorage = permissionManager.hasStorage()
    )
) {

    private val attachments: Subject<List<Attachment>> =
        BehaviorSubject.createDefault(sharedAttachments)
    private val chipsReducer: Subject<(List<Recipient>) -> List<Recipient>> =
        PublishSubject.create()
    private val conversation: Subject<Conversation> = BehaviorSubject.create()
    private val messages: Subject<List<Message>> = BehaviorSubject.create()
    private val selectedChips: Subject<List<Recipient>> = BehaviorSubject.createDefault(listOf())
    private val searchResults: Subject<List<Message>> = BehaviorSubject.create()
    private val searchSelection: Subject<Long> = BehaviorSubject.createDefault(-1)

    var sendClicked = MutableLiveData<Boolean>(false)
    private var shouldShowContacts = threadId == 0L && addresses.isEmpty() && !fromgroup
    private var shouldShowgroupContacts = threadId == 0L && addresses.isEmpty() && fromgroup

    companion object {

        var foronce: Boolean = false
    }

    init {
        val initialConversation = threadId.takeIf { it != 0L }
            ?.let(conversationRepo::getConversationAsync)
            ?.asObservable()
            ?: Observable.empty()
        logDebug("getinfo" + threadId + ">>>>")
        try {
            val selectedConversation = selectedChips
                .skipWhile { it.isEmpty() }
                .map { chips -> chips.map { it.address } }
                .distinctUntilChanged()
                .doOnNext { newState { copy(loading = true) } }
                .observeOn(Schedulers.io())
                .map { addresses ->
                    Pair(
                        conversationRepo.getOrCreateConversation(addresses)?.id
                            ?: 0, addresses
                    )

                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { newState { copy(loading = false) } }
                .switchMap { (threadId, addresses) ->
                    // If we already have this thread in realm, or we're able to obtain it from the
                    // system, just return that.
                    threadId.takeIf { it > 0 }?.let {
                        return@switchMap conversationRepo.getConversationAsync(threadId)
                            .asObservable()
                    }

                    // Otherwise, we'll monitor the conversations until our expected conversation is created
                    conversationRepo.getConversations().asObservable()
                        .filter { it.isLoaded }
                        .observeOn(Schedulers.io())
                        .map {

                            conversationRepo.getOrCreateConversation(addresses)?.id ?: 0
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .switchMap { actualThreadId ->

                            when (actualThreadId) {

                                0L -> Observable.just(Conversation(0))
                                else -> conversationRepo.getConversationAsync(actualThreadId)
                                    .asObservable()
                            }
                        }
                }

            disposables += selectedConversation
                .mergeWith(initialConversation)
                .filter { conversation -> conversation.isLoaded }
                .doOnNext { conversation ->
                    if (!conversation.isValid) {
                        newState { copy(hasError = true) }
                    }
                }
                .filter { conversation -> conversation.isValid }
                .subscribe(conversation::onNext, { error -> handleOnError(error) })

            if (addresses.isNotEmpty()) {
                selectedChips.onNext(addresses.map { address -> Recipient(address = address) })
                newState { copy(conversationtitle = addresses.get(0)) }
            }

            disposables += chipsReducer
                .scan(listOf<Recipient>()) { previousState, reducer -> reducer(previousState) }
                .doOnNext { chips -> newState { copy(selectedChips = chips) } }
                .skipUntil(state.filter { state -> state.editingMode })
                .takeUntil(state.filter { state -> !state.editingMode })
                .subscribe(selectedChips::onNext)
        } catch (e: Exception) {
            logDebug("datasyncerror" + e.message)
        }

        try {


            disposables += conversation
                .distinctUntilChanged { conversation -> conversation.takeIf { it.isValid }?.id }
                .observeOn(AndroidSchedulers.mainThread())
                .map { conversation ->
                    conversation.takeIf { it.isValid }?.id
                    val messages = messageRepo.getMessages(conversation.id)
                    newState {
                        copy(
                            threadId = conversation.id,
                            messages = Pair(conversation, messages)
                        )
                    }
                    messages
                }
                .switchMap { messages -> messages.asObservable() }
                .subscribe(messages::onNext)
        } catch (e: Exception) {
            Log.d("TAG", "errotrr:+ " + e.message)

        }
        try {


            disposables += conversation
                .distinctUntilChanged { conversation -> conversation.takeIf { it.isValid }?.id!! }
                .observeOn(AndroidSchedulers.mainThread())
                .map { conversation ->
                    tryOrNull {

                        val messages =
                            messageRepo.getMessages(conversation.takeIf { it.isValid && conversation.isManaged }?.id!!)
                        try {
                            Log.d(
                                "TAG",
                                ":second " + messages.size + ">>>>>>>>" + conversation.id
                            )
                            newState {
                                copy(
                                    threadId = conversation.takeIf { it.isValid }?.id!!,
                                    messages = Pair(conversation, messages)
                                )
                            }
                        } catch (e: java.lang.Exception) {
                            Log.d("TAG", ": " + e.message)
                        }
                        messages
                    }

                }
                .switchMap { messages ->
                    messages.asObservable()

                }
                .subscribe({ messages::onNext }, { throwable ->
                    logDebug("Throw")
                }, { messages::onError })

        } catch (e: java.lang.Exception) {
            Log.d("TAG", ": " + e.message)
            logDebug("datasyncerror" + e.message)
        }
        disposables += conversation
            .map { conversation -> conversation.getTitle() }
            .distinctUntilChanged()
            .subscribe { title ->
                if (title.equals("")) {
                    if (!addresses.isEmpty()) {
                        contactRepo.getContacts().forEach {
                            if (phoneNumberUtils.compare(
                                    addresses.get(0),
                                    it.numbers.get(0)!!.address
                                )
                            ) {
                                newState { copy(conversationtitle = it.name) }
                                return@subscribe
                            } else
                                newState { copy(conversationtitle = addresses.get(0)) }
                        }


                    } else {
                        newState { copy(conversationtitle = title) }
                    }
                } else {

                    newState { copy(conversationtitle = title) }
                }
            }


        disposables += conversation
            .distinctUntilChanged { conversation -> conversation.takeIf { it.isValid }?.id!! }
            .observeOn(AndroidSchedulers.mainThread())
            .map { conversation ->
                tryOrNull {
                    try {
                        if (conversation.recipients.isValid && conversation.recipients.size != 0) {
                            newState {
                                copy(isgroup = conversation.recipients.size >= 2)
                            }
                        }
                    } catch (e: Exception) {

                    }
                    // val convo = conversationRepo.getConversationAsync(threadId).asObservable()

                }
            }
            .subscribe({ conversation::onNext }, { throwable -> logDebug("Throw") })
        disposables += attachments
            .subscribe { attachments -> newState { copy(attachments = attachments) } }

        disposables += conversation
            .map { conversation -> conversation.id }
            .distinctUntilChanged()
            .withLatestFrom(state) { id, state ->
                Log.d("TAG", ": " + messageRepo.getMessages(id, state.query))
                messageRepo.getMessages(id, state.query)
            }
            .switchMap { messages -> messages.asObservable() }
            .takeUntil(state.map { it.query }.filter { it.isEmpty() })
            .filter { messages -> messages.isLoaded }
            .filter { messages -> messages.isValid }
            .subscribe(searchResults::onNext)

        disposables += Observables.combineLatest(
            searchSelection,
            searchResults
        ) { selected, messages ->
            if (selected == -1L) {
                messages.lastOrNull()?.let { message -> searchSelection.onNext(message.id) }
            } else {
                val position = messages.indexOfFirst { it.id == selected } + 1
                newState { copy(searchSelectionPosition = position, searchResults = messages.size) }
            }
        }.subscribe()

        val latestSubId = messages
            .map { messages -> messages.lastOrNull()?.subId ?: -1 }
            .distinctUntilChanged()

        val subscriptions = ActiveSubscriptionObservable(subscriptionManager)
        disposables += Observables.combineLatest(latestSubId, subscriptions) { subId, subs ->
            val sub = if (subs.size > 1) subs.firstOrNull { it.subscriptionId == subId }
                ?: subs[0] else null
            newState { copy(subscription = sub) }
        }.subscribe()
    }

    override fun bindView(view: ComposeView) {
        super.bindView(view)

        val sharing = sharedText.isNotEmpty() || sharedAttachments.isNotEmpty()
        if (shouldShowContacts) {
            shouldShowContacts = false
            view.showContacts(sharing, selectedChips.blockingFirst())
        } else if (shouldShowgroupContacts) {
            shouldShowgroupContacts = false
            view.showContactsgroup(sharing, selectedChips.blockingFirst())
        }
        view.isvaluestore
            .autoDispose(view.scope())

            .subscribe {

                newState {
                    when (stickerlist.size != 0) {
                        true -> copy(setupgif = true, setupsticker = false)
                        false -> {
                            if (isNetworkAvailable()) {
                                LoardUrl(activity)
                            }
                            copy(setupgif = true, setupsticker = false)
                        }
                    }

                }
            }

        view.isvaluestoresticker
            .autoDispose(view.scope())
            .subscribe {
                if (permissionManager.hasStorage()) {
                    newState {
                        when (stickerlist.size != 0) {
                            true -> copy(setupsticker = true, setupgif = false)
                            false -> {
                                if (isNetworkAvailable()) {
                                    LoardUrl(activity)
                                }
                                copy(setupsticker = true, setupgif = false)

                            }
                        }
                    }

                } else {
                    view.requestStoragePermission(100)
                }
            }
        view.chipsSelectedIntent
            .withLatestFrom(selectedChips) { hashmap, chips ->
                // If there's no contacts already selected, and the user cancelled the contact
                // selection, close the activity
                if (hashmap.isEmpty() && chips.isEmpty()) {
                    newState { copy(hasError = true) }
                }
                // Filter out any numbers that are already selected
                hashmap.filter { (address) ->
                    chips.none { recipient -> phoneNumberUtils.compare(address, recipient.address) }
                }
            }
            .filter { hashmap -> hashmap.isNotEmpty() }
            .map { hashmap ->
                hashmap.map { (address, lookupKey) ->
                    conversationRepo.getRecipients()
                        .asSequence()
                        .filter { recipient -> recipient.contact?.lookupKey == lookupKey }
                        .firstOrNull { recipient ->
                            phoneNumberUtils.compare(
                                recipient.address,
                                address
                            )
                        }
                        ?: Recipient(
                            address = address,
                            contact = lookupKey?.let(contactRepo::getUnmanagedContact)
                        )
                }
            }
            .autoDispose(view.scope())
            .subscribe { chips ->
                chipsReducer.onNext { list -> list + chips }
                view.showKeyboard()
            }
        view.reciepnt
            .withLatestFrom(selectedChips) { _, chips ->
                view.showContacts(sharing, chips)
            }
            .autoDispose(view.scope())
            .subscribe()
        // Set the contact suggestions list to visible when the add button is pressed
        view.optionsItemIntent
            .filter { it == R.id.add }
            .withLatestFrom(selectedChips) { _, chips ->
                view.showContacts(sharing, chips)
            }
            .autoDispose(view.scope())
            .subscribe()

        // Update the list of selected contacts when a new contact is selected or an existing one is deselected
        view.chipDeletedIntent
            .autoDispose(view.scope())
            .subscribe { contact ->
                chipsReducer.onNext { contacts ->
                    val result = contacts.filterNot { it == contact }
                    if (result.isEmpty()) {
                        view.showContacts(sharing, result)
                    }
                    result
                }
            }

        // When the menu is loaded, trigger a new state so that the menu options can be rendered correctly
        view.menuReadyIntent
            .autoDispose(view.scope())
            .subscribe { newState { copy() } }

        // Open the phone dialer if the call button is clicked
        view.optionsItemIntent
            .filter { it == R.id.call }
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .mapNotNull { conversation -> conversation.recipients.firstOrNull() }
            .map { recipient -> recipient.address }
            .autoDispose(view.scope())
            .subscribe(
                { address -> navigator.makePhoneCall(address) },
                { throwable -> logDebug("Throw") })

        // Open the conversation settings if info button is clicked
        view.optionsItemIntent
            .filter { it == R.id.info }
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .autoDispose(view.scope())
            .subscribe({ conversation ->
                try {
                    navigator.showConversationInfo(conversation.takeIf { it.isValid }!!.id)
                } catch (e: Exception) {

                }

            }, { throwable -> logDebug("Throw") })

        // Copy the message contents
        view.optionsItemIntent
            .filter { it == R.id.copy }
            .withLatestFrom(view.messagesSelectedIntent) { _, messageIds ->
                val messages = messageIds.mapNotNull(messageRepo::getMessage).sortedBy { it.date }
                val text = when (messages.size) {
                    1 -> messages.first().getText()
                    else -> messages.foldIndexed("") { index, acc, message ->
                        when {
                            index == 0 -> message.getText()
                            messages[index - 1].compareSender(message) -> "$acc\n${message.getText()}"
                            else -> "$acc\n\n${message.getText()}"
                        }
                    }
                }
                ClipboardUtils.copy(context, text)
                context.makeToast(com.messaging.textrasms.manager.data.R.string.text_copy)
            }
            .autoDispose(view.scope())
            .subscribe { view.clearSelection() }

        // Show the message details
        view.optionsItemIntent
            .filter { it == R.id.details }
            .withLatestFrom(view.messagesSelectedIntent) { _, messages -> messages }
            .mapNotNull { messages -> messages.firstOrNull().also { view.clearSelection() } }
            .mapNotNull(messageRepo::getMessage)
            .subscribe { view.showDetails(it) }

        // Delete the messages
        view.optionsItemIntent
            .filter { it == R.id.delete }
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .withLatestFrom(
                view.messagesSelectedIntent,
                conversation
            ) { _, messages, conversation ->
                deleteMessages.execute(DeleteMessages.Params(messages, conversation.id))
            }
            .autoDispose(view.scope())
            .subscribe({ view.clearSelection() }, { throwable -> logDebug("error") })

        // Forward the message
        view.optionsItemIntent
            .filter { it == R.id.forward }
            .withLatestFrom(view.messagesSelectedIntent) { _, messages ->
                messages?.firstOrNull()?.let { messageRepo.getMessage(it) }?.let { message ->
                    val images = message.parts.filter { it.isImage() }.mapNotNull { it.getUri() }
                    navigator.showCompose(message.getText(), images)
                }
            }
            .autoDispose(view.scope())
            .subscribe({ view.clearSelection() }, { throwable -> logDebug("Throw") })

        view.optionsItemIntent
            .filter { it == R.id.previous }
            .withLatestFrom(searchSelection, searchResults) { _, selection, messages ->
                val currentPosition = messages.indexOfFirst { it.id == selection }
                if (currentPosition <= 0L) messages.lastOrNull()?.id ?: -1
                else messages.getOrNull(currentPosition - 1)?.id ?: -1
            }
            .filter { id -> id != -1L }
            .autoDispose(view.scope())
            .subscribe(searchSelection)

        view.optionsItemIntent
            .filter { it == R.id.next }
            .withLatestFrom(searchSelection, searchResults) { _, selection, messages ->
                val currentPosition = messages.indexOfFirst { it.id == selection }
                if (currentPosition >= messages.size - 1) messages.firstOrNull()?.id ?: -1
                else messages.getOrNull(currentPosition + 1)?.id ?: -1
            }
            .filter { id -> id != -1L }
            .autoDispose(view.scope())
            .subscribe(searchSelection)

        view.optionsItemIntent
            .filter { it == R.id.clear }
            .autoDispose(view.scope())
            .subscribe { newState { copy(query = "", searchSelectionId = -1) } }

        view.sendAsGroupIntent
            .autoDispose(view.scope())
            .subscribe { newState { copy(sendAsGroup = !sendAsGroup) } }

        searchSelection
            .filter { id -> id != -1L }
            .doOnNext { id -> newState { copy(searchSelectionId = id) } }
            .autoDispose(view.scope())
            .subscribe(view::scrollToMessage)

        prefs.keyChanges
            .filter { key -> key.contains("theme") }
            .doOnNext { view.themeChanged() }
            .autoDispose(view.scope())
            .subscribe()

        view.messageClickIntent
            .mapNotNull(messageRepo::getMessage)
            .filter { message -> message.isFailedMessage() }
            .doOnNext { message -> retrySending.execute(message.id) }
            .autoDispose(view.scope())
            .subscribe()

        view.messagePartClickIntent
            .mapNotNull(messageRepo::getPart)
            .filter { part -> part.isImage() || part.isVideo() }
            .autoDispose(view.scope())
            .subscribe { part -> navigator.showMedia(part.id) }

        view.messagePartClickIntent
            .mapNotNull(messageRepo::getPart)
            .filter { part -> !part.isImage() && !part.isVideo() }
            .autoDispose(view.scope())
            .subscribe { part ->
                if (permissionManager.hasStorage()) {
                    tryOrNull {
                        messageRepo.savePart(part.id)?.let(navigator::viewFile)
                    }
                } else {
                    view.requestStoragePermission(0)
                }
            }

        view.messagesSelectedIntent
            .map { selection -> selection.size }
            .autoDispose(view.scope())
            .subscribe { messages ->
                newState {
                    copy(
                        selectedMessages = messages,
                        editingMode = false
                    )
                }
            }

        view.cancelSendingIntent
            .mapNotNull(messageRepo::getMessage)
            .doOnNext { message -> view.setDraft(message.getText()) }
            .autoDispose(view.scope())
            .subscribe { message -> cancelMessage.execute(message.id) }

        view.optionsItemIntent
            .filter { it == R.id.deletemultiple }
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .doOnNext {
                view.selctmultiple(true)
            }

            .withLatestFrom(
                view.messagesSelectedIntent,
                conversation
            ) { _, messages, conversation ->
                deleteMessages.execute(DeleteMessages.Params(messages, conversation.id))
            }
            .autoDispose(view.scope())
            .subscribe {
                view.selctmultiple(true)

            }

        Observables
            .combineLatest(
                view.activityVisibleIntent.distinctUntilChanged(),
                conversation.mapNotNull { conversation ->
                    conversation.takeIf { it.isValid }?.id


                }.distinctUntilChanged()
            )
            { visible, threadId ->
                when (visible) {
                    true -> {
                        activeConversationManager.setActiveConversation(threadId)
                        markRead.execute(listOf(threadId))

                    }

                    false -> activeConversationManager.setActiveConversation(null)
                }
            }

            .autoDispose(view.scope())
            .subscribe()

        view.activityVisibleIntent
            .filter { visible -> !visible }
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .mapNotNull { conversation -> conversation.takeIf { it.isValid }?.id }
            .observeOn(Schedulers.io())
            .withLatestFrom(view.textChangedIntent) { threadId, draft ->
                conversationRepo.saveDraft(threadId, draft.toString())
            }
            .autoDispose(view.scope())
            .subscribe()

        view.attachIntent
            .autoDispose(view.scope())
            .subscribe {

                newState {
                    copy(
                        attaching = !attaching,
                        hasstorage = permissionManager.hasStorage(),
                        setupsticker = false
                    )
                }
            }
        view.attachgalleryclick
            .autoDispose(view.scope())
            .subscribe {
                if (permissionManager.hasStorage()) {

                } else {
                    view.requestStoragePermission(120)
                }
            }



        view.attachstickerclick
            .autoDispose(view.scope())
            .subscribe {
                view.requestSticker()

            }
        view.attachgifclick
            .autoDispose(view.scope())
            .subscribe {
                view.requestgif()

            }

        view.notesIntent
            .autoDispose(view.scope())
            .subscribe {
                if (permissionManager.hasStorage()) {
                    newState { copy(attaching = false) }
                    view.requestNotes()
                } else {
                    view.requestStoragePermission(0)
                }
            }

        view.locationIntent
            .autoDispose(view.scope())
            .subscribe {
                if (permissionManager.hasLocation1() && permissionManager.hasLocation2()) {
                    newState { copy(attaching = false) }
                    view.requestLocation()
                } else {
                    view.requestLocationPermission()
                }
            }

        view.cameraIntent
            .autoDispose(view.scope())
            .subscribe {
                if (permissionManager.hasStorage()) {
                    newState { copy(attaching = false) }
                    view.requestCamera()
                } else {
                    view.requestStoragePermission(0)
                }
            }

        view.galleryIntent
            .doOnNext { newState { copy(attaching = false) } }
            .autoDispose(view.scope())
            .subscribe { view.requestGallery() }

        view.scheduleIntent
            .doOnNext { newState { copy(attaching = false) } }
            .autoDispose(view.scope())
            .subscribe { view.requestDatePicker() }

        Observable.merge(

            view.attachmentSelectedIntent.map { uri -> Attachment.Image(uri) },
            view.inputContentIntent.map { inputContent -> Attachment.Image(inputContent = inputContent) })
            .withLatestFrom(attachments) { attachment, attachments -> attachments + attachment }
            .doOnNext(attachments::onNext)
            .autoDispose(view.scope())
            .subscribe { newState { copy(attaching = false, fromrecent = false) } }

        view.scheduleSelectedIntent
            .filter { scheduled ->
                (scheduled > System.currentTimeMillis()).also { future ->
                    if (!future) context.makeToast(R.string.compose_scheduled_future)
                }
            }
            .autoDispose(view.scope())
            .subscribe { scheduled -> newState { copy(scheduled = scheduled) } }

        view.attachContactIntent
            .doOnNext { newState { copy(attaching = false) } }
            .autoDispose(view.scope())
            .subscribe { view.requestContact() }

        view.contactSelectedIntent
            .map { uri -> Attachment.Contact(getVCard(uri)!!) }
            .withLatestFrom(attachments) { attachment, attachments -> attachments + attachment }
            .subscribeOn(Schedulers.io())
            .autoDispose(view.scope())
            .subscribe(attachments::onNext) { error ->
                context.makeToast(R.string.compose_contact_error)
                Timber.w(error)
            }

        view.attachmentDeletedIntent
            .withLatestFrom(attachments) { bitmap, attachments -> attachments.filter { it !== bitmap } }
            .autoDispose(view.scope())
            .subscribe { attachments.onNext(it) }

        conversation
            .map { conversation -> conversation.draft }
            .distinctUntilChanged()
            .autoDispose(view.scope())
            .subscribe({ draft ->

                if (sharedText.isNotBlank()) {
                    view.setDraft(sharedText)
                } else {
                    view.setDraft(draft)
                }
            }, { throwable -> logDebug("Throw") })

        Observables
            .combineLatest(view.textChangedIntent, attachments) { text, attachments ->
                text.isNotBlank() || attachments.isNotEmpty()
            }
            .autoDispose(view.scope())
            .subscribe { canSend -> newState { copy(canSend = canSend) } }

        view.textChangedIntent
            .observeOn(Schedulers.computation())
            .mapNotNull { draft ->
                tryOrNull {
                    SmsMessage.calculateLength(
                        draft,
                        prefs.unicode.get()
                    )
                }
            }
            .map { array ->
                val messages = array[0]
                val remaining = array[2]

                when {
                    messages <= 1 && remaining > 10 -> ""
                    messages <= 1 && remaining <= 10 -> "$remaining"
                    else -> "$remaining / $messages"
                }
            }
            .distinctUntilChanged()
            .autoDispose(view.scope())
            .subscribe { remaining -> newState { copy(remaining = remaining) } }

        view.scheduleCancelIntent
            .autoDispose(view.scope())
            .subscribe { newState { copy(scheduled = 0) } }

        view.changeSimIntent
            .withLatestFrom(state) { _, state ->
                val subs = subscriptionManager.activeSubscriptionInfoList
                val subIndex =
                    subs.indexOfFirst { it.subscriptionId == state.subscription?.subscriptionId }
                val subscription = when {
                    subIndex == -1 -> null
                    subIndex < subs.size - 1 -> subs[subIndex + 1]
                    else -> subs[0]
                }
                newState { copy(subscription = subscription) }
            }
            .autoDispose(view.scope())
            .subscribe({ throwable -> logDebug("Throw") })

        view.sendIntent
            .filter { permissionManager.isDefaultSms().also { if (!it) view.requestDefaultSms() } }
            .filter { permissionManager.hasSendSms().also { if (!it) view.requestSmsPermission() } }
            .withLatestFrom(view.textChangedIntent) { _, body -> body }
            .map { body -> body.toString() }
            .withLatestFrom(
                state,
                attachments,
                conversation,
                selectedChips
            ) { body, state, attachments,
                conversation, chips ->
                val subId = state.subscription?.subscriptionId ?: -1
                try {
                    sendClicked.value = true

                    val addresses = when (conversation.recipients.isNotEmpty()) {
                        true -> conversation.recipients.map { it.address }
                        false -> chips.map { chip -> chip.address }
                    }
                    val delay = when (prefs.sendDelay.get()) {
                        Preferences.SEND_DELAY_SHORT -> 3000
                        Preferences.SEND_DELAY_MEDIUM -> 5000
                        Preferences.SEND_DELAY_LONG -> 10000
                        else -> 0
                    }

                    when {
                        state.scheduled != 0L -> {
                            newState { copy(scheduled = 0) }
                            val uris = attachments

                                .mapNotNull { it as? Attachment.Image }
                                .map { it.getUri() }
                                .map { it.toString() }


                            val params = AddScheduledMessage
                                .Params(
                                    state.scheduled,
                                    subId,
                                    addresses,
                                    state.sendAsGroup,
                                    body,
                                    uris
                                )
                            addScheduledMessage.execute(params)
                            context.makeToast(R.string.compose_scheduled_toast)
                        }

                        state.sendAsGroup -> {
                            sendMessage.execute(
                                SendMessage
                                    .Params(
                                        subId,
                                        conversation.id,
                                        addresses,
                                        body,
                                        attachments,
                                        delay
                                    )
                            )
                        }

                        conversation.recipients.size == 1 -> {
                            val address = conversation.recipients.map { it.address }
                            sendMessage.execute(
                                SendMessage.Params(
                                    subId,
                                    threadId,
                                    address,
                                    body,
                                    attachments,
                                    delay
                                )
                            )
                        }

                        addresses.size == 1 -> {
                            sendMessage.execute(
                                SendMessage
                                    .Params(subId, threadId, addresses, body, attachments, delay)
                            )

                        }

                        else -> {
                            addresses.forEach { addr ->
                                val threadId = tryOrNull(false) {
                                    TelephonyCompat.getOrCreateThreadId(context, addr)
                                } ?: 0
                                val address = listOf(
                                    conversationRepo
                                        .getConversation(threadId)?.recipients?.firstOrNull()?.address
                                        ?: addr
                                )
                                sendMessage.execute(
                                    SendMessage
                                        .Params(subId, threadId, address, body, attachments, delay)
                                )
                            }
                        }
                    }
                    try {
                        foronce = true
                        view.setDraft("")
                        this.attachments.onNext(ArrayList())

                        if (state.editingMode) {
                            newState {
                                copy(
                                    editingMode = false,
                                    sendAsGroup = true,
                                    hasError = !state.sendAsGroup
                                )
                            }
                        }
                    } catch (e: Exception) {

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            .autoDispose(view.scope())
            .subscribe({ throwable -> logDebug("Throw") })

        view.optionsItemIntent
            .filter { it == android.R.id.home }
            .map { Unit }
            .mergeWith(view.backPressedIntent)
            .withLatestFrom(state) { _, state ->
                when {
                    state.selectedMessages > 0 -> {
                        view.clearSelection()
                    }
                    else -> newState { copy(hasError = true) }
                }
            }
            .autoDispose(view.scope())
            .subscribe()

        view.emojiintent
            .withLatestFrom(state) { _, state ->
                view.hideKeyboard()
                val subs = subscriptionManager.activeSubscriptionInfoList
                val subIndex =
                    subs.indexOfFirst { it.subscriptionId == state.subscription?.subscriptionId }
                val subscription = when {
                    subIndex == -1 -> null
                    subIndex < subs.size - 1 -> subs[subIndex + 1]
                    else -> subs[0]
                }
                newState { copy(subscription = subscription) }
            }
            .autoDispose(view.scope())
            .subscribe()

    }

    private fun getVCard(contactData: Uri): String? {
        val lookupKey =
            context.contentResolver.query(contactData, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
            }

        val vCardUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey)
        return context.contentResolver.openAssetFileDescriptor(vCardUri, "r")
            ?.createInputStream()
            ?.readBytes()
            ?.let { bytes -> String(bytes) }
    }

    @Throws(IOException::class)
    fun getJsonFile(str: String?): String? {
        val inputStreamReader: InputStreamReader?
        inputStreamReader = try {
            InputStreamReader(context.assets.open(str!!), "UTF-8")
        } catch (unused: IOException) {
            null
        }
        val bufferedReader = BufferedReader(inputStreamReader)
        val sb = StringBuilder()
        while (true) {
            val readLine = bufferedReader.readLine()
            if (readLine != null) {
                sb.append(readLine)
            } else {
                bufferedReader.close()
                inputStreamReader!!.close()
                return sb.toString()
            }
        }
    }

    fun getHomeTemplateList() {
        stickerlist = arrayListOf<Sticker>()
        var str: String? = null
        try {
            str = getJsonFile("messages_stickers.json")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (str != null) {
            try {
                val jsonObject = JSONObject(str)
                val jsonArray = jsonObject.getJSONArray("info")
                for (i in 0 until jsonArray.length()) {
                    var jsonObjectmain = jsonArray.getJSONObject(i)
                    var stickerdata: Sticker = Sticker()
                    var stickerurlArrayList = ArrayList<Sticker.stickerurl>()
                    val jsonArraysticker = jsonObjectmain.getJSONArray("image")
                    for (j in 0 until jsonArraysticker.length()) {
                        Log.d("jsondata", "jsondata" + jsonArraysticker.getString(j))
                        var stickerurl: Sticker.stickerurl = Sticker().stickerurl()
                        stickerurl.setUrl(Uri.parse(jsonArraysticker.getString(j)))
                        stickerurl.name = jsonObjectmain.getString("name")
                        stickerurlArrayList.add(stickerurl)
                    }
                    stickerdata.setStickerurlArrayList(stickerurlArrayList)
                    stickerdata.setName(jsonObjectmain.getString("name"))
                    stickerlist.add(stickerdata)
                }
                Log.d("jsondata", "jsondata" + stickerlist.size)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }


    }


    fun LoardUrl(activity: ComposeActivity) {
        getHomeTemplateList()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null


    }

    fun setstate() {
        newState {
            copy(
                attaching = false,
                hasstorage = permissionManager.hasStorage(),
                setupsticker = false
            )
        }

    }

    private fun handleOnError(error: Throwable) {
        // Handle the error appropriately, such as logging or displaying an error message
        error.printStackTrace()
    }
}

