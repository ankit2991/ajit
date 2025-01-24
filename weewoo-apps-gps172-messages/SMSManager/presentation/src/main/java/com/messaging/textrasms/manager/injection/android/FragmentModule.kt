package com.messaging.textrasms.manager.injection.android

import com.messaging.textrasms.manager.feature.fragments.other_fragment
import com.messaging.textrasms.manager.feature.fragments.personal_fragment
import com.messaging.textrasms.manager.feature.fragments.spam_fragment
import com.messaging.textrasms.manager.feature.modules.OtherfragmentModule
import com.messaging.textrasms.manager.feature.modules.PersonalfragmentModule
import com.messaging.textrasms.manager.feature.modules.UnknowntModule
import com.messaging.textrasms.manager.injection.scope.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [PersonalfragmentModule::class])
    abstract fun contributeMainFragment(): personal_fragment


    @FragmentScope
    @ContributesAndroidInjector(modules = [OtherfragmentModule::class])
    abstract fun contributeMainFragmentother(): other_fragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [UnknowntModule::class])
    abstract fun contributeMainFragmentspam(): spam_fragment

}