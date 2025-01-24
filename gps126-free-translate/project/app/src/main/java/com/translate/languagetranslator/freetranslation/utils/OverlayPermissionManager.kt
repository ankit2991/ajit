package com.translate.languagetranslator.freetranslation.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.code4rox.adsmanager.MaxAppOpenAdManager
import com.translate.languagetranslator.freetranslation.utils.Constants.isOnHomeScreen


class OverlayPermissionManager(private val activity: Activity) {
    private var thread: Thread? = null
    private var shouldContinueThread = true
    fun requestOverlay() {
        MaxAppOpenAdManager.Companion.IS_OVERLAY_PERMISSION = true
        isOnHomeScreen = false

        shouldContinueThread = true
        sendToSettings()
        startGrantedCheckThread()
    }

    private fun sendToSettings() {
        val intent = Intent(
            ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        activity.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            shouldContinueThread = true
        }
    }

    private fun startGrantedCheckThread() {
        thread = object : Thread() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            override fun run() {
                var counter = 0
                while (!Settings.canDrawOverlays(activity) && shouldContinueThread && counter < LISTEN_TIMEOUT) {
                    try {
                        counter++
                        Log.d(TAG, "run: still no permission")
                        sleep(THREAD_SLEEP_TIME.toLong())
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                if (shouldContinueThread && counter < LISTEN_TIMEOUT) {
                    val intent = Intent(activity, activity.javaClass)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                        activity.startActivity(intent)
                    } else {
                        activity.startActivityIfNeeded(intent, 0)
                    }
                }
            }
        }
        (thread as Thread).start()
    }

    val isGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(activity)
        } else true

    companion object {
        private const val TAG = "OverlayPermissionManage"
        const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2803
        const val LISTEN_TIMEOUT = 5 * 20
        private const val THREAD_SLEEP_TIME = 200
    }
}
