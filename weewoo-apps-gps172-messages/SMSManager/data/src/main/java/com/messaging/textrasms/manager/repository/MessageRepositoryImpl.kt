package com.messaging.textrasms.manager.repository

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.Sms
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.text.format.DateFormat
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import com.google.android.mms.ContentType
import com.google.android.mms.MMSPart
import com.google.android.mms.pdu_alt.MultimediaMessagePdu
import com.google.android.mms.pdu_alt.PduPersister
import com.klinker.android.send_message.SmsManagerFactory
import com.klinker.android.send_message.StripAccents
import com.klinker.android.send_message.Transaction
import com.messaging.textrasms.manager.compat.TelephonyCompat
import com.messaging.textrasms.manager.extensions.anyOf
import com.messaging.textrasms.manager.manager.ActiveConversationManager
import com.messaging.textrasms.manager.manager.KeyManager
import com.messaging.textrasms.manager.model.Attachment
import com.messaging.textrasms.manager.model.Conversation
import com.messaging.textrasms.manager.model.Message
import com.messaging.textrasms.manager.model.MmsPart
import com.messaging.textrasms.manager.receiver.SendSmsReceiver
import com.messaging.textrasms.manager.receiver.SmsDeliveredReceiver
import com.messaging.textrasms.manager.receiver.SmsSentReceiver
import com.messaging.textrasms.manager.util.*
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import timber.log.Timber
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt


@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val activeConversationManager: ActiveConversationManager,
    private val context: Context,
    private val messageIds: KeyManager,
    private val phoneNumberUtils: PhoneNumberUtils,
    private val prefs: Preferences,
    private val syncRepository: SyncRepository
) : MessageRepository {


    override fun getMessages(threadId: Long, query: String): RealmResults<Message> {

        val realm = Realm.getDefaultInstance()

        return realm
            .where(Message::class.java)
            .takeIf { (!realm.isClosed) }?.equalTo("threadId", threadId)
            ?.takeIf { threadId -> threadId.isValid }
            .let {
                when (query.isEmpty()) {
                    true -> it
                    false -> it?.beginGroup()?.contains("body", query, Case.INSENSITIVE)?.or()
                        ?.contains("parts.text", query, Case.INSENSITIVE)?.endGroup()
                }
            }?.sort("date")?.findAllAsync()!!


    }

    override fun getMessage(id: Long): Message? {
        return Realm.getDefaultInstance()
            .also { realm -> realm.refresh() }
            .where(Message::class.java)
            .equalTo("id", id)
            .findFirst()
    }

    override fun getMessageForPart(id: Long): Message? {
        return Realm.getDefaultInstance()
            .where(Message::class.java)
            .equalTo("parts.id", id)
            .findFirst()
    }

    override fun getLastIncomingMessage(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
            .where(Message::class.java)
            .equalTo("threadId", threadId)
            .beginGroup()
            .beginGroup()
            .equalTo("type", "sms")
            .`in`("boxId", arrayOf(Sms.MESSAGE_TYPE_INBOX, Sms.MESSAGE_TYPE_ALL))
            .endGroup()
            .or()
            .beginGroup()
            .equalTo("type", "mms")
            .`in`("boxId", arrayOf(Mms.MESSAGE_BOX_INBOX, Mms.MESSAGE_BOX_ALL))
            .endGroup()
            .endGroup()
            .sort("date", Sort.DESCENDING)
            .findAll()
    }

    override fun getUnreadCount(): Long {
        return Realm.getDefaultInstance().use { realm ->
            realm.refresh()
            realm.where(Conversation::class.java)
                .equalTo("archived", false)
                .equalTo("blocked", false)
                .equalTo("lastMessage.read", false)
                .count()
        }
    }

    override fun getPart(id: Long): MmsPart? {
        return Realm.getDefaultInstance()
            .where(MmsPart::class.java)
            .equalTo("id", id)
            .findFirst()
    }

    override fun getPartsForConversation(threadId: Long): RealmResults<MmsPart> {
        return Realm.getDefaultInstance()
            .where(MmsPart::class.java)
            .equalTo("messages.threadId", threadId)
            .beginGroup()
            .contains("type", "image/")
            .or()
            .contains("type", "video/")
            .endGroup()
            .sort("id", Sort.DESCENDING)
            .findAllAsync()
    }

    override fun savePart(id: Long): File? {
        val part = getPart(id) ?: return null

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(part.type)
            ?: return null
        val date = part.messages?.first()?.date
        val dir =
            File(Environment.getExternalStorageDirectory(), "Messages/Media").apply { mkdirs() }
        val fileName = part.name?.takeIf { name -> name.endsWith(extension) }
            ?: "${part.type.split("/").last()}_$date.$extension"
        var file: File
        var index = 0
        do {
            file = File(
                dir,
                if (index == 0) fileName else fileName.replace(
                    ".$extension",
                    " ($index).$extension"
                )
            )
            index++
        } while (file.exists())

        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    "android.permission.READ_SMS"
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                FileOutputStream(file).use { outputStream ->
                    context.contentResolver.openInputStream(part.getUri())?.use { inputStream ->
                        inputStream.copyTo(outputStream, 1024)
                    }
                }

            } else {
                return null
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        MediaScannerConnection.scanFile(context, arrayOf(file.path), null, null)

        return file.takeIf { it.exists() }
    }

    override fun getUnreadUnseenMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
            .also { it.refresh() }
            .where(Message::class.java)
            .equalTo("seen", false)
            .equalTo("read", false)
            .equalTo("threadId", threadId)
            .sort("date")
            .findAll()
    }

    override fun getUnreadMessages(threadId: Long): RealmResults<Message> {
        return Realm.getDefaultInstance()
            .where(Message::class.java)
            .equalTo("read", false)
            .equalTo("threadId", threadId)
            .sort("date")
            .findAll()
    }

    override fun markAllSeen() {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Message::class.java).equalTo("seen", false).findAll()
        realm.executeTransaction { messages.forEach { message -> message.seen = true } }
        realm.close()
    }

    override fun markSeen(threadId: Long) {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Message::class.java)
            .equalTo("threadId", threadId)
            .equalTo("seen", false)
            .findAll()

        realm.executeTransaction {
            messages.forEach { message ->
                message.seen = true
            }
        }
        realm.close()
    }

    override fun getmessagecount(threadId: Long): Int {
        val realm = Realm.getDefaultInstance()
        val messages = realm.where(Message::class.java)
            .equalTo("threadId", threadId)
            .equalTo("seen", false)
            .findAll()

        realm.close()
        return messages.size
    }

    override fun markRead(vararg threadIds: Long) {
        Realm.getDefaultInstance()?.use { realm ->
            val messages = realm.where(Message::class.java)
                .anyOf("threadId", threadIds)
                .beginGroup()
                .equalTo("read", false)
                .or()
                .equalTo("seen", false)
                .endGroup()
                .findAll()

            realm.executeTransaction {
                messages.forEach { message ->
                    message.seen = true
                    message.read = true
                }
            }
        }

        val values = ContentValues()
        values.put(Sms.SEEN, true)
        values.put(Sms.READ, true)

        threadIds.forEach { threadId ->
            try {
                val uri =
                    ContentUris.withAppendedId(Telephony.MmsSms.CONTENT_CONVERSATIONS_URI, threadId)
                context.contentResolver.update(uri, values, "${Sms.READ} = 0", null)
            } catch (exception: Exception) {
                Timber.w(exception)
            }
        }
    }

    override fun markUnread(vararg threadIds: Long) {
        Realm.getDefaultInstance()?.use { realm ->
            val conversations = realm.where(Conversation::class.java)
                .anyOf("id", threadIds)
                .equalTo("lastMessage.read", true)
                .findAll()

            realm.executeTransaction {
                conversations.forEach { conversation ->
                    conversation.lastMessage?.read = false
                }
            }
        }
    }

    override fun sendMessage(
        subId: Int,
        threadId: Long,
        addresses: List<String>,
        body: String,
        attachments: List<Attachment>,
        delay: Int
    ) {
        //sent=true
        i = -1
        val signedBody = when {
            prefs.signature.get().isEmpty() -> body
            body.isNotEmpty() -> body + '\n' + prefs.signature.get()
            else -> prefs.signature.get()
        }


        val smsManager = subId.takeIf { it != -1 }
            ?.let(SmsManagerFactory::createSmsManager)
//            ?:SmsManager.getDefault()
            ?:  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
                val smsManager2 = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                smsManager2} else {
                SmsManager.getDefault()
            }

        // We only care about stripping SMS
        val strippedBody = when (prefs.unicode.get()) {
            true -> StripAccents.stripAccents(signedBody)
            false -> signedBody
        }

        val parts = smsManager.divideMessage(strippedBody).orEmpty()
        val forceMms = prefs.longAsMms.get() && parts.size > 1
        if (attachments.isEmpty() && !forceMms) { // SMS
            if (delay > 0) { // With delay
                val sendTime = System.currentTimeMillis() + delay
                val message =
                    insertSentSms(subId, threadId, addresses.first(), strippedBody, sendTime)

                val intent = getIntentForDelayedSms(message.id)

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        sendTime,
                        intent
                    )
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, sendTime, intent)
                }
            } else {

                val message = insertSentSms(subId, threadId, addresses.first(), strippedBody, now())
                if (addresses.size > 1) {
                    var x = 0
                    while (x < addresses.size) {
                        Log.d("TAG", "sendMessage: " + addresses.get(x))
                        sendSms(message, addresses, x)
                        x++
                    }


                } else {
                    sendSms(message, addresses)
                }
            }
        } else {
            val parts = arrayListOf<MMSPart>()

            val maxWidth =
                smsManager.carrierConfigValues.getInt(SmsManager.MMS_CONFIG_MAX_IMAGE_WIDTH)
                    .takeIf { prefs.mmsSize.get() == -1 } ?: Int.MAX_VALUE

            val maxHeight =
                smsManager.carrierConfigValues.getInt(SmsManager.MMS_CONFIG_MAX_IMAGE_HEIGHT)
                    .takeIf { prefs.mmsSize.get() == -1 } ?: Int.MAX_VALUE

            var remainingBytes = when (prefs.mmsSize.get()) {
                -1 -> smsManager.carrierConfigValues.getInt(SmsManager.MMS_CONFIG_MAX_MESSAGE_SIZE)
                0 -> Int.MAX_VALUE
                else -> prefs.mmsSize.get() * 1024
            } * 0.9

            signedBody.takeIf { it.isNotEmpty() }?.toByteArray()?.let { bytes ->
                remainingBytes -= bytes.size
                parts += MMSPart("text", ContentType.TEXT_PLAIN, bytes)
            }

            parts += attachments
                .mapNotNull { attachment -> attachment as? Attachment.Contact }
                .map { attachment -> attachment.vCard.toByteArray() }
                .map { vCard ->
                    remainingBytes -= vCard.size
                    MMSPart("contact", ContentType.TEXT_VCARD, vCard)
                }

            val imageBytesByAttachment = attachments
                .mapNotNull { attachment -> attachment as? Attachment.Image }
                .associateWith { attachment ->
                    val uri = attachment.getUri() ?: return@associateWith byteArrayOf()
                    when (attachment.isGif(context)) {
                        true -> ImageUtils.getScaledGif(context, uri, maxWidth, maxHeight)

                        false -> getBytes(context, uri)
                    }
                }
                .toMutableMap()

            val imageByteCount = imageBytesByAttachment.values.sumBy { byteArray -> byteArray.size }
            if (imageByteCount > remainingBytes) {
                imageBytesByAttachment.forEach { (attachment, originalBytes) ->
                    val uri = attachment.getUri() ?: return@forEach
                    val maxBytes = originalBytes.size / imageByteCount.toFloat() * remainingBytes

                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    val width = options.outWidth
                    val height = options.outHeight
                    val aspectRatio = width.toFloat() / height.toFloat()

                    var attempts = 0
                    var scaledBytes = originalBytes

                    while (scaledBytes.size > maxBytes) {
                        val scale = maxBytes / originalBytes.size * (0.9 - attempts * 0.2)
                        if (scale <= 0) {
                            Timber.w("Failed to compress ${originalBytes.size / 1024}Kb to ${maxBytes.toInt() / 1024}Kb")
                            return@forEach
                        }

                        val newArea = scale * width * height
                        val newWidth = sqrt(newArea * aspectRatio).toInt()
                        val newHeight = (newWidth / aspectRatio).toInt()

                        attempts++
                        scaledBytes = when (attachment.isGif(context)) {
                            true -> ImageUtils.getScaledGif(context, uri, newWidth, newHeight, 80)
                            false -> getBytes(context, uri)
                        }

                        Timber.d("Compression attempt $attempts: ${scaledBytes.size / 1024}/${maxBytes.toInt() / 1024}Kb ($width*$height -> $newWidth*$newHeight)")
                    }

                    Timber.v("Compressed ${originalBytes.size / 1024}Kb to ${scaledBytes.size / 1024}Kb with a target size of ${maxBytes.toInt() / 1024}Kb in $attempts attempts")
                    imageBytesByAttachment[attachment] = scaledBytes
                }
            }

            imageBytesByAttachment.forEach { (attachment, bytes) ->
                parts += when (attachment.isGif(context)) {
                    true -> {
                        MMSPart("image", ContentType.IMAGE_GIF, bytes)
                    }
                    false -> {
                        MMSPart("image", ContentType.IMAGE_PNG, bytes)
                    }
                }
            }

            val transaction = Transaction(context)
            val recipients = addresses.map(phoneNumberUtils::normalizeNumber)

            transaction.sendNewMessage(subId, threadId, recipients, parts, null, null)


        }
    }

    fun getBytes(context: Context, data: Uri): ByteArray {
        val stream = context.contentResolver.openInputStream(data)
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        var len = stream!!.read(buffer)
        while (len != -1) {
            byteBuffer.write(buffer, 0, len)
            len = stream.read(buffer)
        }

        stream.close()

        return byteBuffer.toByteArray()
    }


    fun createContentUri(context: Context?, file: File?) =
        if (context == null || file!!.path.contains("firebase -1")) {
            Uri.EMPTY
        } else {
            try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                Uri.EMPTY
            }

        }

    fun createContentUri(context: Context, fileUri: Uri) =
        if (fileUri.toString().contains("content://")) {
            fileUri
        } else {
            val file = File(fileUri.path)
            createContentUri(context, file)
        }

    private fun createFileFromBitmap(
        context: Context,
        name: String,
        bitmap: Bitmap,
        mimeType: String
    ): File {
        var out: FileOutputStream? = null
        val file = File(context.filesDir, name)

        try {
            if (!file.exists()) {
                file.createNewFile()
            }

            out = FileOutputStream(file)
            bitmap.compress(
                if (mimeType == "image/png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                75, out
            )
        } catch (e: IOException) {
            Log.e("Scale to Send", "failed to write output stream", e)
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                Log.e("Scale to Send", "failed to close output stream", e)
            }
        }

        return file
    }

    private fun generateBitmap(
        byteArr: ByteArray,
        arraySize: Int,
        largerSide: Int,
        scaleAmount: Int
    ): Bitmap {
        val options = BitmapFactory.Options()

        options.inSampleSize = scaleAmount

        options.inPreferredConfig =
            Bitmap.Config.RGB_565
        options.inDither = true

        options.inDensity = largerSide
        options.inTargetDensity = largerSide * (1 / options.inSampleSize)

        return BitmapFactory.decodeByteArray(byteArr, 0, arraySize, options)
    }

    private fun createFileFromBitmap(context: Context, name: String, bitmap: Bitmap): File {
        var out: FileOutputStream? = null
        val file = File(context.filesDir, name)

        try {
            if (!file.exists()) {
                file.createNewFile()
            }

            out = FileOutputStream(file)
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                75, out
            )
        } catch (e: IOException) {
            Log.e("Scale to Send", "failed to write output stream", e)
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                Log.e("Scale to Send", "failed to close output stream", e)
            }
        }

        return file
    }

    var i: Int = -1
    override fun sendSms(message: Message, addresses: List<String>, x: Int) {
        if (addresses.size > 1) {
            if (i != x) {
                i = x

            } else {
                return
            }
            if (addresses.isEmpty()) {
                val smsManager = message.subId.takeIf { it != -1 }
                    ?.let(SmsManagerFactory::createSmsManager)
                    ?:  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
                        val smsManager2 = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                        smsManager2} else {
                        SmsManager.getDefault()
                    }

                val parts = smsManager
                    .divideMessage(if (prefs.unicode.get()) StripAccents.stripAccents(message.body) else message.body)
                    ?: arrayListOf()


                val sentIntents = parts.map {
                    val intent =
                        Intent(context, SmsSentReceiver::class.java).putExtra("id", message.id)
                    PendingIntent.getBroadcast(
                        context,
                        message.id.toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }

                val deliveredIntents = parts.map {
                    val intent =
                        Intent(context, SmsDeliveredReceiver::class.java).putExtra("id", message.id)
                    val pendingIntent = PendingIntent
                        .getBroadcast(
                            context,
                            message.id.toInt(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    if (prefs.delivery.get()) pendingIntent else null
                }

                try {
                    smsManager.sendMultipartTextMessage(
                        message.address,
                        null,
                        parts,
                        ArrayList(sentIntents),
                        ArrayList(deliveredIntents)
                    )
                } catch (e: IllegalArgumentException) {
                    logDebug("errorr" + "sendSms1: " + e.message)
                    Timber.w(e, "Message body lengths: ${parts.map { it?.length }}")
                    markFailed("", message.id, Telephony.MmsSms.ERR_TYPE_GENERIC)
                }
            } else {
                if (addresses[i].length != null) {
                    val smsManager = message.subId.takeIf { it != -1 }
                        ?.let(SmsManagerFactory::createSmsManager)
                        ?:  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
                            val smsManager2 = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                            smsManager2} else {
                            SmsManager.getDefault()
                        }
                    val parts = smsManager
                        .divideMessage(if (prefs.unicode.get()) StripAccents.stripAccents(message.body) else message.body)
                        ?: arrayListOf()

                    try {
                        val sentIntents = parts.map {
                            val intent = Intent(context, SmsSentReceiver::class.java).putExtra(
                                "id",
                                message.id
                            )
                            PendingIntent.getBroadcast(
                                context,
                                message.id.toInt(),
                                intent,
                                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                            )
                        }

                        val deliveredIntents = parts.map {
                            val intent = Intent(context, SmsDeliveredReceiver::class.java).putExtra(
                                "id",
                                message.id
                            )
                            val pendingIntent = PendingIntent
                                .getBroadcast(
                                    context,
                                    message.id.toInt(),
                                    intent,
                                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                                )
                            if (prefs.delivery.get()) pendingIntent else null
                        }
                        smsManager.sendMultipartTextMessage(
                            addresses[i],
                            null,
                            parts,
                            ArrayList(sentIntents),
                            ArrayList(deliveredIntents)
                        )
                    } catch (e: IllegalArgumentException) {
                        logDebug("errorr" + "sendSms: " + e.message)
                        markFailed("", message.id, Telephony.MmsSms.ERR_TYPE_GENERIC)
                    }
                }

            }
        } else {
            if (addresses.isEmpty()) {
                val smsManager = message.subId.takeIf { it != -1 }
                    ?.let(SmsManagerFactory::createSmsManager)
                    ?:  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
                        val smsManager2 = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                        smsManager2} else {
                        SmsManager.getDefault()
                    }

                val parts = smsManager
                    .divideMessage(if (prefs.unicode.get()) StripAccents.stripAccents(message.body) else message.body)
                    ?: arrayListOf()

                val sentIntents = parts.map {
                    val intent =
                        Intent(context, SmsSentReceiver::class.java).putExtra("id", message.id)
                    PendingIntent.getBroadcast(
                        context,
                        message.id.toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }

                val deliveredIntents = parts.map {
                    val intent =
                        Intent(context, SmsDeliveredReceiver::class.java).putExtra("id", message.id)
                    val pendingIntent = PendingIntent
                        .getBroadcast(
                            context,
                            message.id.toInt(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    if (prefs.delivery.get()) pendingIntent else null
                }

                try {
                    smsManager.sendMultipartTextMessage(
                        message.address,
                        null,
                        parts,
                        ArrayList(sentIntents),
                        ArrayList(deliveredIntents)
                    )
                } catch (e: IllegalArgumentException) {
                    logDebug("errorr" + "sendSms2: " + e.message)
                    Timber.w(e, "Message body lengths: ${parts.map { it?.length }}")
                    markFailed("", message.id, Telephony.MmsSms.ERR_TYPE_GENERIC)
                }
            } else {
                try {


                    val smsManager = message.subId.takeIf { it != -1 }
                        ?.let(SmsManagerFactory::createSmsManager)
                        ?:  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
                            val smsManager2 = SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                            smsManager2} else {
                            SmsManager.getDefault()
                        }
                    val parts = smsManager
                        .divideMessage(if (prefs.unicode.get()) StripAccents.stripAccents(message.body) else message.body)
                        ?: arrayListOf()

                    try {
                        val sentIntents = parts.map {
                            val intent = Intent(context, SmsSentReceiver::class.java).putExtra(
                                "id",
                                message.id
                            )
                            PendingIntent.getBroadcast(
                                context,
                                message.id.toInt(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                        }

                        val deliveredIntents = parts.map {
                            val intent = Intent(context, SmsDeliveredReceiver::class.java).putExtra(
                                "id",
                                message.id
                            )
                            val pendingIntent = PendingIntent
                                .getBroadcast(
                                    context,
                                    message.id.toInt(),
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                            if (prefs.delivery.get()) pendingIntent else null
                        }
                        smsManager.sendMultipartTextMessage(
                            message.address,
                            null,
                            parts,
                            ArrayList(sentIntents),
                            ArrayList(deliveredIntents)
                        )

                    } catch (e: IllegalArgumentException) {
                        logDebug("errorr" + "sendSms3: " + e.message)
                        Timber.w(e, "Message body lengths: ${parts.map { it?.length }}")
                        markFailed("", message.id, Telephony.MmsSms.ERR_TYPE_GENERIC)
                    }


                } catch (e: java.lang.Exception) {
                    Log.d("sendSms", "sendSms: " + e.message)

                }
            }
        }
    }

    override fun resendMms(message: Message) {
        val subId = message.subId
        val threadId = message.threadId
        val pdu = tryOrNull {
            PduPersister.getPduPersister(context).load(message.getUri()) as MultimediaMessagePdu
        } ?: return

        val addresses = pdu.to.map { it.string }.filter { it.isNotBlank() }
        val parts = message.parts.mapNotNull { part ->
            val bytes = tryOrNull(false) {
                context.contentResolver.openInputStream(part.getUri())
                    ?.use { inputStream -> inputStream.readBytes() }
            } ?: return@mapNotNull null

            MMSPart(part.name.orEmpty(), part.type, bytes)
        }

        Transaction(context).sendNewMessage(
            subId,
            threadId,
            addresses,
            parts,
            message.subject,
            message.getUri()
        )
    }

    override fun cancelDelayedSms(id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getIntentForDelayedSms(id))
    }

    private fun getIntentForDelayedSms(id: Long): PendingIntent {
        val intent = Intent(context, SendSmsReceiver::class.java).putExtra("id", id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )        }
    }

    override fun insertSentSms(
        subId: Int,
        threadId: Long,
        address: String,
        body: String,
        date: Long
    ): Message {
        val cl = Calendar.getInstance()
        val message = Message().apply {
            this.threadId = threadId
            this.address = address
            this.body = body
            this.date = date
            this.datecompare = toDate(DateFormat.format("yyyy-MM-dd", Date(date)).toString())
            cl.timeInMillis = date
            this.month = cl.get(Calendar.MONTH).toLong()
            this.year = cl.get(Calendar.YEAR).toLong()
            this.subId = subId

            id = messageIds.newId()
            boxId = Sms.MESSAGE_TYPE_OUTBOX
            type = "sms"
            read = true
            seen = true
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: Message? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        val values = contentValuesOf(
            Sms.ADDRESS to address,
            Sms.BODY to body,
            Sms.DATE to System.currentTimeMillis(),
            Sms.READ to true,
            Sms.SEEN to true,
            Sms.TYPE to Sms.MESSAGE_TYPE_OUTBOX,
            Sms.THREAD_ID to threadId
        )

        if (prefs.canUseSubId.get()) {
            values.put(Sms.SUBSCRIPTION_ID, message.subId)
        }

        val uri = context.contentResolver.insert(Sms.CONTENT_URI, values)

        uri?.lastPathSegment?.toLong()?.let { id ->
            realm.executeTransaction { managedMessage?.takeIf { it.isValid }?.contentId = id }
        }
        realm.close()

        if (threadId == 0L) {
            uri?.let(syncRepository::syncMessage)
        }

        return message
    }

    override fun insertReceivedSms(
        subId: Int,
        address: String,
        body: String,
        sentTime: Long
    ): Message {
        val cl = Calendar.getInstance()
        val message = Message().apply {
            this.address = address
            this.body = body
            this.dateSent = sentTime
            this.date = System.currentTimeMillis()
            this.datecompare =
                toDate(DateFormat.format("yyyy-MM-dd", Date(System.currentTimeMillis())).toString())
            cl.timeInMillis = System.currentTimeMillis()
            this.month = cl.get(Calendar.MONTH).toLong()
            this.year = cl.get(Calendar.YEAR).toLong()
            this.subId = subId

            id = messageIds.newId()
            threadId = TelephonyCompat.getOrCreateThreadId(context, address)
            boxId = Sms.MESSAGE_TYPE_INBOX
            type = "sms"
            read = activeConversationManager.getActiveConversation() == threadId
        }
        val realm = Realm.getDefaultInstance()
        var managedMessage: Message? = null
        realm.executeTransaction { managedMessage = realm.copyToRealmOrUpdate(message) }

        val values = contentValuesOf(
            Sms.ADDRESS to address,
            Sms.BODY to body,
            Sms.DATE_SENT to sentTime
        )

        if (prefs.canUseSubId.get()) {
            values.put(Sms.SUBSCRIPTION_ID, message.subId)
        }

        context.contentResolver.insert(Sms.Inbox.CONTENT_URI, values)?.lastPathSegment?.toLong()
            ?.let { id ->
                realm.executeTransaction { managedMessage?.contentId = id }
            }

        realm.close()

        return message
    }

    override fun markSending(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                realm.executeTransaction {
                    message.boxId = when (message.isSms()) {
                        true -> Sms.MESSAGE_TYPE_OUTBOX
                        false -> Mms.MESSAGE_BOX_OUTBOX
                    }
                }

                val values = when (message.isSms()) {
                    true -> contentValuesOf(Sms.TYPE to Sms.MESSAGE_TYPE_OUTBOX)
                    false -> contentValuesOf(Mms.MESSAGE_BOX to Mms.MESSAGE_BOX_OUTBOX)
                }
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }


    override fun markSent(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                realm.executeTransaction {
                    message.boxId = Sms.MESSAGE_TYPE_SENT
                }

                val values = ContentValues()
                values.put(Sms.TYPE, Sms.MESSAGE_TYPE_SENT)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
//        if (!Preferences.getBoolean(this, Preferences.ADSREMOVED)) {
//            MaxAdManager.showInterAd(this,object : OnAdShowCallback{
//                override fun onAdHidden(ishow: Boolean) {
//                    Log.w("MainActivity", "onAdHidden")
//                }
//
//                override fun onAdfailed() {
//                    Log.w("MainActivity", "onAdfailed")
//                }
//
//                override fun onAdDisplay() {
//                    Log.w("MainActivity", "onAdDisplay")
//                }
//
//            })
//        }
    }

    override fun markFailed(address: String, id: Long, resultCode: Int) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()
            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                realm.executeTransaction {

                    message.boxId = Sms.MESSAGE_TYPE_FAILED
                    message.errorCode = resultCode
                    message.group_msg.add(address)
                    logDebug("ffffffffffffffffffffff" + message.group_msg)
                }

                val values = ContentValues()
                values.put(Sms.TYPE, Sms.MESSAGE_TYPE_FAILED)
                values.put(Sms.ERROR_CODE, resultCode)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun markDelivered(id: Long) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                realm.executeTransaction {

                    message.deliveryStatus = Sms.STATUS_COMPLETE
                    message.dateSent = System.currentTimeMillis()
                    message.read = true
                }

                val values = ContentValues()
                values.put(Sms.STATUS, Sms.STATUS_COMPLETE)
                values.put(Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Sms.READ, true)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    var mDebugLog = true
    var mDebugTag = "messagefailed"
    fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

    override fun markDeliveryFailed(id: Long, resultCode: Int) {
        Realm.getDefaultInstance().use { realm ->
            realm.refresh()

            val message = realm.where(Message::class.java).equalTo("id", id).findFirst()
            message?.let {
                realm.executeTransaction {
                    message.deliveryStatus = Sms.STATUS_FAILED
                    message.dateSent = System.currentTimeMillis()
                    message.read = true
                    message.errorCode = resultCode


                }
                val values = ContentValues()
                values.put(Sms.STATUS, Sms.STATUS_FAILED)
                values.put(Sms.DATE_SENT, System.currentTimeMillis())
                values.put(Sms.READ, true)
                values.put(Sms.ERROR_CODE, resultCode)
                context.contentResolver.update(message.getUri(), values, null, null)
            }
        }
    }

    override fun deleteMessages(vararg messageIds: Long) {
        var realm: Realm? = null

        try {
            realm = Realm.getDefaultInstance()
            realm.use { realm ->
                realm.refresh()

                val messages = realm.where(Message::class.java)
                    .anyOf("id", messageIds)
                    .findAll()

                val uris = messages.map { it.getUri() }

                realm.executeTransaction { messages.deleteAllFromRealm() }

                uris.forEach { uri -> context.contentResolver.delete(uri, null, null) }
            }
        } finally {
            realm!!.close()
        }
    }

    fun toDate(dateString: String): Date? {
        var date: Date? = null
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        try {
            date = formatter.parse(dateString)

        } catch (e1: ParseException) {
            e1.printStackTrace()

        }

        return date
    }


}