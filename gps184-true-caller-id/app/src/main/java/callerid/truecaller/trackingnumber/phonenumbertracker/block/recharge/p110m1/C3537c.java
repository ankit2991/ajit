package callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.p110m1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.R;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.RechargePlanActivity;

import java.util.Collections;

public class C3537c extends Fragment {

    public C3541g f15191Y;

    public RecyclerView f15192Z;
    public RelativeLayout f15193a0;


    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        RelativeLayout relativeLayout;
        int i = 0;
        View inflate = layoutInflater.inflate(R.layout.fragment_topup, viewGroup, false);
        this.f15192Z = (RecyclerView) inflate.findViewById(R.id.recyle_Recharge);
        this.f15193a0 = (RelativeLayout) inflate.findViewById(R.id.relNoData);
        this.f15192Z.setLayoutManager(new LinearLayoutManager(getContext()));
        Collections.sort(RechargePlanActivity.f2249D);
        this.f15191Y = new C3541g(RechargePlanActivity.f2249D);
        if (RechargePlanActivity.f2249D.size() > 0) {
            this.f15192Z.setAdapter(this.f15191Y);
            relativeLayout = this.f15193a0;
            i = 8;
        } else {
            relativeLayout = this.f15193a0;
        }
        relativeLayout.setVisibility(i);
        return inflate;
    }
}
