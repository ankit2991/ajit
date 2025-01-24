package com.android.gpslocation.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.android.gpslocation.R

import com.kotlinpermissions.KotlinPermissions

import timber.log.Timber



object PermissionUtils {

    val locationPerms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    fun hasLocationPermissions(context: Context): Boolean = hasPermissions(context, *locationPerms)


    fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (context != null && !permissions.isEmpty()) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }

}

fun Activity.requestAllPermissions(context: Context,onSuccess: ((permissionName:String) -> Unit),onDenied: (() -> Unit), vararg allPerms: String) {
    KotlinPermissions.with(context as FragmentActivity) // where this is an FragmentActivity instance
        .permissions(*allPerms)
        .onAccepted { permissions ->
            permissions.forEach {
                Timber.w("onAccepted  $it")
                onSuccess.invoke(it)
            }
        }
        .onDenied { permissions ->
            permissions.forEach {
                Timber.w("onDenied  $it")
                onDenied.invoke()
            }
            Toast.makeText(applicationContext,getString(R.string.permisson_denied),Toast.LENGTH_LONG).show()
            //   requestAllPermissions(onSuccess, *allPerms)

        }
        .onForeverDenied { permissions ->
            permissions.forEach {
                Timber.w("onForeverDenied  $it")
            }
            openSettings()

        }
        .ask()
}

fun Activity.requestAllPermissions( onSuccess: ((permissionList:List<String>) -> Unit), onDenied: (() -> Unit), vararg allPerms: String) {
    KotlinPermissions.with(this as FragmentActivity) // where this is an FragmentActivity instance
        .permissions(*allPerms)
        .onAccepted { permissions ->
            Timber.w("onAccepted  $permissions")
            onSuccess.invoke(permissions)
        }
        .onDenied { permissions ->
            permissions.forEach {
                Timber.w("onDenied  $it")
                onDenied.invoke()
            }
            Toast.makeText(applicationContext,getString(R.string.permisson_denied),Toast.LENGTH_LONG).show()
            //   requestAllPermissions(onSuccess, *allPerms)

        }
        .onForeverDenied { permissions ->
            permissions.forEach {
                Timber.w("onForeverDenied  $it")
            }
            openSettings()

        }
        .ask()
}

fun Activity.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivityForResult(intent, 101)
}