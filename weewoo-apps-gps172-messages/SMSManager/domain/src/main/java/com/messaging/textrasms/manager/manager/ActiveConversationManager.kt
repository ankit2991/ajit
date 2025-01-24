package com.messaging.textrasms.manager.manager

interface ActiveConversationManager {

    fun setActiveConversation(threadId: Long?)

    fun getActiveConversation(): Long?

}
