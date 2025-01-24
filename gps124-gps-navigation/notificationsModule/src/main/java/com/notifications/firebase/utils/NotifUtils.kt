package com.notifications.firebase.utils

import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.RemoteViews
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.NotificationCompat
import com.notifications.firebase.BuildConfig
import com.notifications.firebase.R
import com.notifications.firebase.services.MessagingService
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


object NotifUtils {

    fun sendAppInstallNotification(context: Context?,/* iconURL: String, title: String, shortDesc: String, longDesc: String?,
                                   feature: String?, appURL: String, notificationID: Int*/data: Map<String, String>?): Boolean {
        if (context == null) return false

        val iconURL = data?.get(MessagingService.ICON_KEY)
        val title = data?.get(MessagingService.APP_TITLE_KEY)
        val shortDesc = data?.get(MessagingService.SHORT_DESC_KEY)
        val longDesc = data?.get(MessagingService.LONG_DESC_KEY)
        val feature = data?.get(MessagingService.APP_FEATURE_KEY)
        val appURL = data?.get(MessagingService.APP_URL_KEY)
        val shouldOpenApp = data?.get(MessagingService.SHOULD_OPEN_APP)

        val notificationID = MessagingService.getNextInt()
        if (shouldOpenApp.equals("yes")){
            if (iconURL != null && title != null && shortDesc != null && feature != null && appURL != null) {
                val standard = "https://play.google.com/store/apps/details?id="
                try {
                    val id = appURL.substring(standard.length)
                    if (BuildConfig.DEBUG) Log.e("package sent ", id)

                    if (!isAppInstalled(id, context) && !NotifTinyDB.getInstance(context).getBoolean(MessagingService.IS_PREMIUM))
                        GlobalScope.launch(Dispatchers.Main)  {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appURL))
                            val pendingIntent = PendingIntent.getActivity(context, 0 /* request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                            val remoteViews = RemoteViews(context.packageName, R.layout.notification_app)

                            remoteViews.setTextViewText(R.id.tv_title, title)
                            remoteViews.setTextViewText(R.id.tv_short_desc, shortDesc)
                            remoteViews.setTextViewText(R.id.tv_long_desc, longDesc)
                            remoteViews.setViewVisibility(R.id.tv_long_desc, if (longDesc != null && !longDesc.isEmpty()) View.VISIBLE else View.GONE)

                            val builder = NotificationCompat.Builder(context, title)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setSmallIcon(R.drawable.ic_ad_small)
                                .setContentIntent(pendingIntent)
                                .setOnlyAlertOnce(true)
                                .setAutoCancel(true)
                                .setCustomContentView(remoteViews)
                                .setCustomBigContentView(remoteViews)

                            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val channel = NotificationChannel(title,
                                    "Channel human readable title",
                                    NotificationManager.IMPORTANCE_DEFAULT)
                                mNotificationManager.createNotificationChannel(channel)
                            }

                            mNotificationManager.notify(notificationID, builder.build())
                            Picasso.get()
                                .load(iconURL)
                                .into(remoteViews, R.id.iv_icon, notificationID, builder.build())
                            Picasso.get()
                                .load(feature)
                                .into(remoteViews, R.id.iv_feature, notificationID, builder.build())

                        }
                    return true
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) Log.e("MsgingService", "package not valid")
                }
            }
        }else{
            if (iconURL != null && title != null && shortDesc != null && feature != null && appURL != null) {
                val standard = "https://play.google.com/store/apps/details?id="
                try {
                    val id = appURL.substring(standard.length)
                    if (BuildConfig.DEBUG) Log.e("package sent ", id)

                    if (!isAppInstalled(id, context) && !NotifTinyDB.getInstance(context).getBoolean(MessagingService.IS_PREMIUM))
                        GlobalScope.launch(Dispatchers.Main)  {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appURL))
                            val pendingIntent = PendingIntent.getActivity(context, 0 /* request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                            val remoteViews = RemoteViews(context.packageName, R.layout.notification_app)

                            remoteViews.setTextViewText(R.id.tv_title, title)
                            remoteViews.setTextViewText(R.id.tv_short_desc, shortDesc)
                            remoteViews.setTextViewText(R.id.tv_long_desc, longDesc)
                            remoteViews.setViewVisibility(R.id.tv_long_desc, if (longDesc != null && !longDesc.isEmpty()) View.VISIBLE else View.GONE)

                            val builder = NotificationCompat.Builder(context, title)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setSmallIcon(R.drawable.ic_ad_small)
                                .setContentIntent(pendingIntent)
                                .setOnlyAlertOnce(true)
                                .setAutoCancel(true)
                                .setCustomContentView(remoteViews)
                                .setCustomBigContentView(remoteViews)

                            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val channel = NotificationChannel(title,
                                    "Channel human readable title",
                                    NotificationManager.IMPORTANCE_DEFAULT)
                                mNotificationManager.createNotificationChannel(channel)
                            }

                            mNotificationManager.notify(notificationID, builder.build())
                            Picasso.get()
                                .load(iconURL)
                                .into(remoteViews, R.id.iv_icon, notificationID, builder.build())
                            Picasso.get()
                                .load(feature)
                                .into(remoteViews, R.id.iv_feature, notificationID, builder.build())

                        }
                    return true
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) Log.e("MsgingService", "package not valid")
                }
            }
        }

        return false
    }


    private fun isAppInstalled(uri: String, context: Context): Boolean {
        val pm = context.packageManager
        return try {
            val applicationInfo = pm.getApplicationInfo(uri, 0)
            //            packageInfo
            applicationInfo.enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

    }

    fun sendUpdateMsg(notifTinyDB: NotifTinyDB, data: Map<String, String>?) {
        if (BuildConfig.DEBUG) Log.e("sendUpdateMsg", "package ${data?.get(MessagingService.APP_URL_KEY)}")

        val appURL = data?.get(MessagingService.APP_URL_KEY)
        val updateMsg = data?.get(MessagingService.UPDATE_MSG_KEY)
        val isCancelable = if (data?.get(MessagingService.IS_CANCELABLE_KEY) != null) data[MessagingService.IS_CANCELABLE_KEY]!!.toBoolean() else false

        notifTinyDB.putBoolean(MessagingService.IS_CANCELABLE_KEY, isCancelable)
        notifTinyDB.putString(MessagingService.UPDATE_MSG_KEY, updateMsg)
        notifTinyDB.putString(MessagingService.APP_URL_KEY, appURL)
    }

    fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(stringId)
    }

    fun showDialog(ctx: Context, title: String, msg: String, isCancelable: Boolean,
                   listener: View.OnClickListener) {

//        val builder = android.app.AlertDialog.Builder(ctx/*, R.style.MyAlertDialogTheme*/)
        val dlg = Dialog(ctx)

        var inflater: LayoutInflater = LayoutInflater.from(ctx)
        var dialogView: View = inflater.inflate(R.layout.dlg_update_app, null)
//        builder.setView(R.layout.dlg_update_app)
        dlg.addContentView(dialogView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        var tvTitle: TextView = dialogView.findViewById(R.id.title)
        var tvMsg: TextView = dialogView.findViewById(R.id.msg)
        var positiveBtn: AppCompatButton = dialogView.findViewById(R.id.btn_positive)

        tvTitle.text = title
        tvMsg.text = msg
        positiveBtn.setOnClickListener(listener)
        dlg.setCancelable(isCancelable)
//        builder.setMessage(msg).setCancelable(isCancelable).setTitle(title)
//                .setPositiveButton("Update", null)


//        val alert = builder.create()
//        alert.setOnShowListener {
//            val button = (alert as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
//            button.setOnClickListener(listener)
//        }
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dlg?.window?.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        dlg?.window?.attributes = lp

        dlg.show()

        dlg?.window?.setLayout(lp.width, getDisplayHeight(ctx))

//        alert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.WHITE)
    }

    private fun getDisplayHeight(context: Context): Int {
        val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay

        return display.height
    }

    fun openUrl(context: Context?, url: String) {
        try {
            val builder = CustomTabsIntent.Builder()
            context?.let { getThemePrimaryColor(it) }?.let { builder.setToolbarColor(it) }
            val customTabsIntent = builder.build()
            context?.let { customTabsIntent.launchUrl(context, Uri.parse(url)) }
        }catch (ee:Exception)
        {

        }
    }

    private fun getThemePrimaryColor(context: Context): Int {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, value, true)
        return value.data
    }
}
