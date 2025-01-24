package com.messaging.textrasms.manager.common.base

import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.Fragment
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.extensions.Optional
import com.messaging.textrasms.manager.extensions.asObservable
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import com.messaging.textrasms.manager.util.PhoneNumberUtils
import com.messaging.textrasms.manager.util.Preferences
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject

abstract class QkFragment_fragment : Fragment() {


    protected val menu: Subject<Menu> = BehaviorSubject.create()

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var conversationRepo: ConversationRepository

    @Inject
    lateinit var messageRepo: MessageRepository

    @Inject
    lateinit var phoneNumberUtils: PhoneNumberUtils

    @Inject
    lateinit var prefs: Preferences
    val threadId: Subject<Long> = BehaviorSubject.createDefault(0)

    /**
     * Switch the theme if the threadId changes
     * Set it based on the latest message in the conversation
     */
    val theme: Observable<Colors.Theme> = threadId
        .distinctUntilChanged()
        .switchMap { threadId ->
            val conversation = conversationRepo.getConversation(threadId)
            when {
                conversation == null -> Observable.just(Optional(null))

                conversation.recipients.size == 1 -> Observable.just(Optional(conversation.recipients.first()))

                else -> messageRepo.getLastIncomingMessage(conversation.id)
                    .asObservable()
                    .mapNotNull { messages -> messages.firstOrNull() }
                    .distinctUntilChanged { message -> message.address }
                    .mapNotNull { message ->
                        conversation.recipients.find { recipient ->
                            phoneNumberUtils.compare(recipient.address, message.address)
                        }
                    }
                    .map { recipient -> Optional(recipient) }
                    .startWith(Optional(conversation.recipients.firstOrNull()))
                    .distinctUntilChanged()
            }
        }
        .switchMap { colors.themeObservable(it.value) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().setTheme(R.style.AppBaseTheme)

    }

    open fun getColoredMenuItems(): List<Int> {
        return listOf()
    }

    /**
     * This can be overridden in case an activity does not want to use the default themes
     */
    open fun getActivityThemeRes(black: Boolean) = when {
        black -> R.style.AppTheme_Black
        else -> R.style.AppTheme
    }

}