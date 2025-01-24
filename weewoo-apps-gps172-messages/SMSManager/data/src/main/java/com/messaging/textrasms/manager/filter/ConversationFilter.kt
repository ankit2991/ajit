package com.messaging.textrasms.manager.filter

import com.messaging.textrasms.manager.model.Conversation
import javax.inject.Inject

class ConversationFilter @Inject constructor(private val recipientFilter: RecipientFilter) :
    Filter<Conversation>() {

    override fun filter(item: Conversation, query: CharSequence): Boolean {
        return item.recipients.any { recipient -> recipientFilter.filter(recipient, query) }
    }

}