package com.translate.languagetranslator.freetranslation.activities.historyBookmark

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.historyBookmark.adapter.HistoryAdapter
import com.translate.languagetranslator.freetranslation.activities.historyBookmark.interfaces.HistoryBookmarkInterface
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_SOURCE_HISTORY
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity(), HistoryBookmarkInterface {


    private var database: TranslationDb? = null

    private lateinit var mFireBaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var configSettings: FirebaseRemoteConfigSettings
    private var historyAdapter: HistoryAdapter? = null
    private var source: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        database = TranslationDb.getInstance(this)
        var bundle = Bundle()
        bundle = intent.extras!!
        source = bundle.getString("history_source")
        if (source == INTENT_KEY_SOURCE_HISTORY) {
            title_toolbar.text = "Translation History"
        } else {
            title_toolbar.text = "Bookmark List"

        }

        initRemoteConfig()

        setToolbar()
        setHistoryData()
    }


    private fun initRemoteConfig() {
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(200)
            .build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)

    }

    private fun setToolbar() {

        iv_toolbar_back_history.setOnClickListener {
            onBackPressed()
        }

        iv_clear_history_selection.setOnClickListener {
            historyAdapter?.clearSelection()
        }
        iv_delete_history_selected.setOnClickListener {
            historyAdapter?.let { adapter ->
                val selectedHistory = adapter.getAllSelectedItems()
                for (history in selectedHistory) {
                    database?.translationTblDao()?.delete(history)
                }
                adapter.clearSelection()


            }
        }
        iv_delete_all_history.setOnClickListener {
            historyAdapter?.let { adapter ->
                if (adapter.getAllData().isNotEmpty()) {
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Are you sure to delete selected history?")
                    builder.setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    builder.setPositiveButton("Delete") { dialog, _ ->
                        database?.translationTblDao()?.deleteAll()
                        dialog.dismiss()
                    }
                    val deleteDialog = builder.create()
                    deleteDialog.show()
                }

            }


        }

    }


    private fun setHistoryData() {
        val mLayoutManager = LinearLayoutManager(this)
        recycler_history.layoutManager = mLayoutManager
        recycler_history.isNestedScrollingEnabled = false
        ViewCompat.setNestedScrollingEnabled(recycler_history, false)
        historyAdapter = HistoryAdapter()
        historyAdapter?.setListener(this)
        recycler_history.adapter = historyAdapter
        val historyData = database!!.translationTblDao().all


        historyData.observe(this, androidx.lifecycle.Observer {
            if (it.size > 0) {
                tv_no_history.visibility = View.GONE
            } else {
                tv_no_history.visibility = View.VISIBLE

            }

            historyAdapter?.setData(it)

        })


    }

    override fun onLongClick(position: Int) {

        historyAdapter?.let { adapter ->
            if (!adapter.isSelection())
                adapter.setSelection(true)
            adapter.setChecked(position)

        }

    }

    override fun onSelectItem(translationTable: TranslationTable?, position: Int) {

        historyAdapter?.let { adapter ->
            if (!adapter.isSelection()) {
                val intent = Intent()
                intent.putExtra("origin", "History")
                intent.putExtra("detail_object", translationTable)
                setResult(Constants.REQ_CODE_HISTORY, intent)
                finish()

            } else {
                adapter.setChecked(position)
            }
        }
    }

    override fun onSelectItem(selection: Boolean, selectedItem: Int) {
        if (selection) {
            toolbar_history_normal.visibility = View.GONE
            toolbar_history_selection.visibility = View.VISIBLE
            val count = historyAdapter!!.getAllSelectedItems().size

            tv_history_selected_items.text = "$count Selected"
        } else {
            toolbar_history_normal.visibility = View.VISIBLE
            toolbar_history_selection.visibility = View.GONE
        }
    }

    override fun onDelete(translationTable: TranslationTable?) {
    }

    override fun onFavorite(translationTable: TranslationTable?) {
        database!!.translationTblDao().updateFAv(translationTable!!.isfav, translationTable.id)

    }

    override fun onSelectionChange(selection: Boolean) {
        if (selection) {
            toolbar_history_normal.visibility = View.GONE
            toolbar_history_selection.visibility = View.VISIBLE
        } else {
            toolbar_history_normal.visibility = View.VISIBLE
            toolbar_history_selection.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}
