package com.translate.languagetranslator.freetranslation.phrasebook.phrasebookTranslation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.translate.languagetranslator.freetranslation.R

class PhrasebookTranslation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phrasebook_translation)

        var manager: FragmentManager=supportFragmentManager
        val transaction: FragmentTransaction = manager.beginTransaction()
        transaction.add(R.id.main_framelayout, PhrasebookDetailFragmet(), "d")
        transaction.addToBackStack(null)
        transaction.commit()
    }
}