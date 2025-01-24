package com.translate.languagetranslator.freetranslation.activities.camera

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.translate.languagetranslator.freetranslation.activities.camera.fragments.OcrImageFragment
import com.translate.languagetranslator.freetranslation.activities.camera.fragments.OcrTextFragment

class OcrFragmentAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            OcrImageFragment()
        } else {
            OcrTextFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if (position == 0)
            "Image"
        else
            "Text"
    }
}