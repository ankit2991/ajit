package com.translate.languagetranslator.freetranslation.activities.historyBookmark

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.historyBookmark.adapter.BookmarkAdapter
import com.translate.languagetranslator.freetranslation.activities.historyBookmark.interfaces.HistoryBookmarkInterface
import com.translate.languagetranslator.freetranslation.appUtils.Constants
import com.translate.languagetranslator.freetranslation.database.TranslationDb
import com.translate.languagetranslator.freetranslation.database.entity.TranslationTable
import kotlinx.android.synthetic.main.activity_bookmark.*

class BookmarkActivity : AppCompatActivity(), HistoryBookmarkInterface {
    private val TAG = "Bookmarked List"

    private var historyAdapter: BookmarkAdapter? = null
    private var database: TranslationDb? = null
    private lateinit var mFireBaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var configSettings: FirebaseRemoteConfigSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)
        database = TranslationDb.getInstance(this)
        initRemoteConfig()
        setToolbar()
        setBookMarkData()
    }

    private fun setToolbar() {
        iv_toolbar_back_bookmark.setOnClickListener {
            onBackPressed()
        }
        iv_clear_bookmark_selection.setOnClickListener {
            historyAdapter?.clearSelection()
        }
        iv_delete_selected_bookmarks.setOnClickListener {
            historyAdapter?.let { adapter ->
                val selectedHistory = adapter.getAllSelectedItems()
                for (history in selectedHistory) {
                    database?.translationTblDao()?.delete(history)
                }
                adapter.clearSelection()


            }
        }

        iv_delete_all_bookmark.setOnClickListener {
            historyAdapter?.let { adapter->
                if (adapter.getAllData().isNotEmpty()){
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Are you sure to delete selected bookmarks?")
                    builder.setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                    }
                    builder.setPositiveButton("Delete") { dialog, _ ->
                        database?.translationTblDao()?.deleteAllFav(true)
                        dialog.dismiss()
                    }
                    val deleteDialog = builder.create()
                    deleteDialog.show()
                }
            }

        }

    }

    private fun initRemoteConfig() {
        mFireBaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(200)
            .build()
        mFireBaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFireBaseRemoteConfig.setDefaultsAsync(R.xml.firebase_config)


    }

    private fun setBookMarkData() {
        val mLayoutManager = LinearLayoutManager(this)
        recycler_bookmark.layoutManager = mLayoutManager

        historyAdapter = BookmarkAdapter()
        historyAdapter?.setListener(this)
        recycler_bookmark.adapter = historyAdapter


        val historyData = database!!.translationTblDao().getFavorite(true)
        historyData.observe(this, androidx.lifecycle.Observer {

            if (it.size > 0) {
                tv_no_bookmark.visibility = View.GONE
            } else {
                tv_no_bookmark.visibility = View.VISIBLE

            }

            historyAdapter?.setData(it)

        })


    }

    override fun onFavorite(translationTable: TranslationTable?) {

    }

    override fun onSelectionChange(selection: Boolean) {
        if (selection) {
            toolbar_bookmark_normal.visibility = View.GONE
            toolbar_bookmark_selection.visibility = View.VISIBLE
        } else {
            toolbar_bookmark_normal.visibility = View.VISIBLE
            toolbar_bookmark_selection.visibility = View.GONE
        }
    }

    override fun onLongClick(position: Int) {
        historyAdapter?.let { adapter ->
            if (!adapter.isSelection())
                adapter.setSelection(true)
            adapter.setChecked(position)

        }

    }


    override fun onSelectItem(translationTable: TranslationTable, position: Int) {
        historyAdapter?.let { adapter ->
            if (!adapter.isSelection()) {
                val intent = Intent()
                intent.putExtra("origin", "Bookmark")
                intent.putExtra("detail_object", translationTable)
                setResult(Constants.REQ_CODE_BOOKMARK, intent)
                finish()
//                openHistoryDetailActivity(
//                    this,
//                    translationTable,
//                    Constants.DETAIL_SOURCE_MAIN
//                )

            } else {
                adapter.setChecked(position)
            }
        }

    }

    override fun onSelectItem(selection: Boolean, selectedItem: Int) {
        if (selection) {
            toolbar_bookmark_normal.visibility = View.GONE
            toolbar_bookmark_selection.visibility = View.VISIBLE
            val count = historyAdapter!!.getAllSelectedItems().size

            tv_bookmark_selected_items.text = "$count Selected"
        } else {
            toolbar_bookmark_normal.visibility = View.VISIBLE
            toolbar_bookmark_selection.visibility = View.GONE
        }
    }

    override fun onDelete(translationTable: TranslationTable?) {
        database!!.translationTblDao().delete(translationTable)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
}

