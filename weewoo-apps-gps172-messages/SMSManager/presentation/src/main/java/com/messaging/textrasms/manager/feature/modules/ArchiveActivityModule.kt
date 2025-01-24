package com.messaging.textrasms.manager.feature.modules

import androidx.lifecycle.ViewModel
import com.messaging.textrasms.manager.feature.models.ArchiveViewModel
import com.messaging.textrasms.manager.injection.ViewModelKey
import com.messaging.textrasms.manager.injection.scope.ActivityScope
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.disposables.CompositeDisposable

@Module
class ArchiveActivityModule {

    @Provides
    @ActivityScope
    fun provideCompositeDiposableLifecycle(): CompositeDisposable = CompositeDisposable()

    @Provides
    @IntoMap
    @ViewModelKey(ArchiveViewModel::class)
    fun provideMainViewModel(viewModel: ArchiveViewModel): ViewModel = viewModel

}