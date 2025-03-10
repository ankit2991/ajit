package com.messaging.textrasms.manager.feature.themepicker.injection

import com.messaging.textrasms.manager.feature.themepicker.ThemePickerController
import com.messaging.textrasms.manager.injection.scope.ControllerScope
import dagger.Subcomponent

@ControllerScope
@Subcomponent(modules = [ThemePickerModule::class])
interface ThemePickerComponent {

    fun inject(controller: ThemePickerController)

    @Subcomponent.Builder
    interface Builder {
        fun themePickerModule(module: ThemePickerModule): Builder
        fun build(): ThemePickerComponent
    }

}