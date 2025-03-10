package com.messaging.textrasms.manager.manager

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.provider.Telephony
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionManagerImpl @Inject constructor(private val context: Context) : PermissionManager {

    override fun isDefaultSms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
                ?.isRoleHeld(RoleManager.ROLE_SMS) == true
        } else {
            Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
        }
    }

    override fun hasLocation1(): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PERMISSION_GRANTED

    override fun hasLocation2(): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PERMISSION_GRANTED

    override fun hasReadSms(): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_SMS
    ) == PERMISSION_GRANTED

    override fun hasSendSms(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PERMISSION_GRANTED
    }

    override fun hasContacts(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PERMISSION_GRANTED
    }

    override fun hasPhone(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PERMISSION_GRANTED
    }

    override fun hasCalling(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PERMISSION_GRANTED
    }

    override fun hasStorage(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED
    }

}