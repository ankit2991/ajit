package com.messaging.textrasms.manager.feature.simplenotes

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.util.extensions.setBackgroundTint
import com.messaging.textrasms.manager.feature.simplenotes.NoteActivity
import com.messaging.textrasms.manager.util.resolveThemeColor
import com.messaging.textrasms.manager.util.tryOrNull
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_note.*

class NoteActivity : QkThemedActivity() {

    private var title: String? = ""
    private var note = ""
    private var dialog: AlertDialog? = null
    private var saveBtn: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        showBackButton(true)
        toolbarTitle.setTextColor(resolveThemeColor(android.R.attr.textColorPrimary))
        if (Build.VERSION.SDK_INT <= 22) {
            titleText.setBackgroundTint(resolveThemeColor(R.attr.messageBackground))
        }
        title = intent.getStringExtra(EXTRA_NOTE_TITLE)
        if (title == null || TextUtils.isEmpty(title)) {
            titleText.requestFocus()
            toolbarTitle.text = getString(R.string.new_note)
        } else {
            titleText.setText(title)
            titleText.setSelection(title!!.length)
            note = HelperUtils.readFile(this@NoteActivity, title)
            noteText.setText(note)
            toolbarTitle.text = title
            titleText.requestFocus()
        }
        titleText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                tryOrNull { saveBtn!!.isVisible = isVisibleSave }

            }
        })
        noteText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                tryOrNull { saveBtn!!.isVisible = isVisibleSave }

            }
        })
    }


    public override fun onPause() {
        if (!isChangingConfigurations) {
            saveFile()
        }
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
        dialog = null
        super.onPause()
    }

    private val isVisibleSave: Boolean
        get() = !TextUtils.isEmpty(
            titleText.text.toString().trim { it <= ' ' }) || !TextUtils.isEmpty(
            noteText.text.toString().trim { it <= ' ' })

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_note, menu)
        saveBtn = menu!!.findItem(R.id.btn_save)
        saveBtn!!.isVisible = isVisibleSave
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
            R.id.btn_save -> {
                onPause()
                onBackPressed()
            }
            R.id.btn_undo -> {
                noteText.setText(note)
                noteText.setSelection(noteText.text!!.length)
                return true
            }
            R.id.btn_delete -> {
                dialog = AlertDialog.Builder(this@NoteActivity)
                    .setTitle(getString(R.string.confirm_delete))
                    .setMessage(getString(R.string.confirm_delete_text))
                    .setPositiveButton(getString(android.R.string.yes)) { dialog, which ->
                        if (HelperUtils.fileExists(this@NoteActivity, title)) {
                            deleteFile(title + HelperUtils.TEXT_FILE_EXTENSION)
                        }
                        title = ""
                        note = ""
                        titleText.setText(title)
                        noteText.setText(note)
                        finish()
                    }
                    .setNegativeButton(getString(android.R.string.no)) { dialog, which -> }
                    .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveFile() {
        var newTitle = titleText.text.toString().trim { it <= ' ' }.replace("/", " ")
        val newNote = noteText.text.toString().trim { it <= ' ' }

        if (TextUtils.isEmpty(newTitle) && TextUtils.isEmpty(newNote)) {
            return
        }

        if (newTitle == title && newNote == note) {
            return
        }
        if (title != newTitle || TextUtils.isEmpty(newTitle)) {
            newTitle = newFileName(newTitle)
            titleText.setText(newTitle)
        }
        HelperUtils.writeFile(this@NoteActivity, newTitle, newNote)
        if (!TextUtils.isEmpty(title) && newTitle != title) {
            deleteFile(title + HelperUtils.TEXT_FILE_EXTENSION)
        }
        title = newTitle
        Toast.makeText(this, "Note saved successfully.", Toast.LENGTH_SHORT).show()
    }

    private fun newFileName(name: String): String {
        var name = name
        if (TextUtils.isEmpty(name)) {
            name = getString(R.string.note)
        }
        if (HelperUtils.fileExists(this@NoteActivity, name)) {
            var i = 1
            while (true) {
                if (!HelperUtils.fileExists(
                        this@NoteActivity,
                        "$name ($i)"
                    ) || title == "$name ($i)"
                ) {
                    name = "$name ($i)"
                    break
                }
                i++
            }
        }
        return name
    }

    companion object {
        private const val EXTRA_NOTE_TITLE = "EXTRA_NOTE_TITLE"

        @JvmStatic
        fun getStartIntent(context: Context?, title: String?): Intent {
            val intent = Intent(context, NoteActivity::class.java)
            intent.putExtra(EXTRA_NOTE_TITLE, title)
            return intent
        }
    }
}