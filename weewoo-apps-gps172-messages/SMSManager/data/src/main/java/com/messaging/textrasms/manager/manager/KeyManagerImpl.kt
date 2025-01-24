package com.messaging.textrasms.manager.manager

import com.messaging.textrasms.manager.model.Message
import io.realm.Realm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyManagerImpl @Inject constructor() : KeyManager {

    private var initialized = false
    private var maxValue: Long = 0

    override fun reset() {
        initialized = true
        maxValue = 0L
    }

    override fun newId(): Long {
        if (!initialized) {
            maxValue = Realm.getDefaultInstance().use { realm ->
                realm.where(Message::class.java).max("id")?.toLong() ?: 0L
            }
            initialized = true
        }

        maxValue++
        return maxValue
    }

}