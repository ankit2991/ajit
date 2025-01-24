package com.messaging.textrasms.manager.filter

import com.messaging.textrasms.manager.extensions.removeAccents
import com.messaging.textrasms.manager.model.Contact
import javax.inject.Inject

class ContactFilter @Inject constructor(private val phoneNumberFilter: PhoneNumberFilter) :
    Filter<Contact>() {

    override fun filter(item: Contact, query: CharSequence): Boolean {
        return item.name.removeAccents().contains(query, true) ||
                item.numbers.map { it.address }
                    .any { address -> phoneNumberFilter.filter(address, query) }
    }

}
