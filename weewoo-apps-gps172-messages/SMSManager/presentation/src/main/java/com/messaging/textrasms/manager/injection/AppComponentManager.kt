package com.messaging.textrasms.manager.injection

import android.app.Application

internal lateinit var appComponent: AppComponent
    private set

internal object AppComponentManager {

    fun init(application: Application) {
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(application))
            .build()
    }

}