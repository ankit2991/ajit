package com.messaging.textrasms.manager.injection

import com.calldorado.ui.views.custom.CalldoradoCustomView
import com.messaging.textrasms.manager.calldorado.AftercallCustomView
import com.messaging.textrasms.manager.common.base.QKApplication
import com.messaging.textrasms.manager.common.base.QkDialog
import com.messaging.textrasms.manager.common.util.QkChooserTargetService
import com.messaging.textrasms.manager.common.widget.*
import com.messaging.textrasms.manager.feature.backup.BackupController
import com.messaging.textrasms.manager.feature.blocking.messages.BlockedMessagesController
import com.messaging.textrasms.manager.feature.compose.editing.DetailedChipView
import com.messaging.textrasms.manager.feature.conversationinfo.injection.ConversationInfoComponent
import com.messaging.textrasms.manager.feature.settings.SettingsController
import com.messaging.textrasms.manager.feature.settings.swipe.SwipeActionsController
import com.messaging.textrasms.manager.feature.themepicker.injection.ThemePickerComponent
import com.messaging.textrasms.manager.feature.widget.WidgetAdapter
import com.messaging.textrasms.manager.injection.android.ActivityBuilderModule
import com.messaging.textrasms.manager.injection.android.BroadcastReceiverBuilderModule
import com.messaging.textrasms.manager.injection.android.FragmentModule
import com.messaging.textrasms.manager.injection.android.ServiceBuilderModule
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        ActivityBuilderModule::class,
        FragmentModule::class,
        BroadcastReceiverBuilderModule::class,
        ServiceBuilderModule::class]
)
interface AppComponent {

    fun conversationInfoBuilder(): ConversationInfoComponent.Builder
    fun themePickerBuilder(): ThemePickerComponent.Builder

    fun inject(application: QKApplication)

    fun inject(controller: BlockedMessagesController)
    fun inject(controller: BackupController)
    fun inject(controller: SettingsController)
    fun inject(controller: SwipeActionsController)

    fun inject(dialog: QkDialog)

    fun inject(service: WidgetAdapter)

    fun inject(service: QkChooserTargetService)

    fun inject(view: AvatarViewThemeColor)
    fun inject(view: AvatarView)
    fun inject(view: DetailedChipView)
    fun inject(view: PagerTitleView)
    fun inject(view: PreferenceView)
    fun inject(view: RadioPreferenceView)
    fun inject(view: QkEditText)
    fun inject(view: QkSwitch)

    fun inject(view: AftercallCustomView)
    fun inject(view: QkTextView)

}
