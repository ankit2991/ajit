package com.messaging.textrasms.manager.manager

interface PermissionManager {

    fun isDefaultSms(): Boolean


    fun hasReadSms(): Boolean

    fun hasSendSms(): Boolean

    fun hasContacts(): Boolean

    fun hasPhone(): Boolean

    fun hasCalling(): Boolean

    fun hasStorage(): Boolean

    fun hasLocation1(): Boolean

    fun hasLocation2(): Boolean

}