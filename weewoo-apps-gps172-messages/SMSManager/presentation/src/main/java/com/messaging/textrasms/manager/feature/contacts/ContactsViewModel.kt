package com.messaging.textrasms.manager.feature.contacts

import android.view.inputmethod.EditorInfo
import com.messaging.textrasms.manager.common.base.QkViewModel
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.extensions.removeAccents
import com.messaging.textrasms.manager.feature.compose.editing.ComposeItem
import com.messaging.textrasms.manager.feature.compose.editing.PhoneNumberAction
import com.messaging.textrasms.manager.filter.ContactFilter
import com.messaging.textrasms.manager.interactor.SetDefaultPhoneNumber
import com.messaging.textrasms.manager.model.*
import com.messaging.textrasms.manager.repository.ContactRepository
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.util.PhoneNumberUtils
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.RealmList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx2.awaitFirst
import javax.inject.Inject

class ContactsViewModel @Inject constructor(
    sharing: Boolean,
    serializedChips: HashMap<String, String?>,
    private val contactFilter: ContactFilter,
    private val contactsRepo: ContactRepository,
    private val conversationRepo: ConversationRepository,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val navigator: Navigator,
    private val setDefaultPhoneNumber: SetDefaultPhoneNumber
) : QkViewModel<ContactsContract, ContactsState>(ContactsState()) {

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
    private val conversation: Subject<Conversation> = BehaviorSubject.create()
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

    private var shouldOpenKeyboard: Boolean = false


    override fun bindView(view: ContactsContract) {
        super.bindView(view)

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
                        address to contact.lookupKey
                    } else {
                        runBlocking {
                            newState { copy(selectedContact = contact) }
                            val action = view.phoneNumberActionIntent.awaitFirst()
                            newState { copy(selectedContact = null) }
                            val numberId = view.phoneNumberSelectedIntent.awaitFirst().value
                            val number = contact.numbers.find { number -> number.id == numberId }

                            if (action == PhoneNumberAction.CANCEL || number == null) {
                                return@runBlocking null
                            }

                            if (action == PhoneNumberAction.ALWAYS) {
                                val params =
                                    SetDefaultPhoneNumber.Params(contact.lookupKey, number.id)
                                setDefaultPhoneNumber.execute(params)
                            }

                            number.address to contact.lookupKey
                        } ?: return@subscribe
                    }
                }), true)
            }, { throwable -> logDebug("Throw") })
        view.groupselection
            .autoDispose(view.scope())
            .subscribe({
                navigator.showgroupContacts()

            }, { throwable -> logDebug("Throw1") })

        view.createcontact
            .autoDispose(view.scope())
            .subscribe({
                view.createcontact()

            }, { throwable -> logDebug("Throw1") })

    }


}
