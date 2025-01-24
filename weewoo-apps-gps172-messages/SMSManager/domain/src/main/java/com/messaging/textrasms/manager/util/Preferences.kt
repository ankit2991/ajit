package com.messaging.textrasms.manager.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor(
    context: Context,
    private val rxPrefs: RxSharedPreferences,
    private val sharedPrefs: SharedPreferences
) {

    companion object {
        const val NIGHT_MODE_SYSTEM = 0
        const val NIGHT_MODE_OFF = 1
        const val NIGHT_MODE_ON = 2
        const val NIGHT_MODE_AUTO = 3

        const val TEXT_SIZE_SMALL = 0
        const val TEXT_SIZE_NORMAL = 1
        const val TEXT_SIZE_LARGE = 2
        const val TEXT_SIZE_LARGER = 3

        const val NOTIFICATION_PREVIEWS_ALL = 0
        const val NOTIFICATION_PREVIEWS_NAME = 1
        const val NOTIFICATION_PREVIEWS_NONE = 2

        const val NOTIFICATION_ACTION_NONE = 0
        const val NOTIFICATION_ACTION_READ = 1
        const val NOTIFICATION_ACTION_REPLY = 2
        const val NOTIFICATION_ACTION_CALL = 3
        const val NOTIFICATION_ACTION_DELETE = 4

        const val SEND_DELAY_NONE = 0
        const val SEND_DELAY_SHORT = 1
        const val SEND_DELAY_MEDIUM = 2
        const val SEND_DELAY_LONG = 3

        const val SWIPE_ACTION_NONE = 0
        const val SWIPE_ACTION_ARCHIVE = 1
        const val SWIPE_ACTION_DELETE = 2
        const val SWIPE_ACTION_CALL = 3
        const val SWIPE_ACTION_READ = 4
        const val SWIPE_ACTION_UNREAD = 5

        const val BLOCKING_MANAGER_SMS = 0
        const val BLOCKING_MANAGER_CC = 1
        const val BLOCKING_MANAGER_SIA = 2

        const val ADSREMOVED = "ADSREMOVED"
        const val IS_FIRST_SESSION = "isfirstsession"
        const val isConsentGivenGDPR = "isConsentGivenGdpr"
        const val ADSREMOVEDLifetime = "ADSREMOVEDLifetime"
        const val ADSREMOVEDMONTH = "ADSREMOVEDMONTH"
        const val ADSREMOVEDYear = "ADSREMOVEDYear"
        const val SYNCING = "syncing"
        const val IS_RATE = "is_rate"
        const val LAST_OPEN = "last_open"
        const val RATE_DIALOG_COUNT = "rate_dialog_count"
        const val FirstLaunch = "FirstLaunch1"
        const val newversion = "newversion"
        const val delay = "delay"
        const val NewVERSION = "NewVERSION"
        const val INTRODUCTION = "INTRODUCTION"
        const val HAS_DISPLAYED_FIRST_PAYMENT_CARD = "HAS_DISPLAYED_FIRST_PAYMENT_CARD"
        const val Country = "Country"
        const val en = 0
        const val Coinsvalue = "Coinsvalue"
        const val SetFilter = "SetFilter"
        const val FirstTimeRemoved = "FirstTimeRemoved"
        const val ISCONSENT = "isconsent"
        const val SELECTED_LANGUAGE = "selected_language"
        const val IS_LANG_SELECTED = "islangselected"
        fun setBoolean(context: Context, key: String, value: Boolean) {
            val preferences: SharedPreferences =
                context.getSharedPreferences(key, Context.MODE_PRIVATE)
            if (preferences != null && !TextUtils.isEmpty(key)) {
                val editor = preferences.edit()
                editor.putBoolean(key, value)
                editor.apply()
            }
        }

        fun getBoolean(context: Context?, key: String): Boolean {
            val preferences: SharedPreferences =
                context!!.getSharedPreferences(key, Context.MODE_PRIVATE)
            if (preferences != null) {
                return preferences.getBoolean(key, false)
            }
            return false
        }

        fun setIntVal(context: Context, key: String, value: Int) {
            val preferences: SharedPreferences =
                context.getSharedPreferences(key, Context.MODE_PRIVATE)
            if (preferences != null && !TextUtils.isEmpty(key)) {
                val editor = preferences.edit()
                editor.putInt(key, value)
                editor.apply()
            }
        }

        fun getIntVal(context: Context, key: String, default: Int = 0): Int {
            val preferences: SharedPreferences =
                context.getSharedPreferences(key, Context.MODE_PRIVATE)
            if (preferences != null) {
                return preferences.getInt(key, default)
            }
            return 0
        }

        fun setStringVal(context: Context, key: String, value: String) {
            val preferences: SharedPreferences =
                context.getSharedPreferences(key, Context.MODE_PRIVATE)
            if (preferences != null && !TextUtils.isEmpty(key)) {
                val editor = preferences.edit()
                editor.putString(key, value)
                editor.apply()
            }
        }

        fun getStringVal(context: Context, key: String): String? {
            val preferences: SharedPreferences =
                context.getSharedPreferences(key, Context.MODE_PRIVATE)
            if (preferences != null) {
                return preferences.getString(key, "")
            }
            return ""
        }
    }



    val didSetReferrer = rxPrefs.getBoolean("didSetReferrer", false)
    val night = rxPrefs.getBoolean("night", false)
    val canUseSubId = rxPrefs.getBoolean("canUseSubId", true)
    val version = rxPrefs.getInteger("version", context.versionCode)
    val changelogVersion = rxPrefs.getInteger("changelogVersion", context.versionCode)

    @Deprecated("This should only be accessed when migrating to @blockingManager")
    val sia = rxPrefs.getBoolean("sia", false)

    val nightMode =
        if (Build.VERSION.SDK_INT >= 29) {
            rxPrefs.getInteger("nightMode", NIGHT_MODE_SYSTEM)
        } else {
            rxPrefs.getInteger("nightMode", NIGHT_MODE_OFF)
        }

    val nightStart = rxPrefs.getString("nightStart", "18:00")
    val nightEnd = rxPrefs.getString("nightEnd", "6:00")
    val black = rxPrefs.getBoolean("black", false)
    val autoColor = rxPrefs.getBoolean("autoColor", true)
    val systemFont = rxPrefs.getBoolean("systemFont", false)
    val textSize = rxPrefs.getInteger("textSize", TEXT_SIZE_NORMAL)
    val blockingManager = rxPrefs.getInteger("blockingManager", BLOCKING_MANAGER_SMS)
    val drop = rxPrefs.getBoolean("drop", false)
    val notifAction1 = rxPrefs.getInteger("notifAction1", NOTIFICATION_ACTION_READ)
    val notifAction2 = rxPrefs.getInteger("notifAction2", NOTIFICATION_ACTION_REPLY)
    val notifAction3 = rxPrefs.getInteger("notifAction3", NOTIFICATION_ACTION_NONE)
    val qkreply = rxPrefs.getBoolean("qkreply", Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
    val qkreplyTapDismiss = rxPrefs.getBoolean("qkreplyTapDismiss", true)
    val sendDelay = rxPrefs.getInteger("sendDelay", SEND_DELAY_NONE)
    val swipeRight = rxPrefs.getInteger("swipeRight", SWIPE_ACTION_ARCHIVE)
    val swipeLeft = rxPrefs.getInteger("swipeLeft", SWIPE_ACTION_DELETE)
    val autoEmoji = rxPrefs.getBoolean("autoEmoji", true)
    val delivery = rxPrefs.getBoolean("delivery", true)
    val signature = rxPrefs.getString("signature", "")
    val unicode = rxPrefs.getBoolean("unicode", false)
    val mobileOnly = rxPrefs.getBoolean("mobileOnly", false)
    val longAsMms = rxPrefs.getBoolean("longAsMms", false)
    val mmsSize = rxPrefs.getInteger("mmsSize", 300)
    val Language = rxPrefs.getInteger("Language", en)

    init {
        val nightModeSummary = rxPrefs.getInteger("nightModeSummary")


        if (nightModeSummary.isSet) {

            nightMode.set(
                when (nightModeSummary.get()) {
                    0 -> NIGHT_MODE_OFF
                    1 -> NIGHT_MODE_ON
                    2 -> NIGHT_MODE_AUTO
                    else -> NIGHT_MODE_OFF
                }
            )
            nightModeSummary.delete()
        }
        if (!Preferences.getBoolean(context, Preferences.ADSREMOVED)) {
            nightMode.set(NIGHT_MODE_OFF)
        }

    }


    val keyChanges: Observable<String> = Observable.create<String> { emitter ->
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            emitter.onNext(key)
        }

        emitter.setCancellable {
            sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }

        sharedPrefs.registerOnSharedPreferenceChangeListener(listener)
    }.share()

    fun theme(
        recipientId: Long = 0,
        default: Int = rxPrefs.getInteger("theme", 0xFF0074FE.toInt()).get()
    ): Preference<Int> {
        return when (recipientId) {
            0L -> rxPrefs.getInteger("theme", 0xFF0074FE.toInt())
            else -> rxPrefs.getInteger("theme_$recipientId", default)
        }
    }

    fun notifications(threadId: Long = 0): Preference<Boolean> {
        val default = rxPrefs.getBoolean("notifications", true)

        return when (threadId) {
            0L -> default
            else -> rxPrefs.getBoolean("notifications_$threadId", default.get())
        }
    }

    fun notificationPreviews(threadId: Long = 0): Preference<Int> {
        val default = rxPrefs.getInteger("notification_previews", 0)

        return when (threadId) {
            0L -> default
            else -> rxPrefs.getInteger("notification_previews_$threadId", default.get())
        }
    }

    fun wakeScreen(threadId: Long = 0): Preference<Boolean> {
        val default = rxPrefs.getBoolean("wake", false)

        return when (threadId) {
            0L -> default
            else -> rxPrefs.getBoolean("wake_$threadId", default.get())
        }
    }

    fun vibration(threadId: Long = 0): Preference<Boolean> {
        val default = rxPrefs.getBoolean("vibration", true)

        return when (threadId) {
            0L -> default
            else -> rxPrefs.getBoolean("vibration$threadId", default.get())
        }
    }

    fun ringtone(threadId: Long = 0): Preference<String> {
        val default =
            rxPrefs.getString("ringtone", Settings.System.DEFAULT_NOTIFICATION_URI.toString())

        return when (threadId) {
            0L -> default
            else -> rxPrefs.getString("ringtone_$threadId", default.get())
        }
    }




}
