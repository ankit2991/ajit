package com.messaging.textrasms.manager.manager

interface KeyManager {

    fun reset()
    fun newId(): Long

}