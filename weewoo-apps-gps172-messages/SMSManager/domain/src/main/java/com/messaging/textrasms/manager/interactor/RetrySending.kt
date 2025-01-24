package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.repository.MessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class RetrySending @Inject constructor(private val messageRepo: MessageRepository) :
    Interactor<Long>() {

    override fun buildObservable(params: Long): Flowable<Message> {
        return Flowable.just(params)
            .doOnNext { messageRepo.markSending(params) }
            .mapNotNull(messageRepo::getMessage)
            .doOnNext { message ->
                when (message.isSms()) {
                    true -> messageRepo.sendSms(message, emptyList())
                    false -> messageRepo.resendMms(message)
                }
            }
    }

}
