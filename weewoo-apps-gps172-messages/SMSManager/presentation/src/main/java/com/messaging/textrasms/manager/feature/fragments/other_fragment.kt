package com.messaging.textrasms.manager.feature.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
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
import com.messaging.textrasms.manager.feature.Activities.MainActivity.Companion.fromother
import com.messaging.textrasms.manager.feature.Views.OtherMainView
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsOtherAdapter
import com.messaging.textrasms.manager.feature.models.OtherModel
import com.messaging.textrasms.manager.feature.states.Archived
import com.messaging.textrasms.manager.feature.states.Inbox
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.model.logDebug
import com.messaging.textrasms.manager.util.Preferences
import dagger.android.support.AndroidSupportInjection
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.personal_fragment.*
import javax.inject.Inject

class other_fragment : QkThemeFragment(), OtherMainView {
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
            mCallback.hassortingother()
        }
    }

    override fun previoussorting() {

        val month =
            Preferences.getIntVal((activity as MainActivity).applicationContext, "selectedMonth")
        val year =
            Preferences.getIntVal((activity as MainActivity).applicationContext, "selectedYear")
        val order = Preferences.getIntVal((activity as MainActivity).applicationContext, "order")
        val toselectdate = Preferences.getStringVal(
            (activity as MainActivity).applicationContext,
            "dayStart"
        ) as String
        val fromselectdate = Preferences.getStringVal(
            (activity as MainActivity).applicationContext,
            "dayEnd"
        ) as String
        val category = Preferences.getIntVal((activity as MainActivity).applicationContext, "which")

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


    companion object {

        fun newInstance(): other_fragment {
            return other_fragment()
        }
    }


    override val sorted: Subject<Boolean> = PublishSubject.create()

    @Inject
    lateinit var conversationsAdapter: ConversationsOtherAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    override val onNewIntentIntent: Subject<Intent> = PublishSubject.create()
    override val activityResumedIntent: Subject<Boolean> = PublishSubject.create()
    override val homeIntent: Subject<Unit> = PublishSubject.create()
    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()

    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()
    var category: Int = 3

    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[OtherModel::class.java]
    }
    private var datasize: Int = 0

    @Inject
    lateinit var navigator: Navigator
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        logDebug("viewcycle" + "Restorein")
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        logDebug("viewcycle" + "attach")
        super.onAttach(context)

    }

    override fun requestDefaultSms() {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun clearSearch() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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


    override fun render(state: MainState) {
        if (state.hasError) {
            activity?.finish()
            return
        }
        mCallback.sendstateother(state)
        conversationsAdapter.emptyView =
            no_search_layout.takeIf { state.page is Inbox || state.page is Archived }


        when (state.page) {
            is Inbox -> {
                if (recyclerView1.adapter !== conversationsAdapter) {
                    recyclerView1.adapter = conversationsAdapter
                }
                conversationsAdapter.setactivity(this.activity!!)
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
                    if (fromother) {
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


    fun optionitem(item: MenuItem) {
        optionsItemIntent.onNext(item.itemId)
    }


    override fun onStart() {
        viewModel.bindView(this)
        super.onStart()
        logDebug("viewcycle" + "onstart")
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
        category = Preferences.getIntVal(this.activity!!.applicationContext, "which")

        return view
    }

    lateinit var mCallback: Menuoptioninterfaceother

    interface Menuoptioninterfaceother {
        fun sendstateother(state: MainState)
        fun hassortingother()
    }

    fun setOnTextClickedListener(callback: Menuoptioninterfaceother) {
        this.mCallback = callback
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            fromother = true
            logDebug("viewcycle" + "visible")
            MainActivity.frompersonal = false
            MainActivity.fromspam = false
            if (recyclerView1 != null)
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
        MainActivity.isvisible = false
        id = id1
        showconnversation(id)
    }

    fun showconnversation(id: Long) {
        navigator.showConversation(this.activity!!, id)
    }
}