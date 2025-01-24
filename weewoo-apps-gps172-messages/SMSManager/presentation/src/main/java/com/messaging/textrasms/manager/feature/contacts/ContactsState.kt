package com.messaging.textrasms.manager.feature.contacts

import com.messaging.textrasms.manager.feature.compose.editing.ComposeItem
import com.messaging.textrasms.manager.model.Contact
import com.messaging.textrasms.manager.model.Recipient

data class ContactsState(
    val query: String = "",
    val composeItems: List<ComposeItem> = ArrayList(),
    val selectedChips: List<Recipient> = ArrayList(),
    val selectedContact: Contact? = null, // For phone number picker
    val hasError: Boolean = false,
    val attaching: Boolean = false

)
