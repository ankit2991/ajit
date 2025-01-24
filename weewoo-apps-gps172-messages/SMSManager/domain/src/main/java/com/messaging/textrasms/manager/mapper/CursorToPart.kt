package com.messaging.textrasms.manager.mapper

import android.database.Cursor
import com.messaging.textrasms.manager.model.MmsPart

interface CursorToPart : Mapper<Cursor, MmsPart> {

    fun getPartsCursor(messageId: Long): Cursor?

}