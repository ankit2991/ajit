package com.messaging.textrasms.manager.feature.settings

import com.messaging.textrasms.manager.common.base.QkViewContract
import com.messaging.textrasms.manager.common.widget.PreferenceView
import io.reactivex.Observable

interface SettingsView : QkViewContract<SettingsState> {
    fun preferenceClicks(): Observable<PreferenceView>
    fun nightModeSelected(): Observable<Int>
    fun textSizeSelected(): Observable<Int>
    fun mmsSizeSelected(): Observable<Int>
    fun LanguageClicked(): Observable<Int>

    fun showNightModeDialog()
    fun showTextSizePicker()
    fun showMmsSizePicker()
    fun loaddata(): Observable<Boolean>
    fun showdialog()
    fun showLanguage()
}
