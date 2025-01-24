package com.messaging.textrasms.manager.blocking

import io.reactivex.Completable
import io.reactivex.Single

interface BlockingClient {

    enum class Capability {
        BLOCK_WITHOUT_PERMISSION,
        BLOCK_WITH_PERMISSION,
        CANT_BLOCK
    }

    sealed class Action {
        class Block(val reason: String? = null) : Action()
        object Unblock : Action()

        object DoNothing : Action()
    }

    fun isAvailable(): Boolean

    fun getClientCapability(): Capability

    fun getAction(address: String): Single<Action>

    fun block(addresses: List<String>): Completable

    fun unblock(addresses: List<String>): Completable

    fun openSettings()


}
