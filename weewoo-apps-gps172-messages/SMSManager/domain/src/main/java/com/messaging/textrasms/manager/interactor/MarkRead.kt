package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.manager.MyNotificationManager
import com.messaging.textrasms.manager.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkRead @Inject constructor(
    private val messageRepo: MessageRepository,
    private val myNotificationManager: MyNotificationManager,
    private val updateBadge: UpdateBadge
) : Interactor<List<Long>>() {

    override fun buildObservable(params: List<Long>): Flowable<*> {
        return Flowable.just(params.toLongArray())
            .doOnNext { threadIds -> messageRepo.markRead(*threadIds) }
            .doOnNext { threadIds -> threadIds.forEach(myNotificationManager::update) }
            .flatMap { updateBadge.buildObservable(Unit) }
    }

}