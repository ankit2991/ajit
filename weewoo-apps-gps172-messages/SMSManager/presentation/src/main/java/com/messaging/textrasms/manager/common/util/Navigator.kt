package com.messaging.textrasms.manager.common.util

import android.app.Activity
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.provider.Telephony
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.calldorado.Calldorado
import com.messaging.textrasms.manager.BuildConfig
import com.messaging.textrasms.manager.FilterActivity
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QKApplication
import com.messaging.textrasms.manager.feature.Activities.*
import com.messaging.textrasms.manager.feature.backup.BackupActivity
import com.messaging.textrasms.manager.feature.blocking.BlockingActivity
import com.messaging.textrasms.manager.feature.compose.ComposeActivity
import com.messaging.textrasms.manager.feature.conversationinfo.ConversationInfoActivity
import com.messaging.textrasms.manager.feature.gallery.GalleryActivity
import com.messaging.textrasms.manager.feature.inapppurchase.InAppPurchaseActivity
import com.messaging.textrasms.manager.feature.notificationprefs.NotificationPrefsActivity
import com.messaging.textrasms.manager.feature.scheduled.ScheduledActivity
import com.messaging.textrasms.manager.feature.settings.SettingsActivity
import com.messaging.textrasms.manager.manager.MyNotificationManager
import com.messaging.textrasms.manager.manager.PermissionManager
import com.messaging.textrasms.manager.utils.Constants
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor(
    private val context: Context,
    private val myNotificationManager: MyNotificationManager,
    private val permissions: PermissionManager
) {

    private fun startActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun startActivityExternal(intent: Intent) {
        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent.createChooser(intent, null))
        }
    }

    fun showRateUsPlayStore() {
        try {
            val playstoreuri1: Uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
            val playstoreIntent1 = Intent(Intent.ACTION_VIEW, playstoreuri1)
            startActivity(playstoreIntent1)
        } catch (exp: Exception) {
            val playstoreuri2: Uri =
                Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
            val playstoreIntent2 = Intent(Intent.ACTION_VIEW, playstoreuri2)
            startActivity(playstoreIntent2)
        }
    }

    fun showPrivacyPolicy() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(context.getString(R.string.privacy_policy_link))
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(context.getString(R.string.privacy_policy_link))
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e2: Exception) {
                Toast.makeText(context, "Unable to find market app", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun openUrl(url:String) {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (e2: Exception) {
                Toast.makeText(context, "Unable to find market app", Toast.LENGTH_LONG).show()
            }
        }
    }




    /**
     * This won't work unless we use startActivityForResult
     */
    fun showDefaultSmsDialog(context: Activity) {
        try {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(RoleManager::class.java) as RoleManager
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                context.startActivityForResult(intent, 42389)
            } else {
                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                context.startActivityForResult(intent, 42389)
            }
        } catch (e: java.lang.Exception) {

        }
    }

    fun showCompose(body: String? = null, images: List<Uri>? = null) {

        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        images?.takeIf { it.isNotEmpty() }?.let {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(images))
        }
        startActivity(intent)


    }

    fun showgroupContacts(
        body: String? = null,
        images: List<Uri>? = null,
        boolean: Boolean = true
    ) {
        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        intent.putExtra("fromgroup", boolean)
        images?.takeIf { it.isNotEmpty() }?.let {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(images))
        }
        startActivity(intent)
    }

    //     fun requestStoragePermissions() {
//        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
//    }
    fun showConversation(threadId: Long, query: String? = null) {
//        if (permissions.hasStorage()) {
        val intent = Intent(context, ComposeActivity::class.java)
            .putExtra("threadId", threadId)
            .putExtra("query", query)

        startActivity(intent)
//        }
//        else{
//           // requestStoragePermissions()
//        }
    }

    fun showConversation(activity: Activity, threadId: Long, query: String? = null) {
//        if (permissions.hasStorage()) {
        val intent = Intent(context, ComposeActivity::class.java)
            .putExtra("threadId", threadId)
            .putExtra("query", query)
        startActivity(intent)
        if (activity != null) {
            activity.overridePendingTransition(R.anim.enter, R.anim.exit)

        }
//        }
//        else{
//           // requestStoragePermissions()
//        }
    }

    fun showConversationInfo(threadId: Long) {
        val intent = Intent(context, ConversationInfoActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    fun showMedia(partId: Long) {
        val intent = Intent(context, GalleryActivity::class.java)
        intent.putExtra("partId", partId)
        startActivity(intent)
    }

    fun showBackup() {
        startActivity(Intent(context, BackupActivity::class.java))
    }

    fun showScheduled() {
        val intent = Intent(context, ScheduledActivity::class.java)
        startActivity(intent)
    }

    fun showSettings() {
        val intent = Intent(context, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun showRemoveAds() {
        startActivity(Intent(context, PurchaseActivity::class.java))
    }

    fun showRemoveAdsCustom() {
        val intent = Intent(context, CustomAdLayoutActivity::class.java)
        startActivity(intent)
    }

    fun showArchiveActivity() {
        startActivity(Intent(context, Archiveactivity::class.java))
    }

    fun showTransactionalActivity() {
        startActivity(Intent(context, Transactionactivity::class.java))
    }

    fun showPromotionalActivity() {
        startActivity(Intent(context, Promotionalactivity::class.java))
    }

    fun showSpamActivity() {
        startActivity(Intent(context, SpamActivity::class.java))
    }

    fun showFilterActivity() {
        startActivity(Intent(context, FilterActivity::class.java))
    }

    fun showBlockedConversations() {
        val intent = Intent(context, BlockingActivity::class.java)
        startActivity(intent)
    }

    fun makePhoneCall(address: String) {
        val action = if (permissions.hasCalling()) Intent.ACTION_CALL else Intent.ACTION_DIAL
        val intent = Intent(action, Uri.parse("tel:$address"))
        startActivityExternal(intent)
    }

    fun showSupport() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("talking.tech.app@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "SMS Support")
        intent.putExtra(
            Intent.EXTRA_TEXT, StringBuilder("\n\n")
                .append("\n\n--- Please write your message above this line ---\n\n")
                .append("Package: ${context.packageName}\n")
                .append("Version: ${BuildConfig.VERSION_NAME}\n")
                .append("Device: ${Build.BRAND} ${Build.MODEL}\n")
                .append("SDK: ${Build.VERSION.SDK_INT}\n")
                .toString()
        )
        startActivityExternal(intent)
    }

    fun showInvite() {
        val urtText = ("Please Download Amazing " + context.getString(R.string.in_app_name)
                + " from google play store:" + "\nhttp://play.google.com/store/apps/details?id=" +
                context.packageName)
        Intent(Intent.ACTION_SEND)
            .setType("text/html")
            .putExtra(Intent.EXTRA_SUBJECT, context.resources.getString(R.string.in_app_name))
            .putExtra(Intent.EXTRA_TEXT, urtText)
            .let { Intent.createChooser(it, "Invite friend") }
            .let(this::startActivityExternal)
    }

    fun addContact(address: String) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setType(ContactsContract.Contacts.CONTENT_TYPE)
            .putExtra(ContactsContract.Intents.Insert.PHONE, address)

        startActivityExternal(intent)
    }

    fun showContact(lookupKey: String) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey))

        startActivityExternal(intent)
    }

    fun viewFile(file: File) {
        val data = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.name.split(".").last())
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(data, type)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivityExternal(intent)
    }

    fun shareFile(file: File) {
        val data = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.name.split(".").last())
        val intent = Intent(Intent.ACTION_SEND)
            .setType(type)
            .putExtra(Intent.EXTRA_STREAM, data)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivityExternal(intent)
    }

    fun shareApp() {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND

        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=" + context.packageName
        )
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Share with"))
    }

    fun showNotificationSettings(threadId: Long = 0) {
        val intent = Intent(context, NotificationPrefsActivity::class.java)
        intent.putExtra("threadId", threadId)
        startActivity(intent)
    }

    fun showNotificationChannel(threadId: Long = 0) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (threadId != 0L) {
                    myNotificationManager.createNotificationChannel(threadId)
                }

                val channelId = myNotificationManager.buildNotificationChannelId(threadId)
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                startActivity(intent)
            }
        } catch (E: java.lang.Exception) {

        }
    }

    fun showCalldorado() {

        (context as QKApplication).currentActivity?.let {
            Constants.IS_FROM_ACTIVITY = true
            Calldorado.createSettingsActivity(it)
        }
    }
}
