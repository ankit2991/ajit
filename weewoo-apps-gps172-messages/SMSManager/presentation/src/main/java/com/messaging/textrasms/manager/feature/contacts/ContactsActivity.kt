package com.messaging.textrasms.manager.feature.contacts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.editorActions
import com.jakewharton.rxbinding2.widget.textChanges
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkThemedActivity
import com.messaging.textrasms.manager.common.util.ViewModelFactory
import com.messaging.textrasms.manager.common.util.extensions.hideKeyboard
import com.messaging.textrasms.manager.common.util.extensions.showKeyboard
import com.messaging.textrasms.manager.common.util.extensions.showKeyboardnumber
import com.messaging.textrasms.manager.common.widget.QkDialog
import com.messaging.textrasms.manager.extensions.Optional
import com.messaging.textrasms.manager.feature.compose.editing.ComposeItem
import com.messaging.textrasms.manager.feature.compose.editing.ComposeItemAdapter
import com.messaging.textrasms.manager.feature.compose.editing.PhoneNumberAction
import com.messaging.textrasms.manager.feature.compose.editing.PhoneNumberPickerAdapter
import com.messaging.textrasms.manager.model.Recipient
import com.messaging.textrasms.manager.model.logDebug
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.contacts_activity.*
import javax.inject.Inject

class ContactsActivity : QkThemedActivity(), ContactsContract {
    override fun removecontact(composeItems: String) {
        TODO("not implemented")
    }

    override val groupIntent: Observable<*>
        get() = TODO("not implemented")
    override val chipsSelectedIntent: Subject<HashMap<String, String?>>
        get() = TODO("not implemented")
    override val chipDeletedIntent: Subject<Recipient>
        get() = TODO("not implemented")

    companion object {
        const val SharingKey = "sharing"
        const val ChipsKey = "chips"
        private const val SelectContactRequestCode = 0

    }

    private var keyboardselected: Boolean = false
    override fun showContacts(sharing: Boolean, chips: List<Recipient>) {
        val serialized =
            HashMap(chips.associate { chip -> chip.address to chip.contact?.lookupKey })
        val intent = Intent(this, ContactsActivity::class.java)
            .putExtra(ContactsActivity.SharingKey, sharing)
            .putExtra(ContactsActivity.ChipsKey, serialized)
        startActivityForResult(intent, SelectContactRequestCode)
    }

    @Inject
    lateinit var contactsAdapter: ComposeItemAdapter

    @Inject
    lateinit var phoneNumberAdapter: PhoneNumberPickerAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override val queryChangedIntent: Observable<CharSequence> by lazy { search.textChanges() }
    override val queryClearedIntent: Observable<*> by lazy { cancel.clicks() }
    override val queryEditorActionIntent: Observable<Int> by lazy { search.editorActions() }
    override val composeItemPressedIntent: Subject<ComposeItem> by lazy { contactsAdapter.clicks }
    override val composeItemLongPressedIntent: Subject<ComposeItem> by lazy { contactsAdapter.longClicks }
    override val phoneNumberSelectedIntent: Subject<Optional<Long>> by lazy { phoneNumberAdapter.selectedItemChanges }
    override val phoneNumberActionIntent: Subject<PhoneNumberAction> = PublishSubject.create()
    override val groupselection: Observable<*> by lazy {
        Observable.merge(
            new_group_layout.clicks(),
            new_group.clicks(),
            group_img.clicks()
        )
    }
    private val viewModel by lazy {
        ViewModelProviders.of(
            this,
            viewModelFactory
        )[ContactsViewModel::class.java]
    }
    override val createcontact: Observable<*> by lazy {
        Observable.merge(
            new_contact_layout.clicks(),
            contact.clicks(),
            contact_img.clicks()
        )
    }

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
        setContentView(R.layout.contacts_activity)
        //  showBackButton(true)
        viewModel.bindView(this)
        contacts.adapter = contactsAdapter
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


        back.setOnClickListener(View.OnClickListener {
            super.onBackPressed()
        })


    }

    override fun createcontact() {
        val intent = Intent(
            Intent.ACTION_INSERT,
            android.provider.ContactsContract.Contacts.CONTENT_URI
        )
        startActivity(intent)
    }

    override fun render(state: ContactsState) {
        cancel.isVisible = state.query.length > 1

        contactsAdapter.data = state.composeItems

        if (state.selectedContact != null && !phoneNumberDialog.isShowing) {
            phoneNumberAdapter.data = state.selectedContact.numbers
            phoneNumberDialog.subtitle = state.selectedContact.name
            phoneNumberDialog.show()
        } else if (state.selectedContact == null && phoneNumberDialog.isShowing) {
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
            logDebug("chipkeys" + result.entries)
            val intent = Intent().putExtra(ChipsKey, result)
            setResult(Activity.RESULT_OK, intent)
            finish()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            // finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}