package com.messaging.textrasms.manager.common.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import com.google.android.mms.ContentType
import com.messaging.textrasms.manager.common.util.TextViewStyler
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.util.tryOrNull
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import javax.inject.Inject


/**
 * Custom implementation of EditText to allow for dynamic text colors
 *
 * Beware of updating to extend AppCompatTextView, as this inexplicably breaks the view in
 * the contacts chip view
 */
class QkEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    EditText(context, attrs) {

    @Inject
    lateinit var textViewStyler: TextViewStyler

    val backspaces: Subject<Unit> = PublishSubject.create()
    val inputContentSelected: Subject<InputContentInfoCompat> = PublishSubject.create()
    var supportsInputContent: Boolean = false

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
            textViewStyler.applyAttributes(this, attrs)
        } else {
            TextViewStyler.applyEditModeAttributes(this, attrs)
        }
    }

    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection {

        val inputConnection =
            object : InputConnectionWrapper(super.onCreateInputConnection(editorInfo), true) {
                override fun sendKeyEvent(event: KeyEvent): Boolean {
                    if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL) {
                        backspaces.onNext(Unit)
                    }
                    return super.sendKeyEvent(event)
                }


                override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                    if (beforeLength == 1 && afterLength == 0) {
                        backspaces.onNext(Unit)
                    }
                    return super.deleteSurroundingText(beforeLength, afterLength)
                }
            }

        if (supportsInputContent) {
            EditorInfoCompat.setContentMimeTypes(
                editorInfo, arrayOf(
                    ContentType.IMAGE_JPEG,
                    ContentType.IMAGE_JPG,
                    ContentType.IMAGE_PNG,
                    ContentType.IMAGE_GIF
                )
            )
        }

        val callback =
            InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, opts ->
                val grantReadPermission =
                    flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && grantReadPermission) {
                    return@OnCommitContentListener tryOrNull {
                        inputContentInfo.requestPermission()
                        inputContentSelected.onNext(inputContentInfo)
                        true
                    } ?: false

                }

                true
            }

        return InputConnectionCompat.createWrapper(inputConnection, editorInfo, callback)
    }

}