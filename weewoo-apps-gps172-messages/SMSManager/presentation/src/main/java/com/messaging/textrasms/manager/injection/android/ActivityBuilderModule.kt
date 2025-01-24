package com.messaging.textrasms.manager.injection.android

import com.messaging.textrasms.manager.FilterActivity
import com.messaging.textrasms.manager.feature.Activities.*
import com.messaging.textrasms.manager.feature.AddFilterActivity
import com.messaging.textrasms.manager.feature.backup.BackupActivity
import com.messaging.textrasms.manager.feature.blocking.BlockingActivity
import com.messaging.textrasms.manager.feature.compose.ComposeActivity
import com.messaging.textrasms.manager.feature.compose.ComposeActivityModule
import com.messaging.textrasms.manager.feature.contacts.ContactsActivity
import com.messaging.textrasms.manager.feature.contacts.ContactsActivityModule
import com.messaging.textrasms.manager.feature.conversationinfo.ConversationInfoActivity
import com.messaging.textrasms.manager.feature.gallery.GalleryActivity
import com.messaging.textrasms.manager.feature.gallery.GalleryActivityModule
import com.messaging.textrasms.manager.feature.groups.ContactsgroupActivity
import com.messaging.textrasms.manager.feature.groups.ContactsgroupActivityModule
import com.messaging.textrasms.manager.feature.inapppurchase.InAppPurchaseActivity
import com.messaging.textrasms.manager.feature.modules.*
import com.messaging.textrasms.manager.feature.notificationprefs.NotificationPrefsActivity
import com.messaging.textrasms.manager.feature.notificationprefs.NotificationPrefsActivityModule
import com.messaging.textrasms.manager.feature.qkreply.QkReplyActivity
import com.messaging.textrasms.manager.feature.qkreply.QkReplyActivityModule
import com.messaging.textrasms.manager.feature.scheduled.ScheduledActivity
import com.messaging.textrasms.manager.feature.scheduled.ScheduledActivityModule
import com.messaging.textrasms.manager.feature.settings.SettingsActivity
import com.messaging.textrasms.manager.feature.simplenotes.NoteActivity
import com.messaging.textrasms.manager.feature.simplenotes.NotesListActivity
import com.messaging.textrasms.manager.injection.scope.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {


    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [SplashActivityModule::class])
    abstract fun bindSplashActivity(): SplashActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [PurchaseActivityModule::class])
    abstract fun bindPurchaseActivity(): PurchaseActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [SearchActivityModule::class])
    abstract fun bindSearchActivity(): Searchactivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ArchiveActivityModule::class])
    abstract fun bindArchiveActivity(): Archiveactivity


    @ActivityScope
    @ContributesAndroidInjector(modules = [TransactionalActivityModule::class])
    abstract fun bindTransactionalActivity(): Transactionactivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [PromotionalActivityModule::class])
    abstract fun bindPromotionsActivity(): Promotionalactivity


    @ActivityScope
    @ContributesAndroidInjector(modules = [SpamragmentModule::class])
    abstract fun bindSpamActivity(): SpamActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBackupActivity(): BackupActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindCustomadActivity(): CustomAdLayoutActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindInAppPurchaseActivity(): InAppPurchaseActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindFilterActivity(): FilterActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindAddFilterActivity(): AddFilterActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindNoteListActivity(): NotesListActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindNoteActivity(): NoteActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ComposeActivityModule::class])
    abstract fun bindComposeActivity(): ComposeActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ContactsActivityModule::class])
    abstract fun bindContactsActivity(): ContactsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ContactsgroupActivityModule::class])
    abstract fun bindContactsgroupActivity(): ContactsgroupActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindConversationInfoActivity(): ConversationInfoActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [GalleryActivityModule::class])
    abstract fun bindGalleryActivity(): GalleryActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [NotificationPrefsActivityModule::class])
    abstract fun bindNotificationPrefsActivity(): NotificationPrefsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [QkReplyActivityModule::class])
    abstract fun bindQkReplyActivity(): QkReplyActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [ScheduledActivityModule::class])
    abstract fun bindScheduledActivity(): ScheduledActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindSettingsActivity(): SettingsActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [])
    abstract fun bindBlockingActivity(): BlockingActivity

}
