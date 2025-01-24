package com.messaging.textrasms.manager.mapper

import android.database.Cursor
import com.messaging.textrasms.manager.model.Recipient

interface CursorToRecipient : Mapper<Cursor, Recipient> {

    fun getRecipientCursor(): Cursor?

    fun getRecipientCursor(id: Long): Cursor?

}