package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.repository.ContactRepository
import io.reactivex.Flowable
import javax.inject.Inject

class SetDefaultPhoneNumber @Inject constructor(
    private val contactRepo: ContactRepository
) : Interactor<SetDefaultPhoneNumber.Params>() {

    data class Params(val lookupKey: String, val phoneNumberId: Long)

    override fun buildObservable(params: Params): Flowable<*> {
        return Flowable.just(params)
            .doOnNext { (lookupKey, phoneNumberId) ->
                contactRepo.setDefaultPhoneNumber(lookupKey, phoneNumberId)
            }
    }

}
