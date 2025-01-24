package com.messaging.textrasms.manager.feature.modules

import com.messaging.textrasms.manager.feature.Activities.PurchaseActivity
import com.messaging.textrasms.manager.injection.scope.ActivityScope
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Named

@Module
class PurchaseActivityModule {

    @Provides
    @Named("threadId")
    fun provideThreadId(activity: PurchaseActivity): Long =
        activity.intent.extras?.getLong("threadId")
            ?: 0L

    @Provides
    @ActivityScope
    fun provideCompositeDiposableLifecycle(): CompositeDisposable = CompositeDisposable()


}