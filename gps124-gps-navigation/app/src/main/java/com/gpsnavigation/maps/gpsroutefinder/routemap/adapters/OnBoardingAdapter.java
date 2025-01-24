package com.gpsnavigation.maps.gpsroutefinder.routemap.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.FirstScreenFragment;
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.SecondScreenFragment;
import com.gpsnavigation.maps.gpsroutefinder.routemap.fragments.ThirdScreenFragment;

public class OnBoardingAdapter extends FragmentStateAdapter {


    public OnBoardingAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 1:
                return new SecondScreenFragment();
            case 2:
                return new ThirdScreenFragment();
            default:
                return new FirstScreenFragment();
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        // Return the total number of fragments
        return 3; // Or the number of fragments you have
    }
}
