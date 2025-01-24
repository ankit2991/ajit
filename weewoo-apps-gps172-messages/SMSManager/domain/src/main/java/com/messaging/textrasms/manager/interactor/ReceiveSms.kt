package com.messaging.textrasms.manager.interactor

import android.content.Context
import android.telephony.SmsMessage
import android.util.Log
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.manager.MyNotificationManager
import com.messaging.textrasms.manager.manager.ShortcutManager
import com.messaging.textrasms.manager.model.AllowNumber
import com.messaging.textrasms.manager.model.FilterBlockedNumber
import com.messaging.textrasms.manager.model.logDebug
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.tryOrNull
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmResults
import timber.log.Timber
import java.util.function.Predicate
import javax.inject.Inject

class ReceiveSms @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val blockingClient: BlockingClient,
    private val prefs: Preferences,
    private val messageRepo: MessageRepository,

    private val myNotificationManager: MyNotificationManager,
    private val updateBadge: UpdateBadge,
    private val shortcutManager: ShortcutManager

) : Interactor<ReceiveSms.Params>() {

    class Params(val subId: Int, val messages: Array<SmsMessage>, val context: Context)


    override fun buildObservable(params: Params): Flowable<*> {
        getblocklist()
        getAllowlist()
        return Flowable.just(params)
            .filter { it.messages.isNotEmpty() }

            .mapNotNull {
                val messages = it.messages
                val address = messages[0].displayOriginatingAddress

                val time = messages[0].timestampMillis
                val body: String = messages
                    .mapNotNull { message -> message.displayMessageBody }
                    .reduce { body, new -> body + new }

                val message = messageRepo.insertReceivedSms(it.subId, address, body, time)
                if (containsName(list, address)) {
                    if (!containsNameallow(getAllowlist(), address)) {
                        conversationRepo.setConversationfilter(message.threadId, true)

                        messageRepo.markRead(message.threadId)
                        conversationRepo.markBlocked(
                            listOf(message.threadId),
                            prefs.blockingManager.get(),
                            "reason"
                        )
                        val address = messages[0].displayOriginatingAddress
                        blockingClient.block(listOf(address)).subscribe()
                    } else {
                        conversationRepo.setConversationfilter(message.threadId, false)
                        blockingClient.unblock(listOf(address)).subscribe()
                    }
                } else if (containsName(list, body)) {
                    if (!containsNameallow(listallow, body)) {
                        conversationRepo.setConversationfilter(message.threadId, true)
                        messageRepo.markRead(message.threadId)
                        conversationRepo.markBlocked(
                            listOf(message.threadId),
                            prefs.blockingManager.get(),
                            "reason"
                        )
                        blockingClient.block(listOf(address)).subscribe()
                    } else {
                        conversationRepo.setConversationfilter(message.threadId, false)
                        blockingClient.unblock(listOf(address)).subscribe()
                    }
                } else {
                    tryOrNull {
                        if (conversationRepo.getBlockedConversationsfilter(message.threadId)) {
                            conversationRepo.setConversationfilter(message.threadId, false)
                            conversationRepo.markUnblocked(message.threadId)
                            blockingClient.unblock(listOf(address)).subscribe()
                        } else {


                        }

                    }
                }
                val action = blockingClient.getAction(address).blockingGet()
                val shouldDrop = prefs.drop.get()
                Timber.v("block=$action, drop=$shouldDrop")
                if (action is BlockingClient.Action.Block && shouldDrop) {
                    return@mapNotNull null
                }
                when (action) {
                    is BlockingClient.Action.Block -> {
                        messageRepo.markRead(message.threadId)
                        conversationRepo.markBlocked(
                            listOf(message.threadId),
                            prefs.blockingManager.get(),
                            action.reason
                        )
                        blockingClient.block(listOf(address)).subscribe()
                    }
                    is BlockingClient.Action.Unblock -> conversationRepo.markUnblocked(message.threadId)
                    else -> Unit
                }
                message
            }
            .doOnNext { message ->
                logDebug("update" + ">>>>>>>>>>>>>>>>>" + "?>>>>>")
                conversationRepo.updateConversations(message.threadId)
            }
            .mapNotNull { message ->
                logDebug("update" + ">>>>>>>>>>>>>>>>>" + "?>>>>>111")
                conversationRepo.getOrCreateConversation(message.threadId)

            }
            .filter { conversation -> !conversation.blocked }

            .doOnNext { conversation ->
                if (conversation.archived) conversationRepo.markUnarchived(conversation.id)
            }
            .doOnNext {
                val messages = params.messages
                val address = messages[0].displayOriginatingAddress
                val action = blockingClient.getAction(address).blockingGet()
                when (action) {
                    is BlockingClient.Action.Block -> {
                        conversationRepo.markBlocked(
                            listOf(it.id),
                            prefs.blockingManager.get(),
                            action.reason
                        )
                        blockingClient.block(listOf(address)).subscribe()
                    }
                    else -> Unit
                }
            }

            .map { conversation -> conversation.id }
            .doOnNext { threadId -> myNotificationManager.update(threadId) }
            .doOnNext { shortcutManager.updateShortcuts() }
            .flatMap { updateBadge.buildObservable(Unit) }
    }

    private var list = ArrayList<FilterBlockedNumber>()
    var mRealm: Realm? = null
    fun getblocklist(): RealmResults<FilterBlockedNumber> {
        mRealm = Realm.getDefaultInstance()
        list = ArrayList<FilterBlockedNumber>()
        val results = mRealm!!.where(FilterBlockedNumber::class.java).findAll()
        list.addAll(mRealm!!.copyFromRealm(results))
        Log.d("getblockdata", "getblockdata: " + list.size)
        return results
    }

    fun containsName(list: List<FilterBlockedNumber>, name: String?): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return list.stream()
                .filter(Predicate<FilterBlockedNumber> { o: FilterBlockedNumber -> o.address == name })
                .findFirst().isPresent
        } else {
            list.forEach { event ->
                if (event.address.equals(name)) {
                    return true
                    //.................
                }
            }

        }
        return false
    }


    private var listallow = ArrayList<AllowNumber>()
    fun getAllowlist(): RealmResults<AllowNumber> {
        mRealm = Realm.getDefaultInstance()
        listallow = ArrayList<AllowNumber>()
        val results = mRealm!!.where(AllowNumber::class.java).findAll()
        listallow.addAll(mRealm!!.copyFromRealm(results))
        Log.d("getblockdata", "getblockdata: " + list.size)
        return results
    }

    fun containsNameallow(list: List<AllowNumber>, name: String?): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return list.stream()
                .filter(Predicate<AllowNumber> { o: AllowNumber -> o.address == name })
                .findFirst().isPresent
        } else {
            list.forEach { event ->
                if (event.address.equals(name)) {
                    return true

                }
            }

        }
        return false
    }
}
