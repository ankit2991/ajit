package com.messaging.textrasms.manager.feature.modules

import androidx.lifecycle.ViewModel
import com.messaging.textrasms.manager.feature.models.SearchViewModel
import com.messaging.textrasms.manager.injection.ViewModelKey
import com.messaging.textrasms.manager.injection.scope.ActivityScope
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.disposables.CompositeDisposable

@Module
class SearchActivityModule {

    @Provides
    @ActivityScope
    fun provideCompositeDiposableLifecycle(): CompositeDisposable = CompositeDisposable()

    @Provides
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    fun provideMainViewModel(viewModel: SearchViewModel): ViewModel = viewModel

}