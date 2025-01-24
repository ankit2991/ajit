package com.messaging.textrasms.manager.feature.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.textChanges
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.extensions.dismissKeyboard
import com.messaging.textrasms.manager.common.util.extensions.scrapViews
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.Views.SearchView
import com.messaging.textrasms.manager.feature.adapters.SearchAdapter
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsAdapter
import com.messaging.textrasms.manager.feature.adapters.conversations.RecentAdapter
import com.messaging.textrasms.manager.feature.blocking.BlockingDialog
import com.messaging.textrasms.manager.feature.models.SearchViewModel
import com.messaging.textrasms.manager.feature.states.Archived
import com.messaging.textrasms.manager.feature.states.Inbox
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.feature.states.Searching
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.messaging.textrasms.manager.utils.Constants
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import kotlinx.android.synthetic.main.search_activity.*
import javax.inject.Inject

class Searchactivity : QkThemedActivity(), SearchView {

    @Inject
    lateinit var blockingDialog: BlockingDialog
    lateinit var navigator: Navigator

    @Inject
    internal lateinit var conversationsAdapter: ConversationsAdapter

    @Inject
    internal lateinit var recentAdapter: RecentAdapter


    @Inject
    lateinit var drawerBadgesExperiment: DrawerBadgesExperiment

    @Inject
    lateinit var searchAdapter: SearchAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val activityResumedIntent: Subject<Boolean> = PublishSubject.create()
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val queryChangedIntent by lazy { toolbarSearch.textChanges() }
    override val queryClearedIntent: Observable<*> by lazy { cancel.clicks() }
    override val onNewIntentIntent: Subject<Intent> = PublishSubject.create()
    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[SearchViewModel::class.java]
    }

    @SuppressLint("SimpleDateFormat")
    override val backPressedSubject: Subject<String> = PublishSubject.create()
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()
    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()

    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity)

        viewModel.bindView(this)
        onNewIntentIntent.onNext(intent)
        recyclerView_recent.adapter = recentAdapter
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView_recent.layoutManager = layoutManager
        recyclerView1.layoutManager = LinearLayoutManager(this)


        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }
        back.setOnClickListener(View.OnClickListener {
            onBackPressed()
            overridePendingTransition(R.anim.slide_down_reverse, R.anim.slide_up_reverse)
        })
    }

    override fun onBackPressed() {
        Constants.IS_FROM_ACTIVITY = true
        super.onBackPressed()
    }


    override fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.run(onNewIntentIntent::onNext)
    }

    override fun clearQuery() {
        toolbarSearch.text = null
    }

    override fun render(state: MainState) {

        if (state.hasError) {
            finish()
            return
        }
        val addContact = when (state.page) {
            is Inbox -> state.page.addContact
            is Archived -> state.page.addContact
            else -> false
        }

        val markPinned = when (state.page) {
            is Inbox -> state.page.markPinned
            is Archived -> state.page.markPinned
            else -> true
        }

        val markRead = when (state.page) {
            is Inbox -> state.page.markRead
            is Archived -> state.page.markRead
            else -> true
        }

        val selectedConversations = when (state.page) {
            is Inbox -> state.page.selected
            is Archived -> state.page.selected
            else -> 0
        }

        toolbarSearch.setVisible(state.page is Inbox && state.page.selected == 0 || state.page is Searching)
        toolbarTitle.setVisible(toolbarSearch.visibility != View.VISIBLE)
        toolbar.menu.findItem(R.id.archive)?.isVisible =
            state.page is Inbox && selectedConversations != 0
        toolbar.menu.findItem(R.id.unarchive)?.isVisible =
            state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.add)?.isVisible = addContact && selectedConversations != 0
        toolbar.menu.findItem(R.id.pin)?.isVisible = markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.Rate)?.isVisible = false
        toolbar.menu.findItem(R.id.selectAllmenu)?.isVisible = false
        toolbar.menu.findItem(R.id.setting)?.isVisible = false




        conversationsAdapter.emptyView =
            empty.takeIf { state.page is Inbox || state.page is Archived }
        cancel.isVisible = toolbarSearch.text.length > 1
        when (state.page) {

            is Inbox -> {

                if (recyclerView1.adapter !== conversationsAdapter) recyclerView1.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData(state.page.data)

                if (recyclerView_recent.adapter !== recentAdapter) recyclerView_recent.adapter =
                    conversationsAdapter
                recentAdapter.updateData(state.page.datarecent)
                title = getString(R.string.main_title_selected, state.page.selected)

            }

            is Searching -> {
                if (recyclerView1.adapter !== searchAdapter) recyclerView1.adapter = searchAdapter
                searchAdapter.data = state.page.data ?: listOf()
                empty.setText(R.string.inbox_search_empty_text)

            }

            is Archived -> {

                showBackButton(state.page.selected > 0)
                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }

            }
        }

        state.page is Inbox


    }


    override fun onResume() {
        super.onResume()
        activityResumedIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityResumedIntent.onNext(false)
    }


    override fun clearSearch() {
        dismissKeyboard()
        toolbarSearch.text = null
    }


    override fun clearSelection() {
        conversationsAdapter.clearSelection()
    }

    override fun themeChanged() {
        recyclerView1.scrapViews()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.searchmain, menu)
        if (menu != null) {
            for (i in 0 until menu.size()) {
                menu.getItem(i).icon?.colorFilter = PorterDuffColorFilter(
                    resolveThemeColor(android.R.attr.textColorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun showBackButton(show: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(show)

    }

    override fun requestDefaultSms() {
        if (::navigator.isInitialized) {
            navigator.showDefaultSmsDialog(this)
        }
    }

    override fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS
            ), 0
        )
    }


    override fun selectionAll(conversations: RealmResults<Conversation>) {
        conversationsAdapter.selectionAll(conversations)
    }


    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
        blockingDialog.show(this, conversations, block)
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(
                resources.getQuantityString(
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

    override fun showArchivedSnackbar() {
        Snackbar.make(drawerLayout1, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
            setActionTextColor(colors.theme().theme)
            show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }


}