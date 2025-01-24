package com.translate.languagetranslator.freetranslation.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RemoteAdDetails(
    @SerializedName("show")
    val show: Boolean = false,
    @SerializedName("priority")
    val priority: String = "fa",
    @SerializedName("counter")
    val counter: Int = 0
)