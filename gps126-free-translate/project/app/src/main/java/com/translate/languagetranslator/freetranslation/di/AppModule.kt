package com.translate.languagetranslator.freetranslation.di

import android.app.Application
import androidx.room.Room

import com.google.firebase.analytics.FirebaseAnalytics

import com.translate.languagetranslator.freetranslation.AppBase
import com.translate.languagetranslator.freetranslation.activities.camera.viewModel.CameraViewModel
import com.translate.languagetranslator.freetranslation.activities.mainScreen.viewModel.MainViewModel
import com.translate.languagetranslator.freetranslation.appUtils.TinyDB
import com.translate.languagetranslator.freetranslation.database.DataRepository
import com.translate.languagetranslator.freetranslation.di.RemoteConfigUtil
import com.translate.languagetranslator.freetranslation.viewmodels.*

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module


object AppModule {

    fun getMainModule(applicationClass: AppBase): Module {
        return module {

            single {
                RemoteConfigUtil.initializeConfigs()
            }

            single {
                DataRepository(applicationClass, get())
            }
            viewModel { SplashViewModel(get()) }
            viewModel { MainActivityViewModel(get()) }
            viewModel { MainViewModel(get()) }
            viewModel { CameraViewModel(get()) }
            viewModel { ConversationActivityViewModel(get()) }
            viewModel { DictionaryActivityViewModel(get()) }
            viewModel { LanguagesViewModel(get()) }
        }
    }


}