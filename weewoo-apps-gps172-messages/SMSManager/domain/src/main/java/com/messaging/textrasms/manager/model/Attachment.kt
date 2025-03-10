package com.messaging.textrasms.manager.model

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.view.inputmethod.InputContentInfoCompat

sealed class Attachment {

    data class Image(
        private val uri: Uri? = null,
        private val inputContent: InputContentInfoCompat? = null
    ) : Attachment() {


        fun getUri(): Uri? {
            logDebug("attach" + uri)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                inputContent?.contentUri ?: uri
            } else {
                uri

            }

        }

        fun isGif(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && inputContent != null) {
                inputContent.description.hasMimeType("image/gif")
            } else {
                uri?.let(context.contentResolver::getType) == "image/gif"
            }
        }
    }

    data class Contact(val vCard: String) : Attachment()

}

class Attachments(attachments: List<Attachment>) : List<Attachment> by attachments

var mDebugLog = true
var mDebugTag = "datasync"
fun logDebug(msg: String) {
    if (mDebugLog) Log.d(mDebugTag, msg)
}