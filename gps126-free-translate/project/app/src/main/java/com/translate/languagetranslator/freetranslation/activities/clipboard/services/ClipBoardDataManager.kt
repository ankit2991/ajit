package com.translate.languagetranslator.freetranslation.activities.clipboard.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Patterns
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.translate.languagetranslator.freetranslation.R
import com.translate.languagetranslator.freetranslation.activities.clipboard.CopyToClipActivity
import com.translate.languagetranslator.freetranslation.activities.home.HomeActivity
import com.translate.languagetranslator.freetranslation.appUtils.Constants.INTENT_KEY_CLIP_BOARD_DATA
import com.translate.languagetranslator.freetranslation.appUtils.Constants.KEY_ORIGIN
import com.translate.languagetranslator.freetranslation.appUtils.TinyDB
import com.translate.languagetranslator.freetranslation.appUtils.showToast

class ClipBoardDataManager : Service() {


    private val FOREGROUND_CHANNEL_ID = "translate_all_channel_id"
    private var mNotificationManager: NotificationManager? = null
    private var mCM: ClipboardManager? = null
    private var copiedWord: String? = null
    private var clipBoardData: String? = null
    private var mOldClip: String? = null
    var isclip = true
    private val onPrimaryClipChangedListener = OnPrimaryClipChangedListener {
        if (mCM != null) {
            try {
                val clipDescription = mCM!!.primaryClipDescription
                if (clipDescription != null) {
                    clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    if (mCM!!.hasPrimaryClip() && mCM!!.primaryClip != null) {
                        val mClip = mCM!!.primaryClip
                        val newClip: String
                        val itemCount = mClip!!.itemCount
                        if (itemCount > 0) {
                            val item = mClip.getItemAt(0)
                            if (item != null) {
                                newClip = mClip.toString()
                                if (newClip != mOldClip && !newClip.contains("NULL")) {
                                    mOldClip = newClip
                                    if (isclip) {
                                        if (!TinyDB.getInstance(this).getBoolean("is_from_app")) {
                                            try {
                                                detectText()
                                            } catch (e: java.lang.Exception) {
                                                e.printStackTrace()
                                            }
                                        } else {
                                            TinyDB.getInstance(this)
                                                .putBoolean("is_from_app", false)
                                        }
                                    }
                                } else {
                                    showToast(
                                        this,
                                        resources.getString(R.string.copy_other_data)
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                startForeground(1,prepareNotification())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        setTheme(R.style.AppTheme)
        kotlin.runCatching {
            mCM = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText("", "")
            if (data != null) {
                mCM!!.setPrimaryClip(data)
            }
            onPrimaryClipChangedListener.let {
                mCM!!.addPrimaryClipChangedListener(it)
            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCM?.let { mccM ->
            onPrimaryClipChangedListener.let {
                mccM.removePrimaryClipChangedListener(it)
            }
        }
    }

    private fun detectText() {
        mCM?.let {
            val charSequence = it.text
            charSequence?.let { char ->
                copiedWord = char.toString().trim()
                if (!copiedWord.isNullOrEmpty() && !copiedWord.isNullOrBlank()) {
                    if (!isValidUrl(copiedWord!!)) {
                        showClipboardDataView()
                    } else {
                        showToast(this, resources.getString(R.string.copy_valid_text))
                    }

                }

            }
        } ?: run {
            showToast(this, resources.getString(R.string.copy_valid_text))

        }
    }

    private fun showClipboardDataView() {
        kotlin.runCatching {
            clipBoardData = copiedWord
            val m = Intent(this, CopyToClipActivity::class.java)
            m.putExtra(KEY_ORIGIN, "service")
            m.putExtra(INTENT_KEY_CLIP_BOARD_DATA, clipBoardData)
            m.flags = 276824064
            startActivity(m)
        }
    }

    private fun isValidUrl(url: String): Boolean {
        val p = Patterns.WEB_URL
        val m = p.matcher(url.toLowerCase())
        return m.matches()
    }

    private fun prepareNotification(): Notification {
        // handle build version above android oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            mNotificationManager!!.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null
        ) {
            val name: CharSequence = getString(R.string.text_notification)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance)
            channel.enableVibration(false)
            mNotificationManager!!.createNotificationChannel(channel)
        }
        val notificationIntent = Intent(this, HomeActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        notificationIntent.putExtra("type", "edittext")
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val remoteViews = RemoteViews(packageName, R.layout.clipboard_notification)
        val notificationBuilder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            } else {
                NotificationCompat.Builder(this)
            }
        notificationBuilder
            .setContent(remoteViews)
            .setSmallIcon(R.drawable.ic_icon_drawer)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }
        return notificationBuilder.build()
    }
}