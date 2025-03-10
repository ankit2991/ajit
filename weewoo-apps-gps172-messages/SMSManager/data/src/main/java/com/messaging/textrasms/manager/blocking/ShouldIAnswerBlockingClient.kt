package com.messaging.textrasms.manager.blocking

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.core.os.bundleOf
import com.messaging.textrasms.manager.util.isInstalled
import com.messaging.textrasms.manager.util.tryOrNull
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import javax.inject.Inject

class ShouldIAnswerBlockingClient @Inject constructor(
    private val context: Context
) : BlockingClient {

    companion object {
        const val RATING_UNKNOWN = 0
        const val RATING_POSITIVE = 1
        const val RATING_NEGATIVE = 2
        const val RATING_NEUTRAL = 3

        const val GET_NUMBER_RATING = 1
    }

    override fun isAvailable(): Boolean = listOf(
        "org.mistergroup.shouldianswer",
        "org.mistergroup.shouldianswerpersonal",
        "org.mistergroup.muzutozvednout"
    )
        .any(context::isInstalled)

    override fun getClientCapability() = BlockingClient.Capability.CANT_BLOCK

    override fun getAction(address: String): Single<BlockingClient.Action> {
        return Binder(context, address).isBlocked()
            .map { blocked ->
                when (blocked) {
                    true -> BlockingClient.Action.Block()
                    false -> BlockingClient.Action.DoNothing
                }
            }
    }

    override fun block(addresses: List<String>): Completable =
        Completable.fromCallable { openSettings() }

    override fun unblock(addresses: List<String>): Completable =
        Completable.fromCallable { openSettings() }

    override fun openSettings() {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage("org.mistergroup.shouldianswer")
            ?: pm.getLaunchIntentForPackage("org.mistergroup.shouldianswerpersonal")
            ?: pm.getLaunchIntentForPackage("org.mistergroup.muzutozvednout")

        intent?.run(context::startActivity)
    }

    private class Binder(
        private val context: Context,
        private val address: String
    ) : ServiceConnection {

        private val subject: SingleSubject<Boolean> = SingleSubject.create()
        private var serviceMessenger: Messenger? = null
        private var isBound: Boolean = false

        fun isBlocked(): Single<Boolean> {
            val intent: Intent? = tryOrNull(false) {
                context.packageManager.getApplicationInfo(
                    "org.mistergroup.shouldianswer",
                    0
                ).enabled
                Intent("org.mistergroup.shouldianswer.PublicService").setPackage("org.mistergroup.shouldianswer")
            } ?: tryOrNull(false) {
                context.packageManager.getApplicationInfo(
                    "org.mistergroup.shouldianswerpersonal",
                    0
                ).enabled
                Intent("org.mistergroup.shouldianswerpersonal.PublicService")
                    .setPackage("org.mistergroup.shouldianswerpersonal")
            } ?: tryOrNull(false) {
                context.packageManager.getApplicationInfo(
                    "org.mistergroup.muzutozvednout",
                    0
                ).enabled
                Intent("org.mistergroup.muzutozvednout.PublicService").setPackage("org.mistergroup.muzutozvednout")
            }

            if (intent != null) {
                context.bindService(intent, this, Context.BIND_AUTO_CREATE)
            } else {
                subject.onSuccess(false)
            }

            return subject
        }

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            serviceMessenger = Messenger(service)
            isBound = true

            val message = Message().apply {
                what = GET_NUMBER_RATING
                data = bundleOf("number" to address)
                replyTo = Messenger(IncomingHandler { response ->
                    subject.onSuccess(response.rating == RATING_NEGATIVE || response.wantBlock)

                    try {
                        if (isBound && serviceMessenger != null) {
                            context.unbindService(this@Binder)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                })
            }

            serviceMessenger?.send(message)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            serviceMessenger = null
            isBound = false
        }
    }

    private class IncomingHandler(private val callback: (response: Response) -> Unit) : Handler() {
        class Response(bundle: Bundle) {
            val rating: Int = bundle.getInt("rating")
            val wantBlock = bundle.getInt("wantBlock") == 1
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GET_NUMBER_RATING -> callback(Response(msg.data))
                else -> super.handleMessage(msg)
            }
        }
    }

}
