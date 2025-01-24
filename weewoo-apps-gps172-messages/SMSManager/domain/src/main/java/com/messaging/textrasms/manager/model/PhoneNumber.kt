package com.messaging.textrasms.manager.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PhoneNumber(
    @PrimaryKey var id: Long = 0,
    var accountType: String? = "",
    var address: String = "",
    var type: String = "",
    var isDefault: Boolean = false
) : RealmObject()
