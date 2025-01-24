package com.messaging.textrasms.manager.feature.qkreply

import android.telephony.SmsMessage
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkViewModel
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.compat.SubscriptionManagerCompat
import com.messaging.textrasms.manager.extensions.asObservable
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.interactor.DeleteMessages
import com.messaging.textrasms.manager.interactor.MarkRead
import com.messaging.textrasms.manager.interactor.SendMessage
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import com.messaging.textrasms.manager.util.ActiveSubscriptionObservable
import com.messaging.textrasms.manager.util.tryOrNull
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class QkReplyViewModel @Inject constructor(
    @Named("threadId") private val threadId: Long,
    private val conversationRepo: ConversationRepository,
    private val deleteMessages: DeleteMessages,
    private val markRead: MarkRead,
    private val messageRepo: MessageRepository,
    private val navigator: Navigator,
    private val sendMessage: SendMessage,
    private val subscriptionManager: SubscriptionManagerCompat
) : QkViewModel<QkReplyView, QkReplyState>(QkReplyState(threadId = threadId)) {

    private val conversation by lazy {
        conversationRepo.getConversationAsync(threadId)
            .asObservable()
            .filter { it.isLoaded }
            .filter { it.isValid }
            .distinctUntilChanged()
    }

    private val messages: Subject<RealmResults<Message>> =
        BehaviorSubject.createDefault(messageRepo.getUnreadMessages(threadId))

    init {
        disposables += markRead
        disposables += sendMessage

        disposables += Observables
            .combineLatest(messages, conversation) { messages, conversation ->
                newState { copy(data = Pair(conversation, messages)) }
                messages
            }
            .switchMap { messages -> messages.asObservable() }
            .filter { it.isLoaded }
            .filter { it.isValid }
            .filter { it.isEmpty() }
            .subscribe { newState { copy(hasError = true) } }

        disposables += conversation
            .map { conversation -> conversation.getTitle() }
            .distinctUntilChanged()
            .subscribe { title -> newState { copy(title = title) } }

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

    override fun bindView(view: QkReplyView) {
        super.bindView(view)

        conversation
            .map { conversation -> conversation.draft }
            .distinctUntilChanged()
            .autoDispose(view.scope())
            .subscribe { draft -> view.setDraft(draft) }

        view.menuItemIntent
            .filter { id -> id == R.id.read }
            .autoDispose(view.scope())
            .subscribe {
                markRead.execute(listOf(threadId)) { newState { copy(hasError = true) } }
            }

        view.menuItemIntent
            .filter { id -> id == R.id.call }
            .withLatestFrom(conversation) { _, conversation -> conversation }
            .mapNotNull { conversation ->
                tryOrNull { conversation.recipients.first()?.address }


            }
            .doOnNext { address -> navigator.makePhoneCall(address) }
            .autoDispose(view.scope())
            .subscribe { newState { copy(hasError = true) } }

        view.menuItemIntent
            .filter { id -> id == R.id.expand }
            .map { messageRepo.getMessages(threadId) }
            .doOnNext(messages::onNext)
            .autoDispose(view.scope())
            .subscribe { newState { copy(expanded = true) } }

        view.menuItemIntent
            .filter { id -> id == R.id.collapse }
            .map { messageRepo.getUnreadMessages(threadId) }
            .doOnNext(messages::onNext)
            .autoDispose(view.scope())
            .subscribe { newState { copy(expanded = false) } }

        view.menuItemIntent
            .filter { id -> id == R.id.delete }
            .observeOn(Schedulers.io())
            .map { messageRepo.getUnreadMessages(threadId).map { it.id } }
            .map { messages -> DeleteMessages.Params(messages, threadId) }
            .autoDispose(view.scope())
            .subscribe { deleteMessages.execute(it) { newState { copy(hasError = true) } } }

        view.menuItemIntent
            .filter { id -> id == R.id.view }
            .doOnNext { navigator.showConversation(threadId) }
            .autoDispose(view.scope())
            .subscribe { newState { copy(hasError = true) } }

        view.textChangedIntent
            .map { text -> text.isNotBlank() }
            .autoDispose(view.scope())
            .subscribe { canSend -> newState { copy(canSend = canSend) } }

        view.textChangedIntent
            .observeOn(Schedulers.computation())
            .map { draft ->
                SmsMessage.calculateLength(draft, false)
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
            .subscribe({ remaining ->
                try {

                    newState { copy(remaining = remaining) }
                } catch (e: java.lang.Exception) {

                }
            }, { messages::onError })

        view.textChangedIntent
            .debounce(100, TimeUnit.MILLISECONDS)
            .map { draft -> draft.toString() }
            .observeOn(Schedulers.io())
            .autoDispose(view.scope())
            .subscribe { draft ->
                try {
                    conversationRepo.saveDraft(threadId, draft)
                } catch (e: Exception) {

                }
            }

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
            .subscribe()

        view.sendIntent
            .withLatestFrom(view.textChangedIntent) { _, body -> body }
            .map { body -> body.toString() }
            .withLatestFrom(state, conversation) { body, state, conversation ->
                try {
                    val subId = state.subscription?.subscriptionId ?: -1
                    if (conversation.recipients.isValid) {
                        val addresses = conversation.recipients.map { it.address }
                        sendMessage.execute(SendMessage.Params(subId, threadId, addresses, body))
                    }
                    view.setDraft("")
                } catch (e: Exception) {

                }
            }
            .doOnNext {
                markRead.execute(listOf(threadId)) { newState { copy(hasError = true) } }
            }
            .autoDispose(view.scope())
            .subscribe()
    }

}
