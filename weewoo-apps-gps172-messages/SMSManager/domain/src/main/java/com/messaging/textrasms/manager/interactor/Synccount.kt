package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.repository.SyncRepository
import io.reactivex.Flowable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class Synccount @Inject constructor(
    private val syncManager: SyncRepository

) : Interactor<Unit>() {
    override fun buildObservable(params: Unit): Flowable<*> {
        return Flowable.just(System.currentTimeMillis())
            .doOnNext { syncManager.personalcount }
            .doOnNext { syncManager.othercount }
            .doOnNext { syncManager.spamcount }
            .map { startTime -> System.currentTimeMillis() - startTime }
            .map { elapsed -> TimeUnit.MILLISECONDS.toSeconds(elapsed) }
            .doOnNext { seconds -> Timber.v("Completed sync in $seconds seconds") }

    }

    fun calls(): Int {
        return syncManager.spamcount
    }

}