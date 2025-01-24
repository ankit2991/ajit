package com.messaging.textrasms.manager.mapper

import android.database.Cursor
import com.messaging.textrasms.manager.model.Conversation

interface CursorToConversation : Mapper<Cursor, Conversation> {

    fun getConversationsCursor(): Cursor?

}