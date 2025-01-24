package com.notifications.firebase.services

import android.content.Context
import android.util.Log
import android.view.View

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.notifications.firebase.BuildConfig
import com.notifications.firebase.utils.NotifUtils
import com.notifications.firebase.utils.NotifTinyDB
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger


open class MessagingService : FirebaseMessagingService() {

    companion object {

        const val ICON_KEY = "icon"
        const val APP_TITLE_KEY = "title"
        const val SHORT_DESC_KEY = "short_desc"
        const val LONG_DESC_KEY = "long_desc"
        const val APP_FEATURE_KEY = "feature"
        const val APP_URL_KEY = "app_url"

        const val UPDATE_MSG_KEY = "update_msg"
        const val IS_CANCELABLE_KEY = "is_cancelable"
        const val SHOULD_OPEN_APP = "should_open_app"


        const val IS_PREMIUM = "is_premium"

        private val seed = AtomicInteger()

        fun getNextInt(): Int {
            return seed.incrementAndGet()
        }

        private const val RELEASE_TOPIC = "gps_navigation"
        private const val DEBUG_TOPIC = "test_app"

        //replaced with asynchronous way
        /*fun subscribeToTopic(context: Context?) {
            try {
                if (FirebaseInstanceId.getInstance() != null && FirebaseInstanceId.getInstance().id != null && !FirebaseInstanceId.getInstance().id.isEmpty()) {
                    if (BuildConfig.DEBUG)
                        FirebaseMessaging.getInstance().subscribeToTopic(DEBUG_TOPIC)
                    else {
                        FirebaseMessaging.getInstance().subscribeToTopic(RELEASE_TOPIC)
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(DEBUG_TOPIC)
                    }
                }
            } catch (e: Exception) {
                Log.e("InstanceIDService ", e.toString())
            }
            val tinyDB: TinyDB? = context?.let { TinyDB.getInstance(it) }
            if (tinyDB?.getString(UPDATE_MSG_KEY) != null && tinyDB.getString(UPDATE_MSG_KEY).isNotEmpty()) {
                NotifUtils.showDialog(context,
                        NotifUtils.getApplicationName(context),
                        tinyDB.getString(UPDATE_MSG_KEY),
                        tinyDB.getBoolean(IS_CANCELABLE_KEY),
                        View.OnClickListener {
                            NotifUtils.openUrl(it.context, tinyDB.getString(APP_URL_KEY))
                        })
            }
        }*/

        fun subscribeToTopic(context: Context?, token: String? = null) {
            try {

                if (!token.isNullOrEmpty()) {
                    if (BuildConfig.DEBUG)
                        FirebaseMessaging.getInstance().subscribeToTopic(DEBUG_TOPIC)
                    else {
                        FirebaseMessaging.getInstance().subscribeToTopic(RELEASE_TOPIC)
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(DEBUG_TOPIC)
                    }
                } else {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener {
                        if (it.isComplete) {
                            try {
                                Timber.tag("King").d("Token->: ${it.result}")
                                Log.e("FCM_TOKEN->", it.result)
                                if (BuildConfig.DEBUG)
                                    FirebaseMessaging.getInstance().subscribeToTopic(DEBUG_TOPIC)
                                else {
                                    FirebaseMessaging.getInstance().subscribeToTopic(RELEASE_TOPIC)
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(DEBUG_TOPIC)
                                }
                            }catch (e:Exception){
                                e.printStackTrace()
                                Log.e("FCM_TOKEN->",  e.printStackTrace().toString())
                            }

                        }
                    }
                }
                val notifTinyDB: NotifTinyDB? = context?.let { NotifTinyDB.getInstance(it) }
                if (notifTinyDB?.getString(UPDATE_MSG_KEY) != null && notifTinyDB.getString(UPDATE_MSG_KEY)
                        .isNotEmpty()
                ) {

                    NotifUtils.showDialog(context,
                        NotifUtils.getApplicationName(context),
                        notifTinyDB.getString(UPDATE_MSG_KEY),
                        notifTinyDB.getBoolean(IS_CANCELABLE_KEY),
                        View.OnClickListener {
                            NotifUtils.openUrl(it.context, notifTinyDB.getString(APP_URL_KEY))
                        })
                }


            } catch (e: Exception) {
                Log.e("FirebaseMessaging", e.toString())
                //return false
            }
        }
    }


    override fun onNewToken(token: String) {
        /*if (BuildConfig.DEBUG) Log.d("token generated ", token)
        subscribeToTopic(null)*/

        Timber.tag("King").d("Token->: $token")

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isComplete)
                Timber.tag("King").d("Token->: ${it.result}")
            subscribeToTopic(this@MessagingService, it.result.toString())
        }
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        if (data.isNotEmpty()) {

            if (data.containsKey(UPDATE_MSG_KEY))
                NotifUtils.sendUpdateMsg(NotifTinyDB.getInstance(this)!!, data)
            else
                NotifUtils.sendAppInstallNotification(this, data)
        }

        /*if (BuildConfig.DEBUG) Log.e("From: ", remoteMessage.from)
        if (BuildConfig.DEBUG) remoteMessage.notification?.let { Log.e("Message  Body:", it.body) }*/

    }


}