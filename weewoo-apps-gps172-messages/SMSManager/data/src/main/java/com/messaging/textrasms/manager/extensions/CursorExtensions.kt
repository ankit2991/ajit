package com.messaging.textrasms.manager.extensions

import android.database.Cursor
import io.reactivex.Flowable

fun Cursor.forEach(closeOnComplete: Boolean = true, method: (Cursor) -> Unit = {}) {
    moveToPosition(-1)
    while (moveToNext()) {
        method.invoke(this)
    }

    if (closeOnComplete) {
        close()
    }
}

fun <T> Cursor.map(map: (Cursor) -> T): List<T> {
    return List(count) { position ->
        moveToPosition(position)
        map(this)
    }
}

fun Cursor.asFlowable(): Flowable<Cursor> {
    return Flowable.range(0, count)
        .map {
            moveToPosition(it)
            this
        }
        .doOnComplete { close() }
}

fun Cursor.dump(): String {
    val lines = mutableListOf<String>()

    lines += columnNames.joinToString(",")
    forEach { lines += (0 until columnCount).joinToString(",", transform = ::getString) }

    return lines.joinToString("\n")
}
