package com.messaging.textrasms.manager.feature.groups

import android.view.inputmethod.EditorInfo
import com.messaging.textrasms.manager.common.base.QkViewModel
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.extensions.removeAccents
import com.messaging.textrasms.manager.feature.compose.editing.ComposeItem
import com.messaging.textrasms.manager.feature.compose.editing.PhoneNumberAction
import com.messaging.textrasms.manager.feature.contacts.ContactsContract
import com.messaging.textrasms.manager.feature.contacts.ContactsState
import com.messaging.textrasms.manager.filter.ContactFilter
import com.messaging.textrasms.manager.filter.ContactGroupFilter
import com.messaging.textrasms.manager.interactor.SetDefaultPhoneNumber
import com.messaging.textrasms.manager.model.*
import com.messaging.textrasms.manager.repository.ContactRepository
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.util.PhoneNumberUtils
import com.messaging.textrasms.manager.util.tryOrNull
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx2.awaitFirst
import javax.inject.Inject
import javax.inject.Named


class ContactsGroupViewModel @Inject constructor(
    sharing: Boolean,
    serializedChips: HashMap<String, String?>,
    private val contactFilter: ContactFilter,
    private val contactGroupFilter: ContactGroupFilter,
    private val contactsRepo: ContactRepository,
    private val conversationRepo: ConversationRepository,
    private val phoneNumberUtils: PhoneNumberUtils,
    @Named("threadId") private val threadId: Long,
    @Named("addresses") private val addresses: List<String>,
    @Named("text") private val sharedText: String,
    @Named("attachments") private val sharedAttachments: Attachments,
    private val contactRepo: ContactRepository,

    private val setDefaultPhoneNumber: SetDefaultPhoneNumber
) : QkViewModel<ContactsContract, ContactsState>(ContactsState()) {
    private val chipsReducer: Subject<(List<Recipient>) -> List<Recipient>> =
        PublishSubject.create()
    var selected: HashMap<String, String?>
    private val contactGroups: Observable<List<ContactGroup>> by lazy { contactsRepo.getUnmanagedContactGroups() }
    private val contacts: Observable<List<Contact>> by lazy { contactsRepo.getUnmanagedContacts() }
    private val recents: Observable<List<Conversation>> by lazy {
        if (sharing) conversationRepo.getUnmanagedConversations() else Observable.just(listOf())
    }
    private val starredContacts: Observable<List<Contact>> by lazy {
        contactsRepo.getUnmanagedContacts(
            true
        )
    }
    private val selectedChipsaddress: Subject<List<Recipient>> =
        BehaviorSubject.createDefault(listOf())
    private val selectedChips = Observable.just(serializedChips)
        .observeOn(Schedulers.io())
        .map { hashmap ->
            hashmap.map { (address, lookupKey) ->
                Recipient(
                    address = address,
                    contact = lookupKey?.let(contactsRepo::getUnmanagedContact)
                )
            }
        }
        .doOnNext { chips: List<Recipient> ->

            newState { copy(selectedChips = chips) }

        }


    private var shouldOpenKeyboard: Boolean = false

    init {

        selected = HashMap()



        disposables += chipsReducer

            .scan(listOf<Recipient>()) { previousState, reducer -> reducer(previousState) }

            .doOnNext { chips: List<Recipient> ->

                newState { copy(selectedChips = chips) }

            }

            .subscribe(selectedChipsaddress::onNext)

    }


    override fun bindView(view: ContactsContract) {
        super.bindView(view)
        val sharing = sharedText.isNotEmpty() || sharedAttachments.isNotEmpty()

        if (shouldOpenKeyboard) {
            view.openKeyboard()
            shouldOpenKeyboard = false
        }

        view.queryChangedIntent
            .autoDispose(view.scope())
            .subscribe { query -> newState { copy(query = query.toString()) } }

        view.queryClearedIntent
            .autoDispose(view.scope())
            .subscribe { view.clearQuery() }

        Observables
            .combineLatest(
                view.queryChangedIntent,
                recents,
                starredContacts,
                contactGroups,
                contacts,
                selectedChips
            ) { query, recents, starredContacts, contactGroups, contacts, selectedChips ->
                val composeItems = mutableListOf<ComposeItem>()
                if (query.isBlank()) {

                    composeItems += contacts
                        .filter { contact -> selectedChips.none { it.contact?.lookupKey == contact.lookupKey } }
                        .map(ComposeItem::Person)

                } else {
                    if (phoneNumberUtils.isPossibleNumber(query.toString())) {
                        val newAddress = phoneNumberUtils.formatNumber(query)
                        val newContact =
                            Contact(numbers = RealmList(PhoneNumber(address = newAddress)))
                        composeItems += ComposeItem.New(newContact)
                    }

                    val normalizedQuery = query.removeAccents()

                    composeItems += contacts
                        .asSequence()
                        .filter { contact -> selectedChips.none { it.contact?.lookupKey == contact.lookupKey } }
                        .filter { contact -> contactFilter.filter(contact, normalizedQuery) }
                        .map(ComposeItem::Person)
                }

                composeItems
            }
            .subscribeOn(Schedulers.computation())
            .autoDispose(view.scope())
            .subscribe { items -> newState { copy(composeItems = items) } }

        view.queryEditorActionIntent
            .filter { actionId -> actionId == EditorInfo.IME_ACTION_DONE }
            .withLatestFrom(state) { _, state -> state }
            .mapNotNull { state -> state.composeItems.firstOrNull() }
            .mergeWith(view.composeItemPressedIntent)
            .map { composeItem -> composeItem to false }
            .mergeWith(view.composeItemLongPressedIntent.map { composeItem -> composeItem to true })
            .observeOn(Schedulers.io())
            .autoDispose(view.scope())
            .subscribe({ (composeItem, force) ->
                view.finish(HashMap(composeItem.getContacts().associate { contact ->
                    if (contact.numbers.size == 1 || contact.getDefaultNumber() != null && !force) {
                        val address = contact.getDefaultNumber()?.address
                            ?: contact.numbers[0]!!.address
                        selected.put(address, contact.lookupKey)
                        logDebug("address" + address + ">>" + selected.keys)
                        address to contact.lookupKey
                    } else {
                        runBlocking {
                            newState { copy(selectedContact = contact) }
                            val action = view.phoneNumberActionIntent.awaitFirst()
                            newState { copy(selectedContact = null) }

                            val numberId = view.phoneNumberSelectedIntent.awaitFirst().value
                            logDebug("key" + numberId)
                            val number = contact.numbers.find { number -> number.id == numberId }

                            if (action == PhoneNumberAction.CANCEL || number == null) {
                                return@runBlocking null
                            }

                            if (action == PhoneNumberAction.ALWAYS) {
                                val params =
                                    SetDefaultPhoneNumber.Params(contact.lookupKey, number.id)
                                setDefaultPhoneNumber.execute(params)
                            }
                            selected.put(number.address, contact.lookupKey)
                            logDebug("address" + number.address)
                            number.address to contact.lookupKey
                        } ?: return@subscribe
                    }
                }), false)
            }, { throwable -> logDebug("Throw2") })
        view.chipDeletedIntent
            .autoDispose(view.scope())
            .subscribe { contact ->

                chipsReducer.onNext { contacts ->

                    val result = contacts.filterNot {
                        tryOrNull {
                            view.removecontact(contact.contact!!.lookupKey)
                            if (selected.containsValue(contact.contact!!.lookupKey)) {
                                val iterator = selected.values.iterator()
                                while (iterator.hasNext()) {
                                    if (iterator.next() == contact.contact!!.lookupKey) {
                                        iterator.remove()
                                    }
                                }

                            }
                        }

                        it == contact

                    }
                    if (result.isEmpty()) {
                        //view.showContacts(sharing, result)
                    }
                    result
                }
            }


        view.chipsSelectedIntent
            .withLatestFrom(selectedChipsaddress) { hashmap, chips ->

                if (hashmap.isEmpty() && chips.isEmpty()) {
                    newState { copy(hasError = true) }
                }

                hashmap.filter { (address) ->

                    chips.none { recipient ->
                        logDebug("number" + address + "???" + recipient.address)
                        phoneNumberUtils.compare(address, recipient.address)
                    }
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

            }
        view.groupIntent
            .withLatestFrom(state) { _, state -> state }
            .autoDispose(view.scope())
            .subscribe({
                view.finish(selected, true)

            }, { throwable -> logDebug("Throw1") })
    }

}
