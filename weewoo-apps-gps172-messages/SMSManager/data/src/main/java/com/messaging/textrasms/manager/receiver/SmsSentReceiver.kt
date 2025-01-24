package com.messaging.textrasms.manager.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import com.messaging.textrasms.manager.interactor.MarkFailed
import com.messaging.textrasms.manager.interactor.MarkSent
import dagger.android.AndroidInjection
import javax.inject.Inject

class SmsSentReceiver : BroadcastReceiver() {

    @Inject
    lateinit var markSent: MarkSent

    @Inject
    lateinit var markFailed: MarkFailed

    companion object {
        var sent: Boolean = false
        var address: String = ""
    }

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val id = intent.getLongExtra("id", 0L)
        Log.d("TAG", "onReceive: " + intent.getStringExtra("add"))
        if (intent.getStringExtra("add") != null) {
            address = intent.getStringExtra("add")!!
        }

        when (resultCode) {
            Activity.RESULT_OK -> {
                sent = true
                executeMarkSentAsync(id)
            }

            else -> {
                sent = true
                executeMarkFailedAsync(address, id, resultCode)
            }
        }
    }

    private fun executeMarkSentAsync(id: Long) {
        MarkSentAsyncTask().execute(id)
    }

    private fun executeMarkFailedAsync(address: String, id: Long, resultCode: Int) {
        MarkFailedAsyncTask().execute(MarkFailed.Params(address, id, resultCode))
    }

    private inner class MarkSentAsyncTask : AsyncTask<Long, Void, Unit>() {
        override fun doInBackground(vararg params: Long?) {
            params.firstOrNull()?.let {
                markSent.execute(it)
            }
        }
    }

    private inner class MarkFailedAsyncTask : AsyncTask<MarkFailed.Params, Void, Unit>() {
        override fun doInBackground(vararg params: MarkFailed.Params?) {
            params.firstOrNull()?.let {
                markFailed.execute(it)
            }
        }
    }
}

//class SmsSentReceiver : BroadcastReceiver() {
//
//    @Inject
//    lateinit var markSent: MarkSent
//
//    @Inject
//    lateinit var markFailed: MarkFailed
//
//    companion object {
//        var sent: Boolean = false
//        var address: String = ""
//    }
//
//    override fun onReceive(context: Context, intent: Intent) {
//        AndroidInjection.inject(this, context)
//
//        val id = intent.getLongExtra("id", 0L)
//        Log.d("TAG", "onReceive: " + intent.getStringExtra("add"))
//        if (intent.getStringExtra("add") != null) {
//            address = intent.getStringExtra("add")!!
//        }
//
//        when (resultCode) {
//            Activity.RESULT_OK -> {
//                sent = true
//                val pendingResult = goAsync()
//                markSent.execute(id) { pendingResult.finish() }
//            }
//
//            else -> {
//                sent = true
//                val pendingResult = goAsync()
//                markFailed.execute(
//                    MarkFailed.Params(
//                        address,
//                        id,
//                        resultCode
//                    )
//                ) { pendingResult.finish() }
//            }
//        }
//    }
//
//
//}
