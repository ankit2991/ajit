package com.messaging.textrasms.manager.feature.themepicker

import com.f2prateek.rx.preferences2.Preference
import com.messaging.textrasms.manager.common.base.QkPresenter
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.manager.WidgetManager
import com.messaging.textrasms.manager.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.withLatestFrom
import javax.inject.Inject
import javax.inject.Named

class ThemePickerPresenter @Inject constructor(
    prefs: Preferences,
    @Named("recipientId") private val recipientId: Long,
    private val colors: Colors,
    private val widgetManager: WidgetManager
) : QkPresenter<ThemePickerView, ThemePickerState>(ThemePickerState(recipientId = recipientId)) {

    private val theme: Preference<Int> = prefs.theme(recipientId)

    override fun bindIntents(view: ThemePickerView) {
        super.bindIntents(view)

        theme.asObservable()
            .autoDispose(view.scope())
            .subscribe { color -> view.setCurrentTheme(color) }

        view.themeSelected()
            .autoDispose(view.scope())
            .subscribe { color ->
                theme.set(color)
                if (recipientId == 0L) {
                    widgetManager.updateTheme()
                }
            }

        view.hsvThemeSelected()
            .doOnNext { color -> newState { copy(newColor = color) } }
            .map { color -> colors.textPrimaryOnThemeForColor(color) }
            .doOnNext { color -> newState { copy(newTextColor = color) } }
            .autoDispose(view.scope())
            .subscribe()

        Observables.combineLatest(
            theme.asObservable(),
            view.hsvThemeSelected()
        ) { old, new -> old != new }
            .autoDispose(view.scope())
            .subscribe { themeChanged -> newState { copy(applyThemeVisible = themeChanged) } }

        view.applyHsvThemeClicks()
            .withLatestFrom(view.hsvThemeSelected()) { _, color ->
                theme.set(color)
                if (recipientId == 0L) {
                    widgetManager.updateTheme()
                }
            }
            .autoDispose(view.scope())
            .subscribe()

        view.clearHsvThemeClicks()
            .withLatestFrom(theme.asObservable()) { _, color -> color }
            .autoDispose(view.scope())
            .subscribe { color -> view.setCurrentTheme(color) }
    }

}