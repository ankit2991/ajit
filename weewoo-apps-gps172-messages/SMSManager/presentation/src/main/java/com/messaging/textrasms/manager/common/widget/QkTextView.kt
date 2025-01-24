package com.messaging.textrasms.manager.common.widget

import android.content.Context
//import android.os.AsyncTask
import kotlinx.coroutines.*
import android.util.AttributeSet
import androidx.emoji.widget.EmojiAppCompatTextView
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.TextViewStyler
import com.messaging.textrasms.manager.injection.appComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

//this function perform in background kotlin

open class QkTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : EmojiAppCompatTextView(context, attrs) {

    @Inject
    lateinit var textViewStyler: TextViewStyler

    var collapseEnabled: Boolean = false

    init {
        if (!isInEditMode) {
            appComponent.inject(this)
            textViewStyler.applyAttributes(this, attrs)
        } else {
            TextViewStyler.applyEditModeAttributes(this, attrs)
            TextViewStyler.setCustomFont(context, context.getString(R.string.fontName), this)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (collapseEnabled) {
            // Using Coroutines to perform background work
            CoroutineScope(Dispatchers.Default).launch {
                val collapsedText = calculateCollapsedText()

                // Update UI on the main thread
                withContext(Dispatchers.Main) {
                    text = collapsedText
                }
            }
        }
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        setLinkTextColor(color)
    }

    private suspend fun calculateCollapsedText(): String {
        return withContext(Dispatchers.Default) {
            layout
                ?.takeIf { layout -> layout.lineCount > 0 }
                ?.let { layout -> layout.getEllipsisCount(layout.lineCount - 1) }
                ?.takeIf { ellipsisCount -> ellipsisCount > 0 }
                ?.let { ellipsisCount -> text.dropLast(ellipsisCount).lastIndexOf(',') }
                ?.takeIf { lastComma -> lastComma >= 0 }
                ?.let { lastComma ->
                    val remainingNames = text.drop(lastComma).count { c -> c == ',' }
                    "${text.take(lastComma)}, +$remainingNames"
                } ?: text.toString()
        }
    }
}


//open class QkTextView @JvmOverloads constructor(
//    context: Context, attrs: AttributeSet? = null
//) : EmojiAppCompatTextView(context, attrs) {
//
//    @Inject
//    lateinit var textViewStyler: TextViewStyler
//
//    var collapseEnabled: Boolean = false
//
//    init {
//        if (!isInEditMode) {
//            appComponent.inject(this)
//            textViewStyler.applyAttributes(this, attrs)
//        } else {
//            TextViewStyler.applyEditModeAttributes(this, attrs)
//            TextViewStyler.setCustomFont(context, context.getString(R.string.fontName), this)
//        }
//    }
//
//    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//        super.onLayout(changed, left, top, right, bottom)
//
//        if (collapseEnabled) {
//            layout
//                ?.takeIf { layout -> layout.lineCount > 0 }
//                ?.let { layout -> layout.getEllipsisCount(layout.lineCount - 1) }
//                ?.takeIf { ellipsisCount -> ellipsisCount > 0 }
//                ?.let { ellipsisCount -> text.dropLast(ellipsisCount).lastIndexOf(',') }
//                ?.takeIf { lastComma -> lastComma >= 0 }
//                ?.let { lastComma ->
//                    val remainingNames = text.drop(lastComma).count { c -> c == ',' }
//                    text = "${text.take(lastComma)}, +$remainingNames"
//                }
//        }
//    }
//
//    override fun setTextColor(color: Int) {
//        super.setTextColor(color)
//        setLinkTextColor(color)
//    }
//
//}