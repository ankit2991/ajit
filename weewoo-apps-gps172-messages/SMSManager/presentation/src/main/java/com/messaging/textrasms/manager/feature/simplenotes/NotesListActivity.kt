package com.messaging.textrasms.manager.feature.simplenotes

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.util.extensions.dismissKeyboard
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.simplenotes.NoteActivity.Companion.getStartIntent
import com.messaging.textrasms.manager.util.resolveThemeColor
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_notes_list.*
import java.util.*

class NotesListActivity : QkThemedActivity() {
    private var searchItem: MenuItem? = null
    private var notesListAdapter: NotesListAdapter? = null
    private var dialog: AlertDialog? = null

    @ColorInt
    private var colourFont = 0

    @ColorInt
    private var colourBackground = 0
    private var isSearching = false
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes_list)
        title = resources.getString(R.string.Notes)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        getSettings(preferences)
        showBackButton(true)
        notesListAdapter = NotesListAdapter(this, colourFont, colourBackground)
        toolbarSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                notesListAdapter!!.filterList(s.toString().lowercase(Locale.getDefault()))
            }

            override fun afterTextChanged(s: Editable) {}
        })
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        val linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = notesListAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fab.isShown) fab.hide()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        cancel.setOnClickListener(View.OnClickListener { searchVisibility(false) })

        toolbar.setNavigationOnClickListener {
            dismissKeyboard()
            finish()
        }
    }

    public override fun onResume() {
        super.onResume()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        notesListAdapter!!.updateList(HelperUtils.getFiles(this))
        showEmptyListMessage()
    }

    public override fun onPause() {
        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        dialog = null
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_notes_list, menu)
        searchItem = menu!!.findItem(R.id.btn_search)
        for (i in 0 until menu.size()) {
            menu.getItem(i).icon?.colorFilter = PorterDuffColorFilter(
                resolveThemeColor(android.R.attr.textColorPrimary),
                PorterDuff.Mode.SRC_IN
            )
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (isSearching) {
                searchVisibility(false)
            } else {
                finish()
            }
            R.id.btn_search -> searchVisibility(true)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun searchVisibility(b: Boolean) {
        isSearching = b
        if (b) {

            cancel.setVisible(true)
            toolbarSearch!!.visibility = View.VISIBLE
            toolbarTitle!!.visibility = View.GONE
            searchItem!!.isVisible = false
        } else {
            cancel.setVisible(false)
            showBackButton(true)
            toolbarSearch!!.visibility = View.GONE
            toolbarSearch!!.setText("")
            toolbarTitle!!.visibility = View.VISIBLE
            searchItem!!.isVisible = true
        }
    }

    override fun onBackPressed() {
        if (!searchItem!!.isVisible) {
            searchVisibility(false)
        } else {
            super.onBackPressed()
        }
    }

    private fun getSettings(preferences: SharedPreferences) {
        colourFont = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK)
    }

    fun passDataToCompose(noteDetails: String) {
        if (noteDetails.isEmpty()) {
            Toast.makeText(this, "Failed to read note !", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent()
        intent.putExtra("noteDetails", noteDetails)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun showEmptyListMessage() {
        notesListAdapter!!.updateList(HelperUtils.getFiles(this))
        if (notesListAdapter!!.itemCount == 0) {
            tv_empty!!.visibility = View.VISIBLE
        } else if (tv_empty!!.visibility == View.VISIBLE) {
            tv_empty!!.visibility = View.GONE
        }
    }

    fun newNote(view: View?) {
        searchVisibility(false)
        startActivity(getStartIntent(this, ""))
    }
}