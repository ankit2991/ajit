package com.messaging.textrasms.manager.feature.gallery

import com.messaging.textrasms.manager.model.MmsPart
import io.realm.RealmResults

data class GalleryState(
    val navigationVisible: Boolean = true,
    val title: String? = "",
    val parts: RealmResults<MmsPart>? = null
)
