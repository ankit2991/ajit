package com.gpsnavigation.maps.gpsroutefinder.routemap.utility

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.view.WindowManager

class CustomDialog private constructor() {

    fun setContentView(view: View?, isCancelable: Boolean): CustomDialog? {
        context?.let {
            dialog = Dialog(it)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog?.setContentView(view!!)
            dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.setCancelable(isCancelable)
            return dialogUtility
        }
        return null
    }

    fun showDialog(): Dialog? {
        dialog?.let {
            if(!it.isShowing)
                dialog?.show()
        }
        return dialog
    }

    fun dismissDialog() {
        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    companion object {
        private var dialogUtility: CustomDialog? = null
        var dialog: Dialog? = null
            private set
        private var context: Context? = null
        fun getInstance(con: Context?): CustomDialog? {
            context = con
            return if (dialogUtility == null) {
                dialogUtility = CustomDialog()
                dialogUtility
            } else {
                dialogUtility
            }
        }
    }
}