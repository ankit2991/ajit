package com.messaging.textrasms.manager.feature.blocking.messages

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkController
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.feature.blocking.BlockingDialog
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.util.resolveThemeColor
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.blocked_messages_controller.*
import kotlinx.android.synthetic.main.container_activity.*
import javax.inject.Inject

class BlockedMessagesController :
    QkController<BlockedMessagesView, BlockedMessagesState, BlockedMessagesPresenter>(),
    BlockedMessagesView {

    override val menuReadyIntent: Subject<Unit> = PublishSubject.create()
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val conversationClicks by lazy { blockedMessagesAdapter.clicks }
    override val selectionChanges by lazy { blockedMessagesAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()

    @Inject
    lateinit var blockedMessagesAdapter: BlockedMessagesAdapter

    @Inject
    lateinit var blockingDialog: BlockingDialog

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var context: Context

    @Inject
    override lateinit var presenter: BlockedMessagesPresenter

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.blocked_messages_controller
    }

    override fun onViewCreated() {
        super.onViewCreated()

//        blockedMessagesAdapter.emptyView = no_search_layout
//        conversations.adapter = blockedMessagesAdapter
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.blocked_messages_title)
        showBackButton(true)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.blocked_messages, menu)
        for (i in 0 until menu.size()) {
            menu.getItem(i).icon?.colorFilter = PorterDuffColorFilter(
                activity!!.resolveThemeColor(android.R.attr.textColorPrimary),
                PorterDuff.Mode.SRC_IN
            )
        }
        menuReadyIntent.onNext(Unit)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun render(state: BlockedMessagesState) {
        blockedMessagesAdapter.updateData(state.data)

        themedActivity?.toolbar?.menu?.findItem(R.id.block)?.isVisible = state.selected > 0
        themedActivity?.toolbar?.menu?.findItem(R.id.delete)?.isVisible = state.selected > 0

        setTitle(
            when (state.selected) {
                0 -> context.getString(R.string.blocked_messages_title)
                else -> context.getString(R.string.main_title_selected, state.selected)
            }
        )
    }

    override fun clearSelection() = blockedMessagesAdapter.clearSelection()

    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
        blockingDialog.show(activity!!, conversations, block)
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(
                resources?.getQuantityString(
                    R.plurals.dialog_delete_conversion,
                    count,
                    count
                )
            )
            .setPositiveButton(R.string.button_delete) { _, _ ->
                confirmDeleteIntent.onNext(
                    conversations
                )
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }
}
