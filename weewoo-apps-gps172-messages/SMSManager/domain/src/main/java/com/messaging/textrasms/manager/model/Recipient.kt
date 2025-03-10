package com.messaging.textrasms.manager.model

import android.telephony.PhoneNumberUtils
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Recipient(
    @PrimaryKey var id: Long = 0,
    var address: String = "",
    var contact: Contact? = null,
    var lastUpdate: Long = 0
) : RealmObject() {

    fun getDisplayName(): String = contact?.name?.takeIf { it.isNotBlank() }
        ?: PhoneNumberUtils.formatNumber(address, Locale.getDefault().country)
        ?: address

    fun getaddress(): String = address
}
