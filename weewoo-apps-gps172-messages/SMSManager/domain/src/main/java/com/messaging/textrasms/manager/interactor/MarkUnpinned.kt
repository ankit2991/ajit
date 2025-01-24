package com.messaging.textrasms.manager.interactor

import com.messaging.textrasms.manager.manager.ShortcutManager
import com.messaging.textrasms.manager.repository.ConversationRepository
import io.reactivex.Flowable
import javax.inject.Inject

class MarkUnpinned @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val updateBadge: UpdateBadge,
    private val shortcutManager: ShortcutManager
) : Interactor<List<Long>>() {

    override fun buildObservable(params: List<Long>): Flowable<*> {
        return Flowable.just(params.toLongArray())
            .doOnNext { threadIds -> conversationRepo.markUnpinned(*threadIds) }
            .doOnNext { shortcutManager.updateShortcuts() }
            .flatMap { updateBadge.buildObservable(Unit) }
    }

}
