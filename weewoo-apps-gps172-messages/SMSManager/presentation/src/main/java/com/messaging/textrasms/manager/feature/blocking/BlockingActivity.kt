package com.messaging.textrasms.manager.feature.blocking

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkController
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.feature.blocking.messages.BlockedMessagesAdapter
import com.messaging.textrasms.manager.feature.blocking.messages.BlockedMessagesController
import com.messaging.textrasms.manager.feature.blocking.messages.BlockedMessagesPresenter
import com.messaging.textrasms.manager.feature.blocking.messages.BlockedMessagesState
import com.messaging.textrasms.manager.feature.blocking.messages.BlockedMessagesView
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.messaging.textrasms.manager.utils.Constants
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.blocked_messages_controller.conversations
import kotlinx.android.synthetic.main.blocked_messages_controller.no_search_layout
import kotlinx.android.synthetic.main.container_activity.*
import kotlinx.android.synthetic.main.layout_big_native_ad.maxAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.nativeAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.textViewRectangleNative
import kotlinx.android.synthetic.main.layout_filter.nativeAd
import javax.inject.Inject

class BlockingActivity : QkThemedActivity(),
    BlockedMessagesView {

    private lateinit var router: Router

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
    lateinit var context: Context

    @Inject
    lateinit var presenter: BlockedMessagesPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_activity)
        backcompose.visibility = View.VISIBLE
        blockedMessagesAdapter.emptyView = no_search_layout
        conversations.adapter = blockedMessagesAdapter
        presenter.bindIntents(this)
        setTitle(R.string.blocked_messages_title)
        backcompose.setOnClickListener {
            onBackPressed()
        }
//        router = Conductor.attachRouter(this, container, savedInstanceState)
//        if (!router.hasRootController()) {
//            router.setRoot(RouterTransaction.with(BlockedMessagesController()))
//        }
    }

    override fun onBackPressed() {
        Constants.IS_FROM_ACTIVITY = true
        super.onBackPressed()
    }

    private fun isPurchased(): Boolean {
        val purchase = Preferences.getBoolean(this, Preferences.ADSREMOVED)
        return purchase
    }
    private fun showNativeAd() {
        if (!isPurchased()) {

            MaxAdManager.createNativeAd(this,maxAdContainer,nativeAdContainer,textViewRectangleNative,{
                ad_view_container.visibility = View.GONE
            },{
                ad_view_container.visibility = View.VISIBLE
            })

        } else {
            ad_view_container.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.blocked_messages, menu)
        if (menu != null){
            for (i in 0 until menu.size()) {
                menu.getItem(i).icon?.colorFilter = PorterDuffColorFilter(
                    resolveThemeColor(android.R.attr.textColorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
        menuReadyIntent.onNext(Unit)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun render(state: BlockedMessagesState) {
        blockedMessagesAdapter.updateData(state.data)

        toolbar?.menu?.findItem(R.id.block)?.isVisible = state.selected > 0
        toolbar?.menu?.findItem(R.id.delete)?.isVisible = state.selected > 0

        setTitle(
            when (state.selected) {
                0 -> resources.getString(R.string.blocked_messages_title)
                else -> resources.getString(R.string.main_title_selected, state.selected)
            }
        )
    }

    override fun clearSelection() = blockedMessagesAdapter.clearSelection()

    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
        blockingDialog.show(this, conversations, block)
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        AlertDialog.Builder(this)
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

    override fun onResume() {
        super.onResume()
        showNativeAd()
    }

}