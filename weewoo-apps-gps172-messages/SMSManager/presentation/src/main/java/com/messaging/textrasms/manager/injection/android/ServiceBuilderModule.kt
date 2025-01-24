package com.messaging.textrasms.manager.injection.android

import com.messaging.textrasms.manager.feature.backup.RestoreBackupService
import com.messaging.textrasms.manager.injection.scope.ActivityScope
import com.messaging.textrasms.manager.receiver.MessageSyncService
import com.messaging.textrasms.manager.receiver.SendSmsReceiver
import com.messaging.textrasms.manager.service.HeadlessSmsSendService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindHeadlessSmsSendService(): HeadlessSmsSendService

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindRestoreBackupService(): RestoreBackupService

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindRestoreMessageService(): MessageSyncService

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun bindSendSmsReceiver(): SendSmsReceiver

}