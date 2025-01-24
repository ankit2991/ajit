package com.messaging.textrasms.manager.manager

interface WidgetManager {

    companion object {
        const val ACTION_NOTIFY_DATASET_CHANGED =
            "com.messaging.textrasms.manager.intent.action.ACTION_NOTIFY_DATASET_CHANGED"
    }

    fun updateUnreadCount()

    fun updateTheme()

}