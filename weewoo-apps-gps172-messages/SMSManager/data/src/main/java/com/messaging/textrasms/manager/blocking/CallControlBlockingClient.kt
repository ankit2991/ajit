package com.messaging.textrasms.manager.blocking

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.core.database.getStringOrNull
import com.callcontrol.datashare.CallControl
import com.messaging.textrasms.manager.extensions.map
import com.messaging.textrasms.manager.util.isInstalled
import com.messaging.textrasms.manager.util.tryOrNull
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class CallControlBlockingClient @Inject constructor(
    private val context: Context
) : BlockingClient {

    private val projection: Array<String> = arrayOf(
        CallControl.Lookup.BLOCK_REASON
    )

    class LookupResult(cursor: Cursor) {
        val blockReason: String? = cursor.getStringOrNull(0)
    }

    override fun isAvailable(): Boolean =
        context.isInstalled("com.flexaspect.android.everycallcontrol")

    override fun getClientCapability() = BlockingClient.Capability.BLOCK_WITH_PERMISSION

    override fun getAction(address: String): Single<BlockingClient.Action> = Single.fromCallable {
        val uri = Uri.withAppendedPath(CallControl.LOOKUP_TEXT_URI, address)
        val blockReason = tryOrNull {
            context.contentResolver.query(uri, projection, null, null, null)
                ?.use { cursor -> cursor.map(::LookupResult) }
                ?.find { result -> result.blockReason != null }
        }?.blockReason

        when (blockReason) {
            null -> BlockingClient.Action.Unblock
            else -> BlockingClient.Action.Block(blockReason)
        }
    }

    override fun block(addresses: List<String>): Completable = Completable.fromCallable {
        val reports = addresses.map { CallControl.Report(it) }
        val reportsArrayList = arrayListOf<CallControl.Report>().apply { addAll(reports) }
        CallControl.addRule(context, reportsArrayList, Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    override fun unblock(addresses: List<String>): Completable = Completable.fromCallable {
        val reports = addresses.map { CallControl.Report(it, null, false) }
        val reportsArrayList = arrayListOf<CallControl.Report>().apply { addAll(reports) }
        CallControl.addRule(context, reportsArrayList, Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    override fun openSettings() {
        CallControl.openBlockedList(context, Intent.FLAG_ACTIVITY_NEW_TASK)
    }


}
