package com.messaging.textrasms.manager.listener

import io.reactivex.Observable

interface ContactAddedListener {

    fun listen(): Observable<*>

}
