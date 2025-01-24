package com.messaging.textrasms.manager.common.base

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.messaging.textrasms.manager.common.util.MenuItemAdapter
import com.messaging.textrasms.manager.common.util.extensions.dpToPx
import com.messaging.textrasms.manager.common.util.extensions.setPadding
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.util.Preferences
import javax.inject.Inject

/**
 * Wrapper around AlertDialog which makes it easier to display lists that use our UI
 */
class QkDialog @Inject constructor(private val context: Context, val adapter: MenuItemAdapter) {

    var title: String? = null

    @Inject
    lateinit var prefs: Preferences

    init {
        appComponent.inject(this)
    }

    fun show(activity: Activity) {

        val recyclerView = RecyclerView(activity)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.setPadding(top = 8.dpToPx(context), bottom = 8.dpToPx(context))

        val dialog = AlertDialog.Builder(activity)
            .setTitle(title)
            .setView(recyclerView)
            .create()

        val clicks = adapter.menuItemClicks

            .subscribe {
                if (dialog.isShowing && !activity.isFinishing) {
                    dialog.dismiss()
                    activity.recreate()
                }

            }

        dialog.setOnDismissListener {
            clicks.dispose()
        }
        if (!activity.isFinishing) {
            try {
                dialog.show()
            } catch (E: Exception) {

            }

        }
    }

    fun setTitle(@StringRes title: Int) {
        this.title = context.getString(title)
    }
}