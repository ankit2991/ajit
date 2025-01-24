package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.manager.MyNotificationManager
import com.messaging.textrasms.manager.repository.ConversationRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MultiselectConversations @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val myNotificationManager: MyNotificationManager,
    private val updateBadge: UpdateBadge
) : Interactor<List<Long>>() {

    override fun buildObservable(params: List<Long>): Flowable<*> {
        return Flowable.just(params.toLongArray())
            .doOnNext { threadIds -> conversationRepo.getConversationspersonalselect() }
            .doOnNext { threadIds -> threadIds.forEach(myNotificationManager::update) }
            .flatMap { updateBadge.buildObservable(Unit) }
    }

}