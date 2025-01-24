package com.messaging.textrasms.manager.feature.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.feature.Activities.MainActivity
import com.messaging.textrasms.manager.feature.compose.ComposeActivity
import com.messaging.textrasms.manager.manager.WidgetManager
import com.messaging.textrasms.manager.util.Preferences
import com.messaging.textrasms.manager.util.getColorCompat
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class WidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var prefs: Preferences

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        when (intent.action) {
            WidgetManager.ACTION_NOTIFY_DATASET_CHANGED -> updateData(context)
            else -> super.onReceive(context, intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        updateData(context)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetId, isSmallWidget(appWidgetManager, appWidgetId))
        }
    }

    private fun updateData(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds =
            appWidgetManager.getAppWidgetIds(ComponentName(context, WidgetProvider::class.java))

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.conversations)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateWidget(context, appWidgetId, isSmallWidget(appWidgetManager, appWidgetId))
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    private fun isSmallWidget(appWidgetManager: AppWidgetManager, appWidgetId: Int): Boolean {
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val size = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)

        var n = 2
        while (70 * n - 30 < size) {
            ++n
        }

        val columns = n - 1

        return columns < 4
    }

    private fun updateWidget(context: Context, appWidgetId: Int, smallWidget: Boolean) {
        Timber.v("updateWidget appWidgetId: $appWidgetId")
        val remoteViews = RemoteViews(context.packageName, R.layout.widget)

        val night = prefs.night.get()
        val black = prefs.black.get()

        remoteViews.setInt(
            R.id.background, "setColorFilter", context.getColorCompat(
                when {
                    night && black -> R.color.black
                    night && !black -> R.color.backgroundDark
                    else -> R.color.white
                }
            )
        )

        remoteViews.setInt(
            R.id.toolbar, "setColorFilter", context.getColorCompat(
                when {
                    night && black -> R.color.black
                    night && !black -> R.color.backgroundDark
                    else -> R.color.backgroundLight
                }
            )
        )

        remoteViews.setTextColor(
            R.id.title, context.getColorCompat(
                when (night) {
                    true -> R.color.textPrimaryDark
                    false -> R.color.textPrimary
                }
            )
        )

        remoteViews.setInt(R.id.compose, "setColorFilter", colors.theme().theme)

        val intent = Intent(context, WidgetService::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .putExtra("small_widget", smallWidget)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        remoteViews.setRemoteAdapter(R.id.conversations, intent)

        val mainIntent = Intent(context, MainActivity::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val mainPI =
            PendingIntent.getActivity(context, PendingIntent.FLAG_MUTABLE, mainIntent, PendingIntent.FLAG_IMMUTABLE)
        remoteViews.setOnClickPendingIntent(R.id.title, mainPI)

        val composeIntent = Intent(context, ComposeActivity::class.java)
        val composePI =
            PendingIntent.getActivity(context, PendingIntent.FLAG_MUTABLE, composeIntent, PendingIntent.FLAG_IMMUTABLE)
        remoteViews.setOnClickPendingIntent(R.id.compose, composePI)

        val startActivityIntent = Intent(context, ComposeActivity::class.java)
        val startActivityPendingIntent = PendingIntent.getActivity(
            context,
            PendingIntent.FLAG_MUTABLE,
            startActivityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        remoteViews.setPendingIntentTemplate(R.id.conversations, startActivityPendingIntent)

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteViews)
    }

}
