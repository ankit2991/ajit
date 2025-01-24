package com.messaging.textrasms.manager.feature.modules

import com.messaging.textrasms.manager.feature.Activities.SplashActivity
import com.messaging.textrasms.manager.injection.scope.ActivityScope
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Named

@Module
class SplashActivityModule {

    @Provides
    @Named("threadId")
    fun provideThreadId(activity: SplashActivity): Long =
        activity.intent.extras?.getLong("threadId")
            ?: 0L

    @Provides
    @ActivityScope
    fun provideCompositeDiposableLifecycle(): CompositeDisposable = CompositeDisposable()


}