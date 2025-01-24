package com.messaging.textrasms.manager.mapper

import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.model.Recipient
import javax.inject.Inject

class CursorToRecipientImpl @Inject constructor(
    private val context: Context,
    private val permissionManager: PermissionManager
) : CursorToRecipient {

    companion object {
        val URI = Uri.parse("content://mms-sms/canonical-addresses")

        const val COLUMN_ID = 0
        const val COLUMN_ADDRESS = 1
    }

    override fun map(from: Cursor) = Recipient(
        id = from.getLong(COLUMN_ID),
        address = from.getString(COLUMN_ADDRESS),
        lastUpdate = System.currentTimeMillis()
    )

    override fun getRecipientCursor(): Cursor? {
        return when (permissionManager.hasReadSms()) {
            true -> context.contentResolver.query(URI, null, null, null, null)
            false -> null
        }
    }

    override fun getRecipientCursor(id: Long): Cursor? {
        return context.contentResolver.query(URI, null, "_id = ?", arrayOf(id.toString()), null)
    }

}