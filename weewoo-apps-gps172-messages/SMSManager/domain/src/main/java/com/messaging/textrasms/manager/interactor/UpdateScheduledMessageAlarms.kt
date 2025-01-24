package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.manager.AlarmManager
import com.messaging.textrasms.manager.repository.ScheduledMessageRepository
import io.reactivex.Flowable
import javax.inject.Inject

class UpdateScheduledMessageAlarms @Inject constructor(
    private val alarmManager: AlarmManager,
    private val scheduledMessageRepo: ScheduledMessageRepository,
    private val sendScheduledMessage: SendScheduledMessage
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> {
        return Flowable.just(params)
            .map { scheduledMessageRepo.getScheduledMessages() }
            .map {
                it.map { message ->
                    Pair(
                        message.id,
                        message.date
                    )
                }
            }
            .flatMap { messages -> Flowable.fromIterable(messages) }
            .doOnNext { (id, date) ->
                alarmManager.setAlarm(
                    date,
                    alarmManager.getScheduledMessageIntent(id)
                )
            }
            .filter { (_, date) -> date < System.currentTimeMillis() }
            .flatMap { (id, _) -> sendScheduledMessage.buildObservable(id) }
    }

}