package com.zaeem.mytvcast.Utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zaeem.mytvcast.ConnectDeviceActivity
import com.zaeem.mytvcast.R


fun <T> AppCompatActivity.startActivityClearTask(clazz: Class<T>) {

    startActivity(Intent(this, clazz).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    )
    slideIn()
}

fun <T> AppCompatActivity.startActivityDefault(clazz: Class<T>) {

    startActivity(
        Intent(this, clazz)
    )
    slideIn()

}

fun <T> AppCompatActivity.startActivityBundle(clazz: Class<T>, bundle: Bundle) {

    startActivity(
        Intent(this, clazz).apply { putExtras(bundle) }
    )
    slideIn()

}

fun <T> AppCompatActivity.startActivityParams(clazz: Class<T>, addParams: (Intent) -> Unit) {

    startActivity(
        Intent(this, clazz).apply { addParams(this) }
    )
    slideIn()

}

fun <T> AppCompatActivity.startActivityParamsClearTask(
    clazz: Class<T>,
    addParams: (Intent) -> Unit
) {

    startActivity(
        Intent(this, clazz).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            addParams(this)
        }
    )
    slideIn()

}


fun <T> Fragment.startActivityClearTask(clazz: Class<T>) {

    startActivity(Intent(requireContext(), clazz).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    )
    slideIn()
}

fun <T> Fragment.startActivityDefault(clazz: Class<T>) {

    startActivity(
        Intent(requireContext(), clazz)
    )
    slideIn()

}

fun <T> Fragment.startActivityBundle(clazz: Class<T>, bundle: Bundle) {

    startActivity(
        Intent(requireContext(), clazz).apply { putExtras(bundle) }
    )
    slideIn()

}

fun <T> Fragment.startActivityParams(clazz: Class<T>, addParams: (Intent) -> Unit) {

    startActivity(
        Intent(requireContext(), clazz).apply { addParams(this) }
    )
    slideIn()

}

fun <T> Fragment.startActivityParamsClearTask(
    clazz: Class<T>,
    addParams: (Intent) -> Unit
) {

    startActivity(
        Intent(requireContext(), clazz).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            addParams(this)
        }
    )
    slideIn()

}


fun AppCompatActivity.slideIn() {
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

fun AppCompatActivity.slideOut() {
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}


fun Fragment.slideIn() {
    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

fun Fragment.slideOut() {
    activity?.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}


fun <T> Context.startActivityClearTask(clazz: Class<T>) {

    startActivity(Intent(this, clazz).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    )
}

fun <T> Context.startActivityDefault(clazz: Class<T>) {

    startActivity(
        Intent(this, clazz)
    )

}

fun <T> Context.startActivityBundle(clazz: Class<T>, bundle: Bundle) {

    startActivity(
        Intent(this, clazz).apply { putExtras(bundle) }
    )

}

fun <T> Context.startActivityParams(clazz: Class<T>, addParams: (Intent) -> Unit) {

    startActivity(
        Intent(this, clazz).apply { addParams(this) }
    )

}

fun <T> Context.startActivityParamsClearTask(
    clazz: Class<T>,
    addParams: (Intent) -> Unit
) {

    startActivity(
        Intent(this, clazz).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            addParams(this)
        }
    )

}


fun Fragment.noDeviceConnected() {
    MaterialAlertDialogBuilder(
        requireContext()
    )
        .setTitle("Error!")
        .setMessage("No Roku Device Connected")
        .setNeutralButton("Connect Roku device") { dialog, which ->
            // Respond to neutral button press

            dialog.dismiss()
            dialog.cancel()
            startActivityDefault(ConnectDeviceActivity::class.java)

        }
        .setPositiveButton("Ok") { dialog, which ->
            // Respond to positive button press

        }
        .show()
}
