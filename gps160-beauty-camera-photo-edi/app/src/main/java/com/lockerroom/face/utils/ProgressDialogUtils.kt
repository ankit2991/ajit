package com.lockerroom.face.utils

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import androidx.constraintlayout.widget.ConstraintLayout
import com.lockerroom.face.R

object ProgressDialogUtils {

    private lateinit var loadingDialog: Dialog
    private var mWindow: Window? = null


    fun showAdLoading(activity: Activity) {

        if (ProgressDialogUtils::loadingDialog.isInitialized && loadingDialog.isShowing) {
            return
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        loadingDialog = Dialog(activity)

        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setCanceledOnTouchOutside(false)
        loadingDialog.setContentView(R.layout.ad_loading_screen)
        mWindow = loadingDialog.window
        loadingDialog.window?.setGravity(Gravity.CENTER)
        mWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mWindow!!.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        loadingDialog.show()
    }

    fun dismissAdLoading() {
        try {
            if (ProgressDialogUtils::loadingDialog.isInitialized && loadingDialog.isShowing) {
                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}