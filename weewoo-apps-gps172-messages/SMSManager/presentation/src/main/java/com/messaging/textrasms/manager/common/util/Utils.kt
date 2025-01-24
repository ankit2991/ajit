package com.messaging.textrasms.manager.common.util

import android.content.Context
import android.util.Log
import com.messaging.textrasms.manager.util.Preferences
import javax.inject.Inject

class Utils @Inject constructor(
    private val context: Context,
    private val prefs: Preferences
) {

    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }


}

