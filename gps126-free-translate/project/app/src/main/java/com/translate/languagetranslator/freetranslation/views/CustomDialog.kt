package com.translate.languagetranslator.freetranslation.views

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.translate.languagetranslator.freetranslation.R

class CustomDialog private constructor() {

    companion object {
        private var dialogUtility: CustomDialog? = null
        private var dialog: Dialog? = null
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

    fun setContentView(view: View?, isCancelable: Boolean): CustomDialog? {
        dialog = Dialog(context!!)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(view!!)
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(isCancelable)
        dialog?.window?.attributes?.windowAnimations = R.style.dialogueStyle
        return dialogUtility
    }


    fun showDialog(): Dialog? {
        if (context is Activity){
            if(!(context as Activity).isFinishing)
                dialog?.show()
        }
        return dialog
    }

    fun dismissDialog() {
        dialog?.let {
            if (it.isShowing)
                it.dismiss()
        }
    }


}