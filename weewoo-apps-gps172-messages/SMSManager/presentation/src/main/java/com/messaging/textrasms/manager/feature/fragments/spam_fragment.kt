package com.messaging.textrasms.manager.feature.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemeFragment
import com.messaging.textrasms.manager.common.util.ChangeSortingDialog
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.extensions.scrapViews
import com.messaging.textrasms.manager.feature.Activities.MainActivity
import com.messaging.textrasms.manager.feature.Activities.MainActivity.Companion.fromspam
import com.messaging.textrasms.manager.feature.Views.SpamMainView
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsOtherAdapter
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsOtherSpamAdapter
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationspamAdapter
import com.messaging.textrasms.manager.feature.models.UnknownModel
import com.messaging.textrasms.manager.feature.states.Archived
import com.messaging.textrasms.manager.feature.states.Inbox
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.tryOrNull
import dagger.android.support.AndroidSupportInjection
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.personal_fragment.*
import kotlinx.android.synthetic.main.widget_list_item.conversation
import javax.inject.Inject

class spam_fragment : QkThemeFragment(), SpamMainView {

    override val syncunknown: Subject<Unit> = PublishSubject.create()

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var conversationsAdapter: ConversationspamAdapter

    @Inject
    lateinit var viewModelFactory1: ViewModelProvider.Factory
    override val onNewIntentIntent: Subject<Intent> = PublishSubject.create()
    override val activityResumedIntent: Subject<Boolean> = PublishSubject.create()
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override var optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()
    lateinit var mCallback: Menuoptioninterfacespam
    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()
    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory1
        )[UnknownModel::class.java]
    }
    private var datasize: Int = 0
    override val sorted: Subject<Boolean> = PublishSubject.create()
    var spamfragment: Boolean = false
    var category: Int = 3
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)


    }

    override fun previoussorting() {
        tryOrNull {
            val month = Preferences.getIntVal(
                (activity as MainActivity).applicationContext,
                "selectedMonth"
            )
            val year =
                Preferences.getIntVal((activity as MainActivity).applicationContext, "selectedYear")
            val order =
                Preferences.getIntVal((activity as MainActivity).applicationContext, "order")
            val toselectdate = Preferences.getStringVal(
                (activity as MainActivity).applicationContext,
                "dayStart"
            ) as String
            val fromselectdate = Preferences.getStringVal(
                (activity as MainActivity).applicationContext,
                "dayEnd"
            ) as String
            val category =
                Preferences.getIntVal((activity as MainActivity).applicationContext, "which")

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
            mCallback.hassortingspam()
        }
    }

    override fun Showsorting() {
        ChangeSortingDialog(activity as MainActivity)
        {
            val month = Preferences.getIntVal(
                (activity as MainActivity).applicationContext,
                "selectedMonth"
            )
            val year =
                Preferences.getIntVal((activity as MainActivity).applicationContext, "selectedYear")
            val order =
                Preferences.getIntVal((activity as MainActivity).applicationContext, "order")
            val toselectdate = Preferences.getStringVal(
                (activity as MainActivity).applicationContext,
                "dayStart"
            ) as String
            val fromselectdate = Preferences.getStringVal(
                (activity as MainActivity).applicationContext,
                "dayEnd"
            ) as String
            val category =
                Preferences.getIntVal((activity as MainActivity).applicationContext, "which")

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
            mCallback.hassortingspam()
        }
    }


    override fun clearSearch() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearSelection() {
        if (::conversationsAdapter.isInitialized) {
            conversationsAdapter.clearSelection()
        }
    }

    override fun themeChanged() {
        recyclerView1.scrapViews()
    }

    override fun showBlockingDialog(conversations: List<Long>, block: Boolean) {
    }

    override fun showDeleteDialog(conversations: List<Long>) {
        val count = conversations.size
        AlertDialog.Builder(activity)
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

    override fun selectionAll(conversations: RealmResults<Conversation>) {
        activity!!.runOnUiThread { conversationsAdapter.selectall(conversations) }
    }

    override fun showArchivedSnackbar() {
        Snackbar.make(constarin_layout, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
            setActionTextColor(resources.getColor(R.color.tools_theme))
            show()
        }
    }

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(msg, msg)
    }

    override fun render(state: MainState) {
        if (state.hasError) {
            activity?.finish()
            return
        }
        mCallback.sendstatespam(state)
        conversationsAdapter.emptyView =
            no_search_layout.takeIf { state.page is Inbox || state.page is Archived }

        when (state.page) {
            is Inbox -> {
                if (recyclerView1.adapter !== conversationsAdapter) {
                    recyclerView1.adapter = conversationsAdapter
                }

                conversationsAdapter.updateData(state.page.data)
                datasize = state.page.data!!.size
                val category = Preferences.getIntVal(
                    (activity as MainActivity).applicationContext,
                    "which"
                )
                if (category != 3 && state.page.data.size == 0) {

                    var title = when {
                        category.equals(1) -> "month"
                        category.equals(2) -> "year"
                        category.equals(4) -> "date"
                        category.equals(0) -> "date"
                        else -> "date"

                    }
                    if (fromspam) {
                        val snack = Snackbar.make(
                            constarin_layout,
                            "You don't have any message of selected" + " " + title + ".",
                            Snackbar.LENGTH_SHORT
                        ).apply {
                            //                        setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
                        }
                        snack.view.setBackgroundColor(
                            ContextCompat.getColor(
                                this.context!!,
                                R.color.tools_theme
                            )
                        )
                        snack.show()
                    }

                }
            }

            else -> {}
        }
        state.page is Inbox
    }


    override fun onStart() {
        viewModel.bindView(this)
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.personal_fragment, container, false)
        val recyclerView1 = view.findViewById<RecyclerView>(R.id.recyclerView1)

        recyclerView1.layoutManager = LinearLayoutManager(activity)

        conversationsAdapter.setactivity(this.activity!!)
        val category = Preferences.getIntVal(this.activity!!.applicationContext, "which")
        if (category != 3) {
            sorted.onNext(true)
        }


        return view
    }

    override fun requestStoragePermissions() {
        activity?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        }

    }

    companion object {

        fun newInstance(): spam_fragment {
            return spam_fragment()
        }

    }

    override fun requestDefaultSms() {
        activity?.let { navigator.showDefaultSmsDialog(it) }
    }

    override fun requestPermissions() {
        activity?.let {
            ActivityCompat.requestPermissions(
                it, arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 0
            )
        }
    }

    interface Menuoptioninterfacespam {
        fun sendstatespam(state: MainState)
        fun hassortingspam()
    }

    fun setOnTextClickedListener(callback: Menuoptioninterfacespam) {
        this.mCallback = callback
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }


    fun optionitem(item: MenuItem) {
        optionsItemIntent.onNext(item.itemId)
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            fromspam = true
            MainActivity.frompersonal = false
            MainActivity.fromother = false
            spamfragment = true
            try {
                if (datasize == 0) {
                    val category = Preferences.getIntVal(
                        (activity as MainActivity).applicationContext,
                        "which"
                    )
                    if (category != 3) {

                        var title = when {
                            category.equals(1) -> "month"
                            category.equals(2) -> "year"
                            category.equals(4) -> "date"
                            category.equals(0) -> "date"
                            else -> "date"

                        }

                        val snack = Snackbar.make(
                            constarin_layout,
                            "You don't have any message of selected" + " " + title + ".",
                            Snackbar.LENGTH_SHORT
                        ).apply {
                        }
                        snack.view.setBackgroundColor(
                            ContextCompat.getColor(
                                this.context!!,
                                R.color.tools_theme
                            )
                        )
                        snack.show()
                    }

                }
            } catch (e: Exception) {

            }
        }
    }

    fun sorting() {
        BackgroundTask().execute()
    }

    private inner class BackgroundTask : AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
        }

        override fun doInBackground(vararg params: Void): String? {

            previoussorting()
            return ""
        }

        override fun onPostExecute(result: String) {

        }
    }


    var id: Long = -1
    fun adshowing(id1: Long) {
        id = id1
        showconnversation(id)
    }

    fun showconnversation(id: Long) {
        navigator.showConversation(this.activity!!, id)
    }
}