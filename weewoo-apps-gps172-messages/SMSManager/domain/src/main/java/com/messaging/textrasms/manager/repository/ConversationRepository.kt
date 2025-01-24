package com.messaging.textrasms.manager.repository

import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.model.Recipient
import com.messaging.textrasms.manager.model.SearchResult
import io.reactivex.Observable
import io.realm.RealmResults
import io.realm.Sort

interface ConversationRepository {

    fun getConversations(
        archived: Boolean = false,
        ispossiblenum: Boolean = false
    ): RealmResults<Conversation>

    fun getConversationspersonalselect(
        archived: Boolean = false,
        ispossiblenum: Boolean = false,
        field: String = "lastMessage.date",
        date: Long = 1L,
        year: Long = 2020L,
        sortorder: String = "lastMessage.date",
        t: Sort = Sort.DESCENDING,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun getConversationspersonal(
        archived: Boolean = false,
        ispossiblenum: Boolean = false,
        field: String = "lastMessage.date",
        date: Long = 1L,
        year: Long = 2020L,
        sortorder: String = "lastMessage.date",
        t: Sort = Sort.DESCENDING,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"

    ): RealmResults<Conversation>

    //
    fun getConversationsother(
        archived: Boolean = false,
        ispossiblenum: Boolean = false,
        field: String = "lastMessage.date",
        date: Long = 1L,
        year: Long = 2020L,
        sortorder: String = "lastMessage.date",
        t: Sort = Sort.DESCENDING
    ): RealmResults<Conversation>

    fun getConversationsUnknown(
        archived: Boolean = false,
        ispossiblenum: Boolean = false,
        field: String = "lastMessage.date",
        date: Long = 1L,
        year: Long = 2020L,
        sortorder: String = "lastMessage.date",
        t: Sort = Sort.DESCENDING
    ): RealmResults<Conversation>


    fun getConversationsknown(
        archived: Boolean = false,
        ispossiblenum: Boolean = false,
        field: String = "lastMessage.date",
        date: Long = 1L,
        year: Long = 2020L,
        sortorder: String = "lastMessage.date",
        t: Sort = Sort.DESCENDING,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun getConversationspam(
        isspam: Boolean = true,
        istransactional: Boolean = false,
        inpromotional: Boolean = false,
        archived: Boolean = false,
        ispossiblenum: Boolean = false,
        field: String = "lastMessage.date",
        date: Long = 1L,
        year: Long = 2020L,
        sortorder: String = "lastMessage.date",
        t: Sort = Sort.DESCENDING
    ): RealmResults<Conversation>

    fun getConversationtransactional(
        isspam: Boolean = true,
        istransactional: Boolean = false,
        inpromotional: Boolean = false,
        archived: Boolean = false,
        ispossiblenum: Boolean = false,
        field: String = "lastMessage.date",
        date: Long = 1L,
        year: Long = 2020L,
        sortorder: String = "lastMessage.date",
        t: Sort = Sort.DESCENDING
    ): RealmResults<Conversation>

    fun getConversationpromotions(
        isspam: Boolean = true,
        inpromotional: Boolean = false,
        archived: Boolean = false,
        ispossiblenum: Boolean = false,
        field: String = "lastMessage.date",
        date: Long = 1L,
        year: Long = 2020L,
        sortorder: String = "lastMessage.date",
        t: Sort = Sort.DESCENDING
    ): RealmResults<Conversation>

    fun getConversationsSnapshot(): List<Conversation>

    fun sortingOfDatasortingOfDataMonth(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun sortingOfDatasortingOfDataYear(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun sortingOfDatasortingOfDataDate(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun sortingOfDatasortingOfDataMonthknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun sortingOfDatasortingOfDataYearknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun sortingOfDatasortingOfDataDateknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun sortingOfDatasortingOfDataMonthunknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun sortingOfDatasortingOfDataYearunknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun sortingOfDatasortingOfDataDateunknown(
        archived: Boolean,
        ispossiblenum: Boolean,
        spam: Boolean,
        field: String,
        month: Long,
        year: Long,
        sortorder: String,
        t: Sort,
        Toselecteddate: String = "2020-08-08",
        fromselecteddate: String = "2020-08-17"
    ): RealmResults<Conversation>

    fun getTopConversations(): List<Conversation>

    fun setConversationName(id: Long, name: String)

    fun searchConversations(query: CharSequence): List<SearchResult>

    fun getBlockedConversations(): RealmResults<Conversation>
    fun getBlockedConversationsfilter(id: Long): Boolean
    fun setConversationfilter(id: Long, name: Boolean)

    fun getBlockedConversationsAsync(): RealmResults<Conversation>

    fun getConversationAsync(threadId: Long): Conversation

    fun getConversation(threadId: Long): Conversation?

    fun getConversations(vararg threadIds: Long): RealmResults<Conversation>

    fun getUnmanagedConversations(): Observable<List<Conversation>>

    fun getRecentConversations(): RealmResults<Conversation>

    fun getRecipients(): RealmResults<Recipient>

    fun getUnmanagedRecipients(): Observable<List<Recipient>>

    fun getRecipient(recipientId: Long): Recipient?

    fun getThreadId(recipient: String): Long?

    fun getThreadId(recipients: Collection<String>): Long?

    fun getOrCreateConversation(threadId: Long): Conversation?

    fun getOrCreateConversation(address: String): Conversation?

    fun getOrCreateConversation(addresses: List<String>): Conversation?
    fun saveDraft(threadId: Long, draft: String)


    fun saveerrror(threadId: Long, draft: Boolean)


    fun savesending(threadId: Long, draft: Boolean)

    fun updateConversations(vararg threadIds: Long)

    fun markArchived(vararg threadIds: Long)

    fun markUnarchived(vararg threadIds: Long)

    fun markPinned(vararg threadIds: Long)

    fun markUnpinned(vararg threadIds: Long)

    fun markBlocked(threadIds: List<Long>, blockingClient: Int, blockReason: String?)

    fun markUnblocked(vararg threadIds: Long)

    fun deleteConversations(vararg threadIds: Long)


}