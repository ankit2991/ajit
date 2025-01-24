package com.messaging.textrasms.manager.feature.contacts

import com.messaging.textrasms.manager.common.base.QkView
import com.messaging.textrasms.manager.extensions.Optional
import com.messaging.textrasms.manager.feature.compose.editing.ComposeItem
import com.messaging.textrasms.manager.feature.compose.editing.PhoneNumberAction
import com.messaging.textrasms.manager.model.Recipient
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface ContactsContract : QkView<ContactsState> {

    val queryChangedIntent: Observable<CharSequence>
    val queryClearedIntent: Observable<*>
    val groupIntent: Observable<*>
    val queryEditorActionIntent: Observable<Int>
    val composeItemPressedIntent: Subject<ComposeItem>
    val composeItemLongPressedIntent: Subject<ComposeItem>
    val phoneNumberSelectedIntent: Subject<Optional<Long>>
    val phoneNumberActionIntent: Subject<PhoneNumberAction>
    val chipsSelectedIntent: Subject<HashMap<String, String?>>
    val chipDeletedIntent: Subject<Recipient>
    val groupselection: Observable<*>
    val createcontact: Observable<*>

    fun clearQuery()
    fun openKeyboard()
    fun finish(result: HashMap<String, String?>, from: Boolean)
    fun showContacts(sharing: Boolean, chips: List<Recipient>)
    fun createcontact()
    fun removecontact(composeItems: String)


}
