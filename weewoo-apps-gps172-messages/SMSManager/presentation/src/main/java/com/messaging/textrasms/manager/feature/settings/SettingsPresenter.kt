package com.messaging.textrasms.manager.feature.settings

import android.content.Context
import android.content.Intent
import com.messaging.textrasms.manager.R
import com.messaging.textrasms.manager.common.base.QkPresenter
import com.messaging.textrasms.manager.common.util.BillingManager
import com.messaging.textrasms.manager.common.util.Colors
import com.messaging.textrasms.manager.common.util.DateFormatter
import com.messaging.textrasms.manager.common.util.Navigator
import com.messaging.textrasms.manager.feature.Activities.MainActivity
import com.messaging.textrasms.manager.interactor.SyncMessagessetting
import com.messaging.textrasms.manager.repository.SyncRepository
import com.messaging.textrasms.manager.util.NightModeManager
import com.messaging.textrasms.manager.util.Preferences
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.autoDispose
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SettingsPresenter @Inject constructor(
    colors: Colors,
    syncRepo: SyncRepository,
    private val context: Context,

    private val dateFormatter: DateFormatter,
    private val billingManager: BillingManager,
    private val navigator: Navigator,
    private val nightModeManager: NightModeManager,
    private val prefs: Preferences,
    private val syncMessages: SyncMessagessetting
) : QkPresenter<SettingsView, SettingsState>(
    SettingsState(
        nightModeId = prefs.nightMode.get()
    )
) {
    init {
        disposables += colors.themeObservable()
            .subscribe { theme -> newState { copy(theme = theme.theme) } }

        val nightModeLabels = context.resources.getStringArray(R.array.night_modes)
        disposables += prefs.nightMode.asObservable()
            .subscribe { nightMode ->
                newState {
                    copy(
                        nightModeSummary = nightModeLabels[nightMode],
                        nightModeId = nightMode
                    )
                }
            }

        disposables += prefs.nightStart.asObservable()
            .map { time -> nightModeManager.parseTime(time) }
            .map { calendar -> calendar.timeInMillis }
            .map { millis -> dateFormatter.getTimestamp(millis) }
            .subscribe { nightStart -> newState { copy(nightStart = nightStart) } }

        disposables += prefs.nightEnd.asObservable()
            .map { time -> nightModeManager.parseTime(time) }
            .map { calendar -> calendar.timeInMillis }
            .map { millis -> dateFormatter.getTimestamp(millis) }
            .subscribe { nightEnd -> newState { copy(nightEnd = nightEnd) } }

        disposables += prefs.black.asObservable()
            .subscribe { black -> newState { copy(black = black) } }

        disposables += prefs.notifications().asObservable()
            .subscribe { enabled -> newState { copy(notificationsEnabled = enabled) } }

        disposables += prefs.autoEmoji.asObservable()
            .subscribe { enabled -> newState { copy(autoEmojiEnabled = enabled) } }

        val delayedSendingLabels = context.resources.getStringArray(R.array.delayed_sending_labels)
        disposables += prefs.sendDelay.asObservable()
            .subscribe { id ->
                newState {
                    copy(
                        sendDelaySummary = delayedSendingLabels[id],
                        sendDelayId = id
                    )
                }
            }

        disposables += prefs.delivery.asObservable()
            .subscribe { enabled -> newState { copy(deliveryEnabled = enabled) } }

        disposables += prefs.signature.asObservable()
            .subscribe { signature -> newState { copy(signature = signature) } }

        val textSizeLabels = context.resources.getStringArray(R.array.text_sizes)
        disposables += prefs.textSize.asObservable()
            .subscribe { textSize ->
                newState { copy(textSizeSummary = textSizeLabels[textSize], textSizeId = textSize) }
            }

        val LanguageLabels = context.resources.getStringArray(R.array.Language_name)
        disposables += prefs.Language.asObservable()
            .subscribe { language ->
                newState { copy(language = LanguageLabels[language], LanguageId = language) }
            }

        disposables += prefs.autoColor.asObservable()
            .subscribe { autoColor -> newState { copy(autoColor = autoColor) } }

        disposables += prefs.systemFont.asObservable()
            .subscribe { enabled -> newState { copy(systemFontEnabled = enabled) } }

        disposables += prefs.unicode.asObservable()
            .subscribe { enabled -> newState { copy(stripUnicodeEnabled = enabled) } }

        disposables += prefs.mobileOnly.asObservable()
            .subscribe { enabled -> newState { copy(mobileOnly = enabled) } }

        disposables += prefs.longAsMms.asObservable()
            .subscribe { enabled -> newState { copy(longAsMms = enabled) } }

        val mmsSizeLabels = context.resources.getStringArray(R.array.mms_sizes)
        val mmsSizeIds = context.resources.getIntArray(R.array.mms_sizes_ids)
        disposables += prefs.mmsSize.asObservable()
            .subscribe { maxMmsSize ->
                val index = mmsSizeIds.indexOf(maxMmsSize)
                newState {
                    copy(
                        maxMmsSizeSummary = mmsSizeLabels[index],
                        maxMmsSizeId = maxMmsSize
                    )
                }
            }

        disposables += syncRepo.syncProgress
            .sample(16, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .subscribe { syncProgress -> newState { copy(syncProgress = syncProgress) } }

        disposables += syncMessages
    }

    override fun bindIntents(view: SettingsView) {
        super.bindIntents(view)
        view.preferenceClicks()
            .autoDispose(view.scope())
            .subscribe {
                Timber.v("Preference click: ${context.resources.getResourceName(it.id)}")

                when (it.id) {
                    R.id.night -> view.showNightModeDialog()

                    R.id.notifications -> navigator.showNotificationSettings()

                    R.id.textSize -> view.showTextSizePicker()

                    R.id.mmsSize -> view.showMmsSizePicker()

                    R.id.language -> view.showLanguage()

                    R.id.backup -> navigator.showBackup()

                    R.id.feedback -> navigator.showSupport()

                    R.id.sync -> {
                        MainActivity.ifsettingsync = true

                        syncMessages.execute(Unit)
                        Preferences.setBoolean(context, Preferences.SYNCING, true)
                    }
                }
            }

        view.nightModeSelected()
            .withLatestFrom(billingManager.upgradeStatus) { mode, upgraded ->
                nightModeManager.updateNightMode(mode)
            }
            .autoDispose(view.scope())
            .subscribe()

        view.textSizeSelected()
            .autoDispose(view.scope())
            .subscribe(prefs.textSize::set)

        view.mmsSizeSelected()
            .autoDispose(view.scope())
            .subscribe(prefs.mmsSize::set)


        view.LanguageClicked()
            .doOnNext {
                Preferences.setBoolean(context,Preferences.IS_LANG_SELECTED,true)
                val intent = Intent(context, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            .autoDispose(view.scope())
            .subscribe(prefs.Language::set)
    }

}