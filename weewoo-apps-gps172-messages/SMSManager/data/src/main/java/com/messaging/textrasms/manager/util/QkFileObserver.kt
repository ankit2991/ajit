package com.messaging.textrasms.manager.util

import android.os.FileObserver
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.io.File

class QkFileObserver(path: String) : FileObserver(path, CREATE or DELETE or MODIFY) {

    private val subject = BehaviorSubject.createDefault<String>(path).toSerialized()

    val observable: Observable<String> = subject
        .doOnSubscribe { startWatching() }
        .doOnDispose { stopWatching() }
        .share()

    init {
        tryOrNull { File(path).mkdirs() }
    }

    override fun onEvent(event: Int, path: String?) {
        path?.let(subject::onNext)
    }

}