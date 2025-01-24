package com.messaging.textrasms.manager.feature.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
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
import com.messaging.textrasms.manager.common.util.extensions.dismissKeyboard
import com.messaging.textrasms.manager.common.util.extensions.scrapViews
import com.messaging.textrasms.manager.feature.Activities.MainActivity
import com.messaging.textrasms.manager.feature.Activities.MainActivity.Companion.fromother
import com.messaging.textrasms.manager.feature.Activities.MainActivity.Companion.frompersonal
import com.messaging.textrasms.manager.feature.Activities.MainActivity.Companion.fromspam
import com.messaging.textrasms.manager.feature.Views.PersonalMainView
import com.messaging.textrasms.manager.feature.adapters.conversations.ConversationsAdapterpersonal
import com.messaging.textrasms.manager.feature.ads.AdvertiseHandler
import com.messaging.textrasms.manager.feature.models.PersonalModel
import com.messaging.textrasms.manager.feature.states.Archived
import com.messaging.textrasms.manager.feature.states.Inbox
import com.messaging.textrasms.manager.feature.states.MainState
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.util.Preferences
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.personal_fragment.constarin_layout
import kotlinx.android.synthetic.main.personal_fragment.no_search_layout
import kotlinx.android.synthetic.main.personal_fragment.recyclerView1
import javax.inject.Inject

class personal_fragment : QkThemeFragment(), PersonalMainView {

    var advertiseHandler: AdvertiseHandler? = null

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

            viewModelpersonal.sorting(
                "",
                month.toLong(),
                year.toLong(),
                sort,
                category,
                toselectdate,
                fromselectdate
            )
            mCallback.hassortingpersonal()
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

        viewModelpersonal.sorting(
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

        fun newInstance(): personal_fragment {
            return personal_fragment()
        }

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

    @Inject
    lateinit var viewModelFactory2: ViewModelProvider.Factory
    private val viewModelpersonal by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory2
        )[PersonalModel::class.java]
    }

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var conversationsAdapter: ConversationsAdapterpersonal

    override val optionsItemIntent: Subject<Int> = PublishSubject.create()
    override val conversationsSelectedIntent by lazy { conversationsAdapter.selectionChanges }
    override val confirmDeleteIntent: Subject<List<Long>> = PublishSubject.create()

    override val undoArchiveIntent: Subject<Unit> = PublishSubject.create()
    override val snackbarButtonIntent: Subject<Unit> = PublishSubject.create()

    var category: Int = 3
    private var datasize: Int = 0

    @Inject
    lateinit var disposables: CompositeDisposable

    override fun clearSearch() {
        activity?.dismissKeyboard()
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
        requireActivity().runOnUiThread { conversationsAdapter.selectall(conversations) }
    }

    override fun showArchivedSnackbar() {
        Snackbar.make(constarin_layout, R.string.toast_archived, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
            setActionTextColor(colors.theme().theme)
            show()
        }
    }

    override fun render(state: MainState) {
        if (state.hasError) {
            activity?.finish()
            return
        }

        mCallback.sendstate(state)
        conversationsAdapter.emptyView =
            no_search_layout.takeIf { state.page is Inbox || state.page is Archived }



        when (state.page) {

            is Inbox -> {
                if (recyclerView1.adapter !== conversationsAdapter) {
                    recyclerView1.adapter = conversationsAdapter


                    conversationsAdapter.setactivity(requireActivity())
                }
                logDebug("datasize" + datasize)

                conversationsAdapter.setDataSize(state.page.data!!.size)
//                var dataItem = ArrayList<Any>()
//                for (item in state.page.data?.indices!!){
//                    if (item % 4 == 0 && item != 0){
//
//                        dataItem.add("ad")
//                        dataItem.add(state.page.data)
//                    }else{
//                        dataItem.add(state.page.data)
//                    }
//                }
                conversationsAdapter.updateData(state.page.data)
                datasize = state.page.data!!.size
                Log.e("personal_fragment","datasize>"+ state.page.data.size)
                val category = Preferences.getIntVal(
                    (activity as MainActivity).applicationContext,
                    "which"
                )

                if (category != 3 && state.page.data.size == 0) {
                    logDebug("getvisibilitypersonal" + state.page.data.size + ">>" + category)
                    var title = when {
                        category.equals(1) -> "month"
                        category.equals(2) -> "year"
                        category.equals(4) -> "date"
                        category.equals(0) -> "date"
                        else -> "date"

                    }

                    if (frompersonal) {

                        val snack = Snackbar.make(
                            constarin_layout,
                            "You don't have any message of selected" + " " + "" + title + ".",
                            Snackbar.LENGTH_SHORT
                        ).apply {
                        }
                        snack.view.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
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

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(msg, msg)
    }


    override fun onStart() {
        viewModelpersonal.bindView(this)
        super.onStart()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        optionsItemIntent.onNext(item.itemId)
        return true
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        advertiseHandler = AdvertiseHandler.getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Preferences.setBoolean(requireActivity(),Preferences.IS_FIRST_SESSION,true)

        val view = inflater.inflate(R.layout.personal_fragment, container, false)
        val recyclerView1 = view.findViewById<RecyclerView>(R.id.recyclerView1)
        recyclerView1.layoutManager = LinearLayoutManager(activity)
        conversationsAdapter.setactivity(requireActivity())
        return view
    }

    lateinit var mCallback: Menuoptioninterface

    //defining Interface
    interface Menuoptioninterface {
        fun sendstate(state: MainState)
        fun hassortingpersonal()
    }

    fun setOnTextClickedListener(callback: Menuoptioninterface) {
        this.mCallback = callback
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            frompersonal = true
            fromother = false
            fromspam = false
            if (recyclerView1 != null) {
                try {
                    if (datasize == 0) {
                        logDebug("getvisibilityphint" + ">>" + datasize)
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
                                //                        setAction(R.string.button_undo) { undoArchiveIntent.onNext(Unit) }
                            }
                            snack.view.setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(),
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
        advertiseHandler!!.isAppStartUpAdsPause = true
        showInterstitialAd(id)
    }

    fun showconnversation(id: Long) {
        navigator.showConversation(requireActivity(), id)
    }

    private fun showInterstitialAd(id: Long) {
        showconnversation(id)
//        if (advertiseHandler!!.isAppStartUpAdsEnabled) {
//            advertiseHandler!!.showInterstitialAds(requireActivity(), 1, object :
//                AdvertiseHandler.AdsListener {
//                override fun onAdsOpened() {
//                    Log.i("TAG", "onAdsOpened: ")
//                }
//
//                override fun onAdsClose() {
//                    Log.i("TAG", "onAdsClose: ")
//                    showconnversation(id)
//                    advertiseHandler!!.isAppStartUpAdsEnabled = false
//                }
//
//                override fun onAdsLoadFailed() {
//                    Log.i("TAG", "onAdsLoadFailed: ")
//                    showconnversation(id)
//                }
//            }, true, false)
//        } else {
//            showconnversation(id)
//        }
    }


    override fun onResume() {
        super.onResume()


                com.messaging.textrasms.manager.utils.Constants.IS_FROM_COMPOSE_ACTIVITY = false
                if (conversationsAdapter != null) {
                    Log.e("personal_fragment",">working")
                    conversationsAdapter.notifyDataSetChanged()

            }
    }
}