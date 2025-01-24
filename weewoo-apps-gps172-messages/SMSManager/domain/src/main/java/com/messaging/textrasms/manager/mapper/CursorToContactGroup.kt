package com.messaging.textrasms.manager.mapper

import android.database.Cursor
import com.messaging.textrasms.manager.model.ContactGroup

interface CursorToContactGroup : Mapper<Cursor, ContactGroup> {

    fun getContactGroupsCursor(): Cursor?

}
