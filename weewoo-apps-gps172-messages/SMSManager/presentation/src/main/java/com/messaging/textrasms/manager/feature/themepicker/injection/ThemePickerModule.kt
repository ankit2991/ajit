package com.messaging.textrasms.manager.feature.themepicker.injection

import com.messaging.textrasms.manager.feature.themepicker.ThemePickerController
import com.messaging.textrasms.manager.injection.scope.ControllerScope
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ThemePickerModule(private val controller: ThemePickerController) {

    @Provides
    @ControllerScope
    @Named("recipientId")
    fun provideThreadId(): Long = controller.recipientId

}