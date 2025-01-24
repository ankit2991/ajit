package com.translate.languagetranslator.freetranslation

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.play.core.review.ReviewManagerFactory
import com.translate.languagetranslator.freetranslation.appUtils.*
import com.translate.languagetranslator.freetranslation.appUtils.Constants.*
import com.translate.languagetranslator.freetranslation.databinding.DialogLoaderBinding
import java.util.*

fun AppCompatActivity.showRatingDialog() {
    val manager = ReviewManagerFactory.create(this)

    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            // We got the ReviewInfo object
            val reviewInfo = task.result
            val flow = manager.launchReviewFlow(this, reviewInfo)
            flow.addOnCompleteListener { _ ->

                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
            }
        } else {
            // There was some problem, log or handle the error code.
            Log.d("IN-APP-REVIEW", "showRatingDialog: " + task.exception?.message)
            //@ReviewErrorCode val reviewErrorCode = (task.getException() as TaskException).errorCode
        }
    }
}

/**
 * Sets user as premium
 * Saves buy time
 */
fun AppCompatActivity.setUserAsPremium(time: Long) {

    TinyDB.getInstance(this).putBoolean(IS_PREMIUM, true)
    TinyDB.getInstance(this).putLong(SUBSCRIPTION_ID_YEAR_BUY_TIME, time)
}

fun AppCompatActivity.handleAppOpenTimes() {
    val openTimes = getPrefInt(APP_OPEN_TIMES)

    //If its subscribed, 1st open after 1st subscription period renovation
    if (isPremium()) {

        putPrefInt(APP_OPEN_TIMES, openTimes + 1)
        val subscriptionBuyTime =
            getPrefLong(SUBSCRIPTION_ID_YEAR_BUY_TIME, System.currentTimeMillis());
        val calHoy = Calendar.getInstance();
        val calSubs = Calendar.getInstance()
        calSubs.timeInMillis = subscriptionBuyTime
        if (BuildConfig.DEBUG)
            calSubs.add(Calendar.MINUTE, 30)
        else
            calSubs.add(Calendar.YEAR, 1)


        val showedAfterFirstRenewal = getPrefBool(SHOWED_RATING_DIALOG_AFTER_FIRST_RENEWAL)
        if (!showedAfterFirstRenewal
            && calHoy.after(calSubs)
        ) {
            putPrefBoolean(SHOWED_RATING_DIALOG_AFTER_FIRST_RENEWAL, true)
            showRatingDialog()
        }


        //Also show rating dialog at 4th open having the yearly subscription active
        if (openTimes == YEARLY_SUBSCRIPTION_APP_OPEN_TIMES_TO_SHOW_RATING_DIALOG)
            showRatingDialog()


    }

}

fun AppCompatActivity.changeStatusBarColor(color: Int) {
    window.statusBarColor =
        ContextCompat.getColor(this, color) // set status background

}


var progressDialog: Dialog? = null

fun showProgressBlurDialog(activity: Activity) {

    if (progressDialog?.isShowing == true)
        return

    progressDialog = Dialog(activity, R.style.FullScreenDialog)

    val binding: DialogLoaderBinding = DataBindingUtil.inflate(
        LayoutInflater.from(activity),
        R.layout.dialog_loader,
        null,
        false
    )

    progressDialog?.setContentView(binding.root)
    progressDialog?.setCancelable(false)
    //  progressDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

    // Add a blur effect to the background of the dialog
    val blurredBitmap = blurBitmap(activity, activity.getScreenshotWithoutStatusBar())
    val blurredDrawable = BitmapDrawable(activity.resources, blurredBitmap)
    progressDialog?.window?.setBackgroundDrawable(blurredDrawable)
    //   progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    progressDialog?.window!!
        .setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    if (!activity.isFinishing) {
        //show dialog
        activity.runOnUiThread { progressDialog?.show() }
    }
}

fun dismissProgressDialog() {
    progressDialog?.dismiss()

}


fun Activity.getScreenshotWithoutStatusBar(): Bitmap {

    // Get the root view of your activity or fragment
    val rootView = window.decorView.rootView

// Get the drawing cache of the root view
    rootView.isDrawingCacheEnabled = true
    val drawingCache = rootView.drawingCache

// Get the height and width of the image without the status bar
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    val statusBarHeight = if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    val width = drawingCache.width
    val height = drawingCache.height - statusBarHeight

    // Create a Bitmap object from the drawing cache
    val bitmap = Bitmap.createBitmap(drawingCache, 0, statusBarHeight, width, height)

    // Disable the drawing cache of the root view
    rootView.isDrawingCacheEnabled = false

    return bitmap
}

fun blurBitmap(context: Context, bitmap: Bitmap): Bitmap {
    val renderScript = RenderScript.create(context)
    val input = Allocation.createFromBitmap(renderScript, bitmap)
    val output = Allocation.createTyped(renderScript, input.type)
    val script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    script.setInput(input)
    script.setRadius(25f)
    script.forEach(output)
    output.copyTo(bitmap)
    renderScript.destroy()
    return bitmap
}
