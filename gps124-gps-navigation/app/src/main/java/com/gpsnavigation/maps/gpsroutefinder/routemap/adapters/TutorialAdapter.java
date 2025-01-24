package com.gpsnavigation.maps.gpsroutefinder.routemap.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.gpsnavigation.maps.gpsroutefinder.routemap.R;
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.FirstScreenFragment;
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.SecondScreenFragment;
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.ThirdScreenFragment;
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.TutorialFourthFragment;
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.TutorialFragment;

public class TutorialAdapter extends FragmentStateAdapter {


    public TutorialAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 1:
                return TutorialFragment.newInstance(R.layout.fragment_tutorial_second);
            case 2:
                return TutorialFragment.newInstance(R.layout.fragment_tutorial_third);
            case 3:
                return TutorialFourthFragment.newInstance();
            default:
                return TutorialFragment.newInstance(R.layout.fragment_tutorial_first);
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        // Return the total number of fragments
        return 4; // Or the number of fragments you have
    }
}
