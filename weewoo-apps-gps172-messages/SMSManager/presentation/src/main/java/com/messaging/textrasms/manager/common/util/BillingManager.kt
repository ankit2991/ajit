package com.messaging.textrasms.manager.common.util

import android.content.Context
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(context: Context) {
    val upgradeStatus = Observable.just(true)
}