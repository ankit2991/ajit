package com.messaging.textrasms.manager.feature.settings.swipe

import androidx.annotation.DrawableRes
import com.messaging.textrasms.manager.R

data class SwipeActionsState(
    @DrawableRes val rightIcon: Int = R.drawable.ic_archive_black_24dp,
    val rightLabel: String = "",

    @DrawableRes val leftIcon: Int = R.drawable.ic_archive_black_24dp,
    val leftLabel: String = ""
)