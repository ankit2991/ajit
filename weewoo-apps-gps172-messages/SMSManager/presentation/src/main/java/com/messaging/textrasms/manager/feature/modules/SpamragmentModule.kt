package com.messaging.textrasms.manager.feature.modules

import androidx.lifecycle.ViewModel
import com.messaging.textrasms.manager.feature.models.SpamModel
import com.messaging.textrasms.manager.injection.ViewModelKey
import com.messaging.textrasms.manager.injection.scope.FragmentScope
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.disposables.CompositeDisposable

@Module
class SpamragmentModule {
    //
    @Provides
    @FragmentScope
    fun provideCompositeDiposableLifecycle(): CompositeDisposable = CompositeDisposable()

    @Provides
    @IntoMap
    @ViewModelKey(SpamModel::class)
    fun providespamViewModel(viewModel: SpamModel): ViewModel = viewModel

}