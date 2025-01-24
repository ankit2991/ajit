package com.messaging.textrasms.manager.receiver

import android.content.*
import com.messaging.textrasms.manager.data.R
import com.messaging.textrasms.manager.interactor.MarkRead
import com.messaging.textrasms.manager.util.makeToast
import dagger.android.AndroidInjection
import javax.inject.Inject

class CopyOTPReceiver : BroadcastReceiver() {

    @Inject
    lateinit var markRead: MarkRead

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        val otp = intent.getStringExtra("otp")
        val clipboard: ClipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("otp", otp)
        clipboard.setPrimaryClip(clip)
        context.makeToast(R.string.otp_copy)
        val pendingResult = goAsync()
        val threadId = intent.getLongExtra("threadId", 0)
        markRead.execute(listOf(threadId)) { pendingResult.finish() }
    }

}