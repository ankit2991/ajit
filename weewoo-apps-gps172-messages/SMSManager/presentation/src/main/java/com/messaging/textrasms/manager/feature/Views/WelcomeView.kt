package com.messaging.textrasms.manager.feature.Views

import com.messaging.textrasms.manager.common.base.QkView
import com.messaging.textrasms.manager.feature.states.MainState
import io.reactivex.Observable

interface WelcomeView : QkView<MainState> {

    val loadddata: Observable<Boolean>
    fun nightModeSelected(): Observable<Int>
    fun LanguageClicked(): Observable<Int>
    val activityResumedIntent: Observable<Boolean>
    fun requestDefaultSms()
    fun requestPermissions()
    fun requestStoragePermissions()
    fun clearSearch()
    fun clearSelection()
    fun themeChanged()

}
