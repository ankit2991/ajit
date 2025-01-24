package com.gpsnavigation.maps.gpsroutefinder.routemap.adapters

import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.MapFragment
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.RouteListFragment


class MainPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {


    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MapFragment()
            1 -> RouteListFragment()
            else -> RouteListFragment()
        }
    }

}
