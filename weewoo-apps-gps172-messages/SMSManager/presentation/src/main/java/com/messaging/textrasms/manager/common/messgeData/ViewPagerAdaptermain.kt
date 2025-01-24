package com.messaging.textrasms.manager.common.messgeData

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.messaging.textrasms.manager.feature.fragments.other_fragment
import com.messaging.textrasms.manager.feature.fragments.personal_fragment
import com.messaging.textrasms.manager.feature.fragments.spam_fragment
import javax.inject.Inject

class ViewPagerAdaptermain @Inject constructor(
    fragmentManager: FragmentManager,
    private val personal_fragment: personal_fragment,
    private val secondFragmet: other_fragment,
    private val thirdFragmet: spam_fragment
) : FragmentPagerAdapter(fragmentManager) {
    private val mFragmentList: MutableList<Fragment> = ArrayList()

    private val mFragmentTitleList: ArrayList<String> = ArrayList()

    //    override fun getItem(position: Int): Fragment {
//        return mFragmentList.get(position)
//
//    }
    override fun getItem(position: Int): Fragment = when (position) {
        0 -> personal_fragment
        1 -> secondFragmet
        2 -> thirdFragmet
        else -> personal_fragment
    }

    override fun getCount(): Int {
        return 3
    }


    override fun getPageTitle(position: Int): CharSequence? {
        return "gfdssss"
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}
