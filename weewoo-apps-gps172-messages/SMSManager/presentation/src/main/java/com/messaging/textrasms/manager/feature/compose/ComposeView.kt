package com.messaging.textrasms.manager.feature.compose

import android.net.Uri
import androidx.core.view.inputmethod.InputContentInfoCompat
import com.messaging.textrasms.manager.common.base.QkView
import com.messaging.textrasms.manager.model.Attachment
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.model.Recipient
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface ComposeView : QkView<ComposeState> {

    val activityVisibleIntent: Observable<Boolean>
    val chipsSelectedIntent: Subject<HashMap<String, String?>>
    val chipDeletedIntent: Subject<Recipient>
    val menuReadyIntent: Observable<Unit>
    val optionsItemIntent: Observable<Int>
    val sendAsGroupIntent: Observable<*>
    val messageClickIntent: Subject<Long>
    val messagePartClickIntent: Subject<Long>
    val messagesSelectedIntent: Observable<List<Long>>
    val cancelSendingIntent: Subject<Long>
    val attachmentDeletedIntent: Subject<Attachment>
    val textChangedIntent: Observable<CharSequence>
    val attachIntent: Observable<Unit>

    //    val attachIntentremove:Observable<Boolean>
    val reciepnt: Observable<*>
    val attachgalleryclick: Observable<Unit>
    val attachstickerclick: Observable<Unit>
    val attachgifclick: Observable<Unit>

    val notesIntent: Observable<*>
    val locationIntent: Observable<*>
    val cameraIntent: Observable<*>
    val galleryIntent: Observable<*>

    val scheduleIntent: Observable<*>
    val attachContactIntent: Observable<*>
    val attachmentSelectedIntent: Observable<Uri>
    val attachmentrecentSelectedIntent: Observable<Uri>

    val attachmentSelectedrecentIntent: Observable<Uri>

    val contactSelectedIntent: Observable<Uri>
    val inputContentIntent: Observable<InputContentInfoCompat>
    val scheduleSelectedIntent: Observable<Long>
    val scheduleCancelIntent: Observable<*>
    val changeSimIntent: Observable<*>
    val emojiintent: Observable<*>
    val sendIntent: Observable<Unit>
    val backPressedIntent: Observable<Unit>
    val isvaluestore: Observable<Boolean>
    val isvaluestoresticker: Observable<Boolean>

    fun requestLocation()
    fun requestLocationPermission()
    fun requestNotes()
    fun clearSelection()
    fun showDetails(message: Message)
    fun requestDefaultSms()
    fun requestStoragePermission(requestcode: Int)
    fun requestSmsPermission()
    fun showContacts(sharing: Boolean, chips: List<Recipient>)
    fun showContactsgroup(sharing: Boolean, chips: List<Recipient>)
    fun themeChanged()
    fun showKeyboard()
    fun hideKeyboard()
    fun requestCamera()
    fun requestGallery()
    fun requestDatePicker()
    fun requestContact()
    fun requestrecentpic()
    fun setDraft(draft: String)
    fun scrollToMessage(id: Long)
    fun selctmultiple(value: Boolean)
    fun requestgif()
    fun requestSticker()
}