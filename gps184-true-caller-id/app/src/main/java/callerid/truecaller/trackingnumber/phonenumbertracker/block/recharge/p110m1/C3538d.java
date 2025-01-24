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
import callerid.truecaller.trackingnumber.phonenumbertracker.block.recharge.RechargePlanActivity;

import java.util.Collections;

public class C3538d extends Fragment {

    public C3541g f15194Y;
    public RecyclerView f15195Z;
    public RelativeLayout f15196a0;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        RelativeLayout relativeLayout;
        int i = 0;
        View inflate = layoutInflater.inflate(R.layout.fragment_topup, viewGroup, false);
        this.f15195Z = (RecyclerView) inflate.findViewById(R.id.recyle_Recharge);
        this.f15196a0 = (RelativeLayout) inflate.findViewById(R.id.relNoData);
        this.f15195Z.setLayoutManager(new LinearLayoutManager(getContext()));
        Collections.sort(RechargePlanActivity.f2250E);
        this.f15194Y = new C3541g(RechargePlanActivity.f2250E);
        if (RechargePlanActivity.f2250E.size() > 0) {
            this.f15195Z.setAdapter(this.f15194Y);
            relativeLayout = this.f15196a0;
            i = 8;
        } else {
            relativeLayout = this.f15196a0;
        }
        relativeLayout.setVisibility(i);
        return inflate;
    }
}
