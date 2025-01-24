package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.repository.SyncRepository
import io.reactivex.Flowable
import timber.log.Timber
import javax.inject.Inject

class SyncContacts @Inject constructor(private val syncManager: SyncRepository) :
    Interactor<Unit>() {

    override fun buildObservable(params: Unit): Flowable<Long> {
        return Flowable.just(System.currentTimeMillis())
            .doOnNext { syncManager.syncContacts() }
            .map { startTime -> System.currentTimeMillis() - startTime }
            .doOnNext { duration -> Timber.v("Completed sync in ${duration}ms") }
    }

}