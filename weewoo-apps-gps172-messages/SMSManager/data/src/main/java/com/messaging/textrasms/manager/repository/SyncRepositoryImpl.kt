package com.messaging.textrasms.manager.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.text.TextUtils
import android.util.Log
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.messaging.textrasms.manager.extensions.insertOrUpdate
import com.messaging.textrasms.manager.extensions.map
import com.messaging.textrasms.manager.manager.KeyManager
import com.messaging.textrasms.manager.mapper.*
import com.messaging.textrasms.manager.model.*
import com.messaging.textrasms.manager.receiver.MessageSyncService
import com.messaging.textrasms.manager.util.Constants
import com.messaging.textrasms.manager.util.PhoneNumberUtils
import com.messaging.textrasms.manager.util.tryOrNull
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.Realm
import io.realm.Sort
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val contentResolver: ContentResolver,
    private val conversationRepo: ConversationRepository,
    private val cursorToConversation: CursorToConversation,
    private val cursorToMessage: CursorToMessage,
    private val cursorToRecipient: CursorToRecipient,
    private val cursorToContact: CursorToContact,
    private val cursorToContactGroup: CursorToContactGroup,
    private val cursorToContactGroupMember: CursorToContactGroupMember,
    private val keys: KeyManager,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val rxPrefs: RxSharedPreferences

) : SyncRepository {


    private var threadid: Long = 0
    private var address: String = ""
    private var body: String = ""
    override val synccount: Subject<SyncRepository.Synccount> =
        BehaviorSubject.createDefault(SyncRepository.Synccount.Idle)


    override var personalcount: Int = 0
    override var othercount: Int = 0
    override var spamcount: Int = 0
    var syncfirst = true

    var isPossibleNumber: Boolean = true
    var status = 0

    var cursorcount = 0
    override val syncProgress: Subject<SyncRepository.SyncProgress> =
        BehaviorSubject.createDefault(SyncRepository.SyncProgress.Idle)

    override fun syncMessages(context: Context, fromsetting: Boolean) {
        syncfirst = true
        var max = 0
        var progress = 0
        var count = 0
        cursorcount = 0
        if (syncProgress.blockingFirst() is SyncRepository.SyncProgress.Running) return
        MessageSyncService.start(context)
        syncProgress.onNext(SyncRepository.SyncProgress.Running(0, 0, true))
        SpamWord = Constants.loadSpamWords(context, "spam_keywords.txt")
        TransactionalWord = Constants.loadTransactionWords(context, "vocab.txt")
        PromotionalWord = Constants.loadPromotionWords(context, "dictionary.txt")

        synccount.onNext(
            SyncRepository.Synccount.Runningcount(
                personalcount,
                othercount,
                spamcount,
                false
            )
        )

        val realm = Realm.getDefaultInstance()

        try {

            realm.beginTransaction()

            val persistedData = realm.copyFromRealm(
                realm.where(Conversation::class.java)
                    .beginGroup()
                    .equalTo("archived", true)
                    .or()
                    .equalTo("blocked", true)
                    .or()
                    .equalTo("pinned", true)
                    .or()
                    .equalTo("isspam", true)
                    .or()
                    .equalTo("ispossible", true)
                    .or()
                    .isNotEmpty("name")
                    .or()
                    .isNotNull("blockingClient")
                    .or()
                    .isNotEmpty("blockReason")
                    .endGroup()
                    .findAll()
            )
                .associateBy { conversation -> conversation.id }
                .toMutableMap()

            realm.delete(Contact::class.java)
            realm.delete(ContactGroup::class.java)
            realm.delete(Conversation::class.java)
            realm.delete(Message::class.java)
            realm.delete(MmsPart::class.java)
            realm.delete(Recipient::class.java)

            keys.reset()

            val messageCursor = cursorToMessage.getMessagesCursor()
            var conversationCursorinitial = cursorToConversation.getConversationsCursor()
            var conversationCursor = cursorToConversation.getConversationsCursor()
            val recipientCursor = cursorToRecipient.getRecipientCursor()

            progress = 0

            max = (messageCursor?.count ?: 0) + (recipientCursor?.count
                ?: 0) + (conversationCursor?.count ?: 0)
            messageCursor?.use {
                val messageColumns = CursorToMessage.MessageColumns(messageCursor)


                val messages = messageCursor.map { cursor ->
                    progress++
                    syncProgress.onNext(SyncRepository.SyncProgress.Running(max, 0, false))
                    val messagetoinsert = cursorToMessage.map(Pair(cursor, messageColumns))

                    messagetoinsert
                }


                realm.insertOrUpdate(messages)


            }
            tryOrNull {

                val messagetoinsert = realm.where(Message::class.java)
                    .sort("date", Sort.DESCENDING)
                    .distinct("threadId")
                    .findAll()
                    .forEach { message ->
                        conversationCursorinitial?.use {
                            if (cursorcount < 25) {
                                conversationCursorinitial =
                                    cursorToConversation.getConversationsCursor()

                                val conversations = conversationCursorinitial!!.map { cursor ->
                                    syncfirst = false

                                    cursorToConversation.map(cursor).apply {
                                        persistedData[id]?.let { persistedConversation ->
                                        }
                                    }
                                }
                                syncProgress.onNext(
                                    SyncRepository.SyncProgress.Running(
                                        max,
                                        progress,
                                        false
                                    )
                                )
                                val conversation = conversations.find { conversation ->

                                    conversation.id == message.threadId
                                }
                                conversation?.lastMessage = message

                                tryOrNull {
                                    val recipients = conversation!!.recipients
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

                                                tryOrNull {
                                                    conversation.recipients.add(this)
                                                }
                                            }
                                        }

                                    conversation.recipients.clear()
                                    conversation.recipients.addAll(recipients)
                                    realm.insertOrUpdate(conversation)
                                    realm.commitTransaction()
                                    realm.beginTransaction()
                                }

                                if (conversation != null) {
                                    cursorcount++
                                    Log.d("syncMessages", "syncMessages: " + cursorcount)
                                    realm.insertOrUpdate(conversation)
                                }
                                realm.commitTransaction()
                                realm.beginTransaction()
                            } else {

                            }
                        }

                    }
            }

            recipientCursor?.use {
                val contacts = realm.copyToRealmOrUpdate(getContacts())

                recipientCursor
                    .map { cursor ->
                        progress++
                        syncProgress.onNext(
                            SyncRepository.SyncProgress.Running(
                                max,
                                progress,
                                true
                            )
                        )
                        count++
                        cursorToRecipient.map(cursor).apply {
                            tryOrNull {
                                contact = contacts.firstOrNull { contact ->

                                    contact.numbers.any {

                                        logDebug("contact>>>" + address + ">>>>>>>>>>>" + it.address)
                                        phoneNumberUtils.compare(address, it.address)
                                    }


                                }
                                realm.insertOrUpdate(this)
                                realm.commitTransaction()
                                realm.beginTransaction()

                            }
                        }
                    }
            }

            val oldBlockedSenders = rxPrefs.getStringSet("pref_key_blocked_senders")
            oldBlockedSenders.get()
                .map { threadIdString -> threadIdString.toLong() }
                .filter { threadId -> !persistedData.contains(threadId) }
                .forEach { threadId ->
                    persistedData[threadId] = Conversation(id = threadId, blocked = true)
                }

            conversationCursor?.use {
                val conversations = conversationCursor.map { cursor ->
                    Log.d("syncMessages", "syncMessages: " + "newstart")
                    progress++
                    syncProgress.onNext(SyncRepository.SyncProgress.Running(max, progress, false))

                    cursorToConversation.map(cursor).apply {
                        persistedData[id]?.let { persistedConversation ->
                            tryOrNull {
                                archived = persistedConversation.archived
                                blocked = persistedConversation.blocked
                                pinned = persistedConversation.pinned
                                name = persistedConversation.name
                                blockingClient = persistedConversation.blockingClient
                                blockReason = persistedConversation.blockReason
                                ispossible = PhoneNumberUtils(context).isValidPhone(getaddress())
                                isspam = persistedConversation.isspam
                                isunknown = persistedConversation.isunknown
                                recipients = persistedConversation.recipients
                                isunknown = persistedConversation.isunknown
                                lastMessage = persistedConversation.lastMessage
                            }
                        }
                        logDebug("ccccccccccccccccccccgj" + this.lastMessage)
                    }
                }

                try {
                    val contacts = realm.copyFromRealm(realm.where(Contact::class.java).findAll())

                    val message = realm.where(Message::class.java)
                        .sort("date", Sort.DESCENDING)
                        .distinct("threadId")
                        .findAll()
                        .forEach { message ->


                            count++
                            syncProgress.onNext(
                                SyncRepository.SyncProgress.Running(
                                    max,
                                    progress,
                                    true
                                )
                            )

                            val conversation = conversations.find { conversation1 ->

                                conversation1.id == message.threadId

                            }

                            conversation?.lastMessage = message

                            tryOrNull {
                                if (phoneNumberUtils.isValidPhone(message!!.address)) {
                                    conversation?.ispossible = true

                                }
                            }
                            if (message!!.isMms()) {
                                conversation?.ispossible = true

                            }

                            tryOrNull {


                                try {

                                    if (message.isMms()) {
                                        conversation?.ispossible = true
                                    }


                                    if (ischeckPromotional(message.body)) {

                                        com.messaging.textrasms.manager.model.logDebug("sspamwords" + "spam")
                                        conversation!!.Promotion = true


                                    }
                                    if (ischeckTran(message.address, message.body)) {
                                        logDebug("value>>>>>>>>>>>>>>>>>>>" + true)
                                        conversation!!.transactional = true
                                    }

                                    if (ischeckspam(message.body)) {
                                        conversation!!.isspam = true
                                    }


                                } catch (e: Exception) {
                                    logDebug("exception" + e.message)

                                }

                                tryOrNull {
                                    val recipients = conversation!!.recipients
                                        .map { recipient -> recipient.id }
                                        .map { id -> cursorToRecipient.getRecipientCursor(id) }
                                        .mapNotNull { recipientCursor ->
                                            syncProgress.onNext(
                                                SyncRepository.SyncProgress.Syncing(
                                                    true
                                                )
                                            )
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
                                                tryOrNull {
                                                    contact = contacts.firstOrNull {


//                                                        conversation.isunknown = checkString(it.name)
                                                    Log.e("IsUnknown",">Name:"+it.name)

                                                        it.numbers.any {
                                                            tryOrNull {
                                                                if (phoneNumberUtils.compare(
                                                                        address,
                                                                        it.address
                                                                    )
                                                                ) {
                                                                    conversation.isunknown = false
                                                                    Log.e("IsUnknown","false")

                                                                } else {
//                                                                    Log.e("IsUnknown",">ADDDRESS:"+address+"  COMPAREADDRESS:>"+it.address)
                                                                    if (!message.isMe() && conversation.recipients.size < 2) {
                                                                        conversation.isunknown = true
                                                                        Log.e("IsUnknown","true")
                                                                    } else {
//                                                                        Log.e("IsUnknown",">10 else part")
                                                                    }
                                                                }
                                                            }
                                                            phoneNumberUtils.compare(
                                                                recipient.address,
                                                                it.address
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    conversation.recipients.clear()
                                    conversation.recipients.addAll(recipients)
                                    realm.insertOrUpdate(conversation)
                                    realm.commitTransaction()
                                    realm.beginTransaction()
                                }


                            }
                        }

                } catch (e: Exception) {
                    logDebug("exception" + e.message)

                }
            }
            synccount.onNext(SyncRepository.Synccount.Idle)

            realm.insert(SyncLog())
            realm.commitTransaction()
            realm.close()
            oldBlockedSenders.delete()
        } catch (e: Throwable) {
            logDebug("throw" + e.message)
            if (realm.isInTransaction) {
            }
            throw e
        }

        syncProgress.onNext(SyncRepository.SyncProgress.Idle)

    }

    fun checkString(s: String):Boolean {
        val containsNumberOnly = s.all { it.isDigit() }
        val containsAlpha = s.any { it.isLetter() }

        if (containsAlpha) {
            Log.e("ISUNKNOWN","The string contains an alphabet."+ s)
            return false
        } else if (containsNumberOnly) {
            Log.e("ISUNKNOWN","The string contains only numbers." + s)
            return true
        } else {
            Log.e("ISUNKNOWN","The string contains neither alphabets nor only numbers."+ s)

            return true
        }
    }


    internal var SpamWord = ArrayList<String>()
    internal var TransactionalWord = ArrayList<String>()
    internal var PromotionalWord = ArrayList<String>()
    override fun ischeckspam(SMS: String): Boolean {
        val words = splitWord(SMS)
        for (c in words.indices) {
            if (words[c].length < 3)
                continue

            for (i in 0..1) {

                Log.d(
                    "sspamwords",
                    "totalword: " + SpamWord.contains(words[c]) + ">>>" + words[c] + ">>>" + SpamWord.size
                )
                if (SpamWord.contains(words[c])) {
                    return true
                }
            }

        }


        return false
    }

    override fun ischeckTransactional(SMS: String): Boolean {
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

    override fun ischeckPromotional(SMS: String): Boolean {
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

    override fun syncConversation() {

    }

    internal var mDebugLog = true
    internal var mDebugTag = "datasync"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

    override fun syncMessage(uri: Uri): Message? {
        logDebug("checksyncing" + "msg sync")
        val type = when {
            uri.toString().contains("mms") -> "mms"
            uri.toString().contains("sms") -> "sms"
            else -> return null
        }

        val id = tryOrNull(false) { ContentUris.parseId(uri) } ?: return null

        val existingId = Realm.getDefaultInstance().use { realm ->
            realm.refresh()
            realm.where(Message::class.java)
                .equalTo("type", type)
                .equalTo("contentId", id)
                .findFirst()
                ?.id
        }

        val stableUri = when (type) {
            "mms" -> ContentUris.withAppendedId(Telephony.Mms.CONTENT_URI, id)
            else -> ContentUris.withAppendedId(Telephony.Sms.CONTENT_URI, id)
        }

        return contentResolver.query(stableUri, null, null, null, null)?.use { cursor ->

            if (!cursor.moveToFirst()) return null

            val columnsMap = CursorToMessage.MessageColumns(cursor)
            cursorToMessage.map(Pair(cursor, columnsMap)).apply {
                existingId?.let { this.id = it }

                conversationRepo.getOrCreateConversation(threadId)
                insertOrUpdate()
            }
        }
    }

    override fun syncContacts() {
        var contacts = getContacts()

        Realm.getDefaultInstance()?.use { realm ->
            val recipients = realm.where(Recipient::class.java).findAll()

            realm.executeTransaction {
                realm.delete(Contact::class.java)
                realm.delete(ContactGroup::class.java)

                contacts = realm.copyToRealmOrUpdate(contacts)
                realm.insertOrUpdate(getContactGroups(contacts))

                recipients.forEach { recipient ->
                    recipient.contact = contacts.find { contact ->
                        contact.numbers.any {
                            phoneNumberUtils.compare(
                                recipient.address,
                                it.address
                            )
                        }
                    }
                }

                realm.insertOrUpdate(recipients)
            }

        }
    }

    private fun getContacts(): List<Contact> {
        val defaultNumberIds = Realm.getDefaultInstance().use { realm ->
            realm.where(PhoneNumber::class.java)
                .equalTo("isDefault", true)
                .findAll()
                .map { number -> number.id }
        }

        return cursorToContact.getContactsCursor()
            ?.map { cursor -> cursorToContact.map(cursor) }
            ?.groupBy { contact -> contact.lookupKey }
            ?.map { contacts ->
                val uniqueNumbers = mutableListOf<PhoneNumber>()
                contacts.value
                    .flatMap { it.numbers }
                    .forEach { number ->
                        number.isDefault = defaultNumberIds.any { id -> id == number.id }
                        val duplicate = uniqueNumbers.find { other ->
                            phoneNumberUtils.compare(number.address, other.address)
                        }

                        if (duplicate == null) {
                            uniqueNumbers += number
                        } else if (!duplicate.isDefault && number.isDefault) {
                            duplicate.isDefault = true
                        }
                    }

                contacts.value.first().apply {
                    numbers.clear()
                    numbers.addAll(uniqueNumbers)
                }
            } ?: listOf()
    }

    private fun getContactGroups(contacts: List<Contact>): List<ContactGroup> {
        val groupMembers = cursorToContactGroupMember.getGroupMembersCursor()
            ?.map(cursorToContactGroupMember::map)
            .orEmpty()

        val groups = cursorToContactGroup.getContactGroupsCursor()
            ?.map(cursorToContactGroup::map)
            .orEmpty()

        groups.forEach { group ->
            group.contacts.addAll(groupMembers
                .filter { member -> member.groupId == group.id }
                .mapNotNull { member -> contacts.find { contact -> contact.lookupKey == member.lookupKey } })
        }

        return groups
    }

    fun splitWord(sms: String): Array<String> {
        Log.d("splitWord", "splitWord: $sms")
        val words = TextUtils.split(sms, "\\W+")
        val temp = HashSet(Arrays.asList(*words))

        return temp.toTypedArray<String>()
    }


    fun ischeckTran(Title: String, SMS: String): Boolean {


        return Constants.parsevalues(Title, SMS)
    }


}
