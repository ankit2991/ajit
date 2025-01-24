package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1.C3535a;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1.C3536b;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1.C3537c;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1.C3538d;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1.Frag_TopUp;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public SimpleFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new Frag_TopUp();
        } else if (position == 1) {
            return new C3538d();
        } else if (position == 2) {
            return new C3537c();
        } else if (position == 3) {
            return new C3536b();
        } else {
            return new C3535a();
        }
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Top up";
            case 1:
                return "Special Recharge";
            case 2:
                return "Roaming";
            case 3:
                return "Full Talktime";
            case 4:
                return "2G/3G/4G Data";
            default:
                return null;
        }
    }

}