package com.translate.languagetranslator.freetranslation.utils

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import com.code4rox.adsmanager.TinyDB
import com.translate.languagetranslator.freetranslation.R


object RateUs {

    fun showDialog(mContext: Context, callBackClick: CallBackClick) {
        val dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.rate_us_dialog)
        val width = (mContext.resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (mContext.resources.displayMetrics.heightPixels * 0.70).toInt()
        dialog.window?.setLayout(width, height)
        val btnRate = dialog.findViewById(R.id.btnRateUs) as Button
        val imgCross = dialog.findViewById(R.id.imgCross) as ImageView
        callBackClick.onload()
        btnRate.setOnClickListener {
            rateUs(mContext)
            dialog.dismiss()
            TinyDB.getInstance(mContext)
                .putBoolean(com.translate.languagetranslator.freetranslation.appUtils.Constants.RATEUS_FIRST_COMPLETE,true)
//           callBackClick.onCancel(dialog)
            callBackClick.onPositive()
        }
        imgCross.setOnClickListener {
            dialog.dismiss()
            callBackClick.onNegitive()
        }
        dialog.show()

    }

    private fun rateUs(context: Context) {
//        val uri = Uri.parse("market://details?id=" + context.packageName)
        val uri = Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)
                )
            )
        }
    }

    interface CallBackClick {
        fun onPositive()
        fun onNegitive()
        fun onload()
    }
}