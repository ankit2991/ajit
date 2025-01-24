package com.lockerroom.face.activities.onboardingScreen

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lockerroom.face.activities.onboardingScreen.onboarding_fragments.OnBoardingFragmentOne
import com.lockerroom.face.activities.onboardingScreen.onboarding_fragments.OnBoardingFragmentThree
import com.lockerroom.face.activities.onboardingScreen.onboarding_fragments.OnBoardingFragmentTwo

class OnBoardingAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {


    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            OnBoardingFragmentOne()
        } else if (position == 1) {
            OnBoardingFragmentTwo()
        } else {
            OnBoardingFragmentThree()
        }
    }
}