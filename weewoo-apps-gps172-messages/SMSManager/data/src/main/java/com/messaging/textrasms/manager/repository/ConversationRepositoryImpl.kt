package com.messaging.textrasms.manager.repository

import android.content.ContentUris
import android.content.Context
import android.provider.Telephony
import android.text.TextUtils
import android.util.Log
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.compat.TelephonyCompat
import com.messaging.textrasms.manager.extensions.anyOf
import com.messaging.textrasms.manager.extensions.asObservable
import com.messaging.textrasms.manager.extensions.map
import com.messaging.textrasms.manager.extensions.removeAccents
import com.messaging.textrasms.manager.filter.ConversationFilter
import com.messaging.textrasms.manager.mapper.CursorToConversation
import com.messaging.textrasms.manager.mapper.CursorToRecipient
import com.messaging.textrasms.manager.model.*
import com.messaging.textrasms.manager.util.Classifier
import com.messaging.textrasms.manager.util.Constants
import com.messaging.textrasms.manager.util.PhoneNumberUtils
import com.messaging.textrasms.manager.util.tryOrNull
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ConversationRepositoryImpl @Inject constructor(
    private val context: Context,
    private val blockingClient: BlockingClient,
    private val conversationFilter: ConversationFilter,
    private val cursorToConversation: CursorToConversation,
    private val cursorToRecipient: CursorToRecipient,
    private val phoneNumberUtils: PhoneNumberUtils
) : ConversationRepository {
    internal var TransactionalWord = ArrayList<String>()
    internal var PromotionalWord = ArrayList<String>()

    override fun sortingOfDatasortingOfDataYear(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            //.equalTo("lastMessage.year", year)
            .greaterThanOrEqualTo("lastMessage.year", year)
            .lessThanOrEqualTo("lastMessage.year", year).endGroup()
            .sort("lastMessage.date", t)
            .findAll()

    }

    override fun sortingOfDatasortingOfDataDate(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
//                .equalTo("lastMessage.year", year)
//                .equalTo("lastMessage.month", month)
            .greaterThanOrEqualTo("lastMessage.datecompare", toDate(Toselecteddate))
            .lessThanOrEqualTo("lastMessage.datecompare", toDate(fromselecteddate)).endGroup()
            .sort("lastMessage.date", t)
            .findAll()

    }

    override fun sortingOfDatasortingOfDataMonth(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        selecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .equalTo("lastMessage.year", year)
            .greaterThanOrEqualTo("lastMessage.month", month)
            .lessThanOrEqualTo("lastMessage.month", month).endGroup()
            .sort("lastMessage.date", t)
            .findAll()


    }

    override fun sortingOfDatasortingOfDataYearknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("ispossible", true)
            .equalTo("isunknown", false)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            //.equalTo("lastMessage.year", year)
            .greaterThanOrEqualTo("lastMessage.year", year)
            .lessThanOrEqualTo("lastMessage.year", year).endGroup()
            .sort("lastMessage.date", t)
            .findAll()

    }

    override fun sortingOfDatasortingOfDataDateknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("ispossible", true)
            .equalTo("isunknown", false)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .greaterThanOrEqualTo("lastMessage.datecompare", toDate(Toselecteddate))
            .lessThanOrEqualTo("lastMessage.datecompare", toDate(fromselecteddate)).endGroup()
            .sort("lastMessage.date", t)
            .findAll()

    }

    override fun sortingOfDatasortingOfDataMonthknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        selecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("ispossible", true)
            .equalTo("isunknown", false)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .equalTo("lastMessage.year", year)
            .greaterThanOrEqualTo("lastMessage.month", month)
            .lessThanOrEqualTo("lastMessage.month", month).endGroup()
            .sort("lastMessage.date", t)
            .findAll()


    }

    override fun sortingOfDatasortingOfDataYearunknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("isunknown", true)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            //.equalTo("lastMessage.year", year)
            .greaterThanOrEqualTo("lastMessage.year", year)
            .lessThanOrEqualTo("lastMessage.year", year).endGroup()
            .sort("lastMessage.date", t)
            .findAll()

    }

    override fun sortingOfDatasortingOfDataDateunknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("isunknown", true)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .greaterThanOrEqualTo("lastMessage.datecompare", toDate(Toselecteddate))
            .lessThanOrEqualTo("lastMessage.datecompare", toDate(fromselecteddate)).endGroup()
            .sort("lastMessage.date", t)
            .findAll()

    }

    override fun sortingOfDatasortingOfDataMonthunknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        selecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("isunknown", true)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .equalTo("lastMessage.year", year)
            .greaterThanOrEqualTo("lastMessage.month", month)
            .lessThanOrEqualTo("lastMessage.month", month).endGroup()
            .sort("lastMessage.date", t)
            .findAll()


    }


    override fun getConversationspam(
        isspam: Boolean,
        istransactional: Boolean,
        inpromotional: Boolean,
        archived: Boolean,
        ispossiblenum: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("isspam", isspam)
            .equalTo("ispossible", false)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .isNotNull("lastMessage")
            .or()
            .isNotEmpty("draft")
            .endGroup()
            .sort(
                arrayOf("pinned", "draft", "lastMessage.date"),
                arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
            )
            .findAll()


    }

    override fun getConversationtransactional(
        isspam: Boolean,
        istransactional: Boolean,
        inpromotional: Boolean,
        archived: Boolean,
        ispossiblenum: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("transactional", istransactional)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .isNotNull("lastMessage")
            .or()
            .isNotEmpty("draft")
            .endGroup()
            .sort(
                arrayOf("pinned", "draft", "lastMessage.date"),
                arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
            )
            .findAll()


    }

    override fun getConversationpromotions(
        isspam: Boolean,
        inpromotional: Boolean,
        archived: Boolean,
        ispossiblenum: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("transactional", false)
            .equalTo("Promotion", inpromotional)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .isNotNull("lastMessage")
            .or()
            .isNotEmpty("draft")
            .endGroup()
            .sort(
                arrayOf("pinned", "draft", "lastMessage.date"),
                arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
            )
            .findAll()
    }

    override fun getConversationsknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        selecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("ispossible", true)
            .equalTo("isunknown", false)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .isNotNull("lastMessage")
            .or()
            .isNotEmpty("draft")
            .endGroup()
            .sort(
                arrayOf("pinned", "draft", "lastMessage.date"),
                arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
            )
            .findAll()


    }

    override fun getConversationsUnknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        field: String,
        date: Long,
        year: Long,
        sortorder: String,
        t: Sort
    ): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("blocked", false)
            .equalTo("isunknown", true)
            .isNotEmpty("recipients")
            .beginGroup()
            .isNotNull("lastMessage")
            .or()
            .isNotEmpty("draft")
            .endGroup()
            .sort(
                arrayOf("pinned", "draft", "lastMessage.date"),
                arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
            )
            .findAll()


    }

    override fun getConversationspersonalselect(
        archived: Boolean,
        ispossiblenum: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        selecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {
        val realm = Realm.getDefaultInstance()

        return realm.where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .findAll()


    }


    override fun getConversationspersonal(
        archived: Boolean,
        ispossiblenum: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        selecteddate: String,
        fromselecteddate: String
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .isNotNull("lastMessage")
            .or()
            .isNotEmpty("draft")
            .endGroup()
            .sort(
                arrayOf("pinned", "draft", "lastMessage.date"),
                arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
            )
            .findAll()


    }


    fun toDate(dateString: String): Date? {
        var date: Date? = null
        Log.e("parse: ", dateString)
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        try {
            date = formatter.parse(dateString)
            Log.e("Print11 : ", date.toString())

        } catch (e1: ParseException) {
            e1.printStackTrace()
        }

        return date
    }

    override fun getConversationsother(
        archived: Boolean,
        ispossiblenum: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort
    ): RealmResults<Conversation> {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("isspam", false)
            .equalTo("ispossible", false)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .isNotNull("lastMessage")
            .or()
            .isNotEmpty("draft")
            .endGroup()
            .sort(
                arrayOf("pinned", "draft", "lastMessage.date"),
                arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
            )
            .findAll()


//
    }

    override fun getConversations(
        archived: Boolean,
        ispossiblenum: Boolean
    ): RealmResults<Conversation> {


        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .notEqualTo("id", 0L)
            .equalTo("archived", archived)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .beginGroup()
            .isNotNull("lastMessage")
            .or()
            .isNotEmpty("draft")
            .endGroup()
            .sort(
                arrayOf("pinned", "draft", "lastMessage.date"),
                arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
            )
            .findAll()


    }


    override fun getTopConversations(): List<Conversation> {
        return Realm.getDefaultInstance().use { realm ->
            realm.copyFromRealm(
                realm.where(Conversation::class.java)
                    .notEqualTo("id", 0L)
                    .isNotNull("lastMessage")
                    .beginGroup()
                    .equalTo("pinned", true)
                    .or()
                    .greaterThan(
                        "lastMessage.date",
                        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
                    )
                    .endGroup()
                    .equalTo("archived", false)
                    .equalTo("blocked", false)
                    .isNotEmpty("recipients")
                    .findAll()
            )
                .sortedWith(compareByDescending<Conversation> { conversation -> conversation.pinned }
                    .thenByDescending { conversation ->
                        realm.where(Message::class.java)
                            .equalTo("threadId", conversation.id)
                            .greaterThan(
                                "date",
                                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
                            )
                            .count()
                    })
        }
    }


    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }


    override fun getConversationsSnapshot(): List<Conversation> {
        return Realm.getDefaultInstance().use { realm ->
            try {
                realm.refresh()

            } catch (e: IllegalStateException) {

            }
            realm.copyFromRealm(
                realm.where(Conversation::class.java)
                    .notEqualTo("id", 0L)
                    .equalTo("archived", false)
                    .equalTo("blocked", false)
                    .isNotEmpty("recipients")
                    .beginGroup()
                    .isNotNull("lastMessage")
                    .or()
                    .isNotEmpty("draft")
                    .endGroup()
                    .sort(
                        arrayOf("pinned", "draft", "lastMessage.date"),
                        arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
                    )
                    .findAll()
            )
        }
    }


    override fun setConversationName(id: Long, name: String) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                realm.where(Conversation::class.java)
                    .equalTo("id", id)
                    .findFirst()
                    ?.name = name
            }
        }
    }

    override fun searchConversations(query: CharSequence): List<SearchResult> {
        val realm = Realm.getDefaultInstance()

        val normalizedQuery = query.removeAccents()
        val conversations = realm.copyFromRealm(
            realm
                .where(Conversation::class.java)
                .notEqualTo("id", 0L)
                .isNotNull("lastMessage")
                .equalTo("blocked", false)
                .isNotEmpty("recipients")
                .sort("pinned", Sort.DESCENDING, "lastMessage.date", Sort.DESCENDING)
                .findAll()
        )

        val messagesByConversation = realm.copyFromRealm(
            realm
                .where(Message::class.java)
                .beginGroup()
                .contains("body", normalizedQuery, Case.INSENSITIVE)
                .or()
                .contains("parts.text", normalizedQuery, Case.INSENSITIVE)
                .endGroup()
                .findAll()
        )
            .asSequence()
            .groupBy { message -> message.threadId }
            .filter { (threadId, _) -> conversations.firstOrNull { it.id == threadId } != null }
            .map { (threadId, messages) ->
                Pair(
                    conversations.first { it.id == threadId },
                    messages.size
                )
            }
            .map { (conversation, messages) ->
                SearchResult(
                    normalizedQuery,
                    conversation,
                    messages
                )
            }
            .sortedByDescending { result -> result.messages }
            .toList()

        realm.close()

        return conversations
            .filter { conversation -> conversationFilter.filter(conversation, normalizedQuery) }
            .map { conversation ->
                SearchResult(
                    normalizedQuery,
                    conversation,
                    0
                )
            } + messagesByConversation
    }

    override fun getBlockedConversations(): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .equalTo("blocked", true)
            .findAll()
    }

    override fun getBlockedConversationsfilter(id: Long): Boolean {
        return Realm.getDefaultInstance().use { realm ->

            realm.where(Conversation::class.java)
                .equalTo("id", id)
                .findAll()
                .asSequence()
                .find { conversation ->
                    conversation.blockfromfilter
                }!!.blockfromfilter

        }
    }

    override fun setConversationfilter(id: Long, name: Boolean) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                realm.where(Conversation::class.java)
                    .equalTo("id", id)
                    .findFirst()
                    ?.blockfromfilter = name
            }
        }
    }

    override fun getBlockedConversationsAsync(): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .equalTo("blocked", true)
            .findAllAsync()
    }

    override fun getConversationAsync(threadId: Long): Conversation {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .equalTo("id", threadId)
            .findFirstAsync()
    }

    override fun getConversation(threadId: Long): Conversation? {

        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .equalTo("id", threadId)
            .findFirst()
    }

    override fun getConversations(vararg threadIds: Long): RealmResults<Conversation> {
        return Realm.getDefaultInstance()
            .where(Conversation::class.java)
            .anyOf("id", threadIds)
            .findAll()
    }

    override fun getUnmanagedConversations(): Observable<List<Conversation>> {
        val realm = Realm.getDefaultInstance()
        return realm.where(Conversation::class.java)
            .sort("lastMessage.date", Sort.DESCENDING)
            .notEqualTo("id", 0L)
            .isNotNull("lastMessage")
            .equalTo("archived", false)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .limit(5)
            .findAllAsync()
            .asObservable()
            .filter { it.isLoaded }
            .filter { it.isValid }
            .map { realm.copyFromRealm(it) }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
    }

    override fun getRecentConversations(): RealmResults<Conversation> {
        val realm = Realm.getDefaultInstance()
        return realm.where(Conversation::class.java)
            .sort("lastMessage.date", Sort.DESCENDING)
            .notEqualTo("id", 0L)
            .isNotNull("lastMessage")
            .equalTo("archived", false)
            .equalTo("ispossible", true)
            .equalTo("blocked", false)
            .isNotEmpty("recipients")
            .limit(5)
            .findAllAsync()

    }

    override fun getRecipients(): RealmResults<Recipient> {
        val realm = Realm.getDefaultInstance()
        return realm.where(Recipient::class.java)
            .findAll()
    }

    override fun getUnmanagedRecipients(): Observable<List<Recipient>> {
        val realm = Realm.getDefaultInstance()
        return realm.where(Recipient::class.java)
            .isNotNull("contact")
            .findAllAsync()
            .asObservable()
            .filter { it.isLoaded && it.isValid }
            .map { realm.copyFromRealm(it) }
            .subscribeOn(AndroidSchedulers.mainThread())
    }

    override fun getRecipient(recipientId: Long): Recipient? {
        return Realm.getDefaultInstance()
            .where(Recipient::class.java)
            .equalTo("id", recipientId)
            .findFirst()
    }

    override fun getThreadId(recipient: String): Long? {
        return getThreadId(listOf(recipient))
    }

    override fun getThreadId(recipients: Collection<String>): Long? {
        return Realm.getDefaultInstance().use { realm ->
            try {
                realm.refresh()

            } catch (e: IllegalStateException) {

            }
            realm.where(Conversation::class.java)
                .findAll()
                .asSequence()
                .filter { conversation -> conversation.recipients.size == recipients.size }
                .find { conversation ->
                    conversation.recipients.map { it.address }.all { address ->
                        recipients.any { recipient -> phoneNumberUtils.compare(recipient, address) }
                    }
                }?.id
        }
    }

    override fun getOrCreateConversation(threadId: Long): Conversation? {
        return getConversation(threadId) ?: getConversationFromCp(threadId)
    }

    override fun getOrCreateConversation(address: String): Conversation? {
        return getOrCreateConversation(listOf(address))
    }

    override fun getOrCreateConversation(addresses: List<String>): Conversation? {
        if (addresses.isEmpty()) {
            return null
        }

        return (getThreadId(addresses)
            ?: tryOrNull { TelephonyCompat.getOrCreateThreadId(context, addresses.toSet()) })
            ?.takeIf { threadId -> threadId != 0L }

            ?.let { threadId ->
                getConversation(threadId)
                    ?.let(Realm.getDefaultInstance()::copyFromRealm)
                    ?: getConversationFromCp(threadId)
            }
    }

    override fun saveDraft(threadId: Long, draft: String) {
        Realm.getDefaultInstance().use { realm ->
            try {
                realm.refresh()

            } catch (e: IllegalStateException) {

            }

            val conversation = realm.where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()
            try {
                realm.executeTransaction {
                    conversation?.takeIf { it.isValid }?.draft = draft
                }
            } catch (e: Exception) {

            }

        }
    }

    override fun saveerrror(threadId: Long, draft: Boolean) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val conversation = realm.where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()
        }
    }

    override fun savesending(threadId: Long, sending: Boolean) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            realm.where(Conversation::class.java)
                .equalTo("id", threadId)
                .findFirst()
        }
    }

    override fun updateConversations(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            try {
                realm.refresh()

            } catch (e: IllegalStateException) {

            }

            threadIds.forEach { threadId ->
                val conversation = realm
                    .where(Conversation::class.java)
                    .equalTo("id", threadId)
                    .findFirst() ?: return

                val message = realm
                    .where(Message::class.java)
                    .equalTo("threadId", threadId)
                    .sort("date", Sort.DESCENDING)
                    .findFirst()

                realm.executeTransaction {
                    conversation.lastMessage = message
                }
            }
        }
    }

    override fun markArchived(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.archived = true }
            }
        }
    }


    override fun markUnarchived(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.archived = false }
            }
        }
    }

    override fun markPinned(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.pinned = true }
            }
        }
    }

    override fun markUnpinned(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { it.pinned = false }
            }
        }
    }

    override fun markBlocked(threadIds: List<Long>, blockingClient: Int, blockReason: String?) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds.toLongArray())
                .equalTo("blocked", false)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { conversation ->
                    conversation.blocked = true
                    conversation.blockingClient = blockingClient
                    conversation.blockReason = blockReason
                }
            }
        }
    }

    override fun markUnblocked(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { conversation ->
                    conversation.blocked = false
                    conversation.blockingClient = null
                    conversation.blockReason = null
                }
            }
        }
    }

    override fun deleteConversations(vararg threadIds: Long) {
        Realm.getDefaultInstance().use { realm ->
            val conversation =
                realm.where(Conversation::class.java).anyOf("id", threadIds).findAll()
            val messages = realm.where(Message::class.java).anyOf("threadId", threadIds).findAll()

            realm.executeTransaction {
                conversation.deleteAllFromRealm()
                messages.deleteAllFromRealm()
            }
        }

        threadIds.forEach { threadId ->
            val uri = ContentUris.withAppendedId(Telephony.Threads.CONTENT_URI, threadId)
            context.contentResolver.delete(uri, null, null)
        }
    }

    fun splitWord(sms: String): Array<String> {
        Log.d("splitWord", "splitWord: $sms")
        // Split words
        val words = TextUtils.split(sms, "\\W+")
        // unique
        val temp = HashSet(Arrays.asList(*words))

        return temp.toTypedArray<String>()
    }

    fun ischeckTransactional(SMS: String): Boolean {
        val words = splitWord(SMS)
        for (c in words.indices) {
            if (words[c].length < 3)
                continue

            for (i in 0..1) {

                Log.d(
                    "transactional",
                    "totalword: " + TransactionalWord.contains(words[c]) + ">>>" + words[c] + ">>>" + TransactionalWord.size
                )
                if (TransactionalWord.contains(words[c])) {
                    return true
                }

            }

        }


        return false
    }

    fun ischeckPromotional(SMS: String): Boolean {
        val words = splitWord(SMS)
        for (c in words.indices) {
            if (words[c].length < 3)
                continue

            for (i in 0..1) {

                Log.d(
                    "promotions",
                    "totalword: " + PromotionalWord.contains(words[c]) + ">>>" + words[c] + ">>>" + PromotionalWord.size
                )
                if (PromotionalWord.contains(words[c])) {
                    return true
                }
            }

        }


        return false
    }

    val snag = Classifier(context)

    private fun getConversationFromCp(threadId: Long): Conversation? {
        TransactionalWord = Constants.loadTransactionWords(context, "vocab.txt")
        PromotionalWord = Constants.loadPromotionWords(context, "dictionary.txt")
        return cursorToConversation.getConversationsCursor()
            ?.map(cursorToConversation::map)
            ?.firstOrNull { it.id == threadId }
            ?.let { conversation ->
                val realm = Realm.getDefaultInstance()
                val contacts = realm.copyFromRealm(realm.where(Contact::class.java).findAll())
                val lastMessage = realm.where(Message::class.java).equalTo("threadId", threadId)
                    .sort("date", Sort.DESCENDING).findFirst()?.let(realm::copyFromRealm)

                val recipients = conversation.recipients
                    .map { recipient -> recipient.id }
                    .map { id -> cursorToRecipient.getRecipientCursor(id) }
                    .mapNotNull { recipientCursor ->
                        recipientCursor?.use {
                            recipientCursor.map {
                                cursorToRecipient.map(
                                    recipientCursor
                                )
                            }
                        }
                    }
                    .flatten()
                    .map { recipient ->
                        recipient.apply {
                            contact = contacts.firstOrNull {
                                logDebug("addresscheck>if" + recipient.address)
                                if (phoneNumberUtils.isValidPhone(recipient.address)) {
                                    conversation.ispossible = true
                                } else if (lastMessage == null || lastMessage.equals("")) {

                                    if (snag.isSpam(recipient.address)) {
                                        logDebug("sspamwords" + "spam")
                                        conversation.isspam = true


                                    }
                                    val action = blockingClient.getAction(address).blockingGet()
                                    when (action) {
                                        is BlockingClient.Action.Block -> {
                                            conversation.blocked = true

                                        }
                                        else -> {}
                                    }
                                } else if (lastMessage != null && !lastMessage.equals("")) {
                                    if (!lastMessage.body.equals("")) {
                                        if (ischeckPromotional(lastMessage.body)) {
                                            logDebug("sspamwords" + "spam")
                                            conversation.Promotion = true


                                        }
                                        if (ischeckTransactional(lastMessage.body)) {
                                            conversation.transactional = true
                                        }
                                        if (snag.isSpam(lastMessage.body + " " + recipient.address)) {
                                            conversation.isspam = true

                                        }
                                    } else if (recipient.id > 1) {

                                    }
                                } else {
                                    conversation.ispossible = false
                                    conversation.isspam = false

                                }

//                                conversation.isunknown = checkString(it.name)

                                it.numbers.any {
                                    tryOrNull {
                                        Log.e("IsUnknown",">1"+address+"CurrentAddress>"+it.address)
                                        if (phoneNumberUtils.compare(address, it.address)) {
//                                            conversation.isunknown = false
                                            Log.e("IsUnknown",">4 false")
                                        } else {
                                            Log.e("IsUnknown",">2"+conversation.recipients.size+"recipient>"+conversation.recipients)
                                            if (!lastMessage!!.isMe() && conversation.recipients.size < 2) {
//                                                conversation.isunknown = true
                                                Log.e("IsUnknown",">3 true")

                                            } else {
                                                Log.e("IsUnknown",">9 else part")
                                            }
                                        }
                                    }
                                    phoneNumberUtils.compare(recipient.address, it.address)
                                }
                            }
                        }
                    }

                conversation.recipients.clear()
                conversation.recipients.addAll(recipients)
                conversation.lastMessage = lastMessage

                try {
                    realm.executeTransaction { it.insertOrUpdate(conversation) }
                } catch (e: Exception) {

                }

                realm.close()

                conversation
            }
    }

    fun checkString(s: String):Boolean {
        val containsNumberOnly = s.all { it.isDigit() }
        val containsAlpha = s.any { it.isLetter() }

        if (containsAlpha) {
            Log.e("ISUNKNOWN","The string contains an alphabet.")
            return false
        } else if (containsNumberOnly) {
            Log.e("ISUNKNOWN","The string contains only numbers.")
            return true
        } else {
            Log.e("ISUNKNOWN","The string contains neither alphabets nor only numbers.")

            return true
        }
    }


}