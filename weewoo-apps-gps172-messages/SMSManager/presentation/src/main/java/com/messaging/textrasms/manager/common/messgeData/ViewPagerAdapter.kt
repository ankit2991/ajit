package com.messaging.textrasms.manager.common.messgeData

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import javax.inject.Inject

class ViewPagerAdapter @Inject constructor(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {
    private val mFragmentList: MutableList<Fragment> = ArrayList()

    private val mFragmentTitleList: ArrayList<String> = ArrayList()

    override fun getItem(position: Int): Fragment {
        return mFragmentList.get(position)

    }


    override fun getCount(): Int {
        return mFragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }


    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitleList.get(position)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}
