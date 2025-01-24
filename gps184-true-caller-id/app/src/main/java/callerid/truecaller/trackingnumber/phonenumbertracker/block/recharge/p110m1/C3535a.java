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


public class C3535a extends Fragment {

    public C3541g f15185Y;

    public RecyclerView f15186Z;

    public RelativeLayout f15187a0;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        RelativeLayout relativeLayout;
        int i = 0;
        View inflate = layoutInflater.inflate(R.layout.fragment_topup, viewGroup, false);
        this.f15186Z = (RecyclerView) inflate.findViewById(R.id.recyle_Recharge);
        this.f15187a0 = (RelativeLayout) inflate.findViewById(R.id.relNoData);
        Collections.sort(RechargePlanActivity.f2248C);
        this.f15186Z.setLayoutManager(new LinearLayoutManager(getContext()));
        this.f15185Y = new C3541g(RechargePlanActivity.f2248C);
        if (RechargePlanActivity.f2248C.size() > 0) {
            this.f15186Z.setAdapter(this.f15185Y);
            relativeLayout = this.f15187a0;
            i = 8;
        } else {
            relativeLayout = this.f15187a0;
        }
        relativeLayout.setVisibility(i);
        return inflate;
    }
}
