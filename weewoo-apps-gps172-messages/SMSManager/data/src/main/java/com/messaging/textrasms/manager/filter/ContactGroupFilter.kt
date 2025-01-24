package com.messaging.textrasms.manager.filter

import com.messaging.textrasms.manager.extensions.removeAccents
import com.messaging.textrasms.manager.model.ContactGroup
import javax.inject.Inject

class ContactGroupFilter @Inject constructor(private val contactFilter: ContactFilter) :
    Filter<ContactGroup>() {

    override fun filter(item: ContactGroup, query: CharSequence): Boolean {
        return item.title.removeAccents().contains(query, true) ||
                item.contacts.any { contact -> contactFilter.filter(contact, query) }
    }

}
