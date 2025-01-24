package com.messaging.textrasms.manager.feature.conversationinfo.injection

import com.messaging.textrasms.manager.feature.conversationinfo.ConversationInfoController
import com.messaging.textrasms.manager.injection.scope.ControllerScope
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ConversationInfoModule(private val controller: ConversationInfoController) {

    @Provides
    @ControllerScope
    @Named("threadId")
    fun provideThreadId(): Long = controller.threadId

}