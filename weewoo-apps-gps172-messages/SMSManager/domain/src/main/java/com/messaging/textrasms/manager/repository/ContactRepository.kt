package com.messaging.textrasms.manager.repository

import android.net.Uri
import com.messaging.textrasms.manager.model.Contact
import com.messaging.textrasms.manager.model.ContactGroup
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.RealmResults

interface ContactRepository {

    fun findContactUri(address: String): Single<Uri>

    fun getContacts(): RealmResults<Contact>

    fun getUnmanagedContact(lookupKey: String): Contact?

    fun getUnmanagedContacts(starred: Boolean = false): Observable<List<Contact>>

    fun getUnmanagedContactGroups(): Observable<List<ContactGroup>>

    fun setDefaultPhoneNumber(lookupKey: String, phoneNumberId: Long)

}
