package com.messaging.textrasms.manager.feature.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.maxAdManager.MaxAdManager
import com.messaging.textrasms.manager.common.util.ChangeSortingDialog
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.extensions.dismissKeyboard
import com.messaging.textrasms.manager.common.util.extensions.scrapViews
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.Views.ArchiveView
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsAdapter
import com.messaging.textrasms.manager.feature.blocking.BlockingDialog
import com.messaging.textrasms.manager.feature.models.ArchiveViewModel
import com.messaging.textrasms.manager.feature.states.Archived
import com.messaging.textrasms.manager.feature.states.Inbox
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.messaging.textrasms.manager.utils.Constants
import dagger.android.AndroidInjection
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.archive_activity.*
import kotlinx.android.synthetic.main.layout_big_native_ad.maxAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.nativeAdContainer
import kotlinx.android.synthetic.main.layout_big_native_ad.textViewRectangleNative
import kotlinx.android.synthetic.main.main_activity.relativeBanner
import javax.inject.Inject

class Archiveactivity : QkThemedActivity(), ArchiveView {

    override fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }

    @Inject
    lateinit var blockingDialog: BlockingDialog
    lateinit var navigator: Navigator

    @Inject
    internal lateinit var conversationsAdapter: ConversationsAdapter

    @Inject
    lateinit var drawerBadgesExperiment: DrawerBadgesExperiment

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val backpress: Subject<Unit> = PublishSubject.create()
    override val activityResumedIntent: Subject<Boolean> = PublishSubject.create()
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val onNewIntentIntent: Subject<Intent> = PublishSubject.create()
    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[ArchiveViewModel::class.java]
    }


    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()

    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.archive_activity)
        viewModel.bindView(this)
        onNewIntentIntent.onNext(intent)
        // showBackButton(true)
        recyclerView1.setHasFixedSize(true)
        recyclerView1.layoutManager = LinearLayoutManager(this)


        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            homeIntent.onNext(Unit)
        }
        back.setOnClickListener { onBackPressed() }

        ThemeChangeTask().execute()
    }


    override fun onBackPressed() {
        Constants.IS_FROM_ACTIVITY = true
        super.onBackPressed()
    }
    @SuppressLint("StaticFieldLeak")
    inner class ThemeChangeTask : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String?): String {

            return ""
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.run(onNewIntentIntent::onNext)
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

        toolbarTitle.setVisible(true)

        toolbar.menu.findItem(R.id.info)?.isVisible = false
        toolbar.menu.findItem(R.id.archive)?.isVisible =
            state.page is Inbox && selectedConversations != 0
        toolbar.menu.findItem(R.id.unarchive)?.isVisible =
            state.page is Archived && selectedConversations != 0
        toolbar.menu.findItem(R.id.delete)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.add)?.isVisible = addContact && selectedConversations != 0
        toolbar.menu.findItem(R.id.pin)?.isVisible =
            markPinned && selectedConversations != 0 && state.page is Archived && state.page.selected != state.page.data!!.size

        toolbar.menu.findItem(R.id.unpin)?.isVisible = !markPinned && selectedConversations != 0
        toolbar.menu.findItem(R.id.read)?.isVisible = markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.unread)?.isVisible = !markRead && selectedConversations != 0
        toolbar.menu.findItem(R.id.Rate)?.isVisible = false
        toolbar.menu.findItem(R.id.selectAllmenu)?.isVisible = false
        toolbar.menu.findItem(R.id.setting)?.isVisible = false
        toolbar.menu.findItem(R.id.block)?.isVisible = selectedConversations != 0
        toolbar.menu.findItem(R.id.sort)?.isVisible = false

        toolbar.menu.findItem(R.id.selectAll)?.isVisible = (selectedConversations == 1)
        toolbar.menu.findItem(R.id.deselectAll)?.isVisible = selectedConversations > 1

        conversationsAdapter.emptyView = no_search_layout
        when (state.page) {

            is Inbox -> {
                showBackButton(state.page.selected > 0)
                if (recyclerView1.adapter !== conversationsAdapter) recyclerView1.adapter =
                    conversationsAdapter
                conversationsAdapter.updateData(state.page.data)
                title = getString(R.string.main_title_selected, state.page.selected)


            }


            is Archived -> {

                title = when (state.page.selected != 0) {
                    true -> getString(R.string.main_title_selected, state.page.selected)
                    false -> getString(R.string.title_archived)
                }
                if (recyclerView1.adapter !== conversationsAdapter) {
                    recyclerView1.adapter = conversationsAdapter
                }
                conversationsAdapter.updateData(state.page.data)


            }

            else -> {}
        }

        state.page is Archived


    }


    override fun Showsorting() {
        ChangeSortingDialog(activity as Archiveactivity)
        {
            val month = Preferences.getIntVal(applicationContext, "selectedMonth")
            val year = Preferences.getIntVal(applicationContext, "selectedYear")
            val order = Preferences.getIntVal(applicationContext, "order")
            val toselectdate = Preferences.getStringVal(applicationContext, "dayStart") as String
            val fromselectdate = Preferences.getStringVal(applicationContext, "dayEnd") as String
            val category = Preferences.getIntVal(applicationContext, "which")

            val sort: Sort
            if (order.equals(1)) {
                sort = Sort.ASCENDING
            } else {
                sort = Sort.DESCENDING
            }

            viewModel.sorting(
                "",
                month.toLong(),
                year.toLong(),
                sort,
                category,
                toselectdate,
                fromselectdate
            )
        }
    }
    private fun isPurchased(): Boolean {
        val purchase = Preferences.getBoolean(this, Preferences.ADSREMOVED)
        return purchase
    }
    private fun showNativeAd() {
        if (!isPurchased()) {

            MaxAdManager.createNativeAd(this,maxAdContainer,nativeAdContainer,textViewRectangleNative,{
                nativeBanner.visibility = View.GONE
            },{
                nativeBanner.visibility = View.VISIBLE
            })

        } else {
            nativeBanner.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        showNativeAd()
        activityResumedIntent.onNext(true)
    }

    override fun onPause() {
        super.onPause()
        activityResumedIntent.onNext(false)
    }


    var activity: Archiveactivity? = null

    init {
        activity = this
    }


    override fun clearSelection() {
        conversationsAdapter.clearSelection()
    }

    override fun themeChanged() {
        recyclerView1.scrapViews()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        if (menu != null) {
            for (i in 0 until menu.size()) {
                menu.getItem(i).icon?.colorFilter = PorterDuffColorFilter(
                    resolveThemeColor(android.R.attr.textColorPrimary),
                    PorterDuff.Mode.SRC_IN
                )
            }
            menu.findItem(R.id.info)?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
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
        activity!!.runOnUiThread { conversationsAdapter.selectall(conversations) }
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


}