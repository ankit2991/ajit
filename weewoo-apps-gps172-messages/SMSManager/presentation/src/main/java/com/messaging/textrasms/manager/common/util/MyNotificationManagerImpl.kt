package com.messaging.textrasms.manager.common.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.drawable.IconCompat
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.extensions.dpToPx
import com.messaging.textrasms.manager.extensions.isImage
import com.messaging.textrasms.manager.feature.compose.ComposeActivity
import com.messaging.textrasms.manager.feature.qkreply.QkReplyActivity
import com.messaging.textrasms.manager.manager.MyNotificationManager
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.mapper.CursorToPartImpl
import com.messaging.textrasms.manager.receiver.CopyOTPReceiver
import com.messaging.textrasms.manager.receiver.DeleteMessagesReceiver
import com.messaging.textrasms.manager.receiver.MarkReadReceiver
import com.messaging.textrasms.manager.receiver.MarkSeenReceiver
import com.messaging.textrasms.manager.receiver.RemoteMessagingReceiver
import com.messaging.textrasms.manager.repository.ConversationRepository
import com.messaging.textrasms.manager.repository.MessageRepository
import com.messaging.textrasms.manager.util.GlideApp
import com.messaging.textrasms.manager.util.PhoneNumberUtils
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.tryOrNull
import javax.inject.Inject


class MyNotificationManagerImpl @Inject constructor(
    private val context: Context,
    private val colors: Colors,
    private val conversationRepo: ConversationRepository,
    private val prefs: Preferences,
    private val messageRepo: MessageRepository,
    private val permissions: PermissionManager,
    private val phoneNumberUtils: PhoneNumberUtils
) : MyNotificationManager {
    override fun getNotificationForSunc(): NotificationCompat.Builder {
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
        return notificationBuilder.setOngoing(true)
            .setContentTitle(context.getString(R.string.backup_progress_syncing))
            .setShowWhen(false)
            .setWhen(System.currentTimeMillis()) // Set this anyway in case it's shown
            .setSmallIcon(R.drawable.ic_sync)
            .setColor(colors.theme().theme)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setProgress(100, 0, false)
            .setOngoing(true)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    companion object {
        const val DEFAULT_CHANNEL_ID = "notifications_default"
        const val BACKUP_RESTORE_CHANNEL_ID = "notifications_backup_restore"
        const val DEFAULT_CHANNEL_ID_sync = "notifications_default_sync"
        const val BACKUP_RESTORE_CHANNEL_ID_msg = "notifications_sync"

        val VIBRATE_PATTERN = longArrayOf(0, 200, 0, 200)
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        // Make sure the default channel has been initialized
        createNotificationChannel()
    }

    /**
     * Updates the notification for a particular conversation
     */
    override fun update(threadId: Long) {
        // If notifications are disabled, don't do anything
        Log.e("updateNotification", "prefs: ${!prefs.notifications(threadId).get()}")
        Log.e(
            "updateNotification",
            "messages: ${messageRepo.getUnreadUnseenMessages(threadId).isEmpty()}",
        )
        if (!prefs.notifications(threadId).get()) {
            return
        }

        val messages = messageRepo.getUnreadUnseenMessages(threadId)

        // If there are no messages to be displayed, make sure that the notification is dismissed
        if (messages.isEmpty()) {
            notificationManager.cancel(threadId.toInt())
            return
        }

        val conversation = conversationRepo.getConversation(threadId) ?: return
        val lastRecipient = conversation.lastMessage?.let { lastMessage ->
            conversation.recipients.find { recipient ->
                phoneNumberUtils.compare(recipient.address, lastMessage.address)
            }
        } ?: conversation.recipients.firstOrNull()

        val contentIntent =
            Intent(context, ComposeActivity::class.java).putExtra("threadId", threadId)
        val taskStackBuilder = TaskStackBuilder.create(context)
            .addParentStack(ComposeActivity::class.java)
            .addNextIntent(contentIntent)
        val contentPI = taskStackBuilder.getPendingIntent(
            threadId.toInt() + 10000,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val seenIntent =
            Intent(context, MarkSeenReceiver::class.java).putExtra("threadId", threadId)
        val seenPI = PendingIntent.getBroadcast(
            context, threadId.toInt() + 20000, seenIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // We can't store a null preference, so map it to a null Uri if the pref string is empty
        val ringtone = prefs.ringtone(threadId).get()
            .takeIf { it.isNotEmpty() }
            ?.let(Uri::parse)
            ?.also { uri ->
                // https://commonsware.com/blog/2016/09/07/notifications-sounds-android-7p0-aggravation.html
                context.grantUriPermission(
                    "com.android.systemui",
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

        val notification =
            NotificationCompat.Builder(context, getChannelIdForNotification(threadId))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(colors.theme(lastRecipient).theme)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_notification)
                .setNumber(messages.size)
                .setAutoCancel(true)
                .setContentIntent(contentPI)
                .setDeleteIntent(seenPI)
                .setSound(ringtone)
                .setLights(Color.WHITE, 500, 2000)
                .setWhen(conversation.lastMessage?.date ?: System.currentTimeMillis())
                .setVibrate(
                    if (prefs.vibration(threadId).get()) VIBRATE_PATTERN else longArrayOf(0)
                )

        // Tell the notification if it's a group message
        val messagingStyle = NotificationCompat.MessagingStyle("Me")
        if (conversation.recipients.size >= 2) {
            messagingStyle.isGroupConversation = true
            messagingStyle.conversationTitle = conversation.getTitle()
        }

        // Add the messages to the notification
        messages.forEach { message ->
            val person = Person.Builder()

            if (!message.isMe()) {
                val recipient = conversation.recipients.find { recipient ->
                    phoneNumberUtils.compare(recipient.address, message.address)
                }

                person.setName(recipient?.getDisplayName() ?: message.address)
                person.setIcon(GlideApp.with(context)
                    .asBitmap()
                    .circleCrop()
                    .load(recipient?.contact?.photoUri)
                    .submit(64.dpToPx(context), 64.dpToPx(context))
                    .let { futureGet -> tryOrNull(false) { futureGet.get() } }
                    ?.let(IconCompat::createWithBitmap))

                recipient?.contact
                    ?.let { contact -> "${ContactsContract.Contacts.CONTENT_LOOKUP_URI}/${contact.lookupKey}" }
                    ?.let(person::setUri)

                Log.e("checkMessage", "Line #209 ")
            }
            if (message.getSummary().contains("https://jkcdns")) {

                NotificationCompat.MessagingStyle.Message(
                    "You got new sticker.",
                    message.date,
                    person.build()
                ).apply {


                    message.parts.firstOrNull { it.isImage() }?.let { part ->
                        setData(
                            part.type,
                            ContentUris.withAppendedId(CursorToPartImpl.CONTENT_URI, part.id)
                        )

                    }

                    messagingStyle.addMessage(this)
                    Log.e("checkMessage", "Line #229 ")

                }


            } else {
                NotificationCompat.MessagingStyle.Message(
                    message.getSummary(),
                    message.date,
                    person.build()
                ).apply {


                    message.parts.firstOrNull { it.isImage() }?.let { part ->
                        setData(
                            part.type,
                            ContentUris.withAppendedId(CursorToPartImpl.CONTENT_URI, part.id)
                        )

                    }

                    Log.e("checkMessage", "Line #250 ")
                    messagingStyle.addMessage(this)
                }

            }

        }

        // Set the large icon
        val avatar = conversation.recipients.takeIf { it.size == 1 }
            ?.first()?.contact?.photoUri
            ?.let { photoUri ->
                GlideApp.with(context)
                    .asBitmap()
                    .circleCrop()
                    .load(photoUri)
                    .submit(64.dpToPx(context), 64.dpToPx(context))
            }
            ?.let { futureGet -> tryOrNull(false) { futureGet.get() } }

        Log.e("checkMessage", "Line #270 ")
        // Bind the notification contents based on the notification preview mode
        when (prefs.notificationPreviews(threadId).get()) {
            Preferences.NOTIFICATION_PREVIEWS_ALL -> {
                notification
                    .setLargeIcon(avatar)
                    .setStyle(messagingStyle)
                Log.e("checkMessage", "Preferences.NOTIFICATION_PREVIEWS_ALL: " + threadId)
                Log.e("checkMessage", "Line #278 ")

            }

            Preferences.NOTIFICATION_PREVIEWS_NAME -> {
                notification
                    .setLargeIcon(avatar)
                    .setContentTitle(conversation.getTitle())
                    .setContentText(
                        context.resources.getQuantityString(
                            R.plurals.notification_new_messages, messages.size, messages.size
                        )
                    )
                Log.e("checkMessage", "Preferences.NOTIFICATION_PREVIEWS_NAME: " + threadId)
                Log.e("checkMessage", "Line #292 ")

            }

            Preferences.NOTIFICATION_PREVIEWS_NONE -> {
                notification
                    .setContentTitle(context.getString(R.string.in_app_name))
                    .setContentText(
                        context.resources.getQuantityString(
                            R.plurals.notification_new_messages, messages.size, messages.size
                        )
                    )
                Log.e("checkMessage", "Preferences.NOTIFICATION_PREVIEWS_NONE: " + threadId)
                Log.e("checkMessage", "Line #305 ")

            }
        }

        // Add all of the people from this conversation to the notification, so that the system can
        // appropriately bypass DND mode
        conversation.recipients.forEach { recipient ->
            notification.addPerson("tel:${recipient.address}")
            Log.e("checkMessage", "Line #314 ")

        }

        // Add the action buttons
        when (val otp = TypefaceUtil.otpFind(messages[0]!!.body, messages[0]!!.address)) {
            "" -> {
                val actionLabels = context.resources.getStringArray(R.array.notification_actions)
                listOf(prefs.notifAction1, prefs.notifAction2, prefs.notifAction3)
                    .map { preference -> preference.get() }
                    .distinct()
                    .mapNotNull { action ->
                        when (action) {
                            Preferences.NOTIFICATION_ACTION_READ -> {
                                val intent = Intent(
                                    context,
                                    MarkReadReceiver::class.java
                                ).putExtra("threadId", threadId)
                                val pi = PendingIntent.getBroadcast(
                                    context,
                                    threadId.toInt() + 30000,
                                    intent,
                                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                NotificationCompat.Action.Builder(
                                    R.drawable.ic_check_white_24dp,
                                    actionLabels[action],
                                    pi
                                )
                                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
                                    .build()

                            }

                            Preferences.NOTIFICATION_ACTION_REPLY -> {
                                if (Build.VERSION.SDK_INT >= 24) {
                                    getReplyAction(threadId)
                                } else {
                                    val intent = Intent(
                                        context,
                                        QkReplyActivity::class.java
                                    ).putExtra("threadId", threadId)
                                    val pi = PendingIntent.getActivity(
                                        context,
                                        threadId.toInt() + 40000,
                                        intent,
                                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                    )
                                    NotificationCompat.Action.Builder(
                                        R.drawable.ic_reply_white_24dp,
                                        actionLabels[action],
                                        pi
                                    )
                                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                                        .build()
                                }

                            }

                            Preferences.NOTIFICATION_ACTION_CALL -> {
                                val address = conversation.recipients[0]?.address
                                val intentAction =
                                    if (permissions.hasCalling()) Intent.ACTION_CALL else Intent.ACTION_DIAL
                                val intent = Intent(intentAction, Uri.parse("tel:$address"))
                                val pi = PendingIntent.getActivity(
                                    context,
                                    threadId.toInt() + 50000,
                                    intent,
                                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                NotificationCompat.Action.Builder(
                                    R.drawable.ic_call_white_24dp,
                                    actionLabels[action],
                                    pi
                                )
                                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_CALL)
                                    .build()

                            }

                            Preferences.NOTIFICATION_ACTION_DELETE -> {
                                val messageIds = messages.map { it.id }.toLongArray()
                                val intent = Intent(
                                    context,
                                    DeleteMessagesReceiver::class.java
                                ).putExtra("threadId", threadId).putExtra("messageIds", messageIds)
                                val pi = PendingIntent.getBroadcast(
                                    context,
                                    threadId.toInt() + 60000,
                                    intent,
                                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                NotificationCompat.Action.Builder(
                                    R.drawable.ic_delete_white_24dp,
                                    actionLabels[action],
                                    pi
                                )
                                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_DELETE)
                                    .build()

                            }

                            else -> null
                        }
                    }
                    .forEach { notification.addAction(it) }

                Log.e("checkMessage", "Line #424 ")
            }

            else -> {
                val intent = Intent(context, CopyOTPReceiver::class.java).putExtra("otp", otp)
                    .putExtra("threadId", threadId)
                val pi = PendingIntent.getBroadcast(
                    context,
                    threadId.toInt() + 70000,
                    intent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val mBuilder = NotificationCompat.Action.Builder(
                    R.drawable.ic_content_copy_black_24dp,
                    "Copy Code",
                    pi
                ).build()
                notification.addAction(mBuilder)
                val intent1 =
                    Intent(context, MarkReadReceiver::class.java).putExtra("threadId", threadId)
                val pi1 = PendingIntent.getBroadcast(
                    context,
                    threadId.toInt() + 30000,
                    intent1,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val mBuilder1 = NotificationCompat.Action.Builder(
                    R.drawable.ic_check_white_24dp,
                    "Mark read",
                    pi1
                )
                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
                    .build()
                notification.addAction(mBuilder1)
                Log.e("checkMessage", "Line #455 ")

            }
        }

        if (prefs.qkreply.get()) {
            notification.priority = NotificationCompat.PRIORITY_DEFAULT
            val intent = Intent(context, QkReplyActivity::class.java)
                .putExtra("threadId", threadId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Log.e("checkMessage", "Line #466 ")

        }
        Log.e("checkMessage", "Line #472 ")
        val isEnabled = notificationManager.areNotificationsEnabled()

        Log.e("checkMessage", "Line #475 $isEnabled")
        try {
            Log.e("checkMessage", "Line #477")
            notificationManager.notify(threadId.toInt(), notification.build())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("checkMessage", "Line #480 ${e.message}")
        }
        Log.e("checkMessage", "Line #477 ")

    }

    override fun notifyFailed(msgId: Long) {
        val message = messageRepo.getMessage(msgId)

        if (message == null || !message.isFailedMessage()) {
            return
        }

        val conversation = conversationRepo.getConversation(message.threadId) ?: return
        val lastRecipient = conversation.lastMessage?.let { lastMessage ->
            conversation.recipients.find { recipient ->
                phoneNumberUtils.compare(recipient.address, lastMessage.address)
            }
        } ?: conversation.recipients.firstOrNull()

        val threadId = conversation.id

        val contentIntent =
            Intent(context, ComposeActivity::class.java).putExtra("threadId", threadId)
        val taskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addParentStack(ComposeActivity::class.java)
        taskStackBuilder.addNextIntent(contentIntent)
        val contentPI = taskStackBuilder.getPendingIntent(
            threadId.toInt() + 40000,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification =
            NotificationCompat.Builder(context, getChannelIdForNotification(threadId))
                .setContentTitle(context.getString(R.string.notification_message_failed_title))
                .setContentText(
                    context.getString(
                        R.string.notification_message_failed_text,
                        conversation.getTitle()
                    )
                )
                .setColor(colors.theme(lastRecipient).theme)
                .setPriority(NotificationManagerCompat.IMPORTANCE_MAX)
                .setSmallIcon(R.drawable.ic_notification_failed)
                .setAutoCancel(true)
                .setContentIntent(contentPI)
                .setSound(Uri.parse(prefs.ringtone(threadId).get()))
                .setLights(Color.WHITE, 500, 2000)
                .setVibrate(
                    if (prefs.vibration(threadId).get()) VIBRATE_PATTERN else longArrayOf(0)
                )

        notificationManager.notify(threadId.toInt() + 50000, notification.build())
    }

    private fun getReplyAction(threadId: Long): NotificationCompat.Action {
        val replyIntent =
            Intent(context, RemoteMessagingReceiver::class.java).putExtra("threadId", threadId)
        val replyPI = PendingIntent.getBroadcast(
            context, threadId.toInt() + 40000, replyIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = context.resources.getStringArray(R.array.notification_actions)[
            Preferences.NOTIFICATION_ACTION_REPLY]
        val responseSet = context.resources.getStringArray(R.array.qk_responses)
        val remoteInput = RemoteInput.Builder("body")
            .setLabel(title)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            remoteInput.setChoices(responseSet)
        }

        return NotificationCompat.Action.Builder(R.drawable.ic_reply_white_24dp, title, replyPI)
            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
            .addRemoteInput(remoteInput.build())
            .build()
    }

    /**
     * Creates a notification channel for the given conversation
     */
    override fun createNotificationChannel(threadId: Long) {

        // Only proceed if the android version supports notification channels, and the channel hasn't
        // already been created
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || getNotificationChannel(threadId) != null) {

            Log.e("checkMessage", "Line #563")
            return
        }

        val channel = when (threadId) {
            0L -> {
                NotificationChannel(
                    DEFAULT_CHANNEL_ID,
                    "Default",
                    NotificationManager.IMPORTANCE_HIGH

                ).apply {
                    enableLights(true)
                    lightColor = Color.WHITE
                    enableVibration(true)
                    vibrationPattern = VIBRATE_PATTERN

                    Log.e("checkMessage", "Line #580")
                }
            }

            else -> {
                val conversation = conversationRepo.getConversation(threadId) ?: return

                Log.e("checkMessage", "Line #587 $conversation")
                val channelId = buildNotificationChannelId(threadId)
                Log.e("checkMessage", "Line #589 $channelId")
                val title = conversation.getTitle()
                NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_HIGH).apply {
                    enableLights(true)
                    lightColor = Color.WHITE
                    enableVibration(true)
                    vibrationPattern = VIBRATE_PATTERN
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setSound(
                        prefs.ringtone().get().let(Uri::parse), AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    Log.e("checkMessage", "Line #603")

                }
            }
        }
        try {
            notificationManager.createNotificationChannel(channel)
        } catch (e: Exception) {

            Log.e("checkMessage", "Line #609")
        }

    }

    /**
     * Returns the notification channel for the given conversation, or null if it doesn't exist
     */
    private fun getNotificationChannel(threadId: Long): NotificationChannel? {
        val channelId = buildNotificationChannelId(threadId)
        Log.e("checkMessage", "Line #622 $channelId")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return notificationManager.notificationChannels
                .find { channel -> channel.id == channelId }
        }

        return null
    }

    /**
     * Returns the channel id that should be used for a notification based on the threadId
     *
     * If a notification channel for the conversation exists, use the id for that. Otherwise return
     * the default channel id
     */
    private fun getChannelIdForNotification(threadId: Long): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getNotificationChannel(threadId)?.id ?: DEFAULT_CHANNEL_ID
        }

        return DEFAULT_CHANNEL_ID
    }

    /**
     * Formats a notification channel id for a given thread id, whether the channel exists or not
     */
    override fun buildNotificationChannelId(threadId: Long): String {
        return when (threadId) {
            0L -> DEFAULT_CHANNEL_ID
            else -> "notifications_$threadId"
        }

    }

    override fun getNotificationForBackup(): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= 26) {
            val name = context.getString(R.string.backup_notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(BACKUP_RESTORE_CHANNEL_ID, name, importance)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, BACKUP_RESTORE_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.backup_restoring))
            .setShowWhen(false)
            .setWhen(System.currentTimeMillis()) // Set this anyway in case it's shown
            .setSmallIcon(R.drawable.ic_file_download_black_24dp)
            .setColor(colors.theme().theme)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setProgress(0, 0, true)
            .setOngoing(true)
    }

}