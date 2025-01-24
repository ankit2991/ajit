package com.messaging.textrasms.manager.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class FilterBlockedNumber(
    @PrimaryKey var id: Long = 0,

    var address: String = "",
    var content: Boolean = false,
    var sender: Boolean = false
) : RealmObject()
