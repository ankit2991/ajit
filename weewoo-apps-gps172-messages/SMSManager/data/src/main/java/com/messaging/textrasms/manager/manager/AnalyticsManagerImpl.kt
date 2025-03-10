package com.messaging.textrasms.manager.manager

import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManagerImpl @Inject constructor() : AnalyticsManager {

    override fun track(event: String, vararg properties: Pair<String, Any>) {
        JSONObject(properties
            .associateBy { pair -> pair.first }
            .mapValues { pair -> pair.value.second })
            .also { Timber.v("$event: $it") }
    }

    override fun setUserProperty(key: String, value: Any) {
        Timber.v("$key: $value")
    }

}