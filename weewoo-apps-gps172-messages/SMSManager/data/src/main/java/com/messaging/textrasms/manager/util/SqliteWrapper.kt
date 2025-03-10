package com.messaging.textrasms.manager.util

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import timber.log.Timber

object SqliteWrapper {

    fun query(
        context: Context,
        uri: Uri,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null,
        logError: Boolean = true
    ): Cursor? {
        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        } catch (e: SQLiteException) {
            if (logError) {
                Timber.e(e)
            }
            null
        }

    }

}