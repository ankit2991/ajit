package com.messaging.textrasms.manager.feature.themepicker

import android.animation.ObjectAnimator
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkController
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.extensions.dpToPx
import com.messaging.textrasms.manager.common.util.extensions.setBackgroundTint
import com.messaging.textrasms.manager.common.util.extensions.setVisible
import com.messaging.textrasms.manager.feature.themepicker.injection.ThemePickerModule
import com.messaging.textrasms.manager.injection.appComponent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.theme_picker_controller.*
import kotlinx.android.synthetic.main.theme_picker_hsv.*
import javax.inject.Inject

class ThemePickerController(
    val recipientId: Long = 0L
) : QkController<ThemePickerView, ThemePickerState, ThemePickerPresenter>(), ThemePickerView {

    @Inject
    override lateinit var presenter: ThemePickerPresenter

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var themeAdapter: ThemeAdapter

    @Inject
    lateinit var themePagerAdapter: ThemePagerAdapter

    private val viewQksmsPlusSubject: Subject<Unit> = PublishSubject.create()

    init {
        appComponent
            .themePickerBuilder()
            .themePickerModule(ThemePickerModule(this))
            .build()
            .inject(this)

        layoutRes = R.layout.theme_picker_controller
    }

    override fun onViewCreated() {
        pager.offscreenPageLimit = 1
        pager.adapter = themePagerAdapter
        tabs.pager = pager

        themeAdapter.data = colors.materialColors

        materialColors.layoutManager = LinearLayoutManager(activity)
        materialColors.adapter = themeAdapter
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.title_theme)
        showBackButton(true)

        themedActivity?.supportActionBar?.let { toolbar ->
            ObjectAnimator.ofFloat(toolbar, "elevation", toolbar.elevation, 0f).start()
        }
    }

    override fun onDetach(view: View) {
        super.onDetach(view)

        themedActivity?.supportActionBar?.let { toolbar ->
            ObjectAnimator.ofFloat(
                toolbar,
                "elevation",
                toolbar.elevation,
                8.dpToPx(toolbar.themedContext).toFloat()
            ).start()
        }
    }

    override fun showQksmsPlusSnackbar() {
        Snackbar.make(contentView, R.string.toast_sms_plus, Snackbar.LENGTH_LONG).run {
            setAction(R.string.button_more) { viewQksmsPlusSubject.onNext(Unit) }
            setActionTextColor(colors.theme().theme)
            show()
        }
    }

    override fun themeSelected(): Observable<Int> = themeAdapter.colorSelected

    override fun hsvThemeSelected(): Observable<Int> = picker.selectedColor

    override fun clearHsvThemeClicks(): Observable<*> = clear.clicks()

    override fun applyHsvThemeClicks(): Observable<*> = apply.clicks()

    override fun viewQksmsPlusClicks(): Observable<*> = viewQksmsPlusSubject

    override fun render(state: ThemePickerState) {
        tabs.setRecipientId(state.recipientId)

        hex.setText(Integer.toHexString(state.newColor).takeLast(6))

        applyGroup.setVisible(state.applyThemeVisible)
        apply.setBackgroundTint(state.newColor)
        apply.setTextColor(state.newTextColor)
    }

    override fun setCurrentTheme(color: Int) {
        picker.setColor(color)
        themeAdapter.selectedColor = color
    }

}