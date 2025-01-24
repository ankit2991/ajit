package com.messaging.textrasms.manager.feature.gallery

import android.content.Context
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkViewModel
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.extensions.mapNotNull
import com.messaging.textrasms.manager.interactor.SaveImage
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.model.logDebug
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import com.messaging.textrasms.manager.util.makeToast
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.Flowable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import javax.inject.Inject
import javax.inject.Named

class GalleryViewModel @Inject constructor(
    conversationRepo: ConversationRepository,
    @Named("partId") private val partId: Long,
    private val context: Context,
    private val messageRepo: MessageRepository,
    private val navigator: Navigator,
    private val saveImage: SaveImage,
    private val permissions: PermissionManager
) : QkViewModel<GalleryView, GalleryState>(GalleryState()) {

    init {
        disposables += Flowable.just(partId)
            .mapNotNull(messageRepo::getMessageForPart)
            .mapNotNull { message -> message.threadId }
            .doOnNext { threadId ->
                newState {
                    copy(
                        parts = messageRepo.getPartsForConversation(
                            threadId
                        )
                    )
                }
            }
            .doOnNext { threadId ->
                newState {
                    copy(title = conversationRepo.getConversation(threadId)?.getTitle())
                }
            }
            .subscribe()
    }

    override fun bindView(view: GalleryView) {
        super.bindView(view)

        view.screenTouched()
            .withLatestFrom(state) { _, state -> state.navigationVisible }
            .map { navigationVisible -> !navigationVisible }
            .autoDispose(view.scope())
            .subscribe { navigationVisible -> newState { copy(navigationVisible = navigationVisible) } }

        view.optionsItemSelected()
            .filter { itemId -> itemId == R.id.save }
            .filter { permissions.hasStorage().also { if (!it) view.requestStoragePermission() } }
            .withLatestFrom(view.pageChanged()) { _, part -> part.id }
            .autoDispose(view.scope())
            .subscribe { partId -> saveImage.execute(partId) { context.makeToast(R.string.gallery_toast_saved) } }

        view.optionsItemSelected()
            .filter { itemId -> itemId == R.id.share }
            .filter { permissions.hasStorage().also { if (!it) view.requestStoragePermission() } }
            .withLatestFrom(view.pageChanged()) { _, part -> part.id }
            .autoDispose(view.scope())
            .subscribe(
                { partId -> messageRepo.savePart(partId)?.let(navigator::shareFile) },
                { throwable -> logDebug("Throw") })
    }

}
