package com.gpsnavigation.maps.gpsroutefinder.routemap.models

import androidx.annotation.Keep
import java.io.Serializable

@Keep
class LocaleLanguageModel : Serializable {

    public var name: String = ""
    public var code: String = ""
    public var status: String = ""

    constructor(name: String, code: String) {
        this.name = name
        this.code = code
    }

    constructor(name: String, code: String, status: String) {
        this.name = name
        this.code = code
        this.status = status
    }

}