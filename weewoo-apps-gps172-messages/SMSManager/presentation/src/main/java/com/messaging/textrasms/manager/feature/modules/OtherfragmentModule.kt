package com.messaging.textrasms.manager.feature.modules

import androidx.lifecycle.ViewModel
import com.messaging.textrasms.manager.feature.models.OtherModel
import com.messaging.textrasms.manager.injection.ViewModelKey
import com.messaging.textrasms.manager.injection.scope.FragmentScope
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.disposables.CompositeDisposable

@Module
class OtherfragmentModule {
    //
    @Provides
    @FragmentScope
    fun provideCompositeDiposableLifecycle(): CompositeDisposable = CompositeDisposable()

    @Provides
    @IntoMap
    @ViewModelKey(OtherModel::class)
    fun provideopotherViewModel(viewModel: OtherModel): ViewModel = viewModel

}