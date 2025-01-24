package com.messaging.textrasms.manager.feature.conversationinfo.injection

import com.messaging.textrasms.manager.feature.conversationinfo.ConversationInfoController
import com.messaging.textrasms.manager.injection.scope.ControllerScope
import dagger.Subcomponent

@ControllerScope
@Subcomponent(modules = [ConversationInfoModule::class])
interface ConversationInfoComponent {

    fun inject(controller: ConversationInfoController)

    @Subcomponent.Builder
    interface Builder {
        fun conversationInfoModule(module: ConversationInfoModule): Builder
        fun build(): ConversationInfoComponent
    }

}