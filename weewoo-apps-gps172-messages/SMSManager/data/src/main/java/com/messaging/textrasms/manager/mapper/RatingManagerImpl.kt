package com.messaging.textrasms.manager.mapper

import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.messaging.textrasms.manager.manager.RatingManager
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject

class RatingManagerImpl @Inject constructor(
    rxPrefs: RxSharedPreferences
) : RatingManager {

    companion object {
        private const val RATING_THRESHOLD = 10
    }

    private val sessions = rxPrefs.getInteger("sessions", 0)
    private val rated = rxPrefs.getBoolean("rated", false)
    private val dismissed = rxPrefs.getBoolean("dismissed", false)

    override val shouldShowRating = Observables.combineLatest(
        sessions.asObservable(),
        rated.asObservable(),
        dismissed.asObservable()
    ) { sessions, rated, dismissed ->
        sessions > RATING_THRESHOLD && !rated && !dismissed
    }

    override fun addSession() {
        sessions.set(sessions.get() + 1)
    }

    override fun rate() {
        rated.set(true)
    }

    override fun dismiss() {
        dismissed.set(true)
    }

}
