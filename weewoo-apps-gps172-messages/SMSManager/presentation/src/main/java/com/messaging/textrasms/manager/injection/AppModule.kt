package com.messaging.textrasms.manager.injection

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModelProvider
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.messaging.textrasms.manager.blocking.BlockingClient
import com.messaging.textrasms.manager.blocking.BlockingManager
import com.messaging.textrasms.manager.common.util.MyNotificationManagerImpl
import com.messaging.textrasms.manager.common.util.ShortcutManagerImpl
import com.messaging.textrasms.manager.common.util.ViewModelFactory
import com.messaging.textrasms.manager.feature.conversationinfo.injection.ConversationInfoComponent
import com.messaging.textrasms.manager.feature.themepicker.injection.ThemePickerComponent
import com.messaging.textrasms.manager.listener.ContactAddedListener
import com.messaging.textrasms.manager.listener.ContactAddedListenerImpl
import com.messaging.textrasms.manager.manager.*
import com.messaging.textrasms.manager.mapper.*
import com.messaging.textrasms.manager.repository.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module(
    subcomponents = [
        ConversationInfoComponent::class,
        ThemePickerComponent::class]
)
class AppModule(private var application: Application) {

    @Provides
    @Singleton
    fun provideContext(): Context = application


    @Provides
    fun provideContentResolver(context: Context): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun provideRxPreferences(preferences: SharedPreferences): RxSharedPreferences {
        return RxSharedPreferences.create(preferences)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    fun provideViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory = factory

    @Provides
    fun provideContactAddedListener(listener: ContactAddedListenerImpl): ContactAddedListener =
        listener

    @Provides
    fun provideActiveConversationManager(manager: ActiveConversationManagerImpl): ActiveConversationManager =
        manager

    @Provides
    fun provideAlarmManager(manager: AlarmManagerImpl): AlarmManager = manager

    @Provides
    fun blockingClient(manager: BlockingManager): BlockingClient = manager

    @Provides
    fun provideKeyManager(manager: KeyManagerImpl): KeyManager = manager

    @Provides
    fun provideNotificationsManager(manager: MyNotificationManagerImpl): MyNotificationManager =
        manager

    @Provides
    fun providePermissionsManager(manager: PermissionManagerImpl): PermissionManager = manager

    @Provides
    fun provideShortcutManager(manager: ShortcutManagerImpl): ShortcutManager = manager

    @Provides
    fun provideWidgetManager(manager: WidgetManagerImpl): WidgetManager = manager

    @Provides
    fun provideCursorToContact(mapper: CursorToContactImpl): CursorToContact = mapper


    @Provides
    fun provideCursorToContactGroup(mapper: CursorToContactGroupImpl): CursorToContactGroup = mapper

    @Provides
    fun provideCursorToContactGroupMember(mapper: CursorToContactGroupMemberImpl): CursorToContactGroupMember =
        mapper

    @Provides
    fun provideCursorToConversation(mapper: CursorToConversationImpl): CursorToConversation = mapper


    @Provides
    fun provideCursorToMessage(mapper: CursorToMessageImpl): CursorToMessage = mapper


    @Provides
    fun provideCursorToPart(mapper: CursorToPartImpl): CursorToPart = mapper


    @Provides
    fun provideCursorToRecipient(mapper: CursorToRecipientImpl): CursorToRecipient = mapper


    @Provides
    fun provideBackupRepository(repository: BackupRepositoryImpl): BackupRepository = repository

    @Provides
    fun provideBlockingRepository(repository: BlockingRepositoryImpl): BlockingRepository =
        repository

    @Provides
    fun provideContactRepository(repository: ContactRepositoryImpl): ContactRepository = repository


    @Provides
    fun provideConversationRepository(repository: ConversationRepositoryImpl): ConversationRepository =
        repository


    @Provides
    fun provideMessageRepository(repository: MessageRepositoryImpl): MessageRepository = repository


    @Provides
    fun provideScheduledMessagesRepository(repository: ScheduledMessageRepositoryImpl): ScheduledMessageRepository =
        repository

    @Provides
    fun provideSyncRepository(repository: SyncRepositoryImpl): SyncRepository = repository


}