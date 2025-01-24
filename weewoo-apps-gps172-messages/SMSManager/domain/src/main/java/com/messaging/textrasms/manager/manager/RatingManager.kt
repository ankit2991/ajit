package com.messaging.textrasms.manager.manager

import io.reactivex.Observable

interface RatingManager {

    val shouldShowRating: Observable<Boolean>

    fun addSession()

    fun rate()

    fun dismiss()

}