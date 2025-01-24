package com.translate.languagetranslator.freetranslation.activities.onboarding

//import com.weewoo.sdkproject.events.EventHelper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.translate.languagetranslator.freetranslation.appUtils.RemoteConfigConstants
import com.translate.languagetranslator.freetranslation.appUtils.getRemoteConfig


class OnboardingViewPagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var onContinueClick: ((Int) -> Unit)? = null
    var isAdsOnly = false

    //  private val eventHelper = EventHelper()
    lateinit var list: ArrayList<Fragment>


    override fun getCount(): Int {
        return list.count()
    }


    public fun setFragments(lst: ArrayList<Fragment>) {
        list = lst
    }

    override fun getItem(position: Int): Fragment {

        //GPS126_01_ads_only


        (list.get(position) as OnboardingFragment).onClick = onContinueClick


        return list.get(position)

    }
}