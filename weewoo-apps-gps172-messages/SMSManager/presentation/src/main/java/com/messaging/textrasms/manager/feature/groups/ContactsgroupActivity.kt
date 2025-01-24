package com.messaging.textrasms.manager.feature.groups

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActions
import com.jakewharton.rxbinding2.widget.textChanges
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.util.ViewModelFactory
import com.messaging.textrasms.manager.common.util.extensions.hideKeyboard
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.common.util.extensions.showKeyboard
import com.messaging.textrasms.manager.common.util.extensions.showKeyboardnumber
import com.messaging.textrasms.manager.common.widget.QkDialog
import com.messaging.textrasms.manager.extensions.Optional
import com.messaging.textrasms.manager.feature.Activities.MainActivity
import com.messaging.textrasms.manager.feature.compose.editing.*
import com.messaging.textrasms.manager.feature.contacts.ContactsActivity
import com.messaging.textrasms.manager.feature.contacts.ContactsContract
import com.messaging.textrasms.manager.feature.contacts.ContactsState
import com.messaging.textrasms.manager.model.Recipient
import com.messaging.textrasms.manager.model.logDebug
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.groupcontact_layout.*
import javax.inject.Inject

class ContactsgroupActivity : QkThemedActivity(), ContactsContract {
    override fun createcontact() {
        TODO("not implemented")
    }

    override val createcontact: Observable<*>
        get() = TODO("not implemented")


    override val groupselection: Observable<*>
        get() = TODO("not implemented")

    companion object {
        const val SharingKeygroup = "sharing"
        const val ChipsKeygroup = "chips"
        private const val SelectContactRequestCode = 0

    }

    override fun removecontact(composeItems: String) {
        contactsAdapter.removeitem(composeItems)
    }

    override fun showContacts(sharing: Boolean, chips: List<Recipient>) {
        val serialized =
            HashMap(chips.associate { chip -> chip.address to chip.contact?.lookupKey })
        val intent = Intent(this, ContactsgroupActivity::class.java)
            .putExtra(ContactsgroupActivity.SharingKeygroup, sharing)
            .putExtra(ContactsgroupActivity.ChipsKeygroup, serialized)
        startActivityForResult(intent, SelectContactRequestCode)
    }

    @Inject
    lateinit var contactsAdapter: ComposeItemAdapter

    @Inject
    lateinit var phoneNumberAdapter: PhoneNumberPickerAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var chipsAdapter: ChipsAdapter
    override val queryChangedIntent: Observable<CharSequence> by lazy { search.textChanges() }
    override val queryClearedIntent: Observable<*> by lazy { cancel.clicks() }
    override val groupIntent: Observable<*> by lazy { next.clicks() }
    override val queryEditorActionIntent: Observable<Int> by lazy { search.editorActions() }
    override val composeItemPressedIntent: Subject<ComposeItem> by lazy { contactsAdapter.clicks }
    override val composeItemLongPressedIntent: Subject<ComposeItem> by lazy { contactsAdapter.longClicks }
    override val phoneNumberSelectedIntent: Subject<Optional<Long>> by lazy { phoneNumberAdapter.selectedItemChanges }
    override val phoneNumberActionIntent: Subject<PhoneNumberAction> = PublishSubject.create()
    override val chipsSelectedIntent: Subject<HashMap<String, String?>> = PublishSubject.create()
    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[ContactsGroupViewModel::class.java]
    }
    override val chipDeletedIntent: Subject<Recipient> by lazy { chipsAdapter.chipDeleted }
    private var keyboardselected: Boolean = false
    private val phoneNumberDialog by lazy {
        QkDialog(this).apply {
            titleRes = R.string.compose_number_picker_title
            adapter = phoneNumberAdapter
            positiveButton = R.string.compose_number_picker_always
            positiveButtonListener = { phoneNumberActionIntent.onNext(PhoneNumberAction.ALWAYS) }
            negativeButton = R.string.compose_number_picker_once
            negativeButtonListener = { phoneNumberActionIntent.onNext(PhoneNumberAction.JUST_ONCE) }
            cancelListener = { phoneNumberActionIntent.onNext(PhoneNumberAction.CANCEL) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.groupcontact_layout)
        viewModel.bindView(this)
        contacts.adapter = contactsAdapter
        chipsAdapter.view = chips
        chips.adapter = chipsAdapter
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        chips.layoutManager = layoutManager
        back.setOnClickListener(View.OnClickListener {
            onBackPressed()
        })


        keyboard.setOnClickListener(View.OnClickListener {
            if (!keyboardselected) {
                keyboard.setImageResource(R.drawable.ic_keyboard)
                search.postDelayed({
                    search.showKeyboardnumber()
                }, 200)
                keyboardselected = true
            } else {
                keyboard.setImageResource(R.drawable.ic_dial)
                search.postDelayed({
                    search.showKeyboard()
                }, 200)
                keyboardselected = false
            }
        })
    }

    override fun render(state: ContactsState) {
        if (state.hasError) {

            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
//            return
        }
        cancel.isVisible = state.query.length > 1
        next.isVisible = state.selectedChips.size != 0
        view.setVisible(state.selectedChips.size != 0)
        chips.setVisible(state.selectedChips.size != 0)
        toolbarTitle.text = when {
            state.selectedChips.size > 0 -> getString(
                R.string.contact_selected, state.selectedChips.size,
                state.composeItems.size
            )
            else -> "Select Reciepnt"
        }
        contactsAdapter.data = state.composeItems
        chipsAdapter.data = state.selectedChips
        if (state.selectedContact != null && !phoneNumberDialog.isShowing) {
            phoneNumberAdapter.data = state.selectedContact.numbers
            phoneNumberDialog.subtitle = state.selectedContact.name
            if (!isFinishing && phoneNumberDialog != null) {
                phoneNumberDialog.show()
            }
        } else if (!isFinishing && state.selectedContact == null && phoneNumberDialog.isShowing) {
            phoneNumberDialog.dismiss()
        }
    }

    override fun clearQuery() {
        search.text = null
    }

    override fun openKeyboard() {
        search.postDelayed({
            search.showKeyboard()
        }, 200)
    }

    override fun finish(result: HashMap<String, String?>, from: Boolean) {
        runOnUiThread(Runnable {
            search.hideKeyboard()
            chipsSelectedIntent.onNext(
                result
                    .let { serializable -> serializable }
                    ?: hashMapOf())
            logDebug("chipkeys" + result)
            val intent = Intent().putExtra(ChipsKeygroup, result)
            setResult(Activity.RESULT_OK, intent)
            if (from) {
                finish()
            }

        })
    }

    override fun onBackPressed() {
        startActivity(Intent(this, ContactsActivity::class.java))
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}