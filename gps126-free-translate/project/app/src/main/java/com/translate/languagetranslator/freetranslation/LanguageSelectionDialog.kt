package com.translate.languagetranslator.freetranslation

import android.content.Context
import androidx.fragment.app.DialogFragment

class LanguageSelectionDialog(context: Context) : DialogFragment() {

    companion object {
        private val instance: LanguageSelectionDialog? = null

        fun newInstance(context: Context?): LanguageSelectionDialog? {
            return instance ?: LanguageSelectionDialog(context!!)
        }
    }


}