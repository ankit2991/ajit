package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.gpsnavigation.maps.gpsroutefinder.routemap.R

class LoadingDialog private constructor() {

    fun showDialog(message: String?): Dialog? {
        initDialog(context)
        val tvMessage =
            dialog?.findViewById<TextView>(R.id.tv_dialog_message)
        if (message != null) tvMessage?.text = message
        dialog?.let {
            if (!it.isShowing)
                it.show()
        }
        return dialog
    }

    private fun initDialog(context: Context?) {
        context?.let {
            dialog = Dialog(it)
            dialog?.let {
                it.requestWindowFeature(Window.FEATURE_NO_TITLE)
                it.setContentView(R.layout.dialog_loading)
                it.window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.setCancelable(false)
            }

        }
    }

    fun dismissDialog() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }


    companion object {
        private var dialogUtility: LoadingDialog? = null
        var dialog: Dialog? = null
            private set
        private var context: Context? = null
        fun getInstance(con: Context?): LoadingDialog? {
            context = con
            return if (dialogUtility == null) {
                dialogUtility = LoadingDialog()
                dialogUtility
            } else {
                dialogUtility
            }
        }

    }
}