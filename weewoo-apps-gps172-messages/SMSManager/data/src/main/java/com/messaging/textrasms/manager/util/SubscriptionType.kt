package com.messaging.textrasms.manager.util

import android.util.Log

enum class SubscriptionType(val value: String) {
    weekly_sub("com.messaging.textrasms.manager_2.99"),
    monthly_sub("com.messaging.textrasms.manager_9.99"),
    yearly_sub("com.messaging.textrasms.manager_29.99");

    companion object {
        fun fromNameToValue(name: String): String =
            if (values().firstOrNull { it.name == name.trim() } != null)
                values().first { it.name == name.trim() }.value else yearly_sub.value
    }
}