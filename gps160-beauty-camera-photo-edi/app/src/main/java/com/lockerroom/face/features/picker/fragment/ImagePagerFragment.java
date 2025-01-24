package com.lockerroom.face.features.picker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.lockerroom.face.features.picker.adapter.PhotoPagerAdapter;
import com.lockerroom.face.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImagePagerFragment extends Fragment {
    public static final String ARG_CURRENT_ITEM = "ARG_CURRENT_ITEM";
    public static final String ARG_PATH = "PATHS";
    private int currentItem = 0;
    private PhotoPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private ArrayList<String> paths;

    public static ImagePagerFragment newInstance(List<String> list, int i) {
        ImagePagerFragment imagePagerFragment = new ImagePagerFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray(ARG_PATH, (String[]) list.toArray(new String[list.size()]));
        bundle.putInt(ARG_CURRENT_ITEM, i);
        imagePagerFragment.setArguments(bundle);
        return imagePagerFragment;
    }

    public void onResume() {
        super.onResume();
    }

    public void setPhotos(List<String> list, int i) {
        this.paths.clear();
        this.paths.addAll(list);
        this.currentItem = i;
        this.mViewPager.setCurrentItem(i);
        this.mViewPager.getAdapter().notifyDataSetChanged();
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.paths = new ArrayList<>();
        Bundle arguments = getArguments();
        if (arguments != null) {
            String[] stringArray = arguments.getStringArray(ARG_PATH);
            this.paths.clear();
            if (stringArray != null) {
                this.paths = new ArrayList<>(Arrays.asList(stringArray));
            }
            this.currentItem = arguments.getInt(ARG_CURRENT_ITEM);
        }
        this.mPagerAdapter = new PhotoPagerAdapter(Glide.with((Fragment) this), this.paths);
    }

    @Nullable
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.__picker_picker_fragment_image_pager, viewGroup, false);
        this.mViewPager = inflate.findViewById(R.id.vp_photos);
        this.mViewPager.setAdapter(this.mPagerAdapter);
        this.mViewPager.setCurrentItem(this.currentItem);
        this.mViewPager.setOffscreenPageLimit(5);
        return inflate;
    }


    public ArrayList<String> getPaths() {
        return this.paths;
    }


    public void onDestroy() {
        super.onDestroy();
        this.paths.clear();
        this.paths = null;
        if (this.mViewPager != null) {
            this.mViewPager.setAdapter((PagerAdapter) null);
        }
    }
}
