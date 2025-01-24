package com.messaging.textrasms.manager.interactor

import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.messaging.textrasms.manager.util.NightModeManager
import com.messaging.textrasms.manager.util.Preferences
import io.reactivex.Flowable
import javax.inject.Inject

class MigratePreferences @Inject constructor(
    private val nightModeManager: NightModeManager,
    private val prefs: Preferences,
    private val rxPrefs: RxSharedPreferences
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> {
        return Flowable.fromCallable { rxPrefs.getBoolean("pref_key_welcome_seen", false) }
            .filter { seen -> seen.get() } // Only proceed if this value is true. It will be set false at the end
            .doOnNext {
                val defaultTheme = prefs.theme().get().toString()
                val oldTheme = rxPrefs.getString("pref_key_theme", defaultTheme).get()
                prefs.theme().set(Integer.parseInt(oldTheme))

                val background = rxPrefs.getString("pref_key_background", "light").get()
                val autoNight = rxPrefs.getBoolean("pref_key_night_auto", false).get()
                when {
                    autoNight -> nightModeManager.updateNightMode(Preferences.NIGHT_MODE_AUTO)
                    background == "light" -> nightModeManager.updateNightMode(Preferences.NIGHT_MODE_OFF)
                    background == "grey" -> nightModeManager.updateNightMode(Preferences.NIGHT_MODE_ON)
                    background == "black" -> {
                        nightModeManager.updateNightMode(Preferences.NIGHT_MODE_ON)
                        prefs.black.set(true)
                    }
                }

                prefs.delivery.set(
                    rxPrefs.getBoolean("pref_key_delivery", prefs.delivery.get()).get()
                )

                prefs.qkreply.set(
                    rxPrefs.getBoolean(
                        "pref_key_quickreply_enabled",
                        prefs.qkreply.get()
                    ).get()
                )
                prefs.qkreplyTapDismiss.set(
                    rxPrefs.getBoolean(
                        "pref_key_quickreply_dismiss",
                        prefs.qkreplyTapDismiss.get()
                    ).get()
                )

                prefs.textSize.set(
                    rxPrefs.getString(
                        "pref_key_font_size",
                        "${prefs.textSize.get()}"
                    ).get().toInt()
                )

                prefs.unicode.set(
                    rxPrefs.getBoolean("pref_key_strip_unicode", prefs.unicode.get()).get()
                )
            }
            .doOnNext { seen -> seen.delete() }
    }

}