package com.messaging.textrasms.manager.manager

interface AnalyticsManager {

    fun track(event: String, vararg properties: Pair<String, Any>)

    fun setUserProperty(key: String, value: Any)

}