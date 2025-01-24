package com.messaging.textrasms.manager.mapper

import android.database.Cursor
import com.messaging.textrasms.manager.model.Contact

interface CursorToContact : Mapper<Cursor, Contact> {

    fun getContactsCursor(): Cursor?

}