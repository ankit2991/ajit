package com.messaging.textrasms.manager.feature.settings

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.jakewharton.rxbinding2.view.clicks
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkController
import com.messaging.textrasms.manager.common.base.QkDialog
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.MenuItem
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.common.util.extensions.animateLayoutChanges
import com.messaging.textrasms.manager.common.widget.PreferenceView
import com.messaging.textrasms.manager.feature.Activities.MainActivity.Companion.ifsettingsync
import com.messaging.textrasms.manager.injection.appComponent
import com.messaging.textrasms.manager.repository.SyncRepository
import com.messaging.textrasms.manager.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.Observable
import kotlinx.android.synthetic.main.settings_controller.*
import javax.inject.Inject

class SettingsController : QkController<SettingsView, SettingsState, SettingsPresenter>(),
    SettingsView {
    override fun LanguageClicked(): Observable<Int> = languageDialog.adapter.menuItemClicks

    override fun showLanguage() {
        languageDialog.show(activity!!)
    }

    override fun showdialog() {

    }

    override fun loaddata(): Observable<Boolean> {
        TODO("not implemented")
    }

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var colors: Colors

    @Inject
    lateinit var nightModeDialog: QkDialog

    @Inject
    lateinit var textSizeDialog: QkDialog

    @Inject
    lateinit var mmsSizeDialog: QkDialog

    @Inject
    override lateinit var presenter: SettingsPresenter

    lateinit var mDialogView: View
    lateinit var mAlertDialog: AlertDialog

    @Inject
    lateinit var languageDialog: QkDialog


    private val progressAnimator by lazy { ObjectAnimator.ofInt(syncingProgress, "progress", 0, 0) }

    init {
        appComponent.inject(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH
        layoutRes = R.layout.settings_controller

        colors.themeObservable()
            .autoDispose(scope())
            .subscribe { activity?.recreate() }
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated() {
        preferences.postDelayed({ preferences?.animateLayoutChanges = true }, 100)

        when (Build.VERSION.SDK_INT >= 29) {
            true -> nightModeDialog.adapter.setData(R.array.night_modes)
            false -> nightModeDialog.adapter.data =
                context.resources.getStringArray(R.array.night_modes)
                    .mapIndexed { index, title -> MenuItem(title, index) }
                    .drop(1)
        }
        textSizeDialog.adapter.setData(R.array.text_sizes)
        mmsSizeDialog.adapter.setData(R.array.mms_sizes, R.array.mms_sizes_ids)
        languageDialog.adapter.setData(R.array.Language_name)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedViewState: Bundle?
    ): View {


        mDialogView = LayoutInflater.from(activity).inflate(R.layout.loader, null)

        val mBuilder = AlertDialog.Builder(activity).setView(mDialogView)

        mAlertDialog = mBuilder.create()
        mAlertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        mAlertDialog.setCancelable(false)

        return super.onCreateView(inflater, container,savedViewState)
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.bindIntents(this)
        setTitle(R.string.title_settings)
        showBackButton(true)
    }

    override fun preferenceClicks(): Observable<PreferenceView> = (0 until preferences.childCount)
        .map { index -> preferences.getChildAt(index) }
        .mapNotNull { view -> view as? PreferenceView }

        .map { preference -> preference.clicks().map { preference } }
        .let { preferences -> Observable.merge(preferences) }

    override fun nightModeSelected(): Observable<Int> = nightModeDialog.adapter.menuItemClicks

    override fun textSizeSelected(): Observable<Int> = textSizeDialog.adapter.menuItemClicks

    override fun mmsSizeSelected(): Observable<Int> = mmsSizeDialog.adapter.menuItemClicks


    override fun render(state: SettingsState) {

        night.summary = state.nightModeSummary
        nightModeDialog.adapter.selectedItem = state.nightModeId

        textSize.summary = state.textSizeSummary
        textSizeDialog.adapter.selectedItem = state.textSizeId
        logDebug("fontt" + state.textSizeSummary)
        mmsSize.summary = state.maxMmsSizeSummary
        mmsSizeDialog.adapter.selectedItem = state.maxMmsSizeId

//        language.summary = state.language
//        languageDialog.adapter.selectedItem = state.LanguageId

        syncingProgress?.progressTintList =
            ColorStateList.valueOf(context.resources.getColor(R.color.tools_theme))
        syncingProgress?.indeterminateTintList =
            ColorStateList.valueOf(context.resources.getColor(R.color.tools_theme))

        when (state.syncProgress) {
            is SyncRepository.SyncProgress.Idle -> {
                if (Preferences.getBoolean(context, "syncing")) {
                    Toast.makeText(context, "Syncing done.", Toast.LENGTH_SHORT).show()
                    Preferences.setBoolean(context, Preferences.SYNCING, false)
                    ifsettingsync = false

                }
                syncingProgress.isVisible = false
                ifsettingsync = false

            }

            is SyncRepository.SyncProgress.Running -> {
                ifsettingsync = true
                syncingProgress.isVisible = true
                syncingProgress.max = state.syncProgress.max
                progressAnimator.apply {
                    setIntValues(
                        syncingProgress.progress,
                        state.syncProgress.progress
                    )
                }.start()
                syncingProgress.isIndeterminate = state.syncProgress.indeterminate
            }

            else -> {}
        }
        privacy_policy.setOnClickListener {
            navigator.showPrivacyPolicy()
        }
        rateus.setOnClickListener {
            navigator.showRateUsPlayStore()
        }
        invite.setOnClickListener {
            navigator.showInvite()
        }
        if (activity != null) {
            try {
                val pInfo: PackageInfo =
                    activity!!.packageManager.getPackageInfo(activity!!.packageName, 0)
                val version1: String = pInfo.versionName
                version?.text = "V ($version1)"
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    override fun showNightModeDialog() = nightModeDialog.show(activity!!)

    override fun showTextSizePicker() = textSizeDialog.show(activity!!)

    override fun showMmsSizePicker() = mmsSizeDialog.show(activity!!)
    internal var mDebugLog = true
    internal var mDebugTag = "IabHelper"
    internal fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }


}