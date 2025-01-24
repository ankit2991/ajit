package com.klinker.android.send_message

class Settings @JvmOverloads constructor(
    // MMS options
    var mmsc: String? = "",
    var proxy: String? = "",
    var port: String? = "0",
    var agent: String? = "",
    var userProfileUrl: String? = "",
    var uaProfTagName: String? = "",

    // SMS options
    var stripUnicode: Boolean = false
)
