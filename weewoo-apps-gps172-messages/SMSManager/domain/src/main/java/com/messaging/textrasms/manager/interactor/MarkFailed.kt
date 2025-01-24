package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.manager.MyNotificationManager
import com.messaging.textrasms.manager.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkFailed @Inject constructor(
    private val messageRepo: MessageRepository,
    private val myNotificationManager: MyNotificationManager
) : Interactor<MarkFailed.Params>() {

    data class Params(val address: String, val id: Long, val resultCode: Int)

    override fun buildObservable(params: Params): Flowable<Unit> {
        return Flowable.just(Unit)
            .doOnNext { messageRepo.markFailed(params.address, params.id, params.resultCode) }
            .doOnNext { myNotificationManager.notifyFailed(params.id) }
    }

}