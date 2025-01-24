package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkDelivered @Inject constructor(private val messageRepo: MessageRepository) :
    Interactor<Long>() {

    override fun buildObservable(params: Long): Flowable<Unit> {
        return Flowable.just(Unit)
            .doOnNext { messageRepo.markDelivered(params) }
    }

}