package com.messaging.textrasms.manager.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ContactGroup(
    @PrimaryKey var id: Long = 0,
    var title: String = "",
    var contacts: RealmList<Contact> = RealmList()
) : RealmObject()
