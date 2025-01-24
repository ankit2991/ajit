package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.util.Calendar

fun getDefaultRouteName(): String {
    return "My Route " + java.util.Calendar.getInstance().get(java.util.Calendar.DATE) + "." + (java.util.Calendar.getInstance().get(
        java.util.Calendar.MONTH
    ) + 1) + "." + java.util.Calendar.getInstance().get(
        java.util.Calendar.YEAR
    )
}

fun openEmailComposer(
    context: Context,
    email: String,
    subject: String,
    body: String
): Boolean {
    val recipients = arrayOf(email)
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:")
    intent.putExtra(Intent.EXTRA_EMAIL, recipients)
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, body)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    return try {
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}

fun openDeviceBrowser(context: Context, url: String?): Boolean {

    val openIntent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        setDataAndType(Uri.parse(url), "text/html")
    }

    return try {
        context.startActivity(openIntent)
        true
    } catch (ex: Exception) {
        try {
            openIntent.addCategory(Intent.CATEGORY_BROWSABLE)
            context.startActivity(openIntent)
            true
        } catch (ex: Exception) {
            false
        }
    }
}
