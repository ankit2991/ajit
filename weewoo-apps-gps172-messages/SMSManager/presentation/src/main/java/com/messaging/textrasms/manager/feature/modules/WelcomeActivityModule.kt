package com.messaging.textrasms.manager.feature.modules

import androidx.lifecycle.ViewModel
import com.messaging.textrasms.manager.feature.Activities.MainActivity
import com.messaging.textrasms.manager.feature.Activities.WelcomeModel
import com.messaging.textrasms.manager.injection.ViewModelKey
import com.messaging.textrasms.manager.injection.scope.ActivityScope
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Named

@Module
class WelcomeActivityModule {

    @Provides
    @Named("threadId")
    fun provideThreadId(activity: MainActivity): Long = activity.intent.extras?.getLong("threadId")
        ?: 0L

    @Provides
    @ActivityScope
    fun provideCompositeDiposableLifecycle(): CompositeDisposable = CompositeDisposable()

    @Provides
    @IntoMap
    @ViewModelKey(WelcomeModel::class)
    fun provideMainViewModel(viewModel: WelcomeModel): ViewModel = viewModel


}